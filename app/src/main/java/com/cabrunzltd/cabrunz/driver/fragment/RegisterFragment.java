package com.cabrunzltd.cabrunz.driver.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.cabrunzltd.cabrunz.driver.MapActivity;
import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.adapter.VehicalTypeListAdapter;
import com.cabrunzltd.cabrunz.driver.base.BaseRegisterFragment;
import com.cabrunzltd.cabrunz.driver.model.VehicalType;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.parse.MultiPartRequester;
import com.cabrunzltd.cabrunz.driver.parse.ParseContent;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;
import com.cabrunzltd.cabrunz.driver.widget.MyFontEdittextView;
import com.cabrunzltd.cabrunz.driver.widget.MyFontTextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.soundcloud.android.crop.Crop;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Kishan H Dhamat
 * 
 */
public class RegisterFragment extends BaseRegisterFragment implements
		OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		AsyncTaskCompleteListener {
	private MyFontEdittextView etRegisterFname, etRegisterLName,
			etRegisterPassword, etRegisterEmail, etRegisterNumber,
			etRegisterAddress, etRegisterBio, etRegisterZipcode,
			etRegisterModel, etRegisterCarno;
	private MyFontTextView tvCountryCode;
	private ImageButton btnFb, btnGplus;
	private GridView gvTypes;
	private ImageView ivProfile;
	private boolean mSignInClicked, mIntentInProgress;
	private ConnectionResult mConnectionResult;
	private GoogleApiClient mGoogleApiClient;
	private AQuery aQuery;
	private SimpleFacebook mSimpleFacebook;
	private ParseContent parseContent;
	private ArrayList<String> countryList;
	private String country;
	private Uri uri = null;
	private String profileImageFilePath, loginType = AndyConstants.MANUAL,
			socialId, profileImageData = null, socialProPicUrl;
	private Bitmap profilePicBitmap;
	private String stringimage;
	private String imageext;
	private ImageOptions profileImageOptions;
	private SimpleFacebookConfiguration facebookConfiguration;
	private ArrayList<VehicalType> listType;
	private VehicalTypeListAdapter adapter;

	private final String TAG = "RegisterFragment";
	private static final int RC_SIGN_IN = 0;
	private int selectedTypePostion = -1;
	private MyFontTextView ettimezone;
	String[] timezone_display, timezone_value;
	int timezone_pos = -1;

	private String mCurrentPhotoPath;

	Permission[] facebookPermission = new Permission[] { Permission.EMAIL };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		registerActivity.setTitle("Register");

		View registerFragmentView = inflater.inflate(
				R.layout.fragment_register, container, false);

		ivProfile = (CircularImageView) registerFragmentView
				.findViewById(R.id.ivRegisterProfile);
		etRegisterAddress = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterAddress);
		etRegisterBio = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterBio);
		etRegisterEmail = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterEmail);
		etRegisterFname = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterFName);
		etRegisterLName = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterLName);
		etRegisterNumber = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterNumber);
		etRegisterPassword = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterPassword);
		etRegisterZipcode = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterZipCode);
		etRegisterModel = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegistermodel);
		etRegisterCarno = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegistercar_no);

		tvCountryCode = (MyFontTextView) registerFragmentView
				.findViewById(R.id.tvRegisterCountryCode);
		btnFb = (ImageButton) registerFragmentView
				.findViewById(R.id.btnRegisterFb);
		btnGplus = (ImageButton) registerFragmentView
				.findViewById(R.id.btnRegisterGplus);
		gvTypes = (GridView) registerFragmentView.findViewById(R.id.gvTypes);
		ettimezone = (MyFontTextView) registerFragmentView
				.findViewById(R.id.etRegistertimezone);
		ettimezone.setOnClickListener(this);

		String timezonedata = TimeZone.getDefault().getID();
		for (int i = 0; i < timezone_value.length; i++) {
			if (timezone_value[i].equals(timezonedata)) {
				ettimezone.setText(timezone_display[i]);
				break;
			}
		}

		tvCountryCode.setOnClickListener(this);
		ivProfile.setOnClickListener(this);
		registerFragmentView.findViewById(R.id.btnRegisterFb)
				.setOnClickListener(this);
		registerFragmentView.findViewById(R.id.btnRegisterGplus)
				.setOnClickListener(this);
		registerFragmentView.findViewById(R.id.tvRegisterSubmit)
				.setOnClickListener(this);

		return registerFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerActivity.actionBar.show();
		registerActivity.setActionBarTitle(getResources().getString(
				R.string.text_register));
		registerActivity.setActionBarIcon(R.drawable.back);
		parseContent = new ParseContent(registerActivity);

		aQuery = new AQuery(registerActivity);
		profileImageOptions = new ImageOptions();
		profileImageOptions.fileCache = true;
		profileImageOptions.memCache = true;
		profileImageOptions.targetWidth = 200;
		profileImageOptions.fallback = R.drawable.user;

		listType = new ArrayList<VehicalType>();
		adapter = new VehicalTypeListAdapter(registerActivity, listType);
		gvTypes.setAdapter(adapter);
		gvTypes.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				for (int i = 0; i < listType.size(); i++)
					listType.get(i).isSelected = false;
				listType.get(position).isSelected = true;
				// onItemClick(position);
				selectedTypePostion = position;
				adapter.notifyDataSetChanged();
			}
		});

		countryList = parseContent.parseCountryCodes();
		for (int i = 0; i < countryList.size(); i++) {
			if (countryList.get(i).contains(country)) {
				tvCountryCode.setText((countryList.get(i).substring(0,
						countryList.get(i).indexOf(" "))));
			}
		}
		if (TextUtils.isEmpty(tvCountryCode.getText())) {
			tvCountryCode.setText((countryList.get(0).substring(0, countryList
					.get(0).indexOf(" "))));
		}

		String countryzipcode = GetCountryZipCode();
		if (!countryzipcode.equals("")) {
			tvCountryCode.setText(countryzipcode);
		}

		getVehicalTypes();
		// facebook api initialization
		facebookConfiguration = new SimpleFacebookConfiguration.Builder()
				.setAppId(getResources().getString(R.string.app_id))
				.setNamespace(getResources().getString(R.string.app_name))
				.setPermissions(facebookPermission).build();
		SimpleFacebook.setConfiguration(facebookConfiguration);

		// Google plus api initialization
		Scope scope = new Scope(AndyConstants.GOOGLE_API_SCOPE_URL);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(registerActivity)
				.enableAutoManage(registerActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		country = Locale.getDefault().getDisplayCountry();
		timezone_display = getResources().getStringArray(R.array.time_zone);
		timezone_value = getResources().getStringArray(R.array.time_zone_value);
		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == 0) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
					&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {

			}
		}
	}

	@Override
	public void onClick(View v) {
		socialProPicUrl = null;
		switch (v.getId()) {
		case R.id.btnRegisterFb:
			if (!mSimpleFacebook.isLogin()) {
				registerActivity.setFbTag(AndyConstants.REGISTER_FRAGMENT_TAG);
//				mSimpleFacebook.login(new OnLoginListener() {
//
//					@Override
//					public void onFail(String arg0) {
//						Toast.makeText(
//								registerActivity,
//								getResources().getString(
//										R.string.toast_facebook_login_failed),
//								Toast.LENGTH_SHORT).show();
//					}
//
//					@Override
//					public void onException(Throwable arg0) {
//					}
//
//					@Override
//					public void onThinking() {
//					}
//
//					@Override
//					public void onNotAcceptingPermissions(Type arg0) {
//						AppLog.Log("UBER",
//								String.format(
//										"You didn't accept %s permissions",
//										arg0.name()));
//					}
//
//					@Override
//					public void onLogin() {
//						// Toast.makeText(registerActivity, "Facebook Success",
//						// Toast.LENGTH_SHORT).show();
//					}
//				});
			} else {
				getFbProfile();
			}
			break;

		case R.id.ivRegisterProfile:
			showPictureDialog();
			break;

		case R.id.btnRegisterGplus:
			mSignInClicked = true;
			if (!mGoogleApiClient.isConnecting()) {
				AndyUtils.showCustomProgressDialog(registerActivity, "",
						getString(R.string.progress_getting_info), false);
				mGoogleApiClient.connect();
			}
			break;

		case R.id.tvRegisterSubmit:
			onRegisterButtonClick();
			break;

		case R.id.tvRegisterCountryCode:
			AlertDialog.Builder countryBuilder = new Builder(registerActivity);
			countryBuilder.setTitle(getResources().getString(
					R.string.dialog_title_country_codes));

			final String[] countryListArray = new String[countryList.size()];
			countryList.toArray(countryListArray);
			countryBuilder.setItems(countryListArray,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							tvCountryCode.setText(countryListArray[which]
									.substring(0, countryListArray[which]
											.indexOf(" ")));
						}
					}).show();
			break;

		case R.id.etRegistertimezone:
			AlertDialog.Builder timezonebuilder = new Builder(getActivity());
			timezonebuilder.setTitle("Time Zones");

			timezonebuilder.setItems(timezone_display,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							ettimezone.setText(timezone_display[which]);
							timezone_pos = which;
						}
					}).show();

			break;

		default:
			break;
		}
	}

	private void onRegisterButtonClick() {
		if (etRegisterFname.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_fname),
					registerActivity);
			return;
		} else if (etRegisterLName.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_lname),
					registerActivity);
			return;
		} else if (etRegisterEmail.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_email),
					registerActivity);
			return;
		} else if (!AndyUtils.eMailValidation(etRegisterEmail.getText()
				.toString())) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_valid_email),
					registerActivity);
			return;
		} else if (etRegisterPassword.getVisibility() == View.VISIBLE) {
			if (etRegisterPassword.getText().length() == 0) {
				AndyUtils
						.showToast(
								getResources().getString(
										R.string.error_empty_password),
								registerActivity);
				return;
			} else if (etRegisterPassword.getText().length() < 6) {
				AndyUtils
						.showToast(
								getResources().getString(
										R.string.error_valid_password),
								registerActivity);
				return;
			}
		}

		if (etRegisterPassword.getVisibility() == View.GONE) {
			if (!TextUtils.isEmpty(socialProPicUrl)) {
				profileImageData = null;
				profileImageData = aQuery.getCachedFile(socialProPicUrl)
						.getPath();
			}
		}

		if (etRegisterNumber.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_number),
					registerActivity);
			return;
		} else if (profileImageData == null || profileImageData.equals("")) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_image),
					registerActivity);
			return;
		} else if (selectedTypePostion == -1) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_type),
					registerActivity);
			return;
		} else if (etRegisterModel.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_model),
					registerActivity);
			return;
		} else if (etRegisterCarno.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_carno),
					registerActivity);
			return;
		} else {
			register(loginType, socialId);
		}
	}

	private void showPictureDialog() {
		Builder pictureDialog = new Builder(
				registerActivity);
		pictureDialog.setTitle(getResources().getString(
				R.string.dialog_chhose_photo));
		String[] pictureDialogItems = {
				getResources().getString(R.string.dialog_from_gallery),
				getResources().getString(R.string.dialog_from_camera) };

		pictureDialog.setItems(pictureDialogItems,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {

						case 0:
							choosePhotoFromGallary();
							break;

						case 1:
							takePhotoFromCamera();
							break;

						}
					}
				});
		pictureDialog.show();
	}

	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				registerActivity.startIntentSenderForResult(mConnectionResult
						.getResolution().getIntentSender(), RC_SIGN_IN, null,
						0, 0, 0, AndyConstants.REGISTER_FRAGMENT_TAG);
			} catch (SendIntentException e) {
				/*
				 * The intent was canceled before it was sent. Return to the
				 * default state and attempt to connect to get an updated
				 * ConnectionResult.
				 */
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			mConnectionResult = result;
			if (mSignInClicked) {
				resolveSignInError();
			}
		}

	}

	private void getFbProfile() {
		AndyUtils.showCustomProgressDialog(registerActivity, "",
				getString(R.string.text_getting_info_facebook), true);
		mSimpleFacebook.getProfile(new OnProfileListener() {
			@Override
			public void onComplete(Profile profile) {
				AndyUtils.removeCustomProgressDialog();
//				AppLog.Log("Uber", "My profile id = " + profile.getId());
				btnFb.setEnabled(false);
				btnGplus.setEnabled(false);
				etRegisterEmail.setText(profile.getEmail());
				etRegisterFname.setText(profile.getFirstName());
				etRegisterLName.setText(profile.getLastName());
				socialId = profile.getId();
				loginType = AndyConstants.SOCIAL_FACEBOOK;
				// etRegisterPassword.setEnabled(false);
				etRegisterPassword.setVisibility(View.GONE);

				if (!TextUtils.isEmpty(profile.getPicture())
						|| !profile.getPicture().equalsIgnoreCase("null")) {
					socialProPicUrl = profile.getPicture();
					aQuery.id(ivProfile).image(profile.getPicture(),
							getAqueryOption());
				} else {
					socialProPicUrl = null;
				}

			}
		});
	}

	private ImageOptions getAqueryOption() {
		ImageOptions options = new ImageOptions();
		options.targetWidth = 200;
		options.memCache = true;
		options.fallback = R.drawable.default_user;
		options.fileCache = true;
		return options;
	}

	public void onItemClick(int pos) {
		selectedTypePostion = pos;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

//		mSignInClicked = false;
//		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SIGN_IN) {

			if (resultCode != registerActivity.RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		} else if (requestCode == AndyConstants.CHOOSE_PHOTO) {
			if (data != null) {

				Uri contentURI = data.getData();
				registerActivity.setFbTag(AndyConstants.REGISTER_FRAGMENT_TAG);
//				AppLog.Log(TAG, "Choose photo on activity result");
				beginCrop(contentURI);
				// profileImageData = getRealPathFromURI(contentURI);
				// aQuery.id(ivProfile).image(profileImageData);
			}

		} else if (requestCode == AndyConstants.TAKE_PHOTO) {
			registerActivity.setFbTag(AndyConstants.REGISTER_FRAGMENT_TAG);
			beginCrop(Uri.parse(mCurrentPhotoPath));


		} else if (requestCode == Crop.REQUEST_CROP) {
//			AppLog.Log(TAG, "Crop photo on activity result");
			if (data != null)
				handleCrop(resultCode, data);
		} else {
			mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
			if (mSimpleFacebook.isLogin()) {
				getFbProfile();
			} else {
				Toast.makeText(
						registerActivity,
						registerActivity.getResources().getString(
								R.string.toast_facebook_login_failed),
						Toast.LENGTH_SHORT).show();
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void choosePhotoFromGallary() {

		// Intent intent = new Intent();
		// intent.setType("image/*");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, 112);
//		startActivityForResult(galleryIntent, AndyConstants.CHOOSE_PHOTO,
//						AndyConstants.REGISTER_FRAGMENT_TAG);

//		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		registerActivity.startActivityForResult(i, 112);

	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,  // prefix
				".jpg",         // suffix
				storageDir      // directory
		);


		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		return image;
	}

	private void takePhotoFromCamera() {

		File photoFile = null;
		try {
			photoFile = createImageFile();
		} catch (IOException ex) {
			// Error occurred while creating the File
//			Log.i(TAG, "IOException");
		}
		// Continue only if the File was successfully created
		if (photoFile != null) {

			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
			startActivityForResult(i, 113);
		}
	}

//	private void takePhotoFromCamera() {
//		Calendar cal = Calendar.getInstance();
//		File file = new File(Environment.getExternalStorageDirectory(),
//				(cal.getTimeInMillis() + ".jpg"));
//
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//
//			file.delete();
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		uri = Uri.fromFile(file);
//		Intent cameraIntent = new Intent(
//				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//		registerActivity.startActivityForResult(cameraIntent,
//				AndyConstants.TAKE_PHOTO, AndyConstants.REGISTER_FRAGMENT_TAG);
//	}

	@Override
	public void onConnected(Bundle arg0) {
//		AndyUtils.removeCustomProgressDialog();
//		String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
//		Person currentPerson = Plus.PeopleApi
//				.getCurrentPerson(mGoogleApiClient);
//
//		String personName = currentPerson.getDisplayName();
//		String personPhoto = currentPerson.getImage().toString();
//
//		btnGplus.setEnabled(false);
//		btnFb.setEnabled(false);
//		etRegisterEmail.setText(email);
//		if (personName.contains(" ")) {
//			String[] split = personName.split(" ");
//			etRegisterFname.setText(split[0]);
//			etRegisterLName.setText(split[1]);
//		} else {
//			etRegisterFname.setText(personName);
//		}
//
//		// etRegisterPassword.setEnabled(false);
//		etRegisterPassword.setVisibility(View.GONE);
//		if (!TextUtils.isEmpty(personPhoto)
//				|| !personPhoto.equalsIgnoreCase("null")) {
//			socialProPicUrl = personPhoto;
//			aQuery.id(ivProfile).image(personPhoto, profileImageOptions);
//		} else {
//			socialProPicUrl = null;
//		}
//
//		socialId = currentPerson.getId();
//		loginType = AndyConstants.SOCIAL_GOOGLE;
//		// etRegisterPassword.setEnabled(false);
//		etRegisterPassword.setVisibility(View.GONE);
	}

	private void register(String type, String id) {

		if (!AndyUtils.isNetworkAvailable(registerActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					registerActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_dialog_register), false);

		if (type.equals(AndyConstants.MANUAL)) {
//			AppLog.Log(TAG, "Simple Register method");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(AndyConstants.URL, AndyConstants.ServiceType.REGISTER);
			map.put(AndyConstants.Params.FIRSTNAME, etRegisterFname.getText()
					.toString());
			map.put(AndyConstants.Params.LAST_NAME, etRegisterLName.getText()
					.toString());
			map.put(AndyConstants.Params.EMAIL, etRegisterEmail.getText()
					.toString());
			map.put(AndyConstants.Params.PASSWORD, etRegisterPassword.getText()
					.toString());
			map.put(AndyConstants.Params.PICTURE, stringimage);
			map.put(AndyConstants.Params.PICTUREEXT, imageext);
			map.put(AndyConstants.Params.PHONE, tvCountryCode.getText()
					.toString().trim()
					+ etRegisterNumber.getText().toString());
			map.put(AndyConstants.Params.BIO, etRegisterBio.getText()
					.toString());
			map.put(AndyConstants.Params.ADDRESS, etRegisterAddress.getText()
					.toString());
			map.put(AndyConstants.Params.STATE, "");
			map.put(AndyConstants.Params.COUNTRY, "");

			map.put("model_no", etRegisterModel.getText().toString());
			map.put("vehicle_no", etRegisterCarno.getText().toString());
			map.put(AndyConstants.Params.ZIPCODE, etRegisterZipcode.getText()
					.toString().trim());
			map.put(AndyConstants.Params.TYPE,
					String.valueOf(listType.get(selectedTypePostion).getId()));
			map.put(AndyConstants.Params.DEVICE_TYPE,
					AndyConstants.DEVICE_TYPE_ANDROID);
			map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
					registerActivity).getDeviceToken());
			map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);
			if (!TextUtils.isEmpty(ettimezone.getText()) && timezone_pos != -1)
				map.put(AndyConstants.Params.TIMEZONE,
						timezone_value[timezone_pos]);

			new MultiPartRequester(registerActivity, map,
					AndyConstants.ServiceCode.REGISTER, this);
		} else {
			registerSoicial(id, type);
		}
	}

	private void registerSoicial(String id, String loginType) {
//		AppLog.Log(TAG, "Register social method");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.REGISTER);
		map.put(AndyConstants.Params.FIRSTNAME, etRegisterFname.getText()
				.toString());
		map.put(AndyConstants.Params.LAST_NAME, etRegisterLName.getText()
				.toString());
		map.put(AndyConstants.Params.ADDRESS, etRegisterAddress.getText()
				.toString());
		map.put(AndyConstants.Params.EMAIL, etRegisterEmail.getText()
				.toString());
		map.put(AndyConstants.Params.PHONE, tvCountryCode.getText().toString()
				.trim()
				+ etRegisterNumber.getText().toString());
		map.put(AndyConstants.Params.PICTURE, profileImageData);
		map.put(AndyConstants.Params.STATE, "");
		map.put(AndyConstants.Params.TYPE,
				String.valueOf(listType.get(selectedTypePostion).getId()));
		map.put(AndyConstants.Params.COUNTRY, "");
		map.put(AndyConstants.Params.BIO, etRegisterBio.getText().toString());
		map.put(AndyConstants.Params.DEVICE_TYPE,
				AndyConstants.DEVICE_TYPE_ANDROID);
		map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
				registerActivity).getDeviceToken());
		map.put(AndyConstants.Params.ZIPCODE, etRegisterZipcode.getText()
				.toString().trim());
		map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
		map.put(AndyConstants.Params.LOGIN_BY, loginType);
		if (!TextUtils.isEmpty(ettimezone.getText()) && timezone_pos != -1)
			map.put(AndyConstants.Params.TIMEZONE, timezone_value[timezone_pos]);
		new MultiPartRequester(registerActivity, map,
				AndyConstants.ServiceCode.REGISTER, this);

	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();

		switch (serviceCode) {
		case AndyConstants.ServiceCode.GET_VEHICAL_TYPES:
//			AppLog.Log(TAG, "Vehicle types  " + response);
			if (parseContent.isSuccess(response)) {
				parseContent.parseTypes(response, listType);
				adapter.notifyDataSetChanged();
			}
			AndyUtils.removeCustomProgressDialog();
			break;

		case AndyConstants.ServiceCode.REGISTER:
//			AppLog.Log(TAG, "Register response :" + response);
			if (response != null)
//				if (parseContent.isSuccess(response)) {
//					AndyUtils.showToast(registerActivity.getResources()
//							.getString(R.string.toast_register_success),
//							registerActivity);
//					 parseContent.parseUserAndStoreToDb(response);
//					 new PreferenceHelper(getActivity())
//					.putPassword(etRegisterPassword.getText().toString());
//					registerActivity.addFragment(new LoginFragment(), false,
//							AndyConstants.LOGIN_FRAGMENT_TAG, false);
//				}
				if (parseContent.isSuccessWithId(response)) {
					int is_approved = 0;
					try {
						JSONObject obj = new JSONObject(response);
						is_approved = obj.getInt("is_approved");

					} catch (JSONException e) {
						e.printStackTrace();
					}

					Bundle bun = new Bundle();
					bun.putInt("approved", is_approved);
					parseContent.parseUserAndStoreToDb(response);
					new PreferenceHelper(getActivity()).putPassword(etRegisterPassword.getText().toString());
					new PreferenceHelper(getActivity()).putApproved(String
							.valueOf(is_approved));
					Intent intent = new Intent(registerActivity, MapActivity.class);
					intent.putExtras(bun);
					startActivity(intent);
					registerActivity.finish();
				}
			break;

		default:
			break;
		}

	}

	private String getRealPathFromURI(Uri contentURI) {
		String result;
		Cursor cursor = registerActivity.getContentResolver().query(contentURI,
				null, null, null, null);

		if (cursor == null) { // Source is Dropbox or other similar local file
								// path
			result = contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			result = cursor.getString(idx);
			cursor.close();
		}
		return result;
	}

	public static Bitmap getRotatedBitmap(File file) {
		Bitmap bitmap = null;
		try {
			File f = file;
			ExifInterface exif = new ExifInterface(f.getPath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			int angle = 0;

			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				angle = 90;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				angle = 180;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				angle = 270;
			}

			Matrix mat = new Matrix();
			mat.postRotate(angle);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;

			Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f),
					null, options);
			bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), mat, true);
			ByteArrayOutputStream outstudentstreamOutputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 20,
					outstudentstreamOutputStream);

		} catch (IOException e) {
//			Log.w("TAG", "-- Error in setting image");
		} catch (OutOfMemoryError oom) {
//			Log.w("TAG", "-- OOM Error in setting image");
		}
		return bitmap;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AndyUtils.removeCustomProgressDialog();
	}

	private void getVehicalTypes() {
		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_getting_types), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.GET_VEHICAL_TYPES);
//		AppLog.Log(TAG, AndyConstants.URL);
		new HttpRequester(registerActivity, map,
				AndyConstants.ServiceCode.GET_VEHICAL_TYPES, true, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerActivity.currentFragment = AndyConstants.REGISTER_FRAGMENT_TAG;
		new PreferenceHelper(registerActivity).putCurrentFragment(AndyConstants.REGISTER_FRAGMENT_TAG);
		mSimpleFacebook = SimpleFacebook.getInstance(registerActivity);
	}

	private void beginCrop(Uri source) {
		// Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
		// "cropped"));
		Uri outputUri = Uri.fromFile(new File(getContext().getCacheDir(), (Calendar.getInstance()
				.getTimeInMillis() + ".jpg")));

		Crop.of(source,outputUri).asSquare().start(getContext(), this, 6709);
		//////////////////////////new Crop(source).output(outputUri).asSquare().start(registerActivity);
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == registerActivity.RESULT_OK) {
//			AppLog.Log(TAG, "Handle crop");
			profileImageData = getRealPathFromURI(Crop.getOutput(result));
			Bitmap map = getRotatedBitmap(new File(profileImageData));
			String filenameArray[] = profileImageData.split("\\.");
			imageext = filenameArray[filenameArray.length-1];


			ivProfile.setImageURI(Crop.getOutput(result));
			ivProfile.setImageBitmap(map);
			stringimage = getStringImage(map);

		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(registerActivity,
					Crop.getError(result).getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}


	public String getStringImage(Bitmap bmp){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] imageBytes = baos.toByteArray();
		String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
		return encodedImage;
	}

	public String GetCountryZipCode() {
		String CountryID = "";
		String CountryZipCode = "";

		TelephonyManager manager = (TelephonyManager) getActivity()
				.getSystemService(getActivity().TELEPHONY_SERVICE);
		// getNetworkCountryIso
		CountryID = manager.getSimCountryIso().toUpperCase();
		String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
		for (int i = 0; i < rl.length; i++) {
			String[] g = rl[i].split(",");
			if (g[1].trim().equals(CountryID.trim())) {
				CountryZipCode = g[0];
				break;
			}
		}
		return CountryZipCode;
	}

}
