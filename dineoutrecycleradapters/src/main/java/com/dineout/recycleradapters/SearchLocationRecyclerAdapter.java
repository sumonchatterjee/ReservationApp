package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.example.dineoutnetworkmodule.AppConstant.JSON_KEY_IS_HEADER;

public class SearchLocationRecyclerAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    private static final int ITEM_TYPE_SEARCH_HEADER = 1;
    private static final int ITEM_TYPE_SEARCH_LIST_ITEM = 2;

    private LayoutInflater layoutInflater;
    private SearchLocationClickListener searchLocationClickListener;

    public SearchLocationRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setSearchLocationClickListener(SearchLocationClickListener searchLocationClickListener) {
        this.searchLocationClickListener = searchLocationClickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        // Get JSONArray
        JSONArray searchJsonArray = getJsonArray();

        if (searchJsonArray != null) {
            // Get Search JSON Object
            JSONObject searchJsonObject = searchJsonArray.optJSONObject(position);

            if (searchJsonObject != null) {
                // Check isHeader
                return ((searchJsonObject.optBoolean(JSON_KEY_IS_HEADER, false)) ?
                        ITEM_TYPE_SEARCH_HEADER : ITEM_TYPE_SEARCH_LIST_ITEM);
            }
        }

        return ITEM_VIEW_TYPE_EMPTY;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check View Type
        if (viewType == ITEM_TYPE_SEARCH_HEADER) {
            return new SearchLocationHeaderViewHolder(
                    layoutInflater.inflate(R.layout.search_location_header, parent, false));

        } else if (viewType == ITEM_TYPE_SEARCH_LIST_ITEM) {
            return new SearchLocationListItemViewHolder(
                    layoutInflater.inflate(R.layout.search_location_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get View Type
        int viewType = holder.getItemViewType();

        if (viewType == ITEM_TYPE_SEARCH_HEADER) {
            showSearchLocationHeaderSection((SearchLocationHeaderViewHolder) holder, listItem);

        } else if (viewType == ITEM_TYPE_SEARCH_LIST_ITEM) {
            if(listItem!=null){
                try {
                    listItem.put("position", position);
                }catch (Exception ex){

                }
            }
            showSearchLocationListItemSection((SearchLocationListItemViewHolder) holder, listItem);
        }
    }

    private void showSearchLocationHeaderSection(SearchLocationHeaderViewHolder viewHolder, JSONObject listItem) {
        if (viewHolder != null && listItem != null) {
            // Set Header Title
            String headerTitle = listItem.optString("title", "");
            if (!AppUtil.isStringEmpty(headerTitle)) {
                viewHolder.getTextViewSearchLocationHeader().setText(headerTitle);
            }
        }
    }

    private void showSearchLocationListItemSection(SearchLocationListItemViewHolder viewHolder, JSONObject listItem) {
        if (viewHolder != null && listItem != null) {
            // Set Search Location Name
            String locationName = listItem.optString("suggestion", "");
            if (!AppUtil.isStringEmpty(locationName)) {
                viewHolder.getTextViewSearchLocationName().setText(locationName);
                viewHolder.getTextViewSearchLocationName().setTag(listItem);
                viewHolder.getTextViewSearchLocationName().setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.textView_search_location_name) {
            handleSearchLocationClick((JSONObject) view.getTag());
        }
    }

    private void handleSearchLocationClick(JSONObject listItem) {
        if (searchLocationClickListener != null) {
            searchLocationClickListener.onSearchLocationItemClick(listItem);
        }
    }

    public interface SearchLocationClickListener {
        void onSearchLocationItemClick(JSONObject listItem);
    }

    private class SearchLocationHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSearchLocationHeader;

        public SearchLocationHeaderViewHolder(View itemView) {
            super(itemView);

            textViewSearchLocationHeader = (TextView) itemView.findViewById(R.id.textView_search_location_header);
            textViewSearchLocationHeader.setClickable(false);
        }

        public TextView getTextViewSearchLocationHeader() {
            return textViewSearchLocationHeader;
        }
    }

    private class SearchLocationListItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSearchLocationName;

        public SearchLocationListItemViewHolder(View itemView) {
            super(itemView);

            textViewSearchLocationName = (TextView) itemView.findViewById(R.id.textView_search_location_name);
            textViewSearchLocationName.setClickable(true);
        }

        public TextView getTextViewSearchLocationName() {
            return textViewSearchLocationName;
        }
    }
}
