package com.appmall.market.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.adapter.AppRankAdapter;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Rank;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.DataPage;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.widget.LoadMoreListView;
import com.appmall.market.widget.LoadMoreListView.OnLoadMoreListener;
import com.appmall.market.widget.PullToRefreshXListView;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.Mode;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

/**
 * 下载热榜界面
 *  
 *
 */
public class AppRankFragment extends BaseFragment implements IDataCallback, OnLoadMoreListener, AppRankAdapter.Callback {

	public static final String EXTRA_CATEGORY_TYPE = "category_type";
	public static final String EXTRA_URL = "url";
	
	public static final int CATEGORY_TYPE_SOFT = 0;
	public static final int CATEGORY_TYPE_GAME = 1;
	public static final int CATEGORY_TYPE_DETAIL = 2;
	
	private View mContentView;
	private PullToRefreshXListView mListViewContainer;
	private LoadMoreListView mListView;
	private AppRankAdapter mAdapter;
	private ArrayList<Advert> mAdvertData;
	private ArrayList<App> mAppData;
	private DataPage mDataPage;
	private int mDataId;
	private Toast mLoadMoreFailedToast;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}
		
		Context context = getActivity();
		mContentView = inflater.inflate(R.layout.fragment_app_rank, null);
		mListViewContainer = (PullToRefreshXListView) mContentView.findViewById(R.id.ptr_listview);	
		mListViewContainer.setMode(Mode.MANUAL_REFRESH_ONLY);
		mListViewContainer.setOnRefreshListener(mPullToRefreshListener);
		Utils.setupPTRLoadingProxy(context.getResources(), mListViewContainer.getLoadingLayoutProxy());
		
		mListView = mListViewContainer.getRefreshableView();
		mListView.setOnLoadMoreListener(this);
		
		mAdvertData = new ArrayList<Advert>();
		mAppData = new ArrayList<App>();
		mAdapter = new AppRankAdapter(context);
		mAdapter.registerCallback(this);
		mDataPage = new DataPage();
		mListView.setAdapter(mAdapter);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		
		mInitData = false;
		return mContentView;
	}
	
	private boolean mInitData = false;
	public void onFragmentActive() {
		if(!mInitData) {
			mInitData = true;
			requestData();
		}
	}

	private void requestData() {
		if (mDataPage == null || mDataPage.getPageCount() <= 0) {
			Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		}
		
		mDataId = IDataConstant.APP_RANK;
		String url = null;
		Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_CATEGORY_TYPE)) {
			int type = args.getInt(EXTRA_CATEGORY_TYPE);
			if (type == CATEGORY_TYPE_GAME) {
				mDataId = IDataConstant.GAME_RANK;
			} else if (type == CATEGORY_TYPE_SOFT) {
				// Keep mDataId == APP_RANK
			} else if (type == CATEGORY_TYPE_DETAIL && args.containsKey(EXTRA_URL)) {
				mDataId = IDataConstant.CATEGORY_DETAIL_HOT;
				url = args.getString(EXTRA_URL);
			}
		}
		Options options = new Options();
		options.mCustomUrl = url;
		DataCenter.getInstance().requestDataAsync(getActivity(), mDataId, this, options);
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (!isAdded())
			return;
		
		mListView.onLoadMoreComplete();
		mListViewContainer.onRefreshComplete();
		Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		
		if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof Rank)) {
			// 数据错误
//			if (mDataPage.getPageCount() >= 1) {
//				showLoadMoreFailedToast();
//				return;
//			}
			if(mLoadMore) {
				showLoadMoreFailedToast();
				mListViewContainer.setMode(Mode.PULL_FROM_END);
				return;
			}
			showLoadingFailedLayout();
			return;
		}
		
		mListView.setVisibility(View.VISIBLE);
		mListViewContainer.setMode(Mode.MANUAL_REFRESH_ONLY);
		mListViewContainer.setVisibility(View.VISIBLE);
		
		Rank rank = (Rank) resp.mData;
		resolveRank(rank);
		refreshAppData();
		
		boolean hasNext = mDataPage.parseNextUrlFromContext(resp.mContext);
		mListView.setNeedLoadMore(hasNext);
		mDataPage.increaseCount();
		if (!hasNext && mDataPage.getPageCount() > 1) {
			mListView.showEndFooterView();
		}
	}

	private void showLoadMoreFailedToast() {
		if (mLoadMoreFailedToast == null) {
			mLoadMoreFailedToast = Toast.makeText(getActivity(), R.string.load_more_failed, Toast.LENGTH_SHORT);
		}
		mLoadMoreFailedToast.show();
	}
	
	private boolean isLoadingFailedLayoutVisible() {
		if (mContentView == null)
			return false;
		
		return mContentView.findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE;
	}
	
	private void showLoadingFailedLayout() {
		View failedLayout = mContentView.findViewById(R.id.loading_failed_layout);
		TextView resultText = (TextView) failedLayout.findViewById(R.id.failed_result);
		TextView tipText = (TextView) failedLayout.findViewById(R.id.failed_tip);
		Button failedButton = (Button) failedLayout.findViewById(R.id.failed_tip_button);
		failedLayout.setVisibility(View.VISIBLE);
		boolean hasNetwork = Utils.isNetworkAvailable(getActivity());
		if (hasNetwork) {
			resultText.setText(R.string.network_not_good);
			tipText.setText(R.string.click_button_refresh_later);
			failedButton.setText(R.string.click_to_refresh);
			failedButton.setOnClickListener(mOnRefreshButtonClicked);
		} else {
			resultText.setText(R.string.network_not_connected);
			tipText.setText(R.string.click_button_setting_network);
			failedButton.setText(R.string.network_setting);
			failedButton.setOnClickListener(mOnSettingNetworkClicked);
		}
	}
	
	private void resolveRank(Rank rank) {
		if (rank == null)
			return;

		if (rank.mAdverts != null) {
			mAdvertData.addAll(rank.mAdverts);
		}
		if (rank.mApps != null) {
			mAppData.addAll(rank.mApps);
		}
		
		ArrayList<ItemDataWrapper> itemData = new ArrayList<ItemDataWrapper>();
		int advCount = mAdvertData.size();
		int index = 0;
		while (index + 2 <= advCount) {
			Advert left = mAdvertData.get(index);
			Advert right = mAdvertData.get(index + 1);
			CategoryAdvert ca = new CategoryAdvert(left, right);
			itemData.add(new ItemDataWrapper(ca, AppRankAdapter.TYPE_ADVERT));
			index += 2;
		}
		
		for (App app : mAppData) {
			itemData.add(new ItemDataWrapper(app, AppRankAdapter.TYPE_APP));
		}
		
		mAdapter.setData(itemData);
	}

	@Override
	protected void onNetworkStateChanged() {
		if (!isAdded())
			return;
		
		if (mContentView.findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE)
			showLoadingFailedLayout();
	}
	
	@Override
	public void onDownload(DownInfoHolder downInfo) {
		CommonInvoke.processAppDownBtn(getActivity(), downInfo.mIcon, downInfo.mItem, true);
	}

	@Override
	public void onAppDetail(App app) {
		Utils.jumpAppDetailActivity(getActivity(), app);
	}
	
	@Override
	protected void onTaskProgress(DownloadTask task) {
		if (task == null || TextUtils.isEmpty(task.mPackageName))
			return;
		Utils.handleButtonProgress(mContentView, R.id.down_button, task);
	}

	private boolean mLoadMore = false;
	@Override
	public void onLoadMore() {
		String url = mDataPage.getNextUrl();
		if (TextUtils.isEmpty(url)) {
			mListView.onLoadMoreComplete();
			return;
		}
		mLoadMore = true;
		
		Options options = new Options();
		options.mCustomUrl = url;
		DataCenter.getInstance().requestDataAsync(getActivity(), mDataId, this, options);
	}
	
	@Override
	protected void refreshAppData() {
		if (isLoadingFailedLayoutVisible())
			showLoadingFailedLayout();
		
		if (mAppData == null || mAdapter == null)
			return;
		
		CommonInvoke.processApps(DataCenter.getInstance(), mAppData, false);
		mAdapter.notifyDataSetChanged();
	}
	
	private OnClickListener mOnRefreshButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			View failedLayout = mContentView.findViewById(R.id.loading_failed_layout);
			failedLayout.setVisibility(View.GONE);
			requestData();
		}
	};
	
	private OnClickListener mOnSettingNetworkClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utils.jumpNetworkSetting(getActivity());
		}
	};

	@Override
	public void onAdvertClicked(Advert advert) {
		if (advert == null)
			return;
		
		Utils.jumpDetailActivity(getActivity(), advert);
	}
	
	private OnRefreshListener<LoadMoreListView> mPullToRefreshListener = new OnRefreshListener<LoadMoreListView>() {
		@Override
		public void onRefresh(PullToRefreshBase<LoadMoreListView> refreshView) {
			onLoadMore();
		}
	};
	
}
