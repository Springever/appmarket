package com.appmall.market.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.appmall.market.R;

public class RankStarWidget extends View {

	private Drawable mRankStar;
	private Drawable mEmptyStar;
	private int mRankValue;
	private int mStarMargin;
	private boolean mEnableRank;
	private RankListener mListener;

	public RankStarWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public RankStarWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public RankStarWidget(Context context) {
		super(context);
		initView();
	}

	@SuppressLint("NewApi")
	private void initView() {
		if (Build.VERSION.SDK_INT >= 11) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		Resources res = getContext().getResources();
		mRankStar = res.getDrawable(R.drawable.rank_star);
		mEmptyStar = res.getDrawable(R.drawable.empty_star);
	}

	public void setListener(RankListener listener) {
		mListener = listener;
	}
	
	public void setRank(int rankValue) {
		if (rankValue < 0)
			rankValue = 0;
		if (rankValue > 10)
			rankValue = 10;
		
		if (rankValue != mRankValue) {
			mRankValue = rankValue;
			invalidate();
			
			if (mListener != null) {
				mListener.onRankChanged();
			}
		}
	}

	public int getRank() {
		return mRankValue;
	}
	
	public void setStarDrawable(int rankRes, int emptyRes) {
		Resources res = getContext().getResources();
		mRankStar = res.getDrawable(rankRes);
		mEmptyStar = res.getDrawable(emptyRes);
	}
	
	public void enableRank(boolean enable) {
		mEnableRank = enable;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mEnableRank)
			return super.onTouchEvent(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			touchOnPos((int) event.getX(), (int) event.getY());
			return true;
		default:
			return super.onTouchEvent(event);
		}
	}

	private void touchOnPos(int x, int y) {
		int width = getWidth();
		int starWidth = (int) ((width - 4f * mStarMargin) / 5f);
		
		int value = 0;
		if (x <= 0) {
			value = 2;
		} else if (x > width) {
			value = 10;
		} else {
			int index = x / (starWidth + mStarMargin) + 1;
			value = index * 2;
		}

		
		if (value != mRankValue) {
			mRankValue = value;
			invalidate();
			
			if (mListener != null) {
				mListener.onRankChanged();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = getWidth();
		int height = getHeight();
		
		int starWidth = (int) ((width - 4f * mStarMargin) / 5f);
		mEmptyStar.setBounds(0, 0, starWidth, height);
		mRankStar.setBounds(0, 0, starWidth, height);
		
		for (int i = 0; i < 5; ++i) {
			mEmptyStar.setBounds(i * starWidth + i * mStarMargin, 0, (i + 1) * starWidth + i * mStarMargin, height);
			mEmptyStar.draw(canvas);
		}
		
		int marginCount = (int) (mRankValue / 2f);
		canvas.save();
		canvas.clipRect(0, 0, starWidth * (mRankValue / 2f) + marginCount * mStarMargin, height);
		for (int i = 0; i < 5; ++i) {
			mRankStar.setBounds(i * starWidth + i * mStarMargin, 0, (i + 1) * starWidth + i * mStarMargin, height);
			mRankStar.draw(canvas);
		}
		canvas.restore();
	}

	public void setStarMargin(int margin) {
		mStarMargin = margin;
	}
	
	public static interface RankListener {
		public void onRankChanged();
	}
	
}
