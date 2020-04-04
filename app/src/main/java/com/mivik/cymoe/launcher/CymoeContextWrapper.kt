package com.mivik.cymoe.launcher

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import com.mivik.cymoe.CYTUS_PACKAGE_NAME
import com.mivik.cymoe.Cymoe
import com.mivik.cymoe.T
import com.mivik.cymoe.adjust

class CymoeContextWrapper(base: Context?) : ContextWrapper(base) {
	override fun getPackageName(): String {
		return CYTUS_PACKAGE_NAME
	}

	override fun attachBaseContext(base: Context?) {
		ContextWrapper::class.java.getDeclaredField("mBase").apply { isAccessible = true }.set(this, base)
	}

	override fun getClassLoader(): ClassLoader {
		return Cymoe.fakeClassLoader
	}
}