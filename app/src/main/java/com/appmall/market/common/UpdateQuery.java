package com.appmall.market.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appmall.market.bean.AppUpdate;
import com.appmall.market.bean.UpdateInfo;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.data.LocalApps;
import com.appmall.market.data.QueryUpdatePost;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UpdateQuery {

	public static boolean startDailyQuery(Context context, IDataCallback callback) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		String lastDay = fmt.format(new Date(AppSettings.getUpdateQueryTime(context)));
		String currDay = fmt.format(new Date());
		
		boolean diffDay = !lastDay.equals(currDay);
		if (diffDay) {
			startQuery(context, callback, false);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean startBackgroundQuery(Context context, IDataCallback callback) {
		long checkInterval = Constants.BACKGROUND_QUERY_MILLSECONDS;
		if(UpdateInfo.gUpdateIntervalHours >0)
			checkInterval = UpdateInfo.gUpdateIntervalHours*Constants.HOUR_MILLSECONDS;
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getBackgroundUpdateQueryTime(context);
		
		if (currTime < lastTime || currTime - lastTime > checkInterval) {
			startQuery(context, callback, false);
			AppSettings.setBackgroundUpdateQueryTime(context, System.currentTimeMillis());
			setBackgroundQueryAlarm(context);
			return true;
		} else {
			return false;
		}
	}


	public static void setBackgroundQueryAlarm(Context context) {
		long checkInterval = Constants.BACKGROUND_QUERY_MILLSECONDS;
		if(UpdateInfo.gUpdateIntervalHours >0)
			checkInterval = UpdateInfo.gUpdateIntervalHours*Constants.HOUR_MILLSECONDS;
		
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getBackgroundUpdateQueryTime(context);
		long nextTime = lastTime + checkInterval;
		if (currTime > nextTime) {
			nextTime = 0;
		}

		Intent intent = new Intent(context, AppService.class);
		intent.setAction(AppService.ACTION_WEEKLY_QUERY_UPDATE);
		PendingIntent operation = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, nextTime, operation);
	}
	

	public static void startQuery(Context context, IDataCallback callback, boolean allowCache) {
		Log.d("demo", "startQuery ");
		int dataId = IDataConstant.UPDATE_INFO;
		Options options = new Options();
		options.mIsUpremindData = true;
		options.mPostData = new QueryUpdatePost(context);
		options.mTryCache = allowCache;
		DataCenter.getInstance().requestDataAsync(context, dataId, callback, options);
		AppSettings.setUpdateQueryTime(context, System.currentTimeMillis());
	}

	public static void onQuerySuccessful(Context context) {
		AppSettings.setUpdateCacheTime(context, System.currentTimeMillis());
	}
	

	public static int parseUpdateCount(Context context, UpdateInfo info) {
		List<AppUpdate> update = new ArrayList<AppUpdate>();
		if (info.mUpdates != null) {
			DataCenter dc = DataCenter.getInstance();
			Set<String> ignoreSet = UpdateIgnore.getIgnorePackageSet(context);
			for (AppUpdate au : info.mUpdates) {
				int status = dc.getPackageInstallStatus(au.mPackageName, au.mVersionCode, au.mVersion);
				if (status != LocalApps.STATUS_INSTALLED_OLD_VERSION)
					continue;
				if (!ignoreSet.contains(au.mPackageName)) {
					update.add(au);
				}
			}
		}
		
		return update.size();
	}
	
	public static int parseWeeklyUpdate(Context context, UpdateInfo info, List<String> outLabels) {
		if (outLabels != null) {
			outLabels.clear();
		}
		
		List<AppUpdate> update = new ArrayList<AppUpdate>();
		if (info.mUpdates != null) {
			DataCenter dc = DataCenter.getInstance();
			Set<String> ignoreSet = UpdateIgnore.getIgnorePackageSet(context);
			for (AppUpdate au : info.mUpdates) {
				int status = dc.getPackageInstallStatus(au.mPackageName, au.mVersionCode, au.mVersion);
				if (status != LocalApps.STATUS_INSTALLED_OLD_VERSION)
					continue;
				if (!ignoreSet.contains(au.mPackageName)) {
					if (outLabels != null) {
						outLabels.add(au.mPackageName);
					}
					
					update.add(au);
				}
			}
		}
		
		return update.size();
	}
}
