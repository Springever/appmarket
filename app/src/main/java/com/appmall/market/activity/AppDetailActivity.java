package com.appmall.market.activity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.TabsFragmentAdapter;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.bitmaputils.ImageLoader;
import com.appmall.market.data.LocalApps;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.fragment.DetailCommentFragment;
import com.appmall.market.fragment.DetailInfoFragment;
import com.appmall.market.widget.DownStatusButton;
import com.appmall.market.widget.RankStarWidget;
import com.appmall.market.widget.UnderlinePageIndicator;
import com.appmall.market.widget.HorizontalViewPager;
import com.appmall.market.widget.DetailInfoLabelLinearLayout;
import com.appmall.market.widget.DetailInfoLabelLinearLayout.LabelInfo;

/**
 * 应用详情界面
 *  
 *
 */
public class AppDetailActivity extends BaseActivity implements OnClickListener, TabsFragmentAdapter.FragmentChangedListener{
	
	public interface OnActivityResultListener {  
	      public void OnActivityResult(Intent intent);  
	  }
	
	public static final String EXTRA_DETAIL_URL = "rank_url";
	public static final String EXTRA_DETAIL_APPINFO = "appinfo";
	public static final String EXTRA_DETAIL_ADVERTINFO = "advertinfo";
	public static final String EXTRA_DETAIL_TYPE = "type";
	
	public static int DETAIL_DATA_TYPE_APP = 1;
	public static int DETAIL_DATA_TYPE_ADVERT = 2;
	
	private static final String TAB_TAG_DETAILINFO = "detailinfo";
	private static final String TAB_TAG_COMMENT = "comment";
	protected static final int MAX_LINE_COUNT = 2;
	private static final String PERCENT_CHAR = "%";
	
	private HorizontalViewPager mViewPager;
	private TabHost mTabHost;
	private TabsFragmentAdapter mFragmentAdapter;
	
	private ImageView mPauseButton;
	private ImageView mCancelButton;
	private DownStatusButton mStatusButton;
	private DetailInfoLabelLinearLayout mLabelLayout;
	private int mInstStatus;
	
	private DownloadTask mTask;
	private App mApp;
	private Advert mAdvert;
	
	private OnActivityResultListener mListener;
	private int mType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		mPauseButton = (ImageView) findViewById(R.id.pause_button);
		mCancelButton = (ImageView) findViewById(R.id.delete_button);
		mStatusButton = (DownStatusButton) findViewById(R.id.detail_progress);
		mLabelLayout = (DetailInfoLabelLinearLayout) findViewById(R.id.laeblgroup_layout);
		mPauseButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		mStatusButton.setOnClickListener(this);
		findViewById(R.id.back_button).setOnClickListener(this);
		findViewById(R.id.top_bar_search).setOnClickListener(this);
		Utils.scaleClickRect(findViewById(R.id.top_bar_search));
		
		mViewPager = (HorizontalViewPager) findViewById(R.id.view_pager);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
		indicator.setLineWidthPercent(0.7f);
		
		Intent intent = getIntent();
		if(intent != null) {
			mType = intent.getIntExtra(EXTRA_DETAIL_TYPE, -1);
			ImageView iconView = (ImageView) findViewById(R.id.app_icon);
			TextView titleView = (TextView) findViewById(R.id.app_title);
			TextView appInfoView = (TextView) findViewById(R.id.app_info);
			RankStarWidget starView = (RankStarWidget) findViewById(R.id.star);		
			if(mType == DETAIL_DATA_TYPE_APP) {
				Serializable extra = intent.getSerializableExtra(EXTRA_DETAIL_APPINFO);
				if (extra != null && extra instanceof App) {	
					mApp = (App)extra;
					String strDownloadNum="";
					if(mApp.mDownloadNum != null && !mApp.mDownloadNum.equalsIgnoreCase("0"))
						strDownloadNum = " | "+mApp.mDownloadNum + "人下载";
					String appInfo = mApp.mCategoryTitle+" | "+Utils.getSizeString(mApp.mSize)+strDownloadNum;
					appInfoView.setText(appInfo);
					titleView.setText(mApp.mTitle);
					starView.setRank((int)(mApp.mStarLevel*2));
					ImageLoader.getInstance().loadImage(mApp.mIconUrl, iconView);
					mTask = DataCenter.getInstance().getTask(mApp.mPackageName);
					{
						ArrayList<LabelInfo> labelList = new ArrayList<LabelInfo>();
						LabelInfo label;
						
						if(mApp.mOffcialStatus == 1) {
							label = new LabelInfo();
							label.mTitle = "官方版";
							label.mTextColorType = 0;
							labelList.add(label);
						}

						if(mApp.mAdvStatus == 1) {
							label = new LabelInfo();
							label.mTitle = "无广告";
							label.mTextColorType = 0;
							labelList.add(label);
						}
						
						{
							label = new LabelInfo();
							label.mTitle = mApp.mSecurityScan;
							label.mTextColorType = 0;
							labelList.add(label);
						}
						
						{
							label = new LabelInfo();
							label.mTitle = mApp.mChargeDescription;
							if(mApp.mCharge == 1)
								label.mTextColorType = 1;
							else
								label.mTextColorType = 0;
							labelList.add(label);
						}					
						mLabelLayout.addLabelList(labelList);
					}
					rebindDownloadTask(mApp);
				}
			} else if(mType == DETAIL_DATA_TYPE_ADVERT) {
				Serializable extra = intent.getSerializableExtra(EXTRA_DETAIL_ADVERTINFO);
				if (extra != null && extra instanceof Advert) {
					mAdvert = (Advert)extra;
					String strDownloadNum="";
					if(mAdvert.mDownloadNum != null && !mAdvert.mDownloadNum.equalsIgnoreCase("0"))
						strDownloadNum = " | "+mAdvert.mDownloadNum + "人下载";
					String appInfo = mAdvert.mCategoryTitle+" | "+Utils.getSizeString(mAdvert.mSize)+strDownloadNum;
					appInfoView.setText(appInfo);
					titleView.setText(mAdvert.mAppTitle);				
					starView.setRank((int)mAdvert.mStartLevel*2);
					ImageLoader.getInstance().loadImage(mAdvert.mAppIconUrl, iconView);
					mTask = DataCenter.getInstance().getTask(mAdvert.mPackageName);
					{
						ArrayList<LabelInfo> labelList = new ArrayList<LabelInfo>();
						LabelInfo label;
						
						if(mAdvert.mOffcialStatus == 1) {
							label = new LabelInfo();
							label.mTitle = "官方版";
							label.mTextColorType = 0;
							labelList.add(label);
						}

						if(mAdvert.mAdvStatus == 1) {
							label = new LabelInfo();
							label.mTitle = "无广告";
							label.mTextColorType = 0;
							labelList.add(label);
						}
						
						{
							label = new LabelInfo();
							label.mTitle = mAdvert.mSecurityScan;
							label.mTextColorType = 0;
							labelList.add(label);
						}
						
						{
							label = new LabelInfo();
							label.mTitle = mAdvert.mChargeDesc;
							if(mAdvert.mCharge == 1)
								label.mTextColorType = 1;
							else
								label.mTextColorType = 0;
							labelList.add(label);
						}	
						
						mLabelLayout.addLabelList(labelList);
					}
					rebindDownloadTask(mAdvert);
				}
			} else {
				finish();
				return;
			}
		}
		
		FragmentManager manager = getSupportFragmentManager();
		mTabHost.setup();
		
		{
			Bundle args = new Bundle();
			if(mType == DETAIL_DATA_TYPE_APP)
				args.putString(DetailInfoFragment.EXTRA_DETAIL_INFOURL, mApp.mDataSource);
			else
				args.putString(DetailInfoFragment.EXTRA_DETAIL_INFOURL, mAdvert.mDataSource);
			mFragmentAdapter = new TabsFragmentAdapter(this, manager, mTabHost, mViewPager, indicator);
			mFragmentAdapter.setOnFragmentChangedListener(this);
			TextView tabIndicator = buildIndicator(R.string.tab_title_detail_detail);
			mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_DETAILINFO).setIndicator(tabIndicator),
					DetailInfoFragment.class, args);
		}

		{
			Bundle args = new Bundle();
			if(mType == DETAIL_DATA_TYPE_APP)
				args.putInt(DetailCommentFragment.EXTRA_APP_CHANNEL_ID, mApp.mID);
			else
				args.putInt(DetailCommentFragment.EXTRA_APP_CHANNEL_ID, mAdvert.mAppId);
			String titleTab = getResources().getString(R.string.tab_title_detail_comment);
			if(mApp != null && mApp.mVotes >0) {
				titleTab = titleTab + "("+mApp.mVotes+")";
			}
			if(mAdvert != null && mAdvert.mVotes >0) {
				titleTab = titleTab + "("+mAdvert.mVotes+")";
			}
			TextView tabIndicator = buildIndicator(titleTab);
			mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_COMMENT).setIndicator(tabIndicator),
					DetailCommentFragment.class, args);
		}

		indicator.setViewPager(mViewPager);
		indicator.setFades(false);
	}
	
	@Override
	public void onFragmentActive(int pos) {
		FragmentManager manager = getSupportFragmentManager();
		List<Fragment> list = manager.getFragments();
		if(list != null && pos< list.size()) {
			Fragment fragment = list.get(pos);
			if(fragment != null) {
				if(pos == 0)
					((DetailInfoFragment)fragment).onFragmentActive();
				if(pos == 1) {
					((DetailCommentFragment)fragment).onFragmentActive();
				}
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.getFragments().get(0);
		if(fragment != null) {
			if(((DetailInfoFragment)fragment).isBigViewMode()) {
				((DetailInfoFragment)fragment).closeBigViewMode();
				return;
			}				
		}	
		super.onBackPressed();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_button:		
			finish();
			break;
		case R.id.top_bar_search:
			startActivity(new Intent(this, SearchActivity.class));
			break;
		case R.id.detail_progress:
			onStatusButtonClicked();
			break;
		case R.id.pause_button:
			if (mTask != null) {
				if (mTask.mStatus == TaskStatus.STATUS_DOWNLOADING
						|| mTask.mStatus == TaskStatus.STATUS_WAIT) {
					doPause();
				} else if (mTask.mStatus == TaskStatus.STATUS_PAUSE
						|| mTask.mStatus == TaskStatus.STATUS_FAILED) {
					doResume();
				}
			}
			break;
		case R.id.delete_button:
			doDelete();
			break;
		}
	}
	
	public void setActivityResultListener(OnActivityResultListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(DetailCommentFragment.REQUEST_COMMENT == requestCode && RESULT_OK == resultCode) {
			if(mListener != null)
				mListener.OnActivityResult(data);
		}
	}
	
	public App getAppInfo() {
		return mApp;
	}
	
	public Advert getAdvert() {
		return mAdvert;
	}
	
	private void onStatusButtonClicked() {
		int status = mTask == null ? TaskStatus.STATUS_UNKNOWN : mTask.mStatus;
		if (mInstStatus == LocalApps.STATUS_INSTALLED) {
			doOpen();
		} else {
			if (status == TaskStatus.STATUS_DOWNLOAD) {
				doInstall();
			} else {
				doDownload();
			}
		}
	}
	
	@Override
	public void update(Observable observable, Object data) {
		if (data == null || !(data instanceof Message))
			return;
		
		Message msg = (Message) data;
		switch (msg.what) {
		case DataCenter.MSG_LOCAL_APP_CHANGED:
		case DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED:
			if (mApp != null) {
				rebindDownloadTask(mApp);
			} else if(mAdvert != null) {
				rebindDownloadTask(mAdvert);
			}
			break;
		case DataCenter.MSG_DOWN_EVENT_PROGRESS:
		case DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED:
			if (mTask == null) {
				String pkgName = "";
				if(mApp != null)
					pkgName = mApp.mPackageName;
				if(mAdvert != null)
					pkgName = mAdvert.mPackageName;
				mTask = DataCenter.getInstance().getTask(pkgName);
			}
			bindDownloadTask();
			break;
		case DataCenter.MSG_INSTALL_SIGNATURE_NOTIFY_EVENT:
			if(Utils.isTopActivity(this)) {
				String packageName = (String)msg.obj;
				if(packageName != null) {
					LocalAppInfo appInfo = DataCenter.getInstance().getLocalApps().getLocalPackage(packageName);
					if(appInfo != null)
						CommonInvoke.showSignatureDiffInstallDialog(this, packageName);
				}
			}
			break;
		}
	}
	
	public void rebindDownloadTask(App app) {
		String packageName = app.mPackageName;
		int verCode = app.mVersionCode;
		String verName = app.mVersionName;
		mInstStatus = DataCenter.getInstance().getPackageInstallStatus(packageName, verCode, verName);
		mTask = DataCenter.getInstance().getTask( app.mPackageName);
		bindDownloadTask();
	}
	
	public void rebindDownloadTask(Advert advert) {
		String packageName = advert.mPackageName;
		int verCode = advert.mVersionCode;
		String verName = advert.mVersionName;
		mInstStatus = DataCenter.getInstance().getPackageInstallStatus(packageName, verCode, verName);
		mTask = DataCenter.getInstance().getTask( advert.mPackageName);
		bindDownloadTask();
	}
	
	@SuppressWarnings("deprecation")
	private void bindDownloadTask() {
		
		long appSize = 0;
		if(mApp != null) {
			appSize = mApp.mSize;
		}
		if(mAdvert != null)
			appSize = mAdvert.mSize;
		// 获取任务状�?
		Resources res = getResources();
		int status = mTask == null ? TaskStatus.STATUS_UNKNOWN : mTask.mStatus;
		// 获取任务进度
		int progress = 0;
		if (mTask != null && mTask.mTotal > 0) {
			progress = (int) (mTask.mTransfered * 100f / mTask.mTotal);
		}
		
		boolean showCtrlButton = false;
		boolean buttonEnable = false;
		String statusText = null;
		int statusTextColor = 0;
		Drawable background = null;
		Drawable progressDrawable = null;
		
		switch (status) {
		case TaskStatus.STATUS_DOWNLOAD:
			buttonEnable = true;
			showCtrlButton = false;
			progressDrawable = null;
			statusText = res.getString(R.string.install);
			statusTextColor = Color.WHITE;
			background = res.getDrawable(R.drawable.button_green);
			break;
		case TaskStatus.STATUS_INSTALLING:
			buttonEnable = false;
			showCtrlButton = false;
			progressDrawable = res.getDrawable(R.drawable.btn_green_bg);
			statusText = res.getString(R.string.installing);
			statusTextColor = Color.WHITE;
			background = res.getDrawable(R.drawable.button_green);
			break;
		case TaskStatus.STATUS_DOWNLOADING:
		case TaskStatus.STATUS_WAIT:
			buttonEnable = false;
			showCtrlButton = true;
			statusText = progress + PERCENT_CHAR;
			statusTextColor = Color.WHITE;
			background = res.getDrawable(R.drawable.button_gray_download);
			progressDrawable = res.getDrawable(R.drawable.button_green);
			break;
		case TaskStatus.STATUS_FAILED:
		case TaskStatus.STATUS_PAUSE:
			buttonEnable = false;
			showCtrlButton = true;
			statusText = res.getString(R.string.pausing);
			statusTextColor = Color.WHITE;
			background = res.getDrawable(R.drawable.button_gray_download);
			progressDrawable = res.getDrawable(R.drawable.button_green);
			break;
		case TaskStatus.STATUS_UNKNOWN:
		default:
			buttonEnable = true;
			showCtrlButton = false;
			progressDrawable = null;
			if (mInstStatus == LocalApps.STATUS_INSTALLED_OLD_VERSION) {
				statusText = res.getString(R.string.update) + Utils.getSizeString(appSize);
				statusTextColor = Color.WHITE;
				background = res.getDrawable(R.drawable.button_green);
			} else {
				statusText = res.getString(R.string.download)+ Utils.getSizeString(appSize);
				statusTextColor = Color.WHITE;
				background = res.getDrawable(R.drawable.button_green);
			}
			break;
		}
		
		// 应用已安装，强制转为安装状�?
		if (mInstStatus == LocalApps.STATUS_INSTALLED) {
			buttonEnable = true;
			showCtrlButton = false;
			progressDrawable = null;
			statusText = res.getString(R.string.open_software);
			background = res.getDrawable(R.drawable.button_gray);
			statusTextColor = res.getColor(R.color.gray_bg_text_color);
		}
		
		if (status == TaskStatus.STATUS_WAIT || status == TaskStatus.STATUS_DOWNLOADING) {
			mPauseButton.setImageResource(R.drawable.app_detail_pause_button_bg);
		} else if (status == TaskStatus.STATUS_PAUSE || status == TaskStatus.STATUS_FAILED) {
			mPauseButton.setImageResource(R.drawable.app_detail_resume_button_bg);
		} else {
			// Do nothing
		}
		
		mPauseButton.setVisibility(showCtrlButton ? View.VISIBLE : View.INVISIBLE);
		mCancelButton.setVisibility(showCtrlButton ? View.VISIBLE : View.INVISIBLE);
		
		mStatusButton.setEnabled(buttonEnable);
		mStatusButton.setProgress(progress);
		mStatusButton.setProgressVisible(progressDrawable != null);
		mStatusButton.setBackgroundDrawable(background);
		mStatusButton.setProgressDrawable(progressDrawable);
		mStatusButton.setText(statusText);
		mStatusButton.setTextColor(statusTextColor);
	}
	
	private void doOpen() {	
		if(mApp != null)
			Utils.reqSystemOpen(this, mApp.mPackageName);
		else if(mAdvert != null) {
			Utils.reqSystemOpen(this, mAdvert.mPackageName);
		}
	}
	
	private void doInstall() {
		if (mTask == null)
			return;
		
		File apkFile = new File(mTask.mLocalPath);
		if (!apkFile.exists()) {
			CommonInvoke.showReDownloadDialog(this, mTask);
			return;
		}
		
		Utils.doTaskInstall(this, mTask);
	}

	private String mDownloadUrl = null;
	public void setDetailDownloadUrl(String detailDownloadUrl) {
		mDownloadUrl = detailDownloadUrl;
	}
	
	private void doDownload() {
		if(mApp == null && mAdvert == null) return;
		
		String title = "";
		int versionCode = 0;
		String versionName ="";
		String iconUrl = "";
		String downloadUrl = "";
		String packageName = "";
		String signature = "";
		long appSize = 0;
		int channelId = 0;
		
		if(mApp != null) {
			title = mApp.mTitle;
			packageName = mApp.mPackageName;
			versionCode = mApp.mVersionCode;
			versionName = mApp.mVersionName;
			iconUrl = mApp.mIconUrl;
			downloadUrl = mApp.mDownloadUrl;
			appSize = mApp.mSize;
			channelId = mApp.mID;
			signature = mApp.mPackageSignature;
		} else {
			title = mAdvert.mTitle;
			packageName = mAdvert.mPackageName;
			versionCode = mAdvert.mVersionCode;
			versionName = mAdvert.mVersionName;
			iconUrl = mAdvert.mAppIconUrl;
			downloadUrl = mAdvert.mAppUrl;
			appSize = mAdvert.mSize;
			channelId = mAdvert.mAppId;
			signature = mAdvert.mPackageSignature;
		}
		
		int source = DownloadTask.SOURCE_APPMARKET;
		if(mInstStatus == LocalApps.STATUS_INSTALLED_OLD_VERSION)
			source = DownloadTask.SOURCE_UPDATE_OP;
		String appSignature =DataCenter.getInstance().getInstallPackageSignature(packageName);
		if(appSignature != null && !appSignature.equalsIgnoreCase(signature)) {
			if(mApp != null) {
				CommonInvoke.showSignatureDiffConfirmDialog(this, source,null, false, mApp);
			} else {
				CommonInvoke.showSignatureDiffConfirmDialog(this, source,null, false, mAdvert);
			}	
		} else {
			String downUrl = null;
			if(!TextUtils.isEmpty(mDownloadUrl))
				downUrl = mDownloadUrl;
			else
				downUrl = downloadUrl;
			CommonInvoke.addDownloadTask(this, source, packageName, channelId, title, iconUrl, versionCode, versionName, downUrl, appSize, null, false, false);
		}
	}

	private void doDelete() {
		Intent service = new Intent(this, DownloadService.class);
		service.setAction(DownloadService.ACTION_REMOVE_TASK);
		service.putExtra(DownloadService.EXTRA_TASK, mTask);
		startService(service);
	}
	
	private void doPause() {
		Intent service = new Intent(this, DownloadService.class);
		service.setAction(DownloadService.ACTION_STOP_TASK);
		service.putExtra(DownloadService.EXTRA_TASK, mTask);
		startService(service);
	}
	
	private void doResume() {
		Intent service = new Intent(this, DownloadService.class);
		service.setAction(DownloadService.ACTION_RESUME_TASK);
		service.putExtra(DownloadService.EXTRA_TASK, mTask);
		startService(service);
	}
	
	private TextView buildIndicator(int titleRes) {
		TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.sub_tab_indicator, null);
		indicator.setText(titleRes);
		return indicator;
	}
	
	private TextView buildIndicator(String titleIndicator) {
		TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.sub_tab_indicator, null);
		indicator.setText(titleIndicator);
		return indicator;
	}

}
