package com.appmall.market.activity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.bean.CheckForUpdate;
import com.appmall.market.bean.UpdateInfo;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.ClientUpgrade;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Constants;
import com.appmall.market.common.Statistics;
import com.appmall.market.common.NotificationMgr;
import com.appmall.market.common.UpdateQuery;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.download.DownloadService;
import com.appmall.market.widget.CommonDialog;
import com.appmall.market.common.NotifycationReceive;
import com.appmall.market.activity.MainActivity;
import com.appmall.market.common.WifiStateReceiver;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import com.appmall.market.common.NetworkStateReceiver;

/**
 * 应用商店主界面
 *  
 *
 */
@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements IDataCallback, Observer, OnClickListener {

	private static final int FEATURE_EXPANDABLE_OPTION_PANEL = 30;
	public static final String TAB_PROMOTE = "promote";
	public static final String TAB_APPLICATION = "application";
	public static final String TAB_GAME = "game";
	public static final String TAB_MANAGE = "manage";
	private static final int TAB_INDEX_MANAGE = 3;
	
	private static final long MAIN_TRANSACT_DELAY = 1000L;
	private static WeakReference<MainActivity> mMainActivityRef;
	
	private TabHost mTabHost;
	private ImageView mDownFloatIcon;
	private TextView mDownFloatText;
	private EditText mSearchEditor;
	private boolean mEnsureBack = false;
	private Toast mEnsureBackToast;
	
	private ImageView mAinimation;
	private void showDownAnimation(ImageView imageView) {
		Drawable drawable = imageView.getDrawable();
		if (drawable == null)
			return;
		mAinimation.setImageDrawable(drawable);
		mAinimation.setVisibility(View.VISIBLE);
		mAinimation.setAlpha(100);
		float iconHeight = getResources().getDimension(R.dimen.common_item_icon_size);
		long animTime1 = 100;
		long animTime2 = 800;
		long animTime3 = animTime1+animTime2;
		float offsetY = getResources().getDimension(R.dimen.anim_icon_offset_y);
		Animation scaleAnim0 = new ScaleAnimation(1f, 1.1f, 1f, 1.1f);
		scaleAnim0.setDuration(animTime1);
		Animation scaleAnim1 = new ScaleAnimation(1f, .33f, 1f, .33f);
		scaleAnim1.setDuration(animTime2);
		Animation translateAnim = new TranslateAnimation(0, mDownFloatIcon.getWidth()*2/3, 0, -(iconHeight*2/3+offsetY));
		translateAnim.setDuration(animTime3);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(scaleAnim0);
		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(translateAnim);
		animSet.setAnimationListener(mFloatDownAnimEndListener);
		mAinimation.startAnimation(animSet);
	}
	
	private void showNumberAnimation() {
		Animation scaleAnim1 = new ScaleAnimation(1.0F, 1.1F, 1.0F, 1.1F, 1, 0.0F, 1, 1.0F);
		scaleAnim1.setDuration(30L);
		Animation scaleAnim2 = new ScaleAnimation(1.1F, 1.0F, 1.1F, 1.0F, 1, 0.0F, 1, 1.0F);
		scaleAnim2.setDuration(100L);
		Animation alphaAnim = new AlphaAnimation(0.8F, 1.0F);
		alphaAnim.setDuration(130L);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(scaleAnim2);
		animSet.addAnimation(alphaAnim);
		mDownFloatText.startAnimation(animSet);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(FEATURE_EXPANDABLE_OPTION_PANEL);
		setContentView(R.layout.activity_main);
		mMainActivityRef = new WeakReference<MainActivity>(this);
		initDataCenter();
		mAinimation = (ImageView)findViewById(R.id.image_animation);
		mDownFloatIcon = (ImageView)findViewById(R.id.layout_search_bar).findViewById(R.id.image_downcount);
		mDownFloatText = (TextView)findViewById(R.id.float_down_icon_text);
		mSearchEditor = (EditText)findViewById(R.id.layout_search_bar).findViewById(R.id.search_edittext);
		mSearchEditor.setFocusable(false);
		mSearchEditor.setOnClickListener(this);	
		mDownFloatIcon.setOnClickListener(this);
		Utils.scaleClickRect(mDownFloatIcon);
		setupTabs();
		processTabAction(getIntent());
		DataCenter.getInstance().addObserver(this);
		resetDownFloatIconVisible();
		Intent intent = getIntent();
		boolean bUpgradeFlag = false;
		if(intent != null) {
			int action = intent.getIntExtra("action", -1);
			if(action == NotificationMgr.NOTIFICATION_ACTION_UPGRADE)
				bUpgradeFlag = true;
			processNotifycationAction(intent);
		}	
		if(AppSettings.isFirstEnter(this)) {
			AppSettings.setFirstEnter(this, false);
	        if(!hasShortcut(this, Constants.AUTHORITY)){
	            createShortCut(this, getResources().getString(R.string.app_name));
	        }
		}	
		final boolean bDailyQueryFlag = bUpgradeFlag;
		mAinimation.postDelayed(new Runnable() {
			@Override
			public void run() {
				MainActivity activity = MainActivity.this;
				// 每周后台检测软件更新
				UpdateQuery.setBackgroundQueryAlarm(activity);
				ClientUpgrade.setNextUpdateAlarm(activity);
				// 每日进入主界面检测软件更新
				if (!UpdateQuery.startDailyQuery(activity, activity)) {
					DataCenter.getInstance().reportUpdateCountChanged();
				}
				// 每日进入主界面检测客户端新版本
				if(!bDailyQueryFlag)
					ClientUpgrade.startDailyQuery(activity, activity);
				// 进入主界面，上传日志
				Statistics.addBootCount(activity);
				Statistics.setNextDailyPostAlarm(activity);
				Statistics.bootPost(activity, activity);
			}
		}, MAIN_TRANSACT_DELAY);				
		registerWifiStateReveiver();
	}
	
	
	//动态注册wifi监听器
	private WifiStateReceiver mWifiReceiver;
	void registerWifiStateReveiver() {
		mWifiReceiver=new WifiStateReceiver(this);
		IntentFilter filter=new IntentFilter();
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		this.registerReceiver(mWifiReceiver,filter);
	}
	
	public static MainActivity getMainActivity() {
		return mMainActivityRef.get();
	}
	
	private void initDataCenter() {
		DataCenter dc = DataCenter.getInstance();
		dc.ensureInit(this);
		dc.getLocalApps().setLocalAppRefresh(false);
		Utils.AsyncTaskExecute(new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				DataCenter.getInstance().refreshLocalData(MainActivity.this);
				return null;
			}
			@Override
			protected void onPostExecute(Object result) {
				DataCenter.getInstance().reportLocalDataChanged();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Statistics.onClientStart(this);
	}

	@Override
	protected void onStop() {
		Statistics.onClientStop(this);
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		DataCenter.getInstance().deleteObserver(this);
		mMainActivityRef = null;
		if(mWifiReceiver != null)
			this.unregisterReceiver(mWifiReceiver);
		super.onDestroy();
	}
	
	private void processNotifycationAction(Intent intent) {
		if(intent != null) {
			int action = intent.getIntExtra(NotifycationReceive.NOTIFY_ACTION, -1);
			if(action == NotificationMgr.NOTIFICATION_ACTION_RETURNTODOWNLOADPAGE) {
				switchToDownloadPage();
			} else if(action == NotificationMgr.NOTIFICATION_ACTION_RETURNTOUPDATEPAGE) {
				Statistics.addClickUpremindNotificationCount(this);
				mTabHost.setCurrentTabByTag(TAB_MANAGE);
				ManageAcitivty manageActivity = (ManageAcitivty) getLocalActivityManager().getActivity(TAB_MANAGE);
				if(manageActivity != null)
					manageActivity.switchToUpdateFragment();
			} else if(action == NotificationMgr.NOTIFICATION_ACTION_UPGRADE) {
				int versionCode = intent.getIntExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONCODE, 100);
				String versionName = intent.getStringExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONNAME);
				String versionSize = intent.getStringExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONSIZE);
				String versionDesc = intent.getStringExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONDESC);
				String versionUrl = intent.getStringExtra(NotifycationReceive.NOTIFY_UPGRADE_VERSIONURL);
				CommonInvoke.showUpdateDialog(this, versionCode, versionName, versionSize, versionDesc, versionUrl);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		processTabAction(intent);
		
		processNotifycationAction(intent);
	}

	private void processTabAction(Intent intent) {
		String action = null;
		if (intent != null) {
			action = intent.getAction();
		}
		
		if (Constants.ACTION_VIEW_DOWNLOAD_STATUS.equals(action)) {
			switchToDownloadPage();
		} else if (Constants.ACTION_VIEW_UPDATE.equals(action)) {
			mTabHost.setCurrentTabByTag(TAB_MANAGE);
			ManageAcitivty manageActivity = (ManageAcitivty) getLocalActivityManager().getActivity(TAB_MANAGE);
			manageActivity.switchToUpdateFragment();		
		}
	}
	
	public void switchToTabActivity(String tabName) {
		mTabHost.setCurrentTabByTag(tabName);
	}
	/**
	 * 初始化五大金刚Tab
	 */
	private void setupTabs() {
		mTabHost = getTabHost();
		mTabHost.setBackgroundColor(getResources().getColor(R.color.tab_widget_bg_color));
		mTabHost.setup();
		
		TabSpec spec = mTabHost.newTabSpec(TAB_PROMOTE)
				.setContent(new Intent(this, PromoteAcitivty.class))
				.setIndicator(buildIndicator(TAB_PROMOTE));
		mTabHost.addTab(spec);
		
		Intent appTab = new Intent(this, ApplicationAcitivty.class);
		appTab.putExtra(ApplicationAcitivty.EXTRA_TYPE, ApplicationAcitivty.TYPE_APP);
		spec = mTabHost.newTabSpec(TAB_APPLICATION)
				.setContent(appTab)
				.setIndicator(buildIndicator(TAB_APPLICATION));
		mTabHost.addTab(spec);
		
		Intent gameTab = new Intent(this, ApplicationAcitivty.class);
		gameTab.putExtra(ApplicationAcitivty.EXTRA_TYPE, ApplicationAcitivty.TYPE_GAME);
		spec = mTabHost.newTabSpec(TAB_GAME)
				.setContent(gameTab)
				.setIndicator(buildIndicator(TAB_GAME));
		mTabHost.addTab(spec);
		
		spec = mTabHost.newTabSpec(TAB_MANAGE)
				.setContent(new Intent(this, ManageAcitivty.class))
				.setIndicator(buildIndicator(TAB_MANAGE));
		mTabHost.addTab(spec);
		
		mTabHost.setCurrentTabByTag(TAB_PROMOTE);
	}

	private View buildIndicator(String tab) {
		int drawableRes = 0;
		int labelRes = 0;
		
		if (TAB_PROMOTE.equals(tab)) {
			drawableRes = R.drawable.indicator_promote;
			labelRes = R.string.tab_title_promote;
		} else if (TAB_APPLICATION.equals(tab)) {
			drawableRes = R.drawable.indicator_app;
			labelRes = R.string.tab_title_application;
		} else if (TAB_GAME.equals(tab)) {
			drawableRes = R.drawable.indicator_game;
			labelRes = R.string.tab_title_game;
		} else if (TAB_MANAGE.equals(tab)) {
			drawableRes = R.drawable.indicator_manage;
			labelRes = R.string.tab_title_manage;
		} else {
			throw new RuntimeException();
		}
		
		View indicator = getLayoutInflater().inflate(R.layout.main_tab_indicator, null);
		indicator.setBackgroundColor(getResources().getColor(R.color.tab_widget_bg_color));
		ImageView icon = (ImageView) indicator.findViewById(R.id.icon);
		icon.setImageResource(drawableRes);
		
		TextView label = (TextView) indicator.findViewById(R.id.label);
		label.setText(labelRes);
				
		return indicator;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_feedback:
			Intent intent = new Intent(this, TermActivity.class);
			intent.putExtra(TermActivity.KEY_TOPBAR_TITLE, getResources().getString(R.string.feed_back));
			intent.putExtra(TermActivity.KEY_OPEN_URL, IDataConstant.URL_FEEDBACK);
			startActivity(intent);
			break;
		case R.id.item_setting:
			mTabHost.setCurrentTabByTag(TAB_MANAGE);
			ManageAcitivty manageActivity = (ManageAcitivty) getLocalActivityManager().getActivity(TAB_MANAGE);
			manageActivity.switchToSettingFragment();
			break;
		case R.id.item_exit:
			int runningCount = DataCenter.getInstance().getTaskList().getRunningAndWaitingTaskCount();
			if (runningCount > 0) {
				showQuitDialog(runningCount);
				break;
			}	
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		
		return true;
	}


	@Override
	public void onBackPressed() {
		int runningCount = DataCenter.getInstance().getTaskList().getRunningAndWaitingTaskCount();
		if (runningCount > 0) {
			showQuitDialog(runningCount);
			return;
		}
		
		if (!mEnsureBack) {
			if (mEnsureBackToast == null) {
				mEnsureBackToast = Toast.makeText(this, R.string.ensure_exit, Toast.LENGTH_SHORT);
			}
			mEnsureBackToast.show();
			mEnsureBack = true;
			mTabHost.postDelayed(new Runnable() {
				@Override
				public void run() {
					mEnsureBack = false;
				}
			}, 2000);
			return;
		}
		
		super.onBackPressed();
	}

	private void showQuitDialog(int runningCount) {
		String message = String.format(Locale.getDefault(),
				getString(R.string.has_task_downloading), runningCount);
		
		final CommonDialog quitDialog = new CommonDialog(this);
		quitDialog.setTitle("确认退出?");
		quitDialog.setMessage(message);
		quitDialog.setCheckBox(true, R.string.continue_downloading_in_background);
		quitDialog.setNegativeButton(R.string.dialog_cancel, null);
		quitDialog.setPositiveButton(R.string.dialog_ok, false, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!quitDialog.isChecked()) {
					Intent service = new Intent(MainActivity.this, DownloadService.class);
					service.setAction(DownloadService.ACTION_STOP_ALL_TASK);
					startService(service);
				}
				finish();
			}
		});
		quitDialog.show();
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (dataId == IDataConstant.UPDATE_INFO) {
			if (!resp.mSuccess || resp.mData == null ||  !(resp.mData instanceof UpdateInfo))
				return;
			
			UpdateQuery.onQuerySuccessful(this);
			UpdateInfo info = (UpdateInfo) resp.mData;
			int count = UpdateQuery.parseUpdateCount(this, info);
			setManageTabIndicatorCount(count);
			AppSettings.setUpdateCount(this, count);
		} else if (dataId == IDataConstant.CHECK_UPDATE) {
			if (resp.mSuccess && resp.mData != null && resp.mData instanceof CheckForUpdate) {
				CheckForUpdate info = (CheckForUpdate) resp.mData;
				String packageName = getPackageName();
				PackageInfo pkgInfo = null;
				try {
					pkgInfo = getPackageManager().getPackageInfo(packageName, 0);
				} catch (NameNotFoundException e) { }
				if (pkgInfo == null)
					return;
				if (TextUtils.isEmpty(info.mFileUrl) || info.mVersionCode <= pkgInfo.versionCode)
					return;
				
				CommonInvoke.showUpdateDialog(this, info.mVersionCode, info.mVersionName,
						Utils.getSizeString(info.mSize), info.mChangeLog, info.mFileUrl);
			}
		} else if (dataId == IDataConstant.STATISTICS) {
			Statistics.onPostCompleted();
			if (resp.mSuccess) {
				Statistics.clearStats(this);
			}
		}
	}

	private void setManageTabIndicatorCount(int count) {
		View indicator = mTabHost.getTabWidget().getChildTabViewAt(TAB_INDEX_MANAGE);
		TextView countText = (TextView) indicator.findViewById(R.id.count);
		countText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
		countText.setText(count > 99 ? "99+" : String.valueOf(count));
	}

	public void resetDownFloatIconVisible() {
		if(!mIsAnimationRunning) {
			int taskNum = DataCenter.getInstance().getTaskList().getUncompletedTaskCount();
			boolean hasTask = taskNum > 0;
			String numMoreText = "";
			if(taskNum >99)
				numMoreText = "+";
			mDownFloatText.setText(( hasTask ? taskNum : 0) + "" + numMoreText);
			mDownFloatText.setVisibility( hasTask ? View.VISIBLE : View.GONE);
			mDownFloatIcon.setImageResource(hasTask ? R.drawable.searchbar_download_white_bg : R.drawable.searchbar_download_white_icon);
		}
	}
			
	private boolean mIsAnimationRunning = false;
	private AnimationListener mFloatDownAnimEndListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) { 
			mIsAnimationRunning = true;
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationEnd(Animation animation) {
			mIsAnimationRunning = false;
			mAinimation.setVisibility(View.GONE);
			resetDownFloatIconVisible();	
			showNumberAnimation();
		}
	};
	
	@Override
	public void update(Observable observable, Object data) {
		if (data == null || !(data instanceof Message))
			return;
		
		Message msg = (Message) data;
		if (msg.what == DataCenter.MSG_UPDATE_COUNT_CHANGED) {
			int count = AppSettings.getUpdateCount(this);
			setManageTabIndicatorCount(count);
		} else if (msg.what == DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED
				|| msg.what == DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED) {			
			resetDownFloatIconVisible();
		} else if (msg.what == DataCenter.MSG_NEW_DOWN_EVENT) {
			final ImageView originIcon = (ImageView) msg.obj;
			
			mIsAnimationRunning = true;
			mAinimation.postDelayed(new Runnable() {
				@Override
				public void run() {
					showDownAnimation(originIcon);
				}
			}, 500);
		} else if(msg.what == DataCenter.MSG_INSTALL_SIGNATURE_NOTIFY_EVENT) {
			if(Utils.isTopActivity(this)) {
				String packageName = (String)msg.obj;
				if(packageName != null) {
					LocalAppInfo appInfo = DataCenter.getInstance().getLocalApps().getLocalPackage(packageName);
					if(appInfo != null)
						CommonInvoke.showSignatureDiffInstallDialog(this, packageName);
				}
			}
		} else if(msg.what == DataCenter.MSG_WIFI_TO_MOBILE_CHANGED_EVENT) {
			if(Utils.isTopActivity(this)) {
				NetworkStateReceiver.showNetworkChangeQueryDialog(this);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mDownFloatIcon) {
			switchToDownloadPage();
		} else if(v == mSearchEditor) {
			switchToSearchPage();
		}
	}
	
	private void switchToSearchPage() {
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}
	
	private void switchToDownloadPage() {
		Intent intent = new Intent(this, ManageDownloadActivity.class);
		startActivity(intent);
	}
	
    private boolean hasShortcut(Context context, String shortcutName) {
		  String url = "";
		  url = "content://" + getAuthorityFromPermission(context, "com.android.launcher.permission.READ_SETTINGS") + "/favorites?notify=true";
		  ContentResolver resolver = context.getContentResolver();
		  Cursor cursor = resolver.query(Uri.parse(url), new String[] { "title", "iconResource" }, "title=?", new String[] { getString(R.string.app_name).trim() }, null);
		  if (cursor != null && cursor.moveToFirst()) {
			  cursor.close();
	  	   	  return true;
		  }
		  return false;
     }
    
	 private String getAuthorityFromPermission(Context context, String permission) {
	  	 if (permission == null)
	  	   return null;
	  	  List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
	  	  if (packs != null) {
	  		  for (PackageInfo pack : packs) {
	  			  ProviderInfo[] providers = pack.providers;
	  			  if (providers != null) {
	  				  for (ProviderInfo provider : providers) {
	  					  if (permission.equals(provider.readPermission))
	  						  return provider.authority;
	  					  if (permission.equals(provider.writePermission))
	  						  return provider.authority;
	  				  }
	  			  }
	  		  }
	  	  }
	  	 return null;
	}
	 
	 public void createShortCut(Context context, String name) {  
		    final Intent shortcutIntent = new Intent(  
		            "com.android.launcher.action.INSTALL_SHORTCUT");  
		  
		    shortcutIntent.putExtra("duplicate", false);  
		    final Parcelable icon = Intent.ShortcutIconResource.fromContext(  
		            context,  
		            R.drawable.ic_launcher);  
		  
		    // 这个参数是启动的activity的action  
		    final Intent targetIntent = new Intent("android.intent.action.MAIN"); 
		    targetIntent.addCategory("android.intent.category.LAUNCHER");
		  
		    // 目标activity  
		    targetIntent.setClass(MainActivity.this, MainActivity.class);
//		    targetIntent.putExtra("url", url);  
		    targetIntent.putExtra("name", name);  
		    targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		  
		    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent);  
		    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);  
		    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);  
		  
		    context.sendBroadcast(shortcutIntent);  
		} 
}
