package com.mivik.argon.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import com.mivik.argon.C;

public class MCard extends LinearLayout implements C, ValueAnimator.AnimatorUpdateListener {
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE,
			MCARD_ELEVATION_START = 8, MCARD_ELEVATION_END = 2;

	private Paint P;
	private float _EStart, _EEnd;
	private ValueAnimator _Ani;
	private int _Size;

	public MCard(Context cx) {
		this(cx, null, 0);
	}

	public MCard(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MCard(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);
		setWillNotDraw(false);
		setBackground(null);
		setClipChildren(false);
		setClipToPadding(false);
		setSize(SIZE_MEDIUM);
		P = new Paint();
		P.setAntiAlias(false);
		P.setDither(false);
		P.setStyle(Paint.Style.FILL);
		setColor(UI.getPrimaryColor(cx, DEFAULT_BACKGROUND_COLOR));
		setElevationRange(MCARD_ELEVATION_START, MCARD_ELEVATION_END);
		_Ani = ValueAnimator.ofFloat(0, 0);
		_Ani.setDuration(PRESS_DURATION);
		_Ani.addUpdateListener(this);
		setOutlineProvider(RoundRectOutLineProvider.getInstance());
		int d = RADIUS << 1;
		setPadding(d, d, d, d);
	}

	public int getSize() {
		return _Size;
	}

	public void setSize(int size) {
		_Size = size;
		int d = RADIUS >> 1;
		int l = d * ((size >>> 16) & 65535);
		int t = d * (size & 65535);
		setPadding(l, t, l, t);
		requestLayout();
		postInvalidate();
	}

	public int getColor() {
		return P.getColor();
	}

	public void setColor(int color) {
		P.setColor(color);
		postInvalidate();
	}

	public void setElevationRange(float from, float to) {
		_EStart = from;
		_EEnd = to;
		setElevation(_EStart);
		postInvalidate();
	}

	public void setElevationStart(float from) {
		_EStart = from;
		setElevation(_EStart);
		postInvalidate();
	}

	public void setElevationEnd(float to) {
		_EEnd = to;
	}

	public float getElevationStart() {
		return _EStart;
	}

	public float getElevationEnd() {
		return _EEnd;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		setElevation((float) animation.getAnimatedValue());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setTranslationY(_EEnd > _EStart ? -TRANSLATION : TRANSLATION);
				if (_Ani.isRunning()) _Ani.cancel();
				_Ani.setFloatValues(_EStart, _EEnd);
				_Ani.start();
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setTranslationY(0);
				if (_Ani.isRunning()) _Ani.cancel();
				_Ani.setFloatValues(_EEnd, _EStart);
				_Ani.start();
				break;
		}
		return ret;
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS, P);
		super.draw(canvas);
	}
}
