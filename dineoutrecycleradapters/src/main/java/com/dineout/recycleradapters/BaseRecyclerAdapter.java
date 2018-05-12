package com.dineout.recycleradapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected final int ITEM_VIEW_TYPE_EMPTY = -1;

    private JSONArray jsonArray;
    private boolean hasFooter = false;
    private boolean hasHeader = false;

    // Default Constructor
    public BaseRecyclerAdapter() {

    }

    // Constructor
    public BaseRecyclerAdapter(JSONArray newJsonArray) {
        // Set List
        setJsonArray(newJsonArray);
    }

    public void setFooter(boolean hasFooter) {
        this.hasFooter = hasFooter;
    }

    public boolean listHasFooter() {
        return hasFooter;
    }

    public void setHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean listHasHeader() {
        return hasHeader;
    }

    @Override
    public int getItemCount() {
        if (listHasFooter()) {
            return (isJsonArrayEmpty() ? 0 : jsonArray.length() + 1);
        } else if (listHasHeader()) {
            return (isJsonArrayEmpty() ? 0 : jsonArray.length() + 1);
        } else {
            return (isJsonArrayEmpty() ? 0 : jsonArray.length());
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Check for Empty List
        if (isJsonArrayEmpty()) {
            return ITEM_VIEW_TYPE_EMPTY;
        } else {
            return defineItemViewType(position);
        }
    }

    public boolean isPositionFooter(int position) {
        return position == jsonArray.length();
    }

    public boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder returnValue;
        if (viewType == ITEM_VIEW_TYPE_EMPTY) {
            returnValue = null;
        } else {
            returnValue = defineViewHolder(parent, viewType);
        }

        if (returnValue == null) {
            returnValue = new InflationErrorViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_item_inflation_error_view, parent, false));
        }

        return returnValue;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            // Do Nothing...
        } else {
            JSONObject listItem=null;

                listItem = getListItem(position);


            if (listItem == null && isPositionFooter(position)) {
                renderListItem(holder, null, position);
            } else if (listItem == null && isPositionHeader(position)) {
                renderListItem(holder, null, position);
            } else if (listItem == null) {
                renderListItem(holder, null, position);
            } else {
               renderListItem(holder,listItem,position);
            }
        }
    }

    /**
     * This function returns the List
     */
    public JSONArray getJsonArray() {
        return jsonArray;
    }

    /**
     * This function initializes the List
     */
    public void setJsonArray(JSONArray newJsonArray) {
        jsonArray = newJsonArray;
    }

    /**
     * This function updates the list
     */
    public void updateJsonArray(JSONArray updateJsonArray) {
        // Check if list is NULL
        if (jsonArray == null) {
            jsonArray = updateJsonArray;

        } else { // Add updateList items to list
            // Get Updated List Size
            int updateListSize = updateJsonArray.length();
            for (int index = 0; index < updateListSize; index++) {
                jsonArray.put(updateJsonArray.opt(index));
            }
        }
    }

    /**
     * This function checks if List is Empty .i.e. NULL || size = 0
     */
    protected boolean isJsonArrayEmpty() {
        return (jsonArray == null || jsonArray.length() == 0);
    }

    /**
     * This function returns the list Item based on position
     */
    protected JSONObject getListItem(int position) {
        if (isJsonArrayEmpty() || position >= jsonArray.length()) {
            return null;
        }
        else {
            return (JSONObject) jsonArray.opt(position);

        }
    }


    /**
     * Define logic for View Type
     */
    protected abstract int defineItemViewType(int position);


    /**
     * Define logic for View Holder
     */
    protected abstract RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType);

    /**
     * Define logic for rendering List Item on Screen
     */
    protected abstract void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position);
}
