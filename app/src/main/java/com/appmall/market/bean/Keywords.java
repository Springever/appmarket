package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.appmall.market.data.IDataBase;
import com.appmall.market.bean.Advert;

/**
 * 热词
 *  
 *
 */
public class Keywords implements IDataBase {

	public static class Keyword implements IDataBase {
		
		private static final String NAME_WORD = "word";
		private static final String NAME_DATA_SOURCE = "data_source";
		
		public String mWord;
		public String mDataSource;
		
		@Override
		public void readFromJSON(JSONObject jsonObj) throws JSONException {
			mWord = jsonObj.optString(NAME_WORD, "");
			mDataSource = jsonObj.optString(NAME_DATA_SOURCE, "");
			
			if (TextUtils.isEmpty(mWord) || TextUtils.isEmpty(mDataSource))
				throw new JSONException("");
		}
		
		@Override
		public JSONObject generateJSONObject() throws JSONException {
			JSONObject ret = new JSONObject();
			ret.put(NAME_WORD, mWord);
			ret.put(NAME_DATA_SOURCE, mDataSource);
			return ret;
		}
	}

	private static final String NAME_PIC_ADVERTS = "pic_adverts";
	private static final String NAME_TEXT_ADVERTS = "text_adverts";
	private static final String NAME_KEYWORDS = "keywords";
	
	public ArrayList<Advert> mPicAdverts = new ArrayList<Advert>();
	public ArrayList<Advert> mTextAdverts = new ArrayList<Advert>();
	public ArrayList<Keyword> mKeywords = new ArrayList<Keyword>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mPicAdverts.clear();
		JSONArray array = jsonObj.optJSONArray(NAME_PIC_ADVERTS);
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Advert adv = new Advert();
					adv.readFromJSON(value);
					mPicAdverts.add(adv);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		mTextAdverts.clear();
		array = jsonObj.optJSONArray(NAME_TEXT_ADVERTS);
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Advert adv = new Advert();
					adv.readFromJSON(value);
					mTextAdverts.add(adv);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		mKeywords.clear();
		array = jsonObj.optJSONArray(NAME_KEYWORDS);
		if (array != null) {
			for (int index = 0; index < array.length(); ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					Keyword kw = new Keyword();
					kw.readFromJSON(value);
					mKeywords.add(kw);
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
		for (Advert adv : mPicAdverts) {
			if (adv != null)
				array.put(adv.generateJSONObject());
		}
		jsonObj.put(NAME_PIC_ADVERTS, array);
		
		array = new JSONArray();
		for (Advert adv : mTextAdverts) {
			if (adv != null)
				array.put(adv.generateJSONObject());
		}
		jsonObj.put(NAME_TEXT_ADVERTS, array);
		
		array = new JSONArray();
		for (Keyword word : mKeywords) {
			if (word != null)
				array.put(word.generateJSONObject());
		}
		jsonObj.put(NAME_KEYWORDS, array);
		return jsonObj;
	}

}
