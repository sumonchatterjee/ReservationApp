package com.dineout.book.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.FilterCheckBoxOptionRecyclerAdapter;
import com.dineout.recycleradapters.FilterDeselectableOptionRecyclerAdapter;
import com.dineout.recycleradapters.FilterOptionRecyclerAdapter;
import com.dineout.recycleradapters.FilterRadioOptionRecyclerAdpater;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.R.id.recyclerView_filter_option;

public class FilterOptionsPanelFragment extends MasterDOFragment
        implements TextWatcher, View.OnClickListener,
        FilterOptionRecyclerAdapter.FilterOptionListener {

    private final String FILTER_TYPE_CHECKBOX = "CHECKBOX";
    private final String FILTER_TYPE_DESELECTABLE_RADIO = "DESELECTABLE_RADIO";
    private final String FILTER_TYPE_RADIO = "RADIO";

    private JSONObject filterCategoryJsonObject;
    private FilterOptionPanelListener filterOptionPanelListener;

    private FilterCheckBoxOptionRecyclerAdapter filterCheckBoxOptionRecyclerAdapter;
    private FilterDeselectableOptionRecyclerAdapter filterDeselectableOptionRecyclerAdapter;
    private FilterRadioOptionRecyclerAdpater filterRadioOptionRecyclerAdpater;

    private RecyclerView recyclerViewFilterOption;
    private EditText editTextFilterOptionSearch;
    private View textViewNoResults;

    public void setFilterCategoryJsonObject(JSONObject filterCategoryJsonObject) {
        this.filterCategoryJsonObject = filterCategoryJsonObject;
    }

    public void setFilterOptionPanelListener(FilterOptionPanelListener filterOptionPanelListener) {
        this.filterOptionPanelListener = filterOptionPanelListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_options_panel, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get Filter Option Search
        editTextFilterOptionSearch = (EditText) getView().findViewById(R.id.editText_filter_option_search);
        editTextFilterOptionSearch.addTextChangedListener(this);

        // Get Search Remove button
        getView().findViewById(R.id.imageView_search_remove).setOnClickListener(this);

        // Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Get Filter Option List
        recyclerViewFilterOption = (RecyclerView) getView().findViewById(recyclerView_filter_option);
        recyclerViewFilterOption.setLayoutManager(linearLayoutManager);

        // Get No Results
        textViewNoResults = getView().findViewById(R.id.textView_no_results);

        // Instantiate Checkbox Adapter
        filterCheckBoxOptionRecyclerAdapter =
                new FilterCheckBoxOptionRecyclerAdapter(getActivity());

        // Set Option Click Listener
        filterCheckBoxOptionRecyclerAdapter.setOptionRecyclerListener(this);

        // Instantiate Deselectable Radio Adapter
        filterDeselectableOptionRecyclerAdapter =
                new FilterDeselectableOptionRecyclerAdapter(getActivity());

        // Set Option Click Listener
        filterDeselectableOptionRecyclerAdapter.setOptionRecyclerListener(this);

        // Instantiate Radio Adapter
        filterRadioOptionRecyclerAdpater =
                new FilterRadioOptionRecyclerAdpater(getActivity());

        // Set Option Click Listener
        filterRadioOptionRecyclerAdpater.setOptionRecyclerListener(this);
    }

    private void hideKeyboard() {
        if (getActivity() != null)
            AppUtil.hideKeyboard(getActivity());
    }

    public void renderFilterOptionPanel() {
        // Hide Keyboard
        hideKeyboard();

        // Check for NULL
        if (filterCategoryJsonObject != null) {
            boolean hasOptionsData = true;

            // Set Search Option Section visibility
            getView().findViewById(R.id.relativeLayout_search_option_section).setVisibility(isSearchVisible() ? View.VISIBLE : View.GONE);

            // Get Filter Type
            String filterType = filterCategoryJsonObject.optString("type", "");

            // Get Filter Option
            JSONArray filterOptionJsonArray = filterCategoryJsonObject.optJSONArray("arr");

            if (!AppUtil.isStringEmpty(filterType)) {
                // Check for Filter Type
                if (filterType.equalsIgnoreCase(FILTER_TYPE_CHECKBOX)) {
                    handleCheckBoxTypeFilter(filterOptionJsonArray);

                } else if (filterType.equalsIgnoreCase(FILTER_TYPE_DESELECTABLE_RADIO)) {
                    handleDeselectableRadioTypeFilter(filterOptionJsonArray);

                } else if (filterType.equalsIgnoreCase(FILTER_TYPE_RADIO)) {
                    handleRadioTypeFilter(filterOptionJsonArray);
                }
            }

            if (filterOptionJsonArray == null ||
                    filterOptionJsonArray.length() == 0) {

                hasOptionsData = false;

                // Show no Results
                textViewNoResults.setVisibility(View.VISIBLE);
            }

            // Set Matched Filter Options
            if (hasOptionsData) {
                if (isSearchVisible()) {

                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    trackEventForCountlyAndGA(getString(R.string.countly_listing),
                            "Filter" + filterCategoryJsonObject.optString("key", "") + "Click",
                            editTextFilterOptionSearch.getText().toString(), hMap);
                    handleSearchTextType(editTextFilterOptionSearch.getText().toString());
                } else {
                    // Hide no Results
                    textViewNoResults.setVisibility(View.GONE);
                }
            }
        }
    }

    private boolean isSearchVisible() {
        // Check for NULL
        if (filterCategoryJsonObject != null) {
            return filterCategoryJsonObject.optBoolean("is_searchable", false);
        }

        return false;
    }

    private void handleCheckBoxTypeFilter(JSONArray filterOptionJsonArray) {
        // Set Data in Adapter
        filterCheckBoxOptionRecyclerAdapter.setListJsonArray(filterOptionJsonArray);

        // Set Adapter in Recycler
        recyclerViewFilterOption.setAdapter(filterCheckBoxOptionRecyclerAdapter);
    }

    private void handleDeselectableRadioTypeFilter(JSONArray filterOptionJsonArray) {
        // Set Data in Adapter
        filterDeselectableOptionRecyclerAdapter.setListJsonArray(filterOptionJsonArray);

        // Set Adapter in Recycler
        recyclerViewFilterOption.setAdapter(filterDeselectableOptionRecyclerAdapter);
    }

    private void handleRadioTypeFilter(JSONArray filterOptionJsonArray) {
        // Set Data in Adapter
        filterRadioOptionRecyclerAdpater.setJsonArray(filterOptionJsonArray);

        // Set Adapter in Recycler
        recyclerViewFilterOption.setAdapter(filterRadioOptionRecyclerAdpater);
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.imageView_search_remove) {
            handleSearchRemoveClick();
        }
    }

    private void handleSearchRemoveClick() {
        if (editTextFilterOptionSearch != null) {
            editTextFilterOptionSearch.setText("");
        }

        // Hide Keyboard
        hideKeyboard();
    }

    /**
     * Search Section - Text Watcher - STARTS
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do Nothing...
    }

    @Override
    public void onTextChanged(CharSequence sequence, int start, int before, int count) {
        handleSearchTextType(sequence.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Do Nothing...
    }
    /**
     * Search Section - Text Watcher - ENDS
     */

    private void handleSearchTextType(String searchText) {
        RecyclerView.Adapter adapter = recyclerViewFilterOption.getAdapter();

        // Show Loader
        showLoader();

        if (adapter instanceof FilterOptionRecyclerAdapter) {
            FilterOptionRecyclerAdapter recyclerAdapter = ((FilterOptionRecyclerAdapter) adapter);
            recyclerAdapter.showMatchedFilterOptions(searchText);

            // Show/Hide no Results
            textViewNoResults.setVisibility((recyclerAdapter.getJsonArray().length() == 0) ? View.VISIBLE : View.GONE);
        }

        // Hide Loader
        hideLoader();
    }

    @Override
    public void onFilterOptionClick(JSONObject optionJsonObject) {
        // Hide Keyboard
        hideKeyboard();
        // Check for NULL / Empty
        if (optionJsonObject != null && filterCategoryJsonObject != null) {

            //track events for countly, ga, qgraph, apsalar
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

            HashMap<String, Object> props = new HashMap<>();
            props.put("label",filterCategoryJsonObject.optString("name", "") + "_"+optionJsonObject.optString("name", ""));

            if(optionJsonObject.optString("applied").equalsIgnoreCase("1")){
                trackEventForCountlyAndGA(getString(R.string.countly_listing), "FilterSelectClick", filterCategoryJsonObject.optString("name", "") + "_"+optionJsonObject.optString("name", ""), hMap);
                trackEventQGraphApsalar("FilterSelectClick",props,true,false,false);

            }else{
                trackEventForCountlyAndGA(getString(R.string.countly_listing), "FilterUnSelectClick", filterCategoryJsonObject.optString("name", "") + "_"+optionJsonObject.optString("name", ""), hMap);
                trackEventQGraphApsalar("FilterUnSelectClick",props,true,false,false);

            }


            // Get Category Key
            String categoryKey = optionJsonObject.optString("key", "");
            categoryKey = ((AppUtil.isStringEmpty(categoryKey)) ?
                    (filterCategoryJsonObject.optString("key", "")) : categoryKey);

            // Get Option Name
            String optionName = optionJsonObject.optString("name", "");

            // Get Applied Flag
            int applied = optionJsonObject.optInt("applied", 0);

            // Check for NULL
            if (filterOptionPanelListener != null) {
                filterOptionPanelListener.
                        onFilterOptionClick(categoryKey, optionName, applied);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        filterCategoryJsonObject = null;
        filterOptionPanelListener = null;

        recyclerViewFilterOption = null;
        editTextFilterOptionSearch = null;
        textViewNoResults = null;

        filterCheckBoxOptionRecyclerAdapter = null;
        filterDeselectableOptionRecyclerAdapter = null;
        filterRadioOptionRecyclerAdpater = null;
    }

    public interface FilterOptionPanelListener {
        void onFilterOptionClick(String categoryKey, String optionName, int applied);
    }
}
