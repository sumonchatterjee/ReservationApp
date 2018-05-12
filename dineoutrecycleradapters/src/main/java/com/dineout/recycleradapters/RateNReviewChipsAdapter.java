package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.recycleradapters.view.holder.RateNReviewChipItemHolder;
import com.dineout.recycleradapters.view.holder.RateNReviewHolderWrapper;

import org.json.JSONObject;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewChipsAdapter extends BaseRecyclerAdapter {
    public static final int TYPE_CHIP_ITEM_LAYOUT = 0;

    private Context mContext;
    private LayoutInflater mInflater;
    private TrackingCallback mTrackingCallback;

    public RateNReviewChipsAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public interface TrackingCallback {
        void tagSelectionCallback(String tagName);
    }

    @Override
    protected int defineItemViewType(int position) {
        return TYPE_CHIP_ITEM_LAYOUT;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        View view;
        RateNReviewHolderWrapper holderWrapper;
        switch (viewType) {
            case TYPE_CHIP_ITEM_LAYOUT:
                view = mInflater.inflate(R.layout.rate_n_review_chips_item_layout, parent, false);
                holderWrapper = new RateNReviewChipItemHolder(view);
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

    public void tagSelectionCallback(String tagName) {
        if (mTrackingCallback != null) {
            mTrackingCallback.tagSelectionCallback(tagName);
        }
    }

    public void setTrackingCallback(TrackingCallback trackingCallback) {
        this.mTrackingCallback = trackingCallback;
    }
}
