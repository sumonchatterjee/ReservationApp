package com.dineout.book.fragment.bookingflow;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.book.fragment.detail.NewWaitTimeFragment;
import com.dineout.recycleradapters.BookingDateSlotAdapter;
import com.dineout.recycleradapters.BookingTimeSlotSectionAdapter;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_PREVIOUS_OFFER_START_FROM;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_REST_START_FROM;

public class BookingTimeSlotFragment extends MasterDOJSONReqFragment
        implements BookingDateSlotAdapter.DateSlotClickListener,
        BookingTimeSlotSectionAdapter.TimeSlotSectionClickListener,
        NewWaitTimeFragment.WaitTimeListener {

    private final int REQUEST_CODE_GET_TIME_SLOTS = 101;

    private long fromTimeStamp;
    private JSONArray dateSlotJsonArray;
    private JSONObject selectedDateJsonObject;
    private BookingDateSlotAdapter bookingDateSlotAdapter;
    private BookingTimeSlotSectionAdapter bookingTimeSlotSectionAdapter;
    private AlertDialog.Builder disabledTimeSlotDialog;
    private LayoutInflater layoutInflater;
    private HashMap<Integer, String> legendMap;
    private JSONObject dataJsonObject;

    private RecyclerView recyclerViewTimeSlot;
    private LinearLayout linearLayoutLegendSection;
    private View relativeLayoutTimeSlotError;
    private boolean isRequestInFlight = false;

    private FrameLayout coachMarkScreenLayout;
    private RelativeLayout topContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create Date Slots
        long dateStartDate = getArguments().getLong(BUNDLE_PREVIOUS_OFFER_START_FROM, 0L);
        if (dateStartDate == 0L) {
            dateStartDate = getArguments().getLong(BUNDLE_REST_START_FROM, 0L);
        }
        dateSlotJsonArray = DateTimeSlotUtil.getDateSlots(dateStartDate);

        // Set Date Slot Selected
        setDateSlotSelected();

        // Instantiate Legend
        layoutInflater = LayoutInflater.from(getActivity());

        // Create Dialog
        disabledTimeSlotDialog = UiUtil.getAlertDialog(
                getActivity(),
                null,
                R.string.text_disabled_time_slot_error_message,
                R.string.button_ok,
                null,
                true,
                false,
                true);

        // Instantiate Date Slot Adapter
        bookingDateSlotAdapter = new BookingDateSlotAdapter(getActivity());
        bookingDateSlotAdapter.setDateSlotClickListener(this);

        // Instantiate Time Slot Adapter
        bookingTimeSlotSectionAdapter = new BookingTimeSlotSectionAdapter(getActivity());
        bookingTimeSlotSectionAdapter.setTimeSlotSectionClickListener(this);

        // Take API Hit - Check if Bundle contains deeplink then do not take API hit
        Bundle arguments = getArguments();
        if (arguments == null || !arguments.getBoolean(AppConstant.BUNDLE_VIA_DEEPLINK, false)) {
            onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_time_slot, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Take API Hit - Check if Bundle contains deeplink then do not take API hit
        Bundle arguments = getArguments();
        if (arguments == null || !arguments.getBoolean(AppConstant.BUNDLE_VIA_DEEPLINK, false)) {
            handleActivityCreated();
        }
    }

    protected void proceedWithTimeSlot(Bundle restaurantBundle) {
        // Set Bundle
        getArguments().putAll(restaurantBundle);

        // Create Date Slots
        long offerStartDate = getArguments().getLong(BUNDLE_PREVIOUS_OFFER_START_FROM, 0L);
        if (offerStartDate > 0L) {
            dateSlotJsonArray = DateTimeSlotUtil.getDateSlots(offerStartDate);
        }

        // Set Date Slot Selected
        setDateSlotSelected();

        // Handle Activity Created
        handleActivityCreated();

        // Take API Hit
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    private void handleActivityCreated() {
        // Track Screen
       // trackScreenToGA(getString(R.string.ga_screen_time_slot_selection));
        trackScreenName(getString(R.string.countly_slot_selection));

        // Initialize View
        initializeView();

        if (isRequestInFlight) {
            showLoader();
        }

        // Prepare Legend Section
        prepareLegendSection();

        // Clean Up UI
        cleanUpUI();
    }

    private void initializeView() {
        // Set Toolbar
        setToolbarTitle(getTitleFromSelectedDate(selectedDateJsonObject));

        coachMarkScreenLayout = (FrameLayout)getView().findViewById(R.id.coach_mark_layout);
        topContainer = (RelativeLayout)getView().findViewById(R.id.top_container);

//        //show coach mark screen for first time
//         if(DOPreferences.getNeedToShowCoachMark(getContext())){
//
//             coachMarkScreenLayout.setVisibility(View.VISIBLE);
//             DOPreferences.setNeedtoShowCoachMark(getContext(),false);
//         }else{
//             coachMarkScreenLayout.setVisibility(View.GONE);
//             DOPreferences.setNeedtoShowCoachMark(getContext(),true);
//         }


        // Define Layout Manager
        LinearLayoutManager linearLayoutManagerHorizontal = new LinearLayoutManager(getActivity());
        linearLayoutManagerHorizontal.setOrientation(LinearLayoutManager.HORIZONTAL);

        // Get Recycler View
        RecyclerView recyclerViewBookingSlot = (RecyclerView) getView().findViewById(R.id.recyclerView_booking_slot);
        recyclerViewBookingSlot.setLayoutManager(linearLayoutManagerHorizontal);
        recyclerViewBookingSlot.setAdapter(bookingDateSlotAdapter);

        // Define Layout Manager
        LinearLayoutManager linearLayoutManagerVertical = new LinearLayoutManager(getActivity());
        linearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);

        // Get Time Slot Recycler View
        recyclerViewTimeSlot = (RecyclerView) getView().findViewById(R.id.recyclerView_time_slot);
        recyclerViewTimeSlot.setLayoutManager(linearLayoutManagerVertical);
        recyclerViewTimeSlot.setAdapter(bookingTimeSlotSectionAdapter);

        // Get Legend Section
        linearLayoutLegendSection = (LinearLayout) getView().findViewById(R.id.linearLayout_legend_section);

        // Get Time Slot Error
        relativeLayoutTimeSlotError = getView().findViewById(R.id.relativeLayout_time_slot_error);

        coachMarkScreenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(coachMarkScreenLayout.getVisibility() == View.VISIBLE){
                    coachMarkScreenLayout.setVisibility(View.GONE);
                    DOPreferences.setNeedtoShowCoachMark(getContext(),false);
                }
            }
        });

    }

    private String getTitleFromSelectedDate(JSONObject dateSlotJsonObject) {
        String title = getString(R.string.text_select_booking_time);

        // Check for NULL / Empty
        if (dateSlotJsonObject != null) {
            title = dateSlotJsonObject.optString(AppConstant.MONTH, "") + " " +
                    dateSlotJsonObject.optString(AppConstant.YEAR);
        }

        return title;
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Cancel Previous Requests
        getNetworkManager().cancel();

        // Refresh UI
        refreshUI(selectedDateJsonObject);

        // Reset FROM TimeStamp
        fromTimeStamp = selectedDateJsonObject.optLong(AppConstant.TIMESTAMP, 0L);

        // Get Time Slots
        getBookingTimeSlots(fromTimeStamp, DateTimeSlotUtil.getToTimeStamp(fromTimeStamp, 0));
    }

    private void setDateSlotSelected() {
        // Check for Arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            // Get Edit Booking Date
            String editBookingDate = arguments.getString(AppConstant.EDIT_BOOKING_DATE, ""); // yyyy-MM-dd

            // Set Date Selected
            selectedDateJsonObject = DateTimeSlotUtil.setGetDateSlotSelected(dateSlotJsonArray, editBookingDate);
        }
    }

    private void getBookingTimeSlots(long fromTimeStamp, long toTimeStamp) {
        // Check if Fragment is attached to Activity
        if (getActivity() == null)
            return;

        // Show Loader
        showLoader();

        isRequestInFlight = true;

        // Get Restaurant Id
        Bundle arguments = getArguments();
        if (arguments == null)
            return;

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(
                REQUEST_CODE_GET_TIME_SLOTS,
                AppConstant.URL_BOOKING_TIME_SLOT,
                ApiParams.getBookingSlotParams(
                        arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""),
                        fromTimeStamp,
                        toTimeStamp,
                        arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_ID, ""),
                        arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, ""),
                        selectedDateJsonObject.optInt(AppConstant.HAS_SLOT),
                        arguments.getBoolean(AppConstant.BUNDLE_IS_EDIT_BOOKING, false)),
                this, this, false);
    }

    @Override
    public void onDateSlotClick(JSONObject dateSlotJsonObject) {
        // Track Event
        //trackEventGA(getString(R.string.ga_screen_time_slot_selection), getString(R.string.ga_action_select_date), null);


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("TIMESTAMP",dateSlotJsonObject.optString("TIMESTAMP"));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_slot_selection),getString(R.string.d_date_click),dateSlotJsonObject.optString("DATE_KEY"),hMap);


        boolean shouldTakeClickAction = true;
        if (dateSlotJsonObject != null) {
            shouldTakeClickAction = (dateSlotJsonObject.optInt(AppConstant.DATE_CLICKABLE_KEY, AppConstant.DATE_CLICKABLE)) == AppConstant.DATE_CLICKABLE;
        }

        // Set Selected Date Slot
        selectedDateJsonObject = dateSlotJsonObject;

        // Refresh UI
        refreshUI(dateSlotJsonObject);

        if (shouldTakeClickAction) {
            // Check for NULL / Empty
            if (dateSlotJsonObject != null) {
                // Get Time Slots
                long dateTimestamp = dateSlotJsonObject.optLong(AppConstant.TIMESTAMP, 0L);
                getBookingTimeSlots(dateTimestamp, DateTimeSlotUtil.getToTimeStamp(dateTimestamp, 0));

            }
        } else {
            dataJsonObject = null;
            showErrorMessage(getResources().getString(R.string.booking_slot_screen_error_title),
                    getResources().getString(R.string.booking_slot_screen_error_msg));
        }
    }

    // Refresh UI
    private void refreshUI(JSONObject dateSlotJsonObject) {
        // Check for View is attached
        if (getView() == null)
            return;

        // Set Title
        setToolbarTitle(getTitleFromSelectedDate(dateSlotJsonObject));

        // Remove Time Slots
        recyclerViewTimeSlot.removeAllViews();

        // Notify Data Change
        bookingTimeSlotSectionAdapter.setJsonArray(null);
        bookingTimeSlotSectionAdapter.notifyDataSetChanged();

        // Hide Legend Section
        linearLayoutLegendSection.setVisibility(View.GONE);

        // Hide Time Slot Error
        relativeLayoutTimeSlotError.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null) {
            return;
        }

        isRequestInFlight = false;

        if (request.getIdentifier() == REQUEST_CODE_GET_TIME_SLOTS) {
            if (responseObject != null && responseObject.optBoolean("status")) {
                // Get Data
                dataJsonObject = responseObject.optJSONObject("data");

                if (dataJsonObject != null) {
                    // Prepare Legend Section
                    prepareLegendSection();

                    // Prepare Date Slot Section
                    prepareDateSlotSection();

                    // Prepare Time Slot Section
                    prepareTimeSlotSection();

                    // Clean Up UI
                    cleanUpUI();

                }
            }
        }
    }

    private void prepareDateSlotSection() {
        // Update Date Slots for No Slots
        Bundle arguments = getArguments();
        if (arguments != null) {
            String type = arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, "");

            if (!AppUtil.isStringEmpty(type)
                    && type.equalsIgnoreCase(AppConstant.BOOKING_TYPE_DEAL)
                    && dataJsonObject != null
                    && dataJsonObject.optJSONObject("dealsValidDates") != null
                    && dataJsonObject.optJSONObject("dealsValidDates").length() > 0) {

                // Mark Offer / Deal Availability
                DateTimeSlotUtil.markAvailableSlots(dataJsonObject.optJSONObject("dealsValidDates"), dateSlotJsonArray);

                DateTimeSlotUtil.handleNetworkCallForSoldDeals(dataJsonObject.optJSONObject("dealsClickDates"), dateSlotJsonArray);
            }
        }

        // Set Data in Adapter
        bookingDateSlotAdapter.setJsonArray(dateSlotJsonArray);

        // Notify Date Change
        bookingDateSlotAdapter.notifyDataSetChanged();
    }

    private void prepareTimeSlotSection() {
        // Check for NULL
        if (selectedDateJsonObject != null) {
            // Get Selected Date Slot
            String selectedDateSlot = selectedDateJsonObject.optString(AppConstant.DATE_KEY, "");

            // Check for NULL / Empty
            if (!AppUtil.isStringEmpty(selectedDateSlot)) {
                // Set Data in Adapter
                if (dataJsonObject != null) {
                    bookingTimeSlotSectionAdapter.setJsonArray(dataJsonObject.optJSONArray(selectedDateSlot));
                }

                // Set Legend
                bookingTimeSlotSectionAdapter.setLegendMap(legendMap);

                // Notify Data Change
                bookingTimeSlotSectionAdapter.notifyDataSetChanged();
            }
        }
    }

    private void prepareLegendSection() {
        // Check for NULL
        if (dataJsonObject != null) {
            // Prepare Legends UI
            prepareLegendsUI(dataJsonObject);

            // Show Legend Section
            linearLayoutLegendSection.setVisibility(View.VISIBLE);
        }
    }

    private void prepareLegendsUI(JSONObject dataJsonObject) {
        legendMap = new HashMap<>();
        JSONArray legendJsonArray = dataJsonObject.optJSONArray("legend");

        // Check for NULL / Empty
        if (legendJsonArray != null && legendJsonArray.length() > 0) {
            // Remove All Views
            linearLayoutLegendSection.removeAllViews();

            int arraySize = legendJsonArray.length();
            for (int index = 0; index < arraySize; index++) {
                // Get Legend
                JSONObject legendJsonObject = legendJsonArray.optJSONObject(index);

                // Check for NULL / Empty
                if (legendJsonObject != null) {
                    // Add Legend Map
                    legendMap.put(legendJsonObject.optInt("offer", -1),
                            legendJsonObject.optString("color", "white"));

                    // Get View
                    View legendView = layoutInflater.inflate(R.layout.time_slot_legend, null, false);

                    // Set Legend Drawable
                    TextView textViewLegendIndicator = (TextView) legendView.findViewById(R.id.textView_legend_indicator);
                    textViewLegendIndicator.setBackground(createLegend(R.dimen.screen_padding_eight, legendJsonObject.optString("color", "white")));

                    // Set Name
                    TextView textViewLegendName = (TextView) legendView.findViewById(R.id.textView_legend_name);
                    textViewLegendName.setText(legendJsonObject.optString("name", ""));

                    // Add View to Parent
                    linearLayoutLegendSection.addView(legendView);
                }
            }
        }
    }

    private ShapeDrawable createLegend(int legendSize, String legendColor) {
        int size = AppUtil.convertDpToPx(getActivity(), legendSize);

        ShapeDrawable ovalDrawable = new ShapeDrawable(new OvalShape());
        ovalDrawable.setIntrinsicHeight(size);
        ovalDrawable.setIntrinsicWidth(size);
        ovalDrawable.getPaint().setColor(Color.parseColor(legendColor));

        return ovalDrawable;
    }

    private void cleanUpUI() {
        // Check Selected Date Slot
        if (dataJsonObject != null) {
            // Get Slots Info
            JSONObject slotInfoJsonObject = dataJsonObject.optJSONObject("slotsInfo");

            // Check for NULL / Empty
            if (selectedDateJsonObject != null && slotInfoJsonObject != null) {
                // Get Slot Info
                JSONObject slotJsonObject = slotInfoJsonObject.optJSONObject(
                        selectedDateJsonObject.optString(AppConstant.DATE_KEY));

                // Show Error Message
                if (slotJsonObject != null && !slotJsonObject.optBoolean("hasSlots", false)) {
                    showErrorMessage(slotJsonObject.optString("title", ""), slotJsonObject.optString("msg", ""));
                }
            }

            //show coach mark screen for first time
            boolean hasRightNowSection = dataJsonObject.optBoolean("hasRightNowSection");
            if(DOPreferences.getNeedToShowCoachMark(getContext()) && hasRightNowSection && DOPreferences.getIsCoachMarkEnabledFromConfig(getContext())){

                coachMarkScreenLayout.setVisibility(View.VISIBLE);
                //DOPreferences.setNeedtoShowCoachMark(getContext(),false);
            }else{
                coachMarkScreenLayout.setVisibility(View.GONE);
               // DOPreferences.setNeedtoShowCoachMark(getContext(),true);
            }

        } else {
            // Hide Time Slot Error
            relativeLayoutTimeSlotError.setVisibility(View.GONE);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        if (request.getIdentifier() == REQUEST_CODE_GET_TIME_SLOTS) {
            // Show Error Message
            showErrorMessage("", "");
        }
    }

    private void showErrorMessage(String title, String errorMessage) {
        // Show Error Message
        if(getView() == null)
            return;
        if(relativeLayoutTimeSlotError!=null) {
            relativeLayoutTimeSlotError.setVisibility(View.VISIBLE);
        }

        // Set Error Title
        TextView textViewErrorTitle = (TextView) getView().findViewById(R.id.textView_error_title);
        textViewErrorTitle.setText((AppUtil.isStringEmpty(title)) ?
                getString(R.string.text_time_slot_error_title) : title);

        // Set Error Message
        TextView textViewErrorMessage = (TextView) getView().findViewById(R.id.textView_error_message);
        textViewErrorMessage.setText((AppUtil.isStringEmpty(errorMessage)) ?
                getString(R.string.text_time_slot_error_message) : errorMessage);
    }

    @Override
    public void showDisabledTimeSlotMessage(String message) {
        // Set Message
        disabledTimeSlotDialog.setMessage(((AppUtil.isStringEmpty(message)) ?
                getString(R.string.text_disabled_time_slot_error_message) : message));

        // Show Message
        disabledTimeSlotDialog.show();
    }

    @Override
    public ShapeDrawable getLegendDrawable(int legendSize, String legendColor) {
        return createLegend(legendSize, legendColor);
    }

    private String getLegendText(int offer) {
        // Get Legend JSON
        JSONArray legendJsonArray = dataJsonObject.optJSONArray("legend");

        // Check for NULL
        if (legendJsonArray != null && legendJsonArray.length() > 0) {
            int legendSize = legendJsonArray.length();
            for (int index = 0; index < legendSize; index++) {
                // Get Legend
                JSONObject legendJsonObject = legendJsonArray.optJSONObject(index);

                // Check for NULL
                if (legendJsonObject != null) {
                    // Check for Offer param
                    if (offer == legendJsonObject.optInt("offer", 0)) {
                        return legendJsonObject.optString("name", "");
                    }
                }
            }
        }

        return "";
    }

    @Override
    public void onTimeSlotClick(JSONObject timeSlotJsonObject) {
        // Track Event
//        trackEventGA(getString(R.string.ga_screen_time_slot_selection),
//                getString(R.string.ga_action_select_slot),
//                getLegendText(timeSlotJsonObject.optInt("offer", -1)));


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("slotValue",timeSlotJsonObject.optString("displayTimeWeb"));
            hMap.put("DateValue",selectedDateJsonObject.optString(AppConstant.DATE_KEY, ""));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_slot_selection),"SlotClick",timeSlotJsonObject.optString("displayTimeWeb"),hMap);

        // Check for NULL / Empty
        if (timeSlotJsonObject != null && selectedDateJsonObject != null) {
            // Get Arguments
            Bundle arguments = getArguments();
            if (arguments == null)
                arguments = new Bundle();

            String selectedDate = selectedDateJsonObject.optString(AppConstant.DATE_KEY, "");
            String selectedTime = timeSlotJsonObject.optString("time", "");
            String displayTime= timeSlotJsonObject.optString("displayTimeWeb", "");

            arguments.putString(AppConstant.BUNDLE_SELECTED_DATE, selectedDate);
            arguments.putString(AppConstant.BUNDLE_SELECTED_TIME, selectedTime);
            arguments.putString(AppConstant.BUNDLE_DISPLAY_TIME, displayTime);
            arguments.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP,
                    DateTimeSlotUtil.getSelectedOfferTimestamp(selectedDate, selectedTime));
            arguments.putInt(AppConstant.BUNDLE_SELECTED_TIME_SLOT_OFFER_STATUS,
                    timeSlotJsonObject.optInt("offer", 0));

            // Check Offer Type
            String type = arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, "");

            if (type.equalsIgnoreCase(AppConstant.BOOKING_TYPE_DEAL)) {
                // Get Offer
                JSONObject prevOfferJsonObject = null;
                try {
                    String prevOfferString = arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_JSON);

                    if (!AppUtil.isStringEmpty(prevOfferString)) {
                        prevOfferJsonObject = new JSONObject(prevOfferString);
                        prevOfferJsonObject.put("dealsLeft", timeSlotJsonObject.optInt("dealsLeft", 0));
                        prevOfferJsonObject.put("transactionDealLimit", timeSlotJsonObject.optInt("transactionDealLimit", 0));

                        arguments.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_JSON, prevOfferJsonObject.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //check if hasRightNowSection is true and the time slot selected lies at index 0
            if (getArguments() != null) {
                getArguments().putBoolean("hasRightNowSection", dataJsonObject.optBoolean("hasRightNowSection"));
                getArguments().putInt("timeSlotSectionPos", timeSlotJsonObject.optInt("timeSlotSectionPos"));
            }


            int hasWaitTime = timeSlotJsonObject.optInt("hasWaitTime", 0);
            if (hasWaitTime == 0) {
//                decideUserNavigation();
                if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
                    UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
                } else {
                    decideUserNavigation();
                }

            } else {
                // Show Wait Time
                showWaitTime();
            }
        }
    }

    private void decideUserNavigation() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            // Check if Offer is Pre-Selected
            if (AppUtil.isStringEmpty(arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_ID, ""))) {
                // All Offer Fragment
                proceedAllOffers();

            } else {
                // Get Offer Indicator
                int offer = arguments.getInt(AppConstant.BUNDLE_SELECTED_TIME_SLOT_OFFER_STATUS, 0);

                if (offer == 1) { // Current Offer Available
                    // Set Previous Offer as Current Offer
                    setPreviousOfferAsCurrentOffer();

                    // Check for Offer or Deal
                    String type = arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, "");

                    if (type.equalsIgnoreCase(AppConstant.BOOKING_TYPE_DEAL)) {
                        // Navigate User to Deal Ticket Counter Screen
                        proceedDealTicketCounter();

                    } else {
                        // Navigate User to Booking Confirmation Screen
                        proceedBookingConfirmation();
                    }
                } else if (offer == 2) {
                    // All Offer Fragment
                    proceedAllOffers();

                } else {
                    // Remove Selected Offer
                    removedSelectedOffer();

                    // All Offer Fragment
                    proceedAllOffers();
                }
            }
        }
    }

    private void setPreviousOfferAsCurrentOffer() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            arguments.putString(AppConstant.BUNDLE_OFFER_ID,
                    arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_ID, ""));
            arguments.putString(AppConstant.BUNDLE_OFFER_TYPE,
                    arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, ""));
            arguments.putLong(AppConstant.BUNDLE_OFFER_START_FROM,
                    arguments.getLong(BUNDLE_PREVIOUS_OFFER_START_FROM, 0L));
            arguments.putString(AppConstant.BUNDLE_OFFER_JSON,
                    arguments.getString(AppConstant.BUNDLE_PREVIOUS_OFFER_JSON, null));
        }
    }

    private void removedSelectedOffer() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            arguments.putString(AppConstant.BUNDLE_OFFER_ID, "");
            arguments.putString(AppConstant.BUNDLE_OFFER_TYPE, "");
            arguments.putString(AppConstant.BUNDLE_OFFER_JSON, null);
        }
    }

    private void proceedAllOffers() {
        // All Offer Fragment
        AllOffersFragment allOffersFragment = new AllOffersFragment();
        allOffersFragment.setArguments(getArguments());

        addToBackStack(getFragmentManager(), allOffersFragment);
    }

    private void proceedBookingConfirmation() {
        // Navigate to Booking Confirmation Screen
        com.dineout.book.fragment.bookingflow.BookingConfirmationFragment
                bookingConfirmationFragment = new com.dineout.book.fragment.bookingflow.BookingConfirmationFragment();
        bookingConfirmationFragment.setArguments(getArguments());

        addToBackStack(getFragmentManager(), bookingConfirmationFragment);
    }

    private void proceedDealTicketCounter() {
        // Navigate to Deal Ticket Counter
        DealTicketQuantityFragment dealTicketQuantityFragment = new DealTicketQuantityFragment();
        dealTicketQuantityFragment.setArguments(getArguments());

        addToBackStack(getFragmentManager(), dealTicketQuantityFragment);
    }

    private void showWaitTime() {
        // Get Wait Time
        NewWaitTimeFragment waitTimeFragment = new NewWaitTimeFragment();
        waitTimeFragment.setArguments(getArguments());
        waitTimeFragment.setWaitTimeListener(this);

        // Pass Wait Time Response
        waitTimeFragment.setWaitTitle(DOPreferences.getWaitTimeTitle(getActivity().getApplicationContext()));

        String waitTimeMessage = DOPreferences.getWaitTimeMessage(getActivity().getApplicationContext());
        if (getArguments() != null) {
            waitTimeMessage = waitTimeMessage.replace("#restaurant_name#", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_NAME, ""));
        }
        waitTimeFragment.setWaitingMsg(waitTimeMessage);

        addToBackStack(getFragmentManager(), waitTimeFragment);
    }

    @Override
    public void onContinueWaitTime() {
//        decideUserNavigation();
        if (TextUtils.isEmpty(DOPreferences.getDinerEmail(getActivity().getApplicationContext()))) {
            UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
        } else {
            decideUserNavigation();
        }
    }

    @Override
    public void trackExpandSection(String label) {
        // Track Event
        trackEventGA(getString(R.string.ga_screen_time_slot_selection), getString(R.string.ga_action_expand_section), label);
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
        if ("1".equals(DOPreferences.isDinerDoPlusMember(getActivity()))
                && AppConstant.BOOKING_TYPE_DOP_OFFER.equals(getArguments().getString(AppConstant.BUNDLE_OFFER_TYPE))) {
            Bundle arguments = getArguments();
            if (arguments == null || !arguments.getBoolean(AppConstant.BUNDLE_VIA_DEEPLINK, false)) {
                onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
            }
        } else {
            decideUserNavigation();
        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        legendMap = null;
        dataJsonObject = null;
        layoutInflater = null;
        dateSlotJsonArray = null;
        selectedDateJsonObject = null;
        bookingDateSlotAdapter = null;
        bookingTimeSlotSectionAdapter = null;
        disabledTimeSlotDialog = null;
        recyclerViewTimeSlot = null;
        linearLayoutLegendSection = null;
        relativeLayoutTimeSlotError = null;
    }


    @Override
    public void handleNavigation() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_SlotSelection",getResources().getString(R.string.d_back_click),getResources().getString(R.string.d_back_click),hMap);

        super.handleNavigation();
    }
}
