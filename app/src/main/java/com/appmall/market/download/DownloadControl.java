package com.appmall.market.download;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.NetworkStateReceiver;
import com.appmall.market.common.NotificationMgr;
import com.appmall.market.common.ShellUtils;
import com.appmall.market.common.Statistics;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.LocalApps;
import com.appmall.network.EventHandler;
import com.appmall.network.FileWriterMgr;
import com.appmall.network.HttpTaskEventArg;
import com.appmall.network.HttpTaskListener;
import com.appmall.network.HttpTaskMgr;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 下载控制逻辑
 *  
 *
 */
public class DownloadControl implements TaskStatus, HttpTaskListener {

	private static final int MAX_RUNNING_COUNT = 2;
	
	private static final int MSG_PROGRESS = 1;
	private static final int MSG_SUCCESS = 2;
	private static final int MSG_FAILED = 3;
	private static final int MSG_CANCEL = 4;
	private static final int MSG_TRY_NEXT_OR_STOP = 5;
	private static final int MSG_SAVE_TASK = 6;
	private static final int MSG_REDIRECT = 7;

	private DownloadService mService;
	private TaskList mTaskList;
	private SparseArrayCompat<DownloadTask> mDownloadList;
	private ControlHandler mHandler;
	private boolean mRunningFlag;
	private FileWriterMgr mFileWriteMgr;
	private Toast mDownStatusToast;

	public DownloadControl(DownloadService service) {
		mService = service;
		mRunningFlag = false;
		mDownloadList = new SparseArrayCompat<DownloadTask>();
		mHandler = new ControlHandler(this);
		mFileWriteMgr = new FileWriterMgr();
	}
	
	public synchronized void initControl() {
		mTaskList = DataCenter.getInstance().getTaskList();
	}
	
	public boolean isRunning() {
		return mRunningFlag;
	}
	
	public void addTask(DownloadTask task) {
		if (task == null || TextUtils.isEmpty(task.mPackageName))
			return;
		
		// 排重
		DownloadTask existTask = mTaskList.getTaskItem(task.mPackageName);
		if (existTask != null)
			return;

		if (TextUtils.isEmpty(task.mLocalPath))
			return;
		File targetFile = new File(task.mLocalPath);
		if (targetFile.exists()) {
			targetFile.delete();
		}
		
		task.mCreateTime = System.currentTimeMillis();
		
		int runningCount = mTaskList.getRunningTaskCount();
		if (runningCount >= MAX_RUNNING_COUNT) {
			task.mStatus = STATUS_WAIT;
			onTaskStatusChanged(task);
			
			mTaskList.addTask(task);
			onTaskListChanged(true);
			return;
		}
		
		boolean reqSuccess = startDownload(task);
		if (reqSuccess) {
			task.mStatus = STATUS_DOWNLOADING;
		} else {
			task.mStatus = STATUS_FAILED;
		}
		onTaskStatusChanged(task);
		mTaskList.addTask(task);
		onTaskListChanged(true);
		
		if (!reqSuccess) {
			mHandler.sendEmptyMessage(MSG_TRY_NEXT_OR_STOP);
		}
	}

	public void resumeTask(String packageName) {
		DownloadTask task = mTaskList.getTaskItem(packageName);
		if (task == null || (task.mStatus != STATUS_PAUSE && task.mStatus != STATUS_FAILED))
			return;
		
		int runningCount = mTaskList.getRunningTaskCount();
		if (runningCount >= MAX_RUNNING_COUNT) {
			task.mStatus = STATUS_WAIT;
			onTaskStatusChanged(task);
			return;
		}
		
		boolean reqSuccess = startDownload(task);
		if (reqSuccess) {
			task.mStatus = STATUS_DOWNLOADING;
		} else {
			task.mStatus = STATUS_FAILED;
		}
		onTaskStatusChanged(task);
		
		if (!reqSuccess) {
			mHandler.sendEmptyMessage(MSG_TRY_NEXT_OR_STOP);
		}
	}
	
	public void stopTask(String packageName) {
		if (TextUtils.isEmpty(packageName))
			return;
		
		DownloadTask task = mTaskList.getTaskItem(packageName);
		if (task == null || (task.mStatus != STATUS_DOWNLOADING && task.mStatus != STATUS_WAIT))
			return;
		
		if (task.mStatus == STATUS_WAIT) {
			task.mStatus = STATUS_PAUSE;
			onTaskStatusChanged(task);
			return;
		}
			
		int index = mDownloadList.indexOfValue(task);
		if (index >= 0) {
			int taskId = mDownloadList.keyAt(index);
			HttpTaskMgr.instance(mService).cancel(taskId);
			mHandler.obtainMessage(MSG_CANCEL, taskId, 0).sendToTarget();
		}
	}
	
	public void removeTask(String packageName) {
		if (TextUtils.isEmpty(packageName))
			return;
		stopTask(packageName);
		
		DownloadTask task = mTaskList.getTaskItem(packageName);
		boolean bNofify = true;
		if(task.mStatus == TaskStatus.STATUS_INSTALLED || task.mStatus == TaskStatus.STATUS_DOWNLOAD)
			bNofify = false;
		mTaskList.removeTask(task);
		onTaskListChanged(bNofify);
	}
	
	public void stopAllTask() {
		List<DownloadTask> list = mTaskList.getTaskList();
		if (list != null && list.size() > 0) {
			for (DownloadTask task : list) {
				stopTask(task.mPackageName);
			}
		}
	}
	
	public DownloadTask getTask(String packageName) {
		return mTaskList.getTaskItem(packageName);
	}
	
	private boolean startDownload(DownloadTask task) {
		if (task == null || TextUtils.isEmpty(task.mTaskUrl)) {
			if (!task.mHasStat) {
				Statistics.addDownFailedCount(mService, task.mPackageName, task.mSource, task.mChannelId, task.mIsPatch);
				task.mHasStat = true;
			}
			return false;
		}
		
		mRunningFlag = true;
		
		HttpTaskMgr taskMgr = HttpTaskMgr.instance(mService);
		String url = task.mTaskUrl;
		String savePath = task.mLocalPath;
		HashMap<String, String> userHeaders = Utils.generateXHeaders(mService, url, null);
		int id = taskMgr.sendDownloadRequest(url, savePath, true, null, this, task, userHeaders, mFileWriteMgr);
		if (id != HttpTaskMgr.HTTPTASK_INVALID_ID) {
			mDownloadList.append(id, task);		
			String format = mService.getString(R.string.toast_start_to_download);
			String toast = String.format(Locale.getDefault(), format, task.mTitle);
			showToast(toast);
			return true;
		} else {
			Statistics.addDownFailedCount(mService, task.mPackageName, task.mSource, task.mChannelId, task.mIsPatch);
			return false;
		}
	}
	
	public boolean startMultiDownload(int taskid, long startPos, long endPos, String url, String savePath) {
		HttpTaskMgr taskMgr = HttpTaskMgr.instance(mService);
		HashMap<String, String> userHeaders = Utils.generateXHeaders(mService, url, null);
		int id = taskMgr.sendMultiDownloadRequest(url, savePath, this, taskid, startPos, endPos, userHeaders, mFileWriteMgr);
		if (id != HttpTaskMgr.HTTPTASK_INVALID_ID) {
			return true;
		} else {
			return false;
		}
	}
	
	private void tryNextOrStopService() {
		DownloadTask next = mTaskList.getNextWaitTask();
		if (next == null) {
			if (mTaskList.getRunningTaskCount() <= 0) {
				mRunningFlag = false;
				mService.stopSelf();
//				NotificationMgr.cancelDownloadStatus(mService);
				NotificationMgr.cancelNotification(NotificationMgr.NOTIFICATION_TYPE_TEXT);
			}
			mHandler.sendEmptyMessage(MSG_SAVE_TASK);
			return;
		}
		
		int runningCount = mTaskList.getRunningTaskCount();
		if (runningCount >= MAX_RUNNING_COUNT) {
			next.mStatus = STATUS_WAIT;
			onTaskStatusChanged(next);
			return;
		}
		
		boolean reqSuccess = startDownload(next);
		if (reqSuccess) {
			next.mStatus = STATUS_DOWNLOADING;
		} else {
			next.mStatus = STATUS_FAILED;
		}
		onTaskStatusChanged(next);
		
		if (!reqSuccess) {
			mHandler.sendEmptyMessage(MSG_TRY_NEXT_OR_STOP);
		}
	}

	public List<DownloadTask> getTaskList() {
		return mTaskList.getTaskList();
	}

	private static class ControlHandler extends Handler {
		private static final String COMMAND_CHMOD = "chmod 604";
		private WeakReference<DownloadControl> mControl;
		private DataCenter mDC = DataCenter.getInstance();
		
		public ControlHandler(DownloadControl ctrl) {
			mControl = new WeakReference<DownloadControl>(ctrl);
		}
		
		@Override
		public void handleMessage(Message msg) {
			DownloadControl control = mControl.get();
			if (control == null)
				return;
			int errorId = msg.arg2;
			switch (msg.what) {
			case MSG_PROGRESS:
				int taskId = msg.arg1;
				HttpTaskEventArg arg = (HttpTaskEventArg)msg.obj;
				DownloadTask task = control.mDownloadList.get(taskId);
				if (task == null)
					return;
//				task.mThreadDownloadTaskList.clear();
//				task.mThreadDownloadTaskList.addAll(arg.mThreadDownloadTaskList);			
				task.mLastTransfered = task.mTransfered;
				task.mTransfered = arg.mlen;
				task.mTotal = arg.mTotal;
				long now = System.currentTimeMillis();
				long interval = now - task.mLastSpeedTime;
				if(task.mLastSpeedTime == 0) {	
					task.mSpeed = 0;
				}else {
					float seconds = (float)interval/1000;
					float speed = ((float)(task.mTransfered-task.mLastTransfered))/(seconds*1024);
					task.mLastSpeed7 = task.mLastSpeed6;
					task.mLastSpeed6 = task.mLastSpeed5;
					task.mLastSpeed5 = task.mLastSpeed4;
					task.mLastSpeed4 = task.mLastSpeed3;
					task.mLastSpeed3 = task.mLastSpeed2;
					task.mLastSpeed2 = task.mLastSpeed1;
					task.mLastSpeed1 = speed;
					
					{
						int speedNum7 = 0;
						float mSpeedTotal7 = 0;
						int speedNum3 = 0;
						float mSpeedTotal3 = 0;
						if(task.mLastSpeed1 != -1) {
							mSpeedTotal7 += task.mLastSpeed1;
							speedNum7++;
							mSpeedTotal3 += task.mLastSpeed1;
							speedNum3++;
						}
						if(task.mLastSpeed2 != -1) {
							mSpeedTotal7 += task.mLastSpeed2;
							speedNum7++;
							mSpeedTotal3 += task.mLastSpeed2;
							speedNum3++;
						}
						if(task.mLastSpeed3 != -1) {
							mSpeedTotal7 += task.mLastSpeed3;
							speedNum7++;
							mSpeedTotal3 += task.mLastSpeed3;
							speedNum3++;
						}
						if(task.mLastSpeed4 != -1) {
							mSpeedTotal7 += task.mLastSpeed4;
							speedNum7++;
						}
						if(task.mLastSpeed5 != -1) {
							mSpeedTotal7 += task.mLastSpeed5;
							speedNum7++;
						}
						if(task.mLastSpeed6 != -1) {
							mSpeedTotal7 += task.mLastSpeed6;
							speedNum7++;
						}
						if(task.mLastSpeed7 != -1) {
							mSpeedTotal7 += task.mLastSpeed7;
							speedNum7++;
						}
						float speedAve7 = 0;
						float speedAve3 = 0;
						if(speedNum7 >0)
							speedAve7 = mSpeedTotal7/speedNum7;
						if(speedNum3 >0)
							speedAve3 = mSpeedTotal3/speedNum3;
						if(speedAve7 > speedAve3)
							task.mSpeed = speedAve7;
						else
							task.mSpeed = speedAve3;
					}			
				}
				
				task.mLastSpeedTime = now;
				mDC.reportDownloadEvent(DataCenter.MSG_DOWN_EVENT_PROGRESS, task);
				sendEmptyMessageDelayed(MSG_SAVE_TASK, 3000);
				break;
			case MSG_SUCCESS:
				taskId = msg.arg1;
				task = control.mDownloadList.get(taskId);
				DataCenter.getInstance().reportDownloadEvent(DataCenter.MSG_DOWN_EVENT_TASK_FINISH, task);
				control.mDownloadList.remove(taskId);
				if (task == null)
					return;
				
				Context context = control.mService.getApplicationContext();
				if (!task.mHasStat) {
					Statistics.addDownSuccessCount(context, task.mPackageName, task.mSource, task.mChannelId, task.mIsPatch);
					task.mHasStat = true;
				}
					
				ShellUtils.execCommand(COMMAND_CHMOD + " " + task.mLocalPath);
				if(task.mIsPatch) {
					//合并patch流程
					task.mStatus = STATUS_MERGING;
					String oldApkFilePath = DataCenter.getInstance().getLocalApps().getLocalPackage(task.mPackageName).mPackageDir;
					String patchFilePath = task.mLocalPath;
					String localPath = context.getFilesDir().getAbsolutePath();
					String tempNewFilePath = localPath + task.mPackageName + "_new.apk";
					String newApkFilePath = patchFilePath.substring(0, patchFilePath.length()-6);

					if(oldApkFilePath  != null && newApkFilePath != null && patchFilePath != null)
						//Utils.mergeApk(context, task, oldApkFilePath, newApkFilePath, patchFilePath, tempNewFilePath);
						Utils.mergeApkNative(context, task, oldApkFilePath, newApkFilePath, patchFilePath, tempNewFilePath);
				} else if(task.mIsSignDiff) {
					task.mStatus = STATUS_DOWNLOAD;
					DataCenter.getInstance().reportDownloadEvent(DataCenter.MSG_INSTALL_SIGNATURE_NOTIFY_EVENT, task.mPackageName);
				} else {
					if (AppSettings.isRootInstall(context) && Utils.isPhoneRoot()) {
						task.mStatus = STATUS_INSTALLING;
						
						String format = context.getString(R.string.toast_download_installing);
						String toast = String.format(Locale.getDefault(), format, task.mTitle);
						control.showToast(toast);
						
						Utils.AsyncTaskExecute(new SilenceInstall(control.mService), task);
					} else if(AppSettings.isAutoInstall(context)) {
						task.mStatus = STATUS_DOWNLOAD;
						Utils.reqSystemInstall(context, task.mLocalPath);
					}
					else {
						task.mStatus = STATUS_DOWNLOAD;
						String format = context.getString(R.string.toast_download_success);
						String toast = String.format(Locale.getDefault(), format, task.mTitle);
						control.showToast(toast);
						String title = String.format(Locale.getDefault(),control.mService.getString(R.string.app_down_success), task.mTitle);
						String text = context.getString(R.string.click_to_install);
						NotificationMgr.showTextNotification(control.mService, title, text, NotificationMgr.NOTIFICATION_ACTION_INSTALLPACKAGE, task.mLocalPath);
					}
				}
				
				control.onTaskStatusChanged(task);
				sendEmptyMessage(MSG_TRY_NEXT_OR_STOP);
				break;
			case MSG_FAILED:
				if(errorId == EventHandler.ERROR_LOOKUP || errorId == EventHandler.ERROR_CONNECT) {
					if(NetworkStateReceiver.hasCapture()) {
						taskId = msg.arg1;
						task = control.mDownloadList.get(taskId);
						control.stopTask(task.mPackageName);
					} else				
						NetworkStateReceiver.captureRunningTaskList(control.mService);
					return;
				}
					
				taskId = msg.arg1;
				task = control.mDownloadList.get(taskId);
				control.mDownloadList.remove(taskId);
				if (task == null)
					return;
				
				context = control.mService.getApplicationContext();
				if (!task.mHasStat) {
					Statistics.addDownFailedCount(context, task.mPackageName, task.mSource, task.mChannelId, task.mIsPatch);
					task.mHasStat = true;
				}
				
				task.mStatus = STATUS_FAILED;
				control.onTaskStatusChanged(task);
				
				String format = control.mService.getString(R.string.toast_download_failed);
				if(errorId == EventHandler.ERROR_IO)
					format = "空间不足，"+format;
				String toast = String.format(Locale.getDefault(), format, task.mTitle);
				control.showToast(toast);
				
				sendEmptyMessage(MSG_TRY_NEXT_OR_STOP);
				break;
			case MSG_CANCEL:
				taskId = msg.arg1;
				task = control.mDownloadList.get(taskId);
				control.mDownloadList.remove(taskId);
				if (task == null)
					return;
				task.mSpeed = 0;
				task.mLastSpeedTime = 0;
				
				
				task.mStatus = STATUS_PAUSE;
				control.onTaskStatusChanged(task);
				sendEmptyMessage(MSG_TRY_NEXT_OR_STOP);
			case MSG_TRY_NEXT_OR_STOP:
				control.tryNextOrStopService();
				break;
			case MSG_SAVE_TASK:
				removeMessages(MSG_SAVE_TASK);
				control.mTaskList.saveTaskListAsync();
				break;
			case MSG_REDIRECT:
				taskId = msg.arg1;
				task = control.mDownloadList.get(taskId);
				if (task == null)
					return;
				task.mTaskUrl = (String) msg.obj;
				sendEmptyMessage(MSG_SAVE_TASK);
				break;
			default:
				throw new RuntimeException();
			}
		}
	}
	
	@Override
	public void onHttpTaskEvent(int taskid, int type, HttpTaskEventArg arg) {
		switch (type) {
		case HTTPTASK_EVENT_DATARECIVE:
			if (arg == null)
				return;
			mHandler.obtainMessage(MSG_PROGRESS, taskid, 0, arg).sendToTarget();
			break;
		case HTTPTASK_EVENT_END:
			mHandler.obtainMessage(MSG_SUCCESS, taskid, 0).sendToTarget();
			break;
		case HTTPTASK_EVENT_FAIL:
			if (arg == null)
				return;
			mHandler.obtainMessage(MSG_FAILED, taskid, arg.mErrorId).sendToTarget();
			break;
		case HTTPTASK_EVENT_REDIRECT:
			if (arg == null)
				return;
			String url = new String(arg.buffer);
			mHandler.obtainMessage(MSG_REDIRECT, taskid, 0, url).sendToTarget();
			break;
		}
	}

	public void onTaskStatusChanged(DownloadTask task) {
		Context context = mService;
		List<String> labels = mTaskList.getNeedDownTaskLabels();
//		NotificationMgr.showDownloadStatus(context, mDownLargeIcon, labels);
		NotificationMgr.showTextNotification(context, labels, NotificationMgr.NOTIFICATION_ACTION_RETURNTODOWNLOADPAGE);
		
		DataCenter.getInstance().reportDownloadEvent(DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED, task);
		mHandler.sendEmptyMessage(MSG_SAVE_TASK);
	}
	
	private void onTaskListChanged(boolean bNotify) {
		Context context = mService;
		List<String> labels = mTaskList.getNeedDownTaskLabels();
//		NotificationMgr.showDownloadStatus(context, mDownLargeIcon, labels);
		if(bNotify)
			NotificationMgr.showTextNotification(context, labels, NotificationMgr.NOTIFICATION_ACTION_RETURNTODOWNLOADPAGE);
		
		int eventId = DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED;
		DataCenter.getInstance().reportDownloadEvent(eventId, mTaskList.getTaskList());
		mHandler.sendEmptyMessage(MSG_SAVE_TASK);
	}

	public void checkForInstalled(String packageName) {
		DownloadTask task = mTaskList.getTaskItem(packageName);
		if (task == null)
			return;
		if (task.mStatus == TaskStatus.STATUS_DOWNLOAD || task.mStatus == TaskStatus.STATUS_INSTALLING || task.mStatus == TaskStatus.STATUS_INSTALLED) {
			int status = DataCenter.getInstance().getPackageInstallStatus(packageName, task.mVersionCode, task.mVersionName);
			if (status == LocalApps.STATUS_INSTALLED  || status == LocalApps.STATUS_INSTALLED_OLD_VERSION) {
				task.mStatus = TaskStatus.STATUS_INSTALLED;
				onTaskListChanged(true);
				
				String format = mService.getString(R.string.toast_download_inst_success);
				String toast = String.format(Locale.getDefault(), format, task.mTitle);
				String title = String.format(Locale.getDefault(),mService.getString(R.string.toast_download_inst_success), task.mTitle);
				String text = mService.getString(R.string.click_to_open);
				NotificationMgr.showTextNotification(mService, title, text, NotificationMgr.NOTIFICATION_ACTION_OPENPACKAGE, task.mPackageName);
				showToast(toast);
				
				if (AppSettings.isInstSuccessRemoveApk(mService) && !(TextUtils.isEmpty(task.mLocalPath))) {
					File apkFile = new File(task.mLocalPath);
					apkFile.delete();
				}
				if(task.mIsPatch && !task.mPatchHasStat) {
					task.mPatchHasStat = true;
					Statistics.addIncUpdateSuccessStat(mService, packageName);
				}
			} else {
				//已经被卸载
				task.mStatus = TaskStatus.STATUS_DOWNLOAD;
				onTaskListChanged(false);
			}
		}
	}
	
	public void installPackage(String packageName) {
		DownloadTask task = mTaskList.getTaskItem(packageName);
		if (task != null && task.mStatus == TaskStatus.STATUS_DOWNLOAD) {
			Utils.doTaskInstall(mService, task);
		}
	}
	
	private void showToast(String toast) {
		if (mDownStatusToast == null) {
			mDownStatusToast = Toast.makeText(mService, toast, Toast.LENGTH_SHORT);
		} else {
			mDownStatusToast.setText(toast);
		}
		mDownStatusToast.show();
	}
	
	public static class SilenceInstall extends AsyncTask<Object, Object, Object> {
		private Context mContext;
		private int mTargetVersion;
		private String mTargetVersionName;

		public SilenceInstall(Context context) {
			mContext = context;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			if (params[0] == null)
				return null;
			
			DownloadTask task = (DownloadTask) params[0];
			if (TextUtils.isEmpty(task.mLocalPath))
				return task;
			
			PackageManager pm = mContext.getPackageManager();
			PackageInfo archiveInfo = pm.getPackageArchiveInfo(task.mLocalPath, 0);
			if (archiveInfo == null || !task.mPackageName.equals(archiveInfo.packageName))
				return task;
			mTargetVersion = archiveInfo.versionCode;
			mTargetVersionName = archiveInfo.versionName;
			
			Utils.reqSilenceInstall(mContext, task.mLocalPath);				
			return task;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (result == null)
				return;
			
			DownloadTask task = (DownloadTask) result;

			PackageManager pm = mContext.getPackageManager();
			
			boolean success = true;
			try {
				PackageInfo info = pm.getPackageInfo(task.mPackageName, 0);
				success = (info.versionCode == mTargetVersion &&
						info.versionName != null && info.versionName.equals(mTargetVersionName));
			} catch (NameNotFoundException e) {
				success = false;
			}
			
			if (!success) {
				task.mStatus = STATUS_DOWNLOAD;
				if(DownloadService.getDownloadControl() != null)
					DownloadService.getDownloadControl().onTaskStatusChanged(task);
				String text = task.mTitle +mContext.getResources().getString(R.string.root_install_fail);			
				Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
			}
		}
	}

}
