package com.mivik.argon.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.mivik.argon.C;
import com.mivik.argon.Global;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UI implements C {
	private static int PrimaryColor;

	static {
		if (Build.VERSION.SDK_INT >= 28) {
			try {
				Method forName = Class.class.getDeclaredMethod("forName", String.class);
				Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

				Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
				Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
				Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
				Object sVmRuntime = getRuntime.invoke(null);
				setHiddenApiExemptions.invoke(sVmRuntime, (Object) (new String[]{"L"}));
				Log.i(T, "Unsealed API");
			} catch (Throwable t) {
				Log.e(T, "Failed to unseal API", t);
			}
		}
	}

	private UI() {
	}

	public static CharSequence tintText(String text, int color) {
		SpannableString s = new SpannableString(text);
		s.setSpan(new ForegroundColorSpan(color), 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		return s;
	}

	public static void tintStatusBar(Activity activity, int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				Window window = activity.getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(color);
			} catch (Throwable t) {
				Log.e(T, "tintStatusBar -> " + activity, t);
			}
		}
	}

	public static int getPrimaryColor(Context cx, int def) {
		if (PrimaryColor == 0) {
			TypedArray arr = cx.obtainStyledAttributes(new int[]{android.R.attr.colorPrimary});
			PrimaryColor = arr.getColor(0, def);
			arr.recycle();
		}
		return PrimaryColor;
	}

	public static int darkenColor(int color, int val) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		if ((r -= val) < 0) r = 0;
		if ((g -= val) < 0) g = 0;
		if ((b -= val) < 0) b = 0;
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public static int transformColor(int from, int to, float pro) {
		int r = (from >> 16) & 255;
		int g = (from >> 8) & 255;
		int b = from & 255;

		r = (int) ((((to >> 16) & 255) - r) * pro + r);
		g = (int) ((((to >> 8) & 255) - g) * pro + g);
		b = (int) (((to & 255) - b) * pro + b);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public static void tintDrawable(Drawable d, int color) {
		if (d == null) return;
		d.setTint(color);
	}

	private static float DP_SCALE = -1;

	public static final int dp2px(float dp) {
		if (DP_SCALE == -1) DP_SCALE = Resources.getSystem().getDisplayMetrics().density;
		return (int) (DP_SCALE * dp + 0.5f);
	}

	public static void onUI(Runnable action) {
		if (Looper.getMainLooper() == Looper.myLooper()) action.run();
		else new Handler(Looper.getMainLooper()).post(action);
	}

	public static void preventDismiss(Dialog dialog) {
		try {
			Field field = Dialog.class.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, false);
		} catch (Throwable e) {
			Log.wtf(T, e);
		}
	}

	public static void forceDismiss(Dialog dialog) {
		try {
			Field field = Dialog.class.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, true);
		} catch (Throwable e) {
			Log.wtf(T, e);
		}
	}

	public static void postShowError(final Context cx, final Throwable t) {
		onUI(new Runnable() {
			@Override
			public void run() {
				showError(cx, t);
			}
		});
	}

	public static AlertDialog showError(final Context cx, Throwable t) {
		final String msg = Log.getStackTraceString(t);
		AlertDialog ret = new AlertDialog.Builder(cx).setTitle("Oops").setMessage(msg).setCancelable(true).setNegativeButton("复制", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ClipboardManager manager = (ClipboardManager) cx.getSystemService(Context.CLIPBOARD_SERVICE);
				manager.setPrimaryClip(ClipData.newPlainText("Error", msg));
			}
		}).setPositiveButton("确定", null).create();
		ret.show();
		return ret;
	}

	public static void postToast(final Context cx, final CharSequence cs) {
		onUI(new Runnable() {
			@Override
			public void run() {
				toast(cx, cs);
			}
		});
	}

	public static boolean isDark(int color) {
		return ((Color.red(color) + Color.green(color) + Color.blue(color)) <= 600);
	}

	public static void toast(Context cx, CharSequence cs) {
		Toast.makeText(cx, cs, Toast.LENGTH_SHORT).show();
	}

	public static int lightenColor(int color, int a) {
		return Color.argb(Color.alpha(color), Math.max(Math.min(Color.red(color) + a, 255), 0), Math.max(Math.min(Color.green(color) + a, 255), 0), Math.max(Math.min(Color.blue(color) + a, 255), 0));
	}

	public static boolean setEditTextCursor(TextView edit, int res) {
		try {
			Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
			f.setAccessible(true);
			f.set(edit, res);
			return true;
		} catch (Throwable t) {
			Log.e(T, "setEditTextCursor -> " + edit, t);
			return false;
		}
	}

	public static void copy(Context cx, String s) {
		android.text.ClipboardManager manager = (android.text.ClipboardManager) cx.getSystemService(Context.CLIPBOARD_SERVICE);
		manager.setText(s);
	}

	public static float presetTextPaint(Context cx, TextPaint p) {
		p.setTypeface(Global.getOpenSans(cx.getAssets()));
		p.setFakeBoldText(true);
		p.setLetterSpacing(0.05f);
		Paint.FontMetrics fm = p.getFontMetrics();
		return fm.descent - fm.ascent;
	}

	public static void setEditTextCursor(TextView edit, Drawable d) {
		try {
			Field f = TextView.class.getDeclaredField("mEditor");
			f.setAccessible(true);
			Object editor = f.get(edit);
			if (Build.VERSION.SDK_INT < 28) {
				f = editor.getClass().getDeclaredField("mCursorDrawable");
				f.setAccessible(true);
				Object arr = f.get(editor);
				Array.set(arr, 0, d);
				Array.set(arr, 1, d);
			} else {
				f = editor.getClass().getDeclaredField("mDrawableForCursor");
				f.setAccessible(true);
				f.set(editor, d);
			}
		} catch (Throwable t) {
			Log.e(T, "setEditTextCursor -> " + edit, t);
		}
	}

	public static void alert(Context cx, String msg) {
		alert(cx, "Alert", msg);
	}

	public static void alert(Context cx, String title, String msg) {
		new AlertDialog.Builder(cx).setTitle(title).setMessage(msg).setCancelable(true).setPositiveButton("OK", null).show();
	}
}