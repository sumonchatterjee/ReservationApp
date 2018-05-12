package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.dineoutnetworkmodule.AppConstant.PARAM_POSITION;

public class FilterCategoryRecyclerAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private final int ITEM_VIEW_TYPE_CATEGORY = 1;
    public static final String PARAM_IS_SELECTED = "isSelected";
    public static final String PARAM_IS_APPLIED = "isApplied";

    private LayoutInflater layoutInflater;
    private FilterCategoryClickListener filterCategoryClickListener;

    public FilterCategoryRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setFilterCategoryClickListener(FilterCategoryClickListener filterCategoryClickListener) {
        this.filterCategoryClickListener = filterCategoryClickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_VIEW_TYPE_CATEGORY;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_CATEGORY) {
            return new FilterCategoryViewHolder(
                    layoutInflater.inflate(R.layout.filter_category_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get View Type
        int viewType = holder.getItemViewType();

        if (viewType == ITEM_VIEW_TYPE_CATEGORY) {
            showFilterCategory((FilterCategoryViewHolder) holder, listItem, position);
        }
    }

    private void showFilterCategory(FilterCategoryViewHolder holder, JSONObject listItem, int position) {
        if (holder != null && listItem != null) {
            // Set Category Name
            String categoryName = listItem.optString("name", "");
            if (!AppUtil.isStringEmpty(categoryName)) {
                holder.getTextViewFilterCategoryName().setText(categoryName);
            }

            // Set Category Position
            try {
                listItem.put(PARAM_POSITION, position);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Get Filter Applied Count
            int appliedCount = getAppliedCount(listItem.optJSONArray("arr"));
            boolean isApplied = (appliedCount > 0);

            // Set Applied Flag
            try {
                listItem.put(PARAM_IS_APPLIED, isApplied);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Set Applied Indicator
            holder.getViewFilterSelectionIndicator().setVisibility((isApplied) ? View.VISIBLE : View.INVISIBLE);

            // Set Applied Count
            holder.getTextViewFilterOptionCount().setText((isApplied) ? Integer.toString(appliedCount) : "");

            // Set Selected
            holder.getRootView().setSelected(listItem.optBoolean(PARAM_IS_SELECTED, false));

            // Set Click Listener
            holder.getRootView().setOnClickListener(this);
            holder.getRootView().setTag(listItem);
        }
    }

    private int getAppliedCount(JSONArray filterOptionJsonArray) {
        int appliedCount = 0;

        if (filterOptionJsonArray != null && filterOptionJsonArray.length() > 0) {
            int filterOptionSize = filterOptionJsonArray.length();

            for (int index = 0; index < filterOptionSize; index++) {
                // Get Filter Option
                JSONObject filterOptionJsonObject = filterOptionJsonArray.optJSONObject(index);

                if (filterOptionJsonObject != null) {
                    // Check if Filter is applied
                    if (filterOptionJsonObject.optInt("applied", 0) == 1) {
                        ++appliedCount;
                    }
                }
            }
        }

        return appliedCount;
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.relativeLayout_filter_option_section) {
            handleCategoryClick((JSONObject) view.getTag());
        }
    }

    private void handleCategoryClick(JSONObject filterCategoryJsonObject) {
        if (filterCategoryClickListener != null) {
            filterCategoryClickListener.onFilterCategoryClick(filterCategoryJsonObject);
        }

        // De-select all Category
        deSelectCategory();

        // Set Category Selected
        setCategorySelected(filterCategoryJsonObject);

        // Notify Change
        notifyDataSetChanged();
    }

    private void deSelectCategory() {
        // Get JSON Array
        JSONArray categoryJsonArray = getJsonArray();

        if (categoryJsonArray != null) {
            int categorySize = categoryJsonArray.length();

            for (int index = 0; index < categorySize; index++) {
                // Get Category JSON
                JSONObject categoryJsonObject = categoryJsonArray.optJSONObject(index);

                if (categoryJsonObject != null) {
                    // Set isSelected false
                    try {
                        categoryJsonObject.put(PARAM_IS_SELECTED, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setCategorySelected(JSONObject filterCategoryJsonObject) {
        // Check for NULL
        if (filterCategoryJsonObject != null) {
            // Set isSelected true
            try {
                filterCategoryJsonObject.put(PARAM_IS_SELECTED, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface FilterCategoryClickListener {
        void onFilterCategoryClick(JSONObject filterCategoryJsonObject);
    }

    private class FilterCategoryViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private View viewFilterSelectionIndicator;
        private TextView textViewFilterCategoryName;
        private TextView textViewFilterOptionCount;

        public FilterCategoryViewHolder(View itemView) {
            super(itemView);

            viewFilterSelectionIndicator = itemView.findViewById(R.id.view_filter_selection_indicator);
            textViewFilterCategoryName = (TextView) itemView.findViewById(R.id.textView_filter_category_name);
            textViewFilterOptionCount = (TextView) itemView.findViewById(R.id.textView_filter_option_count);
            rootView = itemView.findViewById(R.id.relativeLayout_filter_option_section);
            rootView.setClickable(true);
        }

        public View getRootView() {
            return rootView;
        }

        public View getViewFilterSelectionIndicator() {
            return viewFilterSelectionIndicator;
        }

        public TextView getTextViewFilterCategoryName() {
            return textViewFilterCategoryName;
        }

        public TextView getTextViewFilterOptionCount() {
            return textViewFilterOptionCount;
        }
    }
}
