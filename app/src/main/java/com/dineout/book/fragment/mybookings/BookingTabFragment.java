package com.dineout.book.fragment.mybookings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.recycleradapters.BaseRecyclerAdapter;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public abstract class BookingTabFragment extends YouPageWrapperFragment {

    protected final int REQUEST_CODE_GET_BOOKINGS = 101;

    private BookingScrollListener bookingScrollListener;
    private RelativeLayout relativeLayoutNoBookings;
    private ImageView imageViewNoBooking;
    private TextView textViewNoBooking;
    private RecyclerView recyclerViewBookings;
    private BaseRecyclerAdapter recyclerAdapter;
    private int requestType;
    private boolean isLoading, isLastPage;
    private int currentPage, totalRecords, totalPages;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upcoming_history_booking, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize View
        initializeView();

        // authenticate the user
        authenticateUser();
    }

    private void initializeView() {
        currentPage = 1;
        totalRecords = 0;
        totalPages = 0;
        isLoading = false;
        isLastPage = false;

        // Instantiate Scroll Listener
        bookingScrollListener = new BookingScrollListener();

        // Set Recycler Instance
        setRecyclerViewBookings((RecyclerView) getView().findViewById(R.id.recyclerView_bookings));
        getRecyclerViewBookings().setAdapter(getRecyclerAdapter());
        getRecyclerViewBookings().addOnScrollListener(bookingScrollListener);

        // Set No Bookings Section
        setRelativeLayoutNoBookings((RelativeLayout) getView().findViewById(R.id.relativeLayout_no_bookings));

        imageViewNoBooking = (ImageView) getRelativeLayoutNoBookings().findViewById(R.id.iv_no_booking);
        textViewNoBooking = (TextView) getRelativeLayoutNoBookings().findViewById(R.id.tv_no_booking);
    }

    protected void refreshUpcomingBookingList() {
        currentPage = 1;
        totalRecords = 0;
        totalPages = 0;
        isLoading = false;
        isLastPage = false;

        onNetworkConnectionChanged(true);
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        getUserBookings();
    }

    protected void getUserBookings() {
        getBookings();
    }

    // Show Bookings
    protected void showBookings() {
        // Hide Bookings
        getRelativeLayoutNoBookings().setVisibility(View.GONE);
    }

    // Show No Bookings section
    protected void showNoBookings() {
        // Set No Booking Resources
        setNoBookingResources();

        // Show No Bookings
        getRelativeLayoutNoBookings().setVisibility(View.VISIBLE);

        // Empty Recycler
        getRecyclerViewBookings().removeAllViews();
    }

    private void setNoBookingResources() {
        // Get Bookings
        if (getRequestType() == AppConstant.UPCOMING_BOOKING_TAB) {
            imageViewNoBooking.setImageResource(R.drawable.ic_no_upcoming_booking);
            textViewNoBooking.setText(R.string.no_upcoming_bookings);
        } else {
            imageViewNoBooking.setImageResource(R.drawable.ic_no_past_booking);
            textViewNoBooking.setText(R.string.no_history_bookings);
        }
    }

    protected void handleBookingCardClick(JSONObject jsonObject) {
        String bookingId = jsonObject.optString("b_id", "");
        String bookingType = jsonObject.optString("booking_type", "");

        if (!AppUtil.isStringEmpty(bookingId)) {

            if (AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(jsonObject.optString("b_type", ""))) {
                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                        getString(R.string.ga_screen_my_bookings),
//                        getString(("upcoming".equalsIgnoreCase(jsonObject.optString("booking_type"))) ?
//                                R.string.ga_action_upcoming_deals_card : R.string.ga_action_past_deals_card),
//                        jsonObject.optString("title1") + "_" + jsonObject.optString("r_id"));


            } else {
                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                        getString(R.string.ga_screen_my_bookings),
 //                       getString(("upcoming".equalsIgnoreCase(jsonObject.optString("booking_type"))) ?
  //                              R.string.ga_action_upcoming_card : R.string.ga_action_past_booking_card),
//                        null);


                //track event
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(("upcoming".equalsIgnoreCase(jsonObject.optString("booking_type"))) ?
                        R.string.d_upcoming_booking : R.string.d_history_booking),"B_"+bookingId,hMap);

            }

            Bundle bundle = new Bundle();
            bundle.putString("b_id", bookingId);
            //bundle.putString("res_id", jsonObject.optString("r_id"));
            //bundle.putString("res_name", jsonObject.optString("title1"));
            bundle.putString("type", jsonObject.optString("b_type"));
            bundle.putString("bookingType",bookingType);

            BookingDetailsFragment bookingDetailFragment = new BookingDetailsFragment();
            bookingDetailFragment.setArguments(bundle);

            addToBackStack(getActivity(), bookingDetailFragment);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        relativeLayoutNoBookings = null;
        imageViewNoBooking = null;
        textViewNoBooking = null;
        recyclerViewBookings = null;
        recyclerAdapter = null;
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        isLoading = false;

        if (getView() == null || getActivity() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_GET_BOOKINGS) {
            // Check for NULL
            if (responseObject != null) {

                // Check Response Status
                if (responseObject.optBoolean("status")) {

                    // Get Output Params
                    JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                    if (outputParamsJsonObject != null) {
                        // Get Total Records
                        totalRecords = Integer.parseInt(outputParamsJsonObject.optString("num_found", "0"));

                        // Get Data
                        JSONObject dataJsonObject = outputParamsJsonObject.optJSONObject("data");

                        if (dataJsonObject != null) {
                            JSONArray bookingsJsonArray;

                            // Get Bookings
                            if (getRequestType() == AppConstant.UPCOMING_BOOKING_TAB) {
                                bookingsJsonArray = dataJsonObject.optJSONArray("upcoming_bookings");

                            } else {
                                // Get History Bookings
                                bookingsJsonArray = dataJsonObject.optJSONArray("history_bookings");
                            }

                            if (bookingsJsonArray == null || bookingsJsonArray.length() == 0) {
                                showNoBookings();

                            } else {
                                showBookings();
                                processBookingResponse(bookingsJsonArray);
                            }

                            // Calculate Next Page
                            getNextPage();
                        }
                    }
                } else if (responseObject.optJSONObject("res_auth") != null &&
                        !responseObject.optJSONObject("res_auth").optBoolean("status")) {

                    // Show No Bookings
                    showNoBookings();

                    // Ask User to Login
                    //askUserToLoginForBooking();

                } else {
                    // Show No Bookings
                    showNoBookings();

                    // Show Message
                    UiUtil.showToastMessage(getActivity().getApplicationContext(),
                            getString(R.string.text_unable_fetch_details));
                }
            } else {
                // Show Message
                UiUtil.showToastMessage(getActivity().getApplicationContext(),
                        getString(R.string.text_general_error_message));
            }
        }
    }

    private void getNextPage() {
        // Check if PageCount is calculated
        if (totalPages == 0) {
            int quotient = (totalRecords / AppConstant.DEFAULT_PAGINATION_LIMIT);
            int remainder = (totalRecords % AppConstant.DEFAULT_PAGINATION_LIMIT);
            totalPages = quotient;
            if (remainder > 0) {
                ++totalPages;
            }
        }

        ++currentPage;

        // Check for Last Page
        if (currentPage > totalPages) {
            isLastPage = true;
        }
    }

    protected void getBookings() {
        isLoading = true;
        getBookings(currentPage);
    }

    /**
     * Getter - Setter methods
     */
    public RelativeLayout getRelativeLayoutNoBookings() {
        return relativeLayoutNoBookings;
    }

    public void setRelativeLayoutNoBookings(RelativeLayout relativeLayoutNoBookings) {
        this.relativeLayoutNoBookings = relativeLayoutNoBookings;
    }

    public RecyclerView getRecyclerViewBookings() {
        return recyclerViewBookings;
    }

    public void setRecyclerViewBookings(RecyclerView recyclerViewBookings) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewBookings.setLayoutManager(linearLayoutManager);
        this.recyclerViewBookings = recyclerViewBookings;
    }

    public BaseRecyclerAdapter getRecyclerAdapter() {
        return recyclerAdapter;
    }

    public void setRecyclerAdapter(BaseRecyclerAdapter recyclerAdapter) {
        this.recyclerAdapter = recyclerAdapter;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    /**
     * Define Functions
     */
    protected abstract void getBookings(int currentPage);

    protected abstract void processBookingResponse(JSONArray jsonArray);

    private class BookingScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getRecyclerViewBookings().getLayoutManager();
            int lastVisibleItemPosition= linearLayoutManager.findLastVisibleItemPosition();

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(newState == RecyclerView.SCROLL_STATE_IDLE){
                if (getRequestType() == AppConstant.UPCOMING_BOOKING_TAB) {
                    trackEventForCountlyAndGA(getString(R.string.countly_booking), getString(R.string.d_upcoming_scroll), Integer.toString(lastVisibleItemPosition), hMap);
                }else{
                    trackEventForCountlyAndGA(getString(R.string.countly_booking), getString(R.string.d_history_scroll), Integer.toString(lastVisibleItemPosition), hMap);
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getRecyclerViewBookings().getLayoutManager();

            int visibleItemCount = linearLayoutManager.getChildCount();
            int totalItemCount = linearLayoutManager.getItemCount();
            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

            //&& totalItemCount >= PAGE_SIZE

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition > 0) {
                    getBookings();
                }
            }
        }
    }

    @Override
    public void handleNavigation() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
