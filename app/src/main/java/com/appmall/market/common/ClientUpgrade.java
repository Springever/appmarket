package com.appmall.market.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.appmall.market.R;
import com.appmall.market.bean.CheckForUpdate;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;

/**
 * 瀹㈡埛绔崌绾х浉鍏抽�昏緫
 */
public class ClientUpgrade {

	/**
	 * 杩涜姣忔棩瀹㈡埛绔嚜鍔ㄦ洿鏂版煡璇�<br>
	 * 闇�瑕佹弧瓒崇殑鏉′欢<br>
	 * 1. 24涓皬鏃跺唴灏氭湭鏌ヨ杩�<br>
	 * 2. 鐢ㄦ埛閫夋嫨鈥滀笅娆″啀璇粹�濆悗锛岃秴杩�1鍛�
	 * 3. 鐢ㄦ埛寮�鍚簡寮�鍏�
	 * 
	 * @param context
	 * @param callback
	 * @return
	 */
	public static boolean startDailyQuery(Context context, IDataCallback callback) {
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getClientUpdateQueryTime(context);
		
		boolean satisfiedDailyUpdate = currTime < lastTime || currTime - lastTime > Constants.DAILY_MILLSECONDS;

		if (satisfiedDailyUpdate) {
			startQuery(context, callback, false);
			AppSettings.setClientUpdateQueryTime(context, System.currentTimeMillis());
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 浠庢湰鍦扮紦瀛樹腑鍙栧緱瀹㈡埛绔崌绾ф暟鎹�
	 */
	public static void queryCache(Context context, IDataCallback callback) {
		startQuery(context, callback, true);
	}
	
	/**
	 * 鍚庡彴姣忓懆杩涜瀹㈡埛绔崌绾�
	 */
	public static boolean startWeeklyQuery(Context context, IDataCallback callback) {
//		Log.d("demo", "startWeeklyQuery");
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getBackgroundClientUpdateQueryTime(context);
//    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy骞�-MM鏈坉d鏃�-HH鏃秏m鍒唖s绉�");
//		Log.d("demo", "setNextUpdateAlarm currTime = "+formatter.format(new Date(currTime)));
//		Log.d("demo", "setNextUpdateAlarm lastTime = "+formatter.format(new Date(lastTime)));
		
		if (currTime < lastTime || currTime - lastTime > Constants.UPGRADE_MILLSECONDS) {
//			Log.d("demo", "start weekly query start");
			startQuery(context, callback, false);
			AppSettings.setBackgroundClientUpdateQueryTime(context, System.currentTimeMillis());
			setNextUpdateAlarm(context);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 姣忓懆闇�瑕佽繘琛屽鎴风鏇存柊锛屽湪搴旂敤鍚姩鏃惰繘琛屽畾鏃跺櫒鐨勫垵濮嬪寲
	 */
	public static void setNextUpdateAlarm(Context context) {
//		Log.d("demo", "setNextUpdateAlarm");
		long currTime = System.currentTimeMillis();
		long lastTime = AppSettings.getBackgroundClientUpdateQueryTime(context);
		long nextTime = lastTime + Constants.UPGRADE_MILLSECONDS;
		if (currTime > nextTime) {
			// 杈惧埌浜嗗悗鍙版洿鏂版椂闂达紝绔嬪嵆杩涜涓�娆″悗鍙版洿鏂�
			nextTime = 0;
		}
//    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy骞�-MM鏈坉d鏃�-HH鏃秏m鍒唖s绉�");
//		Log.d("demo", "setNextUpdateAlarm lastTime = "+formatter.format(new Date(lastTime)));
//		Log.d("demo", "setNextUpdateAlarm nextTime = "+formatter.format(new Date(nextTime)));

		Intent intent = new Intent(context, AppService.class);
		intent.setAction(AppService.ACTION_WEEKLY_CLIENT_QUERY_UPDATE);
		PendingIntent operation = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, nextTime, operation);
	}

	public static void startQuery(Context context, IDataCallback callback, boolean useCache) {
		int dataId = IDataConstant.CHECK_UPDATE;
		Options options = null;
		
		if (useCache) {
			options = new Options();
			options.mTryCache = true;
		}
		
		DataCenter.getInstance().requestDataAsync(context, dataId, callback, options);
	}

	public static void onBackgroundQueryCompleted(Context context, Response resp) {
		if (resp == null || !resp.mSuccess)
			return;
		if (resp.mData == null || !(resp.mData instanceof CheckForUpdate))
			return;

		CheckForUpdate checkUpdate = (CheckForUpdate)resp.mData;
		String title = context.getResources().getString(R.string.upgrade_notification_title);
		String text = checkUpdate.mChangeLog;
		NotificationMgr.showUpgradeNotification(context, title, text, checkUpdate);
	}
	
	public static void startClientQuery(Context context, IDataCallback callback) {
		int dataId = IDataConstant.CHECK_UPDATE;
		DataCenter.getInstance().requestDataAsync(context, dataId, callback, null);	
		AppSettings.setClientUpdateQueryTime(context, System.currentTimeMillis());
	}
}
