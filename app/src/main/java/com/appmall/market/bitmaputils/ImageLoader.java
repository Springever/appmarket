package com.appmall.market.bitmaputils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.appmall.market.R;
import com.appmall.market.bitmaputils.ImageCache.ImageCacheParams;
import com.appmall.market.bitmaputils.ImageWorker.LoadOptions;

public class ImageLoader {

	private static final String CACHE_NAME = "Images";
	private static final String THUMBNAIL_CACHE_NAME = "Thumbnails";
	private static ImageLoader sInstance;

	public static ImageLoader getInstance() {
		if (sInstance == null) {
			sInstance = new ImageLoader();
		}
		
		return sInstance;
	}
	
	public static String wrapPackageUriString(String packageName) {
		Uri uri = Uri.parse(ImageWorker.PACKAGE + ":" + packageName);
		return uri.toString();
	}
	
	private ImageFetcher mFetcher;
	private ImageFetcher mThumbnailFetcher;
	
	private ImageLoader() { }
	
	public void initLoader(Context context) {
		ImageCache cache = new ImageCache(context, CACHE_NAME);
		
		mFetcher = new ImageFetcher(context);
		mFetcher.setImageCache(cache);
		mFetcher.setLoadingImage(R.drawable.default_app_icon);
		
		ImageCacheParams cacheParams = new ImageCacheParams(THUMBNAIL_CACHE_NAME);
		cacheParams.memoryCacheEnabled = false;
		ImageCache thumbnailCache = new ImageCache(context, cacheParams);
		mThumbnailFetcher = new ImageFetcher(context);
		mThumbnailFetcher.setImageCache(thumbnailCache);
		mThumbnailFetcher.setLoadingImage(R.drawable.detail_default_icon);
	}

	public void loadImage(String data, ImageView imageView) {
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setWillNotCacheDrawing(false);
		mFetcher.loadImage(data, imageView);
	}
		
	public void loadLargeImageByReqHeight(String data, ImageView imageView, int reqHeight) {
		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		imageView.setWillNotCacheDrawing(false);

		LoadOptions opts = null;
		{
			opts = new LoadOptions();
			opts.mImageViewHeight = reqHeight;
		}
		
		mThumbnailFetcher.loadImage(data, imageView, opts);
	}
	
	public void loadLargeImageByReqWidth(String data, ImageView imageView, int reqWidth) {
		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		imageView.setWillNotCacheDrawing(false);

		LoadOptions opts = null;
		{
			opts = new LoadOptions();
			opts.mImageViewWidth = reqWidth;
		}
		
		mThumbnailFetcher.loadImage(data, imageView, opts);
	}
	
	public void loadLargeImageByFITXY(String data, ImageView imageView) {
		imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setWillNotCacheDrawing(false);	
		mThumbnailFetcher.loadImage(data, imageView, null);
	}
	
}
