package com.appmall.market.data;

/**
 * 数据常量
 *  
 *
 */
public interface IDataConstant {

	public static final int DATA_RESULT_OK = 200;
	
	public static final int HOMEPAGE = 0x0001;
	public static final int HOMEPAGE_MORE = 0x0002;
	public static final int MUST_INSTALL_APP = 0x0003;
	public static final int MUST_INSTALL_GAME = 0x0004;
	
	public static final int APP_CATEGORY = 0x0011;
	public static final int APP_RANK = 0x0012;
	public static final int APP_NEW_PROD = 0x0013;
	
	public static final int GAME_CATEGORY = 0x0021;
	public static final int GAME_RANK = 0x0022;
	public static final int GAME_NEW_PROD = 0x0023;
	
	public static final int SEARCH_TAGS = 0x0031;
	public static final int SEARCH_ASSOCIATION = 0x0032;
	public static final int SEARCH_RESULT = 0x0033;
	
	public static final int UPDATE_INFO = 0x0041;
	
	public static final int APP_DETAIL = 0x0051;
	public static final int TOPIC_DETAIL = 0x0052;
	public static final int CATEGORY_DETAIL_HOT = 0x0053;
	public static final int CATEGORY_DETAIL_NEWPROD = 0x0054;
	public static final int APP_DETAIL_COMMENT = 0x0055;
	
	public static final int COMMENT_APP = 0x0061;
//	public static final int FEED_BACK = 0x0062;
	public static final int CHECK_UPDATE = 0x0063;
	
	public static final int STATISTICS = 0x0071;
	
	public static final int COMMENTPAGE = 0x0081;
	public static final int COMMENTPAGE_MORE = 0x0082;
	
	public static final int TARGET_APP_DETAIL = 1;
	public static final int TARGET_TOPIC_DETAIL = 2;
	public static final int TARGET_APP_CATEGORY_DETAIL = 5;
	public static final int TARGET_GAME_CATEGORY_DETAIL = 8;
	public static final int TARGET_OPEN_URL = 16;
	
	public static final int RESP_REVOTE = 5000800;
	
	public static final String JSON_SUFFIX = ".json";	
	
	public static final String SERVER_VERSION = "/v1.0";
	public static final String URL_HOMEPAGE = SERVER_VERSION + "/home.json";
	public static final String URL_HOMEPAGE_MORE = SERVER_VERSION + "/home-more.json";
	public static final String URL_MUST_INSTALL_APP = SERVER_VERSION + "/soft/must-be-installed.json";
	public static final String URL_MUST_INSTALL_GAME = SERVER_VERSION + "/game/must-be-installed.json";
	public static final String URL_APP_CATEGORY = SERVER_VERSION + "/soft/categories.json";
	public static final String URL_APP_RANK =  SERVER_VERSION + "/soft/rank.json";
	public static final String URL_APP_NEW_PROD = SERVER_VERSION + "/soft/newest.json";
	public static final String URL_GAME_CATEGORY = SERVER_VERSION + "/game/categories.json";
	public static final String URL_GAME_RANK = SERVER_VERSION + "/game/rank.json";
	public static final String URL_GAME_NEW_PROD = SERVER_VERSION + "/game/newest.json";
//	public static final String URL_SEARCH_TAGS = SERVER_VERSION + "/keywords.json";
	public static final String URL_SEARCH_TAGS = SERVER_VERSION + "/keywords-extend.json";
	public static final String URL_SEARCH_RESULT = SERVER_VERSION + "/search.json";
	public static final String URL_ASSOCIATION = SERVER_VERSION + "/auto-complete.json";
	public static final String URL_COMMENT_APP = SERVER_VERSION + "/comment.json";
	public static final String URL_FEEDBACK = "http://api.appchaoshi.cn/v1.0/feedback.html";
	public static final String URL_CHECK_UPDATE = "/check-for-update.json";
	public static final String URL_STATISTICS = SERVER_VERSION + "/stat/recv.json";
	public static final String URL_APP_DETAIL_COMMENT = SERVER_VERSION + "/comment-list.json";
	public static final String URL_UPDATE_INFO =  "/cfappstore/v2/upgrade";

}
