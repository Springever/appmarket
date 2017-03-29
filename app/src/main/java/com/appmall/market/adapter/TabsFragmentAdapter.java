package com.appmall.market.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

import com.appmall.market.widget.UnderlinePageIndicator;

public class TabsFragmentAdapter extends FragmentPagerAdapter implements OnPageChangeListener, OnTabChangeListener {

	private Context mContext;
	private ArrayList<TabInfo> mTabs;
	private TabHost mTabHost;
	private UnderlinePageIndicator mIndicator;
	private ViewPager mViewPager;
	private TabChangedListener mTabListener;
	private FragmentChangedListener mFragmentListener;
	
	public static interface TabChangedListener {
		public void onTabChanged(String tabId);
	}
	
	public static interface FragmentChangedListener {
		public void onFragmentActive(int pos);
	}
    
	static final class TabInfo {
        private final Class<?> mClass;
        private final Bundle mArgs;

        TabInfo(Class<?> klass, Bundle args) {
        	mClass = klass;
        	mArgs = args;
        }
    }
	
	static class TabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
	
    public TabsFragmentAdapter(Context context, FragmentManager fm, TabHost tabHost,
    		ViewPager pager, UnderlinePageIndicator indicator) {
        super(fm);
        
        mContext = context;
        mTabHost = tabHost;
        mViewPager = pager;
        mIndicator = indicator;
        mTabs = new ArrayList<TabsFragmentAdapter.TabInfo>();
        
        mViewPager.setAdapter(this);
        mTabHost.setOnTabChangedListener(this);
        mIndicator.setOnPageChangeListener(this);
    }
    
	@Override
    public Fragment getItem(int position) {
		TabInfo info = mTabs.get(position);
		Fragment fragment = Fragment.instantiate(mContext, info.mClass.getName(), info.mArgs);
		return fragment;
    }

    @Override
    public int getCount() {
    	return mTabs == null ? 0 : mTabs.size();
    }

    public void setOnTabChangedListener(TabChangedListener l) {
    	mTabListener = l;
    }
    
    public void setOnFragmentChangedListener(FragmentChangedListener l) {
    	mFragmentListener = l;
    }
    
    public void addTab(TabSpec tabSpec, Class<?> klass, Bundle args) {
		tabSpec.setContent(new TabFactory(mContext));

        TabInfo info = new TabInfo(klass, args);
        mTabs.add(info);
        mTabHost.addTab(tabSpec);
        notifyDataSetChanged();
	}

	@Override
	public void onTabChanged(String tabId) {
		int position = mTabHost.getCurrentTab();
        mViewPager.setCurrentItem(position);
        
        if (mTabListener != null) {
        	mTabListener.onTabChanged(tabId);
        }
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	private boolean mInitNofify = false;
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		if(position ==0 && positionOffset == 0 && !mInitNofify && mFragmentListener != null) {
			mInitNofify = true;
			mFragmentListener.onFragmentActive(0);
		}		
	}

	@Override
	public void onPageSelected(int position) {
        TabWidget widget = mTabHost.getTabWidget();
        int oldFocusability = widget.getDescendantFocusability();
        widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mTabHost.setCurrentTab(position);
        widget.setDescendantFocusability(oldFocusability);
        if(mFragmentListener != null)
        	mFragmentListener.onFragmentActive(position);
	}

}
