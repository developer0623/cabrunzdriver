package com.cabrunzltd.cabrunz.driver.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.cabrunzltd.cabrunz.driver.MainActivity;
import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.locationupdate.LocationUpdateService;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.widget.MyFontTextViewTitle;

import java.util.Calendar;

/**
 * @author Kishan H Dhamat
 * 
 */
@SuppressLint("NewApi")
abstract public class ActionBarBaseActivitiy extends AppCompatActivity
		implements OnClickListener, AsyncTaskCompleteListener {

	protected ActionBar actionBar;
	private int mFragmentId = 0;
	private String mFragmentTag = null;
	public ImageButton btnNotification, btnActionMenu;
	public MyFontTextViewTitle tvTitle;
	public String currentFragment = "";
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		activity = this;
		// Custom Action Bar
		LayoutInflater inflater = (LayoutInflater) actionBar.getThemedContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View customActionBarView = inflater.inflate(R.layout.custom_action_bar,
				null);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		btnNotification = (ImageButton) customActionBarView
				.findViewById(R.id.btnActionNotification);
		btnNotification.setOnClickListener(this);

		tvTitle = (MyFontTextViewTitle) customActionBarView
				.findViewById(R.id.tvTitle);
		tvTitle.setOnClickListener(this);

		btnActionMenu = (ImageButton) customActionBarView
				.findViewById(R.id.btnActionMenu);
		btnActionMenu.setOnClickListener(this);

		try {
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setCustomView(customActionBarView);
			Toolbar parent =(Toolbar) customActionBarView.getParent();
			parent.setPadding(0,0,0,0);
			parent.setContentInsetsAbsolute(0,0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setFbTag(String tag) {
		mFragmentId = 0;
		mFragmentTag = tag;
	}

	public void startLocationUpdateService() {

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		Intent intent = new Intent(this, LocationUpdateService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pintent);
//		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
//				1000 * 30, pintent);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				1000 * 10, pintent);

	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		Fragment fragment = null;
//
//		if (mFragmentId > 0) {
//			fragment = getSupportFragmentManager()
//					.findFragmentById(mFragmentId);
//		} else if (mFragmentTag != null && !mFragmentTag.equalsIgnoreCase("")) {
//			fragment = getSupportFragmentManager().findFragmentByTag(
//					mFragmentTag);
//		}
//
//		if (fragment != null) {
//			fragment.onActivityResult(requestCode, resultCode, data);
//		}
//
//	}

	public void startActivityForResult(Intent intent, int requestCode,
			int fragmentId) {
		mFragmentId = fragmentId;
		mFragmentTag = null;
		super.startActivityForResult(intent, requestCode);
	}

	public void startActivityForResult(Intent intent, int requestCode,
			String fragmentTag) {
		mFragmentTag = fragmentTag;
		mFragmentId = 0;
		super.startActivityForResult(intent, requestCode);
	}

	public void setActionBarTitle(String title) {
		tvTitle.setText(title);
	}

	public void setActionBarIcon(int image) {
		btnActionMenu.setImageResource(image);
	}

	public void startActivityForResult(Intent intent, int requestCode,
			int fragmentId, Bundle options) {
		mFragmentId = fragmentId;
		mFragmentTag = null;
		super.startActivityForResult(intent, requestCode, options);
	}

	public void startActivityForResult(Intent intent, int requestCode,
			String fragmentTag, Bundle options) {
		mFragmentTag = fragmentTag;
		mFragmentId = 0;
		super.startActivityForResult(intent, requestCode, options);
	}

	public void startIntentSenderForResult(Intent intent, int requestCode,
			String fragmentTag, Bundle options) {
		mFragmentTag = fragmentTag;
		mFragmentId = 0;
		super.startActivityForResult(intent, requestCode, options);
	}

	@Override
	@Deprecated
	public void startIntentSenderForResult(IntentSender intent,
			int requestCode, Intent fillInIntent, int flagsMask,
			int flagsValues, int extraFlags) throws SendIntentException {
		super.startIntentSenderForResult(intent, requestCode, fillInIntent,
				flagsMask, flagsValues, extraFlags);
	}

	public void startIntentSenderForResult(IntentSender intent,
			int requestCode, Intent fillInIntent, int flagsMask,
			int flagsValues, int extraFlags, String fragmentTag)
			throws SendIntentException {
		mFragmentTag = fragmentTag;
		mFragmentId = 0;
		super.startIntentSenderForResult(intent, requestCode, fillInIntent,
				flagsMask, flagsValues, extraFlags);
	}

	@Override
	@Deprecated
	public void startIntentSenderForResult(IntentSender intent,
			int requestCode, Intent fillInIntent, int flagsMask,
			int flagsValues, int extraFlags, Bundle options)
			throws SendIntentException {
		super.startIntentSenderForResult(intent, requestCode, fillInIntent,
				flagsMask, flagsValues, extraFlags, options);
	}

	public void startIntentSenderForResult(IntentSender intent,
			int requestCode, Intent fillInIntent, int flagsMask,
			int flagsValues, int extraFlags, Bundle options, String fragmentTag)
			throws SendIntentException {
		mFragmentTag = fragmentTag;
		mFragmentId = 0;
		super.startIntentSenderForResult(intent, requestCode, fillInIntent,
				flagsMask, flagsValues, extraFlags, options);
	}

	@Override
	@Deprecated
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	@Deprecated
	public void startActivityForResult(Intent intent, int requestCode,
			Bundle options) {
		super.startActivityForResult(intent, requestCode, options);
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {

	}

	public void addFragment(Fragment fragment, boolean addToBackStack,
			String tag, boolean isAnimate) {
		try{
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		if (isAnimate) {
			ft.setCustomAnimations(R.anim.slide_in_right,
					R.anim.slide_out_left, R.anim.slide_in_left,
					R.anim.slide_out_right);
		}

		if (addToBackStack) {
			ft.addToBackStack(tag);
		}
		ft.replace(R.id.content_frame, fragment, tag);
		ft.commitAllowingStateLoss();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

	}

	public void removeAllFragment(Fragment replaceFragment,
			boolean addToBackStack, String tag) {
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		manager.popBackStackImmediate(null,
				FragmentManager.POP_BACK_STACK_INCLUSIVE);

		if (addToBackStack) {
			ft.addToBackStack(tag);
		}
		ft.replace(R.id.content_frame, replaceFragment);
		ft.commitAllowingStateLoss();

	}

	public void clearBackStack() {
		FragmentManager manager = getSupportFragmentManager();
		if (manager.getBackStackEntryCount() > 0) {
			FragmentManager.BackStackEntry first = manager
					.getBackStackEntryAt(0);
			manager.popBackStack(first.getId(),
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	protected void goToMainActivity() {
		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);

//		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		finish();
	}

}
