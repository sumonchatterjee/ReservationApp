package com.dineout.book.fragment.deals;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.recycleradapters.DealFilterAdapter;
import com.dineout.recycleradapters.util.JSONUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DealFilterFragment extends MasterDOJSONReqFragment implements View.OnClickListener {

    public static final String DINER_COUNT = "diner_count";
    public static final String CATEGORY = "category";
    private final int REQUEST_CODE_DEAL_CATEGORY = 200;
    private DealFilterAdapter listingAdapter;
    private int offset = 0;
    private int limit = 15;
    private RecyclerView filterRecyclerView;
    private Button applyButton;
    private List<String> selectedCategories = new ArrayList<>();

    private LinearLayout dinerContainer;

    private int dinerCount = 0;

    public static DealFilterFragment getInstance(int dinnerCount, String categories) {
        DealFilterFragment filterFragment = new DealFilterFragment();
        filterFragment.setArguments(bundle(dinnerCount, categories));
        return filterFragment;
    }

    public static Bundle bundle(int dinnerCount, String categories) {
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(categories))
            bundle.putString(CATEGORY, categories);
        if (dinnerCount != 0)
            bundle.putInt(DINER_COUNT, dinnerCount);

        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trackScreenToGA(getString(R.string.ga_screen_deal_filters));

        // Set Style
        AppUtil.setHoloLightTheme(this);

        if (getArguments() != null) {
            dinerCount = getArguments().getInt(DINER_COUNT);
            String categoryQuery = getArguments().getString(CATEGORY);
            if (!TextUtils.isEmpty(categoryQuery)) {
                categoryQuery = categoryQuery.replace("[", "");
                categoryQuery = categoryQuery.replace("]", "");
                String[] categories = categoryQuery.split(",");
                for (String category : categories) {
                    selectedCategories.add(category.replaceAll("\"", ""));
                }
            }
        }

        listingAdapter = new DealFilterAdapter(getActivity(), selectedCategories);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.deal_search_filters, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View rootView = getView();


        if (rootView != null) {
            // Set Toolbar
            setToolbar();

            filterRecyclerView = (RecyclerView) rootView.findViewById(R.id.filter_recycler);

            RecyclerView.LayoutManager mManager = new LinearLayoutManager(getContext());
            filterRecyclerView.setLayoutManager(mManager);
            filterRecyclerView.setAdapter(listingAdapter);

            dinerContainer = (LinearLayout) rootView.findViewById(R.id.dinersContainer);

            View singleUser = dinerContainer.findViewById(R.id.single_user);
            View doubleUser = dinerContainer.findViewById(R.id.double_user);
            View twoPlusUser = dinerContainer.findViewById(R.id.two_plus_user);

            singleUser.setOnClickListener(this);
            doubleUser.setOnClickListener(this);
            twoPlusUser.setOnClickListener(this);

            singleUser.setTag(1);
            doubleUser.setTag(2);
            twoPlusUser.setTag(999);

            View selectedView = dinerContainer.findViewWithTag(dinerCount);
            if (selectedView != null) {
                selectedView.setSelected(true);
            }

            rootView.findViewById(R.id.textView_deal_filter_clear).setOnClickListener(this);

            applyButton = (Button) rootView.findViewById(R.id.btn_apply_summary);
            applyButton.setOnClickListener(this);

        }

        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    private void setToolbar() {
        Toolbar mToolbar = (Toolbar) getView().findViewById(R.id.toolbar_deal_filter);
        mToolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_navigation_arrow_back, null));
        mToolbar.setTitle(R.string.title_filters);

        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btn_apply_summary) {
            trackEventGA(getString(R.string.ga_screen_deal_filters),"Apply",null);
            applyFilters();

        } else if (viewId == R.id.textView_deal_filter_clear) {
            trackEventGA(getString(R.string.ga_screen_deal_filters),"Clear",null);
            clearFilters();
            applyFilters();

        } else {
            for (int i = 0; i < dinerContainer.getChildCount(); i++) {
                dinerContainer.getChildAt(i).setSelected(false);
            }

            view.setSelected(true);

            dinerCount = Integer.valueOf(view.getTag().toString());
            if(dinerCount==1){
                trackEventGA(getString(R.string.ga_screen_deal_filters),"1Person",null);
            }else if (dinerCount==2){
                trackEventGA(getString(R.string.ga_screen_deal_filters),"2Person",null);
            }else if(dinerCount ==999){
                trackEventGA(getString(R.string.ga_screen_deal_filters),"2PlusPerson",null);
            }
        }
    }

    private void applyFilters() {
        StringBuilder queryBuilder = null;

        if (selectedCategories != null && selectedCategories.size() > 0) {
            String lastCategory = selectedCategories.get(selectedCategories.size() - 1);
            queryBuilder = new StringBuilder();
            queryBuilder.append("[");
            for (String category : selectedCategories) {
                queryBuilder.append("\"");
                queryBuilder.append(category);
                queryBuilder.append("\"");
                if (!lastCategory.equalsIgnoreCase(category)) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append("]");

        }

        Intent intent = new Intent();
        if (queryBuilder != null) {
            intent.putExtra(CATEGORY, queryBuilder.toString());
        }
        intent.putExtra(DINER_COUNT, dinerCount);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);
        getDealCategories("ticket", "detailed", DOPreferences.getCityName(getContext()), limit, offset, "deal", "");

    }

    private void getDealCategories(String type, String format, String cityName, int limit, int offset, String ticketType, String searchNeedle) {
        showLoader();
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_DEAL_CATEGORY,
                AppConstant.NODE_DEAL_LISTING_URL,
                ApiParams.getDealListingParams(type, format, cityName, limit, offset, ticketType, searchNeedle, null, 0, null),
                this, this, true);
    }


    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_DEAL_CATEGORY) {
            if (JSONUtil.apiStatus(responseObject)) {
                listingAdapter.setData(responseObject);
                setData(responseObject);
            }
        }
    }


    private void setData(JSONObject data) {
        try {
            JSONObject filters = JSONUtil.getFilters(data);
            if (filters != null) {
                JSONObject byDinerCount = filters.optJSONObject("bydinerCount");
                if (byDinerCount != null) {
                    JSONArray countArray = byDinerCount.optJSONArray("dinerCountJSON");
                    if (countArray != null) {
                        for (int i = 0; i < countArray.length(); i++) {
                            if (countArray.getJSONObject(i).optString("count").equalsIgnoreCase("0")) {
                                dinerContainer.getChildAt(i).setEnabled(false);
                                dinerContainer.getChildAt(i).setSelected(false);
                                dinerContainer.getChildAt(i).setOnClickListener(null);
                                View view = dinerContainer.getChildAt(i);
                                if (view != null) {
                                    view.setAlpha(.3f);
                                }


                            }

                        }
                        listingAdapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (JSONException ex) {

        }
    }


    private void clearFilters() {

        dinerCount = 0;
        selectedCategories.clear();

        for (int i = 0; i < dinerContainer.getChildCount(); i++) {
            dinerContainer.getChildAt(i).setSelected(false);
        }

        listingAdapter.notifyDataSetChanged();

    }


}
