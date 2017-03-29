package com.appmall.market.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.appmall.market.R;
import com.appmall.market.adapter.ItemBuilder.DownAppViewHolder;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.Statistics;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskList;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.widget.CommonDialog;
import com.appmall.market.widget.DownMgrContextMenu;

import android.widget.BaseExpandableListAdapter;

/**
 * 应用下载管理列表Adapter
 *  
 *
 */
public class DownAppAdapter extends BaseExpandableListAdapter {
	
	/* 列表分为更新列表以及忽略列表 */
	public static final int GROUP_DOWNLOADING = 0;
	public static final int GROUP_DOWNLOADED = 1;
	private static final int GROUP_COUNT = 2;
	
	private static final int CHILD_DOWNLOADING = 0;
	private static final int CHILD_DOWNLOADED = 1;
	private static final int CHILD_TYPE_COUNT = 2;
	

	public static final int TYPE_DOWN_APP = 1;
	private static final String PROGRESS_CHAR = " / ";
	
	private LayoutInflater mInflater;
	private DownMgrContextMenu mContextMenu;
	private ListView mListView;
	private Callback mCallback;
	
	private List<DownloadTask> mDownloadingList;
	private List<DownloadTask> mDownloadedList;
	private Context mContext;
	
	public interface Callback {
		public void onCancelClicked(DownloadTask task);
		public void onPauseClicked(DownloadTask task);
		public void onResumeClicked(DownloadTask task);
		public void onInstallClicked(DownloadTask task);
		public void onOpenClicked(DownloadTask task);
		public void onMergeClicked(DownloadTask task);
	}
	
	public void registerCallback(Callback callback) {
		mCallback = callback;
	}
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	private OnClickListener mOnItemClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof DownAppViewHolder))
				return;
			
			String packageName = ((DownAppViewHolder) tag).mPackageName;			
			DownloadTask task = getDownloadTask(packageName);
			if(task == null) return;
			if (mContextMenu == null) {
				mContextMenu = new DownMgrContextMenu(mContext);
				mContextMenu.setContentView(createContextMenuView());
			} else {
				mContextMenu.dismiss();
			}
			
			View contentView = mContextMenu.getContentView();
			TextView menuText = (TextView) contentView.findViewById(R.id.menu_item);
			if(task.mStatus == TaskStatus.STATUS_DOWNLOAD || task.mStatus == TaskStatus.STATUS_INSTALLED)
				menuText.setText(R.string.delete_record);
			else
				menuText.setText(R.string.delete_task);
			contentView.setTag(task);
			contentView.setOnClickListener(mOnMenuButtonClicked);
			mContextMenu.show(v.findViewById(R.id.item_content));
		}
	};
	
	private DownloadTask getDownloadTask(String packageName) {
		DownloadTask findTask = null;
		TaskList taskList = DataCenter.getInstance().getTaskList();
		List<DownloadTask> downloadList = taskList.getTaskList();
		for(DownloadTask task : downloadList) {
			if(task.mPackageName.equalsIgnoreCase(packageName)) {
				findTask = task;
				break;
			}		
		}
		return findTask;
	}
	
	private View createContextMenuView() {
		View ret = mInflater.inflate(R.layout.down_app_manage_context_menu, null);
		ret.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		return ret;
	}
	
	private OnClickListener mOnMenuButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mContextMenu != null && mContextMenu.isShowing())
				mContextMenu.dismiss();
			
			if (mCallback != null) {
				DownloadTask task = (DownloadTask) v.getTag();
				mCallback.onCancelClicked(task);
				
				if (task.mStatus == TaskStatus.STATUS_DOWNLOAD) {
					// 任务已下载完成，按下按钮是删除APK包操作，加入统计
					Statistics.addRemoveApkCount(v.getContext());
				}
			}
		}
	};
	
	private OnClickListener mOnButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCallback == null)
				return;
			
			DownloadTask task = (DownloadTask) v.getTag();
			switch (task.mStatus) {
			case TaskStatus.STATUS_DOWNLOADING:
			case TaskStatus.STATUS_WAIT:
				mCallback.onPauseClicked(task);
				return;
			case TaskStatus.STATUS_PAUSE:
			case TaskStatus.STATUS_FAILED:
				mCallback.onResumeClicked(task);
				return;
			case TaskStatus.STATUS_DOWNLOAD:
				mCallback.onInstallClicked(task);
				return;
			case TaskStatus.STATUS_INSTALLED:
				mCallback.onOpenClicked(task);
				return;
			case TaskStatus.STATUS_MERGING:
			case TaskStatus.STATUS_MERGING_INSTALL:
				mCallback.onMergeClicked(task);
				return;
			}
		}
	};
	
	public DownAppAdapter(Context context, ListView listView) {
		mListView = listView;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(List<DownloadTask> downloadingList, List<DownloadTask> downloadedList) {
		mDownloadingList = downloadingList;
		mDownloadedList = downloadedList;
		notifyDataSetChanged();
	}

	public void setProgress(DownloadTask task) {
//		int firstPos = mListView.getFirstVisiblePosition();
//		int lastPos = mListView.getLastVisiblePosition();
		
		int firstPos = 0;
		int lastPos = mListView.getChildCount()-1;
		
		for (int pos = firstPos; pos <= lastPos; ++pos) {
			View view = mListView.getChildAt(pos);
			if (view == null)
				continue;
			Object tag = view.getTag();
			if (tag == null || !(tag instanceof DownAppViewHolder))
				continue;
			DownAppViewHolder holder = (DownAppViewHolder) tag;
			if (task.mPackageName.equals(holder.mPackageName)) {
				int progress = 0;
				if (task.mTotal != 0)
					progress = (int) (task.mTransfered * 100f / task.mTotal);
				holder.mProgress.setProgress(progress);
				holder.mDescription.setText(Utils.getSizeString(task.mTransfered) + PROGRESS_CHAR
						+ Utils.getSizeString(task.mTotal));
				
				DecimalFormat decimalFormat = new DecimalFormat("#.0");
				float speed = task.mSpeed;			
				String formatData = decimalFormat.format(speed);
				if(formatData != null) {
					if(task.mSpeedIndex >= 1) {
						task.mSpeedIndex = 0;
						if(speed <= 0 || formatData.equalsIgnoreCase("NaN"))
							holder.mStatus.setText("等待中");
						else {
							if(speed > 1024) {
								formatData = decimalFormat.format(speed/1024);
								holder.mStatus.setText(formatData+"m/s");	
							}			
							else
								holder.mStatus.setText(formatData+"k/s");	
						}			
					}else {
						task.mSpeedIndex++;
					}
				}
				break;
			}
		}
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	@Override
	public int getGroupCount() {
		return GROUP_COUNT;
	}

	@Override
	public boolean isEmpty() {
		return getChildrenCount(GROUP_DOWNLOADING) + getChildrenCount(GROUP_DOWNLOADED) <= 0;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition == GROUP_DOWNLOADING) {
			return mDownloadingList == null ? 0 : mDownloadingList.size();
		} else if (groupPosition == GROUP_DOWNLOADED) {
			return mDownloadedList == null ? 0 : mDownloadedList.size();
		} else {
			throw new RuntimeException();
		}
	}
	
	@Override
	public int getChildType(int groupPosition, int childPosition) {
		if (groupPosition == GROUP_DOWNLOADING) {
			return CHILD_DOWNLOADING;
		} else if (groupPosition == GROUP_DOWNLOADED) {
			return CHILD_DOWNLOADED;
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
		if (groupPosition == GROUP_DOWNLOADING) {
			return mDownloadingList.get(childPosition);
		} else if (groupPosition == GROUP_DOWNLOADED) {
			return mDownloadedList.get(childPosition);
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
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (groupPosition == GROUP_DOWNLOADED ) {
			return getDownloadedView(childPosition, isLastChild, convertView, parent);
		} else if (groupPosition == GROUP_DOWNLOADING) {
			return getDownloadingView(childPosition, isLastChild, convertView, parent);
		} else {
			throw new RuntimeException();
		}
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Resources res = parent.getResources();
		String formatString = null;
		boolean bHasButton = false;
		String buttonTitle = "";
		int buttonIconRes = R.drawable.clean_package;
		if (groupPosition == GROUP_DOWNLOADING) {
			formatString = res.getString(R.string.group_downloading);
		} else if (groupPosition == GROUP_DOWNLOADED) {
			formatString = res.getString(R.string.group_downloaded);
			buttonTitle = res.getString(R.string.one_key_clean);
			bHasButton = true;
		} else {
			throw new RuntimeException();
		}
		
		int count = getChildrenCount(groupPosition);
		String title = String.format(Locale.getDefault(), formatString, count);
		convertView = ItemBuilder.buildDownloadGroupView(mInflater, groupPosition, title, isExpanded,convertView, parent, bHasButton, buttonTitle, buttonIconRes);
		
		View emptyView = convertView.findViewById(R.id.empty_view);
		View contentView = convertView.findViewById(R.id.content_layout);
			
		if (groupPosition == GROUP_DOWNLOADED) {
			emptyView.setVisibility(count <= 0 ? View.VISIBLE : View.GONE);
			contentView.setVisibility(count <= 0 ? View.GONE : View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
			contentView.setVisibility(View.VISIBLE);
		}
		
		
		if(bHasButton) 
		{
			convertView.findViewById(R.id.layout_group_button).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(AppSettings.isDeleteALLQuery(mContext))
						showDelConfirmDialog(mContext);
					else
						doClearTask(mContext);
				}
			});
		}
		
		return convertView;
	}
	
	private void showDelConfirmDialog(Context context) {
		String message = context.getResources().getString(R.string.delete_all_record_content);
		String title = context.getResources().getString(R.string.delete_all_record_title);
		
		final CommonDialog queryDialog = new CommonDialog(context);
		queryDialog.setMessage(message);
		queryDialog.setTitle(title);
		queryDialog.setCheckBox(false, R.string.not_notify_text);
		queryDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (queryDialog.isChecked()) {
					AppSettings.setDeleteALLQuery(mContext);
				}
			}
		});
		queryDialog.setPositiveButton(R.string.dialog_ok, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (queryDialog.isChecked()) {
					AppSettings.setDeleteALLQuery(mContext);
				}
				doClearTask(mContext);
			}
		});
		queryDialog.show();
	}
	
	private Toast mClearCacheToast;
	private void doClearTask(Context context) {
		Intent service = new Intent(context, DownloadService.class);
		service.setAction(DownloadService.ACTION_CLEAR_CACHE);
		context.startService(service);
		
		TaskList taskList = DataCenter.getInstance().getTaskList();
		List<DownloadTask> downloadList = taskList.getTaskList();
		if(downloadList != null) {
			List<DownloadTask> downloadingList = new ArrayList<DownloadTask>();
			List<DownloadTask> downloadedList = new ArrayList<DownloadTask>();
			int nCount = downloadList.size();
			for(int i= nCount -1; i>=0 ; i--) {
				DownloadTask task = downloadList.get(i);
				if(task.mStatus == TaskStatus.STATUS_DOWNLOAD || task.mStatus == TaskStatus.STATUS_INSTALLED) {
					taskList.removeTask(task);				
				} else {
					downloadingList.add(0, task);
				}
			}
			taskList.saveTaskListAsync();
			setData(downloadingList, downloadedList);
		}
		
		if (mClearCacheToast == null) {
			mClearCacheToast = Toast.makeText(context, R.string.clear_cache_now, Toast.LENGTH_SHORT);
		}
		mClearCacheToast.show();
		Statistics.addClearCacheCount(context);
	}
	
	private View getDownloadedView(int position, boolean isLastChild, View convertView, ViewGroup parent) {
		DownloadTask item = (DownloadTask) getChild(GROUP_DOWNLOADED, position);
		convertView = ItemBuilder.buildDownAppView(mInflater, position, item, convertView, parent);
		convertView.setOnClickListener(mOnItemClicked);
		DownAppViewHolder holder = (DownAppViewHolder) convertView.getTag();
		holder.mDownButton.setOnClickListener(mOnButtonClicked);
		return convertView;
	}
	
	private View getDownloadingView(int position, boolean isLastChild, View convertView, ViewGroup parent) {
		DownloadTask item = (DownloadTask) getChild(GROUP_DOWNLOADING, position);
		convertView = ItemBuilder.buildDownAppView(mInflater, position, item, convertView, parent);
		convertView.setOnClickListener(mOnItemClicked);
		DownAppViewHolder holder = (DownAppViewHolder) convertView.getTag();
		holder.mDownButton.setOnClickListener(mOnButtonClicked);
		return convertView;
	}

}
