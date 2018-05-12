package com.dineout.book.fragment.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.FilterUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.util.FilterUtil.updateOtherMatchingFilterOptions;
import static com.dineout.recycleradapters.FilterCategoryRecyclerAdapter.PARAM_IS_SELECTED;

public class FilterFragment extends MasterDOJSONReqFragment
        implements View.OnClickListener, FilterCategoryPanelFragment.FilterCategoryHandler,
        FilterOptionsPanelFragment.FilterOptionPanelListener {

    private JSONArray filterJsonArray;
    private JSONArray orgFilterJsonArray;
    private FilterClickListener filterClickListener;
    private String selectedCategoryKey;
    private boolean isOptionSelectionMade;
    private String bundleSearchKeyword;

    public void setFilterJsonArray(JSONArray filterJsonArray) {
        this.filterJsonArray = filterJsonArray;
        orgFilterJsonArray = filterJsonArray;
    }

    public void setFilterClickListener(FilterClickListener filterClickListener) {
        this.filterClickListener = filterClickListener;
    }

    public void setBundleSearchKeyword(String bundleSearchKeyword) {
        this.bundleSearchKeyword = bundleSearchKeyword;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
       // trackScreenToGA(getString(R.string.ga_screen_filters));

        // Initialize View
        initializeView();
    }

    private void initializeView() {
        // Set Toolbar
        setToolbarTitle(R.string.text_filter);

        // Set Click Listener on Reset Button
        getView().findViewById(R.id.textView_filter_reset).setOnClickListener(this);

        // Set Click Listener on Apply Filter Button
        getView().findViewById(R.id.floatingActionButton_filter).setOnClickListener(this);

        // Attach Filter Category Panel
        attachFilterCategoryPanel();

        // Attach Filter Options Panel
        attachFilterOptionsPanel();
    }

    private void attachFilterCategoryPanel() {
        // Prepare Visible Filter List
        prepareVisibleFilterList();

        // Set First Category selected
        setFirstCategorySelected();

        // Get Panel Fragment
        FilterCategoryPanelFragment filterCategoryPanelFragment = new FilterCategoryPanelFragment();
        filterCategoryPanelFragment.setFilterJsonArray(filterJsonArray);
        filterCategoryPanelFragment.setFilterCategoryHandler(this);
        filterCategoryPanelFragment.setChildFragment(true);

        // Attach
        getFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayout_filter_category_panel, filterCategoryPanelFragment)
                .commit();
    }

    private void prepareVisibleFilterList() {
        // Check for NULL
        if (filterJsonArray != null) {
            JSONArray visibleFilterJsonArray = new JSONArray();
            int filterSize = filterJsonArray.length();

            for (int index = 0; index < filterSize; index++) {
                // Get Filter Object
                JSONObject filterJsonObject = filterJsonArray.optJSONObject(index);

                if (filterJsonObject != null) {
                    // Check Filter Visibility
                    if (filterJsonObject.optBoolean("is_visible", false)) {
                        // Get Options Array
                        JSONArray optionsJsonArray = filterJsonObject.optJSONArray("arr");

                        // Check for NULL / Empty
                        if (optionsJsonArray != null && optionsJsonArray.length() > 0) {
                            // Add Filter Object to New List
                            visibleFilterJsonArray.put(filterJsonObject);
                        }
                    }
                }
            }

            // Set Visible Filter List back to Old Filter List variable
            filterJsonArray = visibleFilterJsonArray;
        }
    }

    private void setFirstCategorySelected() {
        // Check for NULL
        if (filterJsonArray != null && filterJsonArray.length() > 0) {
            // Get First Filter Object
            JSONObject filterJsonObject = filterJsonArray.optJSONObject(0);

            if (filterJsonObject != null) {
                // Set Category Key
                selectedCategoryKey = filterJsonObject.optString("key", "");

                // Set isSelected true
                try {
                    filterJsonObject.put(PARAM_IS_SELECTED, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void attachFilterOptionsPanel() {
        // Get Panel Fragment
        final FilterOptionsPanelFragment filterOptionsPanelFragment = new FilterOptionsPanelFragment();
        filterOptionsPanelFragment.setFilterOptionPanelListener(this);
        filterOptionsPanelFragment.setChildFragment(true);

        // Attach
        getFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayout_filter_options_panel, filterOptionsPanelFragment)
                .commit();

        // Populate First Filter Category Options
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                populateFilterOptionsOfFirstCategory(filterOptionsPanelFragment);
            }
        }, 100L);
    }

    private void populateFilterOptionsOfFirstCategory(FilterOptionsPanelFragment filterOptionsPanelFragment) {
        // Check for NULL
        if (filterJsonArray != null && filterJsonArray.length() > 0) {
            // Get First Filter Object
            JSONObject filterOptionJsonObject = filterJsonArray.optJSONObject(0);

            if (filterOptionJsonObject != null) {
                // Set Filter Option Data
                filterOptionsPanelFragment.setFilterCategoryJsonObject(filterOptionJsonObject);

                // Render Filter Option Panel
                filterOptionsPanelFragment.renderFilterOptionPanel();
            }
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.textView_filter_reset) {
            handleResetFilterClick();

        } else if (viewId == R.id.floatingActionButton_filter) {
            handleApplyFilterClick();
        }
    }

    private void handleResetFilterClick() {
        // Track Event
//        trackEventGA(getString(R.string.ga_screen_filters),
//                getString(R.string.ga_action_reset_all), null);


        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_listing),getString(R.string.d_filter_reset_click),getString(R.string.d_filter_reset_click),hMap);

        // Ask for Confirmation
        confirmResetAction();
    }

    private void confirmResetAction() {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION, getString(R.string.text_confirm_reset_action));
        bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, getString(R.string.title_confirm));
        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, getString(R.string.text_reset_and_apply));
        bundle.putString(AppConstant.BUNDLE_DIALOG_NEGATIVE_BUTTON_TEXT, getString(R.string.text_dismiss));

        UiUtil.showCustomDialog(getContext(), bundle, new ResetDialogCallbackListener());
    }

    private void hideKeyboard() {
        if (getActivity() != null)
            AppUtil.hideKeyboard(getActivity());
    }

    private void handleApplyFilterClick() {
        // Hide Keyboard
        hideKeyboard();

        // Track Event
//        trackEventGA(getString(R.string.ga_screen_filters),
//                getString(R.string.ga_action_apply), null);

        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_listing),getString(R.string.d_apply_click),getString(R.string.d_apply_click) ,hMap);

        // Show Loader
        showLoader();

        // Get Applied Filters
        HashMap<String, Object> appliedFilters = FilterUtil.getAppliedFilters(filterJsonArray, orgFilterJsonArray);

        // Hide Loader
        hideLoader();

        // Check for NULL
        if (filterClickListener != null) {
            filterClickListener.onApplyFilterClick(appliedFilters);
        }

        // Pop Out
        popBackStack(getFragmentManager());
    }

    @Override
    public void onFilterCategoryClick(JSONObject filterCategoryJsonObject) {
        // Get Selected Category Key
        String categoryKey = filterCategoryJsonObject.optString("key", "");

        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_listing), "FilterClick",
                filterCategoryJsonObject.optString("name", ""), hMap);

        if (!categoryKey.equalsIgnoreCase(selectedCategoryKey)) {
            // Set Category Key
            selectedCategoryKey = categoryKey;

            // Check if Different Category is Selected
            if (isOptionSelectionMade) {
                // Set Flag
                isOptionSelectionMade = false;

                // Initiate Search API
                initiateSearchAPIHit();

            } else {
                // Perform Click
                performFilterCategoryClick(filterCategoryJsonObject);
            }
        }
    }

    private void initiateSearchAPIHit() {
        HashMap<String, Object> params = new HashMap<>();
        int startIndex = 0;

        // Show Loader
        showLoader();

        // Get Request Params
        params = ApiParams.getSRPSearchParams(params, DOPreferences.getCityName(getContext()),
                bundleSearchKeyword, startIndex, "", "", DOPreferences.getLocationSf(getContext()));

        // Get Applied Filters
        HashMap<String, Object> appliedFilters = FilterUtil.getAppliedFilters(filterJsonArray, orgFilterJsonArray);

        if (appliedFilters != null && appliedFilters.size() > 0) {
            params.putAll(appliedFilters);
        }

        // Take API Hit
        getNetworkManager().jsonRequestPostNode(startIndex, AppConstant.URL_NODE_SRP,
                params, this, this, true);
    }

    @Override
    public void onFilterOptionClick(String categoryKey, String optionName, int applied) {
        // Get Filter Option Fragment
        Fragment fragment = getFragmentManager().findFragmentById(R.id.frameLayout_filter_category_panel);

        if (fragment != null && fragment instanceof FilterCategoryPanelFragment) {
            // Set Flag
            isOptionSelectionMade = true;

            // Track Option Click
//            trackEventGA(getString(R.string.ga_screen_filters),
//                    getString(R.string.ga_action_sub_tags), categoryKey);

            // Update Other Matching Filter Options
            updateOtherMatchingFilterOptions(filterJsonArray, categoryKey, optionName, applied);

            // Downcast Fragment
            FilterCategoryPanelFragment filterCategoryPanelFragment = (FilterCategoryPanelFragment) fragment;

            // Update Filter Category
            filterCategoryPanelFragment.updateFilterCategory();
        }
    }

    private void refreshFiltersOnCategoryClick(JSONArray filterJsonArray) {
        // Set New Filter JSON
        setFilterJsonArray(filterJsonArray);

        // Prepare Visible Filter List
        prepareVisibleFilterList();

        // Refresh Category
        refreshFilterCategory();

        // Refresh Filter Option
        refreshFilterOption();
    }

    private void refreshFilterCategory() {
        // Get Filter Option Fragment
        Fragment fragment = getFragmentManager().findFragmentById(R.id.frameLayout_filter_category_panel);

        if (fragment != null && fragment instanceof FilterCategoryPanelFragment) {
            // Downcast Fragment
            FilterCategoryPanelFragment filterCategoryPanelFragment = (FilterCategoryPanelFragment) fragment;
            filterCategoryPanelFragment.setFilterJsonArray(filterJsonArray);
            filterCategoryPanelFragment.updateFilterCategory();
        }
    }

    private void refreshFilterOption() {
        JSONObject jsonObject = FilterUtil.getSelectedCategoryJSON(filterJsonArray, selectedCategoryKey);
        if (jsonObject != null) {
            performFilterCategoryClick(jsonObject);

        } else {
            // Set First Category selected
            setFirstCategorySelected();

            // Again fetch for Filter Object
            if (filterJsonArray != null && filterJsonArray.length() > 0) {
                // Get First Filter Object
                JSONObject filterJsonObject = filterJsonArray.optJSONObject(0);

                if (filterJsonObject != null) {
                    performFilterCategoryClick(filterJsonObject);
                }
            }
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null)
            return;

        if (responseObject != null && responseObject.optBoolean("status")) {
            // Get Data
            JSONObject dataJsonObject = responseObject.optJSONObject("data");

            if (dataJsonObject != null) {
                // Get Restaurant
                JSONObject restaurantJsonObject = dataJsonObject.optJSONObject("RESTAURANT");

                if (restaurantJsonObject != null) {
                    // Get Filters
                    JSONArray filterJsonArray = restaurantJsonObject.optJSONArray("filters");

                    if (filterJsonArray != null && filterJsonArray.length() > 0) {
                        // Refresh UI
                        refreshFiltersOnCategoryClick(filterJsonArray);
                    }
                }
            }
        }
    }

    private void performFilterCategoryClick(JSONObject filterCategoryJsonObject) {
        // Check for NULL
        if (filterCategoryJsonObject != null) {
            // Get Filter Option Fragment
            Fragment fragment = getFragmentManager().findFragmentById(R.id.frameLayout_filter_options_panel);

            if (fragment != null && fragment instanceof FilterOptionsPanelFragment) {
                // Track Category Click
//                trackEventGA(getString(R.string.ga_screen_filters),
//                        getString(R.string.ga_action_category),
//                        filterCategoryJsonObject.optString("key", ""));

                // Downcast Fragment
                FilterOptionsPanelFragment filterOptionsPanelFragment = (FilterOptionsPanelFragment) fragment;

                // Set Filter Option Data
                filterOptionsPanelFragment.setFilterCategoryJsonObject(filterCategoryJsonObject);

                // Render Filter Option Panel
                filterOptionsPanelFragment.renderFilterOptionPanel();
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        filterJsonArray = null;
        orgFilterJsonArray = null;
        filterClickListener = null;
        selectedCategoryKey = null;
    }

    public interface FilterClickListener {
        void onResetFilterClick(HashMap<String, Object> appliedFilters);

        void onApplyFilterClick(HashMap<String, Object> appliedFilters);
    }

    private class ResetDialogCallbackListener implements DialogListener {
        @Override
        public void onPositiveButtonClick(AlertDialog alertDialog) {
            // Show Loader
            showLoader();

            // Get Applied Filters
            HashMap<String, Object> appliedFilters = FilterUtil.getInvisibleAppliedFilters(orgFilterJsonArray);

            // Hide Loader
            hideLoader();

            // Check for NULL
            if (filterClickListener != null) {
                filterClickListener.onResetFilterClick(appliedFilters);
            }

            // Dismiss Dialog
            alertDialog.dismiss();

            // Pop Out
            popBackStack(getFragmentManager());
        }

        @Override
        public void onNegativeButtonClick(AlertDialog alertDialog) {
            // Dismiss Dialog
            alertDialog.dismiss();
        }
    }


    @Override
    public void handleNavigation() {
        super.handleNavigation();

        //track event for close filter
//        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
//        trackEventForCountlyAndGA(getString(R.string.countly_listing),
//                getString(R.string.d_filter_close_click),getString(R.string.d_filter_close_click),hMap);

    }
}
