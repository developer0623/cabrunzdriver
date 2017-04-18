package com.cabrunzltd.cabrunz.driver.parse;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Hardik A Bhalodi
 */
public class MultiPartRequester {

	private Map<String, String> map;
	private AsyncTaskCompleteListener mAsynclistener;
	private int serviceCode;
	private HttpClient httpclient;
	private Activity activity;
	private AsyncHttpRequest request;
	private static final String TAG = "MultiPartRequester";

	public MultiPartRequester(Activity activity, Map<String, String> map,
			int serviceCode, AsyncTaskCompleteListener asyncTaskCompleteListener) {
		this.map = map;
		this.serviceCode = serviceCode;
		this.activity = activity;

		// is Internet Connection Available...

		if (AndyUtils.isNetworkAvailable(activity)) {
			mAsynclistener = (AsyncTaskCompleteListener) asyncTaskCompleteListener;
			request = (AsyncHttpRequest) new AsyncHttpRequest().execute(map
					.get("url"));
		} else {
			AndyUtils.showToast(
					activity.getResources().getString(
							R.string.toast_no_internet), activity);
		}

	}

	class AsyncHttpRequest extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			map.remove("url");
			try {

				HttpPost httppost = new HttpPost(urls[0]);
				httpclient = new DefaultHttpClient();

				HttpConnectionParams.setConnectionTimeout(
						httpclient.getParams(), 600000);

				MultipartEntityBuilder builder = MultipartEntityBuilder
						.create();
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(15);
				for (String key : map.keySet()) {

//					if (key.equalsIgnoreCase(AndyConstants.Params.PICTURE)) {
//						File f = new File(map.get(key));
//
//						builder.addBinaryBody(key, f,
//								ContentType.MULTIPART_FORM_DATA, f.getName());
//					} else {
//						builder.addTextBody(key, map.get(key), ContentType
//								.create("text/plain", MIME.UTF8_CHARSET));
//					}
					nameValuePairs.add(new BasicNameValuePair(key, map.get(key)));
//					AppLog.Log(TAG, key + "---->" + map.get(key));

				}
//				httppost.setEntity(builder.build());
				try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ActivityManager manager = (ActivityManager) activity
						.getSystemService(Context.ACTIVITY_SERVICE);

				if (manager.getMemoryClass() < 25) {
					System.gc();
				}
				HttpResponse response = httpclient.execute(httppost);

				String responseBody = EntityUtils.toString(
						response.getEntity(), "UTF-8");

				return responseBody;

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
		protected void onPostExecute(String response) {

			if (mAsynclistener != null) {
				mAsynclistener.onTaskCompleted(response, serviceCode);
			}

		}
	}

	// private void showToast(String msg) {
	// Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	// }

	public void cancelTask() {

		request.cancel(true);
//		AppLog.Log(TAG, "task is canelled");

	}
}
