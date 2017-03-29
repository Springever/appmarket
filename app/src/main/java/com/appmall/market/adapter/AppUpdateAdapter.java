package com.appmall.market.adapter;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.appmall.market.R;
import com.appmall.market.adapter.ItemBuilder.IgnoreItemViewHolder;
import com.appmall.market.adapter.ItemBuilder.UpdateInfoHolder;
import com.appmall.market.adapter.ItemBuilder.UpdateItemViewHolder;
import com.appmall.market.bean.AppUpdate;

/**
 * 可更新列表Adapter
 *  
 *
 */
public class AppUpdateAdapter extends BaseExpandableListAdapter {

	/* 列表分为更新列表以及忽略列表 */
	public static final int GROUP_UPDATE = 0;
	public static final int GROUP_IGNORE = 1;
	private static final int GROUP_COUNT = 2;
	
	private static final int CHILD_UPDATE = 0;
	private static final int CHILD_IGNORE = 1;
	private static final int CHILD_TYPE_COUNT = 2;
	
	private List<AppUpdate> mUpdates;
	private List<AppUpdate> mIgnores;
	private LayoutInflater mInflater;
	private String mShowControlPackage;
	private Callback mCallback;
	
	public interface Callback {
		public void onUpdate(UpdateInfoHolder updateInfo);
		public void onIgnore(AppUpdate item);
		public void onRemoveIgnore(AppUpdate item);
	}
	
	public AppUpdateAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	public void registerCallback(Callback callback) {
		mCallback = callback;
	}
	
	public void setData(List<AppUpdate> updatelist, List<AppUpdate> ignorelist) {
		mUpdates = updatelist;
		mIgnores = ignorelist;
		checkmShowControlPackageExist(updatelist, ignorelist);
		notifyDataSetChanged();
	}
	
	private void checkmShowControlPackageExist(List<AppUpdate> updatelist, List<AppUpdate> ignorelist) {
		if(mShowControlPackage != null) {
			boolean bFind = false;
			for(AppUpdate update : updatelist) {
				if(update.mPackageName.equalsIgnoreCase(mShowControlPackage)) {
					bFind = true;
					break;
				}		
			}
			for(AppUpdate ignore : ignorelist) {
				if(ignore.mPackageName.equalsIgnoreCase(mShowControlPackage)) {
					bFind = true;
					break;
				}		
			}
			if(!bFind)
				mShowControlPackage = null;
		}
	}
	
	public List<AppUpdate> getData(int groupPosition) {
		List<AppUpdate> list = null;
		if (groupPosition == GROUP_UPDATE) {
			list = mUpdates;
		} else if (groupPosition == GROUP_IGNORE) {
			list = mIgnores;
		}
		return list;
	}
	
	@Override
	public int getGroupCount() {
		return GROUP_COUNT;
	}

	@Override
	public boolean isEmpty() {
		return getChildrenCount(GROUP_UPDATE) + getChildrenCount(GROUP_IGNORE) <= 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition == GROUP_UPDATE) {
			return mUpdates == null ? 0 : mUpdates.size();
		} else if (groupPosition == GROUP_IGNORE) {
			return mIgnores == null ? 0 : mIgnores.size();
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		if (groupPosition == GROUP_UPDATE) {
			return CHILD_UPDATE;
		} else if (groupPosition == GROUP_IGNORE) {
			return CHILD_IGNORE;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public int getChildTypeCount() {
		return CHILD_TYPE_COUNT;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (groupPosition == GROUP_UPDATE) {
			return mUpdates.get(childPosition);
		} else if (groupPosition == GROUP_IGNORE) {
			return mIgnores.get(childPosition);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition * 0x10000000 + childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Resources res = parent.getResources();
		String formatString = null;
		if (groupPosition == GROUP_UPDATE) {
			formatString = res.getString(R.string.can_be_update);
		} else if (groupPosition == GROUP_IGNORE) {
			formatString = res.getString(R.string.already_ignored);
		} else {
			throw new RuntimeException();
		}
		
		int count = getChildrenCount(groupPosition);
		String title = String.format(Locale.getDefault(), formatString, count);
		convertView = ItemBuilder.buildUpdateGroupView(mInflater, groupPosition, title, isExpanded,
				convertView, parent);	
		
		View emptyView = convertView.findViewById(R.id.empty_view);
		View contentView = convertView.findViewById(R.id.content_layout);
		
		if (groupPosition == GROUP_IGNORE) {
			emptyView.setVisibility(count <= 0 ? View.VISIBLE : View.GONE);
			contentView.setVisibility(count <= 0 ? View.GONE : View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
			contentView.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (groupPosition == GROUP_UPDATE) {
			return getUpdateView(childPosition, isLastChild, convertView, parent);
		} else if (groupPosition == GROUP_IGNORE) {
			return getIgnoreView(childPosition, isLastChild, convertView, parent);
		} else {
			throw new RuntimeException();
		}
	}

	private View getUpdateView(int position, boolean isLastChild, View convertView, ViewGroup parent) {
		AppUpdate item = (AppUpdate) getChild(GROUP_UPDATE, position);
		convertView = ItemBuilder.buildUpdateItemView(mInflater, position, item,
				item.mPackageName.equals(mShowControlPackage), convertView, parent);
		if(isLastChild) {
			convertView.findViewById(R.id.update_item_divider).setVisibility(View.GONE);
		} else {
			convertView.findViewById(R.id.update_item_divider).setVisibility(View.VISIBLE);
		}
			
		convertView.setOnClickListener(mOnItemClicked);
		UpdateItemViewHolder holder = (UpdateItemViewHolder) convertView.getTag();
		holder.mIgnoreButton.setOnClickListener(mOnIgnoreClicked);
		holder.mUpdateInfo.mDownButton.setOnClickListener(mOnUpdateClicked);		
		return convertView;
	}
	
	private View getIgnoreView(final int position, boolean isLastChild, View convertView, ViewGroup parent) {
		final AppUpdate item = (AppUpdate) getChild(GROUP_IGNORE, position);
		convertView = ItemBuilder.buildIgnoreItemView(mInflater, position, item,item.mPackageName.equals(mShowControlPackage),convertView, parent);
		if(isLastChild) {
			convertView.findViewById(R.id.ignore_item_divider).setVisibility(View.GONE);
		} else {
			convertView.findViewById(R.id.ignore_item_divider).setVisibility(View.VISIBLE);
		}
		convertView.setOnClickListener(mOnItemClicked);
		IgnoreItemViewHolder holder = (IgnoreItemViewHolder) convertView.getTag();
//		holder.mIgnoreCancelButton.setOnClickListener(mOnCancelIgnoreClicked);
//		holder.mIgnoreInfo.mDownButton.setOnClickListener(mOnIgnoreUpdateClicked);	
		
		holder.mIgnoreInfo.mDownButton.setOnClickListener(mOnCancelIgnoreClicked);	
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	private OnClickListener mOnItemClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null)
				return;
			
			if(tag instanceof UpdateItemViewHolder) {
				String packageName = ((UpdateItemViewHolder) tag).mPackageName;
				if (packageName.equals(mShowControlPackage)) {
					mShowControlPackage = null;
				} else {
					mShowControlPackage = packageName;
				}
				notifyDataSetChanged();
			} else if(tag instanceof IgnoreItemViewHolder) {
				String packageName = ((IgnoreItemViewHolder) tag).mPackageName;
				if (packageName.equals(mShowControlPackage)) {
					mShowControlPackage = null;
				} else {
					mShowControlPackage = packageName;
				}
				notifyDataSetChanged();
			}
		}
	};
	
	private OnClickListener mOnIgnoreClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCallback != null) {
				AppUpdate item = ((AppUpdate) v.getTag());
				mCallback.onIgnore(item);
			}
		}
	};
	
	private OnClickListener mOnUpdateClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof UpdateInfoHolder))
				return;
			
			if (mCallback != null) {
				mCallback.onUpdate((UpdateInfoHolder) tag);
			}
		}
	};
	
//	private OnClickListener mOnIgnoreUpdateClicked = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			Object tag = v.getTag();
//			if (tag == null || !(tag instanceof UpdateInfoHolder))
//				return;
//			
//			if (mCallback != null) {
//				mCallback.onUpdate((UpdateInfoHolder) tag);
//			}
//		}
//	};
	
	private OnClickListener mOnCancelIgnoreClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCallback != null) {
				AppUpdate item = ((AppUpdate) v.getTag());
				mCallback.onRemoveIgnore(item);
			}
		}
	};

}
