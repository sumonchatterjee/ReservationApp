package com.dineout.book.fragment.myaccount;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.recycleradapters.RestaurantListingAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class MyListFragment extends YouPageWrapperFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
                   RestaurantListingAdapter.OnCardClickedListener,
                   RestaurantListingAdapter.RestaurantListClickListener,RestaurantListingAdapter.OnOfferListener {


    private final int REQUEST_CODE_FAVOURITE = 101;
    RestaurantListingAdapter mAdapter;

    private RecyclerView recyclerViewMyList;
    private View relativeLayoutMessage;
    private TextView textViewMessage;
    private ImageView imageViewMessage;
    private JSONArray dataJsonArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new RestaurantListingAdapter(getActivity());
        mAdapter.setRestaurantListClickListener(this);
        mAdapter.setOnCardClickedListener(this);
        mAdapter.setOnOfferClickedListener(this);
        mAdapter.setNetworkManager(getNetworkManager(), getImageLoader());

        // Register Pref change listener
        DOPreferences.getSharedPreferences(getActivity().getApplicationContext()).
                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Track Screen
        trackScreenName(getString(R.string.countly_you_listing));
        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_you_listing),getString(R.string.d_my_listview),getString(R.string.d_my_listview),hMap);


        return inflater.inflate(R.layout.fragment_my_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle(R.string.title_my_list);

        // Initialize View
        initializeView();

        recyclerViewMyList.setAdapter(mAdapter);

        // authenticate the user
        authenticateUser();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Initialize View
    private void initializeView() {
        // Instantiate Recycler View
        recyclerViewMyList = (RecyclerView) getView().findViewById(R.id.recycler_view_my_list);

        // Set Layout Manager
        recyclerViewMyList.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));


        recyclerViewMyList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = linearLayoutManager.findLastVisibleItemPosition();
                  //track event
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_you_listing),getString(R.string.d_listing_scroll),Integer.toString(visibleItemCount),hMap);

            }
        });


        // Get Message Section
        relativeLayoutMessage = getView().findViewById(R.id.relative_layout_message);

        // Get Text Message
        textViewMessage = (TextView) relativeLayoutMessage.findViewById(R.id.text_view_my_list_message);

        // Get Image Message
        imageViewMessage = (ImageView) relativeLayoutMessage.findViewById(R.id.image_view_my_list_image);

        // Get Login Button
        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
            Button buttonMyListLogin = (Button) relativeLayoutMessage.findViewById(R.id.button_my_list_login);
            buttonMyListLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleLogin();
                }
            });
            buttonMyListLogin.setVisibility(Button.VISIBLE);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);
        if (dataJsonArray == null || dataJsonArray.length() == 0) {
            initializeData();
        }
    }

    // Initialize Data
    private void initializeData() {
        // Check if User is Logged In
        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
            // Set UI
            setUI(false);

        } else {

            showLoader();

            // Hide Message Section
            relativeLayoutMessage.setVisibility(View.GONE);

            // Take API Hit
            getNetworkManager().jsonRequestGet(REQUEST_CODE_FAVOURITE, AppConstant.URL_DINER_FAV_LIST,
                    ApiParams.getDinerFavoriteRestaurantDataParams(((TextUtils.isEmpty(DOPreferences.getDinerId(getActivity()))) ?
                            "" : DOPreferences.getDinerId(getActivity())), DOPreferences.getAuthKey(getActivity())), this, this, false);
        }
    }

    // Ask User to Login
    private void handleLogin() {
        // This dialog will handle login via: Google, Email, New User, Guest User
        // Facebook Login is handled by initiateFacebookLogin() below. Hence, for Facebook Login
        // initiateFacebookLogin() will called on the click of Facebook skipTourButton on the Login Dialog

        // authenticate the user
        authenticateUser();
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null)
            return;

        if (response != null && request.getIdentifier() == REQUEST_CODE_FAVOURITE) {
            // Check Response
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    // Get Output Params
                    JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                    if (outputParamsJsonObject != null) {
                        // Get Data
                        JSONArray dataJsonArray = outputParamsJsonObject.optJSONArray("data");

                        if (dataJsonArray != null && dataJsonArray.length() > 0) {
                            this.dataJsonArray = dataJsonArray;

                            // Get List
                            mAdapter.setData(dataJsonArray, 0, outputParamsJsonObject.optString("app_img_url"));

                            // Set UI
                            setUI(true);

                        } else {
                            // Set UI
                            setUI(false);
                        }
                    } else {
                        // Set UI
                        setUI(false);
                    }
                } else {
                    // Set UI
                    setUI(false);

                }
            } else {
                // Show Message
                UiUtil.showToastMessage(getActivity(), getString(R.string.text_general_error_message));
            }
        }
    }

    // Set UI
    private void setUI(boolean hasFavoriteRestaurants) {
        // Check if User is Logged In
        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
            // Track Event
            trackEventGA(getString(R.string.ga_screen_my_list_guest), getString(R.string.ga_action_login), null);

            // Hide List
            recyclerViewMyList.setVisibility(RecyclerView.GONE);

            // Show Message
            relativeLayoutMessage.setVisibility(View.VISIBLE);

            // Set Message
            setMessage(true);

        } else {
            if (hasFavoriteRestaurants) {
                // Show List
                recyclerViewMyList.setVisibility(RecyclerView.VISIBLE);

                // Hide Message
                relativeLayoutMessage.setVisibility(View.GONE);

            } else {
                // Hide List
                recyclerViewMyList.setVisibility(RecyclerView.GONE);

                // Show Message
                relativeLayoutMessage.setVisibility(View.VISIBLE);

                // Set Message
                setMessage(false);
            }
        }
    }

    // Set Message
    private void setMessage(boolean isLoginError) {
        // Set Message
        textViewMessage.setText((isLoginError) ?
                R.string.text_login_to_view_list :
                R.string.text_create_my_list);

        // Set Image
        imageViewMessage.setImageResource((isLoginError) ?
                R.drawable.img_login_required :
                R.drawable.img_create_list);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Check if Email Id has changed
        if (key.equals(DOPreferences.FAVORITE_COUNT)) {
            // Refresh Tab
            initializeData();
        }
    }

    @Override
    public void onOfferSectionClick(JSONObject jsonObject) {
        if (jsonObject != null) {

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("restID",jsonObject.optString("r_id"));
                hMap.put("poc",Integer.toString(jsonObject.optInt("position")));

            }
            trackEventForCountlyAndGA(getString(R.string.countly_you_listing), getString(R.string.d_restaurant_offers_click),
                    jsonObject.optString("profile_name")+"_"+ jsonObject.optString("r_id"), hMap);
        }

    }

    @Override
    public void onCardClicked(JSONObject jsonObject) {
        if (jsonObject != null) {
            //trackScreenToGA("Details_Reservation");
           // trackEventGA(getString(R.string.ga_screen_my_list), "RestaurantCard", null);


            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("restID",jsonObject.optString("r_id"));
                hMap.put("poc",Integer.toString(jsonObject.optInt("position")));

            }
            trackEventForCountlyAndGA(getString(R.string.countly_you_listing), getString(R.string.d_restaurant_image_click),
                    jsonObject.optString("profile_name") , hMap);



            // Get Instance
            RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();

            // Set Bundle
            Bundle restaurantBundle = new Bundle();
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                    jsonObject.optString("r_id")); // Restaurant Id
            restaurantDetailFragment.setArguments(restaurantBundle);

            addToBackStack(getFragmentManager(), restaurantDetailFragment);
        }
    }

    @Override
    public void onReserveButtonClicked(JSONObject itemObj) {
        //trackScreenToGA("Details_Reservation");
        //trackEventGA(getString(R.string.ga_screen_my_list), getString(R.string.ga_action_reserve_restaurant_card), resId);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",itemObj.optString("r_id"));
            hMap.put("CTA","reserve");
            hMap.put("poc",Integer.toString(itemObj.optInt("position")));

        }
        trackEventForCountlyAndGA(getString(R.string.countly_you_listing),getString(R.string.d_restaurant_click),
                itemObj.optString("profile_name")+"_"+itemObj.optString("r_id"),hMap);

        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();

        Bundle restaurantBundle = new Bundle();
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, itemObj.optString("r_id")); // Restaurant Id
        restaurantDetailFragment.setArguments(restaurantBundle);

        addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
    }

    @Override
    public void onPayBill(JSONObject itemObj) {
        // Track Event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",itemObj.optString("r_id"));
            hMap.put("CTA","paybill");
            hMap.put("poc",Integer.toString(itemObj.optInt("position")));

        }

        trackEventForCountlyAndGA(getString(R.string.countly_you_listing),getString(R.string.d_restaurant_click),
                itemObj.optString("profile_name")+"_"+itemObj.optString("r_id"),hMap);

        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) { // User is NOT logged In
            UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, null);
        } else {
            handlePayBill(itemObj);
        }
    }

    @Override
    public void onSmartpayClick(JSONObject itemObj) {

        // Track Event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

        trackEventForCountlyAndGA(getString(R.string.countly_you_listing),getString(R.string.d_smartpay_click),
                itemObj.optString("profile_name")+"_"+itemObj.optString("r_id"),hMap);

        //showSmartpayDialog();

        String deeplink = itemObj.optString("deepLink", "");

        if(!TextUtils.isEmpty(deeplink)) {
            MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
            if (masterDOFragment != null) {
                addToBackStack(getFragmentManager(), masterDOFragment);
            }
        }
    }

    @Override
    public void onUploadBillClicked(JSONObject itemObj) {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",itemObj.optString("r_id"));
            hMap.put("CTA","uploadBill");
            hMap.put("poc",Integer.toString(itemObj.optInt("position")));

        }

        trackEventForCountlyAndGA(getString(R.string.countly_you_listing), getString(R.string.d_restaurant_click), itemObj.optString("profile_name")+"_"+itemObj.optString("r_id"),hMap);

        UploadBillController utility = new UploadBillController(getNetworkManager(), getActivity(), itemObj.optString("r_id"));
        utility.setPreviousScreenName(getString(R.string.ga_screen_my_list));
        utility.validate();
    }

    private void handlePayBill(JSONObject obj) {

        if (obj != null) {
            Bundle bundle = new Bundle();
            bundle.putString(PaymentConstant.RESTAURANT_NAME, obj.optString("profile_name"));
            bundle.putString(PaymentConstant.RESTAURANT_ID, obj.optString("r_id"));
            bundle.putString(PaymentConstant.BOOKING_ID, obj.optString("r_id"));
            bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.RESTAURANT_TYPE);

            PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());
        } else {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), getString(R.string.paid_restaurant));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mAdapter = null;
        recyclerViewMyList = null;
        relativeLayoutMessage = null;
        textViewMessage = null;
        imageViewMessage = null;
        dataJsonArray = null;

        // UnRegister Pref change listener
        DOPreferences.getSharedPreferences(getActivity().getApplicationContext()).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_you_listing),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}