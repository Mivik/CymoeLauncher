package com.mivik.argon.widget;

import android.graphics.Outline;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import androidx.annotation.RequiresApi;

public class OvalOutlineProvider extends ViewOutlineProvider {
	private static OvalOutlineProvider INSTANCE;

	private OvalOutlineProvider() {
	}

	public static OvalOutlineProvider getInstance() {
		if (INSTANCE == null) INSTANCE = new OvalOutlineProvider();
		return INSTANCE;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void getOutline(View view, Outline outline) {
		outline.setOval(0, 0, view.getWidth(), view.getHeight());
	}
}