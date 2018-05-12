package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;

public class BookingDateSlotAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private final int ITEM_VIEW_DATE_SLOT = 1;
    private LayoutInflater layoutInflater;
    private DateSlotClickListener dateSlotClickListener;

    public BookingDateSlotAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setDateSlotClickListener(DateSlotClickListener dateSlotClickListener) {
        this.dateSlotClickListener = dateSlotClickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_VIEW_DATE_SLOT;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check for Item Type
        if (viewType == ITEM_VIEW_DATE_SLOT) {
            return new DateSlotViewHolder(layoutInflater.inflate(R.layout.date_slot, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item Type
        int itemType = holder.getItemViewType();

        if (itemType == ITEM_VIEW_DATE_SLOT) {
            renderDateSlot((DateSlotViewHolder) holder, listItem);
        }
    }

    private void renderDateSlot(DateSlotViewHolder viewHolder, JSONObject jsonObject) {
        // Check for NULL / Empty
        if (viewHolder != null && jsonObject != null) {
            // Set Day
            String day = jsonObject.optString(AppConstant.DAY, "");
            if (!AppUtil.isStringEmpty(day)) {
                viewHolder.getTextViewDay().setText(day);
            }

            // Set Date
            String date = jsonObject.optString(AppConstant.DATE, "");
            if (!AppUtil.isStringEmpty(date)) {
                viewHolder.getTextViewDate().setText(date);
            }

            // Set Availability Count
            String availabilityCount = jsonObject.optString(AppConstant.DATE_SLOT_TEXT, "");
            if (!AppUtil.isStringEmpty(availabilityCount)) {
                viewHolder.getTextViewAvailabilityCount().setText(availabilityCount);
                viewHolder.getTextViewAvailabilityCount().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTextViewAvailabilityCount().setVisibility(View.INVISIBLE);
            }

            // Check if Selected
            boolean isSelected = jsonObject.optBoolean(AppConstant.IS_SELECTED);
            boolean isEnabled = true;

            viewHolder.getTextViewDay().setEnabled(isEnabled);
            viewHolder.getTextViewDay().setSelected(isSelected);

            viewHolder.getTextViewDate().setEnabled(isEnabled);
            viewHolder.getTextViewDate().setSelected(isSelected);

            viewHolder.getTextViewAvailabilityCount().setEnabled(isEnabled);
            viewHolder.getTextViewAvailabilityCount().setSelected(isSelected);

            viewHolder.getViewSelectedBackground().setVisibility((isEnabled && isSelected) ? View.VISIBLE : View.GONE);

            viewHolder.getViewDateSlotNormal().setVisibility(isSelected ? View.GONE : View.VISIBLE);

            viewHolder.getViewDateSlot().setOnClickListener(this);
            viewHolder.getViewDateSlot().setClickable(isEnabled);
            viewHolder.getViewDateSlot().setTag(jsonObject);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.view_date_slot) {
            handleDateSlotClick((JSONObject) view.getTag());
        }
    }

    private void handleDateSlotClick(JSONObject dateSlotJsonObject) {
        // Check if already NOT selected
        if (dateSlotJsonObject != null && !dateSlotJsonObject.optBoolean(AppConstant.IS_SELECTED, false)) {
            // Handle Date Slot UI Change
            handleDateSlotUIChange(dateSlotJsonObject);

            // Get Date Slots based on Date Slot selected
            if (dateSlotClickListener != null) {
                dateSlotClickListener.onDateSlotClick(dateSlotJsonObject);
            }
        }
    }

    private void handleDateSlotUIChange(JSONObject dateSlotJsonObject) {
        // Refresh Date Slot Selected State
        DateTimeSlotUtil.refreshDateSlotSelectedState(getJsonArray());

        // Set Date Slot Selected
        try {
            dateSlotJsonObject.put(AppConstant.IS_SELECTED, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Notify Date Change
        notifyDataSetChanged();
    }

    public interface DateSlotClickListener {
        void onDateSlotClick(JSONObject dateSlotJsonObject);
    }

    private class DateSlotViewHolder extends RecyclerView.ViewHolder {
        private View viewSelectedBackground;
        private TextView textViewDay;
        private TextView textViewDate;
        private TextView textViewAvailabilityCount;
        private View viewDateSlotNormal;
        private View viewDateSlot;

        public DateSlotViewHolder(View itemView) {
            super(itemView);

            viewSelectedBackground = itemView.findViewById(R.id.view_selected_background);
            textViewDay = (TextView) itemView.findViewById(R.id.textView_day);
            textViewDate = (TextView) itemView.findViewById(R.id.textView_date);
            textViewAvailabilityCount = (TextView) itemView.findViewById(R.id.textView_availability_count);
            viewDateSlotNormal = itemView.findViewById(R.id.view_date_slot_normal);
            viewDateSlot = itemView.findViewById(R.id.view_date_slot);
        }

        public View getViewSelectedBackground() {
            return viewSelectedBackground;
        }

        public TextView getTextViewDay() {
            return textViewDay;
        }

        public TextView getTextViewDate() {
            return textViewDate;
        }

        public TextView getTextViewAvailabilityCount() {
            return textViewAvailabilityCount;
        }

        public View getViewDateSlotNormal() {
            return viewDateSlotNormal;
        }

        public View getViewDateSlot() {
            return viewDateSlot;
        }
    }
}
