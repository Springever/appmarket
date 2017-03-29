package com.appmall.market.activity;

import java.util.List;
import java.util.Observable;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TabHost;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.TabsFragmentAdapter;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.fragment.AppNewProdFragment;
import com.appmall.market.fragment.AppRankFragment;
import com.appmall.market.widget.UnderlinePageIndicator;
import com.appmall.market.common.NetworkStateReceiver;
import android.widget.ImageView;
import java.util.Observer;

public class CategoryDetailActivity extends FragmentActivity implements OnClickListener, TabsFragmentAdapter.FragmentChangedListener, Observer{

	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_RANK_URL = "rank_url";
	public static final String EXTRA_NEW_PROD_URL = "new_product_url";

	private static final String TAB_TAG_RANK = "rank";
	private static final String TAB_TAG_NEW_PROD = "prod";
	
	private ViewPager mViewPager;
	private TabHost mTabHost;
	private TabsFragmentAdapter mFragmentAdapter;
	private TextView mTitle;
	private ImageView mDownFloatIcon;
	private TextView mDownFloatText;
	
	private ImageView mAinimation;
	@Deprecated
	private void showDownAnimation(ImageView imageView) {
		Drawable drawable = imageView.getDrawable();
		if (drawable == null)
			return;
		mAinimation.setImageDrawable(drawable);
		mAinimation.setVisibility(View.VISIBLE);
		mAinimation.setAlpha(100);		
		float iconHeight = getResources().getDimension(R.dimen.common_item_icon_size);
		long animTime1 = 100;
		long animTime2 = 800;
		long animTime3 = animTime1+animTime2;
		float offsetY = getResources().getDimension(R.dimen.anim_icon_offset_y);
		Animation scaleAnim0 = new ScaleAnimation(1f, 1.1f, 1f, 1.1f);
		scaleAnim0.setDuration(animTime1);
		Animation scaleAnim1 = new ScaleAnimation(1f, .33f, 1f, .33f);
		scaleAnim1.setDuration(animTime2);
		Animation translateAnim = new TranslateAnimation(0, mDownFloatIcon.getWidth()*2/3, 0, -(iconHeight*2/3+offsetY));
		translateAnim.setDuration(animTime3);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(scaleAnim0);
		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(translateAnim);
		animSet.setAnimationListener(mFloatDownAnimEndListener);
		mAinimation.startAnimation(animSet);
	}
	
	private void showNumberAnimation() {
		Animation scaleAnim1 = new ScaleAnimation(1.0F, 1.1F, 1.0F, 1.1F, 1, 0.0F, 1, 1.0F);
		scaleAnim1.setDuration(30L);
		Animation scaleAnim2 = new ScaleAnimation(1.1F, 1.0F, 1.1F, 1.0F, 1, 0.0F, 1, 1.0F);
		scaleAnim2.setDuration(100L);
		Animation alphaAnim = new AlphaAnimation(0.8F, 1.0F);
		alphaAnim.setDuration(130L);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(scaleAnim2);
		animSet.addAnimation(alphaAnim);
		mDownFloatText.startAnimation(animSet);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_category_detail);
		findViewById(R.id.back_button).setOnClickListener(this);
		findViewById(R.id.top_bar_download).setOnClickListener(this);
		findViewById(R.id.top_bar_search).setOnClickListener(this);
		Utils.scaleClickRect(findViewById(R.id.top_bar_download));
		Utils.scaleClickRect(findViewById(R.id.top_bar_search));
		mTitle = (TextView) findViewById(R.id.title);
		
		Intent intent = getIntent();
		setIntent(null);
		
		String rankUrl = null;
		String prodUrl = null;
		if (intent.hasExtra(EXTRA_TITLE))
			mTitle.setText(intent.getStringExtra(EXTRA_TITLE));
		if (intent.hasExtra(EXTRA_RANK_URL))
			rankUrl = intent.getStringExtra(EXTRA_RANK_URL);
		if (intent.hasExtra(EXTRA_NEW_PROD_URL))
			prodUrl = intent.getStringExtra(EXTRA_NEW_PROD_URL);
		mAinimation = (ImageView)findViewById(R.id.image_animation);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mDownFloatIcon = (ImageView)findViewById(R.id.top_bar_download);
		mDownFloatText = (TextView)findViewById(R.id.top_bar_download_text);
		UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
		indicator.setLineWidthPercent(0.7f);
		
		FragmentManager manager = getSupportFragmentManager();
		mTabHost.setup();
		
		Bundle argRank = new Bundle();
		argRank.putInt(AppRankFragment.EXTRA_CATEGORY_TYPE, AppRankFragment.CATEGORY_TYPE_DETAIL);
		argRank.putString(AppRankFragment.EXTRA_URL, rankUrl);
		mFragmentAdapter = new TabsFragmentAdapter(this, manager, mTabHost, mViewPager, indicator);
		mFragmentAdapter.setOnFragmentChangedListener(this);
		TextView tabIndicator = buildIndicator(R.string.tab_title_hot);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_RANK).setIndicator(tabIndicator),
				AppRankFragment.class, argRank);
		
		Bundle argNewProd = new Bundle();
		argNewProd.putInt(AppNewProdFragment.EXTRA_CATEGORY_TYPE, AppNewProdFragment.CATEGORY_TYPE_DETAIL);
		argNewProd.putString(AppNewProdFragment.EXTRA_URL, prodUrl);
		tabIndicator = buildIndicator(R.string.tab_title_new_product);
		mFragmentAdapter.addTab(mTabHost.newTabSpec(TAB_TAG_NEW_PROD).setIndicator(tabIndicator),
				AppNewProdFragment.class, argNewProd);

		indicator.setViewPager(mViewPager);
		indicator.setFades(false);
		
		DataCenter.getInstance().addObserver(this);
		resetDownFloatIconVisible();
	}
	
	private void resetDownFloatIconVisible() {
		if(!mIsAnimationRunning) {
			int taskNum = DataCenter.getInstance().getTaskList().getUncompletedTaskCount();
			boolean hasTask = taskNum > 0;
			String numMoreText = "";
			if(taskNum >99)
				numMoreText = "+";
			mDownFloatText.setText(( hasTask ? taskNum : 0) + "" + numMoreText);
			mDownFloatText.setVisibility( hasTask ? View.VISIBLE : View.GONE);
			mDownFloatIcon.setImageResource(hasTask ? R.drawable.searchbar_download_gray_bg : R.drawable.searchbar_download_gray_icon);
		}
	}
	
	private boolean mIsAnimationRunning = false;
	private AnimationListener mFloatDownAnimEndListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) { 
			mIsAnimationRunning = true;
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationEnd(Animation animation) {
			mIsAnimationRunning = false;
			mAinimation.setVisibility(View.GONE);
			resetDownFloatIconVisible();	
			showNumberAnimation();
		}
	};
	
	@Override
	public void update(Observable observable, Object data) {
		if (data == null || !(data instanceof Message))
			return;
		
		Message msg = (Message) data;
		if (msg.what == DataCenter.MSG_DOWN_EVENT_STATUS_CHANGED
				|| msg.what == DataCenter.MSG_DOWN_EVENT_TASK_LIST_CHANGED) {			
			resetDownFloatIconVisible();
		} else if (msg.what == DataCenter.MSG_NEW_DOWN_EVENT) {
			final ImageView originIcon = (ImageView) msg.obj;
			
			mIsAnimationRunning = true;
			mAinimation.postDelayed(new Runnable() {
				@Override
				public void run() {
					showDownAnimation(originIcon);
				}
			}, 500);
		} else if(msg.what == DataCenter.MSG_INSTALL_SIGNATURE_NOTIFY_EVENT) {
			if(Utils.isTopActivity(this)) {
				String packageName = (String)msg.obj;
				if(packageName != null) {
					LocalAppInfo appInfo = DataCenter.getInstance().getLocalApps().getLocalPackage(packageName);
					if(appInfo != null)
						CommonInvoke.showSignatureDiffInstallDialog(this, packageName);
				}
			}
		} else if(msg.what == DataCenter.MSG_WIFI_TO_MOBILE_CHANGED_EVENT) {
			if(Utils.isTopActivity(this)) {
				NetworkStateReceiver.showNetworkChangeQueryDialog(this);
			}
		}
	}
	
	@Override
	public void onFragmentActive(int pos) {
		FragmentManager manager = getSupportFragmentManager();
		List<Fragment> list = manager.getFragments();
		if(list != null && pos< list.size()) {
			Fragment fragment = list.get(pos);
			if(fragment != null) {
				if(pos == 0) {
					((AppRankFragment)fragment).onFragmentActive();
				}
				if(pos == 1) {
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
	public void onClick(View v) {
		if (v.getId() == R.id.back_button) {
			finish();
		} else if(v.getId() == R.id.top_bar_download) {
			startActivity(new Intent(this, ManageDownloadActivity.class));
		} else if(v.getId() == R.id.top_bar_search) {
			startActivity(new Intent(this, SearchActivity.class));
		}
	}
	
}
