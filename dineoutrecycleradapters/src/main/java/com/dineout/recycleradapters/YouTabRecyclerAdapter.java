package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class YouTabRecyclerAdapter extends BaseRecyclerAdapter {

    private final int ITEM_TYPE_OPTION = 1;

    private Context mContext;
    private YouTabItemClickHandler mItemClickHandler;

    // Constructor
    public YouTabRecyclerAdapter(Context context, YouTabItemClickHandler itemClickHandler) {
        // Set Context
        mContext = context;

        // Set Item Click Handler
        mItemClickHandler = itemClickHandler;

        // Set List
        setJsonArray(prepareJSONArray());
    }

    // Prepare JSONArray
    private JSONArray prepareJSONArray() {
        JSONArray jsonArray = new JSONArray();
        String[] optionTitles;
        String[] optionIcons;

        if (TextUtils.isEmpty(DOPreferences.getDinerEmail(mContext)) ||
                !(DOPreferences.isDinerDoPlusMember(mContext).equalsIgnoreCase("1"))) {
            optionTitles = mContext.getResources().getStringArray(R.array.you_items_title);
            optionIcons = mContext.getResources().getStringArray(R.array.you_items_icon);
        } else {
            optionTitles = mContext.getResources().getStringArray(R.array.you_items_title_dineout_plus);
            optionIcons = mContext.getResources().getStringArray(R.array.you_items_icon_dineoutplus);
        }


        String status;
        if(!TextUtils.isEmpty(DOPreferences.getDinerId(mContext))
                && !TextUtils.isEmpty((status = DOPreferences.getPhonePeStatus(mContext)))
                && status.equals("1")) {
            int titlesSize = optionTitles.length;
            String[] optionTitlesTemp = Arrays.copyOf(optionTitles, titlesSize + 1);
            String[] optionIconsTemp = Arrays.copyOf(optionIcons, titlesSize + 1);

            optionTitlesTemp[titlesSize]= DOPreferences.getPhonePeTitle(mContext);
            optionIconsTemp[titlesSize] = "phonepeyouicon";

            optionTitles = optionTitlesTemp;
            optionIcons = optionIconsTemp;
        }

        int titlesSize = optionTitles.length;
        for (int index = 0; index < titlesSize; index++) {
            // Instantiate JSON Object
            JSONObject jsonObject = new JSONObject();

            try {
                // Set Icon
                jsonObject.putOpt("item_icon_id", getImageResourceId(optionIcons[index]));

                // Set Name
                jsonObject.putOpt("item_name", optionTitles[index]);
            } catch (Exception e) {

            }

            // Add JSON Object to JSON Array
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_TYPE_OPTION;
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_OPTION) {
            return new YouItemOptionViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.you_list_view_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder.getItemViewType() == ITEM_TYPE_OPTION) {
            renderYouItem(((YouItemOptionViewHolder) holder), listItem);
        }
    }

    // Render You Item
    private void renderYouItem(YouItemOptionViewHolder viewHolder, final JSONObject youListItem) {
        // Set Icon
        viewHolder.getImageViewOptionIcon().setImageResource(youListItem.optInt("item_icon_id"));

        // Set Name
        viewHolder.getTextViewOptionName().setText(youListItem.optString("item_name"));

        // Set On Click Handler
        viewHolder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (youListItem.optString("item_name").equalsIgnoreCase("Talk to us")) {
                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_you), mContext.getString(R.string.ga_action_talk_us), null);
                }

                if (youListItem.optString("item_name").equalsIgnoreCase("smartPay")) {
                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_you), mContext.getString(R.string.ga_action_smart_pay), null);
                }
                mItemClickHandler.handleItemClick(youListItem.optInt("item_icon_id"));
            }
        });
    }

    // Get Image Resource Id
    private int getImageResourceId(String stringImage) {
        try {
            return mContext.getResources().
                    getIdentifier(stringImage, "drawable", mContext.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Item Click Handler
     */
    public interface YouTabItemClickHandler {
        /**
         * This function is used to handle Click events on List
         *
         * @param position Pass Resource Id of the Icon of List Item
         */
        void handleItemClick(int position);
    }

    /**
     * You Item Option View Holder
     */
    private class YouItemOptionViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewOptionIcon;
        private TextView textViewOptionName;
        private View rootView;

        public YouItemOptionViewHolder(View itemView) {
            super(itemView);

            imageViewOptionIcon = (ImageView) itemView.findViewById(R.id.you_lv_item_iv_icon);
            textViewOptionName = (TextView) itemView.findViewById(R.id.you_lv_item_tv_title);
            rootView = imageViewOptionIcon.getRootView();
        }

        public ImageView getImageViewOptionIcon() {
            return imageViewOptionIcon;
        }

        public TextView getTextViewOptionName() {
            return textViewOptionName;
        }

        public View getRootView() {
            return rootView;
        }
    }
}
