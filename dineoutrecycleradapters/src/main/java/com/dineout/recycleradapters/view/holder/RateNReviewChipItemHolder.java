package com.dineout.recycleradapters.view.holder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.dineout.recycleradapters.R;
import com.dineout.recycleradapters.RateNReviewChipsAdapter;

import org.json.JSONObject;

/**
 * Created by sawai.parihar on 10/03/17.
 */

public class RateNReviewChipItemHolder extends RateNReviewHolderWrapper implements View.OnClickListener {
    private Context mContext;
    private ViewGroup mWrapperLayout;
    private TextView mItemTv;

    public RateNReviewChipItemHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        mWrapperLayout = (ViewGroup) itemView;
        mItemTv = (TextView) itemView.findViewById(R.id.chip_tv);
    }

    public void bindData(JSONObject object, int position) {
        super.bindData(object, position);

        mItemTv.setText(object.optString("value"));
        boolean status = "1".equals(object.optString("status"))
                || "true".equals(object.optString("status"));
        setTagState(!status);
        mWrapperLayout.setOnClickListener(this);

        // start animation
//        setFadeAnimation(mWrapperLayout);
    }

    private void setTagState(boolean isSelected) {
        mWrapperLayout.setSelected(!isSelected);
        mItemTv.setSelected(!isSelected);
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();

        if (vId == R.id.chip_layout_wrapper) {
            boolean isSelected = mWrapperLayout.isSelected();
            setTagState(isSelected);

            try {
                getDataObject().put("status", !isSelected);
            } catch (Exception e) {
                // Exception
            }

            // tracking
            if (!isSelected) {
                ((RateNReviewChipsAdapter) getAdapterInstance()).tagSelectionCallback(getDataObject().optString("value"));
            }
        }
    }

    private void setFadeAnimation(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.rate_n_review_tag_chip);
        view.startAnimation(myAnim);
    }
}
