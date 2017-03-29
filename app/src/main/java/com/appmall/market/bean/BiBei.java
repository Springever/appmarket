package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 必备数据
 *  
 *
 */
public class BiBei implements IDataBase {
	
	private static final String NAME_MUST_INSTALLED = "must_installed";
	
	public ArrayList<MustInstallBatch> mMustInstalled = new ArrayList<MustInstallBatch>();

	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mMustInstalled.clear();
		JSONArray array = jsonObj.optJSONArray(NAME_MUST_INSTALLED);
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					MustInstallBatch mi = new MustInstallBatch();
					mi.readFromJSON(value);
					mMustInstalled.add(mi);
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
		for (MustInstallBatch mi : mMustInstalled) {
			if (mi != null) {
				array.put(mi.generateJSONObject());
			}
		}
		jsonObj.put(NAME_MUST_INSTALLED, array);
		return jsonObj;
	}
}
