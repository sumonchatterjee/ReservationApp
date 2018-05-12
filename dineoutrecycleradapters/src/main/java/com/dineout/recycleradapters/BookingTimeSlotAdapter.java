package com.dineout.recycleradapters;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BookingTimeSlotAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private final int ITEM_TYPE_TIME_SLOT = 1;

    private Context context;
    private LayoutInflater layoutInflater;
    private TimeSlotClickListener timeSlotClickListener;
    private HashMap<Integer, String> legendMap;
    private Integer timeSlotSectionPos;

    public BookingTimeSlotAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setTimeSlotClickListener(TimeSlotClickListener timeSlotClickListener) {
        this.timeSlotClickListener = timeSlotClickListener;
    }

    public void setLegendMap(HashMap<Integer, String> legendMap) {
        this.legendMap = legendMap;
    }

    public void setTimeSlotSectionPos(Integer timeSlotSectionPos) {
        this.timeSlotSectionPos = timeSlotSectionPos;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_TYPE_TIME_SLOT;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check for NULL / Empty
        if (viewType == ITEM_TYPE_TIME_SLOT) {
            return new TimeSlotViewHolder(layoutInflater.inflate(R.layout.time_slot, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item Type
        int itemType = holder.getItemViewType();

        // Check for Item Type
        if (itemType == ITEM_TYPE_TIME_SLOT) {
            showTimeSlot((TimeSlotViewHolder) holder, listItem);
        }
    }

    private void showTimeSlot(TimeSlotViewHolder viewHolder, JSONObject jsonObject) {
        // Check for NULL / Empty
        if (viewHolder != null && jsonObject != null) {
            // Set Time Name
            String time = jsonObject.optString("displayTime", "");
            if (!AppUtil.isStringEmpty(time)) {
                viewHolder.getTextViewTimeLabel().setText(time);
            }

            // Check if Selected
            boolean isSelected = jsonObject.optBoolean(AppConstant.IS_SELECTED, false);
            boolean isEnabled = true;

            viewHolder.getTextViewTimeLabel().setEnabled(isEnabled);
            viewHolder.getTextViewTimeLabel().setSelected(isSelected);

            viewHolder.getFrameLayoutTimeSlot().setEnabled(isEnabled);
            viewHolder.getFrameLayoutTimeSlot().setSelected(isSelected);

            // Set Time Slot Click Listener
            viewHolder.getFrameLayoutTimeSlot().setTag(jsonObject);
            viewHolder.getFrameLayoutTimeSlot().setOnClickListener(this);

            // Set Offer Indicator
            int offer = jsonObject.optInt("offer", 0);
            if (offer >= 0 && offer <= 2) {
                String offerColor = legendMap.get(offer);
                offerColor = (AppUtil.isStringEmpty(offerColor) ? "#FFFFFF" : offerColor);

                // Set Legend Color
                if (timeSlotClickListener != null) {
                    ShapeDrawable ovalDrawable = timeSlotClickListener.getLegendDrawable(R.dimen.legend_size, offerColor);

                    // Check for NULL
                    if (ovalDrawable != null) {
                        TextView textViewLegendIndicator = viewHolder.getTextViewTimeIndicator();
                        textViewLegendIndicator.setBackground(ovalDrawable);
                    }
                }
            } else if (offer == 3) {
                viewHolder.getTextViewTimeIndicator().setBackgroundResource(R.drawable.white_circle);
                viewHolder.getTextViewTimeLabel().setEnabled(false);
                viewHolder.getTextViewTimeLabel().setSelected(false);
                viewHolder.getFrameLayoutTimeSlot().setBackgroundResource(R.drawable.round_white_rect_with_light_grey_border);
                viewHolder.getFrameLayoutTimeSlot().setSelected(false);

            } else { // Disable Time Slot
                viewHolder.getTextViewTimeIndicator().setBackgroundResource(R.drawable.white_circle);
            }
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.frameLayout_time_slot) {
            handleTimeSlotClick((JSONObject) view.getTag());
        }
    }

    private void handleTimeSlotClick(JSONObject timeSlotJsonObject) {
        // Check if Time Slot is disabled
        if (timeSlotJsonObject.optInt("offer", 0) == 3) {
            // Show Message
            if (timeSlotClickListener != null) {
                timeSlotClickListener.showDisabledTimeSlotMessage(timeSlotJsonObject.optString("msg", ""));
            }
        } else {
            // Set All Time Slots Close
            if (timeSlotClickListener != null) {
                timeSlotClickListener.setAllTimeSlotsClose();
            }

            // Mark clicked Time Slot as Selected
            try {
                timeSlotJsonObject.put(AppConstant.IS_SELECTED, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Notify Data Change
            notifyDataSetChanged();

            // Navigate User
            if (timeSlotClickListener != null) {
                try {
                    timeSlotJsonObject.putOpt("timeSlotSectionPos", timeSlotSectionPos);
                } catch (Exception e) {

                }
                timeSlotClickListener.onTimeSlotClick(timeSlotJsonObject);
            }
        }
    }

    public interface TimeSlotClickListener {
        void setAllTimeSlotsClose();

        void showDisabledTimeSlotMessage(String message);

        void onTimeSlotClick(JSONObject timeSlotJsonObject);

        ShapeDrawable getLegendDrawable(int legendSize, String legendColor);
    }

    private class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private View frameLayoutTimeSlot;
        private TextView textViewTimeLabel;
        private TextView textViewTimeIndicator;

        public TimeSlotViewHolder(View itemView) {
            super(itemView);

            frameLayoutTimeSlot = itemView.findViewById(R.id.frameLayout_time_slot);
            textViewTimeLabel = (TextView) itemView.findViewById(R.id.textView_time_label);
            textViewTimeIndicator = (TextView) itemView.findViewById(R.id.textView_time_indicator);
        }

        public View getFrameLayoutTimeSlot() {
            return frameLayoutTimeSlot;
        }

        public TextView getTextViewTimeLabel() {
            return textViewTimeLabel;
        }

        public TextView getTextViewTimeIndicator() {
            return textViewTimeIndicator;
        }
    }
}
