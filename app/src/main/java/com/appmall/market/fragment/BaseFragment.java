package com.appmall.market.fragment;

import java.util.Observable;
import java.util.Observer;

import com.appmall.market.data.DataCenter;
import com.appmall.market.download.DownloadTask;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;

/**
 * Fragment基类
 *  
 *
 */
public class BaseFragment extends Fragment implements Observer {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataCenter.getInstance().ensureInit(getActivity());
		DataCenter.getInstance().addObserver(this);
	}

	@Override
	public void onDestroy() {
		DataCenter.getInstance().deleteObserver(this);
		super.onDestroy();
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
			refreshAppData();
			break;
		case DataCenter.MSG_DOWN_EVENT_PROGRESS:
			if (msg.obj != null && msg.obj instanceof DownloadTask) {
				onTaskProgress((DownloadTask) msg.obj);
			}
			break;
		case DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED:
		case DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED:
			refreshAppData();
			break;
		case DataCenter.MSG_NET_STATE_CHANGED:
			onNetworkStateChanged();
			break;
		}
	}
	
	protected void onTaskProgress(DownloadTask task) { }
	
	protected void onNetworkStateChanged() { }
	
	protected void refreshAppData() { }
	
}
