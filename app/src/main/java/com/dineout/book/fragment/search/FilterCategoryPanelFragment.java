package com.dineout.book.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.FilterCategoryRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class FilterCategoryPanelFragment extends MasterDOFragment
        implements FilterCategoryRecyclerAdapter.FilterCategoryClickListener {

    private JSONArray filterJsonArray;

    private FilterCategoryHandler filterCategoryHandler;
    private FilterCategoryRecyclerAdapter recyclerAdapter;

    public void setFilterJsonArray(JSONArray filterJsonArray) {
        this.filterJsonArray = filterJsonArray;
    }

    public void setFilterCategoryHandler(FilterCategoryHandler filterCategoryHandler) {
        this.filterCategoryHandler = filterCategoryHandler;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_category_panel, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Get List Adapter
        recyclerAdapter = new FilterCategoryRecyclerAdapter(getActivity());
        recyclerAdapter.setFilterCategoryClickListener(this);

        // Get List Instance
        RecyclerView recyclerViewFilterCategory = (RecyclerView) getView().findViewById(R.id.recyclerView_filter_category);
        recyclerViewFilterCategory.setLayoutManager(linearLayoutManager);
        recyclerViewFilterCategory.setAdapter(recyclerAdapter);
        recyclerAdapter.setJsonArray(filterJsonArray);
    }

    @Override
    public void onFilterCategoryClick(JSONObject filterCategoryJsonObject) {
        // Check for NULL
        if (filterCategoryHandler != null) {
            filterCategoryHandler.onFilterCategoryClick(filterCategoryJsonObject);
        }
    }

    public void updateFilterCategory() {
        // Check for NULL
        if (recyclerAdapter != null) {
            recyclerAdapter.setJsonArray(filterJsonArray);
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        filterJsonArray = null;
        recyclerAdapter = null;
        filterCategoryHandler = null;
    }

    public interface FilterCategoryHandler {
        void onFilterCategoryClick(JSONObject filterCategoryJsonObject);
    }
}
