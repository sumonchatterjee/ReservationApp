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

public class UpcomingBookingRecyclerAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    private static final String PARAM_ACTION_SELECTED = "actionSelected";
    private final int UPCOMING_BOOKING_ITEM_TYPE = 1;
    private Context context;
    private LayoutInflater layoutInflater;
    private UpcomingBookingClickListener upcomingBookingClickListener;

    // Constructor
    public UpcomingBookingRecyclerAdapter(Context context) {
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

    public void setUpcomingBookingClickListener(UpcomingBookingClickListener upcomingBookingClickListener) {
        this.upcomingBookingClickListener = upcomingBookingClickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        return UPCOMING_BOOKING_ITEM_TYPE;
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return new UpcomingBookingViewHolder(layoutInflater.inflate(R.layout.upcoming_booking_card_layout, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        renderUpcomingBooking((UpcomingBookingViewHolder) holder, listItem);
    }

    private void renderUpcomingBooking(UpcomingBookingViewHolder viewHolder, final JSONObject jsonObject) {
        // Check for NULL
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
                                    Long.parseLong(jsonObject.optString("dining_dt_time_ts"))));

                    displayDateTime = displayDateTime.replace("#", "at");
                    displayDateTime = displayDateTime.replace("-", "\n");

                    viewHolder.getBookingTime().setText(displayDateTime);
                }
            }

            // Show Expected Booking Confirmation Section
            // Check if Booking is NEW
            if ("NW".equalsIgnoreCase(jsonObject.optString("booking_status")) &&
                    "upcoming".equalsIgnoreCase(jsonObject.optString("booking_type"))) {

                JSONObject expectedConfirmationJsonObject = jsonObject.optJSONObject("expected_confirmation_time");

                if (expectedConfirmationJsonObject != null) {
                    // Set Title 1
                    String title1 = expectedConfirmationJsonObject.optString("title_1");
                    if (!AppUtil.isStringEmpty(title1)) {
                        // Get second Message
                        String title2 = expectedConfirmationJsonObject.optString("title_2", "");
                        if (!AppUtil.isStringEmpty(title2)) {
                            title1 += "\n" + title2;
                        }

                        viewHolder.getTextViewBookingUserExpectation().setText(title1);
                        viewHolder.getTextViewBookingUserExpectation().setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.getTextViewBookingUserExpectation().setVisibility(View.GONE);
                    }
                } else {
                    viewHolder.getTextViewBookingUserExpectation().setVisibility(View.GONE);
                }
            } else {
                viewHolder.getTextViewBookingUserExpectation().setVisibility(View.GONE);
            }

            // Set Booking Status
            final int colorId = AppUtil.getBookingStatusColor(
                    jsonObject.optString("status"),
                    jsonObject.optString("booking_status"),
                    viewHolder.getBookingStatus());

            // Set Text Color
            viewHolder.getBookingStatus().setTextColor(context.getResources().getColor(colorId));

            // Handle Edit Booking
            if (("1").equalsIgnoreCase(jsonObject.optString("is_editable"))) {
                viewHolder.getEditBooking().setVisibility(View.VISIBLE);

                viewHolder.getEditBooking().setOnClickListener(this);
                viewHolder.getEditBooking().setTag(jsonObject);
            } else {
                viewHolder.getEditBooking().setVisibility(View.GONE);
            }

            // Handle Share Booking
            viewHolder.getShareDetailsUpcomingButton().setOnClickListener(this);
            viewHolder.getShareDetailsUpcomingButton().setTag(jsonObject);

            // Get CTA Data
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



                    // Set Click Listener
                    if (isEnabled) {
                        viewHolder.getRelativeLayoutBookingRightButton().setBackgroundResource(R.drawable.round_rectangle_primary_square_shape);
                        GradientDrawable leftShape = (GradientDrawable) viewHolder.getRelativeLayoutBookingRightButton().getBackground();
                        leftShape.setColor(Color.parseColor(btnBgColor));
                    } else {
                        viewHolder.getRelativeLayoutBookingRightButton().setBackgroundResource(R.drawable.round_rectangle_square_disabled_shape);
                        viewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(null);
                    }

                    // Set Button Drawables
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

        if (viewId == R.id.imageViewBookingEdit) {
            if (jsonObject.optString("b_type").equalsIgnoreCase("deal")) {
                upcomingBookingClickListener.onEditDealClick(jsonObject);
            } else {
                upcomingBookingClickListener.onEditBookingClick(jsonObject);
            }


        } else if (viewId == R.id.imageViewBookingShare) {
            upcomingBookingClickListener.onShareClick(jsonObject);

        } else if (viewId == R.id.booking_card_view) {
            upcomingBookingClickListener.onBookingCardClick(jsonObject);
        }
    }

    private void performCtaButtonAction(JSONObject jsonObject) {
        if (jsonObject != null) {
            int action = jsonObject.optInt(PARAM_ACTION_SELECTED);

            switch (action) {
                case AppConstant.CTA_RESERVE_AGAIN: // Reserve Now/Reserve Again
                    // Do Nothing... PAST BOOKING
                    break;

                case AppConstant.CTA_PAY_NOW: // Pay Now
                    upcomingBookingClickListener.onPayNowClick(jsonObject);
                    break;

                case AppConstant.CTA_PAID: // Paid
                    // Do Nothing...
                    break;

                case AppConstant.CTA_UPLOAD_BILL: // Upload Bill
                    upcomingBookingClickListener.onUploadBillClick(jsonObject);
                    break;

                case AppConstant.CTA_UPLOADED: // Uploaded
                    // Do Nothing...
                    break;

                case AppConstant.CTA_WRITE_REVIEW: // Write A Review
                    // Do Nothing... PAST BOOKING
                    break;

                case AppConstant.CTA_REVIEWED: // Reviewed
                    // Do Nothing... PAST BOOKING
                    break;

                case AppConstant.CTA_GET_DIRECTION: // Get Direction
                    upcomingBookingClickListener.onGetDirectionClick(jsonObject);
                    break;

                case AppConstant.CTA_REDEEM: // Get Direction
                    upcomingBookingClickListener.askForRedeemDealConfirmation(jsonObject);
                    break;
            }
        }
    }

    public interface UpcomingBookingClickListener {
        void onBookingCardClick(JSONObject jsonObject);

        void onGetDirectionClick(JSONObject jsonObject);

        void onEditBookingClick(JSONObject jsonObject);

        void onPayNowClick(JSONObject jsonObject);

        void onEditDealClick(JSONObject jsonObject);

        void onShareClick(JSONObject jsonObject);

        void onUploadBillClick(JSONObject jsonObject);

        void askForRedeemDealConfirmation(JSONObject jsonObject);
    }

    public static class UpcomingBookingViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayoutBookingDetailSection;
        private TextView mRestName;
        private TextView mDinerCount;
        private TextView mBookingTime;
        private TextView mBookingStatus;
        private ImageView imageViewEditButton;
        private ImageView imageViewSharingButton;
        private View relativeLayoutBookingLeftButton;
        private ImageView imageViewBookingLeftButton;
        private TextView textViewBookingLeftButton;
        private View relativeLayoutBookingRightButton;
        private ImageView imageViewBookingRightButton;
        private TextView textViewBookingRightButton;
        private TextView textViewBookingUserExpectation;
        private TextView relativeLayoutSubTitleBookingRightButton;
        private TextView relativeLayoutSubTitleBookingLeftButton;
        private View cardView;

        public UpcomingBookingViewHolder(View itemView) {
            super(itemView);

            linearLayoutBookingDetailSection = (LinearLayout) itemView.findViewById(R.id.linearLayout_upcoming_booking_section);
            linearLayoutBookingDetailSection.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.textView_upcoming_booking_header).setVisibility(View.GONE);

            imageViewEditButton = (ImageView) itemView.findViewById(R.id.imageViewBookingEdit);
            imageViewSharingButton = (ImageView) itemView.findViewById(R.id.imageViewBookingShare);
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
            textViewBookingUserExpectation = (TextView) itemView.findViewById(R.id.textView_booking_user_expectation);
            cardView = itemView.findViewById(R.id.booking_card_view);
            relativeLayoutSubTitleBookingRightButton = (TextView) itemView.findViewById(R.id.textViewBookingSubTitleRightButton);
            relativeLayoutSubTitleBookingLeftButton=(TextView) itemView.findViewById(R.id.textViewBookingSubTitleLeftButton);
        }

        public ImageView getEditBooking() {
            return imageViewEditButton;
        }

        public ImageView getShareDetailsUpcomingButton() {
            return imageViewSharingButton;
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

        public TextView getTextViewBookingUserExpectation() {
            return textViewBookingUserExpectation;
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
