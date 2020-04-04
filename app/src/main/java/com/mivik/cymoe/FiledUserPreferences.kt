package com.mivik.cymoe

import android.content.Context

class FiledUserPreferences private constructor(context: Context) : KtPreferences(context, "filed_user") {
	companion object {
		private var instance: FiledUserPreferences? = null

		fun getInstance(context: Context?): FiledUserPreferences {
			if (context != null && instance == null) instance = FiledUserPreferences(context)
			return instance!!
		}
	}

	var key_imei_local by Value.string("")
	var key_imei_local_sdk by Value.string("")
}