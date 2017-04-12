package com.appmall.market.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.appmall.market.ApplicationImpl;
import com.appmall.market.common.Constants;
import com.appmall.market.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/**
 * 扫描并缓存本地已安装的软件
 *  
 *
 */
public class LocalApps {

	public static final int STATUS_NOT_INSTALLED = 0;
	public static final int STATUS_INSTALLED = 1;
	public static final int STATUS_INSTALLED_OLD_VERSION = 2;
	
//    private static String CLASSES_DEX = "classes.dex";
//    private static String RESOURCE_ARSC = "resources.arsc";
    private static String RESOURCE_MF = "MANIFEST.MF";
	
	public static final List<String> WHITE_LIST = Arrays.asList(new String[] {
		Constants.AUTHORITY
	});
	
	/** 本地所有软件 */
	private Map<String, LocalAppInfo> mApps;
	/** 本地已安装的第三方软件，包含系统更新 */
	private List<LocalAppInfo> mThirdApps;
	
	public static class LocalAppInfo {
		public CharSequence mAppLabel;
		public long mAppSize;
		public boolean mIsSystem;
		public String mSignature;
		public String mPackageName;
		public int mVersionCode;
		public long mLastUpdateTime;
		public String mVersion;
		
		//增量更新所需字段
		public String mPackageDir;
		public String mCrcString;
		public String mMd5;

		@Override
		public String toString() {
			return "LocalAppInfo{" +
					"mAppLabel=" + mAppLabel +
					", mAppSize=" + mAppSize +
					", mIsSystem=" + mIsSystem +
					", mSignature='" + mSignature + '\'' +
					", mPackageName='" + mPackageName + '\'' +
					", mVersionCode=" + mVersionCode +
					", mLastUpdateTime=" + mLastUpdateTime +
					", mVersion='" + mVersion + '\'' +
					", mPackageDir='" + mPackageDir + '\'' +
					", mCrcString='" + mCrcString + '\'' +
					", mMd5='" + mMd5 + '\'' +
					'}';
		}
	}
	
//	//获取apk文件中的crc
//    private String getApkCrc(String apkPath) {
//    	String crcString = null;
//    	try {
//            long crcDex = -1;
//            long crcResource = -1;
//            ZipFile zfile=new ZipFile(new File(apkPath));
//            ZipEntry dex = zfile.getEntry(CLASSES_DEX);
//            ZipEntry resouce = zfile.getEntry(RESOURCE_ARSC);
//            if(dex != null)
//            	crcDex = dex.getCrc();
//            if(resouce != null)
//            	crcResource = resouce.getCrc();
//            zfile.close();
//            if(crcDex >0 && crcResource >0) {
//            	crcString = crcDex + "|" + crcResource;
//            }      
////            Log.e("demo", "crcString = "+crcString);
//    	} catch(Exception e) {
//    		e.printStackTrace();
//    	}
//    	return crcString;
//    }
    
    public String getFileMD5(InputStream is) {
  	  MessageDigest digest = null;
  	  byte buffer[] = new byte[1024];
  	  int len;
  	  try {
	  	   digest = MessageDigest.getInstance("MD5");
	  	   while ((len = is.read(buffer, 0, 1024)) != -1) {
	  	    digest.update(buffer, 0, len);
	  	   }
  	  } catch (Exception e) {
	  	   e.printStackTrace();
	  	   return null;
  	  }
  	  BigInteger bigInt = new BigInteger(1, digest.digest());
  	  return bigInt.toString(16);
    }
    
    public String getApkIncMd5(String apkPath) {
    	String md5String = null;
    	try {
    		InputStream inputStream;
            ZipFile zfile=new ZipFile(new File(apkPath));
            Enumeration entryEnum = zfile.entries();
            ZipEntry mf = null;
            if (null != entryEnum) {
            	ZipEntry zipEntry = null;
            	while (entryEnum.hasMoreElements()) {
            		zipEntry = (ZipEntry) entryEnum.nextElement();
            		if(zipEntry.getName().contains(RESOURCE_MF)) {
            			mf = zipEntry;
            			break;
            		}
            	}
            }
            if(mf != null) {
            	inputStream=zfile.getInputStream(mf);
            	md5String = getFileMD5(inputStream);
            }
            zfile.close();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return md5String;
    }
    
	final private void saveLocalAppsParam(List<LocalAppInfo> listData) {
		if (listData == null)
			return;
		
		SharedPreferences pref = ApplicationImpl.getSelfApplicationContext().getSharedPreferences(
				Constants.PREF_LOCAL_APP_PARAM, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.clear();
		
		try {
			for(LocalAppInfo appInfo : listData) {
				String packageName = appInfo.mPackageName;
				LocalAppsParam appParam = new LocalAppsParam();
				appParam.mPackageName = appInfo.mPackageName;
				appParam.mMd5 = appInfo.mMd5;
				appParam.mSize = appInfo.mAppSize;
				editor.putString(packageName, appParam.generateJSONObject().toString());
			}
		} catch (JSONException e) { }
		editor.commit();
	}
	
	final private LocalAppsParam loadLocalAppsParam(String packageName) {
		SharedPreferences pref = ApplicationImpl.getSelfApplicationContext().getSharedPreferences(
				Constants.PREF_LOCAL_APP_PARAM, Context.MODE_PRIVATE);
		String jsonString = pref.getString(packageName, null);
		if(jsonString == null) return null;
		try {
			if(pref != null) {
				LocalAppsParam appParam = new LocalAppsParam();
				appParam.readFromJSON(new JSONObject(jsonString));
				return appParam;
			}	
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    private  boolean mRefreshLocalAppIncMd5Flag = false;
    public boolean isLocalAppRefresh() {
    	return mRefreshLocalAppIncMd5Flag;
    }
    public void setLocalAppRefresh(boolean bRefresh) {
    	mRefreshLocalAppIncMd5Flag = bRefresh;
    }
    public synchronized void refreshLocalAppIncMd5() {
    	Log.d("demo", "refreshLocalAppCrc start mRefreshLocalAppIncMd5Flag = "+mRefreshLocalAppIncMd5Flag);
    	if(!mRefreshLocalAppIncMd5Flag) {
    		mRefreshLocalAppIncMd5Flag = true;
    	  	
	    	long time1 = System.currentTimeMillis();    	
	    	boolean bSave = false;
			for(LocalAppInfo appInfo : mThirdApps) {
				if(appInfo != null) {
					String md5 = null;
					LocalAppsParam appParam = loadLocalAppsParam(appInfo.mPackageName);
					if(appParam != null && appParam.mMd5 != null && appParam.mSize == appInfo.mAppSize)
						md5 = appParam.mMd5;
					if(md5 ==null) {
						md5 = getApkIncMd5(appInfo.mPackageDir);
						bSave = true;
					}
					appInfo.mMd5 = md5;
				}
//				Log.d("demo", "appInfo.mMd5 = " + appInfo.mMd5);
			}
			if(bSave)
				saveLocalAppsParam(mThirdApps);
			
			long time2 = System.currentTimeMillis();
			Log.d("demo", "refreshLocalAppCrc end time = " + (time2-time1));
	    }
    }
	
	/**
	 * 扫描本地软件
	 */
	public synchronized void scan(Context context) {
		
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> instPkgs = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
		if (instPkgs == null)
			return;
		
		List<String> whiteList = new ArrayList<String>(WHITE_LIST);
		whiteList.add(context.getPackageName());
		
		mApps = new HashMap<String, LocalAppInfo>();
		mThirdApps = new ArrayList<LocalApps.LocalAppInfo>();
		for (PackageInfo info : instPkgs) {
			if (info == null || info.applicationInfo == null || info.applicationInfo.sourceDir == null)
				continue;
			if (TextUtils.isEmpty(info.packageName))
				continue;
			if (whiteList.contains(info.packageName))
				continue;
			
			LocalAppInfo localApp = new LocalAppInfo();
			localApp.mPackageName = info.packageName;
			localApp.mVersionCode = info.versionCode;
			localApp.mVersion = info.versionName;
			if (Build.VERSION.SDK_INT >= 9) {
				localApp.mLastUpdateTime = getLastUpdateTime(info);
			}
			localApp.mAppLabel = info.applicationInfo.loadLabel(pm);
			localApp.mAppSize = new File(info.applicationInfo.sourceDir).length();
			localApp.mPackageDir = info.applicationInfo.sourceDir;					
			mApps.put(localApp.mPackageName, localApp);
			localApp.mIsSystem = true;
			// 载入第三方应用列表，即非系统应用且非白名单应用的列表
			if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					|| (info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				localApp.mIsSystem = false;
				mThirdApps.add(localApp);
			}
			
			// 计算签名
			if (info.signatures != null && info.signatures.length > 0) {
				Locale locale = Locale.getDefault();
				if (info.signatures.length == 1 && info.signatures[0] != null) {
					// 单签名
					byte data[] = info.signatures[0].toByteArray();
					data = Utils.getMd5(data);
					localApp.mSignature = Utils.toHexString(data).toLowerCase(locale);
				} else {
					// 多签名
					String signsInPkg[] = new String[info.signatures.length];
					for (int i = 0; i < info.signatures.length; ++i) {
						if (info.signatures[i] != null) {
							byte data[] = info.signatures[i].toByteArray();
							data = Utils.getMd5(data);
							signsInPkg[i] = Utils.toHexString(data).toLowerCase(locale);
						}
					}
					
					// 签名排序
					Arrays.sort(signsInPkg);
					
					// 排序后签名MD5值再做MD5运算，得最后结果
					StringBuilder builder = new StringBuilder();
					for (String signInPkg : signsInPkg) {
						builder.append(signInPkg);
					}
					byte data[] = builder.toString().getBytes();
					data = Utils.getMd5(data);
					localApp.mSignature = Utils.toHexString(data).toLowerCase(locale);
				}
			}
		}
		
		instPkgs.clear();
		System.gc();
		
		if (Build.VERSION.SDK_INT >= 9) {
			Collections.sort(mThirdApps, new PackageTimeComparator());
		} else {
			try {
				Collator collator = Collator.getInstance(Locale.CHINA);
				Collections.sort(mThirdApps, new PackageNameComparator(collator));
			} catch (Exception e) {
				// 系统中不一定存在中文的Collator。避免崩溃，按默认方式排序
			}
		}
		
		setLocalAppRefresh(false);
		Log.d("maowenping",mThirdApps.toString());
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private long getLastUpdateTime(PackageInfo info) {
		return info.lastUpdateTime;
	}
	
	public synchronized List<LocalAppInfo> getThirdPackages() {
		return mThirdApps;
	}
	
	public synchronized List<LocalAppInfo> getPackages() {
		return new ArrayList<LocalApps.LocalAppInfo>(mApps.values());
	}
	
	public synchronized LocalAppInfo getLocalPackage(String packageName) {
		if (mApps == null || TextUtils.isEmpty(packageName))
			return null;
		return mApps.get(packageName);
	}
	
	public synchronized int getPackageInstallStatus(String packageName, int versionCode, String versionName) {
		if (mApps == null || TextUtils.isEmpty(packageName))
			return STATUS_NOT_INSTALLED;
		
		if (versionCode < 0)
			versionCode = 0;
		
		LocalAppInfo local = mApps.get(packageName);
		if (local == null)
			return STATUS_NOT_INSTALLED;
		
		if (local.mVersionCode >= versionCode) {
			if(local.mVersionCode == versionCode) {
				int value = compareVersion(local.mVersion, versionName);
				if(value <0)
					return STATUS_INSTALLED_OLD_VERSION;
			}
			return STATUS_INSTALLED;
		} else {
			return STATUS_INSTALLED_OLD_VERSION;
		}
	}
	
	private static int compareVersion(String baseStr, String targetStr) {
        // 过滤特殊字符
        String regEx = "[^0-9.]";
        Pattern pattern = Pattern.compile(regEx);

        Matcher matcher = pattern.matcher(baseStr);
        baseStr = matcher.replaceAll("").trim();

        matcher = pattern.matcher(targetStr);
        targetStr = matcher.replaceAll("").trim();


        return baseStr.compareTo(targetStr);
    }
	
	public synchronized String getInstallPackageSignature(String packageName) {
		if (mApps == null || TextUtils.isEmpty(packageName))
			return null;
		String signature = null;
		LocalAppInfo localApp = getLocalPackage(packageName);
		if(localApp != null)
			signature = localApp.mSignature;
		return signature;
	}
	
	/**
	 * 按照安装包最后更新时间排序
	 *  
	 *
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class PackageTimeComparator implements Comparator<LocalAppInfo> {
		@Override
		public int compare(LocalAppInfo lhs, LocalAppInfo rhs) {
			if (lhs == null)
				return -1;
			if (rhs == null)
				return 1;
			
			if (lhs.mLastUpdateTime == rhs.mLastUpdateTime)
				return 0;
			else
				return lhs.mLastUpdateTime - rhs.mLastUpdateTime > 0 ? -1 : 1;
		}
	}
	
	/**
	 * 按照安装包标签进行排序
	 *  
	 *
	 */
	private static class PackageNameComparator implements Comparator<LocalAppInfo> {
		
		private Collator mCollator;

		public PackageNameComparator(Collator collator) {
			mCollator = collator;
		}
		
		@Override
		public int compare(LocalAppInfo lhs, LocalAppInfo rhs) {
			if (lhs == null || lhs.mAppLabel == null)
				return -1;
			if (rhs == null || rhs.mAppLabel == null)
				return 1;
			
			String lhsLabelString = (lhs.mAppLabel == null ? "" : lhs.mAppLabel.toString());
			String rhsLabelString = (rhs.mAppLabel == null ? "" : rhs.mAppLabel.toString());
			if (mCollator == null) {
				return lhsLabelString.compareTo(rhsLabelString);
			} else {
				return mCollator.compare(lhsLabelString, rhsLabelString);
			}
		}
	}
	
}
