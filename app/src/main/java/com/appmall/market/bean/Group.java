package com.appmall.market.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 新品数据分组
 *  
 *
 */
public class Group implements IDataBase {

	public static final int GROUP_ID_NONE = -1;

	private static final String NAME_ID = "id";
	private static final String NAME_NAME = "name";
	private static final String NAME_WEIGHT = "weight";
	
	public int mID = GROUP_ID_NONE;
	public String mName;
	public int mWeight;
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mID = jsonObj.optInt(NAME_ID);
		mName = jsonObj.optString(NAME_NAME);
		mWeight = jsonObj.optInt(NAME_WEIGHT);
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(NAME_ID, mID);
		ret.put(NAME_NAME, mName);
		ret.put(NAME_WEIGHT, mWeight);
		return ret;
	}

}
