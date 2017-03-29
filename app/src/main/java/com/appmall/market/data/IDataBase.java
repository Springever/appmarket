package com.appmall.market.data;

import org.json.JSONException;
import org.json.JSONObject;


public interface IDataBase {

	public void readFromJSON(JSONObject jsonObj) throws JSONException;
	public JSONObject generateJSONObject() throws JSONException;
	
}
