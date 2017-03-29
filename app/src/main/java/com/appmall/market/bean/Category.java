package com.appmall.market.bean;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.appmall.market.data.IDataBase;

/**
 * 分类数据
 *  
 *
 */
public class Category implements IDataBase {
	private static final String NAME_ID = "id";
	private static final String NAME_TITLE = "title";
	private static final String NAME_ICON_URL = "icon_url";
	private static final String NAME_BRIEF = "brief";
	private static final String NAME_DATA_SOURCE = "data_source";
	private static final String NAME_DATA_SOURCE_RANK = "rank";
	private static final String NAME_DATA_SOURCE_NEWEST = "newest";
	
	public int mId;
	public String mTitle;
	public String mIconUrl;
	public String mBrief;
	public String mDataSourceRank;
	public String mDataSourceNewest;
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mId = jsonObj.optInt(NAME_ID);
		mTitle = jsonObj.optString(NAME_TITLE);
		mIconUrl = jsonObj.optString(NAME_ICON_URL);
		mBrief = jsonObj.optString(NAME_BRIEF);
		
		JSONObject dataSource = jsonObj.optJSONObject(NAME_DATA_SOURCE);
		if (dataSource == null)
			throw new JSONException("");
		mDataSourceRank = dataSource.optString(NAME_DATA_SOURCE_RANK);
		mDataSourceNewest = dataSource.optString(NAME_DATA_SOURCE_NEWEST);
				
		if (TextUtils.isEmpty(mDataSourceRank) || TextUtils.isEmpty(mDataSourceNewest))
			throw new JSONException("");
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(NAME_ID, mId);
		jsonObj.put(NAME_TITLE, mTitle);
		jsonObj.put(NAME_ICON_URL, mIconUrl);
		jsonObj.put(NAME_BRIEF, mBrief);
		
		JSONObject dataSource = new JSONObject();
		dataSource.put(NAME_DATA_SOURCE_RANK, mDataSourceRank);
		dataSource.put(NAME_DATA_SOURCE_NEWEST, mDataSourceNewest);
		
		jsonObj.put(NAME_DATA_SOURCE, dataSource);
		return jsonObj;
	}
	
}
