package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.dineoutnetworkmodule.AppConstant.RUPEE_IDENTIFIER;

public class FilterRadioOptionRecyclerAdpater extends FilterOptionRecyclerAdapter
        implements View.OnClickListener {

    private final int ITEM_VIEW_TYPE_RADIO = 1;

    private LayoutInflater layoutInflater;

    public FilterRadioOptionRecyclerAdpater(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void setListJsonArray(JSONArray newJsonArray) {
        super.setJsonArray(newJsonArray);

        setCompleteFilterOptionJsonArray(newJsonArray);
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_VIEW_TYPE_RADIO;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check Item View Type
        if (viewType == ITEM_VIEW_TYPE_RADIO) {
            return new RadioGroupListViewHolder(
                    layoutInflater.inflate(R.layout.radio_button_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item View Type
        int itemViewType = holder.getItemViewType();

        if (itemViewType == ITEM_VIEW_TYPE_RADIO) {
            showRadioButtonOption((RadioGroupListViewHolder) holder, listItem);
        }
    }

    private void showRadioButtonOption(RadioGroupListViewHolder viewHolder, JSONObject listItem) {
        if (viewHolder != null && listItem != null) {
            // Get Option Name
            String optionName = listItem.optString("name");
            if (!AppUtil.isStringEmpty(optionName)) {
                // Replace ## with rupee sign
                viewHolder.getRadioListItem().
                        setText((optionName.contains(RUPEE_IDENTIFIER)) ? AppUtil.replaceForRupeeSign(optionName) : optionName);
            }

            // Set Checked
            boolean isApplied = (listItem.optInt("applied", 0) == 1);
            viewHolder.getRadioListItem().setChecked(isApplied);

            // Set Click Listener
            viewHolder.getRadioListItem().setTag(listItem);
            viewHolder.getRadioListItem().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.radio_list_item) {
            handleRadioButtonClick((JSONObject) view.getTag());
        }
    }

    private void handleRadioButtonClick(JSONObject optionJsonObject) {
        // Handle UI update
        handleUIUpdate(optionJsonObject);

        // Handle Option Click
        if (optionRecyclerListener != null) {
            optionRecyclerListener.onFilterOptionClick(optionJsonObject);
        }
    }

    private void handleUIUpdate(JSONObject optionJsonObject) {
        // Check for NULL
        if (optionJsonObject != null) {
            // Reset Applied Flag
            resetAppliedInOptions();

            // Set Applied flag of Selected Filter
            try {
                optionJsonObject.put("applied", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Notify List Data change
            notifyDataSetChanged();
        }
    }

    private void resetAppliedInOptions() {
        // Get Options List
        JSONArray optionJsonArray = getJsonArray();

        if (optionJsonArray != null) {
            int optionSize = optionJsonArray.length();

            for (int index = 0; index < optionSize; index++) {
                // Get Option
                JSONObject optionJsonObject = optionJsonArray.optJSONObject(index);

                if (optionJsonObject != null) {
                    // Reset Applied Flag
                    try {
                        optionJsonObject.put("applied", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class RadioGroupListViewHolder extends RecyclerView.ViewHolder {
        private RadioButton radioListItem;

        RadioGroupListViewHolder(View itemView) {
            super(itemView);

            radioListItem = (RadioButton) itemView.findViewById(R.id.radio_list_item);
        }

        RadioButton getRadioListItem() {
            return radioListItem;
        }
    }
}
