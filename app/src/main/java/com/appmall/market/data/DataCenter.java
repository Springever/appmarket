package com.appmall.market.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.ImageView;

import com.appmall.market.common.Utils;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskList;

/**
 * 数据读取等操作接口
 *  
 *
 */
public class DataCenter extends Observable {

	public static final int MSG_LOCAL_APP_CHANGED = 0x0001;
	public static final int MSG_DOWN_EVENT_PROGRESS = 0x0011;
	public static final int MSG_DOWN_EVENT_STATUS_CHANGED = 0x0012;
	public static final int MSG_DOWN_EVENT_TASK_LIST_CHANGED = 0x0013;
	public static final int MSG_DOWN_EVENT_TASK_FINISH= 0x0014;
	public static final int MSG_UPDATE_COUNT_CHANGED = 0x0021;
	public static final int MSG_NET_STATE_CHANGED = 0x0031;
	public static final int MSG_NEW_DOWN_EVENT = 0x0041;
	public static final int MSG_INSTALL_SIGNATURE_NOTIFY_EVENT = 0x0051;
	public static final int MSG_WIFI_TO_MOBILE_CHANGED_EVENT = 0x0061;
	
	private static DataCenter sInstance = new DataCenter();

	public static DataCenter getInstance() {
		return sInstance;
	}

	private Context mAppContext;
	private LocalApps mLocalApps;
	private TaskList mTaskList;
	private DataCache mDataCache;
	private boolean mIsInitalize;
	
	private SparseArray<DataRequest> mRequests;
	private boolean mHasScannedLocal;
	
//	//测试环境
//	private String m_URL_SERVER = "cffront.test.uae.uc.cn";
//	private String m_UPDATE_SERVER = "appnotifierfront.test.uae.uc.cn";

	
	//正式环境
	private String m_URL_SERVER = "api.appchaoshi.cn";
	private String m_UPDATE_SERVER = "update.appchaoshi.cn";
	
	/**
	 * 执行必要的初始化
	 */
	public void ensureInit(Context context) {
		if (mIsInitalize)
			return;
		
		mAppContext = context.getApplicationContext();
		initDomainName(mAppContext);
		initUpdateDomainName(mAppContext);
		mDataCache = new DataCache();
		mLocalApps = new LocalApps();
		mRequests = new SparseArray<DataRequest>();
		mTaskList = new TaskList(context);
		mTaskList.initializeTasks();
		mIsInitalize = true;
	}
	
	
	private void initDomainName(Context context){
		String Result="";
        try { 
             InputStreamReader inputReader = new InputStreamReader( context.getResources().getAssets().open("domain.txt") ); 
             BufferedReader bufReader = new BufferedReader(inputReader);
             String line="";
             while((line = bufReader.readLine()) != null)
                Result += line;
             
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        if(!TextUtils.isEmpty(Result) && !Result.equalsIgnoreCase("$domain"))
        	m_URL_SERVER = Result;
	}
	
	private void initUpdateDomainName(Context context){
		String Result="";
        try { 
             InputStreamReader inputReader = new InputStreamReader( context.getResources().getAssets().open("domainupdate.txt") ); 
             BufferedReader bufReader = new BufferedReader(inputReader);
             String line="";
             while((line = bufReader.readLine()) != null)
                Result += line;
             
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        if(!TextUtils.isEmpty(Result) && !Result.equalsIgnoreCase("$domainupdate"))
        	m_UPDATE_SERVER = Result;
	}
	
	public String getDomainName() {
		return "http://"+m_URL_SERVER;
	}
	
	public String getUpdateDomainName() {
		return "http://"+m_UPDATE_SERVER;
	}
	
	/**
	 * 刷新本地安装列表
	 */
	public synchronized void refreshLocalData(Context context) {
		mLocalApps.scan(context);
		mHasScannedLocal = true;
	}
	
	/**
	 * 获取任务列表
	 * @param context
	 * @return
	 */
	public TaskList getTaskList() {
		return mTaskList;
	}
	
	/**
	 * 获取特定任务
	 */
	public DownloadTask getTask(String packageName) {
		return mTaskList.getTaskItem(packageName);
	}
	
	/**
	 * 是否有下载任务正在进行
	 */
	public boolean hasTaskRunning() {
		return mTaskList.getRunningTaskCount() > 0;
	}
	
	/**
	 * 查询本地是否安装了版本更高的某软件
	 */
	public int getPackageInstallStatus(String packageName, int versionCode, String versionName) {
		if (mLocalApps == null)
			return LocalApps.STATUS_NOT_INSTALLED;
		
		return mLocalApps.getPackageInstallStatus(packageName, versionCode, versionName);
	}
	
	/**
	 * 查询本地应用的签名
	 */
	public String getInstallPackageSignature(String packageName) {
		if (mLocalApps == null)
			return null;
		return mLocalApps.getInstallPackageSignature(packageName);
	}
	
	/**
	 * 获取本地数据
	 * @return 
	 */
	public List<LocalAppInfo> requestLocalData() {
		if (!mHasScannedLocal) {
			refreshLocalData(mAppContext);
		}
		return mLocalApps.getThirdPackages();
	}
	
	/**
	 * 获取所有本地应用，包含系统以及白名单应用
	 */
	public List<LocalAppInfo> requestAllLocalPackage() {
		if (!mHasScannedLocal) {
			refreshLocalData(mAppContext);
		}
		return mLocalApps.getPackages();
	}
	
	public LocalAppInfo requestLocalPackage(String packageName) {
		return mLocalApps.getLocalPackage(packageName);
	}
	
	public LocalApps getLocalApps() {
		return mLocalApps;
	}
	
	/**
	 * 获取数据
	 */
	public void requestDataAsync(Context context, int dataId, IDataCallback callback, Options options) {
		DataRequest runningReq = mRequests.get(dataId);
		if (runningReq != null && runningReq.getCallback() == callback) {
			runningReq.abort();
			mRequests.remove(dataId);
		}
		
		DataRequest req = new DataRequest(mAppContext, dataId, mDataCache, callback);
		mRequests.put(dataId, req);
		
		String customUrl = null;
		String urlAppend = null;
		IPostData post = null;
		
		if (options != null) {
			customUrl = options.mCustomUrl;
			urlAppend = options.mUrlAppend;
			post = options.mPostData;
			if (options.mTryCache) {
				req.tryCache();
			}
			if (options.mIsUpremindData) {
				req.setUpremindFlag();
			}
		}
		Utils.AsyncTaskExecute(req, customUrl, urlAppend, post);
	}
	
	public boolean hasCache(Context context, int dataId) {
		return mDataCache.hasCache(context, dataId);
	}
	
	/**
	 * 停止某项任务
	 */
	public void abortRequest(Context context, int dataId) {
		DataRequest runningReq = mRequests.get(dataId);
		if (runningReq != null) {
			runningReq.abort();
			mRequests.remove(dataId);
		}
	}
	
	public void reportDownloadEvent(int msgId, Object obj) {
		setChanged();
		Message msg = new Message();
		msg.what = msgId;
		msg.obj = obj;
		notifyObservers(msg);
	}
	
	public void reportLocalDataChanged() {
		setChanged();
		Message msg = new Message();
		msg.what = MSG_LOCAL_APP_CHANGED;
		notifyObservers(msg);
	}
	
	public void reportUpdateCountChanged() {
		setChanged();
		Message msg = new Message();
		msg.what = MSG_UPDATE_COUNT_CHANGED;
		notifyObservers(msg);
	}
	
	public void reportNetStateChanged() {
		setChanged();
		Message msg = new Message();
		msg.what = MSG_NET_STATE_CHANGED;
		notifyObservers(msg);
	}
	
	public void reportNewDownEvent(ImageView icon) {
		if(icon == null) return;
		int[] viewLocation = new int[2];
		icon.getLocationOnScreen(viewLocation);
		
		setChanged();
		Message msg = new Message();
		msg.what = MSG_NEW_DOWN_EVENT;
		msg.arg1 = viewLocation[0];
		msg.arg2 = viewLocation[1];
		msg.obj = icon;
		notifyObservers(msg);
	}
	
	public static class Options {
		public boolean mIsUpremindData;
		public boolean mTryCache;
		public String mCustomUrl;
		public String mUrlAppend;
		public Map<String, String> mQueryParams;
		public IPostData mPostData;
	}
	
	public static class Response {
		
		public static final int STATUS_FAILED = -1;
		
		public boolean mSuccess;
		public int mStatusCode;
		public Map<String, String> mContext;
		public IDataBase mData;
		public IDataBase mOtherData;
	}

}
