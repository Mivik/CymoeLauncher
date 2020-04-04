package com.mivik.cymoe.launcher

import android.content.Context
import android.content.ContextWrapper
import com.mivik.cymoe.CYTUS_PACKAGE_NAME
import com.mivik.cymoe.Cymoe

// 私怀疑这里的东西有些 redundant...
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