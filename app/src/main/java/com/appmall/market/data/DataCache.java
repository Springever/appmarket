package com.appmall.market.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.appmall.market.bean.HotPromoteTop;
import com.appmall.market.bean.UpdateInfo;
import com.appmall.market.data.DataCenter.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 数据缓存管理
 *  
 *
 */
public class DataCache {

	private static final String PREF_DATA_CACHE = "data_cache";

	private static final String NAME_CONTEXT = "context";
	private static final String NAME_ENTITIES = "entities";
	
	@SuppressWarnings("unchecked")
	public synchronized Response loadCacheResponse(Context context, int dataId) {
		boolean readCache = CacheRule.getInstance(context).readCache(dataId);
		if (!readCache)
			return null;
		
		SharedPreferences pref = context.getSharedPreferences(PREF_DATA_CACHE, Context.MODE_PRIVATE);
		String jsonString = pref.getString(String.valueOf(dataId), null);
		
		// 无缓存记录，返回
		if (TextUtils.isEmpty(jsonString))
			return null;
		
		try {
			// 存在有效缓存，解析
			JSONObject jsonObj = new JSONObject(jsonString);
			Response resp = new Response();
			JSONObject jsonContext = jsonObj.optJSONObject(NAME_CONTEXT);
			if (jsonContext != null) {
				resp.mContext = new HashMap<String, String>();
				Iterator<String> iterator = jsonContext.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					resp.mContext.put(key, jsonContext.getString(key));
				}
			}

			JSONObject entities = jsonObj.optJSONObject(NAME_ENTITIES);
			if (entities != null) {
				resp.mData = convertEntitiesToData(dataId, entities);
			}
			
			resp.mSuccess = true;
			
			return resp;
		} catch (Exception e) { }
		
		return null;
	}
	
	public boolean hasCache(Context context, int dataId) {
		boolean bHasCache = false;
		SharedPreferences pref = context.getSharedPreferences(PREF_DATA_CACHE, Context.MODE_PRIVATE);
		String jsonString = pref.getString(String.valueOf(dataId), null);
		
		// 无缓存记录，返回
		if (!TextUtils.isEmpty(jsonString))
			bHasCache = true;
		return bHasCache;
	}

	private IDataBase convertEntitiesToData(int dataId, JSONObject entities) throws JSONException {
		switch (dataId) {
		case IDataConstant.HOMEPAGE:
			HotPromoteTop home = new HotPromoteTop();
			home.readFromJSON(entities);
			return home;
		case IDataConstant.UPDATE_INFO:
			UpdateInfo info = new UpdateInfo();
			info.readFromJSON(entities);
			return info;
		default:
			return null;
		}
	}

	public synchronized void saveResponseCache(Context context, int dataId, Response resp) {
		boolean save = CacheRule.getInstance(context).saveCache(dataId);
		if (resp == null || !resp.mSuccess || !save)
			return;
		
		JSONObject jsonObj = new JSONObject();
		JSONObject jsonContext = null;
		
		try {
			if (resp.mContext != null) {
				jsonContext = new JSONObject();
				Iterator<Entry<String, String>> iterator = resp.mContext.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					String key = entry.getKey();
					String value = entry.getValue();
					if (TextUtils.isEmpty(key))
						continue;
					if (value == null)
						value = "";
					jsonContext.put(key, value);
				}
			}
		
			jsonObj.put(NAME_CONTEXT, jsonContext);
			jsonObj.put(NAME_ENTITIES, resp.mData.generateJSONObject());
			
			SharedPreferences pref = context.getSharedPreferences(PREF_DATA_CACHE, Context.MODE_PRIVATE);
			pref.edit().putString(String.valueOf(dataId), jsonObj.toString()).commit();
		} catch (Exception e) { }
	}

}
