package com.appmall.market.data;

import android.content.Context;
import android.util.SparseArray;

import com.appmall.market.common.AppSettings;

/**
 * 数据缓存规则
 *  
 *
 */
public class CacheRule {

	private static CacheRule sInstance;
	private SparseArray<Rule> mRules;

	public static synchronized CacheRule getInstance(Context context) {
		if (sInstance == null)
			sInstance = new CacheRule(context);
		
		return sInstance;
	}
	
	private CacheRule(Context context) {
		mRules = new SparseArray<CacheRule.Rule>();
		mRules.append(IDataConstant.HOMEPAGE, new FixedRule(true));
		mRules.append(IDataConstant.UPDATE_INFO, new UpdateRule(context));
	}
	
	public boolean readCache(int dataId) {
		Rule rule = mRules.get(dataId);
		if (rule == null)
			return false;
		
		return rule.available();
	}
	
	public boolean saveCache(int dataId) {
		return mRules.get(dataId) != null;
	}
	
	public static interface Rule {
		public boolean available();
	}
	
	public static class FixedRule implements Rule {
		private boolean mAvailable;

		public FixedRule(boolean available) {
			mAvailable = available;
		}

		@Override
		public boolean available() {
			return mAvailable;
		}
	}
	
	public static class UpdateRule implements Rule {

		private static final long UPDATE_VALIDITY = 10 * 60 * 1000L;
		
		private Context mAppContext;

		public UpdateRule(Context context) {
			mAppContext = context.getApplicationContext();
		}
		
		@Override
		public boolean available() {
			long lastTime = AppSettings.getUpdateCacheTime(mAppContext);
			long currTime = System.currentTimeMillis();
			return lastTime + UPDATE_VALIDITY > currTime;
		}
	}

}
