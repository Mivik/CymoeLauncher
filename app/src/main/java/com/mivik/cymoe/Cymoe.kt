package com.mivik.cymoe

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import com.mivik.argon.C
import com.mivik.cymoe.launcher.CymoeInstrumentation
import com.mivik.cymoe.launcher.CymoeResourcesWrapper
import com.mivik.cymoe.launcher.Unsign
import dalvik.system.PathClassLoader
import java.io.IOException
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.security.MessageDigest
import kotlin.properties.Delegates


internal const val T = "Cymoe"

internal const val CYTUS_PACKAGE_NAME = "com.ilongyuan.cytus2.ly.TapTap"

internal const val ASSEMBLY_EXPECTED_MD5 = "178c4c083664e755406707b80231bd93"

fun CharSequence?.empty() = if (this == null) true else length == 0

val mainHandler by lazy {
	Handler(Looper.getMainLooper())
}

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

inline fun ui(crossinline func: () -> Unit) {
	if (Looper.getMainLooper() == Looper.myLooper()) func()
	else mainHandler.post { func() }
}

fun Context.toast(cs: CharSequence) = ui {
	Toast.makeText(this, cs, Toast.LENGTH_SHORT).show()
}

inline fun Context.alert(crossinline func: AlertDialog.Builder.() -> Unit) = ui {
	AlertDialog.Builder(this).apply(func).show()
}

fun Context.clip(cs: CharSequence) {
	val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	manager.setPrimaryClip(ClipData.newPlainText(null, cs))
}

fun Intent?.adjust(): Intent? {
	this ?: return null
	val comp = component
	if (comp != null) component = ComponentName(Cymoe.selfPackageName, comp.className)
	return this
}

fun dp2px(dp: Int) = (Resources.getSystem().displayMetrics.density * dp).toInt()

fun dp2px(dp: Float) = (Resources.getSystem().displayMetrics.density * dp).toInt()

fun Drawable?.tint(color: Int): Drawable? =
	if (this == null) null else DrawableCompat.wrap(this).mutate().apply { setTint(color) }

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

fun Context.displayError(t: Throwable, title: String = "错误") {
	alert {
		val str = Log.getStackTraceString(t)
		setTitle(title)
		setMessage(str)
		setPositiveButton("确定", null)
		setNegativeButton("复制") { _, _ -> clip(str) }
		setCancelable(true)
	}
}

fun mixColor(colorA: Int, colorB: Int): Int {
	var a: Int = Color.alpha(colorA)
	var r: Int = Color.red(colorA)
	var g: Int = Color.green(colorA)
	var b: Int = Color.blue(colorA)
	a += Color.alpha(colorB)
	r += Color.red(colorB)
	g += Color.green(colorB)
	b += Color.blue(colorB)
	return Color.argb(
		if (a > 255) 255 else a,
		if (r > 255) 255 else r,
		if (g > 255) 255 else g,
		if (b > 255) 255 else b
	)
}

fun lightColor(color: Int, light: Int): Int = mixColor(color, Color.rgb(light, light, light))

fun darkColor(color: Int, dark: Int): Int {
	var a: Int = Color.alpha(color)
	var r: Int = Color.red(color)
	var g: Int = Color.green(color)
	var b: Int = Color.blue(color)
	a -= dark
	r -= dark
	g -= dark
	b -= dark
	return Color.argb(if (a < 0) 0 else a, if (r < 0) 0 else r, if (g < 0) 0 else g, if (b < 0) 0 else b)
}

sealed class CymoeException : RuntimeException() {
	object CytusNotInstalled : CymoeException()
	class CytusAssemblyMismatch(val foundMD5: String) : CymoeException()
	class Unknown(val err: Throwable) : CymoeException()
}

object Cymoe {
	@JvmStatic
	external fun nativeInitialize(assetManager: AssetManager, apkPath: String)

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
		try {
			System.loadLibrary("cymoe")
			selfPackageName = context.packageName
			try {
				nativeInitialize(
					context.assets,
					context.packageManager.getApplicationInfo(
						CYTUS_PACKAGE_NAME,
						0
					).sourceDir
				)
				val packageManager = context.packageManager
				fakeApplicationInfo = packageManager.getApplicationInfo(
					CYTUS_PACKAGE_NAME, 0
				)
				fakePackageInfo = packageManager.getPackageInfo(
					CYTUS_PACKAGE_NAME, PackageManager.GET_SIGNATURES
				)
			} catch (e: PackageManager.NameNotFoundException) {
				throw CymoeException.CytusNotInstalled
			}
			apkPath = fakeApplicationInfo.sourceDir
			fakeAssetManager = AssetManager::class.java.newInstance()
			AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
				.invoke(
					fakeAssetManager,
					apkPath
				)
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
			fakeClassLoader = PathClassLoader(apkPath, fakeApplicationInfo.nativeLibraryDir, context.classLoader)
			mInstrumentation =
				CymoeInstrumentation
			setupEnvironment(context)
		} catch (e: CymoeException) {
			synchronized(this) {
				initialized = false
			}
			throw e
		}
	}

	@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
	private fun setupEnvironment(context: Context) {
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
	}

	fun launchCytus(context: Context) {
		context.startActivity(Intent(context, fakeClassLoader.loadClass("com.ilongyuan.cytus2.remaster.MainActivity")))
	}
}