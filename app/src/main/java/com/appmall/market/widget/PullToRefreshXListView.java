package com.appmall.market.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.appmall.market.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase;

public class PullToRefreshXListView extends PullToRefreshAdapterViewBase<LoadMoreListView> {

	public PullToRefreshXListView(Context context) {
		super(context);
	}

	public PullToRefreshXListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshXListView(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshXListView(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public PullToRefreshBase.Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected LoadMoreListView createRefreshableView(Context context, AttributeSet attrs) {
		LoadMoreListView ret = new LoadMoreListView(context, attrs);
		ret.setId(android.R.id.list);
		return ret;
	}

}
