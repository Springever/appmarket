package com.appmall.market.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appmall.market.R;

/**
 * 设置界面中的单个设置项
 *  
 *
 */
public class SettingItem extends LinearLayout {

	public static final int TYPE_DIALOG_TIP = 1;
	public static final int TYPE_CHECKBOX = 2;
	
	private TextView mTitle;
	private TextView mDescription;
	private ImageView mCheckBox;

	public SettingItem(Context context, AttributeSet attrs, int defStyle) {
		this(context, TYPE_DIALOG_TIP);
	}
	public SettingItem(Context context, AttributeSet attrs) {
		this(context, TYPE_DIALOG_TIP);
	}
	public SettingItem(Context context) {
		this(context, TYPE_DIALOG_TIP);
	}

	public SettingItem(Context context, int settingType) {
		super(context);
		
		initItem();
		
		switch (settingType) {			
		case TYPE_CHECKBOX:
			findViewById(R.id.setting_checkbox).setVisibility(View.VISIBLE);
			break;
		}
	}

	
	private void initItem() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.setting_item_layout, this);
		mTitle = (TextView) content.findViewById(R.id.setting_title);
		mDescription = (TextView) content.findViewById(R.id.setting_description);
		mCheckBox = (ImageView) content.findViewById(R.id.setting_checkbox);
	}

	public void setTitle(String title) {
		mTitle.setText(title);
	}
	
	public void setTitle(int titleRes) {
		mTitle.setText(titleRes);
	}
	
	public void setDescription(String description) {
		mDescription.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);
		mDescription.setText(description);
	}
	
	public void setDescription(int descRes) {
		mDescription.setVisibility(View.VISIBLE);
		mDescription.setText(descRes);
	}

	/**
	 * 设定CheckBox的开关状态
	 */
	public void setCheckBoxStatus(boolean isOn) {
		if(isOn)
			mCheckBox.setImageResource(R.drawable.dialog_checkbox_checked);
		else
			mCheckBox.setImageResource(R.drawable.dialog_checkbox_normal);
//		mCheckBox.setChecked(isOn);
	}
	
}
