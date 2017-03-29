package com.appmall.market.bean;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import com.appmall.market.data.IDataBase;

/**
 * 首页广告数据
 *  
 *
 */
public class Advert implements IDataBase , Serializable{

	private static final long serialVersionUID = -5652095125056203362L;
	public static final int POSITION_TOP = 1;
	public static final int POSITION_MIDDLE = 2;

	private static final String NAME_ID = "id";
	private static final String NAME_TITLE = "title";
	private static final String NAME_IMAGE_URL = "image_url";
	private static final String NAME_POSITION = "position";
	private static final String NAME_DATA_TARGET = "data_target";
	private static final String NAME_DATA_SOURCE = "data_source";
	
	private static final String VALUE_POSITION_TOP = "top";
	private static final String VALUE_POSITION_MIDDLE = "middle";
	
	public int mId;
	public String mTitle;
	public String mImageUrl;
	public int mPosition;
	public int mDataTarget;
	public String mDataSource;
	
	public static final int CHARGE_FREE = App.CHARGE_FREE;
	public static final int CHARGE_NOT_FREE = App.CHARGE_NOT_FREE;
	
	//后期补上字段
	private static final String NAME_CHARGE_DESC = "charge_description";
	private static final String NAME_SECURITY_SCAN = "security_scan";
	private static final String NAME_APP_ID = "id";
	private static final String NAME_APP_TITLE = "title";
	private static final String NAME_CATEGORY_TITLE = "category_title";
	private static final String NAME_APP_URL = "download_url";
	private static final String NAME_APP_ICON_URL = "icon_url";
	private static final String NAME_APP_STAR_LEVEL = "star_level";
	private static final String NAME_APP_VOTES = "votes";
	private static final String NAME_APP_SIZE = "size";
	private static final String NAME_CHARGE = "charge";
	private static final String NAME_APP_VERSION_NAME = "version_name";
	private static final String NAME_APP_VERSION_CODE = "version_code";
	private static final String NAME_APP_PACKAGE_NAME = "package_name";
	private static final String NAME_PACKAGE_SIGNATURE = "package_signature";
	private static final String NAME_AD_STATUS = "ad_status"; //1 = 无广告 
	private static final String NAME_OFFICIAL_STATUS = "official_status";//1 = 官方版
	private static final String NAME_DOWNLOAD_NUM = "week_download_num";
	public int mAppId;
	public String mAppTitle;
	public String mCategoryTitle;
	public String mAppUrl;
	public String mAppIconUrl;
	public String mSecurityScan;
	public String mChargeDesc;
	public double mStartLevel;
	public long mVotes;
	public int mCharge;
	public String mPackageName;
	public String mVersionName;
	public int mVersionCode;
	public long mSize;
	public int mAdvStatus;
	public int mOffcialStatus;
	public String mPackageSignature;
	public String mDownloadNum;
	
	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mId = jsonObj.optInt(NAME_ID);
		mTitle = jsonObj.optString(NAME_TITLE);
		mImageUrl = jsonObj.optString(NAME_IMAGE_URL);
		mPosition = convPosStringToInteger(jsonObj.optString(NAME_POSITION));
		mDataTarget = jsonObj.optInt(NAME_DATA_TARGET);
		mDataSource = jsonObj.optString(NAME_DATA_SOURCE);
		
		JSONObject jsonApp = jsonObj.optJSONObject("app");
		if(jsonApp != null) {
			mAppId = jsonApp.optInt(NAME_APP_ID);
			mAppTitle = jsonApp.optString(NAME_APP_TITLE);
			mAppUrl = jsonApp.optString(NAME_APP_URL);
			mAppIconUrl = jsonApp.optString(NAME_APP_ICON_URL);
			mSecurityScan = jsonApp.optString(NAME_SECURITY_SCAN);
			mChargeDesc = jsonApp.optString(NAME_CHARGE_DESC);
			mCategoryTitle = jsonApp.optString(NAME_CATEGORY_TITLE);
			mStartLevel = jsonApp.optDouble(NAME_APP_STAR_LEVEL, 10);
			mVotes = jsonApp.optLong(NAME_APP_VOTES);
			mSize = jsonApp.optLong(NAME_APP_SIZE);
			mVersionCode = jsonApp.optInt(NAME_APP_VERSION_CODE);
			mCharge = jsonApp.optInt(NAME_CHARGE, CHARGE_NOT_FREE);
			mVersionName = jsonApp.optString(NAME_APP_VERSION_NAME);
			mPackageName = jsonApp.optString(NAME_APP_PACKAGE_NAME);
			mPackageSignature = jsonApp.optString(NAME_PACKAGE_SIGNATURE);
			mAdvStatus = jsonApp.optInt(NAME_AD_STATUS);
			mOffcialStatus = jsonApp.optInt(NAME_OFFICIAL_STATUS);
			mDownloadNum = jsonApp.optString(NAME_DOWNLOAD_NUM);
		}
		
		if (TextUtils.isEmpty(mDataSource))
			throw new JSONException("");
	}

	private int convPosStringToInteger(String position) {
		if (VALUE_POSITION_MIDDLE.equalsIgnoreCase(position)) {
			return POSITION_MIDDLE;
		} else {
			return POSITION_TOP;
		}
	}
	
	private String convPosIntegerToString(int position) {
		if (position == POSITION_MIDDLE) {
			return VALUE_POSITION_MIDDLE;
		} else {
			return VALUE_POSITION_TOP;
		}
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(NAME_ID, mId);
		ret.put(NAME_TITLE, mTitle);
		ret.put(NAME_IMAGE_URL, mImageUrl);
		ret.put(NAME_POSITION, convPosIntegerToString(mPosition));
		ret.put(NAME_DATA_TARGET, mDataTarget);
		ret.put(NAME_DATA_SOURCE, mDataSource);
		
		ret.put(NAME_APP_ID, mAppId);
		ret.put(NAME_APP_TITLE, mAppTitle);
		ret.put(NAME_CATEGORY_TITLE, mCategoryTitle);
		ret.put(NAME_APP_URL, mAppUrl);
		ret.put(NAME_APP_ICON_URL, mAppIconUrl);
		ret.put(NAME_SECURITY_SCAN, mSecurityScan);
		ret.put(NAME_CHARGE_DESC, mChargeDesc);
		ret.put(NAME_APP_STAR_LEVEL, mStartLevel);
		ret.put(NAME_APP_VOTES, mVotes);
		ret.put(NAME_APP_SIZE, mSize);
		ret.put(NAME_CHARGE, mCharge);
		ret.put(NAME_APP_VERSION_CODE, mVersionCode);
		ret.put(NAME_APP_VERSION_NAME, mVersionName);
		ret.put(NAME_APP_PACKAGE_NAME, mPackageName);
		ret.put(NAME_PACKAGE_SIGNATURE, mPackageSignature);
		ret.put(NAME_AD_STATUS, mAdvStatus);
		ret.put(NAME_OFFICIAL_STATUS, mOffcialStatus);
		ret.put(NAME_DOWNLOAD_NUM, mDownloadNum);
		return ret;
	}
	
}
