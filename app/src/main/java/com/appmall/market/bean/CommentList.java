package com.appmall.market.bean;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 评论详情
 *  
 *
 */
public class CommentList implements IDataBase {

	private static final String NAME_RATE = "rate";
	private static final String NAME_REPEAT = "repeat";
	private static final String NAME_COMMENTS = "comments";
	
	public static class Rate implements IDataBase {
		private static final String NAME_RATE_TOTAL = "total";
		private static final String NAME_RATE_AVG = "avg";
		private static final String NAME_RATE_SECTION = "section";
		private static final String NAME_RATE_SECTION_1 = "1";
		private static final String NAME_RATE_SECTION_2 = "2";
		private static final String NAME_RATE_SECTION_3 = "3";
		private static final String NAME_RATE_SECTION_4 = "4";
		private static final String NAME_RATE_SECTION_5 = "5";

		public int mTotal;
		public double mAvg;
		public int mSection[] = new int[5];
		
		@Override
		public void readFromJSON(JSONObject jsonObj) throws JSONException {
			mTotal = jsonObj.optInt(NAME_RATE_TOTAL);
			mAvg = jsonObj.optDouble(NAME_RATE_AVG);
			
			JSONObject sectionObj = jsonObj.optJSONObject(NAME_RATE_SECTION);
			Arrays.fill(mSection, 0);
			if (sectionObj != null) {
				mSection[0] = sectionObj.optInt(NAME_RATE_SECTION_1);
				mSection[1] = sectionObj.optInt(NAME_RATE_SECTION_2);
				mSection[2] = sectionObj.optInt(NAME_RATE_SECTION_3);
				mSection[3] = sectionObj.optInt(NAME_RATE_SECTION_4);
				mSection[4] = sectionObj.optInt(NAME_RATE_SECTION_5);
			}
		}
		
		@Override
		public JSONObject generateJSONObject() throws JSONException {
			JSONObject ret = new JSONObject();
			ret.put(NAME_RATE_TOTAL, mTotal);
			ret.put(NAME_RATE_AVG, mAvg);
			
			JSONObject sectionObj = new JSONObject();
			sectionObj.put(NAME_RATE_SECTION_1, mSection[0]);
			sectionObj.put(NAME_RATE_SECTION_2, mSection[1]);
			sectionObj.put(NAME_RATE_SECTION_3, mSection[2]);
			sectionObj.put(NAME_RATE_SECTION_4, mSection[3]);
			sectionObj.put(NAME_RATE_SECTION_5, mSection[4]);
			
			ret.put(NAME_RATE_SECTION, sectionObj);
			return ret;
		}
	}
	
	public static class Comment implements IDataBase {
		private static final String NAME_ID = "id";
		private static final String NAME_STAR_LEVEL = "star_level";
		private static final String NAME_CONTENT = "content";
		private static final String NAME_DEVICE_NAME = "device_name";
		private static final String NAME_UPDATE_TIME = "update_time";
		private static final String NAME_IS_NEW = "is_new";
		
		private int mID;
		public int mStarLevel;
		public String mContent;
		public String mDeviceName;
		public long mUpdateTime;
		public int mIsNew;  //1 = 最新 	0=老版本
		
		@Override
		public void readFromJSON(JSONObject jsonObj) throws JSONException {
			mID = jsonObj.optInt(NAME_ID);
			mStarLevel = jsonObj.optInt(NAME_STAR_LEVEL);
			mContent = jsonObj.optString(NAME_CONTENT);
			mDeviceName = jsonObj.optString(NAME_DEVICE_NAME);
			mUpdateTime = jsonObj.optLong(NAME_UPDATE_TIME);
			mIsNew = jsonObj.optInt(NAME_IS_NEW);
		}
		
		@Override
		public JSONObject generateJSONObject() throws JSONException {
			JSONObject ret = new JSONObject();
			ret.put(NAME_ID, mID);
			ret.put(NAME_STAR_LEVEL, mStarLevel);
			ret.put(NAME_CONTENT, mContent);
			ret.put(NAME_DEVICE_NAME, mDeviceName);
			ret.put(NAME_UPDATE_TIME, mUpdateTime);
			ret.put(NAME_IS_NEW, mIsNew);
			return ret;
		}
	}

	public Rate mRate = new Rate();
	public boolean mRepeat;
	public ArrayList<Comment> mComments = new ArrayList<Comment>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mRate = new Rate();
		JSONObject rateObj = jsonObj.optJSONObject(NAME_RATE);
		if (rateObj != null) {
			mRate.readFromJSON(rateObj);
		}
		
		mRepeat = jsonObj.optBoolean(NAME_REPEAT);
		
		mComments.clear();
		JSONArray array = jsonObj.optJSONArray(NAME_COMMENTS);
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Comment comment = new Comment();
					comment.readFromJSON(value);
					mComments.add(comment);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(NAME_RATE, mRate.generateJSONObject());
		ret.put(NAME_REPEAT, mRepeat);
		
		JSONArray array = new JSONArray();
		for (Comment comment : mComments) {
			if (comment != null)
				array.put(comment.generateJSONObject());
		}
		ret.put(NAME_COMMENTS, array);
		return ret;
	}

}
