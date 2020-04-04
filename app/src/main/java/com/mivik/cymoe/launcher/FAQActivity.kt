package com.mivik.cymoe.launcher

import android.graphics.Color
import android.net.nsd.NsdManager
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import com.mivik.argon.widget.MCard
import com.mivik.cymoe.CymoePreferences
import com.mivik.cymoe.dp2px

class FAQActivity : CymoeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(buildView())
	}

	private fun buildView(): View {
		val context = this
		return LinearLayoutCompat(context).apply {
			gravity = Gravity.CENTER
			clipToPadding = false
			dp2px(20).also { setPadding(it, it, it, it) }
			addView(MCard(context).apply {
//				useCompatPadding = true
				dp2px(15).also { setPadding(it, it, it, it) }
				clipToPadding = true
				color = Color.WHITE
				addView(MPreferencesView(context).apply {
					untouchable = true
					addFAQ("会被封号吗？", "目前为止并没有案例。")
					addFAQ("为什么每次都要重新输入密码？", "因为你没有给 Cymoe 开存储权限，请前去系统设置界面开启。")
					addFAQ("为什么明明设置了跳过开场动画却仍有一部分开场动画？", "开场动画中包含了游戏加载的时间。只有游戏加载完成后才能跳过动画。")
					root.removeViewAt(root.childCount - 1) // remove trailing space
				}.view.apply {
					isFocusable = false
					isFocusableInTouchMode = false
				})
			}, -1, -2)
		}
	}

	private fun MPreferencesView.addFAQ(question: CharSequence, answer: CharSequence) {
		val adjust: MPreferencesView.SimpleItem.() -> Unit = {
			isClickable = false
			background = null
			titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
			subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
		}
		addSimpleItem("Q：$question").adjust()
		addSimpleItem(null, "A：$answer").adjust()
		addSpace(dp2px(20))
	}
}