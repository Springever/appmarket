package com.appmall.market.data;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalAppsParam implements IDataBase {

	private static final String NAME_PACKAGENAME = "packagename";
	private static final String NAME_SIZE = "size";
	private static final String NAME_MD5 = "md5";
	
	public String mPackageName;
	public long mSize;
	public String mMd5;
	
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mPackageName = jsonObj.optString(NAME_PACKAGENAME);
		mSize = jsonObj.optLong(NAME_SIZE);
		mMd5 = jsonObj.optString(NAME_MD5);
	}
		
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(NAME_PACKAGENAME, mPackageName);
		jsonObj.put(NAME_SIZE, mSize);
		jsonObj.put(NAME_MD5, mMd5);
		return jsonObj;
	}
}
