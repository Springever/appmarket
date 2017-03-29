package com.appmall.market.adapter;

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
import com.appmall.market.adapter.ItemBuilder.SearchAssociationAppItemViewHolder;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Keywords.Keyword;
import android.widget.TextView;

public class AssociationAdapter extends BaseAdapter {

	private static final int VIEW_TYPE_APP = 0;
	private static final int VIEW_TYPE_KEYWORD = 1;
	private static final int VIEW_TYPE_COUNT = 2;
	
	private List<App> mApps;
	private List<Keyword> mKeywords;
	private LayoutInflater mInflater;
	private Callback mCallback;
	private Drawable mFirstCornerDrawable;
	private Drawable mNewestCornerDrawable;
	private Drawable mHotCornerDrawable;
	
	public static interface Callback {
		public void onAppClicked(App item);
		public void onAppDownload(DownInfoHolder item);
		public void onKeywordClicked(Keyword keyword);
	}
	
	public AssociationAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		Resources res = context.getResources();
		mFirstCornerDrawable = res.getDrawable(R.drawable.label_first_bg);
		mNewestCornerDrawable = res.getDrawable(R.drawable.label_new_bg);
		mHotCornerDrawable = res.getDrawable(R.drawable.label_hot_bg);
	}
	
	public void registerCallback(Callback callback) {
		mCallback = callback;
	}
	
	public void setData(List<App> apps, List<Keyword> keywords) {
		mApps = apps;
		mKeywords = keywords;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		int count = 0;
		if (mApps != null)
			count += mApps.size();
		if (mKeywords != null)
			count += mKeywords.size();
		return count;
	}

	@Override
	public int getItemViewType(int position) {
		int appSize = mApps == null ? 0 : mApps.size();
		if (position < appSize) {
			return VIEW_TYPE_APP;
		} else {
			return VIEW_TYPE_KEYWORD;
		}
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	@Override
	public Object getItem(int position) {
		int appSize = mApps == null ? 0 : mApps.size();
		if (position < appSize) {
			return mApps.get(position);
		} else {
			int posKeyword = position - appSize;
			return mKeywords.get(posKeyword);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);
		if (viewType == VIEW_TYPE_APP) {
			App info = (App) getItem(position);
			convertView = ItemBuilder.buildSearchAssociationAppItemView(mInflater, position, info, convertView, parent);
			SearchAssociationAppItemViewHolder holder = (SearchAssociationAppItemViewHolder) convertView.getTag();
			bindCornerLabel(info, holder.mCornerLabel);
			holder.mDownInfo.mDownButton.setOnClickListener(mOnAppDownloadClicked);
			convertView.setOnClickListener(mOnAppClicked);
		} else if (viewType == VIEW_TYPE_KEYWORD) {
			Keyword keyword = (Keyword) getItem(position);
			convertView = ItemBuilder.buildSearchAssociationKeywordItemView(mInflater, position, keyword,
					convertView, parent);
			convertView.setOnClickListener(mOnKeywordClicked);
		} else {
			throw new RuntimeException();
		}
		
		return convertView;
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

	private OnClickListener mOnAppClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag != null && !(tag instanceof SearchAssociationAppItemViewHolder))
				return;
			if (mCallback != null) {
				mCallback.onAppClicked(((SearchAssociationAppItemViewHolder) tag).mDownInfo.mItem);
			}
		}
	};
	
	private OnClickListener mOnAppDownloadClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag != null && !(tag instanceof DownInfoHolder))
				return;
			
			if (mCallback != null) {
				mCallback.onAppDownload((DownInfoHolder) tag);
			}
		}
	};
	
	private OnClickListener mOnKeywordClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag != null && !(tag instanceof Keyword))
				return;
			if (mCallback != null) {
				mCallback.onKeywordClicked((Keyword) tag);
			}
		}
	};
	
}
