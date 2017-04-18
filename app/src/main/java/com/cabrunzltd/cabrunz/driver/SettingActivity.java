package com.cabrunzltd.cabrunz.driver;

import java.util.HashMap;

/////////////////////import org.jraf.android.backport.switchwidget.Switch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.app.AppController;
import com.cabrunzltd.cabrunz.driver.base.ActionBarBaseActivitiy;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.parse.ParseContent;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;

/**
 * @author Kishan H Dhamat
 * 
 */
public class SettingActivity extends ActionBarBaseActivitiy implements
		OnCheckedChangeListener, AsyncTaskCompleteListener {
	private PreferenceHelper preferenceHelper;
	private ParseContent parseContent;
	private ToggleButton switchSetting;
	public static boolean avai;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		preferenceHelper = new PreferenceHelper(this);
		parseContent = new ParseContent(this);
		switchSetting = (ToggleButton) findViewById(R.id.switchAvaibility);
		setActionBarTitle(getString(R.string.text_setting));
		setActionBarIcon(R.drawable.back);
		// getSupportActionBar().setTitle(
		// getResources().getString(R.string.text_setting));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);
		switchSetting.setOnClickListener(this);
		checkState();
	}

	private void checkState() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_getting_avaibility),
				false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_STATE + AndyConstants.Params.ID
						+ "=" + preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
		new HttpRequester(this, map, AndyConstants.ServiceCode.CHECK_STATE,
				true, this);
	}

	private void changeState() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}

		AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_changing_avaibilty),
				false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.TOGGLE_STATE);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());

		new HttpRequester(this, map, AndyConstants.ServiceCode.TOGGLE_STATE,
				this);

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
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.CHECK_STATE:
		case AndyConstants.ServiceCode.TOGGLE_STATE:
			if (!parseContent.isSuccess(response)) {
				return;
			}
			Log.e("mahi", "toggle state:" + response);
			if (parseContent.parseAvaibilty(response)) {
				switchSetting.setOnCheckedChangeListener(null);
				switchSetting.setChecked(true);
				avai = true;
				// switchSetting.setChecked(true);
			} else {
				switchSetting.setOnCheckedChangeListener(null);
				switchSetting.setChecked(false);
				avai = false;
				// switchSetting.setChecked(false);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AndyUtils.removeCustomProgressDialog();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnActionNotification:
			Intent a =new Intent(this,MapActivity.class);
			startActivity(a);
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;
		case R.id.switchAvaibility:
			changeState();

			break;
		case R.id.btnActionMenu:
			Intent i =new Intent(this,MapActivity.class);
			startActivity(i);
			break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		
	}
}
