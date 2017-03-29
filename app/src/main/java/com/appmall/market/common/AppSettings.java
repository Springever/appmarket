package com.appmall.market.common;

import com.appmall.market.common.Constants;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 软件设置项
 *  
 *
 */
public class AppSettings {

	
	
	private static final String AUTO_INSTALL = "auto_install";
	private static final String ROOT_INSTALL = "root_install";
	private static final String INST_SUCCESS_REMOVE_APK = "inst_success_remove_apk";
	private static final String DELETE_QUERY = "delete_query";
	private static final String DELETE_ALL_QUERY = "delete_all_query";
	private static final String ROOT_QUERY = "root_query";
	private static final String UPDATE_QUERY_TIME = "update_query_time";
	private static final String UPDATE_COUNT = "update_count";
	private static final String BACKGROUND_UPDATE_QUERY_TIME = "background_update_query_time";
	private static final String CLIENT_UPDATE_QUERY_TIME = "client_update_query_time";
	private static final String DAILY_POST_STAT_TIME = "daily_post_stat_time";
	private static final String UPDATE_CACHE_TIME = "update_cache_time";
	private static final String UPDATE_VERSIONNAME = "update_versionname";
	private static final String CLIENT_WEEKLY_UPDATE_QUERY_TIME = "client_weekly_update_query_time";
	private static final String IS_FIRST_ENTER = "is_first_enter";
	private static final String CHECK_UPDATE_TIME_INTERVAL="check_update_time_interval";
	
	
	public static long getCheckUpdateTimeInterval(Context context) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(CHECK_UPDATE_TIME_INTERVAL, 0);
	}
	
	public static void setCheckUpdateTimeInterval(Context context, long checkTime) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(CHECK_UPDATE_TIME_INTERVAL, checkTime).commit();
	}
	
	public static boolean isFirstEnter(Context context) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(IS_FIRST_ENTER, true);
	}
	
	public static void setFirstEnter(Context context, boolean bFirst) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(IS_FIRST_ENTER, bFirst).commit();
	}
	
	public static void setBackgroundClientUpdateQueryTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(CLIENT_WEEKLY_UPDATE_QUERY_TIME, time).commit();
	}
	
	public static long getBackgroundClientUpdateQueryTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(CLIENT_WEEKLY_UPDATE_QUERY_TIME, 0);
	}
	
	public static void setUpdateVersionName(Context context, String versionName) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putString(UPDATE_VERSIONNAME, versionName).commit();
	}
	
	public static String getUpdateVersionName(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getString(UPDATE_VERSIONNAME, "");
	}
	
	public static void setAutoInstall(Context context, boolean b) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(AUTO_INSTALL, b).commit();
	}
	
	public static boolean isAutoInstall(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(AUTO_INSTALL, true);
	}
	
	public static void setRootInstall(Context context, boolean b) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(ROOT_INSTALL, b).commit();
	}
	
	public static boolean isRootInstall(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(ROOT_INSTALL, false);
	}

	public static void setInstSuccessRemoveApk(Context context, boolean b) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(INST_SUCCESS_REMOVE_APK, b).commit();
	}
	
	public static boolean isInstSuccessRemoveApk(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(INST_SUCCESS_REMOVE_APK, true);
	}

	public static void setDeleteQuery(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(DELETE_QUERY, false).commit();
	}
	
	public static boolean isDeleteQuery(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(DELETE_QUERY, true);
	}
	
	public static void setDeleteALLQuery(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(DELETE_ALL_QUERY, false).commit();
	}
	
	public static boolean isDeleteALLQuery(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(DELETE_ALL_QUERY, true);
	}
	
	public static void setRootQuery(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putBoolean(ROOT_QUERY, false).commit();
	}
	
	public static boolean isRootQuery(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getBoolean(ROOT_QUERY, true);
	}
	
	public static void setUpdateQueryTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(UPDATE_QUERY_TIME, time).commit();
	}
	
	public static long getUpdateQueryTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(UPDATE_QUERY_TIME, 0);
	}
	
	public static void setUpdateCacheTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(UPDATE_CACHE_TIME, time).commit();
	}
	
	public static long getUpdateCacheTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(UPDATE_CACHE_TIME, 0);
	}
	
	public static void setBackgroundUpdateQueryTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(BACKGROUND_UPDATE_QUERY_TIME, time).commit();
	}
	
	public static long getBackgroundUpdateQueryTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(BACKGROUND_UPDATE_QUERY_TIME, 0);
	}
	
	public static void setUpdateCount(Context context, int count) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putInt(UPDATE_COUNT, count).commit();
	}
	
	public static int getUpdateCount(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getInt(UPDATE_COUNT, 0);
	}

	public static void setClientUpdateQueryTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(CLIENT_UPDATE_QUERY_TIME, time).commit();
	}
	
	public static long getClientUpdateQueryTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(CLIENT_UPDATE_QUERY_TIME, 0);
	}
	
	public static void setDailyPostStatTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		pref.edit().putLong(DAILY_POST_STAT_TIME, time).commit();
	}
	
	public static long getDailyPostStatTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Constants.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		return pref.getLong(DAILY_POST_STAT_TIME, -1);
	}
	
}
