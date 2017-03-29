package com.appmall.market.activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.TabsFragmentAdapter;
import com.appmall.market.adapter.TabsFragmentAdapter.TabChangedListener;
import com.appmall.market.common.Statistics;
import com.appmall.market.fragment.ManageLocalFragment;
import com.appmall.market.fragment.ManageUpdateFragment;
import com.appmall.market.fragment.SettingFragment;
import com.appmall.market.widget.UnderlinePageIndicator;

public class ManageAcitivty extends FragmentActivity implements TabChangedListener, TabsFragmentAdapter.FragmentChangedListener{

	private static final String TAB_TAG_UPDATE = "update";
	private static final String TAB_TAG_LOCAL = "local";
	public static final String TAB_TAG_SETTING = "setting";
	
	private ViewPager mViewPager;
	private TabHost mTabHost;
	private TabsFragmentAdapter mFragmentAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_promote);
		
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
		indicator.setLineWidthPercent(0.7f);
		
		FragmentManager manager = getSupportFragmentManager();
		mTabHost.setup();
		
		mFragmentAdapter = new TabsFragmentAdapter(this, manager, mTabHost, mViewPager, indicator);
		mFragmentAdapter.setOnFragmentChangedListener(this);
		TextView tabIndicator = buildIndicator(R.string.tab_title_manage_update);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_UPDATE).setIndicator(tabIndicator),
				ManageUpdateFragment.class, null);
		
		tabIndicator = buildIndicator(R.string.tab_title_manage_local);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_LOCAL).setIndicator(tabIndicator),
				ManageLocalFragment.class, null);
		
		tabIndicator = buildIndicator(R.string.tab_title_manage_settings);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_SETTING).setIndicator(tabIndicator),
				SettingFragment.class, null);
		
		mFragmentAdapter.setOnTabChangedListener(this);
		
		indicator.setViewPager(mViewPager);
		indicator.setFades(false);		
	}
	
	@Override
	public void onFragmentActive(int pos) {
		FragmentManager manager = getSupportFragmentManager();
		List<Fragment> list = manager.getFragments();
		if(list != null && pos< list.size()) {
			Fragment fragment = list.get(pos);
			if(fragment != null) {
				if(pos == 0)
					Statistics.addManageTab1ClickCount(this);
				else if(pos == 1) {
					Statistics.addManageTab2ClickCount(this);
				}
				else if(pos == 2) {
					Statistics.addManageTab3ClickCount(this);
				}
				else if(pos == 3) {
					Statistics.addManageTab4ClickCount(this);
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		resetDownFloatIcon();
	}

	@Override
	protected void onPause() {
		resetDownFloatIcon();
		super.onPause();
	}

	private TextView buildIndicator(int titleRes) {
		TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.sub_tab_indicator, null);
		indicator.setText(titleRes);
		return indicator;
	}
	
	@Override
	public void onBackPressed() {
		Activity parent = null;
		if ((parent = getParent()) != null) {
			parent.onBackPressed();
		}
	}

	public void switchToUpdateFragment() {
		mTabHost.setCurrentTabByTag(TAB_TAG_UPDATE);
	}
	
	
	public void switchToSettingFragment() {
		mTabHost.setCurrentTabByTag(TAB_TAG_SETTING);
	}
	
	@Override
	public void onTabChanged(String tabId) {
		resetDownFloatIcon();
	}
	
	private void resetDownFloatIcon() {
		Activity parent = getParent();
		if (parent != null && parent instanceof MainActivity) {
			((MainActivity) parent).resetDownFloatIconVisible();
		}
	}
	
}
