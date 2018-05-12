package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONObject;

public class PreSuggestedSearchRecyclerAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    private static final int ITEM_TYPE_PRE_SUGGEST = 1;

    private LayoutInflater layoutInflater;
    private PreSuggestClickListener preSuggestClickListener;

    public PreSuggestedSearchRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setPreSuggestClickListener(PreSuggestClickListener clickListener) {
        preSuggestClickListener = clickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_TYPE_PRE_SUGGEST;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_PRE_SUGGEST) {
            return new PreSuggestViewHolder(layoutInflater.inflate(R.layout.pre_suggested_search, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        handlePreSuggestView((PreSuggestViewHolder) holder, listItem, position);
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.textView_pre_suggest_label) {
            handlePreSuggestClick((JSONObject) view.getTag());
        }
    }

    private void handlePreSuggestClick(JSONObject listItemJsonObject) {
        if (preSuggestClickListener != null) {
            preSuggestClickListener.onPreSuggestClick(listItemJsonObject);
        }
    }

    private void handlePreSuggestView(PreSuggestViewHolder viewHolder, JSONObject listItem, int position) {
        if (viewHolder != null && listItem != null) {
            // Set Text
            String stString = listItem.optString("st");
            if (!AppUtil.isStringEmpty(stString)) {
                viewHolder.getTextViewLabel().setText(stString);

                try {
                    listItem.put("position", position);
                } catch (Exception ex) {

                }

                // Set Tag in View
                viewHolder.getTextViewLabel().setTag(listItem);

                // Set Click Listener
                viewHolder.getTextViewLabel().setOnClickListener(this);
            }
        }
    }

    public interface PreSuggestClickListener {
        void onPreSuggestClick(JSONObject listItemJsonObject);
    }

    private class PreSuggestViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewLabel;

        public PreSuggestViewHolder(View itemView) {
            super(itemView);

            textViewLabel = (TextView) itemView.findViewById(R.id.textView_pre_suggest_label);
            textViewLabel.getRootView().setClickable(true);
        }

        public TextView getTextViewLabel() {
            return textViewLabel;
        }
    }
}
