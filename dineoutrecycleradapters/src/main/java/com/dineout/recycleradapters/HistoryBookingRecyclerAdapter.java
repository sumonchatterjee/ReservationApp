package com.dineout.recycleradapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.DatePickerUtil;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryBookingRecyclerAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    private static final String PARAM_ACTION_SELECTED = "actionSelected";
    private final int HISTORY_BOOKING_ITEM_TYPE = 1;
    private Context context;
    private LayoutInflater layoutInflater;
    private HistoryBookingClickListener historyBookingClickListener;

    // Constructor
    public HistoryBookingRecyclerAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    // Set Data
    public void setData(JSONArray jsonArray) {
        setJsonArray(jsonArray);
    }

    // Update Data
    public void updateData(JSONArray jsonArray) {
        updateJsonArray(jsonArray);
    }


    public void setHistoryBookingClickListener(HistoryBookingClickListener historyBookingClickListener) {
        this.historyBookingClickListener = historyBookingClickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        return HISTORY_BOOKING_ITEM_TYPE;
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return new HistoryBookingViewHolder(layoutInflater.inflate(R.layout.history_booking_card_layout, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        renderHistoryBooking((HistoryBookingViewHolder) holder, listItem);
    }

    private void renderHistoryBooking(HistoryBookingViewHolder viewHolder, final JSONObject jsonObject) {
        if (viewHolder != null && jsonObject != null) {
            // Set Restaurant Name
            String restaurantName = jsonObject.optString("title1", "");
            if (!AppUtil.isStringEmpty(restaurantName)) {
                viewHolder.getRestName().setText(restaurantName);
            }

            // Set Diner Count
            String dinerCount = jsonObject.optString("title2", "");
            if (!AppUtil.isStringEmpty(dinerCount)) {
                viewHolder.getDinerCount().setText(dinerCount);
            }

            // Set Booking Date and Time
            String bType = jsonObject.optString("b_type", "");
            if (AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bType)) {
                JSONArray eventJsonArray = jsonObject.optJSONArray("events");

                if (eventJsonArray != null && eventJsonArray.length() > 0) {
                    JSONObject eventJsonObject = eventJsonArray.optJSONObject(0);

                    if (eventJsonObject != null) {
                        String validity = eventJsonObject.optString("validity", "");
                        String time = eventJsonObject.optString("time", "");

                        if (!AppUtil.isStringEmpty(validity) && !AppUtil.isStringEmpty(time)) {
                            viewHolder.getBookingTime().setText(validity + "\n" + time);
                        }
                    }
                }
            } else {
                if (!AppUtil.isStringEmpty(jsonObject.optString("dining_dt_time_ts"))) {
                    DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();

                    // Get Display Date Time
                    String displayDateTime = datePickerUtil.getBookingDisplayDateTime(
                            com.dineout.recycleradapters.util.AppUtil.convertSecondsToMilliseconds(
                                    Long.parseLong(jsonObject.optString("dining_dt_time_ts", "0"))));

                    displayDateTime = displayDateTime.replace("#", "at");
                    displayDateTime = displayDateTime.replace("-", "\n");

                    viewHolder.getBookingTime().setText(displayDateTime);
                }
            }

            //Instantiate Drawable
            String status = "";

            //Check Booking Status Code
            String bookingStatus = jsonObject.optString("booking_status", "");
            if (!AppUtil.isStringEmpty(bookingStatus)) {
                status = ((bookingStatus.equalsIgnoreCase("NW")) ? "Expired" :
                        ((bookingStatus.equalsIgnoreCase("DN")) ? "Denied" : jsonObject.optString("status")));

                // Set Status Text
                viewHolder.getBookingStatus().setText(status);
            }

            int colorId = AppUtil.getColorResourceId(status);

            // Set Background Color
            viewHolder.getBookingStatus().setTextColor(context.getResources().getColor(colorId));

            // Set CTA
            JSONArray ctaJsonArray = jsonObject.optJSONArray("cta");
            if (ctaJsonArray != null && ctaJsonArray.length() > 0) {
                JSONObject leftButtonJsonObject = ctaJsonArray.optJSONObject(0);
                JSONObject rightButtonJsonObject = ctaJsonArray.optJSONObject(1);

                // LEFT BUTTON
                if (leftButtonJsonObject != null) {
                    // Set Title
                    String buttonTitle = leftButtonJsonObject.optString("button_text");
                    if (!AppUtil.isStringEmpty(buttonTitle)) {
                        viewHolder.getTextViewBookingLeftButton().setText(buttonTitle);
                    }


                    String buttonSubTitle = leftButtonJsonObject.optString("button_sub_text");
                    if(!AppUtil.isStringEmpty(buttonSubTitle)){
                        viewHolder.getTextViewLeftSubtitle().setText(buttonSubTitle);
                        viewHolder.getTextViewLeftSubtitle().setVisibility(View.VISIBLE);
                    }else{
                        viewHolder.getTextViewLeftSubtitle().setVisibility(View.GONE);
                    }



                    // Enable
                    String enable = leftButtonJsonObject.optString("enable");
                    final boolean isEnabled = ((!AppUtil.isStringEmpty(enable) && "1".equalsIgnoreCase(enable)));

                    // Action
                    String actionString = leftButtonJsonObject.optString("action");
                    final int action = ((AppUtil.isStringEmpty(actionString)) ? 0 : Integer.parseInt(actionString));


                    String btnBgColor = AppUtil.getCTABtnBg(context, action);

                    // Set Text Color
                    viewHolder.getTextViewBookingLeftButton().
                            setTextColor(context.getResources().getColor(AppUtil.getCtaButtonColor(isEnabled, true)));

                    if(isEnabled) {
                        viewHolder.getRelativeLayoutBookingLeftButton().setBackgroundResource(R.drawable.round_rectangle_primary_square_shape);
                        GradientDrawable leftShape = (GradientDrawable) viewHolder.getRelativeLayoutBookingLeftButton().getBackground();
                        leftShape.setColor(Color.parseColor(btnBgColor));
                    }else{
                        viewHolder.getRelativeLayoutBookingLeftButton().setBackgroundResource(R.drawable.round_rectangle_disabled_button_shape);
                        viewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(null);
                    }

                    // Set Button Drawable
                    /*viewHolder.getImageViewBookingLeftButton().
                            setImageResource(AppUtil.getCtaButtonDrawable(action, isEnabled, true));*/

                    // Get Message
                    final String message = leftButtonJsonObject.optString("msg");

                    // Set Click Listener
                    viewHolder.getRelativeLayoutBookingLeftButton().setTag(jsonObject);
                    viewHolder.getRelativeLayoutBookingLeftButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isEnabled) {
                                // Set Action Selected in JSONObject
                                try {
                                    jsonObject.putOpt(PARAM_ACTION_SELECTED, action);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                performCtaButtonAction(jsonObject);

                            } else {
                                if (!AppUtil.isStringEmpty(message)) {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

                    viewHolder.getRelativeLayoutBookingLeftButton().setVisibility(View.VISIBLE);

                } else {
                    viewHolder.getRelativeLayoutBookingLeftButton().setVisibility(View.GONE);
                }

                // RIGHT BUTTON
                if (rightButtonJsonObject != null) {
                    // Set Title
                    String buttonTitle = rightButtonJsonObject.optString("button_text");
                    if (!AppUtil.isStringEmpty(buttonTitle)) {
                        viewHolder.getTextViewBookingRightButton().setText(buttonTitle);
                    }

                    String buttonSubTitle = rightButtonJsonObject.optString("button_sub_text");
                    if(!AppUtil.isStringEmpty(buttonSubTitle)){
                        viewHolder.getTextViewRightSubtitle().setText(buttonSubTitle);
                        viewHolder.getTextViewRightSubtitle().setVisibility(View.VISIBLE);
                    }else{
                        viewHolder.getTextViewRightSubtitle().setVisibility(View.GONE);
                    }


                    // Enable
                    String enable = rightButtonJsonObject.optString("enable");
                    final boolean isEnabled = ((!AppUtil.isStringEmpty(enable) && "1".equalsIgnoreCase(enable)));

                    // Action
                    String actionString = rightButtonJsonObject.optString("action");
                    final int action = ((AppUtil.isStringEmpty(actionString)) ? 0 : Integer.parseInt(actionString));

                    String btnBgColor = AppUtil.getCTABtnBg(context, action);


                    // Set Text Color
                    viewHolder.getTextViewBookingRightButton().
                            setTextColor(context.getResources().getColor(AppUtil.getCtaButtonColor(isEnabled, true)));


                    if (isEnabled) {
                        viewHolder.getRelativeLayoutBookingRightButton().setBackgroundResource(R.drawable.round_rectangle_primary_square_shape);
                        GradientDrawable leftShape = (GradientDrawable) viewHolder.getRelativeLayoutBookingRightButton().getBackground();
                        leftShape.setColor(Color.parseColor(btnBgColor));
                    }else{
                        viewHolder.getRelativeLayoutBookingRightButton().setBackgroundResource(R.drawable.round_rectangle_square_disabled_shape);
                        viewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(null);
                    }

                    // Set Button Drawable
                    /*viewHolder.getImageViewBookingRightButton().
                            setImageResource(AppUtil.getCtaButtonDrawable(action, isEnabled, true));*/

                    // Get Message
                    final String message = rightButtonJsonObject.optString("msg");

                    // Set Click Listener
                    viewHolder.getRelativeLayoutBookingRightButton().setTag(jsonObject);
                    viewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isEnabled) {
                                // Set Action Selected in JSONObject
                                try {
                                    jsonObject.putOpt(PARAM_ACTION_SELECTED, action);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                performCtaButtonAction(jsonObject);

                            } else {
                                if (!AppUtil.isStringEmpty(message)) {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

                    viewHolder.getRelativeLayoutBookingRightButton().setVisibility(View.VISIBLE);

                } else {
                    viewHolder.getRelativeLayoutBookingRightButton().setVisibility(View.GONE);
                }
            } else {
                viewHolder.getRelativeLayoutBookingLeftButton().setVisibility(View.GONE);
                viewHolder.getRelativeLayoutBookingRightButton().setVisibility(View.GONE);
            }

            // Handle Card Click
            viewHolder.getCardView().setOnClickListener(this);
            viewHolder.getCardView().setTag(jsonObject);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        JSONObject jsonObject = (JSONObject) view.getTag();

        if (viewId == R.id.booking_card_view) {
            historyBookingClickListener.onBookingCardClick(jsonObject);
        }
    }

    private void performCtaButtonAction(JSONObject jsonObject) {
        if (jsonObject != null) {
            int action = jsonObject.optInt(PARAM_ACTION_SELECTED);

            switch (action) {
                case AppConstant.CTA_RESERVE_AGAIN: // Reserve Now/Reserve Again
                    historyBookingClickListener.onReserveAgainClick(jsonObject);
                    break;

                case AppConstant.CTA_PAY_NOW: // Pay Now
                    // Do Nothing... UPCOMING BOOKING
                    break;

                case AppConstant.CTA_PAID: // Paid
                    // Do Nothing...
                    break;

                case AppConstant.CTA_UPLOAD_BILL: // Upload Bill
                    historyBookingClickListener.onUploadBillClick(jsonObject);
                    break;

                case AppConstant.CTA_UPLOADED: // Uploaded
                    // Do Nothing...
                    break;

                case AppConstant.CTA_WRITE_REVIEW: // Write A Review
                    historyBookingClickListener.onWriteReviewClick(jsonObject);
                    break;

                case AppConstant.CTA_REVIEWED: // Reviewed
                    historyBookingClickListener.onWriteReviewClick(jsonObject);
                    break;

                case AppConstant.CTA_GET_DIRECTION: // Get Direction
                    // Do Nothing... UPCOMING BOOKING
                    break;
            }
        }
    }

    public interface HistoryBookingClickListener {
        void onBookingCardClick(JSONObject jsonObject);

        void onReserveAgainClick(JSONObject jsonObject);

        void onWriteReviewClick(JSONObject jsonObject);

        void onUploadBillClick(JSONObject jsonObject);
    }

    private class HistoryBookingViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayoutHistoryBookingSection;
        private TextView mRestName;
        private TextView mDinerCount;
        private TextView mBookingTime;
        private TextView mBookingStatus;
        private View relativeLayoutBookingLeftButton;
        private ImageView imageViewBookingLeftButton;
        private TextView textViewBookingLeftButton;
        private View relativeLayoutBookingRightButton;
        private ImageView imageViewBookingRightButton;
        private TextView textViewBookingRightButton;
        private TextView relativeLayoutSubTitleBookingRightButton;
        private TextView relativeLayoutSubTitleBookingLeftButton;
        private View cardView;

        public HistoryBookingViewHolder(View itemView) {
            super(itemView);

            linearLayoutHistoryBookingSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_history_booking_section);
            linearLayoutHistoryBookingSection.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.textView_history_booking_header).setVisibility(View.GONE);
            itemView.findViewById(R.id.imageViewBookingEdit).setVisibility(View.GONE);
            itemView.findViewById(R.id.imageViewBookingShare).setVisibility(View.GONE);
            itemView.findViewById(R.id.textView_booking_user_expectation).setVisibility(View.GONE);

            mRestName = (TextView) itemView.findViewById(R.id.textViewBookingRestaurantName);
            mDinerCount = (TextView) itemView.findViewById(R.id.textViewBookingDinnerCount);
            mBookingTime = (TextView) itemView.findViewById(R.id.textViewBookingDateTime);
            mBookingStatus = (TextView) itemView.findViewById(R.id.textView_booking_status);
            relativeLayoutBookingLeftButton = itemView.findViewById(R.id.relativeLayout_booking_left_button);
            imageViewBookingLeftButton = (ImageView) itemView.findViewById(R.id.imageView_booking_left_button);
            textViewBookingLeftButton = (TextView) itemView.findViewById(R.id.textViewBookingLeftButton);
            relativeLayoutBookingRightButton = itemView.findViewById(R.id.relativeLayout_booking_right_button);
            imageViewBookingRightButton = (ImageView) itemView.findViewById(R.id.imageView_booking_right_button);
            textViewBookingRightButton = (TextView) itemView.findViewById(R.id.textViewBookingRightButton);
            cardView = itemView.findViewById(R.id.booking_card_view);
            relativeLayoutSubTitleBookingRightButton = (TextView) itemView.findViewById(R.id.textViewBookingSubTitleRightButton);
            relativeLayoutSubTitleBookingLeftButton=(TextView) itemView.findViewById(R.id.textViewBookingSubTitleLeftButton);
        }

        public TextView getRestName() {
            return mRestName;
        }

        public TextView getDinerCount() {
            return mDinerCount;
        }

        public TextView getBookingTime() {
            return mBookingTime;
        }

        public TextView getBookingStatus() {
            return mBookingStatus;
        }

        public View getRelativeLayoutBookingLeftButton() {
            return relativeLayoutBookingLeftButton;
        }

        public ImageView getImageViewBookingLeftButton() {
            return imageViewBookingLeftButton;
        }

        public TextView getTextViewBookingLeftButton() {
            return textViewBookingLeftButton;
        }

        public View getRelativeLayoutBookingRightButton() {
            return relativeLayoutBookingRightButton;
        }

        public ImageView getImageViewBookingRightButton() {
            return imageViewBookingRightButton;
        }

        public TextView getTextViewBookingRightButton() {
            return textViewBookingRightButton;
        }

        public View getCardView() {
            return cardView;
        }

        public TextView getTextViewRightSubtitle() {
            return relativeLayoutSubTitleBookingRightButton;
        }

        public TextView getTextViewLeftSubtitle() {
            return relativeLayoutSubTitleBookingLeftButton;
        }
    }
}
