package com.appmall.market.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.appmall.market.adapter.ItemBuilder.CategoryAdvertHolder;
import com.appmall.market.adapter.ItemBuilder.CategoryItemHolder;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.CategoryItem;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.Category;

/**
 * 分类列表Adapter
 *  
 *
 */
public class CategoryListAdapter extends BaseAdapter {

	public static final int TYPE_CATEGORY_ADVERT = 0;
	public static final int TYPE_CATEGORY_TITLE = 1;
	public static final int TYPE_CATEGORY_ITEM = 2;
	public static final int TYPE_SPACE = 3;
	
	/** 列表控件类型数 */
	public static final int VIEW_TYPE_COUNT = 4;
	
	private LayoutInflater mInflater;
	private List<ItemDataWrapper> mData;
	private CategoryListener mListener;
	
	public static interface CategoryListener {
		public void onCategoryClicked(Category item);
		public void onAdvertClicked(Advert advert);
	}
	
	public void registerCategoryListener(CategoryListener l) {
		mListener = l;
	}
	
	private OnClickListener mOnCategoryClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof Category))
				return;
			
			if (mListener != null)
				mListener.onCategoryClicked((Category) tag);
		}
	};
	
	private OnClickListener mOnAdvertClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof Advert))
				return;
			
			if (mListener != null)
				mListener.onAdvertClicked((Advert) tag);
		}
	};
	
	public CategoryListAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(ArrayList<ItemDataWrapper> data) {
		mData = data;
		notifyDataSetChanged();
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
		case TYPE_CATEGORY_ADVERT:
			CategoryAdvert adverts = (CategoryAdvert) getItem(position);
			convertView = ItemBuilder.buildCategoryAdvert(mInflater, position, adverts, convertView, parent, true);
			CategoryAdvertHolder advertHolder = (CategoryAdvertHolder) convertView.getTag();
			advertHolder.mLeftImage.setOnClickListener(mOnAdvertClicked);
			advertHolder.mRightImage.setOnClickListener(mOnAdvertClicked);
			return convertView;
		case TYPE_CATEGORY_TITLE:
			String title = (String) getItem(position);
			return ItemBuilder.buildCategoryTitle(mInflater, title, convertView, parent);
		case TYPE_CATEGORY_ITEM:
			CategoryItem item = (CategoryItem) getItem(position);
			convertView = ItemBuilder.buildCategoryItemView(mInflater, position, item, convertView, parent);
			CategoryItemHolder holder = (CategoryItemHolder) convertView.getTag();
			holder.mLeftCategory.setOnClickListener(mOnCategoryClicked);
			holder.mRightCategory.setOnClickListener(mOnCategoryClicked);
			return convertView;
		case TYPE_SPACE:
			int height = (Integer) getItem(position);
			return ItemBuilder.buildSpaceItem(height, convertView, parent);
		default:
			throw new RuntimeException();
		}
	}

}
