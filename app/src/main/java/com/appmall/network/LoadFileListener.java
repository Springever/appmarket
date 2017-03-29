package com.appmall.network;

import org.apache.http.util.ByteArrayBuffer;
import android.content.Context;
import android.os.Looper;
import android.os.Message;

class LoadFileListener extends LoadListener {
	// 鏂囦欢淇濆瓨鐨勮矾寰�
	protected String mSavePath;
	protected FileWriterMgr mFileWriterMgr;
	private boolean mPartial = false;
	private long mStartPos = 0;
	private long mEndPos = 0;
	private int mParentTaskid = -1;

	public static LoadFileListener getFileLoadListener(Context context, String url, String savePath,
			boolean synchronous, HttpTaskListener listener, int taskid, int parentid, Looper looper, FileWriterMgr writerMgr, boolean partial) {
		return new LoadFileListener(context, url, savePath, synchronous, listener, taskid, parentid,looper, writerMgr, partial);
	}

	public LoadFileListener(Context context, String url, String savePath, boolean synchronous,
			HttpTaskListener listener, int taskid, int parentid, Looper looper, FileWriterMgr writerMgr, boolean partial) {
		super(context, url, synchronous, listener, taskid, looper);
		mPartial = partial;
		mSavePath = savePath;
		mFileWriterMgr = writerMgr;
		mParentTaskid = parentid;
		LoadFileMultiListener parentListener = getParentListener(mParentTaskid);
		if(parentListener != null) {			
			parentListener.onMultiTaskCreate(this);
		}
//		Log.d("demo", "LoadFileListener m_taskid ="+m_taskid);
//		Log.d("demo", "LoadFileListener mParentTaskid ="+mParentTaskid);
	}
	
	@Override
	protected void handleData(Message msg) {
		if ((mStatusCode >= 301 && mStatusCode <= 303) || mStatusCode == 307) {
			// 閲嶅畾鍚�
			return;
		} else if (mStatusCode >= 400) {
			// Error
			return;
		}
					
		ByteArrayBuffer buffer = (ByteArrayBuffer) msg.obj;
		boolean bOk = mFileWriterMgr.onHandleData(m_taskid, buffer.buffer(), buffer.length());
		if(bOk)
			mStartPos += buffer.length();
		else
			onWriteError();
	}
	
	@Override
	public void cancel() {
//		Log.d("demo", "LoadFileListener cancel() m_taskid = "+m_taskid);
		super.cancel();
		mFileWriterMgr.removeWriteTask(m_taskid);
	}
	
	@Override
	protected void handleEndData() {	
		if ((mStatusCode == 206 || mStatusCode == 200) && mStartPos >= mEndPos) {
			mFileWriterMgr.onHandleEndData(m_taskid);
		}
		super.handleEndData();
	}

	@Override
	protected void handleHeaders(Headers headers) {
		super.handleHeaders(headers);
		if (isCanceled())
			return;

		if (mStatusCode == 206) {
			if (mStartPos != mRangeStart) {
				mStartPos = mRangeStart;
			}
		}
		
		if(mPartial)
			mEndPos = mRangeEnd;
		else
			mEndPos = mContentLength-1;
		
		if(mStatusCode == 206 || mStatusCode == 200) {
			boolean bOk = mFileWriterMgr.addWriteTask(m_taskid, mParentTaskid, mSavePath, mStartPos, mEndPos);
			if(!bOk) {
				failOpenFile();
			}
		}
			
	}

	@Override
	protected void handleStatus(int major, int minor, int code, String reason) {
		super.handleStatus(major, minor, code, reason);
		if (isCanceled())
			return;

		if (mStatusCode == 206 && mStartPos > 0) {
			return;
		}
				
		mStartPos = 0;
	}

	@Override
	protected boolean handlePartialContent() {
		if(isCanceled())
			return true;
		
		if (mStartPos < mEndPos) {
			Network network = Network.getInstance(getContext());
			long begin = mStartPos;
			String range = "bytes=" + begin + "-";
			mRequestHeaders.put("Range", range);
			if (network.requestURL(mMethod, mRequestHeaders, mPostData, this)) {
				return true;
			} else {
				return false;
			}
		} else if (mEndPos+1 == mStartPos) {
			mFileWriterMgr.onHandleEndData(m_taskid);
			LoadFileMultiListener parentListener = getParentListener(mParentTaskid);
			if(parentListener != null) {			
				parentListener.onMultiTaskFinish();
			}
			return true;
		} else {
			return false;
		}
	}
	
	private LoadFileMultiListener getParentListener(int parentid) {
		return (LoadFileMultiListener) HttpTaskMgr.instance(mContext).getTask(parentid);
	}

	private void failOpenFile() {
		cancel();

		HttpTaskEventArg arg = new HttpTaskEventArg();
		arg.mErrorId = HttpTaskListener.FILE_ERROR;
		mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
		return;
	}
	
	private void onWriteError() {
		cancel();
		HttpTaskEventArg arg = new HttpTaskEventArg();
		arg.mErrorId = ERROR_IO;
		mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
	}
}