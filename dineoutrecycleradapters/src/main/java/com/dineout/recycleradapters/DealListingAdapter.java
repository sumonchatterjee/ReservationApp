package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DealListingAdapter extends BaseRecyclerAdapter {

    private static final int ITEM_TYPE_DEAL_LIST = 101;
    Context mContext;
    DineoutNetworkManager networkManager;
    ImageLoader imageLoader;
    private View.OnClickListener mClickListener;


    public DealListingAdapter(Context context, View.OnClickListener clickListener) {
        mContext = context;
        mClickListener = clickListener;

    }

    public void setNetworkManager(DineoutNetworkManager networkManager,
                                  ImageLoader imageLoader) {
        this.networkManager = networkManager;
        this.imageLoader = imageLoader;
    }

    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setData(JSONArray rest_arr, int startIndex) {
        if(startIndex==0) {
            setJsonArray(rest_arr);
            notifyDataSetChanged();
        }
        else {
            if (getJsonArray() != null) {
                JSONArray consolidatedArr = new JSONArray();

                for (int i = 0; i < getJsonArray().length(); i++) {
                    consolidatedArr.put(getJsonArray().opt(i));
                }

                for (int j = 0; j < rest_arr.length(); j++) {
                    consolidatedArr.put(rest_arr.opt(j));
                }

                setJsonArray(consolidatedArr);

               //notifyDataSetChanged();
                notifyItemRangeChanged(consolidatedArr.length() - rest_arr.length()
                        , rest_arr.length());
            }
        }
    }

    @Override
    protected int defineItemViewType(int position) {
        if (getJsonArray() == null) {
            return ITEM_VIEW_TYPE_EMPTY;
        } else {
            return ITEM_TYPE_DEAL_LIST;
        }
    }


    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder.getItemViewType() == ITEM_TYPE_DEAL_LIST) {
            inflateDealList(listItem, (DealViewHolder) holder);
        }
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_DEAL_LIST) {
            return new DealViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.deal_listing_modified_layout, parent, false));
        }
        return null;
    }


    private void inflateDealList(JSONObject itemObject,
                                 DealViewHolder holder) {

        JSONObject dealData = itemObject.optJSONObject("data");

        if (dealData != null) {

            if (dealData.optBoolean("isViewMore")) {
                holder.getViewMore().setVisibility(View.VISIBLE);
            } else {
                holder.getViewMore().setVisibility(View.GONE);
            }

            holder.getImageViewRectSearchCardBackground().setDefaultImageResId(R.drawable.default_list);
            JSONArray imgArray = dealData.optJSONArray("images");
            if (imgArray != null && imgArray.length() > 0) {
                String imageUrl = imgArray.optString(0);

                holder.getImageViewRectSearchCardBackground().setImageUrl
                        (imageUrl, getImageLoader());
            } else {
                holder.getImageViewRectSearchCardBackground().setErrorImageResId(R.drawable.default_list);
            }

            //holder.getImageViewRectSearchCardBackground().setTag(dealData.optString("restaurantId"));
            holder.getImageViewRectSearchCardBackground().setTag(dealData);
            holder.getRestName().setText(dealData.optString("restaurantName"));
            holder.getLocationName().setText(dealData.optString("localityName") + ", " + dealData.optString("areaName"));

            NumberFormat formatter = new DecimalFormat("#0.00");
            if (DOPreferences.isAutoMode(mContext)) {
                if (!AppUtil.isStringEmpty(dealData.optString("distance"))) {
                    holder.getDistance().setVisibility(View.VISIBLE);
                    holder.getLocation().setVisibility(View.VISIBLE);
                    double dis = Double.parseDouble(dealData.optString("distance"));
                    holder.getDistance().setText(formatter.format(dis) + " kms");
                } else {
                    holder.getDistance().setVisibility(View.GONE);
                    holder.getLocation().setVisibility(View.GONE);
                }

            } else {
                holder.getDistance().setVisibility(View.GONE);
                holder.getLocation().setVisibility(View.GONE);
            }

            // Check if Rating is greater than 0
            boolean recency = dealData.optBoolean("recency");

            if (AppUtil.showRecency((recency) ? "1" : "0")) {
                // Show Recency
                holder.getTextViewRecencyTag().setVisibility(View.VISIBLE);

                // Hide Rating
                holder.getTextViewRestRating().setVisibility(View.GONE);

            } else {
                if (dealData.opt("avgRating") == null) {
                    // Hide Rating
                    holder.getTextViewRestRating().setVisibility(View.GONE);

                } else {
                    double avgRating = dealData.optDouble("avgRating", 0L);

                    if (AppUtil.hasNoRating(Double.toString(avgRating))) {
                        // Hide Rating
                        holder.getTextViewRestRating().setVisibility(View.GONE);

                    } else {
                        // Set Rating
                        AppUtil.setRatingValueBackground(Double.toString(avgRating), holder.getTextViewRestRating());

                        // Show Rating
                        holder.getTextViewRestRating().setVisibility(View.VISIBLE);
                    }
                }

                // Hide Recency
                holder.getTextViewRecencyTag().setVisibility(View.GONE);
            }

            holder.getDealLayout().removeAllViews();
            JSONArray dealsArr = dealData.optJSONArray("deals");

            if (dealsArr != null && dealsArr.length() > 0) {
                int dealCount = dealsArr.length();

                for (int index = 0; index < dealCount; index++) {
                    final JSONObject dealJsonObject = dealsArr.optJSONObject(index);
                    LayoutInflater layoutInflater =
                            (LayoutInflater) mContext.getSystemService(Context
                                    .LAYOUT_INFLATER_SERVICE);

                    View dealView = layoutInflater.inflate(R.layout.deal_listing_item, null, false);
                    dealView.setTag(dealData.optString("restaurantId"));


                    dealView.setOnClickListener(mClickListener);
                    RelativeLayout mainConatiner = (RelativeLayout) dealView.findViewById(R.id.main_container);
                    mainConatiner.setTag(dealData.optString("restaurantId"));
                    mainConatiner.setOnClickListener(mClickListener);


                    TextView textViewDealTitle = (TextView) dealView.findViewById(R.id.textView_deal_title);
                    if (!AppUtil.isStringEmpty(dealJsonObject.optString("title"))) {
                        textViewDealTitle.setText(dealJsonObject.optString("title"));
                    }

                    // Set Deal Validity
                    TextView textViewDealDateTime = (TextView) dealView.findViewById(R.id.textView_deal_sold_value);
                    if (!AppUtil.isStringEmpty(dealJsonObject.optString("sold"))) {
                        String s = dealJsonObject.optString("sold");
                        String s2 = s.concat(" sold");

                        textViewDealDateTime.setText(s2.toString());
                    }

                    // Set Our Price
                    TextView textViewDealCurrentPrice = (TextView) dealView.findViewById(R.id.textView_deal_current_price);
                    if (!AppUtil.isStringEmpty(dealJsonObject.optString("price"))) {
                        textViewDealCurrentPrice.setText(String.format(mContext.getString(R.string.container_rupee), dealJsonObject.optString("price")));
                        textViewDealCurrentPrice.setTextColor(mContext.getResources().getColor(R.color.green));
                    }

                    // Set Actual Price
                    TextView textViewDealActualPrice = (TextView) dealView.findViewById(R.id.textView_deal_actual_price);
                    if (!AppUtil.isStringEmpty(dealJsonObject.optString("displayPrice"))) {
                        textViewDealActualPrice.setText(AppUtil.getStrikedText(String.format(
                                mContext.getString(R.string.container_rupee), dealJsonObject.optString("displayPrice"))));
                    }

//                    TextView textViewDealStatus = (TextView) dealView.findViewById(R.id.textView_deal_date_time);
//                    if (!TextUtils.isEmpty(dealJsonObject.optString("validFor"))) {
//                        textViewDealStatus.setText("Valid for " + dealJsonObject.optString("validFor"));
//
//                    }

//                    TextView textViewDealInfo = (TextView) dealView.findViewById(R.id.textView_date_from_to);
//                    if (!AppUtil.isStringEmpty(dealJsonObject.optString("dealBought"))) {
//                        textViewDealInfo.setText(dealJsonObject.optString("dealBought"));
//                        textViewDealInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user_icon, 0, 0, 0);
//                    } else {
//                        textViewDealInfo.setText("");
//                        textViewDealInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//                    }

                    View divider = (View) dealView.findViewById(R.id.divider_deal);
                    if (index == dealCount - 1 && holder.getViewMore().getVisibility() == View.GONE) {
                        divider.setVisibility(View.GONE);
                    } else {
                        divider.setVisibility(View.VISIBLE);
                    }


//                TextView dateTime = (TextView) dealView.findViewById(R.id.textView_date_from_to);
//                if ((!AppUtil.isStringEmpty(dealJsonObject.optString("from_date"))) && (!AppUtil.isStringEmpty(dealJsonObject.optString("to_date")))) {
//                    dateTime.setVisibility(View.VISIBLE);
//                    dateTime.setText("From " + dealJsonObject.optString("from_date") + " " + !AppUtil.isStringEmpty(dealJsonObject.optString("to_date")));
//                } else {
//                    dateTime.setVisibility(View.GONE);
//                }

                    holder.getViewMore().setTag(dealData.optString("restaurantId"));

                    holder.getDealLayout().addView(dealView);

                }
            }
        }
    }


    public class DealViewHolder extends RecyclerView.ViewHolder {

        private View cardView;
        private RelativeLayout relativeLayoutSearchCardParentLayout,relativeLayoutRatingBackground;

        private NetworkImageView imageViewRectSearchCardBackground;
        private TextView restName;



        private TextView textViewSearchCardRatingValue;
        private TextView distance;
        private TextView locationName;
        private LinearLayout dealLayout;
        private TextView viewMore;
        private TextView dateTime;
        private ImageView location;

        private TextView textViewRecencyTag;
        private TextView textViewRestRating;


        public DealViewHolder(View itemView) {
            super(itemView);
            cardView = itemView;
            instantiateViews();
        }



        protected void instantiateViews() {
            relativeLayoutSearchCardParentLayout = (RelativeLayout) cardView.findViewById(R.id.relativeLayoutSearchCardParentLayout);

            imageViewRectSearchCardBackground = (NetworkImageView) cardView.findViewById(R.id.rest_image);

            restName = (TextView) cardView.findViewById(R.id.rest_name);

            locationName = (TextView) cardView.findViewById(R.id.location_name);

            distance = (TextView) cardView.findViewById(R.id.distance);

            dealLayout = (LinearLayout) cardView.findViewById(R.id.deals_layout);

            viewMore = (TextView) cardView.findViewById(R.id.btn_view_more);
            location = (ImageView) cardView.findViewById(R.id.imageViewSearchCardLocation);

            relativeLayoutRatingBackground = (RelativeLayout) cardView.findViewById(R.id.relativeLayoutRating);


            textViewSearchCardRatingValue = (TextView) cardView.findViewById(R.id.textViewSearchCardRating);


            imageViewRectSearchCardBackground.setOnClickListener(mClickListener);
            viewMore.setOnClickListener(mClickListener);

            textViewRecencyTag = (TextView) cardView.findViewById(R.id.textView_recency_tag);
            textViewRestRating = (TextView) cardView.findViewById(R.id.textView_rest_rating);
        }


        public View getCardView() {
            return cardView;
        }


        public TextView getTextViewSearchCardRatingValue() {
            return textViewSearchCardRatingValue;
        }

        public RelativeLayout getRelativeLayoutSearchCardParentLayout() {
            return relativeLayoutSearchCardParentLayout;
        }


        public RelativeLayout getRelativeLayoutRatingBackground() {
            return relativeLayoutRatingBackground;
        }


        public NetworkImageView getImageViewRectSearchCardBackground() {
            return imageViewRectSearchCardBackground;
        }

        public TextView getRestName() {
            return restName;
        }


        public TextView getLocationName() {
            return locationName;
        }

        public TextView getDistance() {
            return distance;
        }

        public LinearLayout getDealLayout() {
            return dealLayout;
        }

        public TextView getViewMore() {
            return viewMore;
        }

        public ImageView getLocation() {
            return location;
        }

        public TextView getTextViewRecencyTag() {
            return textViewRecencyTag;
        }

        public TextView getTextViewRestRating() {
            return textViewRestRating;
        }
    }
}
