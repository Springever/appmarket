package com.appmall.market.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 检测更新请求数据
 *  
 *
 */
public class CommentPost implements IPostData {

	private static final String NAME_APP_CHANNEL_ID = "appChannelId";
	private static final String NAME_STAR_LEVEL = "starLevel";
	private static final String NAME_CONTENT = "content";
	private static final String NAME_DEVICE_NAME = "deviceName";
	
	private int mAppChannelId;
	private int mStartLevel;
	private String mContent;
	private String mDeviceName;

	public CommentPost(int appID) {
		mAppChannelId = appID;
	}
	
	public void setStartLevel(int level) {
		mStartLevel = level;
	}
	
	public void setContent(String content) {
		mContent = content;
	}
	
	public void setDeviceName(String name) {
		mDeviceName = name;
	}
	
	@Override
	public byte[] buildPostData() {
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(NAME_APP_CHANNEL_ID, mAppChannelId);
			jsonObj.put(NAME_STAR_LEVEL, mStartLevel);
			jsonObj.put(NAME_CONTENT, mContent);
			jsonObj.put(NAME_DEVICE_NAME, mDeviceName);
			return jsonObj.toString().getBytes();
		} catch (JSONException e) { }
		
		return new byte[0];
	}
	
}
