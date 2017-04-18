package com.cabrunzltd.cabrunz.driver;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.app.AppController;
import com.cabrunzltd.cabrunz.driver.base.ActionBarBaseActivitiy;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;

/**
 * @author Kishan H Dhamat
 * 
 */
public class MenuDescActivity extends ActionBarBaseActivitiy {
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sidemenu_discript);
		webView = (WebView) findViewById(R.id.wvDesc);
		webView.loadData(
				getIntent().getStringExtra(AndyConstants.Params.CONTENT),
				"text/html", "utf-8");
		setActionBarIcon(R.drawable.taxi);
		// getSupportActionBar().setTitle(
		// getIntent().getStringExtra(AndyConstants.Params.TITLE));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		 AppController.activityResumed();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();		
		 AppController.activityPaused();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnActionNotification:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;

		default:
			break;
		}
	}
}
