package com.dineout.book.fragment.myaccount;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.dialogs.EarningHistoryBreakUpDialog;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.widgets.EarningFilterPopUpWindow;
import com.dineout.recycleradapters.EarningFilterPopUpAdapter;
import com.dineout.recycleradapters.NewMyEarningsAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.dineout.book.dialogs.EarningHistoryBreakUpDialog.INFO_STRING;
import static com.dineout.recycleradapters.util.AppUtil.appendTo;
import static com.dineout.recycleradapters.util.AppUtil.setTextViewInfo;

/**
 * Created by sawai.parihar on 08/05/17.
 */

public class NewMyEarningFragment extends YouPageWrapperFragment implements View.OnClickListener,
        EarningFilterPopUpAdapter.FilterCallback, NewMyEarningsAdapter.NewMyEarningsAdapterCallback {
    private static final int REQUEST_CODE = 0x1;

    private static final Integer API_PAGINATION_LIMIT = 10;
    private static final String PAGINATION_START_INDEX = "pagination_start_index";
    private static final String ALL_API_DATA_FETCHED = "all_api_data_fetched";
    private static final String API_IN_PROGRESS = "api_in_progress";
    private static final String FILTER_ID = "filter_id";

    private NewMyEarningsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_fragment_my_earnings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        mAdapter = new NewMyEarningsAdapter(getActivity());
        rv.setAdapter(mAdapter);

        // set listener
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    boolean allApiDataFetched = Boolean.valueOf(getItemFromArguments(ALL_API_DATA_FETCHED));
                    boolean isApiInProgress = Boolean.valueOf(getItemFromArguments(API_IN_PROGRESS));

                    if (!allApiDataFetched && !isApiInProgress
                            && (visibleItemCount + pastVisibleItems) >= totalItemCount - 4) {
                        earningApiCall();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set toolbar title
        setToolbarTitle(R.string.title_earning_history);

        // authenticate the user
        authenticateUser();
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // reset arguments
        putItemIntoArguments(PAGINATION_START_INDEX, "0");
        putItemIntoArguments(ALL_API_DATA_FETCHED, String.valueOf(false));
        putItemIntoArguments(API_IN_PROGRESS, String.valueOf(false));
//        putItemIntoArguments(FILTER_ID, "");

        // hide container view
        if (getView() != null) {
            getView().findViewById(R.id.container).setVisibility(View.INVISIBLE);
        }

        // set app bar layout in expanded state
        setAppBarLayoutInExpandedState();

        // call api
        earningApiCall();
    }

    @Override
    public void onFilterClick(JSONObject obj) {
        // put filter id in arguments
        putItemIntoArguments(PAGINATION_START_INDEX, "0");
        putItemIntoArguments(ALL_API_DATA_FETCHED, String.valueOf(false));
        putItemIntoArguments(API_IN_PROGRESS, String.valueOf(false));
        putItemIntoArguments(FILTER_ID, obj.optString("filter_key"));

        // reset adapter
        if (mAdapter != null) {
            mAdapter.setJsonArray(null);
            mAdapter.setSectionsData(null);
        }

        // hide container view
        if (getView() != null) {
            getView().findViewById(R.id.container).setVisibility(View.INVISIBLE);
        }

        // set app bar layout in expanded state
        setAppBarLayoutInExpandedState();

        // call api
        earningApiCall();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all_transaction_filter_wrapper_layout:
                EarningFilterPopUpWindow popUpWindow = new EarningFilterPopUpWindow(getActivity());
                popUpWindow.setInfoString(v.getTag().toString());
                popUpWindow.setFilterCallback(this);
                popUpWindow.show(v);

                break;

            case R.id.earning_history_break_up_wrapper:
                v.setEnabled(false);
                EarningHistoryBreakUpDialog frag = new EarningHistoryBreakUpDialog();
                Bundle bundle = new Bundle();
                bundle.putString(INFO_STRING, v.getTag().toString());
                frag.setArguments(bundle);
                showFragment(getActivity().getSupportFragmentManager(), frag);

                final View v1 = v;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v1.setEnabled(true);
                    }
                }, 1000);
                break;
        }
    }

    @Override
    public void openDeepLink(JSONObject object) {
        if (object != null) {
            String deepLink = object.optString("deep_link");
            if (!TextUtils.isEmpty(deepLink)) {
                MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), deepLink);

                if (frag != null) {
                    addToBackStack(getFragmentManager(), frag,
                            R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                }
            }
        }
    }

    private void earningApiCall() {
        // set the api progress flag
        putItemIntoArguments(API_IN_PROGRESS, String.valueOf(true));

        // Take API Hit
        showLoader();


        String startIndex = getItemFromArguments(PAGINATION_START_INDEX);
        String filterId = getItemFromArguments(FILTER_ID);

        getNetworkManager().jsonRequestGet(REQUEST_CODE, AppConstant.URL_TRANSACTION_HISTORY,
                ApiParams.getNewMyEarningsParams(DOPreferences.getDinerId(getActivity().getApplicationContext()),
                        DOPreferences.getCityId(getActivity().getApplicationContext()), startIndex,
                        String.valueOf(API_PAGINATION_LIMIT), filterId), this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        // reset the api progress flag
        putItemIntoArguments(API_IN_PROGRESS, String.valueOf(false));

        if (request.getIdentifier() == REQUEST_CODE) {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {

                    // show container view
                    if (getView() != null) {
                        getView().findViewById(R.id.container).setVisibility(View.VISIBLE);
                    }

                    if (responseObject.optJSONObject("output_params") != null) {
                        // Get Data
                        JSONObject jsonObjectData = responseObject.optJSONObject("output_params").optJSONObject("data");

                        if (jsonObjectData != null) {
                            // Get Section
                            JSONArray jsonArraySection = jsonObjectData.optJSONArray("section");
                            JSONObject jsonObjectSectionData = jsonObjectData.optJSONObject("section_data");
                            JSONObject jsonObjectCData = jsonObjectData.optJSONObject("c_data");

                            // set up header data
                            if (jsonObjectCData != null) {
                                setUpHeaderData(jsonObjectCData);
                            }

                            // set up list data
                            setUpSectionData(jsonArraySection, jsonObjectSectionData);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        // show container view
//        if (getView() != null) {
//            getView().findViewById(R.id.container).setVisibility(View.VISIBLE);
//        }

        // reset the api progress flag
        putItemIntoArguments(API_IN_PROGRESS, String.valueOf(false));
    }

    // set up header data
    private void setUpHeaderData(JSONObject cData) {
        if (cData != null && getView() != null) {
            // set balance info
            JSONObject balanceData = cData.optJSONObject("balance");

            if (balanceData != null) {
                // set amount
                setTextViewInfo(getView().findViewById(R.id.earning_amount_tv),
                        balanceData.optJSONObject("amount"));

                // set amount text
                setTextViewInfo(getView().findViewById(R.id.earning_balance_tv),
                        balanceData.optJSONObject("amount_text"));

                JSONObject amountBreakUpObj = balanceData.optJSONObject("amount_summary");
                if (amountBreakUpObj == null || !amountBreakUpObj.keys().hasNext()) {
                    getView().findViewById(R.id.earning_summary_wrapper).setVisibility(View.GONE);

                } else {
                    getView().findViewById(R.id.earning_summary_wrapper).setVisibility(View.VISIBLE);

                    // set amount summary text
                    setTextViewInfo(getView().findViewById(R.id.earning_summary_tv),
                            amountBreakUpObj.optJSONObject("amount_summary_text"));

                    // set amount summary icon
                    String summaryIconUrl = amountBreakUpObj.optString("amount_summary_icon");
                    NetworkImageView summaryIcon = (NetworkImageView) getView().findViewById(R.id.earning_summary_iv);
                    if (TextUtils.isEmpty(summaryIconUrl)) {
                        summaryIcon.setVisibility(View.GONE);
                    } else {
                        summaryIcon.setVisibility(View.VISIBLE);
                        summaryIcon.setImageUrl(summaryIconUrl, getImageLoader());
                    }
                }

                // amount break up
                if (balanceData.optJSONArray("amount_breakup") != null
                        && balanceData.optJSONArray("amount_breakup").length() > 0) {
                    // set onclick on amount
                    getView().findViewById(R.id.earning_history_break_up_wrapper).setTag(balanceData);
                    getView().findViewById(R.id.earning_history_break_up_wrapper).setOnClickListener(this);

                } else {
                    // set onclick on amount
                    getView().findViewById(R.id.earning_history_break_up_wrapper).setTag(null);
                    getView().findViewById(R.id.earning_history_break_up_wrapper).setOnClickListener(null);
                }
            }

            // set selected filter text
            setTextViewInfo(getView().findViewById(R.id.filter_left_text_tv),
                    cData.optJSONObject("header_text"));

            // show bottom arrow
            if (cData.optJSONObject("header_text") != null &&
                    !TextUtils.isEmpty(cData.optJSONObject("header_text").optString("text"))) {
                getView().findViewById(R.id.transaction_filter_icon_iv).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.transaction_filter_icon_iv).setVisibility(View.GONE);
            }

            // set filter info
            JSONObject filterData = cData.optJSONObject("filter");
            if (filterData != null) {
                // set filter text
                setTextViewInfo(getView().findViewById(R.id.filter_right_text_tv),
                        filterData.optJSONObject("title"));

                // enable filter click if data available
                if (filterData.optJSONArray("filter_list") != null && filterData.optJSONArray("filter_list").length() > 0) {
                    getView().findViewById(R.id.all_transaction_filter_wrapper_layout).setTag(filterData);
                    getView().findViewById(R.id.all_transaction_filter_wrapper_layout).setOnClickListener(this);
                }
            }
        }
    }

    // set section data
    private void setUpSectionData(JSONArray jsonArraySection, JSONObject jsonObjectSectionData) {
        // set up list data

        if(getView()==null)
            return;
        if (jsonArraySection != null && jsonArraySection.length() > 0
                && jsonObjectSectionData != null) {

            // set up start index
            int startIndex = Integer.valueOf(getItemFromArguments(PAGINATION_START_INDEX));
            putItemIntoArguments(PAGINATION_START_INDEX, String.valueOf((startIndex + jsonArraySection.length())));

            if (jsonArraySection.length() < API_PAGINATION_LIMIT) {
                // check if section array length is less than 10 then stop api calls
                putItemIntoArguments(ALL_API_DATA_FETCHED, String.valueOf(true));
            }

            if (mAdapter != null) {
                if (mAdapter.getJsonArray() == null) {
                    mAdapter.setJsonArray(jsonArraySection);
                } else {
                    appendTo(mAdapter.getJsonArray(), jsonArraySection);
                }

                if (mAdapter.getSectionsData() == null) {
                    mAdapter.setSectionsData(jsonObjectSectionData);
                } else {
                    appendTo(mAdapter.getSectionsData(), jsonObjectSectionData);
                }

                mAdapter.setCallback(this);
                mAdapter.setImageLoader(getImageLoader());

                mAdapter.notifyDataSetChanged();
            }
        } else {
            // stop api calls
            putItemIntoArguments(ALL_API_DATA_FETCHED, String.valueOf(true));
        }


        if (mAdapter != null && (mAdapter.getJsonArray() == null
                || mAdapter.getJsonArray().length() == 0)) {
            getView().findViewById(R.id.recycler_view).setVisibility(View.GONE);
            getView().findViewById(R.id.no_earning_layout).setVisibility(View.VISIBLE);

        } else {
            getView().findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.no_earning_layout).setVisibility(View.GONE);
        }
    }

    private boolean putItemIntoArguments(String key, String value) {
        boolean returnValue = false;
        if (getArguments() != null) {
            getArguments().putString(key, value);
            returnValue = true;
        }

        return returnValue;
    }

    private String getItemFromArguments(String key) {
        String returnValue = "";
        if (getArguments() != null && getArguments().containsKey(key)) {
            returnValue = getArguments().getString(key, "");
        }

        return returnValue;
    }

    private void setAppBarLayoutInExpandedState() {
        if (getView() != null) {
            ((AppBarLayout) getView().findViewById(R.id.appbar)).setExpanded(true);
        }
    }
}
