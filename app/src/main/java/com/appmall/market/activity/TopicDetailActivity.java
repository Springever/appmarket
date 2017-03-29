package com.appmall.market.activity;

import java.util.ArrayList;
import java.util.Observable;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.adapter.ItemDataDef.ItemDataWrapper;
import com.appmall.market.adapter.ItemDataDef.TopicHeader;
import com.appmall.market.adapter.TopicDetailAdapter;
import com.appmall.market.adapter.TopicDetailAdapter.Callback;
import com.appmall.market.bean.App;
import com.appmall.market.bean.TopicDetail;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.download.DownloadTask;

/**
 * 专题详情界面
 *  
 *
 */
public class TopicDetailActivity extends BaseActivity implements OnClickListener, IDataCallback, Callback {

	public static final String EXTRA_DETAIL_URL = "rank_url";
	public static final String EXTRA_DETAIL_TITLE = "title";

	private ListView mList;
	private TopicDetailAdapter mAdapter;
	private TopicDetail mDetail;
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
		if (msg.what == DataCenter.MSG_NEW_DOWN_EVENT) {
			final ImageView originIcon = (ImageView) msg.obj;
			
			mIsAnimationRunning = true;
			mAinimation.postDelayed(new Runnable() {
				@Override
				public void run() {
					showDownAnimation(originIcon);
				}
			}, 500);
		} else {
			super.update(observable, data);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_topic_detail);
		
		findViewById(R.id.back_button).setOnClickListener(this);
		findViewById(R.id.top_bar_download).setOnClickListener(this);
		findViewById(R.id.top_bar_search).setOnClickListener(this);
		Utils.scaleClickRect(findViewById(R.id.top_bar_download));
		Utils.scaleClickRect(findViewById(R.id.top_bar_search));
		mAinimation = (ImageView)findViewById(R.id.image_animation);
		mDownFloatIcon = (ImageView)findViewById(R.id.top_bar_download);
		mDownFloatText = (TextView)findViewById(R.id.top_bar_download_text);
		
		mAdapter = new TopicDetailAdapter(this);
		mAdapter.registerCallback(this);
		mList = (ListView) findViewById(R.id.list_view);
		mList.setAdapter(mAdapter);
		
		requestData();
		
		DataCenter.getInstance().addObserver(this);
		resetDownFloatIconVisible();
	}

	private void requestData() {
		Utils.startLoadingAnimation(findViewById(R.id.loading_layout));
		Intent intent = getIntent();
		String detailUrl = intent.getStringExtra(EXTRA_DETAIL_URL);
		String title = intent.getStringExtra(EXTRA_DETAIL_TITLE);
		if(title != null) {
			((TextView)findViewById(R.id.title)).setText(title);
		}
		if (!TextUtils.isEmpty(detailUrl)) {
			int dataId = IDataConstant.TOPIC_DETAIL;
			Options options = new Options();
			options.mCustomUrl = detailUrl;
			DataCenter.getInstance().requestDataAsync(this, dataId, this, options);
		}
	}
				
	protected void onTaskProgress(DownloadTask task) {
		if (task == null || TextUtils.isEmpty(task.mPackageName))
			return;
		Utils.handleButtonProgress(mList, R.id.down_button, task);
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
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		Utils.stopLoadingAnimation(findViewById(R.id.loading_layout));
		
		if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof TopicDetail)) {
			showLoadingFailedLayout();
			return;
		}
		
		mList.setVisibility(View.VISIBLE);
		
		ArrayList<ItemDataWrapper> items = new ArrayList<ItemDataWrapper>();
		mDetail = (TopicDetail) resp.mData;
		
		int marginTop = getResources().getDimensionPixelSize(R.dimen.list_view_margin_top);
		
		TopicHeader header = new TopicHeader(mDetail.mTitle, mDetail.mBrief);
		items.add(new ItemDataWrapper(header, TopicDetailAdapter.TYPE_TOPIC_TITLE));
		items.add(new ItemDataWrapper(marginTop, TopicDetailAdapter.TYPE_SPACE));
		
		if (mDetail.mApps != null) {
			for (App app : mDetail.mApps) {
				if (app == null)
					continue;
				
				items.add(new ItemDataWrapper(app, TopicDetailAdapter.TYPE_APP));
				items.add(new ItemDataWrapper(marginTop, TopicDetailAdapter.TYPE_SPACE));
			}
		}
		
		mAdapter.setData(items);
		refreshAppData();
	}

	@Override
	protected void refreshAppData() {
		if (mDetail == null || mAdapter == null)
			return;
		
		CommonInvoke.processApps(DataCenter.getInstance(), mDetail.mApps, false);
		resetDownFloatIconVisible();
		mAdapter.notifyDataSetChanged();
	}

	private void showLoadingFailedLayout() {
		View failedLayout = findViewById(R.id.loading_failed_layout);
		TextView resultText = (TextView) failedLayout.findViewById(R.id.failed_result);
		TextView tipText = (TextView) failedLayout.findViewById(R.id.failed_tip);
		Button failedButton = (Button) failedLayout.findViewById(R.id.failed_tip_button);
		failedLayout.setVisibility(View.VISIBLE);
		boolean hasNetwork = Utils.isNetworkAvailable(this);
		if (hasNetwork) {
			resultText.setText(R.string.network_not_good);
			tipText.setText(R.string.click_button_refresh_later);
			failedButton.setText(R.string.click_to_refresh);
			failedButton.setOnClickListener(mOnRefreshButtonClicked);
		} else {
			resultText.setText(R.string.network_not_connected);
			tipText.setText(R.string.click_button_setting_network);
			failedButton.setText(R.string.network_setting);
			failedButton.setOnClickListener(mOnSettingNetworkClicked);
		}
	}
	
	@Override
	protected void onNetworkStateChanged() {
		if (findViewById(R.id.loading_failed_layout).getVisibility() == View.VISIBLE)
			showLoadingFailedLayout();
	}
	
	@Override
	public void onDownload(DownInfoHolder downInfo) {
		CommonInvoke.processAppDownBtn(this, downInfo.mIcon, downInfo.mItem, true);
	}

	@Override
	public void onAppDetail(App app) {
		Utils.jumpAppDetailActivity(this, app);
	}
	
	private OnClickListener mOnRefreshButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			View failedLayout = findViewById(R.id.loading_failed_layout);
			failedLayout.setVisibility(View.GONE);
			requestData();
		}
	};
	
	private OnClickListener mOnSettingNetworkClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utils.jumpNetworkSetting(TopicDetailActivity.this);
		}
	};
	
}
