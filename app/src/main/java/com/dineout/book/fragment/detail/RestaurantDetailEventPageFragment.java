package com.dineout.book.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.bookingflow.EventConfirmationFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.dialogs.GenericShareDialog;
import com.dineout.recycleradapters.RestaurantDetailEventPageAdapter;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RestaurantDetailEventPageFragment extends AuthenticationWrapperJSONReqFragment
        implements RestaurantDetailEventPageAdapter.EventClickHandler, View.OnClickListener {

    private final int REQUEST_CODE_RESTAURANT_DETAIL_EVENT = 101;

    private RecyclerView recyclerViewEvent;
    private RestaurantDetailEventPageAdapter pageAdapter;
    private Bundle restaurantBundle;
    private String restaurantId = "";
    private Button buttonEventBuyNow;
    private NestedScrollView nestedScrollViewNoEvents;
    private JSONObject dataJsonObject;
    private String restaurantCategory;
    private String restaurantType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail_event_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get Bundle
        restaurantBundle = getArguments();

        if (restaurantBundle != null) {
            restaurantId = restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, "");
            restaurantCategory = restaurantBundle.getString("RESTAURANT_CATEGORY");
            restaurantType = restaurantBundle.getString("RESTAURANT_TYPE");

        }

        // Initialize View
        initializeView();

        // Take API Hit
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity()));
    }

    private void initializeView() {
        // Page Adapter Instance
        pageAdapter = new RestaurantDetailEventPageAdapter(getActivity().getApplicationContext(), this);

        pageAdapter.setSource("RDP");

        // Define Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Get Recycler Instance
        recyclerViewEvent = (RecyclerView) getView().findViewById(R.id.recyclerView_rest_detail_event);
        recyclerViewEvent.setLayoutManager(linearLayoutManager);
        recyclerViewEvent.setAdapter(pageAdapter);
        ((SimpleItemAnimator) recyclerViewEvent.getItemAnimator()).setSupportsChangeAnimations(false);

        // Get Error Section
        nestedScrollViewNoEvents = (NestedScrollView) getView().findViewById(R.id.nestedScrollView_no_events);

        // Get Buy Now
        buttonEventBuyNow = (Button) getView().findViewById(R.id.button_event_buy_now);
        buttonEventBuyNow.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.button_event_buy_now) {
            handleBuyNow();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Take API Hit
        getEventDetailsFromAPI();
    }

    private void getEventDetailsFromAPI() {
        // Show Loader
        showLoader();

        // Check if View is attached
        if (getView() != null) {
            // Remove All Views in Recycler View
            recyclerViewEvent.removeAllViews();

            // Hide Buy Now Button
            buttonEventBuyNow.setVisibility(View.GONE);

            // Hide Error Section
            nestedScrollViewNoEvents.setVisibility(View.GONE);
        }

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_RESTAURANT_DETAIL_EVENT,
                AppConstant.NODE_RESTAURANT_DETAIL_URL,
                ApiParams.getRestaurantDetailsParams(restaurantId,
                        AppConstant.REST_DETAIL_EVENT_DETAILS),
                this, this, true);
    }

    @Override
    public void onEventCardClick(String title, String url) {
        // trackEventGA(getString(R.string.ga_screen_details_events), getString(R.string.ga_action_event_card), null);

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

        addToBackStack(getParentFragment().getFragmentManager(), webViewFragment);
    }

    @Override
    public void onShareCardClick(JSONObject shareJsonObject) {
        // Track Event
        // trackEventGA(getString(R.string.ga_screen_details_events), getString(R.string.ga_action_share), null);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("resId",restaurantId);
        }
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,getString(R.string.d_restaurant_event_share_click), shareJsonObject.optString("event_name") + "_" + shareJsonObject.optString("event_id"), hMap);

        if (shareJsonObject != null) {
            GenericShareDialog shareDialog = GenericShareDialog.getInstance(shareJsonObject.toString());
            if (getParentFragment() != null) {
                shareDialog.show(getParentFragment().getFragmentManager(), "Events");
            }
        }
    }

    //proceed to event confirmation
    public void handleBuyNow() {
        // Track Event
        String eventId = "";
        String eventTitle="";


        // track event for qgraph, apsalar and branch
        HashMap<String, Object> props = new HashMap<>();
        props.put("restaurantName", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
        props.put("restID", restaurantId);
        props.put("area", dataJsonObject.optString("areaName"));
        props.put("cuisinesList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
        props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
        props.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));

        if (pageAdapter != null) {
            JSONArray selectedEventJsonArray = pageAdapter.getSelectedEvents();
            JSONObject events = selectedEventJsonArray.optJSONObject(0);
            if (events != null && events.length() > 0) {
                eventId = events.optString("eventID");
                eventTitle= events.optString("title");
            }
        }
        props.put("eventID", eventId);


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if (hMap != null && props != null) {
            hMap.putAll(AppUtil.convertAttributes(props));
        }

        // send count_ly and ga tracking
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                "RestaurantEventCTAClick", eventTitle+"_"+eventId, hMap);


        // send q graph
        trackEventQGraphApsalar("RestaurantEventCTAClick", props, true, false, false);


        if (TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) {
            askUserToLoginForBuyingEvents();

        } else {
            proceedForEventConfirmation();
        }
    }

    private void askUserToLoginForBuyingEvents() {

        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }

    private void proceedForEventConfirmation() {
        // Save Event Data
        saveEventsInBundle();

        if (pageAdapter != null) {
            JSONArray selectedEventJsonArray = pageAdapter.getSelectedEvents();

            if (selectedEventJsonArray != null && selectedEventJsonArray.length() > 0) {
                int eventSize = selectedEventJsonArray.length();

                if (eventSize > 1) {
                    UiUtil.showToastMessage(getActivity().getApplicationContext(), "You can purchase tickets for only one Event");

                } else {
                    JSONObject events = selectedEventJsonArray.optJSONObject(0);

                    if (restaurantBundle == null)
                        restaurantBundle = new Bundle();

                    // Get Event Date
                    String date = events.optString("toDate");
                    if (!AppUtil.isStringEmpty(date)) {
                        restaurantBundle.putLong(AppConstant.DATE, Long.valueOf(events.optString("toDate")));
                        restaurantBundle.putAll(DateTimeSlotUtil.getCurrentTime());

                        JSONArray extraInfoJsonArray = dataJsonObject.optJSONArray("extraInfo");
                        if (extraInfoJsonArray != null && extraInfoJsonArray.length() > 0) {
                            restaurantBundle.putString(AppConstant.BUNDLE_EXTRA_INFO, extraInfoJsonArray.toString()); // Extra Info
                        }
                    }

                    EventConfirmationFragment eventConfirmationFragment = new EventConfirmationFragment();
                    eventConfirmationFragment.setEventsObj(events);
                    eventConfirmationFragment.setArguments(restaurantBundle);
                    addToBackStack(getActivity(), eventConfirmationFragment);
                }
            } else {
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Select tickets for an Event");
            }
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAIL_EVENT) {
            // Check Response
            if (responseObject != null && responseObject.optBoolean("status")) {
                if (responseObject.optJSONObject("data") != null &&
                        responseObject.optJSONObject("data").optJSONArray("stream") != null &&
                        responseObject.optJSONObject("data").optJSONArray("stream").length() > 0) {

                    JSONObject restDetailJsonObject = responseObject.optJSONObject("data").optJSONArray("stream").optJSONObject(0);

                    if (restDetailJsonObject != null) {
                        // Get Data JSONObject
                        JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

                        if (dataJsonObject != null) {
                            // Set Data
                            this.dataJsonObject = dataJsonObject;

                            // Get Events
                            JSONArray eventJsonArray = dataJsonObject.optJSONArray("events");

                            if (restaurantBundle != null) {
                                // Get Event JSON from Bundle
                                String eventsJson = restaurantBundle.getString(AppConstant.BUNDLE_EVENTS);

                                if (!AppUtil.isStringEmpty(eventsJson)) {
                                    try {
                                        JSONArray eventJsonArrayBundle = new JSONArray(eventsJson);

                                        if (eventJsonArrayBundle.length() > 0) {
                                            // Set Event JSON
                                            eventJsonArray = eventJsonArrayBundle;
                                        }
                                    } catch (JSONException e) {
//                                        e.printStackTrace();
                                    }
                                }
                            }

                            // Check for NULL
                            if (eventJsonArray != null && eventJsonArray.length() > 0) {
                                // Handle Event Response
                                handleEventResponse(eventJsonArray);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        // Get Error Title
        TextView textViewErrorTitle = (TextView) getView().findViewById(R.id.textView_error_title);
        textViewErrorTitle.setText("No Events Available");

        // Show Error Message
        nestedScrollViewNoEvents.setVisibility(View.VISIBLE);

        // Hide Buy Now Button
        buttonEventBuyNow.setVisibility(View.GONE);
    }

    private void handleEventResponse(JSONArray eventJsonArray) {
        // Set Data in Adapter
        pageAdapter.setData(eventJsonArray);
        pageAdapter.notifyDataSetChanged();

        // Update BuyNow for Free Events
        updateBuyNowForFreeEvents(eventJsonArray);
    }

    private void updateBuyNowForFreeEvents(JSONArray eventJsonArray) {
        boolean isEventCTAEnabled = true;

        if (dataJsonObject != null) {
            isEventCTAEnabled = (dataJsonObject.optInt("eventCTAEnabled", 1) == 1);
        }

        // Set Button
        buttonEventBuyNow.setVisibility(View.VISIBLE);
        buttonEventBuyNow.setEnabled(isEventCTAEnabled);
    }

    private void saveEventsInBundle() {
        if (pageAdapter != null) {
            // Get Event JSON
            JSONArray eventJsonArray = pageAdapter.getJsonArray();

            // Save JSON Array in Bundle
            if (restaurantBundle != null && eventJsonArray != null && eventJsonArray.length() > 0) {
                restaurantBundle.putString(AppConstant.BUNDLE_EVENTS, eventJsonArray.toString());
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        recyclerViewEvent = null;
        pageAdapter = null;
        restaurantBundle = null;
        restaurantId = null;
        buttonEventBuyNow = null;
        dataJsonObject = null;
    }
}
