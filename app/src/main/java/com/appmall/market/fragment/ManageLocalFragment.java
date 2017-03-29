package com.appmall.market.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.appmall.market.R;
import com.appmall.market.adapter.ItemDataDef;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.adapter.LocalAppAdapter;
import com.appmall.market.adapter.LocalAppAdapter.Callback;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.LocalApps.LocalAppInfo;

/**
 * 本地应用管理页面
 *  
 *
 */
public class ManageLocalFragment extends BaseFragment implements Callback, Observer {

	private View mContentView;
	private ListView mListView;
	private LocalAppAdapter mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}
		
		Context context = getActivity();
		mContentView = inflater.inflate(R.layout.fragment_manage_local, null);
		mListView = (ListView) mContentView.findViewById(R.id.list_view);
		
		mAdapter = new LocalAppAdapter(context);
		mAdapter.registerCallback(this);
		mListView.setAdapter(mAdapter);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		
		List<LocalAppInfo> localData = DataCenter.getInstance().requestLocalData();
		buildLocalData(localData);
		
		return mContentView;
	}
	
	@Override
	protected void refreshAppData() {
		List<LocalAppInfo> localData = DataCenter.getInstance().requestLocalData();
		buildLocalData(localData);
	}

	private void buildLocalData(List<LocalAppInfo> data) {
		if (!isAdded())
			return;
		
		if (data == null || data.size() <= 0)
			return;
		
		ArrayList<ItemDataWrapper> items = new ArrayList<ItemDataDef.ItemDataWrapper>();
		for (LocalAppInfo info : data) {
			items.add(new ItemDataWrapper(info, LocalAppAdapter.TYPE_LOCAL_APP));
		}
		mAdapter.setData(items);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onUninstallClick(LocalAppInfo info) {
		if (info != null && !TextUtils.isEmpty(info.mPackageName)); {
			Utils.reqSystemUninstall(getActivity(), info.mPackageName);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		List<LocalAppInfo> localData = DataCenter.getInstance().requestLocalData();
		buildLocalData(localData);
	}

}
