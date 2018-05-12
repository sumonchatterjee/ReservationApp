package com.dineout.book.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.recycleradapters.SortOptionsRecyclerAdapter;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class SortOptionsFragment extends MasterDOJSONReqFragment
        implements SortOptionsRecyclerAdapter.SortOptionHandler {

    public JSONObject sortJsonObject;
    private SortOptionClickListener sortOptionClickListener;

    private View linearLayoutSortFetching;
    private View linearLayoutSortSection;
    private SortOptionsRecyclerAdapter sortOptionsRecyclerAdapter;

    public void setSortJsonObject(JSONObject sortJsonObject) {
        this.sortJsonObject = sortJsonObject;
    }

    public void setSortOptionClickListener(SortOptionClickListener sortOptionClickListener) {
        this.sortOptionClickListener = sortOptionClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sort_options_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Remove Title
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize View
        initializeView();

        // Initialize Data
        initializeData();

        setCancelable(true);
    }

    private void initializeView() {
        // Get Progress Section
        linearLayoutSortFetching = getView().findViewById(R.id.linearLayout_sort_fetching);

        // Get Sort Section
        linearLayoutSortSection = getView().findViewById(R.id.linearLayout_sort_section);

        // Get Progress Bar
        ProgressBar progressBarSort = (ProgressBar) getView().findViewById(R.id.progressBar_sort);
        com.dineout.recycleradapters.util.AppUtil.setProgressBarDecor(getContext(), progressBarSort);

        // Get Recycler View
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        RecyclerView recyclerViewSort = (RecyclerView) getView().findViewById(R.id.recyclerView_sort);
        recyclerViewSort.setLayoutManager(linearLayoutManager);

        // Initialize Adapter
        sortOptionsRecyclerAdapter = new SortOptionsRecyclerAdapter(getContext());
        sortOptionsRecyclerAdapter.setSortOptionHandler(this);

        // Set Adapter in Recycler View
        recyclerViewSort.setAdapter(sortOptionsRecyclerAdapter);
    }

    private void showSortLoader() {
        linearLayoutSortFetching.setVisibility(View.VISIBLE);
        linearLayoutSortSection.setVisibility(View.GONE);
    }

    private void hideSortLoader() {
        linearLayoutSortFetching.setVisibility(View.GONE);
        linearLayoutSortSection.setVisibility(View.VISIBLE);
    }

    private void initializeData() {
        // Show Loader
        showSortLoader();

        // Check if Sort Data is available
        if (sortJsonObject != null) {
            handleSuccess(sortJsonObject);

        } else {
            handleError("");
        }

        //Hide Loader
        hideSortLoader();
    }

    private void handleSuccess(JSONObject sortJsonObject) {
        // Get Title
        String title = sortJsonObject.optString("name", "");
        if (!AppUtil.isStringEmpty(title)) {
            TextView textViewSortTitle = (TextView) getView().findViewById(R.id.textView_sort_title);
            textViewSortTitle.setText(title);
        }

        // Prepare Sort Options
        prepareSortOptions(sortJsonObject);
    }

    private void prepareSortOptions(JSONObject sortJsonObject) {
        // Get Sort Array
        JSONArray sortJsonArray = sortJsonObject.optJSONArray("arr");

        if (sortJsonArray != null && sortJsonArray.length() > 0) {
            // Set Data
            sortOptionsRecyclerAdapter.setJsonArray(sortJsonArray);
            sortOptionsRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void handleError(String errorMessage) {
        // Show Error Message
        UiUtil.showToastMessage(getContext(),
                ((AppUtil.isStringEmpty(errorMessage)) ?
                        getString(R.string.text_general_error_message) : errorMessage));

        dismissAllowingStateLoss();
    }

    @Override
    public void onSortOptionClick(JSONObject listItem) {

        //track event
        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_listing),getString(R.string.d_sort_option_click),
                  listItem.optString("name"),hMap);

        // Dismiss Dialog
        dismissAllowingStateLoss();

        // Check for NULL
        if (sortOptionClickListener != null) {
            sortOptionClickListener.onSortOptionClick(listItem);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        linearLayoutSortFetching = null;
        linearLayoutSortSection = null;
        sortOptionsRecyclerAdapter = null;
        sortJsonObject = null;
        sortOptionClickListener = null;
    }

    public interface SortOptionClickListener {
        void onSortOptionClick(JSONObject sortOptionJsonObject);
    }
}
