package com.appmall.market.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.TabsFragmentAdapter;
import com.appmall.market.fragment.AppCategoryFragment;
import com.appmall.market.fragment.AppNewProdFragment;
import com.appmall.market.fragment.AppRankFragment;
import com.appmall.market.widget.UnderlinePageIndicator;

public class ApplicationAcitivty extends FragmentActivity implements TabsFragmentAdapter.FragmentChangedListener{

	private static final String TAB_TAG_CATEGORY = "category";
	private static final String TAB_TAG_RANK = "rank";
	private static final String TAB_TAG_NEW_PROD = "prod";
	
	public static final String EXTRA_TYPE = "type";
	public static final int TYPE_APP = 0;
	public static final int TYPE_GAME = 1;
	
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
		
		int type = TYPE_APP;
		Intent intent = getIntent();
		if (intent != null) {
			type = intent.getIntExtra(EXTRA_TYPE, TYPE_APP);
		}
		
		Bundle argCategory = new Bundle();
		Bundle argRank = new Bundle();
		Bundle argNewProd = new Bundle();
		if (type == TYPE_APP) {
			argCategory.putInt(AppCategoryFragment.EXTRA_CATEGORY_TYPE, AppCategoryFragment.CATEGORY_TYPE_SOFT);
			argRank.putInt(AppRankFragment.EXTRA_CATEGORY_TYPE, AppRankFragment.CATEGORY_TYPE_SOFT);
			argNewProd.putInt(AppNewProdFragment.EXTRA_CATEGORY_TYPE, AppNewProdFragment.CATEGORY_TYPE_SOFT);
		} else {
			argCategory.putInt(AppCategoryFragment.EXTRA_CATEGORY_TYPE, AppCategoryFragment.CATEGORY_TYPE_GAME);
			argRank.putInt(AppRankFragment.EXTRA_CATEGORY_TYPE, AppRankFragment.CATEGORY_TYPE_GAME);
			argNewProd.putInt(AppNewProdFragment.EXTRA_CATEGORY_TYPE, AppNewProdFragment.CATEGORY_TYPE_GAME);
		}
		int resTab1 = R.string.tab_title_app_category;
		int resTab2 = R.string.tab_title_app_rank;
		int resTab3 = R.string.tab_title_app_new_prod;
		if(type == TYPE_GAME) {
			resTab1 = R.string.tab_title_game_category;
			resTab2 = R.string.tab_title_game_rank;
			resTab3 = R.string.tab_title_game_new_prod;
		}
		mFragmentAdapter = new TabsFragmentAdapter(this, manager, mTabHost, mViewPager, indicator);
		mFragmentAdapter.setOnFragmentChangedListener(this);
		TextView tabIndicator = buildIndicator(resTab1);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_CATEGORY).setIndicator(tabIndicator),
				AppCategoryFragment.class, argCategory);
		
		tabIndicator = buildIndicator(resTab2);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_RANK).setIndicator(tabIndicator),
				AppRankFragment.class, argRank);
		
		tabIndicator = buildIndicator(resTab3);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_NEW_PROD).setIndicator(tabIndicator),
				AppNewProdFragment.class, argNewProd);
		
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
					((AppCategoryFragment)fragment).onFragmentActive();
				if(pos == 1) {
					((AppRankFragment)fragment).onFragmentActive();
				}
				if(pos == 2) {
					((AppNewProdFragment)fragment).onFragmentActive();
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
