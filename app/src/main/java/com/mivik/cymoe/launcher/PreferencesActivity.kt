package com.mivik.cymoe.launcher

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import com.mivik.cymoe.*


class PreferencesActivity : CymoeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(buildView())
	}

	private fun buildView() = buildPreferencesView(this).wrapWithCard().wrapWithPadding()

	companion object {
		@SuppressWarnings("private")
		fun buildPreferencesView(context: Context): View {
			return MPreferencesView(context).apply {
				view.isVerticalScrollBarEnabled = false
				val space = dp2px(20)
				addGroup("核心设置")
				val cymoePref = CymoePreferences.getInstance(context)
				addCheckBoxItem("禁用开场动画").apply {
					checked = cymoePref.disableSplash
					listener = { cymoePref.disableSplash = checked }
				}
				addSimpleItem("设备ID", "查看和更改").apply {
					listener = {
						context.alert {
							setTitle("设备ID")
							val filedUserPref = FiledUserPreferences.getInstance(context)
							val edit = AppCompatEditText(context).apply {
								setText(filedUserPref.key_imei_local_sdk)
								hint = "设备ID"
							}.apply {
								post {
									setSelection(length())
									requestFocus()
								}
							}
							setView(edit)
							setPositiveButton("确定") { _, _ ->
								filedUserPref.key_imei_local = edit.text.toString()
								filedUserPref.key_imei_local_sdk = edit.text.toString()
								context.toast("已保存")
							}
							setNegativeButton("取消", null)
							setCancelable(true)
						}
					}
				}
				addSpace(space)
				addGroup("游戏设置")
				addCheckBoxItem("将 Click 替换为 Flick", "仅供娱乐").apply {
					checked = cymoePref.clickToFlick
					listener = { cymoePref.clickToFlick = checked }
				}
				addCheckBoxItem("将 Drag 替换为 Click", "真 · 点 锁 地 狱").apply {
					checked = cymoePref.dragToClick
					listener = { cymoePref.dragToClick = checked }
				}
				addCheckBoxItem("启用 AutoPlay", "启用 AutoPlay 的分数将不会保存和上传").apply {
					checked = cymoePref.autoPlay
					listener = { cymoePref.autoPlay = checked }
				}
				addSpace(space)
				addGroup("剧情设置")
				addCheckBoxItem("禁用 TrueEnding", "众所周知这是一个 Bug").apply {
					checked = cymoePref.disableTrueEnding
					listener = { cymoePref.disableTrueEnding = checked }
				}
			}.view
		}
	}
}