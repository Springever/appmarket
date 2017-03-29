package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 热门推荐首页底部数据
 *  
 *
 */
public class HotPromoteRest implements IDataBase {

	private static final String NAME_RECOMMENDS = "recommends";
	private static final String NAME_TOPIC = "topics";
	private static final String NAME_ADVERTS = "adverts";
	
	public ArrayList<PromoteRecommend> mRecommends = new ArrayList<PromoteRecommend>();
	public ArrayList<Topic> mTopics = new ArrayList<Topic>();
	public ArrayList<Advert> mAdverts = new ArrayList<Advert>();

	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONArray array = jsonObj.optJSONArray(NAME_RECOMMENDS);
		mRecommends.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					PromoteRecommend recommend = new PromoteRecommend();
					recommend.readFromJSON(value);
					mRecommends.add(recommend);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		array = jsonObj.optJSONArray(NAME_TOPIC);
		mTopics.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Topic topic = new Topic();
					topic.readFromJSON(value);
					mTopics.add(topic);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		array = jsonObj.optJSONArray(NAME_ADVERTS);
		mAdverts.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Advert advert = new Advert();
					advert.readFromJSON(value);
					mAdverts.add(advert);
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
		for (PromoteRecommend recommend : mRecommends) {
			if (recommend != null)
				array.put(recommend.generateJSONObject());
		}
		jsonObj.put(NAME_RECOMMENDS, array);
		
		array = new JSONArray();
		for (Topic topic : mTopics) {
			if (topic != null)
				array.put(topic.generateJSONObject());
		}
		jsonObj.put(NAME_TOPIC, array);
		
		array = new JSONArray();
		for (Advert advert : mAdverts) {
			if (advert != null)
				array.put(advert.generateJSONObject());
		}
		jsonObj.put(NAME_ADVERTS, array);
		return jsonObj;
	}
	
}
