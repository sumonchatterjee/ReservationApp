package com.dineout.book.fragment.mybookings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.bookingflow.BookingTimeSlotFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.dialogs.DOShareDialog;
import com.dineout.recycleradapters.UpcomingBookingRecyclerAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_AREA;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_CITY;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_LOCALITY;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_NAME;

public class UpcomingBookingFragment extends BookingTabFragment
        implements UpcomingBookingRecyclerAdapter.UpcomingBookingClickListener {

    private int REQUEST_CODE_REDEEM_DEAL = 890;

    private boolean isFirstPage;
    private UpcomingBookingRecyclerAdapter recyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFirstPage = false;

        // Get Adapter Instance
        recyclerAdapter = new UpcomingBookingRecyclerAdapter(getActivity());
        recyclerAdapter.setUpcomingBookingClickListener(this);
        setRecyclerAdapter(recyclerAdapter);
    }

    @Override
    protected void getBookings(int currentPage) {
        showLoader();

        isFirstPage = (currentPage == 1);

        setRequestType(AppConstant.UPCOMING_BOOKING_TAB);

        getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_BOOKINGS, AppConstant.URL_USER_BOOKING_SEGG,
                ApiParams.getUserBookingSeggParams(
                        DOPreferences.getDinerId(getActivity().getApplicationContext()),
                        DOPreferences.getAuthKey(getActivity().getApplicationContext()),
                        AppConstant.BOOKING_TYPE_UPCOMING, currentPage, AppConstant.DEFAULT_PAGINATION_LIMIT),
                this, this, false);
    }

    @Override
    protected void processBookingResponse(JSONArray jsonArray) {
        if (isFirstPage) {
            recyclerAdapter.setData(jsonArray);
        } else {
            recyclerAdapter.updateData(jsonArray);
        }

        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetDirectionClick(JSONObject jsonObject) {
        String bookingType = jsonObject.optString("b_type");

        if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingType)) {

            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_deals_upcoming_card),
//                    jsonObject.optString("title1") + "_" + jsonObject.optString("r_id"));

        } else {
            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_get_directions_upcoming_card),
//                    null);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_upcoming_booking_detail),getString(R.string.d_get_directions),jsonObject.optString("b_id"),hMap);
        }

        String restLat = jsonObject.optString("latitude");
        restLat = ((AppUtil.isStringEmpty(restLat)) ? "" : restLat.trim());

        String restLong = jsonObject.optString("longitude");
        restLong = ((AppUtil.isStringEmpty(restLong)) ? "" : restLong.trim());

        Intent intent = AppUtil.getMapDirectionsIntent(restLat, restLong);
        startActivity(intent);
    }

    @Override
    public void onUploadBillClick(JSONObject jsonObject) {
        UploadBillController utility =
                new UploadBillController(getNetworkManager(), getActivity(),
                        jsonObject.optString("r_id"), jsonObject.optString("b_id", ""));
        utility.setPreviousScreenName(getString(R.string.ga_screen_booking_upcoming));
        utility.validate();
    }

    @Override
    public void onEditBookingClick(JSONObject jsonObject) {
        String bookingType = jsonObject.optString("b_type");

        if (AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingType)) {
            // Track DEAL Event
            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
                    getResources().getString(R.string.ga_screen_my_bookings),
                    getResources().getString(R.string.ga_action_edit_deals_upcoming_card),
                    jsonObject.optString("title1") + "_" + jsonObject.optString("r_id"));

        } else {
            // Track Event
            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
                    getResources().getString(R.string.ga_screen_my_bookings),
                    getResources().getString(R.string.ga_action_edit_upcoming_card),
                    null);
        }

        // Set Bundle
        Bundle restaurantBundle = new Bundle();
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, jsonObject.optString("r_id", "")); // Restaurant Id
        restaurantBundle.putString(AppConstant.BUNDLE_BOOKING_ID, jsonObject.optString("b_id", "")); // Booking Id
        restaurantBundle.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP,
                AppUtil.convertSecondsToMilliseconds(Long.parseLong(jsonObject.optString("dining_dt_time_ts", "0")))); // Dining Date Time in milliseconds
        restaurantBundle.putInt(AppConstant.BUNDLE_GUEST_COUNT, Integer.parseInt(jsonObject.optString("cnt_covers", "0")));
        restaurantBundle.putInt(AppConstant.BUNDLE_MALE_COUNT, Integer.parseInt(jsonObject.optString("cnt_covers_males", "0")));
        restaurantBundle.putInt(AppConstant.BUNDLE_FEMALE_COUNT, Integer.parseInt(jsonObject.optString("cnt_covers_females", "0")));
        restaurantBundle.putBoolean(AppConstant.BUNDLE_IS_EDIT_BOOKING, true); // Edit Booking Flag
        restaurantBundle.putInt(AppConstant.BUNDLE_OFFER_ID, Integer.parseInt(jsonObject.optString("offer_id", "0"))); // Selected Offer
        restaurantBundle.putString(AppConstant.BUNDLE_SPECIAL_REQUEST, jsonObject.optString("spcl_req", "")); // Special Request

        restaurantBundle.putString(BUNDLE_RESTAURANT_NAME, jsonObject.optString("title1", ""));
        restaurantBundle.putString(BUNDLE_RESTAURANT_CITY, jsonObject.optString("city_name", ""));
        restaurantBundle.putString(BUNDLE_RESTAURANT_AREA, jsonObject.optString("area_name", ""));
        restaurantBundle.putString(BUNDLE_RESTAURANT_LOCALITY, jsonObject.optString("locality_name", ""));

        String deepLink = DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostRestoDetail +
                "?q=" + jsonObject.optString("restaurant_id");
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, deepLink);

        // Get Instance
        BookingTimeSlotFragment bookingTimeSlotFragment = new BookingTimeSlotFragment();
        bookingTimeSlotFragment.setArguments(restaurantBundle);

        addToBackStack(getParentFragment().getFragmentManager(), bookingTimeSlotFragment);
    }

    @Override
    public void onPayNowClick(JSONObject jsonObject) {
        String alreadyPaid = jsonObject.optString("already_paid");
        int intAlreadyPaid = ((AppUtil.isStringEmpty(alreadyPaid)) ? 0 : Integer.parseInt(alreadyPaid));

        if (intAlreadyPaid == 0) {
            Bundle bundle = new Bundle();
            bundle.putString(PaymentConstant.RESTAURANT_NAME, jsonObject.optString("title1"));
            bundle.putString(PaymentConstant.RESTAURANT_ID, jsonObject.optString("r_id"));
            bundle.putString(PaymentConstant.BOOKING_ID, jsonObject.optString("b_id"));
            bundle.putString(PaymentConstant.DISPLAY_ID, jsonObject.optString("disp_id"));
            bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.BOOKING_TYPE);

            // Track Event
            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
                    getString(R.string.ga_screen_my_bookings),
                    getString(R.string.ga_action_pay_now_upcoming_card), null);

            PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());

        } else {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), getString(R.string.paid_restaurant));
        }
    }

    @Override
    public void onEditDealClick(JSONObject jsonObject) {

        // TODO Release v7.2.0 - Decided Not to handle Edit Deal CTA click

        // Track Event
        /*AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
                getString(R.string.ga_screen_my_bookings),
                getString(R.string.ga_action_edit_deals_upcoming_card),
                null);

        long dateTime = 0;
        String date = jsonObject.optString("dining_dt_time_ts", "0");

        if (!TextUtils.isEmpty(date)) {
            dateTime = Long.parseLong(date);
        }

        Bundle bundle = new Bundle();
        bundle.putString(DealsConfirmationFragment.RESTAURANT_ID, jsonObject.optString("r_id", ""));
        bundle.putLong(DealsConfirmationFragment.DATE, AppUtil.convertSecondsToMilliseconds(dateTime));
        bundle.putString(DealsConfirmationFragment.BOOKING_ID, jsonObject.optString("b_id", ""));
        bundle.putString(DealsConfirmationFragment.DINER_EMAIL, jsonObject.optString("diner_email", ""));
        bundle.putString(DealsConfirmationFragment.DINER_PHONE, jsonObject.optString("diner_phone", ""));
        bundle.putString(DealsConfirmationFragment.DINER_NAME, jsonObject.optString("diner_name", ""));
        bundle.putBoolean(DealsConfirmationFragment.IS_EDIT_DEAL, true);
        bundle.putString(DealsConfirmationFragment.SPCL_REQUEST, jsonObject.optString("spcl_req", ""));
        bundle.putString("FROM", "MyBookings");

        DealsConfirmationFragment dealConfirmationFragment = DealsConfirmationFragment.newInstance(bundle);
        dealConfirmationFragment.setDealsArrays(jsonObject.optJSONArray("deals"));

        addToBackStack(getActivity(), dealConfirmationFragment);*/
    }

    public void onShareClick(JSONObject jsonObject) {
        String bookingType = jsonObject.optString("b_type");
        String dealTitleStr = "";
        String dealCountStr = "";
        String resName = "";
        String type = "";

        if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingType)) {

            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_share_deals_upcoming_card),
//                    jsonObject.optString("title1") + "_" + jsonObject.optString("r_id"));

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_upcoming_share),"D_"+jsonObject.optString("b_id"),hMap);

            type = AppConstant.BOOKING_TYPE_DEAL;

            // Set Deal Title
            dealTitleStr = jsonObject.optString("title1");

            //setResName
            resName = jsonObject.optString("rest_name");

            // Get Deal Count
            int deal = 0;
            JSONArray dealJsonArray = jsonObject.optJSONArray("deals");
            if (dealJsonArray != null) {
                deal = dealJsonArray.length();
            }

            dealCountStr = Integer.toString(deal);
        } else if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bookingType)) {

            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_share_upcoming_events_card),
//                    jsonObject.optString("title") + "_" + jsonObject.optString("r_id"));


            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_upcoming_share),"E_"+jsonObject.optString("b_id"),hMap);

            type = AppConstant.BOOKING_TYPE_EVENT;

            //setResName
            resName = jsonObject.optString("rest_name");

            int deal = 0;
            JSONArray eventJsonArray = jsonObject.optJSONArray("events");
            if (eventJsonArray != null) {

                JSONObject obj = eventJsonArray.optJSONObject(0);

                if (obj != null) {

                    //set event title
                    dealTitleStr = obj.optString("title");
                }

                //set event count
                deal = eventJsonArray.length();
            }

            dealCountStr = Integer.toString(deal);

        } else {
            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_share_upcoming_card), null);

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_upcoming_share),"B_"+jsonObject.optString("b_id"),hMap);


            type = AppConstant.BOOKING_TYPES;

            //setResName
            resName = jsonObject.optString("rest_name");
        }

        DOShareDialog doShareDialog =
                DOShareDialog.newInstance(
                        getActivity().getApplicationContext(),
                        DOPreferences.getDinerFirstName(getActivity().getApplicationContext()),
                        dealTitleStr, dealCountStr, resName,
                        jsonObject.optString("short_url"), jsonObject.optString("display_date"),
                        jsonObject.optString("display_time"), jsonObject.optString("b_id"),
                        jsonObject.optString("cnt_covers"), jsonObject.optString("locality_name"));

        //showFragment(getParentFragment().getFragmentManager(), doShareDialog);
        doShareDialog.show(getActivity().getSupportFragmentManager(), type);
    }

    @Override
    public void onBookingCardClick(JSONObject jsonObject) {
        handleBookingCardClick(jsonObject);
    }

    @Override
    public void askForRedeemDealConfirmation(final JSONObject bookingJsonObject) {
        // Show Confirm Message
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_confirm);
        builder.setMessage(R.string.text_confirm_redeem_deal);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss Dialog
                dialog.dismiss();

                trackEventGA(getString(R.string.ga_screen_home),
                        getString(R.string.ga_action_redeem_deal_confirmation_yes), null);

                if (bookingJsonObject != null) {
                    // Get Booking Id
                    String bookingId = bookingJsonObject.optString("b_id", "");

                    if (!AppUtil.isStringEmpty(bookingId)) {
                        // Proceed with Redeem Deal
                        proceedWithRedeemDeal(bookingId);
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.button_dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                trackEventGA(getString(R.string.ga_screen_home),
                        getString(R.string.ga_action_redeem_deal_confirmation_cancel), null);

                // Dismiss Dialog
                dialog.dismiss();
            }
        });

        // Show Alert Dialog
        builder.show();
    }

    private void proceedWithRedeemDeal(String bookingID) {
        // Show Loading
        showLoader();

        // Take API Hit
        getNetworkManager().stringRequestPost(REQUEST_CODE_REDEEM_DEAL, AppConstant.URL_REDEEM_DEAL,
                ApiParams.getRedeemDealParams(bookingID), new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        try {
                            // Hide Loader
                            hideLoader();

                            if (getActivity() == null || getView() == null || AppUtil.isStringEmpty(responseObject))
                                return;

                            JSONObject responseJsonObject = new JSONObject(responseObject);

                            // Check Response
                            if (responseJsonObject.optBoolean("status")) { // Success
                                // call Booking Details API again ( update Redeem Now text )
                                Toast.makeText(getContext(),"Deal has been redeemed successfully",Toast.LENGTH_SHORT);
                                refreshUpcomingBookingList();

                            } else {
                                // Handle Error
                                handleErrorResponse(new JSONObject(responseObject));
                            }
                        } catch (JSONException e) {
                            // Exception
                        }
                    }
                }, this, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        recyclerAdapter = null;
    }
}
