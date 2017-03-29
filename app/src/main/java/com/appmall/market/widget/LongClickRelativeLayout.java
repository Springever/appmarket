package com.appmall.market.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


public class LongClickRelativeLayout extends RelativeLayout {
    private int mLastMotionX, mLastMotionY;  
    //是否移动了  
    private boolean isMoved;  
    //长按的runnable  
    private Runnable mLongPressRunnable;  
    //移动的阈值  
    private static final int TOUCH_SLOP = 5;  
  
    public LongClickRelativeLayout(Context context) {  
        super(context);  
        mLongPressRunnable = new Runnable() {  
              
            @Override  
            public void run() {
            	performLongClick();  
            }  
        };  
    }  
  
    private long dragResponseMS = 100;
    public boolean dispatchTouchEvent(MotionEvent event) {  
        int x = (int) event.getX();  
        int y = (int) event.getY();  
        switch(event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            mLastMotionX = x;  
            mLastMotionY = y;  
            isMoved = false;  
            postDelayed(mLongPressRunnable, dragResponseMS);  
            break;  
        case MotionEvent.ACTION_MOVE:  
            if(isMoved) break;
            if(Math.abs(mLastMotionX-x) > TOUCH_SLOP   
                    || Math.abs(mLastMotionY-y) > TOUCH_SLOP) {  
                //移动超过阈值，则表示移动了  
                isMoved = true;  
                removeCallbacks(mLongPressRunnable);  
            }  
            break;  
        case MotionEvent.ACTION_UP:
            //释放了  
            removeCallbacks(mLongPressRunnable);  
            break;
	    case MotionEvent.ACTION_CANCEL:
	        removeCallbacks(mLongPressRunnable);  
	        break;  
        } 

        return true;  
    }  
	
}
