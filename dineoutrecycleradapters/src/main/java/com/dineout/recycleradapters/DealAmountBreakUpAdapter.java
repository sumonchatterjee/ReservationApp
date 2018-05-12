package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.recycleradapters.viewmodel.DealAmountBreakUpStyle0ViewModel;
import com.dineout.recycleradapters.viewmodel.DealAmountBreakUpStyle1ViewModel;
import com.dineout.recycleradapters.viewmodel.DealAmountBreakUpStyle2ViewModel;
import com.dineout.recycleradapters.viewmodel.DealAmountBreakUpStyle3ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sawai on 10/01/17.
 */

public class DealAmountBreakUpAdapter extends BaseRecyclerAdapter {
    private Context mContext;
    private LayoutInflater mInflater;

    public static final String VIEW_TYPE = "type";
    public static final String AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_0 = "AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_0";
    public static final String AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_1 = "AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_1";
    public static final String AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_2 = "AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_2";
    public static final String AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_3 = "AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_3";

    private static final int AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_0 = 0;
    private static final int AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_1 = 1;
    private static final int AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_2 = 2;
    private static final int AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_3 = 3;

    public DealAmountBreakUpAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    protected int defineItemViewType(int position) {
        int viewType = -1;
        JSONArray array;
        if ((array = getJsonArray()) != null && position < array.length()) {
            JSONObject itemObject = array.optJSONObject(position);
            if(itemObject != null) {
                String type = itemObject.optString(VIEW_TYPE);

                switch (type) {
                    case AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_0:
                        viewType = AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_0;
                        break;

                    case AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_1:
                        viewType = AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_1;
                        break;

                    case AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_2:
                        viewType = AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_2;
                        break;

                    case AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_3:
                        viewType = AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_3;
                        break;
                }
            }
        }

        return viewType;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder = null;

        switch (viewType) {
            case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_0:
                view =  mInflater.inflate(R.layout.adapter_item_deal_amount_break_up_style_0, parent, false);
                holder = new DealAmountBreakUpStyle0ViewModel(view);
                break;

            case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_1:
                view =  mInflater.inflate(R.layout.adapter_item_deal_amount_break_up_style_1, parent, false);
                holder = new DealAmountBreakUpStyle1ViewModel(view);
                break;

            case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_2:
                view =  mInflater.inflate(R.layout.adapter_item_deal_amount_break_up_style_2, parent, false);
                holder = new DealAmountBreakUpStyle2ViewModel(view);
                break;

            case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_3:
                view =  mInflater.inflate(R.layout.adapter_item_deal_amount_break_up_style_3, parent, false);
                holder = new DealAmountBreakUpStyle3ViewModel(view);
                break;
        }

        return holder;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder != null) {
            switch (defineItemViewType(position)) {
                case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_0:
                    ((DealAmountBreakUpStyle0ViewModel) holder).bind(position, listItem);
                    break;

                case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_1:
                    ((DealAmountBreakUpStyle1ViewModel) holder).bind(position, listItem);
                    break;

                case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_2:
                    ((DealAmountBreakUpStyle2ViewModel) holder).bind(position, listItem);
                    break;

                case AMOUNT_BREAK_UP_SCREEN_STYLE_VIEW_TYPE_3:
                    ((DealAmountBreakUpStyle3ViewModel) holder).bind(position, listItem);
                    break;
            }
        }
    }
}
