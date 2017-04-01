package com.appmall.market.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.adapter.AppUpdateAdapter;
import com.appmall.market.adapter.AppUpdateAdapter.Callback;
import com.appmall.market.adapter.ItemBuilder.UpdateInfoHolder;
import com.appmall.market.bean.AppUpdate;
import com.appmall.market.bean.HotPromoteTop;
import com.appmall.market.bean.UpdateInfo;
import com.appmall.market.common.AppSettings;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.UpdateIgnore;
import com.appmall.market.common.UpdateQuery;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataBase;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.data.LocalApps;
import com.appmall.market.download.DownloadService;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskList;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase;
import com.appmall.market.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.appmall.market.pulltorefresh.library.PullToRefreshExpandableListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 软件更新页面
 */
public class ManageUpdateFragment extends BaseFragment implements IDataCallback, Callback, OnClickListener {

    private View mContentView;

    private PullToRefreshExpandableListView mListViewContainer;

    private ExpandableListView mListView;

    private AppUpdateAdapter mUpdateAdapter;

    private UpdateInfo mInfo;

    private Button mButtonUpdateAll;

    private Context mContext;

    private boolean isDebug = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (mContentView != null) {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            parent.removeView(mContentView);
            return mContentView;
        }

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_manage_update, null);

        mButtonUpdateAll = (Button) mContentView.findViewById(R.id.updateall_button);
        mButtonUpdateAll.setOnClickListener(this);

        mListViewContainer = (PullToRefreshExpandableListView) mContentView.findViewById(R.id.ptr_listview);
        mListViewContainer.setOnRefreshListener(mOnRefreshListener);
        Utils.setupPTRLoadingProxy(mContext.getResources(), mListViewContainer.getLoadingLayoutProxy());
        mListView = mListViewContainer.getRefreshableView();
        mListView.setEmptyView(mContentView.findViewById(android.R.id.empty));
        mUpdateAdapter = new AppUpdateAdapter(mContext);
        mUpdateAdapter.registerCallback(this);

        mListView.setAdapter(mUpdateAdapter);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                mListView.expandGroup(groupPosition);
            }
        });
        mListView.expandGroup(AppUpdateAdapter.GROUP_UPDATE);
        mListView.expandGroup(AppUpdateAdapter.GROUP_IGNORE);
        requestData(true);
        return mContentView;
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonUpdateAll) {
            int nUpdateCount = mUpdateAdapter.getChildrenCount(AppUpdateAdapter.GROUP_UPDATE);
            for (int i = 0; i < nUpdateCount; i++) {
                AppUpdate appUpdate = (AppUpdate) mUpdateAdapter.getChild(AppUpdateAdapter.GROUP_UPDATE, i);
                DownloadTask task = getDownloadTaskByPkgname(appUpdate.mPackageName);
                if (task != null) {
                    if (task.mStatus == TaskStatus.STATUS_PAUSE || task.mStatus == TaskStatus.STATUS_FAILED) {
                        Intent service = new Intent(mContext, DownloadService.class);
                        service.setAction(DownloadService.ACTION_RESUME_TASK);
                        service.putExtra(DownloadService.EXTRA_TASK, task);
                        mContext.startService(service);
                    } else if (task.mStatus == TaskStatus.STATUS_INSTALLED) {
                        Intent service = new Intent(mContext, DownloadService.class);
                        service.setAction(DownloadService.ACTION_EXIST_UPDATE);
                        service.putExtra(DownloadService.EXTRA_TASK, task);
                        mContext.startService(service);
                    }
                } else {
                    task = new DownloadTask();
                    task = DownloadTask.buildNewTask(appUpdate.mPackageName, DownloadTask.SOURCE_UPDATE_LIST, appUpdate.mChannelId);
                    task.mTitle = appUpdate.mLabel;
                    task.mVersionCode = appUpdate.mVersionCode;
                    task.mVersionName = appUpdate.mVersion;
                    task.mTaskUrl = appUpdate.mDownloadPath;
                    task.mLocalPath = CommonInvoke.generateDownloadPath(mContext, appUpdate.mPackageName, appUpdate.mVersionCode, appUpdate.mVersion, task.mIsPatch);

                    Intent service = new Intent(mContext, DownloadService.class);
                    service.setAction(DownloadService.ACTION_START_TASK);
                    service.putExtra(DownloadService.EXTRA_TASK, task);

                    File file = new File(task.mLocalPath);
                    if (!CommonInvoke.ensureStorageSpaceEnough(getActivity(), file.getParent(), appUpdate.mFileSize))
                        break;
                    mContext.startService(service);
                }
            }
        }
    }

    private DownloadTask getDownloadTaskByPkgname(String pkgName) {
        DownloadTask downloadTask = null;
        TaskList taskList = DataCenter.getInstance().getTaskList();
        List<DownloadTask> downloadList = taskList.getTaskList();
        for (DownloadTask task : downloadList) {
            if (task.mPackageName.equalsIgnoreCase(pkgName)) {
                downloadTask = task;
                break;
            }
        }
        return downloadTask;
    }

    @Override
    protected void onTaskProgress(DownloadTask task) {
        if (task == null || TextUtils.isEmpty(task.mPackageName))
            return;
        Utils.handleButtonProgress(mContentView, R.id.update_button, task);
    }

    private void requestData(boolean allowCache) {
        if (!mRefresh)
            Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
        if (!isDebug) {
            UpdateQuery.startQuery(getActivity(), this, allowCache);
        } else {
            AppSettings.setUpdateQueryTime(mContext, System.currentTimeMillis());
            DataCenter.getInstance().requestAllLocalPackage();
            onDataObtain(IDataConstant.UPDATE_INFO,getResponseDemo());
        }
    }

    @Override
    public void onDataObtain(int dataId, Response resp) {
        if (!isAdded())
            return;
        if (dataId != IDataConstant.UPDATE_INFO || !resp.mSuccess || resp.mData == null || !(resp.mData instanceof UpdateInfo)) {
            Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
            if (!mRefresh)
                showLoadingFailedLayout();
            else
                Toast.makeText(mContext, mContext.getResources().getString(R.string.update_refresh_error), Toast.LENGTH_SHORT).show();
            return;
        }
        UpdateQuery.onQuerySuccessful(getActivity());
        mContentView.findViewById(R.id.updateall_button).setVisibility(View.VISIBLE);
        mContentView.findViewById(R.id.ptr_listview).setVisibility(View.VISIBLE);

        mInfo = (UpdateInfo) resp.mData;
        {
            Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
            mListViewContainer.onRefreshComplete();
            showData();

        }
    }

    @Override
    protected void onNetworkStateChanged() {
        if (!isAdded())
            return;

        if (mContentView.findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE)
            showLoadingFailedLayout();
    }

    private void showData() {
        if (mInfo == null)
            return;

        DataCenter dc = DataCenter.getInstance();
        Set<String> ignoreSet = UpdateIgnore.getIgnorePackageSet(getActivity());

        List<AppUpdate> update = new ArrayList<AppUpdate>();
        List<AppUpdate> ignore = new ArrayList<AppUpdate>();

        if (mInfo.mUpdates != null) {
            for (AppUpdate au : mInfo.mUpdates) {
                int status = dc.getPackageInstallStatus(au.mPackageName, au.mVersionCode, au.mVersion);
                if (status != LocalApps.STATUS_INSTALLED_OLD_VERSION)
                    continue;
                if (ignoreSet.contains(au.mPackageName)) {
                    ignore.add(au);
                } else {
                    update.add(au);
                }
                DownloadTask task = dc.getTask(au.mPackageName);
                au.setInstStatus(dc.getPackageInstallStatus(au.mPackageName, au.mVersionCode, au.mVersion));
                au.setTaskStatus(task == null ? TaskStatus.STATUS_UNKNOWN : task.mStatus);
            }
        }
        mUpdateAdapter.setData(update, ignore);
        showAllUpdateText(update);
        checkAllUpdateVisible(update);
        int count = update.size();
        AppSettings.setUpdateCount(getActivity(), count);
        dc.reportUpdateCountChanged();
    }

    private long getUpdateAppSize(AppUpdate updateApp) {
        long updateAppSize = updateApp.mFileSize;
        int instStatus = updateApp.getInstStatus();
        int taskStatus = updateApp.getTaskStatus();
        if (instStatus == LocalApps.STATUS_INSTALLED) {
            updateAppSize = 0;
        }
        if (taskStatus != TaskStatus.STATUS_UNKNOWN && taskStatus != TaskStatus.STATUS_INSTALLED) {
            updateAppSize = 0;
        }

        return updateAppSize;
    }

    private long getUpdateAppSaveSize(AppUpdate updateApp) {
        long saveSize = 0;
        if (updateApp.mHasPatch && updateApp.mPatchSize > 0) {
            saveSize = (updateApp.mFileSize - updateApp.mPatchSize);
            int instStatus = updateApp.getInstStatus();
            int taskStatus = updateApp.getTaskStatus();
            if (instStatus == LocalApps.STATUS_INSTALLED) {
                saveSize = 0;
            }
            if (taskStatus != TaskStatus.STATUS_UNKNOWN && taskStatus != TaskStatus.STATUS_INSTALLED) {
                saveSize = 0;
            }
        }
        return saveSize;
    }

    private void showAllUpdateText(List<AppUpdate> updatelist) {
        long size = 0;
        long patchDiffSize = 0;
        for (AppUpdate au : updatelist) {
            size += getUpdateAppSize(au);
            if (au.mHasPatch && au.mPatchSize > 0)
                patchDiffSize += getUpdateAppSaveSize(au);
        }
        String text = Utils.getTotalUpdateSizeString(size - patchDiffSize) + (patchDiffSize > 0 ? "(共省" + Utils.getSizeString(patchDiffSize) + ")" : "");
        mButtonUpdateAll.setText(text);
    }

    private void checkAllUpdateVisible(List<AppUpdate> updatelist) {
        boolean bVisible = true;
        if (updatelist.size() == 0)
            bVisible = false;
        bVisible = true;
        for (AppUpdate appUpdate : updatelist) {
            boolean bFind = getUpdateAppSize(appUpdate) > 0 ? true : false;
            if (bFind) {
                bVisible = true;
                break;
            } else
                bVisible = false;
        }
        if (bVisible)
            mButtonUpdateAll.setVisibility(View.VISIBLE);
        else
            mButtonUpdateAll.setVisibility(View.GONE);
    }

    private void showLoadingFailedLayout() {
        View failedLayout = mContentView.findViewById(R.id.loading_failed_layout);
        TextView resultText = (TextView) failedLayout.findViewById(R.id.failed_result);
        TextView tipText = (TextView) failedLayout.findViewById(R.id.failed_tip);
        Button failedButton = (Button) failedLayout.findViewById(R.id.failed_tip_button);
        failedLayout.setVisibility(View.VISIBLE);
        boolean hasNetwork = Utils.isNetworkAvailable(getActivity());
        if (hasNetwork) {
            resultText.setText(R.string.network_not_good);
            tipText.setText(R.string.click_button_refresh_later);
            failedButton.setText(R.string.click_to_refresh);
            failedButton.setOnClickListener(mOnRefreshButtonClicked);
        } else {
            resultText.setText(R.string.network_not_connected);
            tipText.setText(R.string.click_button_setting_network);
            failedButton.setText(R.string.network_setting);
            failedButton.setOnClickListener(mOnSettingNetworkClicked);
        }
    }

    @Override
    public void onUpdate(UpdateInfoHolder updateInfo) {
        CommonInvoke.processUpdateBtn(getActivity(), updateInfo.mIcon, updateInfo.mItem);
    }

    @Override
    public void onIgnore(AppUpdate item) {
        UpdateIgnore.onIgnore(getActivity(), item);
        showData();
    }

    @Override
    public void onRemoveIgnore(AppUpdate item) {
        UpdateIgnore.onRemoveIgnore(getActivity(), item);
        showData();
    }

    @Override
    protected void refreshAppData() {
        showData();
    }

    private OnClickListener mOnRefreshButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            View failedLayout = mContentView.findViewById(R.id.loading_failed_layout);
            failedLayout.setVisibility(View.GONE);
            requestData(true);
        }
    };

    private OnClickListener mOnSettingNetworkClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.jumpNetworkSetting(getActivity());
        }
    };

    private boolean mRefresh = false;
    private OnRefreshListener<ExpandableListView> mOnRefreshListener = new OnRefreshListener<ExpandableListView>() {
        @Override
        public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
            mRefresh = true;
            requestData(false);
        }
    };

    private static final String NAME_CONTEXT = "context";
    private static final String NAME_ENTITIES = "entities";

    public DataCenter.Response getResponseDemo() {
        int dataId = IDataConstant.UPDATE_INFO;
        DataCenter.Response resp = null;
        JSONObject jsonObj = null;
        try {
            resp = new DataCenter.Response();
            byte[] bytes = Utils.readFromAsset(mContext, "preload/update.json");
            if (bytes != null) {
                jsonObj = new JSONObject(new String(bytes));
            } else {
                return null;
            }
            JSONObject jsonContext = jsonObj.optJSONObject(NAME_CONTEXT);
            if (jsonContext != null) {
                resp.mContext = new HashMap<String, String>();
                Iterator<String> iterator = jsonContext.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    resp.mContext.put(key, jsonContext.getString(key));
                }
            }
            JSONObject entities = jsonObj.optJSONObject(NAME_ENTITIES);
            if (entities != null) {
                resp.mData = convertEntitiesToData(dataId, entities);
            }
            resp.mSuccess = true;
        } catch (Exception e) {
        }
        return resp;
    }

    private IDataBase convertEntitiesToData(int dataId, JSONObject entities) throws JSONException {
        switch (dataId) {
            case IDataConstant.HOMEPAGE:
                HotPromoteTop home = new HotPromoteTop();
                home.readFromJSON(entities);
                return home;
            case IDataConstant.UPDATE_INFO:
                UpdateInfo info = new UpdateInfo();
                info.readFromJSON(entities);
                return info;
            default:
                return null;
        }
    }
}
