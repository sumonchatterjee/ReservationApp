package com.dineout.recycleradapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.DatePickerUtil;
import com.dineout.recycleradapters.view.widgets.NetworkRoundCornerImageView;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RestaurantDetailReservationPageAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    // Assign positive values only
    public static final int ITEM_TYPE_DATE_TIME = 1;
    private final int ITEM_TYPE_EXTRA_INFO = 2;
    private final int ITEM_TYPE_OFFERS = 3;
    private final int ITEM_TYPE_ADDITIONAL_OFFERS = 4;
    private final int ITEM_TYPE_DEALS = 5;
    private final int ITEM_TYPE_EVENT_CARD = 6;

    private final int REQUEST_CODE_ADDITIONAL_OFFER_INFO = 101;

    private final String dateTimeSectionKey = "dateTimeSection";
    private final String extraInfoSectionKey = "extraInfoSection";
    private final String offerSectionKey = "offers";
    private final String additionalOfferSectionKey = "additionalOffer";
    private final String dealSectionKey = "deals";
    private final String eventCardSectionKey = "reservationEvent";

    private Context mContext;
    private JSONObject restDetailJsonObject;
    private int selectedOfferId;
    private int totalDealSelectedCount = 0;
    private long diningDateTime;
    private HashMap<String, String> additionOfferMsgMap = new HashMap<>();
    private int userEarnings;
    private String earningsText;

    private DineoutNetworkManager networkManager;
    private ReservationTabClickListener clickListener;
    private JSONArray selectedDealArray;

    private boolean isOfferSelectable;
    private boolean isOfferSelected;
    private boolean isAdditionalOfferAvailable;
    private Bundle selectedOfferBundle;
    private Bundle bookingBundle = new Bundle();

    private HashMap<String, Integer> dealVsCount = new HashMap<>();
    private int additionalOfferItemPosition;

    // Constructor
    public RestaurantDetailReservationPageAdapter(Context context) {
        // Set Context
        mContext = context;
    }

    public void setDataForReservationTabAdapter(JSONArray jsonLayoutArray, JSONObject restDetailJsonObject) {
        // Set Data
        setJsonArray(jsonLayoutArray);

        this.restDetailJsonObject = restDetailJsonObject;

        // Fetch Dining Date Time
        fetchDiningDateTime();

        // Set Offer Availability Status
        setOfferAvailabilityStatus();

        // Set Additional Offer Availability Status
        setAdditionalOfferAvailabilityStatus();

        // Set Selected Deals
        setSelectedDeals();
    }

    public void setReservationTabClickListener(ReservationTabClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setNetworkManager(DineoutNetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public JSONArray getSelectedDealArray() {
        return selectedDealArray;
    }

    public long getDiningDateTime() {
        return diningDateTime;
    }

    private void setDiningDateTime(long diningDateTime) {
        this.diningDateTime = diningDateTime;
    }

    public int getUserEarnings() {
        return userEarnings;
    }

    public void setUserEarnings(int userEarnings) {
        this.userEarnings = userEarnings;
    }

    public String getEarningsText() {
        return earningsText;
    }

    public void setEarningsText(String earningsText) {
        this.earningsText = earningsText;
    }

    public boolean isAdditionalOfferAvailable() {
        return isAdditionalOfferAvailable;
    }

    public void setAdditionalOfferAvailable(boolean additionalOfferAvailable) {
        isAdditionalOfferAvailable = additionalOfferAvailable;
    }

    public int getAdditionalOfferItemPosition() {
        return additionalOfferItemPosition;
    }

    public void setAdditionalOfferItemPosition(int additionalOfferItemPosition) {
        this.additionalOfferItemPosition = additionalOfferItemPosition;
    }

    public boolean isOfferSelectable() {
        return isOfferSelectable;
    }

    public void setOfferSelectable(boolean offerSelectable) {
        isOfferSelectable = offerSelectable;
    }

    public boolean isOfferSelected() {
        return isOfferSelected;
    }

    public void setOfferSelected(boolean offerSelected) {
        isOfferSelected = offerSelected;
    }

    public Bundle getSelectedOfferBundle() {
        return selectedOfferBundle;
    }

    public void setSelectedOfferBundle(Bundle selectedOfferBundle) {
        this.selectedOfferBundle = selectedOfferBundle;
    }

    public int getSelectedOfferId() {
        return selectedOfferId;
    }

    public void setSelectedOfferId(int selectedOfferId) {
        this.selectedOfferId = selectedOfferId;
    }

    public HashMap<String, Integer> getDealVsCount() {
        return dealVsCount;
    }

    public void setDealVsCount(HashMap<String, Integer> dealVsCount) {
        this.dealVsCount = dealVsCount;
    }

    public int getTotalDealSelectedCount() {
        return totalDealSelectedCount;
    }

    public void setTotalDealSelectedCount(int totalDealSelectedCount) {
        this.totalDealSelectedCount = totalDealSelectedCount;
    }

    private void fetchDiningDateTime() {
        // Check for NULL
        if (restDetailJsonObject != null) {

            // Get Data
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");
            if (dataJsonObject != null) {

                // Get Selected Date Time
                final long dateTimeInMilliSeconds = dataJsonObject.optLong(AppConstant.JSON_PARAM_DINING_DATE_TIME);

                setDiningDateTime(dateTimeInMilliSeconds);
            }
        }
    }

    private void setOfferAvailabilityStatus() {
        // Reset Values
        isOfferSelected = false;
        boolean offerSelectableStatus = false;

        // Check for NULL
        if (restDetailJsonObject != null) {

            // Get Reservation Layout
            JSONArray reservationLayoutJsonArray = getJsonArray();

            if (reservationLayoutJsonArray != null && reservationLayoutJsonArray.length() > 0) {
                int layoutSize = reservationLayoutJsonArray.length();
                for (int index = 0; index < layoutSize; index++) {
                    JSONObject layoutJsonObject = reservationLayoutJsonArray.optJSONObject(index);

                    if (layoutJsonObject != null) {
                        // Get Key
                        String key = layoutJsonObject.optString("key");

                        if (!AppUtil.isStringEmpty(key) && key.equalsIgnoreCase(offerSectionKey)) {
                            // Get Data
                            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");
                            if (dataJsonObject != null) {

                                // Get Offer Section
                                JSONObject offerSectionJsonObject = dataJsonObject.optJSONObject(offerSectionKey);
                                if (offerSectionJsonObject != null) {

                                    // Get Offers
                                    JSONArray offerJsonArray = offerSectionJsonObject.optJSONArray("offers");

                                    if (offerJsonArray != null && offerJsonArray.length() > 0) {
                                        int offerSize = offerJsonArray.length();

                                        for (int count = 0; count < offerSize; count++) {
                                            final JSONObject offerJsonObject = offerJsonArray.optJSONObject(count);

                                            if (offerJsonObject != null) {

                                                // Set Offer Status
                                                int offerLive = offerJsonObject.optJSONObject("availability").optInt("status");

                                                if (offerLive == 1) { //Active
                                                    // Set Flag
                                                    offerSelectableStatus = true;

                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Set Offer is Selectable
        setOfferSelectable(offerSelectableStatus);
    }

    private void setAdditionalOfferAvailabilityStatus() {
        boolean additionalOfferStatus = false;

        // Check for NULL
        if (restDetailJsonObject != null) {

            // Get Reservation Layout
            JSONArray reservationLayoutJsonArray = getJsonArray();

            if (reservationLayoutJsonArray != null && reservationLayoutJsonArray.length() > 0) {
                int layoutSize = reservationLayoutJsonArray.length();
                for (int index = 0; index < layoutSize; index++) {
                    JSONObject layoutJsonObject = reservationLayoutJsonArray.optJSONObject(index);

                    if (layoutJsonObject != null) {
                        // Get Key
                        String key = layoutJsonObject.optString("key");

                        if (!AppUtil.isStringEmpty(key) && key.equalsIgnoreCase(additionalOfferSectionKey)) {
                            // Get Data
                            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");
                            if (dataJsonObject != null) {

                                // Get Additional Offer Section
                                JSONObject additionalOfferJsonObject = dataJsonObject.optJSONObject(additionalOfferSectionKey);
                                if (additionalOfferJsonObject != null) {

                                    // Get Offers
                                    JSONArray additionalOfferJsonArray = additionalOfferJsonObject.optJSONArray("additionalOffer");

                                    if (additionalOfferJsonArray != null && additionalOfferJsonArray.length() > 0) {
                                        additionalOfferStatus = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Set Additional Offer Availability Status
        setAdditionalOfferAvailable(additionalOfferStatus);
    }

    private void setSelectedDeals() {
        // Check for NULL
        if (restDetailJsonObject != null && restDetailJsonObject.optJSONObject("data") != null) {
            // Get Data JSON Object
            final JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Selected Deals from JSON
            HashMap<String, Integer> dealVsCountMap =
                    (HashMap<String, Integer>) dataJsonObject.opt(AppConstant.BUNDLE_SELECTED_DEAL_COUNT);

            // Check for NULL
            if (dealVsCountMap != null) {
                // Set Selected Deals
                setDealVsCount(dealVsCountMap);
                setTotalDealSelectedCount(getTotalSelectedDealsCount(dealVsCountMap));
            }
        }
    }

    private int getTotalSelectedDealsCount(HashMap<String, Integer> dealVsCountMap) {
        int totalQuantity = 0;

        if (dealVsCountMap != null && dealVsCountMap.size() > 0) {
            Iterator<Map.Entry<String, Integer>> iterator = dealVsCountMap.entrySet().iterator();

            while (iterator.hasNext()) {
                totalQuantity += iterator.next().getValue();
            }
        }

        return totalQuantity;
    }

    @Override
    protected int defineItemViewType(int position) {
        // Get Section Key
        String sectionKey = getJsonArray().optJSONObject(position).optString("key");

        switch (sectionKey) {
            case dateTimeSectionKey:
                return ITEM_TYPE_DATE_TIME;

            case extraInfoSectionKey:
                return ITEM_TYPE_EXTRA_INFO;

            case offerSectionKey:
                return ITEM_TYPE_OFFERS;

            case additionalOfferSectionKey: {
                setAdditionalOfferItemPosition(position);
                return ITEM_TYPE_ADDITIONAL_OFFERS;
            }

            case dealSectionKey:
                return ITEM_TYPE_DEALS;

            case eventCardSectionKey:
                return ITEM_TYPE_EVENT_CARD;

            default:
                return ITEM_VIEW_TYPE_EMPTY;
        }
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check on Item Type
        if (viewType == ITEM_TYPE_DATE_TIME) {
            return new DateTimeViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.rest_date_time_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_EXTRA_INFO) {
            return new ExtraInfoViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.extra_info_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_OFFERS) {
            return new OfferViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.offer_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_ADDITIONAL_OFFERS) {
            return new AdditionalOfferViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.additional_offer_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_DEALS) {
            return new DealViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.deals_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_EVENT_CARD) {
            return new EventCardViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.restaurant_event_section, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item Type
        int itemViewType = holder.getItemViewType();

        if (itemViewType == ITEM_TYPE_DATE_TIME) {
            showDateTimeSection((DateTimeViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_EXTRA_INFO) {
            showExtraSection((ExtraInfoViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_OFFERS) {
            showOfferSection((OfferViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_ADDITIONAL_OFFERS) {
            showAdditionalOfferSection((AdditionalOfferViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_DEALS) {
            showDealSection((DealViewHolder) holder, listItem);

        } else if (itemViewType == ITEM_TYPE_EVENT_CARD) {
            showEventCardSection((EventCardViewHolder) holder, listItem);
        }
    }

    private void showDateTimeSection(DateTimeViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {
            // Get Data JSON Object
            final JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Set Title
            if (!AppUtil.isStringEmpty(jsonObject.optString("title"))) {
                viewHolder.getTextViewTitle().setText(jsonObject.optString("title"));
            }

            DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();
            HashMap<String, String> dateTimeMap = datePickerUtil.getDateTimeFromTimestamp(getDiningDateTime());

            // Set Date
            viewHolder.getTextViewDate().setText(dateTimeMap.get(AppConstant.DATE_PICKER_DATE));

            // Set Time
            viewHolder.getTextViewTime().setText(dateTimeMap.get(AppConstant.DATE_PICKER_TIME));

            // Add Dining Date Time in Milliseconds to bundle
            bookingBundle.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP, getDiningDateTime());

            // Set Click Listener
            viewHolder.getLinearLayoutDateTimeSection().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Click
                    clickListener.onDateTimeClick(getDiningDateTime(),
                            AppUtil.convertSecondsToMilliseconds(dataJsonObject.optLong("nextMinimumBookingTime", 0L)));
                }
            });
        }
    }

    private void showExtraSection(ExtraInfoViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {
            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Set Title
            if (!AppUtil.isStringEmpty(jsonObject.optString("title"))) {
                viewHolder.getTextViewTitle().setText(jsonObject.optString("title"));
            }

            // Set Info
            viewHolder.getLinearLayoutExtraInfoList().removeAllViews();
            JSONArray extraInfoJsonArray = dataJsonObject.optJSONArray("extraInfo");
            if (extraInfoJsonArray != null && extraInfoJsonArray.length() > 0) {
                int arraySize = extraInfoJsonArray.length();
                for (int index = 0; index < arraySize; index++) {
                    JSONObject jsonObjectInfo = extraInfoJsonArray.optJSONObject(index);

                    if (jsonObjectInfo != null) {
                        // Get View
                        View extraInfoView = LayoutInflater.from(mContext).
                                inflate(R.layout.extra_info_list_item, null, false);

                        // Set Icon
                        NetworkImageView imageViewInfoIcon = (NetworkImageView) extraInfoView.findViewById(R.id.imageView_info_icon);
                        if (AppUtil.isStringEmpty(jsonObjectInfo.optString("iconUrl"))) {
                            imageViewInfoIcon.setVisibility(ImageView.GONE);

                        } else {
                            imageViewInfoIcon.setImageUrl(jsonObjectInfo.optString("iconUrl"),
                                    ImageRequestManager.getInstance(mContext).getImageLoader());
                            imageViewInfoIcon.setVisibility(ImageView.VISIBLE);
                        }

                        // Set Text
                        TextView textViewExtraInfoName = (TextView) extraInfoView.findViewById(R.id.textView_extra_info_name);
                        if (!AppUtil.isStringEmpty(jsonObjectInfo.optString("text"))) {
                            textViewExtraInfoName.setText(jsonObjectInfo.optString("text"));
                        }

                        // Add View to the View
                        viewHolder.getLinearLayoutExtraInfoList().addView(extraInfoView);
                    }
                }
            }
        }
    }

    private void showOfferSection(final OfferViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            final JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Check for Edit Booking Offer Id
            if (dataJsonObject.opt(AppConstant.JSON_PARAM_EDIT_BOOKING_OFFER_ID) != null) {
                selectedOfferId = dataJsonObject.optInt(AppConstant.JSON_PARAM_EDIT_BOOKING_OFFER_ID);

            }

            // Get Offer Section
            JSONObject offerSectionJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (offerSectionJsonObject != null) {

                // Set Title
                if (!AppUtil.isStringEmpty(offerSectionJsonObject.optString("title"))) {
                    viewHolder.getTextViewOfferTitle().setText(offerSectionJsonObject.optString("title"));
                }

                // Set Sub Title
                if (!AppUtil.isStringEmpty(offerSectionJsonObject.optString("subTitle"))) {
                    viewHolder.getTextViewOfferSubTitle().setText(
                            AppUtil.getColoredText(offerSectionJsonObject.optString("subTitle"),
                                    mContext.getResources().getColor(R.color.green)));
                    viewHolder.getTextViewOfferSubTitle().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getTextViewOfferSubTitle().setVisibility(View.GONE);
                }

                // Set visibility of Offer Section Book Now button
                viewHolder.getButtonOfferBookNow().setVisibility(isAdditionalOfferAvailable() ? View.GONE : View.VISIBLE);

                // Set Offers
                viewHolder.getRadioGroupOfferList().removeAllViews();
                JSONArray offerJsonArray = offerSectionJsonObject.optJSONArray("offers");
                if (offerJsonArray != null && offerJsonArray.length() > 0) {
                    int arraySize = offerJsonArray.length();

                    // Pre-select first active offer
                    if (dataJsonObject.opt(AppConstant.JSON_PARAM_EDIT_BOOKING_OFFER_ID) == null) { // Not Edit Mode
                        // Check if Offers are Selectable and are not yet selected
                        if (isOfferSelectable() && !isOfferSelected()) {
                            for (int index = 0; index < arraySize; index++) {
                                final JSONObject offerJsonObject = offerJsonArray.optJSONObject(index);

                                if (offerJsonObject != null) {
                                    // Set Offer Status
                                    int offerStatus = offerJsonObject.optJSONObject("availability").optInt("status");

                                    if (offerStatus == 1) { //In-Active
                                        // Set Offer Selected
                                        selectedOfferId = offerJsonObject.optInt("offerID");
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    for (int index = 0; index < arraySize; index++) {
                        final JSONObject offerJsonObject = offerJsonArray.optJSONObject(index);

                        if (offerJsonObject != null) {
                            // Get View
                            View offerView = LayoutInflater.from(mContext).
                                    inflate(R.layout.offer_list_item, null, false);

                            // Get Offer Id
                            final int offerId = offerJsonObject.optInt("offerID");

                            // Get Offer Section
                            View offerSectionView = offerView.findViewById(R.id.relativeLayout_offer_section);

                            // Set Offer Title
                            final RadioButton radioButtonOffer = (RadioButton) offerView.findViewById(R.id.radioButton_offer);
                            radioButtonOffer.setTag(offerJsonObject);
                            if (!AppUtil.isStringEmpty(offerJsonObject.optString("title"))) {
                                radioButtonOffer.setText(offerJsonObject.optString("title"));
                            }

                            // Set Offer Validity
                            TextView textViewOfferValidity = (TextView) offerView.findViewById(R.id.textView_offer_validity);
                            if (offerJsonObject.optJSONObject("availability") != null &&
                                    !AppUtil.isStringEmpty(offerJsonObject.optJSONObject("availability").optString("message"))) {
                                textViewOfferValidity.setText(offerJsonObject.optJSONObject("availability").optString("message"));
                            }

                            // Get Right Arrow Instance
                            ImageView imageViewOfferArrow = (ImageView) offerView.findViewById(R.id.imageView_offer_arrow);

                            // Set Offer Status
                            int offerStatus = offerJsonObject.optJSONObject("availability").optInt("status");
                            if (offerStatus == 0) { //In-Active
                                // Set Radio Button Color and Offer Text Color
                                radioButtonOffer.setEnabled(false);

                                // Set Validity Text Color
                                textViewOfferValidity.setEnabled(false);

                                // Set Right Arrow Color
                                imageViewOfferArrow.setImageResource(R.drawable.ic_light_grey_arrow_right);

                            } else {
                                // Set Radio Button Color and Offer Text Color
                                radioButtonOffer.setEnabled(true);

                                // Set On Click
                                radioButtonOffer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //track offer selected
//                                        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_restaurant_detail),
//                                                mContext.getString(R.string.ga_action_selectoffer), null);

                                        // Set Selected Offer Id
                                        selectedOfferId = (((JSONObject) radioButtonOffer.getTag()).optInt("offerID"));

                                        // Remove Edit Booking Offer Id
                                        if (dataJsonObject.opt(AppConstant.JSON_PARAM_EDIT_BOOKING_OFFER_ID) != null) {
                                            dataJsonObject.remove(AppConstant.JSON_PARAM_EDIT_BOOKING_OFFER_ID);
                                        }

                                        // Deselect all options
                                        deselectAllOffers(viewHolder.getRadioGroupOfferList());

                                        // Set Offer Selected
                                        radioButtonOffer.setChecked(true);

                                        // Save Selected Offer
                                        saveSelectedOffer(radioButtonOffer);

                                        // Set true since Offer is selected
                                        setOfferSelected(true);

                                        // Enable Book Now Button
                                        if (isAdditionalOfferAvailable()) {
                                            // Since Adapter is in process of loading the layout, this call will throw
                                            // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling
                                            // Hence, keep it inside try-catch block
                                            try {
                                                notifyItemChanged(getAdditionalOfferItemPosition());
                                            } catch (Exception e) {
                                                // Do Nothing...
                                            }
                                        } else {
                                            viewHolder.getButtonOfferBookNow().setEnabled(true);
                                        }
                                    }
                                });

                                // Set Validity Text Color
                                textViewOfferValidity.setEnabled(true);

                                // Set Right Arrow Color
                                imageViewOfferArrow.setImageResource(R.drawable.ic_grey_arrow_right);
                            }

                            // Show Offer Detail
                            offerSectionView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    clickListener.onOfferDetailClick(
                                            mContext.getString(R.string.text_offer_detail),
                                            setEarningsInUrl(offerJsonObject.optString("detailUrl")),
                                            getOfferBundle(), getDealVsCount());
                                }
                            });

                            // Set Detail Click Listener
                            imageViewOfferArrow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    clickListener.onOfferDetailClick(
                                            mContext.getString(R.string.text_offer_detail),
                                            setEarningsInUrl(offerJsonObject.optString("detailUrl")),
                                            getOfferBundle(), getDealVsCount());
                                }
                            });

                            // Set Pre-selection back
                            if (offerId == selectedOfferId) {
                                radioButtonOffer.performClick();
                                radioButtonOffer.setChecked(true);
                            } else {
                                radioButtonOffer.setChecked(false);
                            }

                            // Check for last record
                            if ((index + 1) == arraySize) {
                                // Remove Divider
                                offerView.findViewById(R.id.divider_offer).setVisibility(View.GONE);
                            }

                            // Add View to the List
                            viewHolder.getRadioGroupOfferList().addView(offerView);
                        }
                    }

                    // Show Offer Title Section
                    viewHolder.getLinearLayoutOfferTitleSection().setVisibility(View.VISIBLE);

                    // Hide No Offer Section
                    viewHolder.getLinearLayoutNoOffersSection().setVisibility(View.GONE);

                } else { // HANDLE NO OFFERS SECTION + ADDITIONAL OFFERS SECTION
                    if (!isAdditionalOfferAvailable()) {
                        // Hide Offer Title Section
                        viewHolder.getLinearLayoutOfferTitleSection().setVisibility(View.GONE);

                        // Show No Offer Section
                        viewHolder.getLinearLayoutNoOffersSection().setVisibility(View.VISIBLE);
                    }
                }

                if (!isAdditionalOfferAvailable()) {
                    // Enable Book Now button if NO offers are active or exists for selected Date/Time
                    viewHolder.getButtonOfferBookNow().setEnabled((isOfferSelected()) ? true : (isOfferSelectable() ? false : true));

                    // Set Book Now Click Listener
                    viewHolder.getButtonOfferBookNow().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handleBookNowAction();
                        }
                    });
                }
            }
        }
    }

    private Bundle getOfferBundle() {
        Bundle bundle = new Bundle();

        // Put Selected Offer Details
        if (selectedOfferBundle != null) {
            bundle.putAll(selectedOfferBundle);
        }

        // Put Selected Date-Time
        bundle.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP, getDiningDateTime());

        return bundle;
    }

    private void deselectAllOffers(RadioGroup offerRadioGroup) {
        if (offerRadioGroup != null) {
            int offerCount = offerRadioGroup.getChildCount();
            for (int index = 0; index < offerCount; index++) {
                View offerView = offerRadioGroup.getChildAt(index);

                // Un check Radio Button
                RadioButton radioButtonOffer = (RadioButton) offerView.findViewById(R.id.radioButton_offer);
                if (radioButtonOffer != null && radioButtonOffer.isEnabled() && radioButtonOffer.isChecked()) {
                    radioButtonOffer.setChecked(false);
                }
            }
        }
    }

    private void saveSelectedOffer(RadioButton radioButtonOffer) {
        JSONObject offerJsonObject = (JSONObject) radioButtonOffer.getTag();

        if (offerJsonObject != null) {

            if (selectedOfferBundle == null)
                selectedOfferBundle = new Bundle();

            selectedOfferBundle.putInt(AppConstant.BUNDLE_OFFER_ID, offerJsonObject.optInt("offerID"));
            selectedOfferBundle.putString(AppConstant.BUNDLE_OFFER_TITLE, offerJsonObject.optString("title"));
            selectedOfferBundle.putString(AppConstant.BUNDLE_OFFER_VALIDITY, offerJsonObject.optJSONObject("availability").optString("message"));
        }
    }

    private void handleBookNowAction() {
        // Check for NULL
        if (selectedOfferBundle != null) {
            bookingBundle.putAll(selectedOfferBundle);
        }

        clickListener.onBookNowClick(bookingBundle);
    }

    private void showAdditionalOfferSection(final AdditionalOfferViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            final JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Offer Section
            JSONObject additionalOfferSectionJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (additionalOfferSectionJsonObject != null) {

                // Set Title
                if (!AppUtil.isStringEmpty(additionalOfferSectionJsonObject.optString("title"))) {
                    viewHolder.getTextViewAdditionalOfferTitle().setText(additionalOfferSectionJsonObject.optString("title"));
                }

                // Set Enable State
                boolean enabledState = false;
                if (isOfferSelectable()) {
                    if (isOfferSelected()) {
                        enabledState = true;
                    } else {
                        enabledState = false;
                    }
                } else {
                    enabledState = true;
                }

                viewHolder.getButtonAdditionalOfferBookNow().setEnabled(enabledState);

                // Set Book Now Click Listener
                viewHolder.getButtonAdditionalOfferBookNow().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handleBookNowAction();
                    }
                });

                // Set Additional Offers
                viewHolder.getLinearLayoutAdditionalOfferList().removeAllViews();
                JSONArray additionalOfferJsonArray = additionalOfferSectionJsonObject.optJSONArray("additionalOffer");
                if (additionalOfferJsonArray != null && additionalOfferJsonArray.length() > 0) {
                    int arraySize = additionalOfferJsonArray.length();
                    for (int index = 0; index < arraySize; index++) {
                        JSONObject additionalOfferJsonObject = additionalOfferJsonArray.optJSONObject(index);

                        if (additionalOfferJsonObject != null) {
                            // Get View
                            final View additionOfferView = LayoutInflater.from(mContext).
                                    inflate(R.layout.addition_offer_list_item, null, false);

                            // Set Text
                            TextView textViewAdditionalOfferMsg1 = (TextView) additionOfferView.findViewById(R.id.textView_additional_offer_msg1);
                            if (!AppUtil.isStringEmpty(additionalOfferJsonObject.optString("text"))) {
                                // Set Text
                                String message = AppUtil.renderRupeeSymbol(additionalOfferJsonObject.optString("text")).toString();
                                textViewAdditionalOfferMsg1.setText(
                                        AppUtil.getColoredText(message, mContext.getResources().getColor(R.color.green)));

                                // Set Visibility
                                textViewAdditionalOfferMsg1.setVisibility(View.VISIBLE);
                            }

                            // Get Additional Message instance
                            final TextView textViewAdditionalOfferMsg2 = (TextView) additionOfferView.findViewById(R.id.textView_additional_offer_msg2);

                            // Check if Earnings are fetched
                            final String additionOfferURL = additionalOfferJsonObject.optString("url");

                            // Check if URL is there
                            if (!AppUtil.isStringEmpty(additionOfferURL) &&
                                    !AppUtil.isStringEmpty(DOPreferences.getDinerEmail(mContext))) {

                                if (!AppConstant.URL_GET_DINER_WALLET_AMOUNT.equalsIgnoreCase(additionOfferURL)) {

                                    // Check if Data is fetched already
                                    if (AppUtil.isStringEmpty(additionOfferMsgMap.get(additionOfferURL))) {

                                        // Show Progress Bar
                                        final ProgressBar progressBarAdditionalOffer = (ProgressBar) additionOfferView.findViewById(R.id.progressBar_addition_offer);
                                        AppUtil.setProgressBarDecor(mContext, progressBarAdditionalOffer);
                                        progressBarAdditionalOffer.setVisibility(View.VISIBLE);

                                        networkManager.jsonRequestGet(REQUEST_CODE_ADDITIONAL_OFFER_INFO,
                                                additionOfferURL, null, new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
                                                        // Hide Progress Bar
                                                        progressBarAdditionalOffer.setVisibility(View.GONE);

                                                        if (responseObject != null && responseObject.optBoolean("status")) {
                                                            // Get Output Params
                                                            JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");
                                                            if (outputParamsJsonObject != null) {
                                                                // Get Data Array
                                                                JSONArray dataJsonArray = outputParamsJsonObject.optJSONArray("data");
                                                                if (dataJsonArray != null && dataJsonArray.length() > 0) {
                                                                    // Get First JSON Object
                                                                    JSONObject firstJsonObject = dataJsonArray.optJSONObject(0);
                                                                    if (firstJsonObject != null) {
                                                                        // Get Title1
                                                                        String title1 = firstJsonObject.optString("title1");
                                                                        if (!AppUtil.isStringEmpty(title1)) {
                                                                            // Set Earnings Text
                                                                            additionOfferMsgMap.put(additionOfferURL, title1);

                                                                            showUserEarnings(title1, textViewAdditionalOfferMsg2);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(Request request, VolleyError error) {
                                                        // Hide Progress Bar
                                                        progressBarAdditionalOffer.setVisibility(View.GONE);
                                                    }
                                                }, false);
                                    } else {
                                        showUserEarnings(additionOfferMsgMap.get(additionOfferURL), textViewAdditionalOfferMsg2);
                                    }
                                } else {
                                    if (!AppUtil.isStringEmpty(getEarningsText())) {
                                        // Set Earnings Text
                                        additionOfferMsgMap.put(additionOfferURL, getEarningsText());

                                        showUserEarnings(getEarningsText(), textViewAdditionalOfferMsg2);
                                    }
                                }
                            }

                            // Check for last record
                            if ((index + 1) == arraySize) {
                                // Remove Divider
                                additionOfferView.findViewById(R.id.divider_additional_offer).setVisibility(View.GONE);
                            }

                            // Add View to the List
                            viewHolder.getLinearLayoutAdditionalOfferList().addView(additionOfferView);
                        }
                    }

                    // Show Addition Offers Title
                    viewHolder.getTextViewAdditionalOfferTitle().setVisibility(View.VISIBLE);

                    // Show Additional Offers List View
                    viewHolder.getLinearLayoutAdditionalOfferList().setVisibility(View.VISIBLE);

                } else {
                    // Hide Addition Offers Title
                    viewHolder.getTextViewAdditionalOfferTitle().setVisibility(View.GONE);

                    // Hide Additional Offers List View
                    viewHolder.getLinearLayoutAdditionalOfferList().setVisibility(View.GONE);
                }
            }
        }
    }

    private void showUserEarnings(String title1, TextView textViewAdditionalOfferMsg2) {
        title1 = AppUtil.renderRupeeSymbol(title1).toString(); // render rupee sign

        textViewAdditionalOfferMsg2.setText(AppUtil.getColoredText(title1, mContext.getResources().getColor(R.color.green)));
        textViewAdditionalOfferMsg2.setVisibility(View.VISIBLE);
    }

    private void showDealSection(final DealViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {
            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Deals Section
            JSONObject dealsJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (dealsJsonObject != null) {

                // Set Title
                if (!AppUtil.isStringEmpty(dealsJsonObject.optString("title"))) {
                    viewHolder.getTextViewDealTitle().setText(dealsJsonObject.optString("title"));
                }

                // Set Sub Title
                if (!AppUtil.isStringEmpty(dealsJsonObject.optString("subTitle"))) {
                    viewHolder.getTextViewDealSubTitle().setText(
                            AppUtil.getColoredText(dealsJsonObject.optString("subTitle"),
                                    mContext.getResources().getColor(R.color.green)));
                    viewHolder.getTextViewDealSubTitle().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getTextViewDealSubTitle().setVisibility(View.GONE);
                }

                // Get Deals
                viewHolder.getLinearLayoutDealList().removeAllViews();
                JSONArray dealsJsonArray = dealsJsonObject.optJSONArray("deals");
                if (dealsJsonArray != null && dealsJsonArray.length() > 0) {
                    // Set Deals
                    selectedDealArray = dealsJsonArray;
                    int arraySize = dealsJsonArray.length();

                    for (int index = 0; index < arraySize; index++) {
                        final JSONObject dealJsonObject = dealsJsonArray.optJSONObject(index);

                        if (dealJsonObject != null) {
                            // Get View
                            View dealView = LayoutInflater.from(mContext).
                                    inflate(R.layout.deal_list_item, null, false);

                            // Set Title
                            TextView textViewDealTitle = (TextView) dealView.findViewById(R.id.textView_deal_title);
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("title"))) {
                                textViewDealTitle.setText(dealJsonObject.optString("title"));
                            }

                            // Set Deal Timings
                            TextView textViewDealDateTime = (TextView) dealView.findViewById(R.id.textView_deal_date_time);
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("time"))) {
                                String time = dealJsonObject.optString("time").trim();
                                textViewDealDateTime.setText(time);
                            }

                            // Set Our Price
                            TextView textViewDealCurrentPrice = (TextView) dealView.findViewById(R.id.textView_deal_current_price);
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("ourPrice"))) {
                                textViewDealCurrentPrice.setText(String.format(mContext.getString(R.string.container_rupee), dealJsonObject.optString("ourPrice")));
                            }

                            // Set Actual Price
                            TextView textViewDealActualPrice = (TextView) dealView.findViewById(R.id.textView_deal_actual_price);
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("actualPrice"))) {
                                textViewDealActualPrice.setText(AppUtil.getStrikedText(String.format(
                                        mContext.getString(R.string.container_rupee), dealJsonObject.optString("actualPrice"))));
                            }

                            // Set Deal Validity
                            TextView textViewDealValidity = (TextView) dealView.findViewById(R.id.textView_deal_validity);
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("validity"))) {
                                String validityText = dealJsonObject.optString("validity");
                                validityText = validityText.trim();
                                textViewDealValidity.setText(validityText);
                            }

                            // Set Deals Left text_container_deal_left
                            TextView textViewDealStatus = (TextView) dealView.findViewById(R.id.textView_deal_status);
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("dealsLeft"))) {
                                int dealsLeft = dealJsonObject.optInt("dealsLeft");
                                textViewDealStatus.setText(String.format(mContext.getString(R.string.text_container_deal_left), dealsLeft));
                                textViewDealStatus.setTag(dealsLeft);
                            }

                            // Check Availability
                            if (dealJsonObject.optJSONObject("availability") != null) {
                                int status = dealJsonObject.optJSONObject("availability").optInt("status");

                                if (status == 0) {
                                    // Disable Title
                                    textViewDealTitle.setEnabled(false);

                                    // Disable Date Time
                                    textViewDealDateTime.setEnabled(false);

                                    // Disable Our Price
                                    textViewDealCurrentPrice.setEnabled(false);

                                    // Disable Actual Price
                                    textViewDealActualPrice.setEnabled(false);

                                    // Disable Validity
                                    textViewDealValidity.setEnabled(false);

                                    // Hide Minus Button
                                    dealView.findViewById(R.id.imageView_deal_minus).setVisibility(View.GONE);

                                    // Hide Deal Count
                                    dealView.findViewById(R.id.textView_deal_quantity).setVisibility(View.GONE);

                                    // Hide Plus Button
                                    dealView.findViewById(R.id.imageView_deal_plus).setVisibility(View.GONE);

                                    // Set Message
                                    if (!AppUtil.isStringEmpty(dealJsonObject.optJSONObject("availability").optString("message"))) {
                                        textViewDealStatus.setText(dealJsonObject.optJSONObject("availability").optString("message"));
                                    }
                                } else {
                                    // Get Deal Count
                                    int dealsLeft = (int) textViewDealStatus.getTag();

                                    // Get Deal Transaction Limit
                                    int transactionDealLimit = dealJsonObject.optInt("transactionDealLimit");
                                    final int dealLimit = ((transactionDealLimit <= dealsLeft) ? transactionDealLimit : dealsLeft);

                                    // Get Minus Button
                                    final ImageView buttonMinus = (ImageView) dealView.findViewById(R.id.imageView_deal_minus);

                                    // Get Plus Button
                                    final ImageView buttonPlus = (ImageView) dealView.findViewById(R.id.imageView_deal_plus);

                                    // Get Deal Quantity
                                    final TextView textViewDealQuantity = (TextView) dealView.findViewById(R.id.textView_deal_quantity);

                                    // Get Buy Now Button
                                    Button buttonBuyNow = (Button) dealView.findViewById(R.id.button_buy_now);

                                    if (dealsLeft == 0) {
                                        // Disable Minus Button
                                        buttonMinus.setEnabled(false);

                                        // Disable Plus Button
                                        buttonPlus.setEnabled(false);

                                        // Disable Buy Now Button
                                        buttonBuyNow.setEnabled(false);

                                    } else {
                                        // Set Text Change Listener
                                        textViewDealQuantity.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                // Do Nothing...
                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                // Do Nothing...
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {
                                                if (!TextUtils.isEmpty(s.toString())) {
                                                    int currentQuantity = Integer.parseInt(s.toString());

                                                    if (currentQuantity == 0) {
                                                        // Disable Minus Button
                                                        buttonMinus.setEnabled(false);

                                                    } else if (currentQuantity == dealLimit) {
                                                        // Disable Plus Button
                                                        buttonPlus.setEnabled(false);

                                                        // Show Message
                                                        Toast.makeText(mContext,
                                                                R.string.deal_limit_reached,
                                                                Toast.LENGTH_LONG).show();

                                                    } else {
                                                        // Do Nothing...
                                                    }
                                                }
                                            }
                                        });

                                        if (dealVsCount.containsKey(dealJsonObject.optString("dealID"))) {
                                            textViewDealQuantity.setText(String.valueOf(dealVsCount.get(dealJsonObject.optString("dealID"))));
                                        } else {
                                            textViewDealQuantity.setText("0");
                                        }

                                        viewHolder.getButtonBuyNow().setEnabled(totalDealSelectedCount > 0);

                                        // Set Minus Button Click Listener
                                        buttonMinus.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                // Get Current Deal Quantity
                                                int dealQuantity = dealVsCount.containsKey(dealJsonObject.optString("dealID")) ? dealVsCount.get(dealJsonObject.optString("dealID")) : 0;//Integer.parseInt(textViewDealQuantity.getText().toString());

                                                if (dealQuantity > 0) {
                                                    dealQuantity -= 1;
                                                    totalDealSelectedCount -= 1;
                                                }

                                                textViewDealQuantity.setText(String.valueOf(dealQuantity));

                                                // Enable Plus Button
                                                buttonPlus.setEnabled(true);

                                                // Update Buy Now Status
                                                viewHolder.getButtonBuyNow().setEnabled(totalDealSelectedCount > 0);
                                                try {
                                                    if (dealQuantity == 0 && dealVsCount.containsKey(dealJsonObject.optString("dealID"))) {
                                                        dealVsCount.remove(dealJsonObject.optString("dealID"));
                                                    } else {
                                                        dealVsCount.put(dealJsonObject.optString("dealID"), dealQuantity);
                                                        dealJsonObject.put(AppConstant.BUNDLE_SELECTED_DEAL_COUNT, dealQuantity);
                                                    }
                                                } catch (JSONException ex) {

                                                }
                                            }
                                        });

                                        // Set Plus Button Click Listener
                                        buttonPlus.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                // Get Current Deal Quantity
                                                int dealQuantity = dealVsCount.containsKey(dealJsonObject.optString("dealID")) ? dealVsCount.get(dealJsonObject.optString("dealID")) : 0;//Integer.parseInt(textViewDealQuantity.getText().toString());
                                                if (dealQuantity < dealLimit) {
                                                    dealQuantity += 1;
                                                    totalDealSelectedCount += 1;
                                                }

                                                // Update Deal Quantity
                                                textViewDealQuantity.setText(String.valueOf(dealQuantity));

                                                // Enable Minus Button
                                                buttonMinus.setEnabled(true);

                                                // Update Buy Now Status
                                                viewHolder.getButtonBuyNow().setEnabled(totalDealSelectedCount > 0);

                                                try {
                                                    dealVsCount.put(dealJsonObject.optString("dealID"), dealQuantity);
                                                    dealJsonObject.put(AppConstant.BUNDLE_SELECTED_DEAL_COUNT, dealQuantity);
                                                } catch (JSONException ex) {
                                                }
                                            }
                                        });

                                        // Set Initial Deal Quantity
                                        //textViewDealQuantity.setText("0");
                                    }
                                }
                            }


                            // Set More Info Click
                            final String dealUrl = dealJsonObject.optString("dealUrl");
                            if (AppUtil.isStringEmpty(dealUrl)) {
                                // Hide More Info
                                dealView.findViewById(R.id.textView_deal_info).setVisibility(View.GONE);

                            } else {
                                // Show More Info
                                TextView textViewDealInfo = (TextView) dealView.findViewById(R.id.textView_deal_info);
                                textViewDealInfo.setVisibility(View.VISIBLE);

                                // Set Click
                                textViewDealInfo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        clickListener.onDealMoreInfoClick(dealJsonObject.optString("title"),
                                                setEarningsInUrl(dealUrl), dealVsCount);
                                    }
                                });
                            }

                            // Check for last record
                            if ((index + 1) == arraySize) {
                                // Remove Divider
                                dealView.findViewById(R.id.divider_deal).setVisibility(View.GONE);
                            }

                            // Add View to List
                            viewHolder.getLinearLayoutDealList().addView(dealView);
                        }
                    }

                    // Set Buy Now Click
                    viewHolder.getButtonBuyNow().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clickListener.onBuyNowClick(bookingBundle);
                        }
                    });
                } else {
                    // Hide Section
                    viewHolder.getRootView().setVisibility(View.GONE);
                }
            }
        }
    }

    private String setEarningsInUrl(String url) {
        if (!AppUtil.isStringEmpty(url)) {
            // Set Earnings
            url = url.replace(AppConstant.URL_SMARTPAY_PLACE_HOLDER, Integer.toString(getUserEarnings()));
        }

        return url;
    }

    private void showEventCardSection(final EventCardViewHolder viewHolder, JSONObject jsonObject) {
        if (viewHolder != null && restDetailJsonObject != null &&
                restDetailJsonObject.optJSONObject("data") != null) {

            // Get Data JSON Object
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            // Get Deals Section
            JSONObject eventCardJsonObject = dataJsonObject.optJSONObject(jsonObject.optString("key"));

            if (eventCardJsonObject != null) {
                JSONObject reservationEventJsonObject = eventCardJsonObject.optJSONObject("reservationEvent");

                if (reservationEventJsonObject != null) {

                    // Set Notification Message
                    String notification = reservationEventJsonObject.optString("reservationText", "");
                    if (AppUtil.isStringEmpty(notification)) {
                        viewHolder.getFrameLayoutEventNotification().setVisibility(View.GONE);
                    } else {
                        viewHolder.getFrameLayoutEventNotification().setVisibility(View.VISIBLE);
                        viewHolder.getTextViewEventNotification().setText(notification);

                        String imageUrl = reservationEventJsonObject.optString("iconUrl", "");
                        if (AppUtil.isStringEmpty(imageUrl)) {
                            viewHolder.getImageViewNotificationIcon().setVisibility(View.GONE);
                        } else {
                            viewHolder.getImageViewNotificationIcon().setVisibility(View.VISIBLE);
                            viewHolder.getImageViewNotificationIcon().setImageUrl(imageUrl,
                                    ImageRequestManager.getInstance(mContext).getImageLoader());
                        }

                        int paymentRequired = reservationEventJsonObject.optInt("paymentRequired", 0);
                        if (paymentRequired == 0) {
                            final String title = reservationEventJsonObject.optString("title", "");
                            final String eventUrl = reservationEventJsonObject.optString("eventUrl", "");
                            if (!AppUtil.isStringEmpty(eventUrl) && clickListener != null) {
                                viewHolder.getRelativeLayoutNotificationContainer().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        clickListener.onEventNotificationClick(title, eventUrl);
                                    }
                                });
                            }
                        } else {
                            viewHolder.getRelativeLayoutNotificationContainer().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    clickListener.onEventCardClick();
                                }
                            });
                        }
                    }

                    // Check if Event Details are to be displayed
                    if (!reservationEventJsonObject.optBoolean("reservationFlag", true)) {
                        // Set Visibility
                        viewHolder.getParentView().setVisibility(View.VISIBLE);

                        // Set Title
                        String title = eventCardJsonObject.optString("title", "");
                        if (!AppUtil.isStringEmpty(title)) {
                            viewHolder.getTextViewEventTitle().setText(title);
                            viewHolder.getTextViewEventTitle().setVisibility(View.VISIBLE);
                        }

                        // Set Event Background Image
                        JSONObject mediaJsonObject = reservationEventJsonObject.optJSONObject("media");
                        if (mediaJsonObject != null) {

                            JSONArray ticketJsonArray = mediaJsonObject.optJSONArray("ticketImages");
                            if (ticketJsonArray != null && ticketJsonArray.length() > 0) {

                                JSONObject ticketJsonObject = ticketJsonArray.optJSONObject(0);
                                if (ticketJsonObject != null) {
                                    String imageUrl = ticketJsonObject.optString("imageUrl", "");

                                    if (!AppUtil.isStringEmpty(imageUrl)) {
                                        viewHolder.getImageViewEvent().setImageUrl(imageUrl,
                                                ImageRequestManager.getInstance(mContext).getImageLoader());
                                    } else {
                                        // Reset Background Image
                                        viewHolder.getImageViewEvent().setImageUrl("",
                                                ImageRequestManager.getInstance(mContext).getImageLoader());
                                    }
                                } else {
                                    // Reset Background Image
                                    viewHolder.getImageViewEvent().setImageUrl("",
                                            ImageRequestManager.getInstance(mContext).getImageLoader());
                                }
                            } else {
                                // Reset Background Image
                                viewHolder.getImageViewEvent().setImageUrl("",
                                        ImageRequestManager.getInstance(mContext).getImageLoader());
                            }
                        } else {
                            // Reset Background Image
                            viewHolder.getImageViewEvent().setImageUrl("",
                                    ImageRequestManager.getInstance(mContext).getImageLoader());
                        }

                        // Set Event Name
                        final String eventName = reservationEventJsonObject.optString("title", "");
                        if (!AppUtil.isStringEmpty(eventName)) {
                            viewHolder.getTextViewEventName().setText(eventName);
                        }

                        // Set Restaurant Name
                        String restaurantName = dataJsonObject.optString("profileName", "");
                        if (!AppUtil.isStringEmpty(restaurantName)) {
                            viewHolder.getTextViewRestaurantName().setText(restaurantName);
                        }

                        // Set Event Date
                        String eventDate = reservationEventJsonObject.optString("validity", "");
                        if (!AppUtil.isStringEmpty(eventDate)) {
                            viewHolder.getTextViewEventDate().setText(eventDate);
                        }

                        // Set Event Time
                        String eventTime = reservationEventJsonObject.optString("time", "");
                        if (!AppUtil.isStringEmpty(eventTime)) {
                            viewHolder.getTextViewEventTime().setText(eventTime);
                        }

                        // Set Event Price
                        int minimumPrice = reservationEventJsonObject.optInt("minimumPrice", 0);
                        if (minimumPrice == 0) {
                            viewHolder.getTextViewEventPrice().setText(R.string.text_free);
                            viewHolder.getTextViewEventPrice().
                                    setTextColor(mContext.getResources().getColor(R.color.light_green));
                            viewHolder.getTextViewEventPriceTag().setVisibility(View.GONE);
                        } else {
                            viewHolder.getTextViewEventPrice().
                                    setText(String.format(mContext.getString(R.string.container_rupee), Integer.toString(minimumPrice)));
                            viewHolder.getTextViewEventPriceTag().setText(R.string.text_onwards);
                            viewHolder.getTextViewEventPriceTag().setVisibility(View.VISIBLE);
                        }

                        // Set Event Message
                        String message = reservationEventJsonObject.optString("typeText", "");
                        if (!AppUtil.isStringEmpty(message)) {
                            viewHolder.getTextViewRestaurantMessage().setVisibility(View.VISIBLE);
                            viewHolder.getTextViewRestaurantMessage().setText(message);
                        } else {
                            viewHolder.getTextViewRestaurantMessage().setVisibility(View.GONE);
                        }

                        // Set Parent Click
                        int paymentRequired = reservationEventJsonObject.optInt("paymentRequired", 0);
                        if (paymentRequired == 0) {
                            final String eventUrl = reservationEventJsonObject.optString("eventUrl", "");
                            if (!AppUtil.isStringEmpty(eventUrl) && clickListener != null) {
                                viewHolder.getParentView().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        clickListener.onEventNotificationClick(eventName, eventUrl);
                                    }
                                });
                            }
                        } else {
                            viewHolder.getParentView().setOnClickListener(this);
                        }
                    } else {
                        // Set Visibility
                        viewHolder.getParentView().setVisibility(View.GONE);
                        viewHolder.getTextViewRestaurantMessage().setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (clickListener != null) {
            if (viewId == R.id.cardView_event_card) {
                clickListener.onEventCardClick();
            }
        }
    }

    // Reservation Tab Click Listener
    public interface ReservationTabClickListener {
        void onDateTimeClick(long dateTimeInMilliSeconds, long nextMinimumBookingTime);

        void onOfferDetailClick(String title, String url, Bundle offerBundle, HashMap<String, Integer> dealVsCount);

        void onBookNowClick(Bundle bookingBundle);

        void onDealMoreInfoClick(String title, String url, HashMap<String, Integer> dealVsCount);

        void onBuyNowClick(Bundle bookingBundle);

        void onEventCardClick();

        void onEventNotificationClick(String title, String url);
    }

    /**
     * View Holders
     */

    // Date Time Section View Holder
    private class DateTimeViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private LinearLayout linearLayoutDateTimeSection;
        private TextView textViewDate;
        private TextView textViewTime;

        public DateTimeViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            linearLayoutDateTimeSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_date_time_section);
            textViewDate = (TextView) itemView.findViewById(R.id.textView_date);
            textViewTime = (TextView) itemView.findViewById(R.id.textView_time);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public LinearLayout getLinearLayoutDateTimeSection() {
            return linearLayoutDateTimeSection;
        }

        public TextView getTextViewDate() {
            return textViewDate;
        }

        public TextView getTextViewTime() {
            return textViewTime;
        }
    }

    // Extra Info Section View Holder
    private class ExtraInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private LinearLayout linearLayoutExtraInfoList;

        public ExtraInfoViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            linearLayoutExtraInfoList = (LinearLayout) itemView.findViewById(R.id.linearLayout_extra_info_list);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public LinearLayout getLinearLayoutExtraInfoList() {
            return linearLayoutExtraInfoList;
        }
    }

    // Offer Section View Holder
    private class OfferViewHolder extends RecyclerView.ViewHolder {
        private View linearLayoutOfferTitleSection;
        private TextView textViewOfferTitle;
        private TextView textViewOfferSubTitle;
        private RadioGroup radioGroupOfferList;
        private Button buttonOfferBookNow;
        private View linearLayoutNoOffersSection;

        public OfferViewHolder(View itemView) {
            super(itemView);

            linearLayoutOfferTitleSection = itemView.findViewById(R.id.linearLayout_offer_title_section);
            textViewOfferTitle = (TextView) itemView.findViewById(R.id.textView_offer_title);
            textViewOfferSubTitle = (TextView) itemView.findViewById(R.id.textView_offer_sub_title);
            radioGroupOfferList = (RadioGroup) itemView.findViewById(R.id.radioGroup_offer_list);
            buttonOfferBookNow = (Button) itemView.findViewById(R.id.button_offer_book_now);
            linearLayoutNoOffersSection = itemView.findViewById(R.id.linearLayout_no_offers_section);
        }

        public View getLinearLayoutOfferTitleSection() {
            return linearLayoutOfferTitleSection;
        }

        public TextView getTextViewOfferTitle() {
            return textViewOfferTitle;
        }

        public TextView getTextViewOfferSubTitle() {
            return textViewOfferSubTitle;
        }

        public RadioGroup getRadioGroupOfferList() {
            return radioGroupOfferList;
        }

        public Button getButtonOfferBookNow() {
            return buttonOfferBookNow;
        }

        public View getLinearLayoutNoOffersSection() {
            return linearLayoutNoOffersSection;
        }
    }

    // Additional Offer Section View Holder
    private class AdditionalOfferViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewAdditionalOfferTitle;
        private LinearLayout linearLayoutAdditionalOfferList;
        private Button buttonAdditionalOfferBookNow;

        public AdditionalOfferViewHolder(View itemView) {
            super(itemView);

            textViewAdditionalOfferTitle = (TextView) itemView.findViewById(R.id.textView_additional_offer_title);
            linearLayoutAdditionalOfferList = (LinearLayout) itemView.findViewById(R.id.linearLayout_additional_offer_list);
            buttonAdditionalOfferBookNow = (Button) itemView.findViewById(R.id.button_additional_offer_book_now);
        }

        public TextView getTextViewAdditionalOfferTitle() {
            return textViewAdditionalOfferTitle;
        }

        public LinearLayout getLinearLayoutAdditionalOfferList() {
            return linearLayoutAdditionalOfferList;
        }

        public Button getButtonAdditionalOfferBookNow() {
            return buttonAdditionalOfferBookNow;
        }
    }

    // Deal Section View Holder
    private class DealViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDealTitle;
        private TextView textViewDealSubTitle;
        private LinearLayout linearLayoutDealList;
        private Button buttonBuyNow;
        private View rootView;

        public DealViewHolder(View itemView) {
            super(itemView);

            textViewDealTitle = (TextView) itemView.findViewById(R.id.textView_deal_title);
            textViewDealSubTitle = (TextView) itemView.findViewById(R.id.textView_deal_sub_title);
            linearLayoutDealList = (LinearLayout) itemView.findViewById(R.id.linearLayout_deal_list);
            buttonBuyNow = (Button) itemView.findViewById(R.id.button_buy_now);
            rootView = buttonBuyNow.getRootView();
        }

        public TextView getTextViewDealTitle() {
            return textViewDealTitle;
        }

        public TextView getTextViewDealSubTitle() {
            return textViewDealSubTitle;
        }

        public LinearLayout getLinearLayoutDealList() {
            return linearLayoutDealList;
        }

        public Button getButtonBuyNow() {
            return buttonBuyNow;
        }

        public View getRootView() {
            return rootView;
        }
    }

    private class EventCardViewHolder extends RecyclerView.ViewHolder {
        private View frameLayoutEventNotification;
        private View relativeLayoutNotificationContainer;
        private NetworkImageView imageViewNotificationIcon;
        private TextView textViewEventNotification;
        private TextView textViewEventTitle;
        private View parentView;
        private NetworkRoundCornerImageView imageViewEvent;
        private TextView textViewEventName;
        private TextView textViewRestaurantName;
        private TextView textViewEventDate;
        private TextView textViewEventTime;
        private TextView textViewEventPrice;
        private TextView textViewEventPriceTag;
        private TextView textViewRestaurantMessage;

        public EventCardViewHolder(View itemView) {
            super(itemView);

            frameLayoutEventNotification = itemView.findViewById(R.id.frameLayout_event_notification);
            relativeLayoutNotificationContainer = itemView.findViewById(R.id.relativeLayout_notification_container);
            imageViewNotificationIcon = (NetworkImageView) itemView.findViewById(R.id.imageView_notification_icon);
            textViewEventNotification = (TextView) itemView.findViewById(R.id.textView_event_notification);
            textViewEventTitle = (TextView) itemView.findViewById(R.id.textView_event_title);
            parentView = itemView.findViewById(R.id.cardView_event_card);
            imageViewEvent = (NetworkRoundCornerImageView) itemView.findViewById(R.id.image_view_event);
            imageViewEvent.setDefaultImageResId(R.drawable.default_list);
            textViewEventName = (TextView) itemView.findViewById(R.id.text_view_event_name);
            textViewRestaurantName = (TextView) itemView.findViewById(R.id.text_view_restaurant_name);
            textViewEventDate = (TextView) itemView.findViewById(R.id.text_view_event_date);
            textViewEventTime = (TextView) itemView.findViewById(R.id.text_view_event_time);
            textViewEventPrice = (TextView) itemView.findViewById(R.id.text_view_event_price);
            textViewEventPriceTag = (TextView) itemView.findViewById(R.id.text_view_event_price_tag);
            textViewRestaurantMessage = (TextView) itemView.findViewById(R.id.textView_restaurant_message);
        }

        public View getFrameLayoutEventNotification() {
            return frameLayoutEventNotification;
        }

        public View getRelativeLayoutNotificationContainer() {
            return relativeLayoutNotificationContainer;
        }

        public NetworkImageView getImageViewNotificationIcon() {
            return imageViewNotificationIcon;
        }

        public TextView getTextViewEventNotification() {
            return textViewEventNotification;
        }

        public TextView getTextViewEventTitle() {
            return textViewEventTitle;
        }

        public View getParentView() {
            return parentView;
        }

        public NetworkRoundCornerImageView getImageViewEvent() {
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

        public TextView getTextViewEventPrice() {
            return textViewEventPrice;
        }

        public TextView getTextViewEventPriceTag() {
            return textViewEventPriceTag;
        }

        public TextView getTextViewRestaurantMessage() {
            return textViewRestaurantMessage;
        }
    }
}
