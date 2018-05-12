package com.dineout.recycleradapters.view.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.dineout.recycleradapters.R;
import com.dineout.recycleradapters.RateNReviewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_RATING;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewRatingHolder extends RateNReviewHolderWrapper implements RatingBar.OnRatingBarChangeListener {
    private Context mContext;
    private RatingBar mRatingBar;
    private TextView mRatingBarTV;
    private ImageView mRatingBarLogoIv;

    private List<Integer> mRatingIconList;

    public RateNReviewRatingHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        mRatingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
        mRatingBarTV = (TextView) itemView.findViewById(R.id.rating_text_tv);
        mRatingBarLogoIv = (ImageView) itemView.findViewById(R.id.rating_logo_iv);

        mRatingIconList = new ArrayList<>(Arrays.asList(R.drawable.rating_user_icon_0,
                R.drawable.rating_user_icon_1, R.drawable.rating_user_icon_2,
                R.drawable.rating_user_icon_3, R.drawable.rating_user_icon_4,
                R.drawable.rating_user_icon_5));
    }

    public void bindData(JSONObject object, int position) {
        super.bindData(object, position);

        try {
            if (object != null) {
                // set rating
                mRatingBar.setRating(object.optInt(REVIEW_RATING, 0));

                mRatingBar.setOnRatingBarChangeListener(this);

                // get current rating
                int ratingValue = ((RateNReviewAdapter) getAdapterInstance()).getCurrentRatingValue();

                // set current review user icon
                mRatingBarLogoIv.setImageResource(mRatingIconList.get(ratingValue));

                // set review text
                JSONArray ratingReviewArr = object.optJSONArray("ratingReviewArr");
                if (ratingValue == 0) {
                    mRatingBarTV.setVisibility(View.GONE);
                } else {
                    mRatingBarTV.setVisibility(View.VISIBLE);

                    if (ratingReviewArr.length() >= ratingValue) {
                        mRatingBarTV.setText(ratingReviewArr.getString(ratingValue - 1));
                    }
                }


            }
        } catch (Exception e) {
            // Exception
        }
    }

    private void onRatingChangeActions(int ratingValue) {
        JSONObject obj = getDataObject();
        if (obj != null) {
            try {
                obj.put(REVIEW_RATING, ratingValue);

                if (ratingValue == 0) {
                    ((RateNReviewAdapter) getAdapterInstance()).actionAtZeroRating();
                } else {
                    ((RateNReviewAdapter) getAdapterInstance()).actionAtNonZeroRating();
                }

            } catch (JSONException e) {}

            getAdapterInstance().notifyDataSetChanged();
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        onRatingChangeActions((int) rating);
    }
}
