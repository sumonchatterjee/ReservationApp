package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.dineoutnetworkmodule.AppConstant.PARAM_POSITION;
import static com.example.dineoutnetworkmodule.AppConstant.RUPEE_IDENTIFIER;

public class FilterCheckBoxOptionRecyclerAdapter extends FilterOptionRecyclerAdapter
        implements View.OnClickListener {

    private final int ITEM_VIEW_TYPE_CHECKBOX = 1;

    private LayoutInflater layoutInflater;

    public FilterCheckBoxOptionRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void setListJsonArray(JSONArray newJsonArray) {
        super.setJsonArray(newJsonArray);

        setCompleteFilterOptionJsonArray(newJsonArray);
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_VIEW_TYPE_CHECKBOX;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check Item View Type
        if (viewType == ITEM_VIEW_TYPE_CHECKBOX) {
            return new CheckBoxOptionViewHolder(
                    layoutInflater.inflate(R.layout.checkbox_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item View Type
        int itemViewType = holder.getItemViewType();

        if (itemViewType == ITEM_VIEW_TYPE_CHECKBOX) {
            showCheckboxOptions((CheckBoxOptionViewHolder) holder, listItem, position);
        }
    }

    private void showCheckboxOptions(CheckBoxOptionViewHolder viewHolder, JSONObject listItem, int position) {
        if (viewHolder != null && listItem != null) {
            // Set Option Name
            String optionName = listItem.optString("name");
            if (!AppUtil.isStringEmpty(optionName)) {
                // Replace ## with rupee sign
                viewHolder.getCheckBoxItem().
                        setText((optionName.contains(RUPEE_IDENTIFIER)) ? AppUtil.replaceForRupeeSign(optionName) : optionName);
            }

            // Set Position in List Item
            try {
                listItem.put(PARAM_POSITION, position);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Set Checked
            boolean isApplied = (listItem.optInt("applied", 0) == 1);
            viewHolder.getCheckBoxItem().setChecked(isApplied);

            // Set Click Listener
            viewHolder.getCheckBoxItem().setTag(listItem);
            viewHolder.getCheckBoxItem().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.checkBox_item) {
            handleCheckboxClick((JSONObject) view.getTag());
        }
    }

    private void handleCheckboxClick(JSONObject optionJsonObject) {
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

    private class CheckBoxOptionViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBoxItem;

        CheckBoxOptionViewHolder(View itemView) {
            super(itemView);

            checkBoxItem = (CheckBox) itemView.findViewById(R.id.checkBox_item);
        }

        CheckBox getCheckBoxItem() {
            return checkBoxItem;
        }
    }
}
