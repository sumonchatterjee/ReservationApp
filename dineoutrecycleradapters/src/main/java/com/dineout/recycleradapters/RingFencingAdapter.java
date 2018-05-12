package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RingFencingAdapter extends BaseRecyclerAdapter implements Response.Listener<JSONObject> {

    private static final int ITEM_TYPE_PROMO_INPUT = 1;
    private static final int ITEM_TYPE_PROMO_DATA_HEADER = 2;
    private static final int ITEM_TYPE_PROMO_DATA = 3;
    private static final int REQUEST_APPLY_PROMO = 999;

    ArrayList<Integer> listSelectedIndexes;
    DineoutNetworkManager networkManager;
    OnReserveButtonClickListener onReserveButtonClickListener;
    OnPromoButtonClickListener onPromoButtonClickListener;
    private Context mContext;
    private JSONObject jsonObjectSectionData;
    private String promocode;

    public RingFencingAdapter(Context context, DineoutNetworkManager networkManager) {
        mContext = context;
        this.networkManager = networkManager;
        listSelectedIndexes = new ArrayList<>();
    }

    private void setIndexesClicked(String promoCode, int positionClicked) {
        applyCouponRingfencingAPI(promoCode, positionClicked);
    }

    private void setOnApplyButtonClicked(String promocode) {
        // AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_promotions), mContext.getString(R.string.ga_action_verify), null);
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(mContext);

        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly(mContext.getResources().getString(R.string.d_promotion_verify), hMap);
        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.countly_promotion),
                mContext.getResources().getString(R.string.d_promotion_verify), promocode);

        HashMap<String,Object>map=new HashMap<>();
        map.put("label",promocode);
        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventQGraphApsalar( mContext.getResources().getString(R.string.d_promotion_verify),map,true,false);

        applyCouponRingfencingAPI(promocode, REQUEST_APPLY_PROMO);
    }

    private ArrayList<Integer> getIndexesClicked() {
        return listSelectedIndexes;
    }

    @Override
    protected int defineItemViewType(int position) {

        if (getJsonArray() == null) {
            return ITEM_VIEW_TYPE_EMPTY;
        }

        // Get JSON Object
        JSONObject jsonObject = getJsonArray().optJSONObject(position);

        // Get Section Type
        String sectionType = jsonObject.optString("section_type");

        if (!TextUtils.isEmpty(sectionType)) {

            if ("promo_input".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_PROMO_INPUT;

            } else if ("promo_data_header".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_PROMO_DATA_HEADER;

            } else if ("promo_data".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_PROMO_DATA;

            }
        }

        return ITEM_VIEW_TYPE_EMPTY;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_PROMO_INPUT) {
            return new HeaderViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.ring_fencing_header_layout, parent, false));

        } else if (viewType == ITEM_TYPE_PROMO_DATA_HEADER) {

            return new RestaurantHeaderViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.ring_fencing_restaurant_header_layout, parent, false));

        } else if (viewType == ITEM_TYPE_PROMO_DATA) {

            return new PromoDataViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.ring_fencing_promo_data_layout, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {

        String sectionKey = listItem.optString("section_key");

        if (holder == null) {
            return;
        }

        if (holder.getItemViewType() == ITEM_TYPE_PROMO_INPUT) {
            inflateHeaderTypeView(sectionKey, (HeaderViewHolder) holder);


        } else if (holder.getItemViewType() == ITEM_TYPE_PROMO_DATA_HEADER) {
            inflateRestaurantHeaderTypeView(sectionKey, (RestaurantHeaderViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_PROMO_DATA) {
            inflatePromoDataTypeView(sectionKey, (PromoDataViewHolder) holder, position);

        }

    }

    private void closeKeyBoard(View view) {
        InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void inflateHeaderTypeView(String sectionKey, final HeaderViewHolder headerViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("label"))) {
                headerViewHolder.getTextViewpromo_label().setText(jsonObjectSectionKeyData.opt("label").toString());
            }
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("text_box_hint"))) {
                headerViewHolder.getEdittextpromo_input().setHint(jsonObjectSectionKeyData.opt("text_box_hint").toString());
                headerViewHolder.getEdittextpromo_input().setText("");
            }

            if (!TextUtils.isEmpty(promocode)) {
                headerViewHolder.getEdittextpromo_input().setText(promocode);
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("btn_txt"))) {
                headerViewHolder.getButtonvalidate_promo().setText(jsonObjectSectionKeyData.opt("btn_txt").toString());
                headerViewHolder.getButtonvalidate_promo().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeKeyBoard(v);
                        String promoText = headerViewHolder.getEdittextpromo_input().getText().toString();
                        if (!TextUtils.isEmpty(promoText)) {
                            setOnApplyButtonClicked(promoText);
                        } else {
                            Toast.makeText(mContext, "Please enter promocode", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }


            headerViewHolder.getEdittextpromo_input().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    //track for countly
                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(mContext);
                    if (hMap != null) {
                        hMap.put("category", mContext.getString(R.string.countly_promotion));
                        hMap.put("action", mContext.getResources().getString(R.string.d_promotion_code_type));
                        hMap.put("label", s.toString());
                    }

                     //track for ga and countly
                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly(mContext.getResources().getString(R.string.d_promotion_code_type), hMap);
                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.countly_promotion),
                            mContext.getResources().getString(R.string.d_promotion_code_type), s.toString());

                    //track for qgraph, apsalar
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("label", s.toString());

                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventQGraphApsalar(mContext.getResources().getString(R.string.d_promotion_code_type),
                            map, true, false);
                }
            });
        }
    }

    private void inflateRestaurantHeaderTypeView(String sectiocKey, RestaurantHeaderViewHolder restaurantHeaderViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectiocKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectiocKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");

            if (!TextUtils.isEmpty(jsonObjectSectiocKeyData.optString("header"))) {
                restaurantHeaderViewHolder.getTextView_restaurant_specific_coupon_header().setText(jsonObjectSectiocKeyData.opt("header").toString());
            }

            if (!TextUtils.isEmpty(jsonObjectSectiocKeyData.optString("label"))) {
                restaurantHeaderViewHolder.getTextView_lable().setText(jsonObjectSectiocKeyData.opt("label").toString());

            }
        }

    }

    private void inflatePromoDataTypeView(String sectionKey, PromoDataViewHolder promoDataViewHolder, int position) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {

            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            preparePromoData(jsonObjectSectionKeyData, promoDataViewHolder, position);
        }
    }

    private void preparePromoData(final JSONObject jsonObjectsectionKeyData, PromoDataViewHolder promoDataViewHolder, final int position) {

        if (jsonObjectsectionKeyData != null) {
            if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("rest_name"))) {
                promoDataViewHolder.getTextView_restaurant_name().setText(jsonObjectsectionKeyData.opt("rest_name").toString());
            }
            if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("costFor2"))) {
                promoDataViewHolder.getTextView_cost_for_two().setText("Cost For 2 -" + jsonObjectsectionKeyData.opt("costFor2").toString() + " approx");
            }

            if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("locality_name"))) {
                promoDataViewHolder.getTextView_locality_name().setText(jsonObjectsectionKeyData.opt("locality_name").toString());
            }

            if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("promocode_amount"))) {
                promoDataViewHolder.getTextView_promocode_amount().setText(
                        AppUtil.renderRupeeSymbol(jsonObjectsectionKeyData.opt("promocode_amount").toString()));
            }

            if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("expire_datetime"))) {
                promoDataViewHolder.getTextView_expire_datetime().setText(jsonObjectsectionKeyData.opt("expire_datetime").toString());
            }

            if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("coupon_applied"))) {
                if (jsonObjectsectionKeyData.optString("coupon_applied").equals("0") && !getIndexesClicked().contains(position)) {
                    promoDataViewHolder.getTextView_coupon_applied().setVisibility(View.INVISIBLE);
                    if (!TextUtils.isEmpty(jsonObjectsectionKeyData.optString("apply_btn_label"))) {
                        promoDataViewHolder.getButton_reserve_or_apply_btn().setText(jsonObjectsectionKeyData.opt("apply_btn_label").toString());
                        promoDataViewHolder.getButton_reserve_or_apply_btn().setBackgroundResource(R.drawable.ring_fencing_boundary);
                        promoDataViewHolder.getButton_reserve_or_apply_btn().setTextColor(mContext.getResources().getColor(R.color.green));
                    }
                } else {
                    promoDataViewHolder.getButton_reserve_or_apply_btn().setBackgroundResource(R.drawable.ring_fencing_reserve_boundary);
                    promoDataViewHolder.getTextView_coupon_applied().setVisibility(View.VISIBLE);
                    promoDataViewHolder.getButton_reserve_or_apply_btn().setText(jsonObjectsectionKeyData.opt("reserve_btn_label").toString());
                    promoDataViewHolder.getTextView_coupon_applied().setText(jsonObjectsectionKeyData.opt("coupon_applied_text").toString());
                    promoDataViewHolder.getButton_reserve_or_apply_btn().setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                }

                promoDataViewHolder.getButton_reserve_or_apply_btn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String couponApplied = jsonObjectsectionKeyData.optString("coupon_applied");
                        closeKeyBoard(v);

                        if ((listSelectedIndexes != null && listSelectedIndexes.contains(position)) || couponApplied.equalsIgnoreCase("1")) {
                            //reserve click
                            //track event
//                            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_promotions),
//                                    "ReserveNow", jsonObjectsectionKeyData.optString("rest_name") + "_" +
//                                            AppUtil.renderRupeeSymbol(jsonObjectsectionKeyData.opt("promocode_amount").toString()));

                            String callToAction = jsonObjectsectionKeyData.optString("deeplink");
                            getOnReserveButtonClickListener().onReserveButtonClicked(callToAction);


                        } else {
//                            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_promotions),
//                                    "ApplyCoupon", jsonObjectsectionKeyData.optString("rest_name") + "_" +
//                                            AppUtil.renderRupeeSymbol(jsonObjectsectionKeyData.opt("promocode_amount").toString()));


                            String promoCode = jsonObjectsectionKeyData.optString("promocode");
                            setIndexesClicked(promoCode, position);
                        }
                    }
                });
            }
        }
    }

    public void setRingFencingDataObject(JSONArray arr, JSONObject object) {
        this.jsonObjectSectionData = object;
        setJsonArray(arr);
    }

    public void setRingfencingPromocode(String promocodes) {
        promocode = promocodes;
    }

    public void updateSectionObject(JSONObject jsonObject) {
        jsonObjectSectionData = jsonObject;
    }

    private JSONObject getSectionDataDetails(String sectionKey) {
        // Check on Section Key
        if (!TextUtils.isEmpty(sectionKey) && jsonObjectSectionData != null) {
            return jsonObjectSectionData.optJSONObject(sectionKey);
        } else {
            return null;
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        if (request.getIdentifier() == REQUEST_APPLY_PROMO) {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    JSONObject outputParams = responseObject.optJSONObject("output_params");
                    JSONObject data = outputParams.optJSONObject("data");
                    String title = data.optString("title");
                    String msg = data.optString("msg");
                    getOnPromoButtonClickListener().onPromoButtonClicked(title, msg);
                    notifyItemChanged(0);
                } else if (responseObject.optJSONObject("res_auth") != null &&
                        !responseObject.optJSONObject("res_auth").optBoolean("status")) {
                    // Show Message
                    Toast.makeText(mContext,
                            responseObject.optString("error_msg"),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Show Message
                    Toast.makeText(mContext,
                            responseObject.optString("error_msg"),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                // Show Message
                Toast.makeText(mContext,
                        mContext.getString(R.string.text_general_error_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    listSelectedIndexes.add(request.getIdentifier());
                    notifyItemChanged(request.getIdentifier());
                    JSONObject outputParams = responseObject.optJSONObject("output_params");
                    JSONObject data = outputParams.optJSONObject("data");
                    String title = data.optString("title");
                    String msg = data.optString("msg");
                    getOnPromoButtonClickListener().onPromoButtonClicked(title, msg);

                } else if (responseObject.optJSONObject("res_auth") != null &&
                        !responseObject.optJSONObject("res_auth").optBoolean("status")) {
                    // Show Message
                    Toast.makeText(mContext,
                            responseObject.optString("error_msg"),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Show Message
                    Toast.makeText(mContext,
                            responseObject.optString("error_msg"),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                // Show Message
                Toast.makeText(mContext,
                        mContext.getString(R.string.text_general_error_message),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public OnReserveButtonClickListener getOnReserveButtonClickListener() {
        return onReserveButtonClickListener;
    }

    public void setOnReserveButtonClickListener(OnReserveButtonClickListener onReserveButtonClickListener) {
        this.onReserveButtonClickListener = onReserveButtonClickListener;
    }

    public OnPromoButtonClickListener getOnPromoButtonClickListener() {
        return onPromoButtonClickListener;
    }

    public void setOnPromoButtonClickListener(OnPromoButtonClickListener onPromoButtonClickListener) {
        this.onPromoButtonClickListener = onPromoButtonClickListener;
    }

    private void applyCouponRingfencingAPI(String promocode, int positionChanged) {
        networkManager.jsonRequestGet(positionChanged, AppConstant.URL_RING_FENCING_VERYFY_REFERAL,
                ApiParams.getRingFencingVerifyReferralParams(promocode),
                this, null, false);
    }

    public interface OnReserveButtonClickListener {
        void onReserveButtonClicked(String deeplink);
    }

    public interface OnPromoButtonClickListener {
        void onPromoButtonClicked(String title, String msg);
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewpromo_label;
        private EditText edittextpromo_input;
        private Button buttonvalidate_promo;


        public HeaderViewHolder(View itemView) {
            super(itemView);
            textViewpromo_label = (TextView) itemView.findViewById(R.id.promo_label);
            edittextpromo_input = (EditText) itemView.findViewById(R.id.promo_input);
            buttonvalidate_promo = (Button) itemView.findViewById(R.id.validate_promo);
        }

        public TextView getTextViewpromo_label() {
            return textViewpromo_label;
        }

        public EditText getEdittextpromo_input() {
            return edittextpromo_input;
        }

        public Button getButtonvalidate_promo() {
            return buttonvalidate_promo;
        }
    }

    private class RestaurantHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_restaurant_specific_coupon_header;
        private TextView textView_lable;


        public RestaurantHeaderViewHolder(View itemView) {
            super(itemView);
            textView_restaurant_specific_coupon_header = (TextView) itemView.findViewById(R.id.restaurant_specific_coupon_header);
            textView_lable = (TextView) itemView.findViewById(R.id.label);
        }


        public TextView getTextView_restaurant_specific_coupon_header() {
            return textView_restaurant_specific_coupon_header;
        }

        public TextView getTextView_lable() {
            return textView_lable;
        }
    }

    private class PromoDataViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_restaurant_name;
        private TextView textView_cost_for_two;
        private TextView textView_locality_name;
        private TextView textView_promocode_amount;
        private TextView textView_expire_datetime;
        private TextView textView_coupon_applied;
        private Button button_reserve_or_apply_btn;

        public PromoDataViewHolder(View itemView) {
            super(itemView);
            textView_restaurant_name = (TextView) itemView.findViewById(R.id.restaurant_name);
            textView_cost_for_two = (TextView) itemView.findViewById(R.id.cost_for_two);
            textView_locality_name = (TextView) itemView.findViewById(R.id.restaurant_location);
            textView_promocode_amount = (TextView) itemView.findViewById(R.id.cashback);
            textView_expire_datetime = (TextView) itemView.findViewById(R.id.expire_datetime);
            textView_coupon_applied = (TextView) itemView.findViewById(R.id.coupon_applied_or_not);
            button_reserve_or_apply_btn = (Button) itemView.findViewById(R.id.reserve_or_apply_button);
        }

        public TextView getTextView_restaurant_name() {
            return textView_restaurant_name;
        }

        public TextView getTextView_cost_for_two() {
            return textView_cost_for_two;
        }

        public TextView getTextView_locality_name() {
            return textView_locality_name;
        }

        public TextView getTextView_promocode_amount() {
            return textView_promocode_amount;
        }

        public TextView getTextView_expire_datetime() {
            return textView_expire_datetime;
        }

        public TextView getTextView_coupon_applied() {
            return textView_coupon_applied;
        }

        public Button getButton_reserve_or_apply_btn() {
            return button_reserve_or_apply_btn;
        }
    }
}
