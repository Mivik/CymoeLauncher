package com.mivik.argon.widget;

import android.graphics.Outline;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import androidx.annotation.RequiresApi;
import com.mivik.argon.C;

public class RoundRectOutLineProvider extends ViewOutlineProvider {
	private static RoundRectOutLineProvider INSTANCE;

	private RoundRectOutLineProvider() {
	}

	public static RoundRectOutLineProvider getInstance() {
		if (INSTANCE == null) INSTANCE = new RoundRectOutLineProvider();
		return INSTANCE;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void getOutline(View view, Outline outline) {
		outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), C.RADIUS);
	}
}
