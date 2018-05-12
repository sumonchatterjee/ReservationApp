package com.dineout.book.fragment.bookingflow;

import android.os.Bundle;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONArray;
import org.json.JSONObject;

public class CloneBookingTimeSlotFragment extends
                                          com.dineout.book.fragment.bookingflow.BookingTimeSlotFragment {
    private final int REQUEST_CODE_TICKET_DETAIL = 201;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        // Get Restaurant Detail from API
        getRestaurantDetails();
    }


    private void getRestaurantDetails() {
        // Check Network Connectivity
        if (AppUtil.hasNetworkConnection(getActivity())) {
            // Show Loader
            showLoader();

            String restaurantId = getArguments().getString(AppConstant.BUNDLE_RESTAURANT_ID, "");
            String dealId = getArguments().getString(AppConstant.BUNDLE_DEAL_ID, "");

            // Take API Hit
            getNetworkManager().jsonRequestGetNode(REQUEST_CODE_TICKET_DETAIL,
                    AppConstant.NODE_TICKET_DETAIL_URL,
                    ApiParams.getTicketDetailParams(restaurantId, dealId),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
                            // Hide Loader
                            hideLoader();

                            if (getView() == null || getActivity() == null) {
                                return;
                            }

                            if (request.getIdentifier() == REQUEST_CODE_TICKET_DETAIL) {
                                // Process Response
                                if (responseObject != null && responseObject.optBoolean("status")) {
                                    // Get Data
                                    JSONObject dataJsonObject = responseObject.optJSONObject("data");

                                    if (dataJsonObject != null) {
                                        // Get Stream
                                        JSONArray streamJsonArray = dataJsonObject.optJSONArray("stream");

                                        if (streamJsonArray != null && streamJsonArray.length() > 0) {
                                            // Get First Object
                                            JSONObject itemJsonObject = streamJsonArray.optJSONObject(0);

                                            if (itemJsonObject != null) {
                                                JSONObject restDetailJsonObject = itemJsonObject.optJSONObject("restaurantData");

                                                // Check for NULL
                                                if (restDetailJsonObject != null) {
                                                    // Proceed with Booking Slot
                                                    proceedWithTimeSlot(setRestaurantDetailsInBundle(restDetailJsonObject));

                                                    JSONArray reservationSectionDataJsonArray = restDetailJsonObject.optJSONArray("reservationSectionData");
                                                    putTicketGroupsIntoArguments(reservationSectionDataJsonArray);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(Request request, VolleyError error) {
                            // Hide Loader
                            hideLoader();

                            // Pop to Home
                            MasterDOFragment.popToHome(getActivity());
                        }
                    }, true);
        }
    }

    private void putTicketGroupsIntoArguments(JSONArray reservationSectionDataJsonArray) {
        if (reservationSectionDataJsonArray != null && reservationSectionDataJsonArray.length() > 0) {
            JSONObject firstItem = reservationSectionDataJsonArray.optJSONObject(0);

            JSONArray groups = firstItem.optJSONArray("ticketGroups");
            String ticketGroup = "";
            if(groups != null) {
                for(int i = 0;i < groups.length(); i++){
                    ticketGroup += groups.optInt(i);

                    if (i < groups.length() - 1) {
                        ticketGroup += "_";
                    }
                }
            }

            if (getArguments() != null) {
                getArguments().putString(AppConstant.BUNDLE_TICKET_GROUPS, ticketGroup);
            }
        }
    }

    private Bundle setRestaurantDetailsInBundle(JSONObject restDataJsonObject) {
        Bundle restaurantBundle = new Bundle();

        if (restDataJsonObject != null) {

            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                    Integer.toString(restDataJsonObject.optInt("restaurantId")));
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_NAME,
                    restDataJsonObject.optString("profileName"));
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_CITY,
                    restDataJsonObject.optString("cityName"));
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_AREA,
                    restDataJsonObject.optString("areaName"));
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_LOCALITY,
                    restDataJsonObject.optString("localityName"));

            JSONArray imagesJsonArray = restDataJsonObject.optJSONArray("profileImages");
            if (imagesJsonArray != null && imagesJsonArray.length() > 0) {
                JSONObject obj = imagesJsonArray.optJSONObject(0);
                if (obj != null) {
                    String imageUrl = obj.optString("imageUrl");
                    if (!com.dineout.recycleradapters.util.AppUtil.isStringEmpty(imageUrl)) {
                        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_IMAGE_URL, imageUrl);
                    }
                }
            }

            JSONArray cuisineJsonArray = restDataJsonObject.optJSONArray("cuisine");
            if (cuisineJsonArray != null && cuisineJsonArray.length() > 0) {
                String cuisineName = cuisineJsonArray.optString(0, "");
                if (!com.dineout.recycleradapters.util.AppUtil.isStringEmpty(cuisineName)) {
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, cuisineName);
                }

                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, cuisineJsonArray.toString());
            }

            trackArrayAdTechEvent("dCuisine", cuisineJsonArray);
            trackArrayAdTechEvent("dFType", restDataJsonObject.optJSONArray("tags"));

            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, restDataJsonObject.optJSONArray("tags") != null ?
                    restDataJsonObject.optString("tags").toString() : "[]");

            String deepLink = DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostRestoDetail +
                    "?q=" + restDataJsonObject.optString("restaurantId");
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, deepLink);

            restaurantBundle.putDouble("COSTFORTWO", restDataJsonObject.optDouble("costForTwo"));

            JSONArray extraInfoJsonArray = restDataJsonObject.optJSONArray("extraInfo");
            if (extraInfoJsonArray != null && extraInfoJsonArray.length() > 0) {
                restaurantBundle.putString(AppConstant.BUNDLE_EXTRA_INFO, extraInfoJsonArray.toString()); // Extra Info
            }

            // Get Deal JSON
            JSONArray dealJsonArray = restDataJsonObject.optJSONArray("reservationSectionData");

            if (dealJsonArray != null && dealJsonArray.length() > 0) {
                // Get Deal JSON
                JSONObject dealJsonObject = dealJsonArray.optJSONObject(0);

                if (dealJsonObject != null) {
                    restaurantBundle.putString(AppConstant.BUNDLE_OFFER_ID, dealJsonObject.optString("id", ""));
                    restaurantBundle.putString(AppConstant.BUNDLE_OFFER_TYPE, dealJsonObject.optString("type", ""));
                    restaurantBundle.putLong(AppConstant.BUNDLE_OFFER_START_FROM, dealJsonObject.optLong("startFrom", 0L));
                    restaurantBundle.putString(AppConstant.BUNDLE_OFFER_JSON, dealJsonObject.toString());

                    restaurantBundle.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_ID, dealJsonObject.optString("id", ""));
                    restaurantBundle.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_TYPE, dealJsonObject.optString("type", ""));
                    restaurantBundle.putLong(AppConstant.BUNDLE_PREVIOUS_OFFER_START_FROM, dealJsonObject.optLong("startFrom", 0L));
                    restaurantBundle.putString(AppConstant.BUNDLE_PREVIOUS_OFFER_JSON, dealJsonObject.toString());
                }
            }

            restaurantBundle.putInt(AppConstant.BUNDLE_USER_EARNINGS, restDataJsonObject.optInt("dinerCredits", 0));
        }

        return restaurantBundle;
    }

//    private class LoginCallback implements UserAuthenticationController.LoginFlowCompleteCallbacks {
//        @Override
//        public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
//            // Set Title
//            setToolbarTitle("");
//
//            // Get Restaurant Detail from API
//            getRestaurantDetails();
//        }
//
//        @Override
//        public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
//            // Set Title
//            setToolbarTitle("");
//        }
//    }
}
