package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 一组必备软件
 *  
 *
 */
public class MustInstallBatch implements IDataBase {
	
	private static final String NAME_CATEGORY_ID = "category_id";
	private static final String NAME_CATEGORY_TITLE = "category_title";
	private static final String NAME_APPS = "apps";
	
	public int mCategoryId;
	public String mCategoryTitle;
	public ArrayList<App> mApps = new ArrayList<App>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mCategoryId = jsonObj.optInt(NAME_CATEGORY_ID);
		mCategoryTitle = jsonObj.optString(NAME_CATEGORY_TITLE, "");
		
		mCategoryId = jsonObj.has(NAME_CATEGORY_ID) ? jsonObj.getInt(NAME_CATEGORY_ID) : 0;
		mCategoryTitle = jsonObj.has(NAME_CATEGORY_TITLE) ? jsonObj.getString(NAME_CATEGORY_TITLE) : "";
		
		JSONArray array = jsonObj.optJSONArray(NAME_APPS);
		mApps.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					App app = new App();
					app.readFromJSON(value);
					mApps.add(app);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}
	
	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(NAME_CATEGORY_ID, mCategoryId);
		jsonObj.put(NAME_CATEGORY_TITLE, mCategoryTitle);
		JSONArray array = new JSONArray();
		for (App app : mApps) {
			if (app != null)
				array.put(app.generateJSONObject());
		}
		jsonObj.put(NAME_APPS, array);
		return jsonObj;
	}
}
