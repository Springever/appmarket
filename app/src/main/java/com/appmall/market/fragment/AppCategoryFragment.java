package com.appmall.market.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.CategoryListAdapter;
import com.appmall.market.adapter.CategoryListAdapter.CategoryListener;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.CategoryItem;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.Category;
import com.appmall.market.bean.CategoryList;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;

/**
 * 应用分类列表
 *  
 *
 */
public class AppCategoryFragment extends BaseFragment implements IDataCallback, CategoryListener {

	public static final String EXTRA_CATEGORY_TYPE = "category_type";
	public static final int CATEGORY_TYPE_SOFT = 0;
	public static final int CATEGORY_TYPE_GAME = 1;

	private View mContentView;
	private ListView mListView;
	private CategoryListAdapter mAdapter;
	private ArrayList<ItemDataWrapper> mData;
	private int mCategoryType = CATEGORY_TYPE_SOFT;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}
		
		Context context = getActivity();
		mContentView = inflater.inflate(R.layout.fragment_app_category, null);
		mListView = (ListView) mContentView.findViewById(R.id.list_view);
		
		mData = new ArrayList<ItemDataWrapper>();
		mAdapter = new CategoryListAdapter(context);
		mAdapter.registerCategoryListener(this);
		mAdapter.setData(mData);
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
		Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		int dataId = 0;
		Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_CATEGORY_TYPE)) {
			mCategoryType = args.getInt(EXTRA_CATEGORY_TYPE);
			if (mCategoryType == CATEGORY_TYPE_GAME) {
				dataId = IDataConstant.GAME_CATEGORY;
			} else {
				mCategoryType = CATEGORY_TYPE_SOFT;
				dataId = IDataConstant.APP_CATEGORY;
			}
		}
		DataCenter.getInstance().requestDataAsync(getActivity(), dataId, this, null);
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (!isAdded())
			return;
		
		Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		
		if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof CategoryList)) {
			// 网络无连接或数据错误
			showLoadingFailedLayout();
			return;
		}
		
		mListView.setVisibility(View.VISIBLE);
		
		CategoryList restData = (CategoryList) resp.mData;
		resolveCategoryList(restData);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void refreshAppData() {
		if (isLoadingFailedLayoutVisible())
			showLoadingFailedLayout();
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
	
	private void resolveCategoryList(CategoryList restData) {
		int margin = 0;
		mData.add(new ItemDataWrapper(margin, CategoryListAdapter.TYPE_SPACE));
		
		if (restData.mAdverts != null && restData.mAdverts.size() >= 2) {
			Advert left = restData.mAdverts.get(0);
			Advert right = restData.mAdverts.get(1);
			CategoryAdvert advertGrid = new CategoryAdvert(left, right);
			mData.add(new ItemDataWrapper(advertGrid, CategoryListAdapter.TYPE_CATEGORY_ADVERT));
			mData.add(new ItemDataWrapper(margin, CategoryListAdapter.TYPE_SPACE));
		}
		
		if (resolveCategoryItem(restData.mCategories)) {
			mData.add(new ItemDataWrapper(margin, CategoryListAdapter.TYPE_SPACE));
		}
	}
	
	private boolean resolveCategoryItem(ArrayList<Category> items) {
		if (items != null && items.size() >= 2) {
			int appIndex = 0;
			int size = items.size();
			while (appIndex + 1 < size) {
				Category left = items.get(appIndex);
				Category right = items.get(appIndex + 1);
				CategoryItem info = new CategoryItem(left, right);
				mData.add(new ItemDataWrapper(info, CategoryListAdapter.TYPE_CATEGORY_ITEM));
				appIndex += 2;
			}
			
			int lastIndex = mData.size() - 1;
			CategoryItem lastItem = (CategoryItem) mData.get(lastIndex).mData;
			lastItem.mIsLastItem = true;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onCategoryClicked(Category item) {
		if (item == null)
			return;
		
		int target = (mCategoryType == CATEGORY_TYPE_GAME)
				? IDataConstant.TARGET_GAME_CATEGORY_DETAIL
				: IDataConstant.TARGET_APP_CATEGORY_DETAIL;
		String[] source = new String[] { item.mDataSourceRank, item.mDataSourceNewest };
		String title = item.mTitle;
		
		Utils.jumpDetailActivity(getActivity(), target, source, title);
	}

	@Override
	public void onAdvertClicked(Advert advert) {
		if (advert == null)
			return;
	
		Utils.jumpDetailActivity(getActivity(), advert);
	}
	
	@Override
	protected void onNetworkStateChanged() {
		if (!isAdded())
			return;
		
		if (mContentView != null && mContentView.findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE)
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

}
