package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 新品数据
 *  
 *
 */
public class NewProd implements IDataBase {
	
	private static final String NAME_ADVERTS = "adverts";
	private static final String NAME_GROUPS = "groups";
	private static final String NAME_APPS = "apps";
	
	public ArrayList<Advert> mAdverts = new ArrayList<Advert>();
	public ArrayList<Group> mGroups = new ArrayList<Group>();
	public ArrayList<App> mApps = new ArrayList<App>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONArray array = jsonObj.optJSONArray(NAME_ADVERTS);
		mAdverts.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Advert advert = new Advert();
					advert.readFromJSON(value);
					mAdverts.add(advert);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		array = jsonObj.optJSONArray(NAME_GROUPS);
		mGroups.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Group group = new Group();
					group.readFromJSON(value);
					mGroups.add(group);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		array = jsonObj.optJSONArray(NAME_APPS);
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
		JSONArray array = new JSONArray();
		for (Advert advert : mAdverts) {
			if (advert != null)
				array.put(advert);
		}
		jsonObj.put(NAME_ADVERTS, array);
		
		array = new JSONArray();
		for (Group group : mGroups) {
			if (group != null)
				array.put(group.generateJSONObject());
		}
		jsonObj.put(NAME_GROUPS, array);
		
		array = new JSONArray();
		for (App app : mApps) {
			if (app != null)
				array.put(app.generateJSONObject());
		}
		jsonObj.put(NAME_APPS, array);
		return jsonObj;
	}

}
