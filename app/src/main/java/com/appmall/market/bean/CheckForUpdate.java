package com.appmall.market.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

public class CheckForUpdate implements IDataBase {

	private static final String NAME_VERSION = "version";
	private static final String NAME_VERSION_CODE = "version_code";
	private static final String NAME_VERSION_NAME = "version_name";
	private static final String NAME_SIZE = "size";
	private static final String NAME_FORCED = "forced";
	private static final String NAME_CHANGE_LOG = "change_log";
	private static final String NAME_FILE_URL = "file_url";
	
	public int mVersionCode;
	public String mVersionName;
	public long mSize;
	public boolean mForced;
	public String mChangeLog;
	public String mFileUrl;
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONObject versionObj = jsonObj.optJSONObject(NAME_VERSION);
		if (versionObj == null)
			return;
		
		mVersionCode = versionObj.optInt(NAME_VERSION_CODE);
		mVersionName = versionObj.optString(NAME_VERSION_NAME);
		mSize = versionObj.optLong(NAME_SIZE);
		mForced = versionObj.optBoolean(NAME_FORCED);
		mChangeLog = versionObj.optString(NAME_CHANGE_LOG);
		mFileUrl = versionObj.optString(NAME_FILE_URL);
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		JSONObject version = new JSONObject();
		version.put(NAME_VERSION_CODE, mVersionCode);
		version.put(NAME_VERSION_NAME, mVersionName);
		version.put(NAME_SIZE, mSize);
		version.put(NAME_FORCED, mForced);
		version.put(NAME_CHANGE_LOG, mChangeLog);
		version.put(NAME_FILE_URL, mFileUrl);
		ret.put(NAME_VERSION, version);
		return ret;
	}

}
