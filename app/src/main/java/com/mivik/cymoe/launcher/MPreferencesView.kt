package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.Space
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import com.mivik.cymoe.dp2px
import com.mivik.cymoe.empty
import com.mivik.cymoe.lightColor
import com.mivik.cymoe.primaryColor

class MPreferencesView(private val context: Context) {
	var showDividers: Int
		get() = root.showDividers
		set(showDividers) {
			root.showDividers = showDividers
		}

	var untouchable = false

	val root: LinearLayoutCompat = object : LinearLayoutCompat(context) {
		init {
			orientation = VERTICAL
			showDividers = SHOW_DIVIDER_MIDDLE
			setWillNotDraw(false)
			dividerDrawable = DivideDrawable(primaryColor)
		}

		@SuppressLint("RestrictedApi")
		override fun hasDividerBeforeChildAt(childIndex: Int): Boolean {
			if (getChildAt(childIndex) is Space) return false
			if (childIndex != 0 && getChildAt(childIndex - 1) is Space) return false
			return super.hasDividerBeforeChildAt(childIndex)
		}
	}

	private val divider: DivideDrawable =
		DivideDrawable(primaryColor)
	val view: ScrollView = object : ScrollView(context) {
		init {
			isFillViewport = true
			addView(root, FrameLayout.LayoutParams(-1, -1).apply { gravity = Gravity.TOP })
		}

		override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
			return if (untouchable) true else super.onInterceptTouchEvent(ev)
		}

		override fun onTouchEvent(ev: MotionEvent?): Boolean {
			val ret = super.onTouchEvent(ev)
			return if (untouchable) {
				(parent as ViewGroup).onTouchEvent(ev)
				true
			} else ret
		}
	}

	private class DivideDrawable(color: Int) : Drawable() {
		private val paint = Paint()

		override fun setAlpha(a: Int) {}
		override fun setColorFilter(filter: ColorFilter?) {}

		override fun getOpacity() = PixelFormat.TRANSLUCENT

		fun setColor(color: Int) {
			paint.color = color
			invalidateSelf()
		}

		override fun draw(c: Canvas) {
			c.drawRect(bounds, paint)
		}

		override fun getIntrinsicHeight() = 1

		init {
			paint.style = Paint.Style.FILL
			paint.color = color
			paint.strokeWidth = 1f
		}
	}

	fun addView(view: View) = root.addView(view)

	fun addSpace(height: Int) = Space(context).apply {
		layoutParams = LinearLayoutCompat.LayoutParams(-1, height)
		root.addView(this)
	}

	fun addSimpleItem(title: CharSequence?, subtitle: CharSequence? = null) = SimpleItem(context).apply {
		this.title = title
		this.subtitle = subtitle
		root.addView(this)
	}

	fun addCheckBoxItem(
		title: CharSequence?,
		subtitle: CharSequence? = null,
		checked: Boolean = false
	) = CheckBoxItem(context).apply {
		this.title = title
		this.subtitle = subtitle
		this.checked = checked
		root.addView(this)
	}

	fun addSwitchItem(
		title: CharSequence?,
		subtitle: CharSequence? = null,
		checked: Boolean = false
	) = SwitchItem(context).apply {
		this.title = title
		this.subtitle = subtitle
		this.checked = checked
		root.addView(this)
	}

	fun addGroup(title: CharSequence?) = GroupView(context).apply {
		text = title
		root.addView(this)
	}

	class GroupView(
		cx: Context?,
		attr: AttributeSet? = null,
		defStyle: Int = 0
	) : AppCompatTextView(cx, attr, defStyle) {
		init {
			textSize = 20f
			setTextColor(primaryColor)
			dp2px(15).also { setPadding(it, it, it, it) }
		}
	}

	open class SimpleItem(
		context: Context,
		attr: AttributeSet? = null,
		defStyle: Int = 0
	) : LinearLayoutCompat(context, attr, defStyle), View.OnClickListener {
		companion object {
			private var backgroundResourceId: Int? = null

			fun getBackgroundDrawable(context: Context): Drawable {
				backgroundResourceId ?: run {
					val value = TypedValue()
					context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
					backgroundResourceId = value.resourceId
				}
				val arr = context.theme.obtainStyledAttributes(
					backgroundResourceId!!,
					intArrayOf(android.R.attr.selectableItemBackground)
				)
				val ret = arr.getDrawable(0)
				arr.recycle()
				return ret!!
			}
		}

		val titleTextView = AppCompatTextView(context)
		val subtitleTextView = AppCompatTextView(context)
		val descriptionLayout = LinearLayoutCompat(context)
		var listener: ((View) -> Unit)? = null
		var showDialogWhenLongClick = true

		init {
			background = getBackgroundDrawable(context)
			orientation = HORIZONTAL
			dp2px(15).also { setPadding(it, it, it, it) }
			gravity = Gravity.CENTER
			isClickable = true
			descriptionLayout.orientation = VERTICAL
			addView(descriptionLayout, LayoutParams(0, -2).apply { weight = 1f })
			titleTextView.setTextColor(primaryColor)
			titleTextView.textSize = 13f
			titleTextView.visibility = View.GONE
			descriptionLayout.addView(titleTextView)
			subtitleTextView.setTextColor(lightColor(primaryColor, 40))
			subtitleTextView.textSize = 8f
			subtitleTextView.visibility = View.GONE
			descriptionLayout.addView(subtitleTextView)
			super.setOnClickListener(this)
			isLongClickable = true
			setOnLongClickListener {
				if (showDialogWhenLongClick && !title.empty() && !subtitle.empty()) {
					AlertDialog.Builder(context).apply {
						setTitle(title)
						setMessage(subtitle)
						setCancelable(true)
					}.show()
					true
				} else false
			}
		}

		var title: CharSequence?
			get() = titleTextView.text
			set(text) {
				titleTextView.text = text
				titleTextView.visibility = if (text.empty()) View.GONE else View.VISIBLE
			}

		var subtitle: CharSequence?
			get() = subtitleTextView.text
			set(text) {
				subtitleTextView.text = text
				subtitleTextView.visibility = if (text.empty()) View.GONE else View.VISIBLE
			}

		override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

		override fun onClick(v: View) {
			listener?.invoke(v)
		}
	}

	class CheckBoxItem(
		context: Context,
		attr: AttributeSet? = null,
		defStyle: Int = 0
	) :
		SimpleItem(context, attr, defStyle) {
		private val box = AppCompatCheckBox(context).apply {
			try {
				buttonTintList = ColorStateList.valueOf(primaryColor)
			} catch (t: Throwable) {
			}
			this@CheckBoxItem.addView(this)
		}

		var checked: Boolean
			get() = box.isChecked
			set(flag) {
				box.isChecked = flag
			}

		override fun onClick(v: View) {
			box.toggle()
			super.onClick(v)
		}
	}

	class SwitchItem(
		context: Context,
		attr: AttributeSet? = null,
		defStyle: Int = 0
	) :
		SimpleItem(context, attr, defStyle) {
		private val switch = SwitchCompat(context).apply {
			try {
				thumbDrawable.colorFilter = PorterDuffColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP)
			} catch (t: Throwable) {
			}
			addView(this)
		}

		var checked: Boolean
			get() = switch.isChecked
			set(flag) {
				switch.isChecked = flag
			}

		override fun onClick(v: View) {
			switch.toggle()
			super.onClick(v)
		}
	}
}