package com.appmall.market.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.appmall.market.data.IDataBase;

/**
 * 应用详情数据
 *  
 *
 */
public class AppDetail implements IDataBase {

	private static final String NAME_APP = "app";
	private static final String NAME_ID = "id";
	private static final String NAME_PKG_ID = "pkg_id";
	private static final String NAME_TITLE = "title";
	private static final String NAME_CATEGORY_TITLE = "category_title";
	private static final String NAME_SIZE = "size";
	private static final String NAME_VERSION_NAME = "version_name";
	private static final String NAME_STAR_LEVEL = "star_level";
	private static final String NAME_VOTES = "votes";
	private static final String NAME_BRIEF = "brief";
	private static final String NAME_CHANGE_LOG = "change_log";
	private static final String NAME_ICON_URL = "icon_url";
	private static final String NAME_DOWNLOAD_URL = "download_url";
	private static final String NAME_SNAPSHOT_URLS = "snapshot_urls";
	private static final String NAME_CHARGE = "charge";
	private static final String NAME_CHARGE_DESCRIPTION = "charge_description";
	private static final String NAME_SECURITY_SCAN = "security_scan";
	private static final String NAME_PACKAGE_NAME = "package_name";
	private static final String NAME_VERSION_CODE = "version_code";
	private static final String NAME_UPDATE_TIME = "update_time";
	private static final String NAME_DISCLAIMER = "disclaimer";
	private static final String NAME_DEVELOPER = "developer";
	private static final String NAME_RECOMMENDS = "recommends";
	
	public static final int CHARGE_FREE = App.CHARGE_FREE;
	public static final int CHARGE_NOT_FREE = App.CHARGE_NOT_FREE;
	
	public static class SnapShot implements IDataBase {
		
		private static final String NAME_DEFAULT = "default";
		private static final String NAME_HDPI = "hdpi";
		
		public String mDefaultUrl = null;
		public String mHDPIUrl = null;
		
		@Override
		public void readFromJSON(JSONObject jsonObj) throws JSONException {
			mDefaultUrl = jsonObj.optString(NAME_DEFAULT);
			mHDPIUrl = jsonObj.optString(NAME_HDPI);
		}
		
		@Override
		public JSONObject generateJSONObject() throws JSONException {
			JSONObject ret = new JSONObject();
			ret.put(NAME_DEFAULT, mDefaultUrl);
			ret.put(NAME_HDPI, mHDPIUrl);
			return ret;
		}
	}
	
	public int mID;
	public int mPkgID;
	public String mTitle;
	public String mCategoryTitle;
	public long mSize;
	public String mVersionName;
	public int mStarLevel;
	public int mVotes;
	public String mBrief;
	public String mChangeLog;
	public String mIconUrl;
	public String mDownloadUrl;
	public ArrayList<SnapShot> mSnapshots = new ArrayList<SnapShot>();
	public int mCharge;
	public String mChargeDescription;
	public String mSecurityScan;
	public String mPackageName;
	public int mVersionCode;
	public long mUpdateTime;
	public String mDisclaimer;
	public String mDeveloper;
	public ArrayList<App> mRecommends = new ArrayList<App>();
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		JSONObject appObj = jsonObj.optJSONObject(NAME_APP);
		if (appObj == null)
			throw new JSONException("");
		
		mID = appObj.optInt(NAME_ID);
		mPkgID = appObj.optInt(NAME_PKG_ID);
		mTitle = appObj.optString(NAME_TITLE);
		mCategoryTitle = appObj.optString(NAME_CATEGORY_TITLE);
		mSize = appObj.optLong(NAME_SIZE);
		mVersionName = appObj.optString(NAME_VERSION_NAME);
		mStarLevel = appObj.optInt(NAME_STAR_LEVEL);
		mVotes = appObj.optInt(NAME_VOTES);
		mBrief = appObj.optString(NAME_BRIEF);
		mChangeLog = appObj.optString(NAME_CHANGE_LOG);
		mIconUrl = appObj.optString(NAME_ICON_URL);
		mDownloadUrl = appObj.optString(NAME_DOWNLOAD_URL);	
		
		JSONArray array = appObj.optJSONArray(NAME_SNAPSHOT_URLS);
		mSnapshots.clear();
		if (array != null) {
			int size = array.length();
			for (int index = 0; index < size; ++index) {
				JSONObject snapshotObj = array.optJSONObject(index);
				if (snapshotObj == null)
					continue;
				
				try {
					SnapShot ss = new SnapShot();
					ss.readFromJSON(snapshotObj);
					mSnapshots.add(ss);
				} catch (JSONException e) { }
			}
		}
		
		mCharge = appObj.optInt(NAME_CHARGE, CHARGE_NOT_FREE);
		mChargeDescription = appObj.optString(NAME_CHARGE_DESCRIPTION);
		mSecurityScan = appObj.optString(NAME_SECURITY_SCAN);
		mPackageName = appObj.optString(NAME_PACKAGE_NAME);
		mVersionCode = appObj.optInt(NAME_VERSION_CODE);
		mUpdateTime = appObj.optLong(NAME_UPDATE_TIME);
		mDisclaimer = appObj.optString(NAME_DISCLAIMER);
		mDeveloper = appObj.optString(NAME_DEVELOPER);
		
		array = appObj.optJSONArray(NAME_RECOMMENDS);
		mRecommends.clear();
		if (array != null) {
			int size = array.length();
			for (int index = 0; index < size; ++index) {
				JSONObject value = array.optJSONObject(index);
				try {
					App app = new App();
					app.readFromJSON(value);
					mRecommends.add(app);
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		if (TextUtils.isEmpty(mPackageName) || TextUtils.isEmpty(mDownloadUrl))
			throw new JSONException("");
		
		mBrief = convertBRLabels(mBrief);
		mChangeLog = convertBRLabels(mChangeLog);
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		
		JSONObject app = new JSONObject();
		app.put(NAME_ID, mID);
		app.put(NAME_PKG_ID, mPkgID);
		app.put(NAME_TITLE, mTitle);
		app.put(NAME_CATEGORY_TITLE, mCategoryTitle);
		app.put(NAME_SIZE, mSize);
		app.put(NAME_VERSION_NAME, mVersionName);
		app.put(NAME_STAR_LEVEL, mStarLevel);
		app.put(NAME_VOTES, mVotes);
		app.put(NAME_BRIEF, mBrief);
		app.put(NAME_CHANGE_LOG, mChangeLog);
		app.put(NAME_ICON_URL, mIconUrl);
		app.put(NAME_DOWNLOAD_URL, mDownloadUrl);
		
		JSONArray array = new JSONArray();
		for (SnapShot ss : mSnapshots) {
			if (ss != null)
				array.put(ss.generateJSONObject());
		}
		app.put(NAME_SNAPSHOT_URLS, array);
		
		app.put(NAME_CHARGE, mCharge);
		app.put(NAME_CHARGE_DESCRIPTION, mChargeDescription);
		app.put(NAME_SECURITY_SCAN, mSecurityScan);
		app.put(NAME_PACKAGE_NAME, mPackageName);
		app.put(NAME_VERSION_CODE, mVersionCode);
		app.put(NAME_UPDATE_TIME, mUpdateTime);
		app.put(NAME_DISCLAIMER, mDisclaimer);
		app.put(NAME_DEVELOPER, mDeveloper);
		
		array = new JSONArray();
		for (App recommend : mRecommends) {
			if (recommend != null)
				array.put(recommend.generateJSONObject());
		}
		app.put(NAME_RECOMMENDS, array);
		
		jsonObj.put(NAME_APP, app);
		return jsonObj;
	}
	
	private String convertBRLabels(String text) {
		if (TextUtils.isEmpty(text))
			return text;
		
		return text.replaceAll("<br>", "\n");
	}
}
