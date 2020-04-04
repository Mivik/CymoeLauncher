package com.mivik.cymoe.launcher

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.PopupWindow
import com.mivik.argon.widget.MPill
import com.mivik.cymoe.*
import lab.galaxy.yahfa.HookMain
import kotlin.math.absoluteValue

class FloatingButton(var context: Context) : PopupWindow(), View.OnTouchListener, Runnable {
	private val button = MPill(context)

	companion object {
		private const val DRAG_DELTA = 15
		private const val BUTTON_FADE_OUT_ALPHA = 0.4f
		private const val BUTTON_FADE_OUT_DELAY = 2000L

		lateinit var card: View

		fun init(context: Context) {
			card = PreferencesActivity.buildPreferencesView(context).apply {
				// 那个 IMEI 不能用... 默认是弹出一个 androidx 的 Dialog，但是由于游戏的 Activity 不是 AppCompat 的所以会报错
				// 如果是用一个新的 Activity 来弹出这个 Dialog 会因为 Resources 的问题报错... 佛了，只好直接禁用。
				root.removeViewAt(2)
			}.view.wrapWithCard()
		}
	}

	init {
		button.setIconResource(R.mipmap.icon_settings)
		dp2px(48).also {
			width = it
			height = it
		}
		setTouchInterceptor(this)
		contentView = button
		isClippingEnabled = false
	}

	val screenWidth: Int
		get() = context.resources.displayMetrics.heightPixels

	val screenHeight: Int
		get() = context.resources.displayMetrics.widthPixels

	private fun onClick() {
		Dialog(context).apply {
			requestWindowFeature(Window.FEATURE_NO_TITLE)
			setContentView(card)
			setCancelable(true)
			setCanceledOnTouchOutside(true)
			setOnDismissListener { _ ->
				(card.parent as ViewGroup).removeView(card)
			}
			val window = window!!
			window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			window.decorView.setPadding(0, 0, 0, 0)
			window.attributes = window.attributes.apply {
				width = (screenWidth * 0.6).toInt()
				height = (screenHeight * 0.7).toInt()
			}
		}.show()
	}

	private var touchFromX = 0f
	private var touchFromY = 0f
	private var fromX = 0
	private var fromY = 0
	private var dragged = false

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouch(v: View?, event: MotionEvent?): Boolean {
		event ?: return false
		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN -> {
				touchFromX = event.rawX
				touchFromY = event.rawY
				val decorView = contentView.parent as ViewGroup
				val lp = decorView.layoutParams as WindowManager.LayoutParams
				lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
				fromX = lp.x
				fromY = lp.y
				dragged = false
				button.clearAnimation()
				button.alpha = 1f
				mainHandler.removeCallbacks(this)
			}
			MotionEvent.ACTION_MOVE -> {
				val delX = event.rawX - touchFromX
				val delY = event.rawY - touchFromY
				if (delX.absoluteValue >= DRAG_DELTA || delY.absoluteValue >= DRAG_DELTA) {
					update(fromX + delX.toInt(), fromY + delY.toInt(), -1, -1)
					dragged = true
				}
			}
			MotionEvent.ACTION_UP -> {
				if (!dragged) onClick()
				else {
					val decorView = contentView.parent as ViewGroup
					val lp = decorView.layoutParams as WindowManager.LayoutParams
					val targetX = lp.x.coerceAtLeast(0).coerceAtMost(screenWidth - width)
					val targetY = lp.y.coerceAtLeast(0).coerceAtMost(screenHeight - height)
					if (targetX != lp.x || targetY != lp.y) {
						val ofObject = ValueAnimator.ofObject(TypeEvaluator<Point>() { fraction, startValue, endValue ->
							Point(
								(startValue.x + (endValue.x - startValue.x) * fraction).toInt(),
								(startValue.y + (endValue.y - startValue.y) * fraction).toInt()
							)
						}, Point(lp.x, lp.y), Point(targetX, targetY))
						ofObject.addUpdateListener {
							val value = it.animatedValue as Point
							update(value.x, value.y, -1, -1)
						}
						ofObject.interpolator = AccelerateInterpolator()
						ofObject.start()
					}
				}
				mainHandler.postDelayed(this, BUTTON_FADE_OUT_DELAY)
			}
			else -> return false
		}
		return true
	}

	override fun run() {
		button.startAnimation(AlphaAnimation(button.alpha, BUTTON_FADE_OUT_ALPHA).apply {
			fillAfter = true
		})
	}

	override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
		super.showAtLocation(parent, gravity, x, y)
		mainHandler.postDelayed(this, BUTTON_FADE_OUT_DELAY)
	}
}

object FloatingButtonHook {
	val classMainActivity = Cymoe.fakeClassLoader.loadClass(CYTUS_MAIN_ACTIVITY_NAME)

	fun hook() {
		val self = FloatingButtonHook::class.java
		val argTypes = arrayOf(Activity::class.java)
		HookMain.backupAndHook(
			Class.forName("android.app.Activity").getDeclaredMethod("onAttachedToWindow"),
			self.getDeclaredMethod("onAttachedToWindow_new", *argTypes),
			self.getDeclaredMethod("onAttachedToWindow_old", *argTypes)
		)
	}

	@JvmStatic
	external fun Activity.onAttachedToWindow_old()

	@JvmStatic
	fun Activity.onAttachedToWindow_new() {
		onAttachedToWindow_old()
		if (!classMainActivity.isInstance(this)) return
		Handler().post {
			FloatingButton(this).showAtLocation(window.decorView, Gravity.NO_GRAVITY, dp2px(5), dp2px(5))
		}
	}
}