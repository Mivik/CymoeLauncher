package com.mivik.cymoe

import android.content.Context

class CymoePreferences private constructor(context: Context) : KtPreferences(context, "cymoe") {

	companion object {
		private var instance: CymoePreferences? = null

		@JvmStatic
		fun getInstance(context: Context?): CymoePreferences {
			if (context != null && instance == null) instance = CymoePreferences(context)
			return instance!!
		}
	}

	var disableSplash by Value.boolean(true)
	var clickToFlick by Value.boolean(false)
	var dragToClick by Value.boolean(false)
	var autoPlay by Value.boolean(false)
	var disableTrueEnding by Value.boolean(false)
}