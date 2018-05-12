package com.dineout.recycleradapters.view.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;
import com.dineout.recycleradapters.RateNReviewAdapter;
import com.dineout.recycleradapters.RateNReviewChipsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sawai.parihar on 10/03/17.
 */

public class RateNReviewChipsHolder extends RateNReviewHolderWrapper implements RateNReviewChipsAdapter.TrackingCallback {
    private Context mContext;
    private TextView mHeaderText;
    private RecyclerView mChipsRV;

    public RateNReviewChipsHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        mHeaderText = (TextView) itemView.findViewById(R.id.rating_chips_layout_tv);

        mChipsRV = (RecyclerView) itemView.findViewById(R.id.chips_rv);
    }

    public void bindData(JSONObject object, int position) {
        super.bindData(object, position);

        try {
            if (object != null) {
                int ratingValue = ((RateNReviewAdapter) getAdapterInstance()).getCurrentRatingValue();

                // set feedback text
                JSONArray ratingFeedbackArr = object.optJSONArray("ratingFeedbackArr");
                if (ratingFeedbackArr.length() >= ratingValue) {
                    mHeaderText.setText(ratingFeedbackArr.getString(ratingValue - 1));
                }

                // set tags
                JSONArray ratingTagsArr = object.optJSONArray("ratingTagsArr");
                if (ratingTagsArr.length() >= ratingValue) {
                    JSONArray tags = ratingTagsArr.optJSONArray(ratingValue - 1);

                    // create layout manager
                    StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(
                            (int) Math.ceil(tags.length() * 1.0 / 3), StaggeredGridLayoutManager.HORIZONTAL);
                    mChipsRV.setLayoutManager(lm);

                    RateNReviewChipsAdapter adapter = new RateNReviewChipsAdapter(mContext);
                    adapter.setTrackingCallback(this);
                    adapter.setJsonArray(tags);
                    mChipsRV.setAdapter(adapter);
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void tagSelectionCallback(String tagName) {
        ((RateNReviewAdapter) getAdapterInstance()).tagSelectionCallback(tagName);
    }
}
