package com.mivik.cymoe

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat

fun mixColor(colorA: Int, colorB: Int): Int {
	var a: Int = Color.alpha(colorA)
	var r: Int = Color.red(colorA)
	var g: Int = Color.green(colorA)
	var b: Int = Color.blue(colorA)
	a += Color.alpha(colorB)
	r += Color.red(colorB)
	g += Color.green(colorB)
	b += Color.blue(colorB)
	return Color.argb(
		if (a > 255) 255 else a,
		if (r > 255) 255 else r,
		if (g > 255) 255 else g,
		if (b > 255) 255 else b
	)
}

fun lightColor(color: Int, light: Int): Int = mixColor(color, Color.rgb(light, light, light))

fun darkColor(color: Int, dark: Int): Int {
	var a: Int = Color.alpha(color)
	var r: Int = Color.red(color)
	var g: Int = Color.green(color)
	var b: Int = Color.blue(color)
	a -= dark
	r -= dark
	g -= dark
	b -= dark
	return Color.argb(if (a < 0) 0 else a, if (r < 0) 0 else r, if (g < 0) 0 else g, if (b < 0) 0 else b)
}

fun Context.displayError(t: Throwable, title: String = "错误") {
	alert {
		val str = Log.getStackTraceString(t)
		setTitle(title)
		setMessage(str)
		setPositiveButton("确定", null)
		setNegativeButton("复制") { _, _ -> clip(str) }
		setCancelable(true)
	}
}

fun dp2px(dp: Int) = (Resources.getSystem().displayMetrics.density * dp).toInt()

fun dp2px(dp: Float) = (Resources.getSystem().displayMetrics.density * dp).toInt()

fun Drawable?.tint(color: Int): Drawable? =
	if (this == null) null else DrawableCompat.wrap(this).mutate().apply { setTint(color) }

fun Context.toast(cs: CharSequence) = ui {
	Toast.makeText(this, cs, Toast.LENGTH_SHORT).show()
}

inline fun Context.alert(crossinline func: AlertDialog.Builder.() -> Unit) = ui {
	AlertDialog.Builder(this).apply(func).show()
}

fun Context.clip(cs: CharSequence) {
	val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	manager.setPrimaryClip(ClipData.newPlainText(null, cs))
}

val mainHandler by lazy {
	Handler(Looper.getMainLooper())
}

inline fun ui(crossinline func: () -> Unit) {
	if (Looper.getMainLooper() == Looper.myLooper()) func()
	else mainHandler.post { func() }
}