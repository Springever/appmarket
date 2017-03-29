package com.appmall.market.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import com.appmall.market.data.IDataBase;
import com.appmall.market.download.TaskStatus;

/**
 * 软件数据
 *  
 *
 */
public class App implements IDataBase , Serializable{
	
	private static final long serialVersionUID = -5652095125056203363L;
	public static final int CORNER_LABEL_NONE = 0;
	public static final int CORNER_LABEL_FIRST = 1;
	public static final int CORNER_LABEL_NEWEST = 2;
	public static final int CORNER_LABEL_HOT = 3;
	
	public static final int CHARGE_FREE = 0;
	public static final int CHARGE_NOT_FREE = 1;
	
	private static final String NAME_ID = "id";
	private static final String NAME_PKG_ID = "pkg_id";
	private static final String NAME_TITLE = "title";
	private static final String NAME_CATEGORY_TITLE = "category_title";
	private static final String NAME_ICON_URL = "icon_url";
	private static final String NAME_SIZE = "size";
	private static final String NAME_CORNER_LABEL = "corner_label";
	private static final String NAME_STAR_LEVEL = "star_level";
	private static final String NAME_REMARK = "remark";
	private static final String NAME_STYLE = "style";
	private static final String NAME_PACKAGE_NAME = "package_name";
	private static final String NAME_VERSION_NAME = "version_name";
	private static final String NAME_VERSION_CODE = "version_code";
	private static final String NAME_RECOMMEND_NUM = "recommend_num";
	private static final String NAME_DOWNLOAD_URL = "download_url";
	private static final String NAME_VOTES = "votes";
	private static final String NAME_GROUPID = "group_id";
	private static final String NAME_DATA_SOURCE = "data_source";
	private static final String NAME_UPDATE_TIME = "update_time";
	private static final String NAME_CHARGE = "charge";
	private static final String NAME_CHARGE_DESCRIPTION = "charge_description";
	private static final String NAME_SECURITY_SCAN = "security_scan";
	private static final String NAME_PACKAGE_SIGNATURE = "package_signature";
	private static final String NAME_AD_STATUS = "ad_status"; //1 = 无广告 
	private static final String NAME_OFFICIAL_STATUS = "official_status";//1 = 官方版
	private static final String NAME_DOWNLOAD_NUM = "week_download_num";
	
	public int mID;
	public int mPackageID;
	public String mTitle;
	public String mCategoryTitle;
	public String mIconUrl;
	public long mSize;
	public String mVersionName;
	public double mStarLevel;
	public String mRemark;
	public int mStyle;
	public int mCornerLabel;
	public String mPackageName;
	public int mVersionCode;
	public String mDownloadUrl;
	public int mRecommendNumber;
	public String mDataSource;
	public long mUpdateTime;
	public int mVotes;
	public int mGroupId = Group.GROUP_ID_NONE;
	public int mCharge;
	public String mChargeDescription;
	public String mSecurityScan;
	public int mAdvStatus;
	public int mOffcialStatus;
	public String mPackageSignature;
	public String mDownloadNum;
	
	// 标记App是否安装
	private int mInstStatus;
	// 标记App下载状态
	private int mTaskStatus = TaskStatus.STATUS_UNKNOWN;

	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mID = jsonObj.optInt(NAME_ID);
		mPackageID = jsonObj.optInt(NAME_PKG_ID);
		mTitle = jsonObj.optString(NAME_TITLE);
		mCategoryTitle = jsonObj.optString(NAME_CATEGORY_TITLE);
		mIconUrl = jsonObj.optString(NAME_ICON_URL);
		mSize = jsonObj.optLong(NAME_SIZE);
		mVersionName = jsonObj.optString(NAME_VERSION_NAME);
		mStarLevel = jsonObj.optDouble(NAME_STAR_LEVEL);
		mRemark = jsonObj.optString(NAME_REMARK);
		mStyle = jsonObj.optInt(NAME_STYLE);
		mCornerLabel = jsonObj.optInt(NAME_CORNER_LABEL);
		mPackageName = jsonObj.optString(NAME_PACKAGE_NAME);
		mVersionCode = jsonObj.optInt(NAME_VERSION_CODE);
		mDownloadUrl = jsonObj.optString(NAME_DOWNLOAD_URL);
		mRecommendNumber = jsonObj.optInt(NAME_RECOMMEND_NUM);
		mVotes = jsonObj.optInt(NAME_VOTES);
		mGroupId = jsonObj.optInt(NAME_GROUPID);
		mDataSource = jsonObj.optString(NAME_DATA_SOURCE);
		mUpdateTime = jsonObj.optLong(NAME_UPDATE_TIME);
		mCharge = jsonObj.optInt(NAME_CHARGE, CHARGE_NOT_FREE);
		mChargeDescription = jsonObj.optString(NAME_CHARGE_DESCRIPTION);
		mSecurityScan = jsonObj.optString(NAME_SECURITY_SCAN);
		mPackageSignature = jsonObj.optString(NAME_PACKAGE_SIGNATURE);
		mAdvStatus = jsonObj.optInt(NAME_AD_STATUS);
		mOffcialStatus = jsonObj.optInt(NAME_OFFICIAL_STATUS);
		mDownloadNum = jsonObj.optString(NAME_DOWNLOAD_NUM);
				
		// 数据检验
		if (TextUtils.isEmpty(mPackageName) || TextUtils.isEmpty(mDownloadUrl))
			throw new JSONException("");
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(NAME_ID, mID);
		ret.put(NAME_PKG_ID, mPackageID);
		ret.put(NAME_TITLE, mTitle);
		ret.put(NAME_CATEGORY_TITLE, mCategoryTitle);
		ret.put(NAME_ICON_URL, mIconUrl);
		ret.put(NAME_SIZE, mSize);
		ret.put(NAME_VERSION_NAME, mVersionName);
		ret.put(NAME_STAR_LEVEL, mStarLevel);
		ret.put(NAME_REMARK, mRemark);
		ret.put(NAME_STYLE, mStyle);
		ret.put(NAME_CORNER_LABEL, mCornerLabel);
		ret.put(NAME_PACKAGE_NAME, mPackageName);
		ret.put(NAME_VERSION_CODE, mVersionCode);
		ret.put(NAME_DOWNLOAD_URL, mDownloadUrl);
		ret.put(NAME_RECOMMEND_NUM, mRecommendNumber);
		ret.put(NAME_VOTES, mVotes);
		ret.put(NAME_GROUPID, mGroupId);
		ret.put(NAME_DATA_SOURCE, mDataSource);
		ret.put(NAME_UPDATE_TIME, mUpdateTime);
		ret.put(NAME_CHARGE, mCharge);
		ret.put(NAME_CHARGE_DESCRIPTION, mChargeDescription);
		ret.put(NAME_SECURITY_SCAN, mSecurityScan);
		ret.put(NAME_PACKAGE_SIGNATURE, mPackageSignature);
		ret.put(NAME_AD_STATUS, mAdvStatus);
		ret.put(NAME_OFFICIAL_STATUS, mOffcialStatus);
		ret.put(NAME_DOWNLOAD_NUM, mDownloadNum);
		return ret;
	}

	public void setInstStatus(int status) {
		mInstStatus = status;
	}
	
	public int getInstStatus() {
		return mInstStatus;
	}
	
	public void setTaskStatus(int status) {
		mTaskStatus = status;
	}
	
	public int getTaskStatus() {
		return mTaskStatus;
	}
	
}
