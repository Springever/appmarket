package com.appmall.market.data;

import com.appmall.market.data.DataCenter.Response;

public interface IDataCallback {
	
	public void onDataObtain(int dataId, Response resp);
	
}
