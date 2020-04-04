package com.mivik.cymoe

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// 一个简单的工具类，用于实现基于 kotlin property 的 SharedPreferences。
// 来源于 https://blog.csdn.net/Jokey_wz/article/details/82350759，做了一些小的修改。
open class KtPreferences(val preferences: SharedPreferences) {
	constructor(context: Context, name: String) : this(context.getSharedPreferences(name, Context.MODE_PRIVATE))

	protected object Value {
		fun int(defaultValue: Int = 0) = object : ReadWriteProperty<KtPreferences, Int> {
			override fun getValue(thisRef: KtPreferences, property: KProperty<*>): Int {
				return thisRef.preferences.getInt(property.name, defaultValue)
			}

			override fun setValue(thisRef: KtPreferences, property: KProperty<*>, value: Int) {
				thisRef.preferences.edit().putInt(property.name, value).apply()
			}
		}

		fun long(defaultValue: Long = 0L) = object : ReadWriteProperty<KtPreferences, Long> {
			override fun getValue(thisRef: KtPreferences, property: KProperty<*>): Long {
				return thisRef.preferences.getLong(property.name, defaultValue)
			}

			override fun setValue(thisRef: KtPreferences, property: KProperty<*>, value: Long) {
				thisRef.preferences.edit().putLong(property.name, value).apply()
			}
		}

		fun boolean(defaultValue: Boolean = false) = object : ReadWriteProperty<KtPreferences, Boolean> {
			override fun getValue(thisRef: KtPreferences, property: KProperty<*>): Boolean {
				return thisRef.preferences.getBoolean(property.name, defaultValue)
			}

			override fun setValue(thisRef: KtPreferences, property: KProperty<*>, value: Boolean) {
				thisRef.preferences.edit().putBoolean(property.name, value).apply()
			}
		}

		fun float(defaultValue: Float = 0.0f) = object : ReadWriteProperty<KtPreferences, Float> {
			override fun getValue(thisRef: KtPreferences, property: KProperty<*>): Float {
				return thisRef.preferences.getFloat(property.name, defaultValue)
			}

			override fun setValue(thisRef: KtPreferences, property: KProperty<*>, value: Float) {
				thisRef.preferences.edit().putFloat(property.name, value).apply()
			}
		}

		fun string(defaultValue: String? = null) = object : ReadWriteProperty<KtPreferences, String?> {
			override fun getValue(thisRef: KtPreferences, property: KProperty<*>): String? {
				return thisRef.preferences.getString(property.name, defaultValue)
			}

			override fun setValue(thisRef: KtPreferences, property: KProperty<*>, value: String?) {
				thisRef.preferences.edit().putString(property.name, value).apply()
			}
		}

		fun stringSet(defaultValue: Set<String>? = null) =
			object : ReadWriteProperty<KtPreferences, Set<String>?> {
				override fun getValue(thisRef: KtPreferences, property: KProperty<*>): Set<String>? {
					return thisRef.preferences.getStringSet(property.name, defaultValue)
				}

				override fun setValue(thisRef: KtPreferences, property: KProperty<*>, value: Set<String>?) {
					thisRef.preferences.edit().putStringSet(property.name, value).apply()
				}
			}
	}
}