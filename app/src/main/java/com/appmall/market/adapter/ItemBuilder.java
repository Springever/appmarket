package com.appmall.market.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appmall.market.ApplicationImpl;
import com.appmall.market.R;
import com.appmall.market.adapter.ItemDataDef.AdvertItem;
import com.appmall.market.adapter.ItemDataDef.CategoryAdvert;
import com.appmall.market.adapter.ItemDataDef.CategoryItem;
import com.appmall.market.adapter.ItemDataDef.RankItem;
import com.appmall.market.adapter.ItemDataDef.TopicHeader;
import com.appmall.market.adapter.ItemDataDef.TopicItem;
import com.appmall.market.bean.Advert;
import com.appmall.market.bean.App;
import com.appmall.market.bean.AppUpdate;
import com.appmall.market.bean.Keywords.Keyword;
import com.appmall.market.common.Utils;
import com.appmall.market.bitmaputils.ImageLoader;
import com.appmall.market.data.LocalApps;
import com.appmall.market.data.LocalApps.LocalAppInfo;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.download.TaskStatus;
import com.appmall.market.widget.CirclePageIndicator;
import com.appmall.market.widget.RankStarWidget;
import com.appmall.market.widget.ExpandableTextView;
import com.appmall.market.widget.DownStatusButton;
import com.appmall.market.data.DataCenter;

/**
 * 此Builder用于构造通用列表项
 *  
 *
 */
public class ItemBuilder {

	private static final String ITEM_SPILIT_CHAR = " | ";
	private static final String PROGRESS_CHAR = " / ";
	private static final String DATA_PATTERN = "yyyy-MM-dd";
	private static int gray_bg_text_color = Color.parseColor("#000000");
	private static int green_bg_text_color = Color.parseColor("#3fc6a8");

	public static View buildTopBanner(LayoutInflater inflater, int position, HomeAdvertPagerAdapter adapter,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_top_banner, null);
			ViewPager pager = (ViewPager) convertView.findViewById(R.id.pager);
			pager.setAdapter(adapter);
			
			int pageColor = convertView.getResources().getColor(R.color.home_advert_indicator_page_color);
			int radius = convertView.getResources().getDimensionPixelSize(R.dimen.home_advert_indicator_radius);
			CirclePageIndicator indicator = (CirclePageIndicator) convertView.findViewById(R.id.indicator);
			indicator.setViewPager(pager);
			indicator.setStrokeWidth(0f);
			indicator.setRadius(radius);
			indicator.setPageColor(pageColor);
		}
		
		convertView.setTag(adapter);
		return convertView;
	}
	
	/**
	 * 分类标题栏
	 */
	public static View buildCategoryTitle(LayoutInflater inflater, String title,
			View convertView, ViewGroup parent) {
		CategoryTitleHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_category_title, null);
			holder = new CategoryTitleHolder();
			holder.mTitleView = (TextView) convertView.findViewById(R.id.category_title);
			convertView.setTag(holder);
		} else {
			holder = (CategoryTitleHolder) convertView.getTag();
		}
		
		holder.mTitleView.setText(title);
		return convertView;
	}
	
	public static View buildSpaceCategoryTitle(LayoutInflater inflater, String title,
			View convertView, ViewGroup parent) {
		CategoryTitleHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_space_category_title, null);
			holder = new CategoryTitleHolder();
			holder.mTitleView = (TextView) convertView.findViewById(R.id.category_title);
			convertView.setTag(holder);
		} else {
			holder = (CategoryTitleHolder) convertView.getTag();
		}
		
		holder.mTitleView.setText(title);
		return convertView;
	}
	
	public static View buildHotItemView(LayoutInflater inflater, int position, RankItem info, 
			View convertView, ViewGroup parent) {
		HotItemHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_home_app, null);
			holder = new HotItemHolder();
			holder.mItemContent = convertView.findViewById(R.id.item_content);
			holder.mLeftLayout = convertView.findViewById(R.id.left_app_layout);
			holder.mLeftCornerLabel = (TextView) convertView.findViewById(R.id.left_corner_label);
			holder.mLeftDownInfo = new DownInfoHolder();
			holder.mLeftDownInfo.mIcon = (ImageView) convertView.findViewById(R.id.left_icon);
			holder.mLeftDownInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.left_down_button);
			holder.mLeftButtonWrapper = (LinearLayout) convertView.findViewById(R.id.left_down_button_wrapper);
			holder.mLeftLabel = (TextView) convertView.findViewById(R.id.left_label);
			holder.mLeftDescription = (TextView) convertView.findViewById(R.id.left_desciprtion);
			holder.mRightLayout = convertView.findViewById(R.id.right_app_layout);
			holder.mRightCornerLabel = (TextView) convertView.findViewById(R.id.right_corner_label);
			holder.mRightDownInfo = new DownInfoHolder();
			holder.mRightDownInfo.mIcon = (ImageView) convertView.findViewById(R.id.right_icon);
			holder.mRightDownInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.right_down_button);
			holder.mRightButtonWrapper = (LinearLayout) convertView.findViewById(R.id.right_down_button_wrapper);
			holder.mRightLabel = (TextView) convertView.findViewById(R.id.right_label);
			holder.mRightDescription = (TextView) convertView.findViewById(R.id.right_desciprtion);
			holder.mLeftStar = (RankStarWidget) convertView.findViewById(R.id.left_star);
			holder.mRightStar= (RankStarWidget) convertView.findViewById(R.id.right_star);
			holder.mBottomDivider = convertView.findViewById(R.id.bottom_divider);
			convertView.setTag(holder);
		} else {
			holder = (HotItemHolder) convertView.getTag();
		}
		
		holder.mLeftLabel.setVisibility(info.mLeft == null ? View.INVISIBLE : View.VISIBLE);
		holder.mRightLabel.setVisibility(info.mRight == null ? View.INVISIBLE : View.VISIBLE);
		
		if (info.mLeft != null) {
			holder.mLeftDownInfo.mItem = info.mLeft;
			holder.mLeftLayout.setTag(info.mLeft);
			String size = Utils.getSizeString(info.mLeft.mSize);
			holder.mLeftLabel.setText(info.mLeft.mTitle);
			holder.mLeftDescription.setText(info.mLeft.mCategoryTitle + ITEM_SPILIT_CHAR + size);
			holder.mLeftStar.setRank((int)(info.mLeft.mStarLevel*2));
			bindDownButton(holder.mLeftDownInfo, false);
			ImageLoader.getInstance().loadImage(info.mLeft.mIconUrl, holder.mLeftDownInfo.mIcon);
		}
		
		if (info.mRight != null) {
			holder.mRightDownInfo.mItem = info.mRight;
			holder.mRightLayout.setTag(info.mRight);
			String size = Utils.getSizeString(info.mRight.mSize);
			holder.mRightLabel.setText(info.mRight.mTitle);
			holder.mRightDescription.setText(info.mRight.mCategoryTitle + ITEM_SPILIT_CHAR + size);
			holder.mRightStar.setRank((int)info.mRight.mStarLevel*2);
			bindDownButton(holder.mRightDownInfo, false);
			ImageLoader.getInstance().loadImage(info.mRight.mIconUrl, holder.mRightDownInfo.mIcon);
		}
		
		return convertView;
	}

	/**
	 * 中部广告条
	 */
	public static View buildMidBanner(LayoutInflater inflater, int position, Advert advert,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_mid_banner, null);
		}
		
		ImageView image = (ImageView) convertView.findViewById(R.id.image_view);
		ImageLoader.getInstance().loadImage(advert.mImageUrl, image);
		convertView.setTag(advert);
		
		return convertView;
	}
	
	/**
	 * 专题广告项
	 */
	public static View buildTopicItemView(LayoutInflater inflater, int position, TopicItem item,
			View convertView, ViewGroup parent) {
		TopicItemHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_topic_item, null);
			holder = new TopicItemHolder();
			holder.mLeftImage = (ImageView) convertView.findViewById(R.id.left_image);
			holder.mRightImage = (ImageView) convertView.findViewById(R.id.right_image);
			convertView.setTag(holder);
		} else {
			holder = (TopicItemHolder) convertView.getTag();
		}
		
		holder.mLeftImage.setVisibility(item.mLeft == null ? View.INVISIBLE : View.VISIBLE);
		holder.mRightImage.setVisibility(item.mRight == null ? View.INVISIBLE : View.VISIBLE);
		
		if (item.mLeft != null) {
			holder.mLeftImage.setTag(item.mLeft);
			ImageLoader.getInstance().loadImage(item.mLeft.mImageUrl, holder.mLeftImage);
		}
		if (item.mRight != null) {
			holder.mRightImage.setTag(item.mRight);
			ImageLoader.getInstance().loadImage(item.mRight.mImageUrl, holder.mRightImage);
		}
		
		return convertView;
	}

	/**
	 * 广告项
	 */
	public static View buildAdvertItemView(LayoutInflater inflater, int position, AdvertItem item,
			View convertView, ViewGroup parent) {
		AdvertItemHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_advert, null);
			holder = new AdvertItemHolder();
			holder.mItemContent = convertView.findViewById(R.id.item_content);
			holder.mLeftAdvert = convertView.findViewById(R.id.left_layout);
			holder.mRightAdvert = convertView.findViewById(R.id.right_layout);
			holder.mLeftIcon = (ImageView) convertView.findViewById(R.id.left_icon);
			holder.mRightIcon = (ImageView) convertView.findViewById(R.id.right_icon);
			holder.mLeftTitle = (TextView) convertView.findViewById(R.id.left_category_title);
			holder.mRightTitle = (TextView) convertView.findViewById(R.id.right_category_title);
			holder.mBottomDivider = convertView.findViewById(R.id.bottom_divider);
			convertView.setTag(holder);
		} else {
			holder = (AdvertItemHolder) convertView.getTag();
		}
		
		holder.mLeftAdvert.setVisibility(item.mLeft == null ? View.INVISIBLE : View.VISIBLE);
		holder.mRightAdvert.setVisibility(item.mRight == null ? View.INVISIBLE : View.VISIBLE);
		
		if (item.mLeft != null) {
			holder.mLeftAdvert.setTag(item.mLeft);
			ImageLoader.getInstance().loadImage(item.mLeft.mImageUrl, holder.mLeftIcon);
			holder.mLeftTitle.setText(item.mLeft.mTitle);
		}
		if (item.mRight != null) {
			holder.mRightAdvert.setTag(item.mRight);
			ImageLoader.getInstance().loadImage(item.mRight.mImageUrl, holder.mRightIcon);
			holder.mRightTitle.setText(item.mRight.mTitle);
		}
		
		return convertView;
	}

	/**
	 * 分类列表顶部广告栏
	 * @param mFetcher 
	 */
	public static View buildCategoryAdvert(LayoutInflater inflater, int position, 
			CategoryAdvert info, View convertView, ViewGroup parent, boolean hasDivideLine) {
		CategoryAdvertHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_category_advert, null);
			holder = new CategoryAdvertHolder();
			holder.mLeftImage = (ImageView) convertView.findViewById(R.id.left_image);
			holder.mRightImage = (ImageView) convertView.findViewById(R.id.right_image);
			holder.mDivideLine = convertView.findViewById(R.id.category_advert_divider);
			if(!hasDivideLine)
				holder.mDivideLine.setVisibility(View.GONE);
			else
				holder.mDivideLine.setVisibility(View.VISIBLE);
			convertView.setTag(holder);
		} else {
			holder = (CategoryAdvertHolder) convertView.getTag();
		}

		holder.mLeftImage.setVisibility(info.mLeft == null ? View.INVISIBLE : View.VISIBLE);
		holder.mRightImage.setVisibility(info.mRight == null ? View.INVISIBLE : View.VISIBLE);
		
		if (info.mLeft != null) {
			ImageLoader.getInstance().loadImage(info.mLeft.mImageUrl, holder.mLeftImage);
			holder.mLeftImage.setTag(info.mLeft);
		}
		if (info.mRight != null) {
			ImageLoader.getInstance().loadImage(info.mRight.mImageUrl, holder.mRightImage);
			holder.mRightImage.setTag(info.mRight);
		}
		
		return convertView;
	}
	
	/**
	 * 分类列表项
	 */
	public static View buildCategoryItemView(LayoutInflater inflater, int position,
			CategoryItem item, View convertView, ViewGroup parent) {
		CategoryItemHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_category_item, null);
			holder = new CategoryItemHolder();
			holder.mItemInfo = item;
			holder.mBottomDivider = convertView.findViewById(R.id.bottom_divider);
			holder.mItemContent = convertView.findViewById(R.id.item_content);
			holder.mLeftCategory = convertView.findViewById(R.id.left_layout);
			holder.mRightCategory = convertView.findViewById(R.id.right_layout);
			holder.mLeftIcon = (ImageView) convertView.findViewById(R.id.left_icon);
			holder.mRightIcon = (ImageView) convertView.findViewById(R.id.right_icon);
			holder.mLeftTitle = (TextView) convertView.findViewById(R.id.left_category_title);
			holder.mRightTitle = (TextView) convertView.findViewById(R.id.right_category_title);
			convertView.setTag(holder);
		} else {
			holder = (CategoryItemHolder) convertView.getTag();
		}

		holder.mLeftCategory.setVisibility(item.mLeft == null ? View.INVISIBLE : View.VISIBLE);
		holder.mRightCategory.setVisibility(item.mRight == null ? View.INVISIBLE : View.VISIBLE);
		
		if (item.mLeft != null) {
			ImageLoader.getInstance().loadImage(item.mLeft.mIconUrl, holder.mLeftIcon);
			holder.mLeftTitle.setText(item.mLeft.mTitle);
			holder.mLeftCategory.setTag(item.mLeft);
		}
		if (item.mRight != null) {
			ImageLoader.getInstance().loadImage(item.mRight.mIconUrl, holder.mRightIcon);
			holder.mRightTitle.setText(item.mRight.mTitle);
			holder.mRightCategory.setTag(item.mRight);
		}
		
		return convertView;
	}
	
	/**
	 * 评论列表项
	 */
	public static View buildCommentInfoItemView(LayoutInflater inflater, int position,
			View convertView, ViewGroup parent) {
		CommentInfoHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_detail_comment, null);
			holder = new CommentInfoHolder();
			holder.mStar = (RankStarWidget)convertView.findViewById(R.id.star_average_value);
			holder.mCommentText = (TextView)convertView.findViewById(R.id.text_user_comment);
			holder.mModel = (TextView)convertView.findViewById(R.id.text_foot_model);
			holder.mVersion = (TextView)convertView.findViewById(R.id.text_title_version);
			holder.mUpdateTime = (TextView)convertView.findViewById(R.id.text_foot_updatetime);
			convertView.setTag(holder);
		} else {
			holder = (CommentInfoHolder) convertView.getTag();
		}
		
		return convertView;
	}
	
	
	/**
	 * 带有评级功能的软件列表项
	 */
	public static View buildHorizontalRankItemView(LayoutInflater inflater, int position, App info,
			View convertView, ViewGroup parent, String highLightWord, boolean needRankIndex, boolean hasAdv) {
		HorizontalRankHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_hori_rank_app, null);
			holder = new HorizontalRankHolder();
			holder.mCornerLabel = (TextView) convertView.findViewById(R.id.corner_label);
			holder.mDownInfo = new DownInfoHolder();
			holder.mDownInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.down_button);
			holder.mDownInfo.mIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mDescription = (TextView) convertView.findViewById(R.id.desciprtion);
			holder.mStar = (RankStarWidget) convertView.findViewById(R.id.star);
			convertView.setTag(holder);
		} else {
			holder = (HorizontalRankHolder) convertView.getTag();
		}

		holder.mDownInfo.mItem = info;
		ImageLoader.getInstance().loadImage(info.mIconUrl, holder.mDownInfo.mIcon);
		bindDownButton(holder.mDownInfo, true);
		if (TextUtils.isEmpty(highLightWord)) {
			holder.mLabel.setText(info.mTitle);
		} else {
			Utils.highlight(holder.mLabel, info.mTitle, highLightWord);
		}
		if(needRankIndex) {
			int itemIndex = position;
			if(!hasAdv)
				itemIndex++;
			holder.mLabel.setText((itemIndex) +". "+ info.mTitle);
		}		
		holder.mDescription.setText(info.mCategoryTitle + ITEM_SPILIT_CHAR
				+ Utils.getSizeString(info.mSize) + ITEM_SPILIT_CHAR
				+ info.mVersionName);
		holder.mStar.setRank((int)(info.mStarLevel*2));
		
		return convertView;
	}
	
	/**
	 * 带有更新时间的软件列表项
	 */
	public static View buildHorizontalTimeItemView(LayoutInflater inflater, int position, App info,
			View convertView, ViewGroup parent) {
		HorizontalTimeHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_hori_time_app, null);
			holder = new HorizontalTimeHolder();
			holder.mCornerLabel = (TextView) convertView.findViewById(R.id.corner_label);
			holder.mDownInfo = new DownInfoHolder();
			holder.mDownInfo.mIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.mDownInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.down_button);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mDescription = (TextView) convertView.findViewById(R.id.desciprtion);
			holder.mUpdateTime = (TextView) convertView.findViewById(R.id.update_time);
			convertView.setTag(holder);
		} else {
			holder = (HorizontalTimeHolder) convertView.getTag();
		}

		Locale locale = Locale.getDefault();
		SimpleDateFormat dataFormat = new SimpleDateFormat(DATA_PATTERN, locale);
		String format = holder.mUpdateTime.getContext().getString(R.string.update_time);
		String updateTime = String.format(locale, format, dataFormat.format(new Date(info.mUpdateTime)));
		
		holder.mDownInfo.mItem = info;
		bindDownButton(holder.mDownInfo, true);
		holder.mLabel.setText(info.mTitle);
		holder.mDescription.setText(info.mCategoryTitle + ITEM_SPILIT_CHAR
				+ Utils.getSizeString(info.mSize) + ITEM_SPILIT_CHAR
				+ info.mVersionName);
		holder.mUpdateTime.setText(updateTime);
		ImageLoader.getInstance().loadImage(info.mIconUrl, holder.mDownInfo.mIcon);
		
		return convertView;
	}
	
	/**
	 * 空白
	 */
	public static View buildSpaceItem(int height, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new View(parent.getContext());
			convertView.setLayoutParams(new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT, height));
		}
		
		if (convertView.getLayoutParams().height != height) {
			convertView.setLayoutParams(new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT, height));
		}
		
		return convertView;
	}
	
	/**
	 * 本地软件项
	 */
	public static View buildLocalAppView(LayoutInflater inflater, int position, LocalAppInfo item,
			View convertView, ViewGroup parent) {
		LocalAppViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_local_app, null);
			holder = new LocalAppViewHolder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.mUninstallButton = (Button) convertView.findViewById(R.id.uninstall_button);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mDescription = (TextView) convertView.findViewById(R.id.desciprtion);
			holder.mInstDate = (TextView) convertView.findViewById(R.id.install_time);
			convertView.setTag(holder);
		} else {
			holder = (LocalAppViewHolder) convertView.getTag();
		}

		Context context = parent.getContext();
		Locale locale = Locale.getDefault();
		ImageLoader.getInstance().loadImage(ImageLoader.wrapPackageUriString(item.mPackageName), holder.mIcon);
		SimpleDateFormat dataFormat = new SimpleDateFormat(DATA_PATTERN, locale);
		String sizeFormatter = context.getString(R.string.soft_used_space);
		String apkSize = Utils.getSizeString(item.mAppSize);
		String instDataFormatter = context.getString(R.string.install_time);
		String instData = dataFormat.format(item.mLastUpdateTime);
		
		holder.mUninstallButton.setTag(item);
		holder.mLabel.setText(item.mAppLabel);
		holder.mDescription.setText(String.format(locale, sizeFormatter, apkSize));
		holder.mInstDate.setText(String.format(locale, instDataFormatter, instData));
		
		return convertView;
	}
	
	public static View buildUpdateGroupView(LayoutInflater inflater, int position, String title,
			boolean isExpanded, View convertView, ViewGroup parent) {
		UpdateGroupViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_update_group, null);
			holder = new UpdateGroupViewHolder();
			holder.mContentLayout = convertView.findViewById(R.id.content_layout);
			holder.mEmptyLayout = convertView.findViewById(R.id.empty_view);
			holder.mTitle = (TextView) convertView.findViewById(R.id.group_title);
			convertView.setTag(holder);
		} else {
			holder = (UpdateGroupViewHolder) convertView.getTag();
		}
		
		holder.mTitle.setText(title);
		return convertView;
	}
	
	public static View buildDownloadGroupView(LayoutInflater inflater, int position, String title,
			boolean isExpanded, View convertView, ViewGroup parent, boolean needButton, String buttonTitle, int buttonIconRes) {
		DownloadGroupViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_download_group, null);
			holder = new DownloadGroupViewHolder();
			holder.mContentLayout = convertView.findViewById(R.id.content_layout);
			holder.mEmptyLayout = convertView.findViewById(R.id.empty_view);
			holder.mTitle = (TextView) convertView.findViewById(R.id.group_title);
			holder.mButtonLayout = convertView.findViewById(R.id.layout_group_button);
			holder.mButtonTitle = (TextView)convertView.findViewById(R.id.title_group_button);
			holder.mButtonIcon = (ImageView)convertView.findViewById(R.id.image_group_button);
			convertView.setTag(holder);
		} else {
			holder = (DownloadGroupViewHolder) convertView.getTag();
		}
		
		holder.mTitle.setText(title);
		if(needButton) {
			holder.mButtonTitle.setText(buttonTitle);
			holder.mButtonIcon.setImageResource(buttonIconRes);
			holder.mButtonLayout.setVisibility(View.VISIBLE);
		}	
		else
			holder.mButtonLayout.setVisibility(View.GONE);
		return convertView;
	}
	
	/**
	 * 可更新列表项
	 */
	public static View buildUpdateItemView(LayoutInflater inflater, int position, AppUpdate item,
			boolean isExpand, View convertView, ViewGroup parent) {
		UpdateItemViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_update, null);
			holder = new UpdateItemViewHolder();
			holder.mUpdateInfo = new UpdateInfoHolder();
			holder.mUpdateInfo.mIcon = (ImageView) convertView.findViewById(R.id.icon);		
			holder.mUpdateInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.update_button);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mAppSize = (TextView) convertView.findViewById(R.id.text_app_size);
			holder.mNewVersion = (TextView) convertView.findViewById(R.id.new_version);
			holder.mIgnoreButton = (TextView) convertView.findViewById(R.id.ignore_button);
			holder.mUpdateView = (ExpandableTextView) convertView.findViewById(R.id.update_info_view);
			
			holder.mPatchSize = (TextView)convertView.findViewById(R.id.text_patch_size);
			holder.mPatchLine = (ImageView)convertView.findViewById(R.id.image_patch_line);
			convertView.setTag(holder);
		} else {
			holder = (UpdateItemViewHolder) convertView.getTag();
		}

		Resources res = parent.getResources();
		Locale locale = Locale.getDefault();
		String localFormat = res.getString(R.string.local_version);
		String newFormat = res.getString(R.string.new_version);	
		holder.mUpdateInfo.mItem = item;
		ImageLoader.getInstance().loadImage(ImageLoader.wrapPackageUriString(item.mPackageName), holder.mUpdateInfo.mIcon);
		holder.mPackageName = item.mPackageName;
		holder.mLabel.setText(item.mLabel);
		holder.mAppSize.setText(Utils.getSizeString(item.mFileSize));
		if(item.mHasPatch) {
			holder.mPatchSize.setText(Utils.getSizeString(item.mPatchSize));
			holder.mPatchSize.setVisibility(View.VISIBLE);
			holder.mPatchLine.setVisibility(View.VISIBLE);
		} else {
			holder.mPatchSize.setVisibility(View.GONE);
			holder.mPatchLine.setVisibility(View.GONE);
		}
		String oldVersion = String.format(locale, localFormat, item.mLocalVersion);
		String newVersion = String.format(locale, newFormat, item.mVersion);
		if(!oldVersion.equalsIgnoreCase(newVersion))
			holder.mNewVersion.setText(oldVersion + " → " + newVersion);
		else {
			PackageManager pm =ApplicationImpl.getSelfApplicationContext().getPackageManager();
			try {
				PackageInfo info = pm.getPackageInfo(item.mPackageName, 0);
				int versionCode = info.versionCode; 
				holder.mNewVersion.setText(oldVersion+"_"+ versionCode + " → " + newVersion+"_"+item.mVersionCode);
			} catch (NameNotFoundException e) {
				holder.mNewVersion.setText(oldVersion + " → " + newVersion+"_"+item.mVersionCode);
			}	
		}	
		String strUpdateInfo = item.mUpdateInfo;
		if(TextUtils.isEmpty(strUpdateInfo)) {
			strUpdateInfo = res.getString(R.string.update_description_empty);
		}
		holder.mUpdateView.setChangeLevel(item.isImportantUpdate());
		holder.mUpdateView.setUpdateTime(item.mUpdateTime);
		holder.mIgnoreButton.setTag(item);
		Utils.scaleClickRect(holder.mIgnoreButton);
		holder.mUpdateView.setViewButton(holder.mIgnoreButton);
		
		if (TextUtils.isEmpty(strUpdateInfo)) {
			holder.mUpdateView.setVisibility(View.GONE);
		} else {
			holder.mUpdateView.setText(strUpdateInfo);
			holder.mUpdateView.setExpand(isExpand);
			holder.mUpdateView.setVisibility(View.VISIBLE);
		}
		
		bindUpdateButton(holder.mUpdateInfo);
		return convertView;
	}
	
	/**
	 * 已忽略软件项
	 */
	public static View buildIgnoreItemView(LayoutInflater inflater, int position, AppUpdate item,
			boolean isExpand,View convertView, ViewGroup parent) {
		IgnoreItemViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_ignore, null);
			holder = new IgnoreItemViewHolder();
			holder.mIgnoreInfo = new UpdateInfoHolder();
			holder.mIgnoreInfo.mItem = item;
			holder.mIgnoreInfo.mIcon = (ImageView) convertView.findViewById(R.id.icon);		
			holder.mIgnoreInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.update_button);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mAppSize = (TextView) convertView.findViewById(R.id.text_app_size);
			holder.mNewVersion = (TextView) convertView.findViewById(R.id.new_version);
			holder.mIgnoreCancelButton = (TextView) convertView.findViewById(R.id.cancel_ignore_button);
			holder.mIgnoreView = (ExpandableTextView) convertView.findViewById(R.id.ignore_info_view);
			convertView.setTag(holder);
		} else {
			holder = (IgnoreItemViewHolder) convertView.getTag();
		}

		Resources res = parent.getResources();
		Locale locale = Locale.getDefault();
		String localFormat = res.getString(R.string.local_version);
		String newFormat = res.getString(R.string.new_version);
		String descFormat = res.getString(R.string.update_description);
		if(TextUtils.isEmpty(descFormat)) {
			descFormat = res.getString(R.string.update_description_empty);
		}
		ImageLoader.getInstance().loadImage(ImageLoader.wrapPackageUriString(item.mPackageName), holder.mIgnoreInfo.mIcon);
		holder.mPackageName = item.mPackageName;
		holder.mLabel.setText(item.mLabel);
		holder.mAppSize.setText(Utils.getSizeString(item.mFileSize));
		holder.mNewVersion.setText(String.format(locale, localFormat, item.mLocalVersion) + " → " + 
		String.format(locale, newFormat, item.mVersion));
		String strUpdateInfo = item.mUpdateInfo;
		if(TextUtils.isEmpty(strUpdateInfo)) {
			strUpdateInfo = res.getString(R.string.update_description_empty);
		}
		holder.mIgnoreView.setChangeLevel(item.isImportantUpdate());
		holder.mIgnoreView.setUpdateTime(item.mUpdateTime);
		holder.mIgnoreCancelButton.setTag(item);
		holder.mIgnoreInfo.mDownButton.setTag(item);
		if (TextUtils.isEmpty(strUpdateInfo)) {
			holder.mIgnoreView.setVisibility(View.GONE);
		} else {
			holder.mIgnoreView.setText(strUpdateInfo);
			holder.mIgnoreView.setExpand(isExpand);
			holder.mIgnoreView.setVisibility(View.VISIBLE);
		}
//		bindUpdateButton(holder.mIgnoreInfo);
		return convertView;
	}
	
	/**
	 * 下载管理项
	 */
	public static View buildDownAppView(LayoutInflater inflater, int position, DownloadTask item,
			View convertView, ViewGroup parent) {
		DownAppViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_down_app, null);
			holder = new DownAppViewHolder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.mDownButton = (Button) convertView.findViewById(R.id.down_button);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mProgress = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			holder.mDescription = (TextView) convertView.findViewById(R.id.desciprtion);
			holder.mStatus = (TextView) convertView.findViewById(R.id.status);
			convertView.setTag(holder);
		} else {
			holder = (DownAppViewHolder) convertView.getTag();
		}
		
		int progress = 0;
		if (item.mTotal > 0) {
			progress = (int) (item.mTransfered * 100f / item.mTotal);
		}
		Resources res = parent.getResources();
		String status = "";
		int downBtnTextRes = 0;
		int downBtnRes = 0;
		int downBtnTextColor = 0;
		Drawable progressDrawable = null;
		switch (item.mStatus) {
		case TaskStatus.STATUS_PAUSE:
			status = res.getString(R.string.pausing);
			downBtnRes = R.drawable.btn_gray_bg;
			downBtnTextColor = gray_bg_text_color;
			downBtnTextRes = R.string.continue_down;
			progressDrawable = res.getDrawable(R.drawable.down_progress_pause_drawable);
			break;
		case TaskStatus.STATUS_FAILED:
			status = res.getString(R.string.download_failed);
			downBtnRes = R.drawable.btn_gray_bg;
			downBtnTextColor = gray_bg_text_color;
			downBtnTextRes = R.string.retry;
			progressDrawable = res.getDrawable(R.drawable.down_progress_pause_drawable);
			break;
		case TaskStatus.STATUS_INSTALLING:
			status = "";
			downBtnRes = R.drawable.btn_green_bg;
			downBtnTextColor = green_bg_text_color;
			downBtnTextRes = R.string.installing;
			progressDrawable = res.getDrawable(R.drawable.down_progress_running_drawable);
			break;
		case TaskStatus.STATUS_MERGING:
			status = "";
			downBtnRes = R.drawable.btn_green_bg;
			downBtnTextColor = green_bg_text_color;
			downBtnTextRes = R.string.merge;
			progressDrawable = res.getDrawable(R.drawable.down_progress_running_drawable);
			break;
		case TaskStatus.STATUS_DOWNLOADING:
			status = "";
			downBtnRes = R.drawable.btn_green_bg;
			downBtnTextColor = green_bg_text_color;
			downBtnTextRes = R.string.pause;
			progressDrawable = res.getDrawable(R.drawable.down_progress_running_drawable);
			break;
		case TaskStatus.STATUS_MERGING_INSTALL:
			status = "";
			downBtnRes = R.drawable.btn_green_bg;
			downBtnTextColor = green_bg_text_color;
			downBtnTextRes = R.string.install;
			progressDrawable = res.getDrawable(R.drawable.down_progress_running_drawable);
			break;
		case TaskStatus.STATUS_DOWNLOAD:
			status = "";
			downBtnRes = R.drawable.btn_green_bg;
			downBtnTextColor = green_bg_text_color;
			downBtnTextRes = R.string.install;
			progressDrawable = res.getDrawable(R.drawable.down_progress_running_drawable);
			break;
		case TaskStatus.STATUS_WAIT:
			status = res.getString(R.string.waiting);
			downBtnRes = R.drawable.btn_green_bg;
			downBtnTextColor = green_bg_text_color;
			downBtnTextRes = R.string.pause;
			progressDrawable = res.getDrawable(R.drawable.down_progress_pause_drawable);
			break;
		case TaskStatus.STATUS_INSTALLED:
			status = "";
			downBtnRes = R.drawable.btn_gray_bg;
			downBtnTextColor = gray_bg_text_color;
			downBtnTextRes = R.string.open;
			progressDrawable = res.getDrawable(R.drawable.down_progress_running_drawable);
			break;		
		case TaskStatus.STATUS_UNKNOWN:
		default:
			throw new RuntimeException();
		}
		
		if(!TextUtils.isEmpty(item.mIconData)) {
			if(item.mIconData.equalsIgnoreCase(item.mPackageName))
				ImageLoader.getInstance().loadImage(ImageLoader.wrapPackageUriString(item.mPackageName), holder.mIcon);
			else
				ImageLoader.getInstance().loadImage(item.mIconData, holder.mIcon);
		} else
			ImageLoader.getInstance().loadImage(ImageLoader.wrapPackageUriString(item.mPackageName), holder.mIcon);
		
		holder.mPackageName = item.mPackageName;
		holder.mDownButton.setTag(item);
		holder.mDownButton.setBackgroundResource(downBtnRes);
		holder.mDownButton.setText(downBtnTextRes);
		holder.mDownButton.setTextColor(downBtnTextColor);
		holder.mLabel.setText(item.mTitle);
		
		holder.mProgress.setProgress(progress);
		if (Build.VERSION.SDK_INT >10)
			holder.mProgress.setProgressDrawable(progressDrawable);
		else 
			holder.mProgress.setSecondaryProgress(progress);
	
		if(item.mStatus == TaskStatus.STATUS_INSTALLED || item.mStatus == TaskStatus.STATUS_DOWNLOAD || item.mStatus == TaskStatus.STATUS_INSTALLING
			||item.mStatus == TaskStatus.STATUS_MERGING || item.mStatus == TaskStatus.STATUS_MERGING_INSTALL) {
			holder.mProgress.setVisibility(View.GONE);
			holder.mDescription.setText(Utils.getSizeString(item.mTotal));		
		}else {
			holder.mProgress.setVisibility(View.VISIBLE);
			holder.mDescription.setText(Utils.getSizeString(item.mTransfered) + PROGRESS_CHAR
					+ Utils.getSizeString(item.mTotal));
		}
			
		int statusTextColor = item.mStatus == TaskStatus.STATUS_FAILED
				? res.getColor(R.color.list_item_down_app_failed_color)
				: res.getColor(R.color.list_item_down_app_status_color);	
		if(TaskStatus.STATUS_DOWNLOADING != item.mStatus) {
			holder.mStatus.setText(status);
			holder.mStatus.setTextColor(statusTextColor);
		} else {
			holder.mStatus.setTextColor(Color.parseColor("#999999"));
			if(item.mSpeed == 0)
				holder.mStatus.setText("等待中");
		}
		
		if((item.mStatus == TaskStatus.STATUS_FAILED && item.mTransfered == 0) || item.mStatus == TaskStatus.STATUS_WAIT)
			holder.mDescription.setVisibility(View.GONE);
		else
			holder.mDescription.setVisibility(View.VISIBLE);
		
		holder.mDownButton.setEnabled(item.mStatus != TaskStatus.STATUS_INSTALLING && item.mStatus != TaskStatus.STATUS_MERGING);
		
		return convertView;
	}
	
	public static View buildSearchAssociationAppItemView(LayoutInflater inflater, int position, App info,
			View convertView, ViewGroup parent) {
		SearchAssociationAppItemViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_search_association_app, null);
			holder = new SearchAssociationAppItemViewHolder();
			holder.mCornerLabel = (TextView) convertView.findViewById(R.id.corner_label);
			holder.mDownInfo = new DownInfoHolder();
			holder.mDownInfo.mIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.mDownInfo.mDownButton = (DownStatusButton) convertView.findViewById(R.id.down_button);
			holder.mLabel = (TextView) convertView.findViewById(R.id.label);
			holder.mDescription = (TextView) convertView.findViewById(R.id.desciprtion);
			convertView.setTag(holder);
		} else {
			holder = (SearchAssociationAppItemViewHolder) convertView.getTag();
		}

		holder.mDownInfo.mItem = info;
		ImageLoader.getInstance().loadImage(info.mIconUrl, holder.mDownInfo.mIcon);
		bindDownButton(holder.mDownInfo, true);
		holder.mLabel.setText(info.mTitle);
		holder.mDescription.setText(info.mCategoryTitle + ITEM_SPILIT_CHAR
				+ Utils.getSizeString(info.mSize) + ITEM_SPILIT_CHAR
				+ info.mVersionName);
		
		return convertView;
	}
	
	public static View buildSearchAssociationKeywordItemView(LayoutInflater inflater, int position,
			Keyword keyword, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_search_association, null);
		}
		
		TextView keywordView = (TextView) convertView.findViewById(R.id.association_keyword);
		keywordView.setText(keyword.mWord);
		convertView.setTag(keyword);
		
		return convertView;
	}
	
	public static View buildTopicTitleView(LayoutInflater inflater, int position, TopicHeader topic,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_topic_header, null);
		}
		
		TextView briefView = (TextView) convertView.findViewById(R.id.topic_desc);
		briefView.setText(topic.mBrief);
		
		return convertView;
	}
	
	@Deprecated
	private static void bindDownButton(DownInfoHolder downInfo, boolean isHoriItemButton) {
		DownStatusButton button = downInfo.mDownButton;
		App app = downInfo.mItem;
		
		if(downInfo.mIcon != null) {
			DownloadTask task = DataCenter.getInstance().getTask(app.mPackageName);	
			if(task != null) {
				downInfo.mIcon.setTag(task.mPackageName);
				if(downInfo.mDownButton != null)
					Utils.handleButtonProgress(downInfo.mDownButton, task);
			}else {
				downInfo.mIcon.setTag("");
			}
		}
		
		button.setTag(downInfo);
		int instStatus = app.getInstStatus();
		int taskStatus = app.getTaskStatus();
		
		button.setEnabled(taskStatus != TaskStatus.STATUS_INSTALLING && taskStatus != TaskStatus.STATUS_MERGING);
		
		if (instStatus == LocalApps.STATUS_INSTALLED) {
			button.setText(R.string.open);
			button.setTextColor(gray_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_gray_bg);
			return;
		}
		
		if(taskStatus == TaskStatus.STATUS_DOWNLOADING)
			return;

		switch (taskStatus) {
		case TaskStatus.STATUS_DOWNLOAD:
			button.setText(R.string.install);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_FAILED:
			button.setText(R.string.retry);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_INSTALLING:
			button.setText(R.string.installing);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_MERGING:
			button.setText(R.string.merge);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_PAUSE:
			button.setText(R.string.continue_down);
			button.setTextColor(gray_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_gray_bg);
			break;
		case TaskStatus.STATUS_WAIT:
			button.setText(R.string.waiting);
			button.setTextColor(gray_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_gray_bg);
			break;
		default:
			Utils.clearButtonProgress(downInfo.mDownButton);
			if (instStatus == LocalApps.STATUS_INSTALLED_OLD_VERSION) {
				button.setText(R.string.update);
				button.setTextColor(green_bg_text_color);
				button.setBackgroundResource(R.drawable.btn_green_bg);
			} else {
				button.setText(R.string.download);
				button.setTextColor(green_bg_text_color);
				button.setBackgroundResource(R.drawable.btn_green_bg);
			}
			break;
		}
	}

	@Deprecated
	private static void bindUpdateButton(UpdateInfoHolder updateInfo) {
		Button button = updateInfo.mDownButton;
		AppUpdate au = updateInfo.mItem;
		
		if(updateInfo.mIcon != null) {
			DownloadTask task = DataCenter.getInstance().getTask(au.mPackageName);	
			if(task != null) {
				updateInfo.mIcon.setTag(task.mPackageName);
				if(updateInfo.mDownButton != null)
					Utils.handleButtonProgress(updateInfo.mDownButton, task);
			} else {
				updateInfo.mIcon.setTag("");
			}		
		}
		button.setTag(updateInfo);
		int instStatus = au.getInstStatus();
		int taskStatus = au.getTaskStatus();
		
		button.setEnabled(taskStatus != TaskStatus.STATUS_INSTALLING && taskStatus != TaskStatus.STATUS_MERGING);
				
		if (instStatus == LocalApps.STATUS_INSTALLED) {
			button.setText(R.string.open);
			button.setTextColor(gray_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_gray_bg);
			return;
		}
		
		switch (taskStatus) {
		case TaskStatus.STATUS_DOWNLOAD:
			button.setText(R.string.install);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_DOWNLOADING:
			String  buttonText = button.getText().toString();
			if(buttonText != null && buttonText.contains("%")) {
				//如果已经显示进度，则不做处理
			} else {
				button.setText(R.string.pause);
				button.setTextColor(green_bg_text_color);
				button.setBackgroundResource(R.drawable.btn_green_bg);
			}
			break;
		case TaskStatus.STATUS_FAILED:
			button.setText(R.string.retry);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_INSTALLING:
			button.setText(R.string.installing);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_MERGING:
			button.setText(R.string.merge);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		case TaskStatus.STATUS_PAUSE:
			button.setText(R.string.continue_down);
			button.setTextColor(gray_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_gray_bg);
			break;
		case TaskStatus.STATUS_WAIT:
			button.setText(R.string.pause);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		default:
			Utils.clearButtonProgress(updateInfo.mDownButton);
			button.setText(R.string.update);
			button.setTextColor(green_bg_text_color);
			button.setBackgroundResource(R.drawable.btn_green_bg);
			break;
		}
	}
	
	public static class CommentInfoHolder {
		public RankStarWidget mStar;
		public TextView mCommentText;
		public TextView mModel;
		public TextView mVersion;
		public TextView mUpdateTime;
	}
	
	
	public static class HorizontalRankHolder {
		public TextView mCornerLabel;
		public DownInfoHolder mDownInfo;
		public TextView mLabel;
		public TextView mDescription;
		public RankStarWidget mStar;
	}
	
	public static class HorizontalTimeHolder {
		public TextView mCornerLabel;
		public DownInfoHolder mDownInfo;
		public TextView mLabel;
		public TextView mDescription;
		public TextView mUpdateTime;
	}
	
	public static class TimeAppItemHolder {
		public ImageView mIcon;
		public Button mDownButton;
		public TextView mLabel;
		public TextView mDescription;
		public TextView mUpdateTime;
	}

	public static class CategoryTitleHolder {
		public TextView mTitleView;
	}
	
	public static class DownInfoHolder {
		public App mItem;
		public ImageView mIcon;
		public DownStatusButton mDownButton;
	}
	
	public static class UpdateInfoHolder {
		public AppUpdate mItem;
		public ImageView mIcon;
		public DownStatusButton mDownButton;
	}
	
	public static class RemarkItemHolder {
		public DownInfoHolder mInfoHolder;
		public TextView mCornerLabel;
		public TextView mLabel;
		public TextView mDescription;
		public RankStarWidget mStar;
		public TextView mRemark;
	}
	
	public static class HotItemHolder {
		public View mItemContent;
		public View mLeftLayout;
		public TextView mLeftCornerLabel;
		public DownInfoHolder mLeftDownInfo;
		public LinearLayout mLeftButtonWrapper;
		public TextView mLeftLabel;
		public TextView mLeftDescription;
		public View mRightLayout;
		public TextView mRightCornerLabel;
		public DownInfoHolder mRightDownInfo;
		public LinearLayout mRightButtonWrapper;
		public TextView mRightLabel;
		public TextView mRightDescription;
		public View mBottomDivider;
		
		public RankStarWidget mLeftStar;
		public RankStarWidget mRightStar;
	}
	
	public static class CategoryAdvertHolder {
		public ImageView mLeftImage;
		public ImageView mRightImage;
		public View mDivideLine;
	}
	
	public static class TopicItemHolder {
		public ImageView mLeftImage;
		public ImageView mRightImage;
	}
	
	public static class AdvertItemHolder {
		public View mItemContent;
		public View mLeftAdvert;
		public View mRightAdvert;
		public TextView mLeftTitle;
		public TextView mRightTitle;
		public ImageView mLeftIcon;
		public ImageView mRightIcon;
		public View mBottomDivider;
	}

	public static class CategoryItemHolder {
		public CategoryItem mItemInfo;
		public View mItemContent;
		public View mBottomDivider;
		public View mLeftCategory;
		public View mRightCategory;
		public TextView mLeftTitle;
		public TextView mRightTitle;
		public ImageView mLeftIcon;
		public ImageView mRightIcon;
	}

	public static class LocalAppViewHolder {
		public ImageView mIcon;
		public Button mUninstallButton;
		public TextView mLabel;
		public TextView mDescription;
		public TextView mInstDate;
	}
	
	public static class UpdateGroupViewHolder {
		public View mContentLayout;
		public View mEmptyLayout;
		public TextView mTitle;
	}
	
	public static class DownloadGroupViewHolder {
		public View mContentLayout;
		public View mEmptyLayout;
		public TextView mTitle;		
		
		public TextView mButtonTitle;
		public ImageView mButtonIcon;
		public View mButtonLayout;
	}
	
	public static class UpdateItemViewHolder {
		public String mPackageName;
		public TextView mLabel;
		public TextView mAppSize;
		public TextView mNewVersion;
		public UpdateInfoHolder mUpdateInfo;
		public TextView mIgnoreButton;		
		public ExpandableTextView mUpdateView;
		
		public TextView mPatchSize;
		public ImageView mPatchLine;
	}
	
	public static class IgnoreItemViewHolder {
		public String mPackageName;
		public TextView mLabel;
		public TextView mAppSize;
		public TextView mNewVersion;
		public UpdateInfoHolder mIgnoreInfo;
		public TextView mIgnoreCancelButton;	
		public ExpandableTextView mIgnoreView;
	}
	
	public static class DownAppViewHolder {
		public String mPackageName;
		public ImageView mIcon;
		public Button mDownButton;
		public TextView mLabel;
		public ProgressBar mProgress;
		public TextView mDescription;
		public TextView mStatus;
	}
	
	public static class SearchAssociationAppItemViewHolder {
		public DownInfoHolder mDownInfo;
		public TextView mCornerLabel;
		public TextView mLabel;
		public TextView mDescription;
	}

}
