package com.cabrunzltd.cabrunz.driver.utills;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cabrunzltd.cabrunz.driver.db.DBHelper;
import com.google.android.gms.maps.model.LatLng;

public class PreferenceHelper {

	private SharedPreferences app_prefs;
	private final String USER_ID = "user_id";
	private final String DEVICE_TOKEN = "device_token";
	private final String SESSION_TOKEN = "session_token";
	private final String REQUEST_ID = "request_id";
	private final String WALKER_LATITUDE = "latitude";
	private final String WALKER_LONGITUDE = "longitude";
	private final String PASSWORD = "password";
	private final String EMAIL = "email";
	private final String LOGIN_BY = "login_by";
	private final String SOCIAL_ID = "social_id";
	private final String OWNER_ID = "owner_id";
	private final String REQUEST_TIME = "request_time";
	private final String TRIP_START = "trip_start";
	private final String DISTANCE = "distance";
	private final String UNIT = "unit";
	private final String CURRENT_FRAGMENT = "currentFragment";
	private final String APPROVED = "is_approved";
	private final String active = "is_active";
	private final String AVAIL_ON = "availability_on";
	
	private final String S_LATITUDE = "s_lattidue";
	private final String S_LONGITUDE = "s_longitude";
	
	private final String D_LATITUDE = "d_lattidue";
	private final String D_LONGITUDE = "d_longitude";
	
	private final String DRIV_LATITUDE = "drive_lattidue";
	private final String DRIV_LONGITUDE = "drive_longitude";
	private final String is_visible = "is_visible";
	
	
	private Context context;

	public PreferenceHelper(Context context) {
		app_prefs = context.getSharedPreferences(AndyConstants.PREF_NAME,
				Context.MODE_PRIVATE);
		this.context = context;
	}

	public void putUserId(String userId) {
		Editor edit = app_prefs.edit();
		edit.putString(USER_ID, userId);
		edit.commit();
	}

	public String getUserId() {
		return app_prefs.getString(USER_ID, null);

	}

	public void putDeviceToken(String deviceToken) {
		Editor edit = app_prefs.edit();
		edit.putString(DEVICE_TOKEN, deviceToken);
		edit.commit();
	}

	public String getDeviceToken() {
		return app_prefs.getString(DEVICE_TOKEN, null);
	}
	
	public void putIs_Visible(String is_visible) {
		Editor edit = app_prefs.edit();
		edit.putString(is_visible, is_visible);
		edit.commit();
	}

	public String getIs_Visible() {
		return app_prefs.getString(is_visible, "0");
	}


	public void putOwner_ID(String ownerid) {
		Editor edit = app_prefs.edit();
		edit.putString(OWNER_ID, String.valueOf(ownerid));
		edit.commit();
	}

	public String getClientId() {
		return app_prefs.getString(OWNER_ID, null);

	}







	public void putS_Location(LatLng latLang) {
		Editor edit = app_prefs.edit();
		edit.putString(S_LATITUDE, String.valueOf(latLang.latitude));
		edit.putString(S_LONGITUDE, String.valueOf(latLang.longitude));
		edit.commit();
	}

	public LatLng getS_Location() {
		LatLng latLng = new LatLng(0.0, 0.0);
		try {
			latLng = new LatLng(Double.parseDouble(app_prefs.getString(
					S_LATITUDE, "0.0")), Double.parseDouble(app_prefs
					.getString(S_LONGITUDE, "0.0")));
		} catch (NumberFormatException nfe) {
			latLng = new LatLng(0.0, 0.0);
		}
		return latLng;
	}
	
	
	public void putD_Location(LatLng latLang) {
		Editor edit = app_prefs.edit();
		edit.putString(D_LATITUDE, String.valueOf(latLang.latitude));
		edit.putString(D_LONGITUDE, String.valueOf(latLang.longitude));
		edit.commit();
	}

	public LatLng getD_Location() {
		LatLng latLng = new LatLng(0.0, 0.0);
		try {
			latLng = new LatLng(Double.parseDouble(app_prefs.getString(
					D_LATITUDE, "0.0")), Double.parseDouble(app_prefs
					.getString(D_LONGITUDE, "0.0")));
		} catch (NumberFormatException nfe) {
			latLng = new LatLng(0.0, 0.0);
		}
		return latLng;
	}
	

	public void putDrive_Location(LatLng latLang) {
		Editor edit = app_prefs.edit();
		edit.putString(DRIV_LATITUDE, String.valueOf(latLang.latitude));
		edit.putString(DRIV_LONGITUDE, String.valueOf(latLang.longitude));
		edit.commit();
	}

	public LatLng getDrive_Location() {
		LatLng latLng = new LatLng(0.0, 0.0);
		try {
			latLng = new LatLng(Double.parseDouble(app_prefs.getString(
					DRIV_LATITUDE, "0.0")), Double.parseDouble(app_prefs
					.getString(DRIV_LONGITUDE, "0.0")));
		} catch (NumberFormatException nfe) {
			latLng = new LatLng(0.0, 0.0);
		}
		return latLng;
	}

	public void putCurrentFragment(String currentFragment) {
		Editor edit = app_prefs.edit();
		edit.putString(CURRENT_FRAGMENT, currentFragment);
		edit.commit();
	}

	public String getCurrentFragment() {
		return app_prefs.getString(CURRENT_FRAGMENT, "");
	}
	
	public void putActive(String is_active) {
		Editor edit = app_prefs.edit();
		edit.putString(active, is_active);
		edit.commit();
	}

	public String getActive() {
		return app_prefs.getString(active, "");
	}
	
	public void putApproved(String is_approved) {
		Editor edit = app_prefs.edit();
		edit.putString(APPROVED, is_approved);
		edit.commit();
	}

	public String getApproved() {
		return app_prefs.getString(APPROVED, null);
	}
	
	public void putAvail_on(String availability_on) {
		Editor edit = app_prefs.edit();
		edit.putString(AVAIL_ON, availability_on);
		edit.commit();
	}

	public String getAvail_on() {
		return app_prefs.getString(AVAIL_ON, "0");
	}

	public void putSessionToken(String sessionToken) {
		Editor edit = app_prefs.edit();
		edit.putString(SESSION_TOKEN, sessionToken);
		edit.commit();
	}

	public String getSessionToken() {
		return app_prefs.getString(SESSION_TOKEN, null);
	}

	public void putRequestId(int reqId) {
		Editor edit = app_prefs.edit();
		edit.putInt(REQUEST_ID, reqId);
		edit.commit();
	}

	public int getRequestId() {
		return app_prefs.getInt(REQUEST_ID, AndyConstants.NO_REQUEST);
	}

	public void putDistance(Float distance) {
		Editor edit = app_prefs.edit();
		edit.putFloat(DISTANCE, distance);
		edit.commit();
	}

	public float getDistance() {
		return app_prefs.getFloat(DISTANCE, 0.0f);
	}

	public void putUnit(String unit) {
		Editor edit = app_prefs.edit();
		edit.putString(UNIT, unit);
		edit.commit();
	}

	public String getUnit() {
		return app_prefs.getString(UNIT, " ");
	}

	public void putIsTripStart(boolean status) {
		Editor edit = app_prefs.edit();
		edit.putBoolean(TRIP_START, status);
		edit.commit();
	}

	public boolean getIsTripStart() {
		return app_prefs.getBoolean(TRIP_START, false);
	}

	public void putWalkerLatitude(String latitude) {
		Editor edit = app_prefs.edit();
		edit.putString(WALKER_LATITUDE, latitude);
		edit.commit();
	}

	public String getWalkerLatitude() {
		return app_prefs.getString(WALKER_LATITUDE, null);
	}

	public void putWalkerLongitude(String longitude) {
		Editor edit = app_prefs.edit();
		edit.putString(WALKER_LONGITUDE, longitude);
		edit.commit();
	}

	public String getWalkerLongitude() {
		return app_prefs.getString(WALKER_LONGITUDE, null);
	}

	public void putEmail(String email) {
		try{
		Editor edit = app_prefs.edit();
		edit.putString(EMAIL, email);
		edit.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String getEmail() {
		return app_prefs.getString(EMAIL, null);
	}

	public void putPassword(String password) {
		Editor edit = app_prefs.edit();
		edit.putString(PASSWORD, password);
		edit.commit();
	}

	public String getPassword() {
		return app_prefs.getString(PASSWORD, null);
	}

	public void putLoginBy(String loginBy) {
		Editor edit = app_prefs.edit();
		edit.putString(LOGIN_BY, loginBy);
		edit.commit();
	}

	public String getLoginBy() {
		return app_prefs.getString(LOGIN_BY, AndyConstants.MANUAL);
	}

	public void putSocialId(String socialId) {
		Editor edit = app_prefs.edit();
		edit.putString(SOCIAL_ID, socialId);
		edit.commit();
	}

	public String getSocialId() {
		return app_prefs.getString(SOCIAL_ID, null);
	}

	public void putRequestTime(long time) {
		Editor edit = app_prefs.edit();
		edit.putLong(REQUEST_TIME, time);
		edit.commit();
	}

	public long getRequestTime() {
		return app_prefs.getLong(REQUEST_TIME, AndyConstants.NO_TIME);
	}

	public void clearRequestData() {
		putRequestId(AndyConstants.NO_REQUEST);
		putRequestTime(AndyConstants.NO_TIME);
		putDistance(AndyConstants.NO_DISTANCE);
		
		putIsTripStart(false);
		// new DBHelper(context).deleteAllLocations();
	}

	public void Logout() {
		clearRequestData();
		putUserId(null);
		putSessionToken(null);
		putLoginBy(AndyConstants.MANUAL);
		putSocialId(null);
		putApproved(null);
		new DBHelper(context).deleteUser();

	}

}
