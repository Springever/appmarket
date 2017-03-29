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

import com.appmall.market.R;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemBuilder.HotItemHolder;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.adapter.ItemDataDef.RankItem;
import com.appmall.market.bean.App;
import android.widget.TextView;

/**
 * 必备软件/游戏列表Adapter
 *  
 *
 */
public class BiBeiAdapter extends BaseAdapter {

	public static final int TYPE_CATEGORY_TITLE = 0;
	public static final int TYPE_RANK_ITEM = 1;
	public static final int TYPE_SPACE = 2;
	
	/** 列表控件类型数 */
	public static final int VIEW_TYPE_COUNT = 3;

	public interface Callback {
		public void onDownload(DownInfoHolder downInfo);
		public void onAppDetail(App app);
	}
	
	private LayoutInflater mInflater;
	private List<ItemDataWrapper> mData;
	private Callback mCallback;
	private Drawable mFirstCornerDrawable;
	private Drawable mNewestCornerDrawable;
	private Drawable mHotCornerDrawable;
	
	public BiBeiAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Resources res = context.getResources();
		
		mFirstCornerDrawable = res.getDrawable(R.drawable.label_first_bg);
		mNewestCornerDrawable = res.getDrawable(R.drawable.label_new_bg);
		mHotCornerDrawable = res.getDrawable(R.drawable.label_hot_bg);
	}
	
	public void setData(ArrayList<ItemDataWrapper> data) {
		mData = data;
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
		return mData == null ? 0 : mData.size();
	}

	@Override
	public int getItemViewType(int position) {
		return mData.get(position).mItemType;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);

		switch (viewType) {
		case TYPE_CATEGORY_TITLE:
			String title = (String) getItem(position);
			return ItemBuilder.buildCategoryTitle(mInflater, title, convertView, parent);
		case TYPE_RANK_ITEM:
			RankItem item = (RankItem) getItem(position);
//			convertView = ItemBuilder.buildRankItemView(mInflater, position, item, convertView, parent);
			convertView = ItemBuilder.buildHotItemView(mInflater, position, item, convertView, parent);
			bindRankApp(item, convertView);
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
	
	private OnClickListener mOnDownButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof DownInfoHolder))
				return;
			
			if (mCallback != null) {
				mCallback.onDownload((DownInfoHolder) tag);
			}
		}
	};

	private OnClickListener mOnDetailClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof App))
				return;
			
			if (mCallback != null) {
				mCallback.onAppDetail((App) tag);
			}
		}
	};

}
