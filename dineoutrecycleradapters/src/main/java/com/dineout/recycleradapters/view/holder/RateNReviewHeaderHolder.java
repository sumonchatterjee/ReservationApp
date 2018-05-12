package com.dineout.recycleradapters.view.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;

import org.json.JSONObject;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewHeaderHolder extends RateNReviewHolderWrapper {
    private Context mContext;
    private TextView mHeaderTv;

    public RateNReviewHeaderHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        mHeaderTv = (TextView) itemView.findViewById(R.id.header_tv);
    }

    public void bindData(JSONObject object, int position) {
        super.bindData(object, position);

        mHeaderTv.setText(object.optString("text"));
    }
}
