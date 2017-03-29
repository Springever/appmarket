package com.appmall.market.common;

public class Constants {
	public static final String AUTHORITY = "com.appmall.market";
	public static final String PREF_LOCAL_APP_PARAM = "local_app_param";
	public static final String PREF_DOWNLOAD_TASK = "download_task";
	public static final String PREF_VOTE_APP = "vote_app";
	public static final String PREF_APP_SETTINGS = "app_settings";
	public static final String PREF_IGNORE = "ignore";
	public static final String PREF_APP_STAT = "stat";
	public static final String PREF_DOWN_STAT = "down_stat";

	public static final String ACTION_VIEW_DOWNLOAD_STATUS = "com.appmall.market.ACTION_VIEW_DOWNLOAD_STATUS";
	public static final String ACTION_VIEW_UPDATE = "com.appmall.market.ACTION_VIEW_UPDATE";
	
	public static final String ACTION_EXTRA_COUNT = "count";
	
	public static final String UPREMIND_SECRET = "androidupremindcf";
	public static final String VALUE_BUSI = "client";
	public static final String VALUE_APP_KEY = "sO&J%3Mq@lqYRPtt";
	public static final String SITETYPE = "057";
	public static final String LOG_TYPE_BOOT = "10000";
	public static final String LOG_TYPE_DAILY = "10004";
	public static final Object LOG_TYPE_DOWNLOAD = "10008";
	
	public static final String HOME_PRELOAD_JSON = "preload/home.json";	
	/** 反馈来源 */
	public static final int VALUE_FEEDBACK_SOURCE = 1;
	// 时间常量
	public static final long DAILY_MILLSECONDS = 24 * 3600 * 1000;
	public static final long UPGRADE_MILLSECONDS = 3 * DAILY_MILLSECONDS;
	public static final long BACKGROUND_QUERY_MILLSECONDS = 2 * DAILY_MILLSECONDS;
	public static final long HOUR_MILLSECONDS = 3600 * 1000;
}
