package com.appmall.market.widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.appmall.market.R;

/**
 * 带折叠标记的文本框
 *
 */
public class ExpandableTextView extends View {

	private boolean mExpand;
	private String mText;
	
	private boolean mMultiLine;
	private int mDrawablePadding;
	private Paint mPaint;
	private Paint mHighLightPaint;
	private Drawable mExpandDrawable;
	private Drawable mShrinkDrawable;
	private ArrayList<String> mSplits;
	private boolean mIsImportUpdate  = false;
	private long mUpdateTime;
	private View mViewButton;
	private Context mContext;
	private static final String DATA_PATTERN = "yyyy-MM-dd";
	private String mTitle = "";
	private String mImportText = "";
	private final int DEFAULT_TEXT_LINES = 1;

	public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView(context);
	}

	public ExpandableTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ExpandableTextView(Context context) {
		this(context, null, 0);
	}
	
	public void setChangeLevel(boolean isImport) {
		mIsImportUpdate = isImport;
	}
	
	public void setUpdateTime(long updateTime) {
		mUpdateTime = updateTime;
	}
	
	public void setViewButton(View viewButton) {
		mViewButton = viewButton;
	}
	
	private void initView(Context context) {
		mSplits = new ArrayList<String>();
		
		Resources res = context.getResources();
		float textSize = res.getDimensionPixelSize(R.dimen.common_item_desc_text_size);
		int textColor = res.getColor(R.color.list_item_update_info_text_color);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);
		mPaint.setColor(textColor);
		
		mHighLightPaint = new Paint();
		mHighLightPaint.setAntiAlias(true);
		mHighLightPaint.setTextSize(textSize);
		mHighLightPaint.setColor(res.getColor(R.color.update_item_import_change));
		
		mExpandDrawable = res.getDrawable(R.drawable.update_folder_down_bg);
		mShrinkDrawable = res.getDrawable(R.drawable.update_folder_up_bg);
		mDrawablePadding = res.getDimensionPixelSize(R.dimen.list_item_update_info_arrow_padding);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width = 0;
        int height = 0;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int realWidth = widthSize - paddingLeft - paddingRight;
        
        String text = mText == null ? "" : mText;
        Locale locale = Locale.getDefault();
		String format = mContext.getString(R.string.update_time);
		SimpleDateFormat dataFormat = new SimpleDateFormat(DATA_PATTERN, locale);
		String strUpdateTime = String.format(locale, format, dataFormat.format(new Date(mUpdateTime)));
		mTitle = "更新时间：" + strUpdateTime;
        if(mIsImportUpdate)
        	mImportText = mContext.getResources().getString(R.string.important_update)+"\n";
        else
        	mImportText = "\n";
        	
        int textHeight = mPaint.getFontMetricsInt(null)+8;
        float textWidth = mPaint.measureText(text);
        
        if (widthMode == MeasureSpec.EXACTLY) {
        	width = widthSize;
        } else {
        	width = (int) Math.min(textWidth + paddingLeft + paddingRight, widthSize);
        }
        
        mSplits.clear();
        
        Drawable arrowDrawable = mExpand ? mShrinkDrawable : mExpandDrawable;
        float scaleRatio = 1;
		float drawableWidth = arrowDrawable.getIntrinsicWidth() * scaleRatio;
		
        String nextLine = breakNextLine(text, realWidth - mDrawablePadding - drawableWidth);
        if (nextLine == null) {
        	mMultiLine = false;
        } else {
        	mMultiLine = (nextLine.length() < text.length());
        }
        
        if (mMultiLine) {
    		mSplits.add(removeBreakChar(nextLine));
    		
        	boolean thatIsAll = false;
        	String textRemind = text;
        	int breakIndex = nextLine.length();
        	
        	do {
        		textRemind = textRemind.substring(breakIndex);
        		nextLine = breakNextLine(textRemind, realWidth);
        		if (TextUtils.isEmpty(nextLine))
        			break;
        		
        		breakIndex = nextLine.length();
        		thatIsAll = breakIndex >= textRemind.length();
        		mSplits.add(removeBreakChar(nextLine));
        	} while (!thatIsAll);
        } else {
        	mSplits.add(removeBreakChar(text));
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
        	height = heightSize;
        } else {
        	if(mSplits.size() <DEFAULT_TEXT_LINES)
        		height = textHeight;
        	else
        		height = textHeight * (mExpand ? mSplits.size()+2 : DEFAULT_TEXT_LINES+1);
        }
        	        	
        setMeasuredDimension(width, height);
	}

	private String removeBreakChar(String text) {
		if (TextUtils.isEmpty(text))
			return text;
		
		// 去除换行符
		if (text.charAt(text.length() - 1) == '\n') {
			return text.substring(0, text.length() - 1);
		} else {
			return text;
		}
	}

	final private String breakNextLine(String text, float maxWidth) {
		if (text == null)
			return null;

		if (maxWidth <= 0)
			return null;
		
		int index = mPaint.breakText(text, true, maxWidth, null);
//		if(index == text.length()-1)
//			return null;
		String measuredText = text.substring(0, index);
		if(mExpand) 
		{
			int crIndex = measuredText.indexOf('\n');
			if (crIndex != -1) {
				if (crIndex == 0)
					return "\n";
				
				return text.substring(0, crIndex + 1);
			}
		}
		
		return text.substring(0, index);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int lineCount = mSplits.size();
		if (lineCount <= 0)
			return;
		
		int lineHeight = mPaint.getFontMetricsInt(null)+8;
		float textStartPosX = getPaddingLeft();
		float textBaselinePosY = -mPaint.ascent();
		
		int line = 0;
		{
			canvas.drawText(mTitle, 0, textBaselinePosY, mPaint);
			float titleWidth = mPaint.measureText(mTitle);
			canvas.drawText(mImportText, titleWidth+20, textBaselinePosY, mHighLightPaint);
		}
		
		if (lineCount > DEFAULT_TEXT_LINES) {		
			float firstPosX = textStartPosX ;
			String lineText = mSplits.get(line);
			canvas.drawText(lineText, firstPosX, textBaselinePosY +lineHeight, mPaint);
			line ++;
		}

		int beg = line;
		for (; line < lineCount; ++line) {
			String lineText = mSplits.get(line);
			canvas.drawText(lineText, textStartPosX, lineHeight+textBaselinePosY + (line * lineHeight), mPaint);
			if(line == beg && !mExpand && (mSplits.size() >DEFAULT_TEXT_LINES || mViewButton != null)) {
				Drawable arrowDrawable = mExpand ? mShrinkDrawable : mExpandDrawable;
				float scaleRatio = 1;
				float drawableWidth = arrowDrawable.getIntrinsicWidth() * scaleRatio;
				float drawableHeight = arrowDrawable.getIntrinsicHeight()* scaleRatio;
				int offsetTop = (int)(lineHeight-drawableHeight)/2-3;
				arrowDrawable.setBounds((int)(getWidth()- getPaddingRight()-drawableWidth), offsetTop + lineHeight, (int) (getWidth()- getPaddingRight()), (int) drawableHeight+offsetTop+lineHeight);
				arrowDrawable.draw(canvas);
			}
		}
		
		if(mExpand) {
			Drawable arrowDrawable = mExpand ? mShrinkDrawable : mExpandDrawable;
			float scaleRatio = 1;
			float drawableWidth = arrowDrawable.getIntrinsicWidth() * scaleRatio;
			float drawableHeight = arrowDrawable.getIntrinsicHeight()* scaleRatio;
			int offsetTop = (int)(lineHeight-drawableHeight)/2+4;
			arrowDrawable.setBounds((int)(getWidth()- getPaddingRight()-drawableWidth), offsetTop + lineHeight*(lineCount+1), (int) (getWidth()- getPaddingRight()), (int) drawableHeight+offsetTop+ lineHeight*(lineCount+1));
			arrowDrawable.draw(canvas);
		}
	}

	public void setText(String text) {
		mText = text;
		requestLayout();
	}

	public void setExpand(boolean isExpand) {
		if(mSplits.size() >DEFAULT_TEXT_LINES || mViewButton != null) 
		{
			mExpand = isExpand;
			requestLayout();
		}
		if(mViewButton != null) {
			mViewButton.setVisibility(mExpand ? View.VISIBLE : View.GONE);
			mViewButton.setEnabled(mExpand ? true : false);
		}
			
	}
	
}
