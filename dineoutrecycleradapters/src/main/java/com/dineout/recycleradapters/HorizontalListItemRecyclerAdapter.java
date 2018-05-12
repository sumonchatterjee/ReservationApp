package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONObject;

import java.util.HashMap;

public class HorizontalListItemRecyclerAdapter extends BaseRecyclerAdapter
        implements OnClickListener {

    // Assign positive values only
    private static final int ITEM_TYPE_BIG_SCROLL = 1;
    private static final int ITEM_TYPE_EVENT_SCROLL = 2;
    private static final int ITEM_TYPE_SMALL_SCROLL = 3;
    private static final int ITEM_TYPE_FOOTER_VIEW = 4;

    private IHorizontalItemClickListener horizontalItemClickListener;
    private Context mContext;
    private JSONObject jsonObjectFeed;

    // Constructor
    public HorizontalListItemRecyclerAdapter(JSONObject jsonObjectFeed, Context context) {
        // Set List
        super(jsonObjectFeed.optJSONArray("data"));
        if (!TextUtils.isEmpty(jsonObjectFeed.optString("view_msg"))) {
            setFooter(true);
        } else {
            setFooter(false);
        }
        // Set Feed JSON
        this.jsonObjectFeed = jsonObjectFeed;

        // Set Context
        mContext = context;
    }

    @Override
    protected int defineItemViewType(int position) {

        if (listHasFooter() && isPositionFooter(position)) {
            return ITEM_TYPE_FOOTER_VIEW;
        } else {
            // Check Section Type to inflate corresponding View
            if ("big_scroll".equalsIgnoreCase(jsonObjectFeed.optString("section_type"))) {
                return ITEM_TYPE_BIG_SCROLL;

            } else if ("event_scroll".equalsIgnoreCase(jsonObjectFeed.optString("section_type"))) {
                return ITEM_TYPE_EVENT_SCROLL;

            } else if ("small_scroll".equalsIgnoreCase(jsonObjectFeed.optString("section_type"))) {
                return ITEM_TYPE_SMALL_SCROLL;
            }

            return ITEM_VIEW_TYPE_EMPTY;
        }
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_BIG_SCROLL) {
            return new BigScrollViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.big_scroll_card_layout, parent, false));

        } else if (viewType == ITEM_TYPE_EVENT_SCROLL) {
            return new EventScrollViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.event_scroll_card_layout, parent, false));

        } else if (viewType == ITEM_TYPE_SMALL_SCROLL) {
            return new SmallScrollViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.small_scroll_card_layout, parent, false));

        } else if (viewType == ITEM_TYPE_FOOTER_VIEW) {
            return new FooterViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.horizontal_footer_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {

        if (holder.getItemViewType() == ITEM_TYPE_BIG_SCROLL) {
            // Render Restaurant Scroll View
            renderBigScrollView((BigScrollViewHolder) holder, listItem ,position);

        } else if (holder.getItemViewType() == ITEM_TYPE_EVENT_SCROLL) {
            // Render Event Scroll View
            renderEventScrollView((EventScrollViewHolder) holder, listItem,position);

        } else if (holder.getItemViewType() == ITEM_TYPE_SMALL_SCROLL) {
            // Render Small Scroll View
            renderSmallScrollView((SmallScrollViewHolder) holder, listItem,position);

        } else if (holder.getItemViewType() == ITEM_TYPE_FOOTER_VIEW) {
            // Render Event Scroll View
            renderFooterView((FooterViewHolder) holder);
        }
    }

    // Render Restaurant Scroll View
    private void renderBigScrollView(BigScrollViewHolder viewHolder, JSONObject jsonObject,int position) {
        // Check for NULL
        if (viewHolder != null && jsonObject != null) {
            // Set Restaurant Image
            if (!TextUtils.isEmpty(jsonObject.optString("img_url"))) {
                viewHolder.getImageViewRestaurant().
                        setImageUrl((jsonObject.optString("img_url")),
                                ImageRequestManager.getInstance(mContext).getImageLoader());
            }

            // Set Restaurant Name
            if (!TextUtils.isEmpty(jsonObject.optString("title_1"))) {
                viewHolder.getTextViewRestaurantName().setText(jsonObject.optString("title_1"));
            }

            // Set Restaurant Address
            if (!TextUtils.isEmpty(jsonObject.optString("title_2"))) {
                viewHolder.getTextViewRestaurantAddress().setText(jsonObject.optString("title_2"));
            }

            // Set Cost for 2
            if (!TextUtils.isEmpty(jsonObject.optString("title_3"))) {
                viewHolder.getTextViewCostFor2().
                        setText(AppUtil.renderRupeeSymbol(jsonObject.optString("title_3")));
            }

            // Set Offer Count
            if (!TextUtils.isEmpty(jsonObject.optString("title_4"))) {
                viewHolder.getTextViewOfferCount().setText(jsonObject.optString("title_4"));
            }

            // Set Rating
            // Check if Rating is greater than 0
            if (AppUtil.hasNoRating(jsonObject.optString("rating", ""))) {
                // Hide Rating Layout
                viewHolder.getRelativeLayoutRatingContainer().setVisibility(RelativeLayout.GONE);
            } else {
                // Show Rating Layout
                viewHolder.getRelativeLayoutRatingContainer().setVisibility(RelativeLayout.VISIBLE);

                // Set Rating
                AppUtil.setRatingValueBackground(jsonObject.optString("rating"), viewHolder.getTextViewRatingValue());
            }
            try {
                jsonObject.put("position", position);
            }catch (Exception ex){
                //exception
            }
            viewHolder.getRootView().setOnClickListener(this);
            viewHolder.getRootView().setTag(viewHolder.getRootView().getId(), jsonObject);
        }
    }

    // Render Collection Scroll View
    private void renderEventScrollView(EventScrollViewHolder viewHolder, JSONObject jsonObject, int position) {
        // Check for NULL
        if (viewHolder != null && jsonObject != null) {
            // Set Background
            if (!AppUtil.isStringEmpty(jsonObject.optString("img_url"))) {
                viewHolder.getImageViewEvent().setImageUrl(jsonObject.optString("img_url"),
                        ImageRequestManager.getInstance(mContext).getImageLoader());
            }

            // Set Event Name
            if (!AppUtil.isStringEmpty(jsonObject.optString("title_1"))) {
                viewHolder.getTextViewEventName().setText(jsonObject.optString("title_1"));
                viewHolder.getTextViewEventName().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewEventName().setVisibility(View.GONE);
            }

            // Set Restaurant Name
            if (!AppUtil.isStringEmpty(jsonObject.optString("title_2"))) {
                viewHolder.getTextViewRestaurantName().setText(jsonObject.optString("title_2"));
                viewHolder.getTextViewRestaurantName().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewRestaurantName().setVisibility(View.GONE);
            }

            // Set Event Date
            String date = jsonObject.optString("title_3");
            if (!AppUtil.isStringEmpty(date)) {
                viewHolder.getTextViewEventDate().setText(date);
                viewHolder.getTextViewEventDate().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewEventDate().setVisibility(View.GONE);
            }

            // Set Event Time
            //Get Time
            String time = jsonObject.optString("title_4", "");
            if (!AppUtil.isStringEmpty(time)) {
                viewHolder.getTextViewEventTime().setText(time);
                viewHolder.getTextViewEventTime().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewEventTime().setVisibility(View.GONE);
            }

            // Set Event Price Tag
            String priceTag = jsonObject.optString("title_5", "");
            if (AppUtil.isStringEmpty(priceTag)) {
                viewHolder.getTextViewEventPriceTag().setVisibility(View.GONE);
            } else {
                if (priceTag.contains("u20")) {
                    viewHolder.getTextViewEventPriceTag().
                            setText(AppUtil.renderRupeeSymbol(priceTag));
                    viewHolder.getTextViewEventPriceTag().
                            setTextColor(mContext.getResources().getColor(R.color.grey_9b));
                } else {
                    viewHolder.getTextViewEventPriceTag().setText(priceTag);
                    viewHolder.getTextViewEventPriceTag().
                            setTextColor(mContext.getResources().getColor(R.color.light_bright_green));
                }
            }
            try {
                jsonObject.put("position", position);
            }catch (Exception ex){
                //exception
            }
            viewHolder.getRootView().setOnClickListener(this);
            viewHolder.getRootView().setTag(viewHolder.getRootView().getId(), jsonObject);
        }
    }

    private void renderSmallScrollView(SmallScrollViewHolder viewHolder, JSONObject jsonObject, int position) {
        // Check for NULL
        if (viewHolder != null && jsonObject != null) {
            // Set Collection Image
            String imageUrl = jsonObject.optString("img_url", "");
            if (!TextUtils.isEmpty(imageUrl)) {
                viewHolder.getImageViewCollection().
                        setImageUrl(imageUrl, ImageRequestManager.getInstance(mContext).getImageLoader());
            }

            // Set Collection Name
            String collectionName = jsonObject.optString("title_1", "");
            if (!TextUtils.isEmpty(collectionName)) {
                viewHolder.getTextViewCollectionName().setText(collectionName);
                viewHolder.getTextViewCollectionName().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewCollectionName().setVisibility(View.GONE);
            }

            // Set Collection Count
            String collectionCount = jsonObject.optString("title_2", "");
            if (!TextUtils.isEmpty(collectionCount)) {
                viewHolder.getTextViewCollectionCount().setText(collectionCount);
                viewHolder.getTextViewCollectionCount().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewCollectionCount().setVisibility(View.GONE);
            }
            try {
                jsonObject.put("position", position);
            }catch (Exception ex){
                //exception
            }
            viewHolder.getRootView().setOnClickListener(this);
            viewHolder.getRootView().setTag(viewHolder.getRootView().getId(), jsonObject);
        }
    }

    private void renderFooterView(FooterViewHolder viewHolder) {

    }

    @Override
    public void onClick(View view) {
        if (view != null) {

            JSONObject obj = (JSONObject) view.getTag(view.getId());
            HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(mContext);
            if(hMap!=null){

                hMap.put("category", "D_Home");
                if(!TextUtils.isEmpty(jsonObjectFeed.optString("header"))) {
                    hMap.put("label", jsonObjectFeed.optString("header") + "_" + obj.optString("title_1") + "_" + obj.optString("obj_id"));
                }
                hMap.put("action", "CardRestaurantClick");
                hMap.put("poc",Integer.toString(obj.optInt("position")));

            }

            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("CardRestaurantClick",hMap);
            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("D_Home","CardRestaurantClick",jsonObjectFeed.optString("header")+ "_" +obj.optString("title_1")+ "_" + obj.optString("obj_id"));

            horizontalItemClickListener.onHorizontalListItemClicked(obj.optString("link"));
        }
    }

    public void setOnHorizontalItemClickListener(IHorizontalItemClickListener horizontalItemClickListener) {
        this.horizontalItemClickListener = horizontalItemClickListener;
    }

    public interface IHorizontalItemClickListener {
        void onHorizontalListItemClicked(String linkUrl);
    }


    /**
     * View Holders
     */
    private class BigScrollViewHolder extends RecyclerView.ViewHolder {
        private NetworkImageView imageViewRestaurant;
        private TextView textViewRestaurantName;
        private TextView textViewRestaurantAddress;
        private TextView textViewCostFor2;
        private TextView textViewOfferCount;
        private View rootView;
        private RelativeLayout relativeLayoutRatingContainer;
        private TextView textViewRatingValue;

        public BigScrollViewHolder(View itemView) {
            super(itemView);

            rootView = itemView;
            imageViewRestaurant = (NetworkImageView) itemView.findViewById(R.id.image_view_restaurant);
            imageViewRestaurant.setDefaultImageResId(R.drawable.default_list);
            textViewRestaurantName = (TextView) itemView.findViewById(R.id.text_view_restaurant_name);
            textViewRestaurantAddress = (TextView) itemView.findViewById(R.id.text_view_restaurant_address);
            textViewCostFor2 = (TextView) itemView.findViewById(R.id.text_view_cost_for_two);
            textViewOfferCount = (TextView) itemView.findViewById(R.id.text_view_offer_count);
            relativeLayoutRatingContainer = (RelativeLayout) itemView.findViewById(R.id.relative_layout_rating_container);
            textViewRatingValue = (TextView) itemView.findViewById(R.id.text_view_rating_value);
        }

        public NetworkImageView getImageViewRestaurant() {
            return imageViewRestaurant;
        }

        public TextView getTextViewRestaurantName() {
            return textViewRestaurantName;
        }

        public TextView getTextViewRestaurantAddress() {
            return textViewRestaurantAddress;
        }

        public TextView getTextViewCostFor2() {
            return textViewCostFor2;
        }

        public TextView getTextViewOfferCount() {
            return textViewOfferCount;
        }

        public View getRootView() {
            return rootView;
        }

        public RelativeLayout getRelativeLayoutRatingContainer() {
            return relativeLayoutRatingContainer;
        }

        public TextView getTextViewRatingValue() {
            return textViewRatingValue;
        }
    }

    private class EventScrollViewHolder extends RecyclerView.ViewHolder {
        private NetworkImageView imageViewEvent;
        private TextView textViewEventName;
        private TextView textViewRestaurantName;
        private TextView textViewEventDate;
        private TextView textViewEventTime;
        private TextView textViewEventPriceTag;
        private View rootView;

        public EventScrollViewHolder(View itemView) {
            super(itemView);

            imageViewEvent = (NetworkImageView) itemView.findViewById(R.id.image_view_event);
            imageViewEvent.setDefaultImageResId(R.drawable.default_list);
            textViewEventName = (TextView) itemView.findViewById(R.id.text_view_event_name);
            textViewRestaurantName = (TextView) itemView.findViewById(R.id.text_view_restaurant_name);
            textViewEventDate = (TextView) itemView.findViewById(R.id.text_view_event_date);
            textViewEventTime = (TextView) itemView.findViewById(R.id.text_view_event_time);
            textViewEventPriceTag = (TextView) itemView.findViewById(R.id.text_view_event_price_tag);
            rootView = itemView;
        }

        public NetworkImageView getImageViewEvent() {
            return imageViewEvent;
        }

        public TextView getTextViewEventName() {
            return textViewEventName;
        }

        public TextView getTextViewRestaurantName() {
            return textViewRestaurantName;
        }

        public TextView getTextViewEventDate() {
            return textViewEventDate;
        }

        public TextView getTextViewEventTime() {
            return textViewEventTime;
        }

        public TextView getTextViewEventPriceTag() {
            return textViewEventPriceTag;
        }

        public View getRootView() {
            return rootView;
        }
    }

    private class SmallScrollViewHolder extends RecyclerView.ViewHolder {
        private NetworkImageView imageViewCollection;
        private TextView textViewCollectionName;
        private TextView textViewCollectionCount;
        private View rootView;

        public SmallScrollViewHolder(View itemView) {
            super(itemView);

            imageViewCollection = (NetworkImageView) itemView.findViewById(R.id.image_view_collection);
            imageViewCollection.setDefaultImageResId(R.drawable.default_list);
            textViewCollectionName = (TextView) itemView.findViewById(R.id.text_view_collection_name);
            textViewCollectionCount = (TextView) itemView.findViewById(R.id.text_view_collection_count);
            rootView = itemView;
        }

        public NetworkImageView getImageViewCollection() {
            return imageViewCollection;
        }

        public TextView getTextViewCollectionName() {
            return textViewCollectionName;
        }

        public TextView getTextViewCollectionCount() {
            return textViewCollectionCount;
        }

        public View getRootView() {
            return rootView;
        }

    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        //private TextView textViewViewAll;
        private ImageView imageViewLinkViewAll;

        public FooterViewHolder(View itemView) {
            super(itemView);

            imageViewLinkViewAll = (ImageView) itemView.findViewById(R.id.imageViewLinkViewAll);
            imageViewLinkViewAll.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (jsonObjectFeed != null) {
                        if (horizontalItemClickListener != null) {

                            // track for ga and countly
                            HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(mContext);
                            if(hMap!=null){
                                hMap.put("category", "D_Home");

                                if(!TextUtils.isEmpty(jsonObjectFeed.optString("header"))) {
                                    hMap.put("label", jsonObjectFeed.optString("header"));
                                }
                                    hMap.put("action", "CardViewAllArrowClick");

                            }


                            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("CardViewAllArrowClick",hMap);
                            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("D_Home","CardViewAllArrowClick",jsonObjectFeed.optString("header"));


                            // track event for qgraph, apsalar, branch
                            HashMap<String, Object> props = new HashMap<>();
                            props.put("label",jsonObjectFeed.optString("header"));
                            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventQGraphApsalar("CardViewAllArrowClick",props,true,false);


                            horizontalItemClickListener.onHorizontalListItemClicked(jsonObjectFeed.optString("view_url"));
                        }
                    }
                }
            });

            /*textViewViewAll = (TextView) itemView.findViewById(R.id.textViewLinkViewAll);

            textViewViewAll.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (jsonObjectFeed != null) {
                        textViewViewAll.setText(jsonObjectFeed.optString("view_msg"));
                        if (horizontalItemClickListener != null) {
                            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("Home", mContext.getResources().getString(R.string.ga_action_view_all_card) + jsonObjectFeed.optString("header"), null);
                            horizontalItemClickListener.onHorizontalListItemClicked(jsonObjectFeed.optString("view_url"));
                        }
                    }

                }
            });*/
        }
    }
}
