package com.dineout.recycleradapters.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.json.JSONObject;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public abstract class RateNReviewHolderWrapper extends RecyclerView.ViewHolder {
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapterInstance;
    private JSONObject mDataObject;

    public RateNReviewHolderWrapper(View itemView) {
        super(itemView);
    }

    public void bindData(JSONObject object, int position) {
        this.mDataObject = object;
    }

    public JSONObject getDataObject() {
        return mDataObject;
    }

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapterInstance() {
        return mAdapterInstance;
    }

    public void setAdapterInstance(RecyclerView.Adapter<RecyclerView.ViewHolder> adapterInstance) {
        this.mAdapterInstance = adapterInstance;
    }
}
