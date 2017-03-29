package com.appmall.market.bean;

import java.util.ArrayList;

public class AppRankGroupManage {
	
	private ArrayList<AppRankGroup> mGroupList = new ArrayList<AppRankGroup>();
	
	public AppRankGroupManage() {
		
		//test
		{
			AppRankGroup group = new AppRankGroup();
			group.mGroupId = 0;
			group.mGroupRank = 0;
			group.mGroupTitle = "test0";
			mGroupList.add(group);
		}
		{
			AppRankGroup group = new AppRankGroup();
			group.mGroupId = 1;
			group.mGroupRank = 1;
			group.mGroupTitle = "test1";
			mGroupList.add(group);
		}
		
		{
			AppRankGroup group = new AppRankGroup();
			group.mGroupId = 2;
			group.mGroupRank = 2;
			group.mGroupTitle = "test2";
			mGroupList.add(group);
		}
		
		{
			AppRankGroup group = new AppRankGroup();
			group.mGroupId = 3;
			group.mGroupRank = 3;
			group.mGroupTitle = "test3";
			mGroupList.add(group);
		}	
	}
	
	public int getRankbyGroupId(int groupId) {
		int rank = -1;
		for(AppRankGroup group : mGroupList) {
			if(group.mGroupId == groupId) {
				rank = group.mGroupRank;
				break;
			}
		}
		return rank;
	}
	
	public ArrayList<AppRankGroup> getList() {
		return mGroupList;
	}
}