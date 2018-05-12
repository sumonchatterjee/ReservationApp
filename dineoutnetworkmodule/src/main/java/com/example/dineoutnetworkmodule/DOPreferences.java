package com.example.dineoutnetworkmodule;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;

import com.freshdesk.hotline.Hotline;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Exchanger;

import java.util.List;
import java.util.concurrent.Exchanger;

import ly.count.android.sdk.Countly;

public class DOPreferences {
    public static final String PARAM_DINER_EMAIL = "diner_email";
    public static final String FAVORITE_COUNT = "favorite_count";
    public static final String PARAM_DINER_IS_DO_PLUS_MEMBER = "diner_ref_code";
    public static final String PARAM_DINER_PHONE = "diner_phone";
    public static final String PARAM_TEMP_BOOKING_CONFIRMATION_DINER_PHONE = "temp_diner_phone";
    private static final String SHOW_STEP_UPLOAD = "steps_upload";
    private static final String IS_FROM_MOCK_PROVIDER = "mock_provider";
    private static final String PREFS_NAME = "DineOut_Shared_Preference";
    private static final String PARAM_DINER_DOB = "d_dob";
    private static final String PARAM_GENDER = "gender";

    private static final String APP_RATING_THRESHOLD = "app_rating_threshold";
    private static final String IS_ACCESSIBILITY_ENABLED_KEY = "is_accessibility_enabled_flag_int";
    private static final String IS_DRAW_OVER_APP_PERMISSION_ENABLED_KEY = "is_draw_over_app_permission_enabled_flag_int";
    public static final String IS_GIRF_ENABLED = "is_girf_enabled";
    private static final String GIRF_HOME_URL = "girf_home_url";

    private static final String BASE_URL_PHP_API = "base_url_php";
    private static final String BASE_URL_NODE_API = "base_url_node";
    private static final String API_BASE_URL = "base_url_environment";
    private static final String API_BASE_URL_NODE = "base_url_environment_node";
    private static final String CURRENT_LAT = "current_lat";
    private static final String CURRENT_LNG = "current_lng";
    private static final String CURRENT_LAT_UPLOAD_BILL = "current_lat_upload";
    private static final String CURRENT_LNG_UPLOAD_BILL = "current_lng_upload";
    private static final String E_LAT = "e_lat";
    private static final String E_LNG = "e_lng";
    private static final String IS_AUTO_MODE = "is_auto_mode";
    private static final String SUGGESTION = "suggestion";
    private static final String SEARCH_FILTER_BY_CITY = "by_city";
    private static final String SEARCH_FILTER_ARR_LOCAREA = "arr_locarea";
    private static final String SEARCH_FILTER_ARR_AREA = "arr_area";
    private static final String IS_FIRST_START = "is_first_start";
    private static final String PLAY_STORE_RATE_REVIEW = "play_store_rate_review";
    private static final String CITY_ID = "dineout_city_id";
    private static final String CITY_NAME = "dineout_city_name";
    private static final String AREA_NAME = "dineout_area_name";
    private static final String LOCALITY_NAME = "dineout_locality_name";
    private static final String REVIEWED_BOOKING_ID = "dineout_reviewed_booking_id";
    private static final String UPLOAD_DISPLAY_MESSAGE = "dinout_upload_msg";
    private static final String AUTH_KEY = "dineout_auth_key";
    private static final String DEVICE_ID = "device_id";
    private static final String GOOGLE_AD_ID = "google_ad_id";
    private static final String RECENT_SEARCH_LOCATION = "recent_search_location";
    private static final String DO_APP_VERSION = "do_app_version";
    private static final String PARAM_DINER_ID = "diner_id";
    private static final String PARAM_DINER_FIRST_NAME = "diner_first_name";
    private static final String PARAM_DINER_LAST_NAME = "diner_last_name";
    private static final String PARAM_DINER_REF_CODE = "diner_reference_code";
    private static final String PARAM_DINER_PROFILE_IMG = "diner_profile_image";
    private static final String PARAM_DINER_IS_NEW_USER = "diner_new_user";
    private static final String PARAM_DINER_DATE_OF_JOINING = "diner_d_o_j";
    private static final String PARAM_DINER_TOTAL_SAVINGS = "diner_tot_savings";
    private static final String SENT_TOKEN_TO_SERVER = "sent_token_to_server";
    private static final String GCM_REGISTRATION_TOKEN = "GCM_REGISTRATION_TOKEN";
    private static final String WAIT_TIME_TITLE = "wait_time_title";
    private static final String WAIT_TIME_MESSAGE = "wait_time_message";
    private static final String BOOKING_SLOT_INTERVAL = "booking_slot_interval";
    private static final String BOOKING_SHARE_MESSAGE = "booking_share_message";
    private static final String SINGLE_DEAL_SHARE_MESSAGE = "single_deal_share_message";
    private static final String MULTIPLE_DEAL_SHARE_MESSAGE = "multiple_deal_share_message";
    private static final String SINGLE_EVENT_SHARE_MESSAGE = "single_event_share_message";
    private static final String MULTIPLE_EVENT_SHARE_MESSAGE = "multiple_event_share_message";
    private static final String FORCE_UPDATE_TITLE = "force_update_title";
    private static final String FORCE_UPDATE_SUB_TITLE = "force_update_sub_title";
    private static final String DOPLUS_CARD_BACKGROUND = "doplus_card_background";
    private static final String PAYMENT_BOOKING_PENDING_MESSAGE = "booking_pending_message";
    private static final String PAYMENT_NOT_AVAILABLE_MESSAGE = "payment_not_available";
    private static final String JP_MILES_ENABLED = "jp_miles_enabled";
    private static final String JP_MILES_KNOW_MORE_LINK = "jp_miles_know_more_link";
    private static final String JP_MILES_KNOW_MORE_TITLE = "jp_miles_know_more_title";
    private static final String JP_MILES_NUMBER = "jp_miles_number";
    private static final String REDEEM_FAIL_MESSAGE = "redeem_fail_message";
    private static final String REDEEM_KNOW_MORE_LINK = "redeem_know_more_link";
    private static final String SMARTPAY_POPUP_MESSAGE = "smartpay_message";
    private static final String INVALID_DATETIME_MESSAGE = "invalid_datetime_message";
    private static final String LOCATION_SF = "location_sf";
    private static final String PROCESS_DOPLUS_NOTIFICATION = "PROCESS_DOPLUS_NOTIFICATION";
    private static final String PHONEPE_ENABLE = "enabled";
    private static final String PHONEPE_TITLE_TXT = "title_text";

    private static final String LOGIN_SOURCE = "loginSource";
    private static final String LOGIN_DATE = "loginDate";
    private static final String LOGIN_DATE_APP_OPEN = "loginDateAppOpen";
    private static final String LOGIN_DATE_APP_CLOSE = "loginDateAppClose";


    private static final String IN_APP_RATING_KEY = "in_app_rating_key";
    private static final String BTN_CTA_BG_MAP_KEY = "btn_cta_bg_map_key";
    private static final String LATEST_NOTIFICATION_TIME = "latest_notification_key";
    private static final String LAST_NOTIFICATION_TIME = "last_notification_key";
    private static final String GO_NOW_TITLE = "go_now_title_key";
    private static final String GO_NOW_SUBTITLE = "go_now_subtitle_key";
    private static final String GO_NOW_ICON_URL = "go_now_icon_key";
    private static final String GO_LATER_TITLE = "go_later_title_key";
    private static final String GO_LATER_SUBTITLE = "go_later_subtitle_key";
    private static final String GO_LATER_ICON_URL = "go_later_icon_key";

    private static final String IS_COACH_MARK_ENABLE = "coach_mark_enable";
    private static final String NEED_TO_SHOW_COACH_MARK = "need_to_show_coach_mark";
    public static final String IS_COUNTLY_ENABLED = "countly_enabled";


    private static final String SEARCH_TXT = "search_text";


    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setApiBaseUrl(Context context, String baseUrl) {
        getSharedPreferences(context).edit().putString(API_BASE_URL, baseUrl).apply();
    }

    public static String getApiBaseUrl(Context context) {
        return getSharedPreferences(context).getString(API_BASE_URL, "");
    }

    public static void setApiBaseUrlNode(Context context, String baseUrl) {
        getSharedPreferences(context).edit().putString(API_BASE_URL_NODE, baseUrl).apply();
    }

    public static String getApiBaseUrlNode(Context context) {
        return getSharedPreferences(context).getString(API_BASE_URL_NODE, "");
    }

    public static void setPhpBaseUrl(Context context, String baseUrl) {
        getSharedPreferences(context).edit().putString(BASE_URL_PHP_API, baseUrl).apply();
    }

    public static String getPhpBaseUrl(Context context) {
        return getSharedPreferences(context).getString(BASE_URL_PHP_API, "");
    }

    public static void setNodeBaseUrl(Context context, String baseNodeUrl) {
        getSharedPreferences(context).edit().putString(BASE_URL_NODE_API, baseNodeUrl).apply();
    }

    public static String getNodeBaseUrl(Context context) {
        return getSharedPreferences(context).getString(BASE_URL_NODE_API, "");
    }

    public static void setSentTokenToServer(Context context, boolean hasSent) {
        getSharedPreferences(context).edit().putBoolean(SENT_TOKEN_TO_SERVER, hasSent).apply();
    }

    public static boolean getSentTokenToServer(Context context) {
        return getSharedPreferences(context).getBoolean(SENT_TOKEN_TO_SERVER, false);
    }

    public static void setGcmRegistrationToken(Context context, String registrationToken) {
        getSharedPreferences(context).edit().putString(GCM_REGISTRATION_TOKEN, registrationToken).apply();
    }

    public static String getGcmRegistrationToken(Context context) {
        return getSharedPreferences(context).getString(GCM_REGISTRATION_TOKEN, "");
    }

    public static void setFirstLaunch(Context context, boolean isFirstLaunching) {
        getSharedPreferences(context).edit().putBoolean(IS_FIRST_START, isFirstLaunching).apply();
    }

    public static boolean checkFirstLaunch(Context context) {
        return getSharedPreferences(context).getBoolean(IS_FIRST_START, true);
    }

    public static String getDeviceId(Context context) {
        return getSharedPreferences(context).getString(DEVICE_ID, "");
    }

    public static void setDeviceId(Context context, String deviceId) {
        getSharedPreferences(context).edit().putString(DEVICE_ID, deviceId).apply();
    }

    public static String getGoogleAdId(Context context) {
        return getSharedPreferences(context).getString(GOOGLE_AD_ID, "");
    }

    public static void setGoogleAdId(Context context, String googleAdId) {
        getSharedPreferences(context).edit().putString(GOOGLE_AD_ID, googleAdId).apply();
    }

    public static void setReviewedBookingId(Context context, String bookingId) {
        getSharedPreferences(context).edit().putString(REVIEWED_BOOKING_ID, bookingId).apply();
    }

    public static String getReviewedBookingId(Context context) {
        return getSharedPreferences(context).getString(REVIEWED_BOOKING_ID, "-1");
    }

    public static String getDOVersion(Context context) {
        return getSharedPreferences(context).getString(DO_APP_VERSION, null);
    }

    public static void setDOVersion(Context context, String timesCityVersion) {
        getSharedPreferences(context).edit().putString(DO_APP_VERSION, timesCityVersion).apply();
    }

    public static void setAuthKey(Context context, String authkey) {
        getSharedPreferences(context).edit().putString(AUTH_KEY, authkey).apply();
    }

    public static String getAuthKey(Context context) {
        return getSharedPreferences(context).getString(AUTH_KEY, "");
    }

    public static boolean showPlayStoreDialog(Context context) {
        return getSharedPreferences(context).getBoolean(PLAY_STORE_RATE_REVIEW, true);
    }

    public static void setPlayStoreDialogStatus(Context context, boolean status) {
        getSharedPreferences(context).edit().putBoolean(PLAY_STORE_RATE_REVIEW, status).apply();
    }

    public static String getELatitude(Context context) {
        return getSharedPreferences(context).getString(E_LAT, null);
    }

    public static String getELongitude(Context context) {
        return getSharedPreferences(context).getString(E_LNG, null);
    }

    public static String getUploadBillLatitude(Context context) {
        return getSharedPreferences(context).getString(CURRENT_LAT_UPLOAD_BILL, null);
    }

    public static String getUploadBillLongitude(Context context) {
        return getSharedPreferences(context).getString(CURRENT_LNG_UPLOAD_BILL, null);
    }

    public static void saveLatAndLong(Context context, Location location) {

        if (location == null)
            return;

        double latitude = location.getLatitude();
        int precision = (int) Math.pow(10, 5);
        double newLatitude = (double) ((int) (precision * latitude)) / precision;

        double longitude = location.getLongitude();
        double newLongitude = (double) ((int) (precision * longitude)) / precision;
        getSharedPreferences(context).edit().putString(CURRENT_LAT, newLatitude + "").apply();
        getSharedPreferences(context).edit().putString(CURRENT_LNG, newLongitude + "").apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getSharedPreferences(context).edit().putBoolean(IS_FROM_MOCK_PROVIDER,
                    location.isFromMockProvider()).apply();
        }
    }


    public static void saveLatAndLongForUploadBill(Context context, Location location) {

        if (location == null)
            return;

        getSharedPreferences(context).edit().putString(CURRENT_LAT_UPLOAD_BILL, location.getLatitude() + "").apply();
        getSharedPreferences(context).edit().putString(CURRENT_LNG_UPLOAD_BILL, location.getLongitude() + "").apply();


    }

    public static boolean isMockProviderLocation(Context context) {
        return getSharedPreferences(context).getBoolean(IS_FROM_MOCK_PROVIDER, false);
    }

    public static void putRecentLocation(Context context, String recent) {
        getSharedPreferences(context).edit().putString(RECENT_SEARCH_LOCATION, recent).apply();
    }

    public static String getRecentLocation(Context context) {
        return getSharedPreferences(context).getString(RECENT_SEARCH_LOCATION, null);
    }

    public static void saveDinerCredentials(Context context, JSONObject userJsonObject) {
        if (userJsonObject != null && context != null) {
            SharedPreferences.Editor edit = getSharedPreferences(context).edit();

            edit.putString(PARAM_DINER_ID, userJsonObject.optString("d_id"));
            edit.putString(PARAM_DINER_FIRST_NAME, userJsonObject.optString("d_first_name"));
            edit.putString(PARAM_DINER_LAST_NAME, userJsonObject.optString("d_last_name"));
            edit.putString(PARAM_DINER_EMAIL, userJsonObject.optString("d_email"));
            edit.putString(PARAM_DINER_PHONE, userJsonObject.optString("d_phone"));
            edit.putString(PARAM_DINER_DOB, userJsonObject.optString("d_dob"));
            edit.putString(PARAM_GENDER, userJsonObject.optString("gender"));
            edit.putString(PARAM_DINER_REF_CODE, userJsonObject.optString("ref_code"));
            edit.putString(PARAM_DINER_IS_DO_PLUS_MEMBER, userJsonObject.optString("is_do_plus_member"));
            edit.putString(PARAM_DINER_PROFILE_IMG, userJsonObject.optString("profile_image"));
            edit.putString(PARAM_DINER_IS_NEW_USER, userJsonObject.optString("new_user"));
            edit.putString(PARAM_DINER_DATE_OF_JOINING, userJsonObject.optString("d_dt"));
            edit.putString(PARAM_DINER_TOTAL_SAVINGS, userJsonObject.optString("total_savings"));
            edit.putString(JP_MILES_NUMBER, userJsonObject.optString("jp_number"));

            edit.apply();
        }
    }

    public static String getDinerId(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_ID, "");
    }

    public static String getDinerProfileImage(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_PROFILE_IMG, null);
    }

    public static String getDinerFirstName(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_FIRST_NAME, "");
    }

    public static String getDinerLastName(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_LAST_NAME, "");
    }

    public static String getDinerDateOfJoining(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_DATE_OF_JOINING, "");
    }

    public static String getDinerDateOfbirth(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_DOB, "");
    }


    public static void setDinerEmail(Context context, String email) {
        getSharedPreferences(context).edit().putString(PARAM_DINER_EMAIL, email).apply();
    }

    public static String getDinerEmail(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_EMAIL, "");
    }

    public static void setDinerPhone(Context context, String phone) {
        getSharedPreferences(context).edit().putString(PARAM_DINER_PHONE, phone).apply();
    }

    public static String getDinerPhone(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_PHONE, "");
    }

    public static void setTempDinerPhone(Context context, String phone) {
        getSharedPreferences(context).edit().putString(PARAM_TEMP_BOOKING_CONFIRMATION_DINER_PHONE, phone).apply();
    }

    public static String getTempDinerPhone(Context context) {
        return getSharedPreferences(context).getString(PARAM_TEMP_BOOKING_CONFIRMATION_DINER_PHONE, getDinerPhone(context));
    }

    public static String isDinerDoPlusMember(Context context) {
        return getSharedPreferences(context).getString(PARAM_DINER_IS_DO_PLUS_MEMBER, "0");
    }

    public static boolean isDinerNewUser(Context context) {
        String strValue = getSharedPreferences(context).getString(PARAM_DINER_IS_NEW_USER, "");
        return ("1".equals(strValue));
    }

    public static void setDinerNewUser(Context context, String value) {
        getSharedPreferences(context).edit().putString(PARAM_DINER_IS_NEW_USER, value).apply();
    }

    public static void setIsDinerDoPlusMember(Context context, String isDOPlusMember) {
        getSharedPreferences(context).edit().putString(PARAM_DINER_IS_DO_PLUS_MEMBER, isDOPlusMember).apply();
    }

    public static void setDinerGender(Context context, String gender) {
        getSharedPreferences(context).edit().putString(PARAM_GENDER, gender).apply();
    }

    public static String getDinerGender(Context context) {
        return getSharedPreferences(context).getString(PARAM_GENDER, "");
    }


    public static void deleteDinerCredentials(Context context) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString(PARAM_DINER_ID, "");
        edit.putString(PARAM_DINER_FIRST_NAME, "");
        edit.putString(PARAM_DINER_LAST_NAME, "");
        edit.putString(PARAM_DINER_EMAIL, "");
        edit.putString(PARAM_DINER_PHONE, "");
        edit.putString(PARAM_DINER_DOB, "");
        edit.putString(PARAM_DINER_REF_CODE, "");
        edit.putString(PARAM_DINER_IS_DO_PLUS_MEMBER, "0");
        edit.putString(PARAM_DINER_PROFILE_IMG, "");
        edit.putBoolean(PARAM_DINER_IS_NEW_USER, false);
        edit.putString(PARAM_DINER_DATE_OF_JOINING, "");
        edit.putString(PARAM_DINER_TOTAL_SAVINGS, "");
        edit.putString(JP_MILES_NUMBER, "");
        edit.putString(AUTH_KEY, null);
        edit.putBoolean(SHOW_STEP_UPLOAD, true);
        edit.putString(IN_APP_RATING_KEY, "");
        edit.apply();

        Hotline.clearUserData(context);
    }

    public static void saveLocationDetails(Context context, JSONObject jsonObject, boolean isAutoMode) {
        if (jsonObject != null && context != null) {
            SharedPreferences.Editor edit = getSharedPreferences(context).edit();


            double latitude = jsonObject.optDouble("lat");
            double longitude = jsonObject.optDouble("lng");
            int precision = (int) Math.pow(10, 5);
            double newLatitude = (double) ((int) (precision * latitude)) / precision;
            double newLongitude = (double) ((int) (precision * longitude)) / precision;
            edit.putBoolean(IS_AUTO_MODE, isAutoMode);
            edit.putString(E_LAT, Double.toString(newLatitude));
            edit.putString(E_LNG, Double.toString(newLongitude));
            edit.putString(CITY_ID, jsonObject.optString("city_id", ""));
            edit.putString(CITY_NAME, jsonObject.optString("city_name", ""));
            edit.putString(AREA_NAME, jsonObject.optString("area_name", ""));
            edit.putString(LOCALITY_NAME, jsonObject.optString("location_name", ""));
            edit.putString(SUGGESTION, jsonObject.optString("suggestion", ""));

            JSONObject searchFilterJsonObject = jsonObject.optJSONObject("sf");

            if (searchFilterJsonObject != null) {
                edit.putString(SEARCH_FILTER_BY_CITY, searchFilterJsonObject.optString("by_city", ""));
                edit.putString(SEARCH_FILTER_ARR_LOCAREA, searchFilterJsonObject.optString("arr_locarea", ""));
                edit.putString(SEARCH_FILTER_ARR_AREA, searchFilterJsonObject.optString("arr_area", ""));
                edit.putString(LOCATION_SF, searchFilterJsonObject.toString());
            }

            edit.apply();
        }
    }

    public static String getLocationSf(Context context) {
        return DOPreferences.getSharedPreferences(context).getString(LOCATION_SF, null);
    }

    public static boolean isAutoMode(Context context) {
        return getSharedPreferences(context).getBoolean(IS_AUTO_MODE, false);
    }

    public static void setAutoMode(Context context, boolean status) {
        getSharedPreferences(context).edit().putBoolean(IS_AUTO_MODE, status).apply();
    }

    public static String getCurrentLatitude(Context context) {
        return DOPreferences.getSharedPreferences(context).getString(CURRENT_LAT, null);
    }

    public static String getCurrentLongitude(Context context) {
        return DOPreferences.getSharedPreferences(context).getString(CURRENT_LNG, null);
    }

    public static String getCityId(Context context) {
        return getSharedPreferences(context).getString(CITY_ID, "");
    }

    public static String getCityName(Context context) {
        return getSharedPreferences(context).getString(CITY_NAME, "");
    }

    public static String getAreaName(Context context) {
        return getSharedPreferences(context).getString(AREA_NAME, "");
    }

    public static String getLocalityName(Context context) {
        return getSharedPreferences(context).getString(LOCALITY_NAME, "");
    }

    public static String getSuggestion(Context context) {
        return getSharedPreferences(context).getString(SUGGESTION, "");
    }

    public static String getSfByCity(Context context) {
        return getSharedPreferences(context).getString(SEARCH_FILTER_BY_CITY, "");
    }

    public static String getSfArrLocarea(Context context) {
        return getSharedPreferences(context).getString(SEARCH_FILTER_ARR_LOCAREA, "");
    }

    public static String getSfArrArea(Context context) {
        return getSharedPreferences(context).getString(SEARCH_FILTER_ARR_AREA, "");
    }

    public static HashMap<String, Object> getUserDataJSONObject(Context context) {
        HashMap<String, Object> props = new HashMap<>();

        if (context != null &&
                !TextUtils.isEmpty(DOPreferences.getDinerEmail(context.getApplicationContext()))) {

            props.put("email", DOPreferences.getDinerEmail(context.getApplicationContext()));
            props.put("name", DOPreferences.getDinerFirstName(context) + " " +
                    DOPreferences.getDinerLastName(context));
            props.put("avatarUrl", DOPreferences.getDinerProfileImage(context));
            props.put("phone", DOPreferences.getDinerPhone(context));
            props.put("isDineoutPlusMember", DOPreferences.isDinerDoPlusMember(context));
            props.put("dinnerId", DOPreferences.getDinerId(context));
            props.put("cityId", DOPreferences.getCityId(context));
        }

        return props;
    }

    public static void saveAppConfig(Context context, JSONObject appConfigJsonObject) {
        if (context != null && appConfigJsonObject != null) {
            SharedPreferences.Editor edit = getSharedPreferences(context).edit();

//            if (appConfigJsonObject.optJSONObject("system_config") != null) {
//                edit.putString(ACCESSIBILITY_ENABLED,
//                        appConfigJsonObject.optJSONObject("system_config").optString("accessibility_enabled"));
//            }

            if (appConfigJsonObject.optJSONObject("booking_config") != null) {
                edit.putString(WAIT_TIME_TITLE,
                        appConfigJsonObject.optJSONObject("booking_config").optString("waiting_title"));
                edit.putString(WAIT_TIME_MESSAGE,
                        appConfigJsonObject.optJSONObject("booking_config").optString("waiting_message"));
                edit.putString(BOOKING_SLOT_INTERVAL,
                        appConfigJsonObject.optJSONObject("booking_config").optString("booking_slot_interval"));
                edit.putString(BOOKING_SHARE_MESSAGE,
                        appConfigJsonObject.optJSONObject("booking_config").optString("booking_share_message"));

                if (TextUtils.isEmpty(appConfigJsonObject.optJSONObject("booking_config").optString("jp_miles_enabled")) ||
                        "0".equals(appConfigJsonObject.optJSONObject("booking_config").optString("jp_miles_enabled"))) {
                    edit.putBoolean(JP_MILES_ENABLED, false);
                } else if ("1".equals(appConfigJsonObject.optJSONObject("booking_config").optString("jp_miles_enabled"))) {
                    edit.putBoolean(JP_MILES_ENABLED, true);
                } else {
                    edit.putBoolean(JP_MILES_ENABLED, false);
                }

                edit.putString(JP_MILES_KNOW_MORE_LINK,
                        appConfigJsonObject.optJSONObject("booking_config").optString("jp_miles_know_more_link"));

                edit.putString(JP_MILES_KNOW_MORE_TITLE,
                        appConfigJsonObject.optJSONObject("booking_config").optString("jp_miles_know_more_title"));


                JSONObject goingMsg=appConfigJsonObject.optJSONObject("booking_config").optJSONObject("booking_going_message");

                if(goingMsg!=null){

                    JSONObject goingNow = goingMsg.optJSONObject("going_now");
                    JSONObject goingLater = goingMsg.optJSONObject("going_later");

                    if(goingNow!=null){
                        edit.putString(GO_NOW_TITLE,
                                goingNow.optString("title"));
                        edit.putString(GO_NOW_SUBTITLE,
                                goingNow.optString("sub_title"));
                        edit.putString(GO_NOW_ICON_URL,
                                goingNow.optString("icon"));
                    }
                    if(goingLater!=null){
                        edit.putString(GO_LATER_TITLE,
                                goingLater.optString("title"));
                        edit.putString(GO_LATER_SUBTITLE,
                                goingLater.optString("sub_title"));
                        edit.putString(GO_LATER_ICON_URL,
                                goingLater.optString("icon"));
                    }
                }


            }
            String isCoachMarkEnable = appConfigJsonObject.optString("coach_mark_enable");
            if (TextUtils.isEmpty(isCoachMarkEnable) ||
                    "0".equals(isCoachMarkEnable)) {
                edit.putBoolean(IS_COACH_MARK_ENABLE, false);
                //edit.putBoolean(NEED_TO_SHOW_COACH_MARK,false);
            } else if ("1".equals(isCoachMarkEnable)) {
                edit.putBoolean(IS_COACH_MARK_ENABLE, true);
                //edit.putBoolean(NEED_TO_SHOW_COACH_MARK,true);
            } else {
                edit.putBoolean(IS_COACH_MARK_ENABLE, true);
               // edit.putBoolean(NEED_TO_SHOW_COACH_MARK,false);
            }


            if (appConfigJsonObject.optJSONObject("force_update_config") != null) {
                edit.putString(FORCE_UPDATE_TITLE,
                        appConfigJsonObject.optJSONObject("force_update_config").optString("title"));
                edit.putString(FORCE_UPDATE_SUB_TITLE,
                        appConfigJsonObject.optJSONObject("force_update_config").optString("sub_title"));
            }

            if (appConfigJsonObject.optJSONObject("doplus_config") != null) {
                edit.putString(DOPLUS_CARD_BACKGROUND,
                        appConfigJsonObject.optJSONObject("doplus_config").optString("doplus_card_background"));
            }

            if (appConfigJsonObject.optJSONObject("payment_config") != null) {
                edit.putString(PAYMENT_BOOKING_PENDING_MESSAGE,
                        appConfigJsonObject.optJSONObject("payment_config").optString("booking_pending_message"));
                edit.putString(PAYMENT_NOT_AVAILABLE_MESSAGE,
                        appConfigJsonObject.optJSONObject("payment_config").optString("payment_not_available"));
            }

            if (appConfigJsonObject.optJSONObject("deal_config") != null) {
                edit.putString(SINGLE_DEAL_SHARE_MESSAGE,
                        appConfigJsonObject.optJSONObject("deal_config").optString("single_deal_share_message"));
                edit.putString(MULTIPLE_DEAL_SHARE_MESSAGE,
                        appConfigJsonObject.optJSONObject("deal_config").optString("multiple_deal_share_message"));
                edit.putString(REDEEM_FAIL_MESSAGE,
                        appConfigJsonObject.optJSONObject("deal_config").optString("redeem_fail_message"));
                edit.putString(REDEEM_KNOW_MORE_LINK,
                        appConfigJsonObject.optJSONObject("deal_config").optString("redeem_know_more_link"));
            }

            if (appConfigJsonObject.optJSONObject("event_config") != null) {
                edit.putString(SINGLE_EVENT_SHARE_MESSAGE,
                        appConfigJsonObject.optJSONObject("event_config").optString("single_event_share_message"));
                edit.putString(MULTIPLE_EVENT_SHARE_MESSAGE,
                        appConfigJsonObject.optJSONObject("event_config").optString("multiple_event_share_message"));
            }

            if (appConfigJsonObject.optJSONObject("restaurant_config") != null) {
                edit.putString(SMARTPAY_POPUP_MESSAGE,
                        appConfigJsonObject.optJSONObject("restaurant_config").optString("smartpay_message"));
                edit.putString(INVALID_DATETIME_MESSAGE,
                        appConfigJsonObject.optJSONObject("restaurant_config").optString("invalid_datetime_message"));
            }

            if (appConfigJsonObject.optJSONObject("account_config") != null) {
                if (appConfigJsonObject.optJSONObject("account_config").optJSONObject("phonepe") != null) {
                    edit.putString(PHONEPE_ENABLE,
                            appConfigJsonObject.optJSONObject("account_config").optJSONObject("phonepe").optString("enabled"));
                    edit.putString(PHONEPE_TITLE_TXT,
                            appConfigJsonObject.optJSONObject("account_config").optJSONObject("phonepe").optString("title_text"));
                }
            }

            if (appConfigJsonObject.optJSONObject("meta") != null) {
                if (appConfigJsonObject.optJSONObject("meta").optJSONObject("button_bg_data") != null) {
                    edit.putString(BTN_CTA_BG_MAP_KEY,
                            appConfigJsonObject.optJSONObject("meta").optJSONObject("button_bg_data").toString());
                }
            }

            edit.apply();
        }
    }

    public static String getBookingShareMessage(Context context) {
        return getSharedPreferences(context).getString(BOOKING_SHARE_MESSAGE, "");
    }

    public static String getSingleDealShareMessage(Context context) {
        return getSharedPreferences(context).getString(SINGLE_DEAL_SHARE_MESSAGE, "");
    }

    public static String getMultipleDealShareMessage(Context context) {
        return getSharedPreferences(context).getString(MULTIPLE_DEAL_SHARE_MESSAGE, "");
    }

    public static String getSingleEventShareMessage(Context context) {
        return getSharedPreferences(context).getString(SINGLE_EVENT_SHARE_MESSAGE, "");
    }

    public static String getMultipleEventShareMessage(Context context) {
        return getSharedPreferences(context).getString(MULTIPLE_EVENT_SHARE_MESSAGE, "");
    }

    public static String getSmartpayPopupMessage(Context context) {
        return getSharedPreferences(context).getString(SMARTPAY_POPUP_MESSAGE, "");
    }

    public static String getInvalidDatetimeMessage(Context context) {
        return getSharedPreferences(context).getString(INVALID_DATETIME_MESSAGE, "");
    }

    public static String getForceUpdateTitle(Context context) {
        return getSharedPreferences(context).getString(FORCE_UPDATE_TITLE, "Important app update.");
    }

    public static String getForceUpdateSubTitle(Context context) {
        return getSharedPreferences(context).getString(FORCE_UPDATE_SUB_TITLE,
                "A new app version is available for update. Please update now to continue using the app!");
    }

    public static String getDoPlusCardBackground(Context context) {
        return getSharedPreferences(context).getString(DOPLUS_CARD_BACKGROUND, "");
    }

    public static boolean isJpMilesEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(JP_MILES_ENABLED, false);
    }

    public static String getJpMilesNumber(Context context) {
        return getSharedPreferences(context).getString(JP_MILES_NUMBER, "");
    }

    public static void setJpMilesNumber(Context context, String jpMilesNumber) {
        getSharedPreferences(context).edit().putString(JP_MILES_NUMBER, jpMilesNumber).apply();
    }

    public static String getUploadDisplayMessage(Context context) {
        return getSharedPreferences(context).getString(UPLOAD_DISPLAY_MESSAGE,
                "We are uploading your bill in background, will notify once it will be updated successfully.");
    }

    public static void setUploadDisplayMessage(Context context, String msg) {
        getSharedPreferences(context).edit().putString(UPLOAD_DISPLAY_MESSAGE, msg).apply();
    }

    public static String getJpMilesKnowMoreLink(Context context) {
        return getSharedPreferences(context).getString(JP_MILES_KNOW_MORE_LINK, "");
    }

    public static String getWaitTimeTitle(Context context) {
        return getSharedPreferences(context).getString(WAIT_TIME_TITLE, "FIRST COME, FIRST SERVE");
    }

    public static String getWaitTimeMessage(Context context) {
        return getSharedPreferences(context).getString(WAIT_TIME_MESSAGE, "Your details will be forwarded to #restaurant_name# as this Restaurant does not accept reservations at this hour and accepts only walk-in customers.");
    }

    public static String getJpMilesKnowMoreTitle(Context context) {
        return getSharedPreferences(context).getString(JP_MILES_KNOW_MORE_TITLE, "About JP Miles");
    }

    public static void setShowStepUpload(Context context, boolean show) {
        getSharedPreferences(context).edit().putBoolean(SHOW_STEP_UPLOAD, show).apply();
    }

    public static boolean isShowStepUpload(Context context) {
        return getSharedPreferences(context).getBoolean(SHOW_STEP_UPLOAD, true);
    }

    public static void setHasDoPlusDeeplink(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean(PROCESS_DOPLUS_NOTIFICATION, value).apply();
    }

    public static boolean hasDoPlusDeeplink(Context context) {
        return getSharedPreferences(context).getBoolean(PROCESS_DOPLUS_NOTIFICATION, false);
    }

    public static int retrieveAppRatingThresholdValue(Context context) {
        return getSharedPreferences(context).getInt(APP_RATING_THRESHOLD, 4);
    }

//    public static int isAccessibilityEnabled(Context context) {
//        return getSharedPreferences(context).getInt(IS_ACCESSIBILITY_ENABLED_KEY, -1);
//    }
//
//    public static void setAccessibilityEnabled(Context context, int value) {
//        getSharedPreferences(context).edit().putInt(IS_ACCESSIBILITY_ENABLED_KEY, value).apply();
//    }
//
//    public static int isDrawOverAppEnabled(Context context) {
//        return getSharedPreferences(context).getInt(IS_DRAW_OVER_APP_PERMISSION_ENABLED_KEY, -1);
//    }
//
//    public static void setDrawOverAppEnabled(Context context, int value) {
//        getSharedPreferences(context).edit().putInt(IS_DRAW_OVER_APP_PERMISSION_ENABLED_KEY, value).apply();
//    }


    public static boolean isGIRFEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(IS_GIRF_ENABLED, false);
    }

    public static void setGIRFEnabled(Context context, boolean flag) {
        getSharedPreferences(context).edit().putBoolean(IS_GIRF_ENABLED, flag).apply();
    }

    public static String getGIRFURL(Context context) {
        String url = getSharedPreferences(context).getString(GIRF_HOME_URL, "");
        if (!TextUtils.isEmpty(url)) {
            return getParsedGIRFUrl(context, url);
        }

        return "";
    }

    private static String getParsedGIRFUrl(Context context, String girfUrl) {
        String newUrl = girfUrl.replace("##city##", DOPreferences.getCityName(context));
        return newUrl;

    }

    public static void setGIRFURL(Context context, String url) {
        getSharedPreferences(context).edit().putString(GIRF_HOME_URL, url).apply();
    }

    public static String getPhonePeStatus(Context context) {
        return getSharedPreferences(context).getString(PHONEPE_ENABLE, "");
    }

    public static String getPhonePeTitle(Context context) {
        return getSharedPreferences(context).getString(PHONEPE_TITLE_TXT, "");
    }


    public static void isCoachMarkEnabledFromConfig(Context context,boolean coachMarkEnable) {
        getSharedPreferences(context).edit().putBoolean(IS_COACH_MARK_ENABLE, coachMarkEnable).apply();
    }

    public static boolean getIsCoachMarkEnabledFromConfig(Context context) {
        return getSharedPreferences(context).getBoolean(IS_COACH_MARK_ENABLE,false);
    }

    public static void setNeedtoShowCoachMark(Context context,boolean needToBeShown) {
        getSharedPreferences(context).edit().putBoolean(NEED_TO_SHOW_COACH_MARK, needToBeShown).apply();
    }

    public static boolean getNeedToShowCoachMark(Context context) {
        return getSharedPreferences(context).getBoolean(NEED_TO_SHOW_COACH_MARK,true);
    }


//    public static HashMap<String, String> getGeneralEventParameters(Context context) {
//        HashMap<String, String> props = new HashMap<>();
//
//        if (context != null) {
//
//            props.put("deviceId", DOPreferences.getDeviceId(context.getApplicationContext()));
//
//            if(!TextUtils.isEmpty(DOPreferences.getDinerId(context))){
//                props.put("userId", DOPreferences.getDinerId(context));
//            }
//
//           // props.put("sessionId", "");
//
//            if(!TextUtils.isEmpty(DOPreferences.getAreaName(context.getApplicationContext()))){
//                props.put("area", DOPreferences.getAreaName(context.getApplicationContext()));
//            }
//
//            if(!TextUtils.isEmpty(DOPreferences.getCityName(context.getApplicationContext()))){
//                props.put("city", DOPreferences.getCityName(context.getApplicationContext()));
//            }
//
//            if(!TextUtils.isEmpty(DOPreferences.getCurrentLatitude(context.getApplicationContext())) &&
//                    !TextUtils.isEmpty(DOPreferences.getCurrentLongitude(context.getApplicationContext()))) {
//
//                props.put("latlng", DOPreferences.getCurrentLatitude(context.getApplicationContext()) + ","
//                        + DOPreferences.getCurrentLongitude(context.getApplicationContext()));
//            }
//
//            long seconds = System.currentTimeMillis() / 1000l;
//            props.put("timestamp", Long.toString(seconds));
//
//            List<String>searches = SearchUtils.getSearchHistoryList(context);
//            if(searches!=null){
//                if(searches.size()>0){
//                    String userSearch = android.text.TextUtils.join(",", searches);
//                    props.put("lastThreeSearches", userSearch);
//                }
//            }
////            props.put("facebookId", "");
////            props.put("googleId", "");
//
//        }
//        return props;
//    }


    public static HashMap<String, Object> getGeneralEventParametersForQgraph(Context context) {
        HashMap<String, Object> props = new HashMap<>();

        if (context != null) {

            if (!TextUtils.isEmpty(DOPreferences.getCityName(context.getApplicationContext()))) {
                props.put("city", DOPreferences.getCityName(context.getApplicationContext()));
            }

            if (!TextUtils.isEmpty(DOPreferences.getDinerId(context))) {
                props.put("userId", DOPreferences.getDinerId(context));
            }

//            if (!TextUtils.isEmpty(DOPreferences.getDinerFirstName(context.getApplicationContext()))) {
//                props.put("name", DOPreferences.getDinerFirstName(context.getApplicationContext()));
//            }
//
//            if (!TextUtils.isEmpty(DOPreferences.getDinerEmail(context.getApplicationContext()))) {
//                props.put("email", DOPreferences.getDinerEmail(context.getApplicationContext()));
//            }
//
//            if (!TextUtils.isEmpty(DOPreferences.getDinerPhone(context.getApplicationContext()))) {
//                props.put("mobile", DOPreferences.getDinerPhone(context.getApplicationContext()));
//            }

            long millisec = System.currentTimeMillis();
//            int days = (int) (millisec / (1000*60*60*24));
//            props.put("day", Integer.toString(days));


        }
        return props;
    }


    public static void setloginSource(Context context, String loginSource) {
        getSharedPreferences(context).edit().putString(LOGIN_SOURCE, loginSource).apply();
    }

    public static String getLoginSource(Context context) {
        return getSharedPreferences(context).getString(LOGIN_SOURCE, "");
    }

    public static void setloginDate(Context context, String loginDate) {
        getSharedPreferences(context).edit().putString(LOGIN_DATE, loginDate).apply();
    }

    public static String getLoginDate(Context context) {
        return getSharedPreferences(context).getString(LOGIN_DATE, "");

    }


    public static String getInAppRatingJSON(Context context) {
        return getSharedPreferences(context).getString(IN_APP_RATING_KEY, "");
    }

    public static void setInAppRatingJSON(Context context, String value) {
        getSharedPreferences(context).edit().putString(IN_APP_RATING_KEY, value).commit();

    }

    public static void setLoginDateForFirstTimeAppOpen(Context context, String loginDate) {
        getSharedPreferences(context).edit().putString(LOGIN_DATE_APP_OPEN, loginDate).apply();
    }

    public static String getLoginDateForFirstTimeAppOpen(Context context) {
        return getSharedPreferences(context).getString(LOGIN_DATE_APP_OPEN, "");

    }

    public static void setLoginDateForFirstTimeAppClose(Context context, String loginDate) {
        getSharedPreferences(context).edit().putString(LOGIN_DATE_APP_CLOSE, loginDate).apply();
    }

    public static String getLoginDateForFirstTimeAppClose(Context context) {
        return getSharedPreferences(context).getString(LOGIN_DATE_APP_CLOSE, "");

    }

    public static long getLatestNotificationTime(Context context) {
        return getSharedPreferences(context).getLong(LATEST_NOTIFICATION_TIME, 0L);
    }

    public static void setLatestNotificationTime(Context context, long value) {
        getSharedPreferences(context).edit().putLong(LATEST_NOTIFICATION_TIME, value).commit();
    }

    public static long getNotificationSeenTime(Context context) {
        return getSharedPreferences(context).getLong(LAST_NOTIFICATION_TIME, 0L);
    }

    public static void setNotificationSeenTime(Context context, long value) {
        getSharedPreferences(context).edit().putLong(LAST_NOTIFICATION_TIME, value).commit();
    }

    public static String getGoNowTitle(Context context) {
        return getSharedPreferences(context).getString(GO_NOW_TITLE, "");
    }

    public static String getGONowSubtitle(Context context) {
        return getSharedPreferences(context).getString(GO_NOW_SUBTITLE, "");
    }

    public static String getGoNowIconUrl(Context context) {
        return getSharedPreferences(context).getString(GO_NOW_ICON_URL, "");
    }

    public static String getGoLaterTitle(Context context) {
        return getSharedPreferences(context).getString(GO_LATER_TITLE, "");
    }

    public static String getGoLaterSubtitle(Context context) {
        return getSharedPreferences(context).getString(GO_LATER_SUBTITLE, "");
    }

    public static String getGoLaterIconUrl(Context context) {
        return getSharedPreferences(context).getString(GO_LATER_ICON_URL, "");
    }


    public static HashMap<String, String> getGeneralEventParameters(Context context) {
        HashMap<String, String> props = new HashMap<>();

        if (context != null) {


            if (!TextUtils.isEmpty(DOPreferences.getDinerId(context))) {
                props.put("userId", DOPreferences.getDinerId(context));
            }


            if (!TextUtils.isEmpty(DOPreferences.getAreaName(context.getApplicationContext()))) {
                props.put("area", DOPreferences.getAreaName(context.getApplicationContext()));
            }

            if (!TextUtils.isEmpty(DOPreferences.getCityName(context.getApplicationContext()))) {
                props.put("city", DOPreferences.getCityName(context.getApplicationContext()));
            }

            if (!TextUtils.isEmpty(DOPreferences.getCurrentLatitude(context.getApplicationContext())) &&
                    !TextUtils.isEmpty(DOPreferences.getCurrentLongitude(context.getApplicationContext()))) {

                props.put("latlng", DOPreferences.getCurrentLatitude(context.getApplicationContext()) + ","
                        + DOPreferences.getCurrentLongitude(context.getApplicationContext()));
            }

            List<String> searches = SearchUtils.getSearchHistoryList(context);
            if (searches != null) {
                if (searches.size() > 0) {
                    String userSearch = android.text.TextUtils.join(",", searches);
                    props.put("lastThreeSearches", userSearch);
                }
            }


            //THIS DEVICE ID IS GOING VIA COUNTLY
            //props.put("deviceId", DOPreferences.getDeviceId(context.getApplicationContext()));

            //THIS IS INCLUDED IN COUNLTY
//            try {
//                PackageManager manager = context.getPackageManager();
//                PackageInfo info = manager.getPackageInfo(
//                        context.getPackageName(), 0);
//                String version = info.versionName;
//                props.put("appVersion",version);
//            } catch (Exception e) {
//
//            }

//            long seconds = System.currentTimeMillis() / 1000l;
//            props.put("timestamp", Long.toString(seconds));

//            props.put("facebookId", "");
//            props.put("googleId", "");

        }
        return props;
    }


    public static String getCTABtnBgColorFromDOPref(Context context) {
        return getSharedPreferences(context).getString(BTN_CTA_BG_MAP_KEY, "");
    }



    public static boolean isCountlyEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(IS_COUNTLY_ENABLED, true);
    }

    public static void setIsCountlyEnabled(Context context, boolean flag) {
        getSharedPreferences(context).edit().putBoolean(IS_COUNTLY_ENABLED, flag).apply();
    }
}



