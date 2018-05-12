package com.dineout.book.fragment.myaccount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;

import com.dineout.book.R;
import com.dineout.book.util.Constants;
import com.dineout.book.util.UiUtil;
import com.dineout.book.adapter.YourBillAdapter;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class YourBillsFragment extends YouPageWrapperFragment implements View.OnClickListener{

    RecyclerView mRecyclerView;
    YourBillAdapter mAdapter;
    TextView noBillsText;
    //private boolean isLoading = false;
    private boolean isBillPresent = true;
    private boolean listGettingLoaded = false;

    public static YourBillsFragment newInstance() {
        Bundle args = new Bundle();

        YourBillsFragment fragment = new YourBillsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Track Screen
        trackScreenToGA("YourBills");

        return inflater.inflate(R.layout.fragment_your_bill, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle("Your Bills");

        mAdapter = new YourBillAdapter(getActivity(), null);
        RecyclerView.LayoutManager mManager = new LinearLayoutManager(getContext());

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view_your_bills);
        noBillsText = (TextView) getView().findViewById(R.id.text_no_bills);
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new PagingScrollListener());

        if (getView() != null) {
            // Set Click Listener
            getView().findViewById(R.id.textView_your_bills_terms).setOnClickListener(this);
        }

        // authenticate the user
        authenticateUser();

//        // Take API Hit
//        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        fetchBill(0);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.textView_your_bills_terms) {
            // Handle Terms Condition Click
            handleTermsConditionClick();
        }
    }

    private void handleTermsConditionClick() {

        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("D_YourBills","TermsAndConditionClick","TermsAndConditionClick",hMap);

        WebViewFragment webViewFragment = new WebViewFragment();

        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.tnc));
        bundle.putString("url", AppConstant.UPLOAD_BILL_TNC_URL);
        webViewFragment.setArguments(bundle);

        addToBackStack(getActivity().getSupportFragmentManager(), webViewFragment);
    }

    public void fetchBill(int pageNum) {
        HashMap<String, String> param = new HashMap<>();
        param.put("limit", Constants.PAGE_LIMIT + "");

        int offset = pageNum * Constants.PAGE_LIMIT;
        param.put("offset", offset + "");

        showLoader();

        getNetworkManager().jsonRequestGetNode(pageNum, AppConstant.URL_GET_BILL, param, this, this, false);
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        listGettingLoaded = false;
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject != null && responseObject.optBoolean("status")) {
            JSONObject dataJSONObject = responseObject.optJSONObject("data");
            if (dataJSONObject != null) {

                JSONArray outputParamsJsonObject = dataJSONObject.optJSONArray("stream");
                int nextIndex = dataJSONObject.optInt("nextIndex");

                if (outputParamsJsonObject != null) {
                    isBillPresent = true;
                    if (outputParamsJsonObject.length() <= 0) {
                        isBillPresent = mRecyclerView.getAdapter().getItemCount() > 0 ? true : false;
                    }

                    listGettingLoaded = false;

                    if (nextIndex == -1) {
                        listGettingLoaded = true;
                    }

                    mAdapter.updateData(outputParamsJsonObject,
                            request.getIdentifier());
                } else {
                    isBillPresent = mRecyclerView.getAdapter().getItemCount() > 0 ? true : false;

                }
            } else {
                isBillPresent = mRecyclerView.getAdapter().getItemCount() > 0 ? true : false;
            }

            setVisibilityOfNoBillsBanner(isBillPresent);


        } else if (responseObject != null && !responseObject.optBoolean("status")) {
            // handle session expire
//            handleApiResponse(responseObject);

        } else {
            // Show Message
            UiUtil.showToastMessage(getActivity(), getActivity().getString(R.string.text_unable_fetch_details));
        }
    }

    private void setVisibilityOfNoBillsBanner(boolean isBillUploaded) {
        if (isBillUploaded) {
            noBillsText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            noBillsText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }




    public class PagingScrollListener extends RecyclerView.OnScrollListener {

        public PagingScrollListener() {
            super();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (dy > 0) {

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!listGettingLoaded &&
                        (visibleItemCount + pastVisibleItems + 1) >= totalItemCount) {
                    listGettingLoaded = true;
                    int pageNumToBeFetched = totalItemCount / Constants.PAGE_LIMIT;
                    fetchBill(pageNumToBeFetched);
                }
            }
        }
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_your_bills),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
