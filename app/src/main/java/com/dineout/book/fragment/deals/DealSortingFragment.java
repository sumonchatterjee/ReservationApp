package com.dineout.book.fragment.deals;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.DealSortByAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DealSortingFragment extends MasterDOFragment implements View.OnClickListener {
    public static final String SORT_BY = "sortby";
    public static final String SORT_BY_DISPLAY_NAME = "sortbydisplay";
    RecyclerView sortRecyclerView;
    DealSortByAdapter sortAdapter;
    JSONArray sortOptions;



    public static DealSortingFragment getInstance(JSONArray sortOptions) {
        DealSortingFragment sortFragment = new DealSortingFragment();
        sortFragment.sortOptions = sortOptions;
        return sortFragment;
    }



    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null)
            getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public void onRemoveErrorView() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trackScreenToGA(getString(R.string.ga_screen_deal_sort));

        setCancelable(true);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);


        sortAdapter = new DealSortByAdapter(getContext(), this, getImageLoader());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sort_by, container, true);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sortRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view_deals);
        RecyclerView.LayoutManager mManager = new LinearLayoutManager(getContext());
        sortRecyclerView.setLayoutManager(mManager);
        sortRecyclerView.setAdapter(sortAdapter);

        sortAdapter.setSortResult(sortOptions);
        (getView().findViewById(R.id.main_sort_layout)).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.main_sort_layout ){
            dismiss();
        }else {

            String sortBy = (String) v.getTag();
            setTrackEvent(sortBy);
           // trackEventGA(getString(R.string.ga_screen_deal_sort), sortBy, null);

            Intent n = new Intent();
            n.putExtra(SORT_BY, sortBy);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, n);
            dismiss();
        }

    }


    private void setTrackEvent(String sortBy) {
        try {
            if (sortOptions != null) {
                if (sortOptions.length() > 0) {
                    for (int i = 0; i < sortOptions.length(); i++) {
                        JSONObject obj = sortOptions.getJSONObject(i);
                        if (obj.optString("type").equalsIgnoreCase(sortBy)) {
                            trackEventGA(getString(R.string.ga_screen_deal_sort), obj.optString("displayType"), null);

                        }
                    }
                }
            }

        }catch(JSONException ex){

        }
    }
}
