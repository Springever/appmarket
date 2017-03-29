package com.appmall.market.common;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.appmall.market.bean.AppUpdate;

/**
 * 用户忽略更新信息
 *  
 *
 */
public class UpdateIgnore {

	private static final int VALUE_IGNORE = 1;
	
	public static void onIgnore(Context context, AppUpdate item) {
		if (item == null || TextUtils.isEmpty(item.mPackageName))
			return;
		
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_IGNORE, 0);
		pref.edit().putInt(item.mPackageName, VALUE_IGNORE).commit();
	}

	public static void onRemoveIgnore(Context context, AppUpdate item) {
		if (item == null || TextUtils.isEmpty(item.mPackageName))
			return;
		
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_IGNORE, 0);
		pref.edit().remove(item.mPackageName).commit();
	}
	
	public static void clearIgnoreInfo(Context context) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_IGNORE, 0);
		pref.edit().clear().commit();
	}

	public static Set<String> getIgnorePackageSet(Context context) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_IGNORE, 0);
		return pref.getAll().keySet();
	}

}
