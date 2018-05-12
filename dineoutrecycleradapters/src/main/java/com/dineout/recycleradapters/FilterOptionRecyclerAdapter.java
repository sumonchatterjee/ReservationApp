package com.dineout.recycleradapters;

import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class FilterOptionRecyclerAdapter extends BaseRecyclerAdapter {

    FilterOptionListener optionRecyclerListener;
    private JSONArray completeFilterOptionJsonArray;

    void setCompleteFilterOptionJsonArray(JSONArray completeFilterOptionJsonArray) {
        this.completeFilterOptionJsonArray = completeFilterOptionJsonArray;
    }

    public void setOptionRecyclerListener(FilterOptionListener optionRecyclerListener) {
        this.optionRecyclerListener = optionRecyclerListener;
    }

    public abstract void setListJsonArray(JSONArray newJsonArray);

    public void showMatchedFilterOptions(String searchText) {
        // Check for Empty/NULL
        if (AppUtil.isStringEmpty(searchText)) {
            // Reset Filter Options
            setJsonArray(completeFilterOptionJsonArray);

        } else {
            // Set Matched Filter Options
            setJsonArray(getMatchedFilterOptions(searchText));
        }

        // Notify Data Change
        notifyDataSetChanged();
    }

    private JSONArray getMatchedFilterOptions(String searchText) {
        JSONArray matchedFilterOptionJsonArray = new JSONArray();
        int completeFilterOptionSize = completeFilterOptionJsonArray.length();

        for (int index = 0; index < completeFilterOptionSize; index++) {
            // Get Filter Option
            JSONObject filterJsonObject = completeFilterOptionJsonArray.optJSONObject(index);

            if (filterJsonObject != null) {
                // Get Option Name
                String optionName = filterJsonObject.optString("name", "");
                if (!AppUtil.isStringEmpty(optionName) &&
                        optionName.toLowerCase().contains(searchText.toLowerCase())) {
                    // Add Filter Option to Matched Filter List
                    matchedFilterOptionJsonArray.put(filterJsonObject);
                }
            }
        }

        return matchedFilterOptionJsonArray;
    }

    public interface FilterOptionListener {
        void onFilterOptionClick(JSONObject optionJsonObject);
    }
}
