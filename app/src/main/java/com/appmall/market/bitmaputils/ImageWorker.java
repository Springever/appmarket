package com.appmall.market.bitmaputils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public abstract class ImageWorker {
    private static final int FADE_IN_TIME = 500;
    
    static final String HTTP = "http";
	static final String HTTPS = "https";
	static final String PACKAGE = "package";
	static final String FILE = "file";
	static final String ASSET = "asset";

    private ImageCache mImageCache;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    private ScaleType mImageScaleType = ScaleType.FIT_XY;

    protected Context mContext;
    private PackageManager mPackageManager;
    
    protected ImageWorker(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
    }

    public void loadImage(String data, ImageView imageView) {
    	loadImage(data, imageView, null);
    }

    public void loadImage(String data, ImageView imageView, LoadOptions opts) {
        Bitmap bitmap = null;

        String scheme = getDataScheme(data);
        if (TextUtils.isEmpty(scheme))
        	return;
        
    	if (PACKAGE.equals(scheme)) {
    		try {
    			int index = data.indexOf(":") + 1;
				String packageName = data.substring(index);
    			Drawable iconDrawable = mPackageManager.getApplicationIcon(packageName);
    			imageView.setScaleType(ScaleType.FIT_CENTER);
    			imageView.setImageDrawable(iconDrawable);
    			return;
    		} catch (Exception e) { }
    	}
    	
        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (bitmap != null) {
        	imageView.setScaleType(mImageScaleType);
        	imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mLoadingBitmap, task);
            imageView.setScaleType(ScaleType.CENTER);
    		imageView.setWillNotCacheDrawing(false);
            imageView.setImageDrawable(asyncDrawable);
            
            if (opts == null)
            	opts = new LoadOptions();
            task.execute(data, opts);
        }
    }

    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }
    
    public void setScaleType(ScaleType scaleType) {
    	mImageScaleType = scaleType;
    }

    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
    }

    public void setImageCache(ImageCache cacheCallback) {
        mImageCache = cacheCallback;
    }

    public ImageCache getImageCache() {
        return mImageCache;
    }
    
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }
    
    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
    }
    
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (mFadeInBitmap) {
            final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent),
                    new BitmapDrawable(mContext.getResources(), bitmap) });
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageBitmap(bitmap);
        }
        
        imageView.setScaleType(mImageScaleType);
    }

    protected abstract Bitmap processBitmap(String data, LoadOptions opts);

    protected String getDataScheme(String data) {
    	if (TextUtils.isEmpty(data))
    		return null;
    	
    	Uri uri = Uri.parse(data);
        return uri.getScheme();
    }
    
    protected String getDataContent(String data) {
    	if (TextUtils.isEmpty(data))
    		return null;
    	
    	Uri uri = Uri.parse(data);
		return uri.getPath();
		
    }
    
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
        private Object data;
        private final WeakReference<ImageView> imageViewReference;
        private int mImageViewHeightReq = 0;
        private int mImageViewWidthReq = 0;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }
        
        @Override
        protected Bitmap doInBackground(Object... params) {
        	Process.setThreadPriority(Thread.MIN_PRIORITY);
        	
            String data = (String) params[0];
            LoadOptions opts = (LoadOptions) params[1];
            if(opts != null && opts.mImageViewHeight >0)
            	mImageViewHeightReq = opts.mImageViewHeight;
            if(opts != null && opts.mImageViewWidth >0)
            	mImageViewWidthReq = opts.mImageViewWidth;
            
            if (opts == null || (opts.mReqWidth <= 0 && opts.mReqHeight <= 0)) {
            	// 从ImageView中获取尺寸
            	ImageView refView = imageViewReference.get();
                if (refView != null) {
                	opts.mReqWidth = refView.getWidth();
                	opts.mReqHeight = refView.getHeight();
                }
            } else {
            	// Options中指定了尺寸
            }

            if (opts.mReqWidth <= 0 || opts.mReqHeight <= 0) {
            	// 无尺寸数据，保障加载大小不超过屏幕大小
            	DisplayMetrics outMetrics = new DisplayMetrics();
            	WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
				wm.getDefaultDisplay().getMetrics(outMetrics);
				
				opts.mReqWidth = outMetrics.widthPixels;
				opts.mReqHeight = outMetrics.heightPixels;
            }
            
            Bitmap bitmap = null;

            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(data);
            }

            if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
                bitmap = processBitmap(data, opts);
            }

            if (bitmap != null && mImageCache != null) {
                mImageCache.addBitmapToCache(data, bitmap);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || mExitTasksEarly) {
                bitmap = null;
            }

            ImageView imageView = getAttachedImageView();
            //处理高度固定
            if(imageView != null && bitmap != null && mImageViewHeightReq >0  && bitmap.getHeight()>0 && bitmap.getWidth()>0) {
            	imageView.getLayoutParams().height = mImageViewHeightReq;
            	int diyWidth = bitmap.getWidth()*mImageViewHeightReq/bitmap.getHeight();
            	imageView.getLayoutParams().width = diyWidth;
            }
            //处理宽度固定
            if(imageView != null && bitmap != null && mImageViewWidthReq >0  && bitmap.getHeight()>0 && bitmap.getWidth()>0) {
            	imageView.getLayoutParams().width = mImageViewWidthReq;
            	int diyHeight = bitmap.getHeight()*mImageViewWidthReq/bitmap.getWidth();
            	imageView.getLayoutParams().height = diyHeight;
            }
            if (bitmap != null && imageView != null) {
            	imageView.setScaleType(mImageScaleType);
                setImageBitmap(imageView, bitmap);
            }
        }
        
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    public static class LoadOptions {
    	public int mReqWidth;
    	public int mReqHeight;
    	public int mImageViewHeight = 0; //固定高度
    	public int mImageViewWidth = 0; //固定宽度
    	public boolean mRotateToPortrait;
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);

            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    
}
