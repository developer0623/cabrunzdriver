package com.cabrunzltd.cabrunz.driver;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cabrunzltd.cabrunz.driver.gcm.CommonUtilities;
import com.cabrunzltd.cabrunz.driver.gcm.GCMRegisterHendler;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/////////////////////import com.splunk.mint.Mint;

public class MainActivity extends Activity implements View.OnClickListener {

    private boolean isRecieverRegister = false;
    private static final String TAG = "FirstFragment";
    private PreferenceHelper preferenceHelper;
    // private Animation topToBottomAnimation, bottomToTopAnimation;
    private RelativeLayout rlLoginRegisterLayout;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // private MyFontTextView tvMainBottomView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		/*Mint.initAndStartSession(MainActivity.this, "18c8b221");
		BugSenseHandler.initAndStartSession(MainActivity.this, "0ac4dfbb");*/
       ///////////////////// Mint.initAndStartSession(MainActivity.this, "ee14ee6e");
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        //CustomActivityOnCrash.install(this);

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();

        preferenceHelper = new PreferenceHelper(this);
        if (isExternalStorageWritable() == false) {
            Toast.makeText(getApplicationContext(),
                    "No SD Card Please insert !", Toast.LENGTH_LONG).show();

        } else {
            if (!TextUtils.isEmpty(preferenceHelper.getUserId())) {
                startActivity(new Intent(this, MapActivity.class));
                this.finish();
                return;
            }
        }
        setContentView(R.layout.fragment_main);
        rlLoginRegisterLayout = (RelativeLayout) findViewById(R.id.rlLoginRegisterLayout);
        // tvMainBottomView = (MyFontTextView) mainFragmentView
        // .findViewById(R.id.tvMainBottomView);

        findViewById(R.id.btnFirstSignIn).setOnClickListener(this);
        findViewById(R.id.btnFirstRegister).setOnClickListener(this);

//        if (TextUtils.isEmpty(new PreferenceHelper(MainActivity.this)
//                .getDeviceToken())) {
//            isRecieverRegister = true;
//            registerGcmReceiver(mHandleMessageReceiver);
//        } else {
//
//            AppLog.Log(TAG, "device already registerd with :"
//                    + new PreferenceHelper(MainActivity.this).getDeviceToken());
//        }

        if (TextUtils.isEmpty(new PreferenceHelper(MainActivity.this).getDeviceToken())) {
            isRecieverRegister = true;
            AndyUtils.showCustomProgressDialog(this, "", getResources()
                    .getString(R.string.progress_loading), false);
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    AndyUtils.removeCustomProgressDialog();

                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    boolean sentToken = sharedPreferences
                            .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                    Toast.makeText(MainActivity.this, CommonUtilities.aaa, Toast.LENGTH_SHORT).show();

                    CommonUtilities.displayMessage(context, "Device Registerd");
                    new PreferenceHelper(context).putDeviceToken(CommonUtilities.aaa);
                    if (sentToken) {
                        Toast.makeText(MainActivity.this, "gcm success",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.register_gcm_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };

            registerReceiver();
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        } else {

//            AppLog.Log(TAG, "device already registerd with :"
//                    + new PreferenceHelper(MainActivity.this).getDeviceToken());
        }
        // TODO Auto-generated method stub
    }

    private void registerReceiver(){
        if(isRecieverRegister) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isRecieverRegister = false;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {

                finish();
            }
            return false;
        }
        return true;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isRecieverRegister = true;
        super.onPause();

    }

    public class ShutdownReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(Intent.ACTION_SHUTDOWN.equalsIgnoreCase(intent.getAction())) {
//                Log.d("mahi","Boot Receiver - Shutdown event");
                // database operation
            }
        }
    }

    private BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            AndyUtils.removeCustomProgressDialog();
            if (intent.getAction().equals(
                    CommonUtilities.DISPLAY_MESSAGE_REGISTER)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    int resultCode = bundle.getInt(CommonUtilities.RESULT);
//                    AppLog.Log(TAG, "Result code-----> " + resultCode);
                    if (resultCode == Activity.RESULT_OK) {

                    } else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.register_gcm_failed),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            }
        }
    };

    public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
        if (mHandleMessageReceiver != null) {
            AndyUtils.showCustomProgressDialog(this, "", getResources()
                    .getString(R.string.progress_loading), false);
            new GCMRegisterHendler(MainActivity.this, mHandleMessageReceiver);

        }
    }

    public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
        try{
            if (mHandleMessageReceiver != null) {

                if (mHandleMessageReceiver != null) {
                    unregisterReceiver(mHandleMessageReceiver);
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        Intent startRegisterActivity = new Intent(this,
                RegisterActivity.class);
        switch (v.getId()) {

            case R.id.btnFirstRegister:
                if (!AndyUtils.isNetworkAvailable(MainActivity.this)) {
                    AndyUtils.showToast(
                            getResources().getString(R.string.toast_no_internet),
                            MainActivity.this);
                    return;
                }
                startRegisterActivity.putExtra("isSignin", false);

                break;

            case R.id.btnFirstSignIn:

                startRegisterActivity.putExtra("isSignin", true);

                break;

            default:
                break;
        }
        startActivity(startRegisterActivity);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();

    }



    @Override
    public void onDestroy() {
//        if (isRecieverRegister) {
//           unregisterGcmReceiver(mHandleMessageReceiver);
//            isRecieverRegister = false;
//        }
        super.onDestroy();
    }

}
