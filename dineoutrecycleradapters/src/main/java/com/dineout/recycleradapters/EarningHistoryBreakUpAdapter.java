package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONObject;

import static com.dineout.recycleradapters.util.AppUtil.setTextViewInfo;

public class EarningHistoryBreakUpAdapter extends BaseRecyclerAdapter {

    private static final int ITEM_TYPE_EARNING_BREAK_UP = 0;
    private LayoutInflater mInflater;

    public EarningHistoryBreakUpAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected int defineItemViewType(int position) {
        int returnValue;
        if (getJsonArray() == null) {
            returnValue = ITEM_VIEW_TYPE_EMPTY;
        } else {
            returnValue = ITEM_TYPE_EARNING_BREAK_UP;
        }

        return returnValue;
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder returnValue;
        if (viewType == ITEM_TYPE_EARNING_BREAK_UP) {
            View view = mInflater.inflate(R.layout.adapter_earning_history_break_up_item, parent, false);
            returnValue = new EarningHistoryBreakUpHolder(view);

        } else {
            returnValue = null;
        }

        return returnValue;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        ((EarningHistoryBreakUpHolder) holder).bindData(listItem, position);
    }

    private class EarningHistoryBreakUpHolder extends RecyclerView.ViewHolder {
        TextView earningBalanceTv;
        TextView earningExpireTv;

        EarningHistoryBreakUpHolder(View itemView) {
            super(itemView);

            earningBalanceTv = (TextView) itemView.findViewById(R.id.earning_balance_tv);
            earningExpireTv = (TextView) itemView.findViewById(R.id.earning_expire_tv);
        }

        void bindData(JSONObject obj, int pos) {
            // set amount
            setTextViewInfo(earningBalanceTv, obj.optJSONObject("amount"));

            // set amount text
            setTextViewInfo(earningExpireTv, obj.optJSONObject("expiry"));
        }
    }
}
