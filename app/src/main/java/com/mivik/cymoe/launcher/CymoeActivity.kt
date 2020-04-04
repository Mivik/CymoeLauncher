package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import com.mivik.cymoe.dp2px

@SuppressLint("Registered")
open class CymoeActivity : AppCompatActivity() {
	override fun getResources(): Resources {
		val resources = super.getResources()
		return if (resources is CymoeResourcesWrapper) resources.fallback
		else resources
	}

	protected fun View.wrapWithCard(): View = CardView(context).apply {
		addView(this@wrapWithCard)
	}

	protected fun View.wrapWithPadding(): View {
		val context = context
		return LinearLayoutCompat(context).apply {
			gravity = Gravity.CENTER
			clipToPadding = false
			dp2px(20).also { setPadding(it, it, it, it) }
			addView(this@wrapWithPadding, -1, -2)
		}
	}
}