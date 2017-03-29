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
import android.widget.ListView;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.BiBeiAdapter;
import com.appmall.market.adapter.BiBeiAdapter.Callback;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.adapter.ItemDataDef.RankItem;
import com.appmall.market.bean.App;
import com.appmall.market.bean.BiBei;
import com.appmall.market.bean.MustInstallBatch;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.download.DownloadTask;

/**
 * 装机必备页面
 *  
 *
 */
public class PromoteBibeiFragment extends BaseFragment implements IDataCallback, Callback {

	public static final String EXTRA_BIBEI_TYPE = "bibei_type";
	public static final int BIBEI_TYPE_SOFT = 0;
	public static final int BIBEI_TYPE_GAME = 1;
	
	private View mContentView;
	private ListView mListView;
	private BiBeiAdapter mAdapter;
	private ArrayList<ItemDataWrapper> mData;
	private BiBei mBibeiData;
	private int mItemType = 0;   //0= 软件	1=游戏
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}
		
		Context context = getActivity();
		mContentView = inflater.inflate(R.layout.fragment_promote_bibei, null);
		mListView = (ListView) mContentView.findViewById(R.id.list_view);
		
		mData = new ArrayList<ItemDataWrapper>();
		mAdapter = new BiBeiAdapter(context);
		mAdapter.setData(mData);
		mAdapter.registerCallback(this);
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

	public void requestData() {
		Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		int dataId = IDataConstant.MUST_INSTALL_APP;
		Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_BIBEI_TYPE)) {
			if (args.getInt(EXTRA_BIBEI_TYPE) == BIBEI_TYPE_GAME) {
				dataId = IDataConstant.MUST_INSTALL_GAME;
				mItemType =1;
			}
		}
		DataCenter.getInstance().requestDataAsync(getActivity(), dataId, this, null);
	}
	
	@Override
	protected void onNetworkStateChanged() {
		if (!isAdded())
			return;
		
		if (mContentView.findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE)
			showLoadingFailedLayout();
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (!isAdded())
			return;
		
		Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		
		if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof BiBei)) {
			showLoadingFailedLayout();
			return;
		}
		
		mListView.setVisibility(View.VISIBLE);
		
		mBibeiData = (BiBei) resp.mData;
		refreshAppData();
		resolveBiBei();
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

	private void resolveBiBei() {
		if (mBibeiData.mMustInstalled == null)
			return;
		int size = mBibeiData.mMustInstalled.size();
		if (size <= 0)
			return;
		
		int marginTop = getResources().getDimensionPixelSize(R.dimen.list_view_margin_top);
		mData.add(new ItemDataWrapper(marginTop, BiBeiAdapter.TYPE_SPACE));
		
		for (int index = 0; index < size; ++index) {
			MustInstallBatch batch = mBibeiData.mMustInstalled.get(index);
			if (!resolveMustInstallBatch(batch))
				continue;
			
			if (index != size - 1) {
				int margin = getResources().getDimensionPixelSize(R.dimen.list_view_margin_top);
				mData.add(new ItemDataWrapper(margin, BiBeiAdapter.TYPE_SPACE));
			}
		}
		
		mData.add(new ItemDataWrapper(marginTop, BiBeiAdapter.TYPE_SPACE));
		mData.add(new ItemDataWrapper(marginTop, BiBeiAdapter.TYPE_SPACE));
		mData.add(new ItemDataWrapper(marginTop, BiBeiAdapter.TYPE_SPACE));
	}

	private boolean resolveMustInstallBatch(MustInstallBatch batch) {
		int size = 0;
		if (batch == null || batch.mApps == null || (size = batch.mApps.size()) < 2)
			return false;
		
		if (batch.mCategoryTitle == null)
			batch.mCategoryTitle = "";
		mData.add(new ItemDataWrapper(batch.mCategoryTitle, BiBeiAdapter.TYPE_CATEGORY_TITLE));
		
		int appIndex = 0;
		while (appIndex + 1 < size) {
			App appLeft = batch.mApps.get(appIndex);
			App appRight = batch.mApps.get(appIndex + 1);
			RankItem item = new RankItem(appLeft, appRight);
			item.mType = mItemType;
			mData.add(new ItemDataWrapper(item, BiBeiAdapter.TYPE_RANK_ITEM));
			appIndex += 2;
		}
		
		int lastIndex = mData.size() - 1;
		RankItem lastItem = (RankItem) mData.get(lastIndex).mData;
		lastItem.mIsLastItem = true;
		
		return true;
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
		Utils.handleButtonProgress(mContentView, R.id.left_down_button, task);
		Utils.handleButtonProgress(mContentView, R.id.right_down_button, task);
	}

	@Override
	protected void refreshAppData() {
		if (isLoadingFailedLayoutVisible())
			showLoadingFailedLayout();
		
		if (mBibeiData == null || mAdapter == null || mBibeiData.mMustInstalled == null)
			return;
		
		DataCenter dc = DataCenter.getInstance();
		for (MustInstallBatch mib : mBibeiData.mMustInstalled) {
			CommonInvoke.processApps(dc, mib.mApps, false);
		}
		
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
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
}
