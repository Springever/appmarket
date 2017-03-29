package com.appmall.market.data;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.network.HttpTaskEventArg;
import com.appmall.network.HttpTaskListener;
import com.appmall.network.HttpTaskMgr;

/**
 * 统一处理数据获取请求
 *  
 *
 */
public class DataRequest extends AsyncTask<Object, Object, Object> 
		implements IDataConstant, HttpTaskListener {

	public static final int MSG_DATA_OBTAINED = 1;
	
	public static final String EXTRA_CONTEXT = "context";
	public static final String EXTRA_ENTIRES = "entires";

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=utf-8";

	private boolean mIsAbort;
	private boolean mEnableXHeader = true;
	private boolean mUpremindProcess;
	private boolean mTryCache;
	private Context mAppContext;
	private int mDataId;
	private int mHttpTaskId;
	private RespHandler mRespHandler;
	private WeakReference<DataCache> mCacheRef = new WeakReference<DataCache>(null);

	public DataRequest(Context context, int dataId, DataCache cache, IDataCallback callback) {
		mAppContext = context.getApplicationContext();
		mDataId = dataId;
		mCacheRef = new WeakReference<DataCache>(cache);
		mRespHandler = new RespHandler(this, dataId, callback);
	}
	
	public synchronized void abort() {
		mIsAbort = true;
		
		if (mHttpTaskId != HttpTaskMgr.HTTPTASK_INVALID_ID) {
			HttpTaskMgr.instance(mAppContext).cancel(mHttpTaskId);
			mHttpTaskId = HttpTaskMgr.HTTPTASK_INVALID_ID;
		}
		
		cancel(true);
	}
	
	public IDataCallback getCallback() {
		return mRespHandler.getCallback();
	}
	
	public synchronized boolean isAbort() {
		return mIsAbort;
	}

	public void tryCache() {
		mTryCache = true;
	}
	
	public void setUpremindFlag() {
		mEnableXHeader = false;
		mUpremindProcess = true;
	}

	@Override
	protected Object doInBackground(Object... params) {
		Context context = mAppContext;
		if (mTryCache) {
			DataCache cache = mCacheRef.get();
			if (cache != null) {
				Response resp = cache.loadCacheResponse(context, mDataId);
				if (resp != null) {
					notifyResponseHandler(mDataId, resp);
					return null;
				}
			}
		}

		// 1. 用户指定了URL
		String url = null;
		if (params[0] != null) {
			String customUrl = (String) params[0];
			url = customUrl;
		}
		
		// 2. 用户未指定URL，尝试按DataID拼接AppendUrl
		if (TextUtils.isEmpty(url)) {
			String appendUrl = null;
			if (params[1] != null) {
				appendUrl = (String) params[1];
			}
			url = getTargetUrl(mDataId, appendUrl);
		}
		
		if (TextUtils.isEmpty(url)) {
			notifyResponseHandler(mDataId, null);
			return null;
		}
		
		boolean isGet = true;
		byte[] postData = null;
		if (params[2] != null) {
			postData = ((IPostData) params[2]).buildPostData();
			isGet = false;
		}
		
		HashMap<String, String> headers = new HashMap<String, String>();
		if (mEnableXHeader) {
			headers = Utils.generateXHeaders(context, url, postData);
		}
		
		if (!isGet) {
			headers.put(CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
		}
		
		if (!isAbort()) {
			mHttpTaskId = HttpTaskMgr.instance(context).sendRequest(
					url, isGet, postData, this, headers);
			if (mHttpTaskId == HttpTaskMgr.HTTPTASK_INVALID_ID) {
				notifyResponseHandler(mDataId, null);
			}
		}
		
		return null;
	}
	
	@Override
	public void onHttpTaskEvent(int taskid, int type, HttpTaskEventArg arg) {
		Response resp = null;
		
		switch (type) {
		case HTTPTASK_EVENT_END:
			byte[] dataBytes = null;
			ByteArrayBuffer buffer = HttpTaskMgr.instance(mAppContext).readResponseData(taskid);
			if (buffer != null) {
				dataBytes = buffer.toByteArray();
			}
			
			resp = processResponse(dataBytes);
			break;
		case HTTPTASK_EVENT_FAIL:
			resp = new Response();
			resp.mSuccess = false;
			resp.mStatusCode = -1;
			break;
		default:
			// Do not pass here !!
			return;
		}
		
		if (!isAbort()) {
			notifyResponseHandler(mDataId, resp);
		}
	}
	
	private void notifyResponseHandler(int dataId, Response resp) {
		if (mRespHandler != null) {
			mRespHandler.obtainMessage(MSG_DATA_OBTAINED, dataId, 0, resp).sendToTarget();
		}
	}
	
	private Response processResponse(byte[] dataBytes) {
		try {
			DataProcessor processor = null;
			if (mUpremindProcess) {
				processor = new UpremindDataProcessor();
			} else {
				processor = new DataProcessor();
			}
			Response resp = processor.processData(mDataId, dataBytes);
			
			DataCache cache = mCacheRef.get();
			if (cache != null) {
				cache.saveResponseCache(mAppContext, mDataId, resp);
			}
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	final private String getTargetUrl(int dataId, String urlAppend) {
		switch (dataId) {
		case HOMEPAGE:
			return DataCenter.getInstance().getDomainName()+URL_HOMEPAGE;
		case HOMEPAGE_MORE:
			return DataCenter.getInstance().getDomainName()+URL_HOMEPAGE_MORE;
		case MUST_INSTALL_APP:
			return DataCenter.getInstance().getDomainName()+URL_MUST_INSTALL_APP;
		case MUST_INSTALL_GAME:
			return DataCenter.getInstance().getDomainName()+URL_MUST_INSTALL_GAME;
		case APP_CATEGORY:
			return DataCenter.getInstance().getDomainName()+URL_APP_CATEGORY;
		case APP_RANK:
			return DataCenter.getInstance().getDomainName()+URL_APP_RANK;
		case APP_NEW_PROD:
			return DataCenter.getInstance().getDomainName()+URL_APP_NEW_PROD;
		case GAME_CATEGORY:
			return DataCenter.getInstance().getDomainName()+URL_GAME_CATEGORY;
		case GAME_RANK:
			return DataCenter.getInstance().getDomainName()+URL_GAME_RANK;
		case GAME_NEW_PROD:
			return DataCenter.getInstance().getDomainName()+URL_GAME_NEW_PROD;
		case SEARCH_TAGS:
			return DataCenter.getInstance().getDomainName()+URL_SEARCH_TAGS;
		case SEARCH_RESULT:
			return DataCenter.getInstance().getDomainName()+URL_SEARCH_RESULT + urlAppend;
		case SEARCH_ASSOCIATION:
			return DataCenter.getInstance().getDomainName()+URL_ASSOCIATION + urlAppend;
		case UPDATE_INFO:
			return DataCenter.getInstance().getUpdateDomainName()+URL_UPDATE_INFO;
//			return "http://192.168.91.201:9101/cfappstore/v2/upgrade";
		case COMMENT_APP:
			return DataCenter.getInstance().getDomainName()+URL_COMMENT_APP;
		case CHECK_UPDATE:
			return DataCenter.getInstance().getDomainName()+URL_CHECK_UPDATE;		
		case STATISTICS:
			return DataCenter.getInstance().getDomainName()+URL_STATISTICS;
		case APP_DETAIL_COMMENT:
			return DataCenter.getInstance().getDomainName()+URL_APP_DETAIL_COMMENT + urlAppend;
		default:
			return null;
		}
	}
	
	private static class RespHandler extends Handler {

		private int mDataId;
		private WeakReference<IDataCallback> mCallbackRef;
		private WeakReference<DataRequest> mRequestRef;

		public RespHandler(DataRequest req, int dataId, IDataCallback callback) {
			mRequestRef = new WeakReference<DataRequest>(req);
			mDataId = dataId;
			mCallbackRef = new WeakReference<IDataCallback>(callback);
		}

		public IDataCallback getCallback() {
			return mCallbackRef.get();
		}
		
		@Override
		public void handleMessage(Message msg) {
			DataRequest req = mRequestRef.get();
			if (req == null || req.isAbort())
				return;
			IDataCallback callback = mCallbackRef.get();
			
			if (callback != null) {
				Response resp = null;
				if (msg.obj == null || !(msg.obj instanceof Response)) {
					resp = new Response();
					resp.mStatusCode = Response.STATUS_FAILED;
					resp.mSuccess = false;
				} else {
					resp = (Response) msg.obj;
				}
				
				callback.onDataObtain(mDataId, resp);
			}
		}
	}
	
}
