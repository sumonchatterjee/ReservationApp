package com.dineout.recycleradapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import org.json.JSONException;
import org.json.JSONObject;

import static com.dineout.recycleradapters.util.AppUtil.isTrue;

public class RestaurantListingAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private static final int ITEM_TYPE_REST_LIST = 101;

    private Context mContext;
    private DineoutNetworkManager networkManager;
    private ImageLoader imageLoader;
    private String mAppImgUrl = "";
    private OnCardClickedListener onCardClickedListener;
    private RestaurantListClickListener restaurantListClickListener;
    private  OnOfferListener onOfferClickListener;

    public RestaurantListingAdapter(Context context) {
        mContext = context;
    }

    public void setNetworkManager(DineoutNetworkManager networkManager, ImageLoader imageLoader) {
        this.networkManager = networkManager;
        this.imageLoader = imageLoader;
    }

    public RestaurantListClickListener getRestaurantListClickListener() {
        return restaurantListClickListener;
    }

    public void setRestaurantListClickListener(RestaurantListClickListener restaurantListClickListener) {
        this.restaurantListClickListener = restaurantListClickListener;
    }

    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Override
    protected int defineItemViewType(int position) {
        if (getJsonArray() == null) {
            return ITEM_VIEW_TYPE_EMPTY;
        } else {
            return ITEM_TYPE_REST_LIST;
        }
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_REST_LIST) {
            return new RestaurantViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.restaurant_single_list_item, parent, false));
        }

        return null;
    }

    public void setData(JSONArray rest_arr, int startIndex, String appImgUrl) {
        mAppImgUrl = appImgUrl;
        if (startIndex == 0) {
            setJsonArray(rest_arr);
            notifyDataSetChanged();

        } else {
            if (getJsonArray() != null) {
                JSONArray consolidatedArr = new JSONArray();

                for (int i = 0; i < getJsonArray().length(); i++) {
                    consolidatedArr.put(getJsonArray().opt(i));
                }

                for (int j = 0; j < rest_arr.length(); j++) {
                    consolidatedArr.put(rest_arr.opt(j));
                }

                setJsonArray(consolidatedArr);

                notifyItemRangeChanged(consolidatedArr.length() - rest_arr.length()
                        , rest_arr.length());
            }
        }
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder.getItemViewType() == ITEM_TYPE_REST_LIST) {
            try {
                if (listItem != null) {
                    listItem.put("position", position);
                }
                inflateRestaurantList(listItem, (RestaurantViewHolder) holder);
            }catch (Exception ex){

            }
        }
    }

    private void inflateRestaurantList(JSONObject itemObject, RestaurantViewHolder holder) {

        String avg_rating = itemObject.optString("avg_rating");
        String recency = itemObject.optString("recency");


//        Product product = new Product();
//        product.setId(itemObject.optString("r_id"));
//        product.setName(itemObject.optString("profile_name"));
//        product.setCategory("1".equalsIgnoreCase(itemObject.optString("is_pf"))?"PF":
//                ("1".equalsIgnoreCase(itemObject.optString("fullfillment"))?"FF":"Discovery"));
//        AnalyticsHelper.getAnalyticsHelper(mContext).measureProductImpression(product,
//               "addImpression",
//                mContext.getString(R.string.ga_screen_listing));

        if (recency.equals("1")) {
            // Hide Rating Layout
            holder.getRelativeLayoutRatingBackground().setVisibility(RelativeLayout.GONE);

            // Show Recently Added Tag
            holder.getTextViewRestCardNewTag().setVisibility(TextView.VISIBLE);

        } else {
            if (AppUtil.hasNoRating(avg_rating)) {
                holder.getRelativeLayoutRatingBackground().setVisibility(RelativeLayout.GONE);
            } else {
                holder.getRelativeLayoutRatingBackground().setVisibility(RelativeLayout.VISIBLE);

                // Hide Recently Added Tag
                holder.getTextViewRestCardNewTag().setVisibility(TextView.GONE);
                AppUtil.setRatingValueBackground(avg_rating, holder
                        .getTextViewSearchCardRatingValue());
            }
        }

        holder.getCardView().setOnClickListener(this);
        holder.getCardView().setTag(itemObject);

        JSONArray imgArray = itemObject.optJSONArray("img");
        if (imgArray != null && imgArray.length() > 0) {
            String imageUrl = imgArray.optString(0);

            if (mAppImgUrl != null) {
                holder.getImageViewRectSearchCardBackground().
                        setImageUrl((mAppImgUrl + imageUrl), getImageLoader());
            }
        }

        //Set Title
        holder.getRestName().setText(Html.fromHtml
                (itemObject.optString("profile_name")));

        //Set Distance
        String geoDistance = itemObject.optString("geo_distance");

        if (DOPreferences.isAutoMode(mContext.getApplicationContext()) &&
                !TextUtils.isEmpty(geoDistance)) {
            try {
                //Parse Distance to Float
                Float distance = Float.parseFloat(geoDistance);

                //Set Distance
                holder.getTextViewSearchCardDistance().setText(String.format
                        (mContext.getResources().getString(R.string
                                        .container_distance),
                                AppUtil.formatFloatDigits(distance, 2, 2)));
                holder.getTextViewSearchCardDistance().setVisibility(TextView.VISIBLE);

                //Set Goe Icon Visibility
                holder.getImageViewSearchCardLocation().setVisibility(ImageView.VISIBLE);

            } catch (NumberFormatException numberFormat) {
                //Do Nothing...
            }
        }

        //Set Branding
        if (itemObject.optJSONArray("tags") != null && itemObject.optJSONArray("tags").length() > 0) {

            //Check for Dineout Plus Tag
            if (checkIfDineoutPlus(itemObject.optJSONArray("tags"))) {
                //Set Dineout Plus Image
                holder.getImageViewCarlsbergLogo().setImageResource(R.drawable.dineout_plus_tag);

                //Set Visibility
                holder.getImageViewCarlsbergLogo().setVisibility(ImageView.VISIBLE);

            } else if (checkIfCarlsberg(itemObject.optJSONArray("tags"))) {
                //Set Carlsberg Image
                holder.getImageViewCarlsbergLogo().setImageResource(R.drawable.img_carlsberg_listing);

                //Set Visibility
                holder.getImageViewCarlsbergLogo().setVisibility(ImageView.VISIBLE);
            } else {
                //Hide Logo
                holder.getImageViewCarlsbergLogo().setVisibility(ImageView.GONE);
            }
        } else {
            //Hide Logo
            holder.getImageViewCarlsbergLogo().setVisibility(ImageView.GONE);
        }

        if (!AppUtil.isStringEmpty(itemObject.optString("event_text"))) {
            if (!(itemObject.optString("event_text").equalsIgnoreCase("0"))) {
                holder.getTextViewSearchCardRestaurantName().setVisibility(View.VISIBLE);
                holder.getTextViewSearchCardRestaurantName().setText(itemObject.optString("event_text"));
            } else {
                holder.getTextViewSearchCardRestaurantName().setVisibility(View.GONE);
            }
        } else {
            holder.getTextViewSearchCardRestaurantName().setVisibility(View.GONE);
        }

        //Set Address
        String address = itemObject.optString("locality_name", "");
        String areaName = itemObject.optString("area_name", "");
        address = ((AppUtil.isStringEmpty(address)) ? areaName : (address + ", " + areaName));

        if (!AppUtil.isStringEmpty(address)) {
            holder.getRestAddress().setText(address);
        }

        //Set Black Text Color
        holder.getRestAddress().setTextColor(mContext.getResources().getColor(R.color.white));
        holder.getCostForTwo().setTextColor(mContext.getResources().getColor(R.color.grey_4D));

        JSONArray cuisineArr = itemObject.optJSONArray("cuisine");
        String mainCusineName = "";
        if (cuisineArr != null && cuisineArr.length() > 0) {
            int cuisineCount = cuisineArr.length();
            cuisineCount = ((cuisineCount > 2) ? 2 : cuisineCount);


            for (int index = 0; index < cuisineCount; index++) {
                //Get Tag Name
                String cuisineName = cuisineArr.optString(index);

                if (index == 0) {
                    mainCusineName = cuisineName;
                } else {
                    mainCusineName = mainCusineName + ", " + cuisineName;
                }
            }
        }

        if (!AppUtil.isStringEmpty(mainCusineName) && !AppUtil.isStringEmpty(itemObject.optString("costFor2"))) {
            //Set Amount for 2
            holder.getCostForTwo().setText((String.format
                    (mContext.getResources().getString(R.string.container_amount_for_2), itemObject.optString("costFor2"))) + " approx" + " | " + mainCusineName);
        } else {
            if (AppUtil.isStringEmpty(mainCusineName)) {
                holder.getCostForTwo().setText((String.format
                        (mContext.getResources().getString(com.dineout.recycleradapters.R.string.container_amount_for_2), itemObject.optString("costFor2"))) + " approx");

            } else if (AppUtil.isStringEmpty(itemObject.optString("costFor2"))) {
                holder.getCostForTwo().setText(mainCusineName);
            }
        }

        showOffers(holder, itemObject);
        showReserveUploadBillButtons(holder, itemObject);
        holder.getButtonSearchCardReserve().setOnClickListener(this);

    }

//    private void showReserveUploadBillButtons(RestaurantViewHolder viewHolder, JSONObject itemObject) {
//        viewHolder.getButtonSearchCardReserve().removeAllViews();
//        JSONArray array = itemObject.optJSONArray("cta");
//        if (array != null && array.length() > 0) {
//            for (int i = 0; i < array.length(); i++) {
//                try {
//                    final JSONObject ctaObj = array.getJSONObject(i);
//                    if (ctaObj != null) {
//                        final int action = ctaObj.optInt("action");
//                        viewHolder.getButtonSearchCardReserve().setVisibility(View.VISIBLE);
//                        Button myButton = new Button(mContext);
//                        myButton.setText(ctaObj.optString("button_text"));
//
//                        if (ctaObj.optInt("action") == 4) {
//                            setColorSelector(myButton, R.drawable.round_rectangle_white_primary_border_button_selector, mContext.getResources().getColorStateList(R.color.primary_white_text_selector));
//
//                        } else if (ctaObj.optInt("action") == 1) {
//                            setButtonColor(myButton, R.drawable.gen_round_primary_button_selector, mContext.getResources().getColor(R.color.white));
//
//                        } else if (ctaObj.optInt("action") == 2) {
//                            setButtonColor(myButton, R.drawable.upload_bill_background, mContext.getResources().getColor(R.color.colorPrimary));
//                        }
//                        myButton.setTag(itemObject.optString("r_id"));
//                        myButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                String resId = (String) v.getTag();
//                                if (action == 1) {
//                                    //reserve
//                                    getOnCardClickedListener().onReserveButtonClicked(ctaObj);
//                                } else if (action == 4) {
//                                    //upload
//                                    getOnCardClickedListener().onUploadBillClicked(ctaObj);
//                                } else if (action == 2) {
//                                    //pay now
//                                    getOnCardClickedListener().onPayBill(ctaObj);
//                                }
//                            }
//                        });
//
//                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                        lp.setMargins(0, 0, 0, 7);
//                        viewHolder.getButtonSearchCardReserve().addView(myButton, lp);
//                    }
//
//                } catch (JSONException ex) {
//                    // Do Nothing...
//                }
//            }
//        } else {
//            viewHolder.getButtonSearchCardReserve().setVisibility(View.GONE);
//        }
//    }

    private void showReserveUploadBillButtons(RestaurantViewHolder viewHolder, final JSONObject itemObject) {
        try {
            // set cta header text
            String ctaHeaderText = itemObject.optString("ctaHeader", "");
            if (TextUtils.isEmpty(ctaHeaderText)) {
                viewHolder.getCTAHeaderText().setVisibility(View.GONE);

            } else {
                viewHolder.getCTAHeaderText().setVisibility(View.VISIBLE);
                viewHolder.getCTAHeaderText().setText(ctaHeaderText);
            }

            // set text
            JSONArray array = itemObject.optJSONArray("cta");
            if (array != null) {
                viewHolder.getBottomButtonsWrapper().setVisibility(View.VISIBLE);

                if (array.length() > 0) {
                    viewHolder.getBottomLeftBtnWrapper().setVisibility(View.VISIBLE);

                    final JSONObject ctaObj = array.getJSONObject(0);
                    if (ctaObj != null) {
                        String colorString = ctaObj.optString("ctaTextColor", "");
                        if (!colorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                            colorString = "#FFFFFF";
                        }
                        int textColor = Color.parseColor(colorString);

                        // set left button title text color
                        viewHolder.getLeftBtnTitle().setTextColor(textColor);

                        // title
                        String titleText = ctaObj.optString("button_text");
                        if (TextUtils.isEmpty(titleText)) {
                            viewHolder.getLeftBtnTitle().setText("");
                            viewHolder.getLeftBtnTitle().setVisibility(View.GONE);
                        } else {
                            viewHolder.getLeftBtnTitle().setText(titleText);
                            viewHolder.getLeftBtnTitle().setVisibility(View.VISIBLE);
                        }

                        // set left button sub title text color
                        viewHolder.getLeftBtnSubTitle().setTextColor(textColor);

                        // sub title
                        String subTitleText = ctaObj.optString("subtitle_button_text");
                        if (TextUtils.isEmpty(subTitleText)) {
                            viewHolder.getLeftBtnSubTitle().setText("");
                            viewHolder.getLeftBtnSubTitle().setVisibility(View.GONE);
                        } else {
                            viewHolder.getLeftBtnSubTitle().setText(subTitleText);
                            viewHolder.getLeftBtnSubTitle().setVisibility(View.VISIBLE);
                        }

                        // set bg
                        String bgColorString = ctaObj.optString("ctaColor", "");
                        if (!bgColorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                            bgColorString = "#fa5757";
                        }
                        int bgColor = Color.parseColor(bgColorString);
                        GradientDrawable leftShape = (GradientDrawable) viewHolder.getBottomLeftBtnWrapper().getBackground();
                        leftShape.setColor(bgColor);

                        // enable/disable
                        boolean isEnabled = ctaObj.optInt("enable") == 1;
                        viewHolder.getBottomLeftBtnWrapper().setEnabled(isEnabled);

                        // tag object
                        viewHolder.getBottomLeftBtnWrapper().setTag(itemObject);

                        // set onclick
                        viewHolder.getBottomLeftBtnWrapper().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int action = ctaObj.optInt("action");
                                if (action == 1) {
                                    //reserve
                                    getOnCardClickedListener().onReserveButtonClicked(itemObject);
                                } else if (action == 2) {
                                    //pay now
                                    getOnCardClickedListener().onPayBill(itemObject);
                                } else if (action == 4) {
                                    //upload
                                    getOnCardClickedListener().onUploadBillClicked(itemObject);
                                }
                            }
                        });
                    }
                } else {
                    viewHolder.getBottomLeftBtnWrapper().setVisibility(View.GONE);
                    viewHolder.getLeftBtnTitle().setVisibility(View.GONE);
                    viewHolder.getLeftBtnSubTitle().setVisibility(View.GONE);
                }

                if (array.length() > 1) {
                    viewHolder.getBottomRightBtnWrapper().setVisibility(View.VISIBLE);

                    final JSONObject ctaObj = array.getJSONObject(1);
                    if (ctaObj != null) {
                        String colorString = ctaObj.optString("ctaTextColor", "");
                        if (!colorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                            colorString = "#FFFFFF";
                        }
                        int textColor = Color.parseColor(colorString);

                        // set right button title text color
                        viewHolder.getRightBtnTitle().setTextColor(textColor);

                        // title
                        String titleText = ctaObj.optString("button_text");
                        if (TextUtils.isEmpty(titleText)) {
                            viewHolder.getRightBtnTitle().setText("");
                            viewHolder.getRightBtnTitle().setVisibility(View.GONE);
                        } else {
                            viewHolder.getRightBtnTitle().setText(titleText);
                            viewHolder.getRightBtnTitle().setVisibility(View.VISIBLE);
                        }

                        // set right button sub title text color
                        viewHolder.getRightBtnSubTitle().setTextColor(textColor);

                        // sub title
                        String subTitleText = ctaObj.optString("subtitle_button_text");
                        if (TextUtils.isEmpty(subTitleText)) {
                            viewHolder.getRightBtnSubTitle().setText("");
                            viewHolder.getRightBtnSubTitle().setVisibility(View.GONE);
                        } else {
                            viewHolder.getRightBtnSubTitle().setText(subTitleText);
                            viewHolder.getRightBtnSubTitle().setVisibility(View.VISIBLE);
                        }

                        // set bg
                        String bgColorString = ctaObj.optString("ctaColor", "");
                        if (!bgColorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                            bgColorString = "#fa5757";
                        }
                        int bgColor = Color.parseColor(bgColorString);
                        GradientDrawable rightShape = (GradientDrawable) viewHolder.getBottomRightBtnWrapper().getBackground();
                        rightShape.setColor(bgColor);

                        // enable/disable
                        boolean isEnabled = ctaObj.optInt("enable") == 1;
                        viewHolder.getBottomRightBtnWrapper().setEnabled(isEnabled);

                        // tag object
                        viewHolder.getBottomRightBtnWrapper().setTag(itemObject);

                        // set onclick
                        viewHolder.getBottomRightBtnWrapper().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int action = ctaObj.optInt("action");
                                if (action == 1) {
                                    //reserve
                                    getOnCardClickedListener().onReserveButtonClicked(itemObject);
                                } else if (action == 2) {
                                    //pay now
                                    getOnCardClickedListener().onPayBill(itemObject);
                                } else if (action == 4) {
                                    //upload
                                    getOnCardClickedListener().onUploadBillClicked(itemObject);
                                }
                            }
                        });
                    }
                } else {
                    viewHolder.getBottomRightBtnWrapper().setVisibility(View.GONE);
                    viewHolder.getRightBtnTitle().setVisibility(View.GONE);
                    viewHolder.getRightBtnSubTitle().setVisibility(View.GONE);
                }

            } else {
                viewHolder.getBottomButtonsWrapper().setVisibility(View.GONE);
                viewHolder.getCTAHeaderText().setVisibility(View.GONE);
                viewHolder.getBottomLeftBtnWrapper().setVisibility(View.GONE);
                viewHolder.getBottomRightBtnWrapper().setVisibility(View.GONE);
                viewHolder.getLeftBtnTitle().setVisibility(View.GONE);
                viewHolder.getRightBtnTitle().setVisibility(View.GONE);
                viewHolder.getLeftBtnSubTitle().setVisibility(View.GONE);
                viewHolder.getRightBtnSubTitle().setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // Exception
        }
    }

    private void setButtonColor(Button btn, int background, int color) {
        btn.setBackgroundResource(background);
        btn.setTextColor(color);
    }

    private void setColorSelector(Button btn, int background, ColorStateList color) {
        btn.setTextColor(color);
        btn.setBackgroundResource(background);
    }

    private void showOffers(RestaurantViewHolder viewHolder, JSONObject itemObject) {
        // Get Offer Data
        JSONObject offerDataObj = itemObject.optJSONObject("offer_data");
        if (offerDataObj != null) {
            boolean hasOffer = false;

            JSONArray offersArr = offerDataObj.optJSONArray("offers");

            if (offersArr != null && offersArr.length() > 0) {
                JSONObject offerJsonObject = offersArr.optJSONObject(0);

                if (offerJsonObject != null) {
                    // Get Normal Offer Text
                    String offerText = offerJsonObject.optString("title_1");

                    if (!AppUtil.isStringEmpty(offerText)) {
                        hasOffer = true;

                        // Set Offer Text
                        viewHolder.getTextViewNormalOffer().setText(offerText);

                        // Set Other Offer Text - +3 more Offers
                        String moreOffer = offerDataObj.optString("more_offers_text");
                        if (!AppUtil.isStringEmpty(moreOffer)) {
                            hasOffer = true;
                            viewHolder.getLinearLayoutNormalOfferSection().setVisibility(View.VISIBLE);
                            viewHolder.getTextViewNormalOtherOffer().setText(moreOffer);
                            if(!TextUtils.isEmpty(offerDataObj.optString("textColor"))) {
                                viewHolder.getTextViewNormalOtherOffer().setTextColor(Color.parseColor(offerDataObj.optString("textColor")));
                            }
                            viewHolder.getTextViewNormalOtherOffer().setVisibility(View.VISIBLE);
                        }else {
                            viewHolder.getTextViewNormalOtherOffer().setVisibility(View.GONE);
                        }

                        // Show Normal Offer Section
                        viewHolder.getLinearLayoutNormalOfferSection().setVisibility(View.VISIBLE);
                        viewHolder.getLinearLayoutNormalOfferSection().setTag(itemObject);
                        viewHolder.getLinearLayoutNormalOfferSection().setOnClickListener(this);
                    }
                }
            }

            viewHolder.getRelativeLayoutSmartPayOfferSection().setVisibility(
                    itemObject.optBoolean("is_accept_payment") ? View.VISIBLE : View.GONE);

//            // Show SmartPay Section
//            if (isTrue(itemObject, "is_accept_payment")) {
//                String smartPatOffer = offerDataObj.optString("smartpay_text");
//                if (!AppUtil.isStringEmpty(smartPatOffer)) {
//                    // Check if Offer is available
//                    if (hasOffer) {
////                        viewHolder.getViewOfferDivider().setVisibility(View.VISIBLE);
//
//                    } else {
//                        hasOffer = true;
//                    }
//
//                    // Set Text
//                    viewHolder.getTextViewSmartPayOffer().setText(smartPatOffer);
//                    viewHolder.smartPayIndicator.setTag(itemObject);
//                    viewHolder.smartPayIndicator.setOnClickListener(this);
//
//                    // Show SmartPay Offer Section
//                    viewHolder.getRelativeLayoutSmartPayOfferSection().setVisibility(View.VISIBLE);
//                }
//            }

            // Show SmartPay /upload bill indicator

            String smartPatOffer = offerDataObj.optString("smartpay_text");
            if (!AppUtil.isStringEmpty(smartPatOffer)) {

                if (hasOffer) {
         //           viewHolder.getViewOfferDivider().setVisibility(View.VISIBLE);
                    } else {
                        hasOffer = true;
                    }

                // Set Text
                int lastHashPos;
                if ((lastHashPos = smartPatOffer.lastIndexOf("#")) > 0){
                    lastHashPos--;
                    smartPatOffer = smartPatOffer.replace("#", "");
                    SpannableString spanStr = new SpannableString(smartPatOffer);
                    spanStr.setSpan(new ForegroundColorSpan(Color.parseColor(offerDataObj.optString("textColor"))),lastHashPos, smartPatOffer.length(), 0);
                    viewHolder.getTextViewSmartPayOffer().setText(spanStr);

                }else{
                    viewHolder.getTextViewSmartPayOffer().setText(smartPatOffer);
                }

                // Show SmartPay Offer Section
                viewHolder.getRelativeLayoutSmartPayOfferSection().setVisibility(View.VISIBLE);
            }

            int tagId = offerDataObj.optInt("tagId");
            String backgroundColor = offerDataObj.optString("tagColor");
            if(tagId == 0){
                viewHolder.smartPayIndicator.setVisibility(View.GONE);
            }else{

                GradientDrawable gDraw = (GradientDrawable) viewHolder.smartPayIndicator.getBackground();

                if(!TextUtils.isEmpty(backgroundColor)) {
                    gDraw.setColor(Color.parseColor(backgroundColor));
                    if (tagId == 1) {
                        ((TextView) viewHolder.smartPayIndicator).setText("smartPay");

                    } else if (tagId == 2) {
                        ((TextView) viewHolder.smartPayIndicator).setText("Upload Bill");
                    }
                }

                viewHolder.smartPayIndicator.setVisibility(View.VISIBLE);
                try {
                    if (offerDataObj != null) {
                        offerDataObj.put("profile_name", itemObject.optString("profile_name"));
                        offerDataObj.put("r_id", itemObject.optString("r_id"));

                    }
                }catch (Exception ex){
                    //exception
                }
                viewHolder.getRelativeLayoutSmartPayOfferSection().setTag(offerDataObj);
                viewHolder.getRelativeLayoutSmartPayOfferSection().setOnClickListener(this);
            }



            // Check if No Offer Data is available
            if (hasOffer) {
                // Set Section Title
                String title = offerDataObj.optString("title");
                title = ((AppUtil.isStringEmpty(title)) ? "Offers available now" : title);
                viewHolder.getTextViewOfferTitle().setText(title);

                // Show Complete Offer Section
                viewHolder.getLinearLayoutOfferSection().setVisibility(View.VISIBLE);

            }
            viewHolder.getLinearLayoutOfferSection().setTag(itemObject);
            viewHolder.getLinearLayoutOfferSection().setOnClickListener(this);
        }
    }

    private boolean checkIfDineoutPlus(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            if (array.optString(i).equalsIgnoreCase("Dineout Plus")) {
                return true;
            }
        }

        return false;
    }

    private boolean checkIfCarlsberg(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            if (array.optString(i).equalsIgnoreCase("Carlsberg")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.relativeLayout_smartPay_offer_section) {
            JSONObject itemObject = (JSONObject) view.getTag();
            getRestaurantListClickListener().onSmartpayClick(itemObject);

        }
        else if(viewId == R.id.linearLayout_offer_section){
            JSONObject itemObject = (JSONObject) view.getTag();
            getOnOfferClickedListener().onOfferSectionClick(itemObject);
        }
        else {
            JSONObject itemObject = (JSONObject) view.getTag();
            getOnCardClickedListener().onCardClicked(itemObject);
        }
    }

    public OnCardClickedListener getOnCardClickedListener() {
        return onCardClickedListener;
    }

    public void setOnCardClickedListener(OnCardClickedListener
                                                 onCardClickedListener) {
        this.onCardClickedListener = onCardClickedListener;
    }


    public OnOfferListener getOnOfferClickedListener() {
        return onOfferClickListener;
    }

    public void setOnOfferClickedListener(OnOfferListener
                                                 onOfferClickedListener) {
        this.onOfferClickListener = onOfferClickedListener;
    }

    public interface RestaurantListClickListener {
        void onSmartpayClick(JSONObject jsonObject);
    }

    public interface OnCardClickedListener {
        void onCardClicked(JSONObject jsonObject);

        void onReserveButtonClicked(JSONObject jsonObject);

        void onUploadBillClicked(JSONObject jsonObject);

        void onPayBill(JSONObject jsonObject);

    }


    public interface OnOfferListener {

        void onOfferSectionClick(JSONObject jsonObject);

    }


    private class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private View cardView;
        private RelativeLayout relativeLayoutSearchCardParentLayout;
        private NetworkImageView imageViewRectSearchCardBackground;
        private TextView textViewSearchCardFeaturedTag;
        private ImageView imageViewCarlsbergLogo;
        private TextView textViewSearchCardRatingValue;
        private LinearLayout linearLayoutSearchCardTags;
        private TextView textViewSearchCardDistance;
        private ImageView imageViewSearchCardLocation;
        private TextView textViewSearchCardRestaurantName;
        private LinearLayout buttonSearchCardReserve;
        private RelativeLayout relativeLayoutSearchCardDetails;
        private RelativeLayout relativeLayoutRatingBackground;
        private TextView textViewRestCardNewTag;
        private TextView restName;
        private TextView resAddress;
        private TextView costforTwo;

        private View linearLayoutOfferSection;
        private TextView textViewOfferTitle;
        private View linearLayoutNormalOfferSection;
        private TextView textViewNormalOffer;
        private TextView textViewNormalOtherOffer;
        private View relativeLayoutSmartPayOfferSection;
        private TextView textViewSmartPayOffer;

        private TextView mCTAHeaderText;
        private ViewGroup bottomButtonsWrapper;
        private ViewGroup bottomLeftBtnWrapper;
        private ViewGroup bottomRightBtnWrapper;
        private TextView leftBtnTitle;
        private TextView leftBtnSubTitle;
        private TextView rightBtnTitle;
        private TextView rightBtnSubTitle;
        View smartPayIndicator;

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            cardView = itemView;
            instantiateViews();
        }

        protected void instantiateViews() {
            relativeLayoutSearchCardParentLayout = (RelativeLayout) cardView.findViewById(R.id.relativeLayoutSearchCardParentLayout);

            //Get Rectangle Background ImageView Instance
            imageViewRectSearchCardBackground = (NetworkImageView) cardView.findViewById(R.id.imageViewRectSearchCard);
            imageViewRectSearchCardBackground.setDefaultImageResId(R.drawable.default_list);
            imageViewRectSearchCardBackground.setVisibility(NetworkImageView.VISIBLE);

            //Set Featured Text
            textViewSearchCardFeaturedTag = (TextView) cardView.findViewById(R.id.textViewSearchCardFeatured);
            textViewSearchCardFeaturedTag.setVisibility(TextView.GONE);

            //Set Carlsberg Logo
            imageViewCarlsbergLogo = (ImageView) cardView.findViewById(R.id.imageViewSearchCarCarlsbergLogo);

            //Set Rating
            textViewSearchCardRatingValue = (TextView) cardView.findViewById(R.id.textViewSearchCardRating);

            //Inflate Tags
            linearLayoutSearchCardTags = (LinearLayout) cardView.findViewById(R.id.linearLayoutSearchCardTags);

            //Set Distance
            textViewSearchCardDistance = (TextView) cardView.findViewById(R.id.textViewSearchCardDistance);

            //Set Goe Icon Visibility
            imageViewSearchCardLocation = (ImageView) cardView.findViewById(R.id.imageViewSearchCardLocation);

            //Set Title
            textViewSearchCardRestaurantName = (TextView) cardView.findViewById(R.id.textViewSearchCardRestaurant);

            restName = (TextView) cardView.findViewById(R.id.antisocial_tv);

            resAddress = (TextView) cardView.findViewById(R.id.restaurant_location_tv);

            costforTwo = (TextView) cardView.findViewById(R.id.cost_for_two_tv);

            //Get Reserve Button
            buttonSearchCardReserve = (LinearLayout) cardView.findViewById(R.id.buttonSearchCardReserve);

            //Get Bottom Details Section
            relativeLayoutSearchCardDetails = (RelativeLayout) cardView.findViewById(R.id.relativeLayoutSearchCard);
            relativeLayoutSearchCardDetails.setBackgroundResource(R.drawable.rectangle_bottom_white_shape);

            //Get Rating Background Instance
            relativeLayoutRatingBackground = (RelativeLayout) cardView.findViewById(R.id.relativeLayoutRating);
            relativeLayoutRatingBackground.setBackgroundResource(R.drawable.img_bg_corner_gradient);

            // Get New Tag Instance
            textViewRestCardNewTag = (TextView) cardView.findViewById(R.id.textView_recency_tag);

            linearLayoutOfferSection = cardView.findViewById(R.id.linearLayout_offer_section);
            textViewOfferTitle = (TextView) cardView.findViewById(R.id.textView_offers_title);
            linearLayoutNormalOfferSection = cardView.findViewById(R.id.linearLayout_normal_offer_section);
            textViewNormalOffer = (TextView) cardView.findViewById(R.id.textView_Normal_offer);
            textViewNormalOtherOffer = (TextView) cardView.findViewById(R.id.textView_Normal_Other_Offer);
           // viewOfferDivider = cardView.findViewById(R.id.view_offer_divider);
            relativeLayoutSmartPayOfferSection = cardView.findViewById(R.id.relativeLayout_smartPay_offer_section);
            textViewSmartPayOffer = (TextView) cardView.findViewById(R.id.textView_smartPay_offer);
            smartPayIndicator = cardView.findViewById(R.id.textView_smartPay);
            smartPayIndicator.setVisibility(View.VISIBLE);
          //  smartPayIndicator.setOnClickListener(this);

            mCTAHeaderText = (TextView) cardView.findViewById(R.id.cta_header);
            bottomButtonsWrapper = (ViewGroup) cardView.findViewById(R.id.reservation_cta_button_layout);
            bottomLeftBtnWrapper = (ViewGroup) cardView.findViewById(R.id.left_tvs_wrapper);
            bottomRightBtnWrapper = (ViewGroup) cardView.findViewById(R.id.right_tvs_wrapper);
            leftBtnTitle = (TextView) cardView.findViewById(R.id.left_tv_title);
            leftBtnSubTitle = (TextView) cardView.findViewById(R.id.left_tv_sub_title);
            rightBtnTitle = (TextView) cardView.findViewById(R.id.right_tv_title);
            rightBtnSubTitle = (TextView) cardView.findViewById(R.id.right_tv_sub_title);
        }

        public View getLinearLayoutOfferSection() {
            return linearLayoutOfferSection;
        }

        public TextView getTextViewOfferTitle() {
            return textViewOfferTitle;
        }

        public View getLinearLayoutNormalOfferSection() {
            return linearLayoutNormalOfferSection;
        }

        public TextView getTextViewNormalOffer() {
            return textViewNormalOffer;
        }

        public TextView getTextViewNormalOtherOffer() {
            return textViewNormalOtherOffer;
        }

        public View getRelativeLayoutSmartPayOfferSection() {
            return relativeLayoutSmartPayOfferSection;
        }

        public TextView getTextViewSmartPayOffer() {
            return textViewSmartPayOffer;
        }


        public View getCardView() {
            return cardView;
        }

        public RelativeLayout getRelativeLayoutSearchCardParentLayout() {
            return relativeLayoutSearchCardParentLayout;
        }

        public NetworkImageView getImageViewRectSearchCardBackground() {
            return imageViewRectSearchCardBackground;
        }

        public ImageView getImageViewCarlsbergLogo() {
            return imageViewCarlsbergLogo;
        }

        public TextView getTextViewSearchCardRatingValue() {
            return textViewSearchCardRatingValue;
        }

        public LinearLayout getLinearLayoutSearchCardTags() {
            return linearLayoutSearchCardTags;
        }

        public TextView getTextViewSearchCardDistance() {
            return textViewSearchCardDistance;
        }

        public ImageView getImageViewSearchCardLocation() {
            return imageViewSearchCardLocation;
        }

        public TextView getTextViewSearchCardRestaurantName() {
            return textViewSearchCardRestaurantName;
        }

        public TextView getRestName() {
            return restName;
        }


        public TextView getRestAddress() {
            return resAddress;
        }

        public TextView getCostForTwo() {
            return costforTwo;
        }

        public LinearLayout getButtonSearchCardReserve() {
            return buttonSearchCardReserve;
        }

        public RelativeLayout getRelativeLayoutRatingBackground() {
            return relativeLayoutRatingBackground;
        }

        public TextView getTextViewRestCardNewTag() {
            return textViewRestCardNewTag;
        }

        public TextView getCTAHeaderText() {
            return mCTAHeaderText;
        }

        public ViewGroup getBottomButtonsWrapper() {
            return bottomButtonsWrapper;
        }

        public ViewGroup getBottomLeftBtnWrapper() {
            return bottomLeftBtnWrapper;
        }

        public ViewGroup getBottomRightBtnWrapper() {
            return bottomRightBtnWrapper;
        }

        public TextView getLeftBtnTitle() {
            return leftBtnTitle;
        }

        public TextView getLeftBtnSubTitle() {
            return leftBtnSubTitle;
        }

        public TextView getRightBtnTitle() {
            return rightBtnTitle;
        }

        public TextView getRightBtnSubTitle() {
            return rightBtnSubTitle;
        }
    }
}
