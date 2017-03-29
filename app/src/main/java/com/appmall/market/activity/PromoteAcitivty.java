package com.appmall.market.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.TabsFragmentAdapter;
import com.appmall.market.fragment.PromoteBibeiFragment;
import com.appmall.market.fragment.PromoteHotFragment;
import com.appmall.market.widget.UnderlinePageIndicator;

import android.support.v4.app.Fragment;
import java.util.List;

public class PromoteAcitivty extends FragmentActivity implements TabsFragmentAdapter.FragmentChangedListener{

	private static final String TAB_TAG_HOT = "hot";
	private static final String TAB_TAG_BIBEI = "bibei";
	private static final String TAB_TAG_BIWAN = "biwan";
	
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
		
		TextView tabIndicator = buildIndicator(R.string.tab_title_promote_hot);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_HOT).setIndicator(tabIndicator),
				PromoteHotFragment.class, null);
		
		Bundle args = new Bundle();
		tabIndicator = buildIndicator(R.string.tab_title_promote_bibei);
		args.putInt(PromoteBibeiFragment.EXTRA_BIBEI_TYPE, PromoteBibeiFragment.BIBEI_TYPE_SOFT);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_BIBEI).setIndicator(tabIndicator),
				PromoteBibeiFragment.class, args);
		
		args = new Bundle();
		tabIndicator = buildIndicator(R.string.tab_title_promote_biwan);
		args.putInt(PromoteBibeiFragment.EXTRA_BIBEI_TYPE, PromoteBibeiFragment.BIBEI_TYPE_GAME);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_BIWAN).setIndicator(tabIndicator),
				PromoteBibeiFragment.class, args);

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
					((PromoteHotFragment)fragment).onFragmentActive();
				if(pos == 1 || pos == 2) {
					((PromoteBibeiFragment)fragment).onFragmentActive();
				}
			}
		}
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

}
