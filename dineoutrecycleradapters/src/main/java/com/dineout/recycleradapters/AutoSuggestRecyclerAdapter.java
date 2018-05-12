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

public class AutoSuggestRecyclerAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private static final int ITEM_TYPE_AUTO_SUGGEST_HEADER = 1;
    private static final int ITEM_TYPE_AUTO_SUGGEST_ITEM = 2;

    private LayoutInflater layoutInflater;
    private AutoSuggestClickListener autoSuggestClickListener;

    public AutoSuggestRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setAutoSuggestClickListener(AutoSuggestClickListener clickListener) {
        autoSuggestClickListener = clickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        JSONArray autoSuggestJsonArray = getJsonArray();

        if (autoSuggestJsonArray != null && autoSuggestJsonArray.length() > 0) {
            // Get Item
            JSONObject listItemJsonObject = autoSuggestJsonArray.optJSONObject(position);

            if (listItemJsonObject != null) {

                // Get Flag
                boolean isHeader = listItemJsonObject.optBoolean(JSON_KEY_IS_HEADER, false);

                return ((isHeader) ? ITEM_TYPE_AUTO_SUGGEST_HEADER : ITEM_TYPE_AUTO_SUGGEST_ITEM);
            }
        }

        return ITEM_VIEW_TYPE_EMPTY;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check for Item Type
        if (viewType == ITEM_TYPE_AUTO_SUGGEST_HEADER) {
            return new AutoSuggestHeaderViewHolder(layoutInflater.inflate(R.layout.search_auto_suggest_header, parent, false));

        } else if (viewType == ITEM_TYPE_AUTO_SUGGEST_ITEM) {
            return new AutoSuggestItemViewHolder(layoutInflater.inflate(R.layout.search_auto_suggest_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item Type
        int viewType = holder.getItemViewType();

        // Check for Item Type
        if (viewType == ITEM_TYPE_AUTO_SUGGEST_HEADER) {
            showAutoSuggestHeaderSection((AutoSuggestHeaderViewHolder) holder, listItem);

        } else if (viewType == ITEM_TYPE_AUTO_SUGGEST_ITEM) {
            showAutoSuggestItemSection((AutoSuggestItemViewHolder) holder, listItem,position);
        }
    }

    private void showAutoSuggestHeaderSection(AutoSuggestHeaderViewHolder viewHolder, JSONObject listItem) {
        if (viewHolder != null && listItem != null) {

            // Set Title
            String title = listItem.optString("title", "");
            if (!AppUtil.isStringEmpty(title)) {
                viewHolder.getTextViewAutoSuggestHeaderText().setText(title);
            }

            // Set Icon
            /*int resourceId = -1;
            if (title.equalsIgnoreCase(JSON_VALUE_AREA)) {
                resourceId = R.drawable.ic_tag_location;

            } else if (title.equalsIgnoreCase(JSON_VALUE_LOCALITY)) {
                resourceId = R.drawable.ic_tag_location;

            } else if (title.equalsIgnoreCase(JSON_VALUE_TAGS)) {
                resourceId = R.drawable.ic_tag_tags;

            } else if (title.equalsIgnoreCase(JSON_VALUE_CUISINES)) {
                resourceId = R.drawable.ic_tag_cuisine;

            } else if (title.equalsIgnoreCase(JSON_VALUE_RESTAURANT)) {
                resourceId = R.drawable.ic_tag_restaurant;
            }

            if (resourceId > -1) {
                viewHolder.getImageViewAutoSuggestHeaderIcon().setImageResource(resourceId);
                viewHolder.getImageViewAutoSuggestHeaderIcon().setVisibility(ImageView.VISIBLE);

            } else {
                viewHolder.getImageViewAutoSuggestHeaderIcon().setVisibility(ImageView.INVISIBLE);
            }*/
        }
    }

    private void showAutoSuggestItemSection(AutoSuggestItemViewHolder viewHolder, JSONObject listItem,final int position) {
        if (viewHolder != null && listItem != null) {

            // Set Suggestion
            String suggestion = listItem.optString("suggestion", "");
            if (!AppUtil.isStringEmpty(suggestion)) {
                viewHolder.getTextViewAutoSuggestItemText().setText(suggestion);
            }

            // Set Outlet Count
            String outletString = listItem.optString("total_rest_str", "");
            if (AppUtil.isStringEmpty(outletString)) {
                viewHolder.getTextViewAutoSuggestItemOutlet().setVisibility(TextView.GONE);

            } else {
                viewHolder.getTextViewAutoSuggestItemOutlet().setText(outletString);
                viewHolder.getTextViewAutoSuggestItemOutlet().setVisibility(TextView.VISIBLE);
            }

            // Set Detail
            if (!AppUtil.isStringEmpty(listItem.optString("tl_id", "")) &&
                    !AppUtil.isStringEmpty(listItem.optString("r_id", ""))) {

                // Get Profile Name
                String profileName = listItem.optString("profile_name", "");
                if (!AppUtil.isStringEmpty(profileName)) {
                    viewHolder.getTextViewAutoSuggestItemDetail().setText("@" + profileName);
                }

            } else {
                // Get Suggestion 2
                String suggestion2 = listItem.optString("suggestion_2", "");
                if (!AppUtil.isStringEmpty(suggestion2)) {
                    viewHolder.getTextViewAutoSuggestItemDetail().setText(suggestion2);
                    viewHolder.getTextViewAutoSuggestItemDetail().setVisibility(TextView.VISIBLE);

                } else {
                    viewHolder.getTextViewAutoSuggestItemDetail().setVisibility(TextView.GONE);
                }
            }

            // Set Click Listener on Root View
            try {
                listItem.put("position", position);
            }catch (Exception ex){
              //exception
            }
            viewHolder.getRootView().setTag(listItem);
            viewHolder.getRootView().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.relativeLayout_auto_suggest_root) {
            handleAutoSuggestItemClick((JSONObject) view.getTag());
        }
    }

    private void handleAutoSuggestItemClick(JSONObject autoSuggestItemJsonObject) {
        if (autoSuggestClickListener != null) {
            autoSuggestClickListener.onAutoSuggestClick(autoSuggestItemJsonObject);
        }
    }

    public interface AutoSuggestClickListener {
        void onAutoSuggestClick(JSONObject autoSuggestItemJsonObject);
    }

    private class AutoSuggestHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewAutoSuggestHeaderText;

        public AutoSuggestHeaderViewHolder(View itemView) {
            super(itemView);

            textViewAutoSuggestHeaderText = (TextView) itemView.findViewById(R.id.textView_auto_suggest_header_text);
            textViewAutoSuggestHeaderText.getRootView().setClickable(false);
        }

        public TextView getTextViewAutoSuggestHeaderText() {
            return textViewAutoSuggestHeaderText;
        }
    }

    private class AutoSuggestItemViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private TextView textViewAutoSuggestItemOutlet;
        private TextView textViewAutoSuggestItemText;
        private TextView textViewAutoSuggestItemDetail;

        public AutoSuggestItemViewHolder(View itemView) {
            super(itemView);

            textViewAutoSuggestItemOutlet = (TextView) itemView.findViewById(R.id.textView_auto_suggest_item_outlet);
            textViewAutoSuggestItemText = (TextView) itemView.findViewById(R.id.textView_auto_suggest_item_text);
            textViewAutoSuggestItemDetail = (TextView) itemView.findViewById(R.id.textView_auto_suggest_item_detail);
            rootView = itemView.findViewById(R.id.relativeLayout_auto_suggest_root);
            rootView.setClickable(true);
        }

        public View getRootView() {
            return rootView;
        }

        public TextView getTextViewAutoSuggestItemOutlet() {
            return textViewAutoSuggestItemOutlet;
        }

        public TextView getTextViewAutoSuggestItemText() {
            return textViewAutoSuggestItemText;
        }

        public TextView getTextViewAutoSuggestItemDetail() {
            return textViewAutoSuggestItemDetail;
        }
    }
}
