package com.dineout.recycleradapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewBookingDetailsRecyclerAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    private static final int ITEM_TYPE_BOOKING_HEADER = 1;
    private static final int ITEM_TYPE_BOOKING_TIMELINE = 2;
    private static final int ITEM_TYPE_BOOKING_INFO = 3;
    private static final int ITEM_TYPE_EXTRA_INFO = 4;
    private static final int ITEM_TYPE_HOW_TO_USE = 5;
    private static final int ITEM_TYPE_PRIMARY_CTA = 6;
    private static final int ITEM_TYPE_SECONDARY_CTA = 7;
    private static final int ITEM_TYPE_TICKET_TITLE_INFO = 8;
    private static final int ITEM_TYPE_TICKET_INFO = 9;
    private Context mContext;
    private JSONObject jsonObjectSectionData;
    private ImageLoader imageLoader;
    private OnCTAClickedListener onCTAClickedListener;

    public NewBookingDetailsRecyclerAdapter(Context context, ImageLoader imageLoader) {
        mContext = context;
        this.imageLoader = imageLoader;
    }

    public void setBookingDetailObject(JSONArray arr, JSONObject object) {
        this.jsonObjectSectionData = object;
        setJsonArray(arr);
    }

    @Override
    protected int defineItemViewType(int position) {
        if (getJsonArray() == null) {
            return ITEM_VIEW_TYPE_EMPTY;
        }
        // Get JSON Object
        JSONObject jsonObject = getJsonArray().optJSONObject(position);

        // Get Section Type
        String sectionType = jsonObject.optString("section_type");

        if (!TextUtils.isEmpty(sectionType)) {

            if ("header".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_BOOKING_HEADER;

            } else if ("timeline".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_BOOKING_TIMELINE;

            } else if ("booking_info".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_BOOKING_INFO;

            } else if ("extra_info".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_EXTRA_INFO;

            } else if ("how_to_use".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_HOW_TO_USE;

            } else if ("primary_cta".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_PRIMARY_CTA;

            } else if ("secondary_cta".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_SECONDARY_CTA;

            } else if ("title_info".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_TICKET_TITLE_INFO;

            } else if ("ticket_info".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_TICKET_INFO;
            }
        }

        return ITEM_VIEW_TYPE_EMPTY;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_BOOKING_HEADER) {
            return new BookingHeaderViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_details_header, parent, false));

        } else if (viewType == ITEM_TYPE_BOOKING_TIMELINE) {
            return new ProgressIndicatorViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_detail_progress_indicator, parent, false));

        } else if (viewType == ITEM_TYPE_BOOKING_INFO) {
            return new BookingDetailsViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_detail_booking_section, parent, false));

        } else if (viewType == ITEM_TYPE_EXTRA_INFO) {
            return new ExtraInfoViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_detail_extra_info_layout, parent, false));

        } else if (viewType == ITEM_TYPE_HOW_TO_USE) {
            return new ProgressHowToUseViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_detail_how_to_use_layout, parent, false));

        } else if (viewType == ITEM_TYPE_PRIMARY_CTA) {
            return new BookingPrimaryCTAViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_detail_action, parent, false));

        } else if (viewType == ITEM_TYPE_SECONDARY_CTA) {
            return new BookingSecondaryCTAViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.booking_detail_cta, parent, false));

        } else if (viewType == ITEM_TYPE_TICKET_TITLE_INFO) {
            return new TicketTitleViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.ticket_title_layout, parent, false));

        } else if (viewType == ITEM_TYPE_TICKET_INFO) {
            return new TicketInfoViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.ticket_info_layout, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        String sectionKey = listItem.optString("section_key");

        if (holder == null) {
            return;
        }

        if (holder.getItemViewType() == ITEM_TYPE_BOOKING_HEADER) {
            inflateBookingHeaderTypeView(sectionKey, (BookingHeaderViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_BOOKING_TIMELINE) {
            inflateProgressIndicatorTypeView(sectionKey, (ProgressIndicatorViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_BOOKING_INFO) {
            inflateBookingDetailView(sectionKey, (BookingDetailsViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_EXTRA_INFO) {
            inflateExtraInfoView(sectionKey, (ExtraInfoViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_HOW_TO_USE) {
            inflateHowToUseView(sectionKey, (ProgressHowToUseViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_PRIMARY_CTA) {
            inflateBookingActionTypeView(sectionKey, (BookingPrimaryCTAViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_SECONDARY_CTA) {
            inflateBookingCTATypeView(sectionKey, (BookingSecondaryCTAViewHolder) holder, position);

        } else if (holder.getItemViewType() == ITEM_TYPE_TICKET_TITLE_INFO) {
            inflateTicketTitle(sectionKey, (TicketTitleViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_TICKET_INFO) {
            inflateTicketInfo(sectionKey, (TicketInfoViewHolder) holder, position);
        }
    }

    private void inflateTicketInfo(String sectionKey, TicketInfoViewHolder viewHolder, int position) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title"))) {
                viewHolder.getTicketInfoTitle().setText(jsonObjectSectionKeyData.optString("title"));
            }
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("price"))) {
                viewHolder.getTicketInfoSubtitle().setText(String.format(mContext.getResources().getString(R.string.container_rupee), jsonObjectSectionKeyData.optString("price")));
            }
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("quantity"))) {
                viewHolder.getTicketCount().setText("X " + jsonObjectSectionKeyData.optString("quantity"));
            }


            JSONObject obj = getJsonArray().optJSONObject(position + 1);
            if (obj != null) {
                if (!(obj.optString("section_type").equalsIgnoreCase("ticket_info"))) {
                    viewHolder.getDivider().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getDivider().setVisibility(View.GONE);
                }
            }

        }
    }


    private void inflateTicketTitle(String sectionKey, TicketTitleViewHolder viewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title"))) {
                viewHolder.getTicketTitleText().setText(jsonObjectSectionKeyData.optString("title"));
            }
        }
    }

    private void inflateBookingHeaderTypeView(String sectionKey, BookingHeaderViewHolder headerViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);

        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_1"))) {
                headerViewHolder.getTextViewTitle().setText(jsonObjectSectionKeyData.optString("title_1"));
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_2"))) {
                headerViewHolder.getTextViewLocation().
                        setText(jsonObjectSectionKeyData.optString("title_2"));
            }
        }
    }


    private void inflateProgressIndicatorTypeView(String sectionKey, ProgressIndicatorViewHolder indicatorViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        int currentIndex = 0;
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");


            if (jsonObjectSectionKeyData != null) {

                if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("progress_title"))) {
                    indicatorViewHolder.getTextViewTableStatus().
                            setText(jsonObjectSectionKeyData.optString("progress_title"));
                }

                String i = jsonObjectSectionKeyData.optString("current_index");
                if (!TextUtils.isEmpty(i)) {
                    currentIndex = Integer.parseInt(i);
                }

                JSONArray states = jsonObjectSectionKeyData.optJSONArray("progress_states");
                if (states != null && states.length() > 0) {


                    int total = states.length();
                    indicatorViewHolder.getTextDescriptionProgressLayout().removeAllViews();
                    Resources res = mContext.getResources();

                    indicatorViewHolder.getProgressLayout().removeAllViews();

                    for (int index = 0; index < total; index++) {

                        JSONObject currentIndexObject = states.optJSONObject(index);
                        int iconNum = currentIndexObject.optInt("icon");
                        if (index > 0) {
                            View lineView = LayoutInflater.from(mContext).
                                    inflate(com.dineout.recycleradapters.R.layout.line_layout, indicatorViewHolder.getProgressLayout(), false);
                            indicatorViewHolder.getProgressLayout().addView(lineView);
                            lineView.setBackgroundColor(index <= currentIndex ? res.getColor(R.color.colorPrimary) : res.getColor(R.color.dividerColor));
                        }
                        View roundImageView = LayoutInflater.from(mContext).
                                inflate(com.dineout.recycleradapters.R.layout.round_btn_layout, indicatorViewHolder.getProgressLayout(), false);

                        if (index <= currentIndex) {
                            roundImageView.setBackgroundResource(getButton(iconNum));
                        }
                        //roundImageView.setBackgroundColor(index<total? Color.RED:Color.GRAY);
                        else {
                            roundImageView.setBackgroundResource(getButton(99));
                        }
                        indicatorViewHolder.getProgressLayout().addView(roundImageView);
                        TextView progress_text;
                        if (index > 0 && index < total - 1) {
                            progress_text = (TextView) LayoutInflater.from(mContext).
                                    inflate(R.layout.progress_text_description_layout, indicatorViewHolder
                                            .getTextDescriptionProgressLayout(), false);
                        } else if (index == 0) {
                            progress_text = (TextView) LayoutInflater.from(mContext).
                                    inflate(R.layout.progress_text_description_layout_left, indicatorViewHolder
                                            .getTextDescriptionProgressLayout(), false);
                        } else {
                            progress_text = (TextView) LayoutInflater.from(mContext).
                                    inflate(R.layout.progress_text_description_layout_right, indicatorViewHolder
                                            .getTextDescriptionProgressLayout(), false);
                        }

//                        LinearLayout.LayoutParams layoutParams=indicatorViewHolder.getParentLayoutParams();
//                        if(index==0) {
//                            layoutParams.gravity = Gravity.LEFT ;
//                        }
//                        else if(index ==total-1){
//                            layoutParams.gravity = Gravity.RIGHT ;
//                        }
//                        else {
//                            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
//                        }
                        if (currentIndexObject != null) {
                            //progress_text.setLayoutParams(layoutParams);
                            progress_text.setText(currentIndexObject.optString("title"));

                        }
                        indicatorViewHolder.getTextDescriptionProgressLayout().addView(progress_text);


                    }


                }
            }
        }
    }


    private void inflateBookingDetailView(String sectionKey, BookingDetailsViewHolder bookingTypeViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);

        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (jsonObjectSectionKeyData != null) {
                String dinerName = jsonObjectSectionKeyData.optString("diner_name");
                bookingTypeViewHolder.getGuestName().setText(dinerName);
//                long dining_date_time= AppUtil.convertSecondsToMilliseconds(jsonObjectSectionKeyData
//                        .optLong("dining_dt_time_ts",0L));
//
//                String date= AppUtil.getDisplayFormatDateBookingDetails(Long.toString(dining_date_time));
                String date = jsonObjectSectionKeyData.optString("dining_date");
                bookingTypeViewHolder.getBookingDate().setText(date);

                String bookingID = jsonObjectSectionKeyData.optString("disp_id");
                bookingTypeViewHolder.getBookingId().setText(Html.fromHtml("ID: " + "<b>" + bookingID + "</b>"));

                String maleGuestCount = jsonObjectSectionKeyData.optString("cnt_covers_males");
                String femaleGuestCount = jsonObjectSectionKeyData.optString("cnt_covers_females");
                int genderknown = jsonObjectSectionKeyData.optInt("gender_known");
                String guestCountText = "";

                if (genderknown == 0) {
                    guestCountText = maleGuestCount;

                } else {
                    if (maleGuestCount.equalsIgnoreCase("0")) {
                        guestCountText = femaleGuestCount + " F";

                    } else if (femaleGuestCount.equalsIgnoreCase("0")) {
                        guestCountText = maleGuestCount + " M";
                    } else {
                        guestCountText = maleGuestCount + " M / " + femaleGuestCount + " F";
                    }
                }


                bookingTypeViewHolder.getGuestsCount().setText(guestCountText);

                //String timeOfBooking=AppUtil.getTimeForBookingDetails(Long.toString(dining_date_time));
                String timeOfBooking = jsonObjectSectionKeyData.optString("dining_time");
                bookingTypeViewHolder.getBookingTime().setText(timeOfBooking);

            }

        }
    }


    private void inflateExtraInfoView(String sectionKey, ExtraInfoViewHolder extraInfoViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title"))) {
                extraInfoViewHolder.getRulesHeader().setText(jsonObjectSectionKeyData.optString("title"));
                extraInfoViewHolder.getRulesHeader().setVisibility(View.VISIBLE);
            } else {
                extraInfoViewHolder.getRulesHeader().setVisibility(View.GONE);
            }

            JSONArray listArr = jsonObjectSectionKeyData.optJSONArray("list");
            extraInfoViewHolder.getRulesLayout().removeAllViews();

            if (listArr != null && listArr.length() > 0) {
                int totalLength = listArr.length();

                for (int index = 0; index < totalLength; index++) {
                    try {
                        String rules = listArr.get(index).toString();

                        View rulesView = LayoutInflater.from(mContext).
                                inflate(R.layout.rules_text_with_numbering_layout, extraInfoViewHolder.getRulesLayout(), false);

                        TextView rulesTxt = (TextView) rulesView.findViewById(R.id.rulestxt);
                        rulesTxt.setText(rules);

                        extraInfoViewHolder.getRulesLayout().addView(rulesView);


                    } catch (Exception ex) {

                    }
                }

            }
        }

    }


    private void inflateHowToUseView(String sectionKey, final ProgressHowToUseViewHolder progressRulesViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);
        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title"))) {
                progressRulesViewHolder.getHowToUseHeader().setText(jsonObjectSectionKeyData.optString("title"));

                Drawable myDrawable = ContextCompat.getDrawable(mContext, R.drawable.smartpay_corners_round_shape);
                myDrawable.setColorFilter(Color.parseColor(jsonObjectSectionKeyData.optString("color")), PorterDuff.Mode.SRC_IN);
                progressRulesViewHolder.getHowToUseHeader().setBackground(myDrawable);


                progressRulesViewHolder.getHowToUseHeader().setVisibility(View.VISIBLE);
            } else {
                progressRulesViewHolder.getHowToUseHeader().setVisibility(View.GONE);
            }

            final JSONArray stepsArr = jsonObjectSectionKeyData.optJSONArray("steps");
            progressRulesViewHolder.getHowToUseLayout().removeAllViews();


            if (stepsArr != null && stepsArr.length() > 0) {
                final int totalLength = stepsArr.length();

                try {
                    for (int index = 0; index < totalLength; index++) {

                        View howToUseView = LayoutInflater.from(mContext).
                                inflate(R.layout.circel_layout_with_text, progressRulesViewHolder
                                        .getHowToUseLayout(), false);


                        TextView noTxtvw = (TextView) howToUseView.findViewById(R.id.number_txt);
                        int no = index + 1;
                        noTxtvw.setText(no + "");

                        String howToUse = stepsArr.get(index).toString();
                        TextView descriptionTxtvw = (TextView) howToUseView.findViewById(R.id.description_txt);
                        descriptionTxtvw.setText(howToUse);


                        RelativeLayout lineView = (RelativeLayout) howToUseView.findViewById(R.id.vertical_line_layout);


                        if (index < totalLength - 1) {
                            lineView.setVisibility(View.VISIBLE);

                        } else {
                            lineView.setVisibility(View.GONE);
                        }

                        progressRulesViewHolder.getHowToUseLayout().addView(howToUseView);

                    }
                } catch (Exception ex) {

                }


            }
        }

    }


    private void inflateBookingActionTypeView(String sectionKey, BookingPrimaryCTAViewHolder actionViewHolder) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);

        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title"))) {
                actionViewHolder.getPrimaryCTADescription().setText(jsonObjectSectionKeyData.optString("title"));
                actionViewHolder.getPrimaryCTADescription().setVisibility(View.VISIBLE);
            } else {
                actionViewHolder.getPrimaryCTADescription().setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("button_title"))) {
                actionViewHolder.getPrimaryCTAName().setText(jsonObjectSectionKeyData.optString("button_title"));
                actionViewHolder.getPrimaryCTADescription().setVisibility(View.VISIBLE);
            } else {
                actionViewHolder.getPrimaryCTADescription().setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("button_sub_text"))) {
                actionViewHolder.getPrimaryCTASubTitle().setText(jsonObjectSectionKeyData.optString("button_sub_text"));
                actionViewHolder.getPrimaryCTASubTitle().setVisibility(View.VISIBLE);
            } else {
                actionViewHolder.getPrimaryCTASubTitle().setVisibility(View.GONE);
            }
            int action = jsonObjectSectionKeyData.optInt("cta");
            String btnBgColor = AppUtil.getCTABtnBg(mContext, action);

            int isActive = jsonObjectSectionKeyData.optInt("active");

            if (isActive == 1) {
                actionViewHolder.getPrimaryCTA().setBackgroundResource(R.drawable.fragment_restaurant_detail_bottom_button_drawable);
                GradientDrawable leftShape = (GradientDrawable) actionViewHolder.getPrimaryCTA().getBackground();
                leftShape.setColor(Color.parseColor(btnBgColor));
            } else {

                actionViewHolder.getPrimaryCTA().setBackgroundResource(R.drawable.fragment_restaurant_detail_bottom_button_drawable);
                GradientDrawable leftShape = (GradientDrawable) actionViewHolder.getPrimaryCTA().getBackground();
                leftShape.setColor(Color.parseColor("#CECECE"));

            }
            actionViewHolder.getPrimaryCTA().setEnabled(true);
            actionViewHolder.getPrimaryCTA().setOnClickListener(this);
            actionViewHolder.getPrimaryCTA().setTag(jsonObjectSectionKeyData);

        }
    }


    private void inflateBookingCTATypeView(String sectionKey, BookingSecondaryCTAViewHolder viewHolder, int position) {
        JSONObject jsonObjectSectionKeyDetails = getSectionDataDetails(sectionKey);


        if (jsonObjectSectionKeyDetails != null) {
            JSONObject jsonObjectSectionKeyData = jsonObjectSectionKeyDetails.optJSONObject("data");
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_1"))) {
                viewHolder.getTitleTxt().setText(jsonObjectSectionKeyData.optString("title_1"));
            }

            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("title_2"))) {
                viewHolder.getSubTitleTxt().setVisibility(View.VISIBLE);
                viewHolder.getSubTitleTxt().setText(jsonObjectSectionKeyData.optString("title_2"));
            } else {
                viewHolder.getSubTitleTxt().setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(jsonObjectSectionKeyData.optString("icon"))) {
                viewHolder.getIcon().setVisibility(View.VISIBLE);

                viewHolder.getIcon().setImageUrl(jsonObjectSectionKeyData.optString("icon"), imageLoader);
            } else {
                viewHolder.getIcon().setVisibility(View.GONE);
            }


            View baseLayout = viewHolder.getMainLayout();
            baseLayout.setTag(jsonObjectSectionKeyData);
            baseLayout.setOnClickListener(this);


        }
    }


    private JSONObject getSectionDataDetails(String sectionKey) {
        // Check on Section Key
        if (!TextUtils.isEmpty(sectionKey) && jsonObjectSectionData != null) {
            return jsonObjectSectionData.optJSONObject(sectionKey);
        } else {
            return null;
        }
    }

    private int getButton(int number) {
        int dRes = R.drawable.grey_indicator;
        switch (number) {
            case 0:
                dRes = R.drawable.orange_indicator;
                break;
            case 1:
                dRes = R.drawable.tick_indicator;
                break;
            case 2:
                dRes = R.drawable.cancelled_indicator;
                break;
            case 3:
                dRes = R.drawable.waiting_indicator;
                break;
            default:
                dRes = R.drawable.grey_indicator;
                break;


        }
        return dRes;
    }

    @Override
    public void onClick(View v) {
        JSONObject tagObj = (JSONObject) v.getTag();
        if (getOnCTAClickedListener() != null) {
            getOnCTAClickedListener().onCTAClicked(tagObj);
        }
//          if(v.getTag()== AppConstant.CTA_RESTAURANT_DETAIL){
//
//          }
    }

    public OnCTAClickedListener getOnCTAClickedListener() {
        return onCTAClickedListener;
    }

    public void setOnCTAClickedListener(OnCTAClickedListener onCTAClickedListener) {
        this.onCTAClickedListener = onCTAClickedListener;
    }


    public interface OnCTAClickedListener {
        void onCTAClicked(JSONObject jsonObject);
    }

    private class BookingHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewLocation;

        public BookingHeaderViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.restaurant_name_tv);
            textViewLocation = (TextView) itemView.findViewById(R.id.location_tv);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public TextView getTextViewLocation() {
            return textViewLocation;
        }
    }

    private class ProgressIndicatorViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout progressLayout;
        private TextView textViewTableStatus;
        private LinearLayout textDescriptionProgressLayout;

        public ProgressIndicatorViewHolder(View itemView) {
            super(itemView);
            progressLayout = (LinearLayout) itemView.findViewById(R.id.progress_indicator_layout);
            textViewTableStatus = (TextView) itemView.findViewById(R.id.table_status_tv);
            textDescriptionProgressLayout = (LinearLayout) itemView.findViewById(R.id.progress_description_layout);
        }

        public LinearLayout getProgressLayout() {
            return progressLayout;
        }

        public TextView getTextViewTableStatus() {
            return textViewTableStatus;
        }

        public LinearLayout getTextDescriptionProgressLayout() {
            return textDescriptionProgressLayout;
        }

        public LinearLayout.LayoutParams getParentLayoutParams() {
            return (LinearLayout.LayoutParams) getTextDescriptionProgressLayout().getLayoutParams();
        }
    }

    private class BookingDetailsViewHolder extends RecyclerView.ViewHolder {


        private TextView guestName;
        private TextView bookingDate;
        private TextView bookingTime;
        private TextView bookingIDTextView;
        private TextView guestCountTextView;

        public BookingDetailsViewHolder(View itemView) {
            super(itemView);
            guestName = (TextView) itemView.findViewById(R.id.guest_name_textView);
            bookingDate = (TextView) itemView.findViewById(R.id.booking_date_textView);
            bookingTime = (TextView) itemView.findViewById(R.id.booking_time_textView);
            bookingIDTextView = (TextView) itemView.findViewById(R.id.booking_id_textView);
            guestCountTextView = (TextView) itemView.findViewById(R.id.guest_count_text);

        }

        public TextView getGuestName() {
            return guestName;
        }

        public TextView getBookingDate() {
            return bookingDate;
        }

        public TextView getBookingTime() {
            return bookingTime;
        }

        public TextView getBookingId() {
            return bookingIDTextView;
        }

        public TextView getGuestsCount() {
            return guestCountTextView;
        }

    }


    private class ExtraInfoViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout rulesLayout;
        private TextView rulesHeader;



        public ExtraInfoViewHolder(View itemView) {
            super(itemView);
            rulesLayout = (LinearLayout) itemView.findViewById(R.id.rules_layout);
            rulesHeader = (TextView) itemView.findViewById(R.id.rules_header);

        }

        public LinearLayout getRulesLayout() {
            return rulesLayout;
        }

        public TextView getRulesHeader() {
            return rulesHeader;
        }

    }


    private class ProgressHowToUseViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout howToUseLayout;
        private TextView howToUseHeader;

        public ProgressHowToUseViewHolder(View itemView) {
            super(itemView);
            howToUseLayout = (LinearLayout) itemView.findViewById(R.id.rules_indicator_layout);
            howToUseHeader = (TextView) itemView.findViewById(R.id.textView_smartPay);

        }

        public LinearLayout getHowToUseLayout() {
            return howToUseLayout;
        }

        public TextView getHowToUseHeader() {
            return howToUseHeader;
        }

    }


    private class BookingPrimaryCTAViewHolder extends RecyclerView.ViewHolder {
        private TextView primary_cta_description;
        private RelativeLayout primary_cta;
        private TextView ctaTitle;
        private TextView ctaSubTitle;

        public BookingPrimaryCTAViewHolder(View itemView) {
            super(itemView);
            primary_cta_description = (TextView) itemView.findViewById(R.id.primary_cta_description);
            primary_cta = (RelativeLayout) itemView.findViewById(R.id.primary_cta);
            ctaTitle = (TextView) itemView.findViewById(R.id.cta_name);
            ctaSubTitle = (TextView) itemView.findViewById(R.id.cta_sub_title);
        }


        public TextView getPrimaryCTADescription() {
            return primary_cta_description;
        }

        public RelativeLayout getPrimaryCTA() {
            return primary_cta;
        }

        public TextView getPrimaryCTAName() {
            return ctaTitle;
        }

        public TextView getPrimaryCTASubTitle() {
            return ctaSubTitle;
        }

    }

    private class BookingSecondaryCTAViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView subTitle;
        private NetworkImageView actionImgView;

        public BookingSecondaryCTAViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            subTitle = (TextView) itemView.findViewById(R.id.sub_title);
            actionImgView = (NetworkImageView) itemView.findViewById(R.id.secondary_cta_icon);
        }


        public View getMainLayout() {
            return itemView;
        }

        public TextView getTitleTxt() {
            return title;
        }

        public TextView getSubTitleTxt() {
            return subTitle;
        }

        public NetworkImageView getIcon() {
            return actionImgView;
        }
    }

    private class TicketTitleViewHolder extends RecyclerView.ViewHolder {
        private TextView ticketTitle;


        public TicketTitleViewHolder(View itemView) {
            super(itemView);
            ticketTitle = (TextView) itemView.findViewById(R.id.ticket_title);
        }


        public View getMainLayout() {
            return itemView;
        }

        public TextView getTicketTitleText() {
            return ticketTitle;
        }

    }

    private class TicketInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView ticketInfoTitle;
        private TextView ticketInfoSubtitle;
        private TextView ticketCount;
        private View divider;


        public TicketInfoViewHolder(View itemView) {
            super(itemView);
            ticketInfoTitle = (TextView) itemView.findViewById(R.id.ticket_info_title);
            ticketInfoSubtitle = (TextView) itemView.findViewById(R.id.ticket_info_subtitle);
            ticketCount = (TextView) itemView.findViewById(R.id.ticket_count);
            divider = (View) itemView.findViewById(R.id.divider);
        }


        public View getMainLayout() {
            return itemView;
        }

        public TextView getTicketInfoTitle() {
            return ticketInfoTitle;
        }

        public TextView getTicketInfoSubtitle() {
            return ticketInfoSubtitle;
        }

        public TextView getTicketCount() {
            return ticketCount;
        }

        public View getDivider() {
            return divider;
        }

    }
}
