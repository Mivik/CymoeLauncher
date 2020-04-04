package com.mivik.cymoe

import android.content.Context

/**
 * 自用的 SharedPreferences。注意这里的字段在 Assembly 那边会用到，所以不要随便改名。
 */
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