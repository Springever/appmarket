package com.appmall.market.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appmall.market.R;
import com.appmall.market.bitmaputils.ImageLoader;
import com.appmall.market.fragment.DetailCommentFragment;
import com.appmall.market.widget.RankStarWidget;
import com.appmall.market.widget.RankStarWidget.RankListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.graphics.Bitmap;
import com.appmall.market.common.Utils;

public class CommentPostActivity extends Activity implements OnClickListener {

	public static String EXTRA_APP_ICONURL="iconurl";
	public static String EXTRA_APP_TITLE="apptitle";
	public static String EXTRA_APP_VERSIONNAME="appversion";
	public static String EXTRA_APP_ID="appid";
	public static String EXTRA_APP_REPEATE="repeate";
	
	private Toast mTipToast;
	private RankStarWidget mRankStar;
	private TextView mBtnCommit;
	private TextView mBtnCancel;
	
	private TextView mAppTitle;
	private ImageView mAppIcon;
	private TextView mAppVersion;
	
	private EditText mTextComment;
	private TextView mTextStar;
	private int mAppid;
	private boolean mRepeate = false;
	
	private RelativeLayout mLayoutRoot;
	private static Bitmap mBackgroundBitmap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_commentpost);		
		initView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBackgroundBitmap = null;
	}
	
	public static void setBackgroundBitmap(Bitmap bm) {
		mBackgroundBitmap = bm;
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		mLayoutRoot = (RelativeLayout)findViewById(R.id.layout_comment_dialog);
		mRankStar = (RankStarWidget) findViewById(R.id.rank_star);
		mBtnCommit = (TextView)findViewById(R.id.comment_dialog_commit);
		mBtnCancel = (TextView)findViewById(R.id.comment_dialog_cancel);
		mTextComment = (EditText)findViewById(R.id.edit_comment);
		mTextStar = (TextView)findViewById(R.id.text_rank_star);
		
		
		mAppTitle = (TextView)findViewById(R.id.dialog_head_title);
		mAppVersion = (TextView)findViewById(R.id.dialog_head_text_version);
		mAppIcon = (ImageView)findViewById(R.id.dialog_head_icon);
		
		if(getIntent() != null) {
			if(mBackgroundBitmap != null) {
				mLayoutRoot.setBackgroundDrawable(Utils.bitmapToDrawable(mBackgroundBitmap));
			}
			
			mAppTitle.setText(getIntent().getStringExtra(EXTRA_APP_TITLE));
			mAppVersion.setText(getIntent().getStringExtra(EXTRA_APP_VERSIONNAME));
			mAppid = getIntent().getIntExtra(EXTRA_APP_ID, 0);
			ImageLoader.getInstance().loadImage(getIntent().getStringExtra(EXTRA_APP_ICONURL), mAppIcon);
			mRepeate = getIntent().getBooleanExtra(EXTRA_APP_REPEATE, false);
		}

		mRankStar.setStarDrawable(R.drawable.rank_star_big, R.drawable.empty_star_big);
		mRankStar.setStarMargin(getResources().getDimensionPixelOffset(R.dimen.feedback_rankstar_margin));
		mRankStar.enableRank(true);
		mRankStar.setRank(0);
		mRankStar.setListener(mOnRankChanged);
		mBtnCommit.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}
	
	private RankListener mOnRankChanged = new RankListener() {
		@Override
		public void onRankChanged() {
			mTextStar.setVisibility(View.VISIBLE);
			
			int star = mRankStar.getRank() / 2;
			switch (star) {
			case 1:
				mTextStar.setText(R.string.rank_text_star_1);
				break;
			case 2:
				mTextStar.setText(R.string.rank_text_star_2);
				break;
			case 3:
				mTextStar.setText(R.string.rank_text_star_3);
				break;
			case 4:
				mTextStar.setText(R.string.rank_text_star_4);
				break;
			case 5:
				mTextStar.setText(R.string.rank_text_star_5);
				break;
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_button:
			finish();
			break;
		case R.id.comment_dialog_commit:
			if(mRepeate) {
				showTip(R.string.comment_repeate);
				setResult(RESULT_CANCELED);
				finish();
			} else if(mRankStar.getRank() <2) {
				showTip(R.string.comment_rank_info);
			}
			else {
				String inputText = mTextComment.getText().toString();
				String checkText = inputText.replaceAll(" ", "");
				
				if(checkText.length() == 0) {
					showTip(R.string.comment_word_empty);
				} else {
					String content = checkText;
					String model = android.os.Build.MODEL;
					int starLevel = mRankStar.getRank();
					
					Intent resultIntent = new Intent();
					resultIntent.putExtra(DetailCommentFragment.EXTRA_COMMENT_CONTENT, content);
					resultIntent.putExtra(DetailCommentFragment.EXTRA_COMMENT_MODEL, model);
					resultIntent.putExtra(DetailCommentFragment.EXTRA_COMMENT_ISNEW, 1);
					resultIntent.putExtra(DetailCommentFragment.EXTRA_COMMENT_STARLEVEL, starLevel);
					resultIntent.putExtra(DetailCommentFragment.EXTRA_APP_CHANNEL_ID, mAppid);
								
					setResult(RESULT_OK, resultIntent);
					finish();
				}
			}

			break;
		case R.id.comment_dialog_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}

	private void showTip(int feedbackRes) {
		if (mTipToast == null) {
			mTipToast = Toast.makeText(this, feedbackRes, Toast.LENGTH_SHORT);
		} else {
			mTipToast.setText(feedbackRes);
		}
		
		mTipToast.show();
	}
	
}
