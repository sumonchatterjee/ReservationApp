package com.dineout.recycleradapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.util.NetworkUtil;
import com.analytics.utilities.DOLog;
import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.BookingDetailUtil;
import com.dineout.recycleradapters.util.DatePickerUtil;
import com.dineout.recycleradapters.util.ImageData;
import com.dineout.recycleradapters.util.JSONUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dineout.recycleradapters.util.JSONUtil.booleanValue;

public class BookingDetailRecyclerAdapter extends BaseRecyclerAdapter {

    public static final String VIEW_TYPE = "VIEW_TYPE";
    private static final int ITEM_TYPE_BOOKING_DETAIL_IMAGE = 1;
    private static final int ITEM_TYPE_BOOKING_DETAILS = 2;
    private static final int ITEM_TYPE_BOOKING_DO_SHARE = 3;
    private static final int ITEM_TYPE_BOOKING_DO_PLUS = 4;
    private static final int ITEM_TYPE_BOOKING_GET_DIRECTION = 5;
    private static final int ITEM_TYPE_BOOKING_DINNER_FOR_2 = 6;
    private static final int ITEM_TYPE_BOOKING_MEMBERSHIP_DETAIL = 7;
    private static final int ITEM_TYPE_REDEEM_DEAL = 8;
    public boolean hasRedeemedDeal;
    public int redeemDealItemIndex;
    public int getDirectionPaidItemIndex;
    DineoutNetworkManager networkManager;
    ImageLoader imageLoader;
    private Context mContext;
    private LayoutInflater inflater;
    private JSONObject mBookingDetailResponse;
    private JSONObject bookingDetailData;
    private JSONObject resAuthData;
    private View.OnClickListener mClickListener;
    private boolean isFavorite;

    public BookingDetailRecyclerAdapter(Context cxt, View.OnClickListener clickListener) {
        mContext = cxt;
        inflater = LayoutInflater.from(mContext);
        mClickListener = clickListener;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;

        for (int i = 0; i < getItemCount(); i++) {
            if (defineItemViewType(i) == ITEM_TYPE_BOOKING_DO_SHARE) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public boolean isHasRedeemedDeal() {
        return hasRedeemedDeal;
    }

    public void setHasRedeemedDeal(boolean hasRedeemedDeal) {
        this.hasRedeemedDeal = hasRedeemedDeal;
    }

    public int getRedeemDealItemIndex() {
        return redeemDealItemIndex;
    }

    public void setRedeemDealItemIndex(int redeemDealItemIndex) {
        this.redeemDealItemIndex = redeemDealItemIndex;
    }

    public int getGetDirectionPaidItemIndex() {
        return getDirectionPaidItemIndex;
    }

    public void setGetDirectionPaidItemIndex(int getDirectionPaidItemIndex) {
        this.getDirectionPaidItemIndex = getDirectionPaidItemIndex;
    }

    public JSONObject getDetailResponse() {
        return mBookingDetailResponse;
    }

    public void setResDetailData(JSONObject jsonObject) {
        mBookingDetailResponse = jsonObject;
        bookingDetailData = BookingDetailUtil.getData(getDetailResponse());
        resAuthData = BookingDetailUtil.getResAuth(getDetailResponse());
        isFavorite = JSONUtil.booleanValue(bookingDetailData, "is_fav");
        constructDataArray();
    }

    public JSONObject getBookingDetailData() {
        return bookingDetailData;
    }

    @Override
    public void setJsonArray(JSONArray newJsonArray) {
        super.setJsonArray(newJsonArray);
        notifyDataSetChanged();
    }

    private JSONObject getJsonObj(int type) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(VIEW_TYPE, type);
        return jsonObject;
    }

    public JSONObject getData(int position) {
        return bookingDetailData;
    }

    private void constructDataArray() {

        if (bookingDetailData == null) {
            setJsonArray(null);
        }

        try {

            JSONArray jsonArray = new JSONArray();

            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_DETAIL_IMAGE));
            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_DETAILS));
            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_DO_SHARE));
            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_DO_PLUS));
            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_GET_DIRECTION));
            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_DINNER_FOR_2));
            jsonArray.put(getJsonObj(ITEM_TYPE_BOOKING_MEMBERSHIP_DETAIL));
            jsonArray.put(getJsonObj(ITEM_TYPE_REDEEM_DEAL));

            setJsonArray(jsonArray);

        } catch (JSONException ex) {
            DOLog.d(ex.getMessage());
        }
    }


    public JSONObject getItem(int position) {
        return getJsonArray() != null && getJsonArray().length() > position ?
                getJsonArray().optJSONObject(position) : null;
    }


    @Override
    protected int defineItemViewType(int position) {
        JSONObject jsonObject = getItem(position);
        return jsonObject.optInt(VIEW_TYPE);
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case ITEM_TYPE_BOOKING_DETAIL_IMAGE: {
                return new ImagesVH(
                        inflater.inflate(R.layout.resturant_image_layout, parent, false));
            }
            case ITEM_TYPE_BOOKING_DETAILS: {
                return new DetailVH(
                        inflater.inflate(R.layout.resturant_name_layout, parent, false));
            }

            case ITEM_TYPE_BOOKING_DO_SHARE: {
                return new ShareVH(
                        inflater.inflate(R.layout.resturant_share_layout, parent, false));
            }

            case ITEM_TYPE_BOOKING_DO_PLUS: {
                return new DOPlusVH(
                        inflater.inflate(R.layout.do_plus_layout, parent, false));
            }

            case ITEM_TYPE_BOOKING_GET_DIRECTION: {
                return new BookingDetailCtaVH(inflater.inflate(R.layout.booking_detail_get_directions_layout, parent, false));
            }

            case ITEM_TYPE_BOOKING_DINNER_FOR_2: {
                // Check if booking is of Deal
                if (bookingDetailData != null &&
                        !AppUtil.isStringEmpty(bookingDetailData.optString("b_type"))) {

                    if (AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingDetailData.optString("b_type")) || AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bookingDetailData.optString("b_type"))) {
                        return new BookingDealsViewHolder(
                                inflater.inflate(R.layout.booking_deal_section_layout, parent, false));
                    } else {
                        return new DinnerFor2VH(
                                inflater.inflate(R.layout.booking_detail_dinner_layout, parent, false));
                    }
                }

                return null;
            }

            case ITEM_TYPE_BOOKING_MEMBERSHIP_DETAIL: {
                return new MembershipDetailVH(
                        inflater.inflate(R.layout.booking_membership_detail_layout, parent, false));
            }

            case ITEM_TYPE_REDEEM_DEAL: {
                return new RedeemDealViewHolder(
                        inflater.inflate(R.layout.button_list_item, parent, false));
            }

            default: {
                TextView textView = new TextView(mContext);
                textView.setText(mContext.getResources().getString(R.string.default_text));
                new RDBaseVH(textView);
            }
        }

        return null;
    }


    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        ((RDBaseVH) holder).bindView(position, getData(position));
    }

    public void setNetworkManager(DineoutNetworkManager networkManager, ImageLoader imageLoader) {
        this.networkManager = networkManager;
        this.imageLoader = imageLoader;
    }

    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    //extend all view holder of Resturant detail from this
    public class RDBaseVH extends RecyclerView.ViewHolder {

        public RDBaseVH(View itemView) {
            super(itemView);
        }

        public void bindView(int position, JSONObject data) {

        }
    }

    public class ImagesVH extends RDBaseVH {

        private NetworkImageView mImageView;
        private ImageView imageViewDetailDOPlusLogo;
        private TextView mRating;
        private TextView textViewRecencyTag;

        public ImagesVH(View itemView) {
            super(itemView);

            //find view here
            mImageView = (NetworkImageView) itemView.findViewById(R.id.iv_rest_image);
            imageViewDetailDOPlusLogo =
                    (ImageView) itemView.findViewById(R.id.imageViewDetailDOPlusLogo);
            mRating = (TextView) itemView.findViewById(R.id.tv_rest_details_rating);
            textViewRecencyTag = (TextView) itemView.findViewById(R.id.textView_recency_tag);
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            String recency = BookingDetailUtil.getRecency(data);

            if (AppUtil.showRecency(recency)) {
                // Show Recency
                textViewRecencyTag.setVisibility(View.VISIBLE);

                // Hide Rating
                mRating.setVisibility(TextView.GONE);

            } else {
                double rating = BookingDetailUtil.getAvgRating(data);
                if (AppUtil.hasNoRating(Double.toString(rating))) {
                    // Hide Rating
                    mRating.setVisibility(TextView.GONE);

                } else {
                    // Set Rating
                    AppUtil.setRatingValueBackground(Double.toString(rating), mRating);

                    // Show Rating
                    mRating.setVisibility(TextView.VISIBLE);
                }

                // Hide Recency
                textViewRecencyTag.setVisibility(View.GONE);
            }

            ArrayList<ImageData> profileImageData = BookingDetailUtil.getProfileImageData(data);
            String url = "";
            if (profileImageData != null && profileImageData.size() > 0) {
                url = profileImageData.get(0).getImage_url();
            }

            if (!TextUtils.isEmpty(url)) {
                mImageView.setImageUrl(NetworkUtil.appendSizeQuery(itemView.getContext(), url),
                        getImageLoader());
            } else {
                mImageView.setDefaultImageResId(R.drawable.default_list);
            }

            mImageView.setOnClickListener(mClickListener);
        }
    }

    public class DetailVH extends RDBaseVH {

        private ImageView carlsbargLogo;
        private TextView mResName;
        private TextView mRestAddress;
        private TextView mDistance;
        private View mNotifier;


        public DetailVH(View itemView) {
            super(itemView);
            mResName = (TextView) itemView.findViewById(R.id.tv_rd_resto_name);
            mRestAddress = (TextView) itemView.findViewById(R.id.tv_rd_address);
            mDistance = (TextView) itemView.findViewById(R.id.tv_rd_distance);
            carlsbargLogo = (ImageView) itemView.findViewById(R.id.iv_rd_carlsberg_logo);
            mNotifier = itemView.findViewById(R.id.textView_smartPay);
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            mNotifier.setVisibility(
                    BookingDetailUtil.isAcceptPayment(data) == 1 ? View.VISIBLE : View.GONE);
            mNotifier.setOnClickListener(mClickListener);

            mResName.setText(BookingDetailUtil.getResturantName(data));
            mRestAddress.setText(BookingDetailUtil.getResturantAddress(data));

            boolean isCarlsbergRestaurant = !TextUtils.isEmpty(bookingDetailData.optString("is_carlsberg")) &&
                    "1".equals(bookingDetailData.optString("is_carlsberg"));

            if (!isCarlsbergRestaurant) {
                carlsbargLogo.setVisibility(View.GONE);

            } else {
                carlsbargLogo.setVisibility(View.VISIBLE);
            }
            if (DOPreferences.isAutoMode(mContext) && BookingDetailUtil.getDistance(data) > 0) {
                mDistance.setVisibility(View.VISIBLE);
                mDistance.setText(String.format(mContext.getString(R.string.container_distance),
                        AppUtil.formatFloatDigits((float) BookingDetailUtil.getDistance(data), 2, 2)));
            } else {
                mDistance.setVisibility(View.GONE);
            }
        }
    }

    public class ShareVH extends RDBaseVH implements View.OnClickListener {

        private ImageView mFavouriteImage;

        public ShareVH(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.ll_rd_favs).setOnClickListener(this);
            itemView.findViewById(R.id.ll_rd_share_resto_details)
                    .setOnClickListener(mClickListener);
            mFavouriteImage = (ImageView) itemView.findViewById(R.id.iv_rd_favs);
            itemView.findViewById(R.id.ll_rd_map).setOnClickListener(mClickListener);
            itemView.findViewById(R.id.ll_rd_call).setOnClickListener(mClickListener);
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);
            setFavImage();
        }

        @Override
        public void onClick(View v) {
            isFavorite = !isFavorite;
            setFavImage();
            if (mClickListener != null) {
                mClickListener.onClick(v);
            }
        }

        private void setFavImage() {
            mFavouriteImage.setImageResource(
                    isFavorite ? R.drawable.ic_favorite_enable :
                            R.drawable.ic_favorite_disable);
        }
    }

    public class DOPlusVH extends RDBaseVH {
        private TextView mDinerName;
        private TextView mBookingId;
        private RelativeLayout doPlusUniqueCodeSectionLayout;
        private TextView uniqueCodetv;
        private TextView textViewBookingConfirmationTitle1;
        private TextView textViewBookingConfirmationTitle2;
        private View linearLayoutBookingConfirmationContainer;

        public DOPlusVH(View itemView) {
            super(itemView);
            mDinerName = (TextView) itemView.findViewById(R.id.tv_diner_name);
            mBookingId = (TextView) itemView.findViewById(R.id.tv_booking_id);
            doPlusUniqueCodeSectionLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutDOPlusUniqueCodeSection);
            uniqueCodetv = (TextView) itemView.findViewById(R.id.textViewBookingDetailDOPlusUniqueCode);
            textViewBookingConfirmationTitle1 = (TextView) itemView.findViewById(R.id.textView_booking_confirmation_title_1);
            textViewBookingConfirmationTitle2 = (TextView) itemView.findViewById(R.id.textView_booking_confirmation_title_2);
            linearLayoutBookingConfirmationContainer = itemView.findViewById(R.id.linearLayout_booking_confirmation_container);
        }

        public TextView getTextViewBookingConfirmationTitle2() {
            return textViewBookingConfirmationTitle2;
        }

        public TextView getTextViewBookingConfirmationTitle1() {
            return textViewBookingConfirmationTitle1;
        }

        public View getLinearLayoutBookingConfirmationContainer() {
            return linearLayoutBookingConfirmationContainer;
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            JSONObject attributesJsonObject = null;
            if (bookingDetailData != null) {
                mDinerName.setText(String.format(mContext.getResources().getString(R.string.diner_name),
                        BookingDetailUtil.getDinerName(bookingDetailData)));
                mBookingId.setText(BookingDetailUtil.getDisplayId(bookingDetailData));

                attributesJsonObject = BookingDetailUtil.getAttributes(bookingDetailData);
            }

            if (DOPreferences.isDinerDoPlusMember(mContext).equalsIgnoreCase("1") &&
                    attributesJsonObject != null &&
                    attributesJsonObject.optInt("ref_code") > 0) {

                doPlusUniqueCodeSectionLayout.setVisibility(RelativeLayout.VISIBLE);
                uniqueCodetv.setText(Integer.toString(BookingDetailUtil.getRefCode(attributesJsonObject)));

            } else {

                doPlusUniqueCodeSectionLayout.setVisibility(RelativeLayout.GONE);
            }

            // Check if Booking is NEW
            if (bookingDetailData.optString("booking_status").equalsIgnoreCase("NW") &&
                    bookingDetailData.optString("booking_type").equalsIgnoreCase("upcoming")) {

                // Check if Data is available
                JSONObject expectedConfirmationTimeJsonObject =
                        bookingDetailData.optJSONObject("expected_confirmation_time");

                if (expectedConfirmationTimeJsonObject == null) {
                    getLinearLayoutBookingConfirmationContainer().setVisibility(View.GONE);

                } else {
                    boolean showExpectedConfirmationSection = false;

                    // Set Title 1
                    String title1 = expectedConfirmationTimeJsonObject.optString("title_1");
                    if (!AppUtil.isStringEmpty(title1)) {
                        getTextViewBookingConfirmationTitle1().setText(title1);
                        showExpectedConfirmationSection = true;
                    } else {
                        getTextViewBookingConfirmationTitle1().setVisibility(View.GONE);
                    }

                    // Set Title 2
                    String title2 = expectedConfirmationTimeJsonObject.optString("title_2");
                    if (!AppUtil.isStringEmpty(title2)) {
                        getTextViewBookingConfirmationTitle2().setText(title2);
                        showExpectedConfirmationSection = true;
                    } else {
                        getTextViewBookingConfirmationTitle2().setVisibility(View.GONE);
                    }

                    // Set Visibility
                    getLinearLayoutBookingConfirmationContainer().
                            setVisibility(showExpectedConfirmationSection ? View.VISIBLE : View.GONE);
                }

            } else {
                // Hide View
                getLinearLayoutBookingConfirmationContainer().setVisibility(View.GONE);
            }
        }
    }

    public class BookingDetailCtaVH extends RDBaseVH {
        private Button buttonBookingDetailLeftButton;
        private Button buttonBookingDetailRightButton;

        public BookingDetailCtaVH(View itemView) {
            super(itemView);

            buttonBookingDetailLeftButton = (Button) itemView.findViewById(R.id.buttonBookingDetailLeftButton);
            buttonBookingDetailRightButton = (Button) itemView.findViewById(R.id.buttonBookingDetailRightButton);
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);
            int ifPaid = 0;

            // Set Index
            setGetDirectionPaidItemIndex(position);

            // Get CTA Data
            JSONArray ctaJsonArray = bookingDetailData.optJSONArray("cta");
            if (ctaJsonArray != null && ctaJsonArray.length() > 0) {
                JSONObject leftButtonJsonObject = ctaJsonArray.optJSONObject(0);
                JSONObject rightButtonJsonObject = ctaJsonArray.optJSONObject(1);

                // LEFT BUTTON
                if (leftButtonJsonObject != null) {
                    // Set Title
                    String buttonTitle = leftButtonJsonObject.optString("button_text");
                    if (!AppUtil.isStringEmpty(buttonTitle)) {
                        buttonBookingDetailLeftButton.setText(buttonTitle);
                    }

                    // Enable
                    String enable = leftButtonJsonObject.optString("enable");
                    boolean isEnabled = ((!AppUtil.isStringEmpty(enable) && "1".equalsIgnoreCase(enable)));

                    // Action
                    String actionString = leftButtonJsonObject.optString("action");
                    int action = ((AppUtil.isStringEmpty(actionString)) ? 0 : Integer.parseInt(actionString));
                    buttonBookingDetailLeftButton.setTag(action);

                    // Set Text Color
                    buttonBookingDetailLeftButton.
                            setTextColor(mContext.getResources().getColor(AppUtil.getCtaButtonColor(isEnabled, false)));

                    // Set Button Drawable
                    /*buttonBookingDetailLeftButton.
                            setCompoundDrawablesWithIntrinsicBounds(0,
                                    AppUtil.getCtaButtonDrawable(action, isEnabled, false), 0, 0);*/

                    // Set Click Listener
                    if (isEnabled) {
                        buttonBookingDetailLeftButton.setOnClickListener(mClickListener);

                    } else {
                        // Get Message
                        final String message = leftButtonJsonObject.optString("msg");
                        if (!AppUtil.isStringEmpty(message)) {
                            buttonBookingDetailLeftButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    buttonBookingDetailLeftButton.setVisibility(View.VISIBLE);

                } else {
                    buttonBookingDetailLeftButton.setVisibility(View.GONE);
                }

                // RIGHT BUTTON
                if (rightButtonJsonObject != null) {
                    // Set Title
                    String buttonTitle = rightButtonJsonObject.optString("button_text");
                    if (!AppUtil.isStringEmpty(buttonTitle)) {
                        buttonBookingDetailRightButton.setText(buttonTitle);
                    }

                    // Enable
                    String enable = rightButtonJsonObject.optString("enable");
                    boolean isEnabled = ((!AppUtil.isStringEmpty(enable) && "1".equalsIgnoreCase(enable)));

                    // Action
                    String actionString = rightButtonJsonObject.optString("action");
                    int action = ((AppUtil.isStringEmpty(actionString)) ? 0 : Integer.parseInt(actionString));
                    buttonBookingDetailRightButton.setTag(action);

                    // Set Text Color
                    buttonBookingDetailRightButton.
                            setTextColor(mContext.getResources().getColor(AppUtil.getCtaButtonColor(isEnabled, false)));

                    // Set Button Drawable
                    /*buttonBookingDetailRightButton.
                            setCompoundDrawablesWithIntrinsicBounds(0,
                                    AppUtil.getCtaButtonDrawable(action, isEnabled, false), 0, 0);*/

                    // Set Click Listener
                    if (isEnabled) {
                        buttonBookingDetailRightButton.setOnClickListener(mClickListener);

                    } else {
                        // Get Message
                        final String message = rightButtonJsonObject.optString("msg");
                        if (!AppUtil.isStringEmpty(message)) {
                            buttonBookingDetailRightButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    buttonBookingDetailRightButton.setVisibility(View.VISIBLE);

                } else {
                    buttonBookingDetailRightButton.setVisibility(View.GONE);
                }
            } else {
                buttonBookingDetailLeftButton.setVisibility(View.GONE);
                buttonBookingDetailRightButton.setVisibility(View.GONE);
            }

            if (AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingDetailData.optString("b_type"))) {
                // Check if Deal is Redeemed
                if (isHasRedeemedDeal()) {
                    buttonBookingDetailRightButton.setText(R.string.button_paid);
                    buttonBookingDetailRightButton.setTag(1);

                } else {
                    String paid = BookingDetailUtil.isAlreadyPaid(bookingDetailData);

                    if (!AppUtil.isStringEmpty(paid)) {
                        ifPaid = Integer.parseInt(paid);

                        buttonBookingDetailRightButton.setText((ifPaid == 0) ? R.string.button_pay_bill : R.string.button_paid);
                        buttonBookingDetailRightButton.setTag(ifPaid);
                    }
                }
            }
            /*else {
                if (bookingDetailData != null) {
                    int intAlreadyPaid = 0;
                    String value = BookingDetailUtil.isAlreadyPaid(bookingDetailData);

                    if (!AppUtil.isStringEmpty(value)) {
                        intAlreadyPaid = Integer.parseInt(value);
                        buttonBookingDetailRightButton.setText((intAlreadyPaid == 0) ? R.string.button_pay_bill : R.string.button_paid);
                        buttonBookingDetailRightButton.setTag(intAlreadyPaid);
                    }

                    Drawable writeImg = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_write_review_black, null);
                    Drawable reviewedImg = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_already_reviewed, null);

                    if ("1".equalsIgnoreCase(bookingDetailData.optString("is_reviewed"))) {
                        mReview.setText(mContext.getResources().getString(R.string.reviewed));
                        mReview.setCompoundDrawablesWithIntrinsicBounds(null, reviewedImg, null, null);
                        mReview.setEnabled(false);

                    } else {
                        if (AppUtil.isStringEmpty(bookingDetailData.optString("can_review")) ||
                                "0".equalsIgnoreCase(bookingDetailData.optString("can_review"))) {

                            mReview.setText(mContext.getResources().getString(R.string.button_write_review));
                            mReview.setCompoundDrawablesWithIntrinsicBounds(null,
                                    mContext.getResources().getDrawable(R.drawable.ic_write_review_black_disabled), null, null);
                            mReview.setTextColor(mContext.getResources().getColor(R.color.black_40));

                        } else {
                            mReview.setText(mContext.getResources().getString(R.string.button_write_review));
                            mReview.setEnabled(true);
                            mReview.setCompoundDrawablesWithIntrinsicBounds(null, writeImg, null, null);
                        }
                    }
                }

                if ("upcoming".equalsIgnoreCase(bookingDetailData.optString("booking_type"))) {
                    mReserveReviewLayout.setVisibility(View.GONE);
                    mCheckInPayLayout.setVisibility(View.VISIBLE);
                } else {
                    mReserveReviewLayout.setVisibility(View.VISIBLE);
                    mCheckInPayLayout.setVisibility(View.GONE);
                }
            }*/
        }
    }

    public class DinnerFor2VH extends RDBaseVH {
        private TextView mDinerCount, mBookingDate, mBookingTime;
        private TextView mEditBooking, mBookingStatus;

        public DinnerFor2VH(View itemView) {
            super(itemView);
            mDinerCount = (TextView) itemView.findViewById(R.id.tv_booking_diner_count);
            mBookingStatus = (TextView) itemView.findViewById(R.id.textViewBookingStatus);
            mBookingDate = (TextView) itemView.findViewById(R.id.textView_offer_date);
            mBookingTime = (TextView) itemView.findViewById(R.id.textView_offer_time);
            mEditBooking = (TextView) itemView.findViewById(R.id.textView_offer_edit);
            mEditBooking.setOnClickListener(mClickListener);
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            if (bookingDetailData != null) {
                String bkTime = BookingDetailUtil.getDiningDataTime(bookingDetailData).
                        substring(BookingDetailUtil.getDiningDataTime(bookingDetailData).indexOf(" ") + 1);

                mDinerCount.setText(String.format(mContext.getResources().getString(R.string.container_dinner_for),
                        bookingDetailData.optString("slot"), bookingDetailData.optString("cnt_covers")));

                /*String bookingTime = AppUtil.getDisplayFormatDateBookingDetails(bookingDetailData.optString("dining_dt_time"), bkTime);
                mBookingTime.setText(bookingTime);*/

                // Get Selected Date Time
                long dateTimeInMilliSeconds = Long.parseLong(bookingDetailData.optString("dining_dt_time_ts"));
                dateTimeInMilliSeconds = AppUtil.convertSecondsToMilliseconds(dateTimeInMilliSeconds);

                DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();
                HashMap<String, String> dateTimeMap = datePickerUtil.getDateTimeFromTimestamp(dateTimeInMilliSeconds);

                // Set Date
                mBookingDate.setText(dateTimeMap.get(AppConstant.DATE_PICKER_DATE));

                // Set Time
                mBookingTime.setText(dateTimeMap.get(AppConstant.DATE_PICKER_TIME));


                int bookingStatusResourceId = AppUtil.getBookingStatusDrawable(bookingDetailData.optString("status"),
                        bookingDetailData.optString("booking_status"), mBookingStatus);
                mBookingStatus.setBackgroundResource(bookingStatusResourceId);

                //hide show edit booking
                mEditBooking.setVisibility(booleanValue(bookingDetailData, "is_editable") ? View.VISIBLE : View.GONE);
            }
        }
    }

    public class MembershipDetailVH extends RDBaseVH {
        private RelativeLayout cancelBooking, doPlusMembershipSection, resturantDetailLayout;
        private View divider;

        private RelativeLayout uberLayout;

        public MembershipDetailVH(View itemView) {
            super(itemView);
            uberLayout = (RelativeLayout) itemView.findViewById(R.id.rd_uber_layout);
            cancelBooking = (RelativeLayout) itemView.findViewById(R.id.rl_bd_cancel_booking);
            divider = (View) itemView.findViewById(R.id.viewDOPlusMembershipDivider);
            doPlusMembershipSection = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutBookingDetailsDOPlusMembershipSection);
            resturantDetailLayout = (RelativeLayout) itemView.findViewById(R.id.rl_bd_view_resto_detail);
            cancelBooking.setOnClickListener(mClickListener);
            resturantDetailLayout.setOnClickListener(mClickListener);
            doPlusMembershipSection.setOnClickListener(mClickListener);
            uberLayout.setOnClickListener(mClickListener);
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            // Hiding Ride with Uber option issue is in live build (v5.0.4) also
            uberLayout.setVisibility(View.VISIBLE);
            /*if (!TextUtils.isEmpty(getBookingType())) {
                if (getBookingType().equalsIgnoreCase("upcoming")) {
                    uberLayout.setVisibility(View.VISIBLE);
                } else {
                    uberLayout.setVisibility(View.GONE);
                }
            }*/

            if (bookingDetailData != null) {
                if ("1".equalsIgnoreCase(bookingDetailData.optString("can_cancel"))) {
                    cancelBooking.setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.view_bd_cancel_booking_stroke).setVisibility(View.VISIBLE);
                } else {
                    cancelBooking.setVisibility(View.GONE);
                    itemView.findViewById(R.id.view_bd_cancel_booking_stroke).setVisibility(View.GONE);
                }

                JSONObject attributesJsonObject = BookingDetailUtil.getAttributes(bookingDetailData);
                if (DOPreferences.isDinerDoPlusMember(mContext).equalsIgnoreCase("1") &&
                        attributesJsonObject != null &&
                        attributesJsonObject.optInt("ref_code") > 0) {
                    doPlusMembershipSection.setVisibility(View.VISIBLE);

                } else {
                    divider.setVisibility(View.GONE);
                    doPlusMembershipSection.setVisibility(View.GONE);
                }
            }
        }
    }

    private class BookingDealsViewHolder extends RDBaseVH {
        private LinearLayout linearLayoutDeals;
        private TextView textViewBookingStatus;
        private TextView textViewDealDate;
        private TextView textViewDealTime;
        private TextView textViewDealEdit;
        private TextView textViewDealRedeemGuidelines;
        private TextView header;


        public BookingDealsViewHolder(View itemView) {
            super(itemView);

            linearLayoutDeals = (LinearLayout) itemView.findViewById(R.id.linearLayout_deals);
            textViewBookingStatus = (TextView) itemView.findViewById(R.id.textViewBookingStatus);
            textViewDealDate = (TextView) itemView.findViewById(R.id.textView_deal_date);
            textViewDealTime = (TextView) itemView.findViewById(R.id.textView_deal_time);
            textViewDealEdit = (TextView) itemView.findViewById(R.id.textView_deal_edit);
            header = (TextView) itemView.findViewById(R.id.header);
            textViewDealRedeemGuidelines = (TextView) itemView.findViewById(R.id.textView_deal_redeem_guidelines);
        }

        public LinearLayout getLinearLayoutDeals() {
            return linearLayoutDeals;
        }

        public TextView getTextViewBookingStatus() {
            return textViewBookingStatus;
        }

        public TextView getTextViewDealDate() {
            return textViewDealDate;
        }

        public TextView getTextViewDealTime() {
            return textViewDealTime;
        }

        public TextView getTextViewDealEdit() {
            return textViewDealEdit;
        }

        public TextView getTextViewDealRedeemGuidelines() {
            return textViewDealRedeemGuidelines;
        }

        public TextView getHeader() {
            return header;
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            if (bookingDetailData != null) {
                // Set Booking Status
                int bookingStatusResourceId = AppUtil.getBookingStatusDrawable(bookingDetailData.optString("status"),
                        bookingDetailData.optString("booking_status"), getTextViewBookingStatus());

                getTextViewBookingStatus().setBackgroundResource(bookingStatusResourceId);

                if (AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingDetailData.optString("b_type"))) {
                    getHeader().setText("SELECTED DEALS");
                    getTextViewDealRedeemGuidelines().setVisibility(View.VISIBLE);
                    // Display Deals
                    displayDeals(getLinearLayoutDeals(), "deals");

                } else if (AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bookingDetailData.optString("b_type"))) {
                    //getHeader().setText("SELECTED EVENTS");
                    //display events
                    displayDeals(getLinearLayoutDeals(), "events");
                    getTextViewDealRedeemGuidelines().setVisibility(View.GONE);
                }

                // Get Selected Date Time
                long dateTimeInMilliSeconds = Long.parseLong(bookingDetailData.optString("dining_dt_time_ts"));
                String bType = bookingDetailData.optString("b_type", "");
                if (AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bType)) {
                    JSONArray eventJsonArray = bookingDetailData.optJSONArray("events");

                    if (eventJsonArray != null && eventJsonArray.length() > 0) {
                        JSONObject eventJsonObject = eventJsonArray.optJSONObject(0);

                        if (eventJsonObject != null) {
                            // Set Date
                            String validity = eventJsonObject.optString("validity", "");
                            if (!AppUtil.isStringEmpty(validity)) {
                                getTextViewDealDate().setText(validity);
                            }

                            // Set Time
                            String time = eventJsonObject.optString("time", "");
                            if (!AppUtil.isStringEmpty(time)) {
                                getTextViewDealTime().setText(time);
                            }
                        }
                    }
                } else {
                    dateTimeInMilliSeconds = AppUtil.convertSecondsToMilliseconds(dateTimeInMilliSeconds);

                    DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();
                    HashMap<String, String> dateTimeMap = datePickerUtil.getDateTimeFromTimestamp(dateTimeInMilliSeconds);

                    // Set Date
                    getTextViewDealDate().setText(dateTimeMap.get(AppConstant.DATE_PICKER_DATE));

                    // Set Time
                    getTextViewDealTime().setText(dateTimeMap.get(AppConstant.DATE_PICKER_TIME));
                }

                // Check if Deal is Editable
                if (AppUtil.isStringEmpty(bookingDetailData.optString("is_editable")) ||
                        "0".equalsIgnoreCase(bookingDetailData.optString("is_editable"))) {

                    // Hide Edit
                    getTextViewDealEdit().setVisibility(View.GONE);

                } else {
                    // Show Edit
                    getTextViewDealEdit().setVisibility(View.VISIBLE);

                    // Set Edit Deal Booking click listener
                    getTextViewDealEdit().setOnClickListener(mClickListener);
                }

                if (AppConstant.BOOKING_TYPE_DEAL.equalsIgnoreCase(bookingDetailData.optString("b_type"))) {
                    JSONArray dealJsonArray = bookingDetailData.optJSONArray("deals");
                    Bundle bundle = new Bundle();
                    bundle.putLong("DATE", dateTimeInMilliSeconds);
                    bundle.putString("RESTAURANT_ID", bookingDetailData.optString("res_id"));
                    bundle.putString("DEAL_ARRAY", dealJsonArray.toString());
                    bundle.putString("DINER_EMAIL", bookingDetailData.optString("diner_email"));
                    bundle.putString("DINER_PHONE", bookingDetailData.optString("diner_phone"));
                    bundle.putString("DINER_NAME", bookingDetailData.optString("diner_name"));
                    bundle.putString("SPCL_REQUEST", bookingDetailData.optString("spcl_req"));
                    getTextViewDealEdit().setTag(bundle);
                }


                // Set Redeem Guidelines click listener
                getTextViewDealRedeemGuidelines().setOnClickListener(mClickListener);
            }
        }

        private void displayDeals(LinearLayout linearLayoutDeals, String type) {
            // Get Deals and events
            JSONArray dealJsonArray;
            if (type.equalsIgnoreCase("deals")) {
                dealJsonArray = bookingDetailData.optJSONArray("deals");
            } else {
                dealJsonArray = bookingDetailData.optJSONArray("events");


            }


            if (dealJsonArray != null && dealJsonArray.length() > 0) {
                int dealArraySize = dealJsonArray.length();
                linearLayoutDeals.removeAllViews();
                for (int index = 0; index < dealArraySize; index++) {
                    // Get Deal Object
                    JSONObject dealJsonObject = dealJsonArray.optJSONObject(index);
                    getHeader().setText(dealJsonObject.optString("title"));


                    if (dealJsonObject != null) {
                        // Get Deal View
                        View dealView = inflater.inflate(R.layout.deal_display_list_item, null, false);

                        View divider = (View) dealView.findViewById(R.id.divider);
                        if (index == dealArraySize - 1) {
                            divider.setVisibility(View.GONE);
                        } else {
                            divider.setVisibility(View.VISIBLE);
                        }

                        // Set Deal Name
                        TextView textViewDealName = (TextView) dealView.findViewById(R.id.textView_deal_name);
                        if (dealJsonObject.has("ticket_title")) {
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("ticket_title"))) {
                                textViewDealName.setText(dealJsonObject.optString("ticket_title"));
                            }
                        } else {
                            if (!AppUtil.isStringEmpty(dealJsonObject.optString("title"))) {
                                textViewDealName.setText(dealJsonObject.optString("title"));
                            }
                        }

                        TextView textViewDealValidity = (TextView) dealView.findViewById(R.id.textView_deal_validity);
                        if (!AppUtil.isStringEmpty(dealJsonObject.optString("description"))) {
                            textViewDealValidity.setText(dealJsonObject.optString("description"));
                            textViewDealValidity.setVisibility(View.VISIBLE);
                        } else {
                            textViewDealValidity.setVisibility(View.GONE);
                        }


                        // Set Deal Amount
                        if (!AppUtil.isStringEmpty(dealJsonObject.optString("price"))) {
                            TextView textViewDealAmount = (TextView) dealView.findViewById(R.id.textView_deal_amount);
                            if (dealJsonObject.optString("price").equalsIgnoreCase("0")) {
                                textViewDealAmount.setText(R.string.text_free);
                            } else {
                                textViewDealAmount.setText(String.format(mContext.getString(R.string.container_rupee),
                                        dealJsonObject.optString("price")));
                            }
                        }

                        // Set Deal Quantity
                        if (!AppUtil.isStringEmpty(dealJsonObject.optString("quantity"))) {
                            TextView textViewDealQuantity = (TextView) dealView.findViewById(R.id.textView_deal_quantity);
                            textViewDealQuantity.setText(String.format(mContext.getString(R.string.container_quantity),
                                    dealJsonObject.optString("quantity")));
                        }

                        // Add View to Parent
                        linearLayoutDeals.addView(dealView);
                    }
                }
            }
        }
    }

    private class RedeemDealViewHolder extends RDBaseVH {
        private Button buttonListItem;

        public RedeemDealViewHolder(View itemView) {
            super(itemView);

            buttonListItem = (Button) itemView.findViewById(R.id.button_list_item);
        }

        public Button getButtonListItem() {
            return buttonListItem;
        }

        @Override
        public void bindView(int position, JSONObject data) {
            super.bindView(position, data);

            if (bookingDetailData != null) {
                // Set Redeem Deal Index
                setRedeemDealItemIndex(position);

                // Set Text
                if (!AppUtil.isStringEmpty(bookingDetailData.optString("redeem_btn_text"))) {
                    getButtonListItem().setText(bookingDetailData.optString("redeem_btn_text"));
                }

                // Handle Redeem Status
                if (isHasRedeemedDeal()) {
                    dealRedeemed(getButtonListItem());
                    getButtonListItem().setText(R.string.text_deal_redeemed);
                } else {
                    handleRedeemDealStatus(getButtonListItem());
                }
            }
        }

        private void handleRedeemDealStatus(Button buttonListItem) {
            if (!AppUtil.isStringEmpty(bookingDetailData.optString("redeem_status"))) {
                int redeemStatus = Integer.parseInt(bookingDetailData.optString("redeem_status"));

                switch (redeemStatus) {
                    case AppConstant.REDEEM_DEAL_STATUS_HIDE:
                        hideRedeemDeal(buttonListItem);
                        break;

                    case AppConstant.REDEEM_DEAL_STATUS_INACTIVE:
                        inactiveRedeemDeal(buttonListItem);
                        break;

                    case AppConstant.REDEEM_DEAL_STATUS_CAN_REDEEM:
                        proceedRedeemDeal(buttonListItem);
                        break;

                    case AppConstant.REDEEM_DEAL_STATUS_REDEEMED:
                        dealRedeemed(buttonListItem);
                        break;
                }
            }
        }

        private void hideRedeemDeal(Button buttonListItem) {
            // Hide Button
            buttonListItem.setVisibility(View.GONE);
        }

        private void inactiveRedeemDeal(Button buttonListItem) {
            // Set Button Color
            buttonListItem.setBackgroundResource(R.drawable.rectangle_disabled_button_shape);
            buttonListItem.setClickable(false);
        }

        private void proceedRedeemDeal(Button buttonListItem) {
            // Set Button Color
            buttonListItem.setBackgroundResource(R.drawable.rectangle_primary_button_selector);
            buttonListItem.setOnClickListener(mClickListener);
        }

        private void dealRedeemed(Button buttonListItem) {
            // Set Button Color
            buttonListItem.setBackgroundResource(R.drawable.rectangle_success_button_shape);
            buttonListItem.setClickable(false);
        }
    }
}