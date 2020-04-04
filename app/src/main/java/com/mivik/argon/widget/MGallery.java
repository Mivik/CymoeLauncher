package com.mivik.argon.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.mivik.argon.C;

public class MGallery extends FrameLayout implements C, ValueAnimator.AnimatorUpdateListener {
	public static final Interpolator I = new AccelerateDecelerateInterpolator();

	private ValueAnimator Ani;
	private int P;
	private Rect ClipBounds;
	private boolean V = false, R = false;

	public MGallery(Context cx) {
		this(cx, null, 0);
	}

	public MGallery(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MGallery(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);
		ClipBounds = new Rect();
		setWillNotDraw(true);
		Ani = ValueAnimator.ofInt(0, 0);
		Ani.addUpdateListener(this);
		Ani.setDuration(SWITCH_DURATION);
		Ani.setInterpolator(I);
		P = 0;
	}

	public ValueAnimator getAnimator() {
		return Ani;
	}

	public void setInterpolator(Interpolator i) {
		Ani.setInterpolator(i);
	}

	public void setDuration(long du) {
		Ani.setDuration(du);
	}

	public boolean isReversed() {
		return R;
	}

	public void setReversed(boolean re) {
		if (R == re) return;
		R = re;
		updateLayout(getWidth(), getHeight());
	}


	public boolean isVertical() {
		return V;
	}

	public void setVertical(boolean flag) {
		if (V == flag) return;
		if (V = flag)
			setScrollX(0);
		else
			setScrollY(0);
		updateLayout(getWidth(), getHeight());
		setPage(P, false);
	}

	public int getPage() {
		return P;
	}

	public void setPage(int p) {
		setPage(p, true);
	}

	public void setPage(int p, boolean ani) {
		P = p;
		int d = P * (V ? getHeight() : getWidth());
		d = R ? -d : d;
		if (Ani.isRunning()) Ani.cancel();
		if (ani) {
			Ani.setIntValues(V ? getScrollY() : getScrollX(), d);
			Ani.start();
		} else {
			if (V) setScrollY(d);
			else setScrollX(d);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		updateLayout(r - l, b - t);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		ClipBounds.set(0, 0, w, h);
		setClipBounds(ClipBounds);
		updateLayout(w, h);
		setPage(P, false);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (V)
			setScrollY((int) animation.getAnimatedValue());
		else
			setScrollX((int) animation.getAnimatedValue());
	}

	private void updateLayout(int w, int h) {
		int dx = R ? -w : w, dy = R ? -h : h;
		int s = 0;
		if (V)
			for (int i = 0; i < getChildCount(); i++, s += dy)
				getChildAt(i).layout(0, s, w, s + h);
		else
			for (int i = 0; i < getChildCount(); i++, s += dx)
				getChildAt(i).layout(s, 0, s + w, h);
	}
}