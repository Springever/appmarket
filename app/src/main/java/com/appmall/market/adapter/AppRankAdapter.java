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
import com.appmall.market.adapter.ItemBuilder.CategoryAdvertHolder;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemBuilder.HorizontalRankHolder;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;

/**
 * 软件/游戏热榜列表Adapter
 *  
 *
 */
public class AppRankAdapter extends BaseAdapter {

	public static final int TYPE_ADVERT = 0;
	public static final int TYPE_APP = 1;
	
	/** 列表控件类型数 */
	public static final int VIEW_TYPE_COUNT = 2;
	
	private ArrayList<ItemDataWrapper> mItems;
	private LayoutInflater mInflater;
	private Callback mCallback;
	private Drawable mFirstCornerDrawable;
	private Drawable mNewestCornerDrawable;
	private Drawable mHotCornerDrawable;
	
	public interface Callback {
		public void onDownload(DownInfoHolder downInfo);
		public void onAppDetail(App app);
		public void onAdvertClicked(Advert advert);
	}
	
	public AppRankAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		
		Resources res = context.getResources();
		mFirstCornerDrawable = res.getDrawable(R.drawable.label_first_bg);
		mNewestCornerDrawable = res.getDrawable(R.drawable.label_new_bg);
		mHotCornerDrawable = res.getDrawable(R.drawable.label_hot_bg);
	}
	
	public void registerCallback(Callback callback) {
		mCallback = callback;
	}
	
	public void setData(ArrayList<ItemDataWrapper> data) {
		mItems = data;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mItems == null ? 0 : mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position).mData;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mItems.get(position).mItemType;
	}

	private boolean bHasAdv = false;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);
		
		switch (viewType) {
		case TYPE_APP:
			App info = (App) getItem(position);
			convertView = ItemBuilder.buildHorizontalRankItemView(mInflater, position, info, convertView, parent, null, true, bHasAdv);
			
			HorizontalRankHolder holder = (HorizontalRankHolder) convertView.getTag();
			bindCornerLabel(info, holder.mCornerLabel);
			holder.mDownInfo.mDownButton.setOnClickListener(mOnDownButtonClicked);
			convertView.setOnClickListener(mOnDetailClicked);
			return convertView;
		case TYPE_ADVERT:
			bHasAdv = true;
			CategoryAdvert adverts = (CategoryAdvert) getItem(position);
			convertView = ItemBuilder.buildCategoryAdvert(mInflater, position, adverts, convertView, parent,true);
			CategoryAdvertHolder advertHolder = (CategoryAdvertHolder) convertView.getTag();
			advertHolder.mLeftImage.setOnClickListener(mOnAdvertClicked);
			advertHolder.mRightImage.setOnClickListener(mOnAdvertClicked);
			return convertView;
		default:
			throw new RuntimeException();
		}
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

	private OnClickListener mOnDetailClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof HorizontalRankHolder))
				return;
			
			if (mCallback != null) {
				mCallback.onAppDetail(((HorizontalRankHolder) tag).mDownInfo.mItem);
			}
		}
	};
	
	private OnClickListener mOnAdvertClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof Advert))
				return;
			
			if (mCallback != null)
				mCallback.onAdvertClicked((Advert) tag);
		}
	};
	
}
