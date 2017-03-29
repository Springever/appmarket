package com.appmall.market.download;

/**
 * 定义了更新列表项的状态
 *  
 *
 */
public interface TaskStatus  {
	
	/** 状态未知 */
	public static final int STATUS_UNKNOWN = 0;
	/** 软件正在下载中 */
	public static final int STATUS_DOWNLOADING = 1;
	/** 软件在队列中等待 */
	public static final int STATUS_WAIT = 2;
	/** 软件暂停中(或失败) */
	public static final int STATUS_PAUSE = 3;
	/** 软件下载完毕 */
	public static final int STATUS_DOWNLOAD = 4;
	/** 软件下载失败 */
	public static final int STATUS_FAILED = 5;
	/** 软件正在安装中 */
	public static final int STATUS_INSTALLING = 6;
	/** 软件已经安装 */
	public static final int STATUS_INSTALLED = 7;
	/** 软件正在合并中 */
	public static final int STATUS_MERGING = 8; 
	/** 软件合并安装 */
	public static final int STATUS_MERGING_INSTALL = 9;
}
