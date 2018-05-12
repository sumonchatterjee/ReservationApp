package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sumon.chatterjee on 14/06/16.
 */
public class EarningsHistoryRecyclerAdapter extends BaseRecyclerAdapter implements View.OnClickListener {
    private static final int ITEM_TYPE_DINER_WALLET = 1;
    private static final int ITEM_TYPE_ENTITY_DATA = 2;
    Context mContext;
    private JSONObject jsonObjectSectionData;

    public EarningsHistoryRecyclerAdapter(Context context) {
        mContext = context;
    }

    public void setMyEarningsObject(JSONArray arr, JSONObject object) {
        this.jsonObjectSectionData = object;
        setJsonArray(arr);
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

            if ("diner_wallet".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_DINER_WALLET;

            } else if ("wallet_transation".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_ENTITY_DATA;

            }
        }
        return ITEM_VIEW_TYPE_EMPTY;

    }


    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        String sectionKey = listItem.optString("section_key");

        if (holder == null) {
            return;
        }
        if (holder.getItemViewType() == ITEM_TYPE_DINER_WALLET) {
            inflateDinnerWalletTypeView(sectionKey, (DinnerWalletViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_ENTITY_DATA) {
            inflateEntityDataHeaderTypeView(sectionKey, (EntityDataViewHolder) holder);

        }


    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_DINER_WALLET) {

            return new DinnerWalletViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.earnings_transaction_top, parent, false));

        } else if (viewType == ITEM_TYPE_ENTITY_DATA) {

            return new EntityDataViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.earnings_details_view, parent, false));

        }
        return null;
    }

    @Override
    public void onClick(View v) {

    }

    private void inflateDinnerWalletTypeView(String sectionKey, DinnerWalletViewHolder holder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("label"))) {
                holder.getTextViewTitle().setText(jsonObjectSectionKeyData.opt("label").toString());
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("amount"))) {
                holder.getTextViewAmount().
                        setText(String.format(mContext.getString(R.string.container_rupee),
                                jsonObjectSectionKeyData.opt("amount").toString()));
            }
        }
    }

    private void inflateEntityDataHeaderTypeView(String sectionKey, EntityDataViewHolder holder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {

            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_1"))) {
                holder.getEarningsTitle().setText(jsonObjectSectionKeyData.opt("title_1").toString());
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_2"))) {
                holder.getEarningsSecondTitle().setText(jsonObjectSectionKeyData.opt("title_2").toString());
                holder.getEarningsSecondTitle().setVisibility(View.VISIBLE);
            } else {
                holder.getEarningsSecondTitle().setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_3"))) {
                holder.getEarningsLocation().setText(jsonObjectSectionKeyData.opt("title_3").toString());
            }
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_4"))) {
                holder.getEarningsDate().setText(jsonObjectSectionKeyData.opt("title_4").toString());
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("amount"))) {
                if (jsonObjectSectionKeyData.optString("amount").contains("-")) {
                    holder.getEarningsDetailAmount().setTextColor(mContext.getResources().getColor(R.color.red));
                    String amt = jsonObjectSectionKeyData.opt("amount").toString();
                    amt = amt.replace("-", "");
                    holder.getEarningsDetailAmount().
                            setText("- " + String.format(mContext.getString(R.string.container_rupee), amt));

                } else {
                    holder.getEarningsDetailAmount().setTextColor(mContext.getResources().getColor(R.color.green));
                    holder.getEarningsDetailAmount().setText(String.format(mContext.getString(R.string.container_rupee),
                            jsonObjectSectionKeyData.opt("amount").toString()));
                }
            }
        }
    }

    private JSONObject getSectionDataDetails(String sectionKey) {
        // Check on Section Key
        if (!TextUtils.isEmpty(sectionKey) && jsonObjectSectionData != null) {
            return jsonObjectSectionData.optJSONObject(sectionKey);
        } else {
            return null;
        }
    }

    private class DinnerWalletViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewAmount;

        public DinnerWalletViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.earnings_title);
            textViewAmount = (TextView) itemView.findViewById(R.id.earnings_amount);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public TextView getTextViewAmount() {
            return textViewAmount;
        }
    }

    private class EntityDataViewHolder extends RecyclerView.ViewHolder {
        TextView earningsTitle;
        TextView earningsDetailAmount;
        TextView earningsLocation;
        TextView earningsDate;
        TextView titleSecond;

        public EntityDataViewHolder(View itemView) {
            super(itemView);
            earningsTitle = (TextView) itemView.findViewById(R.id.earnings_title);
            earningsDetailAmount = (TextView) itemView.findViewById(R.id.earnings_amount);
            earningsLocation = (TextView) itemView.findViewById(R.id.earnings_location);
            earningsDate = (TextView) itemView.findViewById(R.id.earnings_date);
            titleSecond = (TextView) itemView.findViewById(R.id.earnings_title_second);
        }

        public TextView getEarningsTitle() {
            return earningsTitle;
        }

        public TextView getEarningsDetailAmount() {
            return earningsDetailAmount;
        }

        public TextView getEarningsLocation() {
            return earningsLocation;
        }

        public TextView getEarningsDate() {
            return earningsDate;
        }

        public TextView getEarningsSecondTitle() {
            return titleSecond;
        }
    }


}
