package com.appmall.market.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.appmall.market.ApplicationImpl;
import com.appmall.market.R;
import com.appmall.market.activity.CategoryDetailActivity;
import com.appmall.market.activity.AppDetailActivity;
import com.appmall.market.activity.TermActivity;
import com.appmall.market.activity.TopicDetailActivity;
import com.appmall.market.bean.App;
import com.appmall.market.common.ShellUtils.CommandResult;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.download.DownloadControl.SilenceInstall;

import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.Spannable;
import android.widget.TextView;
import android.text.style.ForegroundColorSpan;
import com.appmall.market.bean.Advert;
import com.appmall.market.pulltorefresh.library.ILoadingLayout;
import com.appmall.market.widget.DownStatusButton;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URLEncoder;
import android.graphics.Rect;
import java.math.BigInteger;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import java.util.List;
import cn.uc.appmall.apk.merge.ZipMerge;

public class Utils {
	
	private static final String DATA_SOURCE_SPLIT_CHAR = "\\|";
	private static final String WIFI = "wifi";
	
	public static String getSizeString(long size) {
	    if(size <= 0)
	    	return "0B";
	    
	    if (size < 100 * 1024) {
	    	return "0.1M";
	    } else {
	    	return new DecimalFormat("#,##0.#").format(size / 1024f / 1024) + "M";
	    }
	}
	
	public static String getTotalUpdateSizeString(long size) {
		String text = "全部更新";
	    if(size <= 0)
	    	return text+"0B";
	    
	    if (size < 1024 * 1024) {
	    	return text+new DecimalFormat("#,##0.#").format(size / 1024f) + "K";
	    } else if(size < 1024 * 1024 *1024){
	    	return text+new DecimalFormat("#,##0.##").format(size / 1024f / 1024) + "M";
	    } else {
	    	return text+new DecimalFormat("#,##0.##").format(size / 1024f / 1024 /1024) + "G";
	    }
	}
	
	public static void scaleClickRect(final View buttonView) {
		if(buttonView == null) return;
		if(View.class.isInstance(buttonView.getParent())) {
			final View parent = (View) buttonView.getParent();
			if(parent != null) {
				parent.post(new Runnable() {
					
					@Override
					public void run() {
						Rect rect = new Rect();
						buttonView.getHitRect(rect);
						rect.top -=60;
						rect.bottom +=60;
						rect.right +=60;
						parent.setTouchDelegate(new TouchDelegate(rect, buttonView));
					}
				});
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static Bitmap takeScreenShot(Activity activity) { 
		//View鏄綘闇�瑕佹埅鍥剧殑View 
		View view = activity.getWindow().getDecorView(); 
		view.setDrawingCacheEnabled(true); 
		view.buildDrawingCache(); 
		Bitmap b1 = view.getDrawingCache(); 
		//鑾峰彇鐘舵�佹爮楂樺害 
		Rect frame = new Rect(); 
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame); 
		int statusBarHeight = frame.top; 
		System.out.println(statusBarHeight); 
		//鑾峰彇灞忓箷闀垮拰楂� 
		int width = activity.getWindowManager().getDefaultDisplay().getWidth(); 
		int height = activity.getWindowManager().getDefaultDisplay().getHeight(); 
		//鍘绘帀鏍囬鏍� 
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight); 
		view.destroyDrawingCache(); 
		return b; 
	}
	
    public static Bitmap drawableToBitmap(Drawable drawable) {
    	BitmapDrawable bd = (BitmapDrawable) drawable;
    	Bitmap bm = bd.getBitmap();
    	return bm;
    }
    
    @SuppressWarnings("deprecation")
    public static Drawable bitmapToDrawable(Bitmap bm) {
    	BitmapDrawable bd=new BitmapDrawable(bm);
    	return bd;
    }
    
    public static void clearButtonProgress(DownStatusButton downButton) {
    	if(downButton != null) {
        	downButton.setProgress(0);
        	downButton.setProgressVisible(false);
    	}
    }
    
    @Deprecated
    public static void handleButtonProgress(DownStatusButton downButton, DownloadTask task) {
    	if(downButton != null) {
			Resources res = ApplicationImpl.getSelfApplicationContext().getResources();
			int status = task == null ? TaskStatus.STATUS_UNKNOWN : task.mStatus;
			String statusText = null;
			int statusTextColor = 0;
			Drawable background = null;
			Drawable progressDrawable = null;
			int progress = (int)(task.mTransfered * 100f / task.mTotal);
			switch (status) {
			case TaskStatus.STATUS_DOWNLOAD:
				progressDrawable = null;
				statusText = res.getString(R.string.install);
				statusTextColor = res.getColor(R.color.green_bg_text_color);
				background = res.getDrawable(R.drawable.btn_green_bg);
				break;
			case TaskStatus.STATUS_INSTALLING:
				statusText = res.getString(R.string.installing);
				statusTextColor = res.getColor(R.color.green_bg_text_color);
				background = res.getDrawable(R.drawable.btn_green_bg);
				break;
			case TaskStatus.STATUS_DOWNLOADING:
			case TaskStatus.STATUS_WAIT:
				statusText = progress + "%";
				statusTextColor = res.getColor(R.color.green_bg_text_color);
				background = res.getDrawable(R.drawable.btn_green_bg);
				progressDrawable = res.getDrawable(R.drawable.btn_green_progress_bg);
				break;
			case TaskStatus.STATUS_FAILED:
			case TaskStatus.STATUS_PAUSE:
				statusText = res.getString(R.string.pausing);
				statusTextColor = res.getColor(R.color.gray_bg_text_color);
				background = res.getDrawable(R.drawable.btn_gray_bg);
				progressDrawable = res.getDrawable(R.drawable.btn_gray_progress_bg);
				break;
			case TaskStatus.STATUS_UNKNOWN:
			default:
				progressDrawable = null;
				break;
			}
			
			downButton.setText(statusText);
			downButton.setTextColor(statusTextColor);
			downButton.setProgress(progress);
			downButton.setProgressVisible(progressDrawable != null);
			downButton.setBackgroundDrawable(background);
			downButton.setProgressDrawable(progressDrawable);
		}
    }
    
    public static void handleButtonProgress(View parentView, int viewid, DownloadTask task) {
    	if(parentView == null) return;
		View view = parentView.findViewWithTag(task.mPackageName);
		if (view != null && (view instanceof ImageView)) {
			DownStatusButton downButton = (DownStatusButton)((View)(view.getParent())).findViewById(viewid);
			if(downButton != null) {
				handleButtonProgress(downButton, task);
			}
		}
    }
	
	public static void setupPTRLoadingProxy(Resources res, ILoadingLayout loadingLayoutProxy) {
		loadingLayoutProxy.setPullLabel(res.getString(R.string.ptr_pull_to_refresh));
		loadingLayoutProxy.setRefreshingLabel(res.getString(R.string.ptr_refreshing));
		loadingLayoutProxy.setReleaseLabel(res.getString(R.string.ptr_release_to_refresh));
		loadingLayoutProxy.setProgressDrawable(res.getDrawable(R.drawable.ptr_refreshing_drawable));
	}

	private final static int kSystemRootStateUnknow = -1;  
    private final static int kSystemRootStateDisable = 0;  
    private final static int kSystemRootStateEnable = 1;  
    private static int systemRootState = kSystemRootStateUnknow;
	public static boolean isPhoneRoot() {
        if (systemRootState == kSystemRootStateEnable) {  
            return true;  
        } else if (systemRootState == kSystemRootStateDisable) {  
            return false;  
        }  
        File f = null;  
        final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/",  
                "/system/sbin/", "/sbin/", "/vendor/bin/" };  
        try {  
            for (int i = 0; i < kSuSearchPaths.length; i++) {  
                f = new File(kSuSearchPaths[i] + "su");  
                if (f != null && f.exists()) {  
                    systemRootState = kSystemRootStateEnable;  
                    return true;  
                }  
            }  
        } catch (Exception e) {  
        }  
        systemRootState = kSystemRootStateDisable;  
        return false;  
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void AsyncTaskExecute(AsyncTask<Object, Object, Object> task, Object... params) {
		try {
			if (Build.VERSION.SDK_INT >= 11) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
			} else {
				task.execute(params);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 璋冪敤绯荤粺鍗歌浇鐣岄潰
	 * @param context Context
	 * @param packageName 寰呭嵏杞界殑鐩爣绋嬪簭packageName
	 */
	public static void reqSystemUninstall(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName))
			return;
		
		Uri packageURI = Uri.parse("package:" + packageName);      
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		if (!(context instanceof Activity))
			uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
	}
	
	public static void doTaskInstall(Context context, DownloadTask task) {
		if(task == null) return;
		LocalAppInfo appInfo = DataCenter.getInstance().getLocalApps().getLocalPackage(task.mPackageName);
		if(task.mIsSignDiff && appInfo != null) {
			DataCenter.getInstance().reportDownloadEvent(DataCenter.MSG_INSTALL_SIGNATURE_NOTIFY_EVENT, task.mPackageName);
			return;
		}
		//濡傛灉寮�鍚潤榛樺畨瑁咃紝浼樺厛闈欓粯瀹夎
		if (AppSettings.isRootInstall(context) && Utils.isPhoneRoot()) {
			task.mStatus = TaskStatus.STATUS_INSTALLING;
			if(DownloadService.getDownloadControl() != null)
				DownloadService.getDownloadControl().onTaskStatusChanged(task);
			Utils.AsyncTaskExecute(new SilenceInstall(context), task);
		} else {
			reqSystemInstall(context, task.mLocalPath);
		}
	}
	
	public static boolean isTopActivity(Activity activity)
	{
	     ActivityManager manager = (ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE) ;
	     List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
	         
	     if(runningTaskInfos != null)
	       	return runningTaskInfos.get(0).topActivity.getClassName().equalsIgnoreCase(activity.getComponentName().getClassName());
	     else
	    	 return false ;
	}
	
	/**
	 * 璋冪敤绯荤粺瀹夎鐣岄潰
	 * @param context Context
	 * @param packageName 寰呭畨瑁呯殑鐩爣绋嬪簭packagePath
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void reqSystemInstall(Context context, String packagePath) {
		if (TextUtils.isEmpty(packagePath))
			return;
					
		File targetFile = new File(packagePath);
		if (!targetFile.exists() || targetFile.isDirectory())
			return;
					
		try {
			Uri packageURI = Uri.fromFile(targetFile);
			Intent intent = null;
			if (Build.VERSION.SDK_INT >= 14) {
				intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
				intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
				intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
				intent.setData(packageURI);
			} else {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(packageURI, "application/vnd.android.package-archive");
			}
			if(context instanceof Activity)
				((Activity) context).startActivityForResult(intent, 0);
			else {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (ActivityNotFoundException anfe) { }
	}
	
	public static void reqSystemOpen(Context context, String pkgName) {
		PackageManager pm = context.getPackageManager();
		try {
			Intent intent = pm.getLaunchIntentForPackage(pkgName);
			if (intent != null) {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (Exception e) { }
	}
	
	/**
	 * 璁＄畻涓�娈垫暟鎹殑md5鍊�
	 */
	public static byte[] getMd5(byte[] data) {
		MessageDigest digester = null;
		byte[] md5 = null;
		try {
			digester = MessageDigest.getInstance("MD5");
			digester.update(data, 0, data.length);
			md5 = digester.digest();
		} catch (NoSuchAlgorithmException e) { }
		return md5;
	}
	
	public static String getFileMD5(File file) {
		  if (!file.isFile()) {
		   return null;
		  }
		  MessageDigest digest = null;
		  FileInputStream in = null;
		  byte buffer[] = new byte[1024];
		  int len;
		  try {
		   digest = MessageDigest.getInstance("MD5");
		   in = new FileInputStream(file);
		   while ((len = in.read(buffer, 0, 1024)) != -1) {
		    digest.update(buffer, 0, len);
		   }
		   in.close();
		  } catch (Exception e) {
		   e.printStackTrace();
		   return null;
		  }
		  BigInteger bigInt = new BigInteger(1, digest.digest());
		  return bigInt.toString(16);
	}
	
	/** 鍗佸叚杩涘埗瀛楃 */
	private final static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F' };
	
	/**
	 * 灏嗕竴娈垫暟鎹娇鐢ㄥ崄鍏繘鍒跺瓧绗︿覆琛ㄧず
	 */
    public static String toHexString(byte[] array) 
    {
    	int length = array.length;
    	char[] buf = new char[length * 2];

    	int bufIndex = 0;
    	for (int i = 0 ; i < length; i++)
    	{
    		byte b = array[i];
    		buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
    		buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
    	}

    	return new String(buf);
    }
    
    /**
	 * 鑾峰彇鎵嬫満鐨処MEI鍙�
	 */
	public static String getImei(Context context) {
		String imei = null;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
		if (imei == null) {
			imei = "";
		}
		return imei;
	}

	public static void openPackage(Context context, String packageName) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		if (intent == null)
			return;
		if (!(context instanceof Activity)) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}
	public static final String EXTRA_DETAIL_TITLE = "title";
	public static final String EXTRA_DETAIL_ICONURL = "iconurl";
	public static final String EXTRA_DETAIL_STARLEVEL = "starlevel";
	public static final String EXTRA_DETAIL_DESC = "desc";
	public static final String EXTRA_DETAIL_UPDATETIME = "updatetime";
	public static void jumpAppDetailActivity(Context context, App app) {
		if (app == null || TextUtils.isEmpty(app.mDataSource))
			return;
		
		Intent detailIntent = new Intent(context, AppDetailActivity.class);
		detailIntent.putExtra(AppDetailActivity.EXTRA_DETAIL_TYPE, AppDetailActivity.DETAIL_DATA_TYPE_APP);
		detailIntent.putExtra(AppDetailActivity.EXTRA_DETAIL_APPINFO, app);
		context.startActivity(detailIntent);
	}
	
	public static void jumpSystemAppManageActivity(Context context) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
		if (!(context instanceof Activity)) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) { }
	}
	
	public static void jumpDetailActivity(Context context, Advert advert) {
		if(advert.mDataTarget == IDataConstant.TARGET_APP_DETAIL) {
			Intent intent = new Intent(context, AppDetailActivity.class);
			intent.putExtra(AppDetailActivity.EXTRA_DETAIL_TYPE, AppDetailActivity.DETAIL_DATA_TYPE_ADVERT);
			intent.putExtra(AppDetailActivity.EXTRA_DETAIL_ADVERTINFO, advert);
			context.startActivity(intent);
		} else if(advert.mDataTarget == IDataConstant.TARGET_OPEN_URL) {
			if(advert.mDataSource != null) {
				Intent intent = new Intent(context, TermActivity.class);
				intent.putExtra(TermActivity.KEY_OPEN_URL, generateJumpUrl(context, advert.mDataSource));
				context.startActivity(intent);
			}
		}
		else {
			int target = advert.mDataTarget;
			String[] source = new String[] { advert.mDataSource };
			String title = advert.mTitle;
			Utils.jumpDetailActivity(context, target, source, title);
		}
	}
	
	private static String generateJumpUrl(Context context, String url) {
		String jumpUrl = url;
		String sid = Base64.encodeToString(Encoder.encode(getUid(context).getBytes()), Base64.NO_WRAP);
		try {
			sid = URLEncoder.encode(sid,"utf8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(jumpUrl.contains("?"))
			jumpUrl += "&sid="+ sid;
		else
			jumpUrl += "?sid=" + sid;
		return jumpUrl;
	}
	
	
	public static void jumpDetailActivity(Context context, int target, String[] source, String title) {
		if (source == null)
			return;
		
		Intent intent = null;
		switch (target) {
//		case IDataConstant.TARGET_APP_DETAIL:
//			if (source.length < 1 || TextUtils.isEmpty(source[0]))
//				return;
//			intent = new Intent(context, AppDetailActivity.class);
//			intent.putExtra(AppDetailActivity.EXTRA_DETAIL_URL, source[0]);
//			break;
		case IDataConstant.TARGET_TOPIC_DETAIL:
			if (source.length < 1 || TextUtils.isEmpty(source[0]))
				return;
			intent = new Intent(context, TopicDetailActivity.class);
			intent.putExtra(TopicDetailActivity.EXTRA_DETAIL_URL, source[0]);
			intent.putExtra(TopicDetailActivity.EXTRA_DETAIL_TITLE, title);
			break;
		case IDataConstant.TARGET_APP_CATEGORY_DETAIL:
		case IDataConstant.TARGET_GAME_CATEGORY_DETAIL:
			String rankUrl = null;
			String newProdUrl = null;
			
			if (source.length == 1 && !TextUtils.isEmpty(source[0])) {
				String[] urls = source[0].split(DATA_SOURCE_SPLIT_CHAR);
				if (urls.length >= 2) {
					rankUrl = urls[0];
					newProdUrl = urls[1];
				}
			} else if (source.length >= 2) {
				rankUrl = source[0];
				newProdUrl = source[1];
			}
			
			if (TextUtils.isEmpty(rankUrl) || TextUtils.isEmpty(newProdUrl))
				return;
			intent = new Intent(context, CategoryDetailActivity.class);
			intent.putExtra(CategoryDetailActivity.EXTRA_TITLE, title);
			intent.putExtra(CategoryDetailActivity.EXTRA_RANK_URL, rankUrl);
			intent.putExtra(CategoryDetailActivity.EXTRA_NEW_PROD_URL, newProdUrl);
			break;
		default:
			return;
		}
		
		context.startActivity(intent);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			String typeName = info.getTypeName();
			if (!TextUtils.isEmpty(typeName)) {
				return typeName.toLowerCase(Locale.getDefault()).equals(WIFI) && info.isAvailable();
			}
		}
		
		return false;
	}

	public static void jumpNetworkSetting(Context context) {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		if (!(context instanceof Activity)) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) { }
	}

	public static boolean reqSilenceInstall(Context context, String filepath) {
		if (TextUtils.isEmpty(filepath))
			return false;
		File file = new File(filepath);
		CommandResult result = ShellUtils.execSuperUserCommand("pm install -r " + file.getAbsolutePath());
		return result.result == 0 && "Success".equalsIgnoreCase(result.successMsg);
	}

	/**
	 * 鑾峰彇鎿嶄綔绯荤粺鐗堟湰
	 */
	public static String getOSVer(Context context) {
		String ret = Build.VERSION.RELEASE;
		if (ret == null)
			ret = "";
		return ret;
	}

	public static NetworkInfo getCurrNetworkInfo(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo();
	}
	
	private static final String KEY_BUSI = "X-BUSI";
	private static final String KEY_VNAME = "X-VNAME";
	private static final String KEY_VCODE = "X-VCODE";
	private static final String KEY_DEVICE = "X-DEVICE";
	private static final String KEY_TIME = "X-TIME";
	private static final String KEY_IMSI = "X-IMSI";
	private static final String KEY_IMEI = "X-IMEI";
	private static final String KEY_SID = "X-SID";
	private static final String KEY_SIGN = "X-SIGN";
	private static final String KEY_OSVER = "X-OSVER";
	private static final String KEY_SCR = "X-SCR";
	private static final String KEY_IAP = "X-IAP";
	private static final String KEY_NET = "X-NET";
	private static final String KEY_APP = "X-APP";
	
	@SuppressWarnings("deprecation")
	public static HashMap<String, String> generateXHeaders(Context context, String url, byte[] postData) {
		if (url == null)
			throw new NullPointerException();
		
		String versionName = null;
		String versionCode = null;
		String device = null;
		String imsiEncrypt = null;
		String imeiEncrypt = null;
		String sid = null;
		String sign = null;
		String osver = null;
		String scr = null;
		String iap = null;
		String net = null;

		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
			versionName = info.versionName;
			versionCode = String.valueOf(info.versionCode);
			device = Build.MODEL;
			
			TelephonyManager telManager = (TelephonyManager) context.getSystemService(
					Context.TELEPHONY_SERVICE);
			String imsi = telManager.getSubscriberId();
			if (imsi == null)
				imsi = "";
			imsiEncrypt = Base64.encodeToString(Encoder.encode(imsi.getBytes()), Base64.NO_WRAP);
			String imei = telManager.getDeviceId();
			if (imei == null)
				imei = "";
			imeiEncrypt = Base64.encodeToString(Encoder.encode(imei.getBytes()), Base64.NO_WRAP);
			
			sid = Base64.encodeToString(Encoder.encode(getUid(context).getBytes()), Base64.NO_WRAP);
			
			Uri uri = Uri.parse(url);
			int portValue = uri.getPort();
			String host = uri.getHost();
			String port = portValue == -1 ? null : String.valueOf(portValue);
			int index = port == null ? url.indexOf(host) + host.length() : url.indexOf(port) + port.length();
			String pathWithQuery = url.substring(index);
			if (postData == null) {
				sign = Utils.toHexString(Utils.getMd5((imeiEncrypt + Constants.VALUE_APP_KEY + pathWithQuery).getBytes()));
			} else {
				ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
				byteOutput.write((imeiEncrypt + Constants.VALUE_APP_KEY + pathWithQuery).getBytes());
				byteOutput.write(postData);
				sign = Utils.toHexString(Utils.getMd5(byteOutput.toByteArray()));
			}
			
			osver = Utils.getOSVer(context);
			
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			scr = display.getWidth() + "x" + display.getHeight();
			
			NetworkInfo cni = Utils.getCurrNetworkInfo(context);
			if (cni != null) {
				net = cni.getTypeName();
				iap = cni.getExtraInfo();
			}
		} catch (Exception e) {
		}
		
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put(KEY_BUSI, Constants.VALUE_BUSI);
		ret.put(KEY_TIME, String.valueOf(System.currentTimeMillis() / 1000));
		ret.put(KEY_APP, Utils.getChannelId(context));
		if (!TextUtils.isEmpty(versionName))
			ret.put(KEY_VNAME, versionName);
		if (!TextUtils.isEmpty(versionCode))
			ret.put(KEY_VCODE, versionCode);
		if (!TextUtils.isEmpty(device))
			ret.put(KEY_DEVICE, device);
		if (!TextUtils.isEmpty(imsiEncrypt))
			ret.put(KEY_IMSI, imsiEncrypt);
		if (!TextUtils.isEmpty(imeiEncrypt))
			ret.put(KEY_IMEI, imeiEncrypt);
		if (!TextUtils.isEmpty(sid))
			ret.put(KEY_SID, sid);
		if (!TextUtils.isEmpty(sign))
			ret.put(KEY_SIGN, sign);
		if (!TextUtils.isEmpty(osver))
			ret.put(KEY_OSVER, osver);
		if (!TextUtils.isEmpty(scr))
			ret.put(KEY_SCR, scr);
		if (!TextUtils.isEmpty(net))
			ret.put(KEY_NET, net);
		if (!TextUtils.isEmpty(iap))
			ret.put(KEY_IAP, iap);
		
		return ret;
	}
	
	public static byte[] readFromAsset(Context context, String fileName) {
		byte[] ret = null;
		InputStream instream = null;
		try {
			instream = context.getAssets().open(fileName);
			byte[] buffer = new byte[8192];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = -1;
			while ((len = instream.read(buffer)) >= 0)
				baos.write(buffer, 0, len);
			baos.flush();
			ret = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
		} finally {
			try {
				if (instream != null)
					instream.close();
			} catch (IOException e) { }
		}
		
		return ret;
	}
	
	public static void highlight(TextView textView,String srcText, String keyword){
		if(TextUtils.isEmpty(keyword))
			return;
		
	    SpannableString spannable = new SpannableString(srcText);
	    CharacterStyle span=new ForegroundColorSpan(Color.parseColor("#3fc6a8"));
	    String key = keyword.toLowerCase();
	    int beginIndex = srcText.toLowerCase().indexOf(key);
	    int endIndex = beginIndex + key.length();
	    if(beginIndex >=0 && endIndex > beginIndex) {
		    spannable.setSpan(span, beginIndex,endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
		    textView.setText(spannable);
	    } else {
	    	textView.setText(srcText);
	    }
	}
	
	public static void startLoadingAnimation(View layoutLoading) {
		layoutLoading.setVisibility(View.VISIBLE);
		ImageView imageLoading = (ImageView)layoutLoading.findViewById(R.id.progress_bar);
		AnimationDrawable animationDrawable = (AnimationDrawable)imageLoading.getDrawable();
		animationDrawable.start();
	}
	
	public static void stopLoadingAnimation(View layoutLoading) {
		layoutLoading.setVisibility(View.GONE);
		ImageView imageLoading = (ImageView)layoutLoading.findViewById(R.id.progress_bar);
		AnimationDrawable animationDrawable = (AnimationDrawable)imageLoading.getDrawable();
		animationDrawable.stop();
	}
	
	public static String buildQueryString(Map<String, String> params) {
		if (params == null || params.size() <= 0)
			return "";
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		ArrayList<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			
			if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
				parameters.add(new BasicNameValuePair(key, value));
			}
		}
		return "?" + URLEncodedUtils.format(parameters, HTTP.UTF_8);
	}
		
	private static String gUid;
	private static String gChannelId;
	private static String createUid(Context context) {
		// 绠�鍗曡鍒�
		String uid = getImei(context)+ System.currentTimeMillis();
		return uid;
	}
	
	public static String getUid(Context context) {
		if(TextUtils.isEmpty(gUid)) {
			String uid = loadUidFromCacheFile(context);
			if(uid == null) {
				//浠庣紦瀛樿幏鍙杣id
				uid = getUidFromPref(context);
				if(uid == null) {
					gUid = createUid(context);
					setUidToPref(context, gUid);
					saveAppInfoToCacheFile(context, gUid, gChannelId);
				}else {
					gUid = uid;
				}	
			} else {
				gUid = uid;
			}
		}
		return gUid;
	}
	
	public static String getChannelId(Context context) {
		if(TextUtils.isEmpty(gChannelId)) {
			String channelId = loadChannelIdFromCacheFile(context);
			if(channelId == null) {
				channelId = getChannelIdFromPref(context);
				if(channelId == null) {
					gChannelId = createChannelId(context);
					setChannelIdToPref(context, gChannelId);
					saveAppInfoToCacheFile(context, gUid, gChannelId);
				}else {
					gChannelId = channelId;
				}
			}
			else {
				gChannelId = channelId;
			}
		}
		return gChannelId;
	}
	
	
	private static String createChannelId(Context context){
		String Result="";
        try { 
             InputStreamReader inputReader = new InputStreamReader( context.getResources().getAssets().open("bid.txt") ); 
             BufferedReader bufReader = new BufferedReader(inputReader);
             String line="";
             while((line = bufReader.readLine()) != null)
                Result += line;
             
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        if(TextUtils.isEmpty(Result) || Result.equalsIgnoreCase("$bid"))
        	Result = "1";
        return Result;
	} 
	
	private static final String APPINFO_DIR_NAME = ".appInfo";
	private static String APPINFO_FILENAME = "appinfo.dat";
	private static String APPINFO_UID = "uid";
	private static String APPINFO_CHANNELID = "channelid";
	private static final String PREF_APPINFO = "pref_appinfo";
	public static void setUidToPref(Context context, String uid) {
		SharedPreferences pref = context.getSharedPreferences(
				PREF_APPINFO, Context.MODE_PRIVATE);
		pref.edit().putString(APPINFO_UID, uid).commit();
	}
	
	public static String getUidFromPref(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				PREF_APPINFO, Context.MODE_PRIVATE);
		return pref.getString(APPINFO_UID, null);
	}
	
	public static void setChannelIdToPref(Context context, String channelId) {
		SharedPreferences pref = context.getSharedPreferences(
				PREF_APPINFO, Context.MODE_PRIVATE);
		pref.edit().putString(APPINFO_CHANNELID, channelId).commit();
	}
	
	public static String getChannelIdFromPref(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				PREF_APPINFO, Context.MODE_PRIVATE);
		return pref.getString(APPINFO_CHANNELID, null);
	}
	
	private static String getAppInfoPath(Context context) {
		File dir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			dir = new File(Environment.getExternalStorageDirectory(), APPINFO_DIR_NAME);
			if (!dir.exists())
				dir.mkdirs();
		} else {
			dir = context.getCacheDir();
		}
		
		File file = new File(dir, APPINFO_FILENAME);
		return file.getAbsolutePath();
	}
	
	public static String loadUidFromCacheFile(Context context) {
		String uid = null;
		String filePath = getAppInfoPath(context);
		FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            byte b[]=new byte[(int)file.length()];     //鍒涘缓鍚堥�傛枃浠跺ぇ灏忕殑鏁扮粍  
            fis.read(b);    //璇诲彇鏂囦欢涓殑鍐呭鍒癰[]鏁扮粍  
            JSONObject json = new JSONObject(new String(b));
            uid = json.getString(APPINFO_UID);
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e2) {
            }
        }
        return uid;
	}
	
	public static String loadChannelIdFromCacheFile(Context context) {
		String channelId = null;
		String filePath = getAppInfoPath(context);
		FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            byte b[]=new byte[(int)file.length()];     //鍒涘缓鍚堥�傛枃浠跺ぇ灏忕殑鏁扮粍  
            fis.read(b);    //璇诲彇鏂囦欢涓殑鍐呭鍒癰[]鏁扮粍  
            JSONObject json = new JSONObject(new String(b));
            channelId = json.getString(APPINFO_CHANNELID);
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e2) {
            }
        }
        return channelId;
	}
	
	public static void saveAppInfoToCacheFile(final Context context, final String uid, final String channelId) {
    	Thread thread = new Thread(
        		new Runnable(){
		            @Override
		            public void run() {
		            	String filePath = getAppInfoPath(context);
		            	FileOutputStream fis = null;
	            		try {
		            		File file = new File(filePath);
		        			fis = new FileOutputStream(file);
		        			JSONObject json = new JSONObject();
		        			json.put(APPINFO_UID, uid);
		        			json.put(APPINFO_CHANNELID, channelId);
		        			fis.write(json.toString().getBytes());
		            	} catch (Exception e) {
		        			e.printStackTrace();
		        		}finally{
		        			try {
		        				if(fis != null ){
		        					fis.close();
		        				}
		        			} catch (Exception e2) {
		        			}
		        		}
		            }
        		});
    	thread.start();
	}
	
	public static boolean copyFile(String oldPath, String newPath) {   
		boolean bOk = true; 
		try {   
		       int bytesum = 0;   
		       int byteread = 0;   
		       File oldfile = new File(oldPath);  
		       File sdFile = new File(newPath);
		       if(sdFile.exists())
		    	   sdFile.delete();
		       if (oldfile.exists()) { //鏂囦欢瀛樺湪鏃�   
		       InputStream inStream = new FileInputStream(oldPath); //璇诲叆鍘熸枃浠�   
		       FileOutputStream fs = new FileOutputStream(newPath);   
		       byte[] buffer = new byte[1444];    
		       while ( (byteread = inStream.read(buffer)) != -1) {   
		           bytesum += byteread; //瀛楄妭鏁� 鏂囦欢澶у皬   
		               System.out.println(bytesum);   
		               fs.write(buffer, 0, byteread);   
		           }   
		           inStream.close(); 
		           oldfile.delete();
		       }
		   }   
		   catch (Exception e) {
			    bOk = false;
			    e.printStackTrace();   
		   }
		return bOk;
	 }
	
	private static boolean gIsMerging = false;
	private static synchronized boolean isMerging() {
		return gIsMerging;
	}
	private static synchronized void setMergeFlag(boolean flag) {
		gIsMerging = flag;
	}
	public static class ApkMergeAsycTask extends AsyncTask<Object, Object, Object> {
		
		private Context mContext;
		public ApkMergeAsycTask(Context context) {
			mContext = context;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			DownloadTask task = null;
			try {
				task = (DownloadTask) params[0];
				String oldApkFilePath = (String)params[1];
				String newApkFilePath = (String)params[2];
				String patchFilePath = (String)params[3];
				String tempNewFilePath = (String)params[4];
				while(isMerging())
					Thread.sleep(100);
				setMergeFlag(true);
				Log.d("demo", "mergeApk start");
				Log.d("demo", "mergeApk oldApkFilePath = "+oldApkFilePath);
				Log.d("demo", "mergeApk newApkFilePath = "+newApkFilePath);
				Log.d("demo", "mergeApk patchFilePath = "+patchFilePath);
				Log.d("demo", "mergeApk tempNewFilePath = "+tempNewFilePath);
				
				//test
			    long time1 = System.currentTimeMillis();
				ZipMerge zipMerge = new ZipMerge();
				File newFile = new File(newApkFilePath);
				if(newFile.exists())
					newFile.delete();
	            zipMerge.mergePatchByBuffer(oldApkFilePath, patchFilePath,newApkFilePath);
				long time2 = System.currentTimeMillis() - time1;
	            Log.d("demo", "mergeApk end meregetime = "+ String.valueOf(time2));
				{
					
					File patchFile = new File(patchFilePath);
					if(patchFile.exists())
						patchFile.delete();
					task.mTotal = newFile.length();
					task.mTransfered = task.mTotal;
					task.mLocalPath = newApkFilePath;
					task.mStatus = TaskStatus.STATUS_DOWNLOAD;
				}
			}  
			catch(Exception e) {
				e.printStackTrace();
			}	
			return task;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			setMergeFlag(false);
			if (result == null)
				return;
			
			DownloadTask task = (DownloadTask) result;
			if(task.mStatus == TaskStatus.STATUS_DOWNLOAD) {
				if (AppSettings.isRootInstall(mContext) && Utils.isPhoneRoot()) {
					task.mStatus = TaskStatus.STATUS_INSTALLING;					
					Utils.AsyncTaskExecute(new SilenceInstall(mContext), task);
				} else if(AppSettings.isAutoInstall(mContext)) {
					task.mStatus = TaskStatus.STATUS_DOWNLOAD;
					Utils.reqSystemInstall(mContext, task.mLocalPath);
				}
			}
			if(DownloadService.getDownloadControl() != null)
				DownloadService.getDownloadControl().onTaskStatusChanged(task);
		}
	}
		
	public static void mergeApk(Context context, final DownloadTask task, final String oldApkFilePath,final String newApkFilePath
			, final String patchFilePath, final String tempNewFilePath) {
		
		if(task != null) {
			File oldFile = new File(oldApkFilePath);
			File patchFile = new File(patchFilePath);
			if(oldFile.exists() && patchFile.exists())
				Utils.AsyncTaskExecute(new ApkMergeAsycTask(context), task, oldApkFilePath, newApkFilePath, patchFilePath, tempNewFilePath);
		}
		
	}
	public native static int nativeMakeApk(String oldApkFilePath, String newApkFilePath, String patchFilePath );
	
}
