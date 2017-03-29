package com.appmall.market.bitmaputils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

public class HttpUtils {

    @SuppressWarnings("deprecation")
	private static void setupProxy(Context context, HttpClient client) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connMgr.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			String proxyAddress = null;
			int proxyPort;
			
			if (Build.VERSION.SDK_INT >= 15) {
				proxyAddress = System.getProperty("http.proxyHost");
				String portStr = System.getProperty("http.proxyPort");
				proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
			} else {
				proxyAddress = android.net.Proxy.getHost(context);
			    proxyPort = android.net.Proxy.getPort(context);
			}
			
			if (proxyPort > 0 && !TextUtils.isEmpty(proxyAddress)) {
				HttpHost host = new HttpHost(proxyAddress, proxyPort);
				ConnRouteParams.setDefaultProxy(client.getParams(), host);
			}
		}
	}
    
    private static boolean isGZipContent(HttpEntity entity) {
		if (entity == null)
			return false;
		
		Header encoding = entity.getContentEncoding();
		return encoding != null && "gzip".equalsIgnoreCase(encoding.getValue());
	}

	private static void setHeader(HttpUriRequest request) {
		if (request == null)
			return;
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
	}
	
	/**
	 * 下载文件<br>
	 * @param url URL
	 * @param file 用于保存的文件
	 * @return 下载成功返回true，失败返回false
	 */
	public static boolean downloadFile(Context context, HttpClient client, String url, File file) {
		File dir = new File(file.getParent());
		File tmpFile = new File(dir, "_tmp");
		if (tmpFile.exists())
			tmpFile.delete();
		
		setupProxy(context, client);
		HttpGet request = new HttpGet(url);
		setHeader(request);
		HttpResponse resp = null;
		HttpEntity entity = null;
		InputStream respStream = null;
		OutputStream outstream = null;
		GZIPInputStream gzipRespStream = null;
		
		try {
			resp = client.execute(request);
			StatusLine status = null;
			if (resp == null || (status = resp.getStatusLine()) == null)
				return false;
			int statusCode = status.getStatusCode();
			if (statusCode < 200 || statusCode >= 300)
				return false;
			entity = resp.getEntity();
			
			if (entity == null || (respStream = entity.getContent()) == null)
				return false;
			
			InputStream in = null;
			outstream = new FileOutputStream(tmpFile);
			if (isGZipContent(entity)) {
				gzipRespStream = new GZIPInputStream(respStream);
				in = gzipRespStream;
			} else {
				in = respStream;
			}
			
			int count = 0;
			byte[] buffer = new byte[10240];
			while ((count = in.read(buffer, 0, 10240)) >= 0) {
				outstream.write(buffer, 0, count);
			}
			
			tmpFile.renameTo(file);
			return true;
			
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (respStream != null)
					respStream.close();
				if (gzipRespStream != null)
					gzipRespStream.close();
				if (outstream != null)
					outstream.close();
				if (entity != null)
					entity.consumeContent();
			} catch (IOException e) { }
		}
	}
	
}
