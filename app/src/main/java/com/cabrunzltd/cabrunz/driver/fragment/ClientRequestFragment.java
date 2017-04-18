package com.cabrunzltd.cabrunz.driver.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidquery.AQuery;
import com.cabrunzltd.cabrunz.driver.GCMIntentService;
import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.SettingActivity;
import com.cabrunzltd.cabrunz.driver.app.AppController;
import com.cabrunzltd.cabrunz.driver.base.BaseMapFragment;
import com.cabrunzltd.cabrunz.driver.locationupdate.LocationHelper;
import com.cabrunzltd.cabrunz.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.cabrunzltd.cabrunz.driver.model.RequestDetail;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.widget.MyFontButton;
import com.cabrunzltd.cabrunz.driver.widget.MyFontTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
//import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
//import com.google.android.gms.internal.am;
//import com.google.android.gms.location.LocationClient;

/**
 * @author Kishan H Dhamat
 *
 */

public class ClientRequestFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener, OnLocationReceived, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {
	private GoogleMap mMap;
	private GoogleApiClient client;
	private LocationRequest mLocationRequest;
	private final String TAG = "ClientRequestFragment";
	private LinearLayout llAcceptReject, settings;
	private View llUserDetailView;
	// private ProgressBar pbTimeLeft;
	private MyFontButton btnClientAccept, btnClientReject,
			btnClientReqRemainTime;
	// private RelativeLayout rlTimeLeft;
	private boolean isContinueRequest, isAccepted = false;
	private Timer timer;
	private MyCountDown ringtone;
	private SeekbarTimer seekbarTimer;
	private RequestDetail requestDetail;
	private Marker markerDriverLocation, markerClientLocation,
			markerClient_d_location;
	//	private LocationClient locationClient, locationdriver;
	private Location location;
	private LocationHelper locationHelper;
	private MyFontTextView tvClientName, tvAddress, d_address, s_address;// ,
	// tvClientPhoneNumber;
	private RatingBar tvClientRating;
	private ImageView ivClientProfilePicture;
	private AQuery aQuery;
	private newRequestReciever requestReciever;
	private boolean selector = false;
	private ToggleButton switchAvaibility;
	AlertDialog.Builder gpsBuilder;
	WakeLock wakeLock;
	private Dialog d;
	MediaPlayer ring;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View clientRequestView = null;
		try {
			clientRequestView = inflater.inflate(
					R.layout.fragment_client_requests, container, false);

			try {
				MapsInitializer.initialize(getActivity());
			} catch (Exception e) {
			}

			llAcceptReject = (LinearLayout) clientRequestView
					.findViewById(R.id.llAcceptReject);
			settings = (LinearLayout) clientRequestView
					.findViewById(R.id.settings);

			llUserDetailView = (View) clientRequestView
					.findViewById(R.id.clientDetailView);
			btnClientAccept = (MyFontButton) clientRequestView
					.findViewById(R.id.btnClientAccept);
			btnClientReject = (MyFontButton) clientRequestView
					.findViewById(R.id.btnClientReject);
			switchAvaibility = (ToggleButton) clientRequestView
					.findViewById(R.id.switchAvaibility);
			switchAvaibility.setOnClickListener(this);
			// pbTimeLeft = (ProgressBar) clientRequestView
			// .findViewById(R.id.pbClientReqTime);
			// rlTimeLeft = (RelativeLayout) clientRequestView
			// .findViewById(R.id.rlClientReqTimeLeft);
			btnClientReqRemainTime = (MyFontButton) clientRequestView
					.findViewById(R.id.btnClientReqRemainTime);
			tvClientName = (MyFontTextView) clientRequestView
					.findViewById(R.id.tvClientName);
			tvAddress = (MyFontTextView) clientRequestView
					.findViewById(R.id.address);
			d_address = (MyFontTextView) clientRequestView
					.findViewById(R.id.d_address);
			s_address = (MyFontTextView) clientRequestView
					.findViewById(R.id.s_address);

			tvClientRating = (RatingBar) clientRequestView
					.findViewById(R.id.tvClientRating);

			ivClientProfilePicture = (ImageView) clientRequestView
					.findViewById(R.id.ivClientImage);

			btnClientAccept.setOnClickListener(this);
			btnClientReject.setOnClickListener(this);
			settings.setVisibility(View.GONE);

			clientRequestView.findViewById(R.id.btnMyLocation)
					.setOnClickListener(this);
			checkState();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return clientRequestView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aQuery = new AQuery(mapActivity);

//		setUpMap();
		setUpMapIfNeeded();
		client = new GoogleApiClient.Builder(getActivity())
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

//		locationHelper = new LocationHelper(getActivity());
//		locationHelper.setLocationReceivedLister(this);
//		locationHelper.onStart();


	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		client.connect();
	}


	private void checkState() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		/*AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_getting_avaibility), false);*/
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_STATE + AndyConstants.Params.ID
						+ "=" + preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.CHECK_STATE, true, this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PowerManager powerManager = (PowerManager) mapActivity
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");
		wakeLock.acquire();
		IntentFilter filter = new IntentFilter(AndyConstants.NEW_REQUEST);
		requestReciever = new newRequestReciever();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				requestReciever, filter);
	}

	private class MyCountDown extends CountDownTimer {
		long duration, interval;

		public MyCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
			start();
			ring = MediaPlayer.create(getActivity(),
					R.raw.car);
			ring.start();
			ring.setLooping(true);
		}

		@Override
		public void onFinish() {
			ring.stop();
		}

		@Override
		public void onTick(long duration) {
			// could set text for a timer here
		}
	}

//	private void addMarker() {
//		if (mMap == null) {
//			setUpMap();
//			return;
//		}
//		locationClient = new LocationClient(mapActivity,
//				new ConnectionCallbacks() {
//
//					@Override
//					public void onDisconnected() {
//
//					}
//
//					@Override
//					public void onConnected(Bundle arg0) {
//						location = locationClient.getLastLocation();
//						if (location != null) {
//							if (mMap != null) {
//								if (markerDriverLocation == null) {
//									markerDriverLocation = mMap
//											.addMarker(new MarkerOptions()
//													.position(
//															new LatLng(
//																	location.getLatitude(),
//																	location.getLongitude()))
//													.icon(BitmapDescriptorFactory
//															.fromResource(R.drawable.pin_driver))
//													.title(getResources()
//															.getString(
//																	R.string.my_location)));
//									mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//											new LatLng(location.getLatitude(),
//													location.getLongitude()),
//											12));
//									preferenceHelper
//											.putDrive_Location(new LatLng(
//													location.getLatitude(),
//													location.getLongitude()));
//								} else {
//									markerDriverLocation
//											.setPosition(new LatLng(location
//													.getLatitude(), location
//													.getLongitude()));
//									preferenceHelper
//											.putDrive_Location(new LatLng(
//													location.getLatitude(),
//													location.getLongitude()));
//								}
//							}
//						} else {
//							showLocationOffDialog();
//						}
//					}
//				}, new OnConnectionFailedListener() {
//
//					@Override
//					public void onConnectionFailed(ConnectionResult arg0) {
//
//					}
//				});
//		locationClient.connect();
//	}

	public void showLocationOffDialog() {

		gpsBuilder = new AlertDialog.Builder(mapActivity);
		gpsBuilder.setCancelable(false);
		gpsBuilder
				.setTitle(getString(R.string.dialog_no_location_service_title))
				.setMessage(getString(R.string.dialog_no_location_service))
				.setPositiveButton(
						getString(R.string.dialog_enable_location_service),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								// continue with delete
								dialog.dismiss();
								Intent viewIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(viewIntent);

							}
						})

				.setNegativeButton(getString(R.string.dialog_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								// do nothing
								dialog.dismiss();
								mapActivity.finish();
							}
						});
		gpsBuilder.create();
		gpsBuilder.show();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.clientReqMap);
			mapFrag.getMapAsync(this);
		}


	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		setUpMap();

	}


	private void setUpMap() {

		if (ActivityCompat.checkSelfPermission(mapActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(mapActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			final int REQUEST_LOCATION = 2;

			if (ActivityCompat.shouldShowRequestPermissionRationale(mapActivity,
					Manifest.permission.ACCESS_FINE_LOCATION)) {
				// Display UI and wait for user interaction
			} else {
				ActivityCompat.requestPermissions(
						mapActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						99);
			}


		}
		mMap.setMyLocationEnabled(true);
//		map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.setTrafficEnabled(true);
		mMap.setIndoorEnabled(true);
		mMap.setBuildingsEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setZoomControlsEnabled(true);

		mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

			// Use default InfoWindow frame

			@Override
			public View getInfoWindow(Marker marker) {
				View v = mapActivity.getLayoutInflater().inflate(
						R.layout.info_window_layout, null);
				TextView title = (TextView) v
						.findViewById(R.id.markerBubblePickMeUp);
				TextView content = (TextView) v
						.findViewById(R.id.infoaddress);
				title.setText(marker.getTitle());

				getAddressFromLocation(marker.getPosition(), content);

				// ((TextView) v).setText(marker.getTitle());
				return v;
			}

			// Defines the contents of the InfoWindow

			@Override
			public View getInfoContents(Marker marker) {

				// Getting view from the layout file info_window_layout View

				// Getting reference to the TextView to set title TextView

				// Returning the view containing InfoWindow contents return
				return null;

			}

		});

		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.showInfoWindow();
				return true;
			}
		});


	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
	}

	@Override
	public void onConnected(Bundle arg0) {

		if (ActivityCompat.checkSelfPermission(mapActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}

		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(500);
		mLocationRequest.setFastestInterval(500);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		//mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
		//mLocationRequest.setSmallestDisplacement(0.1F);

		LocationServices.FusedLocationApi.requestLocationUpdates(client, mLocationRequest, this);
		location = LocationServices.FusedLocationApi.getLastLocation(client);
		if (location != null) {
			if (mMap != null) {
				if (markerDriverLocation == null) {
					markerDriverLocation = mMap
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(
													location.getLatitude(),
													location.getLongitude()))
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.pin_driver))
									.title(getResources()
											.getString(
													R.string.my_location)));
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(location.getLatitude(),
									location.getLongitude()),
							12));
					preferenceHelper
							.putDrive_Location(new LatLng(
									location.getLatitude(),
									location.getLongitude()));
				} else {
					markerDriverLocation
							.setPosition(new LatLng(location
									.getLatitude(), location
									.getLongitude()));
					preferenceHelper
							.putDrive_Location(new LatLng(
									location.getLatitude(),
									location.getLongitude()));
				}
			}
		} else {
			showLocationOffDialog();
		}

	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	@Override
	public void onLocationChanged(Location loc) {
		markerDriverLocation
				.setPosition(new LatLng(loc
						.getLatitude(), loc
						.getLongitude()));
		preferenceHelper
				.putDrive_Location(new LatLng(
						loc.getLatitude(),
						loc.getLongitude()));

	}


//	private void setUpMap() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
//		if (mMap == null) {
//			mMap = ((SupportMapFragment) getActivity()
//					.getSupportFragmentManager().findFragmentById(
//							R.id.clientReqMap)).getMap();
//			mMap.getUiSettings().setZoomControlsEnabled(false);
//			mMap.setMyLocationEnabled(true);
//			mMap.getUiSettings().setMyLocationButtonEnabled(false);
//
//			mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
//
//				// Use default InfoWindow frame
//
//				@Override
//				public View getInfoWindow(Marker marker) {
//					View v = mapActivity.getLayoutInflater().inflate(
//							R.layout.info_window_layout, null);
//					TextView title = (TextView) v
//							.findViewById(R.id.markerBubblePickMeUp);
//					TextView content = (TextView) v
//							.findViewById(R.id.infoaddress);
//					title.setText(marker.getTitle());
//
//					getAddressFromLocation(marker.getPosition(), content);
//
//					// ((TextView) v).setText(marker.getTitle());
//					return v;
//				}
//
//				// Defines the contents of the InfoWindow
//
//				@Override
//				public View getInfoContents(Marker marker) {
//
//					// Getting view from the layout file info_window_layout View
//
//					// Getting reference to the TextView to set title TextView
//
//					// Returning the view containing InfoWindow contents return
//					return null;
//
//				}
//
//			});
//
//			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
//				@Override
//				public boolean onMarkerClick(Marker marker) {
//					marker.showInfoWindow();
//					return true;
//				}
//			});
//			try {
//				addMarker();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switchAvaibility:
			changeState();
			if (SettingActivity.avai) {
				switchAvaibility.setChecked(true);
			} else {
				switchAvaibility.setChecked(false);
			}
			break;
		case R.id.btnClientAccept:
			isAccepted = true;
			cancelSeekbarTimer();
			respondRequest(1);
			break;
		case R.id.btnClientReject:
			isAccepted = false;
			cancelSeekbarTimer();
			respondRequest(0);
			selector = false;
			break;
		case R.id.btnMyLocation:

			/*
			 * Location loc = mMap.getMyLocation(); if (loc != null) { LatLng
			 * latLang = new LatLng(loc.getLatitude(), loc.getLongitude());
			 * mMap.animateCamera(CameraUpdateFactory.newLatLng(latLang)); }
			 */
			if (markerDriverLocation.getPosition() != null) {

				try {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(location.getLatitude(), location
									.getLongitude()), 18));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				showLocationOffDialog();
			}

			break;
		default:
			break;
		}
	}

	private void changeState() {
		// TODO Auto-generated method stub
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_changing_avaibilty), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.TOGGLE_STATE);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());

		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.TOGGLE_STATE, this);

	}

	@Override
	public void onResume() {
		super.onResume();
		// checkState();
//		Log.d("mahi", "onresume");
		AppController.activityResumed();

		mapActivity.currentFragment = AndyConstants.CLIENT_REQUEST_TAG;
		if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
			startCheckingUpcomingRequests();
		}
		mapActivity.setActionBarTitle(getString(R.string.app_name));
		// settings.setVisibility(View.GONE);

	}

	@Override
	public void onPause() {
		super.onPause();

		AppController.activityPaused();
		if (mapActivity.currentFragment
				.equals(AndyConstants.CLIENT_REQUEST_TAG)) {
			showavailability();
//			Log.v("mahi", "opened popup");
		}
		// preferenceHelper.putIs_Visible("1");

		/*
		 * if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
		 * stopCheckingUpcomingRequests(); }
		 */
	}

	/*
	 * @Override public void onSaveInstanceState(Bundle outState) { // No call
	 * for super(). Bug on API Level > 11.
	 * 
	 * }
	 */

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopCheckingUpcomingRequests();
		cancelSeekbarTimer();
		d.dismiss();
		// seekbarTimer.cancel();
		AndyUtils.removeCustomProgressDialog();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				requestReciever);
//		Log.d("mahi", "ondestroy");

	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {

		switch (serviceCode) {
		case AndyConstants.ServiceCode.CHECK_STATE:
			AndyUtils.removeCustomProgressDialog();
			if (response != null)
				if (!parseContent.isSuccess(response)) {
					return;
				}
			if (parseContent.parseAvaibilty(response)) {

			} else {
				if (preferenceHelper.getAvail_on().equals("0")) {
					showavailability();
				}

			}

		case AndyConstants.ServiceCode.TOGGLE_STATE:

//			Log.d("mahi", "toggle resposne" + response);

			if (response != null)
				if (!parseContent.isSuccess(response)) {
					return;
				}

			if (parseContent.parseAvaibilty(response)) {
//				Log.d("mahi", "response" + response);
				AndyUtils.removeCustomProgressDialog();
				switchAvaibility.setChecked(true);
				SettingActivity.avai = true;
			} else {
				AndyUtils.removeCustomProgressDialog();
				switchAvaibility.setChecked(false);
				SettingActivity.avai = false;
			}
			break;

		case AndyConstants.ServiceCode.GET_ALL_REQUEST:
			// AndyUtils.removeCustomProgressDialog();
//			AppLog.Log(TAG, "getAllRequests Response :" + response);
			if (response != null)
				if (!parseContent.isSuccess(response)) {
					return;
				}

			requestDetail = parseContent.parseAllRequests(response);

			if (requestDetail != null && seekbarTimer == null) {

				if (selector == false) {
					selector = true;
					ringtone = new MyCountDown(30000, 1000);
//					GCMIntentService.generateNotification(getActivity(),
//							"New Request");
					stopCheckingUpcomingRequests();
					// startTimerForRespondRequest(requestDetail.getTimeLeft());
					setComponentVisible();
					// pbTimeLeft.setMax(requestDetail.getTimeLeft());

					preferenceHelper.putOwner_ID(requestDetail.getClientId());
					btnClientReqRemainTime.setText(""
							+ requestDetail.getTimeLeft());
					// pbTimeLeft.setProgress(requestDetail.getTimeLeft());
					tvClientName.setText(requestDetail.getClientName());
					d_address.setText(requestDetail.getD_address());
					s_address.setText(requestDetail.getS_address());
					tvAddress.setText("" + requestDetail.getAddress());
					if (requestDetail.getClientRating() != 0) {
						tvClientRating.setRating(requestDetail
								.getClientRating());
//						Log.i("Rating", "" + requestDetail.getClientRating());
					}

					if (requestDetail.getClientProfile() != null)
						if (!requestDetail.getClientProfile().equals(""))
							aQuery.id(ivClientProfilePicture)
									.progress(R.id.pBar)
									.image(requestDetail.getClientProfile());

					markerClientLocation = null;
					markerClientLocation = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(Double.parseDouble(requestDetail
											.getClientLatitude()), Double
											.parseDouble(requestDetail
													.getClientLongitude())))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin_client))
							.title(mapActivity.getResources().getString(
									R.string.client_location)));
					preferenceHelper.putS_Location(markerClientLocation.getPosition());

					markerClient_d_location = null;
					if (requestDetail.getClient_d_latitude() != null
							&& requestDetail.getClient_d_longitude() != null) {
						markerClient_d_location = mMap
								.addMarker(new MarkerOptions()
										.position(
												new LatLng(
														Double.parseDouble(requestDetail
																.getClient_d_latitude()),
														Double.parseDouble(requestDetail
																.getClient_d_longitude())))
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.img_destination))
										.title(mapActivity
												.getResources()
												.getString(
														R.string.client_d_location)));
						preferenceHelper.putD_Location(markerClient_d_location.getPosition());
					}

					Log.e("MSG", "des lat :" + requestDetail.getClient_d_latitude());
					Log.e("MSG", "des long :" + requestDetail.getClient_d_longitude());
					Log.e("MSG", "des helper lat :" + preferenceHelper.getD_Location().latitude);
					Log.e("MSG", "des helper lng :" + preferenceHelper.getD_Location().longitude);



					seekbarTimer = new SeekbarTimer(
							requestDetail.getTimeLeft() * 1000, 1000);
					seekbarTimer.start();
				}
			}

			break;

		case AndyConstants.ServiceCode.RESPOND_REQUEST:
//			AppLog.Log(TAG, "respond Request Response :" + response);
//			Log.e("MSG", "respond request" + response);
			removeNotification();
			AndyUtils.removeCustomProgressDialog();

			if (response != null)
				if (parseContent.isSuccess(response)) {

					if (requestDetail != null)
						if (isAccepted) {
							preferenceHelper.putRequestId(requestDetail
									.getRequestId());
							cancelSeekbarTimer();
							JobFragment jobFragment = new JobFragment();
							Bundle bundle = new Bundle();
							bundle.putInt(AndyConstants.JOB_STATUS,
									AndyConstants.IS_WALKER_STARTED);
							bundle.putSerializable(
									AndyConstants.REQUEST_DETAIL, requestDetail);
							jobFragment.setArguments(bundle);
							preferenceHelper
									.putDistance(AndyConstants.NO_DISTANCE);
							preferenceHelper
									.putRequestTime(AndyConstants.NO_TIME);
							mapActivity.addFragment(jobFragment, false,
									AndyConstants.JOB_FRGAMENT_TAG, true);

						} else {
							cancelSeekbarTimer();
							setComponentInvisible();
							if (markerClientLocation != null
									&& markerClientLocation.isVisible()) {
								markerClientLocation.remove();
							}
							if (markerClient_d_location != null
									&& markerClient_d_location.isVisible()) {
								markerClient_d_location.remove();
							}
							preferenceHelper
									.putRequestId(AndyConstants.NO_REQUEST);
							startCheckingUpcomingRequests();
						}
				}

			// else {
			// AndyUtils.showToast(
			// mapActivity.getResources().getString(
			// R.string.toast_unable_respond_request),
			// mapActivity);
			// }
			break;

		default:
			break;

		}
	}

	private void showavailability() {
		// TODO Auto-generated method stub

		d = new Dialog(getActivity());
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.availablity_popup);
		d.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		d.setCancelable(true);
		d.show();

		Button ok_btn = (Button) d.findViewById(R.id.btn_yes);

		ok_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (!SettingActivity.avai) {
					changeState();
					d.dismiss();
				} else {
					d.dismiss();
				}

			}
		});

		Button close_btn = (Button) d.findViewById(R.id.btn_no);
		close_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				AlertDialog.Builder builder1 = new AlertDialog.Builder(
						mapActivity);
				builder1.setTitle(null);
				builder1.setMessage("You will be made not available for any trips until you change it!");
				builder1.setCancelable(true);
				builder1.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								if (SettingActivity.avai) {
									changeState();
								}
							}
						});

				AlertDialog alert11 = builder1.create();
				alert11.show();
				d.dismiss();

			}
		});

	}

	private class SeekbarTimer extends CountDownTimer {

		public SeekbarTimer(long startTime, long interval) {
			super(startTime, interval);
			// pbTimeLeft.setProgressDrawable(getResources().getDrawable(
			// R.drawable.customprogress));
		}

		@Override
		public void onFinish() {
			if (seekbarTimer == null) {
				return;
			}
			respondRequest(2);
			AndyUtils.showToast(
					mapActivity.getResources().getString(
							R.string.toast_time_over), mapActivity);
			setComponentInvisible();
			preferenceHelper.clearRequestData();
			if (markerClientLocation != null
					&& markerClientLocation.isVisible()) {
				markerClientLocation.remove();
			}
			if (markerClient_d_location != null
					&& markerClient_d_location.isVisible()) {
				markerClient_d_location.remove();
			}
			removeNotification();
			startCheckingUpcomingRequests();
			this.cancel();
			seekbarTimer = null;
			selector = false;

		}

		@Override
		public void onTick(long millisUntilFinished) {
			int time = (int) (millisUntilFinished / 1000);
			if (!isVisible()) {
				return;
			}
			btnClientReqRemainTime.setText("" + time);
			/*try {

				final MediaPlayer r = MediaPlayer.create(getActivity(),
						R.raw.beep);
				if (time <= (Integer.valueOf(requestDetail.getTimeLeft()) - 5)
						&& time >= 0) {
					r.start();

				}
				if (time == 0)
					r.stop();

				r.setOnCompletionListener(new OnCompletionListener() {

					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						mp.release();

					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}*/

			// pbTimeLeft.setProgress(time);
			// if (time <= 5) {
			// pbTimeLeft.setProgressDrawable(getResources().getDrawable(
			// R.drawable.customprogressred));
			// }

		}
	}

	// if status = 1 then accept if 0 then reject
	private void respondRequest(int status) {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		try {
			AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
					.getString(R.string.progress_respond_request), false);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(AndyConstants.URL,
					AndyConstants.ServiceType.RESPOND_REQUESTS);
			map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
			map.put(AndyConstants.Params.REQUEST_ID,
					String.valueOf(requestDetail.getRequestId()));
			map.put(AndyConstants.Params.TOKEN,
					preferenceHelper.getSessionToken());
			map.put(AndyConstants.Params.ACCEPTED, String.valueOf(status));
			new HttpRequester(mapActivity, map,
					AndyConstants.ServiceCode.RESPOND_REQUEST, this);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(mapActivity, "Try again", Toast.LENGTH_SHORT).show();

		}
	}

	public void checkRequestStatus() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_dialog_request), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_REQUEST_STATUS
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ preferenceHelper.getRequestId());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, true, this);
	}

	public void getAllRequests() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			return;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.GET_ALL_REQUESTS
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());

		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.GET_ALL_REQUEST, true, this);
	}

	private class TimerRequestStatus extends TimerTask {
		@Override
		public void run() {
			if (isContinueRequest && preferenceHelper.getActive().equals("1")) {
				// isContinueRequest = false;
				getAllRequests();
			}
		}

	}

	private void startCheckingUpcomingRequests() {
//		AppLog.Log(TAG, "start checking upcomingRequests");
		isContinueRequest = true;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerRequestStatus(),
				AndyConstants.DELAY, AndyConstants.TIME_SCHEDULE);
	}

	private void stopCheckingUpcomingRequests() {
//		AppLog.Log(TAG, "stop checking upcomingRequests");
		isContinueRequest = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void removeNotification() {
		try {
			NotificationManager manager = (NotificationManager) mapActivity
					.getSystemService(mapActivity.NOTIFICATION_SERVICE);
			manager.cancel(AndyConstants.NOTIFICATION_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationReceived(LatLng latlong) {
		if (latlong != null) {
			if (mMap != null) {
				if (markerDriverLocation == null) {
					markerDriverLocation = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(latlong.latitude,
											latlong.longitude))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.mylocation))
							.title(mapActivity.getResources().getString(
									R.string.my_location)));
					mMap.animateCamera(CameraUpdateFactory
							.newLatLngZoom(new LatLng(latlong.latitude,
									latlong.longitude), 16));
				} else {
					markerDriverLocation.setPosition(new LatLng(
							latlong.latitude, latlong.longitude));
				}
			}
		} else {
//			location = locationClient.getLastLocation();
			if (markerDriverLocation == null) {
				markerDriverLocation = mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(location.getLatitude(), location
										.getLongitude()))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pin_driver))
						.title(getResources().getString(R.string.my_location)));
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(location.getLatitude(), location
								.getLongitude()), 12));
				preferenceHelper.putDrive_Location(new LatLng(location
						.getLatitude(), location.getLongitude()));
			} else {
				markerDriverLocation.setPosition(new LatLng(location
						.getLatitude(), location.getLongitude()));
				preferenceHelper.putDrive_Location(new LatLng(location
						.getLatitude(), location.getLongitude()));
			}
		}
	}

	public void setComponentVisible() {
		llAcceptReject.setVisibility(View.VISIBLE);
		btnClientReqRemainTime.setVisibility(View.VISIBLE);
		// rlTimeLeft.setVisibility(View.VISIBLE);
		llUserDetailView.setVisibility(View.VISIBLE);
	}

	public void setComponentInvisible() {
		llAcceptReject.setVisibility(View.GONE);
		btnClientReqRemainTime.setVisibility(View.GONE);
		// rlTimeLeft.setVisibility(View.GONE);
		llUserDetailView.setVisibility(View.GONE);
	}

	public void cancelSeekbarTimer() {
		if (seekbarTimer != null) {
			seekbarTimer.cancel();
			seekbarTimer = null;
			ring.stop();
		}
	}

	public void onDestroyView() {
		wakeLock.release();
//		Log.d("mahi", "ondestroy views");
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.clientReqMap);
		if (f != null) {
			try {
				getFragmentManager().beginTransaction().remove(f).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mMap = null;
		cancelSeekbarTimer();
		super.onDestroyView();
	}

	/* added by amal */
	private String strAddress = null;

	private void getAddressFromLocation(final LatLng latlng,
			final TextView content) {

		Geocoder gCoder = new Geocoder(getActivity());
//		Log.d("hey", String.valueOf(strAddress));
		try {
			final List<Address> list = gCoder.getFromLocation(latlng.latitude,
					latlng.longitude, 1);
			if (list != null && list.size() > 0) {
				Address address = list.get(0);
				StringBuilder sb = new StringBuilder();
				if (address.getAddressLine(0) != null) {

					sb.append(address.getAddressLine(0)).append(", ");
				}
				sb.append(address.getLocality()).append(", ");
				// sb.append(address.getPostalCode()).append(",");
				sb.append(address.getCountryName());
				strAddress = sb.toString();

				strAddress = strAddress.replace(",null", "");
				strAddress = strAddress.replace("null", "");
				strAddress = strAddress.replace("Unnamed", "");
				if (!TextUtils.isEmpty(strAddress)) {

					content.setText(strAddress);

				}
			}
//			Log.d("hey", strAddress);

		} catch (IOException exc) {
			exc.printStackTrace();
		}

	}

	private class newRequestReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String response = intent.getStringExtra(AndyConstants.NEW_REQUEST);
//			AppLog.Log(TAG, "FROM BROAD CAST-->" + response);
			if(d != null)
			{
				d.dismiss();
			}
			try {
				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 1) {

					requestDetail = parseContent.parseNotification(response);
					if (requestDetail != null && seekbarTimer == null) {
						ringtone = new MyCountDown(30000, 1000);
						
						if (selector == false) {
							selector = true;
							stopCheckingUpcomingRequests();
							// startTimerForRespondRequest(requestDetail.getTimeLeft());

							setComponentVisible();
							// pbTimeLeft.setMax(requestDetail.getTimeLeft());
							btnClientReqRemainTime.setText(""
									+ requestDetail.getTimeLeft());
							// pbTimeLeft.setProgress(requestDetail.getTimeLeft());
							tvClientName.setText(requestDetail.getClientName());
							d_address.setText(requestDetail.getD_address());
							s_address.setText(requestDetail.getS_address());
							// tvClientPhoneNumber.setText(requestDetail
							// .getClientPhoneNumber());
							tvAddress.setText("" + requestDetail.getAddress());
							if (requestDetail.getClientRating() != 0) {
								tvClientRating.setRating(requestDetail
										.getClientRating());
//								Log.i("Rating",	"" + requestDetail.getClientRating());
							}

							if (requestDetail.getClientProfile() != null)
								if (!requestDetail.getClientProfile()
										.equals(""))
									aQuery.id(ivClientProfilePicture)
											.progress(R.id.pBar)
											.image(requestDetail
													.getClientProfile());
							markerClientLocation = null;
							markerClientLocation = mMap
									.addMarker(new MarkerOptions()
											.position(
													new LatLng(
															Double.parseDouble(requestDetail
																	.getClientLatitude()),
															Double.parseDouble(requestDetail
																	.getClientLongitude())))
											.icon(BitmapDescriptorFactory
													.fromResource(R.drawable.pin_client))
											.title(mapActivity
													.getResources()
													.getString(
															R.string.client_location)));

							markerClient_d_location = null;
//							Log.d("xxx",
//									"he" + requestDetail.getClient_d_latitude());
//							Log.d("xxx",
//									"she"
//											+ requestDetail
//													.getClient_d_longitude());
							if (requestDetail.getClient_d_latitude() != null
									&& requestDetail.getClient_d_longitude() != null) {
//								Log.d("xxx", "inside");
								markerClient_d_location = mMap
										.addMarker(new MarkerOptions()
												.position(
														new LatLng(
																Double.parseDouble(requestDetail
																		.getClient_d_latitude()),
																Double.parseDouble(requestDetail
																		.getClient_d_longitude())))
												.icon(BitmapDescriptorFactory
														.fromResource(R.drawable.img_destination))
												.title(mapActivity
														.getResources()
														.getString(
																R.string.client_d_location)));
							}

							seekbarTimer = new SeekbarTimer(
									requestDetail.getTimeLeft() * 1000, 1000);
							seekbarTimer.start();
//							AppLog.Log(TAG, "From broad cast recieved request");
						}
					}

				} else {
					setComponentInvisible();
					selector = false;
					preferenceHelper.clearRequestData();
					if (markerClientLocation != null
							&& markerClientLocation.isVisible()) {
						markerClientLocation.remove();
					}
					if (markerClient_d_location != null
							&& markerClient_d_location.isVisible()) {
						markerClient_d_location.remove();
					}
					cancelSeekbarTimer();
					removeNotification();
					startCheckingUpcomingRequests();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}
}
