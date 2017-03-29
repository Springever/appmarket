package com.appmall.market.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class DownStatusButton extends Button {

	private Drawable mProgressDrawable;
	private float mProgress;
	private Rect mBounds;
	private boolean mProgressVisible;

	public DownStatusButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initView();
	}

	public DownStatusButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView();
	}

	public DownStatusButton(Context context) {
		super(context);
		
		initView();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if (w != oldw || h != oldh) {
			mBounds = new Rect(0, 0, w, h);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initView() {
		if (Build.VERSION.SDK_INT >= 11) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	public void setProgressDrawable(Drawable drawable) {
		mProgressDrawable = drawable;
	}
	
	public void setProgress(float progress) {
		mProgress = progress;
		invalidate();
	}

	public void setProgressVisible(boolean visible) {
		mProgressVisible = visible;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (mProgressVisible && !isButtonPressed()) {
			int width = getWidth();
			int height = getHeight();
			int progressWidth = (int) (mProgress * width / 100f);
			mProgressDrawable.setBounds(mBounds);		
			canvas.save();
			canvas.clipRect(0, 0, progressWidth, height);
			mProgressDrawable.draw(canvas);
			canvas.restore();
		}
		
		super.onDraw(canvas);
	}

	final private boolean isButtonPressed() {
		int[] state = getDrawableState();
		int index = 0;
		for (;index < state.length; ++index)
			if (state[index] == android.R.attr.state_pressed)
				return true;
		
		return false;
	}
	
}
