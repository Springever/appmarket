package com.appmall.market.bitmaputils;

import java.io.File;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import com.appmall.market.common.Utils;
import android.graphics.BitmapFactory;


public class ImageFetcher extends ImageResizer {
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String HTTP_CACHE_DIR = "http";
    
	private static final int TIME_OUT = 10 * 1000;
	
	private DefaultHttpClient mClient;

    public ImageFetcher(Context context) {
        super(context);
        
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, TIME_OUT);
        httpParams.setIntParameter(HttpConnectionParams.SO_TIMEOUT, TIME_OUT);
        mClient = new DefaultHttpClient(httpParams);
    }
    
    @Override
    protected synchronized Bitmap processBitmap(String data, LoadOptions opts) {
    	String scheme = getDataScheme(data);
    	String content = getDataContent(data);
    	
    	if (TextUtils.isEmpty(scheme))
    		return null;
    	
    	if ((HTTP.equals(scheme) || HTTPS.equals(scheme)) && !TextUtils.isEmpty(content)) {
    		final File f = downloadBitmap(mContext, data);
    		
    		if (f == null)
    			return null;
    		
    		Uri uri = Uri.fromFile(f);
    		return super.processBitmap(uri.toString(), opts);
    	} else if (isAsset(data)) {
    		Uri uri = Uri.parse(data);
    		byte[] buffer = Utils.readFromAsset(mContext, uri.getSchemeSpecificPart());
    		if (buffer != null) {
    			return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
    		}
    	}
    	
        return null;
    }
    
    protected boolean isAsset(String data) {
    	if (TextUtils.isEmpty(data))
    		return false;
    	
    	Uri uri = Uri.parse(data);
    	String scheme = uri.getScheme();
    	return ASSET.equalsIgnoreCase(scheme);
    }

	public File downloadBitmap(Context context, String urlString) {
        final File cacheDir = DiskLruCache.getDiskCacheDir(context, HTTP_CACHE_DIR);

        final DiskLruCache cache =
                DiskLruCache.openCache(context, cacheDir, HTTP_CACHE_SIZE);

        if (cache == null)
        	return null;
        
        final File cacheFile = new File(cache.createFilePath(urlString));

        if (cache.containsKey(urlString)) {
            return cacheFile;
        }

        boolean success = HttpUtils.downloadFile(context, mClient, urlString, cacheFile);
        return success ? cacheFile : null;
    }

}
