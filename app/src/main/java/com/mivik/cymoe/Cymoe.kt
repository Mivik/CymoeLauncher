package com.mivik.cymoe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mivik.argon.C
import com.mivik.cymoe.launcher.CymoeInstrumentation
import com.mivik.cymoe.launcher.CymoeResourcesWrapper
import com.mivik.cymoe.launcher.FloatingButtonHook
import com.mivik.cymoe.launcher.R
import dalvik.system.PathClassLoader
import java.io.IOException
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.security.MessageDigest
import kotlin.properties.Delegates

internal const val T = "Cymoe"

internal const val CYTUS_PACKAGE_NAME = "com.ilongyuan.cytus2.ly.TapTap"

internal const val CYTUS_MAIN_ACTIVITY_NAME = "com.ilongyuan.cytus2.remaster.MainActivity"

internal const val ASSEMBLY_EXPECTED_MD5 = "178c4c083664e755406707b80231bd93"

fun CharSequence?.empty() = if (this == null) true else length == 0

fun Class<*>.isCymoeClass(): Boolean = name.startsWith("com.mivik.cymoe.")

fun ByteArray.calcMD5(): ByteArray = MessageDigest.getInstance("md5").digest(this)

fun ByteArray.toHexString(): String {
	val hexString = "0123456789abcdef"
	val ret = StringBuffer(size * 2)
	for (i in 0 until size) {
		val v: Int = this[i].toInt() and 0xFF
		ret.append(hexString[v ushr 4])
		ret.append(hexString[v and 0x0F])
	}
	return ret.toString()
}

val requiredPermissions = arrayOf(
	Manifest.permission.READ_PHONE_STATE,
	Manifest.permission.READ_EXTERNAL_STORAGE,
	Manifest.permission.WRITE_EXTERNAL_STORAGE,
	Manifest.permission.WAKE_LOCK
)

var primaryColor by Delegates.notNull<Int>()

internal fun internalInit(context: Context) {
	val array = context.theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary))
	primaryColor = array.getColor(0, 0)
	CymoePreferences.getInstance(context)
}

sealed class CymoeException : RuntimeException() {
	object CytusNotInstalled : CymoeException()
	class CytusAssemblyMismatch(val foundMD5: String) : CymoeException()
	class Unknown(val err: Throwable) : CymoeException()
}

object Cymoe {
	@JvmStatic
	external fun nativeInitialize(assetManager: AssetManager, apkPath: String)

	// 解除 Android P 及以上的反射 API 限制
	fun unsealHiddenApi() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
		try {
			val forName =
				Class::class.java.getDeclaredMethod("forName", String::class.java)
			val getDeclaredMethod = Class::class.java.getDeclaredMethod(
				"getDeclaredMethod",
				String::class.java,
				java.lang.reflect.Array.newInstance(Class::class.java, 0).javaClass
			)
			val vmRuntimeClass =
				forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
			val getRuntime =
				getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
			val setHiddenApiExemptions = getDeclaredMethod.invoke(
				vmRuntimeClass, "setHiddenApiExemptions", arrayOf<Class<*>>(
					Array<String>::class.java
				)
			) as Method
			val sVmRuntime = getRuntime.invoke(null)
			setHiddenApiExemptions.invoke(sVmRuntime, arrayOf("L") as Any)
			Log.i(C.T, "Unsealed API")
		} catch (t: Throwable) {
			Log.e(C.T, "Failed to unseal API", t)
		}
	}

	lateinit var fakeApplicationInfo: ApplicationInfo
	lateinit var fakePackageInfo: PackageInfo
	lateinit var fakeAssetManager: AssetManager
	lateinit var fakeResources: Resources
	lateinit var fakeClassLoader: ClassLoader
	lateinit var mInstrumentation: Instrumentation
	lateinit var apkPath: String
	lateinit var selfPackageName: String

	private var initialized = false

	@SuppressLint("DiscouragedPrivateApi")
	fun init(context: Context) {
		synchronized(this) {
			if (initialized) return
			initialized = true
		}
		// 下面的工作都只需要做一遍
		try {
			// 1. 加载本地库
			System.loadLibrary("cymoe")

			// 2. 获取 Cytus II 的各种信息
			selfPackageName = context.packageName
			try {
				nativeInitialize(
					context.assets,
					context.packageManager.getApplicationInfo(CYTUS_PACKAGE_NAME, 0).sourceDir
				)
				val packageManager = context.packageManager
				fakeApplicationInfo = packageManager.getApplicationInfo(CYTUS_PACKAGE_NAME, 0)
				fakePackageInfo = packageManager.getPackageInfo(CYTUS_PACKAGE_NAME, PackageManager.GET_SIGNATURES)
			} catch (e: PackageManager.NameNotFoundException) {
				throw CymoeException.CytusNotInstalled
			}
			apkPath = fakeApplicationInfo.sourceDir

			// 3. 从 Cytus II 的安装包创建 AssetManager 和 Resources，同时验证 Assembly-CSharp.dll
			fakeAssetManager = AssetManager::class.java.newInstance()
			AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
				.invoke(fakeAssetManager, apkPath)
			try {
				val foundMD5 =
					fakeAssetManager.open("bin/Data/Managed/Assembly-CSharp.dll").use { it.readBytes().calcMD5() }
						.toHexString()
				if (foundMD5 != ASSEMBLY_EXPECTED_MD5) throw CymoeException.CytusAssemblyMismatch(
					foundMD5
				)
			} catch (e: IOException) {
				throw CymoeException.Unknown(e)
			}
			fakeResources =
				CymoeResourcesWrapper(
					context.resources,
					fakeAssetManager
				)

			// 4. 创建自己的 ClassLoader 和 Instrumentation，并注入到运行时
			fakeClassLoader = PathClassLoader(apkPath, fakeApplicationInfo.nativeLibraryDir, context.classLoader)
			mInstrumentation = CymoeInstrumentation
			inject(context)
		} catch (e: CymoeException) {
			synchronized(this) {
				initialized = false
			}
			throw e
		}
	}

	@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
	private fun inject(context: Context) {
		val classActivityThread = Class.forName("android.app.ActivityThread")
		val classLoadedApk = Class.forName("android.app.LoadedApk")
		val currentActivityThread = classActivityThread.getDeclaredMethod("currentActivityThread").invoke(null)
		classActivityThread.getDeclaredField("mInstrumentation").apply {
			isAccessible = true
			set(currentActivityThread, mInstrumentation)
		}
		val mPackages = classActivityThread.getDeclaredField("mPackages").apply { isAccessible = true }.get(
			currentActivityThread
		) as Map<*, *>
		val loadedApk = (mPackages[context.packageName] as WeakReference<*>).get()
		classLoadedApk.getDeclaredField("mClassLoader").apply { isAccessible = true }.set(
			loadedApk,
			fakeClassLoader
		)
		FloatingButtonHook.hook()
	}

	fun launchCytus(context: Context) {
		// 没错，就是这么简单
		context.startActivity(Intent(context, fakeClassLoader.loadClass(CYTUS_MAIN_ACTIVITY_NAME)))
	}
}