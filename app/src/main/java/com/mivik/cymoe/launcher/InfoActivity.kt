package com.mivik.cymoe.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import com.mivik.argon.widget.MCard
import com.mivik.argon.widget.OvalOutlineProvider
import com.mivik.cymoe.*

class InfoActivity : CymoeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(buildView())
	}

	@SuppressLint("SetTextI18n")
	private fun buildView(): View {
		val context = this
		return LinearLayoutCompat(context).apply {
			gravity = Gravity.CENTER
			clipToPadding = false
			dp2px(20).also { setPadding(it, it, it, it) }
			addView(MCard(context).apply {
				color = Color.WHITE
				dp2px(15).also { setPadding(it, it, it, it) }
				addView(object : LinearLayoutCompat(context) {
					override fun onTouchEvent(event: MotionEvent?): Boolean {
						super.onTouchEvent(event)
						(parent as ViewGroup).onTouchEvent(event)
						return true
					}
				}.apply {
					gravity = Gravity.CENTER
					orientation = LinearLayoutCompat.VERTICAL
					addView(AppCompatImageView(context).apply {
						gravity = Gravity.CENTER
						setImageResource(R.mipmap.my_avatar)
						circularize()
						layoutParams = dp2px(128).let { FrameLayout.LayoutParams(it, it) }
					})
					var marginParam = LinearLayoutCompat.LayoutParams(-1, -2).apply { topMargin = dp2px(5) }
					addView(AppCompatTextView(context).apply {
						text = "Mivik"
						setTextColor(primaryColor)
						setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
						gravity = Gravity.CENTER
						layoutParams = marginParam
					})
					addView(AppCompatTextView(context).apply {
						text = "一只喜欢音游的程序员\n\u2588\u2588岁，是学生"
						setTextColor(lightColor(primaryColor, 20))
						setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
						gravity = Gravity.CENTER
						layoutParams = marginParam
					})
					addView(LinearLayoutCompat(context).apply {
						orientation = LinearLayoutCompat.HORIZONTAL
						gravity = Gravity.CENTER
						marginParam =
							LinearLayoutCompat.LayoutParams(-1, -2).apply { topMargin = dp2px(10) }
						layoutParams = marginParam
						marginParam =
							dp2px(32).let { LinearLayoutCompat.LayoutParams(it, it).apply { leftMargin = dp2px(15) } }
						addView(AppCompatImageView(context).apply {
							setImageDrawable(getDrawable(R.mipmap.icon_website).tint(primaryColor))
							scaleType = ImageView.ScaleType.FIT_XY
							setOnClickListener {
								startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mivik.gitee.io")))
							}
							layoutParams = dp2px(32).let { LinearLayoutCompat.LayoutParams(it, it) }
						})
						addView(AppCompatImageView(context).apply {
							setImageDrawable(getDrawable(R.mipmap.icon_github).tint(primaryColor))
							scaleType = ImageView.ScaleType.FIT_XY
							setOnClickListener {
								startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Mivik")))
							}
							layoutParams = marginParam
						})
						addView(AppCompatImageView(context).apply {
							setImageDrawable(getDrawable(R.mipmap.icon_qq).tint(primaryColor))
							scaleType = ImageView.ScaleType.FIT_XY
							setOnClickListener {
								try {
									startActivity(
										Intent(
											Intent.ACTION_VIEW,
											Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=250851048")
										)
									)
								} catch (e: Throwable) {
									toast("貌似你还并没有安装QQ...")
								}
							}
							layoutParams = marginParam
						})
					})
				}, -1, -2)
			}, -1, -2)
		}
	}
}