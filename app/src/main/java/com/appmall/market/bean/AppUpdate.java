package com.appmall.market.bean;

import java.text.SimpleDateFormat;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import com.appmall.market.data.IDataBase;

/**
 * 应用更新信息
 *  
 *
 */
public class AppUpdate implements IDataBase {

	/** 用于移除软件标题后无用版本号的匹配正则表达式 */
	private static final String UNUSED_INFO_REGEXP = "[vV][0-9].*[(（][aA].*";
	
	private static final String JSON_PACKAGE_NAME = "packagename";
	private static final String JSON_VERSION = "version";
	private static final String JSON_VERSION_NAME = "versionname";
	private static final String JSON_SIZE = "size";
	private static final String JSON_TITLE = "title";
	private static final String JSON_UP_DATE = "upDate";
	private static final String JSON_DOWN_URL = "downUrl";
	private static final String JSON_LOCAL_VERSION = "local_version";
	private static final String JSON_UPDATE_INFO = "updateinfo";
	private static final String NAME_PKG_ID = "pkg_id";
	private static final String NAME_IS_PATCH = "isPatch";
	private static final String PATCH_SIZE = "patchSize";
	private static final String PATCH_URL = "patchUrl";
	
	/** 更新软件包名 */
	public String mPackageName;
	
	/** 更新软件名称 */
	public String mLabel;
	
	/** 版本号 */
	public int mVersionCode;
	
	/** 版本 */
	public String mVersion;
	
	/** 文件大小 */
	public long mFileSize;
	
	/** 更新时间 */
	public long mUpdateTime;
	
	/** 下载地址 */
	public String mDownloadPath;
	
	/** 本地版本 */
	public String mLocalVersion;
	
	/** 更新说明 */
	public String mUpdateInfo;
	/** 渠道ID*/
	public int mChannelId;
	
	//增量更新参数
	public boolean mHasPatch;
	public long mPatchSize;
	public String mPatchUrl;

	
	/** 标记更新的下载状态 */
	private int mTaskStatus;
	/** 标记更新的安装状态 */
	private int mInstStatus;
	
	public boolean isImportantUpdate() {
		String newVersionFirstWord = mVersion.substring(0,1);
		String oldVersionFirstWord = mLocalVersion.substring(0,1);
		return !newVersionFirstWord.equalsIgnoreCase(oldVersionFirstWord);
	}

	@Override
	public void readFromJSON(JSONObject jsonObj) throws JSONException {
		mPackageName = jsonObj.optString(JSON_PACKAGE_NAME);
		mVersionCode = jsonObj.optInt(JSON_VERSION);
		mVersion = jsonObj.optString(JSON_VERSION_NAME);
		mFileSize = jsonObj.optLong(JSON_SIZE);
		mLabel = jsonObj.optString(JSON_TITLE);
		mUpdateInfo = jsonObj.optString(JSON_UPDATE_INFO);
		mChannelId = jsonObj.optInt(NAME_PKG_ID);
		mHasPatch = jsonObj.optBoolean(NAME_IS_PATCH);
		mPatchSize = jsonObj.getLong(PATCH_SIZE);
		mPatchUrl = jsonObj.optString(PATCH_URL);
		
		//容错处理，防止patch包比原始包大的情况
		if(mPatchSize >mFileSize && mHasPatch) {
			mHasPatch = false;
			mPatchSize = 0;
		}
		
		try {
			mUpdateTime = jsonObj.getLong(JSON_UP_DATE);
		} catch (JSONException e) {
			// 接口可能出现直接传输日期字符串的方式，做一些异常处理
			mUpdateTime = tryParseTimeString(jsonObj.getString(JSON_UP_DATE));
		}
		mDownloadPath = jsonObj.optString(JSON_DOWN_URL);
		mLocalVersion = jsonObj.optString(JSON_LOCAL_VERSION);
		
		mLabel = filterLabelVersion(mLabel);
		
		if (TextUtils.isEmpty(mPackageName) || TextUtils.isEmpty(mDownloadPath))
			throw new JSONException("");
	}

	@Override
	public JSONObject generateJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(JSON_PACKAGE_NAME, mPackageName);
		ret.put(JSON_VERSION, mVersionCode);
		ret.put(JSON_VERSION_NAME, mVersion);
		ret.put(JSON_SIZE, mFileSize);
		ret.put(JSON_TITLE, mLabel);
		ret.put(JSON_UP_DATE, mUpdateTime);
		ret.put(JSON_DOWN_URL, mDownloadPath);
		ret.put(JSON_LOCAL_VERSION, mLocalVersion);
		ret.put(JSON_UPDATE_INFO, mUpdateInfo);
		ret.put(NAME_PKG_ID, mChannelId);
		ret.put(NAME_IS_PATCH, mHasPatch);
		ret.put(PATCH_SIZE, mPatchSize);
		ret.put(PATCH_URL, mPatchUrl);
		return ret;
	}
	
	final private long tryParseTimeString(String string) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		try {
			return format.parse(string).getTime();
		} catch (Exception e) { }
		
		return 0;
	}
	
	final public static String filterLabelVersion(String label) {
		if (TextUtils.isEmpty(label))
			return "";
		
		return label.replaceAll(UNUSED_INFO_REGEXP, "");
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AppUpdate))
			return false;
		
		AppUpdate another = (AppUpdate) o;
		if (mPackageName == null) {
			return another.mPackageName == null;
		} else {
			return mPackageName.equals(mPackageName);
		}
	}

	@Override
	public int hashCode() {
		return mPackageName == null ? 0 : mPackageName.hashCode();
	}

	public void setTaskStatus(int status) {
		mTaskStatus = status;
	}
	
	public int getTaskStatus() {
		return mTaskStatus;
	}

	public void setInstStatus(int status) {
		mInstStatus = status;
	}
	
	public int getInstStatus() {
		return mInstStatus;
	}
	
}
