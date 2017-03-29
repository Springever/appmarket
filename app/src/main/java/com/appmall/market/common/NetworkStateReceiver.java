package com.appmall.market.common;

import java.util.List;

import com.appmall.market.R;
import com.appmall.market.data.DataCenter;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.widget.CommonDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.net.NetworkInfo.State;

public class NetworkStateReceiver extends BroadcastReceiver {
      
	public static boolean gReportWifi2MobileFlag = false;
	
    @Override  
    public void onReceive(Context context, Intent intent) {	
    	State wifiState = null;  
        State mobileState = null;  
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();  
        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        Log.e("demo", "wifiState = "+wifiState);
        Log.e("demo", "mobileState = "+mobileState);
        boolean bConnect = false;
        if (wifiState != null && mobileState != null  
                && State.CONNECTED != wifiState  
                && State.CONNECTED == mobileState) {  
        	Log.d("demo", "mobile ok");
        	bConnect = true;
        	if(mRunningDownloadTaskList != null && mRunningDownloadTaskList.size() >0 && !gReportWifi2MobileFlag) {
        		gReportWifi2MobileFlag = true;
        		Log.d("demo", "report wifi_to_mobile");
        		DataCenter.getInstance().reportDownloadEvent(DataCenter.MSG_WIFI_TO_MOBILE_CHANGED_EVENT, null);
        	} 		
        } else if (wifiState != null && mobileState != null  
                && State.CONNECTED != wifiState  
                && State.CONNECTED != mobileState) {  
        	NetworkStateReceiver.captureRunningTaskList(context);
        	Log.d("demo", "all disconnect");
        } else if (wifiState != null && State.CONNECTED == wifiState) {  
        	Log.d("demo", "wifi ok");
        	bConnect = true;
        	if(mRunningDownloadTaskList != null && mRunningDownloadTaskList.size() >0) {
        		if(gDialog != null && gDialog.isShowing()) {
        			gReportWifi2MobileFlag = false;
        			gDialog.dismiss();
        		}		
        		resumeNetworkTaskList(context);
        	} 	
        }
        if(bConnect) {
			DataCenter dc = DataCenter.getInstance();
			dc.ensureInit(context);
			dc.reportNetStateChanged();
        }
    }
    
    public static void captureRunningTaskList(Context context) {
    	try {
        	if(mRunningDownloadTaskList == null) { 	
        		int runningCount = DataCenter.getInstance().getTaskList().getRunningAndWaitingTaskCount();
        		if (runningCount > 0) {
        			mRunningDownloadTaskList = DataCenter.getInstance().getTaskList().getRunningTaskList();
    				Intent service = new Intent(context, DownloadService.class);
    				service.setAction(DownloadService.ACTION_STOP_ALL_TASK);
    				context.startService(service);
        		}
        	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static boolean hasCapture() {
    	return (mRunningDownloadTaskList!=null);
    }
    
    private static List<DownloadTask> mRunningDownloadTaskList;
    public static void resumeNetworkTaskList(Context context) {
    	List<DownloadTask> taskList = DataCenter.getInstance().getTaskList().getTaskList();
    	if(taskList == null) return;
		for(DownloadTask task : mRunningDownloadTaskList) {
			if(taskList.contains(task))
			{
				Intent service = new Intent(context, DownloadService.class);
				service.setAction(DownloadService.ACTION_RESUME_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				context.startService(service);
			}
		}
		mRunningDownloadTaskList = null;
    }
      
    private static CommonDialog gDialog = null;
	public static void showNetworkChangeQueryDialog(final Context context) {
		if(mRunningDownloadTaskList != null) 
		{
			String message = "褰撳墠澶勪簬2G/3G缃戠粶锛岃蒋浠朵笅杞借嚜鍔ㄦ殏鍋滐紝鏄惁缁х画涓嬭浇锛�";
			String title = "涓嬭浇宸叉殏鍋�";
			
			final CommonDialog queryDialog = new CommonDialog(context);
			gDialog = queryDialog;
			queryDialog.setMessage(message);
			queryDialog.setTitle(title);
			queryDialog.setCheckBoxVisible(false);
			queryDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					gReportWifi2MobileFlag = false;
					mRunningDownloadTaskList = null;
				}
			});
			queryDialog.setPositiveButton(R.string.dialog_continue, false, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					gReportWifi2MobileFlag = false;
					resumeNetworkTaskList(context);
				}
			});
			queryDialog.show();
		}
	}
  
}
