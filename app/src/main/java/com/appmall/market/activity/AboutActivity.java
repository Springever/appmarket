package com.appmall.market.activity;

import java.util.Locale;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.appmall.market.R;


public class AboutActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);
		
		initView();
	}

	private void initView() {
		Locale locale = Locale.getDefault();
		String versionname = getClientVersionName();
		String clientVersion = String.format(locale, getString(R.string.client_version), versionname);
		TextView versionText = (TextView) findViewById(R.id.large_logo_version);
		versionText.setText(clientVersion);

		findViewById(R.id.back_button).setOnClickListener(this);
	}

	private String getClientVersionName() {
		PackageManager pm =getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_button:
			finish();
			break;
		}
	}

}
