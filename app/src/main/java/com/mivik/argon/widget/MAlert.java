package com.mivik.argon.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.mivik.argon.drawable.CrossDrawable;

public class MAlert extends MCard {
	private ImageView Left, Close;
	private TextView Content;
	private int IconSize;

	public MAlert(Context cx) {
		this(cx, null, 0);
	}

	public MAlert(Context cx, AttributeSet attrs) {
		this(cx, attrs, 0);
	}

	public MAlert(Context cx, AttributeSet attrs, int style) {
		super(cx, attrs, style);
		setOrientation(MAlert.HORIZONTAL);
		setClickable(false);
		setGravity(Gravity.CENTER_VERTICAL);
		Left = new ImageView(cx);
		addView(Left);
		Content = new TextView(cx);
		LayoutParams para = new LayoutParams(0, -2);
		para.weight = 1;
		addView(Content, para);
		Close = new ImageView(cx);
		Close.setImageDrawable(new CrossDrawable());
		Close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(View.GONE);
			}
		});
		addView(Close);

		setIconSize((int) UI.presetTextPaint(cx, Content.getPaint()));
		setColor(UI.getPrimaryColor(cx, DEFAULT_BACKGROUND_COLOR));
	}

	public void setText(CharSequence cs) {
		Content.setText(cs);
	}

	public Drawable getIcon() {
		return Left.getDrawable();
	}

	public void setIcon(Drawable d) {
		if (d == null) {
			Left.setImageDrawable(null);
			return;
		}
		d = d.mutate();
		d.setBounds(0, 0, IconSize, IconSize);
		Left.setImageDrawable(d);
	}

	public int getIconSize() {
		return IconSize;
	}

	public void setIconSize(int size) {
		IconSize = size;
		if (Left.getDrawable() != null) {
			Left.getDrawable().setBounds(0, 0, size, size);
			Left.postInvalidate();
		}
		LayoutParams para = new LayoutParams(IconSize, IconSize);
		para.rightMargin = IconSize * 2 / 3;
		Left.setLayoutParams(para);
		Left.requestLayout();
		para = new LayoutParams(IconSize, IconSize);
		Close.setLayoutParams(para);
		Close.requestLayout();
	}

	private void setContentColor(int color) {
		Drawable d = getIcon();
		UI.tintDrawable(d, color);
		Content.setTextColor(color);
	}

	@Override
	public void setColor(int color) {
		super.setColor(color);
		if (Left == null) return;
		boolean dark = UI.isDark(color);
		setContentColor(dark ? Color.WHITE : Color.BLACK);
	}
}
