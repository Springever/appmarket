package com.appmall.market;

import android.app.Application;
import android.content.Context;

import com.appmall.market.bitmaputils.ImageLoader;
import com.appmall.market.common.Statistics;
import com.appmall.market.common.UninstallUtility;

import java.lang.Thread.UncaughtExceptionHandler;

public class ApplicationImpl extends Application {

	private static Context mApplicationContext;

	@Override
	public void onCreate() {
		super.onCreate();
		mApplicationContext = getApplicationContext();
		ImageLoader.getInstance().initLoader(mApplicationContext);
		UninstallUtility.notifyUninstall(mApplicationContext);
		UncaughtExceptionHandler exHandler = Thread.getDefaultUncaughtExceptionHandler();
		UncaughtExceptionHandler newExHandler = new LoggerExceptionHandler(mApplicationContext, exHandler);
		Thread.setDefaultUncaughtExceptionHandler(newExHandler);
	}
	
	public static Context getSelfApplicationContext() {
		return mApplicationContext;
	}

	public static class LoggerExceptionHandler implements UncaughtExceptionHandler {

		private Context mAppContext;
		private UncaughtExceptionHandler mOriginHandler;
		
		public LoggerExceptionHandler(Context context, UncaughtExceptionHandler origin) {
			mAppContext = context;
			mOriginHandler = origin;
		}

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			Throwable cause = ex;
			Throwable t = null;
			while ((t = cause.getCause()) != null) {
				cause = t;
				t = cause.getCause();
			}
			
			String message = cause.getMessage();
			String className = null;
			int line = -1;
			
			StackTraceElement[] stackTrace = cause.getStackTrace();
			if (stackTrace != null && stackTrace.length > 0) {
				className = stackTrace[0].getClassName();
				line = stackTrace[0].getLineNumber();
			}
			
			Statistics.addException(mAppContext, "Class:" + className + ",Line:" + line + ",Message:" + message);
			mOriginHandler.uncaughtException(thread, ex);
		}
	}
	
}
