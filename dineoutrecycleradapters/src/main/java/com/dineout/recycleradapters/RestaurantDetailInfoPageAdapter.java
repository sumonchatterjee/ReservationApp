package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.viewmodel.MenuItemViewModel;
import com.dineout.recycleradapters.viewmodel.MenuThumbnailHandler;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RestaurantDetailInfoPageAdapter extends BaseRecyclerAdapter {

    private final int ITEM_TYPE_VIEW_MENU = 1;
    private final int ITEM_TYPE_OTHER_DETAILS = 2;
    private final int ITEM_TYPE_HORIZONTAL_LIST = 3;
    private final int ITEM_TYPE_BUTTON = 4;
    private final int ITEM_TYPE_BILL_SECTION = 5;

    private final String billSectionSectionKey = "billSection";
    private final String viewMenuSectionKey = "menuImages";
    private final String otherDetailsSectionKey = "otherDetailsSection";
    private final String similarRestaurantsSectionKey = "similarRestaurant";
    private final String addToMyListButtonKey = "addToMyListButton";
    HorizontalListItemRecyclerAdapter.IHorizontalItemClickListener horizontalItemClickListener;
    private Context mContext;
    private JSONObject restDetailJsonObject;
    private ImageLoader imageLoader;
    private InfoTabClickListener clickListener;

    private MenuThumbnailHandler menuThumbnailHandler;

    // Constructor
    public RestaurantDetailInfoPageAdapter(Context context, MenuThumbnailHandler handler) {
        // Set Context
        mContext = context;
        this.menuThumbnailHandler = handler;
    }

    public void setDataForAdapter(JSONArray jsonArray, JSONObject restDetailJsonObject) {
        this.restDetailJsonObject = restDetailJsonObject;

        setJsonArray(jsonArray);
    }

    public void setInfoTabClickListener(InfoTabClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    @Override
    protected int defineItemViewType(int position) {
        String sectionKey = getJsonArray().optJSONObject(position).optString("key");

        switch (sectionKey) {
            case billSectionSectionKey:
                return ITEM_TYPE_BILL_SECTION;

            case viewMenuSectionKey:
                return ITEM_TYPE_VIEW_MENU;

            case otherDetailsSectionKey:
                return ITEM_TYPE_OTHER_DETAILS;

            case similarRestaurantsSectionKey:
                return ITEM_TYPE_HORIZONTAL_LIST;

            case addToMyListButtonKey:
                return ITEM_TYPE_BUTTON;

            default:
                return 0;
        }
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check Item Type
        if (viewType == ITEM_TYPE_VIEW_MENU) {
            return new ViewMenuViewHolder(LayoutInflater.from(mContext).inflate
                    (R.layout.menu_image_section, parent, false));

        } else if (viewType == ITEM_TYPE_OTHER_DETAILS) {
            return new OtherDetailsViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.restaurant_info_list_item, parent, false));

        } else if (viewType == ITEM_TYPE_HORIZONTAL_LIST) {
            return new HorizontalListViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.feeds_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_BUTTON) {
            return new ButtonListItemViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.restaurant_detail_button_list_item, null, false));

        } else if (viewType == ITEM_TYPE_BILL_SECTION) {
            return new BillSectionViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.bill_section_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder == null) return;

        // Get Item View Type
        int itemViewType = holder.getItemViewType();

        if (itemViewType == ITEM_TYPE_VIEW_MENU) {
            showViewMenuSection((ViewMenuViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_OTHER_DETAILS) {
            showOtherDetailsSection((OtherDetailsViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_HORIZONTAL_LIST) {
            showHorizontalListSection((HorizontalListViewHolder) holder, listItem, "big_scroll");

        } else if (itemViewType == ITEM_TYPE_BUTTON) {
            showButtonSection((ButtonListItemViewHolder) holder);

        } else if (itemViewType == ITEM_TYPE_BILL_SECTION) {
            showBillSection((BillSectionViewHolder) holder, listItem);
        }
    }

    private void showViewMenuSection(ViewMenuViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Menu Section
            JSONObject menuSectionJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (menuSectionJsonObject != null) {

                String title = menuSectionJsonObject.optString("title", "View Menu");
                JSONArray menuArray = menuSectionJsonObject.optJSONArray("menuImages");

                viewHolder.bindData(title, getMenuUrlList(menuArray));
            }
        }
    }

    private ArrayList<String> getMenuUrlList(JSONArray menuJsonArray) {
        ArrayList<String> menus = new ArrayList<>();
        int arraySize = menuJsonArray.length();

        for (int index = 0; index < arraySize; index++) {
            JSONObject menuJsonObject = menuJsonArray.optJSONObject(index);

            if (menuJsonObject != null) {
                // Get Menu Image URL
                String menuImageURL = menuJsonObject.optString("imageUrl");

                if (!AppUtil.isStringEmpty(menuImageURL)) {
                    menus.add(menuImageURL);
                }
            }
        }

        return menus;
    }

    private void showOtherDetailsSection(OtherDetailsViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            final JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Opening Hours Section
            final JSONArray timingsJsonArray = dataJsonObject.optJSONArray("timings");
            if (timingsJsonArray == null || timingsJsonArray.length() == 0) {
                // Hide Opening Hours Section
                viewHolder.getLinearLayoutInfoOpeningSection().setVisibility(View.GONE);

            } else {
                // Show Opening Hours Section
                viewHolder.getLinearLayoutInfoOpeningSection().setVisibility(View.VISIBLE);

                int arraySize = dataJsonObject.optJSONArray("timings").length();
                for (int index = 0; index < arraySize; index++) {
                    JSONObject timingJsonObject = timingsJsonArray.optJSONObject(index);

                    if (timingJsonObject != null) {
                        if (timingJsonObject.opt("status") != null) {
                            // Set Timings
                            viewHolder.getTextViewInfoRestOpeningDateTime().
                                    setText(String.format(mContext.getString(R.string.text_container_today_timings),
                                            timingJsonObject.optString("time")));

                            // Set Status Text
                            viewHolder.getTextViewRestOpeningStatus().setText(
                                    mContext.getString((timingJsonObject.optInt("status") == 0) ? R.string.text_closed : R.string.text_open));

                            // Set Status Color
                            viewHolder.getTextViewRestOpeningStatus().setEnabled((timingJsonObject.optInt("status") == 1));

                            break;
                        }
                    }
                }

                // Set Opening Hours Click
                viewHolder.getLinearLayoutInfoOpeningSection().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickListener.onOpeningHourClick(getOpeningHoursList(timingsJsonArray));
                    }
                });
            }

            // Address
            if (!AppUtil.isStringEmpty(dataJsonObject.optString("address"))) {
                StringBuilder addressStringBuilder =
                        new StringBuilder(dataJsonObject.optString("address"));

                if (!AppUtil.isStringEmpty(dataJsonObject.optString("localityName"))) {
                    addressStringBuilder.append(", " + dataJsonObject.optString("localityName"));
                }

                if (!AppUtil.isStringEmpty(dataJsonObject.optString("areaName"))) {
                    addressStringBuilder.append(", " + dataJsonObject.optString("areaName"));
                }

                // Set Address
                viewHolder.getTextViewInfoAddress().setText(addressStringBuilder.toString());
            }

            // Show Static Map
            viewHolder.getRestLocationMapTile().setImageUrl(
                    getStaticMapUrl(dataJsonObject.optString("lat"),
                            dataJsonObject.optString("lon")), imageLoader);

            // Set Map Click
            viewHolder.getRestLocationMapTile().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onMapClick(dataJsonObject.optString("lat"), dataJsonObject.optString("lon"));
                }
            });

            // Features
            // Empty Feature List
            viewHolder.getLinearLayoutInfoFeatureList().removeAllViews();
            JSONArray featuresJsonArray = dataJsonObject.optJSONArray("feature");
            if (featuresJsonArray != null && featuresJsonArray.length() > 0) {
                int arraySize = featuresJsonArray.length();
                for (int index = 0; index < arraySize; index++) {
                    JSONObject featureJsonObject = featuresJsonArray.optJSONObject(index);

                    if (featureJsonObject != null) {
                        // Get View
                        View featureView = LayoutInflater.from(mContext).inflate(R.layout.feature_list_item, null, false);

                        NetworkImageView imageViewFeatureIcon = (NetworkImageView) featureView.findViewById(R.id.imageView_feature_icon);
                        if (AppUtil.isStringEmpty(featureJsonObject.optString("thumbnail"))) {
                            // Hide Feature Icon
                            imageViewFeatureIcon.setVisibility(View.GONE);

                        } else {
                            // Show Feature Icon
                            imageViewFeatureIcon.setVisibility(View.VISIBLE);

                            // Set Image
                            imageViewFeatureIcon.setImageUrl(featureJsonObject.optString("thumbnail"), imageLoader);
                        }

                        TextView textViewFeatureName = (TextView) featureView.findViewById(R.id.textView_feature_name);
                        if (!AppUtil.isStringEmpty(featureJsonObject.optString("featureName"))) {
                            textViewFeatureName.setText(featureJsonObject.optString("featureName"));
                        }

                        // Add Feature View to List
                        viewHolder.getLinearLayoutInfoFeatureList().addView(featureView);
                    }
                }

                // Show Feature Section
                viewHolder.getLinearLayoutInfoFeatureSection().setVisibility(View.VISIBLE);

            } else {
                // Hide Feature Section
                viewHolder.getLinearLayoutInfoFeatureSection().setVisibility(View.GONE);
            }

            // Featured Tags
            JSONArray tagsJsonArray = dataJsonObject.optJSONArray("tags");
            if (tagsJsonArray != null && tagsJsonArray.length() > 0) {
                int arraySize = tagsJsonArray.length();
                StringBuilder tagsStringBuilder = new StringBuilder("");
                for (int index = 0; index < arraySize; index++) {
                    String tag = tagsJsonArray.optString(index);

                    if (!AppUtil.isStringEmpty(tag)) {
                        if (tagsStringBuilder.length() == 0) {
                            tagsStringBuilder.append(tag);

                        } else {
                            tagsStringBuilder.append(", " + tag);
                        }
                    }
                }

                // Set Tags
                if (!AppUtil.isStringEmpty(tagsStringBuilder.toString())) {
                    viewHolder.getTextViewInfoFeaturedTags().setText(tagsStringBuilder.toString());
                }

                // Show Featured Tags Section
                viewHolder.getLinearLayoutInfoFeaturedTagsSection().setVisibility(View.VISIBLE);

                HashMap<String, Object> props = new HashMap<String, Object>();
                props.put("Tags", tagsStringBuilder.toString());
               // AnalyticsHelper.getAnalyticsHelper(mContext).trackEvent("Tags", props);

            } else {
                // Hide Featured Tags Section
                viewHolder.getLinearLayoutInfoFeaturedTagsSection().setVisibility(View.GONE);
            }

            // Call
            final JSONArray phoneJsonArray = dataJsonObject.optJSONArray("phone");
            if (phoneJsonArray != null && phoneJsonArray.length() > 0) {
                // Set Click Listener
                viewHolder.getLinearLayoutInfoCallSection().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickListener.onPhoneClick(getPhoneList(phoneJsonArray));
                    }
                });

                // Check for Discovery Restaurant
                if (AppConstant.RESTAURANT_TYPE_DISCOVERY.equalsIgnoreCase(
                        AppUtil.getRestaurantType(dataJsonObject.optInt("restaurantType")))) {
                    // Set Text
                    viewHolder.getTextViewInfoCall().setText(R.string.text_call_restaurant);
                } else {
                    // Set Text
                    viewHolder.getTextViewInfoCall().setText(R.string.text_call_dineout);
                }

                // Show Call Section
                viewHolder.getLinearLayoutInfoCallSection().setVisibility(View.VISIBLE);

            } else {
                // Hide Call Section
                viewHolder.getLinearLayoutInfoCallSection().setVisibility(View.GONE);
            }
        }
    }

    // This function inflates Restaurant Scroll Type View
    private void showHorizontalListSection(HorizontalListViewHolder horizontalListViewHolder,
                                           JSONObject jsonObject, String sectionType) {
        if (horizontalListViewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Set Feeds Section background

            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Similar Restaurant Section
            JSONObject similarRestaurantJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (similarRestaurantJsonObject != null) {

                // Set Title
                if (!AppUtil.isStringEmpty(similarRestaurantJsonObject.optString("title"))) {
                    horizontalListViewHolder.getTextViewSectionTitle().setText(similarRestaurantJsonObject.optString("title"));
                }

                // Set More Text Visibility
                horizontalListViewHolder.getTextViewSectionMore().setVisibility(View.GONE);

                // Add Section Type in JSON Object
                try {
                    // Prepare View of Section List
                    JSONArray jsonArray = similarRestaurantJsonObject.optJSONArray("similarRestaurant");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        prepareHorizontalScrollList(horizontalListViewHolder.getRecyclerViewHorizontal(),
                                prepareHorizontalJsonObject(jsonArray, sectionType));
                    }
                } catch (JSONException e) {
                    Log.e("DynamicFeedsHome", "section_type could not be added");
                }
            }
        }
    }

    private void showBillSection(BillSectionViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Bill Section
            JSONObject sectionJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (sectionJsonObject != null) {
                JSONObject billSectionJsonObject = sectionJsonObject.optJSONObject("billSection");

                if (billSectionJsonObject != null) {
                    // Set Title
                    String title = billSectionJsonObject.optString("text", "");
                    if (!AppUtil.isStringEmpty(title)) {
                        viewHolder.getTextViewBillText().setText(title);
                    }

                    // Set Button
                    String buttonText = billSectionJsonObject.optString("buttonText");
                    if (!AppUtil.isStringEmpty(buttonText)) {
                        viewHolder.getButtonBillAction().setText(buttonText);
                    }

                    // Set CTA
                    final int action = billSectionJsonObject.optInt("buttonCTA", -1);
                    if (action >= 0) {
                        viewHolder.getButtonBillAction().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                clickListener.onBillCtaClick(action);
                            }
                        });
                    } else {
                        viewHolder.getButtonBillAction().setOnClickListener(null);
                    }

                    // Set Sub Text
                    String subText = billSectionJsonObject.optString("subText");
                    if (!AppUtil.isStringEmpty(subText)) {
                        viewHolder.getTextViewBillSubText().setText(subText);
                        viewHolder.getTextViewBillSubText().setVisibility(View.VISIBLE);
                        viewHolder.getViewBillSectionDivider().setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.getTextViewBillSubText().setVisibility(View.GONE);
                        viewHolder.getViewBillSectionDivider().setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private JSONObject prepareHorizontalJsonObject(JSONArray jsonArray, String sectionType) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        // Add Section Type
        jsonObject.put("section_type", sectionType);

        // Define Data Array
        JSONArray dataJsonArray = new JSONArray();

        int arraySize = jsonArray.length();
        for (int index = 0; index < arraySize; index++) {
            JSONObject itemJsonObject = jsonArray.optJSONObject(index).optJSONObject("data");

            if (itemJsonObject != null) {
                JSONObject dataJsonObject = new JSONObject();

                dataJsonObject.put("img_url", itemJsonObject.optString("images", "")); // Restaurant Image
                dataJsonObject.put("title_1", itemJsonObject.optString("restaurantName", "")); // Restaurant Name
                dataJsonObject.put("title_2", itemJsonObject.optString("localityName", "") + ", " + itemJsonObject.optString("areaName", "")); // Restaurant Locality, Area
                dataJsonObject.put("title_3", String.format(mContext.getString(R.string.text_container_cost_for_2), itemJsonObject.optInt("costFor2", 0))); // Cost for 2
                dataJsonObject.put("title_4", ((itemJsonObject.optInt("offers", 0) == 0) ? "" :
                        ((itemJsonObject.optInt("offers", 0) == 1) ? String.format(mContext.getString(R.string.text_container_single_offer), itemJsonObject.optInt("offers", 0)) :
                                String.format(mContext.getString(R.string.text_container_multiple_offer), itemJsonObject.optInt("offers", 0))))); // Offer Count
                dataJsonObject.put("link", itemJsonObject.optString("deepLink", "")); // Link Url
                dataJsonObject.put("rating", Double.toString(itemJsonObject.optDouble("avgRating", 0))); // Rating

                dataJsonArray.put(dataJsonObject);
            }
        }

        // Add data
        jsonObject.put("data", dataJsonArray);

        return jsonObject;
    }

    // Prepare View of Section List
    private void prepareHorizontalScrollList(RecyclerView recyclerView, JSONObject jsonObject) {
        // Initiate Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager
                (mContext, LinearLayoutManager.HORIZONTAL, false);

        // Set Layout Manager
        recyclerView.setLayoutManager(linearLayoutManager);

        // Instantiate Adapter
        HorizontalListItemRecyclerAdapter horizontalListItemRecyclerAdapter =
                new HorizontalListItemRecyclerAdapter(jsonObject, mContext);

        // Set Adapter
        recyclerView.setAdapter(horizontalListItemRecyclerAdapter);

        // Notify Data Change
        horizontalListItemRecyclerAdapter.notifyDataSetChanged();

        // Set Click Listener
        horizontalListItemRecyclerAdapter.setOnHorizontalItemClickListener(horizontalItemClickListener);
    }

    public void setOnHorizontalItemClickListener(HorizontalListItemRecyclerAdapter.IHorizontalItemClickListener horizontalItemClickListener) {
        this.horizontalItemClickListener = horizontalItemClickListener;
    }

    private void showButtonSection(final ButtonListItemViewHolder viewHolder) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Is marked Fav
            final boolean isFavorite = dataJsonObject.optBoolean("isFavorite");

            // Set Button Text
            viewHolder.getTextViewButtonText().setText((isFavorite) ?
                    R.string.button_remove_from_my_list : R.string.button_add_to_my_list);

            // Set Icon
            viewHolder.getImageViewButtonIcon().setImageResource((isFavorite) ?
                    R.drawable.ic_blue_minus : R.drawable.ic_blue_add);

            // Set Click Listener
            viewHolder.getButtonListItem().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onFavUnFavButtonClick(restDetailJsonObject.optJSONObject("data").optInt("restaurantId"),
                            !isFavorite);
                }
            });
        }
    }

    private ArrayList<String> getOpeningHoursList(JSONArray timingsJsonArray) {
        ArrayList<String> timings = new ArrayList<>();

        int arraySize = timingsJsonArray.length();
        for (int index = 0; index < arraySize; index++) {
            JSONObject timingJsonObject = timingsJsonArray.optJSONObject(index);

            if (timingJsonObject != null) {
                timings.add(timingJsonObject.optString("day") + " - " + timingJsonObject.optString("time"));
            }
        }

        return timings;
    }

    private ArrayList<String> getPhoneList(JSONArray phoneJsonArray) {
        ArrayList<String> phones = new ArrayList<>();

        int arraySize = phoneJsonArray.length();
        for (int index = 0; index < arraySize; index++) {
            String phone = phoneJsonArray.optString(index);

            if (!AppUtil.isStringEmpty(phone)) {
                phones.add(phone);
            }
        }

        return phones;
    }

    private String getStaticMapUrl(String lat, String lng) {
        return ("http://maps.googleapis.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=16" +
                "&scale=false&size=" + AppUtil.dpToPx(100, mContext.getResources()) + "x"
                + AppUtil.dpToPx(75, mContext.getResources()) + "&maptype=roadmap" +
                "&format=png&visual_refresh=true&markers=size:large%7Ccolor:red%7Clabel:O%7C" + lat + "," + lng);
    }

    public interface InfoTabClickListener {
        void onViewMenuClick(ArrayList<String> imageUrlList);

        void onOpeningHourClick(ArrayList<String> timingList);

        void onPhoneClick(ArrayList<String> phoneList);

        void onMapClick(String lat, String lng);

        void onFavUnFavButtonClick(int restaurantId, boolean favAction);

        void onBillCtaClick(int action);
    }

    /**
     * View Holders
     */
    // Bill Section View Holder
    private class BillSectionViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewBillText;
        private Button buttonBillAction;
        private View viewBillSectionDivider;
        private TextView textViewBillSubText;

        public BillSectionViewHolder(View itemView) {
            super(itemView);

            textViewBillText = (TextView) itemView.findViewById(R.id.textView_bill_text);
            buttonBillAction = (Button) itemView.findViewById(R.id.button_bill_action);
            viewBillSectionDivider = itemView.findViewById(R.id.view_bill_section_divider);
            textViewBillSubText = (TextView) itemView.findViewById(R.id.textView_bill_sub_text);
        }

        public TextView getTextViewBillText() {
            return textViewBillText;
        }

        public Button getButtonBillAction() {
            return buttonBillAction;
        }

        public View getViewBillSectionDivider() {
            return viewBillSectionDivider;
        }

        public TextView getTextViewBillSubText() {
            return textViewBillSubText;
        }
    }

    // View Menu Section View Holder
    private class ViewMenuViewHolder extends RecyclerView.ViewHolder {
        private MenuItemViewModel menuSection;
        private View rootView;

        public ViewMenuViewHolder(View v) {
            super(v);

            this.rootView = v;
        }

        public void bindData(String title, ArrayList<String> list) {
            menuSection = new MenuItemViewModel(rootView, title, list, menuThumbnailHandler);
        }
    }

    // Other Details Section View Holder
    private class OtherDetailsViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayoutInfoOpeningSection;
        private TextView textViewInfoRestOpeningDateTime;
        private TextView textViewInfoOpenHours;
        private TextView textViewRestOpeningStatus;
        private TextView textViewInfoAddress;
        private NetworkImageView restLocationMapTile;
        private LinearLayout linearLayoutInfoFeatureSection;
        private LinearLayout linearLayoutInfoFeatureList;
        private LinearLayout linearLayoutInfoFeaturedTagsSection;
        private TextView textViewInfoFeaturedTags;
        private LinearLayout linearLayoutInfoCallSection;
        private TextView textViewInfoCall;

        public OtherDetailsViewHolder(View itemView) {
            super(itemView);

            linearLayoutInfoOpeningSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_info_opening_section);
            textViewInfoRestOpeningDateTime = (TextView) itemView.findViewById(R.id.textView_info_rest_opening_date_time);
            textViewInfoOpenHours = (TextView) itemView.findViewById(R.id.textView_info_open_hours);
            textViewRestOpeningStatus = (TextView) itemView.findViewById(R.id.textView_rest_opening_status);
            textViewInfoAddress = (TextView) itemView.findViewById(R.id.textView_info_address);
            restLocationMapTile = (NetworkImageView) itemView.findViewById(R.id.rest_location_map_tile);
            linearLayoutInfoFeatureSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_info_feature_section);
            linearLayoutInfoFeatureList = (LinearLayout) itemView.findViewById(R.id.linearLayout_info_feature_list);
            linearLayoutInfoFeaturedTagsSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_info_featured_tags_section);
            textViewInfoFeaturedTags = (TextView) itemView.findViewById(R.id.textView_info_featured_tags);
            linearLayoutInfoCallSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_info_call_section);
            textViewInfoCall = (TextView) itemView.findViewById(R.id.textView_info_call);
        }

        public LinearLayout getLinearLayoutInfoOpeningSection() {
            return linearLayoutInfoOpeningSection;
        }

        public TextView getTextViewInfoRestOpeningDateTime() {
            return textViewInfoRestOpeningDateTime;
        }

        public TextView getTextViewInfoOpenHours() {
            return textViewInfoOpenHours;
        }

        public TextView getTextViewRestOpeningStatus() {
            return textViewRestOpeningStatus;
        }

        public TextView getTextViewInfoAddress() {
            return textViewInfoAddress;
        }

        public NetworkImageView getRestLocationMapTile() {
            return restLocationMapTile;
        }

        public LinearLayout getLinearLayoutInfoFeatureSection() {
            return linearLayoutInfoFeatureSection;
        }

        public LinearLayout getLinearLayoutInfoFeatureList() {
            return linearLayoutInfoFeatureList;
        }

        public LinearLayout getLinearLayoutInfoFeaturedTagsSection() {
            return linearLayoutInfoFeaturedTagsSection;
        }

        public TextView getTextViewInfoFeaturedTags() {
            return textViewInfoFeaturedTags;
        }

        public LinearLayout getLinearLayoutInfoCallSection() {
            return linearLayoutInfoCallSection;
        }

        public TextView getTextViewInfoCall() {
            return textViewInfoCall;
        }
    }

    private class HorizontalListViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSectionTitle;
        private TextView textViewSectionMore;
        private RecyclerView recyclerViewHorizontal;
        private View rootView;

        public HorizontalListViewHolder(View itemView) {
            super(itemView);

            textViewSectionTitle = (TextView) itemView.findViewById(R.id.text_view_section_title);
            textViewSectionMore = (TextView) itemView.findViewById(R.id.text_view_section_more);
            recyclerViewHorizontal = (RecyclerView) itemView.findViewById(R.id.recycler_view_section_list);
            rootView = recyclerViewHorizontal.getRootView();
            rootView.setBackgroundResource(R.color.white);
        }

        public TextView getTextViewSectionTitle() {
            return textViewSectionTitle;
        }

        public TextView getTextViewSectionMore() {
            return textViewSectionMore;
        }

        public RecyclerView getRecyclerViewHorizontal() {
            return recyclerViewHorizontal;
        }

        public View getRootView() {
            return rootView;
        }
    }

    private class ButtonListItemViewHolder extends RecyclerView.ViewHolder {
        private View buttonListItem;
        private TextView textViewButtonText;
        private ImageView imageViewButtonIcon;

        public ButtonListItemViewHolder(View itemView) {
            super(itemView);

            buttonListItem = itemView.findViewById(R.id.secondary_button_list_item);

            // Set Icon
            imageViewButtonIcon = (ImageView) itemView.findViewById(R.id.imageView_button_icon);

            // Set Text
            textViewButtonText = (TextView) itemView.findViewById(R.id.textView_button_text);
        }

        public View getButtonListItem() {
            return buttonListItem;
        }

        public TextView getTextViewButtonText() {
            return textViewButtonText;
        }

        public ImageView getImageViewButtonIcon() {
            return imageViewButtonIcon;
        }
    }
}
