package com.appmall.market.fragment;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.activity.AboutActivity;
import com.appmall.market.activity.TermActivity;
import com.appmall.market.bean.CheckForUpdate;
import com.appmall.market.common.AExecuteAsRoot;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.ClientUpgrade;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.widget.SettingItem;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View.OnClickListener;


public class SettingFragment extends BaseFragment implements OnClickListener , IDataCallback{
	
	public static final int RESULT_SHOULD_EXIT = Activity.RESULT_FIRST_USER;
	
	private SettingItem mAutoInstall;
	private SettingItem mRootInstall;
	private SettingItem mDeleteApk;
	
	
	
	private SettingItem mCheckUpdate;
	private SettingItem mFeedback;
	private SettingItem mAbout;

	private View mContentView;
	Context mContext;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mContext = getActivity();
		mContentView = inflater.inflate(R.layout.fragment_setting, null);
		initView();
		updateSettings();
		return mContentView;
	}
	
	private void initView() {
		mAutoInstall = new SettingItem(mContext, SettingItem.TYPE_CHECKBOX);
		mAutoInstall.setOnClickListener(this);
		mAutoInstall.setTitle(R.string.auto_install);
		mAutoInstall.setDescription(R.string.if_disabled_would_not_auto_install);
		
		if(Utils.isPhoneRoot()) {
			mRootInstall = new SettingItem(mContext, SettingItem.TYPE_CHECKBOX);
			mRootInstall.setOnClickListener(this);
			mRootInstall.setTitle(R.string.root_install);
			mRootInstall.setDescription(R.string.root_install_info);
		}
		
		mDeleteApk = new SettingItem(mContext, SettingItem.TYPE_CHECKBOX);
		mDeleteApk.setOnClickListener(this);
		mDeleteApk.setTitle(R.string.install_success_delete_apk);
		mDeleteApk.setDescription(R.string.delete_apk_info);
				
		LinearLayout optionsLayout = (LinearLayout) mContentView.findViewById(R.id.options_layout);
		optionsLayout.addView(mAutoInstall);
		if(mRootInstall != null)
			optionsLayout.addView(mRootInstall);
		optionsLayout.addView(mDeleteApk);
		
		mCheckUpdate = new SettingItem(mContext, SettingItem.TYPE_DIALOG_TIP);
		mCheckUpdate.setOnClickListener(this);
		mCheckUpdate.setTitle(R.string.check_update);
		
		mFeedback = new SettingItem(mContext, SettingItem.TYPE_DIALOG_TIP);
		mFeedback.setOnClickListener(this);
		mFeedback.setTitle(R.string.feed_back);
		
		mAbout = new SettingItem(mContext, SettingItem.TYPE_DIALOG_TIP);
		mAbout.setOnClickListener(this);
		mAbout.setTitle(R.string.about);
		
		LinearLayout functionsLayout = (LinearLayout) mContentView.findViewById(R.id.functions_layout);
		functionsLayout.addView(mCheckUpdate);
		functionsLayout.addView(mFeedback);
		functionsLayout.addView(mAbout);
		
	}
	
    private class ExecuteAsRoot extends AExecuteAsRoot {  
    	  
        @Override  
        protected ArrayList<String> getCommandsToExecute() {  
            ArrayList<String> list = new ArrayList<String>();  
            list.add("add kill-server");  
            list.add("adb devices");  
            return list;  
        }  
    }  
    
    public boolean execSuperUserCommand() {
    	boolean result = false;
        try {     
            result = new ExecuteAsRoot().execute();
        } catch (Exception e) {  
            e.printStackTrace();  
        }
        return result;
    }
	
	@Override
	public void onClick(View v) {
		if (v == mAutoInstall) {
			boolean b = !AppSettings.isAutoInstall(mContext);
			AppSettings.setAutoInstall(mContext, b);
		} else if (v == mDeleteApk) {
			boolean b = !AppSettings.isInstSuccessRemoveApk(mContext);
			AppSettings.setInstSuccessRemoveApk(mContext, b);
		} else if (v == mRootInstall) {
			boolean bRoot = AppSettings.isRootInstall(mContext);
			if(!bRoot) {
				boolean bOk = execSuperUserCommand();
				if(bOk)
					AppSettings.setRootInstall(mContext, !bRoot);
				else
					Toast.makeText(mContext, mContext.getResources().getString(R.string.root_fail_text), Toast.LENGTH_SHORT).show();
			} else {
				AppSettings.setRootInstall(mContext, !bRoot);
			}
		} else if (v == mCheckUpdate) {
			doCheckUpdate();
		} else if (v == mFeedback) {
			Intent intent = new Intent(mContext, TermActivity.class);		
			intent.putExtra(TermActivity.KEY_TOPBAR_TITLE, mContext.getResources().getString(R.string.feed_back));
			intent.putExtra(TermActivity.KEY_OPEN_URL, IDataConstant.URL_FEEDBACK);
			mContext.startActivity(intent);
		} else if (v == mAbout) {
			startActivity(new Intent(mContext, AboutActivity.class));
		} else {
			throw new RuntimeException();
		}
		
		updateSettings();
	}

	private void updateSettings() {
		boolean b = AppSettings.isAutoInstall(mContext);
		mAutoInstall.setCheckBoxStatus(b);
		if(mRootInstall != null)
			mRootInstall.setCheckBoxStatus(AppSettings.isRootInstall(mContext));
		mDeleteApk.setCheckBoxStatus(AppSettings.isInstSuccessRemoveApk(mContext));
	}
	
	private Toast mToast;
	private void showToast(String toastText) {
		if (mToast == null) {
			mToast = Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(toastText);
		}
		
		mToast.show();
	}
	
	private void doCheckUpdate() {
		showToast(getString(R.string.check_client_update_please_wait));
		ClientUpgrade.startClientQuery(mContext, this);
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (resp.mSuccess && resp.mData != null && resp.mData instanceof CheckForUpdate) {
			CheckForUpdate info = (CheckForUpdate) resp.mData;
			do {
				String packageName = mContext.getPackageName();
				PackageInfo pkgInfo = null;
				try {
					pkgInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
				} catch (NameNotFoundException e) { }
				if (pkgInfo == null)
					break;
				if (TextUtils.isEmpty(info.mFileUrl) || info.mVersionCode <= pkgInfo.versionCode)
					break;
				
				CommonInvoke.showUpdateDialog(getActivity(), info.mVersionCode, info.mVersionName,
						Utils.getSizeString(info.mSize), info.mChangeLog, info.mFileUrl);
				
				// Success, return
				return;
			} while (false);
		}
		
		// 没有更新或更新信息出错，提示无最新版本
		showToast(getString(R.string.your_client_version_is_the_newest));
	}
	
}