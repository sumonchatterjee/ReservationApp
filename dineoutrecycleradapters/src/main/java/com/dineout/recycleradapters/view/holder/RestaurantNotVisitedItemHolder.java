package com.dineout.recycleradapters.view.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;
import com.dineout.recycleradapters.RateNReviewRestaurantNotVisitedReasonsAdapter;

import org.json.JSONObject;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RestaurantNotVisitedItemHolder extends RateNReviewHolderWrapper implements View.OnClickListener {
    private Context mContext;
    private TextView mReasonTv;

    public RestaurantNotVisitedItemHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        mReasonTv = (TextView) itemView.findViewById(R.id.reason_tv);
    }

    public void bindData(JSONObject object, int position) {
        super.bindData(object, position);
        mReasonTv.setText(object.optString("value"));
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ((RateNReviewRestaurantNotVisitedReasonsAdapter) getAdapterInstance()).itemClick(getDataObject());
    }
}
