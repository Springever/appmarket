package com.appmall.market.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.widget.ImageView;

import com.appmall.market.R;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.AppUpdate;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.LocalApps;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.widget.CommonDialog;

/**
 * 包含整个项目中通用的逻辑
 *  
 *
 */
public class CommonInvoke {
	
	public static final long LARGE_FILE_SIZE = 20 * 1024 * 1024L;
	private static final String DIR_NAME = "appmall";
	private static final String APK_SUFFIX = ".apk";
	
	private static boolean sQueryedNetworkTip = false;
	
	/**
	 * 对应用进行过滤处理
	 * @param dc DataCenter实例
	 * @param apps 待处理的列表
	 * @param removeInst true表示移除已安装的应用
	 * @return
	 */
	public static List<App> processApps(DataCenter dc, List<App> apps, boolean removeInst) {
		if (dc == null || apps == null || apps.size() <= 0)
			return new ArrayList<App>(0);

		List<App> ret = null;
		if (removeInst) {
			ret = new ArrayList<App>();
		} else {
			ret = apps;
		}
		
		for (App app : apps) {
			int instStatus = dc.getPackageInstallStatus(app.mPackageName, app.mVersionCode, app.mVersionName);
			app.setInstStatus(instStatus);
			
			if (removeInst) {
				if (instStatus == LocalApps.STATUS_INSTALLED) {
					continue;
				} else {
					ret.add(app);
				}
			}
			
			DownloadTask task = dc.getTask(app.mPackageName);
			app.setTaskStatus(task == null ? TaskStatus.STATUS_UNKNOWN : task.mStatus);
		}
		
		return ret;
	}
	
	/**
	 * 生成应用更新接口的签名
	 */
	public static String getUpremindSignature(String imei) {
		return Utils.toHexString(Utils.getMd5((Constants.UPREMIND_SECRET + imei).getBytes()))
				.toLowerCase(Locale.getDefault());
	}
	
	/**
	 * 生成文件下载路径
	 */
	public static String generateDownloadPath(Context context, String packageName, int versionCode, String versionName ,boolean isPatch) {
		File dir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			dir = new File(Environment.getExternalStorageDirectory(), DIR_NAME);
			if (!dir.exists())
				dir.mkdirs();
		} else {
			dir = context.getCacheDir();
		}
		
		File file = new File(dir, packageName +"_"+versionCode+ APK_SUFFIX + (isPatch ?".patch":""));
		return file.getAbsolutePath();
	}
	
	/**
	 * 清空缓存目录
	 */
	public static void clearCacheDirectory(Context context, List<String> filter) {
		File cacheDir = context.getExternalCacheDir();
		if (cacheDir != null && cacheDir.exists()) {
			deleteDirectory(context, cacheDir, filter);
		}
		
		cacheDir = context.getCacheDir();
		if (cacheDir != null && cacheDir.exists()) {
			deleteDirectory(context, cacheDir, filter);
		}
	}
	
	/**
	 * 清空下载目录
	 */
	public static void clearDownloadDirectory(Context context, List<String> filter) {
		File dir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			dir = new File(Environment.getExternalStorageDirectory(), DIR_NAME);
		} else {
			dir = context.getCacheDir();
		}
		deleteDirectory(context, dir, filter);
	}
	
	/**
	 * 删除某个目录及所有文件
	 * @param filter 
	 */
	final private static void deleteDirectory(Context context, File dir, List<String> filter) {
		if (!dir.isDirectory() && (filter == null || !filter.contains(dir.getAbsolutePath()))) {
			dir.delete();
			return;
		}
		
		File[] files = dir.listFiles();
		if (files == null)
			return;
		if (files.length == 0 && (filter == null || !filter.contains(dir.getAbsolutePath()))) {
			dir.delete();
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				deleteDirectory(context, file, filter);
			} else {
				if (filter == null || !filter.contains(file.getAbsolutePath())) {
					file.delete();
				}
			}
		}
	}
	
	/**
	 * 处理软件列表下载按钮的逻辑
	 */
	public static void processAppDownBtn(Activity activity, ImageView icon, App app, boolean showAnimation) {
		if (app == null || TextUtils.isEmpty(app.mPackageName))
			return;
		
		int instStatus = app.getInstStatus();
		int taskStatus = app.getTaskStatus();
		if (instStatus == LocalApps.STATUS_INSTALLED) {
			Utils.openPackage(activity, app.mPackageName);
		} else {
			DownloadTask task = DataCenter.getInstance().getTask(app.mPackageName);
			//有版本更新，则从下载列表中删除老的任务
			if(TaskStatus.STATUS_INSTALLED == taskStatus && instStatus == LocalApps.STATUS_INSTALLED_OLD_VERSION) {
				task.mTaskUrl = app.mDownloadUrl;
				Intent service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_EXIST_UPDATE);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
				return;
			}
			switch (taskStatus) {
			case TaskStatus.STATUS_DOWNLOAD:
				if (task != null && !TextUtils.isEmpty(task.mLocalPath)
						&& task.mStatus == TaskStatus.STATUS_DOWNLOAD) {
					File apkFile = new File(task.mLocalPath);
					if (!apkFile.exists()) {
						showReDownloadDialog(activity, task);
						return;
					}
					
					Utils.doTaskInstall(activity, task);
				}
				break;
				
			case TaskStatus.STATUS_DOWNLOADING:
			case TaskStatus.STATUS_WAIT:
				task = DataCenter.getInstance().getTask(app.mPackageName);
				if (task == null)
					return;
				Intent service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_STOP_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
				break;
				
			case TaskStatus.STATUS_PAUSE:
			case TaskStatus.STATUS_FAILED:
				service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_RESUME_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
				break;
			case TaskStatus.STATUS_UNKNOWN:
				int mInstStatus = DataCenter.getInstance().getPackageInstallStatus(app.mPackageName, app.mVersionCode, app.mVersionName);
				int source = DownloadTask.SOURCE_APPMARKET;
				if(mInstStatus == LocalApps.STATUS_INSTALLED_OLD_VERSION) {
					source = DownloadTask.SOURCE_UPDATE_OP;
					String appSignature =DataCenter.getInstance().getInstallPackageSignature(app.mPackageName);
					if(appSignature != null && !appSignature.equalsIgnoreCase(app.mPackageSignature)) {
						showSignatureDiffConfirmDialog(activity,source,icon, showAnimation, app);
						break;
					}
				}			
				addDownloadTask(activity, source, app.mPackageName, app.mID, app.mTitle, app.mIconUrl, app.mVersionCode, app.mVersionName
						, app.mDownloadUrl,app.mSize, icon, showAnimation, false);
				break;
			default:
				break;
			}
		}
	}
	
	public static void addDownloadTask(Activity activity, int sourceType, String packageName, int channelId, String title, String iconUrl, int versionCode
			, String versionName,String url, long appSize, ImageView icon, boolean showAnimation, boolean isSignDiff) {
		DownloadTask task = DownloadTask.buildNewTask(packageName, sourceType, channelId);
		task.setSignDiff(isSignDiff);
		task.mTitle = title;
		task.mIconData = iconUrl;
		task.mVersionCode = versionCode;
		task.mVersionName = versionName;
		task.mTaskUrl = url;
		task.mLocalPath = generateDownloadPath(activity, packageName,versionCode,versionName, task.mIsPatch);

		Intent service = new Intent(activity, DownloadService.class);
		service.setAction(DownloadService.ACTION_START_TASK);
		service.putExtra(DownloadService.EXTRA_TASK, task);
		
		PendingIntent pendingIntent = PendingIntent.getService(activity, 0, service, 0);
		if (showNetworkTip(activity, appSize, pendingIntent, icon))
			return;
		
		File file = new File(task.mLocalPath);
		if (!ensureStorageSpaceEnough(activity, file.getParent(), appSize))
			return;
		
		activity.startService(service);
		
		if(showAnimation)
			DataCenter.getInstance().reportNewDownEvent(icon);
	}
	
	public static void showSignatureDiffInstallDialog(final Context context,final String packageName) {
		String message = context.getResources().getString(R.string.signature_diff_install_content);
		String title = context.getResources().getString(R.string.signature_diff_title);
		final CommonDialog queryDialog = new CommonDialog(context);
		queryDialog.setMessage(message);
		queryDialog.setTitle(title);
		queryDialog.setCheckBoxVisible(false);
		queryDialog.setNegativeButton(R.string.dialog_cancel, null);
		queryDialog.setPositiveButton(R.string.dialog_uninstall, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utils.reqSystemUninstall(context, packageName);
			}
		});
		queryDialog.show();
	}
	
	public static void showSignatureDiffConfirmDialog(final Activity activity, final int sourceType, final ImageView icon, final boolean showAnimation, final App app) {
		String message = activity.getResources().getString(R.string.signature_diff_content);
		String title = activity.getResources().getString(R.string.signature_diff_title);
		
		final CommonDialog queryDialog = new CommonDialog(activity);
		queryDialog.setMessage(message);
		queryDialog.setTitle(title);
		queryDialog.setCheckBoxVisible(false);
		queryDialog.setNegativeButton(R.string.dialog_cancel, null);
		queryDialog.setPositiveButton(R.string.dialog_continue, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addDownloadTask(activity, sourceType,app.mPackageName, app.mID, app.mTitle, app.mIconUrl, app.mVersionCode, app.mVersionName
						, app.mDownloadUrl,app.mSize, icon, showAnimation, true);
			}
		});
		queryDialog.show();
	}
	
	public static void showSignatureDiffConfirmDialog(final Activity activity, final int sourceType,final ImageView icon, final boolean showAnimation, final Advert advert) {
		String message = activity.getResources().getString(R.string.signature_diff_content);
		String title = activity.getResources().getString(R.string.signature_diff_title);
		
		final CommonDialog queryDialog = new CommonDialog(activity);
		queryDialog.setMessage(message);
		queryDialog.setTitle(title);
		queryDialog.setCheckBoxVisible(false);
		queryDialog.setNegativeButton(R.string.dialog_cancel, null);
		queryDialog.setPositiveButton(R.string.dialog_continue, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addDownloadTask(activity, sourceType, advert.mPackageName, advert.mAppId, advert.mTitle, advert.mAppIconUrl, advert.mVersionCode
						, advert.mVersionName, advert.mAppUrl,advert.mSize, icon, showAnimation, true);
			}
		});
		queryDialog.show();
	}
	
	/**
	 * 处理更新按钮下载的逻辑
	 */
	public static void processUpdateBtn(Activity activity, ImageView icon, AppUpdate update) {
		if (update == null || TextUtils.isEmpty(update.mPackageName))
			return;
		
		int instStatus = update.getInstStatus();
		int taskStatus = update.getTaskStatus();
		if (instStatus == LocalApps.STATUS_INSTALLED) {
			Utils.openPackage(activity, update.mPackageName);
		} else {
			DownloadTask task = DataCenter.getInstance().getTask(update.mPackageName);
			//有版本更新，则从下载列表中删除老的任务
			if(TaskStatus.STATUS_INSTALLED == taskStatus && instStatus == LocalApps.STATUS_INSTALLED_OLD_VERSION) {
				task.mIsPatch = update.mHasPatch;
				if(update.mHasPatch)
					task.mTaskUrl = update.mPatchUrl;
				else
					task.mTaskUrl = update.mDownloadPath;
				Intent service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_EXIST_UPDATE);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
				return;
			}
			
			switch (taskStatus) {
			case TaskStatus.STATUS_DOWNLOAD:
				if (task != null && !TextUtils.isEmpty(task.mLocalPath)
						&& task.mStatus == TaskStatus.STATUS_DOWNLOAD) {
					File apkFile = new File(task.mLocalPath);
					if (!apkFile.exists()) {
						showReDownloadDialog(activity, task);
						return;
					}
					
					Utils.doTaskInstall(activity, task);
				}
				break;
				
			case TaskStatus.STATUS_DOWNLOADING:
			case TaskStatus.STATUS_WAIT:
				task = DataCenter.getInstance().getTask(update.mPackageName);
				if (task == null)
					return;
				Intent service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_STOP_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
				break;
				
			case TaskStatus.STATUS_PAUSE:
			case TaskStatus.STATUS_FAILED:
				service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_RESUME_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
				break;
			case TaskStatus.STATUS_UNKNOWN:
				task = DownloadTask.buildNewTask(update.mPackageName, DownloadTask.SOURCE_UPDATE_LIST, update.mChannelId, update.mHasPatch, update.mDownloadPath);
				task.mTitle = update.mLabel;
				task.mIconData = update.mPackageName;
				task.mVersionCode = update.mVersionCode;
				task.mVersionName = update.mVersion;
				if(update.mHasPatch)
					task.mTaskUrl = update.mPatchUrl;
				else
					task.mTaskUrl = update.mDownloadPath;
				task.mLocalPath = generateDownloadPath(activity, update.mPackageName,update.mVersionCode,update.mVersion, task.mIsPatch);
				
				service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_START_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				
				PendingIntent pendingIntent = PendingIntent.getService(activity, 0, service, 0);
				if (showNetworkTip(activity, update.mFileSize, pendingIntent, icon))
					return;
				File file = new File(task.mLocalPath);
				if (!ensureStorageSpaceEnough(activity, file.getParent(), update.mFileSize))
					return;
				
				activity.startService(service);
				
				DataCenter.getInstance().reportNewDownEvent(icon);
				break;
			default:
				break;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static boolean ensureStorageSpaceEnough(Activity activity, String path, long size) {
		if (TextUtils.isEmpty(path))
			return false;
		
		StatFs sf = new StatFs(path);
		long blockSize = sf.getBlockSize();
		int blockCount = sf.getAvailableBlocks();
		
		if (blockSize * blockCount - size > 0)
			return true;
		
		final Context context = activity.getApplicationContext();
		CommonDialog dialog = new CommonDialog(activity);
		dialog.setCheckBoxVisible(false);
		dialog.setTitle(R.string.dialog_title);
		dialog.setMessage(R.string.dialog_space_not_enough);
		dialog.setNegativeButton(R.string.dialog_cancel, null);
		dialog.setPositiveButton(R.string.dialog_enter_app_manage_setting, true, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utils.jumpSystemAppManageActivity(context);
			}
		});
		
		if (activity != null && !activity.isFinishing()) {
			dialog.show();
		}
		
		return false;
	}

	/**
	 * 如果文件大小超过某个较大的值，提醒用户是否使用wifi下载
	 * @param icon 
	 */
	public static boolean showNetworkTip(Activity activity, long filesize,
			PendingIntent pendingIntent, final ImageView icon) {
		boolean isLargeSize = filesize >= CommonInvoke.LARGE_FILE_SIZE;
		boolean isNetAvailable = Utils.isNetworkAvailable(activity);
		boolean isWifiConnected = Utils.isWifiConnected(activity);
		
		if (isLargeSize && isNetAvailable && !isWifiConnected && !sQueryedNetworkTip) {
			final Context context = activity;
			final PendingIntent continueIntent = pendingIntent;
			CommonDialog dialog = new CommonDialog(activity);
			dialog.setTitle(R.string.dialog_title);
			dialog.setMessage(R.string.dialog_network_tip);
			dialog.setNegativeButton(R.string.continue_download,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								continueIntent.send();
								if (icon != null) {
									DataCenter.getInstance().reportNewDownEvent(icon);
								}
							} catch (CanceledException e) { }
						}
					});
			dialog.setPositiveButton(R.string.dialog_enter_network_setting, true, 
					new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Utils.jumpNetworkSetting(context);
							}
					}
			);
			dialog.show();
			sQueryedNetworkTip = true;
			return true;
		}
		
		return false;
	}
	
	/**
	 * 显示客户端存在更新的对话框
	 */
	public static void showUpdateDialog(final Activity activity, final int versionCode, final String versionName,
			String updateSize, String updateInfo, final String fileUrl) {
		String messageFormatter = activity.getString(R.string.update_dialog_message_formatter);
		String message = String.format(Locale.getDefault(), messageFormatter,
				versionName, updateSize, updateInfo);
		
		CommonDialog dialog = new CommonDialog(activity);
		dialog.setTitle(R.string.app_store_has_new_version);
		dialog.setMessage(message);
		dialog.setCheckBoxVisible(false);
		dialog.setNegativeButton(R.string.show_update_next_time, null);
		dialog.setPositiveButton(R.string.update_immediately, true, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PackageManager pm = activity.getPackageManager();
				String packageName = activity.getPackageName();
				PackageInfo info = null;
				try {
					info = pm.getPackageInfo(packageName, 0);
					if (info == null)
						return;
				} catch (NameNotFoundException e) { }
				CharSequence label = info.applicationInfo.loadLabel(pm);
				
				DownloadTask task = DownloadTask.buildNewTask(packageName, DownloadTask.SOURCE_UPDATE_OP, 0);
				task.mTitle = label == null ? "" : label.toString();
				task.mIconData = packageName;
				task.mVersionCode = versionCode;
				task.mVersionName = versionName;
				task.mTaskUrl = fileUrl;
				task.mLocalPath = CommonInvoke.generateDownloadPath(activity, packageName,task.mVersionCode,task.mVersionName, task.mIsPatch);
				
				AppSettings.setUpdateVersionName(activity, versionName);
				
				Intent service = new Intent(activity, DownloadService.class);
				service.setAction(DownloadService.ACTION_START_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, task);
				activity.startService(service);
			}
		});
		dialog.show();
	}
	
	/**
	 * 显示下载文件不存在，是否重新下载的对话框
	 */
	public static void showReDownloadDialog(Activity activity, DownloadTask task) {
		final DownloadTask retryTask = task;
		final Context context = activity;
		CommonDialog dialog = new CommonDialog(activity);
		dialog.setCheckBoxVisible(false);
		dialog.setTitle("重新下载?");
		dialog.setMessage(R.string.download_file_not_exist_need_redownload);
		dialog.setPositiveButton(R.string.dialog_ok, true, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent service = new Intent(context, DownloadService.class);
				service.setAction(DownloadService.ACTION_RESTART_TASK);
				service.putExtra(DownloadService.EXTRA_TASK, retryTask);
				context.startService(service);
			}
		});
		dialog.setNegativeButton(R.string.dialog_cancel, null);
		dialog.show();
	}
	
}
