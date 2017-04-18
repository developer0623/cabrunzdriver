package com.cabrunzltd.cabrunz.driver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.cabrunzltd.cabrunz.driver.adapter.DrawerAdapter;
import com.cabrunzltd.cabrunz.driver.app.AppController;
import com.cabrunzltd.cabrunz.driver.base.ActionBarBaseActivitiy;
import com.cabrunzltd.cabrunz.driver.db.DBHelper;
import com.cabrunzltd.cabrunz.driver.fragment.ClientRequestFragment;
import com.cabrunzltd.cabrunz.driver.fragment.FeedbackFrament;
import com.cabrunzltd.cabrunz.driver.fragment.JobFragment;
import com.cabrunzltd.cabrunz.driver.model.ApplicationPages;
import com.cabrunzltd.cabrunz.driver.model.Bill;
import com.cabrunzltd.cabrunz.driver.model.RequestDetail;
import com.cabrunzltd.cabrunz.driver.model.User;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.parse.ParseContent;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;
import com.cabrunzltd.cabrunz.driver.widget.MyFontTextViewDrawer;

import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Kishan H Dhamat
 * 
 */
public class MapActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener, AsyncTaskCompleteListener {
	// private DrawerLayout drawerLayout;
	private DrawerAdapter adapter;
	private ListView drawerList;
	// private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private PreferenceHelper preferenceHelper;
	private ParseContent parseContent;
	private static final String TAG = "MapActivity";
	private ArrayList<ApplicationPages> arrayListApplicationPages;
	private boolean isDataRecieved = false, isRecieverRegistered = false,
			isNetDialogShowing = false, isGpsDialogShowing = false;
	private AlertDialog internetDialog, gpsAlertDialog;
	private LocationManager manager;
	private MenuDrawer mMenuDrawer;
	private DBHelper dbHelper;
	private AQuery aQuery;
	private ImageOptions imageOptions;
	private ImageView ivMenuProfile;
	private MyFontTextViewDrawer tvMenuName;
	private int is_approved = 0;
	private BroadcastReceiver mReceiver = null;
	private SharedPreferences pref;// =getSharedPreferences("approved",
									// MODE_PRIVATE);
	SharedPreferences.Editor editor;// =pref.edit();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
		pref = getSharedPreferences("approved", MODE_PRIVATE);
		editor = pref.edit();

		// Mint.initAndStartSession(MapActivity.this, "fdd1b971");
		// setContentView(R.layout.activity_map);
		preferenceHelper = new PreferenceHelper(this);
		
		///////////////////////////////mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer = MenuDrawer.attach(this);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int menusize = (int) (width * 0.80);
		mMenuDrawer.setMenuSize(menusize);
		mMenuDrawer.setContentView(R.layout.activity_map);
		mMenuDrawer.setMenuView(R.layout.menu_drawer);
		mMenuDrawer.setDropShadowEnabled(true);
		arrayListApplicationPages = new ArrayList<ApplicationPages>();
		parseContent = new ParseContent(this);
		mTitle = mDrawerTitle = getTitle();
		// drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		ivMenuProfile = (ImageView) findViewById(R.id.ivMenuProfile);
		tvMenuName = (MyFontTextViewDrawer) findViewById(R.id.tvMenuName);
		drawerList.setOnItemClickListener(this);
		adapter = new DrawerAdapter(this, arrayListApplicationPages);
		drawerList.setAdapter(adapter);

		// drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
		// GravityCompat.START);
		btnActionMenu.setVisibility(View.VISIBLE);
		btnActionMenu.setOnClickListener(this);
		tvTitle.setOnClickListener(this);
		btnNotification.setVisibility(View.GONE);
		setActionBarIcon(R.drawable.menu);
		IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        mReceiver = new ShutdownReceiver();
        registerReceiver(mReceiver, filter);
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);

		// mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
		// R.drawable.slide_btn, R.string.drawer_open,
		// R.string.drawer_close) {
		//
		// public void onDrawerClosed(View view) {
		// getSupportActionBar().setTitle(mTitle);
		// // supportInvalidateOptionsMenu(); // creates call to
		// // onPrepareOptionsMenu()
		// }
		//
		// public void onDrawerOpened(View drawerView) {
		// getSupportActionBar().setTitle(mDrawerTitle);
		// supportInvalidateOptionsMenu();
		// }
		// };
		// drawerLayout.setDrawerListener(mDrawerToggle);

		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		aQuery = new AQuery(this);
		imageOptions = new ImageOptions();
		imageOptions.memCache = true;
		imageOptions.fileCache = true;
		imageOptions.targetWidth = 200;
		imageOptions.fallback = R.drawable.user;

		dbHelper = new DBHelper(getApplicationContext());
		
//		Log.d("mahi", new PreferenceHelper(this).getCurrentFragment());
		User user = dbHelper.getUser();
		if (isExternalStorageWritable() == false) {
			Toast.makeText(getApplicationContext(),
					"No SD Card Please insert !", Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, MainActivity.class));
			this.finish();

		}
		try {
//			Log.d("xxx", "from map activity" + user);
			aQuery.id(ivMenuProfile).progress(R.id.pBar)
					.image(user.getPicture(), imageOptions);
			tvMenuName.setText(user.getFname() + " " + user.getLname());
			// if (savedInstanceState == null) {
			// selectItem(-1);
			// }

			is_approved = Integer.valueOf(new PreferenceHelper(this)
					.getApproved());
			editor.putInt("approved value", is_approved);
			editor.commit();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if (pref.getInt("approved value", 0) == 0) {
		// new AlertDialog.Builder(this)
		// .setTitle("Driver not approved")
		// .setMessage("Please contact your admin to approve you")
		// .setPositiveButton(android.R.string.ok,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// // continue with delete
		// }
		// }).setIcon(android.R.drawable.ic_dialog_alert)
		// .show();
		// }

	}

	private boolean isExternalStorageWritable() {
		// TODO Auto-generated method stub
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// if (mDrawerToggle.onOptionsItemSelected(item)) {
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	public void checkStatus() {
		if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
//			AppLog.Log(TAG, "onResume getreuest in progress");
			getRequestsInProgress();
		} else {
//			AppLog.Log(TAG, "onResume check request status");
			checkRequestStatus();
		}
	}

	private void getMenuItems() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.APPLICATION_PAGES);
		new HttpRequester(this, map,
				AndyConstants.ServiceCode.APPLICATION_PAGES, true, this);
	}
	
	public boolean isAppIsInBackground(Context context) {
		
		    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		    List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		    if (!tasks.isEmpty()) {
		      ComponentName topActivity = tasks.get(0).topActivity;
		      if (!topActivity.getPackageName().equals(context.getPackageName())) {
		        return true;
		      }
		    }

		    return false;
		    
		  }
	
	public class ShutdownReceiver extends BroadcastReceiver {

	    @Override
	    public void onReceive(Context context, Intent intent) {

	         if(Intent.ACTION_SHUTDOWN.equalsIgnoreCase(intent.getAction())) {
//	               Log.d("mahi","Boot Receiver - Shutdown event");
	               // database operation
	               changeState();
	         }
	    }
	}
    

	public BroadcastReceiver GpsChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			AppLog.Log(TAG, "On recieve GPS provider broadcast");
			final LocationManager manager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				// do something
				removeGpsDialog();
			} else {
				// do something else
				if (isGpsDialogShowing) {
					return;
				}
				ShowGpsDialog();
			}

		}
	};
	public BroadcastReceiver internetConnectionReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo activeWIFIInfo = connectivityManager
					.getNetworkInfo(connectivityManager.TYPE_WIFI);

			if (activeWIFIInfo.isConnected() || activeNetInfo.isConnected()) {
				removeInternetDialog();
			} else {
				if (isNetDialogShowing) {
					return;
				}
				showInternetDialog();
			}
		}
	};

	private void ShowGpsDialog() {
		AndyUtils.removeCustomProgressDialog();
		isGpsDialogShowing = true;
		AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(
				MapActivity.this);
		gpsBuilder.setCancelable(false);
		gpsBuilder
				.setTitle(getString(R.string.dialog_no_gps))
				.setMessage(getString(R.string.dialog_no_gps_messgae))
				.setPositiveButton(getString(R.string.dialog_enable_gps),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// continue with delete
								Intent intent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(intent);
								removeGpsDialog();
							}
						})

				.setNegativeButton(getString(R.string.dialog_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
								removeGpsDialog();
								finish();
							}
						});
		gpsAlertDialog = gpsBuilder.create();
		gpsAlertDialog.show();
	}

	private void removeGpsDialog() {
		if (gpsAlertDialog != null && gpsAlertDialog.isShowing()) {
			gpsAlertDialog.dismiss();
			isGpsDialogShowing = false;
			gpsAlertDialog = null;

		}
	}

	private void removeInternetDialog() {
		if (internetDialog != null && internetDialog.isShowing()) {
			internetDialog.dismiss();
			isNetDialogShowing = false;
			internetDialog = null;

		}
	}

	private void showInternetDialog() {
		AndyUtils.removeCustomProgressDialog();
		isNetDialogShowing = true;
		AlertDialog.Builder internetBuilder = new AlertDialog.Builder(
				MapActivity.this);
		internetBuilder.setCancelable(false);
		internetBuilder
				.setTitle(getString(R.string.dialog_no_internet))
				.setMessage(getString(R.string.dialog_no_inter_message))
				.setPositiveButton(getString(R.string.dialog_enable_3g),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// continue with delete
								Intent intent = new Intent(
										android.provider.Settings.ACTION_SETTINGS);
								startActivity(intent);
								removeInternetDialog();
							}
						})
				.setNeutralButton(getString(R.string.dialog_enable_wifi),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed Cancel button. Write
								// Logic Here
								startActivity(new Intent(
										Settings.ACTION_WIFI_SETTINGS));
								removeInternetDialog();
							}
						})
				.setNegativeButton(getString(R.string.dialog_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
								removeInternetDialog();
								finish();
							}
						});
		internetDialog = internetBuilder.create();
		internetDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		 AppController.activityResumed();
		 
		 
		
       //isAppIsInBackground(this);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			ShowGpsDialog();
		} else {
			removeGpsDialog();
		}
		registerReceiver(internetConnectionReciever, new IntentFilter(
				"android.net.conn.CONNECTIVITY_CHANGE"));
		registerReceiver(GpsChangeReceiver, new IntentFilter(
				LocationManager.PROVIDERS_CHANGED_ACTION));
		isRecieverRegistered = true;

		if (AndyUtils.isNetworkAvailable(this)
				&& manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			if (!isDataRecieved) {

				checkStatus();
				//startLocationUpdateService();

			}
//			Log.d("mahi", new PreferenceHelper(MapActivity.this).getActive());
			if(new PreferenceHelper(MapActivity.this).getActive().equals("1")){
			startLocationUpdateService();
		}
		}

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
		isAppIsInBackground(this);
		 AppController.activityPaused();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AndyUtils.removeCustomProgressDialog();
		// Mint.closeSession(this);
		if (isRecieverRegistered) {
			unregisterReceiver(internetConnectionReciever);
			unregisterReceiver(GpsChangeReceiver);
			unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// AndyUtils.showToast("Postion :" + arg2, this);
		// drawerLayout.closeDrawer(drawerList);
		mMenuDrawer.closeMenu();
		if (position == 0) {
			startActivity(new Intent(this, ProfileActivity.class));
		} else if (position == 1) {
			startActivity(new Intent(this, HistoryActivity.class));
		} else if (position == 2) {
			startActivity(new Intent(this, SettingActivity.class));
		} else if (position == (arrayListApplicationPages.size() - 1)) {
			// new AlertDialog.Builder(this)
			// .setTitle(getString(R.string.dialog_logout))
			// .setMessage(getString(R.string.dialog_logout_text))
			// .setPositiveButton(android.R.string.yes,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int which) {
			// // continue with delete
			// checkState();
			//
			// }
			// })
			// .setNegativeButton(android.R.string.no,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int which) {
			// // do nothing
			// dialog.cancel();
			// }
			// }).setIcon(android.R.drawable.ic_dialog_alert)
			// .show();

			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.logout);

			Button dialogButton = (Button) dialog.findViewById(R.id.log);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					checkState();
					
					preferenceHelper.putAvail_on("0");
				}
			});
			Button log = (Button) dialog.findViewById(R.id.no);
			// if button is clicked, close the custom dialog
			log.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();

				}
			});
			dialog.show();
		} else {
			Intent intent = new Intent(this, MenuDescActivity.class);
			intent.putExtra(AndyConstants.Params.TITLE,
					arrayListApplicationPages.get(position).getTitle());
			intent.putExtra(AndyConstants.Params.CONTENT,
					arrayListApplicationPages.get(position).getData());
			startActivity(intent);
		}
	}

	// @Override
	// public void setTitle(CharSequence title) {
	// mTitle = title;
	// getSupportActionBar().setTitle(mTitle);
	// }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnActionMenu:
			mMenuDrawer.toggleMenu();
			break;

		case R.id.tvTitle:
			mMenuDrawer.toggleMenu();
			break;
		default:
			break;
		}
	}

	public void getRequestsInProgress() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		/*AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_dialog_request),
				false);*/
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.REQUEST_IN_PROGRESS
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
		new HttpRequester(this, map,
				AndyConstants.ServiceCode.REQUEST_IN_PROGRESS, true, this);
	}

	public void checkRequestStatus() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		/*AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_dialog_request),
				false);*/
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_REQUEST_STATUS
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ preferenceHelper.getRequestId());
//		Log.e("mahi", "CHECK_REQUEST_STATUS =" + map);
		new HttpRequester(this, map,
				AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, true, this);
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		super.onTaskCompleted(response, serviceCode);

		switch (serviceCode) {
		case AndyConstants.ServiceCode.REQUEST_IN_PROGRESS:
			
//			AppLog.Log(TAG, "requestInProgress Response :" + response);
			if (!parseContent.isSuccess(response)) {
				if (parseContent.getErrorCode(response) == AndyConstants.REQUEST_ID_NOT_FOUND) {
					AndyUtils.removeCustomProgressDialog();
					preferenceHelper.clearRequestData();
					getMenuItems();
					addFragment(new ClientRequestFragment(), false,
							AndyConstants.CLIENT_REQUEST_TAG, true);
				} else if (parseContent.getErrorCode(response) == AndyConstants.INVALID_TOKEN) {
					if (preferenceHelper.getLoginBy().equalsIgnoreCase(
							AndyConstants.MANUAL))
						login();
					else
						loginSocial(preferenceHelper.getUserId(),
								preferenceHelper.getLoginBy());
				}
				return;
			}
			AndyUtils.removeCustomProgressDialog();
			int requestId = parseContent.parseRequestInProgress(response);

			if (requestId == AndyConstants.NO_REQUEST) {
				getMenuItems();
				addFragment(new ClientRequestFragment(), false,
						AndyConstants.CLIENT_REQUEST_TAG, true);
			} else {
				checkRequestStatus();
			}
			break;
		case AndyConstants.ServiceCode.CHECK_REQUEST_STATUS:
			

//			Log.d("mahi", "checkrequeststatus:" + response);
			if (response != null)
//			AppLog.Log(TAG, "checkrequeststatus Response :" + response);

			if (!parseContent.isSuccess(response)) {
				if (parseContent.getErrorCode(response) == AndyConstants.REQUEST_ID_NOT_FOUND) {
					preferenceHelper.clearRequestData();
					//AndyUtils.removeCustomProgressDialog();
					addFragment(new ClientRequestFragment(), false,
							AndyConstants.CLIENT_REQUEST_TAG, true);

				} else if (parseContent.getErrorCode(response) == AndyConstants.INVALID_TOKEN) {
					if (preferenceHelper.getLoginBy().equalsIgnoreCase(
							AndyConstants.MANUAL))
						login();
					else
						loginSocial(preferenceHelper.getUserId(),
								preferenceHelper.getLoginBy());
				}
				return;
			} 
			//AndyUtils.removeCustomProgressDialog();
			Bundle bundle = new Bundle();
			JobFragment jobFragment = new JobFragment();
			RequestDetail requestDetail = parseContent
					.parseRequestStatus(response);

			if (requestDetail == null) {
				return;
			}
			getMenuItems();
			switch (requestDetail.getJobStatus()) {
			case AndyConstants.IS_WALKER_STARTED:
				if (!currentFragment.equals(
						AndyConstants.JOB_FRGAMENT_TAG)) {
					bundle.putInt(AndyConstants.JOB_STATUS,
							AndyConstants.IS_WALKER_STARTED);
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					jobFragment.setArguments(bundle);
					addFragment(jobFragment, false,
							AndyConstants.JOB_FRGAMENT_TAG, true);
				}
				break;
			case AndyConstants.IS_WALKER_ARRIVED:
				if (!currentFragment.equals(
						AndyConstants.JOB_FRGAMENT_TAG)) {
					bundle.putInt(AndyConstants.JOB_STATUS,
							AndyConstants.IS_WALKER_ARRIVED);
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					jobFragment.setArguments(bundle);
					addFragment(jobFragment, false,
							AndyConstants.JOB_FRGAMENT_TAG, true);
				}
				break;
			case AndyConstants.IS_WALK_STARTED:
			if (!currentFragment.equals(
						AndyConstants.JOB_FRGAMENT_TAG)) {
					bundle.putInt(AndyConstants.JOB_STATUS,
							AndyConstants.IS_WALK_STARTED);
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					jobFragment.setArguments(bundle);
					addFragment(jobFragment, false,
							AndyConstants.JOB_FRGAMENT_TAG, true);
				}
				break;
			case AndyConstants.IS_WALK_COMPLETED:
				if (!currentFragment.equals(
						AndyConstants.JOB_FRGAMENT_TAG)) {
					bundle.putInt(AndyConstants.JOB_STATUS,
							AndyConstants.IS_WALK_COMPLETED);
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					jobFragment.setArguments(bundle);
					addFragment(jobFragment, false,
							AndyConstants.JOB_FRGAMENT_TAG, true);
				}

				break;
			case AndyConstants.IS_DOG_RATED:
				if (!currentFragment.equals(
						AndyConstants.FEEDBACK_FRAGMENT_TAG)) {
					FeedbackFrament feedbackFrament = new FeedbackFrament();
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					bundle.putString(AndyConstants.Params.TIME, 0 + " "
							+ getResources().getString(R.string.text_mins));
					bundle.putString(AndyConstants.Params.DISTANCE, 0 + " "
							+ getResources().getString(R.string.text_miles));
					Bill bill = parseContent
							.parsebillwhenwalkcomplete(response);
					bundle.putSerializable("bill", bill);

					feedbackFrament.setArguments(bundle);
					addFragment(feedbackFrament, false,
							AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
				}
				break;
			 case AndyConstants.IS_ASSIGNED:
				 bundle.putInt(AndyConstants.JOB_STATUS,
				 AndyConstants.IS_ASSIGNED);
				 jobFragment.setArguments(bundle);
				 addFragment(jobFragment, false, AndyConstants.JOB_FRGAMENT_TAG, true);
			 break;
			default:
				break;
			}

			break;
		case AndyConstants.ServiceCode.LOGIN:
			if (response != null)
			AndyUtils.removeCustomProgressDialog();
			if (parseContent.isSuccessWithId(response)) {
				try {
					JSONObject obj = new JSONObject(response);
					is_approved = obj.getInt("is_approved");

				} catch (JSONException e) {
					e.printStackTrace();
				}

				new PreferenceHelper(this).putApproved(String
						.valueOf(is_approved));
				checkStatus();
			}
			break;
		case AndyConstants.ServiceCode.APPLICATION_PAGES:
			if (response != null)

//			AppLog.Log("mahi", "Menuitems Response::" + response);
			arrayListApplicationPages = parseContent.parsePages(
					arrayListApplicationPages, response);
			ApplicationPages applicationPages = new ApplicationPages();
			applicationPages.setData("");
			applicationPages.setId(-4);
			applicationPages.setIcon("");
			applicationPages.setTitle(getString(R.string.text_logout));
			arrayListApplicationPages.add(applicationPages);
			adapter.notifyDataSetChanged();
			isDataRecieved = true;
			break;
		case AndyConstants.ServiceCode.TOGGLE_STATE:
			AndyUtils.removeCustomProgressDialog();
//			Log.d("mahi", "toogle off res"+response);
			if (!parseContent.isSuccess(response)) {
				Toast.makeText(this, "Could not logout.Please try again",
						Toast.LENGTH_LONG).show();
				return;
			} else {
				preferenceHelper.Logout();
				goToMainActivity();
			}
			break;
		case AndyConstants.ServiceCode.CHECK_STATE:
//			Log.e("mahi", "CHECK_STATE:" + response);
			if (parseContent.parseAvaibilty(response)) {
				changeState();
			} else {
				preferenceHelper.Logout();
				goToMainActivity();
			}
			break;
		default:
			break;
		}
	}

	private void login() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
		map.put(AndyConstants.Params.EMAIL, preferenceHelper.getEmail());
		map.put(AndyConstants.Params.PASSWORD, preferenceHelper.getPassword());
		map.put(AndyConstants.Params.DEVICE_TYPE,
				AndyConstants.DEVICE_TYPE_ANDROID);
		map.put(AndyConstants.Params.DEVICE_TOKEN,
				preferenceHelper.getDeviceToken());
		map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);
		new HttpRequester(this, map, AndyConstants.ServiceCode.LOGIN, this);

	}

	private void loginSocial(String id, String loginType) {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
		map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
		map.put(AndyConstants.Params.DEVICE_TYPE,
				AndyConstants.DEVICE_TYPE_ANDROID);
		map.put(AndyConstants.Params.DEVICE_TOKEN,
				preferenceHelper.getDeviceToken());
		map.put(AndyConstants.Params.LOGIN_BY, loginType);
		new HttpRequester(this, map, AndyConstants.ServiceCode.LOGIN, this);

	}

	private void changeState() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}

		/*AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_changing_avaibilty),
				false);*/

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.TOGGLE_STATE);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
//       Log.d("mahi", "toogle off"+map);
		new HttpRequester(this, map, AndyConstants.ServiceCode.TOGGLE_STATE,
				this);

	}

	private void checkState() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		/*AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_getting_avaibility),
				false);*/
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_STATE + AndyConstants.Params.ID
						+ "=" + preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
//		Log.e("", "CHECK_STATE:" + map);
		new HttpRequester(this, map, AndyConstants.ServiceCode.CHECK_STATE,
				true, this);
	}

}