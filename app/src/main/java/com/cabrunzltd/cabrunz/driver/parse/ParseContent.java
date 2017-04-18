package com.cabrunzltd.cabrunz.driver.parse;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.db.DBHelper;
import com.cabrunzltd.cabrunz.driver.maputills.PolyLineUtils;
import com.cabrunzltd.cabrunz.driver.model.ApplicationPages;
import com.cabrunzltd.cabrunz.driver.model.BeanRoute;
import com.cabrunzltd.cabrunz.driver.model.BeanStep;
import com.cabrunzltd.cabrunz.driver.model.Bill;
import com.cabrunzltd.cabrunz.driver.model.History;
import com.cabrunzltd.cabrunz.driver.model.RequestDetail;
import com.cabrunzltd.cabrunz.driver.model.Route;
import com.cabrunzltd.cabrunz.driver.model.Step;
import com.cabrunzltd.cabrunz.driver.model.User;
import com.cabrunzltd.cabrunz.driver.model.VehicalType;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;
import com.cabrunzltd.cabrunz.driver.utills.PreferenceHelper;
import com.cabrunzltd.cabrunz.driver.utills.ReadFiles;
import com.google.android.gms.maps.model.LatLng;

public class ParseContent {
	private Activity activity;
	private PreferenceHelper preferenceHelper;
	private final String KEY_SUCCESS = "success";
	private final String KEY_ERROR = "error";
	private final String IS_WALKER_STARTED = "is_walker_started";
	private final String IS_WALKER_ARRIVED = "is_walker_arrived";
	private final String IS_WALK_STARTED = "is_started";
	private final String IS_DOG_RATED = "is_dog_rated";
	private final String IS_WALK_COMPLETED = "is_completed";
	private final String KEY_ERROR_CODE = "error_code";
	private final String CURRENCY = "currency";

	private final String BASE_PRICE = "base_price";
	private final String TYPES = "types";
	private final String ID = "id";
	private final String ICON = "icon";
	private final String IS_DEFAULT = "is_default";
	private final String PRICE_PER_UNIT_TIME = "price_per_unit_time";
	private final String PRICE_PER_UNIT_DISTANCE = "price_per_unit_distance";

	public ParseContent(Activity activity) {
		this.activity = activity;
		preferenceHelper = new PreferenceHelper(activity);
	}


	public BeanRoute parseRoute(String response, BeanRoute routeBean) {

		try {
			BeanStep stepBean;
			JSONObject jObject = new JSONObject(response);
			JSONArray jArray = jObject.getJSONArray("routes");
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject innerjObject = jArray.getJSONObject(i);
				if (innerjObject != null) {
					JSONArray innerJarry = innerjObject.getJSONArray("legs");
					for (int j = 0; j < innerJarry.length(); j++) {

						JSONObject jObjectLegs = innerJarry.getJSONObject(j);
						routeBean.setDistanceText(jObjectLegs.getJSONObject(
								"distance").getString("text"));
						routeBean.setDistanceValue(jObjectLegs.getJSONObject(
								"distance").getInt("value"));

						routeBean.setDurationText(jObjectLegs.getJSONObject(
								"duration").getString("text"));
						routeBean.setDurationValue(jObjectLegs.getJSONObject(
								"duration").getInt("value"));

						routeBean.setStartAddress(jObjectLegs
								.getString("start_address"));
						routeBean.setEndAddress(jObjectLegs
								.getString("end_address"));

						routeBean.setStartLat(jObjectLegs.getJSONObject(
								"start_location").getDouble("lat"));
						routeBean.setStartLon(jObjectLegs.getJSONObject(
								"start_location").getDouble("lng"));
						routeBean.setEndLat(jObjectLegs.getJSONObject(
								"end_location").getDouble("lat"));
						routeBean.setEndLon(jObjectLegs.getJSONObject(
								"end_location").getDouble("lng"));

						JSONArray jstepArray = jObjectLegs
								.getJSONArray("steps");
						if (jstepArray != null) {
							for (int k = 0; k < jstepArray.length(); k++) {
								stepBean = new BeanStep();
								JSONObject jStepObject = jstepArray
										.getJSONObject(k);
								if (jStepObject != null) {

									stepBean.setHtml_instructions(jStepObject
											.getString("html_instructions"));
									stepBean.setStrPoint(jStepObject
											.getJSONObject("polyline")
											.getString("points"));
									stepBean.setStartLat(jStepObject
											.getJSONObject("start_location")
											.getDouble("lat"));
									stepBean.setStartLon(jStepObject
											.getJSONObject("start_location")
											.getDouble("lng"));
									stepBean.setEndLat(jStepObject
											.getJSONObject("end_location")
											.getDouble("lat"));
									stepBean.setEndLong(jStepObject
											.getJSONObject("end_location")
											.getDouble("lng"));

									stepBean.setListPoints(new PolyLineUtils()
											.decodePoly(stepBean.getStrPoint()));
									routeBean.getListStep().add(stepBean);
								}

							}
						}
					}

				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return routeBean;
	}

	public boolean isSuccessWithId(String response) {
		if (TextUtils.isEmpty(response)) {
			return false;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				preferenceHelper.putUserId(jsonObject
						.getString(AndyConstants.Params.ID));
				preferenceHelper.putSessionToken(jsonObject
						.getString(AndyConstants.Params.TOKEN));
				preferenceHelper.putEmail(jsonObject
						.getString(AndyConstants.Params.EMAIL));
				// preferenceHelper.putEmail(jsonObject
				// .optString(AndyConstants.Params.EMAIL));
				preferenceHelper.putLoginBy(jsonObject
						.getString(AndyConstants.Params.LOGIN_BY));
				if (!preferenceHelper.getLoginBy().equalsIgnoreCase(
						AndyConstants.MANUAL)) {
					preferenceHelper.putSocialId(jsonObject
							.getString(AndyConstants.Params.SOCIAL_UNIQUE_ID));
				}
				return true;
			} else {
				AndyUtils.showErrorToast(jsonObject.getInt(KEY_ERROR_CODE),
						activity);
				return false;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isSuccess(String response) {
		if (TextUtils.isEmpty(response))
			return false;
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				return true;
			} else {
				AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
				return false;
			}

			// else {
			//
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void parseWhenWalkReached(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			double totalCost = jsonObject.getDouble(AndyConstants.Params.TOTAL);
			String reqid = jsonObject
					.getString(AndyConstants.Params.REQUEST_ID);
			showProviderPriceConfirmationAlert(reqid, totalCost);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Bill parsebillwhenwalkcomplete(String response) {
		Bill bill = new Bill();
//		Log.e("", "walkcomplete:" + response);
		if (TextUtils.isEmpty(response)) {
			return null;
		}

		try {

			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {

				JSONObject billobject = jsonObject.getJSONObject("bill");
				bill.setBasePrice(billobject.getString(BASE_PRICE));
				bill.setPayment_mode(billobject.getString("payment_mode"));

				bill.setDistance(billobject
						.getString(AndyConstants.Params.DISTANCE));
				bill.setUnit(billobject.getString("unit"));
				bill.setTime(billobject.getString(AndyConstants.Params.TIME));
				bill.setDistanceCost(billobject
						.getString(AndyConstants.Params.DISTANCE_COST));
				bill.setPerdistancecost(billobject
						.getString("distance_cost_only"));
				bill.setPertimecost(billobject.getString("time_cost_only"));
				bill.setTimeCost(billobject
						.getString(AndyConstants.Params.TIME_COST));
				bill.setCurrency(billobject.getString(CURRENCY));
				bill.setTotal(billobject.getString("total"));
				bill.setActual_price(billobject.getString("actual_total"));
				bill.setPromo_discount(billobject.getString("promo_discount"));
				bill.setPromo(billobject.getString("promo"));

				JSONObject walkerobject = billobject.getJSONObject("walker");
				bill.setPrimary_amount(walkerobject.getString("amount"));

				JSONObject adminobject = billobject.getJSONObject("admin");
				bill.setSecoundry_amount(adminobject.getString("amount"));

			}
			if (jsonObject.has("sub_total")) {
				// EDIT SCENARIO
				bill.setQuotedPrice(jsonObject.getString("sub_total"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bill;

	}

	public ArrayList<String> parseCountryCodes() {
		String response = "";
		ArrayList<String> list = new ArrayList<String>();
		try {
			response = ReadFiles.readRawFileAsString(activity,
					R.raw.countrycodes);

			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				list.add(object.getString("phone-code") + " "
						+ object.getString("name"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public int getErrorCode(String response) {
		if (TextUtils.isEmpty(response))
			return 0;
		try {
//			AppLog.Log("TAG", response);
			JSONObject jsonObject = new JSONObject(response);
			return jsonObject.getInt(KEY_ERROR_CODE);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int parseRequestInProgress(String response) {
//		Log.e("", "REQUEST parse:" + response);
		if (TextUtils.isEmpty(response)) {
			return AndyConstants.NO_REQUEST;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				int requestId = jsonObject
						.getInt(AndyConstants.Params.REQUEST_ID);
				preferenceHelper.putRequestId(requestId);
				return requestId;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return AndyConstants.NO_REQUEST;
	}

	public RequestDetail parseRequestStatus(String response) {
		if (TextUtils.isEmpty(response)) {
			return null;
		}
		RequestDetail requestDetail = null;
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				requestDetail = new RequestDetail();
				requestDetail.setJobStatus(AndyConstants.IS_ASSIGNED);
				JSONObject object = jsonObject
						.getJSONObject(AndyConstants.Params.REQUEST);
				requestDetail.setD_address(object
						.getString("destination_address"));
				requestDetail.setS_address(object.getString("source_address"));

				if (object.getInt(IS_WALKER_STARTED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_WALKER_STARTED);
					// status = AndyConstants.IS_WALKER_STARTED;
				} else if (object.getInt(IS_WALKER_ARRIVED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_WALKER_ARRIVED);
					// status = AndyConstants.IS_WALKER_ARRIVED;
				} else if (object.getInt(IS_WALK_STARTED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_WALK_STARTED);
					// status = AndyConstants.IS_WALK_STARTED;
					// String time =
					// object.optString(AndyConstants.Params.START_TIME);
					// // "start_time": "2014-11-20 03:27:37"
					// if (!TextUtils.isEmpty(time)) {
					// try {
					// TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
					// Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
					// Locale.ENGLISH).parse(time);
					// AppLog.Log("TAG", "START DATE---->" + date.toString()
					// + " month:" + date.getMonth());
					// preferenceHelper.putRequestTime(date.getTime());
					// requestDetail.setStartTime(date.getTime());
					// } catch (ParseException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					//
					// }
				} else if (object.getInt(IS_WALK_COMPLETED) == 0) {
					preferenceHelper.putIsTripStart(true);
					requestDetail.setJobStatus(AndyConstants.IS_WALK_COMPLETED);
					// status = AndyConstants.IS_WALK_COMPLETED;
				} else if (object.getInt(IS_DOG_RATED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_DOG_RATED);
					// status = AndyConstants.IS_DOG_RATED;
				}
				if (object.has("time")) {
					requestDetail.setTime(object.getString("time"));
					requestDetail.setDistance(object.getString("distance"));
				}

				JSONObject ownerDetailObject = object
						.getJSONObject(AndyConstants.Params.OWNER);
				requestDetail.setClientName(ownerDetailObject
						.getString(AndyConstants.Params.NAME));
				requestDetail.setClientProfile(ownerDetailObject
						.getString(AndyConstants.Params.PICTURE));
				requestDetail.setClientPhoneNumber(ownerDetailObject
						.getString(AndyConstants.Params.PHONE));
				requestDetail.setAddress(ownerDetailObject
						.getString(AndyConstants.Params.ADDRESS));
				requestDetail.setClientRating((float) ownerDetailObject
						.optDouble(AndyConstants.Params.RATING));
				requestDetail.setClientLatitude(ownerDetailObject
						.getString(AndyConstants.Params.LATITUDE));
				requestDetail.setClientLongitude(ownerDetailObject
						.getString(AndyConstants.Params.LONGITUDE));

				try {
					requestDetail.setClient_d_latitude(ownerDetailObject
							.getString(AndyConstants.Params.DEST_LATITUDE));
					requestDetail.setClient_d_longitude(ownerDetailObject
							.getString(AndyConstants.Params.DEST_LONGITUDE));
				} catch (Exception e) {
					e.printStackTrace();
				}
				// requestDetail.setUnit(object.getString("unit"));
				JSONObject jsonObjectBill = jsonObject.optJSONObject("bill");
				if (jsonObjectBill != null) {
					requestDetail.setTime(jsonObjectBill.getString("time"));
					requestDetail.setDistance(jsonObjectBill
							.getString("distance"));
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return requestDetail;
	}


	public RequestDetail parseClientlocation(String response) {

//		Log.d("xxx", "from parse all request  " + response);
		if (TextUtils.isEmpty(response)) {
			return null;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				RequestDetail requestDetail = new RequestDetail();
				requestDetail.setClientLatitude(jsonObject
						.getString(AndyConstants.Params.LATITUDE));

				requestDetail.setClientLongitude(jsonObject
						.getString(AndyConstants.Params.LONGITUDE));

				return requestDetail;



			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public RequestDetail parseAllRequests(String response) {

//		Log.d("xxx", "from parse all request  " + response);
		if (TextUtils.isEmpty(response)) {
			return null;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.INCOMING_REQUESTS);
				if (jsonArray.length() > 0) {
					JSONObject object = jsonArray.getJSONObject(0);

					if (object.getInt(AndyConstants.Params.REQUEST_ID) != AndyConstants.NO_REQUEST) {
						RequestDetail requestDetail = new RequestDetail();
						requestDetail.setRequestId(object
								.getInt(AndyConstants.Params.REQUEST_ID));
						requestDetail.setD_address(object
								.getString("destination_address"));
						requestDetail.setS_address(object
								.getString("source_address"));
						int timeto_respond = object
								.getInt(AndyConstants.Params.TIME_LEFT_TO_RESPOND);
						if (timeto_respond < 0) {
							return null;
						} else {
							requestDetail.setTimeLeft(timeto_respond);
						}
						JSONObject requestData = object
								.getJSONObject(AndyConstants.Params.REQUEST_DATA);
						JSONObject ownerDetailObject = requestData
								.getJSONObject(AndyConstants.Params.OWNER);

						requestDetail.setClientId(ownerDetailObject
								.getString(AndyConstants.Params.OWNERID));
						requestDetail.setClientName(ownerDetailObject
								.getString(AndyConstants.Params.NAME));
						requestDetail.setClientProfile(ownerDetailObject
								.getString(AndyConstants.Params.PICTURE));
						requestDetail.setClientPhoneNumber(ownerDetailObject
								.getString(AndyConstants.Params.PHONE));
						if (!TextUtils.isEmpty(ownerDetailObject
								.getString(AndyConstants.Params.RATING))) {
							requestDetail
									.setClientRating((float) ownerDetailObject
											.getDouble(AndyConstants.Params.RATING));
						} else {
							requestDetail.setClientRating(0);
						}
						requestDetail.setClientLatitude(ownerDetailObject
								.getString(AndyConstants.Params.LATITUDE));

						requestDetail.setClientLongitude(ownerDetailObject
								.getString(AndyConstants.Params.LONGITUDE));
						try {
							requestDetail
									.setClient_d_latitude(ownerDetailObject
											.getString(AndyConstants.Params.DEST_LATITUDE));
							requestDetail
									.setClient_d_longitude(ownerDetailObject
											.getString(AndyConstants.Params.DEST_LONGITUDE));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return requestDetail;
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public User parseUserAndStoreToDb(String response) {
		User user = null;
		try {
			JSONObject jsonObject = new JSONObject(response);

			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				user = new User();
				DBHelper dbHelper = new DBHelper(activity);
				user.setUserId(jsonObject.getInt(AndyConstants.Params.ID));
				user.setEmail(jsonObject.optString(AndyConstants.Params.EMAIL));
				user.setFname(jsonObject
						.getString(AndyConstants.Params.FIRSTNAME));
				user.setLname(jsonObject
						.getString(AndyConstants.Params.LAST_NAME));

				user.setAddress(jsonObject
						.getString(AndyConstants.Params.ADDRESS));
				user.setBio(jsonObject.getString(AndyConstants.Params.BIO));
				user.setZipcode(jsonObject
						.getString(AndyConstants.Params.ZIPCODE));
				user.setPicture(jsonObject
						.getString(AndyConstants.Params.PICTURE));
				user.setContact(jsonObject
						.getString(AndyConstants.Params.PHONE));
				user.setTimezone(jsonObject
						.getString(AndyConstants.Params.TIMEZONE));
				dbHelper.createUser(user);

			} else {
				// AndyUtils.showToast(jsonObject.getString(KEY_ERROR),
				// activity);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}

	// public boolean isSuccess(String response) {
	// if (TextUtils.isEmpty(response)) {
	// return false;
	// }
	// try {
	// JSONObject jsonObject = new JSONObject(response);
	// if (jsonObject.getBoolean(KEY_SUCCESS)) {
	// return true;
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return false;
	//
	// }
	public RequestDetail parseNotification(String response) {
//		Log.e("xxx", "from parsenotification  " + response);

		if (TextUtils.isEmpty(response)) {
			return null;
		}
		try {

			JSONObject object = new JSONObject(response);

			if (object.getInt(AndyConstants.Params.REQUEST_ID) != AndyConstants.NO_REQUEST) {
				RequestDetail requestDetail = new RequestDetail();
				requestDetail.setRequestId(object
						.getInt(AndyConstants.Params.REQUEST_ID));

				int timeto_respond = object
						.getInt(AndyConstants.Params.TIME_LEFT_TO_RESPOND);
				if (timeto_respond < 0) {
					return null;
				} else {
					requestDetail.setTimeLeft(timeto_respond);
				}

				JSONObject requestData = object
						.getJSONObject(AndyConstants.Params.REQUEST_DATA);
				JSONObject ownerDetailObject = requestData
						.getJSONObject(AndyConstants.Params.OWNER);
				requestDetail.setClientName(ownerDetailObject
						.getString(AndyConstants.Params.NAME));
				requestDetail.setClientProfile(ownerDetailObject
						.getString(AndyConstants.Params.PICTURE));
				requestDetail.setClientPhoneNumber(ownerDetailObject
						.getString(AndyConstants.Params.PHONE));
				requestDetail.setAddress(ownerDetailObject
						.getString(AndyConstants.Params.ADDRESS));
				requestDetail.setClientRating((float) ownerDetailObject
						.getDouble(AndyConstants.Params.RATING));
				requestDetail.setClientLatitude(ownerDetailObject
						.getString(AndyConstants.Params.LATITUDE));
				requestDetail.setClientLongitude(ownerDetailObject
						.getString(AndyConstants.Params.LONGITUDE));
				requestDetail.setD_address(ownerDetailObject
						.getString("destination_address"));
				requestDetail.setS_address(ownerDetailObject
						.getString("source_address"));
//				Log.e("",
//						"parseNotification:"
//								+ ownerDetailObject
//										.getString(AndyConstants.Params.DEST_LATITUDE));
				try {
					requestDetail.setClient_d_latitude(ownerDetailObject
							.getString(AndyConstants.Params.DEST_LATITUDE));
					requestDetail.setClient_d_longitude(ownerDetailObject
							.getString(AndyConstants.Params.DEST_LONGITUDE));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return requestDetail;

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public ArrayList<ApplicationPages> parsePages(
			ArrayList<ApplicationPages> list, String response) {
//		Log.e("", "Drawer Menu:" + response);
		list.clear();
		ApplicationPages applicationPages = new ApplicationPages();
		applicationPages.setId(-1);
		applicationPages.setTitle(activity.getResources().getString(
				R.string.text_profile));
		applicationPages.setData("");
		applicationPages.setIcon("");
		list.add(applicationPages);

		applicationPages = new ApplicationPages();
		applicationPages.setId(-2);
		applicationPages.setTitle(activity.getResources().getString(
				R.string.text_history));
		applicationPages.setData("");
		applicationPages.setIcon("");
		list.add(applicationPages);

		applicationPages = new ApplicationPages();
		applicationPages.setId(-3);
		applicationPages.setTitle(activity.getResources().getString(
				R.string.text_setting));
		applicationPages.setData("");
		applicationPages.setIcon("");

		list.add(applicationPages);
		if (TextUtils.isEmpty(response)) {
			return list;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.INFORMATIONS);
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						applicationPages = new ApplicationPages();
						JSONObject object = jsonArray.getJSONObject(i);
						applicationPages.setId(object
								.getInt(AndyConstants.Params.ID));
						applicationPages.setTitle(object
								.getString(AndyConstants.Params.TITLE));
						applicationPages.setData(object
								.getString(AndyConstants.Params.CONTENT));
						applicationPages.setIcon(object
								.getString(AndyConstants.Params.ICON));
						// list.add(applicationPages);
					}
				}

			}
			// else {
			// AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean checkDriverStatus(String response) {
		if (TextUtils.isEmpty(response))
			return false;
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				if (jsonObject.getInt(AndyConstants.Params.IS_ACTIVE) == 0) {
					return false;
				} else {
					return true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Route parseRoute(String response, Route routeBean) {

		try {
			Step stepBean;
			JSONObject jObject = new JSONObject(response);
			JSONArray jArray = jObject.getJSONArray("routes");
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject innerjObject = jArray.getJSONObject(i);
				if (innerjObject != null) {
					JSONArray innerJarry = innerjObject.getJSONArray("legs");
					for (int j = 0; j < innerJarry.length(); j++) {

						JSONObject jObjectLegs = innerJarry.getJSONObject(j);
						routeBean.setDistanceText(jObjectLegs.getJSONObject(
								"distance").getString("text"));
						routeBean.setDistanceValue(jObjectLegs.getJSONObject(
								"distance").getInt("value"));

						routeBean.setDurationText(jObjectLegs.getJSONObject(
								"duration").getString("text"));
						routeBean.setDurationValue(jObjectLegs.getJSONObject(
								"duration").getInt("value"));

						routeBean.setStartAddress(jObjectLegs
								.getString("start_address"));
						routeBean.setEndAddress(jObjectLegs
								.getString("end_address"));

						routeBean.setStartLat(jObjectLegs.getJSONObject(
								"start_location").getDouble("lat"));
						routeBean.setStartLon(jObjectLegs.getJSONObject(
								"start_location").getDouble("lng"));

						routeBean.setEndLat(jObjectLegs.getJSONObject(
								"end_location").getDouble("lat"));
						routeBean.setEndLon(jObjectLegs.getJSONObject(
								"end_location").getDouble("lng"));

						JSONArray jstepArray = jObjectLegs
								.getJSONArray("steps");
						if (jstepArray != null) {
							for (int k = 0; k < jstepArray.length(); k++) {
								stepBean = new Step();
								JSONObject jStepObject = jstepArray
										.getJSONObject(k);
								if (jStepObject != null) {

									stepBean.setHtml_instructions(jStepObject
											.getString("html_instructions"));
									stepBean.setStrPoint(jStepObject
											.getJSONObject("polyline")
											.getString("points"));
									stepBean.setStartLat(jStepObject
											.getJSONObject("start_location")
											.getDouble("lat"));
									stepBean.setStartLon(jStepObject
											.getJSONObject("start_location")
											.getDouble("lng"));
									stepBean.setEndLat(jStepObject
											.getJSONObject("end_location")
											.getDouble("lat"));
									stepBean.setEndLong(jStepObject
											.getJSONObject("end_location")
											.getDouble("lng"));

									stepBean.setListPoints(new PolyLineUtils()
											.decodePoly(stepBean.getStrPoint()));
									routeBean.getListStep().add(stepBean);
								}

							}
						}
					}

				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return routeBean;
	}

	public ArrayList<VehicalType> parseTypes(String response,
			ArrayList<VehicalType> list) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				JSONArray jsonArray = jsonObject.getJSONArray(TYPES);
				for (int i = 0; i < jsonArray.length(); i++) {
					VehicalType type = new VehicalType();
					JSONObject typeJson = jsonArray.getJSONObject(i);
					type.setBasePrice(typeJson.getString(BASE_PRICE));
					type.setIcon(typeJson.getString(ICON));
					type.setId(typeJson.getInt(ID));
					type.setName(typeJson.getString(AndyConstants.Params.NAME));
					type.setPricePerUnitDistance(typeJson
							.getString(PRICE_PER_UNIT_DISTANCE));
					type.setPricePerUnitTime(typeJson
							.getString(PRICE_PER_UNIT_TIME));
					list.add(type);

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;

	}

	public ArrayList<History> parseHistory(String response,
			ArrayList<History> list) {

		list.clear();

		if (TextUtils.isEmpty(response)) {
			return list;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.REQUESTS);
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object = jsonArray.getJSONObject(i);
						History history = new History();
						history.setId(object.getInt(AndyConstants.Params.ID));
						history.setDate(object
								.getString(AndyConstants.Params.DATE));
						history.setDistance(object
								.getString(AndyConstants.Params.DISTANCE));
						history.setTime(object
								.getString(AndyConstants.Params.TIME));
						history.setBasePrice(object.getString(BASE_PRICE));
						history.setDistanceCost(object
								.getString(AndyConstants.Params.DISTANCE_COST));
						history.setCurrency(object.getString(CURRENCY));
						history.setTimecost(object
								.getString(AndyConstants.Params.TIME_COST));
						history.setTotal(new DecimalFormat("0.00").format(Double
								.parseDouble(object
										.getString(AndyConstants.Params.TOTAL))));
						JSONObject ja_primary = object.getJSONObject("walker");

						history.setPrimary_amount(ja_primary
								.getString("amount"));

						JSONObject ja_secoundry = object.getJSONObject("admin");
						history.setSecoundry_amount(ja_secoundry
								.getString("amount"));
						history.setPromo(object.getString("promo"));
						history.setPromo_discount(object
								.getString("promo_discount"));
						history.setActual_price(object
								.getString("actual_total"));
						JSONObject userObject = object
								.getJSONObject(AndyConstants.Params.OWNER);
						history.setFirstName(userObject
								.getString(AndyConstants.Params.FIRSTNAME));
						history.setLastName(userObject
								.getString(AndyConstants.Params.LAST_NAME));
						history.setPhone(userObject
								.getString(AndyConstants.Params.PHONE));
						history.setPicture(userObject
								.getString(AndyConstants.Params.PICTURE));
						history.setEmail(userObject
								.getString(AndyConstants.Params.EMAIL));
						history.setBio(userObject
								.getString(AndyConstants.Params.BIO));
						list.add(history);
					}
				}

			}
			// else {
			// AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param response
	 * @return
	 */
	public boolean parseAvaibilty(String response) {
		if (TextUtils.isEmpty(response))
			return false;
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
//				if (jsonObject.getString("is_available").equals("1") &&jsonObject.getString("is_active").equals("1")) {
				if (jsonObject.getString("is_active").equals("1")) {
					preferenceHelper.putActive("1");
//					Log.d("mahi", "check");
					return true;
				} else {
					ApplicationPages.isavailable = false;
//					Log.d("mahi", "uncheck");
					preferenceHelper.putActive("0");
				}
			}

			// else {
			// AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param response
	 * @param points
	 */
	public ArrayList<LatLng> parsePathRequest(String response,
			ArrayList<LatLng> points) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.LOCATION_DATA);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject json = jsonArray.getJSONObject(i);
					points.add(new LatLng(Double.parseDouble(json
							.getString(AndyConstants.Params.LATITUDE)), Double
							.parseDouble(json
									.getString(AndyConstants.Params.LONGITUDE))));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return points;
	}

	private void showProviderPriceConfirmationAlert(final String reqid,
			final double totalCost) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Trip Price");
		builder.setMessage(String.format("\nThe price for the trip is %.2f\n",
				totalCost));

		builder.setPositiveButton(AndyConstants.FARE_AGREED,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent pushIntent = new Intent(
								AndyConstants.FARE_AGREEMENT_STATUS);
						pushIntent.putExtra(
								AndyConstants.Params.FARE_AGREEMENT_STATUS,
								AndyConstants.FARE_AGREED);
						pushIntent.putExtra(AndyConstants.Params.TOTAL,
								totalCost);
						pushIntent.putExtra(AndyConstants.Params.REQUEST_ID,
								reqid);
						LocalBroadcastManager.getInstance(activity)
								.sendBroadcast(pushIntent);
					}
				});
		builder.setNegativeButton(AndyConstants.FARE_DISAGREED,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent pushIntent = new Intent(
								AndyConstants.FARE_AGREEMENT_STATUS);
						pushIntent.putExtra(
								AndyConstants.Params.FARE_AGREEMENT_STATUS,
								AndyConstants.FARE_DISAGREED);
						showDisagreementPopup(totalCost, reqid);
						LocalBroadcastManager.getInstance(activity)
								.sendBroadcast(pushIntent);
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showDisagreementPopup(final double totalCost,
			final String reqid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Quote Price");
		final EditText etQuotePrice = new EditText(activity);
		etQuotePrice.setHint("Quote your price");
		etQuotePrice.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(etQuotePrice);
		builder.setNeutralButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String quote = etQuotePrice.getText().toString();
				double quotedPrice = 0;
				if (TextUtils.isEmpty(quote)) {
					quotedPrice = totalCost;
				} else {
					quotedPrice = Double.parseDouble(quote);
				}
				Intent pushIntent = new Intent(AndyConstants.EDIT_AMOUNT_STATUS);
				pushIntent.putExtra(AndyConstants.Params.QUOTED_PRICE,
						quotedPrice);
				pushIntent.putExtra(AndyConstants.Params.REQUEST_ID, reqid);
				LocalBroadcastManager.getInstance(activity).sendBroadcast(
						pushIntent);
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

}
