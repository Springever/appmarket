package com.appmall.market.adapter;

import java.util.ArrayList;
import java.util.List;

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
import com.appmall.market.adapter.ItemBuilder.HorizontalTimeHolder;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;

public class AppNewProdAdapter extends BaseAdapter {
	
	public static final int TYPE_ITEM_GROUP = 0;
	public static final int TYPE_ITEM_ITEM = 1;
	public static final int TYPE_ADVERT = 2;
	public static final int TYPE_SPACE = 3;
	
	/** 列表控件类型数 */
	public static final int VIEW_TYPE_COUNT = 3;
	private List<ItemDataWrapper> mData;
	
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
	
	public AppNewProdAdapter(Context context) {
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
		mData = data;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position).mData;
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
	public View getView(int position, View convertView, ViewGroup parent) {		
		int viewType = getItemViewType(position);
		switch (viewType) {
			case TYPE_ITEM_GROUP:
				String title = (String) getItem(position);
				convertView = ItemBuilder.buildSpaceCategoryTitle(mInflater, title, convertView, parent);
				break;
			case TYPE_ITEM_ITEM:
				App info = (App) getItem(position);
				convertView = ItemBuilder.buildHorizontalTimeItemView(mInflater, position, info, convertView, parent);	
				HorizontalTimeHolder holder = (HorizontalTimeHolder) convertView.getTag();
				bindCornerLabel(info, holder.mCornerLabel);
				holder.mDownInfo.mDownButton.setOnClickListener(mOnDownButtonClicked);
				convertView.setOnClickListener(mOnDetailClicked);
				break;
			case TYPE_ADVERT:
				CategoryAdvert adverts = (CategoryAdvert) getItem(position);
				convertView = ItemBuilder.buildCategoryAdvert(mInflater, position, adverts, convertView, parent, false);
				CategoryAdvertHolder advertHolder = (CategoryAdvertHolder) convertView.getTag();
				advertHolder.mLeftImage.setOnClickListener(mOnAdvertClicked);
				advertHolder.mRightImage.setOnClickListener(mOnAdvertClicked);
				break;
			case TYPE_SPACE:
				int height = (Integer) getItem(position);
				return ItemBuilder.buildSpaceItem(height, convertView, parent);
		}
		return convertView;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mData.get(position).mItemType;
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
			if (tag == null || !(tag instanceof HorizontalTimeHolder))
				return;
			
			if (mCallback != null) {
				mCallback.onAppDetail(((HorizontalTimeHolder) tag).mDownInfo.mItem);
			}
		}
	};
}
