package com.dineout.book.fragment.mybookings;

import android.os.Bundle;
import android.view.View;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.recycleradapters.HistoryBookingRecyclerAdapter;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.HashMap;

import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_NAME;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_POSITIVE_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.appendObject;


import java.util.HashMap;

public class HistoryBookingFragment extends BookingTabFragment
        implements HistoryBookingRecyclerAdapter.HistoryBookingClickListener,
        RateNReviewUtil.RateNReviewCallbacks {

    private boolean isFirstPage;
    private HistoryBookingRecyclerAdapter recyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFirstPage = false;

        recyclerAdapter = new HistoryBookingRecyclerAdapter(getActivity());
        recyclerAdapter.setHistoryBookingClickListener(this);
        setRecyclerAdapter(recyclerAdapter);
    }

    @Override
    protected void getBookings(int currentPage) {
        showLoader();

        isFirstPage = (currentPage == 1);

        setRequestType(AppConstant.HISTORY_BOOKING_TAB);

        getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_BOOKINGS, AppConstant.URL_USER_BOOKING_SEGG,
                ApiParams.getUserBookingSeggParams(
                        DOPreferences.getDinerId(getActivity().getApplicationContext()),
                        DOPreferences.getAuthKey(getActivity().getApplicationContext()),
                        AppConstant.BOOKING_TYPE_HISTORY, currentPage, AppConstant.DEFAULT_PAGINATION_LIMIT),
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
    public void onBookingCardClick(JSONObject jsonObject) {
        handleBookingCardClick(jsonObject);
    }

    @Override
    public void onReserveAgainClick(JSONObject jsonObject) {
        // Track Event
//        AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                getString(R.string.ga_screen_my_bookings),
//                getString(R.string.ga_action_reserve_again_past_booking_card),
//                null);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_history_booking_rseserve_again_review),jsonObject.optString("b_id"),hMap);

        // Get Instance
        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();

        // Set Bundle
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, jsonObject.optString("r_id", "")); // Restaurant Id
        restaurantDetailFragment.setArguments(bundle);

        addToBackStack(getParentFragment().getFragmentManager(), restaurantDetailFragment);
    }

    @Override
    public void onWriteReviewClick(JSONObject jsonObject) {
        // Track Event
//        AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                getString(R.string.ga_screen_my_bookings),
//                getString(R.string.ga_action_write_review_past_booking_card),
//                null);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_history_booking_write_review),jsonObject.optString("b_id"),hMap);


        JSONObject obj = new JSONObject();
        try {
            JSONObject reviewData = jsonObject.optJSONObject("review");
            if (reviewData != null && reviewData.optJSONArray("button") != null) {
                int actionKey = Integer.valueOf(REVIEW_POSITIVE_ACTION);

                // append review box data using action key
                if (reviewData.optJSONObject("review_box") != null) {
                    JSONObject actionValue = reviewData.optJSONObject("review_box").optJSONObject(
                            String.valueOf(actionKey));

                    appendObject(obj, actionValue);
                }

                // append review data
                appendObject(obj, reviewData.optJSONObject("review_data"));

                // put action
//                obj.put(REVIEW_ACTION, actionKey);
            }

            obj.put(BOOKING_ID, jsonObject.optString("b_id", ""));
            obj.put(RESTAURANT_NAME, jsonObject.optString("title1"));
            obj.put(RESTAURANT_ID, jsonObject.optString("r_id", ""));
        } catch (Exception e) {
            // Exception
        }

        RateNReviewDialog frag = new RateNReviewDialog();
        Bundle bl = new Bundle();
        bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_my_booking));
        bl.putString(INFO_STRING, obj.toString());
        frag.setArguments(bl);
        frag.setRateNReviewCallback(this);

        showFragment(getActivity().getSupportFragmentManager(), frag);

//        Bundle bundle = new Bundle();
//        bundle.putString(WriteReviewFragment.BOOKING_ID, jsonObject.optString("b_id", ""));
//        bundle.putString(WriteReviewFragment.RESTAURANT_NAME, jsonObject.optString("title1"));
//        bundle.putString(WriteReviewFragment.RESTAURANT_ID, jsonObject.optString("r_id", ""));
//
//        WriteReviewFragment writeReviewFragment = new WriteReviewFragment();
//        writeReviewFragment.setArguments(bundle);
//
//
//        addToBackStack(getActivity().getSupportFragmentManager(), writeReviewFragment);

    }


    @Override
    public void onUploadBillClick(JSONObject jsonObject) {
        UploadBillController utility =
                new UploadBillController(getNetworkManager(), getActivity(),
                        jsonObject.optString("r_id"), jsonObject.optString("b_id", ""));
        utility.setPreviousScreenName(getString(R.string.ga_screen_booking_history));
        utility.validate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        recyclerAdapter = null;
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
        // Show No Bookings
        getRelativeLayoutNoBookings().setVisibility(View.GONE);

        // reset adapter
        recyclerAdapter.setJsonArray(new JSONArray());
        recyclerAdapter.notifyDataSetChanged();

        // call booking api
        refreshUpcomingBookingList();
    }
}
