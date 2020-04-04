package com.mivik.argon.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatTextView;
import com.mivik.argon.C;

public class MButton extends AppCompatTextView implements ValueAnimator.AnimatorUpdateListener, C {
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE,
			MBUTTON_ELEVATION_START = 8, MBUTTON_ELEVATION_END = 4;

	private int _Color;
	private Paint P, SD;
	private ValueAnimator _Ani;
	private float _EStart, _EEnd;
	private boolean _Outline, _Border = true;
	private float __Tmp_EStart;
	private int _TColor;
	private int _FColor, CD, CC;
	private int _IconSize;
	private int _Size;
	private int _Alpha;
	private float _Ele;
	private int Rad;
	private RectF tmpRect = new RectF();

	public MButton(Context cx) {
		this(cx, null, 0);
	}

	public MButton(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MButton(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);

		setBackgroundDrawable(null);
		setRadius(RADIUS);
		setSize(SIZE_MEDIUM);
		setGravity(Gravity.CENTER);
		setClickable(true);

		P = new Paint();
		P.setAntiAlias(true);
		P.setStrokeWidth(2);
		P.setStyle(Paint.Style.FILL);
		SD = new Paint();
		SD.setAntiAlias(true);
		SD.setStrokeWidth(2);
		SD.setColor(Color.TRANSPARENT);
		SD.setStyle(Paint.Style.STROKE);

		setElevationRange(MBUTTON_ELEVATION_START, MBUTTON_ELEVATION_END);
		setOutline(false);

		setColor(UI.getPrimaryColor(getContext(), DEFAULT_BACKGROUND_COLOR));
		setDisabledColor(DISABLE_COLOR);
		setIconSize((int) UI.presetTextPaint(getContext(), getPaint()));
		_Ani = ValueAnimator.ofFloat(0, 0);
		_Ani.setDuration(PRESS_DURATION);
		_Ani.addUpdateListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			setOutlineProvider(RoundRectOutLineProvider.getInstance());
	}

	// Public Methods

	public int getRadius() {
		return Rad;
	}

	public void setRadius(int rad) {
		Rad = rad;
		postInvalidate();
	}

	public int getDisabledColor() {
		return CD;
	}

	public void setDisabledColor(int color) {
		CD = color;
	}

	public Drawable getIcon() {
		return getCompoundDrawables()[0];
	}

	public void setIconResource(int res) {
		setIcon(getContext().getDrawable(res));
	}

	public void setIcon(Drawable d) {
		d = d.mutate();
		UI.tintDrawable(d, getTextColors().getDefaultColor());
		setSize(_Size);
		d.setBounds(0, 0, _IconSize, _IconSize);
		Drawable[] q = getCompoundDrawables();
		setCompoundDrawables(d, q[1], q[2], q[3]);
	}

	public int getIconSize() {
		return _IconSize;
	}

	public void setIconSize(int size) {
		_IconSize = size;
		Drawable d = getCompoundDrawables()[0];
		if (d != null) d.setBounds(0, 0, size, size);
	}

	public boolean isLink() {
		return isOutline() && (!isShowBorder());
	}

	public void setLink(boolean flag) {
		if (flag) {
			setOutline(true);
			setShowBorder(false);
		} else {
			setOutline(false);
			setShowBorder(true);
		}
	}

	public boolean isShowBorder() {
		return _Border;
	}

	public void setShowBorder(boolean border) {
		_Border = border;
		if (border) SD.setAlpha(255);
		postInvalidate();
	}

	public boolean isOutline() {
		return _Outline;
	}

	public void setOutline(boolean outline) {
		if (_Outline == outline) return;
		float tmp;
		if (outline) {
			setContentColor(_FColor);
			setElevation(_EStart = tmp = 0);
			_TColor = Color.WHITE;
		} else {
			setElevation(_EStart = tmp = __Tmp_EStart);
			_TColor = _Color;
			SD.clearShadowLayer();
			P.setAlpha(255);
		}
		_Outline = outline;
		updateColor();
		setElevation(_EStart = tmp);
		postInvalidate();
	}

	public int getSize() {
		return _Size;
	}

	public void setSize(int size) {
		_Size = size;
		int d = Rad / 2;
		int l = d * ((size >>> 16) & 65535);
		int t = d * (size & 65535);
		int q = Rad << 1;
		if (getIcon() == null)
			setPadding(l, t, l, t);
		else
			setPadding(l - q, t, l - q, t);
		setCompoundDrawablePadding(q);
		requestLayout();
		postInvalidate();
	}

	public void setColor(int color) {
		setColor(color, UI.isDark(color));
	}

	public void setColor(int color, boolean dark) {
		_Color = color;
		updateColor();
		_Outline = !_Outline;
		setOutline(!_Outline);
		postInvalidate();
	}

	public int getColor() {
		return _Color;
	}

	public void setElevationRange(float from, float to) {
		__Tmp_EStart = _EStart = from;
		_EEnd = to;
		setElevation(_EStart);
		postInvalidate();
	}

	public void setElevationStart(float from) {
		__Tmp_EStart = _EStart = from;
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

	// Override Methods

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		if (!isEnabled()) return ret;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setTranslationY(_EEnd > _EStart ? -TRANSLATION : TRANSLATION);
				if (_Ani.isRunning()) _Ani.cancel();
				_Ani.setFloatValues(0, 1);
				_Ani.start();
				postInvalidate();
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setTranslationY(0);
				if (_Ani.isRunning()) _Ani.cancel();
				_Ani.setFloatValues(1, 0);
				_Ani.start();
				postInvalidate();
				break;
		}
		return ret;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		updateColor();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int sx = getScrollX(), sy = getScrollY();
		tmpRect.set(sx, sy, sx + getWidth(), sy + getHeight());
		if (_Outline) {
			P.setColor(_FColor);
			SD.setColor(_FColor);
			if (_Border) {
				canvas.drawRoundRect(tmpRect, Rad, Rad, SD);
			} else {
				SD.setAlpha(_Alpha);
				canvas.drawRoundRect(tmpRect, Rad, Rad, SD);
			}
			P.setAlpha(_Alpha);
		}
		canvas.drawRoundRect(tmpRect, Rad, Rad, P);
		super.onDraw(canvas);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		float pro = (float) animation.getAnimatedValue();
		setElevation((_EEnd - _EStart) * pro + _EStart);
		if (_Outline) {
			_Alpha = (int) (255 * pro);
			//P.setColor(_TColor = UI.transformColor(_FColor, Color.WHITE, 1 - pro));
			setContentColor(UI.transformColor(_FColor, UI.isDark(_FColor) ? Color.WHITE : Color.BLACK, pro));
			postInvalidate();
		} else {
			P.setColor(_TColor = UI.darkenColor(_Color, (int) (DARKEN * pro)));
			postInvalidate();
		}
	}

	public int getContentColor() {
		return CC;
	}

	public void setContentColor(int color) {
		CC = color;
		setContentColorInternal(color);
	}

	@Override
	public void setElevation(float elevation) {
		if (_Outline) {
			SD.setShadowLayer(elevation, 0, elevation / 3, 0x91000000);
			postInvalidate();
		} else {
			_Ele = Integer.MIN_VALUE;
			super.setElevation(elevation);
		}
		_Ele = elevation;
	}

	@Override
	public float getElevation() {
		return _Ele;
	}

	// Private Methods

	private void setContentColorInternal(int color) {
		Drawable d = getIcon();
		UI.tintDrawable(d, color);
		setTextColor(color);
	}

	protected void updateColor() {
		if (isEnabled()) {
			final boolean dark = UI.isDark(_Color);
			P.setColor(_Color);
			_FColor = dark ? _Color : Color.BLACK;
			if (_Outline)
				setContentColor(_Color);
			else
				setContentColor(CC);
		} else {
			P.setColor(CD);
			_FColor = CD;
			setContentColorInternal(_Outline ? CD : Color.LTGRAY);
		}
	}
}