package com.appmall.market.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;

import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Constants;
import com.appmall.market.common.Encoder;
import com.appmall.market.common.Utils;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import android.util.Log;

/**
 * 妫�娴嬫洿鏂拌姹傛暟锟�?
 *  
 *
 */
public class QueryUpdatePost implements IPostData {

	private static final String PACKAGENAME = "P";
	private static final String VERSION = "V";
	private static final String VERSIONNAME = "N";
	private static final String SIGNATURE = "S";
	private static final String ISSYSTEM = "T";
	private static final String SYSTEM = "S";
	private static final String USER = "U";
//	private static final String APKCRC = "C";
	private static final String APKINCMD5 = "M";
	
	private static final String PARAM_IMEI = "imei";
	private static final String PARAM_SID = "sid";
	private static final String PARAM_IMSI = "imsi";
	private static final String PARAM_VER = "ver";
	private static final String PARAM_VERCODE = "vercode";
	private static final String PARAM_APP = "app";
	private static final String PARAM_UA = "ua";
	private static final String PARAM_SITETYPE = "sitetype";
	private static final String PARAM_OSVER = "osver";
	private static final String PARAM_SCR = "scr";
	private static final String PARAM_IAP = "iap";
	private static final String PARAM_NET = "net";
	private static final String PARAM_SIGN = "sign";
	private static final String PARAM_ITEMS = "items";
	
	private Context mAppContext;

	public QueryUpdatePost(Context context) {
		mAppContext = context.getApplicationContext();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public byte[] buildPostData() {
		String imeiEncrypt = null;
		String sidEncrypt = null;
		String imsiEncrypt = null;
		String ver = null;
		int vercode = 0;
		String ua = null;
		String osver = null;
		String scr = null;
		String iap = null;
		String net = null;
		String sign = null;
		String itemsEncrypt = null;
		
		try {
			String imei = Utils.getImei(mAppContext);
			if (!TextUtils.isEmpty(imei))
				imeiEncrypt = Base64.encodeToString(Encoder.encode(imei.getBytes()), Base64.NO_WRAP);
			
			String sid = Utils.getUid(mAppContext);
			if (!TextUtils.isEmpty(sid))
				sidEncrypt = Base64.encodeToString(Encoder.encode(sid.getBytes()), Base64.NO_WRAP);
			
			TelephonyManager telManager = (TelephonyManager) mAppContext.getSystemService(
					Context.TELEPHONY_SERVICE);
			String imsi = telManager.getSubscriberId();
			if (!TextUtils.isEmpty(imsi))
				imsiEncrypt = Base64.encodeToString(Encoder.encode(imsi.getBytes()), Base64.NO_WRAP);
			
			PackageManager pm = mAppContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mAppContext.getPackageName(), 0);
			ver = pi.versionName;
			vercode = pi.versionCode;
			
			ua = Build.MODEL;
			osver = Utils.getOSVer(mAppContext);
			
			WindowManager wm = (WindowManager) mAppContext.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			scr = display.getWidth() + "x" + display.getHeight();
			
			NetworkInfo cni = Utils.getCurrNetworkInfo(mAppContext);
			if (cni != null) {
				net = cni.getTypeName();
				iap = cni.getExtraInfo();
			}
			
			sign = CommonInvoke.getUpremindSignature(imei);
			
			String items = buildAppItems();
			itemsEncrypt = Base64.encodeToString(Encoder.encode(items.getBytes()), Base64.NO_WRAP);
		} catch (Exception e) {
			Log.d("demo", "e = "+e.getMessage());
			e.printStackTrace();
		}
		
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(PARAM_VERCODE, String.valueOf(vercode)));
		params.add(new BasicNameValuePair(PARAM_APP, Utils.getChannelId(mAppContext)));
		params.add(new BasicNameValuePair(PARAM_SITETYPE, Constants.SITETYPE));
		
		if (!TextUtils.isEmpty(imeiEncrypt))
			params.add(new BasicNameValuePair(PARAM_IMEI, imeiEncrypt));
		if (!TextUtils.isEmpty(sidEncrypt))
			params.add(new BasicNameValuePair(PARAM_SID, sidEncrypt));
		if (!TextUtils.isEmpty(imsiEncrypt))
			params.add(new BasicNameValuePair(PARAM_IMSI, imsiEncrypt));
		if (!TextUtils.isEmpty(ver))
			params.add(new BasicNameValuePair(PARAM_VER, ver));
		if (!TextUtils.isEmpty(ua))
			params.add(new BasicNameValuePair(PARAM_UA, ua));
		if (!TextUtils.isEmpty(osver))
			params.add(new BasicNameValuePair(PARAM_OSVER, osver));
		if (!TextUtils.isEmpty(scr))
			params.add(new BasicNameValuePair(PARAM_SCR, scr));
		if (!TextUtils.isEmpty(iap))
			params.add(new BasicNameValuePair(PARAM_IAP, iap));
		if (!TextUtils.isEmpty(net))
			params.add(new BasicNameValuePair(PARAM_NET, net));
		if (!TextUtils.isEmpty(sign))
			params.add(new BasicNameValuePair(PARAM_SIGN, sign));
		if (!TextUtils.isEmpty(itemsEncrypt))
			params.add(new BasicNameValuePair(PARAM_ITEMS, itemsEncrypt));
		
		return URLEncodedUtils.format(params, HTTP.UTF_8).getBytes();
	}

	private String buildAppItems() {
		Log.d("demo", "buildAppItems");
		List<LocalAppInfo> packs = DataCenter.getInstance().requestAllLocalPackage();
				
		//鏆傛椂鍘绘帀澧為噺鏇存柊閫昏緫
//		DataCenter.getInstance().getLocalApps().refreshLocalAppIncMd5();
		
		JSONArray array = new JSONArray();
		for (LocalAppInfo pack : packs) {
			try {
				JSONObject obj = new JSONObject();
				obj.put(PACKAGENAME, pack.mPackageName);
				obj.put(VERSION, pack.mVersionCode);
				obj.put(VERSIONNAME, pack.mVersion);
				obj.put(SIGNATURE, pack.mSignature);
				obj.put(ISSYSTEM, pack.mIsSystem ? SYSTEM : USER);
//				if(pack.mPackageName.contains("sina")) {
//					Log.d("demo", "pack.mPackageDir = "+pack.mPackageDir);
//				}
//				Log.d("demo", "pack.mPackageName = "+pack.mPackageName);
//				Log.d("demo", "pack.mMd5 = "+pack.mMd5);
				if(!TextUtils.isEmpty(pack.mMd5)) {
					obj.put(APKINCMD5, pack.mMd5);	
				}
				array.put(obj);
			} catch (JSONException e) { }
		}
		
		return array.toString();
	}
	
}
