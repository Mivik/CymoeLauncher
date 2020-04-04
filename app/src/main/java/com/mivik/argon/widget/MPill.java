package com.mivik.argon.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.mivik.argon.C;
import com.mivik.argon.listener.CheckListener;

public class MPill extends View implements C, ValueAnimator.AnimatorUpdateListener {
	public static final int DEFAULT_SIZE = UI.dp2px(64), DEFAULT_ACCENT_COLOR = Color.BLACK;
	public static final float SCALE = 2 / 5f;

	private float _EStart, _EEnd;
	private ValueAnimator AniE, AniC;
	private Paint P;
	private int S;
	private int _C, _A, CD, CC;
	private Drawable _Icon;
	private boolean _Checked = false;
	private CheckListener _Lis;

	public MPill(Context cx) {
		this(cx, null, 0);
	}

	public MPill(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MPill(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);

		P = new Paint();
		P.setAntiAlias(false);
		P.setDither(false);
		P.setStyle(Paint.Style.FILL);
		setColor(UI.getPrimaryColor(cx, DEFAULT_ACCENT_COLOR));
		setDisabledColor(DISABLE_COLOR);

		AniE = ValueAnimator.ofFloat(0, 0);
		AniE.setDuration(PRESS_DURATION);
		AniE.addUpdateListener(this);

		AniC = ValueAnimator.ofFloat(0, 0);
		AniC.setDuration(SWITCH_DURATION);
		AniC.addUpdateListener(this);
		setElevationRange(MButton.MBUTTON_ELEVATION_START, MButton.MBUTTON_ELEVATION_END);
		setOutlineProvider(OvalOutlineProvider.getInstance());
	}

	public int getContentColor() {
		return _A;
	}

	public void setContentColor(int color) {
		_A = color;
		if (isEnabled()) {
			P.setColor(_Checked ? _C : _A);
			setIconColor(_Checked ? _A : _C);
			postInvalidate();
		}
	}

	public int getDisabledColor() {
		return CD;
	}

	public void setDisabledColor(int color) {
		CD = color;
		postInvalidate();
	}

	public void setColor(int color) {
		_C = color;
		_A = UI.isDark(color) ? Color.WHITE : Color.BLACK;
		if (isEnabled()) {
			P.setColor(_Checked ? _C : _A);
			setIconColor(_Checked ? _A : _C);
			postInvalidate();
		}
	}

	public CheckListener getCheckListener() {
		return _Lis;
	}

	public void setCheckListener(CheckListener lis) {
		_Lis = lis;
	}

	public Drawable getIcon() {
		return _Icon;
	}

	public void setIconResource(int res) {
		setIcon(getContext().getDrawable(res));
	}

	public void setIcon(Drawable icon) {
		if (_Icon == icon || icon == null) return;
		_Icon = icon.mutate();
		_Icon.setCallback(this);
		UI.tintDrawable(_Icon, _Checked ? _A : _C);
		updateBounds();
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

	public boolean isChecked() {
		return _Checked;
	}

	public void setChecked(boolean flag) {
		setChecked(flag, true);
	}

	public void setChecked(boolean flag, boolean ani) {
		if (_Checked == flag) return;
		_Checked = flag;
		if (_Lis != null && _Lis.onChecked(_Checked)) return;
		if (ani) {
			if (AniC.isRunning()) AniC.cancel();
			if (flag) AniC.setFloatValues(0, 1);
			else AniC.setFloatValues(1, 0);
			AniC.start();
		} else {
			if (flag) setProgress(1);
			else setProgress(0);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		if (!isEnabled()) return ret;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setTranslationY(_EEnd > _EStart ? -TRANSLATION : TRANSLATION);
				if (AniE.isRunning()) AniE.cancel();
				AniE.setFloatValues(_EStart, _EEnd);
				AniE.start();
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setTranslationY(0);
				setChecked(!_Checked);

				if (AniE.isRunning()) AniE.cancel();
				AniE.setFloatValues(_EEnd, _EStart);
				AniE.start();
				break;
		}
		return ret;
	}

	@Override
	protected void onMeasure(int w, int h) {
		int s = Math.min(
				MeasureSpec.getMode(w) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(w) : DEFAULT_SIZE,
				MeasureSpec.getMode(h) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(h) : DEFAULT_SIZE
		);
		setMeasuredDimension(s, s);
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return DEFAULT_SIZE;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return DEFAULT_SIZE;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		float pro = (float) animation.getAnimatedValue();
		if (animation == AniE) setElevation(pro);
		else setProgress(pro);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		S = Math.min(w, h);
		updateBounds();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int cx = getWidth() / 2, cy = getHeight() / 2;
		canvas.drawCircle(cx, cy, S / 2, P);
		if (_Icon != null) {
			int s = _Icon.getBounds().width() >> 1;
			canvas.translate(cx - s, cy - s);
			_Icon.draw(canvas);
			canvas.translate(s - cx, s - cy);
		}
		super.onDraw(canvas);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (_Icon == null) return;
		int d = enabled ? _C : DISABLE_COLOR;
		if (_Checked) P.setColor(d);
		else setIconColor(d);
		postInvalidate();
	}

	private void updateBounds() {
		if (_Icon != null) _Icon.setBounds(0, 0, (int) (S * SCALE), (int) (S * SCALE));
	}

	private void setIconColor(int color) {
		UI.tintDrawable(_Icon, color);
	}

	private void setProgress(float pro) {
		int d = isEnabled() ? _C : CD;
		P.setColor(UI.transformColor(_A, d, pro));
		setIconColor(d - P.getColor() + _A);
		postInvalidate();
	}
}