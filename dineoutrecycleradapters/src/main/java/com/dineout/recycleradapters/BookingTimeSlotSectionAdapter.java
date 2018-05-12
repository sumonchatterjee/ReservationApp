package com.dineout.recycleradapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;

import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.ImageRequestManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BookingTimeSlotSectionAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener, BookingTimeSlotAdapter.TimeSlotClickListener {

    private final int ITEM_TYPE_TIME_SLOT_SECTION = 1;

    private Context context;
    private LayoutInflater layoutInflater;
    private TimeSlotSectionClickListener timeSlotSectionClickListener;
    private HashMap<Integer, String> legendMap;

    public BookingTimeSlotSectionAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setTimeSlotSectionClickListener(TimeSlotSectionClickListener timeSlotSectionClickListener) {
        this.timeSlotSectionClickListener = timeSlotSectionClickListener;
    }

    public void setLegendMap(HashMap<Integer, String> legendMap) {
        this.legendMap = legendMap;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_TYPE_TIME_SLOT_SECTION;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check Item Type
        if (viewType == ITEM_TYPE_TIME_SLOT_SECTION) {
            return new TimeSlotSectionViewHolder(layoutInflater.inflate(R.layout.time_slot_section, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item Type
        int itemType = holder.getItemViewType();

        // Check for Item Type
        if (itemType == ITEM_TYPE_TIME_SLOT_SECTION) {
            showTimeSlotSection((TimeSlotSectionViewHolder) holder, listItem, position);
        }
    }

    private void showTimeSlotSection(TimeSlotSectionViewHolder viewHolder, JSONObject jsonObject, int position) {
        // Check for NULL / Empty
        if (viewHolder != null && jsonObject != null) {
            // Set Item Position
            try {
                jsonObject.put(AppConstant.POSITION, position);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Set Section Title
            String title = jsonObject.optString("title", "");
            if (!AppUtil.isStringEmpty(title)) {
                viewHolder.getTextViewLabel().setText(title);
            }

            // Set Left String (color change for deals also)
            String leftString = jsonObject.optString("subTitle", "");
            if (!AppUtil.isStringEmpty(leftString)) {
                viewHolder.getTextViewLeftString().setText(leftString);
                viewHolder.getTextViewLeftString().setVisibility(View.VISIBLE);
                viewHolder.getTextViewLeftString().setTextColor(Color.parseColor("#898989"));
            } else {
                viewHolder.getTextViewLeftString().setVisibility(View.GONE);
            }

             //Set sub text INFO
            String subText = jsonObject.optString("subText", "");
            if (!AppUtil.isStringEmpty(subText)) {
                viewHolder.getTextViewInfoMessage().setText(subText);
                viewHolder.getTextViewInfoMessage().setVisibility(View.VISIBLE);
            }else{
                viewHolder.getTextViewInfoMessage().setVisibility(View.GONE);
            }


             // set icon url
            String iconUrl=jsonObject.optString("iconUrl");
           if(!AppUtil.isStringEmpty(iconUrl)) {
               viewHolder.getWaitingIcon().setImageUrl(iconUrl,
                       ImageRequestManager.getInstance(context).getImageLoader());
               viewHolder.getWaitingIcon().setVisibility(View.VISIBLE);
           }else{
               viewHolder.getWaitingIcon().setVisibility(View.GONE);
            }

            // Set Drop Down Arrow Indicator
            int isOpen = jsonObject.optInt("isOpen", 0);
            viewHolder.getImageViewDrawerPointer().setSelected((isOpen == 1));

            // If Open then show Options
            if (isOpen == 1) {
                // Get Time Slots
                JSONArray timeSlotJsonArray = jsonObject.optJSONArray("data");

                if (timeSlotJsonArray == null || timeSlotJsonArray.length() == 0) {
                    // Show Time Slot Error Message
                    viewHolder.getTextViewErrorMessage().setVisibility(View.VISIBLE);

                    // Hide Time Slots
                    viewHolder.getRecyclerViewTimeSlot().setVisibility(View.GONE);

                    //Hide info section
                    viewHolder.getInfoSectionLayout().setVisibility(View.GONE);



                } else {
                    // Instantiate Adapter
                    BookingTimeSlotAdapter bookingTimeSlotAdapter = new BookingTimeSlotAdapter(context);
                    bookingTimeSlotAdapter.setTimeSlotClickListener(this);
                    bookingTimeSlotAdapter.setLegendMap(legendMap);

                    // set time slot section position
                    bookingTimeSlotAdapter.setTimeSlotSectionPos(position);

                    // Set Data in Adapter
                    bookingTimeSlotAdapter.setJsonArray(timeSlotJsonArray);

                    // Set Adapter
                    viewHolder.getRecyclerViewTimeSlot().setAdapter(bookingTimeSlotAdapter);

                    // Show Time Slots
                    viewHolder.getRecyclerViewTimeSlot().setVisibility(View.VISIBLE);

                    //Show info section
                    viewHolder.getInfoSectionLayout().setVisibility(View.VISIBLE);

                    // Hide Time Slot Error Message
                    viewHolder.getTextViewErrorMessage().setVisibility(View.GONE);


                    // Notify Data Change
                    bookingTimeSlotAdapter.notifyDataSetChanged();
                }
            } else {
                // Hide Time Slots
                viewHolder.getRecyclerViewTimeSlot().setVisibility(View.GONE);

                //Show info section
                viewHolder.getInfoSectionLayout().setVisibility(View.GONE);
            }

            // Set Time Slot Section Click Listener
            viewHolder.getRelativeLayoutTimeSlotSection().setTag(jsonObject);
            viewHolder.getRelativeLayoutTimeSlotSection().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.relativeLayout_time_slot_section) {
            handleTimeSlotSectionClick((JSONObject) view.getTag());
        }
    }

    private void handleTimeSlotSectionClick(JSONObject timeSlotSectionJsonObject) {
        // Check for NULL
        if (timeSlotSectionClickListener != null) {
            timeSlotSectionClickListener.trackExpandSection(timeSlotSectionJsonObject.optString("title", ""));
        }

        if(timeSlotSectionJsonObject!=null){
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(context);
            if(hMap!=null){
                hMap.put("slottypeValue",timeSlotSectionJsonObject.optString("title"));

            }

            AnalyticsHelper.getAnalyticsHelper(context).trackEventCountly("SlotTypeClick",hMap);
            AnalyticsHelper.getAnalyticsHelper(context).trackEventGA("B_SlotSelection","SlotTypeClick",timeSlotSectionJsonObject.optString("title"));

        }

        // Check for NULL / Empty
        if (timeSlotSectionJsonObject != null) {
            // Check if Section is already OPEN
            if (timeSlotSectionJsonObject.optInt("isOpen", 0) == 1) {
                // Close this Section
                try {
                    timeSlotSectionJsonObject.put("isOpen", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Notify Date Item Change
                notifyItemChanged(timeSlotSectionJsonObject.optInt(AppConstant.POSITION, 0));

            } else {
                // Set Open/Close of Time Slot Sections
                DateTimeSlotUtil.setTimeSlotSectionOpenClose(getJsonArray(), timeSlotSectionJsonObject);

                // Notify Data Change
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void setAllTimeSlotsClose() {
        DateTimeSlotUtil.setAllTimeSlotsClose(getJsonArray());
    }

    @Override
    public void showDisabledTimeSlotMessage(String message) {
        // Check for NULL
        if (timeSlotSectionClickListener != null) {
            timeSlotSectionClickListener.showDisabledTimeSlotMessage(message);
        }
    }

    @Override
    public void onTimeSlotClick(JSONObject timeSlotJsonObject) {
        // Check for NULL
        if (timeSlotSectionClickListener != null) {
            timeSlotSectionClickListener.onTimeSlotClick(timeSlotJsonObject);
        }
    }

    @Override
    public ShapeDrawable getLegendDrawable(int legendSize, String legendColor) {
        // Check for NULL
        if (timeSlotSectionClickListener != null) {
            return timeSlotSectionClickListener.getLegendDrawable(legendSize, legendColor);
        }

        return null;
    }

    public interface TimeSlotSectionClickListener {
        void showDisabledTimeSlotMessage(String message);

        void onTimeSlotClick(JSONObject timeSlotJsonObject);

        ShapeDrawable getLegendDrawable(int legendSize, String legendColor);

        void trackExpandSection(String label);
    }

    private class TimeSlotSectionViewHolder extends RecyclerView.ViewHolder {
        private View relativeLayoutTimeSlotSection;
        private TextView textViewLabel;
        private TextView textViewLeftString;
        private ImageView imageViewDrawerPointer;
        private TextView textViewErrorMessage;
        private RecyclerView recyclerViewTimeSlot;
        private View viewLabelLowerDivider;
        private RelativeLayout infoSection;
        private NetworkImageView waitingIcon;
        private TextView textViewInfo;

        public TimeSlotSectionViewHolder(View itemView) {
            super(itemView);

            relativeLayoutTimeSlotSection = itemView.findViewById(R.id.relativeLayout_time_slot_section);
            textViewLabel = (TextView) itemView.findViewById(R.id.textView_label);
            textViewLeftString = (TextView) itemView.findViewById(R.id.textView_sub_text);
            imageViewDrawerPointer = (ImageView) itemView.findViewById(R.id.imageView_drawer_pointer);
            textViewErrorMessage = (TextView) itemView.findViewById(R.id.textView_error_message);
            recyclerViewTimeSlot = (RecyclerView) itemView.findViewById(R.id.recyclerView_time_slot);
            viewLabelLowerDivider = itemView.findViewById(R.id.view_label_lower_divider);
            infoSection = (RelativeLayout)itemView.findViewById(R.id.restaurant_info_layout) ;
            waitingIcon= (NetworkImageView)itemView.findViewById(R.id.imageView_watch) ;
            textViewInfo= (TextView)itemView.findViewById(R.id.info_txt) ;

            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
            recyclerViewTimeSlot.setLayoutManager(gridLayoutManager);
        }

        public View getRelativeLayoutTimeSlotSection() {
            return relativeLayoutTimeSlotSection;
        }

        public TextView getTextViewLabel() {
            return textViewLabel;
        }

        public TextView getTextViewLeftString() {
            return textViewLeftString;
        }

        public ImageView getImageViewDrawerPointer() {
            return imageViewDrawerPointer;
        }

        public TextView getTextViewErrorMessage() {
            return textViewErrorMessage;
        }

        public RecyclerView getRecyclerViewTimeSlot() {
            return recyclerViewTimeSlot;
        }

        public View getViewLabelLowerDivider() {
            return viewLabelLowerDivider;
        }

        public RelativeLayout getInfoSectionLayout() {
            return infoSection;
        }

        public NetworkImageView getWaitingIcon() {
            return waitingIcon;
        }

        public TextView getTextViewInfoMessage() {
            return textViewInfo;
        }

    }
}
