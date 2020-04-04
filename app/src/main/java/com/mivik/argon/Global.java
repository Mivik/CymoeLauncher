package com.mivik.argon;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

public class Global {
	public static Xfermode X = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

	private Global() {
	}

	private static Typeface FONT_OPEN_SANS;

	public static Typeface getOpenSans(AssetManager ass) {
		if (FONT_OPEN_SANS == null) FONT_OPEN_SANS = Typeface.createFromAsset(ass, "font/OpenSans-Regular.ttf");
		return FONT_OPEN_SANS;
	}
}
