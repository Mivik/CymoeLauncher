package com.mivik.argon;

import android.graphics.Color;
import com.mivik.argon.widget.UI;

public interface C {
	String T = "Argon";
	int COLOR_INFO = 0xFF11CDEF, COLOR_SUCCESS = 0xFF2DCE89, COLOR_WARNING = 0xFFFB6340, COLOR_DANGER = 0xFFF5365C;
	int RADIUS = UI.dp2px(5);
	long PRESS_DURATION = 150;
	long SWITCH_DURATION = 180;
	int SIZE_SMALL = (4 << 16) | 2, SIZE_MEDIUM = (10 << 16) | 5, SIZE_LARGE = (8 << 16) | 7;
	int TRANSLATION = 2;
	int DISABLE_COLOR = Color.LTGRAY;
	int DARKEN = 20;
}