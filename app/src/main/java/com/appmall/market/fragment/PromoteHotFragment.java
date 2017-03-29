package com.appmall.market.fragment;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.adapter.HotAdapter;
import com.appmall.market.adapter.HotAdapter.Callback;
import com.appmall.market.adapter.HomeAdvertPagerAdapter;
import com.appmall.market.adapter.HomeAdvertPagerAdapter.OnItemClickedListener;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemDataDef.AdvertItem;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.adapter.ItemDataDef.RankItem;
import com.appmall.market.adapter.ItemDataDef.TopicItem;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.HotPromoteRest;
import com.appmall.market.bean.HotPromoteTop;
import com.appmall.market.bean.PromoteRecommend;
import com.appmall.market.bean.Topic;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Constants;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.DataPage;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.widget.LoadMoreListView;
import com.appmall.market.widget.LoadMoreListView.OnLoadMoreListener;
import com.appmall.market.download.DownloadTask;


public class PromoteHotFragment extends BaseFragment implements IDataCallback, OnLoadMoreListener, Callback {

	private static final int MAX_CATEGORY_APP_SIZE = 20;

	private static final long FEW_CONTENT_LOAD_DELAY = 300L;
	
	private View mContentView;
	private LoadMoreListView mListView;
	private HotAdapter mAdapter;
	private HomeAdvertPagerAdapter mAdvertAdapter;
	private DataPage mDataPager;
	private boolean mQueryHomeData;
	
	private HotPromoteTop mTopData;
	private HotPromoteRest mRestData;
	
	private Context mContext;
	private boolean mLoadMoreOnFirstIn = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}

		mContext = getActivity();
		mDataPager = new DataPage();
		mContentView = inflater.inflate(R.layout.fragment_promote_hot, null);
		mListView = (LoadMoreListView) mContentView.findViewById(R.id.list_view);
		mListView.setOnLoadMoreListener(this);
		mListView.setNeedLoadMore(false);
		mListView.setCacheColorHint(Color.TRANSPARENT);

		mAdapter = new HotAdapter(mContext);
		mAdapter.registerCallback(this);
		mListView.setAdapter(mAdapter);
		
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
		int dataId = IDataConstant.HOMEPAGE;
		Options options = new Options();
		options.mTryCache = true;
		DataCenter.getInstance().requestDataAsync(mContext, dataId, this, options);
	}

	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (!isAdded())
			return;
		
		if (dataId == IDataConstant.HOMEPAGE) {
			boolean remoteData = mQueryHomeData;
			
			if (!mQueryHomeData) {
				Context ctx = getActivity();
				int homeDataId = IDataConstant.HOMEPAGE;
				DataCenter.getInstance().requestDataAsync(ctx, homeDataId, PromoteHotFragment.this, null);
				
				if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof HotPromoteTop)) {
					resp.mData = new HotPromoteTop();
					byte[] bytes = Utils.readFromAsset(ctx, Constants.HOME_PRELOAD_JSON);
					if (bytes != null) {
						try {
							JSONObject jsonObj = new JSONObject(new String(bytes));
							((HotPromoteTop) resp.mData).readFromJSON(jsonObj);
							resp.mSuccess = true;
						} catch (JSONException e) { }
					}
				}
				
				mQueryHomeData = true;
			}
			
			if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof HotPromoteTop))
				return;
			
			mTopData = (HotPromoteTop) resp.mData;
			showTopData();
			
			if (remoteData) {
				boolean hasNext = mDataPager.parseNextUrlFromContext(resp.mContext);
				mListView.setNeedLoadMore(hasNext);
				mListView.postDelayed(new FewContentLoadRunnable(), FEW_CONTENT_LOAD_DELAY);
				
				if(hasNext && mLoadMoreOnFirstIn) {
					mLoadMoreOnFirstIn = false;
					onLoadMore();
				}
			}
					
		} else if (dataId == IDataConstant.HOMEPAGE_MORE) {
			mListView.onLoadMoreComplete();
			
			if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof HotPromoteRest)) {
				if(mLoadMore) {
					showLoadMoreFailedToast();
					return;
				}
			}
			
			if (mRestData == null) {
				mRestData = (HotPromoteRest) resp.mData;
			} else {
				HotPromoteRest remoteData = (HotPromoteRest) resp.mData;
				
				if (remoteData.mAdverts != null)
					mRestData.mAdverts.addAll(remoteData.mAdverts);
				if (remoteData.mRecommends != null)
					mRestData.mRecommends.addAll(remoteData.mRecommends);
				if (remoteData.mTopics != null)
					mRestData.mTopics.addAll(remoteData.mTopics);
			}
			
			showRestData();
			
			boolean hasNext = mDataPager.parseNextUrlFromContext(resp.mContext);
			mListView.setNeedLoadMore(hasNext);
			
			mListView.postDelayed(new FewContentLoadRunnable(), FEW_CONTENT_LOAD_DELAY);
		}
	}
	
	private Toast mLoadMoreFailedToast;
	private void showLoadMoreFailedToast() {
		if (mLoadMoreFailedToast == null) {
			mLoadMoreFailedToast = Toast.makeText(getActivity(), R.string.load_more_failed, Toast.LENGTH_SHORT);
		}
		mLoadMoreFailedToast.show();
	}

	private void showTopData() {
		if (mTopData == null)
			return;
		
		Advert middleAdvert = null;
		ArrayList<ItemDataWrapper> listData = new ArrayList<ItemDataWrapper>();
		if (mTopData.mAdverts != null && mTopData.mAdverts.size() > 0) {
			List<Advert> topAdverts = new ArrayList<Advert>();
			for (Advert advert : mTopData.mAdverts) {
				if (advert == null)
					continue;
				
				if (advert.mPosition == Advert.POSITION_MIDDLE) {
					middleAdvert = advert;
				} else {
					topAdverts.add(advert);
				}
			}
			
			if (topAdverts.size() > 0) {
				if (mAdvertAdapter == null) {
					mAdvertAdapter = buildAdvertAdapter();
				}
				mAdvertAdapter.setAdvertData(topAdverts);
				listData.add(new ItemDataWrapper(mAdvertAdapter, HotAdapter.TYPE_TOP_BANNER));
			}
		}
		
		int marginTop = getResources().getDimensionPixelSize(R.dimen.list_view_margin_top);
		listData.add(new ItemDataWrapper(marginTop, HotAdapter.TYPE_SPACE));
		
		if (mTopData.mRecommends != null && mTopData.mRecommends.size() > 0) {
			resolveRecommend(mTopData.mRecommends.get(0), true, listData);
			
			for (int index = 1; index < mTopData.mRecommends.size(); ++index) {
				resolveRecommend(mTopData.mRecommends.get(index), false, listData);
			}
		}
		
		if (middleAdvert != null) {
			listData.add(new ItemDataWrapper(middleAdvert, HotAdapter.TYPE_MID_BANNER));
			listData.add(new ItemDataWrapper(marginTop, HotAdapter.TYPE_SPACE));
		}
		mAdapter.setTopData(listData);
	}
	
	private void showRestData() {
		if (mRestData == null)
			return;
		
		ArrayList<ItemDataWrapper> listData = new ArrayList<ItemDataWrapper>();
		if (mRestData.mRecommends != null && mRestData.mRecommends.size() > 0) {
			for (PromoteRecommend recommend : mRestData.mRecommends) {
				resolveRecommend(recommend, false, listData);
			}
		}
		resolveTopic(mRestData.mTopics, listData);
		resolveAdverts(mRestData.mAdverts, listData);
		
		int marginTop = getResources().getDimensionPixelSize(R.dimen.list_view_margin_top);
		listData.add(new ItemDataWrapper(marginTop, HotAdapter.TYPE_SPACE));
		
		mAdapter.setRestData(listData);
	}

	private boolean resolveRecommend(PromoteRecommend recommend, boolean recEveryDay, ArrayList<ItemDataWrapper> listData) {
		if (recommend == null || recommend.mApps == null)
			return false;
		
		List<App> appList = CommonInvoke.processApps(DataCenter.getInstance(), recommend.mApps, true);
		int marginTop = getResources().getDimensionPixelSize(R.dimen.list_view_margin_top);
		
//		if (recEveryDay) {
//			// 澶勭悊姣忔棩涓€鑽
//			if (appList.size() <= 0)
//				return false;
//			if (recommend.mCategoryTitle == null)
//				recommend.mCategoryTitle = "";
//			App app = appList.get(0);
//			listData.add(new ItemDataWrapper(recommend.mCategoryTitle, HotAdapter.TYPE_CATEGORY_TITLE));
//			listData.add(new ItemDataWrapper(app, HotAdapter.TYPE_RECOMMEND_EVERYDAY));
//			listData.add(new ItemDataWrapper(marginTop, HotAdapter.TYPE_SPACE));
//			return true;
//		}
		
		int size = Math.min(appList.size(), MAX_CATEGORY_APP_SIZE);
		if (size < 2)
			return false;
		
		if (recommend.mCategoryTitle == null)
			recommend.mCategoryTitle = "";
		listData.add(new ItemDataWrapper(recommend.mCategoryTitle, HotAdapter.TYPE_CATEGORY_TITLE));
		int appIndex = 0;
		while (appIndex < size) {
			if (appIndex < 0 || appIndex + 1 >= size)
				break;
			App appLeft = appList.get(appIndex);
			App appRight = appList.get(appIndex + 1);
			RankItem item = new RankItem(appLeft, appRight);
			listData.add(new ItemDataWrapper(item, HotAdapter.TYPE_RANK_APP));
			appIndex += 2;
		}
		
		int lastIndex = listData.size() - 1;
		RankItem lastItem = (RankItem) listData.get(lastIndex).mData;
		lastItem.mIsLastItem = true;
		
		listData.add(new ItemDataWrapper(marginTop, HotAdapter.TYPE_SPACE));
		
		return true;
	}

	private void resolveTopic(ArrayList<Topic> topics, ArrayList<ItemDataWrapper> listData) {
		if (topics == null || topics.size() < 2)
			return;
		int index = 0;
		while (index + 1 < topics.size()) {
			Topic left = topics.get(index);
			Topic right = topics.get(index + 1);
			TopicItem topicPair = new TopicItem(left, right);
			listData.add(new ItemDataWrapper(topicPair, HotAdapter.TYPE_TOPIC));
			index += 2;
		}
	}
	
	private void resolveAdverts(ArrayList<Advert> adverts, ArrayList<ItemDataWrapper> listData) {
		if (adverts == null || adverts.size() <= 0)
			return;
		
		int index = 0;
		int size = adverts.size();
		while (index + 1 < size) {
			Advert left = adverts.get(index);
			Advert right = adverts.get(index + 1);
			AdvertItem item = new AdvertItem(left, right);
			listData.add(new ItemDataWrapper(item, HotAdapter.TYPE_ADVERT));
			index += 2;
		}
		
		int lastIndex = listData.size() - 1;
		if(lastIndex >= 0) {
			Object data = listData.get(lastIndex).mData;
			if(data != null && data instanceof AdvertItem) {
				AdvertItem lastItem = (AdvertItem) data;
				lastItem.mIsLastItem = true;
			}
		}
	}
	
	private HomeAdvertPagerAdapter buildAdvertAdapter() {
		HomeAdvertPagerAdapter ret = new HomeAdvertPagerAdapter();
		ret.registerItemClickedListener(mOnAdvertPagerClicked);
		return ret;
	}
	
	boolean mLoadMore = false;
	@Override
	public void onLoadMore() {
		Activity activity = getActivity();
		if (activity == null || activity.isFinishing())
			return;
		
		String url = mDataPager.getNextUrl();
		if (TextUtils.isEmpty(url)) {
			mListView.onLoadMoreComplete();
			return;
		}
		
		int dataId = IDataConstant.HOMEPAGE_MORE;
		mLoadMore = true;
		Options opts = new Options();
		opts.mCustomUrl = url;
		DataCenter.getInstance().requestDataAsync(activity.getApplicationContext(), dataId, this, opts);
	}

	@Override
	public void onDownload(DownInfoHolder downInfo) {
		CommonInvoke.processAppDownBtn(getActivity(), downInfo.mIcon, downInfo.mItem, true);
	}

	@Override
	public void onAdvertDetailClicked(int target, String[] source, String title) {
		Utils.jumpDetailActivity(getActivity(), target, source, title);
	}
	
	@Override
	public void onAdvertClicked(Advert advert) {
		Utils.jumpDetailActivity(getActivity(), advert);
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
		DataCenter dc = DataCenter.getInstance();
		if (mTopData != null && mTopData.mRecommends != null) {
			for (PromoteRecommend pr : mTopData.mRecommends) {
				if (pr == null)
					continue;
				CommonInvoke.processApps(dc, pr.mApps, false);
			}
		}
		
		if (mRestData != null && mRestData.mRecommends != null) {
			for (PromoteRecommend pr : mRestData.mRecommends) {
				if (pr == null)
					continue;
				CommonInvoke.processApps(dc, pr.mApps, false);
			}
		}
		
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	private OnItemClickedListener mOnAdvertPagerClicked = new OnItemClickedListener() {
		@Override
		public void onAdvertViewClicked(Advert advert) {
			Activity activity = getActivity();
			if (activity == null || activity.isFinishing())
				return;
			
			Utils.jumpDetailActivity(activity, advert);
		}
	};
	
	private class FewContentLoadRunnable implements Runnable {
		@Override
		public void run() {
			if (mDataPager == null || TextUtils.isEmpty(mDataPager.getNextUrl()))
				return;
			
			boolean fewContent = false;
			if (mListView.getFirstVisiblePosition() <= 0) {
				int lastPosition = mListView.getLastVisiblePosition();
				fewContent = mListView.getAdapter().getCount() == lastPosition + 1;
			}
			
			if (fewContent)
				mListView.loadManual();
		}
	}
	
}