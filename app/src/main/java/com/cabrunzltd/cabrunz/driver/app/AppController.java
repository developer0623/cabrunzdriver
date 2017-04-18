package com.cabrunzltd.cabrunz.driver.app;

import java.util.ArrayList;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.cabrunzltd.cabrunz.driver.MapActivity;


/**
 * Created by Amal on 28-06-2015.
 */
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();
    private static double total=0;
    public static ArrayList<Menu> menuListGlobal = new ArrayList<Menu>();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static boolean isInBackground;
    public static boolean isAwakeFromBackground;
    private static final int backgroundAllowance = 10000;
    //LruBitmapCache mLruBitmapCache;

    private static AppController mInstance;
    private static Context context;

    public static double getTotal() {
        return total;
    }

    public static void setTotal(double total) {
        AppController.total = total;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = AppController.this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        

        return this.mImageLoader;
    }

    

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    public static void activityPaused() {
        isInBackground = true;
        isAwakeFromBackground = true;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInBackground) {
                    isAwakeFromBackground = true;
                }
            }
        }, backgroundAllowance);
        Log.v("activity status", "activityPaused");
    }

    public static void activityResumed() {
        isInBackground = false;
        if(isAwakeFromBackground){
            // do something when awake from background
            Log.v("mahi", "isAwakeFromBackground");          
   			 
        }
        isAwakeFromBackground = false;
        Log.v("activity status", "activityResumed");
    }
}

