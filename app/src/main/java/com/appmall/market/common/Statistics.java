package com.appmall.market.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.data.IPostData;
import com.appmall.market.download.DownloadTask;


public class Statistics {

	public static final String STAT_A1 = "a1";
	public static final String STAT_A2 = "a2";
	public static final String STAT_B1 = "b1";
	public static final String STAT_B2 = "b2";
	public static final String STAT_B3 = "b3";
	public static final String STAT_B4 = "b4";
	public static final String STAT_B5 = "b5"; 
	public static final String STAT_B6 = "b6";
	public static final String STAT_B7 = "b7";
	public static final String STAT_C1 = "c1";
	public static final String STAT_C2 = "c2";
	public static final String STAT_C3 = "c3";
	public static final String STAT_C4 = "c4";
	public static final String STAT_C5 = "c5";
	public static final String STAT_D1 = "d1";
	public static final String STAT_D2 = "d2";
	public static final String STAT_E1 = "e1";
	public static final String STAT_E2 = "e2";
	public static final String STAT_M1 = "m1";
	public static final String STAT_M2 = "m2";
	public static final String STAT_M3 = "m3";
	public static final String STAT_M4 = "m4";
	private static final String LOGTYPE = "logtype";
	private static final String PACKAGENAME = "packagename";
	private static final String NAME_PKG_ID = "appid";
	private static final String SOURCE = "source";	
	private static final String DOWNLOADS = "downloads";
	private static final String EXCEPTIONS = "exceptions";
	
	private static final String KEY_START = "start";
	private static final String KEY_DOWNLOAD = "download";
	private static final String KEY_SETTING = "setting";
	private static final String KEY_ALERT = "alert";
	private static final String KEY_CRASH = "crash";
	private static final String KEY_MANAGE = "manage";
	
	private static long sStartClientTime = -1;
	private static boolean sPosting;
	
	private static void setStat(Context context, String statKey, String value) {
		if (TextUtils.isEmpty(statKey) || TextUtils.isEmpty(value))
			return;
		
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_STAT, Context.MODE_PRIVATE);
		pref.edit().putString(statKey, value).commit();
	}
	
	private static String getStat(Context context, String statKey) {
		if (TextUtils.isEmpty(statKey))
			return null;
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_STAT, Context.MODE_PRIVATE);
		return pref.getString(statKey, null);
	}
	
	private static int getStatIntegerValue(Context context, String statKey, int defaultValue) {
		String value = getStat(context, statKey);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	private static int getStatBooleanValue(Context context, boolean boolValue) {
		return boolValue ? 1 : 0;
	}
	
	/**
	 * 杩斿洖缁熻淇℃伅
	 */
	public static String buildStatString(Context context, String logType) {
		JSONObject statObj = new JSONObject();
		
		try {
			// 鍚姩鏃堕棿
			JSONObject startObj = new JSONObject();
			startObj.put(LOGTYPE, logType);
			startObj.put(STAT_A1, getStatIntegerValue(context, STAT_A1, 0));
			startObj.put(STAT_A2, getStatIntegerValue(context, STAT_A2, 0));
			statObj.put(KEY_START, startObj);
			
			// 涓嬭浇淇℃伅
			String downloadsValue = getStat(context, DOWNLOADS);
			if (downloadsValue != null) {
				try {
					JSONArray downArray = new JSONArray(downloadsValue);
					statObj.put(KEY_DOWNLOAD, downArray);
				} catch (JSONException e) { }
			}
			
			// 璁剧疆椤�
			JSONObject settingObj = new JSONObject();
			settingObj.put(LOGTYPE, logType);
			settingObj.put(STAT_C1, getStatBooleanValue(context, AppSettings.isAutoInstall(context)));
			settingObj.put(STAT_C2, getStatBooleanValue(context, AppSettings.isInstSuccessRemoveApk(context)));
			settingObj.put(STAT_C3, getStatBooleanValue(context, AppSettings.isRootInstall(context)));
			settingObj.put(STAT_C4, getStatIntegerValue(context, STAT_C4, 0));
			settingObj.put(STAT_C5, getStatIntegerValue(context, STAT_C5, 0));
			statObj.put(KEY_SETTING, settingObj);
			
			// 绠＄悊璁剧疆椤�
			JSONObject manageObj = new JSONObject();
			int m1 = getStatIntegerValue(context, STAT_M1, 0);
			int m2 = getStatIntegerValue(context, STAT_M2, 0);
			int m3 = getStatIntegerValue(context, STAT_M3, 0);
			int m4 = getStatIntegerValue(context, STAT_M4, 0);
			if(m1 + m2 + m3 + m4 >0) {
				manageObj.put(LOGTYPE, logType);
				manageObj.put(STAT_M1, m1);
				manageObj.put(STAT_M2, m2);
				manageObj.put(STAT_M3, m3);
				manageObj.put(STAT_M4, m4);
				statObj.put(KEY_MANAGE, manageObj);
			}
			
			// 閫氱煡鏍忔彁閱�
			JSONObject alertObj = new JSONObject();
			int d1 = getStatIntegerValue(context, STAT_D1, 0);
			int d2 = getStatIntegerValue(context, STAT_D2, 0);
			if (d1 + d2 > 0) {
				alertObj.put(LOGTYPE, logType);
				alertObj.put(STAT_D1, d1);
				alertObj.put(STAT_D2, d2);
				statObj.put(KEY_ALERT, alertObj);
			}
			
			// 宕╂簝淇℃伅
			String exceptionsValue = getStat(context, EXCEPTIONS);
			if (exceptionsValue != null) {
				JSONArray exceptionsArray = new JSONArray(exceptionsValue);
				JSONArray crashArray = new JSONArray();
				for (int index = 0; index < exceptionsArray.length(); ++index) {
					JSONObject exceptionObj = exceptionsArray.optJSONObject(index);
					if (exceptionObj == null)
						continue;
					exceptionObj.put(LOGTYPE, logType);
					crashArray.put(exceptionObj);
				}
				statObj.put(KEY_CRASH, crashArray);
			}
		} catch (JSONException e) { }
		
		return statObj.toString();
	}
	
	public static void addIncUpdateSuccessStat(Context context, String packageName)  {	
		if (TextUtils.isEmpty(packageName))
			return;
		
		Log.d("demo", "addIncUpdateSuccessStat");
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_APP_STAT, Context.MODE_PRIVATE);
		String key = DOWNLOADS;
		String value = pref.getString(key, "");
		JSONArray downloadsArray = null;
		try {
			downloadsArray = new JSONArray(value);
		} catch (JSONException e) {
			downloadsArray = new JSONArray();
		}
		int size = downloadsArray.length();
		int target = -1;
		for (int index = 0; index < size; ++index) {
			JSONObject downObj = downloadsArray.optJSONObject(index);
			if (downObj != null) {
				String pkgValue = downObj.optString(PACKAGENAME);					
				if (packageName.equals(pkgValue)) {
					target = index;
					break;
				}
			}
		}
		
		JSONObject targetObj = null;
		if (target >= 0) {
			targetObj = downloadsArray.optJSONObject(target);
		}
		if (targetObj == null) {
			targetObj = new JSONObject();
			target = -1;
		}
		int incUpdateInstallSucCount = targetObj.optInt(STAT_B7);
		incUpdateInstallSucCount++;
		try {
			targetObj.put(PACKAGENAME, packageName);
			targetObj.put(LOGTYPE, Constants.LOG_TYPE_DOWNLOAD);
			targetObj.put(STAT_B7, incUpdateInstallSucCount);
		} catch (JSONException e) { }
		
		if (target == -1) {
			downloadsArray.put(targetObj);
		} else {
			try {
				downloadsArray.put(target, targetObj);
			} catch (JSONException e) { }
		}
		
		pref.edit().putString(key, downloadsArray.toString()).commit();
	}
	
	private static void addDownStat(Context context, String packageName, int source, int channelId, boolean isSuccess, boolean isIncUpdate) {
		if (TextUtils.isEmpty(packageName))
			return;
		
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_STAT, Context.MODE_PRIVATE);
		
		String key = DOWNLOADS;
		String value = pref.getString(key, "");
		JSONArray downloadsArray = null;
		try {
			downloadsArray = new JSONArray(value);
		} catch (JSONException e) {
			downloadsArray = new JSONArray();
		}

		int size = downloadsArray.length();
		int target = -1;
		for (int index = 0; index < size; ++index) {
			JSONObject downObj = downloadsArray.optJSONObject(index);
			if (downObj != null) {
				String pkgValue = downObj.optString(PACKAGENAME);
				int sourceValue = downObj.optInt(SOURCE);						
				if (packageName.equals(pkgValue) && sourceValue == source) {
					target = index;
					break;
				}
			}
		}
		
		JSONObject targetObj = null;
		if (target >= 0) {
			targetObj = downloadsArray.optJSONObject(target);
		}
		if (targetObj == null) {
			targetObj = new JSONObject();
			target = -1;
		}
		
		int downCount = targetObj.optInt(STAT_B1);
		int successCount = targetObj.optInt(STAT_B2);
		int failedCount = targetObj.optInt(STAT_B3);
		int updateCount = targetObj.optInt(STAT_B4);
		int updateDownSucCount = targetObj.optInt(STAT_B5);
		int incUpdateSucCount = targetObj.optInt(STAT_B6);
		int incUpdateInstallSucCount = targetObj.optInt(STAT_B7);
		if(source == DownloadTask.SOURCE_UPDATE_LIST || source == DownloadTask.SOURCE_UPDATE_OP)
			updateCount++;
		if (isSuccess) {
			successCount ++;
		} else {
			failedCount ++;
		}
		if((source == DownloadTask.SOURCE_UPDATE_LIST || source == DownloadTask.SOURCE_UPDATE_OP) && isSuccess)
			updateDownSucCount++;
		if(isSuccess && isIncUpdate)
			incUpdateSucCount++;
		
		try {
			targetObj.put(PACKAGENAME, packageName);
			targetObj.put(NAME_PKG_ID, channelId);
			targetObj.put(LOGTYPE, Constants.LOG_TYPE_DOWNLOAD);
			if(source == DownloadTask.SOURCE_UPDATE_LIST)
				targetObj.put(SOURCE, DownloadTask.SOURCE_UPDATE_LIST);
			else
				targetObj.put(SOURCE, DownloadTask.SOURCE_APPMARKET);
			targetObj.put(STAT_B1, downCount + 1);
			targetObj.put(STAT_B2, successCount);
			targetObj.put(STAT_B3, failedCount);
			targetObj.put(STAT_B4, updateCount);
			targetObj.put(STAT_B5, updateDownSucCount);
			targetObj.put(STAT_B6, incUpdateSucCount);
			targetObj.put(STAT_B7, incUpdateInstallSucCount);
		} catch (JSONException e) { }
		
		if (target == -1) {
			downloadsArray.put(targetObj);
		} else {
			try {
				downloadsArray.put(target, targetObj);
			} catch (JSONException e) { }
		}
		
		pref.edit().putString(key, downloadsArray.toString()).commit();
	}

	/**
	 * 娓呯┖鎵�鏈夌粺璁′俊鎭�
	 */
	public static void clearStats(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_STAT, Context.MODE_PRIVATE);
		pref.edit().clear().commit();
	}
	
	/**
	 * 鍚姩娆℃暟 +1
	 */
	public static void addBootCount(Context context) {
		int count = getStatIntegerValue(context, STAT_A1, 0);
		setStat(context, STAT_A1, String.valueOf(count + 1));
	}


	public static void onClientStart(Activity activity) {
		sStartClientTime = System.currentTimeMillis();
	}


	public static void onClientStop(Activity activity) {
		long endTime = System.currentTimeMillis();
		if (sStartClientTime == -1 || sStartClientTime >= endTime) {
			sStartClientTime = -1;
			return;
		}
		
		long time = getStatIntegerValue(activity, STAT_A2, 0);
		setStat(activity, STAT_A2, String.valueOf(time + (endTime - sStartClientTime) / 1000));
		sStartClientTime = -1;
	}
	

	public static void addDownSuccessCount(Context context, String packageName, int source, int pkgid, boolean isIncUpdate) {
		if (TextUtils.isEmpty(packageName))
			return;
		
		addDownStat(context, packageName, source,pkgid, true, isIncUpdate);
	}
	

	public static void addDownFailedCount(Context context, String packageName, int source, int pkgid, boolean isIncUpdate) {
		if (TextUtils.isEmpty(packageName))
			return;
		
		addDownStat(context, packageName, source,pkgid, false, isIncUpdate);
	}
	

	public static void addRemoveApkCount(Context context) {
		int count = getStatIntegerValue(context, STAT_C4, 0);
		setStat(context, STAT_C4, String.valueOf(count + 1));
	}
	

	public static void addClearCacheCount(Context context) {
		int count = getStatIntegerValue(context, STAT_C5, 0);
		setStat(context, STAT_C5, String.valueOf(count + 1));
	}
	

	public static void addUpremindNotificationCount(Context context) {
		int count = getStatIntegerValue(context, STAT_D1, 0);
		setStat(context, STAT_D1, String.valueOf(count + 1));
	}
	

	public static void addClickUpremindNotificationCount(Context context) {
		int count = getStatIntegerValue(context, STAT_D2, 0);
		setStat(context, STAT_D2, String.valueOf(count + 1));
	}
	

	public static void addManageTab1ClickCount(Context context) {
		int count = getStatIntegerValue(context, STAT_M1, 0);
		setStat(context, STAT_M1, String.valueOf(count + 1));
	}
	

	public static void addManageTab2ClickCount(Context context) {
		int count = getStatIntegerValue(context, STAT_M2, 0);
		setStat(context, STAT_M2, String.valueOf(count + 1));
	}
	

	public static void addManageTab3ClickCount(Context context) {
		int count = getStatIntegerValue(context, STAT_M3, 0);
		setStat(context, STAT_M3, String.valueOf(count + 1));
	}
	

	public static void addManageTab4ClickCount(Context context) {
		int count = getStatIntegerValue(context, STAT_M4, 0);
		setStat(context, STAT_M4, String.valueOf(count + 1));
	}
	
	
	public static void addException(Context context, String exceptionMsg) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_STAT, Context.MODE_PRIVATE);
		String key = EXCEPTIONS;
		String value = pref.getString(key, "");
		JSONArray exceptionArray = null;
		try {
			exceptionArray = new JSONArray(value);
		} catch (JSONException e) {
			exceptionArray = new JSONArray();
		}
		
		try {
			JSONObject exceptionObj = new JSONObject();
			exceptionObj.put(STAT_E1, exceptionMsg);
			exceptionObj.put(STAT_E2, System.currentTimeMillis());
			exceptionArray.put(exceptionObj);
			pref.edit().putString(key, exceptionArray.toString()).commit();
		} catch (JSONException e) {}
	}

	public static void bootPost(Context context, IDataCallback callback) {
		post(context, callback, Constants.LOG_TYPE_BOOT);
	}
	
	public static boolean dailyPost(Context context, IDataCallback callback) {
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getDailyPostStatTime(context);
		
		if (currTime < lastTime || currTime - lastTime > Constants.DAILY_MILLSECONDS) {
			post(context, callback, Constants.LOG_TYPE_DAILY);
			AppSettings.setDailyPostStatTime(context, System.currentTimeMillis());
			setNextDailyPostAlarm(context);
			return true;
		} else {
			return false;
		}
	}
	
	private static synchronized void post(Context context, IDataCallback callback, String logType) {
		if (sPosting)
			return;
		
		int dataId = IDataConstant.STATISTICS;
		Options options = new Options();
		String statData = buildStatString(context, logType);
		options.mPostData = new PostData(statData);
		DataCenter.getInstance().requestDataAsync(context, dataId, callback, options);
		sPosting = true;
	}
	
	public static synchronized void onPostCompleted() {
		sPosting = false;
	}
	
	public static void setNextDailyPostAlarm(Context context) {
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getDailyPostStatTime(context);
		long nextTime = lastTime + Constants.DAILY_MILLSECONDS;
		if (currTime > nextTime) {
			// 杈惧埌浜嗗悗鍙板彂閫佹椂闂达紝绔嬪嵆鍙戦�佷竴娆�
			nextTime = 0;
		}

		Intent intent = new Intent(context, AppService.class);
		intent.setAction(AppService.ACTION_DAILY_POST_STAT);
		PendingIntent operation = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, nextTime, operation);
	}
	
	private static class PostData implements IPostData {
		private String mData;
		public PostData(String data) {
			mData = data;
		}
		@Override
		public byte[] buildPostData() {
			if (TextUtils.isEmpty(mData))
				return null;
			String encryptString = Base64.encodeToString(Encoder.encode(mData.getBytes()), Base64.NO_WRAP);
			return encryptString.getBytes();
		}
	}

}
