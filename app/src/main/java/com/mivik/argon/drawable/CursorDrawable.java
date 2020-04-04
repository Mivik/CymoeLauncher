package com.mivik.argon.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class CursorDrawable extends Drawable {
	public static final int DEFAULT_COLOR = Color.BLACK;
	public static final float DEFAULT_WIDTH = 5;

	private Paint P;

	public CursorDrawable() {
		P = new Paint();
		P.setAntiAlias(false);
		P.setDither(false);
		P.setColor(DEFAULT_COLOR);
		P.setStyle(Paint.Style.STROKE);
		P.setStrokeCap(Paint.Cap.SQUARE);
		P.setStrokeWidth(DEFAULT_WIDTH);
	}

	public Paint getPaint() {
		return P;
	}

	public void setWidth(float width) {
		P.setStrokeWidth(width);
		invalidateSelf();
	}

	public void setColor(int color) {
		P.setColor(color);
		invalidateSelf();
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRect(getBounds(), P);
	}

	@Override
	public void setAlpha(int alpha) {
		P.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		P.setColorFilter(colorFilter);
		invalidateSelf();
	}

	@Override
	public int getIntrinsicWidth() {
		return 1;
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}