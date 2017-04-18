package com.cabrunzltd.cabrunz.driver.locationupdate;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class LocationHelper implements LocationListener
//		,GooglePlayServicesClient.ConnectionCallbacks,
//		GooglePlayServicesClient.OnConnectionFailedListener
{

	private LocationRequest mLocationRequest;
//	private LocationClient mLocationClient;
	private OnLocationReceived mLocationReceived;
	private Context context;
	private LatLng latLong;

	public interface OnLocationReceived {
		public void onLocationReceived(LatLng latlong);
	}

	public LocationHelper(Context context) {
		this.context = context;

		mLocationRequest = LocationRequest.create();

		// Set the update interval
		mLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

//		mLocationClient = new LocationClient(context, this, this);
	}

	public void setLocationReceivedLister(OnLocationReceived mLocationReceived) {
		this.mLocationReceived = mLocationReceived;
	}

	public LatLng getCurrentLocation() {
		return latLong;
	}

	public LatLng getLastLocation() {

		// If Google Play Services is available
		if (servicesConnected()) {
			// Get the current location
//			Location currentLocation = mLocationClient.getLastLocation();
			// Display the current location in the UI
//			latLong = LocationUtils.getLatLng(currentLocation);
		}

		return latLong;
	}

	public void onStart() {
//		mLocationClient.connect();
	}

	public void onStop() {
		// If the client is connected
//		if (mLocationClient.isConnected()) {
//			stopPeriodicUpdates();
//		}
//
//		// After disconnect() is called, the client is considered "dead".
//		mLocationClient.disconnect();
	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			return false;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		latLong = LocationUtils.getLatLng(location);

		if (mLocationReceived != null && latLong != null) {
			mLocationReceived.onLocationReceived(latLong);
		} else {
//			Location currentLocation = mLocationClient.getLastLocation();
//
//			// Display the current location in the UI
//			latLong = LocationUtils.getLatLng(currentLocation);
//			mLocationReceived.onLocationReceived(latLong);
		}
	}

//	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}

//	@Override
	public void onConnected(Bundle connectionHint) {
		startPeriodicUpdates();
	}

//	@Override
	public void onDisconnected() {
	}

	private void startPeriodicUpdates() {
//		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
//		mLocationClient.removeLocationUpdates(this);
	}

	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
