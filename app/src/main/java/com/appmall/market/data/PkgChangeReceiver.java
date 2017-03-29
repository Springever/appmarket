package com.appmall.market.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.appmall.market.download.DownloadService;

public class PkgChangeReceiver extends BroadcastReceiver {
	
	private static final String PACKAGE_PREFIX = "package:";

	@Override
	public void onReceive(Context context, Intent intent) {
		String dataString = intent.getDataString();
		String pkgName = null;
		if (!TextUtils.isEmpty(dataString)) {
			int index = dataString.indexOf(PACKAGE_PREFIX);
			if (index == 0) {
				pkgName = dataString.substring(PACKAGE_PREFIX.length());
			}
		}
		
		Intent service = new Intent(context, DownloadService.class);
		service.setAction(DownloadService.ACTION_PACKAGE_CHANGED);
		service.putExtra(DownloadService.EXTRA_PACKAGE_NAME, pkgName);
		context.startService(service);
	}

}
