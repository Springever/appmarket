package com.appmall.market.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.adapter.AppListRankAdapter;
import com.appmall.market.adapter.AppListRankAdapter.Callback;
import com.appmall.market.adapter.AssociationAdapter;
import com.appmall.market.adapter.ItemBuilder.DownInfoHolder;
import com.appmall.market.bean.App;
import com.appmall.market.bean.Keywords;
import com.appmall.market.bean.Keywords.Keyword;
import com.appmall.market.bean.SearchAssociation;
import com.appmall.market.bean.SearchRecommend;
import com.appmall.market.bean.SearchResult;
import com.appmall.market.common.CommonInvoke;
import com.appmall.market.common.Utils;
import com.appmall.market.data.DataCenter;
import com.appmall.market.data.DataCenter.Options;
import com.appmall.market.data.DataCenter.Response;
import com.appmall.market.data.DataPage;
import com.appmall.market.data.IDataCallback;
import com.appmall.market.data.IDataConstant;
import com.appmall.market.download.DownloadTask;
import com.appmall.market.widget.KeywordsFlow;
import com.appmall.market.widget.LoadMoreListView;
import com.appmall.market.widget.LoadMoreListView.OnLoadMoreListener;
import com.appmall.market.bean.Advert;
import com.appmall.market.bitmaputils.ImageLoader;
import android.widget.RelativeLayout;

/**
 * 搜索界面
 *  
 *
 */
public class SearchActivity extends BaseActivity implements IDataCallback, OnClickListener, 
		KeywordsFlow.onItemClickListener, OnEditorActionListener, OnLoadMoreListener, Callback,
		TextWatcher, AssociationAdapter.Callback {

	private static final long ASSOCIATION_DELAY = 500;

	private AppListRankAdapter mResultAdapter;
	private AssociationAdapter mAssociationAdapter;
	private EditText mSearchEditor;
	private ListView mAssociationList;
	private TextView mSearchButton;
	private Button mClearSearchButton;
	private LoadMoreListView mResultList;
	private Button mNextRowButton;
	private Toast mLoadMoreFailedToast;
	private ImageView mBackButton;
	private LinearLayout mKeywordContent;
	private String[] mColors;
	
	private boolean mChangeBySetText;
	private DataPage mDataPage;
	private Keywords mKeywords;
	private KeywordPicker mKeywordPicker;
	private ArrayList<App> mResult;
	
	private SearchAssociation mSearchAssociation;
	private RelativeLayout mEmptyLayout;
	private boolean mIsSearchLoadMoreFlag;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_search);
		
		mSearchEditor = (EditText) findViewById(R.id.search_edittext);
		mSearchButton = (TextView) findViewById(R.id.search_button);
		mClearSearchButton = (Button) findViewById(R.id.clear_search_text_button);
		mKeywordContent = (LinearLayout) findViewById(R.id.keyword_content_layout);
		mNextRowButton = (Button) findViewById(R.id.next_row_button);
		mResultList = (LoadMoreListView) findViewById(R.id.search_list);
//		mResultEmptyView = (TextView) findViewById(R.id.empty_text);
//		mEmptyViewHeader = (TextView) findViewById(R.id.empty_list_header);
		mEmptyLayout = (RelativeLayout) findViewById(R.id.empty_layout);
		mAssociationList = (ListView) findViewById(R.id.association_list_view);
		mBackButton = (ImageView)findViewById(R.id.back_button);
		mAssociationList.setCacheColorHint(Color.TRANSPARENT);
		
		mResultAdapter = new AppListRankAdapter(this);
		mResultAdapter.registerCallback(this);
		
		mResultList.setOnLoadMoreListener(this);
		mResultList.setAdapter(mResultAdapter);
//		mResultList.setEmptyView(mResultEmptyView);
		mResultList.setCacheColorHint(Color.TRANSPARENT);
		
		mColors = getResources().getStringArray(R.array.search_colors);
		
		initView();
		
		int dataId = IDataConstant.SEARCH_TAGS;
		DataCenter.getInstance().requestDataAsync(this, dataId, this, null);
	}
		
	protected void onTaskProgress(DownloadTask task) {
		if (task == null || TextUtils.isEmpty(task.mPackageName))
			return;
		if(mResultList.getVisibility() == View.VISIBLE)
			Utils.handleButtonProgress(mResultList, R.id.down_button, task);
		if(mAssociationList.getVisibility() == View.VISIBLE)
			Utils.handleButtonProgress(mAssociationList, R.id.down_button, task);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Process the outside touch event of menu fragment
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			processOutsideTouchOnAssociationList(ev);
			processOutsideTouchOnEditor(ev);
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	private void processOutsideTouchOnEditor(MotionEvent ev) {
		int[] location = new int[2];
		mSearchEditor.getLocationOnScreen(location);
		int right = location[0] + mSearchEditor.getWidth();
		int bottom = location[1] + mSearchEditor.getHeight();
		Rect rtView = new Rect(location[0], location[1], right, bottom);
		
		int x = (int) ev.getRawX();
		int y = (int) ev.getRawY();
		if (!rtView.contains(x, y)) {
			InputMethodManager manager = (InputMethodManager) getSystemService(
					Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(mSearchEditor.getWindowToken(), 0);
		}
	}

	private void processOutsideTouchOnAssociationList(MotionEvent ev) {
		if (mAssociationList.getVisibility() != View.VISIBLE)
			return;
		
		int[] location = new int[2];
		mAssociationList.getLocationOnScreen(location);
		int right = location[0] + mAssociationList.getWidth();
		int bottom = location[1] + mAssociationList.getHeight();
		Rect rtView = new Rect(location[0], location[1], right, bottom);
		
		int x = (int) ev.getRawX();
		int y = (int) ev.getRawY();
		if (!rtView.contains(x, y)) {
			setAssociationListVisible(false);
		}
	}

	private void initView() {
		mClearSearchButton.setOnClickListener(this);
		mSearchButton.setOnClickListener(this);
		mSearchEditor.setOnEditorActionListener(this);
		mSearchEditor.addTextChangedListener(this);
		mNextRowButton.setOnClickListener(this);
		mBackButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mSearchButton) {
			onSearch();
		} else if (v == mNextRowButton) {
			nextKeyword();
		} else if (v == mClearSearchButton) {
			mChangeBySetText = true;
			mSearchEditor.setText("");
		} else if(v == mBackButton)
			finish();
	}
	
	private void onSearch() {
		mEmptyLayout.setVisibility(View.GONE);
		CharSequence chars = mSearchEditor.getText();
		if (TextUtils.isEmpty(chars))
			return;
		
		String searchWord = chars.toString();
		onSearch(searchWord, null);
	}

	private void onSearch(String keyword, String searchUrl) {
		mChangeBySetText = true;
		mSearchEditor.setText(keyword);
		int indexEnd = keyword.length();
		if(indexEnd < 40)
			mSearchEditor.setSelection(indexEnd, indexEnd);
		
		InputMethodManager manager = (InputMethodManager) getSystemService(
				Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(mSearchEditor.getWindowToken(), 0);

		setSearchLayoutVisible(true);
		Utils.startLoadingAnimation(findViewById(R.id.loading_layout));
		mResultList.hideEndFooterView();
		mResultList.setVisibility(View.GONE);
		
		mDataPage = new DataPage();
		mResult = new ArrayList<App>();
		mResultAdapter.setData(mResult, keyword);
		
		int dataId = IDataConstant.SEARCH_RESULT;
		Options options = new Options();
		if (TextUtils.isEmpty(searchUrl)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("keyword", keyword);
			options.mUrlAppend = Utils.buildQueryString(params);
		} else {
			options.mCustomUrl = searchUrl;
		}
		DataCenter dc = DataCenter.getInstance();
		dc.requestDataAsync(this, dataId, this, options);
		mIsSearchLoadMoreFlag = false;
		setAssociationListVisible(false);
	}

	private void setSearchLayoutVisible(boolean visible) {
		View keywordLayout = findViewById(R.id.keyword_layout);
		View searchListLayout = findViewById(R.id.search_list_layout);
		keywordLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
		searchListLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private boolean isShowResultList() {
		return findViewById(R.id.search_list_layout).getVisibility() == View.VISIBLE;
	}

	private void nextKeyword() {
		if (mKeywordPicker == null)
			return;
		
		List<Advert> picAdverts = mKeywordPicker.nextPicAdverts();
		List<Advert> textAdverts = mKeywordPicker.nextTextAdverts();
		List<Keyword> keywords = mKeywordPicker.nextKeywords();
		
		mKeywordContent.removeAllViews();
		if (addAdvertView(picAdverts))
			addDividerView();
		if (addTextAdvertView(textAdverts))
			addDividerView();
		if (addKeywordsView(keywords))
			addDividerView();
	}
	
	private boolean addAdvertView(List<Advert> adverts) {
		if (adverts == null || adverts.size() < 3)
			return false;
		
		Advert advert1 = adverts.get(0);
		Advert advert2 = adverts.get(1);
		Advert advert3 = adverts.get(2);
		if (advert1 == null || advert2 == null || advert3 == null)
			return false;
		
		View advertView = getLayoutInflater().inflate(R.layout.list_item_search_advert, null);
		ImageView imageView1 = (ImageView) advertView.findViewById(R.id.search_advert1);
		ImageView imageView2 = (ImageView) advertView.findViewById(R.id.search_advert2);
		ImageView imageView3 = (ImageView) advertView.findViewById(R.id.search_advert3);
		
		ImageLoader.getInstance().loadImage(advert1.mImageUrl, imageView1);
		ImageLoader.getInstance().loadImage(advert2.mImageUrl, imageView2);
		ImageLoader.getInstance().loadImage(advert3.mImageUrl, imageView3);
		
		TextView textView1 = (TextView) advertView.findViewById(R.id.search_text_view1);
		textView1.setText(advert1.mTitle);
		TextView textView2 = (TextView) advertView.findViewById(R.id.search_text_view2);
		textView2.setText(advert2.mTitle);
		TextView textView3 = (TextView) advertView.findViewById(R.id.search_text_view3);
		textView3.setText(advert3.mTitle);
		
		View advertView1 = advertView.findViewById(R.id.advert1);
		advertView1.setTag(advert1);
		advertView1.setOnClickListener(mOnAdvertClicked);

		View advertView2 = advertView.findViewById(R.id.advert2);
		advertView2.setTag(advert2);
		advertView2.setOnClickListener(mOnAdvertClicked);

		View advertView3 = advertView.findViewById(R.id.advert3);
		advertView3.setTag(advert3);
		advertView3.setOnClickListener(mOnAdvertClicked);
		
		mKeywordContent.addView(advertView);
		return true;
	}
	
	private boolean addTextAdvertView(List<Advert> textAdverts) {
		if (textAdverts == null || textAdverts.size() < 3)
			return false;
		
		Advert advert1 = textAdverts.get(0);
		Advert advert2 = textAdverts.get(1);
		Advert advert3 = textAdverts.get(2);
		if (advert1 == null || advert2 == null || advert3 == null)
			return false;
		
		View advertView = getLayoutInflater().inflate(R.layout.list_item_search, null);
		
		TextView textView1 = (TextView) advertView.findViewById(R.id.search_text_view1);
		textView1.setTag(advert1);
		textView1.setText(advert1.mTitle);
		textView1.setOnClickListener(mOnAdvertClicked);

		TextView textView2 = (TextView) advertView.findViewById(R.id.search_text_view2);
		textView2.setTag(advert2);
		textView2.setText(advert2.mTitle);
		textView2.setOnClickListener(mOnAdvertClicked);
		
		TextView textView3 = (TextView) advertView.findViewById(R.id.search_text_view3);
		textView3.setTag(advert3);
		textView3.setText(advert3.mTitle);
		textView3.setOnClickListener(mOnAdvertClicked);
		
		mKeywordContent.addView(advertView);
		return true;
	}

	private boolean addKeywordsView(List<Keyword> keywords) {
		if (keywords == null || keywords.size() < 3)
			return false;
				
		Keyword kw1 = keywords.get(0);
		Keyword kw2 = keywords.get(1);
		Keyword kw3 = keywords.get(2);
		if (kw1 == null || kw2 == null || kw3 == null)
			return false;
		
		View keywordView = getLayoutInflater().inflate(R.layout.list_item_search, null);
		
		TextView textView1 = (TextView) keywordView.findViewById(R.id.search_text_view1);
		textView1.setTag(kw1);
		textView1.setTextColor(getRandomColor());
		textView1.setText(kw1.mWord);
		textView1.setOnClickListener(mOnKeywordClicked);
		
		TextView textView2 = (TextView) keywordView.findViewById(R.id.search_text_view2);
		textView2.setTag(kw2);
		textView2.setTextColor(getRandomColor());
		textView2.setText(kw2.mWord);
		textView2.setOnClickListener(mOnKeywordClicked);

		TextView textView3 = (TextView) keywordView.findViewById(R.id.search_text_view3);
		textView3.setTag(kw3);
		textView3.setTextColor(getRandomColor());
		textView3.setText(kw3.mWord);
		textView3.setOnClickListener(mOnKeywordClicked);
		
		mKeywordContent.addView(keywordView);
		return true;
	}
	
	private void addDividerView() {		
		View view = new View(this);
		int height = getResources().getDimensionPixelSize(R.dimen.keyword_layout_margin_hori);
		view.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, height));
		
		mKeywordContent.addView(view);
	}
	
	private OnClickListener mOnAdvertClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof Advert))
				return;
			
			Advert adv = (Advert) tag;
			Utils.jumpDetailActivity(SearchActivity.this, adv);
		}
	};
	
	private OnClickListener mOnKeywordClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof Keyword))
				return;
			
			Keyword kw = (Keyword) tag;
			onSearch(kw.mWord, kw.mDataSource);
		}
	};
	
	private int getRandomColor(){
		String colorString = mColors[(int) (Math.random() * mColors.length)];
		return Color.parseColor(colorString);
	}
	
	@Override
	public void onDataObtain(int dataId, Response resp) {
		if (dataId == IDataConstant.SEARCH_TAGS) {
			if (!resp.mSuccess || resp.mData == null)
				return;
			mKeywords = (Keywords) resp.mData;
			mKeywordPicker = new KeywordPicker(mKeywords);
			nextKeyword();
		} else if (dataId == IDataConstant.SEARCH_RESULT) {
			Utils.stopLoadingAnimation(findViewById(R.id.loading_layout));
			mEmptyLayout.setVisibility(View.GONE);
			mResultList.setVisibility(View.VISIBLE);
			mResultList.onLoadMoreComplete();
			
			SearchResult result = null;
			if (resp.mSuccess && resp.mData != null) {
				result = (SearchResult) resp.mData;
				if (result.mApps != null && result.mApps.size() > 0) {
					addSearchResult(result.mApps);
					refreshAppData();
					if(!mIsSearchLoadMoreFlag)
						mResultList.setSelection(0);
					boolean hasNext = mDataPage.parseNextUrlFromContext(resp.mContext);
					mResultList.setNeedLoadMore(hasNext);
					mDataPage.increaseCount();
					if (!hasNext && mDataPage.getPageCount() > 1) {
						mResultList.showEndFooterView();
					}
					return;
				}
			}
				
			//获取推荐数据
			if(!mIsSearchLoadMoreFlag) {
				boolean bHasRecommendData = false;			
				if(resp.mSuccess && resp.mOtherData != null) {
					SearchRecommend recommendData = (SearchRecommend) resp.mOtherData;
					if(recommendData != null && recommendData.mApps.size()>0) {
						addSearchResult(recommendData.mApps);
						refreshAppData();
						mResultList.setNeedLoadMore(false);
						mEmptyLayout.setVisibility(View.VISIBLE);
						bHasRecommendData = true;
					}else {
						mEmptyLayout.setVisibility(View.VISIBLE);
						findViewById(R.id.empty_list_header).setVisibility(View.GONE);
					}
				}
				
				if(!bHasRecommendData) {
					// 无效数据
					if (mDataPage.getPageCount() >= 1) {
						showLoadMoreFailedToast();
						return;
					}
				}	
			}
			return;
		} else if (dataId == IDataConstant.SEARCH_ASSOCIATION) {
			if (!resp.mSuccess || resp.mData == null || !(resp.mData instanceof SearchAssociation)) {
				setAssociationListVisible(false);
				return;
			}
			mSearchAssociation = (SearchAssociation) resp.mData;
			
			if (mAssociationAdapter == null) {
				mAssociationAdapter = new AssociationAdapter(this);
				mAssociationAdapter.registerCallback(this);
				mAssociationList = (ListView) findViewById(R.id.association_list_view);
				mAssociationList.setAdapter(mAssociationAdapter);
			}

			setAssociationListVisible(true);
			mAssociationAdapter.setData(mSearchAssociation.mApps, mSearchAssociation.mKeywords);
			refreshAppData();
		}
	}

	@Override
	protected void refreshAppData() {
		DataCenter dc = DataCenter.getInstance();
		
		if (mSearchAssociation != null && mAssociationAdapter != null) {
			CommonInvoke.processApps(dc, mSearchAssociation.mApps, false);
			mAssociationAdapter.notifyDataSetChanged();
		}
		
		if (mResult != null && mResultAdapter != null) {
			CommonInvoke.processApps(dc, mResult, false);
			mResultAdapter.notifyDataSetChanged();
		}
	}

	private void showLoadMoreFailedToast() {
		if (mLoadMoreFailedToast == null) {
			mLoadMoreFailedToast = Toast.makeText(this, R.string.load_more_failed, Toast.LENGTH_SHORT);
		}
		mLoadMoreFailedToast.show();
	}

	private void addSearchResult(ArrayList<App> apps) {
		if (apps == null || apps.size() <= 0)
			return;
		
		for (App app : apps) {
			mResult.add(app);
		}
	}

	@Override
	public void onBackPressed() {
		if (isShowResultList()) {
			setSearchLayoutVisible(false);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onItemClicked(String item, String url) {
		if (TextUtils.isEmpty(item) || TextUtils.isEmpty(url))
			return;
		
		onSearch(item, url);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			onSearch();
			return true;
		}
		
		return false;
	}

	@Override
	public void onLoadMore() {
		String url = mDataPage.getNextUrl();
		if (TextUtils.isEmpty(url)) {
			mResultList.onLoadMoreComplete();
			showLoadMoreFailedToast();
			return;
		}
		
		int dataId = IDataConstant.SEARCH_RESULT;
		Options options = new Options();
		options.mCustomUrl = url;
		DataCenter.getInstance().requestDataAsync(this, dataId, this, options);
		mIsSearchLoadMoreFlag = true;
	}

	@Override
	public void onDownload(DownInfoHolder downInfo) {
		CommonInvoke.processAppDownBtn(this, downInfo.mIcon, downInfo.mItem, false);
	}

	@Override
	public void onAppDetail(App app) {
		Utils.jumpAppDetailActivity(this, app);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }

	@Override
	public void afterTextChanged(Editable s) {
		if (TextUtils.isEmpty(s)) {
			setSearchLayoutVisible(false);
			setAssociationListVisible(false);
			return;
		}
		
		if (mChangeBySetText) {
			// Filter setText() changed.
			mChangeBySetText = false;
			return;
		}
		
		mSearchEditor.removeCallbacks(mAssociationSearch);
		mSearchEditor.postDelayed(mAssociationSearch, ASSOCIATION_DELAY);
	}
	
	public void setAssociationListVisible(boolean visible) {
		mAssociationList.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	private Runnable mAssociationSearch = new Runnable() {
		@Override
		public void run() {
			Editable s = mSearchEditor.getEditableText();
			if (TextUtils.isEmpty(s))
				return;
			
			String keyword = s.toString();
			int dataId = IDataConstant.SEARCH_ASSOCIATION;
			Map<String, String> params = new HashMap<String, String>();
			params.put("keyword", keyword);
			
			Options options = new Options();
			options.mUrlAppend = Utils.buildQueryString(params);
			DataCenter.getInstance().requestDataAsync(SearchActivity.this, dataId, 
					SearchActivity.this, options);
		}
	};

	@Override
	public void onAppClicked(App item) {
		Utils.jumpAppDetailActivity(this, item);
		setAssociationListVisible(false);
	}

	@Override
	public void onAppDownload(DownInfoHolder downInfo) {
		CommonInvoke.processAppDownBtn(this, downInfo.mIcon, downInfo.mItem, false);
//		setAssociationListVisible(false);
	}

	@Override
	public void onKeywordClicked(Keyword keyword) {
		onSearch(keyword.mWord, keyword.mDataSource);
		setAssociationListVisible(false);
	}
	
	private class KeywordPicker {
		
		private Keywords mValue;
		private int mNextAdvertIndex = -1;
		private int mNextTextAdvertIndex = -1;
		private int mNextKeywordIndex = -1;

		public KeywordPicker(Keywords keywords) {
			mValue = keywords;
		}
		
		public List<Advert> nextPicAdverts() {
			if (mValue == null || mValue.mPicAdverts == null || mValue.mPicAdverts.size() < 3)
				return null;
			List<Advert> ret = new ArrayList<Advert>();
			if (mNextAdvertIndex == -1)
				mNextAdvertIndex = 0;
			for (int count = 0; count < 3; ++count) {
				if (mNextAdvertIndex >= mValue.mPicAdverts.size())
					mNextAdvertIndex = 0;
				ret.add(mValue.mPicAdverts.get(mNextAdvertIndex++));
			}
			return ret;
		}
		
		public List<Advert> nextTextAdverts() {
			if (mValue == null || mValue.mTextAdverts == null || mValue.mTextAdverts.size() < 3)
				return null;
			List<Advert> ret = new ArrayList<Advert>();
			if (mNextTextAdvertIndex == -1)
				mNextTextAdvertIndex = 0;
			for (int count = 0; count < 3; ++count) {
				if (mNextTextAdvertIndex >= mValue.mTextAdverts.size())
					mNextTextAdvertIndex = 0;
				ret.add(mValue.mTextAdverts.get(mNextTextAdvertIndex++));
			}
			return ret;
		}
		
		public List<Keyword> nextKeywords() {
			if (mValue == null || mValue.mKeywords == null || mValue.mKeywords.size() < 3)
				return null;
			List<Keyword> ret = new ArrayList<Keyword>();
			if (mNextKeywordIndex == -1)
				mNextKeywordIndex = 0;
			for (int count = 0; count < 3; ++count) {
				if (mNextKeywordIndex >= mValue.mKeywords.size())
					mNextKeywordIndex = 0;
				ret.add(mValue.mKeywords.get(mNextKeywordIndex++));
			}
			return ret;
		}
	}

}
