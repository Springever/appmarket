package com.appmall.market.download;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;
import java.util.ArrayList;

/**
 * 下载任务
 *  
 *
 */
public class DownloadTask implements TaskStatus, IDataBase, Serializable {
	
	public static class ThreadDownloadTask{
		public long mStartPos = 0;
		public long mEndPos = 0;
	}

	public static final int SOURCE_APPMARKET = 0;
	public static final int SOURCE_UPDATE_LIST = 1;
	public static final int SOURCE_UPDATE_OP = 2;
	
	private static final long serialVersionUID = -5652095125056203361L;
	
	private static final String NAME_PACKAGE_NAME = "package_name";
	private static final String NAME_STATUS = "status";
	private static final String NAME_TITLE = "title";
	private static final String NAME_ICON_DATA = "icon_path";
	private static final String NAME_TRANSFERED = "transfered";
	private static final String NAME_TOTAL = "total";
	private static final String NAME_TASK_URL = "task_url";
	private static final String NAME_LOCAL_PATH = "local_path";
	private static final String NAME_CREATE_TIME = "create_time";
	private static final String NAME_VERSION_CODE = "version_code";
	private static final String NAME_VERSION_NAME = "version_name";
	private static final String NAME_HAS_STAT = "has_stat";
	private static final String NAME_SOURCE = "source";
	private static final String NAME_CHANNEL_ID = "channel_id";
	private static final String NAME_MULTI_FLAG = "multiflag";
	private static final String NAME_IS_PATCH = "is_patch";
	private static final String NAME_PATCH_REDOWNLOAD = "patch_redownload";
	private static final String NAME_IS_SIGNDIFF = "is_signdiff";
	
//	private static final String NAME_THREAD_ARRAY = "taskarray";
//	private static final String NAME_THREAD_STARTPOS = "startpos";
//	private static final String NAME_THREAD_ENDPOS = "endpos";
	
	
	public String mPackageName;
	public int mStatus;
	public String mTitle;
	public String mIconData;
	public String mTaskUrl;
	public String mLocalPath;
	public long mCreateTime;
	public int mVersionCode;
	public String mVersionName;
	public boolean mHasStat;
	public int mSource;
	public int mChannelId;
	public boolean mIsSignDiff = false;
	
	
	public boolean  mIsPatch = false;
	public boolean mPatchHasStat = false;
	public String mPatchRedownloadUrl;
		
	public long mLastTransfered;
	public long mLastSpeedTime = 0;
	public float mSpeed = 0;
	public long mTransfered = 0;
	public long mTotal = 0;
	public int mSpeedIndex =1;
	
	public float mLastSpeed1 = -1;
	public float mLastSpeed2 = -1;
	public float mLastSpeed3 = -1;
	public float mLastSpeed4 = -1;
	public float mLastSpeed5 = -1;
	public float mLastSpeed6 = -1;
	public float mLastSpeed7 = -1;
	
	//多线程参数
	public boolean mMuiltDownload = false;
	public final int MAX_THREAD_NUM =2;
	public ArrayList<ThreadDownloadTask> mThreadDownloadTaskList = new ArrayList<ThreadDownloadTask>(MAX_THREAD_NUM);
	
//	public void checkThreadTaskList() {
//		if(mThreadDownloadTaskList.size() == 0) {
//			long div = mTotal/MAX_THREAD_NUM;
//			for(int i=0; i< MAX_THREAD_NUM; i++) {
//				ThreadDownloadTask threadTask = new ThreadDownloadTask();
//				if(i == MAX_THREAD_NUM-1) {
//					threadTask.mStartPos = i*div;
//					threadTask.mEndPos = mTotal-1;	
//				} else {
//					threadTask.mStartPos = i*div;
//					threadTask.mEndPos = (i+1)*div-1;	
//				}
//				mThreadDownloadTaskList.add(threadTask);
//			}
//		}
//	}
//	public long getMultiDownloadStartPos(int index) {
//		long startPos = 0;
//		ThreadDownloadTask threadTask = mThreadDownloadTaskList.get(index);
//		if(threadTask != null)
//			startPos = threadTask.mStartPos;
//		return startPos;
//	}
//	public long getMultiDownloadEndPos(int index) {
//		long endPos = 0;
//		ThreadDownloadTask threadTask = mThreadDownloadTaskList.get(index);
//		if(threadTask != null)
//			endPos = threadTask.mEndPos;
//		return endPos;
//	}
//	public int getMultiDownloadCount() {
//		return mThreadDownloadTaskList.size();
//	}
		
	public static DownloadTask buildNewTask(String packageName, int source, int channelid, boolean isPatch, String patchRedownloadUrl) {
		DownloadTask task = new DownloadTask();
		task.mPackageName = packageName;
		task.mSource = source;
		task.mChannelId = channelid;
		task.mIsPatch = isPatch;
		task.mPatchRedownloadUrl = patchRedownloadUrl;
		return task;
	}
	
	public static DownloadTask buildNewTask(String packageName, int source, int channelid) {
		DownloadTask task = new DownloadTask();
		task.mPackageName = packageName;
		task.mSource = source;
		task.mChannelId = channelid;
		return task;
	}
		
	public void clear() {
		mLastSpeed1 = -1;
		mLastSpeed2 = -1;
		mLastSpeed3 = -1;
		mLastSpeed4 = -1;
		mLastSpeed5 = -1;
		mLastTransfered = 0;
		mLastSpeedTime = 0;
		mSpeed = 0;
		mTransfered = 0;
		mMuiltDownload = false;
		mThreadDownloadTaskList.clear();
	}
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mPackageName = jsonObj.optString(NAME_PACKAGE_NAME);
		mStatus = jsonObj.optInt(NAME_STATUS, STATUS_UNKNOWN);
		mTitle = jsonObj.optString(NAME_TITLE);
		mIconData = jsonObj.optString(NAME_ICON_DATA);
		mTransfered = jsonObj.optLong(NAME_TRANSFERED);
		mLastTransfered = mTransfered;
		mTotal = jsonObj.optLong(NAME_TOTAL);
		mTaskUrl = jsonObj.optString(NAME_TASK_URL);
		mLocalPath = jsonObj.optString(NAME_LOCAL_PATH);
		mCreateTime = jsonObj.optLong(NAME_CREATE_TIME);
		mVersionCode = jsonObj.optInt(NAME_VERSION_CODE);
		mVersionName = jsonObj.optString(NAME_VERSION_NAME);
		mHasStat = jsonObj.optBoolean(NAME_HAS_STAT);
		mSource = jsonObj.optInt(NAME_SOURCE);
		mChannelId = jsonObj.optInt(NAME_CHANNEL_ID);
		mMuiltDownload = jsonObj.optBoolean(NAME_MULTI_FLAG, false);
		mIsPatch = jsonObj.optBoolean(NAME_IS_PATCH);
		mPatchRedownloadUrl = jsonObj.optString(NAME_PATCH_REDOWNLOAD);
		mIsSignDiff = jsonObj.optBoolean(NAME_IS_SIGNDIFF);
//		//多线程数据
//		{
//			mThreadDownloadTaskList.clear();
//			JSONArray jsonArray = jsonObj.getJSONArray(NAME_THREAD_ARRAY);
//			for(int i=0; i< jsonArray.length(); i++) {
//				JSONObject jsonTask = jsonArray.getJSONObject(i);
//				ThreadDownloadTask downloadTask = new ThreadDownloadTask();
//				downloadTask.mStartPos = jsonTask.optLong(NAME_THREAD_STARTPOS);
//				downloadTask.mEndPos = jsonTask.optLong(NAME_THREAD_ENDPOS);
//				mThreadDownloadTaskList.add(downloadTask);
//			}
//		}
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(NAME_PACKAGE_NAME, mPackageName);
		jsonObj.put(NAME_STATUS, mStatus);
		jsonObj.put(NAME_TITLE, mTitle);
		jsonObj.put(NAME_ICON_DATA, mIconData);
		jsonObj.put(NAME_TRANSFERED, mTransfered);
		jsonObj.put(NAME_TOTAL, mTotal);
		jsonObj.put(NAME_TASK_URL, mTaskUrl);
		jsonObj.put(NAME_LOCAL_PATH, mLocalPath);
		jsonObj.put(NAME_CREATE_TIME, mCreateTime);
		jsonObj.put(NAME_VERSION_CODE, mVersionCode);
		jsonObj.put(NAME_VERSION_NAME, mVersionName);
		jsonObj.put(NAME_HAS_STAT, mHasStat);
		jsonObj.put(NAME_SOURCE, mSource);
		jsonObj.put(NAME_CHANNEL_ID, mChannelId);
		jsonObj.put(NAME_MULTI_FLAG, mMuiltDownload);
		jsonObj.put(NAME_IS_PATCH, mIsPatch);
		jsonObj.put(NAME_PATCH_REDOWNLOAD, mPatchRedownloadUrl);
		jsonObj.put(NAME_IS_SIGNDIFF, mIsSignDiff);
		
		
//		//多线程数据
//		{
//			try{
//				JSONArray jsonArray = new JSONArray();
//				for(ThreadDownloadTask threadTask : mThreadDownloadTaskList) {
//					JSONObject jsonTask = new JSONObject();
//					jsonTask.put(NAME_THREAD_STARTPOS, threadTask.mStartPos);
//					jsonTask.put(NAME_THREAD_ENDPOS, threadTask.mEndPos);
//					jsonArray.put(jsonTask);
//				}
//				jsonObj.put(NAME_THREAD_ARRAY, jsonArray);
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//
//		}	
		return jsonObj;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DownloadTask))
			return false;
		DownloadTask another = (DownloadTask) o;
		
		if (mPackageName == null) {
			return another.mPackageName == null;
		} else {
			return mPackageName.equals(another.mPackageName);
		}
	}
	
	public void setSignDiff(boolean isDiff) {
		mIsSignDiff = isDiff;
	}

	@Override
	public int hashCode() {
		return mPackageName == null ? 0 : mPackageName.hashCode();
	}
	
}
