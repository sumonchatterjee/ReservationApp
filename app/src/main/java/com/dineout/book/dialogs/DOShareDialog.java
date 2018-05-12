package com.dineout.book.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.controller.MyClipboardManager;
import com.dineout.book.util.UiUtil;
import com.dineout.book.adapter.ShareIntentGridAdapter;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DOShareDialog extends MasterDOFragment {

    public static final String WHATSAPP_PACKAGE = "com.whatsapp";
    public static final String GMAIL_PACAKGE = "com.google.android.gm";
    public static final String FACEBOOK_PACKAGE = "com.facebook.katana";
    public static final String MESSAGE_PACKAGE = "com.android.mms";
    public static final String GOOGLE_PLUS = "com.google.android.apps.plus";
    public static final String TWITTER_PACKAGE = "com.twitter.android";
    public static final int FB_RESULT_CODE = 101;
    public static final int GMAIL_RESULT_CODE = 102;
    public static final int WHATSAPP_RESULT_CODE = 103;
    public static final int MESSAGE_RESULT_CODE = 104;
    public static final int GOOGLE_PLUS_RESULT_CODE = 105;

    private static final String ARG_SHARE_MESSAGE = "share_message";
    private static final String ARG_SHARE_SUBJECT = "share_subject";
    private static final String ARG_SHARE_REST_URL = "share_rest_url";
    private static final String ARG_RESTO_NAME = "resto_name";
    private static final String ARG_BOOKING_DATE = "booking_date";
    private static final String ARG_BOOKING_SLOT = "booking_slot";
    private static final String ARG_BOOKING_ID = "booking_id";
    private static final String ARG_GUEST_COUNT = "guest_count";
    private static final String ARG_RESTO_LOCALITY = "resto_locality";
    private static final String MAIL_SUBJECT = "Lets Plan !!";
    private static final String ARG_DINER_NAME = "diner_name";
    private static final String ARG_DEAL_NAME = "deal";
    private static final String ARG_DEAL_COUNT = "deal_count";
    private static final String ARG_TYPE = "type";

    private static Context mContext;
    private static MainApplicationClass mMainApplicationClass;
    GridView mGridView;
    String mStrShareMessage, mStrShareRestoName, mStrShareBookingDate, mStrShareBookingSlot,
            mDisplayBookingId, mStrUsername = "", mGuestCount, mRestoLocality, dealName, dealCount, dinerName,type;
    OnCancelListener onCancelListener = null;
    ShareIntentGridAdapter adapter;
    List<ResolveInfo> activities = null, finalListPackages = null;
    HashMap<String, ResolveInfo> activitiesMap = null;
    private String shareSubject = "";
    private boolean isRestaurantSharing;
    private String restUrl = "";
//    private LoginDialogListener mLoginDialogListener;
    private JSONObject bookingMessageJsonObject;
    private JSONObject restaurantMessageJsonObject;

    public static DOShareDialog newInstance(Context mContext, String dinerName, String deal, String dealCount, String restName, String restUrl,
                                            String bookingDate, String bookingSlot, String displayBookingId,
                                            String guestCount, String restaurantLocality) {

        DOShareDialog fragment = new DOShareDialog();
        DOShareDialog.mContext = mContext;
        mMainApplicationClass = MainApplicationClass.getInstance();
//        fragment.mLoginDialogListener = mLoginDialogListener;

        Bundle args = new Bundle();
        args.putString(ARG_DINER_NAME, dinerName);
        args.putString(ARG_DEAL_NAME, deal);
        args.putString(ARG_DEAL_COUNT, dealCount);
        args.putString(ARG_RESTO_NAME, restName);
        args.putString(ARG_BOOKING_DATE, bookingDate);
        args.putString(ARG_BOOKING_SLOT, bookingSlot);
        args.putString(ARG_BOOKING_ID, displayBookingId);
        args.putString(ARG_GUEST_COUNT, guestCount);
        args.putString(ARG_SHARE_REST_URL, restUrl);
        args.putString(ARG_RESTO_LOCALITY, restaurantLocality);


        fragment.setArguments(args);

        return fragment;
    }

    public static DOShareDialog newInstance(Context mContext, String shareMsg, String subject) {

        DOShareDialog fragment = new DOShareDialog();
        DOShareDialog.mContext = mContext;
        mMainApplicationClass = MainApplicationClass.getInstance();
//        fragment.mLoginDialogListener = mLoginDialogListener;
        fragment.isRestaurantSharing = true;

        Bundle args = new Bundle();
        args.putString(ARG_SHARE_MESSAGE, shareMsg);
        args.putString(ARG_SHARE_SUBJECT, subject);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type=getTag();
        if (getArguments() != null) {
            if (!isRestaurantSharing) {
                restUrl = getArguments().getString(ARG_SHARE_REST_URL);
                mStrShareRestoName = getArguments().getString(ARG_RESTO_NAME);
                mStrShareBookingDate = getArguments().getString(ARG_BOOKING_DATE);
                mStrShareBookingSlot = getArguments().getString(ARG_BOOKING_SLOT);
                mDisplayBookingId = getArguments().getString(ARG_BOOKING_ID);
                mGuestCount = getArguments().getString(ARG_GUEST_COUNT);
                mRestoLocality = getArguments().getString(ARG_RESTO_LOCALITY);
                dealName = getArguments().getString(ARG_DEAL_NAME);
                dealCount = getArguments().getString(ARG_DEAL_COUNT);
                dinerName = getArguments().getString(ARG_DINER_NAME);



            } else {
                mStrShareMessage = getArguments().getString(ARG_SHARE_MESSAGE);
                shareSubject = getArguments().getString(ARG_SHARE_SUBJECT);

                try {
                    setRestaurantMessageJsonObject(new JSONObject(mStrShareMessage));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mStrUsername = DOPreferences.getDinerFirstName(mContext.getApplicationContext());
        }
    }

    public void setTitle() {
        getDialog().setTitle(R.string.share);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(R.string.share);

        getDialog().getWindow().setBackgroundDrawableResource(R.color.white);
        View view = inflater.inflate(R.layout.share_dialog_fragment, container);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        activitiesMap = new HashMap<String, ResolveInfo>();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");

        if (getActivity() != null) {
            activities = getActivity().getPackageManager().queryIntentActivities(sendIntent, 0);
            for (ResolveInfo info : activities) {
                activitiesMap.put(info.activityInfo.packageName, info);
            }
        }

        if(type.equalsIgnoreCase("booking")) {
            getBookingMessageFromStorage();
        }

        if (!TextUtils.isEmpty(dealCount)) {
            int count = Integer.parseInt(dealCount);
            if (count > 1) {
                if(type.equalsIgnoreCase("deal")) {
                    getMultipleDealMessageFromStorage();
                }else if(type.equalsIgnoreCase("event")){
                    getMultipleEventMessageFromStorage();
                }
            } else {
                if(type.equalsIgnoreCase("deal")) {
                    getSingleDealMessageFromStorage();
                }else if(type.equalsIgnoreCase("event")){
                    getSingleEventMessageFromStorage();
                }

            }
        }

        manageInstalledPackages();

        mGridView = (GridView) getView().findViewById(R.id.gridview_share_apps);
        if (adapter == null)
            adapter = new ShareIntentGridAdapter(getActivity(), new ArrayList<ResolveInfo>());

        adapter.clear();
        adapter.notifyDataSetChanged();

        if (finalListPackages == null || finalListPackages.size() == 0) {
            // Show Message
            UiUtil.showToastMessage(mContext, getString(R.string.text_application_not_installed));

            // Dismiss Dialog
            dismissAllowingStateLoss();

        } else {
            int packageSize = finalListPackages.size();
            for (int i = 0; i < packageSize; i++) {
                adapter.add(finalListPackages.get(i));
            }

            mGridView.setAdapter(adapter);

            // Hide More option
            getView().findViewById(R.id.tv_more_option).setVisibility(View.GONE);

            setupListeners();
        }
    }

    public JSONObject getRestaurantMessageJsonObject() {
        return restaurantMessageJsonObject;
    }

    public void setRestaurantMessageJsonObject(JSONObject restaurantMessageJsonObject) {
        this.restaurantMessageJsonObject = restaurantMessageJsonObject;
    }

    private JSONObject getBookingMessageJsonObject() {
        return bookingMessageJsonObject;
    }

    private void setBookingMessageJsonObject(JSONObject bookingMessageJsonObject) {
        this.bookingMessageJsonObject = bookingMessageJsonObject;
    }

    private void getBookingMessageFromStorage() {
        try {
            setBookingMessageJsonObject(new JSONObject(
                    DOPreferences.getBookingShareMessage(mContext.getApplicationContext())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getSingleDealMessageFromStorage() {
        try {
            setBookingMessageJsonObject(new JSONObject(
                    DOPreferences.getSingleDealShareMessage(mContext.getApplicationContext())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getMultipleDealMessageFromStorage() {
        try {
            setBookingMessageJsonObject(new JSONObject(
                    DOPreferences.getMultipleDealShareMessage(mContext.getApplicationContext())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getSingleEventMessageFromStorage() {
        try {
            setBookingMessageJsonObject(new JSONObject(
                    DOPreferences.getSingleEventShareMessage(mContext.getApplicationContext())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getMultipleEventMessageFromStorage() {
        try {
            setBookingMessageJsonObject(new JSONObject(
                    DOPreferences.getMultipleEventShareMessage(mContext.getApplicationContext())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if (finalListPackages == null)
                    return;
                ResolveInfo info = finalListPackages.get(position);
                if (info.activityInfo.packageName.equals(FACEBOOK_PACKAGE)) {
                    shareOnFacebook(info);
                } else if (info.activityInfo.packageName.equals(WHATSAPP_PACKAGE)) {
                    shareOnWhatsapp(info);
                } else if (info.activityInfo.packageName.equals(GMAIL_PACAKGE)) {
                    shareOnGmail(info);
                } else if (info.activityInfo.packageName.equals(MESSAGE_PACKAGE)) {
                    shareOnMessages(info);
                } else if (info.activityInfo.packageName.equals(TWITTER_PACKAGE)) {
                    shareOnTwitter(info);
                } else {
                    shareOnOthers(info);
                }
                DOShareDialog.this.dismiss();
            }
        });

        getView().findViewById(R.id.tv_more_option).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);

                for (int i = 4; i < finalListPackages.size(); i++) {
                    adapter.add(finalListPackages.get(i));
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void manageInstalledPackages() {
        finalListPackages = new ArrayList<ResolveInfo>();

        if (activitiesMap.containsKey(WHATSAPP_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(WHATSAPP_PACKAGE));
            activitiesMap.remove(WHATSAPP_PACKAGE);
        }

        if (activitiesMap.containsKey(GMAIL_PACAKGE)) {
            finalListPackages.add(activitiesMap.get(GMAIL_PACAKGE));
            activitiesMap.remove(GMAIL_PACAKGE);
        }

        if (activitiesMap.containsKey(FACEBOOK_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(FACEBOOK_PACKAGE));
            activitiesMap.remove(FACEBOOK_PACKAGE);
        }

        if (activitiesMap.containsKey(MESSAGE_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(MESSAGE_PACKAGE));
            activitiesMap.remove(MESSAGE_PACKAGE);
        }

        if (activitiesMap.containsKey(GOOGLE_PLUS)) {
            finalListPackages.add(activitiesMap.get(GOOGLE_PLUS));
            activitiesMap.remove(GOOGLE_PLUS);
        }

        if (activitiesMap.containsKey(TWITTER_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(TWITTER_PACKAGE));
            activitiesMap.remove(TWITTER_PACKAGE);
        }

        // Commented by Shashank - 17 May'16 (to hide options like Zomato)
        /*for (String packageName : activitiesMap.keySet()) {
            finalListPackages.add(activitiesMap.get(packageName));
        }*/
    }

    private boolean isStringMessageAvailable(String messageKey) {
        return (getBookingMessageJsonObject() != null &&
                !TextUtils.isEmpty(getBookingMessageJsonObject().optString(messageKey)));
    }

    private boolean isJSONAvailable(String messageKey) {
        return (getBookingMessageJsonObject() != null &&
                getBookingMessageJsonObject().optJSONObject(messageKey) != null);
    }

    private String prepareBookingMessageForFacebook() {
        String message = "";

        if (isStringMessageAvailable("facebook")) {
            message = getBookingMessageJsonObject().optString("facebook");

            message = message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
            message = message.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, restUrl);
            if(message.contains(AppConstant.DEAL_PLACE_HOLDER)){
                message = message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
            }else{
                message = message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
            }
        }

        return message;
    }

    private String getRestaurantSharingMessage(String key) {
        return getRestaurantMessageJsonObject().optString(key);
    }

    private void shareOnFacebook(ResolveInfo info) {

        String sharingText = getString(R.string.share_default);

        if (isRestaurantSharing) {
            sharingText = getRestaurantSharingMessage("facebook");

        } else {
            String message = prepareBookingMessageForFacebook();
            if (TextUtils.isEmpty(message)) {
                if (!TextUtils.isEmpty(mStrShareRestoName))
                    sharingText = String.format(getString(R.string.share_fb), mStrShareRestoName, restUrl);
            } else {
                sharingText = message;
            }
        }

        final String finalSharingText = sharingText;

        final MoreDialog moreDialog = new MoreDialog(mContext.getResources().getString(R.string.referral_invite_fb_title), sharingText, "Cancel", "Copy", mContext, true);
        moreDialog.setTextStyle(getResources().getDimension(R.dimen.font_size_16), Gravity.CENTER);
        moreDialog.setTextTitleStyle(getResources().getDimension(R.dimen.font_size_14), Gravity.CENTER);

        moreDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                UiUtil.showToastMessage(mContext, mContext.getString(R.string.text_copied));

                moreDialog.dismiss();

                MyClipboardManager clipboardManager = new MyClipboardManager();
                clipboardManager.copyToClipboard(mContext, finalSharingText);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setPackage("com.facebook.katana");

                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setType("text/plain");
//                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://www.dineout.co.in/get");
                mContext.startActivity(shareIntent);
            }
        });

        moreDialog.show(getActivity().getSupportFragmentManager(), "ShareOnFB");
    }

    private String prepareBookingMessageForWhatsapp() {
        String message = "";

        if (isStringMessageAvailable("whatsapp")) {
            message = getBookingMessageJsonObject().optString("whatsapp");

            message = message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
            message = message.replaceAll(AppConstant.BOOKING_ID_PLACE_HOLDER, mDisplayBookingId);
            message = message.replaceAll(AppConstant.BOOKING_DATE_PLACE_HOLDER, mStrShareBookingDate);
            message = message.replaceAll(AppConstant.BOOKING_TIME_PLACE_HOLDER, mStrShareBookingSlot);
            message = message.replaceAll(AppConstant.DINER_COUNT_PLACE_HOLDER, mGuestCount);
            message = message.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, restUrl);
            if(message.contains(AppConstant.DEAL_PLACE_HOLDER)){
                message = message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
            }else{
                message = message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
            }

            message = message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, mRestoLocality);
        }

        return message;
    }

    private void shareOnWhatsapp(ResolveInfo info) {

        String sharingText = getString(R.string.share_default);

        if (isRestaurantSharing) {
            sharingText = getRestaurantSharingMessage("whatsapp");

        } else {
            String message = prepareBookingMessageForWhatsapp();
            if (TextUtils.isEmpty(message)) {
                if (!TextUtils.isEmpty(mStrShareBookingDate) && !TextUtils.isEmpty(mStrShareBookingSlot)) {
                    sharingText = String.format(getString(R.string.share_whatsapp), mStrShareRestoName,
                            mStrShareBookingDate, mStrShareBookingSlot, restUrl, mDisplayBookingId);
                } else if (!TextUtils.isEmpty(mStrShareRestoName)) {
                    sharingText = String.format(getString(R.string.share_fb), mStrShareRestoName, restUrl);
                }
            } else {
                sharingText = message;
            }
        }

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(WHATSAPP_PACKAGE, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        startActivityForResult(sharingIntent, WHATSAPP_RESULT_CODE);
    }

    private String prepareBookingMessageForMail(String sectionKey) {
        String message = "";

        if (isJSONAvailable("email")) {
            JSONObject emailJsonObject = getBookingMessageJsonObject().optJSONObject("email");
            message = emailJsonObject.optString(sectionKey, "");

            if (!TextUtils.isEmpty(message)) {
                if (sectionKey.equalsIgnoreCase("subject")) {
                    if (message.contains(AppConstant.DINER_FIRST_NAME_PLACE_HOLDER)) {
                        message = message.replaceAll(AppConstant.DINER_FIRST_NAME_PLACE_HOLDER, mStrUsername);
                    } else {
                        message = message.replaceAll(AppConstant.DINER_NAME_PLACE_HOLDER, dinerName);
                    }

                    message = TextUtils.isEmpty(mStrShareRestoName) ? message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, "") : message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
                    message = TextUtils.isEmpty(mRestoLocality) ? message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, "") : message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, mRestoLocality);
                    if(message.contains(AppConstant.DEAL_PLACE_HOLDER)) {
                        message = TextUtils.isEmpty(dealName) ? message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, "") : message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
                    }else{
                        message = TextUtils.isEmpty(dealName) ? message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, "") : message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
                    }

                } else if (sectionKey.equalsIgnoreCase("body")) {

                    if (message.contains(AppConstant.DINER_FIRST_NAME_PLACE_HOLDER)) {
                        message = message.replaceAll(AppConstant.DINER_FIRST_NAME_PLACE_HOLDER, mStrUsername);
                    } else {
                        message = message.replaceAll(AppConstant.DINER_NAME_PLACE_HOLDER, dinerName);
                    }

                    message = TextUtils.isEmpty(mStrShareRestoName) ? message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, "") : message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
                    message = TextUtils.isEmpty(mRestoLocality) ? message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, "") : message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, mRestoLocality);
                    message = TextUtils.isEmpty(mStrShareBookingDate) ? message.replaceAll(AppConstant.BOOKING_DATE_PLACE_HOLDER, "") : message.replaceAll(AppConstant.BOOKING_DATE_PLACE_HOLDER, mStrShareBookingDate);
                    message = TextUtils.isEmpty(mStrShareBookingSlot) ? message.replaceAll(AppConstant.BOOKING_TIME_PLACE_HOLDER, "") : message.replaceAll(AppConstant.BOOKING_TIME_PLACE_HOLDER, mStrShareBookingSlot);
                    if(message.contains(AppConstant.DEAL_PLACE_HOLDER)) {
                        message = TextUtils.isEmpty(dealName) ? message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, "") : message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
                    }else{
                        message = TextUtils.isEmpty(dealName) ? message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, "") : message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
                    }


                }
            }
        }

        return message;
    }

    private String getRestaurantShareEmailBody() {
        String emailBody = "";

        if (getRestaurantMessageJsonObject() != null) {
            JSONObject emailJsonObject = getRestaurantMessageJsonObject().optJSONObject("email");

            if (emailJsonObject != null) {
                emailBody = emailJsonObject.optString("body");
            }
        }

        return emailBody;
    }

    private void shareOnGmail(ResolveInfo info) {
        String sharingText = getString(R.string.share_default);

        if (isRestaurantSharing) {
            sharingText = getRestaurantShareEmailBody();

        } else {
            String message = prepareBookingMessageForMail("body");
            if (TextUtils.isEmpty(message)) {
                if (!TextUtils.isEmpty(mStrShareBookingDate) && !TextUtils.isEmpty(mStrShareBookingSlot)) {
                    String userName = !TextUtils.isEmpty(DOPreferences
                            .getDinerEmail(getContext().getApplicationContext())) ?
                            DOPreferences
                                    .getDinerFirstName(getContext().getApplicationContext()) : mStrUsername;
                    sharingText = String.format(getString(R.string.share_email), userName, mStrShareRestoName, mStrShareBookingDate, mStrShareBookingSlot, restUrl, userName);
                } else if (!TextUtils.isEmpty(mStrShareRestoName))
                    sharingText = String.format(getString(R.string.share_fb), mStrShareRestoName, restUrl);
            } else {
                sharingText = message;
            }
        }

        String subjectLine = prepareBookingMessageForMail("subject");

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(GMAIL_PACAKGE, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,
                ((TextUtils.isEmpty(subjectLine)) ?
                        (isRestaurantSharing ? shareSubject : MAIL_SUBJECT) : subjectLine));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);

        startActivityForResult(sharingIntent, GMAIL_RESULT_CODE);
    }

    private String prepareBookingMessageForSms() {
        String message = "";

        if (isStringMessageAvailable("sms")) {
            message = getBookingMessageJsonObject().optString("sms");

            message = message.replaceAll(AppConstant.DINER_NAME_PLACE_HOLDER, mStrUsername);
            message = message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
            message = message.replaceAll(AppConstant.BOOKING_DATE_PLACE_HOLDER, mStrShareBookingDate);
            message = message.replaceAll(AppConstant.BOOKING_TIME_PLACE_HOLDER, mStrShareBookingSlot);
            if(message.contains(AppConstant.DEAL_PLACE_HOLDER)){
                message = message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
            }else{
                message = message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
            }

            message = message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, mRestoLocality);
            message = message.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, restUrl);
        }

        return message;
    }

    private void shareOnMessages(ResolveInfo info) {

        String sharingText = getString(R.string.share_default);

        if (isRestaurantSharing) {
            sharingText = getRestaurantSharingMessage("sms");

        } else {
            String message = prepareBookingMessageForSms();
            if (TextUtils.isEmpty(message)) {
                if (!TextUtils.isEmpty(mStrShareBookingDate) && !TextUtils.isEmpty(mStrShareBookingSlot)) {
                    sharingText = String.format(getString(R.string.share_message), mStrShareRestoName, mStrShareBookingDate, mStrShareBookingSlot, restUrl, mDisplayBookingId);
                } else if (!TextUtils.isEmpty(mStrShareRestoName)) {
                    sharingText = String.format(getString(R.string.share_fb), mStrShareRestoName, restUrl);
                }
            } else {
                sharingText = message;
            }
        }

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(MESSAGE_PACKAGE, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        startActivityForResult(sharingIntent, MESSAGE_RESULT_CODE);
    }

    private String prepareBookingMessageForTwitter() {
        String message = "";

        if (isStringMessageAvailable("twitter")) {
            message = getBookingMessageJsonObject().optString("twitter");

            message = TextUtils.isEmpty(mStrShareRestoName) ? message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, "") : message.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
            message = TextUtils.isEmpty(restUrl) ? message.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, "") : message.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, restUrl);
            message = TextUtils.isEmpty(mRestoLocality) ? message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, "") : message.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, mRestoLocality);
            if(message.contains(AppConstant.DEAL_PLACE_HOLDER)){
                message = message.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
            }else{
                message = message.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
            }
        }

        return message;
    }

    private void shareOnTwitter(ResolveInfo info) {

        String sharingText = getString(R.string.share_twitter);
        if (isRestaurantSharing) {
            sharingText = getRestaurantSharingMessage("twitter");

        } else {
            String message = prepareBookingMessageForTwitter();
            if (TextUtils.isEmpty(message)) {
                if (!TextUtils.isEmpty(mStrShareRestoName))
                    sharingText = String.format(getString(R.string.share_twitter_rest_url), mStrShareRestoName, restUrl);
            } else {
                sharingText = message;
            }
        }

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        try {
            sharingIntent.setClassName(TWITTER_PACKAGE, "com.twitter.android.composer.ComposerActivity");
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
            startActivityForResult(sharingIntent, MESSAGE_RESULT_CODE);
        } catch (Exception e) {
            sharingIntent.setClassName(TWITTER_PACKAGE, info.activityInfo.name);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
            startActivityForResult(sharingIntent, MESSAGE_RESULT_CODE);
        }
    }

    private void shareOnOthers(ResolveInfo info) {
        String sharingText = getString(R.string.share_default);

        if (isRestaurantSharing) {
            sharingText = getRestaurantSharingMessage("default");

        } else {
            sharingText = getBookingMessageJsonObject().optString("default");
            if(TextUtils.isEmpty(sharingText)){
                sharingText = String.format(getString(R.string.share_fb), mStrShareRestoName, restUrl);
            }
        }


        sharingText = TextUtils.isEmpty(mStrShareRestoName) ? sharingText.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, "") : sharingText.replaceAll(AppConstant.RESTAURANT_NAME_PLACE_HOLDER, mStrShareRestoName);
        sharingText = TextUtils.isEmpty(restUrl) ? sharingText.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, "") : sharingText.replaceAll(AppConstant.TINY_URL_PLACE_HOLDER, restUrl);
        sharingText = TextUtils.isEmpty(mRestoLocality) ? sharingText.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, "") : sharingText.replaceAll(AppConstant.RESTAURANT_LOCALITY_PLACE_HOLDER, mRestoLocality);
        if(sharingText.contains(AppConstant.DEAL_PLACE_HOLDER)){
            sharingText = sharingText.replaceAll(AppConstant.DEAL_PLACE_HOLDER, dealName);
        }else{
            sharingText = sharingText.replaceAll(AppConstant.EVENT_PLACE_HOLDER, dealName);
        }

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, MAIL_SUBJECT);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        startActivityForResult(sharingIntent, GOOGLE_PLUS_RESULT_CODE);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null)
            onCancelListener.onCancel(getDialog());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null)
            getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }
}