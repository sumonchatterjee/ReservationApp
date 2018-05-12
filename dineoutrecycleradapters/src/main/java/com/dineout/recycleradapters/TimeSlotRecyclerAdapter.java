package com.dineout.recycleradapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.json.JSONObject;

public class TimeSlotRecyclerAdapter extends BaseRecyclerAdapter {
    @Override
    protected int defineItemViewType(int position) {
        return 0;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {

    }
}
