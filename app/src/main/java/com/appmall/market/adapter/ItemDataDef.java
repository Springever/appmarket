package com.appmall.market.adapter;

import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Category;
import com.appmall.market.bean.Topic;

/**
 * 软件数据结构定义
 *  
 *
 */
public class ItemDataDef {

	/** 用于列表中需要显示多种数据类型 */
	public static class ItemDataWrapper {
		public ItemDataWrapper(Object data, int type) {
			mData = data;
			mItemType = type;
		}
		
		/** 数据 */
		public Object mData;
		
		/** 数据类型，由各Adapter定义维护 */
		public int mItemType;
	}

	/**
	 * 带有评分信息的软件项
	 *  
	 *
	 */
	public static class RankItem {
		public int mType = 0;  // type=0 软件 	type=1 游戏
		public App mLeft;
		public App mRight;
		public boolean mIsLastItem = false;
		
		public RankItem(App left, App right) {
			mLeft = left;
			mRight = right;
		}
	}
	
	/**
	 * 专题项
	 *  
	 *
	 */
	public static class TopicItem {
		public Topic mLeft;
		public Topic mRight;
		
		public TopicItem(Topic left, Topic right) {
			mLeft = left;
			mRight = right;
		}
	}
	
	/**
	 * 首屏广告
	 *  
	 *
	 */
	public static class AdvertItem {
		public Advert mLeft;
		public Advert mRight;
		public boolean mIsLastItem = false;
		
		public AdvertItem(Advert left, Advert right) {
			mLeft = left;
			mRight = right;
		}
	}
	
	/**
	 * 分类类表顶部广告
	 */
	public static class CategoryAdvert {
		public Advert mLeft;
		public Advert mRight;
		
		public CategoryAdvert(Advert left, Advert right) {
			mLeft = left;
			mRight = right;
		}
	}
	
	/**
	 * 分类列表项
	 */
	public static class CategoryItem {
		public Category mLeft;
		public Category mRight;
		public boolean mIsLastItem = false;
		
		public CategoryItem(Category left, Category right) {
			mLeft = left;
			mRight = right;
		}
	}
	
	/**
	 * 专题
	 *  
	 *
	 */
	public static class TopicHeader {
		public String mTitle;
		public String mBrief;
		
		public TopicHeader(String title, String brief) {
			mTitle = title;
			mBrief = brief;
		}
	}
	
}
