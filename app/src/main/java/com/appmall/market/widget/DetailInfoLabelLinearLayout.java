package com.appmall.market.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import com.appmall.market.R;
import android.text.TextUtils;


public class DetailInfoLabelLinearLayout extends LinearLayout {
	
	public static class LabelInfo {
		public String mTitle;
		public int mTextColorType=0;	//0 = 绿色 	1 = 警告
	}
	
	private Context mContext;
	
	public DetailInfoLabelLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mContext = context;
        setOrientation(LinearLayout.HORIZONTAL);
	}

	public DetailInfoLabelLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext = context;
        setOrientation(LinearLayout.HORIZONTAL);
	}
  
    public DetailInfoLabelLinearLayout(Context context) {  
        super(context);
        mContext = context;
        setOrientation(LinearLayout.HORIZONTAL);
    }  
 
    private int LABEL_MARGIN = 10;
	public void addLabelList(ArrayList<LabelInfo> labelList) {
		if(labelList == null) return;
		removeAllViews();
		for(LabelInfo labelInfo : labelList) {
			TextView tv = new TextView(mContext);
			tv.setPadding(0, 0, LABEL_MARGIN, 0);
			tv.setText(" "+labelInfo.mTitle);
			tv.setSingleLine(true);
			tv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
			tv.setGravity(Gravity.CENTER);
			Drawable safeTipDrawable = getResources().getDrawable(R.drawable.app_security_ok);
			safeTipDrawable.setBounds(0, 0, safeTipDrawable.getIntrinsicWidth(),safeTipDrawable.getIntrinsicHeight());
			
			Drawable dangerTipDrawable = getResources().getDrawable(R.drawable.app_security_alarm);
			dangerTipDrawable.setBounds(0, 0, dangerTipDrawable.getIntrinsicWidth(),dangerTipDrawable.getIntrinsicHeight());
			
			if(labelInfo.mTextColorType == 0) {
				tv.setCompoundDrawables(safeTipDrawable, null, null, null);
				tv.setTextColor(Color.parseColor("#00af00"));
			}		
			else {
				tv.setCompoundDrawables(dangerTipDrawable, null, null, null);
				tv.setTextColor(Color.parseColor("#ffbb33"));
			}
				
			LayoutParams layoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParam.gravity = Gravity.CENTER_VERTICAL;
			addView(tv, layoutParam);
		}
	}
}
