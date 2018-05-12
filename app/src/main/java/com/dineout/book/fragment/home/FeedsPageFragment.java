package com.dineout.book.fragment.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.Response.Listener;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.dialogs.RestaurantReviewAskUserFeedbackDialog;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.fragment.mybookings.BookingDetailsFragment;
import com.dineout.book.fragment.bookingflow.BookingTimeSlotFragment;
import com.dineout.book.fragment.mybookings.BookingsFragment;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.dialogs.DOShareDialog;
import com.dineout.book.widgets.RestaurantReviewAskUserFeedback;
import com.dineout.recycleradapters.DynamicFeedsHomePageAdapter;
import com.dineout.recycleradapters.HorizontalListItemRecyclerAdapter.IHorizontalItemClickListener;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;


import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.addValueToJsonObject;
import static com.dineout.recycleradapters.util.RateNReviewUtil.appendObject;
import static com.dineout.recycleradapters.util.RateNReviewUtil.isInAppRatingTimerExpired;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_AREA;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_CITY;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_LOCALITY;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_NAME;

public class FeedsPageFragment extends MasterDOJSONReqFragment implements
        Listener<JSONObject>, ErrorListener, IHorizontalItemClickListener,
        DynamicFeedsHomePageAdapter.UpcomingBookingCardClickListener,
        DynamicFeedsHomePageAdapter.BannerClickListener,
        DynamicFeedsHomePageAdapter.RestaurantFeedbackCallback,
        DynamicFeedsHomePageAdapter.InAppRatingCallback,
        RateNReviewUtil.RateNReviewCallbacks,DynamicFeedsHomePageAdapter.LatestNotificationCallback {

    private static final int REQUEST_HOME_PAGE_CARD = 101;
    private final int AUTO_SCROLL = 0x02;
    private int REQUEST_CODE_REDEEM_DEAL = 890;
    private RecyclerView recyclerViewFeeds;
    private DynamicFeedsHomePageAdapter feedsRecyclerAdapter;
    private BannerHandler mAutoScrollHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feeds, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));

        initializeView();
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        initiateGetHomePageCardAPI();
    }

    // Initialize View
    private void initializeView() {
        // Get Recycler View Instance
        recyclerViewFeeds = (RecyclerView) getView().findViewById(R.id.recycler_view_feeds);

        DynamicFeedsHomePageAdapter feedsAdapter = new
                DynamicFeedsHomePageAdapter(getActivity());
        feedsAdapter.setNetworkManager(getNetworkManager(), getImageLoader());


        // Define Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager
                (getActivity(), LinearLayoutManager.VERTICAL, false);

        // Set Layout Manager
        recyclerViewFeeds.setLayoutManager(linearLayoutManager);

        // Set Recycler Adapter
        recyclerViewFeeds.setAdapter(feedsAdapter);
    }

    private void initiateGetHomePageCardAPI() {
        showLoader();
        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_HOME_PAGE_CARD, AppConstant.URL_HOME_PAGE_CARD,
                ApiParams.getHomePageCardParams(
                        DOPreferences.getSfByCity(getActivity().getApplicationContext()),
                        DOPreferences.getSfArrLocarea(getActivity().getApplicationContext()),
                        DOPreferences.getSfArrArea(getActivity().getApplicationContext()),
                        String.valueOf(DOPreferences.isAutoMode(getActivity().getApplicationContext()) ? 1 : 0)),
                this, this, true);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_HOME_PAGE_CARD) {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    if (responseObject.optJSONObject("output_params") != null) {
                        // Show List
                        recyclerViewFeeds.setVisibility(RecyclerView.VISIBLE);

                        // Get Data
                        JSONObject jsonObjectData = responseObject.optJSONObject("output_params").optJSONObject("data");

                        if (jsonObjectData != null) {
                            // Get Section
                            JSONArray jsonArraySection = jsonObjectData.optJSONArray("section");

                            // Get Section Data
                            JSONObject jsonObjectSectionData = jsonObjectData.optJSONObject("section_data");

                            if (jsonArraySection != null && jsonArraySection.length() > 0 && jsonObjectSectionData != null) {
                                prepareFeeds(jsonArraySection, jsonObjectSectionData);
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareFeeds(JSONArray jsonArraySection, JSONObject jsonObjectSectionData) {
        // add rating item
//        addRatingView(jsonArraySection, jsonObjectSectionData);

        // Initialize Feeds Adapter
        feedsRecyclerAdapter = (DynamicFeedsHomePageAdapter) recyclerViewFeeds.getAdapter();

        if (feedsRecyclerAdapter != null) {

            if(!isInAppRatingTimerExpired(getActivity())) {
                feedsRecyclerAdapter.setSectionDataObject(jsonObjectSectionData, getNewJSONArray(jsonArraySection));
            } else {
                feedsRecyclerAdapter.setSectionDataObject(jsonObjectSectionData, jsonArraySection);
            }


            feedsRecyclerAdapter.notifyDataSetChanged();

            // Set Horizontal List Click Listener
            feedsRecyclerAdapter.setOnHorizontalItemClickListener(this);

            // Set Upcoming Booking Card Click Listener
            feedsRecyclerAdapter.setUpcomingBookingCardClickListener(this);

            // Set Banner Click Listener
            feedsRecyclerAdapter.setBannerClickListener(this);

            // set restaurant feedback callback
            feedsRecyclerAdapter.setRestaurantFeedbackCallback(this);

            // set in app rating callback
            feedsRecyclerAdapter.setInAppRatingCallback(this);

            feedsRecyclerAdapter.setLatestNotificationCallback(this);
        }
    }

    private JSONArray getNewJSONArray(JSONArray originalArray){
        JSONArray result = new JSONArray();
        for(int i=0;i<originalArray.length();i++)
        {

            JSONObject obj= (JSONObject) originalArray.opt(i);
            String sectionType= obj.optString("section_type");
            if(!sectionType.equalsIgnoreCase("in_app_rating"))
            {
                result.put(originalArray.opt(i));
            }
        }

        return result;

    }

    private void addRatingView(JSONArray jsonArraySection, JSONObject jsonObjectSectionData) {
        JSONObject section = new JSONObject();
        try {
            section.put("section_key", "app_rating");
            section.put("section_type", "app_rating");
        } catch (Exception e) {}

        JSONObject sectionData = new JSONObject();
        try {
            JSONObject data = new JSONObject();
            data.put("rating_value", 4);
            data.put("rating_editable", true);

            sectionData.put("data", data);
        } catch (Exception e) {}

        try {
            jsonArraySection.put(3, section);
            jsonObjectSectionData.put("app_rating", sectionData);
        } catch (Exception e) {}
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);
    }

    @Override
    public void onHorizontalListItemClicked(String linkUrl) {
        if (!TextUtils.isEmpty(linkUrl)) {

            MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), linkUrl);

//            if (frag != null && frag instanceof ReferEarnFragment) {
//                if (!TextUtils.isEmpty(DOPreferences.getDinerId(
//                        getActivity().getApplicationContext()))) {
//                    addToBackStack(getActivity(), frag);
//                }
//            }

//            if (frag != null && frag instanceof RestaurantDetailFragment) {
//                addToBackStack(getActivity(), frag);

//            }
        if (frag != null) {
                addToBackStack(getActivity(), frag);
            }
        }
    }

    @Override
    public void onUpcomingBookingCardClick(JSONObject bookingResponse) {

        //Navigate to Booking Details Activity
        if (!TextUtils.isEmpty(bookingResponse.optString("b_id"))) {
            Bundle bundle = new Bundle();
            bundle.putString("b_id", bookingResponse.optString("b_id"));
            //bundle.putString("res_id", bookingResponse.optString("r_id"));
            bundle.putString("type", bookingResponse.optString("b_type"));

            JSONArray dealJsonArray = bookingResponse.optJSONArray("deals");


            if (!(bookingResponse.optString("b_type").equalsIgnoreCase(AppConstant.BOOKING_TYPE_DEAL)) && !(bookingResponse.optString("b_type").equalsIgnoreCase(AppConstant.BOOKING_TYPE_EVENT))) {
                //track event
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA("D_Home","CardBookingClick",bookingResponse.optString("title") + "_" + bookingResponse.optString("r_id"),hMap);

            }

            BookingDetailsFragment bookingDetailFragment = new BookingDetailsFragment();
            bookingDetailFragment.setArguments(bundle);
            addToBackStack(getActivity().getSupportFragmentManager(), bookingDetailFragment);
        }
    }


    @Override
    public void onViewAllBookings(JSONObject bookingResponse) {

        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("D_Home","CardBookingViewAllClick",bookingResponse.optString("title") + "_" + bookingResponse.optString("r_id"),hMap);

        addToBackStack(getActivity(), new BookingsFragment());
    }

    @Override
    public void onUploadBillClick(JSONObject bookingResponse) {

        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("D_Home","CardBookingUploadBillClick",bookingResponse.optString("title") + "_" + bookingResponse.optString("r_id"),hMap);

        UploadBillController utility =
                new UploadBillController(getNetworkManager(), getActivity(),
                        bookingResponse.optString("r_id"), bookingResponse.optString("b_id"));
        utility.setPreviousScreenName(getString(R.string.ga_screen_home));
        utility.validate();
    }

    @Override
    public void onEditBookingClick(JSONObject bookingResponse) {
        // Check if Booking is of Deal
        String bookingType = bookingResponse.optString("b_type");

        if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingType)) {

            // TODO Release v7.2.0 - Decided Not to handle Edit Deal CTA click

            /*// Track Event
            trackEventGA(getString(R.string.ga_screen_home),
                    getString(R.string.ga_action_edit_deals_upcoming_card), bookingResponse.optString("title1") + "_" + bookingResponse.optString("r_id"));

            Bundle confirmationBundle = new Bundle();
            confirmationBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                    bookingResponse.optString("r_id")); // Restaurant Id
            confirmationBundle.putString(AppConstant.BOOKING_ID, bookingResponse.optString("b_id")); // Booking Id
            confirmationBundle.putLong(AppConstant.DATE,
                    AppUtil.convertSecondsToMilliseconds(bookingResponse.optLong(AppConstant.PARAM_DINING_TIMESTAMP, 0L))); // Dining Date Time in milliseconds
            confirmationBundle.putBoolean(AppConstant.IS_EDIT_DEAL, true); // Edit Booking Flag
            confirmationBundle.putString(AppConstant.DINER_EMAIL, bookingResponse.optString("diner_email"));
            confirmationBundle.putString(AppConstant.DINER_NAMES, bookingResponse.optString("diner_name"));
            confirmationBundle.putString(AppConstant.DINER_PHONE, bookingResponse.optString("diner_phone"));
            confirmationBundle.putString(AppConstant.SPCL_REQUEST, bookingResponse.optString("spcl_req"));
            confirmationBundle.putString("FROM", "MyBookings");

            DealsConfirmationFragment dealConfirmationFragment = DealsConfirmationFragment.newInstance(confirmationBundle);
            dealConfirmationFragment.setDealsArrays(bookingResponse.optJSONArray("deals"));

            addToBackStack(getActivity(), dealConfirmationFragment);*/

        } else {
            // Track Event
//            trackEventGA(getString(R.string.ga_screen_home),
//                    getString(R.string.ga_action_edit_upcoming_card), bookingResponse.optString("rest_name"));

            // Set Bundle
            Bundle restaurantBundle = new Bundle();
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                    bookingResponse.optString("r_id")); // Restaurant Id
            restaurantBundle.putString(AppConstant.BUNDLE_BOOKING_ID, bookingResponse.optString("b_id")); // Booking Id
            restaurantBundle.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP,
                    AppUtil.convertSecondsToMilliseconds(bookingResponse.optLong(AppConstant.PARAM_DINING_TIMESTAMP, 0L))); // Dining Date Time in milliseconds
            restaurantBundle.putBoolean(AppConstant.BUNDLE_IS_EDIT_BOOKING, true); // Edit Booking Flag
            restaurantBundle.putInt(AppConstant.BUNDLE_OFFER_ID, Integer.parseInt(bookingResponse.optString("offer_id"))); // Selected Offer
            restaurantBundle.putInt(AppConstant.BUNDLE_GUEST_COUNT,
                    Integer.parseInt(bookingResponse.optString("cnt_covers"))); // Guest Count

            restaurantBundle.putInt(AppConstant.BUNDLE_MALE_COUNT, Integer.parseInt(bookingResponse.optString("cnt_covers_males", "0")));
            restaurantBundle.putInt(AppConstant.BUNDLE_FEMALE_COUNT, Integer.parseInt(bookingResponse.optString("cnt_covers_females", "0")));
            restaurantBundle.putString(AppConstant.BUNDLE_SPECIAL_REQUEST,
                    bookingResponse.optString("spcl_req")); // Special Request

            restaurantBundle.putString(BUNDLE_RESTAURANT_NAME, bookingResponse.optString("title1", ""));
            restaurantBundle.putString(BUNDLE_RESTAURANT_CITY, bookingResponse.optString("city_name", ""));
            restaurantBundle.putString(BUNDLE_RESTAURANT_AREA, bookingResponse.optString("area_name", ""));
            restaurantBundle.putString(BUNDLE_RESTAURANT_LOCALITY, bookingResponse.optString("locality_name", ""));

            String deepLink = DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostRestoDetail +
                    "?q=" + bookingResponse.optString("restaurant_id");
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, deepLink);

            // Add arguments to Fragment
            BookingTimeSlotFragment bookingTimeSlotFragment = new BookingTimeSlotFragment();
            bookingTimeSlotFragment.setArguments(restaurantBundle);

            addToBackStack(getActivity().getSupportFragmentManager(), bookingTimeSlotFragment);
        }
    }

    @Override
    public void onShareBookingDetailsClick(JSONObject bookingResponse) {
        String dealTitle = "";
        String dealCount = "";
        String type = "";

        String bookingType = bookingResponse.optString("b_type");
        if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingType)) {

            type = AppConstant.BOOKING_TYPE_DEAL;

//            trackEventGA(getString(R.string.ga_screen_home), getString(R.string.ga_action_share_deals_upcoming_card), bookingResponse.optString("title1") + "_" + bookingResponse.optString("r_id"));

            // Set Deal Title
            dealTitle = bookingResponse.optString("title1");

            // Get Deal Count
            int deal = 0;
            JSONArray dealJsonArray = bookingResponse.optJSONArray("deals");
            if (dealJsonArray != null) {
                deal = dealJsonArray.length();
            }

            dealCount = Integer.toString(deal);
        } else if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bookingType)) {

            type = AppConstant.BOOKING_TYPE_EVENT;

            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_share_upcoming_events_card),
//                    bookingResponse.optString("title") + "_" + bookingResponse.optString("r_id"));


            int deal = 0;
            JSONArray eventJsonArray = bookingResponse.optJSONArray("events");
            if (eventJsonArray != null) {

                JSONObject obj = eventJsonArray.optJSONObject(0);

                if (obj != null) {

                    //set event title
                    dealTitle = obj.optString("title");
                }

                //set event count
                deal = eventJsonArray.length();
            }

            dealCount = Integer.toString(deal);

        } else {
            // Track Event
//            trackEventGA(getString(R.string.ga_screen_home),
//                    getString(R.string.ga_action_share_upcoming_events_card), bookingResponse.optString("title"));

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("D_Home","CardBookingShareClick",bookingResponse.optString("title") + "_" + bookingResponse.optString("r_id"),hMap);
            type = AppConstant.BOOKING_TYPES;
        }

        long myTimestamp = Long.parseLong(bookingResponse.optString("dining_dt_time_ts")) * 1000;

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy '-' h:mm a");
        String date = sdf.format(myTimestamp);
        String[] parts = date.split("-");

        DOShareDialog doShareDialog = DOShareDialog.
                newInstance(getActivity(), DOPreferences.getDinerFirstName(getContext()),
                        dealTitle, dealCount,
                        bookingResponse.optString("rest_name"),
                        bookingResponse.optString("short_url"),
                        parts[0], //display_date
                        parts[1], //display_time
                        bookingResponse.optString("b_id"),
                        bookingResponse.optString("cnt_covers"),
                        bookingResponse.optString("locality_name"));

        doShareDialog.show(getActivity().getSupportFragmentManager(), type);
    }

    @Override
    public void onGetDirectionsClick(JSONObject bookingResponse) {

        String bookingType = bookingResponse.optString("b_type");
        if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingType)) {

            //track for deals
//            trackEventGA(getString(R.string.ga_screen_home), getString(R.string.ga_action_deals_upcoming_card),
//                    bookingResponse.optString("title1") + "_" + bookingResponse.optString("r_id"));

        } else if (!AppUtil.isStringEmpty(bookingType) &&
                AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bookingType)) {

            //track for events
//            trackEventGA(getString(R.string.ga_screen_home), getString(R.string.ga_action_events_upcoming_card),
//                    bookingResponse.optString("title"));


        } else {

            // Track Event for booking type
//            trackEventGA(getString(R.string.ga_screen_home),
//                    getString(R.string.ga_action_get_directions_upcoming_card), null);
        }

        String restLat = bookingResponse.optString("latitude");
        restLat = AppUtil.setStringEmpty(restLat);

        String restLong = bookingResponse.optString("longitude");
        restLong = AppUtil.setStringEmpty(restLong);

        Intent intent = AppUtil.getMapDirectionsIntent(restLat, restLong);

        if (intent != null) {
            //Navigate to Maps App
            startActivity(intent);
        }
    }

    @Override
    public void onPayNowClick(JSONObject bookingResponse) {
        String alreadyPaid = bookingResponse.optString("already_paid");
        int intAlreadyPaid = ((AppUtil.isStringEmpty(alreadyPaid)) ? 0 : Integer.parseInt(alreadyPaid));

        if (intAlreadyPaid == 0) {
//            trackEventGA(getString(R.string.ga_screen_home),
//                    getString(R.string.ga_action_pay_now_upcoming_card), null);


            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("D_Home","CardBookingPayBillClick",bookingResponse.optString("title") + "_" + bookingResponse.optString("r_id"),hMap);

            Bundle bundle = new Bundle();
            bundle.putString(PaymentConstant.RESTAURANT_NAME,
                    bookingResponse.optString("rest_name"));
            bundle.putString(PaymentConstant.RESTAURANT_ID,
                    bookingResponse.optString("r_id"));
            bundle.putString(PaymentConstant.BOOKING_ID,
                    bookingResponse.optString("b_id"));
            bundle.putString(PaymentConstant.DISPLAY_ID,
                    bookingResponse.optString("disp_id"));

            bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.BOOKING_TYPE);

            PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());

        } else {

            UiUtil.showToastMessage(getContext(), getContext().getResources().getString(R.string.paid_restaurant));
        }
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

//                trackEventGA(getString(R.string.ga_screen_home),
//                        getString(R.string.ga_action_redeem_deal_confirmation_yes), null);

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

//                trackEventGA(getString(R.string.ga_screen_home),
//                        getString(R.string.ga_action_redeem_deal_confirmation_cancel), null);

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

                                Toast.makeText(getContext(),"Deal has been redeemed successfully",Toast.LENGTH_SHORT);
                                // call Booking Details API again ( update Redeem Now text )
                                onNetworkConnectionChanged(true);

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
    public void onDestroyView() {
        super.onDestroyView();

        recyclerViewFeeds = null;
        feedsRecyclerAdapter = null;
       // mAutoScrollHandler = null;
    }

    @Override
    public void onBannerClick(JSONObject bannerJsonObject,int position) {

        // Track Event
//        trackEventGA(getString(R.string.ga_screen_home),
//                getString(R.string.ga_action_card_banner), bannerJsonObject.optString("link_url"));


        HashMap<String,String> hMap=DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("poc",Integer.toString(position));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_home),
                getString(R.string.d_card_banner_click),Integer.toString(position) + "_"+bannerJsonObject.optString("link_url"),hMap);

        //track event for qgraph and apsalar
        HashMap<String, Object> props = new HashMap<>();
        props.put("label", bannerJsonObject.optString("link_url"));
        trackEventQGraphApsalar(getString(R.string.d_card_banner_click),props,true,false,false);

        if (bannerJsonObject != null) {
            if (!TextUtils.isEmpty(bannerJsonObject.optString("link_type")) &&
                    !TextUtils.isEmpty(bannerJsonObject.optString("link_url"))) {



                    // Check for Type
                    if ("app".equalsIgnoreCase(bannerJsonObject.optString("link_type"))) {


                        MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(),
                                bannerJsonObject.optString("link_url"));



                        if (frag != null && (frag instanceof DOPlusFragment || frag instanceof DOPlusRestaurantFragment)) {
                            if (((HomePageMasterFragment)getParentFragment()).getViewPager() != null) {
                                ((HomePageMasterFragment)getParentFragment()).getViewPager().setCurrentItem(1);
                            }
                        }

                        else if (frag != null) {
                            addToBackStack(getActivity(), frag);
                        }
                    } else if ("web_view"
                            .equalsIgnoreCase(bannerJsonObject.optString("link_type"))) {

                        WebViewFragment webViewFragment = new WebViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("title", bannerJsonObject.optString("link_title"));
                        bundle.putString("url", bannerJsonObject.optString("link_url"));
                        webViewFragment.setArguments(bundle);
                        addToBackStack(getActivity().getSupportFragmentManager(), webViewFragment);
                    }

            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAutoScrollHandler != null) {
            mAutoScrollHandler.removeMessages(AUTO_SCROLL);
            mAutoScrollHandler = null;
        }
    }

    @Override
    public void onBannerAutoScroll(final ViewPager pager) {

        if (mAutoScrollHandler == null && pager != null)
            mAutoScrollHandler = new BannerHandler(pager);


    if(mAutoScrollHandler != null) {
      mAutoScrollHandler.removeMessages(AUTO_SCROLL);
      mAutoScrollHandler.sendEmptyMessageDelayed(AUTO_SCROLL, 2000);
     }
    }

    private class BannerHandler extends Handler {

        private ViewPager mPager;

        public BannerHandler(ViewPager pager) {
            this.mPager = pager;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentPosition = mPager.getCurrentItem();
            int position = currentPosition;

            if (mPager.getAdapter() != null && currentPosition == mPager.getAdapter().getCount() - 1) {
                position = 0;
            } else {
                position += 1;
            }

            mPager.setCurrentItem(position, !(position == 0));
            if(mAutoScrollHandler!=null) {

                mAutoScrollHandler.sendEmptyMessageDelayed(AUTO_SCROLL, 5000);
            }
        }
    }

    @Override
    public void askUserAboutRestaurant(JSONObject object) {
        if (object != null) {
            // ask user to give feedback for restaurant info
            if (object.optJSONArray("button") != null
                    && object.optJSONArray("button").length() == 1) {
                if (object.optString("review_popup").equals("1")) {
                    showRNRDialog(object);
                } else if (object.optString("bottom_review_box").equals("1")) {
                    setRestaurantReviewAskUserFeedbackData(object);
                }
            } else if (object.optString("review_popup").equals("1")) {
                showRestaurantReviewAskUserFeedbackDialog(object);
            } else if (object.optString("bottom_review_box").equals("1")) {
                setRestaurantReviewAskUserFeedbackData(object);
            }
        }
    }

    private void showRNRDialog(JSONObject object) {
        try {
            String actionKey = object.optJSONArray("button").getJSONObject(0).optString("action");

            JSONObject value = new JSONObject();
            appendObject(value, object.optJSONObject("review_box").optJSONObject(actionKey));
            appendObject(value, object.optJSONObject("review_data"));

            addValueToJsonObject(value, REVIEW_ACTION, actionKey);
            addValueToJsonObject(value, BOOKING_ID, object.optString(BOOKING_ID));


            // show screen
            RateNReviewDialog rnrDialog = new RateNReviewDialog();
            Bundle bl = new Bundle();
            bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_home));
            bl.putString(INFO_STRING, value.toString());
            rnrDialog.setArguments(bl);
            rnrDialog.setRateNReviewCallback(this);

            showFragment(getActivity().getSupportFragmentManager(), rnrDialog);
        } catch (Exception e) {
            // Exception
        }
    }

    private void setRestaurantReviewAskUserFeedbackData(JSONObject review) {
        if (getView() != null) {
            RestaurantReviewAskUserFeedback askUserFeedback =
                    (RestaurantReviewAskUserFeedback) getView().findViewById(R.id.restaurant_review_ask_user_layout);

            askUserFeedback.setCallback(this);
            if (askUserFeedback != null) {
                askUserFeedback.setVisibility(View.VISIBLE);

                askUserFeedback.setData(review);
                askUserFeedback.inflateData();
            }
        }
    }

    private void resetRestaurantReviewAskUserFeedbackData(JSONObject review) {
        if (getView() != null) {
            RestaurantReviewAskUserFeedback askUserFeedback =
                    (RestaurantReviewAskUserFeedback) getView().findViewById(R.id.restaurant_review_ask_user_layout);

            askUserFeedback.setCallback(null);
            if (askUserFeedback != null) {
                askUserFeedback.setVisibility(View.GONE);

                askUserFeedback.setData(review);
                askUserFeedback.inflateData();
            }
        }
    }

    private void showRestaurantReviewAskUserFeedbackDialog(JSONObject review) {
        RestaurantReviewAskUserFeedbackDialog frag = new RestaurantReviewAskUserFeedbackDialog();
        Bundle bl = new Bundle();
        bl.putString(INFO_STRING, review.toString());
        frag.setArguments(bl);
        frag.setCallback(this);

        showFragment(getActivity().getSupportFragmentManager(), frag);
    }

    @Override
    public void onRatingChange(JSONObject object) {
        RateNReviewDialog frag = new RateNReviewDialog();
        frag.setRateNReviewCallback(this);
        Bundle bl = new Bundle();
        bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_app_rating));
        bl.putString(INFO_STRING, object.toString());
        frag.setArguments(bl);

        showFragment(getActivity().getSupportFragmentManager(), frag);
    }

    @Override
    public void onReviewSubmission() {
        if (feedsRecyclerAdapter != null) {
            feedsRecyclerAdapter.setJsonArray(new JSONArray());
            feedsRecyclerAdapter.notifyDataSetChanged();
        }
        resetRestaurantReviewAskUserFeedbackData(null);
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity()));
    }

    @Override
    public void onRNRError(JSONObject errorObject) {
        if (feedsRecyclerAdapter != null) {
            feedsRecyclerAdapter.setJsonArray(new JSONArray());
            feedsRecyclerAdapter.notifyDataSetChanged();
        }

        resetRestaurantReviewAskUserFeedbackData(null);
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity()));
    }

    @Override
    public void onDialogDismiss() {
        if (feedsRecyclerAdapter != null) {
            feedsRecyclerAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onNewNotification(JSONObject object) {
        ImageView iv = null;
        HomePageMasterFragment frag = ((HomePageMasterFragment)this.getParentFragment());

        if(frag != null && getView()!=null){
            iv = (ImageView) frag.getView().findViewById(R.id.image_button_notification);
        }

        if(object != null && getContext()!=null){
            DOPreferences.setLatestNotificationTime(getContext().getApplicationContext(), object.optLong("latest_notification_ts"));

            long latestNotificationTime = DOPreferences.getLatestNotificationTime(getContext().getApplicationContext());
            long seenNotificationTime = DOPreferences.getNotificationSeenTime(getContext().getApplicationContext());

            if(latestNotificationTime > seenNotificationTime) {
                if(iv != null){
                    iv.setImageResource(R.drawable.new_notification);
                }
            }else{
                if(iv!=null){
                    iv.setImageResource(R.drawable.notification_icon);
                }
            }
        }

    }

    protected void showLoader() {
        ProgressBar progressBar;

        if (getView() != null &&
                (progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader)) != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void hideLoader() {
        ProgressBar progressBar;

        if (getView() != null &&
                (progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader)) != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
