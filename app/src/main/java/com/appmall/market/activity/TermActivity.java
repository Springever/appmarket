package com.appmall.market.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appmall.market.R;
import com.appmall.market.common.Utils;

public class TermActivity extends Activity {

	public static final String KEY_OPEN_URL = "url";
	public static final String KEY_TOPBAR_TITLE = "topbar_title";
	private RelativeLayout		mLayoutLoading;
	private ImageView			mImageLoading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_term);
		initView();
	}
	
	private void initView() {
		mImageLoading = (ImageView) findViewById(R.id.image_loading);
		mLayoutLoading = (RelativeLayout) findViewById(R.id.layout_loading);
		Intent intent = getIntent();
		String url = intent.getStringExtra(KEY_OPEN_URL);
		String title = intent.getStringExtra(KEY_TOPBAR_TITLE);
		if(url != null) {
			WebView webView = (WebView) findViewById(R.id.web_view);
			webView.setWebViewClient(mWebViewClient);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setNeedInitialFocus(false);
			webView.getSettings().setAppCacheEnabled(false);
			webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);  
			webView.loadUrl(url);
		}
		if(title != null) {
			findViewById(R.id.top_bar_layout).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.title)).setText(title);
			findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					finish();
				}
			});
		} else {
			findViewById(R.id.top_bar_layout).setVisibility(View.GONE);
		}
	}
	
	private WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			startLoading();
			view.setVisibility(View.GONE);
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			stopLoading();
			view.setVisibility(View.VISIBLE);
			super.onPageFinished(view, url);
		}
		
        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            view.destroy();
            view = null;
            showLoadingFailedLayout();
        }
        
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
	};
	
	private void showLoadingFailedLayout() {
		View failedLayout = findViewById(R.id.loading_failed_layout);
		TextView resultText = (TextView) failedLayout.findViewById(R.id.failed_result);
		TextView tipText = (TextView) failedLayout.findViewById(R.id.failed_tip);
		Button failedButton = (Button) failedLayout.findViewById(R.id.failed_tip_button);
		failedButton.setVisibility(View.GONE);
		tipText.setVisibility(View.GONE);
		failedLayout.setVisibility(View.VISIBLE);
		boolean hasNetwork = Utils.isNetworkAvailable(this);
		if (hasNetwork) {
			resultText.setText(R.string.network_not_good);
			tipText.setText(R.string.click_button_refresh_later);
		} else {
			resultText.setText(R.string.network_not_connected);
			tipText.setText(R.string.click_button_setting_network);
		}
	}
	
	private void startLoading() {
		mLayoutLoading.setVisibility(View.VISIBLE);
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this, R.anim.load_animation);  
        mImageLoading.startAnimation(hyperspaceJumpAnimation);
	}
	
	private void stopLoading() {
		mLayoutLoading.setVisibility(View.GONE);
	}

}
