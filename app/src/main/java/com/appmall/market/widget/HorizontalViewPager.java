package com.appmall.market.widget;

import android.widget.HorizontalScrollView;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.content.Context;
import android.view.View;

public class HorizontalViewPager extends ViewPager {
	
    public HorizontalViewPager(Context context) {
        super(context);
    }

    public HorizontalViewPager(Context context, AttributeSet attrs) {
       super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof HorizontalScrollView) {
                return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}