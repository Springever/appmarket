package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

public class SearchRecommend implements IDataBase {

	private static final String NAME_RECOMMENDS = "recommends";
	
	public ArrayList<App> mApps = new ArrayList<App>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONArray array = jsonObj.optJSONArray(NAME_RECOMMENDS);
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
		JSONObject ret = new JSONObject();
		JSONArray array = new JSONArray();
		for (App app : mApps) {
			if (app != null)
				array.put(app.generateJSONObject());
		}
		ret.put(NAME_RECOMMENDS, array);
		return ret;
	}

}
