package com.appmall.market.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.appmall.market.pulltorefresh.library.PullToRefreshBase;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.Mode;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.appmall.market.pulltorefresh.library.PullToRefreshExpandableListView;
import com.appmall.market.R;
import com.appmall.market.adapter.DownAppAdapter;
import com.appmall.market.adapter.DownAppAdapter.Callback;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskList;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.widget.CommonDialog;
import com.appmall.market.activity.MainActivity;
import com.appmall.market.common.NetworkStateReceiver;
/**
 * 涓嬭浇绠＄悊椤甸潰
 *  
 *
 */
public class ManageDownloadActivity extends BaseActivity implements Callback, OnClickListener {

	private PullToRefreshExpandableListView mGroupListViewContainer;
	private ExpandableListView mGroupListView;
	private DownAppAdapter mAdapter;
	private Context mContext;
	private static WeakReference<ManageDownloadActivity> mActivityRef;
	
	public static ManageDownloadActivity getMainActivity() {
		return mActivityRef.get();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = ManageDownloadActivity.this;
		setContentView(R.layout.activity_manage_download);
		mActivityRef = new WeakReference<ManageDownloadActivity>(this);
		initView();
	}
	
	private void initView() {
		mGroupListViewContainer = (PullToRefreshExpandableListView)findViewById(R.id.ptr_listview);
		mGroupListView = mGroupListViewContainer.getRefreshableView();
		mGroupListView.setEmptyView(findViewById(android.R.id.empty));
		findViewById(R.id.emptyview_button).setOnClickListener(this);
		findViewById(R.id.back_button).setOnClickListener(this);
		
		mAdapter = new DownAppAdapter(mContext, mGroupListView);
		mAdapter.registerCallback(this);
		mAdapter.setContext(mContext);
			
		mGroupListView.setAdapter(mAdapter);
		mGroupListView.setCacheColorHint(Color.TRANSPARENT);
		mGroupListView.expandGroup(DownAppAdapter.GROUP_DOWNLOADING);
		mGroupListView.expandGroup(DownAppAdapter.GROUP_DOWNLOADED);
		mGroupListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});
		
		mGroupListViewContainer.setOnRefreshListener(mOnDownloadListener);
		mGroupListViewContainer.setMode(Mode.DISABLED);
		
		showData();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back_button:		
				finish();
				break;
			case R.id.emptyview_button:
				MainActivity.getMainActivity().switchToTabActivity(MainActivity.TAB_PROMOTE);
				finish();
				break;
		}
	}
	
	private void showData() {
		TaskList taskList = DataCenter.getInstance().getTaskList();
		List<DownloadTask> downloadList = taskList.getTaskList();
		if(downloadList != null) {
			List<DownloadTask> downloadingList = new ArrayList<DownloadTask>();
			List<DownloadTask> downloadedList = new ArrayList<DownloadTask>();
			for(DownloadTask task: downloadList) {
				if(task.mStatus == TaskStatus.STATUS_INSTALLING
						|| task.mStatus == TaskStatus.STATUS_DOWNLOAD 
						|| task.mStatus == TaskStatus.STATUS_INSTALLED 
						|| task.mStatus == TaskStatus.STATUS_MERGING 
						|| task.mStatus == TaskStatus.STATUS_MERGING_INSTALL) {
					downloadedList.add(task);
				}else {
					downloadingList.add(task);
				}
			}
			if(mAdapter != null)
				mAdapter.setData(downloadingList, downloadedList);
		}	
	}
	
	private OnRefreshListener<ExpandableListView> mOnDownloadListener = new OnRefreshListener<ExpandableListView>() {
		@Override
		public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
			
		}
	};

	@Override
	public void update(Observable observable, Object data) {
		if (data == null || !(data instanceof Message))
			return;
		
		Message msg = (Message) data;
		switch (msg.what) {
		case DataCenter.MSG_DOWN_EVENT_PROGRESS:
			if(mAdapter != null)
				mAdapter.setProgress((DownloadTask) msg.obj);
			break;
		case DataCenter.MSG_LOCAL_APP_CHANGED:
		case DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED:
		case DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED:
			showData();
			break;
		case DataCenter.MSG_INSTALL_SIGNATURE_NOTIFY_EVENT:
			if(Utils.isTopActivity(this)) {
				String packageName = (String)msg.obj;
				if(packageName != null) {
					LocalAppInfo appInfo = DataCenter.getInstance().getLocalApps().getLocalPackage(packageName);
					if(appInfo != null)
						CommonInvoke.showSignatureDiffInstallDialog(this, packageName);
				}
			}
			break;
		case DataCenter.MSG_WIFI_TO_MOBILE_CHANGED_EVENT:
			if(Utils.isTopActivity(this)) {
				NetworkStateReceiver.showNetworkChangeQueryDialog(this);
			}
			break;
		case DataCenter.MSG_NET_STATE_CHANGED:
			break;
		}
	}
	
	private void showDelConfirmDialog(Context context, final DownloadTask task) {
		String message = context.getResources().getString(R.string.delete_record_content);
		String title = context.getResources().getString(R.string.delete_record_title);
		
		final CommonDialog queryDialog = new CommonDialog(context);
		queryDialog.setMessage(message);
		queryDialog.setTitle(title);
		queryDialog.setCheckBox(false, R.string.not_notify_text);
		queryDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (queryDialog.isChecked()) {
					AppSettings.setDeleteQuery(mContext);
				}
			}
		});
		queryDialog.setPositiveButton(R.string.dialog_ok, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (queryDialog.isChecked()) {
					AppSettings.setDeleteQuery(mContext);
				}
				Intent service = new Intent(mContext, DownloadService.class);
				service.setAction(DownloadService.ACTION_REMOVE_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				mContext.startService(service);	
			}
		});
		queryDialog.show();
	}
	
	@Override
	protected void refreshAppData() {
		showData();
	}

	@Override
	public void onCancelClicked(DownloadTask task) {
		if((task.mStatus == TaskStatus.STATUS_DOWNLOAD || task.mStatus == TaskStatus.STATUS_INSTALLED) && AppSettings.isDeleteQuery(mContext)) {
			showDelConfirmDialog(mContext, task);
		} else {
			Intent service = new Intent(mContext, DownloadService.class);
			service.setAction(DownloadService.ACTION_REMOVE_TASK);
			service.putExtra(DownloadService.EXTRA_TASK, task);
			mContext.startService(service);	
		}
	}

	@Override
	public void onPauseClicked(DownloadTask task) {
		Intent service = new Intent(mContext, DownloadService.class);
		service.setAction(DownloadService.ACTION_STOP_TASK);
		service.putExtra(DownloadService.EXTRA_TASK, task);
		mContext.startService(service);
	}

	@Override
	public void onResumeClicked(DownloadTask task) {
		Intent service = new Intent(mContext, DownloadService.class);
		service.setAction(DownloadService.ACTION_RESUME_TASK);
		service.putExtra(DownloadService.EXTRA_TASK, task);
		mContext.startService(service);
	}

	@Override
	public void onInstallClicked(DownloadTask task) {			
		boolean fileExist = false;	
		if (!TextUtils.isEmpty(task.mLocalPath)) {
			File apkFile = new File(task.mLocalPath);
			fileExist = apkFile.exists();
		}
		
		if (fileExist) {
			Utils.doTaskInstall(mContext, task);
		} else {
			CommonInvoke.showReDownloadDialog(this, task);
		}
	}
	
	@Override
	public void onMergeClicked(DownloadTask task) {
		String oldApkFilePath = DataCenter.getInstance().getLocalApps().getLocalPackage(task.mPackageName).mPackageDir;
		String patchFilePath = task.mLocalPath;
		String localPath = this.getFilesDir().getAbsolutePath();
		String tempNewFilePath = localPath + task.mPackageName + "_" + task.mVersionCode  +"_new.apk";
		String newApkFilePath = patchFilePath.substring(0, patchFilePath.length()-6);
		
		Log.d("demo", "oldApkFilePath = "+oldApkFilePath);
		Log.d("demo", "patchFilePath = "+patchFilePath);
		Log.d("demo", "newApkFilePath = "+newApkFilePath);
		if(oldApkFilePath  != null && newApkFilePath != null && patchFilePath != null)
			Utils.mergeApk(this, task, oldApkFilePath, newApkFilePath, patchFilePath, tempNewFilePath);
	}
		
	public void onOpenClicked(DownloadTask task) {
		PackageManager pm = mContext.getPackageManager();
		try {
			Intent intent = pm.getLaunchIntentForPackage(task.mPackageName);
			if (intent != null) {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		} catch (Exception e) { 
			Toast.makeText(mContext, "搴旂敤涓嶅瓨鍦�", Toast.LENGTH_SHORT).show();
		}
	}

}
