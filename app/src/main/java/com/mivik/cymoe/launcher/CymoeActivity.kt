package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("Registered")
open class CymoeActivity : AppCompatActivity() {
	override fun getResources(): Resources {
		val resources = super.getResources()
		return if (resources is CymoeResourcesWrapper) resources.fallback
		else resources
	}
}