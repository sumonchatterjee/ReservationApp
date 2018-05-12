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

public class FilterDeselectableOptionRecyclerAdapter extends FilterOptionRecyclerAdapter
        implements View.OnClickListener {

    private final int ITEM_VIEW_TYPE_DESELECTABLE_RADIO = 1;

    private LayoutInflater layoutInflater;

    public FilterDeselectableOptionRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void setListJsonArray(JSONArray newJsonArray) {
        super.setJsonArray(newJsonArray);

        setCompleteFilterOptionJsonArray(newJsonArray);
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_VIEW_TYPE_DESELECTABLE_RADIO;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check Item View Type
        if (viewType == ITEM_VIEW_TYPE_DESELECTABLE_RADIO) {
            return new DeselectableRadioOptionViewHolder(
                    layoutInflater.inflate(R.layout.deselectable_radio_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item View Type
        int itemViewType = holder.getItemViewType();

        if (itemViewType == ITEM_VIEW_TYPE_DESELECTABLE_RADIO) {
            showDeselectableRadioOption((DeselectableRadioOptionViewHolder) holder, listItem);
        }
    }

    private void showDeselectableRadioOption(DeselectableRadioOptionViewHolder viewHolder, JSONObject listItem) {
        if (viewHolder != null && listItem != null) {
            // Set Option Name
            String optionName = listItem.optString("name");
            if (!AppUtil.isStringEmpty(optionName)) {
                // Replace ## with rupee sign
                viewHolder.getDeselectableRadioItem().
                        setText((optionName.contains(RUPEE_IDENTIFIER)) ? AppUtil.replaceForRupeeSign(optionName) : optionName);
            }

            // Set Checked
            boolean isApplied = (listItem.optInt("applied", 0) == 1);
            viewHolder.getDeselectableRadioItem().setChecked(isApplied);

            // Set Click Listener
            viewHolder.getDeselectableRadioItem().setTag(listItem);
            viewHolder.getDeselectableRadioItem().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.deselectable_radio_item) {
            handleDeselectableRadioButtonClick((JSONObject) view.getTag());
        }
    }

    private void handleDeselectableRadioButtonClick(JSONObject optionJsonObject) {
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
            // Get Current Applied Status of Selected Option
            int applied = optionJsonObject.optInt("applied", 0);
            int intendedApplied = ((applied == 0) ? 1 : 0);

            // Reset Applied Flag
            resetAppliedInOptions();

            // Set Applied flag of Selected Filter
            try {
                optionJsonObject.put("applied", intendedApplied);
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

    private class DeselectableRadioOptionViewHolder extends RecyclerView.ViewHolder {
        private RadioButton deselectableRadioItem;

        DeselectableRadioOptionViewHolder(View itemView) {
            super(itemView);

            deselectableRadioItem = (RadioButton) itemView.findViewById(R.id.deselectable_radio_item);
        }

        RadioButton getDeselectableRadioItem() {
            return deselectableRadioItem;
        }
    }
}
