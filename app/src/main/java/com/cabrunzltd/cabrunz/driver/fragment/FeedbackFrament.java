package com.cabrunzltd.cabrunz.driver.fragment;

import java.text.DecimalFormat;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.androidquery.AQuery;
import com.cabrunzltd.cabrunz.driver.MapActivity;
import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.SettingActivity;
import com.cabrunzltd.cabrunz.driver.base.BaseMapFragment;
import com.cabrunzltd.cabrunz.driver.model.Bill;
import com.cabrunzltd.cabrunz.driver.model.RequestDetail;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.widget.MyFontEdittextView;
import com.cabrunzltd.cabrunz.driver.widget.MyFontTextView;

/**
 * @author Kishan H Dhamat
 * 
 */
public class FeedbackFrament extends BaseMapFragment implements
		AsyncTaskCompleteListener {

	private MyFontEdittextView etFeedbackComment;
	private ImageView ivDriverImage;
	private RatingBar ratingFeedback;
	private MyFontTextView tvTime, tvDistance, tvClientName;

	private final String TAG = "FeedbackFrament";
	private AQuery aQuery;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View feedbackFragmentView = inflater.inflate(
				R.layout.fragment_feedback, container, false);

		etFeedbackComment = (MyFontEdittextView) feedbackFragmentView
				.findViewById(R.id.etFeedbackComment);
		tvTime = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvFeedBackTime);
		tvDistance = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvFeedbackDistance);
		ratingFeedback = (RatingBar) feedbackFragmentView
				.findViewById(R.id.ratingFeedback);
		ivDriverImage = (ImageView) feedbackFragmentView
				.findViewById(R.id.ivFeedbackDriverImage);
		tvClientName = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvClientName);

		mapActivity.setActionBarTitle(getResources().getString(
				R.string.text_feedback));

		feedbackFragmentView.findViewById(R.id.tvFeedbackSubmit)
				.setOnClickListener(this);
		feedbackFragmentView.findViewById(R.id.tvFeedbackskip)
				.setOnClickListener(this);

		return feedbackFragmentView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aQuery = new AQuery(mapActivity);
		RequestDetail requestDetail = (RequestDetail) getArguments()
				.getSerializable(AndyConstants.REQUEST_DETAIL);
		Bill bill = (Bill) getArguments().getSerializable("bill");

		final Dialog mDialog = new Dialog(mapActivity,
				android.R.style.Theme_Translucent_NoTitleBar);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mDialog.setContentView(R.layout.bill_layout);
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		DecimalFormat perHourFormat = new DecimalFormat("0.0");
		final LinearLayout promolayout = (LinearLayout) mDialog
				.findViewById(R.id.promolayout);
		// String currency = bill.getCurrency();
		String basePrice = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getBasePrice())));
		String basePricetmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(basePrice)));
		String totalTmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getTotal())));
		if (!TextUtils.isEmpty(bill.getQuotedPrice())) {
			totalTmp = bill.getQuotedPrice();
		}
		String distCostTmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getDistanceCost())));
		String timeCost = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getTimeCost())));
		String primary_amount = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getPrimary_amount())));
		String secoundry_amount = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getSecoundry_amount())));
		String discounts = String.valueOf(decimalFormat.format(Math.abs((Double
				.parseDouble(primary_amount) + Double
				.parseDouble(secoundry_amount))
				- (Double.parseDouble(totalTmp)))));
		if (bill.getPromo().equals("1")) {
			promolayout.setVisibility(View.VISIBLE);
			((TextView) mDialog.findViewById(R.id.tvactualPrice)).setText(" "
					+ bill.getActual_price());
			((TextView) mDialog.findViewById(R.id.tvdiscountPrice)).setText(" "
					+ bill.getPromo_discount());
		} else
			promolayout.setVisibility(View.GONE);

		String quotedPrice = null;
		if (!TextUtils.isEmpty(bill.getQuotedPrice())) {
			quotedPrice = bill.getQuotedPrice();
		}

		((TextView) mDialog.findViewById(R.id.tvBasePrice)).setText(" "
				+ basePrice);

		if (Integer.valueOf(bill.getPayment_mode()) == 1) {
			((MyFontTextView) mDialog.findViewById(R.id.payment_type))
					.setText(getResources().getString(R.string.cash_payment));
		} else
			((MyFontTextView) mDialog.findViewById(R.id.payment_type))
					.setText(getResources().getString(R.string.card_payment));

		((TextView) mDialog.findViewById(R.id.tvBillDistancePerMile))
				.setText(bill.getPerdistancecost() + " "
						+ getResources().getString(R.string.text_cost_per_mile));

		((TextView) mDialog.findViewById(R.id.tvBillTimePerHour)).setText(bill
				.getPertimecost()
				+ " "
				+ getResources().getString(R.string.text_cost_per_min));

		((TextView) mDialog.findViewById(R.id.adminCost))
				.setText(getResources().getString(R.string.text_cost_for_admin)
						+ " : " + bill.getCurrency() + " " + secoundry_amount);

		((TextView) mDialog.findViewById(R.id.providercost))
				.setText(getResources().getString(
						R.string.text_cost_for_provider)
						+ " : " + bill.getCurrency() + " " + primary_amount);

		((TextView) mDialog.findViewById(R.id.discounts))
				.setText(getResources().getString(R.string.text_discount)
						+ " : " + bill.getCurrency() + " " + discounts);

		((TextView) mDialog.findViewById(R.id.tvDis1)).setText(" "
				+ distCostTmp);
		((TextView) mDialog.findViewById(R.id.tvtim)).setText(" "
				+ bill.getTime() + " " + "min");
		((TextView) mDialog.findViewById(R.id.tvdis)).setText(" "
				+ String.format("%.3f", Double.parseDouble(bill.getDistance()))
				+ " " + bill.getUnit());

		((TextView) mDialog.findViewById(R.id.tvTime1)).setText(" " + timeCost);

		((TextView) mDialog.findViewById(R.id.tvTotal1))
				.setText(" " + totalTmp);

		Button btnConfirm = (Button) mDialog
				.findViewById(R.id.btnBillDialogClose);

		btnConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDialog.dismiss();
			}
		});

		mDialog.setCancelable(true);
		mDialog.show();

		if (requestDetail.getClientProfile() != null)
			aQuery.id(ivDriverImage).image(requestDetail.getClientProfile());
		// tvTime.setText(getArguments().getString(AndyConstants.Params.TIME));
		// tvDistance.setText(getArguments().getString(
		// AndyConstants.Params.DISTANCE));
//		Log.d("mahi", "time in feedback" + requestDetail.getTime());
		tvTime.setText((int) (Double.parseDouble(requestDetail.getTime()))
				+ " " + getString(R.string.text_mins));
		tvDistance.setText(new DecimalFormat("0.00").format(Double
				.parseDouble(requestDetail.getDistance()))
				+ " "
				+ mapActivity.getResources().getString(R.string.text_miles));// requestDetail.getUnit());
		tvClientName.setText(requestDetail.getClientName());
		preferenceHelper.putAvail_on("0");
	}

	@Override
	public void onResume() {
		super.onResume();
		// checkState();
		/*
		 * preferenceHelper
		 * .putCurrentFragment(AndyConstants.FEEDBACK_FRAGMENT_TAG);
		 */
		mapActivity.currentFragment = AndyConstants.FEEDBACK_FRAGMENT_TAG;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tvFeedbackSubmit:

			/*
			 * if (TextUtils.isEmpty(etFeedbackComment.getText().toString())) {
			 * AndyUtils.showToast( mapActivity.getResources().getString(
			 * R.string.text_empty_feedback), mapActivity); return; } else {
			 */
			giveRating();
			// }
			break;

		case R.id.tvFeedbackskip:
			preferenceHelper.clearRequestData();
			showavailability();

		default:
			break;
		}
	}

	// giving feedback for perticular job
	private void giveRating() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_rating), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.RATING);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.RATING,
				String.valueOf(ratingFeedback.getNumStars()));
		map.put(AndyConstants.Params.COMMENT, etFeedbackComment.getText()
				.toString().trim());

		new HttpRequester(mapActivity, map, AndyConstants.ServiceCode.RATING,
				this);
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.RATING:
//			AppLog.Log(TAG, "rating response" + response);
			if (parseContent.isSuccess(response)) {
				preferenceHelper.clearRequestData();
				AndyUtils.showToast(
						mapActivity.getResources().getString(
								R.string.toast_feedback_success), mapActivity);
				showavailability();

			}

			break;
		case AndyConstants.ServiceCode.TOGGLE_STATE:

//			Log.d("mahi", "toggle resposne" + response);

			if (response != null)
				if (!parseContent.isSuccess(response)) {
					return;
				}
			preferenceHelper.putAvail_on("1");
			Intent i = new Intent(mapActivity,MapActivity.class);
			startActivity(i);
			mapActivity.finish();

			break;
		default:
			break;
		}
	}

	private void showavailability() {
		// TODO Auto-generated method stub
		final Dialog d = new Dialog(mapActivity);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.availablity_popup);
		d.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		d.setCancelable(true);
		d.show();

		Button ok_btn = (Button) d.findViewById(R.id.btn_yes);

		ok_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				d.dismiss();
				preferenceHelper.putAvail_on("1");
				Intent i = new Intent(mapActivity,MapActivity.class);
				startActivity(i);
				mapActivity.finish();
				//changeState();

			}
		});

		Button close_btn = (Button) d.findViewById(R.id.btn_no);
		close_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				d.dismiss();
				AlertDialog.Builder builder1 = new AlertDialog.Builder(
						mapActivity);
				builder1.setTitle(null);
				builder1.setMessage("You will be made not available for any trips until you change it!");
				builder1.setCancelable(true);
				builder1.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								
									changeState();
								 
							}
						});

				AlertDialog alert11 = builder1.create();
				alert11.show();
				d.dismiss();

			}
		});

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
}
