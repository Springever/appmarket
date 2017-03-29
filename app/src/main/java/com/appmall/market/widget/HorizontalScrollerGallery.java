package com.appmall.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class HorizontalScrollerGallery extends HorizontalScrollView {

	public static interface ScrollerListener {
		public void onSizeChanged(int view, int total);
		public void onScroll(int offset, int view, int total);
	}

	private ScrollerListener mListener;
	private int mViewWidth;
	private int mTotalWidth;
	
	public HorizontalScrollerGallery(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public HorizontalScrollerGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HorizontalScrollerGallery(Context context) {
		super(context);
	}

	public void setOnScrollListener(ScrollerListener l) {
		mListener = l;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		if (changed && mListener != null) {
			mViewWidth = getWidth();
			mTotalWidth = getChildAt(0).getWidth();
			mListener.onSizeChanged(mViewWidth, mTotalWidth);
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (mListener != null) {
			mListener.onScroll(l, mViewWidth, mTotalWidth);
		}
	}

}
