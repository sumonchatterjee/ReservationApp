package com.dineout.book.fragment.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.interfaces.CallPhonePermissionGrantedListener;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.activity.MasterDOLauncherActivity;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.login.LoginFlowBaseFragment;
import com.dineout.book.fragment.maps.MapFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.myaccount.ReferEarnFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.dialogs.GenericListDialog;
import com.dineout.book.dialogs.PhoneDialog;
import com.dineout.recycleradapters.HorizontalListItemRecyclerAdapter;
import com.dineout.recycleradapters.RestaurantDetailInfoPageAdapter;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.viewmodel.MenuThumbnailHandler;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RestaurantDetailInfoPageFragment extends AuthenticationWrapperJSONReqFragment
        implements RestaurantDetailInfoPageAdapter.InfoTabClickListener,
        HorizontalListItemRecyclerAdapter.IHorizontalItemClickListener, MenuThumbnailHandler,
        CallPhonePermissionGrantedListener {

    private final int REQUEST_CODE_RESTAURANT_DETAIL_INFO = 101;
    private final int REQUEST_CODE_FAV_UNFAV = 102;
    String trackScreenDetail = "";
    private String phoneNumber;

    private RecyclerView recyclerViewRestDetailInfo;
    private boolean favAction;
    private String restaurantId;
    private Bundle restaurantBundle;
    private JSONObject restDetailJsonObject;
    private String restaurantCategory;
    private String restaurantType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail_info_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get Bundle
        restaurantBundle = getArguments();

        // Set Restaurant Id
        if (restaurantBundle != null) {
            restaurantId = restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, "");
            restaurantCategory = restaurantBundle.getString("RESTAURANT_CATEGORY");
            restaurantType = restaurantBundle.getString("RESTAURANT_TYPE");
        }

        RestaurantDetailInfoPageAdapter pageAdapter = new RestaurantDetailInfoPageAdapter(getActivity(), this);

        // Get Recycler View
        recyclerViewRestDetailInfo = (RecyclerView) getView().findViewById(R.id.recyclerView_rest_detail_info);

        // Define Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Set Layout Manager
        recyclerViewRestDetailInfo.setLayoutManager(linearLayoutManager);

        // Set Info Click Listener
        pageAdapter.setInfoTabClickListener(this);

        // Set Image Loader
        pageAdapter.setImageLoader(getImageLoader());

        // Set Horizontal Click Listener
        pageAdapter.setOnHorizontalItemClickListener(this);

        // Set Adapter
        recyclerViewRestDetailInfo.setAdapter(pageAdapter);

        // Get Info Tab Details
        onNetworkConnectionChanged(com.dineout.book.util.AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));

        // Register Call Phone Permission Listener
        if (getActivity() instanceof MasterDOLauncherActivity) {
            ((MasterDOLauncherActivity) getActivity()).registerListenerForCallPhonePermission(this);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Get Info Tab Details
        getInfoDetailsFromAPI();
    }

    // Get Info Tab Details
    private void getInfoDetailsFromAPI() {
        // Show Loader
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_RESTAURANT_DETAIL_INFO,
                AppConstant.NODE_RESTAURANT_DETAIL_URL,
                ApiParams.getRestaurantDetailsParams(restaurantId,
                        AppConstant.REST_DETAIL_INFO_DETAILS),
                this, this, false);
    }

    // Handle Get Info API Success
    private void handleGetInfoAPISuccess(JSONObject restDetailJsonObject) {
        if (recyclerViewRestDetailInfo.getAdapter() != null) {
            RestaurantDetailInfoPageAdapter adapter = (RestaurantDetailInfoPageAdapter) recyclerViewRestDetailInfo.getAdapter();
            adapter.setDataForAdapter
                    (restDetailJsonObject.optJSONObject("layout").optJSONArray("info"), restDetailJsonObject);
            adapter.notifyDataSetChanged();
        }
    }

    private Object getParamValue(Class classType, String paramKey) {
        Object paramValue = null;

        if (restDetailJsonObject != null && !TextUtils.isEmpty(paramKey)) {

            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            if (dataJsonObject != null) {
                if (classType == String.class) {
                    paramValue = dataJsonObject.optString(paramKey);

                } else if (classType == Integer.class) {
                    paramValue = dataJsonObject.optInt(paramKey);

                } else if (classType == Boolean.class) {
                    paramValue = dataJsonObject.optBoolean(paramKey);
                }
            }
        }

        return paramValue;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        restaurantId = null;
        restaurantBundle = null;
        recyclerViewRestDetailInfo = null;
        restDetailJsonObject = null;
        phoneNumber = null;
        trackScreenDetail = null;

        // UnRegister Call Phone Permission Listener
        if (getActivity() instanceof MasterDOLauncherActivity) {
            ((MasterDOLauncherActivity) getActivity()).unregisterListenerForCallPhonePermission();
        }
    }


    /**
     * API Response Handlers
     */
    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAIL_INFO) {
            // Check Response
            if (responseObject != null && responseObject.optBoolean("status")) {
                if (responseObject.optJSONObject("data") != null &&
                        responseObject.optJSONObject("data").optJSONArray("stream") != null &&
                        responseObject.optJSONObject("data").optJSONArray("stream").length() > 0) {

                    JSONObject restDetailJsonObject = responseObject.optJSONObject("data").optJSONArray("stream").optJSONObject(0);

                    if (restDetailJsonObject != null) {
                        // Set JSON Response
                        this.restDetailJsonObject = restDetailJsonObject;

                        //track info screen
//                        int type = restDetailJsonObject.optJSONObject("data").optInt("restaurantType");
//                        trackScreenDetail = restDetailJsonObject.optJSONObject("data").optString("profileName") + "_" +
//                                restDetailJsonObject.optJSONObject("data").optString("localityName") + "_" +
//                                restDetailJsonObject.optJSONObject("data").optString("cityName") + "_" + type;

                        // Handle Get Info API Success
                        handleGetInfoAPISuccess(restDetailJsonObject);
                    }
                }
            }
        } else if (request.getIdentifier() == REQUEST_CODE_FAV_UNFAV) {
            // Check Response
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    if (favAction) {
                        UiUtil.showSnackbar(recyclerViewRestDetailInfo, getString(R.string.text_favourite_marked_success), 0);
                    } else {
                        UiUtil.showSnackbar(recyclerViewRestDetailInfo, getString(R.string.text_favourite_remove_success), 0);
                    }

                    // Refresh Data
                    onNetworkConnectionChanged(com.dineout.book.util.AppUtil.hasNetworkConnection(getActivity()));

                } else if (responseObject.optJSONObject("res_auth") != null &&
                        !responseObject.optJSONObject("res_auth").optBoolean("status")) {

                    askUserToLogin(favAction);
                }
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        hideLoader();

        if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAIL_INFO) {
            super.onErrorResponse(request, error);

        } else if (request.getIdentifier() == REQUEST_CODE_FAV_UNFAV) {
            // Show Error Message
            UiUtil.showSnackbar(recyclerViewRestDetailInfo, getString(R.string.text_general_error_message), 0);
        }
    }


    /**
     * Click Listeners
     */
    @Override
    public void onViewMenuClick(ArrayList<String> imageUrlList) {
        // Check for NULL
        if (imageUrlList != null && imageUrlList.size() > 0) {

            // Track Event
//            trackEventGA(getString(R.string.ga_screen_info),
//                    getString(R.string.ga_action_view_menu), trackScreenDetail);

            Bundle bundle = new Bundle();
            bundle.putStringArrayList("menus", imageUrlList);
            bundle.putBoolean("isMenu", true);
            bundle.putInt("position", 0);
            bundle.putString("restaurantCategory",restaurantCategory);
            bundle.putString("restaurantType",restaurantType);


            ImagePagerFragment imageFragment = new ImagePagerFragment();
            imageFragment.setArguments(bundle);

            addToBackStack(getParentFragment().getFragmentManager(), imageFragment);
        }
    }

    @Override
    public void onOpeningHourClick(ArrayList<String> timingList) {
        // Check for NULL
        if (timingList != null && timingList.size() > 0) {
            // Track Event
//            trackEventGA(getString(R.string.ga_screen_info),
//                    getString(R.string.ga_action_opening_hours), trackScreenDetail);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("restID",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
            }
            trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,"RestaurantViewAllOpeningHoursClick",
                    restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+ restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID),hMap);


            GenericListDialog genericListDialog = new GenericListDialog(timingList, getActivity().getApplicationContext());
            genericListDialog.show(getFragmentManager(), "timingsDialog");
        }
    }

    @Override
    public void onMapClick(String lat, String lng) {
        // Track Event
//        trackEventGA(getString(R.string.ga_screen_info),
//                getString(R.string.ga_action_maps), trackScreenDetail);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,"RestaurantAddressMapClick",
                (String) getParamValue(String.class, "profileName")+"_"+(String) getParamValue(String.class, "restaurantId"),hMap);


        // Process Distance
        String distance = (String) getParamValue(String.class, "distance");
        distance = ((com.dineout.book.util.AppUtil.isStringEmpty(distance)) ? "" :
                com.dineout.book.util.AppUtil.formatFloatDigits(Float.valueOf(distance), 2, 2));

        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_DISTANCE, distance);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_NAME, (String) getParamValue(String.class, "profileName"));
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ADDRESS, (String) getParamValue(String.class, "address"));
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
        //bundle.putInt(AppConstant.BUNDLE_ACCEPT_PAYMENT, (((boolean) getParamValue(Boolean.class, "isSmartPay")) ? 1 : 0));
        bundle.putString(AppConstant.BUNDLE_RECENCY, (((boolean) getParamValue(Boolean.class, "recency")) ? "1" : "0"));
        bundle.putString(AppConstant.BUNDLE_RATING, (String) getParamValue(String.class, "avgRating"));
        bundle.putString(AppConstant.BUNDLE_DESTINATION_LATITUDE, (String) getParamValue(String.class, "lat"));
        bundle.putString(AppConstant.BUNDLE_DESTINATION_LONGITUDE, (String) getParamValue(String.class, "lon"));

        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        addToBackStack(getParentFragment().getFragmentManager(), mapFragment);
    }

    @Override
    public void onPhoneClick(ArrayList<String> phoneList) {
        // Check for NULL
        if (phoneList != null && phoneList.size() > 0) {
            // Track Event
//            trackEventGA(getString(R.string.ga_screen_info),
//                    getString(R.string.ga_action_called), trackScreenDetail);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("resId",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
            }
            trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,"RestaurantCallDineout","RestaurantCallDineout",hMap);


            //JSONObject dataRest = restDetailJsonObject.optJSONObject("data");
//            if (dataRest != null) {
//                HashMap<String, Object> props = new HashMap<String, Object>();
//                props.put("RestaurantID", dataRest.optString("restaurantId"));
//                props.put("RestaurantType", dataRest.optInt("restaurantType") == 0 ?
//                        "Discovery" : dataRest.optInt("restaurantType") == 1 ? "PF" : "FF");
//
//                props.put("ResCuisine", dataRest.optJSONArray("cuisine") != null ?
//                        dataRest.optString("cuisine").toString() : "[]");
//                props.put("CuisinesList", dataRest.optJSONArray("cuisine") != null ?
//                        dataRest.optString("cuisine").toString() : "[]");
//                props.put("TagsList", dataRest.optJSONArray("tags") != null ?
//                        dataRest.optString("tags").toString() : "[]");
//
//                String deepLink = DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostRestoDetail +
//                        "?q=" + dataRest.optString("restaurantId");
//                props.put("ResDeeplink", deepLink);
//                trackMixPanelEvent("RestaurantCalled", props);
//            }


            // Check for Phone Size
            if (phoneList.size() == 1) {
                handleDirectPhoneCall(phoneList.get(0));
            } else {


                PhoneDialog phoneDialog = new PhoneDialog(phoneList, getActivity());
                phoneDialog.show(getFragmentManager(), "phoneDialog");
            }
        }
    }

    private void handleDirectPhoneCall(String phoneNumber) {
        // Set Phone Number
        this.phoneNumber = phoneNumber;

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

    @Override
    public void onHorizontalListItemClicked(String linkUrl) {
        //track event
       // trackEventGA(getString(R.string.ga_screen_info), getString(R.string.ga_action_similar_cards), trackScreenDetail);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("resId",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, "RestaurantSimilarRestaurantClick",
                restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantId,hMap);



        if (!TextUtils.isEmpty(linkUrl)) {
            MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), linkUrl);

            if (frag != null && frag instanceof ReferEarnFragment) {
                if (!TextUtils.isEmpty(DOPreferences.getDinerId(
                        getActivity().getApplicationContext()))) {
                    addToBackStack(getParentFragment().getFragmentManager(), frag);
                }
            }

            if (frag != null && frag instanceof RestaurantDetailFragment) {
                addToBackStack(getParentFragment().getFragmentManager(), frag);
            }
        }
    }

    @Override
    public void onFavUnFavButtonClick(int restaurantId, boolean favAction) {
       // mSessionState = SESSION_STATE_FAV;
        this.favAction = favAction;

        // Track Event
//        trackEventGA(getString(R.string.ga_screen_info),
//                getString((favAction) ? R.string.ga_action_favourited :
//                        R.string.ga_action_unfavourited), trackScreenDetail);

         // track countly and ga
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("resId",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, getString((favAction) ? R.string.d_add_from_my_list :
                R.string.d_remove_from_my_list), restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+Integer.toString(restaurantId),hMap);


        //track qgraph
        HashMap<String, Object> props = new HashMap<>();
        props.put("restaurantName", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
        props.put("restID", restaurantId);
        props.put("area", restDetailJsonObject.optString("areaName"));
        props.put("cuisinesList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
        props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
        props.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
        trackEventQGraphApsalar("RestaurantAddToMyList", props, true, false, false);




        if (restaurantId > 0) {
            // Check if User is Logged In
            if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) { // User is NOT logged In
                // Ask User to Login
                askUserToLogin(favAction);

            } else { // User is logged in
                // Take API Hit
                markRestaurantFavUnfav(favAction);
            }
        }
    }

    @Override
    public void onBillCtaClick(int action) {
        if (action == AppConstant.CTA_PAY_NOW) {

//            if (TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) {
//
//                Bundle bundle = new Bundle();
//                UserAuthenticationController.getInstance(getActivity()).startLoginFlow(bundle,
//                        new LoginListener(LoginListener.TYPE_LOGIN_SMART_PAY_WITHOUT_RESERVATION, null));
//            }else{
//                payWithoutBooking();
//            }

            if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) { // User is NOT logged In
                UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, null);
            } else {
                payWithoutBooking();
            }

        } else if (action == AppConstant.CTA_UPLOAD_BILL) {

           // trackEventGA("Details_Info", "UploadBill", null);
            UploadBillController utility =
                    new UploadBillController(getNetworkManager(), getActivity(), restaurantId);
            utility.setPreviousScreenName(getString(R.string.ga_screen_info));
            utility.validate();
        }
    }

    private void payWithoutBooking() {

       // trackEventGA("Details_Info", "PayBill", null);

        JSONObject data = restDetailJsonObject.optJSONObject("data");
        Bundle bundle = new Bundle();
        bundle.putString(PaymentConstant.RESTAURANT_NAME,
                data.optString("profileName"));
        bundle.putString(PaymentConstant.RESTAURANT_ID,
                data.optString("restaurantId"));
        bundle.putString(PaymentConstant.BOOKING_ID,
                data.optString("restaurantId"));
        bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.RESTAURANT_TYPE);
        PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());

    }


    private void askUserToLogin(final boolean favAction) {

        // initiate login flow
        Bundle b = new Bundle();
        b.putBoolean(LoginListener.FAV_KEY, favAction);
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, new LoginListener(LoginListener.TYPE_LOGIN_FAV_CLICK, b));
    }

    private void proceedLoginSuccess(boolean favAction, JSONObject object) {
        if (getActivity() != null) {
            // Take API Hit
            markRestaurantFavUnfav(favAction);

            if (DOPreferences.isDinerNewUser(getActivity().getApplicationContext())) {
                //showRedeemDialog(null);

                // Set Is New User flag
                DOPreferences.setDinerNewUser(getActivity().getApplicationContext(), "0");
            }
        }
    }

    private void proceedLoginFailure(JSONObject loginFlowCompleteFailureObject) {
        if (getActivity() != null && loginFlowCompleteFailureObject != null) {
            String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
                return;
            }

            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
            // Show Error Message
            if (!com.dineout.book.util.AppUtil.isStringEmpty(cause)) {
                UiUtil.showToastMessage(getActivity().getApplicationContext(), cause);
            }
        }
    }

    private void markRestaurantFavUnfav(boolean favAction) {
        // Set Flag
        this.favAction = favAction;

        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_CODE_FAV_UNFAV, AppConstant.URL_SET_UNSET_FAV_REST,
                ApiParams.getSetUnsetFavoriteRestaurantParams(restaurantId,
                        DOPreferences.getDinerId(getActivity().getApplicationContext()),
                        ((favAction) ? "fav" : "unfav")),
                this, this, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        trackScreenDetail = null;
//        recyclerViewRestDetailInfo = null;
//        restaurantId = null;
//        restaurantBundle = null;
//        restDetailJsonObject = null;
//        phoneNumber = null;
    }

    public void showMenuImage(ArrayList<String> images, int position) {

//        trackEventGA(getString(R.string.ga_screen_info),
//                getString(R.string.ga_action_view_menu), trackScreenDetail);

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("menus", images);
        bundle.putBoolean("isMenu", true);
        bundle.putInt("position", position);
        bundle.putString("detail","menu_click");
        bundle.putString("restaurantCategory",restaurantCategory);
        bundle.putString("restaurantType",restaurantType);


        ImagePagerFragment imageFragment = new ImagePagerFragment();
        imageFragment.setArguments(bundle);

        addToBackStack(getParentFragment().getFragmentManager(), imageFragment);
    }

    @Override
    public void onCallPhonePermissionGrant(boolean granted) {
        if (granted && !AppUtil.isStringEmpty(phoneNumber)) {
            performCall(phoneNumber);
        }
    }

    private class LoginListener implements UserAuthenticationController.LoginFlowCompleteCallbacks {
        static final int TYPE_LOGIN_FAV_CLICK = 0;
        static final int TYPE_LOGIN_SMART_PAY_WITHOUT_RESERVATION = 1;
        static final String FAV_KEY = "fav_key";

        private int mType;
        private Bundle mBundle;

        LoginListener(int type, Bundle bundle) {
            mType = type;
            mBundle = bundle;
        }

        @Override
        public void loginFlowCompleteSuccess(JSONObject object) {
//            switch (mType) {
//                case TYPE_LOGIN_FAV_CLICK:
//                    if (mBundle != null) {
//                        boolean favAction = mBundle.getBoolean(FAV_KEY);
//                        proceedLoginSuccess(favAction, object);
//                    }
//                    break;
//
//                case TYPE_LOGIN_SMART_PAY_WITHOUT_RESERVATION:
//                    payWithoutBooking();
//                    break;
//            }
        }

        @Override
        public void loginFlowCompleteFailure(JSONObject object) {

        }
    }


}