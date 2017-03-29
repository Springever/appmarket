package com.appmall.market.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.appmall.market.R;

/**
 * 下载管理界面的上下文弹出菜单
 *
 */
public class DownMgrContextMenu extends PopupWindow {

	private Context mAppContext;
	private DisplayMetrics mScreenMetrics;
	
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				dismiss();
				return true;
			}
			
			return false;
		}
	};

	public DownMgrContextMenu(Context context) {
		super(context);
		mAppContext = context.getApplicationContext();
		mScreenMetrics = new DisplayMetrics();
		
		getScreenSize();
		setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(true);
		setTouchInterceptor(mOnTouchListener);
	}
	
	private void getScreenSize() {
		WindowManager wm = (WindowManager) mAppContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(mScreenMetrics);
	}

	public void setContntView(View content) {
		setContentView(content);
	}
	
	public void show(View anchor) {
		View contentView = getContentView();
		if (contentView == null || anchor == null)
			return;
		
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);	
		Resources res = mAppContext.getResources();
		int yOffset = res.getDimensionPixelSize(R.dimen.down_app_manage_context_menu_bg_padding);
		int contentWidth = contentView.getMeasuredWidth();
		int contentHeight = contentView.getMeasuredHeight() +yOffset;
		
		int anchorPosY = location[1] + anchor.getHeight();
		boolean reverse = anchorPosY + contentHeight > mScreenMetrics.heightPixels;
		setBackgroundDrawable(pickBackground(reverse));
		
		int xOff = (int) ((anchor.getWidth() - contentWidth) / 2f);
		int yOff = reverse ? -(anchor.getHeight() + contentHeight)+yOffset : -yOffset;
		showAsDropDown(anchor, xOff, yOff);
	}

	final private Drawable pickBackground(boolean reverse) {
		int resId = reverse ? R.drawable.down_mgr_context_menu_reverse_bg : R.drawable.down_mgr_context_menu_normal_bg;
		return mAppContext.getResources().getDrawable(resId);
	}

}
