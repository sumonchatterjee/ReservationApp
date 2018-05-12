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

public class EarningFilterPopUpAdapter extends BaseRecyclerAdapter {

    private static final int ITEM_TYPE_POP_UP = 0;
    private LayoutInflater mInflater;
    private FilterCallback mFilterCallback;
    private FilterPopUpDismissCallback mFilterPopUpDismissCallback;

    public interface FilterCallback {
        void onFilterClick(JSONObject obj);
    }

    public interface FilterPopUpDismissCallback {
        void onDismiss();
    }

    public EarningFilterPopUpAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected int defineItemViewType(int position) {
        int returnValue;
        if (getJsonArray() == null) {
            returnValue = ITEM_VIEW_TYPE_EMPTY;
        } else {
            returnValue = ITEM_TYPE_POP_UP;
        }

        return returnValue;
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder returnValue;
        if (viewType == ITEM_TYPE_POP_UP) {
            View view = mInflater.inflate(R.layout.adpater_earning_filter_item, parent, false);
            returnValue = new PopUpItemHolder(view);

        } else {
            returnValue = null;
        }

        return returnValue;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        ((PopUpItemHolder) holder).bindData(listItem, position);
    }

    private class PopUpItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewGroup filterWrapper;
        RadioButton filterRadioButton;
        TextView filterText;

        PopUpItemHolder(View itemView) {
            super(itemView);

            filterWrapper = (ViewGroup) itemView.findViewById(R.id.filter_item_wrapper);
            filterRadioButton = (RadioButton) itemView.findViewById(R.id.radio_btn_iv);
            filterText = (TextView) itemView.findViewById(R.id.radio_btn_tv);
        }

        void bindData(JSONObject itemObj, int pos) {
            if (itemObj != null) {
                // set text
                setTextViewInfo(filterText, itemObj.optJSONObject("text"));

                // set checked status
                filterRadioButton.setChecked(itemObj.optInt("selected", 0) == 1);

                // on click
                filterWrapper.setTag(itemObj);
                filterWrapper.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.filter_item_wrapper) {
                RadioButton rb = (RadioButton) v.findViewById(R.id.radio_btn_iv);
                if (rb != null && !rb.isChecked() && mFilterCallback != null) {
                    mFilterCallback.onFilterClick(((JSONObject) v.getTag()));
                }

                if (mFilterPopUpDismissCallback != null) {
                    mFilterPopUpDismissCallback.onDismiss();
                }
            }
        }
    }

    public FilterCallback getCallback() {
        return mFilterCallback;
    }

    public void setFilterCallback(FilterCallback callback) {
        this.mFilterCallback = callback;
    }

    public void setFilterPopUpDimissCallback(FilterPopUpDismissCallback callback) {
        this.mFilterPopUpDismissCallback = callback;
    }
}
