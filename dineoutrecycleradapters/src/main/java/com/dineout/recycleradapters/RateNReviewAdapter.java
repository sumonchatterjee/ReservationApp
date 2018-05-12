package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.dineout.recycleradapters.view.holder.RateNReviewChipsHolder;
import com.dineout.recycleradapters.view.holder.RateNReviewHeaderHolder;
import com.dineout.recycleradapters.view.holder.RateNReviewHolderWrapper;
import com.dineout.recycleradapters.view.holder.RateNReviewRatingHolder;
import com.dineout.recycleradapters.view.holder.RateNReviewTapToWriteHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.dineout.recycleradapters.util.RateNReviewUtil.HEADER_LAYOUT;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RATING_LAYOUT;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_CHIPS_LAYOUT;
import static com.dineout.recycleradapters.util.RateNReviewUtil.TAP_TO_WRITE_REVIEW_LAYOUT;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewAdapter extends BaseRecyclerAdapter {
    public static final int TYPE_HEADER_LAYOUT = 0;
    public static final int TYPE_RATING_LAYOUT = 1;
    public static final int TYPE_REVIEW_CHIPS_LAYOUT = 2;
    public static final int TYPE_TAP_TO_WRITE_REVIEW_LAYOUT = 3;

    private Context mContext;
    private LayoutInflater mInflater;
    private JSONArray mMainData;
    private RateNReviewAdapterCallback mRateNReviewAdapterCallback;
    private TrackingCallback mTrackingCallback;

    public interface RateNReviewAdapterCallback {
        void setSubmitButtonVisibility(boolean isVisible);
        void setSubmitButtonText();
    }

    public interface TrackingCallback {
        void tagSelectionCallback(String tagName);
    }

    public RateNReviewAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mMainData = new JSONArray();
    }

    @Override
    protected int defineItemViewType(int position) {
        int returnValue = -1;

        if (getJsonArray() != null) {
            switch (getJsonArray().optJSONObject(position).optString(RateNReviewUtil.TYPE)) {
                case HEADER_LAYOUT:
                    returnValue = TYPE_HEADER_LAYOUT;
                    break;

                case RATING_LAYOUT:
                    returnValue = TYPE_RATING_LAYOUT;
                    break;

                case REVIEW_CHIPS_LAYOUT:
                    returnValue = TYPE_REVIEW_CHIPS_LAYOUT;
                    break;

                case TAP_TO_WRITE_REVIEW_LAYOUT:
                    returnValue = TYPE_TAP_TO_WRITE_REVIEW_LAYOUT;
                    break;
            }

            return returnValue;
        }

        return position;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        View view;
        RateNReviewHolderWrapper holderWrapper;
        switch (viewType) {
            case TYPE_HEADER_LAYOUT:
                view = mInflater.inflate(R.layout.rate_n_review_header_layout, parent, false);
                holderWrapper = new RateNReviewHeaderHolder(view);
                break;

            case TYPE_RATING_LAYOUT:
                view = mInflater.inflate(R.layout.rate_n_review_rating_layout, parent, false);
                holderWrapper = new RateNReviewRatingHolder(view);
                break;

            case TYPE_REVIEW_CHIPS_LAYOUT:
                view = mInflater.inflate(R.layout.rate_n_review_chips_layout, parent, false);
                holderWrapper = new RateNReviewChipsHolder(view);
                break;

            case TYPE_TAP_TO_WRITE_REVIEW_LAYOUT:
                view = mInflater.inflate(R.layout.rate_n_review_tap_to_write_layout, parent, false);
                holderWrapper = new RateNReviewTapToWriteHolder(view);
                break;

            default:
                holderWrapper = null;
        }

        return holderWrapper ;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder != null && holder instanceof RateNReviewHolderWrapper) {
            ((RateNReviewHolderWrapper) holder).setAdapterInstance(this);
            ((RateNReviewHolderWrapper) holder).bindData(listItem, position);
        }
    }

    public void initMainData() {
        RateNReviewUtil.initMainData(mMainData, getJsonArray());
    }

    public int getCurrentRatingValue() {
        return RateNReviewUtil.getCurrentRatingValue(getJsonArray());
    }

    public void actionAtZeroRating() {
        JSONArray array = RateNReviewUtil.getFilteredArrayAtZeroRating(mMainData);
        setJsonArray(array);

        // disabled submit rating button
        mRateNReviewAdapterCallback.setSubmitButtonVisibility(false);

        // set submit button text
        mRateNReviewAdapterCallback.setSubmitButtonText();
    }

    public void actionAtNonZeroRating() {
        JSONArray array = RateNReviewUtil.getFilteredArrayAtNonZeroRating(mContext, mMainData);
        setJsonArray(array);

        // enabled submit rating button
        mRateNReviewAdapterCallback.setSubmitButtonVisibility(true);

        // set submit button text
        mRateNReviewAdapterCallback.setSubmitButtonText();
    }

    public void setRateNReviewAdapterCallback(RateNReviewAdapterCallback rateNReviewAdapterCallback) {
        this.mRateNReviewAdapterCallback = rateNReviewAdapterCallback;
    }

    public void tagSelectionCallback(String tagName) {
        if (mTrackingCallback != null) {
            mTrackingCallback.tagSelectionCallback(tagName);
        }
    }

    public void setTrackingCallback(TrackingCallback trackingCallback) {
        this.mTrackingCallback = trackingCallback;
    }
}
