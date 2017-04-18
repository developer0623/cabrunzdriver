package com.cabrunzltd.cabrunz.driver.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.androidquery.AQuery;
import com.cabrunzltd.cabrunz.driver.MapActivity;
import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.SettingActivity;
import com.cabrunzltd.cabrunz.driver.base.BaseMapFragment;
import com.cabrunzltd.cabrunz.driver.db.DBHelper;
import com.cabrunzltd.cabrunz.driver.locationupdate.LocationHelper;
import com.cabrunzltd.cabrunz.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.cabrunzltd.cabrunz.driver.model.Bill;
import com.cabrunzltd.cabrunz.driver.model.RequestDetail;
import com.cabrunzltd.cabrunz.driver.model.Route;
import com.cabrunzltd.cabrunz.driver.model.Step;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.parse.ParseContent;
import com.cabrunzltd.cabrunz.driver.parse.VolleyHttpRequest;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;
import com.cabrunzltd.cabrunz.driver.widget.MyFontTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
//import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
//import com.google.android.gms.location.LocationClient;

/**
 * @author Kishan H Dhamat
 * 
 */
public class JobFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, OnLocationReceived,
		GoogleApiClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener, ErrorListener {
	private GoogleMap map;
	private GoogleApiClient client;
	private LocationRequest mLocationRequest;
	private LocationRequest locationRequest;
	// private PolylineOptions lineOptions;
	// private BeanRoute route;
	// private ArrayList<LatLng> points;
	private MyFontTextView tvJobTime, tvJobDistance, tvJobStatus, tvClientName,
			tvAddress, d_address, s_address;
	private ImageView ivClientProfilePicture;
	private Route routeDest;
	AlertDialog.Builder gpsBuilder;
	private LatLng myLatLng,latlngdestination,latlanclient;
	private PolylineOptions lineOptionsDest;
	private Polyline polyLineDest;
	private ArrayList<LatLng> pointsDest;
	private RequestQueue requestQueue;
	private ImageButton tvnavi;
	private RatingBar tvClientRating;
	private ParseContent parseContent;
//	private LocationClient locationClient;
	private Location location;
	Chronometer tvtripTime;
	private LocationHelper locationHelper;
	private AQuery aQuery;
	private RequestDetail requestDetail;
	private ArrayList<LatLng> points;
	private PolylineOptions lineOptions;
	private Marker markerDriverLocation, markerClientLocation,
			markerClient_d_location;
	private Timer elapsedTimer;
	private DBHelper dbHelper;
	private BroadcastReceiver mReceiver;
	private int jobStatus = 0;
	private String time, distance = "0";
	private final String TAG = "JobFragment";
	private Pubnub pubnub;
	private String navilat, navilan = "";
	long timetrip = 0;

	DecimalFormat decimalFormat;
	private FareAgreementStatusReceiver fareAgreementStatusReceiver;
	private EditAmountStatusReceiver editAmountStatusReceiver;
	public static final long ELAPSED_TIME_SCHEDULE = 60 * 1000;

	private LinearLayout fareview, timeview, timer;
	private ImageView agree, disagree;
	private EditText customfare;
	private TextView amount, endtrip;
	private boolean checkagree, discheckagree;
	private double edit_amount;
	// private ImageView tvClientPhoneNumber;
	private ToggleButton switchSetting;
	WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PowerManager powerManager = (PowerManager) mapActivity
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");
		wakeLock.acquire();

		IntentFilter fareAgreementStatusIntentFilter = new IntentFilter(
				AndyConstants.FARE_AGREEMENT_STATUS);
		fareAgreementStatusReceiver = new FareAgreementStatusReceiver();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				fareAgreementStatusReceiver, fareAgreementStatusIntentFilter);

		IntentFilter editAmountStatusIntentFilter = new IntentFilter(
				AndyConstants.EDIT_AMOUNT_STATUS);
		editAmountStatusReceiver = new EditAmountStatusReceiver();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				editAmountStatusReceiver, editAmountStatusIntentFilter);

		pubnub = new Pubnub(AndyConstants.PUBNUB_PUBLISH_KEY,
				AndyConstants.PUBNUB_SUBSCRIBE_KEY);
		requestQueue = Volley.newRequestQueue(mapActivity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View jobFragmentView = null;
		try {
			jobFragmentView = inflater.inflate(R.layout.fragment_job,
					container, false);
		
		try {
			MapsInitializer.initialize(getActivity());
		} catch (Exception e) {
		}
		
		agree = (ImageView) jobFragmentView.findViewById(R.id.agree);
		disagree = (ImageView) jobFragmentView.findViewById(R.id.disagree);
		switchSetting = (ToggleButton) jobFragmentView
				.findViewById(R.id.switchAvaibility);
		switchSetting.setOnClickListener(this);
		customfare = (EditText) jobFragmentView.findViewById(R.id.customfare);
		amount = (TextView) jobFragmentView.findViewById(R.id.amount);
		endtrip = (TextView) jobFragmentView.findViewById(R.id.endtrip);
		tvnavi = (ImageButton) jobFragmentView.findViewById(R.id.tvnavi);

		fareview = (LinearLayout) jobFragmentView.findViewById(R.id.fareView);
		timeview = (LinearLayout) jobFragmentView.findViewById(R.id.timeView);
		timer = (LinearLayout) jobFragmentView.findViewById(R.id.timer);

		tvAddress = (MyFontTextView) jobFragmentView.findViewById(R.id.address);
		d_address = (MyFontTextView) jobFragmentView
				.findViewById(R.id.d_address);
		s_address = (MyFontTextView) jobFragmentView
				.findViewById(R.id.s_address);
		tvJobTime = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobTime);
		tvtripTime = (Chronometer) jobFragmentView
				.findViewById(R.id.tvtripTime);
		tvJobDistance = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobDistance);
		tvJobStatus = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobStatus);
		tvJobStatus.setVisibility(View.VISIBLE);
		tvClientName = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvClientName);
		// tvClientPhoneNumber = (ImageView) jobFragmentView
		// .findViewById(R.id.tvClientNumber);
		tvClientRating = (RatingBar) jobFragmentView
				.findViewById(R.id.tvClientRating);
		ivClientProfilePicture = (ImageView) jobFragmentView
				.findViewById(R.id.ivClientImage);
		// tvClientPhoneNumber.setOnClickListener(this);
		tvJobStatus.setOnClickListener(this);
		tvnavi.setOnClickListener(this);
		
		//switchSetting.setImageResource(R.drawable.off);
			locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setInterval(10000);
			locationRequest.setFastestInterval(5000);
			client = new GoogleApiClient.Builder(getActivity())
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
			setUpMapIfNeeded();
		jobFragmentView.findViewById(R.id.tvJobCallClient).setOnClickListener(
				this);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return jobFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		parseContent = new ParseContent(mapActivity);
		decimalFormat = new DecimalFormat("0.00");
		myLatLng = new PreferenceHelper(mapActivity).getDrive_Location();
		latlngdestination = new PreferenceHelper(mapActivity).getD_Location();
		Log.e("MSG", "des job lat :" + new PreferenceHelper(mapActivity).getD_Location().latitude);
		Log.e("MSG", "des job lng :" + new PreferenceHelper(mapActivity).getD_Location().longitude);
		latlanclient = new PreferenceHelper(mapActivity).getS_Location();
		points = new ArrayList<LatLng>();
		aQuery = new AQuery(mapActivity);
		dbHelper = new DBHelper(mapActivity);
		jobStatus = getArguments().getInt(AndyConstants.JOB_STATUS,
				AndyConstants.IS_WALKER_STARTED);
//		jobStatus = getArguments().getInt(AndyConstants.JOB_STATUS);
		requestDetail = (RequestDetail) getArguments().getSerializable(
				AndyConstants.REQUEST_DETAIL);
//		Log.d("mahi", "job status" + jobStatus);




//		if (jobStatus == AndyConstants.IS_WALK_COMPLETED
//				|| jobStatus == AndyConstants.IS_WALK_REACHED) {
//
//			startElapsedTimer();
//			getPathFromServer();
//		} else if(jobStatus == AndyConstants.IS_ASSIGNED || jobStatus == AndyConstants.IS_WALKER_STARTED  ) {
//			setMarkerOnRoad(myLatLng,latlanclient);
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		}
		setClientDetails(requestDetail);
//		setjobStatus(jobStatus);

//		setUpMap();
		//switchSetting.setImageResource(R.drawable.off);
//		locationHelper = new LocationHelper(getActivity());
//		locationHelper.setLocationReceivedLister(this);
//		locationHelper.onStart();
		preferenceHelper.putAvail_on("0");
		
		
		
		checkState();

		// getDistance();

	}

	private void setMarkerOnRoad(LatLng source, LatLng destination) {
		String msg = null;
		if (source == null) {
			msg = "Unable to get source location, please try again";
		} else if (destination == null) {
			msg = "Unable to get destination location, please try again";
		}
		if (msg != null) {
			Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
			return;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				"http://maps.googleapis.com/maps/api/directions/json?origin="
						+ source.latitude + "," + source.longitude
						+ "&destination=" + destination.latitude + ","
						+ destination.longitude + "&sensor=false");
//		Log.d("map", "send" + map);

		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.DRAW_PATH_ROAD, true, this);

//		map.addPolyline(new PolylineOptions().add(source, destination).width(5).color(Color.BLUE).geodesic(true));
	}

	/**
	 * 
	 */
	private void getPathFromServer() {
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_loading), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.PATH_REQUEST
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ preferenceHelper.getRequestId());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.PATH_REQUEST, true, this);
	}

	private void setClientDetails(RequestDetail requestDetail) {
		tvClientName.setText(requestDetail.getClientName());
		d_address.setText(requestDetail.getD_address());
		s_address.setText(requestDetail.getS_address());
		// tvClientPhoneNumber.setText(requestDetail.getClientPhoneNumber());
		tvAddress.setText("" + requestDetail.getAddress());
		preferenceHelper.putAvail_on("0");

		if (requestDetail.getTime() != null) {
			tvJobTime.setText("" + requestDetail.getTime() + ""
					+ mapActivity.getResources().getString(R.string.text_mins));
			tvJobDistance
					.setText(requestDetail.getDistance()
							+ ""
							+ mapActivity.getResources().getString(
									R.string.text_miles));
		}
		if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {

			startElapsedTimer();
			getPathFromServer();
			
		}

		if (requestDetail.getClientRating() != 0) {
			tvClientRating.setRating(requestDetail.getClientRating());
		}
		if (requestDetail.getClientProfile() != null)
			if (!requestDetail.getClientProfile().equals(""))
				aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
						.image(requestDetail.getClientProfile());

		if (map == null) {
			return;
		} 

	}

	/**
	 * it is used for seeting text for jobstatus on textview
	 */
	private void setjobStatus(int jobStatus) {
		pointsDest = new ArrayList<LatLng>();
//		Log.e("", "jobStatus:" + jobStatus);
		switch (jobStatus) {
		case AndyConstants.IS_WALKER_STARTED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walker_started));
			setMarkerOnRoad(myLatLng,latlanclient);
			
			break;
		case AndyConstants.IS_WALKER_ARRIVED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walker_arrived));
			setMarkerOnRoad(myLatLng,latlanclient);
			
			break;
		case AndyConstants.IS_WALK_STARTED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walk_started));
			drawPath(myLatLng,latlngdestination);

			// timeview.setVisibility(View.GONE);
			break;
		case AndyConstants.IS_WALK_COMPLETED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walk_completed));
			//setMarkerOnRoad(myLatLng,latlngdestination);
			
			timeview.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvJobStatus:

			switch (jobStatus) {
			case AndyConstants.IS_WALKER_STARTED:
				walkerStarted();
				break;
			case AndyConstants.IS_WALKER_ARRIVED:
				walkerArrived();
				break;
			case AndyConstants.IS_WALK_STARTED:
				walkStarted();
				break;
			case AndyConstants.IS_WALK_COMPLETED:
				walkCompleted();
				// requestWalkReached();
				break;

			default:
				break;
			}

			break;
		case R.id.switchAvaibility:
			changeState();
			/*
			 * if (SettingActivity.avai) {
			 * switchSetting.setImageResource(R.drawable.on); } else {
			 * switchSetting.setImageResource(R.drawable.off); }
			 */
			break;
		case R.id.tvnavi:
			if (jobStatus == 3 || jobStatus == 4) {
				navilat = requestDetail.getClient_d_latitude();
				navilan = requestDetail.getClient_d_longitude();
			} else {
				navilat = requestDetail.getClientLatitude();
				navilan = requestDetail.getClientLongitude();
			}

			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + navilat + "," + navilan));
			startActivity(i);
			break;
		case R.id.tvJobCallClient:
			if (!TextUtils.isEmpty(requestDetail.getClientPhoneNumber())) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"
						+ requestDetail.getClientPhoneNumber()));
				startActivity(callIntent);
			} else {
				Toast.makeText(
						mapActivity,
						mapActivity.getResources().getString(
								R.string.toast_number_not_found),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private void drawPath(LatLng source, LatLng destination) {
		if (source == null || destination == null) {
			return;
		}
		if (destination.latitude != 0) {

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(AndyConstants.URL,
					"http://maps.googleapis.com/maps/api/directions/json?origin="
							+ source.latitude + "," + source.longitude
							+ "&destination=" + destination.latitude + ","
							+ destination.longitude + "&sensor=false");
			// new HttpRequester(activity, map, Const.ServiceCode.DRAW_PATH,
			// true,
			// this);
//			Log.d("map", "map draw" + map);
			requestQueue.add(new VolleyHttpRequest(1, map,
					AndyConstants.ServiceCode.DRAW_PATH, this, this));
		}



	}

	/**
	 * send this when walk completed
	 */

	private void walkCompleted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_COMPLETED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		map.put(AndyConstants.Params.DISTANCE, preferenceHelper.getDistance()
				+ "");
		map.put(AndyConstants.Params.TIME, time);
//		Log.d("mahi", "map sending" + map);
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALK_COMPLETED, this);
	}

	/**
	 * send this when job started
	 */
	private void walkStarted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_STARTED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
//		Log.e("", "" + map);
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALK_STARTED, this);

		// ///////////////////////////////////////

		// HashMap<String, String> map1 = new HashMap<String, String>();
		// map1.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_STARTED);
		// map1.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		// map1.put(AndyConstants.Params.REQUEST_ID,
		// String.valueOf(preferenceHelper.getRequestId()));
		// map1.put(AndyConstants.Params.TOKEN,
		// preferenceHelper.getSessionToken());
		// map1.put(AndyConstants.Params.LATITUDE,
		// preferenceHelper.getWalkerLatitude());
		// map1.put(AndyConstants.Params.LONGITUDE,
		// preferenceHelper.getWalkerLongitude());
		// map1.put(AndyConstants.Params.DISTANCE,
		// decimalFormat.format(preferenceHelper.getDistance()));
		// map1.put(AndyConstants.Params.UNIT, preferenceHelper.getUnit());
		// pubnubSend(AndyConstants.PUBNUB_WALKER_STARTED_REQ_ID, new
		// JSONObject(map1));
	}

	/**
	 * send this when walker arrived client's location
	 */
	private void walkerArrived() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_ARRIVED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
//		Log.d("mahi", "" + map);
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALKER_ARRIVED, this);

		// ////////////////////////////////////////

		// HashMap<String, String> map1 = new HashMap<String, String>();
		// // map.put(AndyConstants.URL,
		// AndyConstants.ServiceType.WALK_ARRIVED);
		// map1.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		// map1.put(AndyConstants.Params.REQUEST_ID,
		// String.valueOf(preferenceHelper.getRequestId()));
		// map1.put(AndyConstants.Params.TOKEN,
		// preferenceHelper.getSessionToken());
		// map1.put(AndyConstants.Params.LATITUDE,
		// preferenceHelper.getWalkerLatitude());
		// map1.put(AndyConstants.Params.LONGITUDE,
		// preferenceHelper.getWalkerLongitude());
		// map1.put(AndyConstants.Params.DISTANCE,
		// decimalFormat.format(preferenceHelper.getDistance()));
		// map1.put(AndyConstants.Params.UNIT, preferenceHelper.getUnit());
		// pubnubSend(AndyConstants.PUBNUB_WALKER_ARRIVED_REQ_ID, new
		// JSONObject(map1));
	}

	/**
	 * send this when walker started his/her run
	 */
	private void walkerStarted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALKER_STARTED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALKER_STARTED, this);

		// //////////////////////////////////////////////////////

		// HashMap<String, String> map1 = new HashMap<String, String>();
		// //map.put(AndyConstants.URL,
		// AndyConstants.ServiceType.WALKER_STARTED);
		// map1.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		// map1.put(AndyConstants.Params.REQUEST_ID,
		// String.valueOf(preferenceHelper.getRequestId()));
		// map1.put(AndyConstants.Params.TOKEN,
		// preferenceHelper.getSessionToken());
		// map1.put(AndyConstants.Params.LATITUDE,
		// preferenceHelper.getWalkerLatitude());
		// map1.put(AndyConstants.Params.LONGITUDE,
		// preferenceHelper.getWalkerLongitude());
		// map1.put(AndyConstants.Params.DISTANCE,
		// decimalFormat.format(preferenceHelper.getDistance()));
		// map1.put(AndyConstants.Params.UNIT, preferenceHelper.getUnit());
		// Log.d("MSG", "pub nub sending"+new JSONObject(map1).toString());
		// pubnubSend(AndyConstants.PUBNUB_WALK_STARTED_REQ_ID, new
		// JSONObject(map1));
	}

	private void requestWalkReached() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_REACHED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		map.put(AndyConstants.Params.DISTANCE, preferenceHelper.getDistance()
				+ "");
		map.put(AndyConstants.Params.TIME, time);
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALK_REACHED, this);

		// /////////////////////////////////////

		HashMap<String, String> map1 = new HashMap<String, String>();
		map1.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_REACHED);
		map1.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map1.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map1.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map1.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map1.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		map1.put(AndyConstants.Params.DISTANCE, preferenceHelper.getDistance()
				+ "");
		map1.put(AndyConstants.Params.TIME, time);
//		Log.d("MSG", "Walkerreached params" + new JSONObject(map1));
		pubnubSend(AndyConstants.PUBNUB_WALKER_REACHED_REQ_ID, new JSONObject(
				map1));
	}

	/* added by amal */
	private String strAddress = null;

	private void getAddressFromLocation(final LatLng latlng,
			final TextView content) {

		/*
		 * et.setText("Waiting for Address"); et.setTextColor(Color.GRAY);
		 */
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 */

		Geocoder gCoder = new Geocoder(getActivity());
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
			/*
			 * getActivity().runOnUiThread(new Runnable() {
			 * 
			 * @Override public void run() { // TODO Auto-generated method stub
			 * if (!TextUtils.isEmpty(strAddress)) {
			 * 
			 * et.setText(strAddress);
			 * 
			 * 
			 * } else { et.setText("");
			 * 
			 * }
			 * 
			 * } });
			 */

		} catch (IOException exc) {
			exc.printStackTrace();
		}
		// }
		// }).start();

	}


	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
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
		map.setMyLocationEnabled(true);

		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.getUiSettings().setZoomControlsEnabled(false);



		map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

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

					title.setText(marker.getTitle());
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

			map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {

					marker.showInfoWindow();

					return true;
				}
			});

//			try {
//				addMarker();
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}






	}


	@Override
	public void onConnectionFailed(ConnectionResult result) {
	}

	@Override
	public void onConnected(Bundle connectionHint) {

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
		mLocationRequest.setInterval(3000);
		mLocationRequest.setFastestInterval(3000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		Location location = LocationServices.FusedLocationApi.getLastLocation(client);
		LocationServices.FusedLocationApi.requestLocationUpdates(client, mLocationRequest, this);
			if (location != null) {
				if (map != null) {
					if (markerDriverLocation == null) {
						markerDriverLocation = map
								.addMarker(new MarkerOptions().position(
										new LatLng(	location.getLatitude(),	location.getLongitude()))
											.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_driver))
											.title(getResources().getString(R.string.my_location)));

									markerDriverLocation.setVisible(true);
						myLatLng = new LatLng(location.getLatitude(),
								location.getLongitude());
//						animateCameraToMarker(latLang);
									map.animateCamera(CameraUpdateFactory.newLatLngZoom(
											new LatLng(location.getLatitude(),
													location.getLongitude()),
											16));
									//myLatLng =  new LatLng(location.getLatitude(), location.getLongitude());
						preferenceHelper.putDrive_Location(new LatLng(location.getLatitude(), location.getLongitude()));

					} else {
						markerDriverLocation.setPosition(
								new LatLng(location.getLatitude(), location.getLongitude()));
									myLatLng =  new LatLng(location.getLatitude(), location.getLongitude());
									preferenceHelper.putDrive_Location(new LatLng(location.getLatitude(), location.getLongitude()));
					}
				}
			} else{
				showLocationOffDialog();
			}

		setMarkerD_C();
		setjobStatus(jobStatus);
//		if (jobStatus == AndyConstants.IS_WALKER_STARTED || jobStatus == AndyConstants.IS_ASSIGNED ) {
//			setMarkerOnRoad(myLatLng,latlanclient);
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		} else if (jobStatus == AndyConstants.IS_WALKER_ARRIVED
//				|| jobStatus == AndyConstants.IS_WALK_STARTED) {
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		}



	}

	private void animateCameraToMarker(LatLng latLng) {
		try {

			CameraUpdate cameraUpdate = null;

			cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
			map.animateCamera(cameraUpdate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	@Override
	public void onLocationChanged(Location loc) {
		markerDriverLocation.setPosition(new LatLng(loc
				.getLatitude(), loc.getLongitude()));
		setjobStatus(jobStatus);
		//		if (jobStatus == AndyConstants.IS_WALKER_STARTED || jobStatus == AndyConstants.IS_ASSIGNED ) {
//			setMarkerOnRoad(myLatLng,latlanclient);
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		} else if (jobStatus == AndyConstants.IS_WALKER_ARRIVED
//				|| jobStatus == AndyConstants.IS_WALK_STARTED) {
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		}


	}

	public void setMarkerD_C(){
		if(map !=null){
			if (markerClient_d_location == null) {

					markerClient_d_location = map
							.addMarker(new MarkerOptions()
									.position(latlngdestination)
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.img_destination))
									.title("Destination"));




			}

			if (markerClientLocation == null) {

				markerClientLocation = map.addMarker(new MarkerOptions()
						.position(latlanclient)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.img_usermappin)));

				if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.job_start_location));
				} else {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.client_location));

				}
			} else {
				markerClientLocation.setPosition(latlanclient);
				if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.job_start_location));
				} else {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.client_location));

				}
			}
		}
	}


	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.jobMap);
			mapFrag.getMapAsync(this);
		}


	}

//	private void setUpMap() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
//		if (googleMap == null) {
//			googleMap = ((SupportMapFragment) getActivity()
//					.getSupportFragmentManager().findFragmentById(R.id.jobMap))
//					.getMap();
//			//initPreviousDrawPath();
//
//
//
//			googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
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
//					title.setText(marker.getTitle());
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
//			googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
//				@Override
//				public boolean onMarkerClick(Marker marker) {
//
//					marker.showInfoWindow();
//
//					return true;
//				}
//			});
//
//			try {
//				addMarker();
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//	}

	// It will add marker on map of walker location
	private void addMarker() {
//		if (googleMap == null) {
//			setUpMap();
//			return;
//		}
//
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
//							if (googleMap != null) {
//								if (markerDriverLocation == null) {
//									markerDriverLocation = googleMap
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
//
//									markerDriverLocation.setVisible(true);
//									/*googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//											new LatLng(location.getLatitude(),
//													location.getLongitude()),
//											16));*/
//									//myLatLng =  new LatLng(location.getLatitude(), location.getLongitude());
//									//preferenceHelper.putDrive_Location(new LatLng(location.getLatitude(), location.getLongitude()));
//
//								} else {
//									markerDriverLocation
//											.setPosition(new LatLng(location
//													.getLatitude(), location
//													.getLongitude()));
//									//myLatLng =  new LatLng(location.getLatitude(), location.getLongitude());
//									//preferenceHelper.putDrive_Location(new LatLng(location.getLatitude(), location.getLongitude()));
//								}
//							}
//						} else{
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
//
//
//	   if (jobStatus == AndyConstants.IS_WALKER_STARTED ||jobStatus == AndyConstants.IS_ASSIGNED ) {
//			setMarkerOnRoad(myLatLng,latlanclient);
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		} else if (jobStatus == AndyConstants.IS_WALKER_ARRIVED
//				|| jobStatus == AndyConstants.IS_WALK_STARTED) {
//			setMarkerOnRoad(myLatLng,latlngdestination);
//		}
	}
	
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

	public void onDestroyView() {
		wakeLock.release();
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.jobMap);
		if (f != null) {
			try {
				getFragmentManager().beginTransaction().remove(f).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		map = null;
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopElapsedTimer();
		/*
		 * timetrip = tvtripTime.getBase() - SystemClock.elapsedRealtime(); int
		 * hours = (int) (timetrip / 3600000); int minutes = (int) (timetrip -
		 * hours * 3600000) / 60000; preferenceHelper.putRequestTime(minutes);
		 */

		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.
		// checkState();
	}

	private void initPreviousDrawPath() {
		// points = dbHelper.getLocations();
		lineOptions = new PolylineOptions();
		lineOptions.addAll(points);
		lineOptions.width(15);
		lineOptions.color(getResources().getColor(R.color.skyblue));
		map.addPolyline(lineOptions);
		points.clear();
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {

		switch (serviceCode) {

		case AndyConstants.ServiceCode.GET_CLIENT_LOCATION:

//				Log.d("mahi", "toggle resposne" + response);

				if (response != null)
					if (!parseContent.isSuccess(response)) {
						return;
					}

			requestDetail = parseContent.parseClientlocation(response);
			latlanclient = new LatLng(Double.parseDouble(requestDetail
					.getClientLatitude()), Double
					.parseDouble(requestDetail
							.getClientLongitude()));
			preferenceHelper.putS_Location(latlanclient);
			setMarkerD_C();



				break;

		case AndyConstants.ServiceCode.CHECK_STATE:
		case AndyConstants.ServiceCode.TOGGLE_STATE:
			

//			Log.d("mahi", "toggle response" + response);
			if (response != null)
				if (parseContent.parseAvaibilty(response)) {
					SettingActivity.avai = true;
					switchSetting.setChecked(true);
					AndyUtils.removeCustomProgressDialog();
				} else {
					SettingActivity.avai = false;
					switchSetting.setChecked(false);
					AndyUtils.removeCustomProgressDialog();
				}
			break;
		case AndyConstants.ServiceCode.WALKER_STARTED:
//			Log.e(TAG, "walker started response " + response);
			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
					jobStatus = AndyConstants.IS_WALKER_ARRIVED;
					setjobStatus(jobStatus);
//					if(markerDriverLocation.getPosition() !=null && markerClientLocation.getPosition() != null){
//					try {
//						setMarkerOnRoad(markerDriverLocation.getPosition(),
//								markerClientLocation.getPosition());
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					preferenceHelper.putS_Location(markerClientLocation.getPosition());
//					}
				}

			break;
		case AndyConstants.ServiceCode.WALKER_ARRIVED:
//			AppLog.Log(TAG, "walker arrived response " + response);
			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
					jobStatus = AndyConstants.IS_WALK_STARTED;
//					latlngdestination
//					if(markerDriverLocation.getPosition() !=null && markerClient_d_location.getPosition() != null){
//					setMarkerOnRoad(markerDriverLocation.getPosition(),
//							markerClient_d_location.getPosition());
//					preferenceHelper.putD_Location(markerClient_d_location.getPosition());
//					}

//					if(markerDriverLocation.getPosition() !=null && latlngdestination!=null){
//						setMarkerOnRoad(markerDriverLocation.getPosition(),
//								latlngdestination);
////						preferenceHelper.putD_Location(markerClient_d_location.getPosition());
//					}

					setjobStatus(jobStatus);
				}
			break;

		case AndyConstants.ServiceCode.DRAW_PATH_ROAD:
//			Log.d("map", "" + response);
			if (!TextUtils.isEmpty(response)) {
				routeDest = new Route();
				parseContent.parseRoute(response, routeDest);

				final ArrayList<Step> step = routeDest.getListStep();
				System.out.println("step size=====> " + step.size());

				lineOptionsDest = new PolylineOptions().width(15).color(getResources().getColor(
						R.color.color_button_blue));
				lineOptionsDest.geodesic(true);

				for (int i = 0; i < step.size(); i++) {
					List<LatLng> path = step.get(i).getListPoints();
					System.out.println("step =====> " + i + " and "
							+ path.size());
					pointsDest.addAll(path);
				}

				if(pointsDest !=null && pointsDest.size() > 0){
					try{

						drawPath(latlanclient, latlngdestination);
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			break;

		case AndyConstants.ServiceCode.DRAW_PATH:
			if (!TextUtils.isEmpty(response)) {
				routeDest = new Route();
				parseContent.parseRoute(response, routeDest);

				final ArrayList<Step> step = routeDest.getListStep();
				System.out.println("step size=====> " + step.size());
//				pointsDest = new ArrayList<LatLng>();
				lineOptionsDest = new PolylineOptions().width(15).color(getResources().getColor(
						R.color.color_button_blue));
				lineOptionsDest.geodesic(true);

				for (int i = 0; i < step.size(); i++) {
					List<LatLng> path = step.get(i).getListPoints();
					System.out.println("step =====> " + i + " and "
							+ path.size());
					pointsDest.addAll(path);
				}
				
				try {
					if (polyLineDest != null)
						polyLineDest.remove();
					lineOptionsDest.addAll(pointsDest);
					if (lineOptionsDest != null && map != null) {
						polyLineDest = map.addPolyline(lineOptionsDest);

						boundLatLang();
					}
				} catch (NotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // #00008B rgb(0,0,139)
				
			}
			break;
		case AndyConstants.ServiceCode.WALK_STARTED:
			timeview.setVisibility(View.VISIBLE);

			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
					preferenceHelper.putIsTripStart(true);
					jobStatus = AndyConstants.IS_WALK_COMPLETED;
//					if(markerDriverLocation !=null && markerClient_d_location != null){
//					setMarkerOnRoad(markerDriverLocation.getPosition(),
//							markerClient_d_location.getPosition());
//					preferenceHelper.putD_Location(markerClient_d_location.getPosition());
//					}
					setjobStatus(jobStatus);
					// getDistance();
					preferenceHelper.putRequestTime(Calendar.getInstance()
							.getTimeInMillis());
					if (markerClientLocation != null) {
						markerClientLocation.setTitle(mapActivity
								.getResources().getString(
										R.string.job_start_location));
					}
					startElapsedTimer();
					//markerClientLocation.remove();

				}

			break;
		case AndyConstants.ServiceCode.WALK_COMPLETED:

			if (response != null)
				if (parseContent.isSuccess(response)) {
					tvtripTime.stop();
					AndyUtils.removeCustomProgressDialog();
					FeedbackFrament feedbackFrament = new FeedbackFrament();
					Bundle bundle = new Bundle();
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					Bill bill = parseContent
							.parsebillwhenwalkcomplete(response);
					bundle.putSerializable("bill", bill);
					// bundle.putString(
					// AndyConstants.Params.TIME,
					// time
					// + " "
					// + mapActivity.getResources().getString(
					// R.string.text_mins));
					// bundle.putString(
					// AndyConstants.Params.DISTANCE,
					// decimalFormat.format(preferenceHelper.getDistance())
					// // / (1000 * 1.62))
					// + " "
					// + mapActivity.getResources().getString(
					// R.string.text_miles));
					requestDetail.setTime(time);

					requestDetail.setDistance(" "
							+ preferenceHelper.getDistance());
					requestDetail.setUnit(preferenceHelper.getUnit());
					feedbackFrament.setArguments(bundle);
					mapActivity.addFragment(feedbackFrament, false,
							AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
				}
			break;

		case AndyConstants.ServiceCode.WALK_REACHED:
			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
					// parseContent.parseWhenWalkReached(response);
					timeview.setVisibility(View.GONE);
					fareview.setVisibility(View.VISIBLE);
					tvJobStatus.setVisibility(View.GONE);
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(response);
						final double totalCost = jsonObject
								.getDouble(AndyConstants.Params.TOTAL);
						final String reqid = jsonObject
								.getString(AndyConstants.Params.REQUEST_ID);
						amount.setText("" + totalCost);
						agree.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								discheckagree = false;
								disagree.setImageResource(R.drawable.unselectable);
								if (!checkagree) {
									agree.setImageResource(R.drawable.selectable);
									checkagree = true;
								} else {
									agree.setImageResource(R.drawable.unselectable);
									checkagree = false;
									Intent pushIntent = new Intent(
											AndyConstants.FARE_AGREEMENT_STATUS);
									pushIntent
											.putExtra(
													AndyConstants.Params.FARE_AGREEMENT_STATUS,
													AndyConstants.FARE_DISAGREED);
									pushIntent.putExtra(
											AndyConstants.Params.TOTAL,
											totalCost);
									pushIntent.putExtra(
											AndyConstants.Params.REQUEST_ID,
											reqid);
									LocalBroadcastManager.getInstance(
											mapActivity).sendBroadcast(
											pushIntent);
								}
							}
						});
						disagree.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								agree.setImageResource(R.drawable.unselectable);
								checkagree = false;
								if (!discheckagree) {
									disagree.setImageResource(R.drawable.selectable);
									discheckagree = true;
									Intent pushIntent = new Intent(
											AndyConstants.FARE_AGREEMENT_STATUS);
									pushIntent
											.putExtra(
													AndyConstants.Params.FARE_AGREEMENT_STATUS,
													AndyConstants.FARE_DISAGREED);
									pushIntent.putExtra(
											AndyConstants.Params.TOTAL,
											totalCost);
									pushIntent.putExtra(
											AndyConstants.Params.REQUEST_ID,
											reqid);
									LocalBroadcastManager.getInstance(
											mapActivity).sendBroadcast(
											pushIntent);
								} else {
									disagree.setImageResource(R.drawable.unselectable);
									discheckagree = false;
								}
							}
						});
						endtrip.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								if (!checkagree && !discheckagree) {
									Toast.makeText(mapActivity,
											"Please agree the amount", Toast.LENGTH_SHORT)
											.show();
								}
								if (checkagree) {
									Intent pushIntent = new Intent(
											AndyConstants.FARE_AGREEMENT_STATUS);
									pushIntent
											.putExtra(
													AndyConstants.Params.FARE_AGREEMENT_STATUS,
													AndyConstants.FARE_AGREED);
									pushIntent.putExtra(
											AndyConstants.Params.TOTAL,
											totalCost);
									pushIntent.putExtra(
											AndyConstants.Params.REQUEST_ID,
											reqid);
									LocalBroadcastManager.getInstance(
											mapActivity).sendBroadcast(
											pushIntent);
								}
								if (discheckagree) {
									if (customfare.getText().length() == 0) {
										Toast.makeText(mapActivity,
												"Please Enter Your Amount", Toast.LENGTH_SHORT)
												.show();
									} else {
										Intent pushIntent = new Intent(
												AndyConstants.EDIT_AMOUNT_STATUS);
										edit_amount = Double.parseDouble(""
												+ customfare.getText());
										pushIntent
												.putExtra(
														AndyConstants.Params.QUOTED_PRICE,
														""
																+ customfare
																		.getText());
										pushIntent
												.putExtra(
														AndyConstants.Params.REQUEST_ID,
														reqid);
										LocalBroadcastManager.getInstance(
												mapActivity).sendBroadcast(
												pushIntent);
									}
								}
							}
						});
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			break;

		case AndyConstants.ServiceCode.GET_ROUTE:
//			 if (parseContent.isSuccess(response)) {
//			 jobStatus = AndyConstants.;
//			 setjobStatus(jobStatus);
//			 }
		case AndyConstants.ServiceCode.PATH_REQUEST:
//			AppLog.Log(TAG, "Path request :" + response);
			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
					parseContent.parsePathRequest(response, points);
					//initPreviousDrawPath();
				}
			break;

		case AndyConstants.ServiceCode.AGREE_AMOUNT:
			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
//					Log.d("MSG", "AgreeResponse: " + response);
					jobStatus = AndyConstants.IS_WALK_REACHED;
					FeedbackFrament feedbackFrament = new FeedbackFrament();
					Bundle bundle = new Bundle();
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					Bill bill = parseContent
							.parsebillwhenwalkcomplete(response);
					bundle.putSerializable("bill", bill);
					long timeElapsed = SystemClock.elapsedRealtime()
							- tvtripTime.getBase();
					int hours = (int) (timeElapsed / 3600000);
					int minutes = (int) (timeElapsed - hours * 3600000) / 60000;

					requestDetail.setTime(String.valueOf(minutes));
					// requestDetail.setTime(time);
					requestDetail.setDistance(" "
							+ preferenceHelper.getDistance());
					requestDetail.setUnit(preferenceHelper.getUnit());
					feedbackFrament.setArguments(bundle);
					mapActivity.addFragment(feedbackFrament, false,
							AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
				}
			break;

		case AndyConstants.ServiceCode.EDIT_AMOUNT:
			if (response != null)
				if (parseContent.isSuccess(response)) {
					AndyUtils.removeCustomProgressDialog();
//					Log.d("MSG", "EditResponse: " + response);
					jobStatus = AndyConstants.IS_WALK_REACHED;
					FeedbackFrament feedbackFrament = new FeedbackFrament();
					Bundle bundle = new Bundle();
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					Bill bill = parseContent
							.parsebillwhenwalkcomplete(response);
					bundle.putSerializable("bill", bill);

					requestDetail.setTime(time);
					requestDetail.setDistance(" "
							+ preferenceHelper.getDistance());
					requestDetail.setUnit(preferenceHelper.getUnit());
					feedbackFrament.setArguments(bundle);
					mapActivity.addFragment(feedbackFrament, false,
							AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
				}
			break;

		default:
			break;
		}
	}

	/**
	 * 
	 */
	private void startElapsedTimer() {
		elapsedTimer = new Timer();
		elapsedTimer.scheduleAtFixedRate(new TimerRequestStatus(),
				AndyConstants.DELAY, ELAPSED_TIME_SCHEDULE);
	}

	private void stopElapsedTimer() {
		if (elapsedTimer != null) {
			elapsedTimer.cancel();
			elapsedTimer = null;
		}
	}

	private class TimerRequestStatus extends TimerTask {
		@Override
		public void run() {
			// isContinueRequest = false;
//			AppLog.Log(TAG, "In elapsed time timer");
			mapActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (isVisible()) {
						if (preferenceHelper.getRequestTime() == AndyConstants.NO_TIME) {
							preferenceHelper.putRequestTime(System
									.currentTimeMillis());
						}
						time = String.valueOf((Calendar.getInstance()
								.getTimeInMillis() - preferenceHelper
								.getRequestTime())
								/ (1000 * 60));
						tvJobTime.setText(time
								+ " "
								+ mapActivity.getResources().getString(
										R.string.text_mins));
					}
				}
			});

		}
	}

	private void checkState() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_getting_avaibility), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_STATE + AndyConstants.Params.ID
						+ "=" + preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
//		Log.d("mahi", "toggle request" + map);
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.CHECK_STATE, true, this);
	}

	private void changeState() {
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
		// TODO Auto-generated method stub
		super.onResume();
		// checkState();
		mapActivity.currentFragment = AndyConstants.JOB_FRGAMENT_TAG;

		checkingClientLocation();
		
		//preferenceHelper.putCurrentFragment(AndyConstants.JOB_FRGAMENT_TAG);
		/*
		 * if (SettingActivity.avai) {
		 * switchSetting.setImageResource(R.drawable.on); } else {
		 * switchSetting.setImageResource(R.drawable.off);
		 * 
		 * }
		 */
		/*
		 * timetrip = tvtripTime.getBase() - SystemClock.elapsedRealtime(); int
		 * hours = (int) (timetrip / 3600000); int minutes = (int) (timetrip -
		 * hours * 3600000) / 60000; preferenceHelper.putRequestTime(minutes);
		 */

		registerCancelReceiver();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		client.connect();
		super.onStart();
	}



	private class TimerRequestLocation extends TimerTask {
		@Override
		public void run() {
			getclientlocation();
		}

	}

	private void getclientlocation(){
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			return;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.GET_CLIENT_LOCATION
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.OWNERID + "="
						+ preferenceHelper.getClientId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());

			new HttpRequester(mapActivity, map,
					AndyConstants.ServiceCode.GET_CLIENT_LOCATION, true, this);
	}

	private void checkingClientLocation() {
//		AppLog.Log(TAG, "start checking upcomingRequests");
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerRequestLocation(),
				AndyConstants.DELAY, AndyConstants.TIME_SCHEDULE);
	}

	private void registerCancelReceiver() {
		IntentFilter intentFilter = new IntentFilter("CANCEL_REQUEST");
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
//				AppLog.Log("JobFragment", "CANCEL_REQUEST");
				stopElapsedTimer();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						mapActivity);
				builder.setMessage("One of your trip canceled by user.")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// do things
										dialog.cancel();
										preferenceHelper.clearRequestData();
										mapActivity
												.startActivity(new Intent(
														mapActivity,
														MapActivity.class));
										mapActivity.finish();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

			}
		};
		mapActivity.registerReceiver(mReceiver, intentFilter);
	}

	@Override
	public void onLocationReceived(LatLng latlong) {

		if (map == null) {
			return;
		}
		try {

			if (markerClient_d_location == null) {
				if (requestDetail.getClient_d_latitude() != null
						&& requestDetail.getClient_d_longitude() != null) {
					markerClient_d_location = map
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(
													Double.parseDouble(requestDetail
															.getClient_d_latitude()),
													Double.parseDouble(requestDetail
															.getClient_d_longitude())))
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.img_destination))
									.title("Destination"));

					latlngdestination =  new LatLng(Double.parseDouble(requestDetail.getClient_d_latitude()),Double.parseDouble(requestDetail.getClient_d_longitude()));
					//preferenceHelper.putD_Location(latlngdestination);

				}
			}

			if (markerClientLocation == null) {

				markerClientLocation = map.addMarker(new MarkerOptions()
						.position(
								new LatLng(Double.parseDouble(requestDetail
										.getClientLatitude()), Double
										.parseDouble(requestDetail
												.getClientLongitude())))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.img_usermappin)));
				latlanclient =  new LatLng(Double.parseDouble(requestDetail.getClientLatitude()),Double.parseDouble(requestDetail.getClientLongitude()));
				//preferenceHelper.putS_Location(latlanclient);

				if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.job_start_location));
				} else {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.client_location));

				}
			}
			if (latlong != null) {
				if (map != null) {
					// if (markerDriverLocation == null) {
					if (markerDriverLocation != null) {
						markerDriverLocation.remove();
					}
					markerDriverLocation = map
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(latlong.latitude,
													latlong.longitude))
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.pin_driver))
									.title(getResources().getString(
											R.string.my_location)));
					//myLatLng =  latlong;
					//preferenceHelper.putDrive_Location(latlong);

					/*googleMap.animateCamera(CameraUpdateFactory
							.newLatLngZoom(new LatLng(latlong.latitude,
									latlong.longitude), 16));*/
					// } else {
					markerDriverLocation.setPosition(new LatLng(
							latlong.latitude, latlong.longitude));
					if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
						//drawTrip(new LatLng(latlong.latitude, latlong.longitude));

						HashMap<String, String> map1 = new HashMap<String, String>();
						map1.put(AndyConstants.Params.ID,
								preferenceHelper.getUserId());
						map1.put(AndyConstants.Params.REQUEST_ID,
								String.valueOf(preferenceHelper.getRequestId()));
						map1.put(AndyConstants.Params.TOKEN,
								preferenceHelper.getSessionToken());
						map1.put(AndyConstants.Params.LATITUDE,
								preferenceHelper.getWalkerLatitude());
						map1.put(AndyConstants.Params.LONGITUDE,
								preferenceHelper.getWalkerLongitude());
						map1.put(AndyConstants.Params.DISTANCE, decimalFormat
								.format(preferenceHelper.getDistance()));
						map1.put(AndyConstants.Params.UNIT,
								preferenceHelper.getUnit());
						map1.put(AndyConstants.Params.TIME, time);
						pubnubSend(AndyConstants.PUBNUB_WALKER_STARTED_REQ_ID,
								new JSONObject(map1));

						// distance = decimalFormat.format(distanceMeter / (1000
						// * 1.62));

						// tvJobDistance.setText(decimalFormat
						// .format(preferenceHelper.getDistance()
						// / (1000 * 1.62))
						// + " "
						// + mapActivity.getResources().getString(
						// R.string.text_miles));

						tvJobDistance.setText(decimalFormat
								.format(preferenceHelper.getDistance()
								// * (1000 * 1.62)
								) + " " + preferenceHelper.getUnit());

						// }
					}
					// getDistance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private void getDistance() {
	// if (googleMap == null || markerDriverLocation == null) {
	// return;
	// }
	// if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
	//
	// ArrayList<LatLng> latLngs = dbHelper.getLocations();
	// int distanceMeter = 0;
	// if (latLngs.size() >= 2) {
	// for (int i = 0; i < latLngs.size() - 1; i++) {
	// Location location1 = new Location("");
	// Location location2 = new Location("");
	// location1.setLatitude(latLngs.get(i).latitude);
	// location1.setLongitude(latLngs.get(i).longitude);
	// location2.setLatitude(latLngs.get(i +
	// 1).latitude);googleMap.setInfoWindowAdapter(this);
	// location2.setLongitude(latLngs.get(i + 1).longitude);
	// distanceMeter = distanceMeter
	// + (int) location1.distanceTo(location2);
	//
	// }
	// }
	// // AndyUtils.showToast("Meter:" + distanceMeter, mapActivity);
	// DecimalFormat decimalFormat = new DecimalFormat("0.00");
	// distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
	// tvJobDistance
	// .setText(distance
	// + " "
	// + mapActivity.getResources().getString(
	// R.string.text_miles));
	// // Location jobStartLocation = new Location("");
	// // Location currentLocation = new Location("");
	// // jobStartLocation.setLatitude(Double.parseDouble(requestDetail
	// // .getClientLatitude()));
	// // jobStartLocation.setLongitude(Double.parseDouble(requestDetail
	// // .getClientLongitude()));
	// // currentLocation
	// // .setLatitude(markerDriverLocation.getPosition().latitude);
	// // currentLocation
	// // .setLongitude(markerDriverLocation.getPosition().longitude);
	// // AppLog.Log(TAG, jobStartLocation.distanceTo(currentLocation)
	// // + " METERS ");
	// // int distanceMeter = (int) jobStartLocation
	// // .distanceTo(currentLocation);
	// // DecimalFormat decimalFormat = new DecimalFormat("0.0");
	// // distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
	// // tvJobDistance
	// // .setText(distance
	// // + " "
	// // + mapActivity.getResources().getString(
	// // R.string.text_miles));
	// }
	// }

	private void drawTrip(LatLng latlng) {

		if (map != null) {
			// setMarker(latlng);
			points.add(latlng);
			// dbHelper.addLocation(latlng);
			lineOptions = new PolylineOptions();
			lineOptions.addAll(points);
			lineOptions.width(15);
			lineOptions.color(getResources().getColor(R.color.skyblue));

			map.addPolyline(lineOptions);
		}

	}

	class EditAmountStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			double quotedPrice = intent.getDoubleExtra(
					AndyConstants.Params.QUOTED_PRICE, 0);
			String reqId = intent
					.getStringExtra(AndyConstants.Params.REQUEST_ID);
//			Log.e("", "AmountStatusReceiver::" + quotedPrice);
			callEditAmountApi(context, reqId, quotedPrice);
		}
	}

	class FareAgreementStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String fareAgreementStatus = intent
					.getStringExtra(AndyConstants.Params.FARE_AGREEMENT_STATUS);
			if (fareAgreementStatus.equals(AndyConstants.FARE_AGREED)) {
				// Call Agree API

				double totalCost = intent.getDoubleExtra(
						AndyConstants.Params.TOTAL, 0);
				String reqId = intent
						.getStringExtra(AndyConstants.Params.REQUEST_ID);
				callFareAgreeApi(context, reqId, totalCost);
			} else {
//				Log.d("MSG", "Disagreed");
			}
		}
	}

	private void callFareAgreeApi(Context context, String reqId,
			double totalCost) {
		// token id request_id sub_total
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.AGREE_AMOUNT);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID, reqId);
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put("sub_total", String.valueOf(totalCost));
		new HttpRequester(context, map, AndyConstants.ServiceCode.AGREE_AMOUNT,
				this);
	}

	private void boundLatLang() {

		try {
			if (markerDriverLocation != null && markerClientLocation != null
					&& markerClient_d_location != null) {
				LatLngBounds.Builder bld = new LatLngBounds.Builder();
				bld.include(new LatLng(
						markerDriverLocation.getPosition().latitude,
						markerDriverLocation.getPosition().longitude));
				bld.include(new LatLng(
						markerClientLocation.getPosition().latitude,
						markerClientLocation.getPosition().longitude));
				bld.include(new LatLng(
						markerClient_d_location.getPosition().latitude,
						markerClient_d_location.getPosition().longitude));
				LatLngBounds latLngBounds = bld.build();

				map.animateCamera(CameraUpdateFactory.newLatLngBounds(
						latLngBounds, 18));
			} else if (markerDriverLocation != null
					&& markerClientLocation != null) {
				LatLngBounds.Builder bld = new LatLngBounds.Builder();
				bld.include(new LatLng(
						markerDriverLocation.getPosition().latitude,
						markerDriverLocation.getPosition().longitude));
				bld.include(new LatLng(
						markerClientLocation.getPosition().latitude,
						markerClientLocation.getPosition().longitude));
				LatLngBounds latLngBounds = bld.build();

				map.animateCamera(CameraUpdateFactory.newLatLngBounds(
						latLngBounds, 18));
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private void callEditAmountApi(Context context, String reqId,
			double quotedPrice) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.EDIT_AMOUNT);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID, reqId);
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		// DecimalFormat df = new DecimalFormat("#.00");
		map.put("sub_total", "" + edit_amount);
		new HttpRequester(context, map, AndyConstants.ServiceCode.EDIT_AMOUNT,
				this);
	}

	private void pubnubSend(final String reqId, final JSONObject message) {
		Callback callback = new Callback() {
			@Override
			public void successCallback(String channel, Object response) {
				super.successCallback(channel, response);
//				Log.d("MSG", "Pub nub response" + response);
//				Log.d("MSG", "Pub nub req id" + reqId);

				// if(reqId.equals(AndyConstants.PUBNUB_WALK_STARTED_REQ_ID)) {
				// jobStatus = AndyConstants.IS_WALKER_ARRIVED;
				// } else
				// if(reqId.equals(AndyConstants.PUBNUB_WALKER_ARRIVED_REQ_ID))
				// {
				// jobStatus = AndyConstants.IS_WALK_STARTED;
				// } else
				// if(reqId.equals(AndyConstants.PUBNUB_WALKER_STARTED_REQ_ID))
				// {
				// preferenceHelper.putIsTripStart(true);
				// jobStatus = AndyConstants.IS_WALK_COMPLETED;
				// // getDistance();
				// // preferenceHelper.putRequestTime(Calendar.getInstance()
				// // .getTimeInMillis());
				// if (markerClientLocation != null) {
				// mapActivity.runOnUiThread(new Runnable() {
				// @Override
				// public void run() {
				// markerClientLocation.setTitle(mapActivity.getResources()
				// .getString(R.string.job_start_location));
				// }
				// });
				// }
				// startElapsedTimer();
				// mapActivity.runOnUiThread(new Runnable() {
				// @Override
				// public void run() {
				// try{
				// markerClientLocation.remove();
				// }catch(Exception e){
				// e.printStackTrace();
				// }
				// }
				// });
				// } else

				if (reqId.equals(AndyConstants.PUBNUB_WALKER_REACHED_REQ_ID)) {
					// parseContent.parseWhenWalkReached(message.toString());
					if (parseContent.isSuccess(message.toString())) {
						// parseContent.parseWhenWalkReached(response);
						timeview.setVisibility(View.GONE);
						fareview.setVisibility(View.VISIBLE);
						tvJobStatus.setVisibility(View.GONE);
						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(message.toString());
							final double totalCost = jsonObject
									.getDouble(AndyConstants.Params.TOTAL);
							final String reqid = jsonObject
									.getString(AndyConstants.Params.REQUEST_ID);
							amount.setText("$" + totalCost);
							agree.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									discheckagree = false;
									disagree.setImageResource(R.drawable.icon_price);
									if (!checkagree) {
										agree.setImageResource(R.drawable.ic_launcher);
										checkagree = true;
									} else {
										agree.setImageResource(R.drawable.icon_price);
										checkagree = false;
										Intent pushIntent = new Intent(
												AndyConstants.FARE_AGREEMENT_STATUS);
										pushIntent
												.putExtra(
														AndyConstants.Params.FARE_AGREEMENT_STATUS,
														AndyConstants.FARE_DISAGREED);
										pushIntent.putExtra(
												AndyConstants.Params.TOTAL,
												totalCost);
										pushIntent
												.putExtra(
														AndyConstants.Params.REQUEST_ID,
														reqid);
										LocalBroadcastManager.getInstance(
												mapActivity).sendBroadcast(
												pushIntent);
									}
								}
							});
							disagree.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									agree.setImageResource(R.drawable.icon_price);
									checkagree = false;
									if (!discheckagree) {
										disagree.setImageResource(R.drawable.ic_launcher);
										discheckagree = true;
										Intent pushIntent = new Intent(
												AndyConstants.FARE_AGREEMENT_STATUS);
										pushIntent
												.putExtra(
														AndyConstants.Params.FARE_AGREEMENT_STATUS,
														AndyConstants.FARE_DISAGREED);
										pushIntent.putExtra(
												AndyConstants.Params.TOTAL,
												totalCost);
										pushIntent
												.putExtra(
														AndyConstants.Params.REQUEST_ID,
														reqid);
										LocalBroadcastManager.getInstance(
												mapActivity).sendBroadcast(
												pushIntent);
									} else {
										disagree.setImageResource(R.drawable.icon_price);
										discheckagree = false;
									}
								}
							});
							endtrip.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if (!checkagree && !discheckagree) {
										Toast.makeText(mapActivity,
												"Please agree the amount", Toast.LENGTH_LONG)
												.show();
									}
									if (checkagree) {
										Intent pushIntent = new Intent(
												AndyConstants.FARE_AGREEMENT_STATUS);
										pushIntent
												.putExtra(
														AndyConstants.Params.FARE_AGREEMENT_STATUS,
														AndyConstants.FARE_AGREED);
										pushIntent.putExtra(
												AndyConstants.Params.TOTAL,
												totalCost);
										pushIntent
												.putExtra(
														AndyConstants.Params.REQUEST_ID,
														reqid);
										LocalBroadcastManager.getInstance(
												mapActivity).sendBroadcast(
												pushIntent);

									}
									if (discheckagree) {
										if (customfare.getText().length() == 0) {
											Toast.makeText(mapActivity,
													"Please Enter Your Amount",
													Toast.LENGTH_LONG).show();
										} else {
											Intent pushIntent = new Intent(
													AndyConstants.EDIT_AMOUNT_STATUS);

											pushIntent
													.putExtra(
															AndyConstants.Params.QUOTED_PRICE,
															""
																	+ customfare
																			.getText());
											pushIntent
													.putExtra(
															AndyConstants.Params.REQUEST_ID,
															reqid);
											LocalBroadcastManager.getInstance(
													mapActivity).sendBroadcast(
													pushIntent);
										}
									}
								}
							});
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				mapActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setjobStatus(jobStatus);
					}
				});
			}

			@Override
			public void errorCallback(String channel, PubnubError pbnbError) {
				super.errorCallback(channel, pbnbError);
//				Log.e("MSG", "Error sending message: " + pbnbError);
			}
		};
		pubnub.publish(reqId, message, callback);
	}

	@Override
	public void onErrorResponse(VolleyError arg0) {
		// TODO Auto-generated method stub

	}

}
