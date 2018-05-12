package com.example.dineoutnetworkmodule;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.dineoutnetworkmodule.AppConstant.PARAM_DEEPLINK;
import static com.example.dineoutnetworkmodule.AppConstant.PARAM_TEMP;

public class ApiParams {

    // rating screen params
    private static String RATING_SCREEN_TAG = "tags";
    private static final String RATING_SCREEN_RATING = "rating";
    private static final String RATING_SCREEN_FEEDBACK = "feedback";
    private static final String RATING_SCREEN_DEVICE = "device";
    private static final String RATING_SCREEN_OS_VERSION = "os_version";
    public static final String APP_RATING = "app_rating";
    public static final String RESTAURANT_RATING = "review";
    public static String DOPLUS_TYPE = "doplus";
    public static String BOOKING_TYPE = "booking";
    public static String RESTAURANT_TYPE = "restaurant";
    public static int PAYMENT_TYPE_PAYU = 0;
    public static int PAYMENT_TYPE_MOBIKWIK = 2;
    public static int PAYMENT_TYPE_PAYTM = 1;
    public static String PARAM_START = "start";
    private static String PARAM_AUTH_KEY = "ak";
    private static String PARAM_DINER_ID = "diner_id";
    private static String DEVICE_TYPE = "android";
    private static String PARAM_UBER_SERVER_TOKEN = "server_token";
    private static String PARAM_UBER_START_LATITUDE = "start_latitude";
    private static String PARAM_UBER_START_LONGITUDE = "start_longitude";
    private static String PARAM_UBER_END_LATITUDE = "end_latitude";
    private static String PARAM_UBER_END_LONGITUDE = "end_longitude";
    private static String PARAM_RES_ID = "res_id";
    private static String PARAM_REST_ID = "rest_id";
    private static String PARAM_REST_CITY_ID = "rest_city_id";
    private static String PARAM_RESTAURANT_ID = "restaurant_id";
    private static String PARAM_ACTION = "action";
    private static String PARAM_UKEY = "ukey";
    private static String PARAM_CITY_ID = "city_id";
    private static String PARAM_PHONE = "phone";
    private static String PARAM_NAME = "name";
    private static String PARAM_FIRST_NAME = "first_name";
    private static String PARAM_LAST_NAME = "last_name";
    private static String PARAM_EMAIL = "email";
    private static String PARAM_IMAGE = "image";
    private static String PARAM_UPDATE_IMAGE = "update_image";
    private static String PARAM_PAGE = "page";
    private static String PARAM_LAT = "lat";
    private static String PARAM_LONG = "long";
    private static String PARAM_LNG = "lng";
    private static String PARAM_BY_CITY = "by_city";
    private static String PARAM_ARR_LOCAREA = "arr_locarea";
    private static String PARAM_ARR_AREA = "arr_area";
    private static String PARAM_TIMESTAMP = "ts";
    private static String PARAM_OTP_NEW_PASSWORD = "new_password";
    private static String PARAM_OTP_CONFIRM_PASSWORD = "confirm_password";
    private static String PARAM_OTP_ID = "otp_id";
    private static String PARAM_OTP = "otp";
    private static String DINER_COUNT = "bydinerCount";
    private static String PARAM_DINER_NAME = "diner_name";
    private static String PARAM_DINER_EMAIL = "diner_email";
    private static String PARAM_DINER_PHONE = "diner_phone";
    private static String PARAM_SPECIAL_REQUEST = "spcl_req";
    private static String PARAM_REF_CODE = "ref_code";
    private static String PARAM_REVIEW_TYPE = "review_type";
    private static String REVIEW_TYPE_GENERIC = "generic";
    private static String REVIEW_TYPE_AUTHENTIC = "authentic";
    private static String PARAM_B_ID = "b_id";
    private static String PARAM_RATING_FOOD = "rating_food";
    private static String PARAM_RATING_SERVICE = "rating_service";
    private static String PARAM_RATING_AMBIENCE = "rating_ambience";
    private static String PARAM_RATING_DESC = "rating_desc";
    private static String PARAM_CITY_NAME = "city_name";
    private static String PARAM_PASSWORD = "password";
    private static String PARAM_CONFIRM_PASSWORD = "confirm_password";
    private static String PARAM_TYPE = "type";
    private static String PARAM_ACCESS_TOKEN = "access_token";
    private static String PARAM_LIMIT = "limit";
    private static String PARAM_OFFSET = "offset";
    private static String PARAM_OFFER_ID = "offer_id";
    private static String PARAM_BOOKING_TYPE = "booking_type";
    private static String PARAM_PEOPLE = "people";
    private static String PARAM_MALE = "male";
    private static String PARAM_DEVICE_REG_ID = "device_reg_id";
    private static String PARAM_USER_EMAIL = "user_email";
    private static String PARAM_USER_ID = "user_id";
    private static String PARAM_USER_NAME = "user_name";
    private static String PARAM_SOLR_SEARCH_ARR_TAG = "arr_tag[]";
    private static String PARAM_GPS_ENABLED = "gps_enabled";
    private static String PARAM_JP_NUMBER = "jp_number";
    private static String PARAM_SPCL_REQ = "spcl_req";
    private static String PARAM_RESPONSE_TYPE = "responseType";
    private static String PARAM_DINING_DATE_TIME = "diningDateTime";
    private static String PARAM_RESTAURANTS_ID = "restaurantID";
    private static String PARAM_ENTITY_TYPE = "entity_type";
    private static String PARAM_ENTITY_ID = "entity_id";
    private static String PARAM_OBJ_TYPE = "obj_type";
    private static String PARAM_BOOKING_DATE_TIME = "booking_date_time";
    private static String PARAM_DINING_DT_TIME = "dining_dt_time";
    private static String PARAM_REVIEW_ID = "review_id";
    private static String PARAM_DEVICE_TOKEN = "d_token";
    private static String PARAM_DEVICE_TYPE = "d_type";
    private static String PARAM_DEVICE_ID = "d_id";
    private static String PARAM_FORMAT = "format";
    private static String PARAM_BY_TICKET_TYPE = "byTicketType";
    private static String PARAM_BY_SEARCH_TYPE = "search_type";
    private static String PARAM_SEARCH_NEEDLE = "search_needle";
    private static String PARAM_SEARCH_CATEGORY = "byCategory";
    private static String PARAM_SORT_BY = "sortby";
    private static String PARAM_BY_TYPE = "by_type";
    private static String PARAM_NCODE = "ncode";
    private static String PARAM_FROM_TIME = "fromTime";
    private static String PARAM_TO_TIME = "toTime";
    private static String PARAM_OFER_ID = "offerID";
    private static String PARAM_DEAL_VALIDITY = "dealValidity";
    private static String PARAM_REQUEST_TYPE = "requestType";
    private static String PARAM_INPUT = "input";
    private static String PARAM_REVIEW_TAG = "tags";
    private static String PARAM_REVIEW_ACTION = "actions";
    private static String PARAM_My_EARNING_START = "start";
    private static String PARAM_My_EARNING_LIMIT = "limit";
    private static String PARAM_My_EARNING_FILTER = "filter";

    /**
     * This function returns common parameters required to be passed in all requests
     */
    private static HashMap<String, String> getCommonParams() {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_TEMP, "1");

        return params;
    }

    /**
     * This function returns Get Diner Profile API params
     */
    public static HashMap<String, String> getDinerProfileParams(String dinerId, String ak) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        if (!TextUtils.isEmpty(ak))
            params.put(PARAM_AUTH_KEY, ak);

        return params;
    }

    /**
     * This function returns Logout API params
     */
    public static HashMap<String, String> getLogoutParams(String dinerId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);

        return params;
    }

    /**
     * This function returns Insert Mobile Id API params
     */
    public static HashMap<String, String> getInsertMobileIdParams(String gcmRegistrationId, String deviceId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DEVICE_TOKEN, gcmRegistrationId);
        params.put(PARAM_DEVICE_TYPE, DEVICE_TYPE);
        params.put(PARAM_DEVICE_ID, deviceId);

        return params;
    }

    /**
     * This function returns Waiting Time API params
     */
    public static HashMap<String, String> getWaitingTimeParams(String restaurantId, long bookingDateTime) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_REST_ID, restaurantId);
        params.put(PARAM_BOOKING_DATE_TIME, Long.toString(bookingDateTime));

        return params;
    }

    /**
     * This function returns Cancel Booking API params
     */
    public static HashMap<String, String> getCancelBookingParams(String bookingId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_B_ID, bookingId);

        return params;
    }

    /**
     * This function returns Fetch Uber Price API params
     */
    public static HashMap<String, String> getFetchUberPriceParams(String serverToken, String startLatitude,
                                                                  String startLongitude, String endLatitude,
                                                                  String endLongitude) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_UBER_SERVER_TOKEN, serverToken);
        params.put(PARAM_UBER_START_LATITUDE, startLatitude);
        params.put(PARAM_UBER_START_LONGITUDE, startLongitude);
        params.put(PARAM_UBER_END_LATITUDE, endLatitude);
        params.put(PARAM_UBER_END_LONGITUDE, endLongitude);

        return params;
    }

    /**
     * This function returns Fetch Uber Cabs Arrival Time API params
     */
    public static HashMap<String, String> getFetchUberCabsArrivalTimeParams(String serverToken, String startLatitude,
                                                                            String startLongitude) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_UBER_SERVER_TOKEN, serverToken);
        params.put(PARAM_UBER_START_LATITUDE, startLatitude);
        params.put(PARAM_UBER_START_LONGITUDE, startLongitude);

        return params;
    }

    /**
     * This function returns Get Booking Detail API params
     */
    public static HashMap<String, String> getBookingDetailParams(String bookingId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_B_ID, bookingId);

        return params;
    }

    /**
     * This function returns Set Unset Favorite Restaurant API params
     */
    public static HashMap<String, String> getSetUnsetFavoriteRestaurantParams(String restaurantId, String dinerId,
                                                                              String action) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_RESTAURANT_ID, restaurantId);
        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_ACTION, action);

        return params;
    }

    /**
     * This function returns Set Is Viewed Notification Tracking API params
     */
    public static HashMap<String, String> getSetIsViewedNotificationTrackingParams(String uKey) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_UKEY, uKey);

        return params;
    }

    /**
     * This function returns Doplus Conversion Detail API params
     */
    public static HashMap<String, String> getDoplusConversionDetailParams(String cityId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_CITY_ID, cityId);

        return params;
    }

    /**
     * This function returns Update Diner Info API params
     */
    public static HashMap<String, String> getUpdateDinerInfoParams(String dinerId, String firstName, String lastName,
                                                                   String phone, boolean isProfileImageSelected,
                                                                   String image) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);

        if (firstName != null) {
            params.put(PARAM_FIRST_NAME, firstName);
        }

        if (lastName != null) {
            params.put(PARAM_LAST_NAME, lastName);
        }

        params.put(PARAM_PHONE, phone);

        if (isProfileImageSelected) {
            params.put(PARAM_IMAGE, image);
        }

        params.put(PARAM_UPDATE_IMAGE, String.valueOf(isProfileImageSelected));

        return params;
    }

    /**
     * This function returns Forgot Password API params
     */
    public static HashMap<String, String> getForgotPasswordParams(String email) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_EMAIL, email);

        return params;
    }

    public static HashMap<String, String> getForceForgotPasswordParams(String dinerEmail) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_EMAIL, dinerEmail);

        return params;
    }

    /**
     * This function returns Get Diner Payment History API params
     */
    public static HashMap<String, String> getDinerPaymentHistoryParams(String dinerId, String currentPage) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_PAGE, currentPage);

        return params;
    }

    /**
     * This function returns App Tag Cuisine API params
     */
    public static HashMap<String, String> getAppTagCuisineParams(String cityId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_CITY_ID, cityId);

        return params;
    }

    /**
     * This function returns Location API params
     */
    public static HashMap<String, String> getLocationParams(String searchNeedle, String latitude,
                                                            String longitude) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_SEARCH_NEEDLE, searchNeedle);
        params.put(PARAM_LAT, latitude);
        params.put(PARAM_LNG, longitude);

        return params;
    }

    /**
     * This function returns Forgot Email API params
     */
    public static HashMap<String, String> getForgotEmailParams(String phone) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_PHONE, phone);

        return params;
    }

    /**
     * This function returns Send OTP API params
     */
    public static HashMap<String, String> getSendOtpParams(String dinerId, String phone) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_PHONE, phone);

        return params;
    }

    /**
     * This function returns Verify Diner API params
     */
    public static HashMap<String, String> getVerifyDinerParams(String dinerId, String otp, String phone) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_OTP, otp);
        params.put(PARAM_PHONE, phone);

        return params;
    }

    /**
     * This function returns Restaurant Details API params
     */
    public static HashMap<String, String> getRestaurantDetailsParams(String dinerId, String restaurantId,
                                                                     String latitude, String longitude) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_RES_ID, restaurantId);
        params.put(PARAM_LAT, latitude);
        params.put(PARAM_LONG, longitude);

        return params;
    }

    /**
     * This function returns Restaurant Details API params
     */
    public static HashMap<String, String> getRestaurantDetailsParams(String restaurantId, String responseType) {
        HashMap<String, String> params = new HashMap<>();

        /**
         * Response Type
         * all - Reservation + Info
         * reservation - Reservation
         * info - Info
         */
        params.put(PARAM_RESTAURANTS_ID, restaurantId);
        params.put(PARAM_RESPONSE_TYPE, responseType);

        return params;
    }

    /**
     * This function returns Submit Booking Review API params
     */
    public static HashMap<String, String> getSubmitBookingReviewParams(String dinerId, String intentBookingId,
                                                                       String apiBookingId, String rating,
                                                                       String review, String restaurantId, String reviewId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);

        if (intentBookingId == null || intentBookingId.equalsIgnoreCase("") || intentBookingId.equalsIgnoreCase("-1")) {
            if (apiBookingId == null || apiBookingId.equalsIgnoreCase("") || apiBookingId.equalsIgnoreCase("-1")) {
                params.put(PARAM_REVIEW_TYPE, REVIEW_TYPE_GENERIC);
            } else {
                params.put(PARAM_REVIEW_TYPE, REVIEW_TYPE_AUTHENTIC);
                params.put(PARAM_B_ID, apiBookingId);
            }
        } else {
            params.put(PARAM_REVIEW_TYPE, REVIEW_TYPE_AUTHENTIC);
            params.put(PARAM_B_ID, intentBookingId);
        }

        params.put(PARAM_RATING_FOOD, rating);
        params.put(PARAM_RATING_SERVICE, rating);
        params.put(PARAM_RATING_AMBIENCE, rating);
        params.put(PARAM_RATING_DESC, review);
        params.put(PARAM_REVIEW_ID, reviewId);

        if (!TextUtils.isEmpty(restaurantId)) {
            params.put(PARAM_REST_ID, restaurantId);
        }

        return params;
    }

    /**
     * This function returns Submit Review API params
     */
    public static HashMap<String, String> getSubmitReviewParams(String dinerId,
                                                                       String bookingId, String restaurantId,
                                                                       String rating, String reviewDesc, String reviewId,
                                                                       String reviewTags, String reviewAction) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_B_ID, bookingId);
        params.put(PARAM_RATING_FOOD, rating);
        params.put(PARAM_RATING_SERVICE, rating);
        params.put(PARAM_RATING_AMBIENCE, rating);
        params.put(PARAM_RATING_DESC, reviewDesc);
        params.put(PARAM_REVIEW_ID, reviewId);
        params.put(PARAM_REST_ID, restaurantId);
        params.put(PARAM_REVIEW_TAG, reviewTags);
        params.put(PARAM_REVIEW_ACTION, reviewAction);

        return params;
    }

    /**
     * This function returns submit review restaurant not visited API params
     */
    public static HashMap<String, String> getSubmitReviewRestaurantNotVisitedParams(String dinerId,
                                                                String bookingId, String restaurantId,
                                                                String reviewId, String reviewTags,
                                                                                    String reviewAction) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_B_ID, bookingId);
        params.put(PARAM_REVIEW_ID, reviewId);
        params.put(PARAM_REST_ID, restaurantId);
        params.put(PARAM_REVIEW_TAG, reviewTags);
        params.put(PARAM_REVIEW_ACTION, reviewAction);

        return params;
    }

    /**
     * This function returns Get Reward Schemes Data API params
     */
    public static HashMap<String, String> getRewardSchemesDataParams(String dinerId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);

        return params;
    }

    /**
     * This function returns Get Restaurant Review API params
     */
    public static HashMap<String, String> getRestaurantReviewParams(String restaurantId, String limit, String offset) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_RESTAURANTS_ID, restaurantId);
        params.put(PARAM_LIMIT, limit);
        params.put(PARAM_OFFSET, offset);

        return params;
    }

    /**
     * This function returns SOLR Search Auto Suggest API params
     */
    public static HashMap<String, String> getSolrSearchAutoSuggestParams(String cityName,
                                                                         String searchNeedle,
                                                                         boolean isDoSpecific) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_CITY_NAME, cityName);
        params.put(PARAM_SEARCH_NEEDLE, searchNeedle);

        if (isDoSpecific)
            params.put(PARAM_SOLR_SEARCH_ARR_TAG, "Dineout Plus");

        return params;
    }

    /**
     * This function returns Register API params
     */
    public static HashMap<String, String> getRegisterParams(String phone, String name, String email,
                                                            String password, String confirmPassword) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_NAME, name);
        params.put(PARAM_PHONE, phone);
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);
        params.put(PARAM_CONFIRM_PASSWORD, confirmPassword);

        return params;
    }

    /**
     * This function returns Login API params
     */
    public static HashMap<String, String> getLoginParams(String email, String password, String type,
                                                         String accessToken) {
        HashMap<String, String> params = new HashMap<>();

        if (!TextUtils.isEmpty(email))
            params.put(PARAM_EMAIL, email);

        if (!TextUtils.isEmpty(password))
            params.put(PARAM_PASSWORD, password);

        if (!TextUtils.isEmpty(type))
            params.put(PARAM_TYPE, type);

        if (!TextUtils.isEmpty(accessToken))
            params.put(PARAM_ACCESS_TOKEN, accessToken);

        return params;
    }

    public static HashMap<String, String> getNativeLoginParameter(String email, String password) {

        HashMap<String, String> params = new HashMap<>();

        if (!TextUtils.isEmpty(email))
            params.put(PARAM_EMAIL, email);

        if (!TextUtils.isEmpty(password))
            params.put(PARAM_PASSWORD, password);

        return params;
    }

    public static HashMap<String, String> getSocialLoginParameters(String type, String token) {

        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_TYPE, type);

        params.put(PARAM_ACCESS_TOKEN, token);

        return params;
    }


    public static HashMap<String, String> getLoginPhoneEmailParameter(String email, String password) {

        HashMap<String, String> params = getCommonParams();

        if (!TextUtils.isEmpty(email))
            params.put(PARAM_INPUT, email);

        if (!TextUtils.isEmpty(password))
            params.put(PARAM_PASSWORD, password);

        return params;
    }


    /**
     * This function returns Generate Booking API params
     */
    public static HashMap<String, String> getGenerateBookingParams(String offerId, long diningDateTime,
                                                                   String maleCount,
                                                                   String guestCount, String restaurantId,
                                                                   String email, String dinerId, String userName,
                                                                   String phone, String specialRequest,
                                                                   String bookingId, String bookingType,
                                                                   String deviceRegId, String jpMilesNumber) {
        HashMap<String, String> params = new HashMap<>();

        if (!TextUtils.isEmpty(offerId)) {
            params.put(PARAM_OFFER_ID, offerId);
        }
        //params.put(PARAM_B_ID, bookingId);
        params.put(PARAM_BOOKING_TYPE, bookingType);
        params.put(PARAM_DINING_DT_TIME, Long.toString(diningDateTime));
        params.put(PARAM_MALE, maleCount);
        params.put(PARAM_PEOPLE, guestCount);
        //params.put(PARAM_DEVICE_REG_ID, deviceRegId);
        params.put(PARAM_RES_ID, restaurantId);
        params.put(PARAM_USER_EMAIL, email);
        params.put(PARAM_USER_ID, dinerId);
        params.put(PARAM_USER_NAME, userName);
        params.put(PARAM_PHONE, phone);
        params.put(PARAM_SPECIAL_REQUEST, specialRequest);
        params.put(PARAM_JP_NUMBER, jpMilesNumber);

        return params;
    }

    /**
     * This function returns User Booking Segg API params
     */
    public static HashMap<String, String> getUserBookingSeggParams(String userId, String ak,
                                                                   String bookingType,
                                                                   int start, int limit) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_USER_ID, userId);

//        if (!TextUtils.isEmpty(ak)) {
//            params.put(AppConstant.DEFAULT_HEADER_AUTH_KEY, ak);
//        }

        if (!TextUtils.isEmpty(bookingType)) {
            params.put(PARAM_BY_TYPE, bookingType);
        }

        params.put(PARAM_START, Integer.toString(start));
        params.put(PARAM_LIMIT, Integer.toString(limit));

        return params;
    }

    /**
     * This function returns Get Diner Favorite Restaurant Data API params
     */
    public static HashMap<String, String> getDinerFavoriteRestaurantDataParams(String dinerId, String ak) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);

        if (!TextUtils.isEmpty(ak)) {
            params.put(AppConstant.DEFAULT_HEADER_AUTH_KEY, ak);
        }

        return params;
    }

    /**
     * API: home_page_card
     */
    public static HashMap<String, String> getHomePageCardParams(String byCity, String locArea,
                                                                String area, String gpsEnabled) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_BY_CITY, byCity);
        params.put(PARAM_ARR_LOCAREA, locArea);
        params.put(PARAM_ARR_AREA, area);
        params.put(PARAM_GPS_ENABLED, gpsEnabled);

        return params;
    }

    public static HashMap<String, String> getRingFencingParams(String cityId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_CITY_ID, cityId);

        return params;
    }

    public static HashMap<String, String> getRingFencingVerifyReferralParams(String refCode) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_REF_CODE, refCode);
        return params;
    }

    public static HashMap<String, String> getGiftVoucherVerifyReferralParams(String entityType, String refCode, String pin) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_ENTITY_TYPE, entityType);
        params.put(PARAM_REF_CODE, refCode);
        params.put(PARAM_NCODE, pin);
        return params;
    }

    public static HashMap<String, String> getMyEarningsParams(String cityId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_CITY_ID, cityId);

        return params;
    }

    public static HashMap<String, String> getNewMyEarningsParams(String dinerId, String cityId, String start, String limit, String filter) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_CITY_ID, cityId);
        params.put(PARAM_My_EARNING_START, start);
        params.put(PARAM_My_EARNING_LIMIT, limit);
        params.put(PARAM_My_EARNING_FILTER, filter);

        return params;
    }


    public static HashMap<String, String> getDOPromoCodeParams(String action, String code, String objId, String type, String dinerId) {

        HashMap<String, String> param = new HashMap<>();

        param.put("type", "doplus");
        param.put("diner_id", dinerId);
        param.put("obj_id", "1");
        param.put("obj_type", "doplus");
        param.put("action", action);
        param.put("promocode", code);
        return param;
    }

    public static HashMap<String, String> getBillingSummaryParams(String id, String objId, String type, boolean isWalletUsed) {
        HashMap<String, String> param = new HashMap<>();
        param.put("diner_id", id);
        param.put("obj_id", objId);
        param.put("obj_type", type);
        param.put("action", "billing_summary");
        param.put("is_do_wallet_used", isWalletUsed ? "1" : "0");
        return param;
    }


    public static HashMap<String, String> getBillingSummaryParamsBooking(String id, String objId,
                                                                         String type, boolean isWalletUsed, String amount) {
        HashMap<String, String> param = getBillingSummaryParams(id, objId, type, isWalletUsed);
        param.put("bill_amount", amount);
        return param;
    }

    public static HashMap<String, String> getDOPaymentOptions(String id) {
        HashMap<String, String> param = new HashMap<>();
        param.put("diner_id", id);
        return param;
    }

    public static Map<String, String> getPayFromWalletParam(String id, String dinerId, String doHash, boolean isDOWallet, String type, String amt) {
        Map<String, String> param = new HashMap<>();
        param.put("diner_id", dinerId);
        param.put("amt_to_pay", amt);
        param.put("do_hash", doHash);
        param.put("obj_id", id);
        param.put("obj_type", type);
        param.put("is_do_wallet_used", isDOWallet ? "1" : "0");
        return param;
    }

    public static Map<String, String> getPayFromPromoCode(String id, String dinerId, String doHash, String type) {
        Map<String, String> param = new HashMap<>();
        param.put("diner_id", dinerId);
        param.put("do_hash", doHash);
        param.put("obj_id", id);
        param.put("obj_type", type);
        return param;
    }


    public static Map<String, String> getPayFromPromocodeParam(String id, String dinerId, String doHash, boolean isDOWallet, String type, String amt) {
        Map<String, String> param = new HashMap<>();
        param.put("diner_id", dinerId);
        param.put("amt_to_pay", amt);
        param.put("do_hash", doHash);
        param.put("obj_id", id);
        param.put("obj_type", type);
        param.put("is_do_wallet_used", "0");
        return param;
    }


    public static Map<String, String> getNotificationParams(String startIndex, String endIndex) {
        Map<String, String> param = new HashMap<>();
        param.put("start", startIndex);
        param.put("limit", endIndex);
        return param;
    }

    public static Map<String, String> getInitPaymentParams(String dinerId, String id, boolean isDOWallet,
                                                           String type, String paymentType) {

        Map<String, String> param = new HashMap<>();
        param.put("diner_id", dinerId);
        param.put("obj_id", id);
        param.put("is_do_wallet_used", isDOWallet ? "1" : "0");
        param.put("obj_type", type);
        param.put("payment_type", paymentType);

        return param;
    }

    public static Map<String, String> getAddMoneyPaymentParams(String dinerId, String id, boolean isDOWallet,
                                                               String type, String paymentType, String amount) {

        Map<String, String> param = getInitPaymentParams(dinerId, id, isDOWallet, type, paymentType);

        param.put("money_to_add", amount);
        return param;
    }

    public static Map<String, String> validateJPNumberParams(String jpMilesNumber) {
        Map<String, String> param = new HashMap<>();
        param.put(PARAM_JP_NUMBER, jpMilesNumber);
        return param;
    }


    /**
     * This function returns Verify refferel API params
     */
    public static HashMap<String, String> getRefferelCodeParams(String dinerId, String restaurantId, String refCode
    ) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);
        params.put(PARAM_REF_CODE, refCode);
        params.put(PARAM_ENTITY_TYPE, "restaurant");
        params.put(PARAM_ENTITY_ID, restaurantId);

        return params;
    }


    /**
     * This function returns Deals bill amount
     */
    public static HashMap<String, String> getBillAmountParams(String dinerName, String dinerPhn, String dinerEmail, String[] dealIds, String[] dealQuantity, String restaurantId, String unixTimestamp, String objType) {

        HashMap<String, String> param = new HashMap<>();
        param.put(PARAM_OBJ_TYPE, objType);

        int i = 0;
        for (String object : dealIds) {
            param.put("obj_id[" + (i++) + "]", object);
        }

        int j = 0;
        for (String object : dealQuantity) {
            param.put("obj_quantity[" + (j++) + "]", object);
        }

        param.put(PARAM_REST_ID, restaurantId);
        param.put(PARAM_BOOKING_DATE_TIME, unixTimestamp);
        param.put(PARAM_DINER_PHONE, dinerPhn);

        param.put(PARAM_DINER_NAME, dinerName);
        param.put(PARAM_DINER_EMAIL, dinerEmail);
        param.put(PARAM_ACTION, "get_bill_amount");

        return param;
    }


    /**
     * This function returns Deals billing summary params
     */
    public static HashMap<String, String> getDealsBillingSummaryParams(String type, boolean isWalletUsed) {
        HashMap<String, String> param = new HashMap<>();
        param.put(PARAM_OBJ_TYPE, type);
        param.put(PARAM_ACTION, "billing_summary");
        param.put("is_do_wallet_used", isWalletUsed ? "1" : "0");
        return param;
    }

    public static Map<String, String> getDealsPayFromPromoCode(String amountToPay, String doHash, String type) {
        Map<String, String> param = new HashMap<>();

        param.put("do_hash", doHash);
        param.put("amt_to_pay", amountToPay);
        param.put("obj_type", type);
        return param;
    }

    public static Map<String, String> getDealPayFromWalletParam(String doHash, String type, String amt) {
        Map<String, String> param = new HashMap<>();

        param.put("amt_to_pay", amt);
        param.put("do_hash", doHash);
        param.put("obj_type", type);
        return param;
    }

    public static Map<String, String> editBooking(String bookingId, long diningDateTime, String maleCount,
                                                  String guestCount, String offerId, String specialRequest) {
        Map<String, String> param = new HashMap<>();

        param.put(PARAM_B_ID, bookingId);
        param.put(PARAM_DINING_DT_TIME, Long.toString(diningDateTime));
        param.put(PARAM_MALE, maleCount);
        param.put(PARAM_PEOPLE, guestCount);
        param.put(PARAM_OFFER_ID, offerId);
        param.put(PARAM_SPCL_REQ, specialRequest);

        return param;
    }


    public static Map<String, String> editDeal(String bookingId, long diningDateTime) {
        Map<String, String> param = new HashMap<>();

        param.put(PARAM_B_ID, bookingId);
        param.put(PARAM_DINING_DT_TIME, Long.toString(diningDateTime));
        param.put(PARAM_OFFER_ID, "0");

        return param;
    }

    public static Map<String, String> getAppRatingParams(String type) {

        Map<String, String> param = new HashMap<>();
        param.put("obj_type", type);

        return param;
    }


    /**
     * This function returns Deal listing API params
     */
    public static HashMap<String, String> getDealListingParams(String type, String format,
                                                               String cityName, int limit, int offset, String byTicketType, String searchNeedle, String category, int dinerCount, String sortBy) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_TYPE, type);
        params.put(PARAM_FORMAT, format);
        params.put(PARAM_CITY_NAME, cityName);
        params.put(AppConstant.PARAM_SOLR_SEARCH_LIMIT, limit + "");
        params.put(PARAM_OFFSET, offset + "");
        params.put(PARAM_BY_TICKET_TYPE, byTicketType);


        if (!TextUtils.isEmpty(searchNeedle)) {
            params.put(PARAM_SEARCH_NEEDLE, searchNeedle);
        }

        if (!TextUtils.isEmpty(category)) {
            params.put(PARAM_SEARCH_CATEGORY, category);
        }

        if (dinerCount > 0) {
            params.put(DINER_COUNT, String.valueOf(dinerCount));
        }

        if (!TextUtils.isEmpty(sortBy)) {
            params.put(PARAM_SORT_BY, sortBy);
        }

        return params;
    }

    public static HashMap<String, String> getDealSearchAutoSuggestParams(String cityName,
                                                                         String searchNeedle,
                                                                         boolean isDoSpecific) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_CITY_NAME, cityName);
        params.put(PARAM_SEARCH_NEEDLE, searchNeedle);

        if (isDoSpecific) {
            params.put(PARAM_SOLR_SEARCH_ARR_TAG, "Dineout Plus");
        }

        params.put(PARAM_BY_SEARCH_TYPE, "Deals");
        return params;
    }

    public static HashMap<String, String> getRedeemDealParams(String bookingId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_B_ID, bookingId);
        return params;
    }

    public static HashMap<String, String> getEarningsHistoryParams() {
        return new HashMap<>();
    }

    public static HashMap<String, String> getDinerWalletAmountParams(String restaurantId, String restaurantCityId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_REST_ID, restaurantId);
        params.put(PARAM_REST_CITY_ID, restaurantCityId);

        return params;
    }


    public static HashMap<String, String> getRatingFeedbackParams(String feedback, String rating,
                                                                  String OSVersion, String deviceId) {

        HashMap<String, String> params = new HashMap<>();
        params.put(RATING_SCREEN_FEEDBACK, feedback);
        params.put(RATING_SCREEN_RATING, rating);
        params.put(RATING_SCREEN_OS_VERSION, OSVersion);
        params.put(RATING_SCREEN_DEVICE, deviceId);

        return params;
    }

    public static HashMap<String, String> getAppRatingParams(String tags, String rating,
                                                                  String OSVersion, String deviceId) {

        HashMap<String, String> params = new HashMap<>();
        params.put(RATING_SCREEN_TAG, tags);
        params.put(RATING_SCREEN_RATING, rating);
        params.put(RATING_SCREEN_OS_VERSION, OSVersion);
        params.put(RATING_SCREEN_DEVICE, deviceId);

        return params;
    }

    /**
     * This function returns Get Validate Account API params
     */
    public static HashMap<String, String> getValidateAccountParams(String userEmailOrPhone) {
        HashMap<String, String> params = getCommonParams();

        params.put(PARAM_INPUT, userEmailOrPhone);

        return params;
    }

    /**
     * This function returns Get Booking for Review Balance API params
     */
    public static HashMap<String, String> getBookingForReviewParams(String dinerId) {
        HashMap<String, String> params = new HashMap<>();

        params.put(PARAM_DINER_ID, dinerId);

        return params;
    }

    /**
     * This function returns Verify Diner API params
     */
    public static HashMap<String, String> getVerifyotpParams(String otpId, String otp) {
        HashMap<String, String> params = getCommonParams();

        params.put(PARAM_OTP_ID, otpId);
        params.put(PARAM_OTP, otp);


        return params;
    }


    /**
     * This function returns Verify Diner API params
     */
    public static HashMap<String, String> getRestPasswordParams(String newPassword, String confirmPassword, String otpId, String otp) {
        HashMap<String, String> params = getCommonParams();
        params.put(PARAM_OTP_NEW_PASSWORD, newPassword);
        params.put(PARAM_OTP_CONFIRM_PASSWORD, confirmPassword);
        params.put(PARAM_OTP_ID, otpId);
        params.put(PARAM_OTP, otp);

        return params;
    }


    public static HashMap<String, String> getDinerParams(String dinerId, String ak) {
        HashMap<String, String> params = new HashMap<>();

        return params;
    }

    public static HashMap<String, Object> getSRPSearchParams(HashMap<String, Object> params,
                                                             String cityName, String searchNeedle,
                                                             int start, String deeplink,
                                                             String sfString, String locationSf) {

        if (isStringNotEmpty(cityName))
            params.put(PARAM_CITY_NAME, cityName);

        if (isStringNotEmpty(searchNeedle))
            params.put(PARAM_SEARCH_NEEDLE, searchNeedle);

        if (isStringNotEmpty(Integer.toString(start)))
            params.put(PARAM_START, Integer.toString(start));

        // Set Deeplink
        if (isStringNotEmpty(deeplink)) {
            params.put(PARAM_DEEPLINK, deeplink);

        } else {
            params.remove(PARAM_DEEPLINK);
        }

        // Set Auto Suggest Input
        setSfParams(sfString, params);

        // Set Location Sf
        setLocationSf(locationSf, params);

        return params;
    }

    private static void setSfParams(String sfString, HashMap<String, Object> params) {
        // Check for NULL
        if (sfString != null && !sfString.isEmpty()) {
            JSONArray sfJsonArray = null;
            try {
                sfJsonArray = new JSONArray(sfString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (sfJsonArray != null && sfJsonArray.length() > 0) {
                int sfSize = sfJsonArray.length();

                for (int index = 0; index < sfSize; index++) {
                    // Get Object
                    JSONObject sfJsonObject = sfJsonArray.optJSONObject(index);

                    if (sfJsonObject != null) {
                        // Get sf Key
                        String sfKey = sfJsonObject.optString("key", "");

                        if (sfKey != null && !sfKey.isEmpty()) {
                            // Get sf Value
                            Object valueObject = sfJsonObject.opt("value");

                            if (valueObject != null) {
                                // Add in params
                                if (params != null) {
                                    params.put(sfKey, valueObject);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setLocationSf(String locationSf, HashMap<String, Object> params) {
        // Check for NULL
        if (isStringNotEmpty(locationSf)) {
            JSONObject locationJsonObject = null;
            try {
                locationJsonObject = new JSONObject(locationSf);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (locationJsonObject != null) {
                // Get Location Keys
                Iterator<String> keys = locationJsonObject.keys();

                if (keys != null) {
                    while (keys.hasNext()) {
                        // Get Key
                        String key = keys.next();

                        // Get Value
                        String value = locationJsonObject.optString(key, "");

                        // Add to Params
                        params.put(key, value);
                    }
                }
            }
        }
    }

    private static boolean isStringNotEmpty(String value) {
        return (value != null && !value.trim().isEmpty());
    }

    public static HashMap<String, String> getBookingSlotParams(String restaurantId, long fromTimeStamp,
                                                               long toTimeStamp, String offerId,
                                                               String offerType, int dealValidity,
                                                               boolean isEditBooking) {
        HashMap<String, String> params = new HashMap<>();

        if (isStringNotEmpty(restaurantId))
            params.put(PARAM_RESTAURANTS_ID, restaurantId);

        if (fromTimeStamp > 0)
            params.put(PARAM_FROM_TIME, Long.toString(fromTimeStamp));

        if (toTimeStamp > 0)
            params.put(PARAM_TO_TIME, Long.toString(toTimeStamp));

        if (isStringNotEmpty(offerId))
            params.put(PARAM_OFER_ID, offerId);

        if (isStringNotEmpty(offerType)) {
            params.put(PARAM_TYPE, offerType);

            // Check if type is Deal
            if (offerType.equalsIgnoreCase(AppConstant.BOOKING_TYPE_DEAL)) {
                if (dealValidity == AppConstant.HAS_SLOT_DONT_KNOW) {
                    params.put(PARAM_DEAL_VALIDITY, "1");
                }
            }
        }

        if (isEditBooking) {
            params.put(PARAM_REQUEST_TYPE, "edit");
        }

        return params;
    }

    public static HashMap<String, String> getOffers(String restaurantId, long selectedTimeStamp,
                                                    String offerId, String offerType) {
        HashMap<String, String> params = new HashMap<>();

        if (isStringNotEmpty(restaurantId))
            params.put(PARAM_RESTAURANTS_ID, restaurantId);

        if (selectedTimeStamp > 0)
            params.put(PARAM_DINING_DATE_TIME, Long.toString(selectedTimeStamp));

        if (isStringNotEmpty(offerId))
            params.put(PARAM_OFER_ID, offerId);

        if (isStringNotEmpty(offerType))
            params.put(PARAM_TYPE, offerType);

        return params;
    }

    public static HashMap<String, String> getTicketDetailParams(String restaurantID, String dealID) {
        HashMap<String, String> params = new HashMap<>();

        params.put("restaurantID", restaurantID);
        params.put("dealID", dealID);

        return params;
    }

    public static HashMap<String, String> getTicketSavingsParams(String restaurantID, String dealID, int dealQuantity) {
        HashMap<String, String> params = new HashMap<>();

        params.put("restaurantID", restaurantID);
        params.put("dealID", dealID);
        params.put("quantity", Integer.toString(dealQuantity));

        return params;
    }
}
