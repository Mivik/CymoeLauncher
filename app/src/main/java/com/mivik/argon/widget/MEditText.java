package com.mivik.argon.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import com.mivik.argon.drawable.CursorDrawable;

public class MEditText extends MButton {
	public static final int SWITCH_ICON_SIZE = UI.dp2px(24), PADDING = UI.dp2px(10);
	public static final int INPUTTYPE_TEXT_PASSWORD = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
			INPUTTYPE_TEXT_VISIBLE_PASSWORD = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
			INPUTTYPE_NUMBER_PASSWORD = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD,
			INPUTTYPE_NUMBER_VISIBLE_PASSWORD = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL;

	private CursorDrawable Cursor;
	private Drawable DToVisible, DToInvisible;
	private int _S;

	public MEditText(Context cx) {
		this(cx, null, 0);
	}

	public MEditText(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MEditText(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);
		setFocusable(true);
		setFocusableInTouchMode(true);
		UI.setEditTextCursor(this, Cursor = new CursorDrawable());
		setColor(getColor());

		setSwitchIconSize(SWITCH_ICON_SIZE);
	}

	public int getSwitchIconSize() {
		return _S;
	}

	public void setSwitchIconSize(int size) {
		_S = size;
		updateSwitchDrawable();
		postInvalidate();
	}

	public void setPasswordSwitchResource(int tv, int ti) {
		setPasswordSwitch(getContext().getResources().getDrawable(tv), getContext().getResources().getDrawable(ti));
	}

	public boolean isSwitchPassword() {
		return DToVisible != null;
	}

	public void setPasswordSwitch(Drawable tv, Drawable ti) {
		if ((tv == null) != (ti == null)) throw new IllegalArgumentException();
		if (tv == null) {
			DToVisible = DToInvisible = null;
			postInvalidate();
			return;
		}
		DToVisible = tv.mutate();
		DToInvisible = ti.mutate();
		DToVisible.setCallback(this);
		DToInvisible.setCallback(this);
		updateSwitchDrawable();
		postInvalidate();
	}

	public boolean isPasswordVisible() {
		final int variation = getInputType() & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
		return (variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) ||
				(variation == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_NORMAL));
	}

	public void setPasswordVisible(boolean flag) {
		final int variation = getInputType() & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
		if (variation == INPUTTYPE_NUMBER_PASSWORD || variation == INPUTTYPE_NUMBER_VISIBLE_PASSWORD)
			setInputType(flag ? INPUTTYPE_NUMBER_VISIBLE_PASSWORD : INPUTTYPE_NUMBER_PASSWORD);
		else
			setInputType(flag ? INPUTTYPE_TEXT_VISIBLE_PASSWORD : INPUTTYPE_TEXT_PASSWORD);
		postInvalidate();
	}

	public boolean isInputPassword() {
		final int variation = getInputType() & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
		return variation == INPUTTYPE_NUMBER_PASSWORD
				|| variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
				|| variation == INPUTTYPE_NUMBER_PASSWORD;
	}

	public void setSelection(int start, int stop) {
		Selection.setSelection(getText(), start, stop);
	}

	public void setSelection(int index) {
		Selection.setSelection(getText(), index);
	}

	public void selectAll() {
		Selection.selectAll(getText());
	}

	public void extendSelection(int index) {
		Selection.extendSelection(getText(), index);
	}

	public void switchPasswordVisibility() {
		setPasswordVisible(isPasswordVisible() ? false : true);
	}

	@Override
	public void setInputType(int type) {
		int st = getSelectionStart(), en = getSelectionEnd();
		super.setInputType(type);
		setSelection(st, en);
	}

	@Override
	public boolean getFreezesText() {
		return true;
	}

	@Override
	protected boolean getDefaultEditable() {
		return true;
	}

	@Override
	protected MovementMethod getDefaultMovementMethod() {
		return ArrowKeyMovementMethod.getInstance();
	}

	@Override
	public Editable getText() {
		CharSequence text = super.getText();
		if (text == null) return null;
		if (text instanceof Editable) return (Editable) super.getText();
		super.setText(text, BufferType.EDITABLE);
		return (Editable) super.getText();
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, BufferType.EDITABLE);
	}

	@Override
	public void setEllipsize(TextUtils.TruncateAt ellipsis) {
		if (ellipsis == TextUtils.TruncateAt.MARQUEE)
			throw new IllegalArgumentException("EditText cannot use the ellipsize mode TextUtils.TruncateAt.MARQUEE");
		super.setEllipsize(ellipsis);
	}

	@Override
	public CharSequence getAccessibilityClassName() {
		return EditText.class.getName();
	}

	@Override
	public void setContentColor(int color) {
		super.setContentColor(color);
		if (Cursor == null) return;
		UI.tintDrawable(DToVisible, color);
		UI.tintDrawable(DToInvisible, color);
		Cursor.setColor(color);
		setHintTextColor(color);
		postInvalidate();
	}

	private boolean _ClickedPassword;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		if (DToVisible == null) return ret;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!(_ClickedPassword = clickedPassword((int) event.getX(), (int) event.getY()))) break;
				return true;
			case MotionEvent.ACTION_MOVE:
				if (!_ClickedPassword) break;
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (!(_ClickedPassword = clickedPassword((int) event.getX(), (int) event.getY()))) break;
				switchPasswordVisibility();
				return true;
		}
		return ret;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isSwitchPassword()) {
			int tx = getScrollX() + getWidth() - _S - PADDING, ty = getScrollY() + (getHeight() >> 1) - (_S >> 1);
			canvas.translate(tx, ty);
			(isPasswordVisible() ? DToVisible : DToInvisible).draw(canvas);
			canvas.translate(-tx, -ty);
		}
	}

	private void updateSwitchDrawable() {
		if (DToVisible == null) {
			setPadding(getPaddingLeft(), getPaddingTop(), getPaddingLeft(), getPaddingBottom());
			return;
		}
		setPadding(getPaddingLeft(), getPaddingTop(), (PADDING << 1) + _S, getPaddingBottom());
		UI.tintDrawable(DToVisible, getContentColor());
		UI.tintDrawable(DToInvisible, getContentColor());
		DToVisible.setBounds(0, 0, _S, _S);
		DToInvisible.setBounds(0, 0, _S, _S);
	}

	private boolean clickedPassword(int x, int y) {
		int tx = getWidth() - _S - PADDING, ty = (getHeight() >> 1) - (_S >> 1);
		return x >= tx && x <= (tx + _S) && y >= ty && y <= (ty + _S);
	}
}
