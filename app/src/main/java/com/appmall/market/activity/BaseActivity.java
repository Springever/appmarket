package com.appmall.market.activity;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.common.NetworkStateReceiver;
import com.appmall.market.data.DataCenter;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.data.LocalApps.LocalAppInfo;

public class BaseActivity extends FragmentActivity implements Observer  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataCenter.getInstance().ensureInit(this);
		DataCenter.getInstance().addObserver(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		DataCenter.getInstance().deleteObserver(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshAppData();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data == null || !(data instanceof Message))
			return;
		
		Message msg = (Message) data;
		switch (msg.what) {
		case DataCenter.MSG_LOCAL_APP_CHANGED:
		case DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED:
		case DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED:
			refreshAppData();
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
		case DataCenter.MSG_DOWN_EVENT_PROGRESS:
			if (msg.obj != null && msg.obj instanceof DownloadTask) {
				onTaskProgress((DownloadTask) msg.obj);
			}
			break;
		case DataCenter.MSG_NET_STATE_CHANGED:
			onNetworkStateChanged();
			break;
		case DataCenter.MSG_WIFI_TO_MOBILE_CHANGED_EVENT:
			if(Utils.isTopActivity(this)) {
				NetworkStateReceiver.showNetworkChangeQueryDialog(this);
			}
			break;
		}
	}
	
	protected void onNetworkStateChanged() { }
	protected void onTaskProgress(DownloadTask task) { }

	protected void refreshAppData() { }
}
