package com.cabrunzltd.cabrunz.driver.locationupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by future on 2/3/17.
 */

public class ProcessTimerReceiver extends BroadcastReceiver{
    private PreferenceHelper preferenceHelper;
    private HttpClient httpclient;
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e(TAG, "locationupdate");

//        if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST && preferenceHelper.getActive().equals("1")) {
//            new UploadDataToServer().execute();
//        } else {
//            new UploadTripLocationData().execute();
//        }
    }


//    private class UploadDataToServer extends AsyncTask<String, Void, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//                httpclient = new DefaultHttpClient();
//                HttpPost httppost = new HttpPost(
//                        AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION);
//                HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 100000);
//                HttpConnectionParams.setSoTimeout(httpclient.getParams(), 100000);
//
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.ID, id));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.TOKEN, token));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.LATITUDE, latitude));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.LONGITUDE, longitude));
//                // nameValuePairs.add(new BasicNameValuePair(
//                // AndyConstants.Params.DISTANCE, 0 + ""));
//
//
//                AppLog.Log("ID", id);
//                AppLog.Log("Token", token);
//                AppLog.Log("Latitude", latitude);
//                AppLog.Log("Longitude", longitude);
//                try {
//                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//
//                HttpResponse response11 = httpclient.execute(httppost);
//                String response = EntityUtils.toString(response11.getEntity(), "UTF-8");
//
//                return response;
//            } catch (Exception e) {
//                e.printStackTrace();
//            } catch (OutOfMemoryError oume) {
//                System.gc();
//                // Toast.makeText(
//                // activity.getParent().getParent(),
//                // "Run out of memory please colse the other background apps and try again!",
//                // Toast.LENGTH_LONG).show();
//            } finally {
//                if (httpclient != null)
//                    httpclient.getConnectionManager().shutdown();
//
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//            isApiCalled = false;
////			stopSelf();
//        }
//    }
//
//    private class UploadTripLocationData extends
//            AsyncTask<String, String, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//                httpclient = new DefaultHttpClient();
//                HttpPost httppost = new HttpPost(AndyConstants.ServiceType.REQUEST_LOCATION_UPDATE);
//                HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 100000);
////				HttpConnectionParams.setSoTimeout(httpclient.getParams(), 100000);
//
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.ID, id));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.TOKEN, token));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.LATITUDE, latitude));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.LONGITUDE, longitude));
//                // nameValuePairs.add(new BasicNameValuePair(
//                // AndyConstants.Params.DISTANCE, 0 + ""));
//                nameValuePairs.add(new BasicNameValuePair(
//                        AndyConstants.Params.REQUEST_ID, preferenceHelper
//                        .getRequestId() + ""));
//
//                AppLog.Log("ID", id);
//                AppLog.Log("Token", token);
//                AppLog.Log("Latitude", latitude);
//                AppLog.Log("Longitude", longitude);
//                try {
//                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                AppLog.Log("Request id", "" + preferenceHelper.getRequestId());
//
//
//
//                HttpResponse response11 = httpclient.execute(httppost);
//                String response = EntityUtils.toString(response11.getEntity());
//                // String response = httpRequest.postData(
//                // AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION,
//                // nameValuePairs);
//                // response false
//                // Log.e("TAG", "request location send Response:::"
//                // + response);
//                JSONObject jsonObject = new JSONObject(response);
//                if (jsonObject.getBoolean("success")) {
//                    isApiCalled = false;
//                    preferenceHelper.putDistance(Float.parseFloat(jsonObject
//                            .getString(AndyConstants.Params.DISTANCE)));
//                    preferenceHelper.putUnit(jsonObject.getString("unit"));
//
//                }
//
//                return response;
//            } catch (Exception e) {
//                e.printStackTrace();
//            } catch (OutOfMemoryError oume) {
//                System.gc();
//                // Toast.makeText(
//                // activity.getParent().getParent(),
//                // "Run out of memory please colse the other background apps and try again!",
//                // Toast.LENGTH_LONG).show();
//            } finally {
//                if (httpclient != null)
//                    httpclient.getConnectionManager().shutdown();
//
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
////			stopSelf();
//        }
//    }
}
