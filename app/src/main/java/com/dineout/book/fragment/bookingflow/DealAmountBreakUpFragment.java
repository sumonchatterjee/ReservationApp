package com.dineout.book.fragment.bookingflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.DealAmountBreakUpAdapter;

import org.json.JSONArray;


public class DealAmountBreakUpFragment extends MasterDOFragment implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private DealAmountBreakUpAdapter mAdapter;
    private JSONArray mArray;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deal_amount_break_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_deal_break_up);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        mAdapter = new DealAmountBreakUpAdapter(getActivity());
        mAdapter.setJsonArray(mArray);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareToolbar();
    }

    private void prepareToolbar() {
            // Get Back
            if (getView() != null) {
                ImageView imageViewAllOfferBack = (ImageView) getView().findViewById(R.id.imageView_all_offer_back);
                TextView textViewTitle = (TextView) getView().findViewById(R.id.textView_all_offer_title);
                TextView textViewSubTitle = (TextView) getView().findViewById(R.id.textView_all_offer_sub_title);

                imageViewAllOfferBack.setImageResource(R.drawable.ic_cross_icon);
                imageViewAllOfferBack.setOnClickListener(this);

                if (getArguments() != null) {
                    String title = getArguments().getString("title");
                    String subTitle = getArguments().getString("subtitle");

                    textViewTitle.setText(title);
                    textViewSubTitle.setText(subTitle);
                }
            }
    }

    public void setData(JSONArray array) {
        this.mArray = array;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_all_offer_back:
                handleNavigation();
                break;
        }
    }
}
