package com.appmall.market.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.adapter.AppNewProdAdapter;
import com.appmall.market.adapter.AppNewProdAdapter.Callback;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Group;
import com.appmall.market.bean.NewProd;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.DataPage;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.Mode;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.appmall.market.widget.LoadMoreListView;
import com.appmall.market.widget.PullToRefreshXListView;
import com.appmall.market.widget.LoadMoreListView.OnLoadMoreListener;

/**
 * 新品速递页面
 *  
 *
 */
public class AppNewProdFragment extends BaseFragment implements IDataCallback, Callback, OnLoadMoreListener {

	public static final String EXTRA_CATEGORY_TYPE = "category_type";
	public static final String EXTRA_URL = "url";
	
	public static final int CATEGORY_TYPE_SOFT = 0;
	public static final int CATEGORY_TYPE_GAME = 1;
	public static final int CATEGORY_TYPE_DETAIL = 2;
	
	private View mContentView;
	private PullToRefreshXListView mListViewContainer;
	private LoadMoreListView mListView;
	private AppNewProdAdapter mAdapter;
	private DataPage mDataPage;
	
	private ArrayList<Group> mGroupData;
	private ArrayList<Advert> mAdvertData;
	private ArrayList<App> mAppData;
	
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
		mContentView = inflater.inflate(R.layout.fragment_app_newprod, null);
		mListViewContainer = (PullToRefreshXListView) mContentView.findViewById(R.id.ptr_listview);	
		mListViewContainer.setMode(Mode.MANUAL_REFRESH_ONLY);
		mListViewContainer.setOnRefreshListener(mPullToRefreshListener);
		Utils.setupPTRLoadingProxy(context.getResources(), mListViewContainer.getLoadingLayoutProxy());
				
		mListView = mListViewContainer.getRefreshableView();
		mListView.setOnLoadMoreListener(this);
		
		mAppData = new ArrayList<App>();
		mAdvertData = new ArrayList<Advert>();
		mGroupData = new ArrayList<Group>();
		
		mAdapter = new AppNewProdAdapter(context);
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
	
	@Override
	protected void onTaskProgress(DownloadTask task) {
		if (task == null || TextUtils.isEmpty(task.mPackageName))
			return;
		Utils.handleButtonProgress(mContentView, R.id.down_button, task);
	}
	
	private void requestData() {
		if (mDataPage == null || mDataPage.getPageCount() <= 0) {
			Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		}
		
		mDataId = IDataConstant.APP_NEW_PROD;
		String url = null;
		Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_CATEGORY_TYPE)) {
			int type = args.getInt(EXTRA_CATEGORY_TYPE);
			if (type == CATEGORY_TYPE_GAME) {
				mDataId = IDataConstant.GAME_NEW_PROD;
			} else if (type == CATEGORY_TYPE_SOFT) {
				// Keep mDataId == IDataConstant.APP_NEW_PROD
			} else if (type == CATEGORY_TYPE_DETAIL && args.containsKey(EXTRA_URL)) {
				mDataId = IDataConstant.CATEGORY_DETAIL_NEWPROD;
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
		
		if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof NewProd)) {
			// 数据错误
//			if (mDataPage.getPageCount() >= 1) {
//				showLoadMoreFailedToast();
//				return;
//			}
			if(mLoadMore) {
				showLoadMoreFailedToast();
				mListViewContainer.setMode(Mode.MANUAL_REFRESH_ONLY);
				mListViewContainer.setMode(Mode.PULL_FROM_END);
				return;
			}
			showLoadingFailedLayout();
			return;
		}
		
		mListView.setVisibility(View.VISIBLE);
		mListViewContainer.setMode(Mode.MANUAL_REFRESH_ONLY);
		mListViewContainer.setVisibility(View.VISIBLE);
		
		NewProd np = (NewProd) resp.mData;
		resolveNewProd(np);
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
	
	private void resolveNewProd(NewProd np) {
		if (np == null)
			return;
		
		if (np.mGroups != null)
			mGroupData.addAll(np.mGroups);
		if (np.mAdverts != null)
			mAdvertData.addAll(np.mAdverts);
		if (np.mApps != null)
			mAppData.addAll(np.mApps);
		
		SparseArray<ArrayList<App>> groupData = new SparseArray<ArrayList<App>>();
		sortGroup(mGroupData);
		for (Group group : mGroupData) {
			if (group == null)
				continue;
			groupData.append(group.mID, new ArrayList<App>());
		}
		for (App app : mAppData) {
			groupData.get(app.mGroupId).add(app);
		}
		
		ArrayList<ItemDataWrapper> itemData = new ArrayList<ItemDataWrapper>();
		int advCount = mAdvertData.size();
		int index = 0;
		while (index + 2 <= advCount) {
			Advert left = mAdvertData.get(index);
			Advert right = mAdvertData.get(index + 1);
			CategoryAdvert ca = new CategoryAdvert(left, right);
			itemData.add(new ItemDataWrapper(ca, AppNewProdAdapter.TYPE_ADVERT));
			index += 2;
		}
		
		for (Group group : mGroupData) {
			ArrayList<App> appList = groupData.get(group.mID);
			// Group里没有软件，过滤
			if (appList.size() <= 0)
				continue;
			
			itemData.add(new ItemDataWrapper(group.mName, AppNewProdAdapter.TYPE_ITEM_GROUP));
			for (App app : appList) {
				itemData.add(new ItemDataWrapper(app, AppNewProdAdapter.TYPE_ITEM_ITEM));
			}			
		}
		
		mAdapter.setData(itemData);
	}
	
	private void sortGroup(List<Group> groupList) {
		if (groupList == null)
			return;
		
		Collections.sort(groupList, new Comparator<Group>() {
			@Override
			public int compare(Group lhs, Group rhs) {
				if (lhs == null)
					return rhs == null ? 0 : -1;
				if (rhs == null)
					return 1;
				
				return lhs.mWeight - rhs.mWeight;
			}
		});
	}

	@Override
	public void onDownload(DownInfoHolder downInfo) {
		CommonInvoke.processAppDownBtn(getActivity(), downInfo.mIcon, downInfo.mItem, true);
	}

	@Override
	public void onAppDetail(App app) {
		Utils.jumpAppDetailActivity(getActivity(), app);
	}

	boolean mLoadMore = false;
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
	
	@Override
	protected void onNetworkStateChanged() {
		if (!isAdded())
			return;
		
		if (mContentView.findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE)
			showLoadingFailedLayout();
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
