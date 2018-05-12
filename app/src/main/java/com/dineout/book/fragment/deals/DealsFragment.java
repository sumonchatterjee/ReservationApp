package com.dineout.book.fragment.deals;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.recycleradapters.DealFilterAdapter;
import com.dineout.recycleradapters.DealListingAdapter;
import com.dineout.recycleradapters.DealSortByAdapter;
import com.dineout.recycleradapters.util.JSONUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class DealsFragment extends MasterDOJSONReqFragment implements View.OnClickListener {

    public static final int PAGE_LIMIT = 5;
    public static final int PAGE_NUM = 0;

    public String sortQuery;
    boolean isRestaurantListGettingLoaded = false;
    private DealListingAdapter listingAdapter;
    private JSONArray streamTags;
    private RecyclerView mRecyclerView;
    private String searchNeedle = "", byTicketType = "deal", format = "detailed", type = "ticket";
    private RelativeLayout filterLayout, sortLayout;
    private ImageView filterSelectedImvw, sortSelectedImvw;
    private String categoryQuery, ticketType = "deal";
    private int dinerCount;
    private JSONArray sortArray;
    private CardView topLayout;
    private RelativeLayout noDealTv;
    private Button resetFilterBtn;
    private TextView noDealsTxt;

    public static void cleanFilter() {
        DealFilterAdapter.setDinerCount(-1);
        DealFilterAdapter.setFilterQuery("");
        DealSortByAdapter.setSortQuery("");
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        categoryQuery = DealFilterAdapter.getFilterQuery();
        dinerCount = DealFilterAdapter.getDinerCount();

        sortQuery = DealSortByAdapter.getSortQuery();
        int startIndex = PAGE_LIMIT * PAGE_NUM;

        getDealListing(type, format, DOPreferences.getCityName(getContext()), PAGE_LIMIT, startIndex, byTicketType, searchNeedle, categoryQuery, dinerCount, sortQuery);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_deals_listing, container, false);
        if (!isChildFragment()) {
            view.findViewById(R.id.toolbar_fragment).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.toolbar_fragment).setVisibility(View.GONE);
        }
        trackScreenToGA(getString(R.string.ga_screen_view_deals_listing));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Title
        setToolbarTitle(getString(R.string.ga_screen_deals));

//        if (getArguments() != null) {
//            searchNeedle = getArguments().getString(DealSearchFragment.SEARCH_NEEDLE);
//            byTicketType = getArguments().getString(DealSearchFragment.BY_TICKET_TYPE, byTicketType);
//            format = getArguments().getString(DealSearchFragment.FORMAT, format);
//            type = getArguments().getString(DealSearchFragment.TYPE, type);
//        }

        listingAdapter = new DealListingAdapter(getActivity(), this);
        listingAdapter.setNetworkManager(getNetworkManager(), getImageLoader());

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view_deals);
        filterSelectedImvw = (ImageView) getView().findViewById(R.id.filter_selected);
        sortSelectedImvw = (ImageView) getView().findViewById(R.id.sorting_selected);
        topLayout = (CardView) getView().findViewById(R.id.top_layout);
        noDealTv = (RelativeLayout) getView().findViewById(R.id.no_deals);
        resetFilterBtn = (Button) getView().findViewById(R.id.reset_filter);
        noDealsTxt = (TextView) getView().findViewById(R.id.no_deals_tv);

        RecyclerView.LayoutManager mManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.setAdapter(listingAdapter);

        filterLayout = ((RelativeLayout) getView().findViewById(R.id.filter_btn_layout));
        sortLayout = ((RelativeLayout) getView().findViewById(R.id.sort_by_btn_layout));
        filterLayout.setOnClickListener(this);
        sortLayout.setOnClickListener(this);

        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int
                    dy) {
                if (dy > 0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();


                    if (!isRestaurantListGettingLoaded && (visibleItemCount +
                            pastVisibleItems) >=
                            totalItemCount - 2) {
                        isRestaurantListGettingLoaded = true;
                        int pageNumToBeFetched = totalItemCount / PAGE_LIMIT;
                        int startIndex = PAGE_LIMIT * pageNumToBeFetched;
                        if (startIndex > 0) {
                            getDealListing(type, format, DOPreferences.getCityName(getContext()), PAGE_LIMIT, startIndex, byTicketType, searchNeedle, categoryQuery, dinerCount, sortQuery);
                        }
                    }
                }
            }

        });
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        isRestaurantListGettingLoaded = false;

        renderFilterBtn();
        String reason = "";
        if (JSONUtil.apiStatus(responseObject)) {
            if (responseObject.optJSONObject("data") != null) {
                if (JSONUtil.getDealStreamData(responseObject) != null &&
                        JSONUtil.getDealStreamData(responseObject).length() > 0) {

                    JSONObject dataObj = responseObject.optJSONObject("data");
                    if (dataObj != null && dataObj.optInt("count") < PAGE_LIMIT) {
                        isRestaurantListGettingLoaded = true;
                    }

                    JSONArray datas = JSONUtil.getDealStreamData(responseObject);
                    if (datas != null) {
                        JSONObject obj = datas.optJSONObject(0);
                        if (obj != null) {
                            if (!TextUtils.isEmpty(obj.optString("type"))) {
                                if (obj.optString("type").equalsIgnoreCase("empty")) {

                                    resetFilterBtn.setVisibility(View.GONE);
                                    JSONObject deals = obj.optJSONObject("data");
                                    if (deals != null) {
                                        reason = deals.optString("reason");
                                    }
                                    noDealsTxt.setText(reason.toString());
                                    noDealsTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.coming_soon, 0, 0);
                                    topLayout.setVisibility(View.GONE);
                                    noDealTv.setVisibility(View.VISIBLE);


                                } else {
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                    topLayout.setVisibility(View.VISIBLE);
                                    noDealTv.setVisibility(View.GONE);


                                    JSONArray streamList = JSONUtil.getDealStreamData(responseObject);
                                    setSortData(JSONUtil.getDealSortByData(responseObject));

                                    setTagsData(streamList);
                                    if (listingAdapter != null) {
                                        listingAdapter.setData(datas, request
                                                .getIdentifier());
                                    }
                                }
                            }
                        }
                    }


                } else {
                    if (getView() == null)
                        return;
                    int startIndex = request.getIdentifier();
                    if (startIndex == 0) {
                        noDealTv.setVisibility(View.VISIBLE);
                        topLayout.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(DealFilterAdapter.getFilterQuery())
                                || ((DealFilterAdapter.getDinerCount() > 0))) {
                            resetFilterBtn.setVisibility(View.VISIBLE);
                            resetFilterBtn.setOnClickListener(this);
                            noDealsTxt.setText(getString(R.string.button_deals_result));
                            noDealsTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.no_deal_ico, 0, 0);


                        } else {
                            resetFilterBtn.setVisibility(View.GONE);
                            noDealsTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.no_deal_ico, 0, 0);
                            noDealsTxt.setText(getString(R.string.no_deals));
                        }
                    }
                }
            }
        } else {

            int startIndex = request.getIdentifier();
            if (startIndex == 0) {
                noDealTv.setVisibility(View.VISIBLE);
                topLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);

                if ((!TextUtils.isEmpty(DealFilterAdapter.getFilterQuery()))
                        || ((DealFilterAdapter.getDinerCount() > 0))) {
                    resetFilterBtn.setVisibility(View.VISIBLE);
                    resetFilterBtn.setOnClickListener(this);
                    noDealsTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.no_deal_ico, 0, 0);
                    noDealsTxt.setText(getString(R.string.button_deals_result));

                } else {

                    resetFilterBtn.setVisibility(View.GONE);
                    noDealsTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.no_deal_ico, 0, 0);
                    noDealsTxt.setText(getString(R.string.no_deals));
                }
            }
        }

    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);


    }

    private void setTagsData(JSONArray jsonArray) {
        streamTags = jsonArray;
    }

    private void setSortData(JSONArray jsonArray) {
        sortArray = jsonArray;
    }

    private void getDealListing(String type, String format, String cityName, int limit, int startIndex, String ticketType, String searchNeedle, String category, int dinerCount, String sortBy) {
        showLoader();
        getNetworkManager().jsonRequestGetNode(startIndex,
                AppConstant.NODE_DEAL_LISTING_URL,
                ApiParams.getDealListingParams(type, format, cityName, limit, startIndex, ticketType, searchNeedle, category, dinerCount, sortBy),
                this, this, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_btn_layout:
                //track event
                trackEventGA(getString(R.string.ga_screen_view_deals_listing), getString(R.string.ga_action_deals_filters), null);
                DealFilterFragment dealFilterFragment = DealFilterFragment.getInstance(dinerCount, categoryQuery);
                dealFilterFragment.setTargetFragment(this, Constants.DEALS_FILTER_REQUEST_CODE);
                showFragment(getFragmentManager(), dealFilterFragment);
                break;

            case R.id.sort_by_btn_layout:

                //track event
                trackEventGA(getString(R.string.ga_screen_view_deals_listing), getString(R.string.ga_action_deals_sort), null);
                if (sortArray != null) {
                    if (sortArray.length() > 0) {
                        DealSortingFragment sortingFragment = DealSortingFragment.getInstance(sortArray);
                        sortingFragment.setTargetFragment(this, Constants.DEALS_SORT_REQUEST_CODE);
                        showFragment(getFragmentManager(), sortingFragment);
                    }
                }

                break;

            case R.id.rest_image:
                JSONObject dealData = (JSONObject) v.getTag();
                String restaurantId = "";
                if (!TextUtils.isEmpty(dealData.optString("restaurantId"))) {
                    restaurantId = dealData.optString("restaurantId");
                }
                long price = 0l;
                if (!TextUtils.isEmpty(dealData.optString("price"))) {
                    price = Long.parseLong(dealData.optString("price"));
                }

//                trackEventGA(getString(R.string.ga_screen_view_deals_listing), getString(R.string.ga_action_deal_card), dealData.optString("title") + "_" +
//                        dealData.optString("restaurantId") + "_" + dealData.optString("ticketId"), price);

                if (!TextUtils.isEmpty(restaurantId)) {

                    RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
                    Bundle restaurantBundle = new Bundle();
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, restaurantId);
                    restaurantDetailFragment.setArguments(restaurantBundle);
                    // trackScreenToGA("Details_Reservation");
                    addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
                }
                break;

            case R.id.btn_view_more:

                //track event
                trackEventGA(getString(R.string.ga_screen_view_deals_listing), "ViewAllDeals", (String) v.getTag());

                if (!TextUtils.isEmpty((String) v.getTag())) {
                    RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
                    Bundle restaurantBundle = new Bundle();
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, (String) v.getTag());
                    restaurantDetailFragment.setArguments(restaurantBundle);
                    //trackScreenToGA("Details_Reservation");
                    addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
                }
                break;


            case R.id.main_container:
                String dealDatas = (String) v.getTag();
                //track event
                trackEventGA(getString(R.string.ga_screen_view_deals_listing), "ViewRestaurantDetails", (String) v.getTag());

                if (!TextUtils.isEmpty(dealDatas)) {

                    RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();
                    Bundle restaurantBundle = new Bundle();
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, dealDatas);
                    restaurantDetailFragment.setArguments(restaurantBundle);
                    //trackScreenToGA("Details_Reservation");
                    addToBackStack(getActivity().getSupportFragmentManager(), restaurantDetailFragment);
                }
                break;

            case R.id.reset_filter:
                resetFilter();
                break;

            default:
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.DEALS_FILTER_REQUEST_CODE) {
                categoryQuery = data.getStringExtra(DealFilterFragment.CATEGORY);
                dinerCount = data.getIntExtra(DealFilterFragment.DINER_COUNT, 0);

                DealFilterAdapter.setFilterQuery(categoryQuery);
                DealFilterAdapter.setDinerCount(dinerCount);

                int startIndex = PAGE_LIMIT * PAGE_NUM;
                getDealListing(type, format, DOPreferences.getCityName(getContext()), PAGE_LIMIT, startIndex, ticketType, "", categoryQuery, dinerCount, sortQuery);

            } else if (requestCode == Constants.DEALS_SORT_REQUEST_CODE) {
                sortQuery = data.getStringExtra(DealSortingFragment.SORT_BY);

                // DOPreferences.setSortBy(getContext(), sortQuery);
                DealSortByAdapter.setSortQuery(sortQuery);

                int startIndex = PAGE_LIMIT * PAGE_NUM;
                getDealListing(type, format, DOPreferences.getCityName(getContext()), PAGE_LIMIT, startIndex, ticketType, "", categoryQuery, dinerCount, sortQuery);
            }
        }
    }

    private void renderFilterBtn() {
        if (dinerCount > 0 || !TextUtils.isEmpty(categoryQuery)) {
            filterSelectedImvw.setVisibility(View.VISIBLE);
        } else {
            filterSelectedImvw.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(sortQuery)) {
            sortSelectedImvw.setVisibility(View.VISIBLE);
        } else {
            sortSelectedImvw.setVisibility(View.GONE);
        }
    }

    private void resetFilter() {
        cleanFilter();
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
    }
}
