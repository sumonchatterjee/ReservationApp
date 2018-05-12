package com.dineout.book.fragment.detail;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.controller.UploadBillController;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.bookingflow.BookingTimeSlotFragment;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.dialogs.DOShareDialog;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RestaurantDetailFragment extends AuthenticationWrapperJSONReqFragment
        implements Toolbar.OnMenuItemClickListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    private final int REQUEST_CODE_RESTAURANT_DETAIL_RESERVATION = 101;
    private final int REQUEST_CODE_GET_DINER_WALLET_AMOUNT = 102;

    private static final String RESPONSE_INFO_STRING = "btn_cta_bg_info_string";

    private boolean isDiscovery;
    private boolean apiShowReserveTable;
    private int walletAmount;
    private String restaurantId = "";
    private Bundle restaurantBundle;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CollapsingToolbarLayout collapsingToolbar;
    private JSONObject dataJsonObject;
    private ArrayList<MasterDOFragment> tabFragments;
    private RestaurantDetailTabPager restaurantDetailTabPager;
    //private Button buttonRestaurantReserve;
    private View additionalOfferSection;
    private RestaurantDetailEventPageFragment eventPageFragment;
    private View.OnClickListener navigationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popBackStack(getActivity().getSupportFragmentManager());
        }
    };
    private TextView mCTAHeaderText;
    private ViewGroup mBottomButtonsWrapper;
    private ViewGroup mBottomLeftBtnWrapper;
    private ViewGroup mBottomRightBtnWrapper;
    private TextView mLeftBtnTitle;
    private TextView mLeftBtnSubTitle;
    private TextView mRightBtnTitle;
    private TextView mRightBtnSubTitle;

    private boolean isSmartPay,isUploadBill;
    private String restaurantType;
    private String restaurantCategory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set has Menu Options
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
        trackScreenName(getContext().getResources().getString(R.string.countly_reaturant_detail));

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());

        // Set Bundle
        restaurantBundle = getArguments();

        // Set Restaurant Id
        if (restaurantBundle != null) {
            restaurantId = restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, "");
            //trackEventGA("Details", "Details_Reservation", restaurantId);


        }

        // Setup View
        setUpView();

        // Get Restaurant Detail from API
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity()));

    }

    private void setUpView() {
        // Create Tabs
        tabLayout = (TabLayout) getView().findViewById(R.id.tabLayout_rest_detail);

        viewPager = (ViewPager) getView().findViewById(R.id.viewPager_rest_detail);

        restaurantDetailTabPager = new RestaurantDetailTabPager(getChildFragmentManager());

        collapsingToolbar = (CollapsingToolbarLayout) getView().findViewById(R.id.collapsingToolbar_rest_detail);

        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(navigationListener);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.inflateMenu(R.menu.menu_restaurant_details);

        mCTAHeaderText = (TextView) getView().findViewById(R.id.cta_header);
        mBottomButtonsWrapper = (ViewGroup) getView().findViewById(R.id.reservation_cta_button_layout);
        mBottomLeftBtnWrapper = (ViewGroup) getView().findViewById(R.id.left_tvs_wrapper);
        mBottomRightBtnWrapper = (ViewGroup) getView().findViewById(R.id.right_tvs_wrapper);
        mLeftBtnTitle = (TextView) getView().findViewById(R.id.left_tv_title);
        mLeftBtnSubTitle = (TextView) getView().findViewById(R.id.left_tv_sub_title);
        mRightBtnTitle = (TextView) getView().findViewById(R.id.right_tv_title);
        mRightBtnSubTitle = (TextView) getView().findViewById(R.id.right_tv_sub_title);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:
                if (dataJsonObject != null) {
                    shareRestaurantDetails(dataJsonObject);
                }
                return true;
        }

        return false;
    }

    private void shareRestaurantDetails(JSONObject dataJsonObject) {
        // Track Event
        trackEventGA(getString(R.string.ga_screen_detail),
                getString(R.string.ga_action_share), dataJsonObject.optString("profileName"));

        String shareMessage = "";
        String subject = "";

        if (dataJsonObject.optJSONObject("share") != null) {
            shareMessage = dataJsonObject.optJSONObject("share").toString();

            if (dataJsonObject.optJSONObject("share").optJSONObject("email") != null) {
                subject = dataJsonObject.optJSONObject("share").optJSONObject("email").optString("subject");
            }
        }

        DOShareDialog doShareDialog = DOShareDialog.newInstance(getActivity(), shareMessage, subject);

        doShareDialog.show(getActivity().getSupportFragmentManager(), "booking");
    }

    @Override
    public void onClick(View view) {
        // View Id
        int viewId = view.getId();

        if (viewId == R.id.imageView_restaurant_banner) {
            handleWebBannerClick((String) view.getTag());

        }else if (viewId == R.id.relative_layout_additional_offer_section){
            JSONObject obj = (JSONObject) view.getTag();
            String deeplink = obj.optString("deepLink", "");

            if(!TextUtils.isEmpty(deeplink)) {
                MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
                if (masterDOFragment != null) {
                    addToBackStack(getFragmentManager(), masterDOFragment);
                }
            }
        }

    }

    private void handleWebBannerClick(String bannerClickUrl) {
        // Track Event
       // trackEventGA("Details", getString(R.string.ga_action_banner_girf), "GIRF_" + bannerClickUrl);

        // Check for NULL / Empty
        if (!AppUtil.isStringEmpty(bannerClickUrl)) {
            MasterDOFragment fragment = DeeplinkParserManager.getFragment(getActivity(), bannerClickUrl);
            addToBackStack(getFragmentManager(), fragment);
        }
    }

    // Ask User to Login
//    private void askUserToLogin(final Bundle bookingBundle) {
//
//        // initiate login flow
//        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null,
//                new BookingLoginCallback(bookingBundle, BookingLoginCallback.BOOKING));
//    }

//    private void proceedLoginSuccess(Bundle bookingBundle) {
//
//        if (getActivity() == null)
//            return;
//
//        // Set New Diner Flag
//        if (DOPreferences.isDinerNewUser(getActivity().getApplicationContext())) {
//            // Set Is New User flag
//            DOPreferences.setDinerNewUser(getActivity().getApplicationContext(), "0");
//        }
//
//        proceedSelectBookingSlot(bookingBundle);
//    }

    private void proceedSelectBookingSlot(Bundle bookingBundle) {
        // Add Booking details to bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            bookingBundle.putAll(arguments);
        }

        BookingTimeSlotFragment bookingTimeSlotFragment = new BookingTimeSlotFragment();
        bookingTimeSlotFragment.setArguments(bookingBundle);

        addToBackStack(getFragmentManager(), bookingTimeSlotFragment);
    }

    // Get Restaurant Detail from API
    private void getRestaurantDetailFromAPI() {
        // Check for View attached
        if (getView() != null) {
            // Hide Tabs
            if (tabLayout != null) {
                tabLayout.setVisibility(View.GONE);
            }



            // hide bottom buttons views
            if (mBottomButtonsWrapper != null) mBottomButtonsWrapper.setVisibility(View.GONE);
            if (mBottomLeftBtnWrapper != null) mBottomLeftBtnWrapper.setVisibility(View.GONE);
            if (mBottomRightBtnWrapper != null) mBottomRightBtnWrapper.setVisibility(View.GONE);
            if (mLeftBtnTitle != null) mLeftBtnTitle.setVisibility(View.GONE);
            if (mLeftBtnSubTitle != null) mLeftBtnSubTitle.setVisibility(View.GONE);
            if (mRightBtnTitle != null) mRightBtnTitle.setVisibility(View.GONE);
            if (mRightBtnSubTitle != null) mRightBtnSubTitle.setVisibility(View.GONE);
        }

        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_RESTAURANT_DETAIL_RESERVATION,
                AppConstant.NODE_RESTAURANT_DETAIL_URL,
                ApiParams.getRestaurantDetailsParams(restaurantId,
                        ((restaurantBundle.getBoolean(AppConstant.BUNDLE_IS_EDIT_BOOKING)) ?
                                AppConstant.REST_EDIT_OFFER_BOOKING_RESERVATION_DETAILS :
                                AppConstant.REST_DETAIL_RESERVATION_DETAILS)),
                this, this, true);
    }

    // Display Restaurant Head Details
    private void displayRestaurantHeadDetails(final JSONObject dataJsonObject) {
        if (dataJsonObject != null) {
            // Show Image
            final JSONArray profileImageJsonArray = dataJsonObject.optJSONArray("profileImages");

            if (profileImageJsonArray != null && profileImageJsonArray.length() > 0) {
                // Get Image JSON Object
                JSONObject profileJsonObject = profileImageJsonArray.optJSONObject(0);

                if (profileJsonObject != null) {
                    // Show Image
                    ((NetworkImageView) getView().findViewById(R.id.imageView_rest_image))
                            .setImageUrl(profileJsonObject.optString("imageUrl"),
                                    ImageRequestManager.getInstance(getContext()).getImageLoader());
                }

                // Set Gallery Click
                getView().findViewById(R.id.frameLayout_rest_image).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Track Event
//                        trackEventGA(getString(R.string.ga_screen_detail),
//                                getString(R.string.ga_action_photo), dataJsonObject.optString("profileName"));

                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("menus", getProfileImages(profileImageJsonArray));
                        bundle.putInt("position", 0);
                        bundle.putString("detail","restaurantimage");
                        bundle.putString("restaurantCategory",restaurantCategory);
                        bundle.putString("restaurantType",restaurantType);

                        ImagePagerFragment imageFragment = new ImagePagerFragment();
                        imageFragment.setArguments(bundle);

                        addToBackStack(getFragmentManager(), imageFragment);
                    }
                });
            }

            // Set Restaurant Title
            collapsingToolbar.setTitle(dataJsonObject.optString("profileName"));

            // Show Restaurant Name
            ((TextView) getView().findViewById(R.id.textView_rest_name)).setText(dataJsonObject.optString("profileName"));

            // Set Restaurant Name in Bundle
            restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_NAME, dataJsonObject.optString("profileName"));

            // Show Restaurant Address
            ((TextView) getView().findViewById(R.id.textView_rest_address)).setText(dataJsonObject.optString("localityName") +
                    ", " + dataJsonObject.optString("areaName"));

            // Show Distance
            String distance = dataJsonObject.optString("distance");
            if (!DOPreferences.isAutoMode(getActivity().getApplicationContext()) ||
                    AppUtil.isStringEmpty(distance)) {
                // Hide Distance
                getView().findViewById(R.id.textView_rest_distance).setVisibility(View.GONE);

            } else {
                // Set Distance
                ((TextView) getView().findViewById(R.id.textView_rest_distance)).setText(
                        AppUtil.formatFloatDigits(Float.parseFloat(distance), 2, 2) + " kms");

                // Show Distance
                getView().findViewById(R.id.textView_rest_distance).setVisibility(View.VISIBLE);
            }

            // Show Recency/Rating
            // Check if Rating is greater than 0
            boolean recency = dataJsonObject.optBoolean("recency");
            TextView textViewRating = (TextView) getView().findViewById(R.id.textView_rest_rating);
            TextView textViewRecency = (TextView) getView().findViewById(R.id.textView_recency_tag);

            if (AppUtil.showRecency((recency) ? "1" : "0")) {
                // Show Recency
                textViewRecency.setVisibility(View.VISIBLE);

                // Hide Rating
                textViewRating.setVisibility(View.GONE);

            } else {
                if (AppUtil.isStringEmpty(dataJsonObject.optString("avgRating")) ||
                        AppUtil.hasNoRating(dataJsonObject.optString("avgRating"))) {
                    // Hide Rating
                    textViewRating.setVisibility(View.GONE);

                } else {
                    trackAdTechEvent("int", "dRating:" + dataJsonObject.optString("avgRating"));
                    //completeAdTechSesion();

                    // Set Rating
                    AppUtil.setRatingValueBackground(dataJsonObject.optString("avgRating"), textViewRating);

                    // Show Rating
                    textViewRating.setVisibility(View.VISIBLE);
                }

                // Hide Recency
                textViewRecency.setVisibility(View.GONE);
            }

            // Show cuisines
            // Get Cuisine List
            JSONArray cuisineJsonArray = dataJsonObject.optJSONArray("cuisine");

            if (cuisineJsonArray != null && cuisineJsonArray.length() > 0) {
                StringBuilder cuisineStringBuilder = new StringBuilder("");
                StringBuilder cuisineLog = new StringBuilder("");
                int cuisineListSize = cuisineJsonArray.length();

                for (int index = 0; index < cuisineListSize; index++) {
                    // Get Cuisine Object
                    String cuisine = cuisineJsonArray.optString(index);

                    if (!TextUtils.isEmpty(cuisine)) {
                        if (AppUtil.isStringEmpty(cuisineStringBuilder.toString())) {
                            cuisineStringBuilder.append(cuisine);

                        } else {
                            cuisineStringBuilder.append(", " + cuisine);
                        }
                    }

                    // Append Cuisine
                    cuisineLog.append(cuisine + "|");
                }

                // Display Text
                ((TextView) getView().findViewById(R.id.textView_rest_cuisine)).setText(cuisineStringBuilder.toString());

                HashMap<String, Object> props = new HashMap<String, Object>();
                props.put("Cuisine", cuisineLog.toString());

                // Track Cuisines
              //  trackEventGA("Cuisine", props);

            } else {
                getView().findViewById(R.id.textView_rest_cuisine).setVisibility(View.GONE);
            }

            // Show cost for 2
            if (AppUtil.isStringEmpty(dataJsonObject.optString("costForTwo"))) {
                // Hide section
                getView().findViewById(R.id.linearLayout_rest_cost_for_2).setVisibility(View.GONE);
            } else {
                ((TextView) getView().findViewById(R.id.textView_rest_cost_for_2)).setText(
                        String.format(getString(R.string.text_cost_for_2_container),
                                dataJsonObject.optString("costForTwo")) + " ");
            }

            // Show DO Plus Tag
            JSONArray tagJsonArray = dataJsonObject.optJSONArray("tags");
            if (tagJsonArray != null && tagJsonArray.length() > 0) {
                int tagsSize = tagJsonArray.length();
                for (int index = 0; index < tagsSize; index++) {
                    String tagName = tagJsonArray.optString(index);

                    if (!AppUtil.isStringEmpty(tagName) &&
                            AppConstant.TAG_DINEOUT_PLUS.equalsIgnoreCase(tagName)) {
                        // Show DO Plus Tag
                        getView().findViewById(R.id.imageView_dineoutplus).setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            // Show Additional Offer
            JSONArray additionalOfferJsonArray = dataJsonObject.optJSONArray("additionalOffer");
            if (additionalOfferJsonArray != null && additionalOfferJsonArray.length() > 0) {
                JSONObject additionalOfferJsonObject = additionalOfferJsonArray.optJSONObject(0);

                // Check for NULL
                if (additionalOfferJsonObject != null) {
                    // Show Additional Offer Section
                    showAdditionalOffer(additionalOfferJsonObject);


                    if (!TextUtils.isEmpty(DOPreferences.getDinerId(getActivity()))) {
                        // Get Earnings from API
                        getEarningsFromAPI(additionalOfferJsonObject);
                    }
                }

                //set on click listener

            }

            // Show Restaurant Header
            getView().findViewById(R.id.linearLayout_rest_detail_head).setVisibility(View.VISIBLE);

            // Check if Edit Booking
            if (restaurantBundle != null && restaurantBundle.get(AppConstant.BUNDLE_OFFER_ID) != null) {
                try {
                    dataJsonObject.put(AppConstant.JSON_PARAM_EDIT_BOOKING_OFFER_ID,
                            restaurantBundle.getInt(AppConstant.BUNDLE_OFFER_ID));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Show Web Banner
            JSONObject restWebBannerJsonObject = dataJsonObject.optJSONObject("restWebBanner");
            if (restWebBannerJsonObject != null) {
                // Get Image URL
                String bannerImageUrl = restWebBannerJsonObject.optString("imageUrl", "");
                if (!AppUtil.isStringEmpty(bannerImageUrl)) {
                    // Get Banner Image
                    NetworkImageView imageViewRestaurantBanner = (NetworkImageView) getView().findViewById(R.id.imageView_restaurant_banner);
                    imageViewRestaurantBanner.setDefaultImageResId(R.drawable.default_list);
                    imageViewRestaurantBanner.setImageUrl(bannerImageUrl, ImageRequestManager.getInstance(getContext()).getImageLoader());

                    // Make it Visible
                    getView().findViewById(R.id.linearLayout_web_banner_container).setVisibility(View.VISIBLE);

                    // Get Click URL
                    String bannerClickUrl = restWebBannerJsonObject.optString("deeplink", "");
                    if (!AppUtil.isStringEmpty(bannerClickUrl)) {
                        imageViewRestaurantBanner.setTag(bannerClickUrl);
                        imageViewRestaurantBanner.setOnClickListener(this);
                    }
                }
            }

            // Set Global Reserve Table Button
            apiShowReserveTable = (dataJsonObject.optInt("showReserveButton", 1) == 1);
        }
    }

    private void showAdditionalOffer(JSONObject additionalOfferJsonObject) {

        TextView textViewAdditionalOfferMessage = (TextView) getView().findViewById(R.id.textView_additional_offer_message);

        // Check for NULL
        if (additionalOfferJsonObject != null) {
            // get Message
            String message = additionalOfferJsonObject.optString("text", "");

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(message);

            // set text bold
            int firstHashPos = message.indexOf("#") + 1;
            int secondHashPos = message.indexOf("#", firstHashPos);
            if (firstHashPos != -1 && secondHashPos != -1
                    && firstHashPos < message.length() && secondHashPos <= message.length()) {

                StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
                stringBuilder.setSpan(bss, firstHashPos, secondHashPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);


                int lastHashPos;
                if ((lastHashPos = message.lastIndexOf("#")) > 0){
                    stringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#CCFFFFFF")),
                            0, lastHashPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    lastHashPos--;
                    stringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            lastHashPos, message.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            for (int i = 0; i < stringBuilder.length();) {
                if (stringBuilder.charAt(i) == '#') {
                    stringBuilder.delete(i, i+1);
                } else {
                    i++;
                }
            }

            textViewAdditionalOfferMessage.setText(stringBuilder);


            // Show Additional Offer Section
            additionalOfferSection = getView().findViewById(R.id.relative_layout_additional_offer_section);
            additionalOfferSection.setVisibility(View.VISIBLE);

            String backgroundColor = additionalOfferJsonObject.optString("backgroundColor");

            if (!backgroundColor.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                backgroundColor = "#428827";
            }

            additionalOfferSection.setBackgroundColor(Color.parseColor(backgroundColor));

            additionalOfferSection.setTag(additionalOfferJsonObject);
            additionalOfferSection.setOnClickListener(this);
        }
    }

    private void getEarningsFromAPI(JSONObject additionalOfferJsonObject) {
        // Check for NULL
        if (additionalOfferJsonObject != null) {
            // Get Wallet Earnings URL
            String earningsUrl = additionalOfferJsonObject.optString("url", "");

            if (!AppUtil.isStringEmpty(earningsUrl)) {
                // Check for NULL
                if (additionalOfferSection != null) {
                    additionalOfferSection.findViewById(R.id.progressBar_additional_offer).setVisibility(View.VISIBLE);
                }

                // Take API Hit
                if (dataJsonObject != null) {
                    getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_DINER_WALLET_AMOUNT, earningsUrl,
                            ApiParams.getDinerWalletAmountParams(restaurantId, dataJsonObject.optString("cityId", "")),
                            this, null, false);
                }
            }
        }
    }

    private ArrayList<String> getProfileImages(JSONArray profileImageJsonArray) {
        ArrayList<String> profiles = new ArrayList<>();
        int arraySize = profileImageJsonArray.length();

        for (int index = 0; index < arraySize; index++) {
            JSONObject profileJsonObject = profileImageJsonArray.optJSONObject(index);

            if (profileJsonObject != null) {
                String profileUrl = profileJsonObject.optString("imageUrl");

                if (!TextUtils.isEmpty(profileUrl)) {
                    profiles.add(profileUrl);
                }
            }
        }

        return profiles;
    }

    // Display Pager
    private void displayPager(JSONObject restDetailJsonObject) {
        // Check Response
        if (restDetailJsonObject != null) {

            // Get Data
            JSONObject dataJsonObject = restDetailJsonObject.optJSONObject("data");

            if (dataJsonObject != null) {

                // Check if Restaurant is Discovery
                isDiscovery = (AppConstant.RESTAURANT_TYPE_DISCOVERY.equalsIgnoreCase(
                        com.dineout.recycleradapters.util.AppUtil.getRestaurantType(dataJsonObject.optInt("restaurantType"))));

                boolean hasEvent = dataJsonObject.optBoolean("hasEvent", false);

                // Get Tabs List
                tabFragments = getTabsList(isDiscovery, hasEvent, restDetailJsonObject);

                // Get View Pager Adapter instance
                restaurantDetailTabPager.setTabFragments(tabFragments);

                // Set View Pager
                viewPager.setAdapter(restaurantDetailTabPager);
                viewPager.setOffscreenPageLimit(tabFragments.size());
                viewPager.addOnPageChangeListener(this);

                // set btn cta bh info string
                storeDataForBottomButtonsBg(dataJsonObject);

                // Set Tab on Load
                setTabOnLoad();

                // Attach ViewPager to Tab Layout
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Do Nothing...
    }

    @Override
    public void onPageSelected(int position) {
        if (tabFragments != null && tabFragments.size() > 0) {
            int tabCount = tabFragments.size();
            for (int index = 0; index < tabCount; index++) {
                Fragment fragment = tabFragments.get(index);

                if (index == position && restaurantBundle != null) {
                    if (fragment instanceof RestaurantDetailAllOffersFragment) {
                        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAB,
                                AppConstant.RESTAURANT_TAB_RESERVATION);

                        inflateDataForBottomButtonsBg(false, getArguments().getString(RESPONSE_INFO_STRING));
//                        showHideReserveTableButtonForEventTab(false);

                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        if(!TextUtils.isEmpty(restaurantCategory)) {
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType+"_"+restaurantCategory,"RestaurantReservationTab",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME) +"_"+restaurantId,hMap);
                        }else{
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType,"RestaurantReservationTab",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME) +"_"+restaurantId,hMap);
                        }
                        break;

                    } else if (fragment instanceof RestaurantDetailEventPageFragment) {
                        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAB,
                                AppConstant.RESTAURANT_TAB_EVENT);

                        inflateDataForBottomButtonsBg(true, getArguments().getString(RESPONSE_INFO_STRING));

                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        if(!TextUtils.isEmpty(restaurantCategory)) {
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType+"_"+restaurantCategory,"RestaurantEventsTab",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME) +"_"+restaurantId,hMap);
                        }else{
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType,"RestaurantEventsTab",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME) +"_"+restaurantId,hMap);
                        }

                        // track event for qgraph, apsalar and branch
                        HashMap<String, Object> props = new HashMap<>();
                        props.put("restaurantName",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
                        props.put("restID",restaurantId);
                        props.put("area",dataJsonObject.optString("areaName"));
                        props.put("cuisinesList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
                        props.put("tagsList", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
                        props.put("DeepLinkURL", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
                        trackEventQGraphApsalar("RestaurantEventsTab",props,true,false,false);

                        break;

                    } else if (fragment instanceof RestaurantDetailInfoPageFragment) {
                        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAB,
                                AppConstant.RESTAURANT_TAB_INFO);

                        inflateDataForBottomButtonsBg(false, getArguments().getString(RESPONSE_INFO_STRING));


                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        if(!TextUtils.isEmpty(restaurantCategory)) {
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType+"_"+restaurantCategory,"RestaurantInfoTab", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID),hMap);
                        }else{
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType,"RestaurantInfoTab", restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID),hMap);
                        }
                        break;

                    } else if (fragment instanceof RestaurantDetailReviewsPageFragment) {
                        restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAB,
                                AppConstant.RESTAURANT_TAB_REVIEW);

                        inflateDataForBottomButtonsBg(false, getArguments().getString(RESPONSE_INFO_STRING));

                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        if(!TextUtils.isEmpty(restaurantCategory)) {
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType+"_"+ restaurantCategory,"RestaurantReviewsTab",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID),hMap);
                        }else{
                            trackEventForCountlyAndGA("D_RestaurantDetail"+"_"+restaurantType,"RestaurantReviewsTab",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID),hMap);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Do Nothing...
    }

    private void showHideReserveTableButtonForEventTab(boolean isEventTab) {
        if (apiShowReserveTable) {
            if (isEventTab) {
                mBottomButtonsWrapper.setVisibility(View.GONE);

            } else {
                mBottomButtonsWrapper.setVisibility((isDiscovery) ? View.GONE : View.VISIBLE);
            }
        } else {
            mBottomButtonsWrapper.setVisibility(View.GONE);
        }
    }

    private void setTabOnLoad() {
        if (restaurantBundle != null) {
            // Get Tab
            String tab = restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAB, "");

            // Get Tab Position
            int tabPosition = getFragmentPosition(tab);

            // Set View Pager on Tab
            viewPager.setCurrentItem(tabPosition);

            // Set Reserve Table Text
            // Check if push is for Event Tab and Fragments list holds Event Tab only then set it as TRUE
            boolean hasEventTabSelected = (tab.equalsIgnoreCase(AppConstant.RESTAURANT_TAB_EVENT) &&
                    tabPosition >= 0);

            // inflate bottom buttons data
            inflateDataForBottomButtonsBg(hasEventTabSelected, getArguments().getString(RESPONSE_INFO_STRING));
//            showHideReserveTableButtonForEventTab(hasEventTabSelected);
        }
    }

    // set btn cta bh info string
    private void storeDataForBottomButtonsBg(final JSONObject infoObj) {
        if (getArguments() != null && infoObj != null) {
            getArguments().putString(RESPONSE_INFO_STRING, infoObj.toString());
        }
    }

    // inflate bottom buttons data
    private void inflateDataForBottomButtonsBg(boolean isEventTab, String infoString) {
//        if (apiShowReserveTable) {
            if (isEventTab) {
                mBottomButtonsWrapper.setVisibility(View.GONE);

            } else {
//                mBottomButtonsWrapper.setVisibility((isDiscovery) ? View.GONE : View.VISIBLE);

                try {
                    final JSONObject infoObj = new JSONObject(infoString);

                    // set cta header text
                    String ctaHeaderText = infoObj.optString("ctaHeader", "");
                    if (TextUtils.isEmpty(ctaHeaderText)) {
                        mCTAHeaderText.setVisibility(View.GONE);

                    } else {
                        mCTAHeaderText.setVisibility(View.VISIBLE);
                        mCTAHeaderText.setText(ctaHeaderText);
                    }

                    // set text
                    JSONArray array = infoObj.optJSONArray("cta");
                    if (array != null) {
                        mBottomButtonsWrapper.setVisibility(View.VISIBLE);

                        if (array.length() > 0) {
                            mBottomLeftBtnWrapper.setVisibility(View.VISIBLE);

                            final JSONObject ctaObj = array.getJSONObject(0);
                            if (ctaObj != null) {
                                String colorString = ctaObj.optString("ctaTextColor", "");
                                if (!colorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                                    colorString = "#FFFFFF";
                                }
                                int textColor = Color.parseColor(colorString);

                                // set left button title text color
                                mLeftBtnTitle.setTextColor(textColor);

                                // title
                                String titleText = ctaObj.optString("button_text");
                                if (TextUtils.isEmpty(titleText)) {
                                    mLeftBtnTitle.setText("");
                                    mLeftBtnTitle.setVisibility(View.GONE);
                                } else {
                                    mLeftBtnTitle.setText(titleText);
                                    mLeftBtnTitle.setVisibility(View.VISIBLE);
                                }

                                // set left button sub title text color
                                mLeftBtnSubTitle.setTextColor(textColor);

                                // sub title
                                String subTitleText = ctaObj.optString("subtitle_button_text");
                                if (TextUtils.isEmpty(subTitleText)) {
                                    mLeftBtnSubTitle.setText("");
                                    mLeftBtnSubTitle.setVisibility(View.GONE);
                                } else {
                                    mLeftBtnSubTitle.setText(subTitleText);
                                    mLeftBtnSubTitle.setVisibility(View.VISIBLE);
                                }

                                // set bg
                                String bgColorString = ctaObj.optString("ctaColor", "");
                                if (!bgColorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                                    bgColorString = "#fa5757";
                                }
                                int bgColor = Color.parseColor(bgColorString);
                                GradientDrawable leftShape = (GradientDrawable) mBottomLeftBtnWrapper.getBackground();
                                leftShape.setColor(bgColor);

                                // enable/disable
                                boolean isEnabled = ctaObj.optInt("enable") == 1;
                                mBottomLeftBtnWrapper.setEnabled(isEnabled);

                                // tag object
                                mBottomLeftBtnWrapper.setTag(infoObj);

                                // set onclick
                                mBottomLeftBtnWrapper.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int action = ctaObj.optInt("action");
                                        if (action == 2) {
                                            //pay now
                                            onPayBill(infoObj);
                                        } else if (action == 4) {
                                            //upload
                                            onUploadBillClicked(infoObj);
                                        }  else if (action == 19) {
                                            // reservation click
                                            handleReserveTableClick();
                                        }
                                    }
                                });
                            }
                        } else {
                            mBottomLeftBtnWrapper.setVisibility(View.GONE);
                            mLeftBtnTitle.setVisibility(View.GONE);
                            mLeftBtnSubTitle.setVisibility(View.GONE);
                        }

                        if (array.length() > 1) {
                            mBottomRightBtnWrapper.setVisibility(View.VISIBLE);

                            final JSONObject ctaObj = array.getJSONObject(1);
                            if (ctaObj != null) {
                                String colorString = ctaObj.optString("ctaTextColor", "");
                                if (!colorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                                    colorString = "#FFFFFF";
                                }
                                int textColor = Color.parseColor(colorString);

                                // set right button title text color
                                mRightBtnTitle.setTextColor(textColor);

                                // title
                                String titleText = ctaObj.optString("button_text");
                                if (TextUtils.isEmpty(titleText)) {
                                    mRightBtnTitle.setText("");
                                    mRightBtnTitle.setVisibility(View.GONE);
                                } else {
                                    mRightBtnTitle.setText(titleText);
                                    mRightBtnTitle.setVisibility(View.VISIBLE);
                                }

                                // set right button sub title text color
                                mRightBtnSubTitle.setTextColor(textColor);

                                // sub title
                                String subTitleText = ctaObj.optString("subtitle_button_text");
                                if (TextUtils.isEmpty(subTitleText)) {
                                    mRightBtnSubTitle.setText("");
                                    mRightBtnSubTitle.setVisibility(View.GONE);
                                } else {
                                    mRightBtnSubTitle.setText(subTitleText);
                                    mRightBtnSubTitle.setVisibility(View.VISIBLE);
                                }

                                // set bg
                                String bgColorString = ctaObj.optString("ctaColor", "");
                                if (!bgColorString.matches(com.dineout.recycleradapters.util.AppUtil.VALID_COLOR_REGEX)) {
                                    bgColorString = "#fa5757";
                                }
                                int bgColor = Color.parseColor(bgColorString);
                                GradientDrawable rightShape = (GradientDrawable) mBottomRightBtnWrapper.getBackground();
                                rightShape.setColor(bgColor);

                                // enable/disable
                                boolean isEnabled = ctaObj.optInt("enable") == 1;
                                mBottomRightBtnWrapper.setEnabled(isEnabled);

                                // tag object
                                mBottomRightBtnWrapper.setTag(infoObj);

                                // set onclick
                                mBottomRightBtnWrapper.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int action = ctaObj.optInt("action");
                                        if (action == 2) {
                                            //pay now
                                            onPayBill(infoObj);
                                        } else if (action == 4) {
                                            //upload
                                            onUploadBillClicked(infoObj);
                                        }  else if (action == 19) {
                                            // reservation click
                                            handleReserveTableClick();
                                        }
                                    }
                                });
                            }
                        } else {
                            mBottomRightBtnWrapper.setVisibility(View.GONE);
                            mRightBtnTitle.setVisibility(View.GONE);
                            mRightBtnSubTitle.setVisibility(View.GONE);
                        }

                    } else {
                        mBottomButtonsWrapper.setVisibility(View.GONE);
                        mCTAHeaderText.setVisibility(View.GONE);
                        mBottomLeftBtnWrapper.setVisibility(View.GONE);
                        mBottomRightBtnWrapper.setVisibility(View.GONE);
                        mLeftBtnTitle.setVisibility(View.GONE);
                        mRightBtnTitle.setVisibility(View.GONE);
                        mLeftBtnSubTitle.setVisibility(View.GONE);
                        mRightBtnSubTitle.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    // Exception
                }
            }
//        } else {
//            mBottomButtonsWrapper.setVisibility(View.GONE);
//        }
    }

    private void handleReserveTableClick() {
        // Check for NULL
        if (dataJsonObject == null)
            return;

        String restaurantName= restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME);

        // Track Event
        //trackEventGA("Details", getString(R.string.ga_action_reserve_table), null);
        //trackEventGA("D_Listing", "RestaurantReservationCTAClick", restaurantId);
        try {
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("restID",restaurantId);
            }

            if(!TextUtils.isEmpty(restaurantCategory)) {
                trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail) + "_" + restaurantType + "_" + restaurantCategory,
                        getString(R.string.d_restaurant_reservations_click),
                        restaurantName+ "_"+restaurantId, hMap);
            }else{
                trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail) + "_" + restaurantType, getString(R.string.d_restaurant_reservations_click),
                        restaurantName+ "_"+restaurantId, hMap);


            }
        } catch (Exception e) {
            // Exception
        }

        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, restaurantId);

        JSONArray extraInfoJsonArray = dataJsonObject.optJSONArray("extraInfo");
        if (extraInfoJsonArray != null && extraInfoJsonArray.length() > 0) {
            bundle.putString(AppConstant.BUNDLE_EXTRA_INFO, extraInfoJsonArray.toString()); // Extra Info
        }

        bundle.putInt(AppConstant.BUNDLE_USER_EARNINGS, walletAmount);
        bundle.putDouble("COSTFORTWO", dataJsonObject.optDouble("costForTwo"));


        // Booking Time Slot
        proceedSelectBookingSlot(bundle);
    }

    public void onUploadBillClicked(JSONObject obj) {
        // resId
        String resId = (obj == null) ? "" : obj.optString("restaurantId");
        String resName = (obj == null) ? "" : obj.optString("profileName");

        //upload bill click

        HashMap<String,String>map=DOPreferences.getGeneralEventParameters(getContext());
        if(map!=null){
            map.put("restID",obj.optString("restaurantId"));
        }

        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType, "RestaurantUploadBillCTAClick", resName+"_"+resId,map);

        UploadBillController utility = new UploadBillController(getNetworkManager(), getActivity(), obj.optString("restaurantId"));
        utility.setPreviousScreenName(getString(R.string.ga_screen_listing));
        utility.validate();
    }

    public void onPayBill(JSONObject obj) {
        // resId
        String resId = (obj == null) ? "" : obj.optString("restaurantId");
        String resName = (obj == null) ? "" : obj.optString("profileName");

        // Track Pay Bill Click - TODO in next release
        HashMap<String,String>map=DOPreferences.getGeneralEventParameters(getContext());
        if(map!=null){
            map.put("resId",obj.optString("r_id"));
        }

        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType, "RestaurantPaymentCTAClick", resName+"_"+resId,map);

        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) { // User is NOT logged In
            UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, null);
        } else {
            handlePayBill(obj);
        }
    }

    private void handlePayBill(JSONObject obj) {
        if (obj != null) {
            Bundle bundle = new Bundle();
            bundle.putString(PaymentConstant.RESTAURANT_NAME, obj.optString("profileName"));
            bundle.putString(PaymentConstant.RESTAURANT_ID, obj.optString("restaurantId"));
            bundle.putString(PaymentConstant.BOOKING_ID, obj.optString("restaurantId"));
            bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.RESTAURANT_TYPE);

            // Track Event
//            AnalyticsHelper.getAnalyticsHelper(getActivity().getApplicationContext()).trackEventGA(
//                    getString(R.string.ga_screen_my_bookings),
//                    getString(R.string.ga_action_pay_now_upcoming_card), null);

            PaymentUtils.initiatePayment(getActivity(), bundle, getNetworkManager());

        } else {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), getString(R.string.paid_restaurant));
        }
    }



    private int getFragmentPosition(String tab) {
        int position = -1;

        if (!AppUtil.isStringEmpty(tab) && tabFragments != null && tabFragments.size() > 0) {
            int tabCount = tabFragments.size();
            for (int index = 0; index < tabCount; index++) {
                Fragment fragment = tabFragments.get(index);

                if (fragment instanceof RestaurantDetailAllOffersFragment &&
                        tab.equalsIgnoreCase(AppConstant.RESTAURANT_TAB_RESERVATION)) {
                    position = index;
                    break;

                } else if (fragment instanceof RestaurantDetailEventPageFragment &&
                        tab.equalsIgnoreCase(AppConstant.RESTAURANT_TAB_EVENT)) {
                    position = index;
                    break;

                } else if (fragment instanceof RestaurantDetailInfoPageFragment &&
                        tab.equalsIgnoreCase(AppConstant.RESTAURANT_TAB_INFO)) {
                    position = index;
                    break;

                } else if (fragment instanceof RestaurantDetailReviewsPageFragment &&
                        tab.equalsIgnoreCase("review")) {
                    position = index;
                    break;
                }
            }
        }

        return position;
    }

    private ArrayList<MasterDOFragment> getTabsList(
            boolean isDiscovery, boolean hasEvent, JSONObject restDetailJsonObject) {
        ArrayList<MasterDOFragment> tabFragments = new ArrayList<>();

        if (isDiscovery) {
            if (hasEvent) {
                tabFragments.add(getEventPageFragment());
                tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.text_rest_detail_event_page)));
            }
        } else {
            tabFragments.add(getReservationPageFragment(restDetailJsonObject));
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.text_rest_detail_reservation_page)));

            if (hasEvent) {
                tabFragments.add(getEventPageFragment());
                tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.text_rest_detail_event_page)));
            }
        }

        // Add Tabs
        tabFragments.add(getInfoPageFragment());
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.text_rest_detail_info_page)));

        tabFragments.add(getReviewsPageFragment());
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.text_rest_detail_reviews_page)));

        return tabFragments;
    }

    private RestaurantDetailAllOffersFragment getReservationPageFragment(JSONObject restDetailJsonObject) {
        // Get Instance
        RestaurantDetailAllOffersFragment reservationPageFragment =
                RestaurantDetailAllOffersFragment.getInstance(restDetailJsonObject);

        reservationPageFragment.setChildFragment(true);

        // Set Bundle
        reservationPageFragment.setArguments(restaurantBundle);

        return reservationPageFragment;
    }

    private RestaurantDetailEventPageFragment getEventPageFragment() {
        // Get Instance
        eventPageFragment = new RestaurantDetailEventPageFragment();

        // Set as Child Fragment
        eventPageFragment.setChildFragment(true);

        // Set Bundle
        eventPageFragment.setArguments(restaurantBundle);

        return eventPageFragment;
    }

    private RestaurantDetailInfoPageFragment getInfoPageFragment() {
        // Get Instance
        RestaurantDetailInfoPageFragment infoPageFragment =
                new RestaurantDetailInfoPageFragment();

        infoPageFragment.setChildFragment(true);

        // Set Bundle
        infoPageFragment.setArguments(restaurantBundle);

        return infoPageFragment;
    }

    private RestaurantDetailReviewsPageFragment getReviewsPageFragment() {
        // Get Instance
        RestaurantDetailReviewsPageFragment reviewsPageFragment =
                new RestaurantDetailReviewsPageFragment();

        reviewsPageFragment.setChildFragment(true);

        // Set Bundle
        reviewsPageFragment.setArguments(restaurantBundle);

        return reviewsPageFragment;
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Get Restaurant Detail from API
        getRestaurantDetailFromAPI();
    }

    public void switchToEventTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1); // Reservation | Events | Info | Reviews
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeAllChildFragments();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        restaurantId = null;
        restaurantBundle = null;
        tabLayout = null;
        viewPager = null;
        collapsingToolbar = null;
        dataJsonObject = null;
        tabFragments = null;
        restaurantDetailTabPager = null;
        navigationListener = null;
        additionalOfferSection = null;
        eventPageFragment = null;
    }

    private void removeAllChildFragments() {

        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        if (childFragments != null && !childFragments.isEmpty()) {
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getChildFragmentManager().beginTransaction();
            for (Fragment childFragment : childFragments) {
                if (childFragment != null) {

                    fragmentTransaction.remove(childFragment);

                }
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    /**
     * API Handlers
     */
    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAIL_RESERVATION) {
            // Process Response
            if (responseObject != null && responseObject.optBoolean("status")) {
                if (responseObject.optJSONObject("data") != null &&
                        responseObject.optJSONObject("data").optJSONArray("stream") != null &&
                        responseObject.optJSONObject("data").optJSONArray("stream").length() > 0) {

                    JSONObject restDetailJsonObject = responseObject.optJSONObject("data").optJSONArray("stream").optJSONObject(0);

                    if (restDetailJsonObject != null) {

                        // Display Restaurant Head Details
                        dataJsonObject = restDetailJsonObject.optJSONObject("data");

                        restaurantType = com.dineout.recycleradapters.util.AppUtil.getRestaurantTypeForAnalytics(dataJsonObject.optInt("restaurantType"));

                        isSmartPay   =  dataJsonObject.optBoolean("isSmartPay");
                        isUploadBill = dataJsonObject.optBoolean("isUploadBill");

                        if(isSmartPay){
                            restaurantCategory="SmartPay";
                        }else if(isUploadBill){
                            restaurantCategory="billUpload";
                        }

                        if(restaurantBundle!=null){
                            restaurantBundle.putString("RESTAURANT_CATEGORY",restaurantCategory);
                            restaurantBundle.putString("RESTAURANT_TYPE",restaurantType);
                        }

                        // Display Restaurant Details in Header
                        displayRestaurantHeadDetails(dataJsonObject);


                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        if(hMap!=null) {
                            hMap.put("restID",restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""));
                        }

                        if(!TextUtils.isEmpty(restaurantCategory)){
                            trackEventForCountlyAndGA(getContext().getResources().getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory,
                                    getContext().getResources().getString(R.string.d_restaurant_reservation_click),restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME + "_"+restaurantId),hMap);

                        }else{
                            trackEventForCountlyAndGA(getContext().getResources().getString(R.string.countly_reaturant_detail)+"_"+restaurantType,
                                    getContext().getResources().getString(R.string.d_restaurant_reservation_click),restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME + "_"+restaurantId),hMap);

                        }


                        // Display Pager
                        displayPager(restDetailJsonObject);



                    }
                }
            }
        } else if (request.getIdentifier() == REQUEST_CODE_GET_DINER_WALLET_AMOUNT) {
            // Check for NULL
            if (additionalOfferSection != null) {
                additionalOfferSection.findViewById(R.id.progressBar_additional_offer).setVisibility(View.GONE);
            }

            if (responseObject != null && responseObject.optBoolean("status")) {
                // Get Output Params
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");
                if (outputParamsJsonObject != null) {
                    // Get Data
                    JSONArray dataJsonArray = outputParamsJsonObject.optJSONArray("data");
                    if (dataJsonArray != null && dataJsonArray.length() > 0) {
                        JSONObject dataJsonObject = dataJsonArray.optJSONObject(0);

                        if (dataJsonObject != null) {
                            // Get Wallet Amount
                            walletAmount = dataJsonObject.optInt("wallet_amount");

                            // Check for NULL
                            if (additionalOfferSection != null) {
                                TextView textViewUserEarnings = (TextView) additionalOfferSection.findViewById(R.id.textView_user_earnings);
                                View relativeLayoutRestUserEarningsSection = additionalOfferSection.findViewById(R.id.relativeLayout_rest_user_earnings_section);

                                String earningsText = dataJsonObject.optString("title1", "");
                                if (!AppUtil.isStringEmpty(earningsText)) {
                                    earningsText = earningsText.replaceAll("##", "");
                                    earningsText = com.dineout.recycleradapters.util.AppUtil.renderRupeeSymbol(earningsText).toString();
                                    textViewUserEarnings.setText(earningsText);
                                    textViewUserEarnings.setVisibility(View.VISIBLE);
                                    relativeLayoutRestUserEarningsSection.setVisibility(View.VISIBLE);
                                }
                            }

                            // Set User Wallet Amount in JSON
                            try {
                                this.dataJsonObject.put(AppConstant.BUNDLE_USER_EARNINGS, walletAmount);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        if (getView() == null || getActivity() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_CODE_GET_DINER_WALLET_AMOUNT) {
            // Check for NULL
            if (additionalOfferSection != null) {
                additionalOfferSection.findViewById(R.id.progressBar_additional_offer).setVisibility(View.GONE);
            }
        }

//        // Show Reserve Button
//        buttonRestaurantReserve.setVisibility(View.VISIBLE);
    }

//    private class BookingLoginCallback implements UserAuthenticationController.LoginFlowCompleteCallbacks {
//
//        public static final int BOOKING = 0x01;
//        private Bundle mBundle;
//        private int mType;
//
//        public BookingLoginCallback(Bundle bundle, int type) {
//            mBundle = bundle;
//            mType = type;
//        }
//
//        @Override
//        public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
//
//        }
//
//        @Override
//        public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
//
//        }
//    }

    /**
     * Restaurant Detail Tab Pager
     */
    private class RestaurantDetailTabPager extends FragmentStatePagerAdapter {
        private ArrayList<MasterDOFragment> tabFragments = new ArrayList<>();

        public RestaurantDetailTabPager(FragmentManager fm) {
            super(fm);
        }

        public void setTabFragments(ArrayList<MasterDOFragment> tabFragments) {
            this.tabFragments = tabFragments;
        }

        @Override
        public Fragment getItem(int position) {
            if (tabFragments != null && !tabFragments.isEmpty() && tabFragments.size() > position) {
                return tabFragments.get(position);
            }

            return null;
        }

        @Override
        public int getCount() {
            return ((tabFragments == null || tabFragments.isEmpty()) ? 0 : tabFragments.size());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (tabFragments != null && !tabFragments.isEmpty() && tabFragments.size() > position) {
                MasterDOFragment fragment = tabFragments.get(position);

                if (fragment instanceof RestaurantDetailEventPageFragment) {
                    return getString(R.string.text_rest_detail_event_page);

                } else if (fragment instanceof RestaurantDetailAllOffersFragment) {
                    return getString(R.string.text_rest_detail_reservation_page);

                } else if (fragment instanceof RestaurantDetailInfoPageFragment) {
                    return getString(R.string.text_rest_detail_info_page);

                } else if (fragment instanceof RestaurantDetailReviewsPageFragment) {
                    return getString(R.string.text_rest_detail_reviews_page);
                }
            }

            return "";
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}