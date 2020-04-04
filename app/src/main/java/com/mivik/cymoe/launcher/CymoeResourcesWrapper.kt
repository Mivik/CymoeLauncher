package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.content.res.*
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.RequiresApi
import com.mivik.cymoe.CYTUS_PACKAGE_NAME
import java.io.InputStream

class CymoeResourcesWrapper(
	val fallback: Resources,
	assets: AssetManager
) : Resources(assets, fallback.displayMetrics, fallback.configuration) {
	override fun getString(id: Int): String =
		try {
			super.getString(id)
		} catch (e: NotFoundException) {
			fallback.getString(id)
		}

	override fun getString(id: Int, vararg formatArgs: Any?): String =
		try {
			super.getString(id, *formatArgs)
		} catch (e: NotFoundException) {
			fallback.getString(id, *formatArgs)
		}

	override fun getStringArray(id: Int): Array<String> =
		try {
			super.getStringArray(id)
		} catch (e: NotFoundException) {
			fallback.getStringArray(id)
		}

	override fun getAnimation(id: Int): XmlResourceParser =
		try {
			super.getAnimation(id)
		} catch (e: NotFoundException) {
			fallback.getAnimation(id)
		}

	override fun getBoolean(id: Int): Boolean =
		try {
			super.getBoolean(id)
		} catch (e: NotFoundException) {
			fallback.getBoolean(id)
		}

	override fun getColor(id: Int): Int =
		try {
			super.getColor(id)
		} catch (e: NotFoundException) {
			fallback.getColor(id)
		}

	@RequiresApi(Build.VERSION_CODES.M)
	override fun getColor(id: Int, theme: Theme?): Int =
		try {
			super.getColor(id, theme)
		} catch (e: NotFoundException) {
			fallback.getColor(id, theme)
		}

	override fun getColorStateList(id: Int): ColorStateList =
		try {
			super.getColorStateList(id)
		} catch (e: NotFoundException) {
			fallback.getColorStateList(id)
		}

	@RequiresApi(Build.VERSION_CODES.M)
	override fun getColorStateList(id: Int, theme: Theme?): ColorStateList =
		try {
			super.getColorStateList(id, theme)
		} catch (e: NotFoundException) {
			fallback.getColorStateList(id, theme)
		}

	override fun getTextArray(id: Int): Array<CharSequence> =
		try {
			super.getTextArray(id)
		} catch (e: NotFoundException) {
			fallback.getTextArray(id)
		}

	@SuppressLint("Recycle")
	override fun obtainTypedArray(id: Int): TypedArray =
		try {
			super.obtainTypedArray(id)
		} catch (e: NotFoundException) {
			fallback.obtainTypedArray(id)
		}

	override fun getText(id: Int): CharSequence =
		try {
			super.getText(id)
		} catch (e: NotFoundException) {
			fallback.getText(id)
		}

	override fun getText(id: Int, def: CharSequence?): CharSequence? =
		try {
			super.getText(id)
		} catch (e: NotFoundException) {
			try {
				fallback.getText(id)
			} catch (_: NotFoundException) {
				def
			}
		}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? =
		try {
			super.getDrawableForDensity(id, density, theme)
		} catch (e: NotFoundException) {
			fallback.getDrawableForDensity(id, density, theme)
		}

	@SuppressLint("NewApi")
	override fun getFloat(id: Int): Float =
		try {
			super.getFloat(id)
		} catch (e: NotFoundException) {
			fallback.getFloat(id)
		}

	@SuppressLint("Recycle")
	override fun obtainAttributes(set: AttributeSet?, attrs: IntArray?): TypedArray =
		try {
			super.obtainAttributes(set, attrs)
		} catch (e: NotFoundException) {
			fallback.obtainAttributes(set, attrs)
		}

	override fun getDimensionPixelSize(id: Int): Int =
		try {
			super.getDimensionPixelSize(id)
		} catch (e: NotFoundException) {
			fallback.getDimensionPixelSize(id)
		}

	override fun getIntArray(id: Int): IntArray =
		try {
			super.getIntArray(id)
		} catch (e: NotFoundException) {
			fallback.getIntArray(id)
		}

	override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) =
		try {
			super.getValue(id, outValue, resolveRefs)
		} catch (e: NotFoundException) {
			fallback.getValue(id, outValue, resolveRefs)
		}

	override fun getValue(name: String?, outValue: TypedValue?, resolveRefs: Boolean) =
		try {
			super.getValue(name, outValue, resolveRefs)
		} catch (e: NotFoundException) {
			fallback.getValue(name, outValue, resolveRefs)
		}

	override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String =
		try {
			super.getQuantityString(id, quantity, *formatArgs)
		} catch (e: NotFoundException) {
			fallback.getQuantityString(id, quantity, *formatArgs)
		}

	override fun getQuantityString(id: Int, quantity: Int): String =
		try {
			super.getQuantityString(id, quantity)
		} catch (e: NotFoundException) {
			fallback.getQuantityString(id, quantity)
		}

	override fun getResourcePackageName(resid: Int): String =
		try {
			super.getResourcePackageName(resid)
		} catch (e: NotFoundException) {
			fallback.getResourcePackageName(resid)
		}

	override fun openRawResourceFd(id: Int): AssetFileDescriptor =
		try {
			super.openRawResourceFd(id)
		} catch (e: NotFoundException) {
			fallback.openRawResourceFd(id)
		}

	override fun getDimension(id: Int): Float =
		try {
			super.getDimension(id)
		} catch (e: NotFoundException) {
			fallback.getDimension(id)
		}

	override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int =
		try {
			// the only hack
			super.getIdentifier(name, defType, CYTUS_PACKAGE_NAME)
		} catch (e: NotFoundException) {
			fallback.getIdentifier(name, defType, defPackage)
		}

	override fun getQuantityText(id: Int, quantity: Int): CharSequence =
		try {
			super.getQuantityText(id, quantity)
		} catch (e: NotFoundException) {
			fallback.getQuantityText(id, quantity)
		}

	override fun openRawResource(id: Int, value: TypedValue?): InputStream =
		try {
			super.openRawResource(id, value)
		} catch (e: NotFoundException) {
			fallback.openRawResource(id, value)
		}

	override fun getInteger(id: Int): Int =
		try {
			super.getInteger(id)
		} catch (e: NotFoundException) {
			fallback.getInteger(id)
		}

	override fun getDrawable(id: Int): Drawable =
		try {
			super.getDrawable(id)
		} catch (e: NotFoundException) {
			fallback.getDrawable(id)
		}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	override fun getDrawable(id: Int, theme: Theme?): Drawable =
		try {
			super.getDrawable(id, theme)
		} catch (e: NotFoundException) {
			fallback.getDrawable(id, theme)
		}

	override fun getResourceTypeName(resid: Int): String =
		try {
			super.getResourceTypeName(resid)
		} catch (e: NotFoundException) {
			fallback.getResourceTypeName(resid)
		}

	override fun getLayout(id: Int): XmlResourceParser =
		try {
			super.getLayout(id)
		} catch (e: NotFoundException) {
			fallback.getLayout(id)
		}

	@RequiresApi(Build.VERSION_CODES.O)
	override fun getFont(id: Int): Typeface =
		try {
			super.getFont(id)
		} catch (e: NotFoundException) {
			fallback.getFont(id)
		}

	override fun getResourceName(resid: Int): String =
		try {
			super.getResourceName(resid)
		} catch (e: NotFoundException) {
			fallback.getResourceName(resid)
		}

	override fun getDimensionPixelOffset(id: Int): Int =
		try {
			super.getDimensionPixelOffset(id)
		} catch (e: NotFoundException) {
			fallback.getDimensionPixelOffset(id)
		}

	override fun getValueForDensity(id: Int, density: Int, outValue: TypedValue?, resolveRefs: Boolean) =
		try {
			super.getValueForDensity(id, density, outValue, resolveRefs)
		} catch (e: NotFoundException) {
			fallback.getValueForDensity(id, density, outValue, resolveRefs)
		}

	override fun getResourceEntryName(resid: Int): String =
		try {
			super.getResourceEntryName(resid)
		} catch (e: NotFoundException) {
			fallback.getResourceEntryName(resid)
		}

	override fun getFraction(id: Int, base: Int, pbase: Int): Float =
		try {
			super.getFraction(id, base, pbase)
		} catch (e: NotFoundException) {
			fallback.getFraction(id, base, pbase)
		}
}