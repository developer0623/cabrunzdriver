package com.cabrunzltd.cabrunz.driver;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

import com.cabrunzltd.cabrunz.driver.app.AppController;
import com.cabrunzltd.cabrunz.driver.base.ActionBarBaseActivitiy;
import com.cabrunzltd.cabrunz.driver.fragment.LoginFragment;
import com.cabrunzltd.cabrunz.driver.fragment.RegisterFragment;
import com.cabrunzltd.cabrunz.driver.gcm.GCMRegisterHendler;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;

/**
 * @author Kishan H Dhamat
 * 
 */
public class RegisterActivity extends ActionBarBaseActivitiy {
	public ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		actionBar = getSupportActionBar();
		
		// addFragment(new UberMainFragment(), false,
		// AndyConstants.MAIN_FRAGMENT_TAG);
		if (getIntent().getBooleanExtra("isSignin", false)) {
			clearBackStack();
			addFragment(new LoginFragment(), false,
					AndyConstants.LOGIN_FRAGMENT_TAG, true);
		} else {
			clearBackStack();
			addFragment(new RegisterFragment(), false,
					AndyConstants.REGISTER_FRAGMENT_TAG, true);
		}
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
		// TODO Auto-generated method stub
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
			break;
		case R.id.btnActionMenu:
			onBackPressed();
			break;
		default:
			break;
		}

	}

	public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
		if (mHandleMessageReceiver != null) {
			AndyUtils.showCustomProgressDialog(this, "", getResources()
					.getString(R.string.progress_loading), false);
			new GCMRegisterHendler(RegisterActivity.this,
					mHandleMessageReceiver);

		}
	}

	public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
		if (mHandleMessageReceiver != null) {

			if (mHandleMessageReceiver != null) {
				unregisterReceiver(mHandleMessageReceiver);
			}

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		Fragment signinFragment = getSupportFragmentManager()
				.findFragmentByTag(AndyConstants.LOGIN_FRAGMENT_TAG);
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(
				AndyConstants.REGISTER_FRAGMENT_TAG);
		if ((fragment != null && fragment.isVisible()) || (signinFragment != null && signinFragment.isVisible()) ) {

			goToMainActivity();
		}  else {
			super.onBackPressed();
		}

	}

}
