package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.PersistableBundle
import com.mivik.cymoe.isCymoeClass

object CymoeInstrumentation : Instrumentation() {

	override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
		if (activity != null) injectActivityIfNeeded(activity)
		super.callActivityOnCreate(activity, icicle)
	}

	override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?, persistentState: PersistableBundle?) {
		if (activity != null) injectActivityIfNeeded(activity)
		super.callActivityOnCreate(activity, icicle, persistentState)
	}

	@SuppressLint("PrivateApi")
	private fun injectActivityIfNeeded(activity: Activity) {
		if (activity.javaClass.isCymoeClass()) return
		val fieldMBase = ContextWrapper::class.java.getDeclaredField("mBase").apply { isAccessible = true }
		val mBase = fieldMBase.get(activity) as Context?

		fieldMBase.set(activity, CymoeContextWrapper(mBase))
	}
}