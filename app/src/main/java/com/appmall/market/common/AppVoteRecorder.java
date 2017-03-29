package com.appmall.market.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 记录用户是否已经对某个App投过票
 *  
 *
 */
public class AppVoteRecorder {

	public static final boolean hasVote(Context context, int appId) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_VOTE_APP, Context.MODE_PRIVATE);
		return pref.getBoolean(String.valueOf(appId), false);
	}
	
	public static final void setVoted(Context context, int appId) {
		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_VOTE_APP, Context.MODE_PRIVATE);
		pref.edit().putBoolean(String.valueOf(appId), true).commit();
	}
	
}
