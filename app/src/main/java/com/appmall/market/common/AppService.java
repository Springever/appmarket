package com.appmall.market.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.appmall.market.R;
import com.appmall.market.bean.AppUpdate;
import com.appmall.market.bean.UpdateInfo;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.LocalApps;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.bean.CheckForUpdate;
import com.appmall.market.common.ClientUpgrade;

public class AppService extends Service {

	public static final String ACTION_WEEKLY_QUERY_UPDATE = "action_weekly_query_update";
	public static final String ACTION_DAILY_POST_STAT = "action_daily_post_stat";
	public static final String ACTION_WEEKLY_CLIENT_QUERY_UPDATE = "action_weekly_client_query_update";
	
	private int mCommandCount = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		
		DataCenter.getInstance().ensureInit(this);
		mCommandCount = 0;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = null;
		if (intent != null) {
			action = intent.getAction();
		}
		
		incomingCommand();
		
		if (ACTION_WEEKLY_QUERY_UPDATE.equals(action)) {
			boolean started = UpdateQuery.startBackgroundQuery(this, mBackgroundWeeklyQueryCallback);
			if (!started) {
				doCommandCompleted();
			}
		} else if (ACTION_DAILY_POST_STAT.equals(action)) {
			boolean started = Statistics.dailyPost(this, mPostStatCallback);
			if (!started) {
				doCommandCompleted();
			}
		} else if (ACTION_WEEKLY_CLIENT_QUERY_UPDATE.equals(action)) {
			boolean started = ClientUpgrade.startWeeklyQuery(this, mBackgroundWeeklyClientQuery);
			if (!started) {
				doCommandCompleted();
			}
		} else {
			doCommandCompleted();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	private void incomingCommand() {
		mCommandCount ++;
	}
	
	private void doCommandCompleted() {
		mCommandCount --;
		if (mCommandCount <= 0) {
			stopSelf();
		}
	}
	
	private long getUpdateAppSaveSize(AppUpdate updateApp) {
		long saveSize = 0;
		if(updateApp.mHasPatch && updateApp.mPatchSize >0) {
			saveSize = (updateApp.mFileSize-updateApp.mPatchSize);
			int instStatus = updateApp.getInstStatus();
			int taskStatus = updateApp.getTaskStatus();
			if (instStatus == LocalApps.STATUS_INSTALLED) {
				saveSize = 0;
			}
			if(taskStatus != TaskStatus.STATUS_UNKNOWN && taskStatus != TaskStatus.STATUS_INSTALLED) {
				saveSize = 0;
			}
		}
		return saveSize;
	}

	private IDataCallback mBackgroundWeeklyQueryCallback = new IDataCallback() {
		@Override
		public void onDataObtain(int dataId, Response resp) {
			UpdateInfo updateInfo = (UpdateInfo) resp.mData;
			if (resp.mSuccess && resp.mData != null && updateInfo != null) {
				UpdateQuery.onQuerySuccessful(getApplicationContext());
				
				List<String> outLabels = new ArrayList<String>();
				int count = UpdateQuery.parseWeeklyUpdate(AppService.this, (UpdateInfo) resp.mData, outLabels);
				AppSettings.setUpdateCount(AppService.this, count);
				DataCenter.getInstance().reportUpdateCountChanged();
				long patchDiffSize = 0;
				for (AppUpdate au : updateInfo.mUpdates) {
					if(au.mHasPatch && au.mPatchSize >0)
						patchDiffSize += getUpdateAppSaveSize(au);
				}
				Locale locale = Locale.getDefault();
				
				String strFormat = "";
				String strSaveSize = "";
				if(patchDiffSize >0) {
					strFormat = getApplicationContext().getResources().getString(R.string.update_app_notification_text_addsavesize);
					strSaveSize = Utils.getSizeString(patchDiffSize);
				} else {
					strFormat = getApplicationContext().getResources().getString(R.string.update_app_notification_text);					
				}
				String strTitle = String.format(locale, strFormat, outLabels.size());
					
				if(outLabels.size() >0)
					NotificationMgr.showIconNotification(getApplicationContext(), strTitle, outLabels, strSaveSize, NotificationMgr.NOTIFICATION_ACTION_RETURNTOUPDATEPAGE);
			}
			doCommandCompleted();
		}
	};
	
	private IDataCallback mPostStatCallback = new IDataCallback() {
		@Override
		public void onDataObtain(int dataId, Response resp) {
			Statistics.onPostCompleted();
			if (resp.mSuccess) {
				Statistics.clearStats(getApplicationContext());
			}
			doCommandCompleted();
		}
	};
	
	private IDataCallback mBackgroundWeeklyClientQuery = new IDataCallback() {
		@Override
		public void onDataObtain(int dataId, Response resp) {
			if (resp.mSuccess && resp.mData != null && resp.mData instanceof CheckForUpdate) {
				ClientUpgrade.onBackgroundQueryCompleted(getApplicationContext(), resp);
			}
			doCommandCompleted();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
