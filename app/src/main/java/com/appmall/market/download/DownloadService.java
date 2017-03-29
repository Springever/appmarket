package com.appmall.market.download;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;

/**
 * 涓嬭浇鏈嶅姟
 *  
 *
 */
public class DownloadService extends Service {

	public static final String ACTION_START_TASK = "action_start_task";
	public static final String ACTION_STOP_TASK = "action_pause_task";
	public static final String ACTION_STOP_ALL_TASK = "action_stop_all_task";
	public static final String ACTION_RESUME_TASK = "action_resume_task";
	public static final String ACTION_REMOVE_TASK = "action_remove_task";
	public static final String ACTION_RESTART_TASK = "action_restart_task";
	public static final String ACTION_EXIST_UPDATE = "action_exist_update";
	public static final String ACTION_PACKAGE_CHANGED = "action_package_changed";
	public static final String ACTION_INSTALL_TASK = "action_install_task";
	public static final String ACTION_CLEAR_CACHE = "action_clear_cache";
	
	public static final String EXTRA_TASK = "task";
	public static final String EXTRA_PACKAGE_NAME = "package_name";
	
	
	//澶氱嚎绋嬩笅杞介渶瑕佺敤鍒扮殑鍙傛暟
	public static final String EXTRA_MULTI_STARTPOS = "startpos";
	public static final String EXTRA_MULTI_ENDPOS = "endpos";
	public static final String EXTRA_MULTI_TASKID = "taskid";
	public static final String EXTRA_MULTI_URL = "url";
	public static final String EXTRA_MULTI_SAVEPATH = "savepath";
	public static final String ACTION_START_MULTI_TASK = "action_start_multi_task";
	

	private ServiceBinder mBinder;
	private static DownloadControl mControl;
	private CacheCleanner mCacheCleanner;
	
	private Toast mAddTaskToast;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public static DownloadControl getDownloadControl() {
		return mControl;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		DataCenter.getInstance().ensureInit(this);
		mBinder = new ServiceBinder();
		mControl = new DownloadControl(this);
		mControl.initControl();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String action = null;
		if (intent != null) {
			action = intent.getAction();
		}
//		Log.d("demo", "onStartCommand = "+action);
		
		if (ACTION_START_TASK.equals(action)) {
			Serializable extra = intent.getSerializableExtra(EXTRA_TASK);
			if (extra != null && extra instanceof DownloadTask) {
				DownloadTask task = (DownloadTask) extra;
				showAddTaskToast(task);
				mControl.addTask(task);
			}
		} else if (ACTION_STOP_TASK.equals(action)) {
			Serializable extra = intent.getSerializableExtra(EXTRA_TASK);
			if (extra != null && extra instanceof DownloadTask) {
				mControl.stopTask(((DownloadTask) extra).mPackageName);
			}
		} else if (ACTION_STOP_ALL_TASK.equals(action)) {
			mControl.stopAllTask();
		} else if (ACTION_RESUME_TASK.equals(action)) {
			Serializable extra = intent.getSerializableExtra(EXTRA_TASK);
			if (extra != null && extra instanceof DownloadTask) {
				mControl.resumeTask(((DownloadTask) extra).mPackageName);
			}
		} else if (ACTION_REMOVE_TASK.equals(action)) {
			Serializable extra = intent.getSerializableExtra(EXTRA_TASK);
			if (extra != null && extra instanceof DownloadTask) {
				DownloadTask task = (DownloadTask) extra;
				mControl.removeTask(((DownloadTask) extra).mPackageName);
				if (!TextUtils.isEmpty(task.mLocalPath)) {
					File apkFile = new File(task.mLocalPath);
					apkFile.delete();
				}
			}
		} else if (ACTION_RESTART_TASK.equals(action)) {
			Serializable extra = intent.getSerializableExtra(EXTRA_TASK);
			if (extra != null && extra instanceof DownloadTask) {
				DownloadTask task = (DownloadTask) extra;
				mControl.removeTask(task.mPackageName);
				task.clear();
				if(task.mIsPatch && task.mPatchRedownloadUrl != null) {
					task.mTaskUrl = task.mPatchRedownloadUrl;
					task.mIsPatch = false;
				}					
				mControl.addTask(task);
			}
		} else if(ACTION_EXIST_UPDATE.equals(action)) {
			Serializable extra = intent.getSerializableExtra(EXTRA_TASK);
			if (extra != null && extra instanceof DownloadTask) {
				DownloadTask task = (DownloadTask) extra;
				mControl.removeTask(task.mPackageName);
				task.clear();
				mControl.addTask(task);
			}
		}else if (ACTION_PACKAGE_CHANGED.equals(action)) {
			DataCenter.getInstance().refreshLocalData(this);
			String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
			mControl.checkForInstalled(packageName);
			DataCenter.getInstance().reportLocalDataChanged();
		} else if (ACTION_INSTALL_TASK.equals(action)) {
			String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
			mControl.installPackage(packageName);
		} else if (ACTION_CLEAR_CACHE.equals(action)) {
			if (mCacheCleanner == null) {
				mCacheCleanner = new CacheCleanner(this);
				Utils.AsyncTaskExecute(mCacheCleanner);
			}
		} else if(ACTION_START_MULTI_TASK.equals(action)) {
			long startPos = intent.getLongExtra(EXTRA_MULTI_STARTPOS, 0);
			long endPos = intent.getLongExtra(EXTRA_MULTI_ENDPOS, 0);
			int taskid = intent.getIntExtra(EXTRA_MULTI_TASKID, 0);
			String url = intent.getStringExtra(EXTRA_MULTI_URL);
			String savePath = intent.getStringExtra(EXTRA_MULTI_SAVEPATH);
			mControl.startMultiDownload(taskid, startPos, endPos, url, savePath);
		}
		
		if (!mControl.isRunning())
			stopSelf();
		
		return super.onStartCommand(intent, flags, startId);
	}

	private void showAddTaskToast(DownloadTask task) {
		if (task == null)
			return;
		
		String formatter = getString(R.string.toast_already_added_to_download);
		String text = String.format(Locale.getDefault(), formatter, task.mTitle);
		if (mAddTaskToast == null) {
			mAddTaskToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		} else {
			mAddTaskToast.setText(text);
		}
		
		mAddTaskToast.show();
	}

	/**
	 * 璇锋眰浠诲姟鍒楄〃
	 * @return
	 */
	public List<DownloadTask> getTaskList() {
		return mControl.getTaskList();
	}
	
	/**
	 * 璇锋眰鏌愪竴椤逛换鍔�
	 */
	public DownloadTask getTask(String packageName) {
		return mControl.getTask(packageName);
	}
	
	private static class CacheCleanner extends AsyncTask<Object, Object, Object> {
		
		private WeakReference<DownloadService> mRef;

		public CacheCleanner(DownloadService service) {
			mRef = new WeakReference<DownloadService>(service);
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			DownloadService service = mRef.get();
			if (service == null)
				return null;
			
			List<String> filter = new ArrayList<String>();
			List<DownloadTask> list = service.getTaskList();
			if (list != null && list.size() > 0) {
				for (DownloadTask task : list) {
					if (TextUtils.isEmpty(task.mLocalPath))
						continue;
					filter.add(task.mLocalPath);
				}
			}
			
			CommonInvoke.clearDownloadDirectory(service, filter);
			CommonInvoke.clearCacheDirectory(service, filter);
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			DownloadService service = mRef.get();
			if (service == null)
				return;
			
			Toast.makeText(service, R.string.clear_cache_completed, Toast.LENGTH_LONG).show();
			service.mCacheCleanner = null;
		}
	}
	
	/**
	 * 鏈湴鏈嶅姟锛屽綋澶栭儴缁戝畾Binder鍚庯紝鍙互鐩存帴鑾峰彇Service鐨勫疄渚�
	 *  
	 *
	 */
	public class ServiceBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}
}
