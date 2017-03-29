package com.appmall.market.bean;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.appmall.market.data.IDataBase;

public class Topic implements IDataBase {

	private static final String NAME_ID = "id";
	private static final String NAME_TITLE = "title";
	private static final String NAME_IMAGE_URL = "image_url";
	private static final String NAME_DATA_SOURCE = "data_source";
	
	public int mId;
	public String mTitle;
	public String mImageUrl;
	public String mDataSource;

	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mId = jsonObj.optInt(NAME_ID);
		mTitle = jsonObj.optString(NAME_TITLE, "");
		mImageUrl = jsonObj.optString(NAME_IMAGE_URL, "");
		mDataSource = jsonObj.optString(NAME_DATA_SOURCE, "");
		
		if (TextUtils.isEmpty(mDataSource))
			throw new JSONException("");
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(NAME_ID, mId);
		jsonObj.put(NAME_TITLE, mTitle);
		jsonObj.put(NAME_IMAGE_URL, mImageUrl);
		jsonObj.put(NAME_DATA_SOURCE, mDataSource);
		return jsonObj;
	}
	
}
