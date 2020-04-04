package com.mivik.argon.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.mivik.argon.Global;
import com.mivik.argon.listener.CheckListener;

public class MCheckBox extends View implements com.mivik.argon.C, ValueAnimator.AnimatorUpdateListener {
	public static final int DEFAULT_COLOR = Color.DKGRAY, DEFAULT_SIZE = UI.dp2px(24), DEFAULT_PADDING = UI.dp2px(3);
	public static final float SCALE = 0.35f, RSCALE = 1f / 10, SMALL = 0.08f, EMPTY = 0.1f;

	private static final float TT = (float) Math.sqrt(2) / 2 + 1, EPS = 0.0001f;
	private static final Interpolator I = new DecelerateInterpolator();

	private Paint P, C;
	private int S;
	private Path D;
	private float R, Pad = 0, Emp = 0, pro;
	private float[] L = new float[6];
	private ValueAnimator Ani;
	private boolean Checked;
	private int CU, CA, CD;
	private CheckListener Lis;

	public MCheckBox(Context cx) {
		this(cx, null, 0);
	}

	public MCheckBox(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MCheckBox(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);

		setLayerType(LAYER_TYPE_SOFTWARE, null);

		P = new Paint();
		P.setAntiAlias(true);
		P.setDither(true);
		P.setStyle(Paint.Style.FILL);

		C = new Paint();
		C.setAntiAlias(true);
		C.setDither(true);
		C.setStyle(Paint.Style.STROKE);
		C.setStrokeCap(Paint.Cap.SQUARE);
		C.setXfermode(Global.X);
		setColor(UI.getPrimaryColor(cx, DEFAULT_COLOR));
		setUnactivatedColor(Color.GRAY);
		setDisabledColor(DISABLE_COLOR);

		D = new Path();
		Ani = ValueAnimator.ofFloat(0, 1, 2);
		Ani.setDuration(SWITCH_DURATION * 2);
		Ani.addUpdateListener(this);
		Ani.setInterpolator(I);

		setPadding(DEFAULT_PADDING);
	}

	public void setPadding(int s) {
		super.setPadding(s, s, s, s);
		updatePath(getWidth(), getHeight());
		postInvalidate();
	}

	public int getDisabledColor() {
		return CD;
	}

	public void setDisabledColor(int color) {
		CD = color;
	}

	public int getUnactivatedColor() {
		return CU;
	}

	public void setUnactivatedColor(int color) {
		CU = color;
		if (Checked) return;
		P.setColor(color);
		postInvalidate();
	}

	public int getColor() {
		return CA;
	}

	public void setColor(int color) {
		CA = color;
		if (!Checked) return;
		P.setColor(color);
		postInvalidate();
	}

	public void setChecked(boolean flag) {
		setChecked(flag, true);
	}

	public void setChecked(boolean checked, boolean ani) {
		if (Checked == checked) return;
		Checked = checked;
		if (Lis != null && Lis.onChecked(checked)) return;
		if (Ani == null) return;
		if (!ani) {
			if (checked) {
				Emp = S / 2;
				D.reset();
				D.moveTo(L[0], L[1]);
				D.lineTo(L[2], L[3]);
				D.lineTo(L[4], L[5]);
				if (isEnabled()) P.setColor(CA);
			} else {
				Emp = S * EMPTY;
				D.reset();
				if (isEnabled()) P.setColor(CU);
			}
			postInvalidate();
			return;
		}
		if (checked) {
			Emp = S * EMPTY;
			D.reset();
			if (isEnabled()) P.setColor(CA);
		} else {
			Emp = S / 2;
			if (isEnabled()) P.setColor(CU);
		}
		if (Ani.isRunning()) Ani.cancel();
		Ani.start();
	}

	public boolean isChecked() {
		return Checked;
	}

	public CheckListener getCheckListener() {
		return Lis;
	}

	public void setCheckListener(CheckListener lis) {
		Lis = lis;
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		if (!(left == top && left == right && left == bottom)) throw new IllegalArgumentException();
		setPadding(left);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updatePath(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
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
	protected void onDraw(Canvas canvas) {
		int cx = getWidth() >> 1, cy = getHeight() >> 1;
		cx -= S >> 1;
		cy -= S >> 1;
		canvas.drawRoundRect(cx + Pad, cy + Pad, cx + S - Pad, cy + S - Pad, R, R, P);
		P.setXfermode(Global.X);
		canvas.drawRoundRect(cx + Emp, cy + Emp, cx + S - Emp, cy + S - Emp, (1 - pro) * R, (1 - pro) * R, P);
		P.setXfermode(null);
		canvas.drawPath(D, C);
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isEnabled()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN
					|| event.getAction() == MotionEvent.ACTION_MOVE) return true;
			if (event.getAction() == MotionEvent.ACTION_UP) {
				setChecked(!Checked);
				return true;
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator ani) {
		pro = (float) ani.getAnimatedValue();
		D.reset();
		if (Checked) {
			if (pro >= 1) {
				pro -= 1;
				if (pro <= EPS) return;
				Emp = S / 2;
				Pad = S * SMALL * (1 - pro);
				final float cx = L[2], cy = L[3];
				D.moveTo(cx + (L[0] - cx) * pro, cy + (L[1] - cy) * pro);
				D.lineTo(cx, cy);
				D.lineTo(cx + (L[4] - cx) * pro, cy + (L[5] - cy) * pro);
			} else {
				Pad = S * SMALL * pro;
				Emp = S * EMPTY + S * (0.5f - EMPTY) * pro;
			}
		} else {
			if (pro >= 1) {
				pro = 2 - pro;
				Pad = S * SMALL * pro;
				Emp = S * EMPTY + S * (0.5f - EMPTY) * pro;
			} else {
				if (pro < EPS) return;
				Pad = S * SMALL * pro;
				final float cx = getWidth() / 2f, cy = getHeight() / 2f;
				D.moveTo(L[0] + (cx - L[0]) * pro, L[1] + (cy - L[1]) * pro);
				D.lineTo(L[2] + (cx - L[2]) * pro, L[3] + (cy - L[3]) * pro);
				D.lineTo(L[4] + (cx - L[4]) * pro, L[5] + (cy - L[5]) * pro);
			}
		}
		postInvalidate();
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
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled)
			P.setColor(Checked ? CA : CU);
		else
			P.setColor(CD);
		postInvalidate();
	}

	private void updatePath(int w, int h) {
		S = Math.min(w, h) - getPaddingLeft() * 2;
		R = S * RSCALE;
		Emp = S * EMPTY;
		C.setStrokeWidth(R);
		int cx = w >> 1, cy = h >> 1;
		cx -= S >> 1;
		cy -= S >> 1;
		cx += R * TT;
		cy += R * TT;
		float p = S - (R * TT * 2);
		int of = (int) (p * SCALE / 2);
		cy -= of;
		L[0] = cx;
		L[1] = cy + p * (1 - SCALE);
		L[2] = cx + p * SCALE;
		L[3] = cy + p;
		L[4] = cx + p;
		L[5] = cy + p * SCALE;
	}
}
