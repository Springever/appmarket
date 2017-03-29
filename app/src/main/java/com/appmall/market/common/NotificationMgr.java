package com.appmall.market.common;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.content.pm.PackageManager;

import com.appmall.market.R;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.appmall.market.bean.CheckForUpdate;


public class NotificationMgr {
	private static NotificationManager nm;
	private static String NOTIFICATION_ACTION = "com.appmall.market.action.notification";
	
	public static int NOTIFICATION_TYPE_TEXT = 1;
	public static int NOTIFICATION_TYPE_ICON = 2;
	
	
	public static int NOTIFICATION_ACTION_RETURNTODOWNLOADPAGE = 1;
	public static int NOTIFICATION_ACTION_RETURNTOUPDATEPAGE = 2;
	public static int NOTIFICATION_ACTION_INSTALLPACKAGE = 3;
	public static int NOTIFICATION_ACTION_OPENPACKAGE = 4;
	public static int NOTIFICATION_ACTION_UPGRADE = 5;
	
	public static void showTextNotification(Context context, List<String> labels, int action) {
		if (labels == null || labels.size() <= 0)
		return;
	
		int count = labels.size();
		String spilit = context.getString(R.string.notification_labels_spilit);
		String title = String.format(Locale.getDefault(), context.getString(R.string.app_is_downloading), count);
		String text = buildLabelsText(labels, spilit);
		showTextNotification(context, NOTIFICATION_TYPE_TEXT, title, title, text, action, "");
	}
	
	public static void showTextNotification(Context context, String title, String content, int action, String pkgInfo) {
		showTextNotification(context, NOTIFICATION_TYPE_TEXT, title, title, content, action, pkgInfo);
	}
	
	public static void showIconNotification(Context context, String title,List<String> pkgList, String titleInfo,int action) {
		showIconNotification(context, NOTIFICATION_TYPE_ICON, title, title, titleInfo, pkgList, action);
	}
	
	public static void showUpgradeNotification(Context context, String title, String content, CheckForUpdate checkUpdate) {
		if(context != null) {			
			String msg = content.replace("\n", " ");
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.notifycation_small_icon)
					.setContentTitle(title)
					.setContentText(msg);
			mBuilder.setTicker(title);//第一次提示消息的时候显示在通知栏上
			Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher);
			mBuilder.setLargeIcon(largeIcon);
			mBuilder.setAutoCancel(true);//自己维护通知的消失
			mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
			
			int action = NOTIFICATION_ACTION_UPGRADE;
			Intent notifyIntent = new Intent();
			notifyIntent.setAction(NOTIFICATION_ACTION);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_TYPE, NOTIFICATION_TYPE_TEXT);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_ACTION, action);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONCODE, checkUpdate.mVersionCode);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONNAME, checkUpdate.mVersionName);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONSIZE, Utils.getSizeString(checkUpdate.mSize));
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONDESC, checkUpdate.mChangeLog);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONURL, checkUpdate.mFileUrl);
			
			//封装一个Intent
			PendingIntent pt = PendingIntent.getBroadcast(context, action, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			// 设置通知主题的意图
			mBuilder.setContentIntent(pt);
			//获取通知管理器对象
			if(nm == null)
				nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(action);
			nm.notify(action, mBuilder.build()); 
		}
	}
	
	private static void showTextNotification(Context context, int type, String tickertext, String title, String content, int action, String pkgInfo) {
		if(context != null)
		{			
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.notifycation_small_icon)
					.setContentTitle(title)
					.setContentText(content);
			mBuilder.setTicker(title);//第一次提示消息的时候显示在通知栏上
			Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher);
			mBuilder.setLargeIcon(largeIcon);
			mBuilder.setAutoCancel(true);//自己维护通知的消失
			mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
			
			Intent notifyIntent = new Intent();
			notifyIntent.setAction(NOTIFICATION_ACTION); 			 
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_TYPE, type);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_ACTION, action);
			notifyIntent.putExtra(NotifycationReceive.NOTIFY_PKGINFO, pkgInfo);
			
			//封装一个Intent
			PendingIntent pt = PendingIntent.getBroadcast(context, action, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			// 设置通知主题的意图
			mBuilder.setContentIntent(pt);
			//获取通知管理器对象
			if(nm == null)
				nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(action);
			nm.notify(action, mBuilder.build()); 
		 }
	}
    
	@SuppressWarnings("deprecation")
    private static String getStrTime(long when) {
    	String strTime = "";
		 Date curDate = new Date(when);//获取当前时间 
		 int hour = curDate.getHours();
		 int min = curDate.getMinutes();
		 String zero = "";
		 if(min <=9)
			 zero = "0";
		 strTime = hour + ":" +zero+ min;
		 return strTime;
    }
	
	private static void showIconNotification(Context context, int type, String tickertext, String title,String titleInfo,List<String> pkgList, int action) {
		if(context != null)
		{			
			 Statistics.addUpremindNotificationCount(context);
			 CharSequence tickerText = tickertext; // 状态栏显示的通知文本提示   
			 if(TextUtils.isEmpty(titleInfo))
				 tickerText = title;
			 else
				 tickerText = title+titleInfo;
			 long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示  
			 NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
			 .setSmallIcon(R.drawable.notifycation_small_icon)
			 .setTicker(tickerText); 
			 RemoteViews contentView = new RemoteViews(context.getPackageName(),  R.layout.notification_icon_layout);
			 
			 {
				 PackageManager pManager = context.getPackageManager();
				 int appCount = pkgList.size();
				 try {
					 
					 if(appCount >=1) {
						 Drawable appDrawable = pManager.getApplicationIcon(pkgList.get(0));				 
						 if(appDrawable != null) {
							 Bitmap iconBitmap = Utils.drawableToBitmap(appDrawable);  
							 contentView.setImageViewBitmap(R.id.image_icon1, iconBitmap);
							 contentView.setViewVisibility(R.id.image_icon1, View.VISIBLE);
						 }
					 }
					 
					 if(appCount >=2) {
						 Drawable appDrawable = pManager.getApplicationIcon(pkgList.get(1));				 
						 if(appDrawable != null) {
							 Bitmap iconBitmap = Utils.drawableToBitmap(appDrawable);  
							 contentView.setImageViewBitmap(R.id.image_icon2, iconBitmap);
							 contentView.setViewVisibility(R.id.image_icon2, View.VISIBLE);
						 }
					 }
					 
					 if(appCount >=3) {
						 Drawable appDrawable = pManager.getApplicationIcon(pkgList.get(2));				 
						 if(appDrawable != null) {
							 Bitmap iconBitmap = Utils.drawableToBitmap(appDrawable);  
							 contentView.setImageViewBitmap(R.id.image_icon3, iconBitmap);
							 contentView.setViewVisibility(R.id.image_icon3, View.VISIBLE);
						 }
					 }
					 
					 if(appCount >=4) {
						 Drawable appDrawable = pManager.getApplicationIcon(pkgList.get(3));				 
						 if(appDrawable != null) {
							 Bitmap iconBitmap = Utils.drawableToBitmap(appDrawable);  
							 contentView.setImageViewBitmap(R.id.image_icon4, iconBitmap);
							 contentView.setViewVisibility(R.id.image_icon4, View.VISIBLE);
						 }
					 }				 
					 contentView.setViewVisibility(R.id.text_icon_content, appCount >4 ?View.VISIBLE: View.GONE);
					 
				 } catch(Exception e) {
		            e.printStackTrace();
		         }
			 }
			 				 
			 contentView.setTextViewText(R.id.title_notification, title);
			 contentView.setTextViewText(R.id.time_notification, getStrTime(when));
			 contentView.setTextViewText(R.id.title_notification_info, titleInfo);
			 mBuilder.setContent(contentView);
			 mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
			 mBuilder.setAutoCancel(true);
			 Intent notifyIntent = new Intent();
			 notifyIntent.setAction(NOTIFICATION_ACTION); 			 
			 notifyIntent.putExtra(NotifycationReceive.NOTIFY_TYPE, type);
			 notifyIntent.putExtra(NotifycationReceive.NOTIFY_ACTION, action);
			 PendingIntent pt = PendingIntent.getBroadcast(context, action, notifyIntent, PendingIntent.FLAG_ONE_SHOT);
			 mBuilder.setContentIntent(pt);
			 if(nm == null)
				 nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			 nm.cancel(action);
			 nm.notify(action, mBuilder.build()); 
		 }
	}
	
	private static String buildLabelsText(List<String> labels, String spilit) {
		if (labels == null || labels.size() == 0)
			return "";
		
		StringBuilder builder = new StringBuilder();
		for (String label : labels) {
			if (TextUtils.isEmpty(label))
				continue;
			builder.append(label + spilit);
		}
		
		if (builder.length() == 0)
			return "";

		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
	
	public static void cancelNotification(int type) {
		if(nm != null)
			nm.cancel(type);
	}
	
	public static void cancelAllNotification() {
		if(nm != null)
			nm.cancelAll();
	}

}
