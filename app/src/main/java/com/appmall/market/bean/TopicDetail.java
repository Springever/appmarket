package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 专题详情
 *  
 *
 */
public class TopicDetail implements IDataBase {

	private static final String NAME_TOPIC = "topic";
	private static final String NAME_ID = "id";
	private static final String NAME_TITLE = "title";
	private static final String NAME_BRIEF = "brief";
	private static final String NAME_IMAGE_URL = "image_url";
	private static final String NAME_APPS = "apps";
	
	public int mID;
	public String mTitle;
	public String mBrief;
	public String mImageUrl;
	public ArrayList<App> mApps = new ArrayList<App>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONObject topicObj = jsonObj.optJSONObject(NAME_TOPIC);
		if (topicObj == null)
			throw new JSONException("");
		
		mID = topicObj.optInt(NAME_ID);
		mTitle = topicObj.optString(NAME_TITLE);
		mBrief = topicObj.optString(NAME_BRIEF);
		mImageUrl = topicObj.optString(NAME_IMAGE_URL);
		
		JSONArray array = topicObj.optJSONArray(NAME_APPS);
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
		JSONObject topicObj = new JSONObject();
		topicObj.put(NAME_ID, mID);
		topicObj.put(NAME_TITLE, mTitle);
		topicObj.put(NAME_BRIEF, mBrief);
		topicObj.put(NAME_IMAGE_URL, mImageUrl);
		JSONArray array = new JSONArray();
		for (App app : mApps) {
			if (app != null)
				array.put(app.generateJSONObject());
		}
		topicObj.put(NAME_APPS, array);
		
		jsonObj.put(NAME_TOPIC, topicObj);
		return jsonObj;
	}

}
