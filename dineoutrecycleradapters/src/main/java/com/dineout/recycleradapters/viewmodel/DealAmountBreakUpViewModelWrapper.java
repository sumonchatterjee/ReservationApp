package com.dineout.recycleradapters.viewmodel;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.json.JSONObject;

/**
 * Created by sawai on 10/01/17.
 */

public abstract class DealAmountBreakUpViewModelWrapper extends RecyclerView.ViewHolder {

    public DealAmountBreakUpViewModelWrapper(View itemView) {
        super(itemView);
    }

    public abstract void bind(int position, JSONObject data);
}
