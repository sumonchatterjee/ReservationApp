package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.dineout.recycleradapters.view.holder.RateNReviewHeaderHolder;
import com.dineout.recycleradapters.view.holder.RateNReviewHolderWrapper;
import com.dineout.recycleradapters.view.holder.RestaurantNotVisitedItemHolder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewRestaurantNotVisitedReasonsAdapter extends BaseRecyclerAdapter {
    private static final int TYPE_REASON_LAYOUT = 0;

    private Context mContext;
    private LayoutInflater mInflater;
    private AdapterCallback mAdapterCallback;

    public RateNReviewRestaurantNotVisitedReasonsAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public interface AdapterCallback {
        void itemClick(JSONObject object);
    }

    @Override
    protected int defineItemViewType(int position) {
        return TYPE_REASON_LAYOUT;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        View view;
        RateNReviewHolderWrapper holderWrapper;
        switch (viewType) {
            case TYPE_REASON_LAYOUT:
                view = mInflater.inflate(R.layout.restaurant_not_visisted_item_layout, parent, false);
                holderWrapper = new RestaurantNotVisitedItemHolder(view);
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

    public void setAdapterCallback(AdapterCallback callback) {
        this.mAdapterCallback = callback;
    }

    public void itemClick(JSONObject object) {
        if (mAdapterCallback != null) {
            mAdapterCallback.itemClick(object);
        }
    }
}
