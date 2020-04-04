package com.mivik.cymoe.launcher

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.mivik.argon.widget.MButton
import com.mivik.cymoe.*
import com.yanzhenjie.permission.AndPermission
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : CymoeActivity() {
	private var launching = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		internalInit(this)
		if (requiredPermissions.any {
				ContextCompat.checkSelfPermission(
					this,
					it
				) != PackageManager.PERMISSION_GRANTED
			}) requestRequiredPermissions()
		else syncIMEIIfNeeded()
		val context = this
		setContentView(
			LinearLayoutCompat(context).apply {
				gravity = Gravity.CENTER
				orientation = LinearLayoutCompat.VERTICAL
				clipToPadding = false
				clipChildren = false
				dp2px(50).also { setPadding(it, 0, it, 0) }
				val marginParam = LinearLayoutCompat.LayoutParams(-1, -2).apply { topMargin = dp2px(20) }
				val argonize: MButton.() -> MButton = {
					color = Color.WHITE
					contentColor = primaryColor
					this
				}
				addView(MButton(context).apply {
					text = "启动"
					setOnClickListener {
						launchCytus(it)
					}
					layoutParams = LinearLayoutCompat.LayoutParams(-1, -2)
				}.argonize())
				addView(MButton(context).apply {
					text = "设置"
					color = Color.WHITE
					contentColor = primaryColor
					setOnClickListener {
						startActivity(Intent(context, PreferencesActivity::class.java))
					}
					layoutParams = marginParam
				}.argonize())
				addView(MButton(context).apply {
					text = "常见问题"
					color = Color.WHITE
					contentColor = primaryColor
					setOnClickListener {
						startActivity(Intent(context, FAQActivity::class.java))
					}
					layoutParams = marginParam
				}.argonize())
				addView(MButton(context).apply {
					text = "关于"
					setOnClickListener {
						startActivity(Intent(context, InfoActivity::class.java))
					}
					layoutParams = marginParam
				}.argonize())
			}
		)
	}

	/**
	 * 将 "IMEI" 与龙渊同步
	 */
	private fun syncIMEIIfNeeded() {
		val pref = FiledUserPreferences.getInstance(this)
		if (pref.key_imei_local_sdk.empty() || pref.key_imei_local != pref.key_imei_local_sdk) {
			try {
				val idFile = File(Environment.getExternalStorageDirectory(), "longyuan/EquipmentCode/EquipmentCode.cr")
				if (!idFile.exists()) {
					toast("由于无法在本地找到指定文件，设备ID同步失败")
					return
				}
				pref.key_imei_local = idFile.readText().trim()
				pref.key_imei_local_sdk = idFile.readText().trim()
				toast("龙渊设备ID同步成功")
			} catch (e: IOException) {
				displayError(e, "读取设备ID时发生错误")
			}
		}
	}

	private fun showDeniedDialog(ok: (() -> Unit)? = null, cancel: (() -> Unit)? = null) {
		alert {
			setTitle("抱歉")
			setMessage("我们需要一些权限来运行 Cymoe，其中大部分都是来自 Cytus II 的权限。没有它们 Cymoe 将无法正常工作。".run {
				// 如果 Android 版本在 Q 或以上，IMEI 不能被获取到，龙渊就会随机生成一个 "IMEI" 并存储在 SharedPreferences 中。
				// 如果我们不手动在 SharedPreferences 中指定和本地安装的 Cytus II 相同的 "IMEI"，龙渊就会把 Cymoe 当作一个新设备。过多的绑定设备会造成登录受限。
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
						this@MainActivity,
						Manifest.permission.READ_EXTERNAL_STORAGE
					) != PackageManager.PERMISSION_GRANTED
				)
					"$this\n\n同时，由于没有存储权限，Cymoe 将无法访问龙渊存储在你本地的设备ID。由于你的设备是 Android Q 及以上，龙渊将会把 Cymoe 启动的 Cytus II 当作新的设备，而同一个账号使用的设备过多将会导致无法登录。\n\n为 Cymoe 授予存储权限能简单地解决这一问题。"
				else this
			})
			setPositiveButton("好吧", if (ok == null) null else DialogInterface.OnClickListener { _, _ -> ok() })
			setNegativeButton(
				"但是我拒绝",
				if (cancel == null) null else DialogInterface.OnClickListener { _, _ -> cancel() })
			setCancelable(false)
		}
	}

	private fun requestRequiredPermissions() {
		AndPermission.with(this)
			.runtime()
			.permission(requiredPermissions)
			.rationale { _, _, executor ->
				executor.execute()
			}.onGranted {
				syncIMEIIfNeeded()
			}.onDenied {
				showDeniedDialog(ok = { requestRequiredPermissions() })
			}.start()
	}

	private fun launchCytus(view: View) {
		synchronized(this) {
			if (launching) return
			launching = true
			view.isEnabled = false
		}
		val unlock = {
			synchronized(this@MainActivity) {
				launching = false
				ui {
					view.isEnabled = true
				}
			}
		}
		FloatingButton.init(this)
		thread {
			try {
				Cymoe.init(this)
				ui {
					try {
						Cymoe.launchCytus(this)
						synchronized(this) {
							launching = false
							view.isEnabled = true
						}
					} catch (e: CymoeException) {
						handleCymoeException(e)
						unlock()
					}
				}
			} catch (e: CymoeException) {
				handleCymoeException(e)
				unlock()
			}
		}
	}

	private fun handleCymoeException(e: CymoeException) = ui {
		when (e) {
			is CymoeException.CytusNotInstalled -> toast("你还没有安装 TapTap 渠道的 Cytus II...")
			is CymoeException.CytusAssemblyMismatch ->
				alert {
					setTitle("Cytus II 不兼容")
					setMessage("Cymoe 支持的 Cytus：$ASSEMBLY_EXPECTED_MD5\n本机安装的 Cytus：${e.foundMD5}")
					setPositiveButton("确定", null)
					setCancelable(true)
				}
			is CymoeException.Unknown ->
				displayError(e.err, "未知错误")
		}
	}
}