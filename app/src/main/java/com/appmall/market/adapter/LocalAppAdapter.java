package com.appmall.market.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.appmall.market.adapter.ItemBuilder.LocalAppViewHolder;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.data.LocalApps.LocalAppInfo;

/**
 * 本地应用管理列表Adapter
 *  
 *
 */
public class LocalAppAdapter extends BaseAdapter {

	public static final int TYPE_LOCAL_APP = 1;
	
	/** 列表控件类型数 */
	public static final int VIEW_TYPE_COUNT = 2;
	
	private LayoutInflater mInflater;
	private ArrayList<ItemDataWrapper> mData;
	private Callback mCallback;

	public static interface Callback {
		public void onUninstallClick(LocalAppInfo info);
	}
	
	public LocalAppAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(ArrayList<ItemDataWrapper> data) {
		mData = data;
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
		case TYPE_LOCAL_APP:
			LocalAppInfo item = (LocalAppInfo) getItem(position);
			convertView = ItemBuilder.buildLocalAppView(mInflater, position, item, convertView, parent);
			LocalAppViewHolder holder = (LocalAppViewHolder) convertView.getTag();
			holder.mUninstallButton.setOnClickListener(mOnUninstallClicked);
			return convertView;
		default:
			throw new RuntimeException();
		}
	}

	private OnClickListener mOnUninstallClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCallback != null) {
				mCallback.onUninstallClick((LocalAppInfo) v.getTag());
			}
		}
	};
	
}
