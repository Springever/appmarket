/*
 *****************************************************************************
 * Copyright (C) 2005-2011 UCWEB Corporation. All Rights Reserved
 * File        : HttpTaskMgr.java
 * Description : HTTPTaskMgr�¼�֪ͨ�Ĳ������Ͷ���
 * Creation    : 2011/07/18
 * Author      : zhangxm@ucweb.com
 * History     : 
 ******************************************************************************
**/

package com.appmall.network;

import java.util.ArrayList;

import com.appmall.market.download.DownloadTask.ThreadDownloadTask;

public class HttpTaskEventArg
{
	public long mlen = 0;
	public long mTotal = 0;
	public byte[] buffer;
	public int mErrorId = 0;
	public ArrayList<ThreadDownloadTask> mThreadDownloadTaskList = new ArrayList<ThreadDownloadTask>(3);
}