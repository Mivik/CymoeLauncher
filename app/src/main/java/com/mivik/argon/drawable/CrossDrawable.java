package com.mivik.argon.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class CrossDrawable extends Drawable {
	public static final int DEFAULT_COLOR = Color.WHITE;
	public static final float DEFAULT_WIDTH = 5;

	private Paint P;

	public CrossDrawable() {
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
		Rect r = getBounds();
		float wid = P.getStrokeWidth();
		float d = Math.min(r.bottom - r.top, r.right - r.left) / 2f - wid;
		float cx = (r.left + r.right) / 2f, cy = (r.top + r.bottom) / 2f;
		canvas.drawLine(cx - d, cy - d, cx + d, cy + d, P);
		canvas.drawLine(cx - d, cy + d, cx + d, cy - d, P);
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
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
