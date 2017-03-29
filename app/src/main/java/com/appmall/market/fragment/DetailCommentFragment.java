package com.appmall.market.fragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.activity.AppDetailActivity;
import com.appmall.market.activity.AppDetailActivity.OnActivityResultListener;
import com.appmall.market.activity.CommentPostActivity;
import com.appmall.market.adapter.DetailCommentAdapter;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.CommentList;
import com.appmall.market.bean.CommentList.Comment;
import com.appmall.market.common.Utils;
import com.appmall.market.data.CommentPost;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.DataPage;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.widget.LoadMoreListView;
import com.appmall.market.widget.LoadMoreListView.OnLoadMoreListener;
import com.appmall.market.widget.RankStarWidget;


public class DetailCommentFragment extends BaseFragment implements IDataCallback, OnLoadMoreListener, OnClickListener, OnActivityResultListener{
	public static final int REQUEST_COMMENT = 99;
	public static final String EXTRA_APP_CHANNEL_ID = "app_channel_id";
	public static final String EXTRA_COMMENT_CONTENT = "content";
	public static final String EXTRA_COMMENT_MODEL = "model";
	public static final String EXTRA_COMMENT_ISNEW = "isnew";
	public static final String EXTRA_COMMENT_STARLEVEL = "starlevel";
	public static final String EXTRA_COMMENT_UPDATETIME = "updatetime";
	
	private View mContentView;
	private LoadMoreListView mListView;
	private DetailCommentAdapter mAdapter;
	
	private int mAppID;
	private DataPage mCommentPage;
	private CommentList mConmmentList;
	
	private Context mContext;
	private View mHeadView;
	
	private boolean mRepeate = false; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}
		
		mContext = getActivity();
		((AppDetailActivity)getActivity()).setActivityResultListener(this);
		mContentView = inflater.inflate(R.layout.fragment_detail_comment, null);
		mListView = (LoadMoreListView) mContentView.findViewById(R.id.listview_detail_comment);
		mListView.setOnLoadMoreListener(this);
		mListView.setNeedLoadMore(true);
		
		{
			mHeadView = inflater.inflate(R.layout.comment_list_header, null);
			TextView btnComment = (TextView)mHeadView.findViewById(R.id.button_detail_comment);
			btnComment.setOnClickListener(this);
			mListView.addHeaderView(mHeadView, null, false);
		}
	
		mListView.setVisibility(View.GONE);
		mListView.setDivider(null);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		mAdapter = new DetailCommentAdapter(mContext);
		mListView.setAdapter(mAdapter);
		
		mCommentPage = new DataPage();
		mConmmentList = new CommentList();
		
		Bundle args = getArguments();
		mAppID = args.getInt(EXTRA_APP_CHANNEL_ID);
		
		mInitData = false;
		return mContentView;
	}
	
	private boolean mInitData = false;
	public void onFragmentActive() {
		if(!mInitData) {
			mInitData = true;
			requestData();
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	private Toast mTipToast;
	private void showTip(int feedbackRes) {
		if (mTipToast == null) {
			mTipToast = Toast.makeText(mContext, feedbackRes, Toast.LENGTH_SHORT);
		} else {
			mTipToast.setText(feedbackRes);
		}
		
		mTipToast.show();
	}
	
	Comment mLocalComment = new Comment();
	public void OnActivityResult(Intent intent) {
		String model = intent.getStringExtra(EXTRA_COMMENT_MODEL);
		int isNew = intent.getIntExtra(EXTRA_COMMENT_ISNEW, 1);
		String content = intent.getStringExtra(EXTRA_COMMENT_CONTENT);
		int appId = intent.getIntExtra(EXTRA_APP_CHANNEL_ID, 0);
		int starLevel = intent.getIntExtra(EXTRA_COMMENT_STARLEVEL, 0);
		
		mLocalComment.mContent = content;
		mLocalComment.mIsNew = isNew;
		mLocalComment.mDeviceName = model;
		mLocalComment.mStarLevel = starLevel/2;
		mLocalComment.mUpdateTime = System.currentTimeMillis();
		
		{
			int dataId = IDataConstant.COMMENT_APP;	
			CommentPost data = new CommentPost(appId);
			data.setContent(content);
			data.setStartLevel(starLevel/2);
			data.setDeviceName(model);
			
			Options options = new Options();
			options.mPostData = data;
			DataCenter.getInstance().requestDataAsync(mContext, dataId, this, options);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_detail_comment:
			App app = ((AppDetailActivity)getActivity()).getAppInfo();
			if(app != null) {
				mAppID = app.mID;
				Intent intent = new Intent(mContext, CommentPostActivity.class);
				intent.putExtra(CommentPostActivity.EXTRA_APP_ICONURL, app.mIconUrl);
				intent.putExtra(CommentPostActivity.EXTRA_APP_TITLE, app.mTitle);
				intent.putExtra(CommentPostActivity.EXTRA_APP_ID, app.mID);
				intent.putExtra(CommentPostActivity.EXTRA_APP_VERSIONNAME, app.mVersionName);
				intent.putExtra(CommentPostActivity.EXTRA_APP_REPEATE, mRepeate);
				CommentPostActivity.setBackgroundBitmap(Utils.takeScreenShot(getActivity()));
				((AppDetailActivity)getActivity()).startActivityForResult(intent, REQUEST_COMMENT);
			} else {
				Advert advert = ((AppDetailActivity)getActivity()).getAdvert();
				if(advert != null) {
					mAppID = advert.mAppId;
					Intent intent = new Intent(mContext, CommentPostActivity.class);
					intent.putExtra(CommentPostActivity.EXTRA_APP_ICONURL, advert.mAppIconUrl);
					intent.putExtra(CommentPostActivity.EXTRA_APP_TITLE, advert.mTitle);
					intent.putExtra(CommentPostActivity.EXTRA_APP_ID, advert.mAppId);
					intent.putExtra(CommentPostActivity.EXTRA_APP_VERSIONNAME, advert.mVersionName);
					intent.putExtra(CommentPostActivity.EXTRA_APP_REPEATE, mRepeate);
					CommentPostActivity.setBackgroundBitmap(Utils.takeScreenShot(getActivity()));
					((AppDetailActivity)getActivity()).startActivityForResult(intent, REQUEST_COMMENT);
				}
			}
			
			break;
		}
	}
	
	boolean mLoadMore = false;
	@Override
	public void onLoadMore() {
		mLoadMore = true;
		requestData();
	}
	
	public void requestData() {
		Activity activity = getActivity();
		if (activity == null)
			return;
		
		if (mCommentPage == null || mCommentPage.getPageCount() <= 0) {
			Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		}
		
		int dataId = IDataConstant.APP_DETAIL_COMMENT;
		Map<String, String> params = new HashMap<String, String>();
		params.put("page", String.valueOf(mCommentPage.getPageCount() + 1));
		params.put("appChannelId", String.valueOf(mAppID));
		Options opts = new Options();
		opts.mUrlAppend = Utils.buildQueryString(params);
		
		DataCenter.getInstance().requestDataAsync(activity, dataId, this, opts);
	}
	
	private void showCommentData(CommentList data) {
		Activity activity = getActivity();
		if (activity == null || activity.isFinishing())
			return;
		
		if (data == null)
			return;
		
		if (data.mRate != null && data.mRate.mAvg != 0) {
			String people = activity.getString(R.string.rate_people);
			TextView textAverage = (TextView)mHeadView.findViewById(R.id.text_average_value);
			textAverage.setText(mConmmentList.mRate == null ? "0.0" : String.format(Locale.getDefault(), "%.1f", mConmmentList.mRate.mAvg));
			RankStarWidget starWidget = (RankStarWidget)mHeadView.findViewById(R.id.star_average_value);
			starWidget.setRank(mConmmentList.mRate == null ? 0 : (int)(mConmmentList.mRate.mAvg*2));
			TextView textVotes = (TextView)mHeadView.findViewById(R.id.total_vote_value);
			textVotes.setText((mConmmentList.mRate == null ? 0 : mConmmentList.mRate.mTotal) + people);

			float[] percents = new float[5];
			if (mConmmentList.mRate == null || mConmmentList.mRate.mSection == null ||
					mConmmentList.mRate.mSection.length != 5 || mConmmentList.mRate.mTotal == 0) {
				Arrays.fill(percents, 0f);
			} else {
				for (int i = 0; i < 5; ++i) {
					percents[i] = (float)mConmmentList.mRate.mSection[i] / mConmmentList.mRate.mTotal;
				}
			}
			
			View progressBar = mHeadView.findViewById(R.id.view_star_five_bar);
			View progressStar5 = mHeadView.findViewById(R.id.view_star_five_progress);
			View progressStar4 = mHeadView.findViewById(R.id.view_star_four_progress);
			View progressStar3 = mHeadView.findViewById(R.id.view_star_three_progress);
			View progressStar2 = mHeadView.findViewById(R.id.view_star_two_progress);
			View progressStar1 = mHeadView.findViewById(R.id.view_star_one_progress);
			int barWidth = progressBar.getLayoutParams().width;
			
			int width1 = (int) (barWidth * percents[0]);
			int width2 = (int) (barWidth * percents[1]);
			int width3 = (int) (barWidth * percents[2]);
			int width4 = (int) (barWidth * percents[3]);
			int width5 = (int) (barWidth * percents[4]);
			
			progressStar1.getLayoutParams().width = width1;
			progressStar2.getLayoutParams().width = width2;
			progressStar3.getLayoutParams().width = width3;
			progressStar4.getLayoutParams().width = width4;
			progressStar5.getLayoutParams().width = width5;
			
			((TextView)mHeadView.findViewById(R.id.view_star_five_text)).setText(mConmmentList.mRate.mSection[4]+"");
			((TextView)mHeadView.findViewById(R.id.view_star_four_text)).setText(mConmmentList.mRate.mSection[3]+"");
			((TextView)mHeadView.findViewById(R.id.view_star_three_text)).setText(mConmmentList.mRate.mSection[2]+"");
			((TextView)mHeadView.findViewById(R.id.view_star_two_text)).setText(mConmmentList.mRate.mSection[1]+"");
			((TextView)mHeadView.findViewById(R.id.view_star_one_text)).setText(mConmmentList.mRate.mSection[0]+"");
		}
						
		mAdapter.setData(data.mComments);
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if(getActivity() == null) return;
		
		if(dataId == IDataConstant.APP_DETAIL_COMMENT) {
			Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
			mListView.onLoadMoreComplete();
			mListView.setVisibility(View.VISIBLE);
			
			if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof CommentList)) {
				if(mLoadMore)
					Toast.makeText(mContext, R.string.load_more_failed, Toast.LENGTH_SHORT).show();
					return;
			}
							
			if(!mLoadMore) {
				mConmmentList = (CommentList) resp.mData;
				mRepeate = mConmmentList.mRepeat;
			} else
				mConmmentList.mComments.addAll(((CommentList) resp.mData).mComments);
				
			if(mRepeate)
				mHeadView.findViewById(R.id.button_detail_comment).setVisibility(View.GONE);
			showCommentData(mConmmentList);
			
			mCommentPage.increaseCount();
			boolean hasNext = mCommentPage.parseNextUrlFromContext(resp.mContext);
			mListView.setNeedLoadMore(hasNext);
		} else if(dataId == IDataConstant.COMMENT_APP) {
			if(resp.mSuccess) {
				mRepeate = true;
				mHeadView.findViewById(R.id.button_detail_comment).setVisibility(View.GONE);
				showTip(R.string.comment_success);
				if(mLocalComment != null) {
					mConmmentList.mComments.add(0, mLocalComment);
					if(mLocalComment != null && mConmmentList.mRate != null && mConmmentList.mRate.mTotal <99) {
						mConmmentList.mRate.mTotal++;
						mConmmentList.mRate.mSection[mLocalComment.mStarLevel-1]++;
						float total = 0;
						for (int i = 0; i < 5; ++i) {
							total += (float)mConmmentList.mRate.mSection[i]*(i+1);
						}
						mConmmentList.mRate.mAvg = (float)total/mConmmentList.mRate.mTotal;
					}
					showCommentData(mConmmentList);
				}
			}			
			else
				showTip(R.string.comment_fail);
		}

	}
}
