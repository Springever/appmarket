package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appmall.market.data.IDataBase;

/**
 * 分类列表数据
 *  
 *
 */
public class CategoryList implements IDataBase {

	private static final String NAME_CATEGORIES = "categories";
	private static final String NAME_ADVERTS = "adverts";
	
	public ArrayList<Advert> mAdverts = new ArrayList<Advert>();
	public ArrayList<Category> mCategories = new ArrayList<Category>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONArray array = jsonObj.optJSONArray(NAME_ADVERTS);
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
		
		array = jsonObj.optJSONArray(NAME_CATEGORIES);
		mCategories.clear();
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Category cate = new Category();
					cate.readFromJSON(value);
					mCategories.add(cate);
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
		for (Advert advert : mAdverts) {
			if (advert != null)
				array.put(advert.generateJSONObject());
		}
		jsonObj.put(NAME_ADVERTS, array);
		
		array = new JSONArray();
		for (Category category : mCategories) {
			if (category != null)
				array.put(category.generateJSONObject());
		}
		jsonObj.put(NAME_CATEGORIES, array);
		return jsonObj;
	}

}
