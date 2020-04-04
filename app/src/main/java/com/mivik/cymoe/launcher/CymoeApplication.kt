package com.mivik.cymoe.launcher

import android.app.Application
import android.content.Context
import com.mivik.cymoe.Cymoe

class CymoeApplication : Application() {
	override fun attachBaseContext(base: Context?) {
		Cymoe.unsealHiddenApi()
		Unsign.hook()
		ComponentNameHook.hook()
		ResourcesHook.hook()
		super.attachBaseContext(base)
	}
}