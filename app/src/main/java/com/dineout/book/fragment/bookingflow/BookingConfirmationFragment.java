package com.dineout.book.fragment.bookingflow;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.mybookings.BookingDetailsFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.dialogs.MoreDialog;
import com.dineout.recycleradapters.MaleSelectorAdapter;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.ImageRequestManager;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_EXTRA_INFO;
import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_OFFER_JSON;

public class BookingConfirmationFragment extends AuthenticationWrapperJSONReqFragment
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, MaleSelectorAdapter.OnItemClickListener {

    private final int REQUEST_CODE_VALIDATE_JP_NUMBER = 100;
    private final int REQUEST_CODE_CONFIRM_TABLE = 101;
    private final int REQUEST_CODE_EDIT_BOOKING = 103;
    private final int REQUEST_CODE_RESTAURANT_DETAIL_EXTRA_INFO = 104;

    private Bundle arguments;
    private AppEventsLogger logger;
    private boolean isEditBooking;

    private LinearLayout linearLayoutConfirmTableContainer;
    private EditText editTextGuestName;
    private EditText editTextGuestEmailAddress;
    private EditText editTextGuestPhoneNumber;
    private EditText editTextGuestSpecialRequest;
    private Button buttonConfirmTable;

    private ProgressBar progressBarJpAction;
    private EditText editTextJpMilesNumber;
    private ImageView jpActionButton;
    private TextView textViewJpMessage;
    private View jpActionSection;
    private boolean isJpMilesNumberVerified;
    private boolean isJpMilesNumberVerificationInProcess;
    private JSONArray extraInfoJsonArray;
    private MaleSelectorAdapter maleAdapter;
    private MaleSelectorAdapter femaleAdapter;
    private String maleCount, femaleCount;
    private String offerTitle;

    private RelativeLayout goNowLayout;
    private TextView goNowTitleTxt;
    private TextView goNowSubTitleTxt;
    private NetworkImageView waitingIcon;

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public JSONArray getExtraInfoJsonArray() {
        return extraInfoJsonArray;
    }

    public void setExtraInfoJsonArray(JSONArray extraInfoJsonArray) {
        this.extraInfoJsonArray = extraInfoJsonArray;
    }

    @Override
    public void onMaleItemClick(String item) {

        //track
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),getString(R.string.d_event_male_click),
                item,hMap);

        maleCount = item;
    }

    @Override
    public void onFemaleItemClick(String item) {

        //track
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),getString(R.string.d_event_female_click),
                item,hMap);

        femaleCount = item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for Pref Change
        DOPreferences.getSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);

        String[] guestCountList = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};

        maleAdapter = new MaleSelectorAdapter(guestCountList, "male");
        femaleAdapter = new MaleSelectorAdapter(guestCountList, "female");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_confirmation, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
       // trackScreenToGA(getString(R.string.ga_screen_confirm_booking));

        trackScreenName(getString(R.string.countly_booking_confirmation));

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());

        // Get Bundle
        arguments = getArguments();
        if (arguments == null)
            arguments = new Bundle();

        // Set Offer Title
        JSONObject selectedOfferJsonObject = new JSONObject();
        try {
            String offerJsonString = arguments.getString(BUNDLE_OFFER_JSON);
            if (!AppUtil.isStringEmpty(offerJsonString)) {
                selectedOfferJsonObject = new JSONObject(offerJsonString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        offerTitle = selectedOfferJsonObject.optString("title_2", "");

        // Set Extra Info
        JSONArray extraInfoJsonArray = null;
        try {
            String extraInfoString = arguments.getString(BUNDLE_EXTRA_INFO);

            if (!AppUtil.isStringEmpty(extraInfoString)) {
                extraInfoJsonArray = new JSONArray(extraInfoString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setExtraInfoJsonArray(extraInfoJsonArray);

        // Initialize View
        initializeView();

        // Initialize Data
        initializeData();

        // Track Event
        trackBookingInitiatedEvent();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Activate Logging (Facebook)
        //AppEventsLogger.activateApp(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        // De-activate App Event Logger
        //AppEventsLogger.deactivateApp(getActivity());
    }

    private void initializeView() {
        // Initialize logger
        logger = AppEventsLogger.newLogger(getActivity());

        // Get Back
        ImageView imageViewAllOfferBack = (ImageView) getView().findViewById(R.id.imageView_all_offer_back);
        imageViewAllOfferBack.setOnClickListener(this);

        // Set Title
        TextView textViewTitle = (TextView) getView().findViewById(R.id.textView_all_offer_title);
        textViewTitle.setText(getBookingDateTime());

        // Set Sub Title
        TextView textViewSubTitle = (TextView) getView().findViewById(R.id.textView_all_offer_sub_title);
        if (AppUtil.isStringEmpty(offerTitle)) {
            textViewSubTitle.setVisibility(View.GONE);

        } else {
            textViewSubTitle.setText(offerTitle);
            textViewSubTitle.setVisibility(View.VISIBLE);
        }

        // Get Edit Booking Flag
        isEditBooking = arguments.getBoolean(AppConstant.BUNDLE_IS_EDIT_BOOKING, false);

        //set male and female count
        maleCount = Integer.toString(arguments.getInt(AppConstant.BUNDLE_MALE_COUNT, 0));
        femaleCount = Integer.toString(arguments.getInt(AppConstant.BUNDLE_FEMALE_COUNT, 0));

        // Track Screen
        if (isEditBooking) {
            //trackEventGA(getString(R.string.confirm_edit_booking), null);
        }

        // Get Info
        ImageView imageViewInfo = (ImageView) getView().findViewById(R.id.imageView_info);
        imageViewInfo.setOnClickListener(this);

        RecyclerView maleRecyclerView = (RecyclerView) getView().findViewById(R.id.male_recycler_view);
        RecyclerView femaleRecyclerView = (RecyclerView) getView().findViewById(R.id.female_recycler_view);
        LinearLayoutManager horizontalLayoutManagerMale = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager horizontalLayoutManagerFemale = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        maleRecyclerView.setLayoutManager(horizontalLayoutManagerMale);
        maleAdapter.setItemClickListener(this);
        maleRecyclerView.setAdapter(maleAdapter);

        femaleRecyclerView.setLayoutManager(horizontalLayoutManagerFemale);
        femaleAdapter.setItemClickListener(this);
        femaleRecyclerView.setAdapter(femaleAdapter);

        // Get Container
        linearLayoutConfirmTableContainer = (LinearLayout) getView().findViewById(R.id.linearLayout_confirm_table_container);

        // Set Guest Count in case of Edit Booking
        if (isEditBooking) {
            maleAdapter.setMaleSelection(arguments.getInt(AppConstant.BUNDLE_MALE_COUNT, -1));
            femaleAdapter.setFemaleSelection(arguments.getInt(AppConstant.BUNDLE_FEMALE_COUNT, -1));

            if (arguments.getInt(AppConstant.BUNDLE_MALE_COUNT) > horizontalLayoutManagerMale.findLastVisibleItemPosition()) {
                maleRecyclerView.scrollToPosition(arguments.getInt(AppConstant.BUNDLE_MALE_COUNT) - 2);
            }

            if (arguments.getInt(AppConstant.BUNDLE_MALE_COUNT) > horizontalLayoutManagerFemale.findLastVisibleItemPosition()) {
                femaleRecyclerView.scrollToPosition(arguments.getInt(AppConstant.BUNDLE_FEMALE_COUNT) - 2);
            }
        }

        // Set Diner Name
        editTextGuestName = (EditText) getView().findViewById(R.id.editText_guest_name);
        String dinerName = (DOPreferences.getDinerFirstName(getActivity().getApplicationContext()) + " "
                + DOPreferences.getDinerLastName(getActivity().getApplicationContext()));

        if (!TextUtils.isEmpty(arguments.getString(AppConstant.BUNDLE_GUEST_NAME))) {
            editTextGuestName.setText(arguments.getString(AppConstant.BUNDLE_GUEST_NAME));
        } else if (!TextUtils.isEmpty(dinerName)) {
            editTextGuestName.setText(dinerName);
        }

        // Set Diner Email Address
        editTextGuestEmailAddress = (EditText) getView().findViewById(R.id.editText_guest_email_address);
        String dinerEmail = DOPreferences.getDinerEmail(getActivity().getApplicationContext());

        if (!TextUtils.isEmpty(arguments.getString(AppConstant.BUNDLE_GUEST_EMAIL))) {
            editTextGuestEmailAddress.setText(arguments.getString(AppConstant.BUNDLE_GUEST_EMAIL));
        } else if (!TextUtils.isEmpty(dinerEmail)) {
            editTextGuestEmailAddress.setText(dinerEmail);
        }

        editTextGuestPhoneNumber = (EditText) getView().findViewById(R.id.editText_guest_phone_number);

        /*// Set Diner Phone Number
        editTextGuestPhoneNumber = (EditText) getView().findViewById(R.id.editText_guest_phone_number);
        String dinerPhone = DOPreferences.getDinerPhone(getActivity().getApplicationContext());

        if (!TextUtils.isEmpty(arguments.getString(AppConstant.BUNDLE_GUEST_PHONE))) {
            editTextGuestPhoneNumber.setText(arguments.getString(AppConstant.BUNDLE_GUEST_PHONE));
        } else if (!TextUtils.isEmpty(dinerPhone)) {
            editTextGuestPhoneNumber.setText(dinerPhone);
        }*/

        // Get Special Request
        editTextGuestSpecialRequest = (EditText) getView().findViewById(R.id.editText_guest_special_request);

        if (!TextUtils.isEmpty(arguments.getString(AppConstant.BUNDLE_SPECIAL_REQUEST))) {
            editTextGuestSpecialRequest.setText(arguments.getString(AppConstant.BUNDLE_SPECIAL_REQUEST));
        }

        // Get JP Miles Know More
        TextView textViewJpMilesKnowMore = (TextView) getView().findViewById(R.id.text_jp_know_more);
        if (TextUtils.isEmpty(DOPreferences.getJpMilesKnowMoreLink(getActivity().getApplicationContext()))) {
            textViewJpMilesKnowMore.setVisibility(View.GONE);
        } else {
            // Set Click Event on More Info
            textViewJpMilesKnowMore.setOnClickListener(this);
        }

        // Get Confirm Table Instance
        buttonConfirmTable = (Button) getView().findViewById(R.id.button_confirm_table);
        if (arguments.getBoolean("hasRightNowSection") && arguments.getInt("timeSlotSectionPos") == 0) {
            buttonConfirmTable.setText(getResources().getString(R.string.go_now_text));
        } else {
            buttonConfirmTable.setText(getResources().getString(R.string.table_confirm_text));
        }

        buttonConfirmTable.setOnClickListener(this);

        // Show Extra Info
        if (!isEditBooking ||
                (getExtraInfoJsonArray() != null && getExtraInfoJsonArray().length() > 0)) {
            showExtraInfo();
        } else {
            getExtraInfoFromAPI();
        }

        // Update UI for Edit Booking
        updateUIForEditBooking();


        goNowLayout =  (RelativeLayout) getView().findViewById(R.id.go_now_layout);
        goNowTitleTxt= (TextView) getView().findViewById(R.id.title_txt);
        goNowSubTitleTxt= (TextView) getView().findViewById(R.id.sub_title_txt);
        waitingIcon=(NetworkImageView)getView().findViewById(R.id.waiting_icon);

        showGoNowGoLaterLayout();
    }

    //show go now, go later layout
    private void showGoNowGoLaterLayout(){

        boolean hasRightNowSection = getArguments().getBoolean("hasRightNowSection");
        int position = getArguments().getInt("timeSlotSectionPos");

        String selectedTime = getArguments().getString(AppConstant.BUNDLE_DISPLAY_TIME);
        if (hasRightNowSection && position==0 && !TextUtils.isEmpty(DOPreferences.getGoNowTitle(getContext()))) {

            //String timein12Format="";
            goNowLayout.setVisibility(View.VISIBLE);
            String goNowTitle=DOPreferences.getGoNowTitle(getContext());

//            try {
//                final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
//                final Date dateObj = sdf.parse(selectedTime );
//                timein12Format=new SimpleDateFormat("K:mm a").format(dateObj);
//
//            } catch (final ParseException e) {
//                e.printStackTrace();
//            }

            if(!TextUtils.isEmpty(goNowTitle)) {
                goNowTitle = goNowTitle.replace("#timeslot#", selectedTime);
            }

            goNowTitleTxt.setText(goNowTitle);
            goNowSubTitleTxt.setText(DOPreferences.getGONowSubtitle(getContext()));

            if(!TextUtils.isEmpty(DOPreferences.getGoNowIconUrl(getContext()))){
                waitingIcon.setImageUrl(DOPreferences.getGoNowIconUrl(getContext()),
                        ImageRequestManager.getInstance(getContext()).getImageLoader());
            }

        }else if(!TextUtils.isEmpty(DOPreferences.getGoLaterTitle(getContext()))){

            String time="";
            goNowLayout.setVisibility(View.VISIBLE);
            String goLaterTitle=DOPreferences.getGoLaterTitle(getContext());

//            try {
//                final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
//                final Date dateObj = sdf.parse(selectedTime );
//                time=new SimpleDateFormat("K:mm a").format(dateObj);
//
//            } catch (final ParseException e) {
//                e.printStackTrace();
//            }

            if(!TextUtils.isEmpty(goLaterTitle)) {
                goLaterTitle = goLaterTitle.replace("#timeslot#", selectedTime);
            }

            goNowTitleTxt.setText(goLaterTitle);
            goNowSubTitleTxt.setText(DOPreferences.getGoLaterSubtitle(getContext()));

            if(TextUtils.isEmpty(DOPreferences.getGoLaterIconUrl(getContext()))){
                waitingIcon.setImageUrl(DOPreferences.getGoLaterIconUrl(getContext()),
                        ImageRequestManager.getInstance(getContext()).getImageLoader());
            }
        }else{
            goNowLayout.setVisibility(View.GONE);
        }
    }

    private String getBookingDateTime() {
        if (arguments != null) {
            return DateTimeSlotUtil.getDateTimeSubTitle(
                    arguments.getString(AppConstant.BUNDLE_SELECTED_DATE, ""),
                    arguments.getString(AppConstant.BUNDLE_SELECTED_TIME, ""));
        }

        return "";
    }

    private void showExtraInfo() {
        // Check if Extra Info is available
        if (getExtraInfoJsonArray() != null && getExtraInfoJsonArray().length() > 0) {
            // Get Extra Info Section
            LinearLayout linearLayoutExtraInfoSection =
                    (LinearLayout) getView().findViewById(R.id.linearLayout_extra_info_section);

            // Get Extra Info Parent Layout
            LinearLayout linearLayoutExtraInfoList =
                    (LinearLayout) linearLayoutExtraInfoSection.findViewById(R.id.linearLayout_extra_info_list);

            int arraySize = getExtraInfoJsonArray().length();
            for (int index = 0; index < arraySize; index++) {
                JSONObject jsonObjectInfo = getExtraInfoJsonArray().optJSONObject(index);

                if (jsonObjectInfo != null) {
                    // Get View
                    View extraInfoView = LayoutInflater.from(getActivity().getApplicationContext()).
                            inflate(com.dineout.recycleradapters.R.layout.extra_info_list_item, null, false);

                    // Set Icon
                    NetworkImageView imageViewInfoIcon = (NetworkImageView) extraInfoView.findViewById(com.dineout.recycleradapters.R.id.imageView_info_icon);
                    if (com.dineout.recycleradapters.util.AppUtil.isStringEmpty(jsonObjectInfo.optString("iconUrl"))) {
                        imageViewInfoIcon.setVisibility(ImageView.GONE);

                    } else {
                        imageViewInfoIcon.setImageUrl(jsonObjectInfo.optString("iconUrl"),
                                ImageRequestManager.getInstance(getActivity().getApplicationContext()).getImageLoader());
                        imageViewInfoIcon.setVisibility(ImageView.VISIBLE);
                    }

                    // Set Text
                    TextView textViewExtraInfoName = (TextView) extraInfoView.findViewById(com.dineout.recycleradapters.R.id.textView_extra_info_name);
                    if (!com.dineout.recycleradapters.util.AppUtil.isStringEmpty(jsonObjectInfo.optString("text"))) {
                        textViewExtraInfoName.setText(jsonObjectInfo.optString("text"));
                    }

                    // Add View to the View
                    linearLayoutExtraInfoList.addView(extraInfoView);
                }
            }

            // Show Extra Info Section
            linearLayoutExtraInfoSection.setVisibility(View.VISIBLE);
        }
    }

    private void getExtraInfoFromAPI() {
        // Take API Hit
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_RESTAURANT_DETAIL_EXTRA_INFO,
                AppConstant.NODE_RESTAURANT_DETAIL_URL,
                ApiParams.getRestaurantDetailsParams(arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""),
                        AppConstant.REST_DETAIL_EXTRA_INFO),
                this, this, true);
    }

    private void updateUIForEditBooking() {
        if (isEditBooking) {
            // Set Title
            setToolbarTitle(R.string.title_edit_booking);

            // Disable Diner Name
            disableEditText(editTextGuestName);

            // Disable Diner Email Address
            disableEditText(editTextGuestEmailAddress);

            // Disable Diner Phone Number
            disableEditText(editTextGuestPhoneNumber);

            // Change Button Name
            buttonConfirmTable.setText(R.string.button_edit_booking);
        }
    }

    private void disableEditText(EditText editText) {
        if (editText != null) {
            editText.setEnabled(false);
        }
    }

    private void initializeData() {
        // Setup JP Miles Section
        setupJPMilesSection();

        // Log Facebook Add to Cart event
        logFacebookAddToCartEvent();
    }

    private void trackBookingInitiatedEvent() {

        trackAdTechEvent("ua", "ResInitiated");
        //completeAdTechSesion();


        HashMap<String, Object> props = new HashMap<>();

        props.put("RestaurantName", arguments.getString(AppConstant.BUNDLE_RESTAURANT_NAME, ""));
        props.put("RestaurantID", arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""));
        props.put("City", arguments.getString(AppConstant.BUNDLE_RESTAURANT_CITY, ""));
        props.put("Area", arguments.getString(AppConstant.BUNDLE_RESTAURANT_AREA, ""));
        props.put("Locality", arguments.getString(AppConstant.BUNDLE_RESTAURANT_LOCALITY, ""));
        props.put("Date", arguments.getString(AppConstant.BUNDLE_SELECTED_DATE, ""));
        props.put("Slot", arguments.getString(AppConstant.BUNDLE_SELECTED_TIME, ""));
        props.put("ResImageURL", arguments.getString(AppConstant.BUNDLE_RESTAURANT_IMAGE_URL, ""));
        props.put("ResDeepLink", arguments.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
        props.put("ResCuisine", arguments.getString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, ""));
        props.put("CuisinesList", arguments.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
        props.put("TagList", arguments.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));

//        Product product = new Product();
//        product.setId(arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""));
//        product.setName(arguments.getString(AppConstant.BUNDLE_RESTAURANT_NAME, ""));
//
//        ProductAction action = new ProductAction(ProductAction.ACTION_CHECKOUT);
//
//        measureProductAction(product, action, getString(R.string.ga_screen_confirm_booking));

        //trackEventGA(getString(R.string.push_label_booking_initiated), props);
    }

    // Setup JP Miles Section
    private void setupJPMilesSection() {
        if (DOPreferences.isJpMilesEnabled(getActivity().getApplicationContext())) {

            // Check Know More Link
            if (TextUtils.isEmpty(DOPreferences.getJpMilesKnowMoreLink(getActivity().getApplicationContext()))) {
                getView().findViewById(R.id.text_jp_know_more).setVisibility(View.GONE);

            } else {
                // Set Click Event on More Info
                getView().findViewById(R.id.text_jp_know_more).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        //track
                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        trackEventForCountlyAndGA(getResources().getString(R.string.countly_booking_confirmation),
                                getResources().getString(R.string.d_jp_know_more_click),getResources().getString(R.string.d_jp_know_more_click),hMap);

                        setBookingDetailsInBundle();

                        WebViewFragment webViewFragment = new WebViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("title", DOPreferences.getJpMilesKnowMoreTitle(getActivity().getApplicationContext()));
                        bundle.putString("url", DOPreferences.getJpMilesKnowMoreLink(getActivity().getApplicationContext()));
                        webViewFragment.setArguments(bundle);
                        addToBackStack(getActivity().getSupportFragmentManager(), webViewFragment);

                    }
                });
            }

            // Get JP Action Section instance
            jpActionSection = getView().findViewById(R.id.jp_action_section);

            // Get JP Action Progress Bar instance
            progressBarJpAction = (ProgressBar) getView().findViewById(R.id.progressBar_jp_action);
            progressBarJpAction.getIndeterminateDrawable().
                    setColorFilter(getActivity().getResources().getColor(R.color.colorPrimary),
                            android.graphics.PorterDuff.Mode.MULTIPLY);

            // Set Text Change Listener
            editTextJpMilesNumber = (EditText) getView().findViewById(R.id.editText_jp_miles_number);

            // Get API Message instance
            textViewJpMessage = (TextView) getView().findViewById(R.id.text_view_jp_message);

            // Set Cross Action
            jpActionButton = (ImageView) getView().findViewById(R.id.image_view_jp_action);

            // Set Saved JP Miles Number
            setSavedJPMilesNumber();

            // Set Click Listener on JP Miles Action Button
            jpActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Empty Number
                    editTextJpMilesNumber.setText("");

                    // Remove JP Message
                    textViewJpMessage.setText("");

                    // Set Text Color
                    editTextJpMilesNumber.setTextColor(getResources().getColor(R.color.grey_4D));

                    // Hide JP Action Section
                    jpActionSection.setVisibility(View.GONE);
                }
            });


            // Set Text Water on JP Miles Number
            editTextJpMilesNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence typedText, int start, int count, int after) {
                    // Remove JP Message
                    if (textViewJpMessage != null) {
                        textViewJpMessage.setText("");
                    }

                    if (typedText != null && typedText.length() ==
                            getResources().getInteger(R.integer.jp_mile_number_length)) {
                        // Check Text Color of JP Miles Number
                        editTextJpMilesNumber.setTextColor(getResources().getColor(R.color.grey_4D));
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Do Nothing...
                }

                @Override
                public void afterTextChanged(Editable typedText) {
                    // Check Length of Text Typed in
                    if (typedText != null) {

                        if (typedText.length() ==
                                getResources().getInteger(R.integer.jp_mile_number_length) && !isJpMilesNumberVerificationInProcess) {
                            // Reset Flag
                            isJpMilesNumberVerified = false;

                            // Reset Flag
                            isJpMilesNumberVerificationInProcess = true;

                            // Hide Keyboard
                            AppUtil.hideKeyboard(getActivity());

                            // Empty Error Message
                            textViewJpMessage.setText("");

                            // Disable JP Miles Section
                            editTextJpMilesNumber.setEnabled(false);

                            // Show JP Action Section
                            jpActionSection.setVisibility(View.VISIBLE);

                            // Show JP Action Progress Bar
                            progressBarJpAction.setVisibility(View.VISIBLE);

                            // Hide JP Action Button
                            jpActionButton.setVisibility(View.GONE);

                            // Take API Hit
                            getNetworkManager().jsonRequestGet(REQUEST_CODE_VALIDATE_JP_NUMBER,
                                    AppConstant.URL_VALIDATE_JP_NUMBER,
                                    ApiParams.validateJPNumberParams(editTextJpMilesNumber.getText().toString()),
                                    BookingConfirmationFragment.this, null, false);
                        }
                    }
                }
            });
        }
    }

    // Set Saved JP Miles Number
    private void setSavedJPMilesNumber() {
        if (!TextUtils.isEmpty(DOPreferences.getJpMilesNumber(getActivity().getApplicationContext()))) {
            // Set JP Miles Number
            editTextJpMilesNumber.setText(DOPreferences.getJpMilesNumber(getActivity().getApplicationContext()));

            // Change JP Miles Number
            editTextJpMilesNumber.setTextColor(getResources().getColor(R.color.pista_green));

            // Show JP Action Section
            jpActionSection.setVisibility(View.VISIBLE);

            // Hide JP ProgressBar
            progressBarJpAction.setVisibility(View.GONE);

            // Show JP Action Button
            jpActionButton.setVisibility(View.VISIBLE);

            // Set JP Message Text
            textViewJpMessage.setText(R.string.text_verified);

            // Set JP Message Text Color
            textViewJpMessage.setTextColor(getResources().getColor(R.color.pista_green));

            // Set Flag
            isJpMilesNumberVerified = true;
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        switch (viewId) {

            case R.id.imageView_all_offer_back:
                handleNavigationBack();
                break;

            case R.id.imageView_info:
                handleInfoAction();
                break;

            case R.id.text_jp_know_more:
                handleJpMilesKnowMoreAction();
                break;

            case R.id.button_confirm_table:
                handleConfirmTableAction();
                break;

            default:
                break;
        }
    }

    private void handleNavigationBack() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getResources().getString(R.string.countly_booking_confirmation),
                getResources().getString(R.string.d_back_click),getResources().getString(R.string.d_back_click),hMap);

        popBackStack(getFragmentManager());
    }

    private void handleInfoAction() {

        //track
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getResources().getString(R.string.countly_booking_confirmation),
                getResources().getString(R.string.d_guest_info_click),getResources().getString(R.string.d_guest_info_click),hMap);

        // Show Info Dialog
        MoreDialog moreDialog = new MoreDialog(getString(R.string.title_information),
                getString(R.string.table_confirmation_hint), getActivity());
        moreDialog.show(getFragmentManager(), "tableDialog");
    }

    private void handleJpMilesKnowMoreAction() {
        WebViewFragment webViewFragment = new WebViewFragment();

        Bundle bundle = new Bundle();
        bundle.putString("title", DOPreferences.getJpMilesKnowMoreTitle(getActivity().getApplicationContext()));
        bundle.putString("url", DOPreferences.getJpMilesKnowMoreLink(getActivity().getApplicationContext()));

        webViewFragment.setArguments(bundle);

        addToBackStack(getFragmentManager(), webViewFragment);
    }

    private void setBookingDetailsInBundle() {
        if (arguments != null) {

            arguments.putInt(AppConstant.BUNDLE_GUEST_COUNT,
                    Integer.parseInt(getGuestCount(maleCount, femaleCount))); // Guest Count

            if (AppUtil.isStringEmpty(maleCount)) {
                maleCount = "0";
            }

            if (AppUtil.isStringEmpty(femaleCount)) {
                femaleCount = "0";
            }

            if (!AppUtil.isStringEmpty(maleCount)) {
                arguments.putInt(AppConstant.BUNDLE_MALE_COUNT,
                        Integer.parseInt(maleCount));
            }

            if (!AppUtil.isStringEmpty(femaleCount)) {
                arguments.putInt(AppConstant.BUNDLE_FEMALE_COUNT,
                        Integer.parseInt(femaleCount));
            }

            arguments.putString(AppConstant.BUNDLE_GUEST_NAME,
                    editTextGuestName.getText().toString()); // Guest Name
            arguments.putString(AppConstant.BUNDLE_GUEST_EMAIL,
                    editTextGuestEmailAddress.getText().toString()); // Guest Email
            arguments.putString(AppConstant.BUNDLE_GUEST_PHONE,
                    editTextGuestPhoneNumber.getText().toString()); // Guest Phone
            arguments.putString(AppConstant.BUNDLE_SPECIAL_REQUEST,
                    editTextGuestSpecialRequest.getText().toString()); // Special Request
        }
    }

    private void handleConfirmTableAction() {
        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());

        if (isEditBooking) {
            // Track Event
//            trackEventGA(getString(R.string.confirm_edit_booking),
//                    getString(R.string.ga_action_confirm_edit), null);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("B_Confirmation","CTAClick",
                    getString(R.string.ga_action_confirm_edit),hMap);

            String totalGuest = getGuestCount(maleCount, femaleCount);

            if (!AppUtil.isStringEmpty(totalGuest)) {
                if (Integer.parseInt(totalGuest) > 20) {
                    Toast.makeText(getContext(), "Number of guests cannot be more than 20 people", Toast.LENGTH_SHORT).show();
                } else {
                    //Show Edit Booking Dialog
                    showEditBookingDialog();
                }
            }

        } else {



            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            String spclRqst=editTextGuestSpecialRequest.getText().toString();


            //since no personal information needs to get passed in GA, hence label is empty
            trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),"SpecialRequestType",spclRqst,hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),getString(R.string.d_edit_name_click),"",hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),getString(R.string.d_edit_email_click),"",hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),getString(R.string.d_edit_mobile_click),"",hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_booking_confirmation),getString(R.string.d_edit_jpno_click),editTextJpMilesNumber.getText().toString(),hMap);


            trackEventForCountlyAndGA("B_Confirmation","BookingConfirmationCTAClick",
                    arguments.getString(AppConstant.BUNDLE_RESTAURANT_NAME, "")+"_"+arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""),hMap);

            //event for qgraph, apsalr
            trackEventQGraphApsalar("BookingConfirmationCTAClick",new HashMap<String, Object>(),true,false,false);

            // Validate fields
            if (validateResults()) {
                // Confirm Booking
                confirmBooking();
            }
        }
    }

    private boolean validateResults() {
        if (getView() == null)
            return false;

        boolean isGuestCountValid = validateGuestCount();
        boolean isNameValid = validateUserName();
        boolean isNumberValid = validateNumber();
        boolean isEmailValid = validateEmail();

        return (isGuestCountValid && isNameValid && isNumberValid && isEmailValid);
    }

    private boolean validateGuestCount() {
        // int selectedItemPosition = spinnerNoOfGuests.getSelectedItemPosition();
        int selectedMaleCount = 0;
        int selectedFemaleCount = 0;

        if (!AppUtil.isStringEmpty(maleCount)) {
            selectedMaleCount = Integer.parseInt(maleCount);
        }

        if (!AppUtil.isStringEmpty(femaleCount)) {
            selectedFemaleCount = Integer.parseInt(femaleCount);
        }

        int guestCount = (selectedFemaleCount + selectedMaleCount);
        if (guestCount > 20) {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), "Number of guests cannot be more than 20 people");
            return false;
        }

        if (selectedMaleCount <= 0 && selectedFemaleCount <= 0) {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please select number of guest(s)");
            return false;
        }


        return true;
    }

    private boolean validateUserName() {
        String dinerName = editTextGuestName.getText().toString().trim();

        if (TextUtils.isEmpty(dinerName)) {
            editTextGuestName.setError("Name cannot be blank");
            return false;
        }

        return true;
    }

    public boolean validateNumber() {
        String phoneNumber = editTextGuestPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            editTextGuestPhoneNumber.setError("Number cannot be blank");
            return false;

        } else if (phoneNumber.length() != 10) {
            editTextGuestPhoneNumber.setError("Number is not valid");
            return false;
        }

        return true;
    }

    public boolean validateEmail() {
        String emailAddress = editTextGuestEmailAddress.getText().toString().trim();

        if (TextUtils.isEmpty(emailAddress)) {
            editTextGuestEmailAddress.setError("Email cannot be blank");
            return false;
        }

        if (!isEmailValid(emailAddress)) {
            editTextGuestEmailAddress.setError("Error:Invalid Email Address");
            return false;
        }

        return true;
    }

    private void showEditBookingDialog() {
        MoreDialog dialog = new MoreDialog("Edit Booking?", getResources().getString(R.string.booking_edit_msg), getActivity(), true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();

                // Track Event
//                trackEventGA(getString(R.string.confirm_edit_booking),
//                        getString(R.string.ga_action_edit_warning), null);

                // Show Loading Dialog
                showLoader();

                getNetworkManager().stringRequestPost(REQUEST_CODE_EDIT_BOOKING, AppConstant.URL_EDIT_BOOKING,
                        ApiParams.editBooking(
                                arguments.getString(AppConstant.BUNDLE_BOOKING_ID, ""),
                                arguments.getLong(AppConstant.BUNDLE_DATE_TIMESTAMP, 0L),
                                maleCount,
                                getGuestCount(maleCount, femaleCount),
                                Integer.toString(arguments.getInt(AppConstant.BUNDLE_OFFER_ID, 0)),
                                editTextGuestSpecialRequest.getText().toString()
                        ), new Response.Listener<String>() {
                            @Override
                            public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                                try {
                                    parseJSON(new JSONObject(responseObject), REQUEST_CODE_EDIT_BOOKING);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, BookingConfirmationFragment.this, false);
            }
        });

        dialog.show(getFragmentManager(), "booking_detail");
    }

    private String getGuestCount(String maleCount, String femaleCount) {
        int selectedMale = 0;
        int selectedFemale = 0;

        if (!AppUtil.isStringEmpty(maleCount)) {
            selectedMale = Integer.parseInt(maleCount);
        }

        if (!AppUtil.isStringEmpty(femaleCount)) {
            selectedFemale = Integer.parseInt(femaleCount);
        }

        return Integer.toString(selectedMale + selectedFemale);
    }

    private void parseJSON(JSONObject responseObject, int identifier) {
        // Hide Loading Dialog
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        switch (identifier) {
            case REQUEST_CODE_EDIT_BOOKING:

                if (responseObject != null) {
                    if (responseObject.optBoolean("status")) {

                        MoreDialog dialog = new MoreDialog(getResources().getString(R.string.edit_booking), getActivity().getResources().getString(R.string.edit_booking_status), getActivity(), false);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Dismiss Dialog
                                dialog.dismiss();

                                // Pop
                                popToHome(getActivity());
                            }
                        });

                        dialog.show(getFragmentManager(), "booking_detail");

                    } else {
                        handleErrorResponse(responseObject);
                    }
                }
                break;


            case REQUEST_CODE_CONFIRM_TABLE:

                if (responseObject != null) {
                    if (responseObject.optBoolean("status")) {
                        String dinerName = editTextGuestName.getText().toString().trim();
                        String emailAddress = editTextGuestEmailAddress.getText().toString().trim();
                        String phoneNumber = editTextGuestPhoneNumber.getText().toString().trim();
                        int selectedMaleCount = 0;
                        int selectedFemaleCount = 0;
                        if (!AppUtil.isStringEmpty(maleCount)) {
                            selectedMaleCount = Integer.parseInt(maleCount);
                        }

                        if (!AppUtil.isStringEmpty(femaleCount)) {
                            selectedFemaleCount = Integer.parseInt(femaleCount);
                        }

                        int guestCount = selectedMaleCount + selectedFemaleCount;
                        String restaurantId = arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, "");
                        String restaurantName = arguments.getString(AppConstant.BUNDLE_RESTAURANT_NAME, "");
                        String restaurantCity = arguments.getString(AppConstant.BUNDLE_RESTAURANT_CITY, "");
                        String restaurantArea = arguments.getString(AppConstant.BUNDLE_RESTAURANT_AREA, "");
                        String restaurantLocality = arguments.getString(AppConstant.BUNDLE_RESTAURANT_LOCALITY, "");

                        HashMap<String, Object> props = new HashMap<>();
                        props.put("EmailId", emailAddress);
                        props.put("Successful", true);
                        props.put("RestaurantName", restaurantName);
                        props.put("City", restaurantCity);
                        props.put("Area", restaurantArea);
                        props.put("Locality", restaurantLocality);
                        props.put("NumberOfPAX", guestCount);
                        props.put("Date", arguments.getString(AppConstant.BUNDLE_SELECTED_DATE));
                        props.put("Slot", arguments.getString(AppConstant.BUNDLE_SELECTED_TIME));
                        props.put("OfferName", offerTitle);
                        props.put("ResImageURL", arguments.getString(AppConstant.BUNDLE_RESTAURANT_IMAGE_URL, ""));
                        props.put("ResDeepLink", arguments.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK, ""));
                        props.put("ResCuisine", arguments.getString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, ""));
                        props.put("CuisinesList", arguments.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
                        props.put("TagList", arguments.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
                        props.put("RestaurantID", restaurantId);

                        // Track Event
                        //trackEventGA(getString(R.string.push_label_table_booked), props);


                        double amt = (guestCount) * (arguments.getDouble("COSTFORTWO") / 2);
                        trackAdTechEvent("utran", "dAmt:" + amt);
                        trackAdTechEvent("utran", "dName:" + restaurantName);
                        trackAdTechEvent("utran", "dloc:" + restaurantCity + ":" + restaurantArea + ":" + restaurantLocality);
                        trackAdTechEvent("utran", "dCount:" + guestCount);

                        String res_area="";
                        String rest_locality="";
                        String onlyArea="";
                        String area="";

                        if(!TextUtils.isEmpty(DOPreferences.getSfArrLocarea(getContext()))){


                            String locArea=DOPreferences.getSfArrLocarea(getContext());
                            String[] rest_loc = locArea.replace(" ", "").split(",");
                            if (rest_loc.length > 0) {

                                res_area = rest_loc[0];
                                rest_locality = rest_loc[1];
                                area=rest_locality + ":" + res_area;
                            }

                        }else if(!TextUtils.isEmpty(DOPreferences.getSfArrArea(getContext()))){
                            onlyArea=DOPreferences.getSfArrArea(getContext());
                        }


                        //for pre-selected location in app
                        String location = com.dineout.recycleradapters.util.AppUtil.
                                prepareString(DOPreferences.getCityName(getContext()), area,
                                        onlyArea);

                        trackAdTechEvent("ua", "dLoc:" + location);


                        // Log Facebook Purchase Event
                        logFacebookPurchaseEvent();

                        // Revenue Generation Reporting for Apsalar
                        AnalyticsHelper.getAnalyticsHelper(MainApplicationClass.getInstance())
                                .trackApsalarRevenueEvent(restaurantId, restaurantName, guestCount);


                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        trackEventForCountlyAndGA("B_Confirmation","BookingSuccessful",restaurantName+"_"+restaurantId,hMap);


                        if(props!=null){
                            props.put("label",restaurantName+"_"+restaurantId);
                        }

                        // track for apsalar, branch and qgraph
                        trackEventQGraphApsalar("BookingSuccessful",props,true,true,true);


                        JSONObject outputParam = responseObject.optJSONObject("output_params");
                        if (outputParam != null) {
                            JSONObject data = outputParam.optJSONObject("data");
                            if (data != null) {
                                Bundle bundle = new Bundle();
                                bundle.putInt("status", 1);
                                bundle.putString("b_id", data.optString("b_id"));
                                bundle.putString("disp_id", data.optString("disp_id"));
                                bundle.putString("res_id", restaurantId);
                                bundle.putString("res_name", restaurantName);
                                bundle.putString("booking_date", arguments.getString(AppConstant.BUNDLE_SELECTED_DATE));
                                bundle.putString("booking_time", arguments.getString(AppConstant.BUNDLE_SELECTED_TIME));
                                bundle.putString("dinner_name", dinerName);
                                bundle.putString("rest_url", data.optString("short_url"));
                                bundle.putString("guest_count", Integer.toString(guestCount));
                                bundle.putString("restaurant_locality", data.optString("locality_name"));
                                bundle.putInt("is_accept_payment", data.optInt("is_accept_payment"));
                                bundle.putString("message_1", data.optString("success_message_1"));
                                bundle.putString("message_2", data.optString("success_message_2"));
                                bundle.putInt("rating_dialog", data.optInt("show_rating_dialog"));
                                bundle.putString("type", "booking");
                                bundle.putString("previous_fragment", getResources().getString(R.string.text_booking_confirmation));


                                BookingDetailsFragment bookingDetailFragment = new BookingDetailsFragment();
                                bookingDetailFragment.setArguments(bundle);
                                addToBackStack(getActivity(), bookingDetailFragment);
                            }
                        }
                    } else {
                        setBookingDetailsInBundle();
                        handleErrorResponse(responseObject);

                    }
                }
                break;
        }
    }


    private void confirmBooking() {
        // Track Event
//        trackEventGA(getString(R.string.ga_screen_confirm_booking),
//                getString(R.string.ga_action_confirm_table), null);

        // Show Loading Dialog
        showLoader();

        if (editTextJpMilesNumber != null &&
                editTextJpMilesNumber.getText().toString().length() ==
                        getResources().getInteger(R.integer.jp_mile_number_length) &&
                isJpMilesNumberVerificationInProcess) {

            // Show Message
            UiUtil.showSnackbar(linearLayoutConfirmTableContainer, "JP Miles Number is being verified. Please wait", 0);

            return;
        }

        // JP Miles
        String jpMilesNumber = "";
        if (editTextJpMilesNumber != null &&
                editTextJpMilesNumber.getText().toString().length() ==
                        getResources().getInteger(R.integer.jp_mile_number_length) &&
                isJpMilesNumberVerified) {
            jpMilesNumber = editTextJpMilesNumber.getText().toString();
        }


        if (getContext() != null && editTextGuestPhoneNumber != null) {
            DOPreferences.setTempDinerPhone(getContext(), editTextGuestPhoneNumber.getText().toString().trim());
        }

        // Take API Hit

        HashMap<String, String> params = ApiParams.getGenerateBookingParams(arguments.getString(AppConstant.BUNDLE_OFFER_ID, "0"),
                arguments.getLong(AppConstant.BUNDLE_DATE_TIMESTAMP, 0L),
                maleCount,
                getGuestCount(maleCount, femaleCount),
                arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""),
                editTextGuestEmailAddress.getText().toString().trim(),
                DOPreferences.getDinerId(getActivity().getApplicationContext()),
                editTextGuestName.getText().toString().trim(),
                editTextGuestPhoneNumber.getText().toString().trim(),
                editTextGuestSpecialRequest.getText().toString().trim(),
                "new", "12", "NA", jpMilesNumber);


        showLoader();

        getNetworkManager().stringRequestPost(REQUEST_CODE_CONFIRM_TABLE, AppConstant.URL_GENERATE_BOOKING,
                params, new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        try {
                            parseJSON(new JSONObject(responseObject), REQUEST_CODE_CONFIRM_TABLE);
                        } catch (JSONException e) {

                        }
                    }
                }, this, false);

    }

    private void logFacebookPurchaseEvent() {
        // Prepare Data
        Bundle bundle = AppUtil.getFacebookAppEventData();
        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID,
                arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""));

        // Log Event
        logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, bundle);
    }

    private void logFacebookAddToCartEvent() {
        // Prepare Data
        Bundle bundle = AppUtil.getFacebookAppEventData();
        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID,
                arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID, ""));

        // Log Event
        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, bundle);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // UnRegister Pref Change Listener
        DOPreferences.getSharedPreferences(getContext()).
                unregisterOnSharedPreferenceChangeListener(this);

        arguments = null;
        logger = null;
        linearLayoutConfirmTableContainer = null;
        editTextGuestName = null;
        editTextGuestEmailAddress = null;
        editTextGuestPhoneNumber = null;
        editTextGuestSpecialRequest = null;
        buttonConfirmTable = null;
        progressBarJpAction = null;
        editTextJpMilesNumber = null;
        jpActionButton = null;
        textViewJpMessage = null;
        jpActionSection = null;
        extraInfoJsonArray = null;
        maleAdapter = null;
        femaleAdapter = null;
        maleCount = null;
        femaleCount = null;
        offerTitle = null;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // Set Diner Phone Number
        if (editTextGuestPhoneNumber == null) {
            editTextGuestPhoneNumber = (EditText) getView().findViewById(R.id.editText_guest_phone_number);
        }
        String dinerPhone = DOPreferences.getDinerPhone(getActivity().getApplicationContext());

        if (!TextUtils.isEmpty(arguments.getString(AppConstant.BUNDLE_GUEST_PHONE))) {
            editTextGuestPhoneNumber.setText(arguments.getString(AppConstant.BUNDLE_GUEST_PHONE));
        } else if (!TextUtils.isEmpty(dinerPhone)) {
            editTextGuestPhoneNumber.setText(dinerPhone);
        }

        maleAdapter.setMaleSelection(arguments.getInt(AppConstant.BUNDLE_MALE_COUNT, -1));
        femaleAdapter.setFemaleSelection(arguments.getInt(AppConstant.BUNDLE_FEMALE_COUNT, -1));
    }


    /**
     * API Handlers
     */
    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_VALIDATE_JP_NUMBER) {
            if (responseObject != null) {
                // Set Verification Flag
                isJpMilesNumberVerificationInProcess = false;

                // Enable JP Miles Number
                editTextJpMilesNumber.setEnabled(true);

                // Hide Progress Bar
                progressBarJpAction.setVisibility(View.GONE);

                // Show JP Action Button
                jpActionButton.setVisibility(View.VISIBLE);

                if (responseObject.optBoolean("status")) {
                    // Set Flag
                    isJpMilesNumberVerified = true;

                    // Change JP Miles Number
                    editTextJpMilesNumber.setTextColor(getResources().getColor(R.color.pista_green));

                    // Set Success Text
                    textViewJpMessage.setText(R.string.text_verified);
                    textViewJpMessage.setTextColor(getResources().getColor(R.color.pista_green));

                    DOPreferences.setJpMilesNumber(getActivity().getApplicationContext(),
                            editTextJpMilesNumber.getText().toString());

                } else {
                    // Change JP Miles Number
                    editTextJpMilesNumber.setTextColor(getResources().getColor(R.color.red_cc));

                    // Set Success Text
                    textViewJpMessage.setText(responseObject.optString("error_msg"));
                    textViewJpMessage.setTextColor(getResources().getColor(R.color.red_cc));
                }
            }
        } else if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAIL_EXTRA_INFO) {
            if (responseObject != null && responseObject.optBoolean("status")) {

                // Get Data
                JSONObject dataJsonObject = responseObject.optJSONObject("data");
                if (dataJsonObject != null) {

                    // Get Stream
                    JSONArray streamJsonArray = dataJsonObject.optJSONArray("stream");
                    if (streamJsonArray != null && streamJsonArray.length() > 0) {

                        // Get First Item
                        JSONObject firstItemJsonObject = streamJsonArray.optJSONObject(0);
                        if (firstItemJsonObject != null) {

                            // Get Data
                            dataJsonObject = firstItemJsonObject.optJSONObject("data");
                            if (dataJsonObject != null) {

                                // Set Extra Info Array
                                setExtraInfoJsonArray(dataJsonObject.optJSONArray("extraInfo"));

                                // Show Extra Info
                                showExtraInfo();
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

        if (request.getIdentifier() == REQUEST_CODE_CONFIRM_TABLE) {

            UiUtil.showSnackbar(linearLayoutConfirmTableContainer, getString(R.string.text_server_error), R.color.snackbar_error_background_color);

        } else if (request.getIdentifier() == REQUEST_CODE_EDIT_BOOKING) {
            // Hide Loading Dialog
            UiUtil.showSnackbar(linearLayoutConfirmTableContainer, getString(R.string.text_server_error), R.color.snackbar_error_background_color);

        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase(DOPreferences.PARAM_DINER_PHONE)) {
            // Get Bundle of Fragments
            Bundle bundle = getArguments();

            if (bundle != null) {
                String phoneNumber = DOPreferences.getDinerPhone(getActivity().getApplicationContext());
                bundle.putString(AppConstant.BUNDLE_GUEST_PHONE, phoneNumber);
            }
        }
    }


}
