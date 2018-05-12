package com.dineout.book.fragment.bookingflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.book.dialogs.GenericShareDialog;
import com.dineout.recycleradapters.AllOfferAdapter;
import com.dineout.recycleradapters.RestaurantDetailEventPageAdapter;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_IS_EDIT_BOOKING;

public class AllOffersFragment extends MasterDOJSONReqFragment
        implements View.OnClickListener, AllOfferAdapter.AllOfferClickListener,
        RestaurantDetailEventPageAdapter.EventClickHandler {

    private final int REQUEST_CODE_GET_OFFERS = 101;

    private Bundle arguments;
    private AllOfferAdapter allOfferAdapter;
    private RestaurantDetailEventPageAdapter eventsAdapter;
    private RecyclerView recyclerViewAllOffers;
    private View relativeLayoutOfferError;
    private Button buttonContinue;
    private JSONObject streamDataJsonObject;
    private boolean isRequestInFlight = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Bundle
        arguments = getArguments();
        if (arguments == null)
            arguments = new Bundle();

        // All Offer Adapter

        boolean hasRightNowSection=arguments.getBoolean("hasRightNowSection");
        int position = arguments.getInt("timeSlotSectionPos");
        boolean isInstantBooking= hasRightNowSection && (position==0);
        allOfferAdapter = new AllOfferAdapter(getActivity(),isInstantBooking);

        // Page Adapter Instance
        eventsAdapter = new RestaurantDetailEventPageAdapter(getActivity(), this);

        // Take API Hit
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_offers, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
        //trackScreenToGA(getString(R.string.ga_screen_select_offers));



        // Initialize View
        initializeView();
        if (isRequestInFlight) {
            showLoader();
        }

        // Check for NULL
        if (streamDataJsonObject != null) {
            renderResponse(streamDataJsonObject);
        }
    }

    private void initializeView() {
        // Get Back
        ImageView imageViewAllOfferBack = (ImageView) getView().findViewById(R.id.imageView_all_offer_back);
        imageViewAllOfferBack.setOnClickListener(this);

        // Set Sub Title
        TextView textViewSubTitle = (TextView) getView().findViewById(R.id.textView_all_offer_sub_title);
        textViewSubTitle.setText(getBookingDateTime());

        // Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Get RecyclerView
        recyclerViewAllOffers = (RecyclerView) getView().findViewById(R.id.recyclerView_all_offers);
        recyclerViewAllOffers.setLayoutManager(linearLayoutManager);

        // Get Continue Button
        buttonContinue = (Button) getView().findViewById(R.id.button_continue);
        buttonContinue.setOnClickListener(this);

        // Get Error
        relativeLayoutOfferError = getView().findViewById(R.id.relativeLayout_offer_error);
    }

    private String getBookingDateTime() {
        if (arguments != null) {
            return DateTimeSlotUtil.getDateTimeSubTitle(
                    arguments.getString(AppConstant.BUNDLE_SELECTED_DATE, ""),
                    arguments.getString(AppConstant.BUNDLE_SELECTED_TIME, ""));
        }

        return "";
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Get Offers
        getOffers();
    }

    private void getOffers() {
        // Check if Fragment is attached to Activity
        if (getActivity() == null)
            return;

        // Show Loader
        showLoader();

        // Check if View is attached
        if (getView() != null) {
            // Hide Error Message
            showHideErrorMessage(View.GONE);

            // Empty Recycler View
            recyclerViewAllOffers.removeAllViews();

            // Hide Button
            buttonContinue.setVisibility(View.GONE);
        }

        // Get Arguments
        if (getArguments() == null)
            return;

        isRequestInFlight = true;

        // Get Offers
        getNetworkManager().jsonRequestGetNode(
                REQUEST_CODE_GET_OFFERS,
                AppConstant.URL_GET_OFFERS,
                ApiParams.getOffers(
                        arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""),
                        arguments.getLong(AppConstant.BUNDLE_DATE_TIMESTAMP, 0L),
                        arguments.getString(AppConstant.BUNDLE_OFFER_ID, ""),
                        ((arguments.getBoolean(BUNDLE_IS_EDIT_BOOKING, false) ?
                                AppConstant.BOOKING_TYPE_EDIT :
                                arguments.getString(AppConstant.BUNDLE_OFFER_TYPE, "")))),
                this, this, true);
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.imageView_all_offer_back) {
            handleNavigationBack();

        } else if (viewId == R.id.button_continue) {
            handleContinueButton(view);
        }
    }

    private void handleNavigationBack() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getResources().getString(R.string.countly_select_offer),
                getResources().getString(R.string.d_back_click),getResources().getString(R.string.d_back_click),hMap);

        popBackStack(getFragmentManager());
    }

    private void handleContinueButton(View view) {
        // Get Tag
        int responseType = (int) view.getTag();

        if (responseType == 1) {
            // Navigate to Booking Confirmation Screen
            proceedBookingConfirmation(null);

        } else if (responseType == 2) {
            // Buy Event
            handleBuyNow();
        }
    }

    public void handleBuyNow() {
        // Track Event
        trackEventGA(getString(R.string.ga_screen_select_offers), getString(R.string.ga_action_buy_event), null);

        proceedForEventConfirmation();
    }

    private void proceedForEventConfirmation() {
        String eventId="";
        String eventTitle="";
        // Save Event Data
        saveEventsInBundle();

        if (eventsAdapter != null) {
            JSONArray selectedEventJsonArray = eventsAdapter.getSelectedEvents();

            if (selectedEventJsonArray != null && selectedEventJsonArray.length() > 0) {
                int eventSize = selectedEventJsonArray.length();

                if (eventSize > 1) {
                    UiUtil.showToastMessage(getActivity().getApplicationContext(), "You can purchase tickets for only one Event");

                } else {
                    JSONObject events = selectedEventJsonArray.optJSONObject(0);
                    if (events != null && events.length() > 0) {
                        eventId = events.optString("eventID");
                        eventTitle= events.optString("title");
                    }

                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                    if (hMap != null) {
                        hMap.put("restaurantName", arguments.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
                        hMap.put("restID", arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID));
                        hMap.put("eventID", eventId);
                    }

                    // send countly and ga tracking
                    trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID),
                            "RestaurantEventCTAClick", eventTitle+"_"+eventId, hMap);


                    String date = events.optString("toDate");

                    if (!AppUtil.isStringEmpty(date)) {
                        arguments.putLong(AppConstant.DATE, Long.valueOf(events.optString("toDate")));
                    }

                    EventConfirmationFragment eventConfirmationFragment = new EventConfirmationFragment();
                    eventConfirmationFragment.setEventsObj(events);
                    eventConfirmationFragment.setArguments(arguments);
                    addToBackStack(getActivity(), eventConfirmationFragment);
                }
            } else {
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Select tickets for an Event");
            }
        }
    }

    @Override
    public void onCardClick(JSONObject jsonObject) {
        // Check for NULL
        if (jsonObject != null) {
            // Check for type = events -> Event Tab
            if (jsonObject.optString("type", "").equalsIgnoreCase("event")) {
                // Go to Event Tab
                navigateToEventTab();

            } else {

                int poc = jsonObject.optInt("position");
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null){
                    hMap.put("poc",Integer.toString(poc));
                }
                trackEventForCountlyAndGA(getString(R.string.countly_select_offer),getString(R.string.d_offer_detail_click),
                        jsonObject.optString("title_2"),hMap);

                // Go to Web View
                navigateToWebView(jsonObject);
            }
        }
    }

    private void navigateToEventTab() {
        // Open Event Tab
        arguments.putString(AppConstant.BUNDLE_RESTAURANT_TAB, AppConstant.RESTAURANT_TAB_EVENT);

        // Instantiate Restaurant Detail Screen
        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
        restaurantDetailFragment.setArguments(arguments);

        addToBackStack(getFragmentManager(), restaurantDetailFragment);
    }

    private void navigateToWebView(JSONObject jsonObject) {
        // Get URL
        String url = jsonObject.optString("url", "");

        if (!AppUtil.isStringEmpty(url)) {
            // Set Bundle
            Bundle bundle = new Bundle();
            bundle.putString("title", jsonObject.optString("title_2", ""));
            bundle.putString("url", url);

            // Get Instance
            WebViewFragment webViewFragment = new WebViewFragment();
            webViewFragment.setArguments(bundle);

            addToBackStack(getFragmentManager(), webViewFragment);
        }
    }

    @Override
    public void onOfferButtonClick(JSONObject jsonObject) {
        // Check for NULL
        if (jsonObject != null) {
            // Get Is Paid Flag
            int isPaid = jsonObject.optInt("is_paid", -1);
            if (isPaid == 0) { // Reserve Offer
                handleReserveButtonClick(jsonObject);

            } else if (isPaid == 1) { // Buy Deal
                handleBuyButtonClick(jsonObject);

            } else {
                // Do Nothing...
            }
        }
    }

    private void handleReserveButtonClick(JSONObject jsonObject) {
        // Track Event
       // trackEventGA(getString(R.string.ga_screen_select_offers), getString(R.string.ga_action_reserve_offer), null);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("poc",Integer.toString(jsonObject.optInt("position")));
            hMap.put("cta","Reserve_Offer");
            hMap.put("offerID",jsonObject.optString("id"));
        }
        trackEventForCountlyAndGA(getResources().getString(R.string.countly_select_offer),getResources().getString(R.string.d_offer_cta_click),
                jsonObject.optString("title_2"),hMap);

        // Check for NULL
        if (jsonObject != null) {
            proceedBookingConfirmation(jsonObject);
        }
    }

    private void proceedBookingConfirmation(JSONObject jsonObject) {
        // Add Selected Offer in bundle
        if (jsonObject != null) {
            arguments.putString(AppConstant.BUNDLE_OFFER_ID, jsonObject.optString("id", ""));
            arguments.putString(AppConstant.BUNDLE_OFFER_TYPE, jsonObject.optString("type", ""));
            arguments.putString(AppConstant.BUNDLE_OFFER_JSON, jsonObject.toString());
        }

        BookingConfirmationFragment bookingConfirmationFragment = new BookingConfirmationFragment();
        bookingConfirmationFragment.setArguments(arguments);

        addToBackStack(getFragmentManager(), bookingConfirmationFragment);
    }

    private void handleBuyButtonClick(JSONObject jsonObject) {
        // Track Event
        trackEventGA(getString(R.string.ga_screen_select_offers), getString(R.string.ga_action_buy_deals), arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID));

        // Add Selected Offer in bundle
        arguments.putString(AppConstant.BUNDLE_OFFER_ID, jsonObject.optString("id", ""));
        arguments.putString(AppConstant.BUNDLE_OFFER_TYPE, jsonObject.optString("type", ""));
        arguments.putString(AppConstant.BUNDLE_OFFER_JSON, jsonObject.toString());

        // Deal Ticket Quantity
        DealTicketQuantityFragment dealTicketQuantityFragment = new DealTicketQuantityFragment();
        dealTicketQuantityFragment.setArguments(arguments);

        addToBackStack(getFragmentManager(), dealTicketQuantityFragment);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null) {
            return;
        }

        isRequestInFlight = false;

        if (responseObject != null && responseObject.optBoolean("status", false)) {
            // Get Data
            JSONObject dataJsonObject = responseObject.optJSONObject("data");

            if (dataJsonObject != null) {
                // Get Stream
                JSONArray streamJsonArray = dataJsonObject.optJSONArray("stream");

                if (streamJsonArray != null && streamJsonArray.length() > 0) {
                    // Get Stream Data
                    JSONObject streamDataJsonObject = streamJsonArray.optJSONObject(0);
                    this.streamDataJsonObject = streamDataJsonObject;
                    renderResponse(streamDataJsonObject);

                } else {
                    // Show Error Message
                    showHideErrorMessage(View.VISIBLE);
                }
            } else {
                // Show Error Message
                showHideErrorMessage(View.VISIBLE);
            }
        } else {
            // Show Error Message
            showHideErrorMessage(View.VISIBLE);
        }
    }

    private void showHideErrorMessage(int visibility) {
        // Show Error Message
        relativeLayoutOfferError.setVisibility(visibility);

        // Show Button
        buttonContinue.setVisibility(visibility);
    }

    private void renderResponse(JSONObject streamDataJsonObject) {
        if (streamDataJsonObject != null) {
            // Show Offer Message
            showOfferMessage(streamDataJsonObject.optString("header"));

            // Get Response Type
            int responseType = streamDataJsonObject.optInt("responseType", 0);

            // Set Title
            TextView textViewTitle = (TextView) getView().findViewById(R.id.textView_all_offer_title);

            if (responseType == 1) { // All Offers
                handleAllOffersResponse(streamDataJsonObject);

                // Show Continue button only if ONLY DEALS are coming based on hasOffer = 0/1
                buttonContinue.setVisibility(
                        (streamDataJsonObject.optInt("hasOffers", 0) == 1) ? View.GONE : View.VISIBLE);

                //track offers
                trackScreenName(getString(R.string.countly_select_offer));

                // Set "Select Offers" title
                textViewTitle.setText(R.string.text_select_offer);

            } else if (responseType == 2) { // Events

                //track event
                trackScreenName(getString(R.string.countly_select_offer));

                handleEventsResponse(streamDataJsonObject.optJSONArray("events"));

                // Show Button
                buttonContinue.setVisibility(View.VISIBLE);

                // Set "Select Event" title
                textViewTitle.setText(R.string.text_select_event);
            }

            // Set Tag
            buttonContinue.setTag(responseType);
        }
    }

    private void handleAllOffersResponse(JSONObject streamDataJsonObject) {
        // Get Offer Data
        JSONArray offerDataJsonArray = streamDataJsonObject.optJSONArray("offerData");

        if (offerDataJsonArray != null && offerDataJsonArray.length() > 0) {
            // Get All Offers Adapter
            allOfferAdapter.setJsonArray(offerDataJsonArray);
            allOfferAdapter.setImageLoader(getImageLoader());
            allOfferAdapter.setAllOfferClickListener(this);

            // Set Adapter in List
            recyclerViewAllOffers.setAdapter(allOfferAdapter);

            // Notify Data Change
            allOfferAdapter.notifyDataSetChanged();

        } else {
            // Set Error Title
            TextView textViewErrorTitle = (TextView) getView().findViewById(R.id.textView_error_title);
            String errorTitle = streamDataJsonObject.optString("errorTitle", "");
            errorTitle = ((AppUtil.isStringEmpty(errorTitle)) ? getString(R.string.text_time_slot_error_title) : errorTitle);
            textViewErrorTitle.setText(errorTitle);

            // Set Error Message
            TextView textViewErrorMessage = (TextView) getView().findViewById(R.id.textView_error_message);
            String errorMessage = streamDataJsonObject.optString("errorMsg", "");
            errorMessage = ((AppUtil.isStringEmpty(errorMessage)) ? getString(R.string.text_offer_error_message) : errorMessage);
            textViewErrorMessage.setText(errorMessage);

            // Show Error Message
            showHideErrorMessage(View.VISIBLE);
        }
    }

    private void handleEventsResponse(JSONArray eventJsonArray) {

        eventsAdapter.setSource("AllOffers");
        // Set Data in Adapter
        eventsAdapter.setData(eventJsonArray);

        // Set Adapter in List
        recyclerViewAllOffers.setAdapter(eventsAdapter);

        // Notify Data Change
        eventsAdapter.notifyDataSetChanged();

        // Update BuyNow for Free Events
        updateBuyNowForFreeEvents(eventJsonArray);
    }

    private void updateBuyNowForFreeEvents(JSONArray eventJsonArray) {
        boolean hasAllFreeEvents = true;

        int ticketSize = eventJsonArray.length();
        for (int index = 0; index < ticketSize; index++) {
            JSONObject ticketJsonObject = eventJsonArray.optJSONObject(index);
            if (ticketJsonObject != null) {
                int isPaymentRequired = ticketJsonObject.optInt("paymentRequired", 0);

                if (isPaymentRequired != 0) {
                    hasAllFreeEvents = false;
                    break;
                }
            }
        }

        // Set Button
        buttonContinue.setText(R.string.button_buy_now);
        buttonContinue.setEnabled(!hasAllFreeEvents);
    }

    private void showOfferMessage(String offerMessage) {
        // Check for NULL /  Empty
        if (!AppUtil.isStringEmpty(offerMessage)) {
            // Get Offer Message
            TextView textViewMessage = (TextView) getView().findViewById(R.id.textView_message);
            textViewMessage.setText(offerMessage);
            textViewMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEventCardClick(String title, String url) {
        // Track Event
      //  trackEventGA(getString(R.string.ga_screen_select_offers), getString(R.string.ga_action_event_card), null);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getResources().getString(R.string.countly_select_event),
                getResources().getString(R.string.d_event_image_click),title,hMap);

        // Save Event Data
        saveEventsInBundle();

        // Set Bundle
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);

        // Get Instance
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.setArguments(bundle);

        addToBackStack(getFragmentManager(), webViewFragment);
    }

    private void saveEventsInBundle() {
        if (eventsAdapter != null) {
            // Get Event JSON
            JSONArray eventJsonArray = eventsAdapter.getJsonArray();

            // Save JSON Array in Bundle
            if (getArguments() != null && eventJsonArray != null && eventJsonArray.length() > 0) {
                arguments.putString(AppConstant.BUNDLE_EVENTS, eventJsonArray.toString());
            }
        }
    }

    @Override
    public void onShareCardClick(JSONObject jsonObject) {
        // Track Event
       // trackEventGA(getString(R.string.ga_screen_select_offers), getString(R.string.ga_action_share), null);

        if (jsonObject != null) {
            GenericShareDialog shareDialog = GenericShareDialog.getInstance(jsonObject.toString());
            shareDialog.show(getFragmentManager(), "Events");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        allOfferAdapter = null;
        eventsAdapter = null;
        recyclerViewAllOffers = null;
        relativeLayoutOfferError = null;
        buttonContinue = null;
        streamDataJsonObject = null;
    }


}
