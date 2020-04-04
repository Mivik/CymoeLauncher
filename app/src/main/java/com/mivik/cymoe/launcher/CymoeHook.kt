package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.app.ContextImpl
import android.app.LoadedApk
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.IBinder
import android.os.UserHandle
import android.util.Log
import android.view.Display
import com.mivik.cymoe.CYTUS_PACKAGE_NAME
import com.mivik.cymoe.Cymoe
import com.mivik.cymoe.T
import lab.galaxy.yahfa.HookMain
import java.lang.reflect.*

/**
 * 简单的工具类，逐个 hook [targetMethods] 中的 Method/Constructor。
 * hook 函数名字应为 (原函数名)_new
 * backup 函数名字应为 (原函数名)_old
 *
 * @see HookMain
 */
abstract class CymoeHook {
	// 这里不用到 <out Member>，因为在高版本 Android API 上开发会把由方法和构造函数构建出的数组自动推断为 Executable 类
	// 而在 Android 低版本上是没有这个类的，因此在构造数组时会报错
	abstract val targetMethods: Array<Member>

	fun hook() {
		val clz = javaClass
		hookAll@ for (targetMethod in targetMethods) {
			val parameterTypes: Array<Class<*>>
			val name: String
			when (targetMethod) {
				is Method -> {
					parameterTypes =
						if (Modifier.isStatic(targetMethod.modifiers)) targetMethod.parameterTypes else arrayOf(
							targetMethod.declaringClass,
							*targetMethod.parameterTypes
						)
					name = targetMethod.name
				}
				is Constructor<*> -> {
					parameterTypes = arrayOf(
						targetMethod.declaringClass,
						*targetMethod.parameterTypes
					)
					name = targetMethod.declaringClass.simpleName
				}
				else -> continue@hookAll
			}
			val newMethod = clz.getDeclaredMethod(name + "_new", *parameterTypes)
			try {
				Log.e(T, "hooking $name")
				val oldMethod = clz.getDeclaredMethod(name + "_old", *parameterTypes)
				HookMain.backupAndHook(targetMethod, newMethod, oldMethod)
			} catch (e: NoSuchMethodException) {
				HookMain.hook(targetMethod, newMethod)
			}
		}
	}
}

/**
 * hook [ComponentName] 的构造方法，使所有原本包名为 Cytus 报名的 [ComponentName] 指向 Cymoe
 */
@SuppressLint("SoonBlockedPrivateApi")
object ComponentNameHook : CymoeHook() {
	override val targetMethods = run {
		val classComponentName = Class.forName("android.content.ComponentName")
		arrayOf<Member>(
			classComponentName.getConstructor(String::class.java, String::class.java),
			classComponentName.getConstructor(Context::class.java, String::class.java),
			classComponentName.getConstructor(Context::class.java, Class::class.java)
		)
	}

	@JvmStatic
	external fun ComponentName.ComponentName_old(packageName: String, className: String)

	@JvmStatic
	external fun ComponentName.ComponentName_old(context: Context, className: String)

	@JvmStatic
	external fun ComponentName.ComponentName_old(context: Context, cls: Class<*>)

	@JvmStatic
	fun ComponentName.ComponentName_new(packageName: String, className: String) =
		if (packageName == CYTUS_PACKAGE_NAME) ComponentName_old(Cymoe.selfPackageName, className)
		else ComponentName_old(packageName, className)

	@JvmStatic
	fun ComponentName.ComponentName_new(context: Context, className: String) =
		if (context.packageName == CYTUS_PACKAGE_NAME) ComponentName_old(
			Cymoe.selfPackageName, className
		)
		else ComponentName_old(context, className)

	@JvmStatic
	fun ComponentName.ComponentName_new(context: Context, cls: Class<*>) =
		if (context.packageName == CYTUS_PACKAGE_NAME) ComponentName_old(
			Cymoe.selfPackageName, cls.name
		)
		else ComponentName_old(context, cls)
}

/**
 * hook [Context] 的 [Resources] 对象，使之在被创建后不久（完全替换有些麻烦）就指向我们自己的 [CymoeResourcesWrapper]。
 *
 * @see CymoeResourcesWrapper
 */
object ResourcesHook : CymoeHook() {
	private lateinit var fieldMResources: Field

	override val targetMethods = run {
		val classContextImpl = Class.forName("android.app.ContextImpl")
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			arrayOf<Member>(
				classContextImpl.getDeclaredMethod("setResources", Class.forName("android.content.res.Resources"))
			)
		} else {
			fieldMResources = classContextImpl.getDeclaredField("mResources").apply { isAccessible = true }
			arrayOf<Member>(
				classContextImpl.getDeclaredConstructor(
					classContextImpl,
					ActivityThread::class.java,
					LoadedApk::class.java,
					IBinder::class.java,
					UserHandle::class.java,
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Int::class.java
					else Boolean::class.java,
					Display::class.java,
					Configuration::class.java,
					Int::class.java
				)
			)
			// TODO Implement compatibility for Android N below
		}
	}

	@JvmStatic
	external fun ContextImpl.setResources_old(resources: Resources)

	@JvmStatic
	fun ContextImpl.setResources_new(resources: Resources) {
		setResources_old(
			try {
				Cymoe.fakeResources
			} catch (ignored: UninitializedPropertyAccessException) {
				resources
			}
		)
	}

	@JvmStatic
	external fun ContextImpl.ContextImpl_old(
		container: ContextImpl?,
		mainThread: ActivityThread?,
		packageInfo: LoadedApk?,
		activityToken: IBinder?,
		user: UserHandle?,
		flags: Int,
		display: Display?,
		overrideConfiguration: Configuration?,
		createDisplayWithId: Int
	)

	@JvmStatic
	fun ContextImpl.ContextImpl_new(
		container: ContextImpl?,
		mainThread: ActivityThread?,
		packageInfo: LoadedApk?,
		activityToken: IBinder?,
		user: UserHandle?,
		flags: Int,
		display: Display?,
		overrideConfiguration: Configuration?,
		createDisplayWithId: Int
	) {
		ContextImpl_old(
			container,
			mainThread,
			packageInfo,
			activityToken,
			user,
			flags,
			display,
			overrideConfiguration,
			createDisplayWithId
		)
		try {
			fieldMResources.set(this, Cymoe.fakeResources)
		} catch (t: Throwable) {
		}
	}

	@JvmStatic
	external fun ContextImpl.ContextImpl_old(
		container: ContextImpl?,
		mainThread: ActivityThread?,
		packageInfo: LoadedApk?,
		activityToken: IBinder?,
		user: UserHandle?,
		restricted: Boolean,
		display: Display?,
		overrideConfiguration: Configuration?,
		createDisplayWithId: Int
	)

	@JvmStatic
	fun ContextImpl.ContextImpl_new(
		container: ContextImpl?,
		mainThread: ActivityThread?,
		packageInfo: LoadedApk?,
		activityToken: IBinder?,
		user: UserHandle?,
		restricted: Boolean,
		display: Display?,
		overrideConfiguration: Configuration?,
		createDisplayWithId: Int
	) {
		ContextImpl_old(
			container,
			mainThread,
			packageInfo,
			activityToken,
			user,
			restricted,
			display,
			overrideConfiguration,
			createDisplayWithId
		)
		try {
			fieldMResources.set(this, Cymoe.fakeResources)
		} catch (t: Throwable) {
		}
	}
}