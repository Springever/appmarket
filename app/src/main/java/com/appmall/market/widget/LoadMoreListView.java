package com.appmall.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.appmall.market.R;

/**
 * 支持自动分页加载的ListView
 * 
 *  
 * 
 */
public class LoadMoreListView extends ListView implements OnScrollListener {

	/**
	 * Listener that will receive notifications every time the list scrolls.
	 */
	private OnScrollListener mOnScrollListener;
	private LayoutInflater mInflater;

	// footer view
	private RelativeLayout mFooterView;
	private View mLoadMoreLayout;
	private View mLoadEndLayout;

	// Listener to process load more items when user reaches the end of the list
	private OnLoadMoreListener mOnLoadMoreListener;

	private boolean mLoadMore = false;
	// To know if the list is loading more items
	private boolean mIsLoadingMore = false;
	private int mCurrentScrollState;
	private boolean mChildFillView;

	public LoadMoreListView(Context context) {
		super(context);
		init(context);
	}

	public LoadMoreListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFooterView = (RelativeLayout) mInflater.inflate(R.layout.load_more_footer, this, false);
		mLoadMoreLayout = mFooterView.findViewById(R.id.load_more_layout);
		mLoadEndLayout = mFooterView.findViewById(R.id.load_end_layout);
		addFooterView(mFooterView);

		super.setOnScrollListener(this);
	}

	public void setNeedLoadMore(boolean loadMore) {
		mLoadMore = loadMore;
	}
	
	public void showEndFooterView() {
		mLoadMoreLayout.setVisibility(View.GONE);
		mLoadEndLayout.setVisibility(View.VISIBLE);
	}
	
	public void hideEndFooterView() {
		mLoadEndLayout.setVisibility(View.GONE);
	}
	
	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		
		int count = getCount();
		int lastPosition = getLastVisiblePosition();
		mChildFillView = (lastPosition < count - 1);
	}

	public boolean isChildFillView() {
		return mChildFillView;
	}
	
	/**
	 * Set the listener that will receive notifications every time the list
	 * scrolls.
	 */
	@Override
	public void setOnScrollListener(AbsListView.OnScrollListener l) {
		mOnScrollListener = l;
	}

	/**
	 * Register a callback to be invoked when this list reaches the end (last
	 * item be visible)
	 */
	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		mOnLoadMoreListener = onLoadMoreListener;
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}

		if (mOnLoadMoreListener != null) {
			boolean loadMore = mLoadMore && firstVisibleItem + visibleItemCount >= totalItemCount - 1;
			if (!mIsLoadingMore && loadMore && mCurrentScrollState != SCROLL_STATE_IDLE) {
				mLoadMoreLayout.setVisibility(View.VISIBLE);
				mIsLoadingMore = true;
				onLoadMore();
			}
		}
	}

	public void loadManual() {
		if (mIsLoadingMore)
			return;

		mLoadMoreLayout.setVisibility(View.VISIBLE);
		mIsLoadingMore = true;
		onLoadMore();
	}
	
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	public void onLoadMore() {
		if (mOnLoadMoreListener != null) {
			mOnLoadMoreListener.onLoadMore();
		}
	}

	/**
	 * Notify the loading more operation has finished
	 */
	public void onLoadMoreComplete() {
		mIsLoadingMore = false;
		mLoadMoreLayout.setVisibility(View.GONE);
	}

	/**
	 * Interface definition for a callback to be invoked when list reaches the
	 * last item (the user load more items in the list)
	 */
	public interface OnLoadMoreListener {
		/**
		 * Called when the list reaches the last item (the last item is visible to the user)
		 */
		public void onLoadMore();
	}

}
