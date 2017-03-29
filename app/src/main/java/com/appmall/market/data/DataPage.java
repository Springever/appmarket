package com.appmall.market.data;

import java.util.Map;

import android.text.TextUtils;

public class DataPage {

	private static final String NAME_NEXT_PAGE = "next_page";
	
	private int mPageCount;
	private String mNextUrl;
	
	public int getPageCount() {
		return mPageCount;
	}

	public void increaseCount() {
		mPageCount ++;
	}

	public String getNextUrl() {
		return mNextUrl;
	}

	public void setNextUrl(String url) {
		this.mNextUrl = url;
	}

	public boolean parseNextUrlFromContext(Map<String, String> contextParams) {
		if (contextParams == null || !contextParams.containsKey(NAME_NEXT_PAGE))
			return false;
		
		this.mNextUrl = contextParams.get(NAME_NEXT_PAGE);
		return !TextUtils.isEmpty(this.mNextUrl);
	}
	
}
