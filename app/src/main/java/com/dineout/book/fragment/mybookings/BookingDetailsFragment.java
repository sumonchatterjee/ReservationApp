package com.dineout.book.fragment.mybookings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.dialogs.GenericShareDialog;
import com.dineout.book.dialogs.MoreDialog;
import com.dineout.book.dialogs.PhoneDialog;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.dialogs.RatingDialog;
import com.dineout.book.fragment.bookingflow.BookingTimeSlotFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.maps.MapFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.fragment.InvoiceFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.recycleradapters.NewBookingDetailsRecyclerAdapter;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_NAME;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_POSITIVE_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_RATING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_TEXT;
import static com.dineout.recycleradapters.util.RateNReviewUtil.appendObject;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_AREA;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_CITY;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_ID;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_LOCALITY;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_NAME;

public class BookingDetailsFragment extends AuthenticationWrapperJSONReqFragment
        implements NewBookingDetailsRecyclerAdapter.OnCTAClickedListener,
        RateNReviewUtil.RateNReviewCallbacks {

    private static final int REQUEST_CODE_BOOKING_LAYOUT = 888;
    private RecyclerView bookingRecyclerView;
    private String bookingID;
    private String source, type, bookingType;
    private NewBookingDetailsRecyclerAdapter bookingDetailsRecyclerAdapter;
    private int REQUEST_CODE_CANCEL_BOOKING = 889;
    private int REQUEST_CODE_INVOICE = 899;
    private int REQUEST_CODE_REDEEM_DEAL = 890;
    private JSONObject cDataJsonObject;
    private JSONObject reviewJsonObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            //track screen
            source = getArguments().getString("previous_fragment");
            type = getArguments().getString("type");
            bookingType = getArguments().getString("bookingType");

            if (!AppUtil.isStringEmpty(source)) {
                if (source.equalsIgnoreCase(getResources().getString(R.string.text_booking_confirmation)) || source.equalsIgnoreCase(getResources().getString(R.string.events_confirmation))) {
                    //trackScreenToGA(getString(R.string.ga_screen_thankyou));
                    trackScreenName(getString(R.string.ga_screen_thankyou));

                } else {
                    if (!AppUtil.isStringEmpty(type)) {

                        if (type.equalsIgnoreCase("deal")) {
                            //trackScreenToGA(getString(R.string.ga_screen_deal_booking_details));
                            trackScreenName("B_BookingDetail_D");

                        } else if (type.equalsIgnoreCase("event")) {
                            //trackScreenToGA(getString(R.string.ga_screen_event_booking_details));
                            trackScreenName("B_BookingDetail_E");

                        } else {
                            // trackScreenToGA(getString(R.string.ga_screen_booking_details));
                            trackScreenName("B_BookingDetail_B");
                        }
                    }
                }
            }

            int showRatingDialog = getArguments().getInt("rating_dialog");

            if (showRatingDialog == 1) {
//                showPlayStoreDialog();
            }
        }

        bookingDetailsRecyclerAdapter = new NewBookingDetailsRecyclerAdapter(getContext(), getImageLoader());
        bookingDetailsRecyclerAdapter.setOnCTAClickedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_details, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeViews();

        initializeData(); //get data from bundle, send from other screens

        setToolbarTitle("Booking Detail");


    }

    @Override
    public void onStart() {
        super.onStart();
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
    }

    private void initializeViews() {
        bookingRecyclerView = (RecyclerView) getView().findViewById(R.id.booking_detail_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager
                (getActivity(), LinearLayoutManager.VERTICAL, false);
        bookingRecyclerView.setLayoutManager(linearLayoutManager);
        bookingRecyclerView.setAdapter(bookingDetailsRecyclerAdapter);
    }

    private void initializeData() {
        if (getArguments() != null) {
            bookingID = getArguments().getString("b_id");
        }
    }

    private void showPlayStoreDialog() {
        if (!DOPreferences.showPlayStoreDialog(getActivity())) {
            return;
        }
        MasterDOFragment.showFragment(getActivity().getSupportFragmentManager(), new RatingDialog());
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        getAndShowBookingDetails();
    }


    void getAndShowBookingDetails() {
        if (TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) {
            loginGetBookingDetails();

        } else {
            showLoader();

            getNetworkManager().jsonRequestGet(REQUEST_CODE_BOOKING_LAYOUT, AppConstant.URL_GET_BOOKING_DETAIL,
                    ApiParams.getBookingDetailParams(bookingID), this, this, false);
        }
    }


    private void loginGetBookingDetails() {
        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }


    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null)
            return;


        if (request.getIdentifier() == REQUEST_CODE_BOOKING_LAYOUT) {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    if (responseObject.optJSONObject("output_params") != null) {
                        // Get Data
                        JSONObject jsonObjectData = responseObject.optJSONObject("output_params").optJSONObject("data");

                        if (jsonObjectData != null) {
                            // Set C DATA
                            cDataJsonObject = jsonObjectData.optJSONObject("c_data");
                            reviewJsonObject = jsonObjectData.optJSONObject("review");

                            // Get Section
                            JSONArray jsonArraySection = jsonObjectData.optJSONArray("section");
                            JSONObject jsonObjectSectionData = jsonObjectData.optJSONObject("section_data");

                            if (jsonArraySection != null && jsonArraySection.length() > 0 && jsonObjectSectionData != null) {
                                prepareItem(jsonArraySection, jsonObjectSectionData);
                            }
                        }
                    }
                } else if (responseObject.optJSONObject("res_auth") != null &&
                        !responseObject.optJSONObject("res_auth").optBoolean("status")) {

                    // Ask User to Login
                    loginGetBookingDetails();

                } else {
                    // Show Message
                    UiUtil.showToastMessage(getActivity().getApplicationContext(),
                            getString(R.string.text_unable_fetch_details));
                }
            } else {
                // Show Message
                UiUtil.showToastMessage(getActivity().getApplicationContext(),
                        getString(R.string.text_general_error_message));
            }
        } else if (request.getIdentifier() == REQUEST_CODE_CANCEL_BOOKING) {
            if (responseObject != null && responseObject.optBoolean("status")) {
                UiUtil.showSnackbar(getView(), getString(R.string.text_booking_cancelled_success), 0);

                backNavigation();

            } else {
                UiUtil.showSnackbar(getView(), getString(R.string.text_please_try_again), 0);
            }
        }
    }

    private void recordCancelBookingEvent(JSONObject ctaObject) {

        HashMap<String, Object> props = new HashMap<>();
        props.put("RestaurantName", ctaObject.optString("restaurant_name"));
        props.put("RestaurantAddress", ctaObject.optString("address"));
        props.put("NumberOfPAX", ctaObject.optString("cnt_covers"));
        props.put("Date", ctaObject.optString("dining_dt_time"));
        props.put("Slot", ctaObject.optString("slot"));

       // trackEventGA(getString(R.string.ga_screen_booking_details), props);
    }


    private void prepareItem(JSONArray jsonArraySection, JSONObject jsonObjectSectionData) {
        if (bookingDetailsRecyclerAdapter != null) {
            bookingDetailsRecyclerAdapter.setBookingDetailObject(jsonArraySection, jsonObjectSectionData);
            bookingDetailsRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCTAClicked(JSONObject jsonObject) {
        if (jsonObject != null) {
            if (jsonObject.has("active")) {
                int isActive = jsonObject.optInt("active");
                if (isActive == 0) {
                    Toast.makeText(getContext(), jsonObject.optString("error_msg"), Toast.LENGTH_SHORT).show();
                } else {
                    handleActiveCTAs(jsonObject);
                }
            } else {
                handleActiveCTAs(jsonObject);
            }

        }
    }

    private void handleActiveCTAs(JSONObject jsonObject) {
        JSONObject clickedCTAObject = jsonObject.optJSONObject("cta_data");
        int ctaNum = jsonObject.optInt("cta");
        redirectionCTA(ctaNum, clickedCTAObject);
    }


    public void redirectionCTA(int num, JSONObject ctaObject) {
        HashMap<String, Object> msp = new HashMap<>();
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        switch (num) {

            case AppConstant.CTA_RESTAURANT_DETAIL:
                msp.put("label", ctaObject.optString("cta_detail"));
                trackEventQGraphApsalar("BookingDetailCTAClick", msp, true, true, true);
                trackEventRestaurantDetail(ctaObject);
                navigateToRestaurantDetail(ctaObject);
                break;


            case AppConstant.CTA_EDIT_REVIEW:
//                trackEventGA(getString(R.string.ga_screen_booking_details),
//                        "edit review", ctaObject.optString("restaurant_id"));
                msp.put("category", "B_BookingDetail");
                msp.put("label", ctaObject.optString("cta_detail"));
                msp.put("action", "BookingDetailCTAClick");
                trackEventQGraphApsalar("BookingDetailCTAClick", msp, true, true, true);

                if(hMap!=null){
                    hMap.put("category", "B_BookingDetail");
                    hMap.put("label", ctaObject.optString("cta_detail"));
                    hMap.put("action", "BookingDetailCTAClick");
                }

                trackEventForCountlyAndGA("B_BookingDetail","BookingDetailCTAClick",ctaObject.optString("cta_detail"),hMap);

                JSONObject obj = new JSONObject();
                try {
                    if (reviewJsonObject != null && reviewJsonObject.optJSONArray("button") != null) {
                        int actionKey = Integer.valueOf(REVIEW_POSITIVE_ACTION);

                        // append review box data using action key
                        if (reviewJsonObject.optJSONObject("review_box") != null) {
                            JSONObject actionValue = reviewJsonObject.optJSONObject("review_box").optJSONObject(
                                    String.valueOf(actionKey));

                            appendObject(obj, actionValue);
                        }

                        // append review data
                        appendObject(obj, reviewJsonObject.optJSONObject("review_data"));

                        // put action
//                        obj.put(REVIEW_ACTION, actionKey);
                    }

                    obj.put(BOOKING_ID, ctaObject.optString("b_id"));
                    obj.put(RESTAURANT_NAME, ctaObject.optString("restaurant_name"));
                    obj.put(RESTAURANT_ID, ctaObject.optString("restaurant_id"));

                    JSONObject reviewObj = ctaObject.optJSONObject("review_data");
                    if (reviewObj != null) {
                        obj.put(REVIEW_ID, reviewObj.optInt("rv_id"));
                        obj.put(REVIEW_TEXT, reviewObj.optString("rv_desc"));
                        obj.put(REVIEW_RATING, reviewObj.optInt("rv_overall_rating"));

                    }
                } catch (Exception e) {
                    // Exception
                }

                RateNReviewDialog RNRDialog = new RateNReviewDialog();
                Bundle bl = new Bundle();
                bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_booking_detail_b));
                bl.putString(INFO_STRING, obj.toString());
                RNRDialog.setArguments(bl);
                RNRDialog.setRateNReviewCallback(this);

                showFragment(getActivity().getSupportFragmentManager(), RNRDialog);


//                JSONObject reviewObj = ctaObject.optJSONObject("review_data");
//                if (reviewObj != null) {
//                    // Edit Action
//                    final Bundle reviewBundle = new Bundle();
//                    reviewBundle.putString("rest_name", ctaObject.optString("restaurant_name"));
//                    reviewBundle.putString("resId", ctaObject.optString("restaurant_id"));
//                    reviewBundle.putString("b_id", ctaObject.optString("b_id"));
//                    reviewBundle.putInt(AppConstant.BUNDLE_REVIEW_ID, reviewObj.optInt("rv_id"));
//                    reviewBundle.putString(AppConstant.BUNDLE_REVIEW_TEXT, reviewObj.optString("rv_desc"));
//                    reviewBundle.putInt(AppConstant.BUNDLE_REVIEW_RATING, reviewObj.optInt("rv_overall_rating"));
//
//                    WriteReviewFragment writeReviewFragment = new WriteReviewFragment();
//                    writeReviewFragment.setArguments(reviewBundle);
//                    addToBackStack(getActivity(), writeReviewFragment);
//
//                }

                break;


            case AppConstant.CTA_RESERVE_AGAIN:
                trackReserveAgain(ctaObject);
                navigateToRestaurantDetail(ctaObject);
                break;

            case AppConstant.CTA_CALL_MANAGER:

                trackEventCallToManager(ctaObject);
                JSONArray managerNumberArr = ctaObject.optJSONArray("manager_number");
                if (managerNumberArr != null) {
                    int length = managerNumberArr.length();

                    if (length == 1) {
                        handleDirectPhoneCall(managerNumberArr.optString(0));
                    } else if (length > 1) {
                        ArrayList<String> phoneListdata = new ArrayList<String>();
                        for (int i = 0; i < managerNumberArr.length(); i++) {
                            phoneListdata.add(managerNumberArr.opt(i).toString());

                        }
                        PhoneDialog phoneDialog = new PhoneDialog(phoneListdata, getActivity());
                        phoneDialog.show(getFragmentManager(), "phoneDialog");
                    }
                }
                break;

            case AppConstant.CTA_VIEW_INVOICE:
                String invoiceNumber = ctaObject.optString("transaction_id");
                if (!TextUtils.isEmpty(invoiceNumber)) {
                    getPaymentInvoice(invoiceNumber);
                }
                break;


            case AppConstant.CTA_WEBVIEW:
                //track event
                if ("upcoming".equalsIgnoreCase(getArguments().getString("bookingType"))) {
                    // Track Event
                    trackEventForCountlyAndGA("D_HistoryBookingDetail",
                            "SelectedOfferViewClick", bookingID, hMap);
                } else {
                    trackEventForCountlyAndGA("B_BookingDetail_B",
                            "SelectedOfferClick", bookingID, hMap);
                }

                String webUrl = ctaObject.optString("link");
                String title = ctaObject.optString("title");
                WebViewFragment webViewFragment = new WebViewFragment();

                Bundle bundle = new Bundle();
                bundle.putString("title", title);
                bundle.putString("url", webUrl);

                webViewFragment.setArguments(bundle);

                addToBackStack(getFragmentManager(), webViewFragment);
                break;

            case AppConstant.CTA_GET_DIRECTION:

                trackGetDirection(ctaObject);
                String latitude = ctaObject.optString("latitude");
                String longitude = ctaObject.optString("longitude");

                Intent intent = AppUtil.getMapDirectionsIntent(latitude, longitude);
                startActivity(intent);
                break;

            case AppConstant.CTA_CANCEL_BOOKING:
                doCancelBooking(ctaObject);
                break;

            case AppConstant.CTA_WRITE_REVIEW:
                writeReview(ctaObject);
                break;

            case AppConstant.CTA_EDIT_BOOKING:
                editBookingDetails(ctaObject);
                break;

            case AppConstant.CTA_RIDE_WITH_UBER:
                openMap(ctaObject);
                break;

            case AppConstant.CTA_PAY_NOW:
                payNow(ctaObject);
                break;

            case AppConstant.CTA_SHARE_BOOKING:
                //track event
                if ("upcoming".equalsIgnoreCase(getArguments().getString("bookingType"))) {

                    trackEventForCountlyAndGA("D_UpcomingBookingDetail",
                            "BookingDetailSecondaryCTAClick","ShareBooking"+"_"+ bookingID, hMap);
                } else {

                    trackEventForCountlyAndGA("B_BookingDetail_B",
                            "BookingDetailSecondaryCTAClick","ShareBooking"+"_"+ bookingID, hMap);
                }

                GenericShareDialog shareDialog = GenericShareDialog.getInstance(ctaObject.toString());
                shareDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                break;

            case AppConstant.CTA_UPLOAD_BILL:
                onUploadBillClick(ctaObject);
                break;

            case AppConstant.CTA_REDEEM:
                handleRedeemDealAction(ctaObject);
                break;

            case AppConstant.CTA_DEEPLINK:
                MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), ctaObject.optString("deep_link"));

                if (frag != null) {
                    addToBackStack(getActivity(), frag);
                }
                break;
        }
    }

    private void payNow(JSONObject ctaObject) {

        // Track Event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_BookingDetail_B",
                "BookingDetailCTAClick", getString(R.string.ga_action_pay_now), hMap);


//        trackEventGA(getString(R.string.ga_screen_booking_details),
//                getString(R.string.ga_action_pay_now), ctaObject.optString("restaurant_name"));

        Bundle bundle = new Bundle();
        bundle.putString(PaymentConstant.RESTAURANT_NAME,
                ctaObject.optString("restaurant_name"));
        bundle.putString(PaymentConstant.RESTAURANT_ID,
                ctaObject.optString("restaurant_id"));
        bundle.putString(PaymentConstant.BOOKING_ID,
                ctaObject.optString("b_id"));
        bundle.putString(PaymentConstant.DISPLAY_ID,
                ctaObject.optString("disp_id"));
        bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.BOOKING_TYPE);

        PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());
    }

    private void navigateToRestaurantDetail(JSONObject ctaObject) {
        String rest_id = ctaObject.optString("restaurant_id");
        if (!TextUtils.isEmpty(rest_id)) {
            RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();

            // Set Bundle
            Bundle restaurantBundle = new Bundle();
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                    rest_id);
            restaurantDetailFragment.setArguments(restaurantBundle);
            addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);

        }
    }

    private void handleDirectPhoneCall(String phoneNumber) {
        //If Permission is Granted
        if (PermissionUtils.handleCallPermission(getActivity())) {
            //Perform Calling Function
            performCall(phoneNumber);
        }
    }

    public void performCall(String phoneNumber) {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + phoneNumber));

        try {
            phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(phoneIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            UiUtil.showToastMessage(getActivity().getApplicationContext(),
                    getString(R.string.call_failed_response));
        }
    }


    private void doCancelBooking(final JSONObject jsonObject) {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

        if ("upcoming".equalsIgnoreCase(getArguments().getString("bookingType"))) {
            // Track Event
            trackEventForCountlyAndGA("D_UpcomingBookingDetail",
                    "BookingDetailSecondaryCTAClick","CancelBooking"+"_"+ bookingID, hMap);
        } else {
            // Track Event
            trackEventForCountlyAndGA("B_BookingDetail_B",
                    "BookingDetailSecondaryCTAClick","CancelBooking"+"_"+ bookingID, hMap);
        }

        recordCancelBookingEvent(jsonObject);


        MoreDialog dialog = new MoreDialog("Cancel Booking?", getResources().getString(R.string.booking_cancel_msg), getActivity(), true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (jsonObject != null) {

                    cancelBooking(jsonObject.optString("b_id"));
                }
            }
        });

        dialog.show(getActivity().getSupportFragmentManager(), "booking_detail");
    }

    private void cancelBooking(String bookingId) {
        showLoader();
        getNetworkManager().jsonRequestGet(REQUEST_CODE_CANCEL_BOOKING, AppConstant.URL_CANCEL_BOOKING,
                ApiParams.getCancelBookingParams(bookingId), this, this, false);
    }

    private void writeReview(JSONObject ctaObject) {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

        if ("history".equalsIgnoreCase(bookingType)) {

            // Track Event
            trackEventForCountlyAndGA("D_HistoryBookingDetail",
                    "BookingDetailSecondaryCTAClick","WriteReview"+"_"+ bookingID, hMap);

        } else {

            // Track Event
            trackEventForCountlyAndGA("B_BookingDetail_B",
                    "BookingDetailSecondaryCTAClick","WriteReview"+"_"+ bookingID, hMap);

        }

        JSONObject obj = new JSONObject();
        try {
            if (reviewJsonObject != null && reviewJsonObject.optJSONArray("button") != null) {
                int actionKey;
                if (reviewJsonObject.optJSONArray("button").getJSONObject(0).optString("text").equalsIgnoreCase("Yes")) {
                    actionKey = reviewJsonObject.optJSONArray("button").getJSONObject(0).optInt("action");
                } else {
                    actionKey = reviewJsonObject.optJSONArray("button").getJSONObject(1).optInt("action");
                }
                if (reviewJsonObject.optJSONObject("review_box") != null) {
                    JSONObject actionValue = reviewJsonObject.optJSONObject("review_box").optJSONObject(
                            String.valueOf(actionKey));

                    appendObject(obj, actionValue);
                }
            }

            if (ctaObject != null) {
                obj.put(BOOKING_ID, bookingID);
                obj.put(RESTAURANT_NAME, ctaObject.optString("restaurant_name"));
                obj.put(RESTAURANT_ID, ctaObject.optString("restaurant_id"));
            }
        } catch (Exception e) {
            // Exception
        }

        RateNReviewDialog RNRDialog = new RateNReviewDialog();
        Bundle bl = new Bundle();
        bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_booking_detail_b));
        bl.putString(INFO_STRING, obj.toString());
        RNRDialog.setArguments(bl);

        showFragment(getActivity().getSupportFragmentManager(), RNRDialog);

//        Bundle bundle = new Bundle();
//        if (ctaObject != null) {
//            bundle.putString(WriteReviewFragment.BOOKING_ID, ctaObject.optString("b_id"));
//            bundle.putString(WriteReviewFragment.RESTAURANT_NAME, ctaObject.optString("restaurant_name"));
//            bundle.putString(WriteReviewFragment.RESTAURANT_ID, ctaObject.optString("restaurant_id"));
//        }
//
//        WriteReviewFragment writeReviewFragment = new WriteReviewFragment();
//        writeReviewFragment.setArguments(bundle);
//
//        addToBackStack(getActivity(), writeReviewFragment);
    }

    private void editBookingDetails(JSONObject ctaObject) {

        // Get Instance

        if (ctaObject != null) {

            String bookingType = ctaObject.optString("b_type");
            if (!AppUtil.isStringEmpty(bookingType)) {

                if (bookingType.equalsIgnoreCase("booking")) {
                    // Track Event
//                    trackEventGA(getString(R.string.ga_screen_booking_details),
//                            getString(R.string.ga_action_edit_booking), null);

                    // Set Bundle

                    //profileImages [X] - NOT COMING
                    //BUNDLE_RESTAURANT_CUISINE_NAME [X] - NOT COMING
                    //BUNDLE_RESTAURANT_CUISINELIST [X] - NOT COMING
                    //BUNDLE_RESTAURANT_TAGLIST [X] - NOT COMING

                    Bundle restaurantBundle = new Bundle();
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, ctaObject
                            .optString("restaurant_id")); // Restaurant Id
                    restaurantBundle.putString(AppConstant.BUNDLE_BOOKING_ID, ctaObject
                            .optString("b_id")); // Booking Id
                    restaurantBundle.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP, AppUtil
                            .convertSecondsToMilliseconds(ctaObject
                                    .optLong("dining_dt_time_ts", 0L))); // Dining Date Time in milliseconds
                    restaurantBundle
                            .putBoolean(AppConstant.BUNDLE_IS_EDIT_BOOKING, true); // Edit Booking Flag
                    restaurantBundle.putInt(AppConstant.BUNDLE_OFFER_ID, ctaObject
                            .optInt("offer_id")); // Selected Offer
                    restaurantBundle.putInt(AppConstant.BUNDLE_GUEST_COUNT,
                            ctaObject.optInt("cnt_covers_females") +
                                    ctaObject.optInt("cnt_covers_males")); // Guest Count
                    restaurantBundle.putInt(AppConstant.BUNDLE_MALE_COUNT, ctaObject
                            .optInt("cnt_covers_males")); // male Count

                    restaurantBundle.putInt(AppConstant.BUNDLE_FEMALE_COUNT, ctaObject
                            .optInt("cnt_covers_females"));//female count
                    restaurantBundle.putString(AppConstant.BUNDLE_SPECIAL_REQUEST,
                            ctaObject.optString("spcl_req")); // Special Request

                    restaurantBundle.putString(BUNDLE_RESTAURANT_NAME, cDataJsonObject.optString("restaurant_name", ""));
                    restaurantBundle.putString(BUNDLE_RESTAURANT_CITY, cDataJsonObject.optString("city_name", ""));
                    restaurantBundle.putString(BUNDLE_RESTAURANT_AREA, cDataJsonObject.optString("area_name", ""));
                    restaurantBundle.putString(BUNDLE_RESTAURANT_LOCALITY, cDataJsonObject.optString("locality_name", ""));

                    String deepLink = DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostRestoDetail +
                            "?q=" + cDataJsonObject.optString("restaurant_id");
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, deepLink);

                    // Add Bundle in Fragment
                    BookingTimeSlotFragment bookingTimeSlotFragment = new BookingTimeSlotFragment();
                    bookingTimeSlotFragment.setArguments(restaurantBundle);

                    addToBackStack(getActivity().getSupportFragmentManager(), bookingTimeSlotFragment);

                } else if (bookingType.equalsIgnoreCase("deal")) {

                    // TODO Release v7.2.0 - Decided Not to handle Edit Deal CTA click

                    // Track Event
                    /*trackEventGA(getString(R.string.ga_screen_deal_booking_details),
                            getString(R.string.ga_action_edit_booking), null);


                    long dateTime = 0;
                    String date = ctaObject.optString("dining_dt_time_ts", "0");

                    if (!TextUtils.isEmpty(date)) {
                        dateTime = Long.parseLong(date);
                    }
                    JSONArray dealArray = ctaObject.optJSONArray("deals");

                    Bundle b = new Bundle();
                    b.putString(AppConstant.BUNDLE_RESTAURANT_ID, ctaObject
                            .optString("restaurant_id"));
                    b.putLong(AppConstant.DATE, AppUtil.convertSecondsToMilliseconds(dateTime));
                    b.putBoolean(AppConstant.IS_EDIT_DEAL, true);
                    b.putString(AppConstant.BOOKING_ID, ctaObject.optString("b_id"));
                    b.putString(AppConstant.DINER_NAMES, ctaObject.optString("diner_name"));
                    b.putString(AppConstant.DINER_EMAIL, ctaObject.optString("diner_email"));
                    b.putString(AppConstant.DINER_PHONE, ctaObject.optString("diner_phone"));
                    b.putString(AppConstant.SPCL_REQUEST, ctaObject.optString("spcl_req"));

                    DealsConfirmationFragment deals = DealsConfirmationFragment.newInstance(b);
                    deals.setDealsArrays(dealArray);

                    addToBackStack(getActivity(), deals);*/
                }
            }
        }
    }

    void openMap(JSONObject ctaObject) {

        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

        if ("upcoming".equalsIgnoreCase(getArguments().getString("bookingType"))) {
            if (!AppUtil.isStringEmpty(bookingType)) {

                if ("deal".equalsIgnoreCase(ctaObject.optString("b_type"))) {

                    trackEventForCountlyAndGA("D_UpcomingBookingDetail_D",
                            "BookingDetailSecondaryCTAClick","RideWithUber"+"_"+ bookingID, hMap);
                } else {
                    // Track Event
                    trackEventForCountlyAndGA("D_UpcomingBookingDetail_B",
                            "BookingDetailSecondaryCTAClick","RideWithUber"+"_"+ bookingID, hMap);
                }
            }

        } else {
            if ("deal".equalsIgnoreCase(ctaObject.optString("b_type"))) {

                trackEventForCountlyAndGA("B_BookingDetail_D",
                        "BookingDetailSecondaryCTAClick","RideWithUber"+"_"+ bookingID, hMap);

            } else {
                // Track Event
                trackEventForCountlyAndGA("B_BookingDetail_B",
                        "BookingDetailSecondaryCTAClick","RideWithUber"+"_"+ bookingID, hMap);

            }
        }

        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_DISTANCE, "");
        bundle.putString(BUNDLE_RESTAURANT_NAME, ctaObject.optString("restaurant_name"));
        bundle.putString(BUNDLE_RESTAURANT_ID, ctaObject.optString("restaurant_id"));
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ADDRESS, ctaObject.optString("address"));
        //bundle.putInt(AppConstant.BUNDLE_ACCEPT_PAYMENT, acceptPayment);
        bundle.putString(AppConstant.BUNDLE_RECENCY, ctaObject.optString("recency"));
        bundle.putString(AppConstant.BUNDLE_RATING, ctaObject.optString("avg_rating"));
        bundle.putString(AppConstant.BUNDLE_DESTINATION_LATITUDE, ctaObject.optString("latitude"));
        bundle.putString(AppConstant.BUNDLE_DESTINATION_LONGITUDE, ctaObject.optString("longitude"));

        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        addToBackStack(getActivity(), mapFragment);
    }

    private void onUploadBillClick(JSONObject ctaObject) {
        // Track Event
        //trackEventGA(getString(R.string.ga_screen_booking_details), "UploadBill", ctaObject.optString("restaurant_id"));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_BookingDetail_B",
                "BookingDetailCTAClick", "UploadBill", hMap);


        UploadBillController utility =
                new UploadBillController(getNetworkManager(), getActivity(),
                        ctaObject.optString("restaurant_id"), bookingID);
        utility.setPreviousScreenName(getString(R.string.ga_screen_booking_details));

        utility.validate();
    }


    private void getPaymentInvoice(String transID) {

        showLoader();

        Map<String, String> param = new HashMap<>();
        param.put("diner_id", DOPreferences.getDinerId(getContext()));
        param.put("trans_id", transID);

        getNetworkManager().stringRequestPost(REQUEST_CODE_INVOICE,
                AppConstant.URL_GET_PAYMENT_INVOICE, param, new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

                        hideLoader();

                        handleInvoiceResponse(responseObject);
                    }
                }, this, false);
    }

    private void handleInvoiceResponse(String responseObject) {

        if (getActivity() == null || getView() == null)
            return;

        try {
            JSONObject resp = new JSONObject(responseObject);
            if (resp.optBoolean("status")) {

                JSONObject outputParam = resp.optJSONObject("output_params");
                if (outputParam != null) {
                    JSONObject data = outputParam.optJSONObject("data");

                    Bundle bundle = new Bundle();
                    bundle.putString(InvoiceFragment.INVOICE_RESPONSE_KEY, data.toString());
                    bundle.putString("source", "Booking Detail");
                    InvoiceFragment invoiceFragment = InvoiceFragment.newInstance(bundle);
                    addToBackStack(getFragmentManager(), invoiceFragment);

                }
            } else {
                handleErrorResponse(new JSONObject(responseObject));
            }
        } catch (Exception e) {
            // exception
        }
    }

    private void handleRedeemDealAction(JSONObject ctaObject) {
        //track event
        //trackEventGA(getString(R.string.ga_screen_deal_booking_details), getString(R.string.ga_action_redeem_deal), null);

        // Ask for Confirmation
        askForRedeemDealConfirmation(ctaObject);
    }

    private void askForRedeemDealConfirmation(final JSONObject jsonObject) {
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

                // trackEventGA(getString(R.string.ga_screen_deal_booking_details), getString(R.string.ga_action_redeem_deal_confirmation_yes), null);

                if (jsonObject != null) {
                    // Proceed with Redeem Deal
                    proceedWithRedeemDeal(jsonObject.optString("b_id"));
                }
            }
        });

        builder.setNegativeButton(R.string.button_dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // trackEventGA(getString(R.string.ga_screen_deal_booking_details), getString(R.string.ga_action_redeem_deal_confirmation_cancel), null);

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

                            if (getActivity() == null || getView() == null)
                                return;

                            JSONObject responseJsonObject = new JSONObject(responseObject);

                            // Check Response
                            if (responseJsonObject != null) {
                                if (responseJsonObject.optBoolean("status")) { // Success
                                    Toast.makeText(getContext(), "Deal has been redeemed successfully", Toast.LENGTH_SHORT);
                                    // call Booking Details API again ( update Redeem Now text )
                                    onNetworkConnectionChanged(true);

                                } else {
//                                    //track event
//                                    trackEventGA(getString(R.string.ga_screen_deal_booking_details), getString(R.string.ga_action_redeem_deal_error), null);
//
//                                    // Show Error Message
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                                    builder.setTitle(R.string.text_error);
//                                    builder.setMessage(responseJsonObject.optString("error_msg"));
//                                    builder.setCancelable(false);
//                                    builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            // Dismiss Dialog
//                                            dialog.dismiss();
//                                        }
//                                    });
//
//                                    // Show Alert Dialog
//                                    builder.show();
                                    handleErrorResponse(new JSONObject(responseObject));
                                }
                            }
                        } catch (JSONException e) {
                            // Exception
                        }
                    }
                }, this, false);
    }

    @Override
    public void handleNavigation() {
        backNavigation();
    }

    @Override
    public boolean onPopBackStack() {
        backNavigation();
        return true;
    }

    private void backNavigation() {
        if (!AppUtil.isStringEmpty(source)) {
            if (source.equalsIgnoreCase(getResources().getString(R.string.text_booking_confirmation)) ||
                    source.equalsIgnoreCase(getResources().getString(R.string.events_confirmation))) {

                popToHome(getActivity());
            } else {
                popBackStack(getActivity().getSupportFragmentManager());
            }
        } else {
            popBackStack(getActivity().getSupportFragmentManager());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bookingRecyclerView = null;
        bookingID = null;
        source = null;
        type = null;
        bookingDetailsRecyclerAdapter = null;
        cDataJsonObject = null;
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject object) {
        super.loginFlowCompleteSuccess(object);

        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
    }

    public void handleErrorResponse(JSONObject responseObject) {
        super.handleErrorResponse(responseObject);
    }


    //track event for countly and ga for call manager click
    private void trackEventCallToManager(JSONObject ctaObject) {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if ("upcoming".equalsIgnoreCase(getArguments().getString("bookingType"))) {
            if (!AppUtil.isStringEmpty(bookingType)) {

                if ("deal".equalsIgnoreCase(ctaObject.optString("b_type"))) {

                    trackEventForCountlyAndGA("D_UpcomingBookingDetail_D",
                            "BookingDetailSecondaryCTAClick","CallManager"+"_"+ bookingID, hMap);
                } else {
                    // Track Event
                    trackEventForCountlyAndGA("D_UpcomingBookingDetail_B",
                            "BookingDetailSecondaryCTAClick","CallManager"+"_"+ bookingID, hMap);
                }
            }

        } else {
            if ("deal".equalsIgnoreCase(ctaObject.optString("b_type"))) {

                trackEventForCountlyAndGA("B_BookingDetail_D",
                        "BookingDetailSecondaryCTAClick","CallManager"+"_"+ bookingID, hMap);

            } else {
                // Track Event
                trackEventForCountlyAndGA("B_BookingDetail_B",
                        "BookingDetailSecondaryCTAClick","CallManager"+"_"+ bookingID, hMap);

            }
        }
    }

    //track event for countly and ga for reserve again

    private void trackReserveAgain(JSONObject ctaObject) {
        if(!TextUtils.isEmpty(bookingType)) {
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if ("history".equalsIgnoreCase(bookingType)) {
                // Track Event
                trackEventForCountlyAndGA("D_HistoryBookingDetail",
                        "BookingDetailSecondaryCTAClick", "ReserveAgain" + "_" + bookingID, hMap);

            } else {
                // Track Event
                trackEventForCountlyAndGA("B_BookingDetail_B",
                        "BookingDetailSecondaryCTAClick", "ReserveAgain" + "_" + bookingID, hMap);

            }
        }
    }


    //track event for countly and ga for get direction
    private void trackGetDirection(JSONObject ctaObject) {
        String bookingType = ctaObject.optString("b_type");
        HashMap<String, String> hMaps = DOPreferences.getGeneralEventParameters(getContext());

        if ("upcoming".equalsIgnoreCase(getArguments().getString("bookingType"))) {
            if (!AppUtil.isStringEmpty(bookingType)) {

                if ("deal".equalsIgnoreCase(bookingType)) {

                    trackEventForCountlyAndGA("D_UpcomingBookingDetail_D",
                            "BookingDetailSecondaryCTAClick","GetDirections"+"_"+ bookingID, hMaps);

                } else {
                    // Track Event
                    trackEventForCountlyAndGA("D_UpcomingBookingDetail_B",
                            "BookingDetailSecondaryCTAClick","GetDirections"+"_"+ bookingID, hMaps);
                }
            }
        } else {
            if (!AppUtil.isStringEmpty(bookingType)) {
                if ("deal".equalsIgnoreCase(bookingType)) {

                    trackEventForCountlyAndGA("B_BookingDetail_D",
                            "BookingDetailSecondaryCTAClick","GetDirections"+"_"+ bookingID, hMaps);

                } else {
                    // Track Event
                    trackEventForCountlyAndGA("B_BookingDetail_B",
                            "BookingDetailSecondaryCTAClick","GetDirections"+"_"+ bookingID, hMaps);

                }
            }
        }

    }


    //track event for countly and ga for restaurant detail
    private void trackEventRestaurantDetail(JSONObject ctaObject) {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(!TextUtils.isEmpty(type) && !TextUtils.isEmpty(bookingType)) {
            if ("history".equalsIgnoreCase(bookingType)) {

                    if (type.equalsIgnoreCase("deal")) {
                        //track event
                        trackEventForCountlyAndGA("D_HistoryBookingDetail",
                                "BookingDetailSecondaryCTAClick", "RestaurantDetailView" + "_" + bookingID, hMap);

                    } else {
                        // Track Event
                        trackEventForCountlyAndGA("D_HistoryBookingDetail",
                                "BookingDetailSecondaryCTAClick", "RestaurantDetailView" + "_" + bookingID, hMap);

                }

            } else if ("upcoming".equalsIgnoreCase(bookingType)) {

                    if (type.equalsIgnoreCase("deal")) {
                        //track event
                        trackEventForCountlyAndGA("D_UpcomingBookingDetail",
                                "BookingDetailSecondaryCTAClick", "RestaurantDetailView" + "_" + bookingID, hMap);

                    } else {
                        // Track Event
                        trackEventForCountlyAndGA("D_UpcomingBookingDetail",
                                "BookingDetailSecondaryCTAClick", "RestaurantDetailView" + "_" + bookingID, hMap);

                    }

            } else {
                if (type.equalsIgnoreCase("deal")) {
                    //track event
                    trackEventForCountlyAndGA("B_BookingDetail_D",
                            "BookingDetailSecondaryCTAClick", "RestaurantDetailView" + "_" + bookingID, hMap);

                } else {
                    // Track Event
                    trackEventForCountlyAndGA("B_BookingDetail_B",
                            "BookingDetailSecondaryCTAClick", "RestaurantDetailView" + "_" + bookingID, hMap);

                }
            }
        }


}


    @Override
    public void onDialogDismiss() {
        // do nothing
    }

    @Override
    public void onReviewSubmission() {
        refreshScreen();
    }

    @Override
    public void onRNRError(JSONObject errorObject) {
        if (errorObject != null) {
            String errorCode = errorObject.optString("error_code", "");
            switch (errorCode) {
                case LOGIN_SESSION_EXPIRE_CODE:
                    refreshScreen();
                    break;
            }
        }
    }


    private void refreshScreen() {
        // reset adapter
        bookingDetailsRecyclerAdapter.setJsonArray(new JSONArray());
        bookingDetailsRecyclerAdapter.notifyDataSetChanged();

        // make api call
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
    }

}
