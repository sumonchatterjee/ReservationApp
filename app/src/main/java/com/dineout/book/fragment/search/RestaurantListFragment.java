package com.dineout.book.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.home.NotificationFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.util.FilterUtil;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.recycleradapters.ModifiedRestaurantListAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.dineoutnetworkmodule.ApiParams.PARAM_START;
import static com.example.dineoutnetworkmodule.AppConstant.PARAM_DEEPLINK;
import static com.example.dineoutnetworkmodule.AppConstant.PARAM_SF;
import static com.example.dineoutnetworkmodule.AppConstant.PARAM_SORTBY;

public class RestaurantListFragment extends MasterDOJSONReqFragment
        implements OnClickListener, ModifiedRestaurantListAdapter.OnCardClickedListener,
        ModifiedRestaurantListAdapter.RestaurantListClickListener,
        ModifiedRestaurantListAdapter.OnOfferListener,
        SortOptionsFragment.SortOptionClickListener, FilterFragment.FilterClickListener {

    private boolean isRestaurantListGettingLoaded = false;
    private ModifiedRestaurantListAdapter mAdapter;
    private HashMap<String, Object> params = null;
    private JSONObject restaurantJsonObject;
    private JSONArray filterJsonArray;
    private JSONObject sortJsonObject;
    private String sortOptionKey;
    private int startIndex;
    private boolean isFiltered;
    private boolean isRequestInTransit = false;

    private RecyclerView mRecyclerView;
    private LinearLayout noResultsFoundLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ModifiedRestaurantListAdapter(getActivity());
        mAdapter.setRestaurantListClickListener(this);
        mAdapter.setOnCardClickedListener(this);
        mAdapter.setOnOfferClickedListener(this);
        mAdapter.setNetworkManager(getNetworkManager(), getImageLoader());
        params = null;

        // Get Restaurant List
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Set API Params
        setSRPApiParams(0);

        // Fetch SRP
        getRestaurantList(0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
        trackScreenName(getString(R.string.countly_listing));

        noResultsFoundLayout = (LinearLayout) getView().findViewById(R.id.ll_no_result_layout);

        ImageButton backButton = (ImageButton) getView().findViewById(R.id.iv_toolbar_back_icon);
        backButton.setOnClickListener(this);

        ImageButton searchButton = (ImageButton) getView().findViewById(R.id.imageButtonRestResultSearch);
        searchButton.setOnClickListener(this);

        ImageView notificationIcon = (ImageView) getView().findViewById(R.id.image_button_notification) ;
        long latestNotificationTime = DOPreferences.getLatestNotificationTime(getContext());
        long seenNotificationTime = DOPreferences.getNotificationSeenTime(getContext());
        if(latestNotificationTime > seenNotificationTime) {
            notificationIcon.setImageResource(R.drawable.new_notification);
        } else{
            notificationIcon.setImageResource(R.drawable.notification_icon);
        }
        notificationIcon.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.restaurant_list_recycler);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    trackEventForCountlyAndGA(getString(R.string.countly_listing),
                            getString(R.string.d_search_listing), Integer.toString(lastVisibleItem), hMap);
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


                        // Hide No Results Message
                        noResultsFoundLayout.setVisibility(View.GONE);

                        // Set API Params
                        setSRPApiParams(pageNumToBeFetched);

                        // Fetch SRP
                        getRestaurantList(pageNumToBeFetched);
                    }
                }
            }
        });

        // Set Header Info
        setHeaderInfo();

        // Show Filter Sort Section
        showFilterSortSection();

        if (isRequestInTransit) {
            showLoader();
        }
    }

    private void getRestaurantList(int pageNum) {
        // Check if Fragment is attached to Window Manager
        if (getActivity() == null)
            return;

        isRequestInTransit = true;

        // Show Loader
        showLoader();

        startIndex = (AppConstant.DEFAULT_PAGINATION_LIMIT * pageNum);

        // Set Start Index
        params.put(PARAM_START, Integer.toString(startIndex));

        // Take API Hit
        getNetworkManager().jsonRequestPostNode(startIndex, AppConstant.URL_NODE_SRP,
                params, this, this, true);
    }

    private void setSRPApiParams(int pageNum) {
        // Check for NULL
        if (params == null) {
            params = new HashMap<>();
        }

        Bundle arguments = getArguments();

        params = ApiParams.getSRPSearchParams(
                params,
                DOPreferences.getCityName(getContext()),
                arguments.getString(Constants.SEARCH_KEYWORD),
                (AppConstant.DEFAULT_PAGINATION_LIMIT * pageNum),
                arguments.getString(PARAM_DEEPLINK),
                arguments.getString(PARAM_SF),
                DOPreferences.getLocationSf(getContext()));
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null) {
            return;
        }

        isRequestInTransit = false;

        if (responseObject != null && responseObject.optBoolean("status")) {
            isRestaurantListGettingLoaded = false;
            JSONObject dataJsonObject = responseObject.optJSONObject("data");

            if (dataJsonObject != null) {
                JSONObject restaurantJsonObject = dataJsonObject.optJSONObject("RESTAURANT");

                if (restaurantJsonObject != null) {
                    // Set JSON Object
                    this.restaurantJsonObject = restaurantJsonObject;

                    // Get Num Found
                    int totalRecords = restaurantJsonObject.optInt("matches", 0);

                    // Setup UI on First Page
                    setupUIOnFirstPage();

                    // Check number of Records
                    if (totalRecords == 0) {
                        // Show No Results
                        showNoResults();

                    } else {
                        // Hide View
                        noResultsFoundLayout.setVisibility(View.GONE);

                        JSONArray listing = restaurantJsonObject.optJSONArray("listing");

                        if (listing != null && listing.length() > 0 && listing.length() < 10) {
                            isRestaurantListGettingLoaded = true;
                        }

                        if (mAdapter != null) {
                            mAdapter.setData(listing, request.getIdentifier(), restaurantJsonObject.optJSONObject("banner_data"));
                        }
                    }
                }
            }
        } else {
            // Show No Results
            showNoResults();
        }
    }

    private void setupUIOnFirstPage() {
        // Check for First Page
        if (startIndex == 0) {
            // Set Header Info
            setHeaderInfo();

            // Set Filter Data
            filterJsonArray = getArrayFromJSONResponse("filters");

            // Set Sort Data
            sortJsonObject = getObjectFromJSONResponse("sort_filters");

            // Set Filtered
            isFiltered = getBooleanValueFromJSONResponse("is_filtered");

            // Show Filter Sort Section
            showFilterSortSection();

            // Remove Deeplink from Bundle
            handleRemovingDeeplink();

            // Remove sf from Bundle
            handleRemovingSf();

            // Again Set Applied Filters in Params
            setAppliedFiltersInParams();

            // Again Set Applied Sorting in Params
            setAppliedSortingInParams();
        }
    }

    private void setMainTitle(String title) {
        // Set Main Title
        if (AppUtil.isStringEmpty(title)) {
            title = getString(R.string.text_search_results);
        }

        TextView textViewMainTitle = (TextView) getView().findViewById(R.id.textView_main_title);
        textViewMainTitle.setText(title);
    }

    private void setHeaderInfo() {
        // Check for NULL
        if (restaurantJsonObject != null) {
            // Set Main Title
            setMainTitle(restaurantJsonObject.optString("title", ""));

            // Set Sub Title
            setSubTitle(restaurantJsonObject.optInt("matches", 0));
        }
    }

    private void setSubTitle(int totalRecords) {
        // Set Sub Title
        TextView subTitleTextView = (TextView) getView().findViewById(R.id.textView_sub_title);
        subTitleTextView.setText(totalRecords + " " +
                ((totalRecords <= 1) ? getString(R.string.text_result) : getString(R.string.text_results)));

        subTitleTextView.setVisibility(View.VISIBLE);
    }

    private void showFilterSortSection() {
        boolean isFilterDataAvailable = false;

        // Get Filter Data
        View filterSectionView = getView().findViewById(R.id.relativeLayout_filter_section);
        if (filterJsonArray != null && filterJsonArray.length() > 0) {
            isFilterDataAvailable = true;
            filterSectionView.setVisibility(View.VISIBLE);
            filterSectionView.setOnClickListener(this);
            filterSectionView.setTag(filterJsonArray);

            // Set Indicator
            filterSectionView.findViewById(R.id.view_filter_indicator).
                    setVisibility((isFiltered) ? View.VISIBLE : View.GONE);

        } else {
            filterSectionView.setVisibility(View.GONE);
        }

        // Get Sort Data
        View sortSectionView = getView().findViewById(R.id.relativeLayout_sort_section);
        if (sortJsonObject != null) {
            // Check for Sort Options
            JSONArray sortOptionJsonArray = sortJsonObject.optJSONArray("arr");

            if (sortOptionJsonArray != null && sortOptionJsonArray.length() > 0) {
                sortSectionView.setVisibility(View.VISIBLE);
                sortSectionView.setOnClickListener(this);
                sortSectionView.setTag(sortJsonObject);

                // Check if Filter Data is available
                if (isFilterDataAvailable) {
                    getView().findViewById(R.id.view_filer_sort_divider).setVisibility(View.VISIBLE);
                }

                // Set Sort By Option Name
                setAppliedSortName(sortOptionJsonArray);

            } else {
                sortSectionView.setVisibility(View.GONE);
                getView().findViewById(R.id.view_filer_sort_divider).setVisibility(View.GONE);
            }
        } else {
            sortSectionView.setVisibility(View.GONE);
            getView().findViewById(R.id.view_filer_sort_divider).setVisibility(View.GONE);
        }
    }

    private void setAppliedSortName(JSONArray sortOptionJsonArray) {
        // Check for NULL / Empty
        if (sortOptionJsonArray != null && sortOptionJsonArray.length() > 0) {
            TextView textViewSortOptionName = (TextView) getView().findViewById(R.id.textView_sort_option_name);

            int sortOptionSize = sortOptionJsonArray.length();
            for (int index = 0; index < sortOptionSize; index++) {
                // Get Sort Option
                JSONObject sortOptionJsonObject = sortOptionJsonArray.optJSONObject(index);

                if (sortOptionJsonObject != null) {
                    if (sortOptionJsonObject.optInt("applied", 0) == 0) {
                        textViewSortOptionName.setVisibility(View.GONE);

                    } else {
                        String sortOptionName = sortOptionJsonObject.optString("name", "");
                        if (AppUtil.isStringEmpty(sortOptionName)) {
                            textViewSortOptionName.setVisibility(View.GONE);
                        } else {
                            textViewSortOptionName.setText(sortOptionName);
                            textViewSortOptionName.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void handleRemovingDeeplink() {
        // Check for NULL
        if (getArguments() != null) {
            // Remove Deeplink from bundle
            getArguments().putString(PARAM_DEEPLINK, "");

            // Remove Deeplink from Params
            params.remove(PARAM_DEEPLINK);
        }
    }

    private void handleRemovingSf() {
        // Check for NULL
        if (getArguments() != null) {
            // Remove Sf from bundle
            getArguments().putString(PARAM_SF, "");

            // Remove Sf from Params
            params.remove(PARAM_SF);
        }
    }

    private void setAppliedFiltersInParams() {
        // Get Applied Filters
        HashMap<String, Object> appliedFilters =
                FilterUtil.getAppliedFilters(getArrayFromJSONResponse("filters"), null);

        // Add Applied Filters to Params
        if (appliedFilters != null && params != null) {
            params.putAll(appliedFilters);
        }

        //track events
        trackAdTechGAEvents(appliedFilters);

    }


    //track events for adtech , countly, ga
    private void trackAdTechGAEvents(HashMap<String, Object> appliedFilters) {

        //track adtech event
        if (appliedFilters != null && params != null) {

            String alphaOnly = "";
            String filterLoc = "";
            String filteredCuisine = "";

            if (appliedFilters.get("tag") != null) {
                String tag = appliedFilters.get("tag").toString();
                if (!TextUtils.isEmpty(tag)) {
                    String[] rest_tags = tag.split(",");
                    StringBuilder sb = new StringBuilder();
                    if (rest_tags.length > 0) {
                        for (int i=0; i< rest_tags.length ; i++) {
                            String str=rest_tags[i];
                            str=str.replaceAll("[^a-zA-Z]+", "");
                            sb.append(str);
                            if(i!=rest_tags.length-1) {
                                sb.append(",");
                            }
                        }
                    }

                    alphaOnly=sb.toString();
                }
            }

            if (appliedFilters.get("locality_area") != null) {
                String rest_city = "", rest_locality = "", rest_area = "", rest_cuisine = "";
                if (params.containsKey("city_name")) {
                    rest_city = (String) params.get("city_name");
                }

                String loc = appliedFilters.get("locality_area").toString();
                if (!TextUtils.isEmpty(loc)) {
                    String[] rest_loc = loc.split(",");
                    if (rest_loc.length > 0) {
                        rest_area = rest_loc[0];
                        rest_locality = rest_loc[1];

                        if (!TextUtils.isEmpty(rest_area)) {
                            rest_area = rest_area.replaceAll("[^a-zA-Z0-9]+", "");
                        }

                        if (!TextUtils.isEmpty(rest_locality)) {
                            rest_locality = rest_locality.replaceAll("[^a-zA-Z0-9]+", "");
                        }

                        filterLoc = rest_city + ":" + rest_locality + ":" + rest_area;
                    }
                }
            }

                if (appliedFilters.get("cuisine") != null) {
                    String cusineFil = appliedFilters.get("cuisine").toString();


                    if (!TextUtils.isEmpty(cusineFil)) {
                        String[] rest_cuisine = cusineFil.split(",");
                        StringBuilder sb = new StringBuilder();
                        if (rest_cuisine.length > 0) {
                            for (int i=0; i< rest_cuisine.length ; i++) {
                                String str=rest_cuisine[i];
                                str=str.replaceAll("[^a-zA-Z]+", "");
                                sb.append(str);
                                if(i!=rest_cuisine.length-1) {
                                    sb.append(",");
                                }
                            }
                        }

                        filteredCuisine=sb.toString();
                    }


                }

                if (appliedFilters.containsKey("tag") &&
                        appliedFilters.containsKey("locality_area") &&
                        appliedFilters.containsKey("cuisine")) {

                    //track adtech events
                    trackAdTechEvent("int", "dFType:" + alphaOnly);
                    trackAdTechEvent("int", "dLoc:" + filterLoc);
                    trackAdTechEvent("int", "dCuisine:" + filteredCuisine);

                    //track countly and GA
                    HashMap<String, String> hMap =
                            DOPreferences.getGeneralEventParameters(getContext());

                    if (appliedFilters.containsKey("tag")) {
                        trackEventForCountlyAndGA("D_Listing", "ListingView", alphaOnly, hMap);
                    }

                    if (appliedFilters.containsKey("cuisine")) {
                        trackEventForCountlyAndGA("D_Listing", "ListingView", filteredCuisine, hMap);
                    }

                    if (appliedFilters.containsKey("locality_area") ||
                            params.containsKey("arr_area") || params.containsKey("city_name")) {
                        trackEventForCountlyAndGA("D_Listing", "ListingView", "NONE", hMap);
                    }

                } else if (appliedFilters.containsKey("tag")) {

                    trackAdTechEvent("int", "dFType:" + alphaOnly);

                    //loc also need to be tracked
                    if (params.containsKey("city_name") && params.containsKey("arr_area")) {
                        trackAdTechEvent("int",
                                "dLoc:" + params.get("city_name") + ":" + params.get("arr_area"));
                    } else if (params.containsKey("city_name") &&
                            params.containsKey("arr_locarea")) {

                        String localityArea = params.get("arr_locarea").toString();
                        if (!TextUtils.isEmpty(localityArea)) {
                            if (localityArea.contains(",")) {
                                List<String> areaList = Arrays.asList(localityArea.split(","));
                                if (areaList != null && areaList.size() > 0) {
                                    String arrea = areaList.get(0);
                                    String locality = areaList.get(1);

                                    if (!TextUtils.isEmpty(arrea) && !TextUtils.isEmpty(locality)) {
                                        trackAdTechEvent("int",
                                                "dLoc:" + params.get("city_name") + ":" + locality +
                                                        ":" + arrea);
                                    }
                                }
                            }
                        }

                    } else if (params.containsKey("city_name")) {
                        trackAdTechEvent("int", "dLoc:" + params.get("city_name"));
                    } else if (params.containsKey("arr_area")) {
                        trackAdTechEvent("int", "dLoc:" + params.get("arr_area"));
                    }

                } else if (appliedFilters.containsKey("locality_area")) {
                    trackAdTechEvent("int", "dLoc:" + filterLoc);

                } else if (appliedFilters.containsKey("cuisine")) {
                    trackAdTechEvent("int", "dCuisine:" + filteredCuisine);
                }

            }

    }

    private void setAppliedSortingInParams() {
        // Get Applied Sort
        JSONObject obj=(getObjectFromJSONResponse("sort_filters"));

        // Add Applied Filters to Params
        if (obj != null ){
            JSONArray sortArry = obj.optJSONArray("arr");
            if(sortArry!=null){
                if(sortArry.length()>0){
                    for(int i=0;i<sortArry.length();i++){
                        JSONObject sortOptionJsonObject = sortArry.optJSONObject(i);
                        if(sortOptionJsonObject!=null){
                            int isApplied= sortOptionJsonObject.optInt("applied", 0) ;
                             if(isApplied == 1){
                                 setSortOptionInParams(sortOptionJsonObject.optString("key"));
                             }
                        }
                    }
                }
            }
        }
    }

    private String getStringValueFromJSONResponse(String key) {
        if (!AppUtil.isStringEmpty(key) && restaurantJsonObject != null) {
            return restaurantJsonObject.optString(key, "");
        }

        return "";
    }

    private boolean getBooleanValueFromJSONResponse(String key) {
        if (!AppUtil.isStringEmpty(key) && restaurantJsonObject != null) {
            return restaurantJsonObject.optBoolean(key, false);
        }

        return false;
    }

    private JSONObject getObjectFromJSONResponse(String key) {
        if (!AppUtil.isStringEmpty(key) && restaurantJsonObject != null) {
            return restaurantJsonObject.optJSONObject(key);
        }

        return null;
    }

    private JSONArray getArrayFromJSONResponse(String key) {
        if (!AppUtil.isStringEmpty(key) && restaurantJsonObject != null) {
            return restaurantJsonObject.optJSONArray(key);
        }

        return null;
    }

    private void showNoResults() {
        // Remove Views
        mAdapter.setJsonArray(null);
        if (mRecyclerView != null) {
            mRecyclerView.removeAllViews();
        }

        // Show No Results Message
        if (noResultsFoundLayout != null) {
            noResultsFoundLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        // Show No Results
        if (getView() != null) {
            showNoResults();
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.imageButtonRestResultSearch) {
            // Track Search Click
            // trackEventGA(getString(R.string.ga_screen_listing), getString(R.string.ga_screen_search), null);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_search_icon_click), getString(R.string.d_search_icon_click), hMap);

            addToBackStack(getFragmentManager(), new SearchFragment());

        } else if (viewId == R.id.iv_toolbar_back_icon) {

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_back_button_click), getString(R.string.d_back_button_click), hMap);


            popBackStack(getFragmentManager());

        } else if (viewId == R.id.relativeLayout_filter_section) {

            //track filter click
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_filters_click), getString(R.string.d_filters_click), hMap);


            // Calling Filter Screen
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.setFilterJsonArray((JSONArray) view.getTag());
            filterFragment.setFilterClickListener(this);
            filterFragment.setBundleSearchKeyword((getArguments() == null) ?
                    "" : getArguments().getString(Constants.SEARCH_KEYWORD));

            addToBackStack(getFragmentManager(), filterFragment);

        } else if (viewId == R.id.relativeLayout_sort_section) {
            // Track Sort Click
//            trackEventGA(getString(R.string.ga_screen_listing),
//                    getString(R.string.ga_action_sort), null);


            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_sort_click), getString(R.string.d_sort_click), hMap);


            // Show Sort Dialog
            SortOptionsFragment sortOptionsFragment = new SortOptionsFragment();
            sortOptionsFragment.setSortJsonObject((JSONObject) view.getTag());
            sortOptionsFragment.setSortOptionClickListener(this);
            showFragment(getActivity().getSupportFragmentManager(), sortOptionsFragment);

        } else if(viewId == R.id.image_button_notification){

            //track event
            HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("D_Notification","NotificationIconClick","D_Listing",hMap);

            //open notification
            NotificationFragment notificationFragment = new NotificationFragment();
            addToBackStack(getActivity(), notificationFragment);
        }
    }

    @Override
    public boolean onPopBackStack() {
        // Track Back Click
//        trackEventGA(getString(R.string.ga_screen_listing),
//                getString(R.string.ga_action_back), null);

        return super.onPopBackStack();
    }

    @Override
    public void onCardClicked(JSONObject jsonObject) {
        if (jsonObject != null) {
            // Track Restaurant Card Click
//            trackEventGA(getString(R.string.ga_screen_listing),
//                    getString(R.string.ga_action_restaurant_card),
//                    jsonObject.optString("r_id"));

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if (hMap != null) {
                hMap.put("restID", jsonObject.optString("r_id"));
                hMap.put("restaurantName", jsonObject.optString("profile_name"));
                hMap.put("poc", Integer.toString(jsonObject.optInt("position")));

            }
            trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_listing_image),
                    jsonObject.optString("profile_name")+"_"+jsonObject.optString("r_id"), hMap);


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
    public void onReserveButtonClicked(JSONObject obj) {
        String resId = obj.optString("r_id");

        try {
//            trackScreenToGA(getString(R.string.ga_screen_restaurant_detail));
//            trackEventGA("D_Listing", "RestaurantReservationCTAClick", obj.optString("profile_name")+"_"+resId);

            // Track Event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if (hMap != null) {
                hMap.put("restID", obj.optString("r_id"));
                hMap.put("CTA", "reserve");
                hMap.put("restaurantName", obj.optString("profile_name"));
                hMap.put("poc", Integer.toString(obj.optInt("position")));
            }
            trackEventForCountlyAndGA(getString(R.string.countly_listing), "RestaurantReservationCTAClick",
                    obj.optString("profile_name")+"_"+obj.optString("r_id"), hMap);

            trackAdTechEvent("ua", "dLoc:" + obj.optString("locality_name") + "," + obj.optString("area_name"));
            //completeAdTechSesion();
        } catch (Exception e) {
            // Exception
        }

        Bundle restaurantBundle = new Bundle();
        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, resId); // Restaurant Id

        RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
        restaurantDetailFragment.setArguments(restaurantBundle);

        addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
    }

    @Override
    public void onUploadBillClicked(JSONObject obj) {
        try {
            // trackEventGA("D_Listing", "RestaurantUploadBillCTAClick", resName+"_"+resId);

            // Track Event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if (hMap != null) {
                hMap.put("restID", obj.optString("r_id"));
                hMap.put("cta", "uploadbill");
                hMap.put("poc", Integer.toString(obj.optInt("position")));
            }

            trackEventForCountlyAndGA(getString(R.string.countly_listing), "RestaurantUploadBillCTAClick",
                    obj.optString("profile_name")+"_"+obj.optString("r_id"), hMap);
        } catch (Exception e) {
            // Exception
        }

        // resId
        String resId = (obj == null) ? "" : obj.optString("r_id");
        String resName = (obj == null) ? "" : obj.optString("profile_name");

        UploadBillController utility = new UploadBillController(getNetworkManager(), getActivity(), resId);
        utility.setPreviousScreenName(getString(R.string.ga_screen_listing));
        utility.validate();
    }

    @Override
    public void onPayBill(JSONObject obj) {
        try {
            // Track Event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if (hMap != null) {
                hMap.put("restID", obj.optString("r_id"));
                hMap.put("cta", "paybill");

            }

            trackEventForCountlyAndGA(getString(R.string.countly_listing), "RestaurantPaymentCTAClick",
                    obj.optString("profile_name")+"_"+obj.optString("r_id"), hMap);
        } catch (Exception e) {
            // Exception
        }


        // resId
        String resId = (obj == null) ? "" : obj.optString("r_id");
        String resName = (obj == null) ? "" : obj.optString("profile_name");

        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) { // User is NOT logged In
            UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, null);
        } else {
            handlePayBill(obj);
        }
    }

    private void handlePayBill(JSONObject obj) {

        if (obj != null) {
            Bundle bundle = new Bundle();
            bundle.putString(PaymentConstant.RESTAURANT_NAME, obj.optString("profile_name"));
            bundle.putString(PaymentConstant.RESTAURANT_ID, obj.optString("r_id"));
            bundle.putString(PaymentConstant.BOOKING_ID, obj.optString("r_id"));
            bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.RESTAURANT_TYPE);

            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_pay_now_upcoming_card), null);

            PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());

        } else {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), getString(R.string.paid_restaurant));
        }
    }

    @Override
    public void onSmartpayClick(JSONObject obj) {
        // Track Event
        //  trackEventGA(getString(R.string.ga_screen_listing), getString(R.string.ga_action_smart_pay_card), null);

        // Track Event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if (hMap != null) {
            hMap.put("restID", obj.optString("r_id"));
            hMap.put("poc", Integer.toString(obj.optInt("position")));
        }

        trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_smartpay_click),
                obj.optString("profile_name")+"_"+obj.optString("r_id"), hMap);

        //showSmartpayDialog();

        String deeplink = obj.optString("deepLink", "");

        if(!TextUtils.isEmpty(deeplink)) {
            MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
            if (masterDOFragment != null) {
                addToBackStack(getFragmentManager(), masterDOFragment);
            }
        }
    }


    @Override
    public void onOfferSectionClick(JSONObject jsonObject) {
        if (jsonObject != null) {

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if (hMap != null) {
                hMap.put("restID", jsonObject.optString("r_id"));
                hMap.put("poc", Integer.toString(jsonObject.optInt("position")));

            }
            trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_restaurant_offers_click),
                    jsonObject.optString("profile_name")+"_"+jsonObject.optString("r_id"), hMap);


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
    public void onBannerClick(String linkUrl) {
        // Track Event
       // trackEventGA(getString(R.string.ga_screen_listing), getString(R.string.ga_action_banner_girf), "GIRF_" + linkUrl);

        // Check for Empty
        if (!AppUtil.isStringEmpty(linkUrl)) {
            MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), linkUrl);
            addToBackStack(getActivity(), frag);
        }
    }

    @Override
    public void onSortOptionClick(JSONObject sortOptionJsonObject) {
        if (sortOptionJsonObject != null) {
            // Check if already Applied Option is NOT selected
            if (sortOptionJsonObject.optInt("applied", 0) == 0) {
                // Get Sort Option Key
                String sortOptionKey = sortOptionJsonObject.optString("key", "");
                this.sortOptionKey = sortOptionKey;

                // Track Sort Selected Option Click

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_listing), getString(R.string.d_sort_click), sortOptionKey, hMap);

                // Set Sort Option
                setSortOptionInParams(sortOptionKey);

                // Fetch SRP
                getRestaurantList(0);
            }
        }
    }

    private void setSortOptionInParams(String sortOptionKey) {
        // Check for NULL
        if (!AppUtil.isStringEmpty(sortOptionKey)) {
            // Add Sort Option
            params.put(PARAM_SORTBY, sortOptionKey);
        }
    }

    @Override
    public void onResetFilterClick(HashMap<String, Object> appliedFilters) {
        // Empty Params
        if (params != null) {
            params.clear();
        }

        // Set API Params
        setSRPApiParams(0);

        // Set Sort Option
        setSortOptionInParams(sortOptionKey);

        // Add Filters
        if (appliedFilters != null) {
            params.putAll(appliedFilters);
        }

        // Fetch SRP
        getRestaurantList(0);
    }

    @Override
    public void onApplyFilterClick(HashMap<String, Object> appliedFilters) {
        // Empty Params
        if (params != null) {
            params.clear();
        }

        // Set API Params
        setSRPApiParams(0);

        // Set Sort Option
        setSortOptionInParams(sortOptionKey);

        // Add Filters
        if (appliedFilters != null) {
            params.putAll(appliedFilters);
        }

        // Fetch SRP
        getRestaurantList(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAdapter = null;
        params = null;
        mRecyclerView = null;
        noResultsFoundLayout = null;
        restaurantJsonObject = null;
        filterJsonArray = null;
        sortJsonObject = null;
        sortOptionKey = null;
    }
}
