package com.appmall.market.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
 
public class WifiStateReceiver extends BroadcastReceiver {
	
	public WifiStateReceiver(Context context) {
		
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
		{
			NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(info.getState().equals(NetworkInfo.State.DISCONNECTED))
			{//濡傛灉鏂紑杩炴帴
				Log.d("demo", "wifi淇″彿  DISCONNECTED");
				NetworkStateReceiver.captureRunningTaskList(context);
			} else if(info.getState().equals(NetworkInfo.State.CONNECTED)) {
				Log.d("demo", "wifi淇″彿  CONNECTED");
			} 
		}
	}
	
	public int getStrength(Context context)
	{
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		if (info.getBSSID() != null) {
			int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
			// 閾炬帴閫熷害
//			int speed = info.getLinkSpeed();
//			// 閾炬帴閫熷害鍗曚綅
//			String units = WifiInfo.LINK_SPEED_UNITS;
//			// Wifi婧愬悕绉�
//			String ssid = info.getSSID();
			return strength;
 
		}
		return 0;
	}
 
}