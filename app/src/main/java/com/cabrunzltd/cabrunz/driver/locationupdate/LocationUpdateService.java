package com.cabrunzltd.cabrunz.driver.locationupdate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.BasicResponseHandler;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.cabrunzltd.cabrunz.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;
import com.google.android.gms.maps.model.LatLng;

public class LocationUpdateService extends IntentService {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private HttpClient httpclient;
    private PreferenceHelper preferenceHelper;
    private LocationHelper locationHelper;
    private String id, token, latitude, longitude;
    boolean isApiCalled = false;


    public LocationUpdateService() {
        this("MySendLocationService");
    }

    public LocationUpdateService(String name) {
        super("MySendLocationService");
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
//            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
//			Log.e(TAG, "onLocationChanged: " + location);
//            AppLog.Log("TAG", "onLocationReceived Lat : " + location.getLatitude()
//                    + " , long : " + location.getLongitude());
            if (location != null) {
                preferenceHelper
                        .putWalkerLatitude(String.valueOf(location.getLatitude()));
                preferenceHelper.putWalkerLongitude(String
                        .valueOf(location.getLongitude()));
            }
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)
                    && location != null) {

                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                if (!AndyUtils.isNetworkAvailable(getApplicationContext())) {
                    // AndyUtils.showToast(
                    // getResources().getString(R.string.toast_no_internet),
                    // getApplicationContext());
                    return;
                }

                if (!isApiCalled) {
                    isApiCalled = true;

                    if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST && preferenceHelper.getActive().equals("1")) {
                        new UploadDataToServer().execute();
                    } else {
                        new UploadTripLocationData().execute();
                    }
                }

            }

        }

        @Override
        public void onProviderDisabled(String provider) {
//            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
//            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(TAG, "onStartCommand");
//		super.onStartCommand(intent, flags, startId);
        preferenceHelper = new PreferenceHelper(getApplicationContext());
        id = preferenceHelper.getUserId();
        token = preferenceHelper.getSessionToken();
//		 if (driverId.equals("")) {
//		 driverId = getDriverID();
//		 }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
//        Log.e(TAG, "onCreate");
        initializeLocationManager();
        int repeatTime = 5;
        AlarmManager processTimer = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ProcessTimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        processTimer.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), repeatTime*1000, pendingIntent);

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, 0,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, 0,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
//        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
//					Log.i(TAG, "fail to remove location listners, ignore", ex);
				}
			}
		}
	}

	private void initializeLocationManager() {
//		Log.e(TAG, "initializeLocationManager");
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

		}
	}







	private class UploadDataToServer extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION);
				HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 100000);
				HttpConnectionParams.setSoTimeout(httpclient.getParams(), 100000);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.ID, id));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.TOKEN, token));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LATITUDE, latitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LONGITUDE, longitude));
				// nameValuePairs.add(new BasicNameValuePair(
				// AndyConstants.Params.DISTANCE, 0 + ""));


//				AppLog.Log("ID", id);
//				AppLog.Log("Token", token);
//				AppLog.Log("Latitude", latitude);
//				AppLog.Log("Longitude", longitude);
				try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}


				HttpResponse response11 = httpclient.execute(httppost);
				String response = EntityUtils.toString(response11.getEntity(), "UTF-8");

				return response;
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError oume) {
                System.gc();
                // Toast.makeText(
                // activity.getParent().getParent(),
                // "Run out of memory please colse the other background apps and try again!",
                // Toast.LENGTH_LONG).show();
            } finally {
                if (httpclient != null)
                    httpclient.getConnectionManager().shutdown();

            }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			isApiCalled = false;
//			stopSelf();
		}
	}

	private class UploadTripLocationData extends
			AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(AndyConstants.ServiceType.REQUEST_LOCATION_UPDATE);
				HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 100000);
//				HttpConnectionParams.setSoTimeout(httpclient.getParams(), 100000);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.ID, id));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.TOKEN, token));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LATITUDE, latitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LONGITUDE, longitude));
				// nameValuePairs.add(new BasicNameValuePair(
				// AndyConstants.Params.DISTANCE, 0 + ""));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.REQUEST_ID, preferenceHelper
						.getRequestId() + ""));

//				AppLog.Log("ID", id);
//				AppLog.Log("Token", token);
//				AppLog.Log("Latitude", latitude);
//				AppLog.Log("Longitude", longitude);
				try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

//				AppLog.Log("Request id", "" + preferenceHelper.getRequestId());



				HttpResponse response11 = httpclient.execute(httppost);
				String response = EntityUtils.toString(response11.getEntity());
				// String response = httpRequest.postData(
				// AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION,
				// nameValuePairs);
				// response false
				// Log.e("TAG", "request location send Response:::"
				// + response);
				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getBoolean("success")) {
					isApiCalled = false;
					preferenceHelper.putDistance(Float.parseFloat(jsonObject
							.getString(AndyConstants.Params.DISTANCE)));
					preferenceHelper.putUnit(jsonObject.getString("unit"));

				}

				return response;
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError oume) {
                System.gc();
                // Toast.makeText(
                // activity.getParent().getParent(),
                // "Run out of memory please colse the other background apps and try again!",
                // Toast.LENGTH_LONG).show();
            } finally {
                if (httpclient != null)
                    httpclient.getConnectionManager().shutdown();

            }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
//			stopSelf();
		}
	}
}
