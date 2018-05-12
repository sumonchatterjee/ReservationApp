package com.dineout.recycleradapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.recycleradapters.HorizontalListItemRecyclerAdapter.IHorizontalItemClickListener;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.DatePickerUtil;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_NAME;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_RATING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.addValueToJsonObject;

public class DynamicFeedsHomePageAdapter extends BaseRecyclerAdapter
        implements Response.Listener<JSONObject>, Response.ErrorListener, View.OnClickListener {

    public static final int ITEM_TYPE_DATE_TIME = 4;
    // Assign positive values only
    private static final int ITEM_TYPE_BANNER = 1;
    private static final int ITEM_TYPE_HORIZONTAL_LIST = 2;
    private static final int ITEM_TYPE_BOOKING = 3;
    private static final int ITEM_TYPE_BANNER_SMALL = 5;
    private static final int ITEM_TYPE_APP_RATING = 6;
    private static final int REQUEST_UPCOMING_BOOKING_CARD = 101;

    IHorizontalItemClickListener horizontalItemClickListener;
    DineoutNetworkManager networkManager;
    ImageLoader imageLoader;
    BannersAdapter adapter;
    Handler handler = new Handler();
    private boolean isCallForBookingMade = false;
    private JSONObject bookingResponse;
    private Context mContext;
    private JSONObject jsonObjectSectionData;
    private int bookingCardPosition;
    private BookingViewHolder bookingViewHolder;
    private UpcomingBookingCardClickListener upcomingBookingCardClickListener;
    private BookingDatePickerListener bookingDatePickerListener;
    private RestaurantFeedbackCallback restaurantFeedbackCallback;
    private LatestNotificationCallback latestNotificationCallback;
    private InAppRatingCallback inAppRatingCallback;
    private BannerClickListener bannerClickListener;
    private long dateTimeInMilliSeconds;

    // Constructor
    public DynamicFeedsHomePageAdapter(Context cxt) {
        mContext = cxt;

        setCallToBookingAlreadyMade(false);
    }

    public void setSectionDataObject(JSONObject object, JSONArray arr) {
        this.jsonObjectSectionData = object;
        setJsonArray(arr);

        // Get Current Time in Milliseconds
        getCurrentTimeInMilliseconds();
    }

    private void getCurrentTimeInMilliseconds() {
        if (dateTimeInMilliSeconds == 0L) {
            DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();
            dateTimeInMilliSeconds = datePickerUtil.getCurrentTimeInMilliseconds();
        }
    }

    // Defining Item View Type Logic
    @Override
    protected int defineItemViewType(int position) {

        if (getJsonArray() == null) {
            return ITEM_VIEW_TYPE_EMPTY;
        }

        // Get JSON Object
        JSONObject jsonObject = getJsonArray().optJSONObject(position);

        // Get Section Key
        String sectionType = jsonObject.optString("section_type");

        if (!TextUtils.isEmpty(sectionType)) {

            if ("banner".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_BANNER;

            } else if ("banner_small".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_BANNER_SMALL;

            } else if ("big_scroll".equalsIgnoreCase(sectionType) ||
                    "small_scroll".equalsIgnoreCase(sectionType) ||
                    "event_scroll".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_HORIZONTAL_LIST;

            } else if ("booking".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_BOOKING;

            } else if ("datetime".equalsIgnoreCase(sectionType)) {
                return ITEM_TYPE_DATE_TIME;
            } else if ("in_app_rating".equalsIgnoreCase(sectionType)){
                return ITEM_TYPE_APP_RATING;
            }
        }

        return ITEM_VIEW_TYPE_EMPTY;
    }

    public void setNetworkManager(DineoutNetworkManager networkManager,
                                  ImageLoader imageLoader) {
        this.networkManager = networkManager;
        this.imageLoader = imageLoader;
    }

    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    // Defining View Holder Logic
    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_BANNER) {
            return new BannerViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.banner_card_layout, parent, false));

        } else if (viewType == ITEM_TYPE_BANNER_SMALL) {
            return new BannerSmallViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.banner_small_card_layout, parent, false));

        } else if (viewType == ITEM_TYPE_HORIZONTAL_LIST) {
            return new HorizontalListViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.feeds_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_BOOKING) {
            return new BookingViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.upcoming_booking_card_layout, parent, false));

        } else if (viewType == ITEM_TYPE_DATE_TIME) {
            return new DateTimeViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.date_time_section_layout, parent, false));

        } else if (viewType == ITEM_TYPE_APP_RATING) {
            return new AppRatingViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.app_rate_item, parent, false));
        }

        return null;
    }

    // Defining logic for rendering List Item
    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {

        // Get Section Key
        String sectionKey = listItem.optString("section_key");
        String sectionType = listItem.optString("section_type");

        if (holder == null) {
            return;
        }

        if (holder.getItemViewType() == ITEM_TYPE_BANNER) {

            inflateBannerTypeView(sectionKey, (BannerViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_BANNER_SMALL) {

            inflateBannerSmallTypeView(sectionKey, (BannerSmallViewHolder) holder);

        } else if (holder.getItemViewType() == ITEM_TYPE_HORIZONTAL_LIST) {

            inflateHorizontalListTypeView((HorizontalListViewHolder)
                    holder, sectionKey, sectionType,position);

        } else if (holder.getItemViewType() == ITEM_TYPE_BOOKING) {
            // Set Booking Card Position
            setBookingCardPosition(position);

            // Set Booking View Holder
            setBookingViewHolder((BookingViewHolder) holder);

            inflateBookingTypeView(sectionKey);

        } else if (holder.getItemViewType() == ITEM_TYPE_DATE_TIME) {

            inflateDateTimeTypeView((DateTimeViewHolder) holder, sectionKey, position);
        } else if (holder.getItemViewType() == ITEM_TYPE_APP_RATING) {
            inflateAppRatingView((AppRatingViewHolder) holder, sectionKey, position);
        }
    }

    // This function returns the corresponding Section Details
    private JSONObject getSectionDetails(String sectionKey) {
        // Check on Section Key
        if (!TextUtils.isEmpty(sectionKey) && jsonObjectSectionData != null) {
            return jsonObjectSectionData.optJSONObject(sectionKey);
        } else {
            return null;
        }
    }

    // This function inflates Banner type view
    private void inflateBannerTypeView(String sectionKey, BannerViewHolder bannerViewHolder) {
        JSONObject jsonObjectSectionDetails = getSectionDetails(sectionKey);

        if (jsonObjectSectionDetails != null) {
            prepareBannerSectionWithData(
                    bannerViewHolder.getPager(),
                    bannerViewHolder.getRadioGroup(),
                    jsonObjectSectionDetails.optJSONArray("data"));
        }
    }

    // This function inflates Banner Small type view
    private void inflateBannerSmallTypeView(String sectionKey, BannerSmallViewHolder bannerSmallViewHolder) {
        JSONObject jsonObjectSectionDetails = getSectionDetails(sectionKey);

        if (jsonObjectSectionDetails != null) {
            prepareBannerSectionWithData(
                    bannerSmallViewHolder.getViewPagerBannerSmall(),
                    bannerSmallViewHolder.getPosIndicatorBannerSmall(),
                    jsonObjectSectionDetails.optJSONArray("data"));
        }
    }

    public void setRadioButtons(ViewPager viewPager, RadioGroup radioGroup, JSONArray data) {

        if (radioGroup == null)
            return;

        radioGroup.removeAllViews();
        int bannersCount = 0;
        if (data != null) {
            bannersCount = data.length();
        }

        int id = radioGroup.getChildCount();
        final int currentItem = viewPager.getCurrentItem();

        while (bannersCount > radioGroup.getChildCount()) {
            ViewGroup vg = (ViewGroup) View.inflate(radioGroup.getContext(),
                    R.layout.layout_indicator_radio_button, null);
            RadioButton radioButton = (RadioButton) vg.getChildAt(0);
            radioButton.setId(id);
            radioButton.setClickable(false);
            radioGroup.addView(vg);
            radioButton.setChecked(currentItem == id);
            id++;
        }

        radioGroup.check(currentItem);
    }

    private void prepareBannerSectionWithData(final ViewPager viewPager, final RadioGroup radioGroup,
                                              final JSONArray bannerJsonArray) {

        // Check if there is more than 1 banner
        if (bannerJsonArray != null && bannerJsonArray.length() > 1) {
            setRadioButtons(viewPager, radioGroup, bannerJsonArray);
            radioGroup.setVisibility(View.VISIBLE);
        } else {
            radioGroup.setVisibility(View.GONE);
        }

        BannersAdapter adapter = new BannersAdapter(getBannerClickListener());
        adapter.setNetworkManager(getNetworkManager(), getImageLoader());
        adapter.setArray(bannerJsonArray);
        viewPager.setAdapter(adapter);

        getBannerClickListener().onBannerAutoScroll(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int currentBannerItem) {
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE)
                    radioGroup.check(currentBannerItem);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public BannerClickListener getBannerClickListener() {
        return bannerClickListener;
    }

    public void setBannerClickListener(BannerClickListener bannerClickListener) {
        this.bannerClickListener = bannerClickListener;
    }

    // This function inflates Restaurant Scroll Type View
    private void inflateHorizontalListTypeView(HorizontalListViewHolder horizontalListViewHolder,
                                               String sectionKey, String sectionType, final int position) {
        // Get corresponding Section Details
        final JSONObject jsonObjectSectionDetails = getSectionDetails(sectionKey);

        if (jsonObjectSectionDetails != null) {
            // Set Section Title
            String title = jsonObjectSectionDetails.optString("header", "");
            if (!TextUtils.isEmpty(title)) {
                horizontalListViewHolder.getTextViewSectionTitle().setText(title);
                horizontalListViewHolder.getTextViewSectionTitle().setVisibility(View.VISIBLE);
            } else {
                horizontalListViewHolder.getTextViewSectionTitle().setVisibility(View.GONE);
            }

            // Set Section Sub Title
            String subTitle = jsonObjectSectionDetails.optString("sub_header", "");
            if (!TextUtils.isEmpty(subTitle)) {
                horizontalListViewHolder.getTextViewSectionSubTitle().setText(subTitle);
                horizontalListViewHolder.getTextViewSectionSubTitle().setVisibility(View.VISIBLE);
            } else {
                horizontalListViewHolder.getTextViewSectionSubTitle().setVisibility(View.GONE);
            }

            // Set More Text
            if (!TextUtils.isEmpty(jsonObjectSectionDetails.optString("view_msg"))) {
                // Set Text
                horizontalListViewHolder.getTextViewSectionMore().
                        setText(jsonObjectSectionDetails.optString("view_msg"));

                // Handle Click using jsonObjectSectionDetails.optString("view_url")
                horizontalListViewHolder.getTextViewSectionMore().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // track event for countly and ga
                        HashMap<String,String> hMap=DOPreferences.getGeneralEventParameters(mContext);
                        if(hMap!=null){
                            hMap.put("category","D_Home");
                            hMap.put("action","CardViewAllClick");
                            hMap.put("label",jsonObjectSectionDetails.optString("header"));
                            hMap.put("poc",Integer.toString(position));
                        }

                        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("Card"+jsonObjectSectionDetails.optString("header")+"Click",hMap);
                        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("D_Home", "CardViewAllClick", jsonObjectSectionDetails.optString("header"));


                        // track event for qgraph, apsalar, branch
                        HashMap<String, Object> props = new HashMap<>();
                        props.put("label",jsonObjectSectionDetails.optString("header"));
                        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventQGraphApsalar("CardViewAllClick",props,true,false);

                        horizontalItemClickListener.
                                onHorizontalListItemClicked(jsonObjectSectionDetails.optString("view_url"));
                    }
                });

                // Set Visibility
                horizontalListViewHolder.getTextViewSectionMore().setVisibility(View.VISIBLE);

            } else {
                // Set Visibility
                horizontalListViewHolder.getTextViewSectionMore().setVisibility(View.GONE);
            }

            // Add Section Type in JSON Object
            try {
                jsonObjectSectionDetails.put("section_type", sectionType);
            } catch (JSONException e) {
                Log.e("DynamicFeedsHome", "section_type could not be added");
            }

            // Prepare View of Section List
            prepareHorizontalScrollList(horizontalListViewHolder.getRecyclerViewHorizontal(),
                    jsonObjectSectionDetails);
        }
    }

    // This function inflates Booking Type View
    private void inflateBookingTypeView(String sectionKey) {
        // Get corresponding Section Details
        JSONObject jsonObject = getSectionDetails(sectionKey);

        if (jsonObject != null) {
            if (jsonObject.optJSONObject("data") != null) {
                // Get API
                String bookingApiName = jsonObject.optJSONObject("data").optString("api_url");

                if (!TextUtils.isEmpty(bookingApiName)  && !isCallToBookingAlreadyMade() &&
                        !TextUtils.isEmpty(DOPreferences.getDinerId(mContext))) {
                    // Take API Hit
                    getNetworkManager().jsonRequestPost(REQUEST_UPCOMING_BOOKING_CARD, bookingApiName,
                            new HashMap<String, String>(), this, this, false);

                    // Set Flag
                    setCallToBookingAlreadyMade(true);

                } else {
                    // Set Booking Details
                    setBookingViewDetails(getBookingResponse(), getBookingViewHolder());
                    //setCallToBookingAlreadyMade(false);

//                    //set notification icon in toolbar
//                    if (getLatestNotificationCallback() != null) {
//                        getLatestNotificationCallback().onNewNotification(new JSONObject());
//                    }
                }
            }
        }
    }

    private boolean isCallToBookingAlreadyMade() {
        return isCallForBookingMade;
    }

    private void setCallToBookingAlreadyMade(boolean isCallForBookingMade) {
        this.isCallForBookingMade = isCallForBookingMade;
    }

    private JSONObject getBookingResponse() {
        return bookingResponse;
    }

    private void setBookingResponse(JSONObject bookingResponse) {
        this.bookingResponse = bookingResponse;
    }

    private int getBookingCardPosition() {
        return bookingCardPosition;
    }

    private void setBookingCardPosition(int bookingCardPosition) {
        this.bookingCardPosition = bookingCardPosition;
    }

    private BookingViewHolder getBookingViewHolder() {
        return bookingViewHolder;
    }

    private void setBookingViewHolder(BookingViewHolder bookingViewHolder) {
        this.bookingViewHolder = bookingViewHolder;
    }

    // This function inflates Date Time Type View
    private void inflateDateTimeTypeView(DateTimeViewHolder viewHolder,
                                         String sectionKey, final int position) {
        // Get corresponding Section Details
        JSONObject jsonObject = getSectionDetails(sectionKey);

        if (jsonObject != null && jsonObject.optJSONObject("data") != null) {
            // Get Data
            jsonObject = jsonObject.optJSONObject("data");

            // Set Label
            if (!TextUtils.isEmpty(jsonObject.optString("title"))) {
                viewHolder.getTextViewDatetimeLabel().setText(jsonObject.optString("title", "").toUpperCase());
            }

            // Get Selected Date Time
            DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();
            HashMap<String, String> dateTimeMap = datePickerUtil.getDateTimeFromTimestamp(dateTimeInMilliSeconds);

            //Update Guest Count
            viewHolder.getTextViewBookingGuestCount().setText(
                    String.format(mContext.getString(R.string.container_multiple_guest), "2"));

            //Update Date
            viewHolder.getTextViewBookingDate().setText(dateTimeMap.get(AppConstant.DATE_PICKER_DATE));

            // Set Time
            viewHolder.getTextViewBookingTime().setText(dateTimeMap.get(AppConstant.DATE_PICKER_TIME));

            // Handle Click
            viewHolder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getBookingDatePickerListener().onBookingDatePickerClick(position);
                }
            });
        }
    }

    private void inflateAppRatingView(AppRatingViewHolder viewHolder, String sectionKey, final int position) {
        final JSONObject sectionData = getSectionDetails(sectionKey);
        JSONObject data;
        if (sectionData != null && ((data = sectionData.optJSONObject("data")) != null)) {
            viewHolder.headerTv.setText(data.optString("title"));

            int appRatingPref = RateNReviewUtil.getInAppRatingValuePref(mContext);
            viewHolder.ratingBar.setRating(appRatingPref);
            viewHolder.ratingBar.setIsIndicator(appRatingPref > 0);
            viewHolder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser) {
                        JSONObject infoObj;
                        try {
                            infoObj = sectionData.getJSONObject("data").getJSONObject("tags_data");

                            JSONObject object = new JSONObject();
                            object.put(RateNReviewUtil.TARGET_SCREEN_KEY, RateNReviewUtil.IN_APP_RATING);
                            RateNReviewUtil.appendObject(object, infoObj.optJSONObject("review_box"));
                            RateNReviewUtil.appendObject(object, infoObj.optJSONObject("review_data"));
                            object.put(REVIEW_RATING, (int) rating);

                            inAppRatingCallback.onRatingChange(object);

                            // app rating tracking
                            try {
                                String categoryName = mContext.getString(R.string.ga_rnr_category_home);
                                String actionName = mContext.getString(R.string.ga_rnr_action_app_rating_star_click);
                                AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(categoryName, actionName, String.valueOf(rating));
                            } catch (Exception e) {
                                // Exception
                            }

                        } catch (Exception e) {
                            // Exception
                        }
                    }
                }
            });
        }
    }

    public BookingDatePickerListener getBookingDatePickerListener() {
        return bookingDatePickerListener;
    }

    public void setBookingDatePickerListener(BookingDatePickerListener bookingDatePickerListener) {
        this.bookingDatePickerListener = bookingDatePickerListener;
    }

    // Prepare View of Section List
    private void prepareHorizontalScrollList(RecyclerView recyclerView, final JSONObject jsonObject) {
        // Initiate Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager
                (mContext, LinearLayoutManager.HORIZONTAL, false);

        // Set Layout Manager
        recyclerView.setLayoutManager(linearLayoutManager);

        // Instantiate Adapter
        HorizontalListItemRecyclerAdapter horizontalListItemRecyclerAdapter =
                new HorizontalListItemRecyclerAdapter(jsonObject, mContext);

        // Set Adapter
        recyclerView.setAdapter(horizontalListItemRecyclerAdapter);

        // Notify Data Change
        horizontalListItemRecyclerAdapter.notifyDataSetChanged();

        // Set Click Listener
        horizontalListItemRecyclerAdapter.setOnHorizontalItemClickListener(horizontalItemClickListener);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(mContext);
                if(hMap!=null){
                    hMap.put("category","D_Home");
                    hMap.put("label",jsonObject.optString("header"));
                    hMap.put("action","CardScroll");
                }

                if(newState == RecyclerView.SCROLL_STATE_SETTLING){
                    Log.d("","state_settling");
                }

                if(newState == RecyclerView.SCROLL_STATE_IDLE){
//                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("Card"+jsonObject.optString("header")+"Click",hMap);
//                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("D_Home","Card"+jsonObject.optString("header")+"Click",Integer.toString(lastVisibleItem));

                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("CardScroll",hMap);
                    AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("D_Home","CardScroll",jsonObject.optString("header"));

                }

            }

        });
    }

    public void setOnHorizontalItemClickListener(IHorizontalItemClickListener horizontalItemClickListener) {
        this.horizontalItemClickListener = horizontalItemClickListener;
    }

    /**
     * API Handlers
     */
    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        if (request.getIdentifier() == REQUEST_UPCOMING_BOOKING_CARD) {
            // Check Response Status
            if (responseObject != null && responseObject.optBoolean("status")) {
                if (responseObject.optJSONObject("output_params") != null) {
                    if (responseObject.optJSONObject("output_params").optJSONObject("data") != null) {
                        // Get Data
                        JSONObject dataJsonObject = responseObject.optJSONObject("output_params").optJSONObject("data");

                        if (dataJsonObject != null) {
                            // Get Title
                            String title = dataJsonObject.optString("title", "");

                            // Get Meta Object
                            JSONObject metaJsonObject = dataJsonObject.optJSONObject("meta");

                            if (metaJsonObject != null) {
                                // Set Title
                                try {
                                    metaJsonObject.put("title", title);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // Set Booking Response
                                setBookingResponse(metaJsonObject);

                                // Notify Item Change
                                notifyItemChanged(getBookingCardPosition());
                            }

                            JSONObject review;
                            if ((review = dataJsonObject.optJSONObject("review")) != null) {
                                if (metaJsonObject != null) {
                                    addValueToJsonObject(review, RESTAURANT_NAME, metaJsonObject.optString("rest_name"));
                                    addValueToJsonObject(review, RESTAURANT_ID, metaJsonObject.optString("r_id"));
                                }

                                if (getRestaurantFeedbackCallback() != null) {
                                    getRestaurantFeedbackCallback().askUserAboutRestaurant(review);
                                }
                            }
                        }
                        // Get Notification Data
                        JSONObject notificationJsonObject =dataJsonObject.optJSONObject("notification");
                        if(notificationJsonObject!=null){
                                if (getLatestNotificationCallback() != null) {
                                    getLatestNotificationCallback().onNewNotification(notificationJsonObject);
                                }
                        }

                    } else {
                        updateDataAfterCallToBooking();
                    }
                }
            } else {

                // Remove Item
                updateDataAfterCallToBooking();
            }
        }
    }

    private void updateDataAfterCallToBooking() {
        int bookingCardPosition = getBookingCardPosition();
        String sectionKeyBooking = "booking";
        //JSONArray arr=getJsonArray();
        JSONArray arr = new JSONArray();
        int updateListSize = getJsonArray().length();
        for (int index = 0; index < updateListSize; index++) {
            if (index != bookingCardPosition) {
                arr.put(getJsonArray().opt(index));
            }
        }

        JSONObject jsonObject = jsonObjectSectionData;
        jsonObject.remove(sectionKeyBooking);
        setSectionDataObject(jsonObject, arr);
        notifyItemRemoved(0);
    }

    private void setBookingViewDetails(final JSONObject bookingResponse, BookingViewHolder bookingViewHolder) {
        // Check for NULL
        if (bookingViewHolder != null && bookingResponse != null) {

            // Show Booking Card
            bookingViewHolder.getLinearLayoutBookingDetailSection().
                    setVisibility(LinearLayout.VISIBLE);

            // View All
            bookingViewHolder.getViewAllBookingsTxt().setVisibility(View.VISIBLE);
            bookingViewHolder.getViewAllBookingsTxt().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getUpcomingBookingCardClickListener().onViewAllBookings(bookingResponse);
                }
            });

            // Set Title
            String title = bookingResponse.optString("title", "");
            if (!AppUtil.isStringEmpty(title)) {
                bookingViewHolder.getTextViewUpcomingBookingHeader().setText(title);
                bookingViewHolder.getTextViewUpcomingBookingHeader().setVisibility(View.VISIBLE);
            } else {
                bookingViewHolder.getTextViewUpcomingBookingHeader().setVisibility(View.GONE);
            }

            // Set Restaurant Name
            String restaurantName = bookingResponse.optString("title1", "");
            if (!AppUtil.isStringEmpty(restaurantName)) {
                bookingViewHolder.getTextViewBookingRestaurantName().setText(restaurantName);
            }

            // Set Diner Count
            String dinerCount = bookingResponse.optString("title2", "");
            if (!AppUtil.isStringEmpty(dinerCount)) {
                bookingViewHolder.getTextViewBookingDinnerCount().setText(dinerCount);
            }

            // Set Booking Date and Time
            String bType = bookingResponse.optString("b_type", "");
            if (AppConstant.BOOKING_TYPE_EVENT.equalsIgnoreCase(bType)) {
                JSONArray eventJsonArray = bookingResponse.optJSONArray("events");

                if (eventJsonArray != null && eventJsonArray.length() > 0) {
                    JSONObject eventJsonObject = eventJsonArray.optJSONObject(0);

                    if (eventJsonObject != null) {
                        String validity = eventJsonObject.optString("validity", "");
                        String time = eventJsonObject.optString("time", "");

                        if (!AppUtil.isStringEmpty(validity) && !AppUtil.isStringEmpty(time)) {
                            bookingViewHolder.getTextViewBookingDateTime().setText(validity + "\n" + time);
                        }
                    }
                }
            } else {
                if (!AppUtil.isStringEmpty(bookingResponse.optString("dining_dt_time_ts"))) {
                    DatePickerUtil datePickerUtil = DatePickerUtil.getInstance();

                    // Get Display Date Time
                    String displayDateTime = datePickerUtil.getBookingDisplayDateTime(
                            AppUtil.convertSecondsToMilliseconds(
                                    Long.parseLong(bookingResponse.optString("dining_dt_time_ts"))));

                    displayDateTime = displayDateTime.replace("#", "at");
                    displayDateTime = displayDateTime.replace("-", "\n");

                    bookingViewHolder.getTextViewBookingDateTime().setText(displayDateTime);
                }
            }

            // Show Expected Booking Confirmation Section
            // Check if Booking is NEW
            if (bookingResponse.optString("booking_status").equalsIgnoreCase("NW") &&
                    bookingResponse.optString("booking_type").equalsIgnoreCase("upcoming")) {

                // Check if Data is available
                JSONObject expectedConfirmationTimeJsonObject = bookingResponse.optJSONObject("expected_confirmation_time");

                if (expectedConfirmationTimeJsonObject != null) {
                    // Set Booking User Expectation
                    String title1 = expectedConfirmationTimeJsonObject.optString("title_1", "");
                    if (!AppUtil.isStringEmpty(title1)) {
                        // Get second Message
                        String title2 = expectedConfirmationTimeJsonObject.optString("title_2", "");
                        if (!AppUtil.isStringEmpty(title2)) {
                            title1 += "\n" + title2;
                        }

                        bookingViewHolder.getTextViewBookingUserExpectation().setText(title1);
                        bookingViewHolder.getTextViewBookingUserExpectation().setVisibility(View.VISIBLE);
                    } else {
                        bookingViewHolder.getTextViewBookingUserExpectation().setVisibility(View.GONE);
                    }
                } else {
                    bookingViewHolder.getTextViewBookingUserExpectation().setVisibility(View.GONE);
                }
            } else {
                bookingViewHolder.getTextViewBookingUserExpectation().setVisibility(View.GONE);
            }

            // Get Status Drawable and set Text
            final int colorId = AppUtil.getBookingStatusColor(
                    bookingResponse.optString("status"),
                    bookingResponse.optString("booking_status"),
                    bookingViewHolder.getTextViewBookingStatus());

            // Set Text Color
            bookingViewHolder.getTextViewBookingStatus().setTextColor(mContext.getResources().getColor(colorId));

            // Handle Edit Click
            if (!AppUtil.isStringEmpty(bookingResponse.optString("is_editable")) &&
                    bookingResponse.optInt("is_editable") == 1) {
                // Set Visibility
                bookingViewHolder.getImageViewBookingEdit().setVisibility(View.VISIBLE);

                // Set Click Handler
                bookingViewHolder.getImageViewBookingEdit().setOnClickListener(this);

            } else {
                // Set Visibility
                bookingViewHolder.getImageViewBookingEdit().setVisibility(View.GONE);
            }

            // Handle Share Click
            bookingViewHolder.getImageViewBookingShare().setOnClickListener(this);

            // Get CTA Data
            JSONArray ctaJsonArray = bookingResponse.optJSONArray("cta");
            if (ctaJsonArray != null && ctaJsonArray.length() > 0) {
                JSONObject leftButtonJsonObject = ctaJsonArray.optJSONObject(0);
                JSONObject rightButtonJsonObject = ctaJsonArray.optJSONObject(1);

                // LEFT BUTTON
                if (leftButtonJsonObject != null) {
                    // Set Title
                    String buttonTitle = leftButtonJsonObject.optString("button_text");
                    if (!AppUtil.isStringEmpty(buttonTitle)) {
                        bookingViewHolder.getTextViewBookingLeftButton().setText(buttonTitle);
                    }

                    String buttonSubTitle = leftButtonJsonObject.optString("button_sub_text");
                    if(!AppUtil.isStringEmpty(buttonSubTitle)){
                        bookingViewHolder.getTextViewLeftSubtitle().setText(buttonSubTitle);
                        bookingViewHolder.getTextViewLeftSubtitle().setVisibility(View.VISIBLE);
                    }else{
                        bookingViewHolder.getTextViewLeftSubtitle().setVisibility(View.GONE);
                    }

                    // Enable
                    String enable = leftButtonJsonObject.optString("enable");
                    boolean isEnabled = ((!AppUtil.isStringEmpty(enable) && "1".equalsIgnoreCase(enable)));

                    // Action
                    String actionString = leftButtonJsonObject.optString("action");
                    int action = ((AppUtil.isStringEmpty(actionString)) ? 0 : Integer.parseInt(actionString));

                    String btnBgColor = AppUtil.getCTABtnBg(mContext, action);

                    // Set Text Color
                    bookingViewHolder.getTextViewBookingLeftButton().
                            setTextColor(mContext.getResources().getColor(AppUtil.getCtaButtonColor(isEnabled, true)));

                    // Set Button Drawable
                    /*bookingViewHolder.getImageViewBookingLeftButton().
                            setImageResource(AppUtil.getCtaButtonDrawable(action, isEnabled, true));*/

                    // Set Click Listener
                    if (isEnabled) {
                        bookingViewHolder.getRelativeLayoutBookingLeftButton().setBackgroundResource(R.drawable.round_rectangle_primary_square_shape);
                        GradientDrawable leftShape = (GradientDrawable) bookingViewHolder.getRelativeLayoutBookingLeftButton().getBackground();
                        leftShape.setColor(Color.parseColor(btnBgColor));
                        bookingViewHolder.getRelativeLayoutBookingLeftButton().setTag(action);
                        bookingViewHolder.getRelativeLayoutBookingLeftButton().setOnClickListener(this);
                    } else {

                        bookingViewHolder.getRelativeLayoutBookingLeftButton().setBackgroundResource(R.drawable.round_rectangle_square_disabled_shape);
                        // Get Message
                        final String message = leftButtonJsonObject.optString("msg");
                        if (!AppUtil.isStringEmpty(message)) {
                            bookingViewHolder.getRelativeLayoutBookingLeftButton().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            bookingViewHolder.getRelativeLayoutBookingLeftButton().setOnClickListener(null);
                        }
                    }

                    bookingViewHolder.getRelativeLayoutBookingLeftButton().setVisibility(View.VISIBLE);

                } else {
                    bookingViewHolder.getRelativeLayoutBookingLeftButton().setVisibility(View.GONE);
                }

                // RIGHT BUTTON
                if (rightButtonJsonObject != null) {
                    // Set Title
                    String buttonTitle = rightButtonJsonObject.optString("button_text");
                    if (!AppUtil.isStringEmpty(buttonTitle)) {
                        bookingViewHolder.getTextViewBookingRightButton().setText(buttonTitle);
                    }

                    String buttonSubTitle = rightButtonJsonObject.optString("button_sub_text");
                    if(!AppUtil.isStringEmpty(buttonSubTitle)){
                        bookingViewHolder.getTextViewRightSubtitle().setText(buttonSubTitle);
                        bookingViewHolder.getTextViewRightSubtitle().setVisibility(View.VISIBLE);
                    }else{
                        bookingViewHolder.getTextViewRightSubtitle().setVisibility(View.GONE);
                    }
                    // Enable
                    String enable = rightButtonJsonObject.optString("enable");
                    boolean isEnabled = ((!AppUtil.isStringEmpty(enable) && "1".equalsIgnoreCase(enable)));

                    // Action
                    String actionString = rightButtonJsonObject.optString("action");
                    int action = ((AppUtil.isStringEmpty(actionString)) ? 0 : Integer.parseInt(actionString));
                    String btnBgColor = AppUtil.getCTABtnBg(mContext, action);

                    // Set Text Color
                    bookingViewHolder.getTextViewBookingRightButton().
                            setTextColor(mContext.getResources().getColor(AppUtil.getCtaButtonColor(isEnabled, true)));

                    // Set Button Drawable
                    /*bookingViewHolder.getImageViewBookingRightButton().
                            setImageResource(AppUtil.getCtaButtonDrawable(action, isEnabled, true));*/

                    // Set Click Listener
                    if (isEnabled) {
                        bookingViewHolder.getRelativeLayoutBookingRightButton().setBackgroundResource(R.drawable.round_rectangle_primary_square_shape);
                        GradientDrawable leftShape = (GradientDrawable) bookingViewHolder.getRelativeLayoutBookingRightButton().getBackground();
                        leftShape.setColor(Color.parseColor(btnBgColor));
                        bookingViewHolder.getRelativeLayoutBookingRightButton().setTag(action);
                        bookingViewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(this);
                    } else {

                        bookingViewHolder.getRelativeLayoutBookingRightButton().setBackgroundResource(R.drawable.round_rectangle_square_disabled_shape);
                        // Get Message
                        final String message = rightButtonJsonObject.optString("msg");
                        if (!AppUtil.isStringEmpty(message)) {
                            bookingViewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            bookingViewHolder.getRelativeLayoutBookingRightButton().setOnClickListener(null);
                        }
                    }

                    bookingViewHolder.getRelativeLayoutBookingRightButton().setVisibility(View.VISIBLE);

                } else {
                    bookingViewHolder.getRelativeLayoutBookingRightButton().setVisibility(View.GONE);
                }
            } else {
                bookingViewHolder.getRelativeLayoutBookingLeftButton().setVisibility(View.GONE);
                bookingViewHolder.getRelativeLayoutBookingRightButton().setVisibility(View.GONE);
            }

            // Handle Upcoming Booking Card Click
            bookingViewHolder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getUpcomingBookingCardClickListener().onUpcomingBookingCardClick(bookingResponse);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.relativeLayout_booking_left_button ||
                viewId == R.id.relativeLayout_booking_right_button) {
            performCtaButtonAction((int) view.getTag());

        } else if (viewId == R.id.imageViewBookingEdit) {
            getUpcomingBookingCardClickListener().onEditBookingClick(bookingResponse);

        } else if (viewId == R.id.imageViewBookingShare) {
            getUpcomingBookingCardClickListener().onShareBookingDetailsClick(bookingResponse);
        }
    }

    private void performCtaButtonAction(int action) {
        if (bookingResponse != null) {
            switch (action) {
                case AppConstant.CTA_RESERVE_AGAIN: // Reserve Now/Reserve Again
                    // Do Nothing... PAST BOOKING
                    break;

                case AppConstant.CTA_PAY_NOW: // Pay Now
                    getUpcomingBookingCardClickListener().onPayNowClick(bookingResponse);
                    break;

                case AppConstant.CTA_PAID: // Paid
                    // Do Nothing...
                    break;

                case AppConstant.CTA_UPLOAD_BILL: // Upload Bill
                    getUpcomingBookingCardClickListener().onUploadBillClick(bookingResponse);
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
                    getUpcomingBookingCardClickListener().onGetDirectionsClick(bookingResponse);
                    break;

                case AppConstant.CTA_REDEEM: // Get Direction
                    getUpcomingBookingCardClickListener().askForRedeemDealConfirmation(bookingResponse);
                    break;
            }
        }
    }

    private UpcomingBookingCardClickListener getUpcomingBookingCardClickListener() {
        return upcomingBookingCardClickListener;
    }

    public void setUpcomingBookingCardClickListener(UpcomingBookingCardClickListener upcomingBookingCardClickListener) {
        this.upcomingBookingCardClickListener = upcomingBookingCardClickListener;
    }

    public RestaurantFeedbackCallback getRestaurantFeedbackCallback() {
        return restaurantFeedbackCallback;
    }

    public void setRestaurantFeedbackCallback(RestaurantFeedbackCallback restaurantFeedbackCallback) {
        this.restaurantFeedbackCallback = restaurantFeedbackCallback;
    }

    public void setInAppRatingCallback(InAppRatingCallback inAppRatingCallback) {
        this.inAppRatingCallback = inAppRatingCallback;
    }


    public LatestNotificationCallback getLatestNotificationCallback() {
        return latestNotificationCallback;
    }

    public void setLatestNotificationCallback(LatestNotificationCallback latestNotificationCallback) {
        this.latestNotificationCallback = latestNotificationCallback;
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        if (request.getIdentifier() == REQUEST_UPCOMING_BOOKING_CARD) {
            // Do Nothing...
        }
    }

    public interface BannerClickListener {
        void onBannerClick(JSONObject bannerJsonObject,int position);

        void onBannerAutoScroll(ViewPager pager);
    }

    public interface BookingDatePickerListener {
        void onBookingDatePickerClick(int datePickerItemPosition);
    }

    public interface UpcomingBookingCardClickListener {
        /* Handle click on Booking Card */
        void onUpcomingBookingCardClick(JSONObject bookingResponse);

        /* Handle click on Edit Icon */
        void onEditBookingClick(JSONObject bookingResponse);

        /* Handle click on Share Icon */
        void onShareBookingDetailsClick(JSONObject bookingResponse);

        /* Handle click on Get Directions */
        void onGetDirectionsClick(JSONObject bookingResponse);

        /* Handle click on Pay Now */
        void onPayNowClick(JSONObject bookingResponse);

        /* Handle click on view all bookings */
        void onViewAllBookings(JSONObject bookingResponse);

        /* Handle click on Upload Bill */
        void onUploadBillClick(JSONObject bookingResponse);

        /* Handle Redeem Deal */
        void askForRedeemDealConfirmation(JSONObject bookingResponse);
    }

    public interface RestaurantFeedbackCallback {
        void askUserAboutRestaurant(JSONObject object);
    }

    public interface InAppRatingCallback {
        void onRatingChange(JSONObject object);
    }

    public interface LatestNotificationCallback{
        void onNewNotification(JSONObject object);
    }

    private class BannerViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPager;
        private RadioGroup radioGroup;

        protected BannerViewHolder(View itemView) {
            super(itemView);

            viewPager = (ViewPager) itemView.findViewById(R.id.view_pager_banner);
            radioGroup = (RadioGroup) itemView.findViewById(R.id.banner_pos_indicator);
        }

        public ViewPager getPager() {
            return viewPager;
        }

        public RadioGroup getRadioGroup() {
            return radioGroup;
        }
    }

    private class BannerSmallViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPagerBannerSmall;
        private RadioGroup posIndicatorBannerSmall;

        public BannerSmallViewHolder(View itemView) {
            super(itemView);

            viewPagerBannerSmall = (ViewPager) itemView.findViewById(R.id.view_pager_banner_small);
            posIndicatorBannerSmall = (RadioGroup) itemView.findViewById(R.id.pos_indicator_banner_small);
        }

        public ViewPager getViewPagerBannerSmall() {
            return viewPagerBannerSmall;
        }

        public RadioGroup getPosIndicatorBannerSmall() {
            return posIndicatorBannerSmall;
        }
    }

    private class HorizontalListViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSectionTitle;
        private TextView textViewSectionSubTitle;
        private TextView textViewSectionMore;
        private RecyclerView recyclerViewHorizontal;

        public HorizontalListViewHolder(View itemView) {
            super(itemView);

            textViewSectionTitle = (TextView) itemView.findViewById(R.id.text_view_section_title);
            textViewSectionSubTitle = (TextView) itemView.findViewById(R.id.textView_section_sub_title);
            textViewSectionMore = (TextView) itemView.findViewById(R.id.text_view_section_more);
            //textViewSectionMore.setVisibility(View.GONE);
            recyclerViewHorizontal = (RecyclerView) itemView.findViewById(R.id.recycler_view_section_list);
        }

        public TextView getTextViewSectionTitle() {
            return textViewSectionTitle;
        }

        public TextView getTextViewSectionSubTitle() {
            return textViewSectionSubTitle;
        }

        public TextView getTextViewSectionMore() {
            return textViewSectionMore;
        }

        public RecyclerView getRecyclerViewHorizontal() {
            return recyclerViewHorizontal;
        }
    }

    private class BookingViewHolder extends RecyclerView.ViewHolder {
        // Booking Card Layout Elements
        private ImageView imageViewBookingEdit;
        private ImageView imageViewBookingShare;
        private TextView textViewBookingRestaurantName;
        private TextView textViewBookingDinnerCount;
        private TextView textViewBookingDateTime;
        private TextView textViewBookingStatus;
        private View relativeLayoutBookingLeftButton;
        private TextView relativeLayoutSubTitleBookingRightButton;
        private TextView  relativeLayoutSubTitleBookingLeftButton;
        private ImageView imageViewBookingLeftButton;
        private TextView textViewBookingLeftButton;
        private View relativeLayoutBookingRightButton;
        private ImageView imageViewBookingRightButton;
        private TextView textViewBookingRightButton;
        private TextView textViewBookingUserExpectation;

        // Upcoming Booking Card Layout Elements
        private View linearLayoutBookingDetailSection;
        private TextView viewAllBookingsTxt;
        private TextView textViewUpcomingBookingHeader;
        private View rootView;

        public BookingViewHolder(View itemView) {
            super(itemView);

            imageViewBookingEdit = (ImageView) itemView.findViewById(R.id.imageViewBookingEdit);
            imageViewBookingShare = (ImageView) itemView.findViewById(R.id.imageViewBookingShare);
            textViewBookingRestaurantName = (TextView) itemView.findViewById(R.id.textViewBookingRestaurantName);
            textViewBookingDinnerCount = (TextView) itemView.findViewById(R.id.textViewBookingDinnerCount);
            textViewBookingDateTime = (TextView) itemView.findViewById(R.id.textViewBookingDateTime);
            textViewBookingStatus = (TextView) itemView.findViewById(R.id.textView_booking_status);
            relativeLayoutBookingLeftButton = itemView.findViewById(R.id.relativeLayout_booking_left_button);
            relativeLayoutSubTitleBookingRightButton = (TextView) itemView.findViewById(R.id.textViewBookingSubTitleRightButton);
            relativeLayoutSubTitleBookingLeftButton=(TextView) itemView.findViewById(R.id.textViewBookingSubTitleLeftButton);
            imageViewBookingLeftButton = (ImageView) itemView.findViewById(R.id.imageView_booking_left_button);
            textViewBookingLeftButton = (TextView) itemView.findViewById(R.id.textViewBookingLeftButton);
            relativeLayoutBookingRightButton = itemView.findViewById(R.id.relativeLayout_booking_right_button);
            imageViewBookingRightButton = (ImageView) itemView.findViewById(R.id.imageView_booking_right_button);
            textViewBookingRightButton = (TextView) itemView.findViewById(R.id.textViewBookingRightButton);
            textViewBookingUserExpectation = (TextView) itemView.findViewById(R.id.textView_booking_user_expectation);

            linearLayoutBookingDetailSection = itemView.findViewById(R.id.linearLayout_upcoming_booking_section);
            linearLayoutBookingDetailSection.setBackgroundResource(R.color.white_f1);
            viewAllBookingsTxt = (TextView) itemView.findViewById(R.id.textView_view_all_bookings);
            textViewUpcomingBookingHeader = (TextView) itemView.findViewById(R.id.textView_upcoming_booking_header);
            rootView = itemView.findViewById(R.id.booking_card_view);
        }

        public TextView getViewAllBookingsTxt() {
            return viewAllBookingsTxt;
        }

        public ImageView getImageViewBookingEdit() {
            return imageViewBookingEdit;
        }

        public ImageView getImageViewBookingShare() {
            return imageViewBookingShare;
        }

        public TextView getTextViewBookingRestaurantName() {
            return textViewBookingRestaurantName;
        }

        public TextView getTextViewBookingDinnerCount() {
            return textViewBookingDinnerCount;
        }

        public TextView getTextViewBookingDateTime() {
            return textViewBookingDateTime;
        }

        public TextView getTextViewBookingStatus() {
            return textViewBookingStatus;
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

        public View getRootView() {
            return rootView;
        }

        public View getLinearLayoutBookingDetailSection() {
            return linearLayoutBookingDetailSection;
        }

        public TextView getTextViewBookingUserExpectation() {
            return textViewBookingUserExpectation;
        }

        public TextView getTextViewUpcomingBookingHeader() {
            return textViewUpcomingBookingHeader;
        }

        public TextView getTextViewRightSubtitle() {
            return relativeLayoutSubTitleBookingRightButton;
        }

        public TextView getTextViewLeftSubtitle() {
            return relativeLayoutSubTitleBookingLeftButton;
        }
    }

    private class DateTimeViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDatetimeLabel;
        private TextView textViewBookingGuestCount;
        private TextView textViewBookingDate;
        private TextView textViewBookingTime;
        private View rootView;

        public DateTimeViewHolder(View itemView) {
            super(itemView);

            textViewDatetimeLabel = (TextView) itemView.findViewById(R.id.text_view_datetime_label);
            textViewBookingGuestCount = (TextView) itemView.findViewById(R.id.text_view_booking_guest_count);
            textViewBookingDate = (TextView) itemView.findViewById(R.id.text_view_booking_date);
            textViewBookingTime = (TextView) itemView.findViewById(R.id.text_view_booking_time);
            rootView = textViewBookingDate.getRootView();
        }

        public TextView getTextViewDatetimeLabel() {
            return textViewDatetimeLabel;
        }

        public TextView getTextViewBookingGuestCount() {
            return textViewBookingGuestCount;
        }

        public TextView getTextViewBookingDate() {
            return textViewBookingDate;
        }

        public TextView getTextViewBookingTime() {
            return textViewBookingTime;
        }

        public View getRootView() {
            return rootView;
        }
    }

    private class AppRatingViewHolder extends RecyclerView.ViewHolder {
        private TextView headerTv;
        private RatingBar ratingBar;

        public AppRatingViewHolder(View itemView) {
            super(itemView);

            headerTv = (TextView) itemView.findViewById(R.id.header_tv);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
        }
    }
}
