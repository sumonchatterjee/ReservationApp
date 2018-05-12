package com.dineout.recycleradapters.viewmodel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.recycleradapters.BaseRecyclerAdapter;
import com.dineout.recycleradapters.R;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.view.widgets.RoundedImageView;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONObject;

/**
 * Created by sawai.parihar on 17/03/17.
 */

public class ReviewsIconsAdapter extends BaseRecyclerAdapter {
    public static final int TYPE_AUTHOR_ICON_ITEM_LAYOUT = 0;
    public static final int TYPE_AUTHOR_ICON_LAST_ITEM_LAYOUT = 1;

    private Context mContext;
    private LayoutInflater mInflater;

    public ReviewsIconsAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }


    @Override
    protected int defineItemViewType(int position) {
        int returnValue = -1;

        if (getJsonArray() != null && getJsonArray().optJSONObject(position) != null) {
            returnValue = getJsonArray().optJSONObject(position).optInt("type");
        }

        return returnValue;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holderWrapper;
        switch (viewType) {
            case TYPE_AUTHOR_ICON_ITEM_LAYOUT:
                view = mInflater.inflate(R.layout.reviews_icon_layout, parent, false);
                holderWrapper = new ReviewsIconHolder(view);
                break;

            case TYPE_AUTHOR_ICON_LAST_ITEM_LAYOUT:
                view = mInflater.inflate(R.layout.reviews_icon_text_layout, parent, false);
                holderWrapper = new ReviewsIconTextHolder(view);
                break;

            default:
                holderWrapper = null;
        }

        return holderWrapper;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_AUTHOR_ICON_ITEM_LAYOUT:

                // add margin
                int leftMarginValue;
                if (position != 0) {
                    leftMarginValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, mContext.getResources().getDisplayMetrics());
                } else {
                    leftMarginValue = 0;
                }
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) ((ReviewsIconHolder) holder).wrapper.getLayoutParams();
                params.setMargins(-leftMarginValue, 0, 0, 0);
                ((ReviewsIconHolder) holder).wrapper.setLayoutParams(params);


                ((ReviewsIconHolder) holder).iconIv.
                        setDefaultImageResId(R.drawable.img_profile_nav_default);
                String url = listItem.optString("imageUrl");
                if (!AppUtil.isStringEmpty(url)) {
                    ((ReviewsIconHolder) holder).iconIv.setImageUrl(url,
                            ImageRequestManager.getInstance(mContext).getImageLoader());
                }

                break;

            case TYPE_AUTHOR_ICON_LAST_ITEM_LAYOUT:

                int count = listItem.optInt("reviewCount");
                ((ReviewsIconTextHolder) holder).tv.setText(count + "+");
                break;
        }
    }

    private class ReviewsIconHolder extends RecyclerView.ViewHolder {
        RelativeLayout wrapper;
        RoundedImageView iconIv;

        ReviewsIconHolder(View itemView) {
            super(itemView);

            wrapper = (RelativeLayout) itemView.findViewById(R.id.wrapper_layout);
            iconIv = (RoundedImageView) itemView.findViewById(R.id.imageView_review_author_image);
        }
    }

    private class ReviewsIconTextHolder extends RecyclerView.ViewHolder {
        TextView tv;

        ReviewsIconTextHolder(View itemView) {
            super(itemView);

            tv = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
