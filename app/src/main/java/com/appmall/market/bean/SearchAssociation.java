package com.appmall.market.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.bean.Keywords.Keyword;
import com.appmall.market.data.IDataBase;

/**
 * 搜索关联数据
 *  
 *
 */
public class SearchAssociation implements IDataBase {

	private static final String NAME_APPS = "apps";
	private static final String NAME_KEYWORDS = "keywords";
	
	public List<App> mApps = new ArrayList<App>();
	public List<Keywords.Keyword> mKeywords = new ArrayList<Keywords.Keyword>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
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
		
		array = jsonObj.optJSONArray(NAME_KEYWORDS);
		mKeywords.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Keyword kw = new Keyword();
					kw.readFromJSON(value);
					mKeywords.add(kw);
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
		for (App app : mApps) {
			if (app != null)
				array.put(app.generateJSONObject());
		}
		jsonObj.put(NAME_APPS, array);
		
		array = new JSONArray();
		for (Keyword kw : mKeywords) {
			if (kw != null)
				array.put(kw.generateJSONObject());
		}
		jsonObj.put(NAME_KEYWORDS, array);
		return jsonObj;
	}

}
