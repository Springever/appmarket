package com.appmall.market.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

public class AutoSwitchViewPager extends ViewPager {

	private static final float HORI_ANGLE = 3f;
	
	private SwitchHandler mSwitcher;
	private boolean mAttached;
	private float mLastTouchX;
	private float mLastTouchY;

	public AutoSwitchViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoSwitchViewPager(Context context) {
		super(context);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		float distanceX = Math.abs(event.getX() - mLastTouchX);
		float distanceY = Math.abs(event.getY() - mLastTouchY);
		if (distanceX / distanceY > HORI_ANGLE) {
			// Horizontal move
			ViewParent parent = getParent();
			if (parent != null)
				parent.requestDisallowInterceptTouchEvent(true);
		}
		
		mLastTouchX = event.getX();
		mLastTouchY = event.getY();
		
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (mSwitcher != null)
				mSwitcher.stop();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mSwitcher != null)
				mSwitcher.start();
			break;
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
		
		if (mSwitcher != null) {
			mSwitcher.stop();
			mSwitcher = null;
		}
		
		mSwitcher = new SwitchHandler(adapter, this);
		if (mAttached)
			mSwitcher.start();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		mAttached = true;
		if (mSwitcher != null)
			mSwitcher.start();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mSwitcher != null)
			mSwitcher.stop();
		
		mAttached = false;
		super.onDetachedFromWindow();
	}

	private static class SwitchHandler extends Handler {
		private static final long DEFAULT_SWITCH_DELAY = 5000L;
		
		private WeakReference<PagerAdapter> mAdapterRef;
		private WeakReference<ViewPager> mPagerRef;
		private boolean mStart;
		private long mSwitchDelay = DEFAULT_SWITCH_DELAY;

		public SwitchHandler(PagerAdapter adapter, ViewPager pager) {
			mAdapterRef = new WeakReference<PagerAdapter>(adapter);
			mPagerRef = new WeakReference<ViewPager>(pager);
			mStart = false;
		}
		
		public void stop() {
			removeMessages(0);
			mStart = false;
		}
		
		public void start() {
			if (mStart)
				return;
			
			mStart = true;
			sendEmptyMessageDelayed(0, DEFAULT_SWITCH_DELAY);
		}

		@Override
		public void handleMessage(Message msg) {
			removeMessages(0);
			if (!mStart)
				return;
			
			ViewPager pager = mPagerRef.get();
			PagerAdapter adapter = mAdapterRef.get();
			if (pager == null || adapter == null) {
				stop();
				return;
			}
			
			int size = adapter.getCount();
			if (size <= 0) {
				stop();
				return;
			}
			
			int nextIndex = pager.getCurrentItem() + 1;
			if (nextIndex >= size)
				nextIndex = 0;
			pager.setCurrentItem(nextIndex, true);
			
			sendEmptyMessageDelayed(0, mSwitchDelay);
		}
	}
	
}
