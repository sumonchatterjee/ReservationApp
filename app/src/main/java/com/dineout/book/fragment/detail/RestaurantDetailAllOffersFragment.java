package com.dineout.book.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.bookingflow.BookingTimeSlotFragment;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.recycleradapters.AllOfferAdapter;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_REST_START_FROM;

public class RestaurantDetailAllOffersFragment extends MasterDOJSONReqFragment
        implements AllOfferAdapter.AllOfferClickListener {

    private JSONObject restDetailJsonObject;
    private AllOfferAdapter allOfferAdapter;
    private Bundle restaurantBundle;

    private View noOfferView;
    private String restaurantCategory;
    private String restaurantType;

    // Get Instance
    public static RestaurantDetailAllOffersFragment getInstance(JSONObject reservationJsonObject) {
        RestaurantDetailAllOffersFragment restaurantDetailAllOffersFragment = new RestaurantDetailAllOffersFragment();

        restaurantDetailAllOffersFragment.restDetailJsonObject = reservationJsonObject;

        return restaurantDetailAllOffersFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // All Offer Adapter
        allOfferAdapter = new AllOfferAdapter(getActivity(),false);
        allOfferAdapter.setImageLoader(getImageLoader());
        allOfferAdapter.setAllOfferClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail_all_offers, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize View
        initializeView();

        // Initialize Data
        initializeData();
    }

    private void initializeView() {
        // Get Arguments
        restaurantBundle = getArguments();

        if(restaurantBundle!=null) {
            restaurantCategory = restaurantBundle.getString("RESTAURANT_CATEGORY");
            restaurantType = restaurantBundle.getString("RESTAURANT_TYPE");
        }

        // Set details in bundle
        setRestaurantDetailsInBundle(restDetailJsonObject);

        // Define Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Get Recycler
        RecyclerView recyclerViewAllOffers = (RecyclerView) getView().findViewById(R.id.recyclerView_all_offers);
        recyclerViewAllOffers.setLayoutManager(linearLayoutManager);
        recyclerViewAllOffers.setAdapter(allOfferAdapter);

        // Get Error
        noOfferView = getView().findViewById(R.id.nestedScrollView_no_offers);
    }

    private void setRestaurantDetailsInBundle(JSONObject restDetailJsonObject) {
        if (restDetailJsonObject != null) {
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            if (dataJsonObject != null) {
                // Check for NULL
                if (restaurantBundle == null) {
                    restaurantBundle = new Bundle();
                }

                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                        Integer.toString(dataJsonObject.optInt("restaurantId")));
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_NAME,
                        dataJsonObject.optString("profileName"));
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_CITY,
                        dataJsonObject.optString("cityName"));
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_AREA,
                        dataJsonObject.optString("areaName"));
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_LOCALITY,
                        dataJsonObject.optString("localityName"));

                JSONArray imagesJsonArray = dataJsonObject.optJSONArray("profileImages");
                if (imagesJsonArray != null && imagesJsonArray.length() > 0) {
                    JSONObject obj = imagesJsonArray.optJSONObject(0);
                    if (obj != null) {
                        String imageUrl = obj.optString("imageUrl");
                        if (!com.dineout.recycleradapters.util.AppUtil.isStringEmpty(imageUrl)) {
                            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_IMAGE_URL, imageUrl);
                        }
                    }
                }

                JSONArray cuisineJsonArray = dataJsonObject.optJSONArray("cuisine");
                if (cuisineJsonArray != null && cuisineJsonArray.length() > 0) {
                    String cuisineName = cuisineJsonArray.optString(0, "");
                    if (!com.dineout.recycleradapters.util.AppUtil.isStringEmpty(cuisineName)) {
                        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, cuisineName);
                    }

                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, cuisineJsonArray.toString());
                }

                trackArrayAdTechEvent("dCuisine", cuisineJsonArray);
                trackArrayAdTechEvent("dFType", dataJsonObject.optJSONArray("tags"));

                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, dataJsonObject.optJSONArray("tags") != null ?
                        dataJsonObject.optString("tags").toString() : "[]");

                String deepLink = DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostRestoDetail +
                        "?q=" + dataJsonObject.optString("restaurantId");
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, deepLink);

                // Add Buy From
                restaurantBundle.putLong(BUNDLE_REST_START_FROM, dataJsonObject.optLong("startFrom", 0L));
            }
        }
    }

    private void initializeData() {
        // Render All Offers
        renderAllOffers();

        // Ad Tech
        // completeAdTechSesion();
    }

    private void renderAllOffers() {
        // Check for NULL
        if (restDetailJsonObject != null) {
            // Get Offers
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Check for NULL
            if (dataJsonObject != null) {
                // Get Restaurant Data
                JSONArray reservationSectionJsonArray = dataJsonObject.optJSONArray("reservationSectionData");

                // Check for NULL /  Empty
                if (reservationSectionJsonArray != null && reservationSectionJsonArray.length() > 0) {
                    // Set Adapter
                    allOfferAdapter.setJsonArray(reservationSectionJsonArray);

                    // Notify Date Change
                    allOfferAdapter.notifyDataSetChanged();

                    // Log Event
                    logRestaurantDetailEvent();

                } else {
                    // Show Error
                    showHideErrorMessage(View.VISIBLE);
                }
            } else {
                // Show Error
                showHideErrorMessage(View.VISIBLE);
            }
        } else {
            // Show Error
            showHideErrorMessage(View.VISIBLE);
        }
    }

    private void logRestaurantDetailEvent() {
        // Set Properties
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("RestaurantName", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME, ""));
        props.put("Locality", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_LOCALITY, ""));
        props.put("Area", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_AREA, ""));
        props.put("City", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CITY, ""));

        if (restDetailJsonObject != null) {
            props.put("Tag", ((AppConstant.RESTAURANT_TYPE_DISCOVERY.equalsIgnoreCase(
                    com.dineout.recycleradapters.util.AppUtil.getRestaurantType(restDetailJsonObject.optInt("restaurantType", -1)))) ? "discovery" : "fulfillment"));
        }

        props.put("ResImageURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_IMAGE_URL, ""));
        props.put("ResDeepLink", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
        props.put("ResCuisine", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, ""));
        props.put("CuisinesList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
        props.put("TagList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));



        //track event for countly,ga and qgraph
        HashMap<String,String>map=DOPreferences.getGeneralEventParameters(getContext());
        if(map!=null){
            map.put("restaurantName", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME, ""));
            map.put("restID", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""));
            map.put("locality", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_LOCALITY, ""));
            map.put("area", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_AREA, ""));
            map.put("city", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CITY, ""));
            map.put("restaurantImageURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_IMAGE_URL, ""));
            map.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
            map.put("cuisinesList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
            map.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));

        }

        trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType+"_"+restaurantCategory,"RestaurantView", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME, "")+"_"+restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""),map);
        trackEventQGraphApsalar("RestaurantView",props,true,false,false);
    }

    private void showHideErrorMessage(int visibility) {
        // Show Error Message
        noOfferView.setVisibility(visibility);
    }

    @Override
    public void onCardClick(JSONObject jsonObject) {
        JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

        // Check for NULL
        if (dataJsonObject != null) {
            int type = dataJsonObject.optInt("restaurantType");

            if(jsonObject.optString("type", "").equalsIgnoreCase("offer")) {
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null){
                    hMap.put("restID",dataJsonObject.optString("restaurantId"));
                    hMap.put("posOfOffer", jsonObject.optString("position", ""));
                }

                trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                        getString(R.string.d_restaurant_offerdetails_click),
                        jsonObject.optString("title_2") + "_" + jsonObject.optString("id"), hMap);
            }
        }

        // Check for NULL
        if (jsonObject != null) {
            // Check for type = events -> Event Tab
            if (jsonObject.optString("type", "").equalsIgnoreCase("event")) {
                // Go to Event Tab
                onEventCardClick(jsonObject);

            } else {
                //track event for qgraph, apsalar and branch, countly and ga
                HashMap<String, Object> props = new HashMap<>();
                props.put("restaurantName",dataJsonObject.optString("profileName"));
                props.put("restID",dataJsonObject.optString("restaurantId"));
                props.put("area",dataJsonObject.optString("areaName"));
                props.put("cuisinesList",  restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
                props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
                props.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
                props.put("dealDetailURL" ,jsonObject.optString("url", ""));
                props.put("dealID",jsonObject.optString("id"));

                trackEventQGraphApsalar( getString(R.string.d_restaurant_dealdetails_click),props,true,false,false);

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null && props!=null){
                   hMap.putAll(AppUtil.convertAttributes(props));
                }

                trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                        getString(R.string.d_restaurant_dealdetails_click),
                        jsonObject.optString("title_2")+"_"+jsonObject.optString("id"),hMap);

                JSONArray groups=jsonObject.optJSONArray("ticketGroups");

                String ticketGroup = "";
                if(groups != null) {
                    for(int i = 0;i < groups.length(); i++){
                        ticketGroup += groups.optInt(i);

                        if (i < groups.length() - 1) {
                            ticketGroup += "_";
                        }
                    }
                }

                // Go to Web View
                navigateToWebView(jsonObject);
            }
        }
    }

    public void onEventCardClick(JSONObject obj) {
        if (getParentFragment() != null && getParentFragment().getClass() == RestaurantDetailFragment.class) {

            //track for qgraph, apsalar and branch
            HashMap<String, Object> props = new HashMap<>();
            props.put("restaurantName",restDetailJsonObject.optString("profileName"));
            props.put("restID",restDetailJsonObject.optString("restaurantId"));
            props.put("area",restDetailJsonObject.optString("areaName"));
            props.put("cuisinesList",  restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
            props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
            props.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
            props.put("dealDetailURL" ,restDetailJsonObject.optString("url", ""));
            props.put("eventID",restDetailJsonObject.optString("id"));


        //track event for countly and ga
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null && props!=null) {
            hMap.putAll(AppUtil.convertAttributes(props));
        }

            trackEventQGraphApsalar(getString(R.string.d_restaurant_event_click),props,true,false,false);
            trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                    getString(R.string.d_restaurant_event_click),
                    restDetailJsonObject.optString("profileName")+"_"+obj.optString("id"),hMap);


            ((RestaurantDetailFragment) getParentFragment()).switchToEventTab();
        }
    }

    private void navigateToWebView(JSONObject jsonObject) {
        // Get URL
        String url = jsonObject.optString("url", "");

        if (!AppUtil.isStringEmpty(url)) {
            // Prepare URL
            url = setEarningsInUrl(url, restDetailJsonObject.optJSONObject("data").optInt(AppConstant.BUNDLE_USER_EARNINGS, 0));

            // Set Bundle
            Bundle bundle = new Bundle();
            bundle.putString("title", jsonObject.optString("title_2", ""));
            bundle.putString("url", url);

            // Get Instance
            WebViewFragment webViewFragment = new WebViewFragment();
            webViewFragment.setArguments(bundle);

            addToBackStack(getParentFragment().getFragmentManager(), webViewFragment);
        }
    }

    private String setEarningsInUrl(String url, int walletAmount) {
        if (!AppUtil.isStringEmpty(url)) {
            // Set Earnings
            url = url.replace(AppConstant.URL_SMARTPAY_PLACE_HOLDER, Integer.toString(walletAmount));
        }

        return url;
    }

    @Override
    public void onOfferButtonClick(JSONObject jsonObject) {
        // Check for NULL
        if (jsonObject != null) {
            // Get Type
            String type = jsonObject.optString("type", "");

            if (type.equalsIgnoreCase(AppConstant.BOOKING_TYPE_EVENT)) {
                // Track Event
               // trackEventGA(getString(R.string.ga_screen_details_reservation), getString(R.string.ga_action_buy_event), null);

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null){
                    hMap.put("restID",restDetailJsonObject.optString("restaurantId"));
                }
                trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                        getString(R.string.d_restaurant_eventcta_click),jsonObject.optString("title_2")+"_"+jsonObject.optString("id"),hMap);

                // Go to Event Tab
                onEventCardClick(jsonObject);

            } else {
                Bundle bundle = new Bundle();

                // Track Offer / Deal
                if (type.equalsIgnoreCase(AppConstant.BOOKING_TYPE_DEAL)) {
                    JSONArray groups=jsonObject.optJSONArray("ticketGroups");

                    String ticketGroup = "";
                    if(groups != null) {
                        for(int i = 0;i < groups.length(); i++){
                            ticketGroup += groups.optInt(i);

                            if (i < groups.length() - 1) {
                                ticketGroup += "_";
                            }
                        }
                    }

                    bundle.putString(AppConstant.BUNDLE_TICKET_GROUPS, ticketGroup);

                    //track events for qgraph, apsalar,branch, countly and ga
                    HashMap<String, Object> props = new HashMap<>();
                    props.put("restaurantName",restDetailJsonObject.optString("profileName"));
                    props.put("restID",restDetailJsonObject.optString("restaurantId"));
                    props.put("area",restDetailJsonObject.optString("areaName"));
                    props.put("cuisinesList",  restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
                    props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
                    props.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
                    props.put("dealDetailURL" ,jsonObject.optString("url", ""));
                    props.put("dealID",jsonObject.optString("id"));

                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    if(hMap!=null && props!=null){
                        hMap.put("poc",jsonObject.optString("position"));
                        hMap.putAll(AppUtil.convertAttributes(props));
                    }

                    trackEventQGraphApsalar( getString(R.string.d_restaurant_dealdetails_click),props,true,false,false);

                    trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                            getString(R.string.d_restaurant_dealcta_click),jsonObject.optString("title_2")+"_"+jsonObject.optString("id"),hMap);


                } else {

                    // track event for qgraph, apsalar and branch, countly and ga
                    HashMap<String, Object> props = new HashMap<>();
                    props.put("restaurantName",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
                    props.put("restID",restDetailJsonObject.optString("restaurantId"));
                    props.put("area",restDetailJsonObject.optString("areaName"));
                    props.put("city",restDetailJsonObject.optString("cityName"));
                    props.put("locality",restDetailJsonObject.optString("localityName"));
                    props.put("cuisinesList",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
                    props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
                    props.put("offerID",jsonObject.optString("id"));
                    props.put("DeepLinkURL",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));

                   // props.put("cuisinesList",getCusineList());

                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    if(hMap!=null && props!=null){
                        hMap.putAll(AppUtil.convertAttributes(props));
                    }

                    trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                            getString(R.string.d_restaurant_offercta_click),jsonObject.optString("title_2")+"_"+jsonObject.optString("id"),hMap);

                    trackEventQGraphApsalar("RestaurantOfferCTAClick",props,true,false,false);
                }

                bundle.putString(AppConstant.BUNDLE_OFFER_ID, jsonObject.optString("id", ""));
                bundle.putString(AppConstant.BUNDLE_OFFER_TYPE, jsonObject.optString("type", ""));
                bundle.putLong(AppConstant.BUNDLE_OFFER_START_FROM, jsonObject.optLong("startFrom", 0L));
                bundle.putString(AppConstant.BUNDLE_OFFER_JSON, jsonObject.toString());

                bundle.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_ID, jsonObject.optString("id", ""));
                bundle.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, jsonObject.optString("type", ""));
                bundle.putLong(AppConstant.BUNDLE_PREVIOUS_OFFER_START_FROM, jsonObject.optLong("startFrom", 0L));
                bundle.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_JSON, jsonObject.toString());

                bundle.putString(AppConstant.BUNDLE_EXTRA_INFO, getExtraInfo().toString()); // Extra Info
                bundle.putDouble("COSTFORTWO", restDetailJsonObject.optJSONObject("data").optDouble("costForTwo"));
                bundle.putInt(AppConstant.BUNDLE_USER_EARNINGS,
                        restDetailJsonObject.optJSONObject("data").optInt(AppConstant.BUNDLE_USER_EARNINGS, 0));

                // Check if User is logged In
//                if (TextUtils.isEmpty(DOPreferences.getDinerEmail(getActivity().getApplicationContext()))) {
//                    // Ask User to Login
//                    askUserToLogin(bundle);
//                    return;
//                }

                // Booking Time Slot
                proceedSelectBookingSlot(bundle);
            }
        }
    }

    // Ask User to Login
    private void askUserToLogin(final Bundle bookingBundle) {

        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null,
                new BookingLoginCallback(bookingBundle, BookingLoginCallback.OFFER));
    }

    private void proceedLoginSuccess(Bundle bookingBundle) {

        if (getActivity() == null)
            return;

        // Set New Diner Flag
        if (DOPreferences.isDinerNewUser(getActivity().getApplicationContext())) {
            // Set Is New User flag
            DOPreferences.setDinerNewUser(getActivity().getApplicationContext(), "0");
        }

        proceedSelectBookingSlot(bookingBundle);
    }

    private void proceedSelectBookingSlot(Bundle bookingBundle) {
        // Add Booking details to bundle
        if (restaurantBundle != null) {
            bookingBundle.putAll(restaurantBundle);
        }

        BookingTimeSlotFragment bookingTimeSlotFragment = new BookingTimeSlotFragment();
        bookingTimeSlotFragment.setArguments(bookingBundle);

        addToBackStack(getParentFragment().getFragmentManager(), bookingTimeSlotFragment);
    }

    private JSONArray getExtraInfo() {
        JSONArray extraInfoJsonArray = null;

        if (restDetailJsonObject != null) {
            // Get Data
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");
            if (dataJsonObject != null) {
                extraInfoJsonArray = dataJsonObject.optJSONArray("extraInfo");
            }
        }

        return extraInfoJsonArray;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        restDetailJsonObject = null;
        allOfferAdapter = null;
        noOfferView = null;
    }

    private class BookingLoginCallback implements UserAuthenticationController.LoginFlowCompleteCallbacks {

        public static final int OFFER = 0x01;
        private Bundle mBundle;
        private int mType;

        public BookingLoginCallback(Bundle bundle, int type) {
            mBundle = bundle;
            mType = type;
        }

        @Override
        public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {

        }

        @Override
        public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {

        }
    }
}
