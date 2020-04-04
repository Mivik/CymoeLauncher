package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.PersistableBundle
import com.mivik.cymoe.Cymoe
import com.mivik.cymoe.isCymoeClass
import java.util.*

object CymoeInstrumentation : Instrumentation() {
	val activityStack = Stack<Activity>()

	override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
		if (activity != null) injectActivityIfNeeded(activity)
		super.callActivityOnCreate(activity, icicle)
		activityStack.push(activity)
	}

	override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?, persistentState: PersistableBundle?) {
		if (activity != null) injectActivityIfNeeded(activity)
		super.callActivityOnCreate(activity, icicle, persistentState)
		activityStack.push(activity)
	}

	override fun callActivityOnDestroy(activity: Activity?) {
		super.callActivityOnDestroy(activity)
		activityStack.remove(activity)
	}

	/**
	 * 在 [Activity] 启动时注入我们自己的 [ContextWrapper]
	 *
	 * @see CymoeContextWrapper
	 */
	@SuppressLint("PrivateApi")
	private fun injectActivityIfNeeded(activity: Activity) {
		if (activity.javaClass.isCymoeClass()) return
		val fieldMBase = ContextWrapper::class.java.getDeclaredField("mBase").apply { isAccessible = true }
		val mBase = fieldMBase.get(activity) as Context?
		fieldMBase.set(activity, CymoeContextWrapper(mBase))
	}
}

val foregroundActivity: Activity
	get() = Cymoe.mInstrumentation.activityStack.peek()