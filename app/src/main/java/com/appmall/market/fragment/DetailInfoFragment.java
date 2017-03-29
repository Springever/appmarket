package com.appmall.market.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Observer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appmall.market.R;
import com.appmall.market.bean.App;
import com.appmall.market.bean.AppDetail;
import com.appmall.market.bean.AppDetail.SnapShot;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.bitmaputils.ImageLoader;
import com.appmall.market.widget.ViewPagerDot;
import com.appmall.market.widget.LongClickRelativeLayout;


public class DetailInfoFragment extends BaseFragment implements OnClickListener, IDataCallback, Observer{
	private static final String DATA_PATTERN = "yyyy-MM-dd";
	public static final String EXTRA_DETAIL_INFOURL = "detailinfourl";
	private View mContentView;
	
	private boolean mDescExpanded;
	private boolean mUpdateExpanded;
	private boolean mDescExpandEnabled;
	private boolean mUpdateExpandEnabled;
	
	private LinearLayout mThumbnails;
	private TextView mDescText;
	private ImageView mDescArrowIcon;
	private ImageView mUpdateArrowIcon;
	private TextView mUpdateInfoText;
	private View mDescLayout;
	private View mUpdateInfoLayout;
	private Context mContext;
	
	private static final long SHOW_DETAIL_DELAY = 0;
	protected static final int MAX_LINE_COUNT = 3;
	private AppDetail mDetailInfo;
	
	private ArrayList<View> mPageViews;
	private ArrayList<String> mPageIcons;
	private ArrayList<ImageView> mImageViews;
	private ViewPager mViewPager;
	private RelativeLayout mLayoutViewPager;
	private ViewPagerDot mViewPagerDot;
	private ImagePageAdapter mImagePageAdapter;
	
	class ImagePageAdapter extends PagerAdapter {
		
 		@Override 
        public int getCount() {
             return mPageViews.size(); 
        }
 		
 		@Override 
        public boolean isViewFromObject(View arg0, Object arg1) { 
            return arg0 == arg1; 
        }
 		
 		@Override 
        public Object instantiateItem(View arg0, int arg1) {
 			View imageView = (View)mPageViews.get(arg1);
 			ViewPager viewPager = (ViewPager) arg0;	
 			viewPager.addView(imageView); 
            return mPageViews.get(arg1); 
        }
 		
 		@Override
 		public void destroyItem(ViewGroup container, int position, Object object) {
 			container.removeView((View) object);
 		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mContentView != null) {
			ViewGroup parent = (ViewGroup) mContentView.getParent();
			parent.removeView(mContentView);
			return mContentView;
		}
		mContext = getActivity();
		mContentView = inflater.inflate(R.layout.fragment_detail_info, null);	
		InitView();
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
		
	private float mAdjust =1;
	private float mPictureFact = (float)333/200;
	private void InitView() {
		DisplayMetrics dm = new DisplayMetrics();  
		dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
		float density = dm.density;
		mAdjust = density/(float)1.5;
		
		mPageViews = new ArrayList<View>();
		mPageIcons = new ArrayList<String>();
		mImageViews = new ArrayList<ImageView>();
		mLayoutViewPager = (RelativeLayout)getActivity().findViewById(R.id.layout_detail_viewpager);
		mViewPager = (ViewPager)getActivity().findViewById(R.id.viewpager_detail);
		mViewPagerDot = (ViewPagerDot) getActivity().findViewById(R.id.details_dot);
		mImagePageAdapter = new ImagePageAdapter();
		mViewPager.setAdapter(mImagePageAdapter);		
	
		mContentView.findViewById(R.id.app_description_layout).setOnClickListener(this);
		mContentView.findViewById(R.id.app_updateinfo_layout).setOnClickListener(this);
		mContentView.findViewById(R.id.detailInfo_scroller).setVisibility(View.GONE);
		
		mThumbnails = (LinearLayout) mContentView.findViewById(R.id.thumbnail_layout);

		if(mAdjust < 1)
			mThumbnails.getLayoutParams().height = 200;
		else if(mAdjust == 1)
			mThumbnails.getLayoutParams().height = 280;
		else if(mAdjust >= 2) 
			mThumbnails.getLayoutParams().height = 600;
		else
			mThumbnails.getLayoutParams().height = 450;
		
		mDescLayout = mContentView.findViewById(R.id.app_description_layout);
		mDescArrowIcon = (ImageView) mContentView.findViewById(R.id.description_expand_button);
		mUpdateArrowIcon = (ImageView) mContentView.findViewById(R.id.updateinfo_expand_button);
		mDescText = (TextView) mContentView.findViewById(R.id.description_text);
		mUpdateInfoText = (TextView) mContentView.findViewById(R.id.updateinfo_text);
		mUpdateInfoLayout = mContentView.findViewById(R.id.app_updateinfo_layout);
	}
	
	public boolean isBigViewMode() {
		return (mLayoutViewPager.getVisibility() == View.VISIBLE);
	}
	
	public void closeBigViewMode() {
		mLayoutViewPager.setVisibility(View.GONE);
	}
	
	private void requestData() {
		Utils.startLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
		Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_DETAIL_INFOURL)) {
			String detailUrl = args.getString(EXTRA_DETAIL_INFOURL);
			if (!TextUtils.isEmpty(detailUrl)) {
				int dataId = IDataConstant.APP_DETAIL;
				Options options = new Options();
				options.mCustomUrl = detailUrl;
				DataCenter.getInstance().requestDataAsync(mContext, dataId, this, options);
			}
		}
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (dataId == IDataConstant.APP_DETAIL) {		
			Utils.stopLoadingAnimation(mContentView.findViewById(R.id.loading_layout));
			if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof AppDetail)) {
				showLoadingFailedLayout();
				return;
			}
			mContentView.findViewById(R.id.detailInfo_scroller).setVisibility(View.VISIBLE);		
			onDetailObtain((AppDetail) resp.mData);
			mDescText.postDelayed(mShowDetailRunnable, SHOW_DETAIL_DELAY);
		}
	}
	
	private Runnable mShowDetailRunnable = new Runnable() {
		@Override
		public void run() {
			
			int descLineCount = mDescText.getLineCount();
			int updateLineCount = mUpdateInfoText.getLineCount();
			
			if (mDescLayout.getVisibility() == View.VISIBLE) {
				mDescExpandEnabled = descLineCount > MAX_LINE_COUNT;
				mContentView.findViewById(R.id.description_expand_button).setVisibility(mDescExpandEnabled ?
						View.VISIBLE : View.GONE);
				mDescExpanded = false;
				mDescArrowIcon.setImageResource(R.drawable.arrow_down);
				mDescText.setMaxLines(3);
			}
			if (mUpdateInfoLayout.getVisibility() == View.VISIBLE) {
				mUpdateExpandEnabled = updateLineCount > MAX_LINE_COUNT;
				mContentView.findViewById(R.id.updateinfo_expand_button).setVisibility(mUpdateExpandEnabled ?
						View.VISIBLE : View.GONE);
				mUpdateExpanded = false;
				mUpdateArrowIcon.setImageResource(R.drawable.arrow_down);
				mUpdateInfoText.setMaxLines(3);
			}
		}
	};
	
	private void onDetailObtain(AppDetail detail) {	
		//添加容错处理
		if(getActivity() == null) return;
		
		mDetailInfo = detail;
		if (TextUtils.isEmpty(detail.mBrief)) {
			mDescLayout.setVisibility(View.GONE);
		}
		if (TextUtils.isEmpty(detail.mChangeLog)) {
			mUpdateInfoLayout.setVisibility(View.GONE);
		}
		
		mThumbnails.setBackgroundColor(Color.WHITE);
		if (detail.mSnapshots != null && detail.mSnapshots.size() > 0) {
			for(int i=0; i< detail.mSnapshots.size(); i++) {
				SnapShot screenShot = detail.mSnapshots.get(i);
				if(screenShot != null) {
					ImageView newThumbnail = insertThumbnail(i);
					ImageLoader.getInstance().loadLargeImageByReqHeight(screenShot.mDefaultUrl, newThumbnail, mThumbnails.getLayoutParams().height);
				}
			}
		}
		
		mDescText.setText(mDetailInfo.mBrief);
		mUpdateInfoText.setText(mDetailInfo.mChangeLog);
		Locale locale = Locale.getDefault();
		long updateTime = mDetailInfo.mUpdateTime;
		String format = getString(R.string.update_time);
		SimpleDateFormat dataFormat = new SimpleDateFormat(DATA_PATTERN, locale);
		String strUpdateTime = String.format(locale, format, dataFormat.format(new Date(updateTime)));
		
		((TextView)mContentView.findViewById(R.id.text_content_appversion)).setText(mDetailInfo.mVersionName);
		((TextView)mContentView.findViewById(R.id.text_content_appupdatetime)).setText(strUpdateTime);
		if(!TextUtils.isEmpty(mDetailInfo.mDeveloper)) {
			((TextView)mContentView.findViewById(R.id.text_content_appdeveloper)).setText(mDetailInfo.mDeveloper);
		}
		
		int nRecommendNum = mDetailInfo.mRecommends.size();
		if( nRecommendNum >0)
		{
			mContentView.findViewById(R.id.app_recommend_layout).setVisibility(View.VISIBLE);
			if(nRecommendNum >=1) {
				App app = mDetailInfo.mRecommends.get(0);
				if(app != null && app.mIconUrl != null && app.mTitle != null) {
					ImageView imageApp1 = (ImageView)mContentView.findViewById(R.id.image_app1);
					TextView textApp1 = (TextView)mContentView.findViewById(R.id.text_app1);
					textApp1.setText(app.mTitle);
					ImageLoader.getInstance().loadImage(app.mIconUrl, imageApp1);
					mContentView.findViewById(R.id.layout_app1).setVisibility(View.VISIBLE);
					mContentView.findViewById(R.id.layout_app1).setOnClickListener(this);
				}
//				Log.d("demo", "0 app.mIconUrl = "+app.mIconUrl);
			}
			
			if(nRecommendNum >=2) {
				App app = mDetailInfo.mRecommends.get(1);
				if(app != null && app.mIconUrl != null && app.mTitle != null) {
					ImageView imageApp2 = (ImageView)mContentView.findViewById(R.id.image_app2);
					TextView textApp2 = (TextView)mContentView.findViewById(R.id.text_app2);
					textApp2.setText(app.mTitle);
					ImageLoader.getInstance().loadImage(app.mIconUrl, imageApp2);
					mContentView.findViewById(R.id.layout_app2).setVisibility(View.VISIBLE);
					mContentView.findViewById(R.id.layout_app2).setOnClickListener(this);
				}
//				Log.d("demo", "1 app.mIconUrl = "+app.mIconUrl);
			}
			
			if(nRecommendNum >=3) {
				App app = mDetailInfo.mRecommends.get(2);
				if(app != null && app.mIconUrl != null && app.mTitle != null) {
					ImageView imageApp3 = (ImageView)mContentView.findViewById(R.id.image_app3);
					TextView textApp3 = (TextView)mContentView.findViewById(R.id.text_app3);
					textApp3.setText(app.mTitle);
					ImageLoader.getInstance().loadImage(app.mIconUrl, imageApp3);
					mContentView.findViewById(R.id.layout_app3).setVisibility(View.VISIBLE);
					mContentView.findViewById(R.id.layout_app3).setOnClickListener(this);
				}
			}
			
			if(nRecommendNum >=4) {
				App app = mDetailInfo.mRecommends.get(3);
				if(app != null && app.mIconUrl != null && app.mTitle != null) {
					ImageView imageApp4 = (ImageView)mContentView.findViewById(R.id.image_app4);
					TextView textApp4 = (TextView)mContentView.findViewById(R.id.text_app4);
					textApp4.setText(app.mTitle);
					ImageLoader.getInstance().loadImage(app.mIconUrl, imageApp4);
					mContentView.findViewById(R.id.layout_app4).setVisibility(View.VISIBLE);
					mContentView.findViewById(R.id.layout_app4).setOnClickListener(this);
				}
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		int nAppSel = -1;
		switch (v.getId()) {
			case R.id.app_description_layout:
				doExpandDescription();
				break;
			case R.id.app_updateinfo_layout:
				doExpandUpdateInfo();
				break;
			case R.id.layout_app1:
				nAppSel = 0;
				break;
			case R.id.layout_app2:
				nAppSel = 1;
				break;
			case R.id.layout_app3:
				nAppSel = 2;
				break;
			case R.id.layout_app4:
				nAppSel = 3;
				break;
		}
		if(nAppSel != -1) {
			getActivity().finish();
			Utils.jumpAppDetailActivity(mContext, mDetailInfo.mRecommends.get(nAppSel));
		}		
	}
	
	private void showLoadingFailedLayout() {
		View failedLayout = mContentView.findViewById(R.id.loading_failed_layout);
		TextView resultText = (TextView) failedLayout.findViewById(R.id.failed_result);
		TextView tipText = (TextView) failedLayout.findViewById(R.id.failed_tip);
		Button failedButton = (Button) failedLayout.findViewById(R.id.failed_tip_button);
		failedLayout.setVisibility(View.VISIBLE);
		boolean hasNetwork = Utils.isNetworkAvailable(mContext);
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
	
	private OnClickListener mOnRefreshButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			View failedLayout = mContentView.findViewById(R.id.loading_failed_layout);
			failedLayout.setVisibility(View.GONE);		
			requestData();
		}
	};
	
	private OnClickListener mOnSettingNetworkClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utils.jumpNetworkSetting(mContext);
		}
	};
	
	private void doExpandDescription() {
		if (!mDescExpandEnabled)
			return;
		
		mDescExpanded = !mDescExpanded;
		mDescText.setMaxLines(mDescExpanded ? Integer.MAX_VALUE : 3);
		mDescArrowIcon.setImageResource(mDescExpanded ?
				R.drawable.arrow_up : R.drawable.arrow_down);
	}
	
	private void doExpandUpdateInfo() {
		if (!mUpdateExpandEnabled)
			return;
		
		mUpdateExpanded = !mUpdateExpanded;
		mUpdateInfoText.setMaxLines(mUpdateExpanded ? Integer.MAX_VALUE : 3);
		mUpdateArrowIcon.setImageResource(mUpdateExpanded ?
				R.drawable.arrow_up : R.drawable.arrow_down);
	}
	
	Bitmap mScaleBitmap = null;
	int mSelIndex = -1;
	final private ImageView insertThumbnail(final int index) {
		Resources res = getResources();
		final ImageView imageView = new ImageView(mContext);
		int marginLeft = res.getDimensionPixelSize(R.dimen.app_detail_content_margin_left);
		int imageWidth = (int)(mThumbnails.getLayoutParams().height/mPictureFact);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageWidth, LayoutParams.MATCH_PARENT);
		lp.leftMargin = marginLeft;
		lp.gravity = Gravity.CENTER_VERTICAL;
		imageView.setBackgroundColor(getResources().getColor(R.color.detail_Thumnail_bg_color));
		imageView.setLayoutParams(lp);
		mThumbnails.addView(imageView);			
		imageView.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				if(mImagePageAdapter.getCount() == 0) {
					boolean hasHDPI = false;
					mPageIcons.clear();
					mImageViews.clear();
					for(SnapShot snapShot : mDetailInfo.mSnapshots) {
						if(snapShot.mHDPIUrl != null) {
							ImageView view = new ImageView(mContext);
							RelativeLayout.LayoutParams viewLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
							viewLayout.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
							view.setLayoutParams(viewLayout);
							LongClickRelativeLayout pageView = new LongClickRelativeLayout(mContext);
							pageView.setBackgroundColor(getResources().getColor(R.color.detail_Thumnail_bg_color));
							pageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
							pageView.addView(view);
							pageView.setOnLongClickListener(new View.OnLongClickListener() {		
								@Override
								public boolean onLongClick(View v) {
									mLayoutViewPager.setVisibility(View.GONE);
									return true;
								}
							});
							mPageViews.add(pageView);									
							mPageIcons.add(snapShot.mHDPIUrl);
//							Log.d("demo", "snapShot.mHDPIUrl = "+snapShot.mHDPIUrl);
							mImageViews.add(view);
							hasHDPI = true;
						}
					}
					
					//处理大图load
					if(index < mPageIcons.size() && index < mImageViews.size()) {
						//优先load选中的图
//						ImageLoader.getInstance().loadLargeImageByReqWidth(mPageIcons.get(index), mImageViews.get(index), dm.widthPixels);
						ImageLoader.getInstance().loadLargeImageByFITXY(mPageIcons.get(index), mImageViews.get(index));
						//load其余的图
						for(int i=0 ; i< mImageViews.size(); i++) {
							if(i != index) {
//								ImageLoader.getInstance().loadLargeImageByReqWidth(mPageIcons.get(i), mImageViews.get(i), dm.widthPixels);
								ImageLoader.getInstance().loadLargeImageByFITXY(mPageIcons.get(i), mImageViews.get(i));
							}
						}
						mPageIcons.clear();
						mImageViews.clear();
					}
					
					if(hasHDPI) {
						mViewPagerDot.setViewPager(mViewPager);
						mImagePageAdapter.notifyDataSetChanged();
						if(index < mImagePageAdapter.getCount())
							mViewPager.setCurrentItem(index, false);
						else
							mViewPager.setCurrentItem(0, false);
					}
				}else {
					if(index < mImagePageAdapter.getCount())
						mViewPager.setCurrentItem(index, false);
					else
						mViewPager.setCurrentItem(0, false);	
				}
				mLayoutViewPager.setVisibility(View.VISIBLE);
			}
		});
		
		return imageView;
	}
}
