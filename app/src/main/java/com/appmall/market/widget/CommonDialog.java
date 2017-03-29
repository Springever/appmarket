package com.appmall.market.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.appmall.market.R;

/**
 * 通用的对话框
 *  
 *
 */
public class CommonDialog extends Dialog {

	private TextView mMessageView;
	private TextView mTitleView;
	private ImageView mCheckBox;
	private TextView mCheckBoxText;
	private RelativeLayout mContentView;
	private View mCheckBoxLayout;
	private View mButtonLayout;
	private Button mPositiveButton;
	private Button mNegativeButton;
	private boolean mAutoDismiss;

	private View.OnClickListener mInnerClickListner = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_positive_button:
				if (mPositiveListener != null) {
					mPositiveListener.onClick(CommonDialog.this, BUTTON_POSITIVE);
				}
				break;
				
			case R.id.dialog_negative_button:
				if (mNegativeListener != null) {
					mNegativeListener.onClick(CommonDialog.this, BUTTON_NEGATIVE);
				}
				break;
			}
			
			if (mAutoDismiss) {
				dismiss();
			}
		}
	};
	
	private boolean mCheck = false;
	private View.OnClickListener mCheckBoxClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mCheck = !mCheck;
			if(mCheck)
				mCheckBox.setImageResource(R.drawable.dialog_checkbox_checked);
			else
				mCheckBox.setImageResource(R.drawable.dialog_checkbox_normal);
		}
	};
	
	public boolean isChecked() {
		return mCheck;
	}
	
	private OnClickListener mPositiveListener;
	private OnClickListener mNegativeListener;
	
	public CommonDialog(Context context) {
		this(context, true);
	}

	public CommonDialog(Context context, boolean autoDismiss) {
		super(context, R.style.DialogStyle);
		
		setContentView(R.layout.dialog_layout);
		mTitleView = (TextView) findViewById(R.id.dialog_title);
		mContentView = (RelativeLayout) findViewById(R.id.dialog_content);
		mMessageView = (TextView) findViewById(R.id.dialog_message);
		mButtonLayout = findViewById(R.id.dialog_button_layout);
		mCheckBoxLayout = findViewById(R.id.checkbox_layout);
		mCheckBox = (ImageView) findViewById(R.id.check_box);
		mCheckBox.setImageResource(R.drawable.dialog_checkbox_normal);
		mCheckBoxText = (TextView) findViewById(R.id.check_box_text);
		mPositiveButton = (Button) findViewById(R.id.dialog_positive_button);
		mNegativeButton = (Button) findViewById(R.id.dialog_negative_button);
		
		setCanceledOnTouchOutside(false);
		mAutoDismiss = autoDismiss;
	}
	
	public void setTitle(int titleRes) {
		this.setTitle(getContext().getResources().getString(titleRes));
	}
	
	public void setCheckBoxVisible(boolean visible) {
		if(visible) {
			mCheckBox.setVisibility(View.VISIBLE);
			mCheckBoxText.setVisibility(View.VISIBLE);
		}		
		else {
			mCheckBox.setVisibility(View.GONE);	
			mCheckBoxText.setVisibility(View.GONE);
		}		
	}
	
	public void setTitle(CharSequence title) {
		findViewById(R.id.dialog_title_divider).setVisibility(View.VISIBLE);
		mTitleView.setVisibility(View.VISIBLE);
		mTitleView.setText(title);
	}
	
	public void setMessage(int messageRes) {
		this.setMessage(getContext().getResources().getString(messageRes));
	}
	
	public void setMessage(CharSequence message) {
		mMessageView.setVisibility(View.VISIBLE);
		mMessageView.setText(message);
	}
	
	public void setContent(View view) {
		mContentView.removeAllViews();
		mContentView.addView(view, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
	}

	public void setCheckBox(boolean checked, int checkBoxRes) {
		this.setCheckBox(checked, getContext().getResources().getString(checkBoxRes));
	}
	
	public void setCheckBox(boolean checked, CharSequence checkBox) {
		mCheckBoxLayout.setVisibility(View.VISIBLE);
		mCheckBoxLayout.setOnClickListener(mCheckBoxClickListener);	
		mCheck = checked;
		if(mCheck)
			mCheckBox.setImageResource(R.drawable.dialog_checkbox_checked);
		else
			mCheckBox.setImageResource(R.drawable.dialog_checkbox_normal);
		mCheckBoxText.setText(checkBox);
	}
	
	public void setPositiveButton(int textRes, boolean highlight, DialogInterface.OnClickListener l) {
		mButtonLayout.setVisibility(View.VISIBLE);
		mPositiveButton.setVisibility(View.VISIBLE);
		mPositiveButton.setText(textRes);
		mPositiveButton.setOnClickListener(mInnerClickListner);
		
		if (highlight) {
			int color = getContext().getResources().getColor(
					R.color.green_bg_text_color);
			mPositiveButton.setTextColor(color);
		}
		
		mPositiveListener = l;
	}
	
	public void setPositiveButton(String text, DialogInterface.OnClickListener l) {
		mButtonLayout.setVisibility(View.VISIBLE);
		mPositiveButton.setVisibility(View.VISIBLE);
		mPositiveButton.setText(text);
		mPositiveButton.setOnClickListener(mInnerClickListner);
		
		mPositiveListener = l;
	}
	
	public void setNegativeButton(int textRes, DialogInterface.OnClickListener l) {
		mButtonLayout.setVisibility(View.VISIBLE);
		mNegativeButton.setVisibility(View.VISIBLE);
		mNegativeButton.setText(textRes);
		mNegativeButton.setOnClickListener(mInnerClickListner);
		
		mNegativeListener = l;
	}
	
	public void setNegativeButton(String text, DialogInterface.OnClickListener l) {
		mButtonLayout.setVisibility(View.VISIBLE);
		mNegativeButton.setVisibility(View.VISIBLE);
		mNegativeButton.setText(text);
		mNegativeButton.setOnClickListener(mInnerClickListner);
		
		mNegativeListener = l;
	}

}
