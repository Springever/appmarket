package com.appmall.market.widget;

import java.util.Arrays;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.appmall.market.R;

/**
 * 显示圆角的ImageView
 *  
 *
 */
public class RoundImageView extends ImageView {

	private Path mPath = new Path();
	private float mRadius;
	private boolean mRightCornerNotRound;

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RoundImageView(Context context) {
		super(context);
		init();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void init() {
		if (Build.VERSION.SDK_INT >= 11)
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		mRadius = getResources().getDimension(R.dimen.round_image_view_radius);
	}

	public void setRadius(float radius) {
		mRadius = radius;
		computePath();
		invalidate();
	}
	
	public void setRightCornerNotRound(boolean b) {
		mRightCornerNotRound = b;
		computePath();
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if (w != oldw || h != oldh) {
			computePath();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int saveCount = canvas.save();
		canvas.clipPath(mPath);
		super.onDraw(canvas);
		canvas.restoreToCount(saveCount);
	}

	private void computePath() {
		int scrollX = getScrollX();
		int scrollY = getScrollY();
		int left = scrollX + getPaddingLeft();
		int top = scrollY + getPaddingTop();
		int right = scrollX + getRight() - getLeft() - getPaddingRight();
		int bottom = scrollY + getBottom() - getTop() - getPaddingBottom();
		RectF rect = new RectF(left, top, right, bottom);
		float[] radii = new float[8];
		if (mRightCornerNotRound) {
			radii[0] = radii[1] = mRadius;
			radii[2] = radii[3] = 0;
			radii[4] = radii[5] = 0;
			radii[6] = radii[7] = mRadius;
		} else {
			Arrays.fill(radii, mRadius);
		}
		mPath.reset();
		mPath.addRoundRect(rect, radii, Direction.CW);
	}
	
}
