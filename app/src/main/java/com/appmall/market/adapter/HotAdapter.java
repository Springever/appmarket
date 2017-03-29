package com.appmall.market.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.ItemBuilder.AdvertItemHolder;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemBuilder.HotItemHolder;
import com.appmall.market.adapter.ItemBuilder.RemarkItemHolder;
import com.appmall.market.adapter.ItemBuilder.TopicItemHolder;
import com.appmall.market.adapter.ItemDataDef.AdvertItem;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.adapter.ItemDataDef.RankItem;
import com.appmall.market.adapter.ItemDataDef.TopicItem;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Topic;
import com.appmall.market.data.IDataConstant;

/**
 * 主页列表Adapter
 *  
 *
 */
public class HotAdapter extends BaseAdapter {

	public static final int TYPE_TOP_BANNER = 0;
	public static final int TYPE_CATEGORY_TITLE = 1;
//	public static final int TYPE_RECOMMEND_EVERYDAY = 2;
	public static final int TYPE_RANK_APP = 3;
	public static final int TYPE_MID_BANNER = 4;
	public static final int TYPE_TOPIC = 5;
	public static final int TYPE_ADVERT = 6;
	public static final int TYPE_SPACE = 7;
	
	/** 列表控件类型数 */
	public static final int VIEW_TYPE_COUNT = 8;
	
	public static interface Callback {
		public void onDownload(DownInfoHolder downInfo);
		public void onAppDetail(App app);
		public void onAdvertDetailClicked(int target, String[] source, String title);
		public void onAdvertClicked(Advert advert);
	}
	
	private LayoutInflater mInflater;
	private Callback mCallback;
	private Drawable mFirstCornerDrawable;
	private Drawable mNewestCornerDrawable;
	private Drawable mHotCornerDrawable;
	private ArrayList<ItemDataWrapper> mTopData;
	private ArrayList<ItemDataWrapper> mRestData;
	
	public HotAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Resources res = context.getResources();
		mFirstCornerDrawable = res.getDrawable(R.drawable.label_first_bg);
		mNewestCornerDrawable = res.getDrawable(R.drawable.label_new_bg);
		mHotCornerDrawable = res.getDrawable(R.drawable.label_hot_bg);
	}
	
	public void setTopData(ArrayList<ItemDataWrapper> data) {
		mTopData = data;
		notifyDataSetChanged();
	}
	
	public void setRestData(ArrayList<ItemDataWrapper> data) {
		mRestData = data;
		notifyDataSetChanged();
	}
	
	public void registerCallback(Callback callback) {
		mCallback = callback;
	}
	
	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}
	
	@Override
	public int getCount() {
		int count = 0;
		count += (mTopData == null ? 0 : mTopData.size());
		count += (mRestData == null ? 0 : mRestData.size());
		return count;
	}

	@Override
	public int getItemViewType(int position) {
		int topCount = (mTopData == null ? 0 : mTopData.size());
		if (position < mTopData.size()) {
			return mTopData.get(position).mItemType;
		} else {
			return mRestData.get(position - topCount).mItemType;
		}
	}
	
	@Override
	public Object getItem(int position) {
		int topCount = (mTopData == null ? 0 : mTopData.size());
		if (position < mTopData.size()) {
			return mTopData.get(position).mData;
		} else {
			return mRestData.get(position - topCount).mData;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);
		
		switch (viewType) {
		case TYPE_TOP_BANNER:
			HomeAdvertPagerAdapter bannerAdapter = (HomeAdvertPagerAdapter) getItem(position);
			convertView = ItemBuilder.buildTopBanner(mInflater, position, bannerAdapter, convertView, parent);
			convertView.setOnClickListener(mOnAdvertClicked);
			return convertView;
		case TYPE_CATEGORY_TITLE:
			String title = (String) getItem(position);
			return ItemBuilder.buildCategoryTitle(mInflater, title, convertView, parent);
//		case TYPE_RECOMMEND_EVERYDAY:
//			App app = (App) getItem(position);
//			convertView = ItemBuilder.buildRemarkApp(mInflater, position, app, convertView, parent);
//			RemarkItemHolder holder = (RemarkItemHolder) convertView.getTag();
//			bindCornerLabel(app, holder.mCornerLabel);
//			holder.mInfoHolder.mDownButton.setTag(holder.mInfoHolder);
//			holder.mInfoHolder.mDownButton.setOnClickListener(mOnDownButtonClicked);
//			convertView.setOnClickListener(mOnDetailClicked);
//			return convertView;
		case TYPE_RANK_APP:
			RankItem item = (RankItem) getItem(position);
			convertView = ItemBuilder.buildHotItemView(mInflater, position, item, convertView, parent);
			bindRankApp(item, convertView);
			return convertView;
		case TYPE_MID_BANNER:
			Advert advert = (Advert) getItem(position);
			convertView = ItemBuilder.buildMidBanner(mInflater, position, advert, convertView, parent);
			convertView.setOnClickListener(mOnAdvertClicked);
			return convertView;
		case TYPE_TOPIC:
			TopicItem topics = (TopicItem) getItem(position);
			convertView = ItemBuilder.buildTopicItemView(mInflater, position, topics, convertView, parent);
			TopicItemHolder topicHolder = (TopicItemHolder) convertView.getTag();
			topicHolder.mLeftImage.setOnClickListener(mOnAdvertClicked);
			topicHolder.mRightImage.setOnClickListener(mOnAdvertClicked);
			return convertView;
		case TYPE_ADVERT:
			AdvertItem adverts = (AdvertItem) getItem(position);
			convertView = ItemBuilder.buildAdvertItemView(mInflater, position, adverts, convertView, parent);
			bindAdvertItem(adverts, convertView);
			return convertView;
		case TYPE_SPACE:
			int height = (Integer) getItem(position);
			return ItemBuilder.buildSpaceItem(height, convertView, parent);
		default:
			throw new RuntimeException();
		}
	}

	private void bindRankApp(RankItem item, View convertView) {
		HotItemHolder holder = (HotItemHolder) convertView.getTag();
		holder.mLeftLayout.setOnClickListener(mOnDetailClicked);
		holder.mLeftButtonWrapper.setTag(holder.mLeftDownInfo);
		holder.mLeftButtonWrapper.setOnClickListener(mOnDownButtonClicked);
		holder.mRightLayout.setOnClickListener(mOnDetailClicked);
		holder.mRightButtonWrapper.setTag(holder.mRightDownInfo);
		holder.mRightButtonWrapper.setOnClickListener(mOnDownButtonClicked);

		bindCornerLabel(item.mLeft, holder.mLeftCornerLabel);
		bindCornerLabel(item.mRight, holder.mRightCornerLabel);
	}
	
	@SuppressWarnings("deprecation")
	private void bindCornerLabel(App app, TextView textView) {
		if (app == null)
			return;
		Drawable cornerDrawable = null;
		int textResId = -1;
		if (app.mCornerLabel == App.CORNER_LABEL_FIRST) {
			cornerDrawable = mFirstCornerDrawable;
			textResId = R.string.label_title_first;
		} else if (app.mCornerLabel == App.CORNER_LABEL_NEWEST) {
			cornerDrawable = mNewestCornerDrawable;
			textResId = R.string.label_title_new;
		} else if (app.mCornerLabel == App.CORNER_LABEL_HOT) {
			cornerDrawable = mHotCornerDrawable;
			textResId = R.string.label_title_hot;
		} else {
			cornerDrawable = null;
		}
		
		textView.setBackgroundDrawable(cornerDrawable);
		if(textResId != -1)
			textView.setText(textResId);
		else
			textView.setText("");
	}

	private void bindAdvertItem(AdvertItem item, View convertView) {
		AdvertItemHolder holder = (AdvertItemHolder) convertView.getTag();
		holder.mLeftAdvert.setOnClickListener(mOnAdvertClicked);
		holder.mRightAdvert.setOnClickListener(mOnAdvertClicked);
	}
	
	private OnClickListener mOnDownButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof DownInfoHolder))
				return;
			
			if (mCallback != null)
				mCallback.onDownload((DownInfoHolder) tag);
		}
	};
	
	private OnClickListener mOnAdvertClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null)
				return;
			
			int target = 0;
			String source[] = null;
			String title = null;
			if (tag instanceof Advert) {
				Advert advert = (Advert) tag;
				target = advert.mDataTarget;
				source = new String[] { advert.mDataSource };
				title = advert.mTitle;
				if (mCallback != null)
					mCallback.onAdvertClicked(advert);
			} else if (tag instanceof Topic) {
				Topic topic = (Topic) tag;
				target = IDataConstant.TARGET_TOPIC_DETAIL;
				source = new String[] { topic.mDataSource };
				title = topic.mTitle;
				if (mCallback != null)
					mCallback.onAdvertDetailClicked(target, source, title);
			}
		}
	};

	private OnClickListener mOnDetailClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null)
				return;
			
			App app = null;
			if (tag instanceof RemarkItemHolder) {
				RemarkItemHolder holder = (RemarkItemHolder) v.getTag();
				app = holder.mInfoHolder.mItem;
			} else if (tag instanceof App) {
				app = (App) tag;
			}
			
			if (mCallback != null && app != null) {
				mCallback.onAppDetail(app);
			}
		}
	};

}