package com.dineout.book.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dineout.book.R;

import org.json.JSONArray;
import org.json.JSONObject;


public class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int HEADER = 0x03;
    private final int SECTION = 0x04;
    private Context mContext;
    private JSONArray invoiceArray;
    private JSONObject invoiceDetail;

    public InvoiceAdapter(Context context, JSONObject invoice) {
        this.mContext = context;

        invoiceArray = invoice.optJSONArray("section");
        invoiceDetail = invoice.optJSONObject("section_data");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER)
            return new HeaderHolder(LayoutInflater.from(mContext).inflate(R.layout.invoice_header_section_container, parent, false));
        else
            return new SectionHolder(LayoutInflater.from(mContext).inflate(R.layout.invoice_section_container, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == HEADER) {
            String key = invoiceArray.optJSONObject(position).optString("section_key");
            if (!TextUtils.isEmpty(key)) {
                ((HeaderHolder) holder).createHeader(invoiceDetail.optJSONObject(key));
            }
        } else if (getItemViewType(position) == SECTION) {
            String key = invoiceArray.optJSONObject(position).optString("section_key");
            if (!TextUtils.isEmpty(key)) {
                ((SectionHolder) holder).createSection(invoiceDetail.optJSONObject(key), key);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {

        JSONObject options = invoiceArray.optJSONObject(position);
        if (options.optString("section_type").equalsIgnoreCase("header"))
            return HEADER;
        else
            return SECTION;

    }

    @Override
    public int getItemCount() {
        return invoiceArray.length();
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {

        private View mRoot;

        public HeaderHolder(View itemView) {
            super(itemView);
            mRoot = itemView;
        }

        public void createHeader(JSONObject data) {

            JSONArray headerData = data.optJSONArray("data");
            if (headerData != null && headerData.length() > 0) {

                ((LinearLayout) mRoot).addView(createHeaderTitle(headerData.optJSONObject(0)));
                ((LinearLayout) mRoot).addView(createHeaderSubTitle(headerData.optJSONObject(1)));
            }


        }

        private View createHeaderTitle(JSONObject data) {

            View v = LayoutInflater.from(mContext).inflate(R.layout.invoice_header_title, null);
            ((TextView) v.findViewById(R.id.title)).setText(data.optString("title"));


            ((TextView) v.findViewById(R.id.value)).setText(String.format(mContext.getResources()
                    .getString(R.string.container_rupee), data.optString("value")));
            return v;
        }

        private View createHeaderSubTitle(JSONObject data) {

            View v = LayoutInflater.from(mContext).inflate(R.layout.invoice_header_subtitle, null);
            ((TextView) v.findViewById(R.id.title)).
                    setText(data.optString("title"));
            ((TextView) v.findViewById(R.id.value)).
                    setText(com.dineout.recycleradapters.util.AppUtil.renderRupeeSymbol(data.optString("value")));
            return v;
        }
    }

    private class SectionHolder extends RecyclerView.ViewHolder {

        private View mRoot;

        public SectionHolder(View itemView) {
            super(itemView);
            mRoot = itemView;
        }

        public void createSection(JSONObject data, String key) {

            JSONArray headerData = data.optJSONArray("data");
            ((LinearLayout) mRoot).addView(createSectionTitle(data));

            boolean isBilling = data.optString("title").equalsIgnoreCase("billing");

            View v = new View(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            v.setBackgroundColor(mContext.getResources().getColor(R.color.dividerColor));
            params.setMargins(5, 0, 0, 0);
            v.setLayoutParams(params);
            ((LinearLayout) mRoot).addView(v);
            if (headerData != null && headerData.length() > 0) {

                for (int i = 0; i < headerData.length(); i++) {

                    ((LinearLayout) mRoot).addView(createSectionCell(headerData.optJSONObject(i), key));

                    if (i != headerData.length() - 1 && !isBilling) {

                        View view = new View(mContext);
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.dividerColor));
                        view.setLayoutParams(params);
                        ((LinearLayout) mRoot).addView(view);

                    } else if (isBilling && i == headerData.length() - 2) {
                        View view = new View(mContext);
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.dividerColor));
                        view.setLayoutParams(params);
                        ((LinearLayout) mRoot).addView(view);
                        ((LinearLayout) mRoot).addView(v);
                    }
                }

            }
        }

        private View createSectionTitle(JSONObject data) {

            View v = LayoutInflater.from(mContext).inflate(R.layout.invoice_section_title, null);
            ((TextView) v.findViewById(R.id.title)).setText(data.optString("header_title"));
            return v;
        }

        private View createSectionCell(JSONObject data, String key) {

            View v = LayoutInflater.from(mContext).inflate(R.layout.invoice_section_cell, null);
            LinearLayout dealsTimingsLayout = (LinearLayout) v.findViewById(R.id.middle_layout);
            LinearLayout dealsValidityLayout = (LinearLayout) v.findViewById(R.id.last_layout);
            TextView title = (TextView) v.findViewById(R.id.title);
            TextView value = (TextView) v.findViewById(R.id.value);
            TextView dealTiming = (TextView) v.findViewById(R.id.deal_timing);
            TextView dealQuantity = (TextView) v.findViewById(R.id.deal_quantity);
            TextView dealValidity = (TextView) v.findViewById(R.id.deal_validity);

            if (key.equalsIgnoreCase("payment")) {
                value.setText(String.format(mContext.getResources()
                        .getString(R.string.container_rupee), data.optString("value")));
            } else {
                value.setText(data.optString("value"));
            }


            if (data.optString("title").equalsIgnoreCase("Total Bill Amount")) {
                value.setTextColor(mContext.getResources().getColor(R.color.green));
            } else {
                value.setTextColor(mContext.getResources().getColor(R.color.grey_4D));

            }

            if (!TextUtils.isEmpty(data.optString("title"))) {
                if (data.has("ticket_title")){
                    title.setText(data.optString("ticket_title"));
                }else{
                    title.setText(data.optString("title"));
                }

                title.setVisibility(View.VISIBLE);

            } else {
                title.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(data.optString("price"))) {
                value.setText(String.format(mContext.getString(com.dineout.recycleradapters.R.string.container_rupee), data.optString("price")));
                value.setTextColor(mContext.getResources().getColor(R.color.green));
            }

            if (!TextUtils.isEmpty(data.optString("time"))) {
                dealTiming.setText(data.optString("time"));
                dealsTimingsLayout.setVisibility(View.VISIBLE);

            } else {
                dealsTimingsLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(data.optString("quantity"))) {
                dealQuantity.setText("Qty: " + data.optString("quantity"));

            }

            if (!TextUtils.isEmpty(data.optString("valid_for"))) {
                dealValidity.setText(data.optString("valid_for"));
                dealsValidityLayout.setVisibility(View.VISIBLE);

            } else {
                dealsValidityLayout.setVisibility(View.GONE);
            }


            return v;
        }


    }
}
