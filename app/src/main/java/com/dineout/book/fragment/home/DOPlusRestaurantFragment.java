package com.dineout.book.fragment.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.recycleradapters.ModifiedRestaurantListAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class DOPlusRestaurantFragment extends AuthenticationWrapperJSONReqFragment
        implements ModifiedRestaurantListAdapter.OnCardClickedListener,
        ModifiedRestaurantListAdapter.RestaurantListClickListener,ModifiedRestaurantListAdapter.OnOfferListener {

    private boolean isRestaurantListGettingLoaded;
    private ModifiedRestaurantListAdapter mAdapter;
    private HashMap<String, Object> params;

    private LinearLayout linearLayoutDOPlusNoResultFound;
    private RecyclerView recyclerViewDOPlusRestaurantList;

    private View mDoNotAvailable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dineout_plus_tab, container, false);
    }

    protected void findViewById() {
        mAdapter = new ModifiedRestaurantListAdapter(getActivity());
        mAdapter.setRestaurantListClickListener(this);
        mAdapter.setOnCardClickedListener(this);
        mAdapter.setOnOfferClickedListener(this);
        mAdapter.setNetworkManager(getNetworkManager(), getImageLoader());

        recyclerViewDOPlusRestaurantList = (RecyclerView) getView().findViewById(R.id.recyclerViewDOPlusRestaurantList);
        recyclerViewDOPlusRestaurantList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewDOPlusRestaurantList.setAdapter(mAdapter);
        recyclerViewDOPlusRestaurantList.addOnScrollListener(new RestaurantListScrollListener());

        linearLayoutDOPlusNoResultFound = (LinearLayout) getView().findViewById(R.id.linearLayoutDOPlusNoResultFound);

        mDoNotAvailable = getView().findViewById(R.id.imageViewDOPlusNotAvailable);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        findViewById();

        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Hide No DOPlus Available
        mDoNotAvailable.setVisibility(View.GONE);

        // Hide No Results Message
        linearLayoutDOPlusNoResultFound.setVisibility(View.GONE);

        // Set API Params
        setSRPApiParams(0);

        // Fetch SRP
        fetchRestaurantFromSearch(0);
    }

    private void setSRPApiParams(int pageNum) {
        // Check for NULL
        if (params == null) {
            params = new HashMap<>();
        }

        params = ApiParams.getSRPSearchParams(
                params,
                DOPreferences.getCityName(getActivity()),
                "",
                (pageNum * Constants.PAGE_LIMIT),
                "",
                "[{\"key\":\"tag\",\"value\":[\"Dineout Plus\"]}]",
                DOPreferences.getLocationSf(getContext()));
    }

    private void fetchRestaurantFromSearch(int pageNum) {
        // Show Loader
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestPostNode(pageNum, AppConstant.URL_NODE_SRP,
                params, this, this, true);
    }


    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null) {
            return;
        }

        if (responseObject != null && !responseObject.optBoolean("status") && responseObject.optInt("error_code") == 101) {
            showNoDOPlusAvailable();
            return;
        }

        if (responseObject != null && responseObject.optBoolean("status")) {
            isRestaurantListGettingLoaded = false;
            JSONObject dataJsonObject = responseObject.optJSONObject("data");

            if (dataJsonObject != null) {
                JSONObject restaurantJsonObject = dataJsonObject.optJSONObject("RESTAURANT");

                if (restaurantJsonObject != null) {
                    // Get Num Found
                    int totalRecords = restaurantJsonObject.optInt("matches", 0);

                    // Check number of Records
                    if (totalRecords == 0) {
                        // Show No Results
                        showNoResults();

                    } else {
                        // Hide View
                        linearLayoutDOPlusNoResultFound.setVisibility(View.GONE);

                        JSONArray listing = restaurantJsonObject.optJSONArray("listing");

                        if (listing != null && listing.length() > 0 && listing.length() < 10) {
                            isRestaurantListGettingLoaded = true;
                        }

                        if (mAdapter != null) {
                            mAdapter.setData(listing, request.getIdentifier(),null);
                        }
                    }
                }
            }
        }
    }

    private void showNoResults() {
        // Remove Views
        mAdapter.setJsonArray(null);
        recyclerViewDOPlusRestaurantList.removeAllViews();

        // Hide No DOPlus Available Message
        mDoNotAvailable.setVisibility(View.GONE);

        // Show No Results Message
        linearLayoutDOPlusNoResultFound.setVisibility(View.VISIBLE);
    }

    private void showNoDOPlusAvailable() {
        // Remove Views
        mAdapter.setJsonArray(null);
        recyclerViewDOPlusRestaurantList.removeAllViews();

        // Hide No Results Message
        linearLayoutDOPlusNoResultFound.setVisibility(View.GONE);

        // Show No DOPlus Available Message
        mDoNotAvailable.setVisibility(View.VISIBLE);
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        // Show No Results
        showNoResults();
    }

    @Override
    public void onCardClicked(JSONObject jsonObject) {
        // Track Screen
       // trackScreenToGA(getString(R.string.ga_screen_restaurant_detail));

        // Track Event
//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackEventGA(getString(R.string.ga_screen_do_plus_member),
//                        getString(R.string.ga_action_restaurant_card), null);


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",jsonObject.optString("r_id"));
            hMap.put("poc", Integer.toString(jsonObject.optInt("position")));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_doplus), getString(R.string.d_restaurant_image_click),
                jsonObject.optString("profile_name")+"_"+ jsonObject.optString("r_id") , hMap);


        // Set Bundle
        Bundle restaurantBundle = new Bundle();
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                jsonObject.optString("r_id")); // Restaurant Id

        // Get Instance
        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
        restaurantDetailFragment.setArguments(restaurantBundle);

        addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
    }

    @Override
    public void onReserveButtonClicked(JSONObject jsonObject) {
        // Track Event
//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackEventGA(getString(R.string.ga_screen_do_plus_member),
//                        getString(R.string.ga_action_reserve_restaurant_card),
//                        jsonObject.optString("r_id"));


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",jsonObject.optString("r_id"));
            hMap.put("cta","reserve");
            hMap.put("poc",Integer.toString(jsonObject.optInt("position")));

        }
        trackEventForCountlyAndGA(getString(R.string.countly_doplus),"RestaurantReservationCTAClick",
                jsonObject.optString("profile_name")+"_"+jsonObject.optString("r_id"),hMap);

        // Get Instance
        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
        Bundle restaurantBundle = new Bundle();
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                jsonObject.optString("r_id")); // Restaurant Id
        restaurantDetailFragment.setArguments(restaurantBundle);
        addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
    }

    @Override
    public void onUploadBillClicked(JSONObject jsonObject) {
        // resId
        String resId = (jsonObject == null) ? "" : jsonObject.optString("r_id");

        //Track Upload Bill Event
        // trackEventGA(getString(R.string.ga_screen_do_plus_member), getString(R.string.ga_action_upload_bill_restaurant_card), resId);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",jsonObject.optString("r_id"));
            hMap.put("cta","uploadbill");
            hMap.put("poc",Integer.toString(jsonObject.optInt("position")));

        }
        trackEventForCountlyAndGA(getString(R.string.countly_doplus),getString(R.string.d_restaurant_click),
                jsonObject.optString("profile_name")+"_"+jsonObject.optString("r_id"),hMap);

        UploadBillController utility = new UploadBillController(getNetworkManager(), getActivity(), jsonObject.optString("r_id"));
        utility.setPreviousScreenName(getString(R.string.ga_screen_do_plus_member));
        utility.validate();
    }

    @Override
    public void onPayBill(JSONObject jsonObject) {
        // resId
        String resId = (jsonObject == null) ? "" : jsonObject.optString("r_id");

        // Track Pay Bill Click - TODO in next release
        //trackEventGA(getString(R.string.ga_screen_do_plus_member), getString(R.string.ga_action_pay_bill_restaurant_card), jsonObject.optString("r_id"));

        // Track Event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",jsonObject.optString("r_id"));
            hMap.put("cta","paybill");
            hMap.put("poc",Integer.toString(jsonObject.optInt("position")));

        }

        trackEventForCountlyAndGA(getString(R.string.countly_doplus),"RestaurantUploadBillCTAClick",
                jsonObject.optString("profile_name")+"_"+jsonObject.optString("r_id"),hMap);
    }

    @Override
    public void onSmartpayClick(JSONObject obj) {
        // Track Event
       // trackEventGA(getString(R.string.ga_screen_do_plus_member), getString(R.string.ga_action_smart_pay_card), null);

        // Track Event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("restID",obj.optString("r_id"));
            hMap.put("poc", Integer.toString(obj.optInt("position")));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_doplus),getString(R.string.d_smartpay_click),
                obj.optString("profile_name")+"_"+obj.optString("r_id"),hMap);

        //showSmartpayDialog();

        String deeplink = obj.optString("deepLink", "");

        if(!TextUtils.isEmpty(deeplink)) {
            MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
            if (masterDOFragment != null && getParentFragment()!=null) {
                addToBackStack(getParentFragment().getFragmentManager(), masterDOFragment);
            }
        }
    }

    @Override
    public void onBannerClick(String linkUrl) {

    }


    @Override
    public void onOfferSectionClick(JSONObject jsonObject) {
        // Track Screen
        trackScreenToGA(getString(R.string.ga_screen_restaurant_detail));

        // Track Event
        AnalyticsHelper.getAnalyticsHelper(getContext())
                .trackEventGA(getString(R.string.ga_screen_do_plus_member),
                        getString(R.string.ga_action_restaurant_card), null);

        // Set Bundle
        Bundle restaurantBundle = new Bundle();
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                jsonObject.optString("r_id")); // Restaurant Id

        // Get Instance
        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
        restaurantDetailFragment.setArguments(restaurantBundle);

        addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
    }

    class RestaurantListScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);


            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                // Track Page Scroll
                trackEventForCountlyAndGA(getString(R.string.countly_doplus),
                        getString(R.string.d_listing_results_click), Integer.toString(lastVisibleItem), hMap);
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!isRestaurantListGettingLoaded &&
                        (visibleItemCount + pastVisibleItems) >= totalItemCount - 3) {
                    isRestaurantListGettingLoaded = true;
                    int pageNumToBeFetched = totalItemCount / AppConstant.DEFAULT_PAGINATION_LIMIT;

                    // Track Page Scroll
//                    trackEventGA(getString(R.string.ga_screen_do_plus_member),
//                            getString(R.string.ga_action_page_scrolled),
//                            Integer.toString(AppConstant.DEFAULT_PAGINATION_LIMIT * pageNumToBeFetched));

                    // Hide No Results Message
                    linearLayoutDOPlusNoResultFound.setVisibility(View.GONE);

                    // Set API Params
                    setSRPApiParams(pageNumToBeFetched);

                    // Fetch SRP
                    fetchRestaurantFromSearch(pageNumToBeFetched);
                }
            }
        }


    }

//    @Override
//    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
//        super.loginFlowCompleteSuccess(loginFlowCompleteSuccessObject);
//        if (getParentFragment() != null) {
//            ((HomePageMasterFragment) getParentFragment()).refreshViewPager();
//        }
//    }
//
//    @Override
//    public void LoginSessionExpiredPositiveClick() {
//        if (getParentFragment() != null) {
//            ((HomePageMasterFragment) getParentFragment()).refreshViewPager();
//        }
//
//        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(new Bundle(), this);
//    }
//
//    @Override
//    public void LoginSessionExpiredNegativeClick() {
//        if (getParentFragment() != null) {
//            ((HomePageMasterFragment) getParentFragment()).refreshViewPager();
//        }
//    }

    protected void showLoader() {
        ProgressBar progressBar;

        if (getView() != null &&
                (progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader)) != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void hideLoader() {
        ProgressBar progressBar;

        if (getView() != null &&
                (progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader)) != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}

