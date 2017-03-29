package com.appmall.market.common;

import com.appmall.market.activity.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
  
public class NotifycationReceive extends BroadcastReceiver {
	public static final String NOTIFY_TYPE = "type";
	public static final String NOTIFY_ACTION = "action";
	public static final String NOTIFY_PKGINFO = "pkginfo";
	
	public static final String NOTIFY_UPGRADE_VERSIONCODE = "versioncode";
	public static final String NOTIFY_UPGRADE_VERSIONNAME = "versionname";
	public static final String NOTIFY_UPGRADE_VERSIONSIZE = "versionsize";
	public static final String NOTIFY_UPGRADE_VERSIONDESC = "versiondesc";
	public static final String NOTIFY_UPGRADE_VERSIONURL = "versionurl";
	
    @Override  
    public void onReceive(Context context, Intent intent) {
    	if(intent != null) {
    		int action = intent.getIntExtra(NOTIFY_ACTION, -1);
    		if(action != -1) {		
    			String pkgInfo = intent.getStringExtra(NOTIFY_PKGINFO);
    			Log.e("demo", "onReceive action = "+action);
    			Log.e("demo", "onReceive pkgInfo = "+pkgInfo);
    			NotificationMgr.cancelNotification(action);
    			if(action == NotificationMgr.NOTIFICATION_ACTION_RETURNTODOWNLOADPAGE || action == NotificationMgr.NOTIFICATION_ACTION_RETURNTOUPDATEPAGE) {
    				Intent newIntent = new Intent(context, MainActivity.class);
    				newIntent.putExtra(NOTIFY_ACTION, action);
    				newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
    				context.startActivity(newIntent);
    			} else if(action == NotificationMgr.NOTIFICATION_ACTION_INSTALLPACKAGE) {
    				Utils.reqSystemInstall(context, pkgInfo);
    			} else if(action == NotificationMgr.NOTIFICATION_ACTION_OPENPACKAGE) {
    				Utils.reqSystemOpen(context, pkgInfo);
    			} else if(action == NotificationMgr.NOTIFICATION_ACTION_UPGRADE) {
    				int versionCode = intent.getIntExtra(NOTIFY_UPGRADE_VERSIONCODE, 100);
    				String versionName = intent.getStringExtra(NOTIFY_UPGRADE_VERSIONNAME);
    				String versionSize = intent.getStringExtra(NOTIFY_UPGRADE_VERSIONSIZE);
    				String versionDesc = intent.getStringExtra(NOTIFY_UPGRADE_VERSIONDESC);
    				String versionUrl = intent.getStringExtra(NOTIFY_UPGRADE_VERSIONURL);
    				Intent newIntent = new Intent(context, MainActivity.class);
    				newIntent.putExtra(NOTIFY_ACTION, action);
    				newIntent.putExtra(NOTIFY_UPGRADE_VERSIONCODE, versionCode);
    				newIntent.putExtra(NOTIFY_UPGRADE_VERSIONNAME, versionName);
    				newIntent.putExtra(NOTIFY_UPGRADE_VERSIONSIZE, versionSize);
    				newIntent.putExtra(NOTIFY_UPGRADE_VERSIONDESC, versionDesc);
    				newIntent.putExtra(NOTIFY_UPGRADE_VERSIONURL, versionUrl);
    				newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
    				context.startActivity(newIntent);
    			}
    		}
    	}
    }  
} 