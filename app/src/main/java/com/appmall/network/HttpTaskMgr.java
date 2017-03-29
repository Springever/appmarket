/*
 *****************************************************************************
 * Copyright (C) 2005-2011 UCWEB Corporation. All Rights Reserved
 * File        : HttpTaskMgr.java
 * Description : HTTP浠诲姟绠＄悊绫�
 * 				 瀵笰ndroid婧愮爜涓璦ndroid.webkit.Network妯″潡鐨勫皝瑁�
 * Creation    : 2011/07/18
 * Author      : zhangxm@ucweb.com
 * History     : 
 ******************************************************************************
 **/
package com.appmall.network;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.apache.http.util.ByteArrayBuffer;

import com.appmall.market.download.DownloadTask;

import android.content.Context;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

/**
 * HTTP浠诲姟绠＄悊绫伙紝鎻愪緵HTTP涓婁紶锛屼笅杞藉姛鑳� 鍚屾椂鏀寔澶氫釜浠诲姟
 **/
public class HttpTaskMgr {
    // Use a singleton.
    private static final String DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    public static final int HTTPTASK_INVALID_ID = -1;
    
    private static HttpTaskMgr mManager;
    
    private Context mContext;
    private int mTaskid = 0;
    private boolean mConnectionSetup = false;

    private WeakHashMap<LoadListener, Integer> mListenerMap = new WeakHashMap<LoadListener, Integer>();
    private List<SoftReference<ConnectionSetupNotify>> mConSetupListener = new ArrayList<SoftReference<ConnectionSetupNotify>>();

    public static synchronized HttpTaskMgr instance(Context context) {
        if (mManager == null && context != null) {
            mManager = new HttpTaskMgr(context);
        }
        return mManager;
    }

    /**
     * 鍔熻兘锛氬彂閫丠TTP璇锋眰
     * 
     * @param url
     *            : 璇锋眰鐨勫湴鍧�
     * @param savePath
     *            : 鏈嶅姟鍣ㄨ繑鍥炵殑鍐呭淇濆瓨鐨勫湴鍧�锛屽鏋滄槸null锛屽垯鏄繚瀛樺埌鍐呭瓨
     * @param bGet
     *            : 涓簍rue鏃舵槸GET璇锋眰锛屼负false鏃舵槸POST璇锋眰
     * @param postData
     *            : post璇锋眰鏃讹紝postData瀛樻斁POST鐨勬暟鎹�
     * @param listener
     *            : HTTP浠诲姟鐘舵�侀�氱煡鐨勬帴鍙�
     * @return锛氳姹傛垚鍔熸椂杩斿洖Task鐨刬d锛屽け璐ユ椂杩斿洖HTTPTASK_INVALID_ID
     **/
    public int sendMultiDownloadRequest(final String url, final String savePath,
            HttpTaskListener listener,int parentTaskid, long startPos, long endPos, HashMap<String, String> userHeaders, FileWriterMgr writerMgr) {
        synchronized (this) {
            if (sWorkThread == null) {
                sWorkThread = new HandlerThread("LoadListener WorkThread");
                sWorkThread.start();
                
                // Wait for thread start
                SystemClock.sleep(50);
            }
        }
        int taskid = newTaskId();
        HashMap<String, String> headers = new HashMap<String, String>();
        populateStaticHeaders(headers);
        LoadFileListener loader = LoadFileListener.getFileLoadListener(mContext, url, savePath, false,
                listener, taskid, parentTaskid,sWorkThread.getLooper(), writerMgr, true);
        String method = new String("GET");
        Log.d("demo", "sendMultiDownloadRequest startPos = " + startPos + " endPos = "+endPos);
        addContentRange(headers, startPos, endPos);
        int error = 0;
        boolean ret = false;
        try {
            ret = Network.getInstance(mContext).requestURL(method, headers, null, loader);
        } catch (android.net.ParseException ex) {
            error = EventHandler.ERROR_BAD_URL;
        } catch (java.lang.RuntimeException ex) {
            ex.printStackTrace();
            error = EventHandler.ERROR_BAD_URL;
        }

        if (!ret || error != 0) {
            return HTTPTASK_INVALID_ID;
        }
        return taskid;
    }
    
    public int sendDownloadRequest(final String url, final String savePath, boolean bGet, byte[] postData,
            HttpTaskListener listener, DownloadTask task, HashMap<String, String> userHeaders, FileWriterMgr writerMgr) {
        synchronized (this) {
            if (sWorkThread == null) {
                sWorkThread = new HandlerThread("LoadListener WorkThread");
                sWorkThread.start();
                
                // Wait for thread start
                SystemClock.sleep(50);
            }
        }
        
        int taskid = newTaskId();
        HashMap<String, String> headers = new HashMap<String, String>();
        populateStaticHeaders(headers);
        LoadFileMultiListener loader = LoadFileMultiListener.getFileLoadMultiListener(mContext, task, false,
                listener, taskid, sWorkThread.getLooper(), writerMgr);
        String method = new String("GET");
        //璁や负鏄垵娆′笅杞�
        appendHeader(headers, userHeaders);
        if(task.mTransfered == 0) {
        	addContentRange(headers, 0);
        	loader.setTransfered(0);
            int error = 0;
            boolean ret = false;
            try {
                ret = Network.getInstance(mContext).requestURL(method, headers, postData, loader);
            } catch (android.net.ParseException ex) {
                error = EventHandler.ERROR_BAD_URL;
            } catch (java.lang.RuntimeException ex) {
                ex.printStackTrace();
                error = EventHandler.ERROR_BAD_URL;
            }

            if (!ret || error != 0) {
                return HTTPTASK_INVALID_ID;
            }
        } else {
        	loader.setTransfered(task.mTransfered);
        	loader.setTotal(task.mTotal);
        	if(task.mMuiltDownload && task.mThreadDownloadTaskList.size()>0) {
        		loader.continueMultiDowloader(task);
        	} else {
            	addContentRange(headers, task.mTransfered);
                int error = 0;
                boolean ret = false;
                try {
                    ret = Network.getInstance(mContext).requestURL(method, headers, postData, loader);
                } catch (android.net.ParseException ex) {
                    error = EventHandler.ERROR_BAD_URL;
                } catch (java.lang.RuntimeException ex) {
                    ex.printStackTrace();
                    error = EventHandler.ERROR_BAD_URL;
                }

                if (!ret || error != 0) {
                    return HTTPTASK_INVALID_ID;
                }
        	}
        }
                
        addTask(taskid, loader);
        return taskid;
    }
    
    public int sendRequest(final String url, boolean bGet, byte[] postData,
            HttpTaskListener listener, HashMap<String, String> userHeaders) {
    	return sendRequest(url, null, bGet, postData, listener, 0, userHeaders, null);
    }
    
    public int sendRequest(final String url, final String savePath, boolean bGet, byte[] postData,
            HttpTaskListener listener, long startPos, HashMap<String, String> userHeaders, FileWriterMgr writerMgr) {
        synchronized (this) {
            if (sWorkThread == null) {
                sWorkThread = new HandlerThread("LoadListener WorkThread");
                sWorkThread.start();
                
                // Wait for thread start
                SystemClock.sleep(50);
            }
        }

        String method = null;
        LoadListener loader = null;

        int taskid = newTaskId();
        HashMap<String, String> headers = new HashMap<String, String>();
        populateStaticHeaders(headers);
        
        if (bGet) {
            method = new String("GET");
            appendHeader(headers, userHeaders);

            if (savePath == null) {
                loader = LoadListener.getLoadListener(mContext, url, false, listener, taskid,
                        sWorkThread.getLooper());
            } else {
                addContentRange(headers, startPos);
                loader = LoadFileListener.getFileLoadListener(mContext, url, savePath, false,
                        listener, taskid, taskid, sWorkThread.getLooper(), writerMgr, false);
            }

        } else {
            appendHeader(headers, userHeaders);

            method = new String("POST");
            loader = LoadListener.getLoadListener(mContext, url, false, listener, taskid,
                    sWorkThread.getLooper());
        }

        int error = 0;
        boolean ret = false;

        try {
            ret = Network.getInstance(mContext).requestURL(method, headers, postData, loader);
        } catch (android.net.ParseException ex) {
            error = EventHandler.ERROR_BAD_URL;
        } catch (java.lang.RuntimeException ex) {
            ex.printStackTrace();
            error = EventHandler.ERROR_BAD_URL;
        }

        if (!ret || error != 0) {
            return HTTPTASK_INVALID_ID;
        }

        addTask(taskid, loader);
        return taskid;
    }

    /**
     * 娣诲姞鐢ㄦ埛鎸囧畾鐨勫ご瀛楁
     * 
     * @param headers
     */
    private void appendHeader(HashMap<String, String> origin, HashMap<String, String> append) {
    	if (origin == null || append == null)
    		return;
    	
        for (String key : append.keySet()) {
        	origin.put(key, append.get(key));
        }
    }

    /**
     * 鍔熻兘锛氬彇娑堜竴涓狧TTP璇锋眰
     * 
     * @param taskid
     *            : 浠巗endRequest鑾峰彇鐨則askid
     **/
    synchronized public void cancel(int taskid) {
        Iterator<Entry<LoadListener, Integer>> i = mListenerMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<LoadListener, Integer> entry = i.next();
            LoadListener sender = entry.getKey();
            if (entry.getValue() == taskid) {
//            	Log.d("demo", "cancel taskid = "+taskid);
                sender.cancel();
                break;
            }
        }
    }

    /**
     * 鍔熻兘锛欻TTP璇锋眰瀹屾垚鍚庯紝鑾峰彇HTTP鍝嶅簲鐨勬暟鎹�
     * 
     * @param taskid
     *            : 浠巗endRequest鑾峰彇鐨則askid
     * @return锛氭湇鍔″櫒杩斿洖鐨勬暟鎹�
     **/
    synchronized public ByteArrayBuffer readResponseData(int taskid) {
        Iterator<Entry<LoadListener, Integer>> i = mListenerMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<LoadListener, Integer> entry = i.next();
            LoadListener sender = entry.getKey();
            if (entry.getValue() == taskid) {
                return sender.getDataBuffer();
            }
        }
        return null;
    }

    private void populateStaticHeaders(HashMap<String, String> headers) {
        // Accept header should already be there as they are built by WebCore,
        // but in the case they are missing, add some.
        String accept = headers.get("Accept");
        String ua = headers.get("User-Agent");
        String contentType = headers.get("Content-Type");
        
        if (TextUtils.isEmpty(accept))
            headers.put("Accept", DEFAULT_ACCEPT);
        if (TextUtils.isEmpty(ua))
        	headers.put("User-Agent", DEFAULT_USER_AGENT);
        if (TextUtils.isEmpty(contentType))
        	headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
    }

    private void addContentRange(HashMap<String, String> headers, long startPos) {
        long start = startPos;
        String range = "bytes=" + start + "-";
        headers.put("Range", range);
    }
    
    private void addContentRange(HashMap<String, String> headers, long startPos, long endPos) {
        long start = startPos;
        String range = "bytes=" + start + "-" + endPos;
        headers.put("Range", range);
    }

    private HttpTaskMgr(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
    }

    synchronized public int newTaskId() {
        mTaskid++;
        if (mTaskid < 0) {
            mTaskid = 0;
        }

        return mTaskid;
    }

    synchronized private void addTask(int taskid, LoadListener sender) {
        mListenerMap.put(sender, taskid);
    }
    
    synchronized public LoadListener getTask(int taskid) {
    	LoadListener loadListener = null;
        Iterator<Entry<LoadListener, Integer>> i = mListenerMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<LoadListener, Integer> entry = i.next();
            LoadListener sender = entry.getKey();
            if (entry.getValue() == taskid) {
            	loadListener = sender;
                break;
            }
        }
    	return loadListener;
    }

    /**
     * 鍔熻兘锛氭坊鍔犵綉缁滆繛閫氶�氱煡,鏈嚱鏁伴潪绾跨▼瀹夊叏,鍙兘鍦║I绾跨▼鎵ц
     * 
     * @param listener
     *            : 鐩戝惉鑰�
     **/
    public void addConnectionListener(ConnectionSetupNotify listener) {
        mConSetupListener.add(new SoftReference<ConnectionSetupNotify>(listener));
    }

    /**
     * 鍔熻兘锛氬彇娑堣繛鎺ュ缓绔嬬殑浜嬩欢鐩戝惉
     **/
    public void removeConnectionListener(ConnectionSetupNotify listener) {
        for (Iterator<SoftReference<ConnectionSetupNotify>> it = mConSetupListener.iterator(); it
                .hasNext();) {
            SoftReference<ConnectionSetupNotify> refer = it.next();
            if (refer.get() == null || refer.get() == listener) {
                refer.get().onConnectionSetup();
                it.remove();
            }
        }
    }

    public boolean isConnectionSetup() {
        return mConnectionSetup;
    }

    public void setConnectionSetup() {
        mConnectionSetup = true;
        notifyConnectionSetup();
    }

    private void notifyConnectionSetup() {
        for (Iterator<SoftReference<ConnectionSetupNotify>> it = mConSetupListener.iterator(); it
                .hasNext();) {
            SoftReference<ConnectionSetupNotify> listener = it.next();
            if (listener.get() != null) {
                listener.get().onConnectionSetup();
            } else {
                it.remove();
            }
        }
    }

    private static HandlerThread sWorkThread = null;

}