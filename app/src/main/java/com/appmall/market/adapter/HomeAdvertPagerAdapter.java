package com.appmall.market.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.appmall.market.bean.Advert;
import com.appmall.market.bitmaputils.ImageLoader;

public class HomeAdvertPagerAdapter extends PagerAdapter {

	public static interface OnItemClickedListener {
		public void onAdvertViewClicked(Advert advert);
	}
	
	private List<Advert> mAdverts;
	private WeakReference<OnItemClickedListener> mListenerRef;

	public void registerItemClickedListener(OnItemClickedListener listener) {
		mListenerRef = new WeakReference<HomeAdvertPagerAdapter.OnItemClickedListener>(listener);
	}
	
	public void setAdvertData(List<Advert> adverts) {
		mAdverts = adverts;
		notifyDataSetChanged();
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		return mAdverts == null ? 0 : mAdverts.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View itemView = (View) object;
		container.removeView(itemView);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		Context context = container.getContext();
		ImageView itemView = new ImageView(context);
		itemView.setLayoutParams(params);
		itemView.setScaleType(ScaleType.FIT_XY);
		itemView.setOnClickListener(mClickProcessor);
		
		Advert advert = mAdverts.get(position);
		String url = advert == null ? "" : advert.mImageUrl;
		ImageLoader.getInstance().loadImage(url, itemView);
		
		itemView.setTag(position);
		
		if (itemView.getParent() != null) {
			((ViewGroup) itemView.getParent()).removeView(itemView);
		}
		
		container.addView(itemView);
		
		return itemView;
	}
	
	private OnClickListener mClickProcessor = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mListenerRef == null)
				return;
			OnItemClickedListener listener = mListenerRef.get();
			if (listener == null)
				return;
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof Integer))
				return;
			Advert advert = mAdverts.get((Integer) tag);
			listener.onAdvertViewClicked(advert);
		}
	};
}
