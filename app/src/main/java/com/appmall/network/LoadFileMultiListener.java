package com.appmall.network;

import java.util.ArrayList;

import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.network.FileWriterMgr.FileWriter;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.appmall.market.download.DownloadTask.ThreadDownloadTask;


class LoadFileMultiListener extends LoadFileListener {
	
	
	private ArrayList<LoadFileListener> mLoadFileListenerList = null;
	private boolean mUseMultiDownload = false;
	private Context mContext;
	private static int MAX_MULTI_NUM = 2;
	private DownloadTask mTask;
	
	public static LoadFileMultiListener getFileLoadMultiListener(Context context, DownloadTask task,
			boolean synchronous, HttpTaskListener listener, int taskid, Looper looper, FileWriterMgr writerMgr) {
//		task.mTaskUrl = "http://down5.game.uc.cn/s/1/1/201309091957578fe5ee_libdwjyxp_5.4_160914uW1mcJ.apk";
//		task.mTaskUrl = "http://wap3.ucweb.com/files/UCBrowser/zh-cn/999/UCBrowser_V9.7.5.418_Android_pf145_(Build14042410).apk?SESSID=20140425aFV9808j0261%3Aunkown";
		return new LoadFileMultiListener(context, task, synchronous, listener, taskid, looper, writerMgr);
	}
	
	LoadFileMultiListener(Context context, DownloadTask task,boolean synchronous, HttpTaskListener listener, int taskid, Looper looper, FileWriterMgr writerMgr) {
		super(context, task.mTaskUrl, task.mLocalPath, synchronous, listener, taskid, taskid, looper, writerMgr, false);
		mTask = task;
		mContext = context;
//		Log.d("demo", "starttime m_taskid = " + m_taskid);
	}
	
	public void setTransfered(long transfered) {
		mFileWriterMgr.addWriteLen(m_taskid, transfered);
	}
	
	public void setTotal(long total) {
		mFileWriterMgr.setTotalLen(m_taskid, total);
	}
	
	@Override
	public void cancel() {
		super.cancel();
		removeMessages(TIMER_MSG_ID);
		if(mHasMultiDownload && mUseMultiDownload)
		{
			stopMultiDownloader();
		}
//		Log.e("demo", "writer count = "+ mFileWriterMgr.getWriterCount());
	}
	
	private static final int TIMER_MSG_ID = 9999; 
	private static final int NOTIFY_INTERVAL = 500;
	HttpTaskEventArg mArg = new HttpTaskEventArg();
	private void notifyProgress() {
		removeMessages(TIMER_MSG_ID);
		sendMessageDelayed(obtainMessage(TIMER_MSG_ID), NOTIFY_INTERVAL);
		if (mTaskListener != null) {
			if(mArg == null)
				mArg = new HttpTaskEventArg();
			mArg.mlen = mFileWriterMgr.getWriteLen(m_taskid);
			mArg.mTotal = mFileWriterMgr.getTotalLen(m_taskid);
			mArg.buffer = null;
			if(mUseMultiDownload) 
			{
				ArrayList<FileWriter> array = mFileWriterMgr.getFileWriterList(m_taskid);
				if(array != null) {
					mArg.mThreadDownloadTaskList.clear();
					for(FileWriter writer : array) {
						ThreadDownloadTask threadTask = new ThreadDownloadTask();
						threadTask.mStartPos = writer.mStartPos;
						threadTask.mEndPos = writer.mEndPos;
						mArg.mThreadDownloadTaskList.add(threadTask);
					}
				}
			}
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_DATARECIVE, mArg);
		}
	}
	
	
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) { 
			case TIMER_MSG_ID:
				notifyProgress();
				break;
		}
	}
	
	@Override
	protected void handleData(Message msg) {
		super.handleData(msg);
	}
	
	private boolean mHasMultiDownload = false;
	@Override
	protected void handleHeaders(Headers headers) {
		super.handleHeaders(headers);
		mFileWriterMgr.setTotalLen(m_taskid, mContentLength);
		if (mStatusCode == 206 && mUseMultiDownload && mContentLength >0) {
			cancel();
			mHasMultiDownload = true;
			startMultiDownloader();
		} else {
			notifyProgress();
		}
	}
	
	public void continueMultiDowloader(DownloadTask task) {
		for(ThreadDownloadTask threadTask : task.mThreadDownloadTaskList) {
			Log.d("demo", "continueMultiDowloader mStartPos = "+threadTask.mStartPos + "continueMultiDowloader mEndPos = "+threadTask.mEndPos);
			Intent service = new Intent(mContext, DownloadService.class);
			service.setAction(DownloadService.ACTION_START_MULTI_TASK);
			service.putExtra(DownloadService.EXTRA_MULTI_STARTPOS, threadTask.mStartPos);
			service.putExtra(DownloadService.EXTRA_MULTI_ENDPOS, threadTask.mEndPos);
			service.putExtra(DownloadService.EXTRA_MULTI_TASKID, m_taskid);
			service.putExtra(DownloadService.EXTRA_MULTI_URL, mTask.mTaskUrl);
			service.putExtra(DownloadService.EXTRA_MULTI_SAVEPATH, mTask.mLocalPath);
			mContext.startService(service);
		}
		notifyProgress();
	}

	private void startMultiDownloader() {
		mTask.mMuiltDownload = true;
		for(int i=0; i< MAX_MULTI_NUM; i++) {
			long div = mContentLength/MAX_MULTI_NUM;
			long startPos = 0;
			long endPos = 0;
			if(i == MAX_MULTI_NUM-1) {
				startPos = i*div;
				endPos = mContentLength-1;	
			} else {
				startPos = i*div;
				endPos = (i+1)*div-1;	
			}
			Intent service = new Intent(mContext, DownloadService.class);
			service.setAction(DownloadService.ACTION_START_MULTI_TASK);
			service.putExtra(DownloadService.EXTRA_MULTI_STARTPOS, startPos);
			service.putExtra(DownloadService.EXTRA_MULTI_ENDPOS, endPos);
			service.putExtra(DownloadService.EXTRA_MULTI_TASKID, m_taskid);
			service.putExtra(DownloadService.EXTRA_MULTI_URL, mTask.mTaskUrl);
			service.putExtra(DownloadService.EXTRA_MULTI_SAVEPATH, mTask.mLocalPath);
			mContext.startService(service);
		}
		notifyProgress();
	}
	
	private void stopMultiDownloader() {
		if(mLoadFileListenerList != null) {
			for(LoadFileListener listener : mLoadFileListenerList) {
				listener.cancel();
			}
			mLoadFileListenerList.clear();
		}		
	}
	
	public void onMultiTaskFinish() {
		long writeLen = mFileWriterMgr.getWriteLen(m_taskid);
		long totalLen = mFileWriterMgr.getTotalLen(m_taskid);
//		Log.d("demo", "onMultiDownloaderFinish writeLen = "+writeLen);
//		Log.d("demo", "onMultiDownloaderFinish totalLen = "+totalLen);
		if (mTaskListener != null && writeLen >= totalLen) {
			notifyProgress();
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_END, null);		
		}
//		Log.e("demo", "writer count = "+ mFileWriterMgr.getWriterCount());
//		Log.d("demo", "endtime m_taskid = " + m_taskid);
	}
	
	public void onMultiTaskCreate(LoadFileListener listener) {
//		Log.d("demo", "onMultiTaskCreate m_taskid = " + m_taskid);
		if(mLoadFileListenerList == null)
			mLoadFileListenerList = new ArrayList<LoadFileListener>();
		mLoadFileListenerList.add(listener);
	}
	
	@Override
	protected void handleStatus(int major, int minor, int code, String reason) {
		super.handleStatus(major, minor, code, reason);
	}
	
	@Override
	protected void handleEndData() {
		super.handleEndData();
	}
		
}