package com.appmall.market.bean;

import android.content.Context;

import com.appmall.market.ApplicationImpl;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.UpdateQuery;
import com.appmall.market.data.IDataBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新数据
 *  
 *
 */
public class UpdateInfo implements IDataBase {

	private static final String JSON_NOTICE_INTERVAL = "noticeInterval";
	private static final String JSON_UPAPPITEMS = "upappitems";
	private static final String JSON_IGNOREAPPITEMS = "ignoreappitems";
	
	public List<AppUpdate> mUpdates = new ArrayList<AppUpdate>();
	public List<AppUpdate> mIgnores = new ArrayList<AppUpdate>();
	public static long gUpdateIntervalHours = 0;
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		gUpdateIntervalHours = jsonObj.optLong(JSON_NOTICE_INTERVAL);
		Context context = ApplicationImpl.getSelfApplicationContext();
		long updateTimeInterval = AppSettings.getCheckUpdateTimeInterval(context);
		AppSettings.setCheckUpdateTimeInterval(context, gUpdateIntervalHours);
		if(updateTimeInterval != gUpdateIntervalHours)
		{
			UpdateQuery.setBackgroundQueryAlarm(context);
		}
		
		mUpdates.clear();
		Object upAppItemObj = jsonObj.opt(JSON_UPAPPITEMS);
		if (upAppItemObj != null) {
			// 兼容两种更新接口数据
			if (upAppItemObj instanceof JSONArray) {
				parseUpdateArrayData(mUpdates, (JSONArray) upAppItemObj);
			} else if (upAppItemObj instanceof JSONObject) {
				int objCount = ((JSONObject) upAppItemObj).length();
				parseUpdateObjData(mUpdates, (JSONObject) upAppItemObj, objCount);
			} else {
				// Can't resolve upappitems, do nothing.
			}
		}
		
		mIgnores.clear();
		Object ignoreAppItemObj = jsonObj.opt(JSON_IGNOREAPPITEMS);
		if (ignoreAppItemObj != null) {
			if (ignoreAppItemObj instanceof JSONArray) {
				parseUpdateArrayData(mIgnores, (JSONArray) ignoreAppItemObj);
			} else if (ignoreAppItemObj instanceof JSONObject) {
				int objCount = ((JSONObject) ignoreAppItemObj).length();
				parseUpdateObjData(mIgnores, (JSONObject) ignoreAppItemObj, objCount);
			} else {
				// Can't resolve ignoreappitems, do nothing.
			}
		}
	}

	private void parseUpdateObjData(List<AppUpdate> outList, JSONObject jsonObj, int objCount) {
		int length = objCount;
		if (jsonObj == null || objCount <= 0)
			return;
		for (int pos = 0; pos < length; ++pos) {
			JSONObject updateObj = jsonObj.optJSONObject(String.valueOf(pos));
			if (updateObj == null)
				continue;
			try {
				AppUpdate update = new AppUpdate();
				update.readFromJSON(updateObj);
				outList.add(update);
			} catch (JSONException e) {
				continue;
			}
		}
	}

	private void parseUpdateArrayData(List<AppUpdate> outList, JSONArray jsonObj) {
		JSONArray updateArray = jsonObj;
		int length = 0;
		if (updateArray != null && (length = updateArray.length()) > 0) {
			for (int pos = 0; pos < length; ++pos) {
				JSONObject updateObj = updateArray.optJSONObject(pos);
				if (updateObj == null)
					continue;
				try {
					AppUpdate update = new AppUpdate();
					update.readFromJSON(updateObj);
					outList.add(update);
				} catch (JSONException e) {
					continue;
				}
			}
		}
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(JSON_NOTICE_INTERVAL, gUpdateIntervalHours);
		
		JSONArray array = new JSONArray();
		for (AppUpdate update : mUpdates) {
			if (update == null)
				continue;
			JSONObject updateObj = update.generateJSONObject();
			array.put(updateObj);
		}
		ret.put(JSON_UPAPPITEMS, array);
		
		array = new JSONArray();
		for (AppUpdate update : mIgnores) {
			if (update == null)
				continue;
			JSONObject updateObj = update.generateJSONObject();
			array.put(updateObj);
		}
		ret.put(JSON_IGNOREAPPITEMS, array);
		
		return ret;
	}

}
