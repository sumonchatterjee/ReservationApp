package com.example.dineoutnetworkmodule;

import android.net.Uri;

public class AppConstant {


    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x007;

    public static final String BASE_DOMAIN_URL = "https://api.dineout.co.in/";
    public static final String BASE_DOMAIN_URL_NODE = "https://api.dineout.co.in/";
    //public static final String BASE_DOMAIN_URL_REVIEW = "https://api.dineout.co.in/";

//    public static final String NODE_BASE_URL =  "nodeApi/v9/";
    public static final String NODE_BASE_URL =  "nodeApi/v15/";

    public static final String BASE_PHP_URL = "app_api/mobile_app_api_v18/";
    public static final String REVIEW_BASE_URL = "phpApi/api_v1/";

    public static final String NODE_RESTAURANT_DETAIL_URL = "restaurant/details";
    public static final String NODE_RESTAURANT_REVIEW_URL = "review/listing";
    public static final String NODE_DEAL_LISTING_URL = "search";
    public static final String NODE_TICKET_DETAIL_URL = "ticket/detail";
    public static final String NODE_TICKET_SAVINGS_URL = "ticket/savings";


    public static final Uri WEB_URL = Uri.parse("http://dineout.co.in/");
    public static final String UBER_BASE_URL = "https://api.uber.com/v1.2/estimates/";
    public static final String BASE_URL_TNC = "http://www.dineout.co.in/mobile/mobile_new/";
    public static final String DINEOUT_PRIVACY_POLICY = BASE_URL_TNC + "app_privacy_policy";
    public static final String DINEOUT_TNC = BASE_URL_TNC + "app_tnc";
    public static final String UPLOAD_BILL_TNC_URL = "https://www.dineout.co.in/bill_upload_tnc";
    public static final String DINEOUT_SMART_PAY_URL = "http://www.dineout.co.in/smartpay";
    public static final String DINEOUT_GIFT_VOUCHER_URL = "https://www.dineout.co.in/voucherfaq";

    //production
    public static final String DINEOUT_COUNTLY_PRODUCTION_SDK_SERVER_URL="http://wp.dineout.co.in/android/";

    //dev
    public static final String DINEOUT_COUNTLY_DEV_SDK_SERVER_URL="http://wp.dineout.co.in/dev/android/";


    //Permission Request Codes
    public static final int REQUEST_PERMISSION_CALL_PHONE = 0x01;
    public static final int REQUEST_PERMISSION_RECEIVE_SMS = 0x02;
    public static final int REQUEST_PERMISSION_LOCATION = 0x05;
    public static final int REQUEST_PERMISSION_ACCOUNTS = 0x06;

    //Server Type Constants
    public static final String SERVER_TYPE_DEV = "dev";

    public static final int PHONEPE_DEBIT_REQUEST=7890;


    public static final String BUNDLE_GUEST_COUNT = "BUNDLE_GUEST_COUNT";
    public static final String BUNDLE_MALE_COUNT = "BUNDLE_MALE_COUNT";
    public static final String BUNDLE_FEMALE_COUNT = "BUNDLE_FEMALE_COUNT";
    public static final String BUNDLE_GUEST_NAME = "BUNDLE_GUEST_NAME";
    public static final String BUNDLE_GUEST_EMAIL = "BUNDLE_GUEST_EMAIL";
    public static final String BUNDLE_GUEST_PHONE = "BUNDLE_GUEST_PHONE";
    public static final String BUNDLE_GUEST_LOGIN_VIA_OTP = "BUNDLE_GUEST_LOGIN_VIA_OTP";
    public static final String BUNDLE_SPECIAL_REQUEST = "BUNDLE_SPECIAL_REQUEST";
    public static final String BUNDLE_BOOKING_ID = "BUNDLE_BOOKING_ID";
    public static final String BUNDLE_SOURCE_LATITUDE = "BUNDLE_SOURCE_LATITUDE";
    public static final String BUNDLE_SOURCE_LONGITUDE = "BUNDLE_SOURCE_LONGITUDE";
    public static final String BUNDLE_DESTINATION_LATITUDE = "BUNDLE_DESTINATION_LATITUDE";
    public static final String BUNDLE_DESTINATION_LONGITUDE = "BUNDLE_DESTINATION_LONGITUDE";
    public static final String BUNDLE_RESTAURANT_ID = "BUNDLE_RESTAURANT_ID";
    public static final String BUNDLE_RESTAURANT_NAME = "BUNDLE_RESTAURANT_NAME";
    public static final String BUNDLE_RESTAURANT_ADDRESS = "BUNDLE_RESTAURANT_ADDRESS";
    public static final String BUNDLE_RESTAURANT_CITY = "BUNDLE_RESTAURANT_CITY";
    public static final String BUNDLE_RESTAURANT_AREA = "BUNDLE_RESTAURANT_AREA";
    public static final String BUNDLE_RESTAURANT_LOCALITY = "BUNDLE_RESTAURANT_LOCALITY";
    public static final String BUNDLE_RESTAURANT_IMAGE_URL = "BUNDLE_RESTAURANT_IMAGE_URL";
    public static final String BUNDLE_RESTAURANT_CUISINE_NAME = "BUNDLE_RESTAURANT_CUISINE_NAME";
    public static final String BUNDLE_RESTAURANT_DEEPLINK = "BUNDLE_RESTAURANT_DEEPLINK";
    public static final String BUNDLE_RESTAURANT_CUISINELIST = "BUNDLE_RESTAURANT_CUISINE_LIST";
    public static final String BUNDLE_RESTAURANT_TAGLIST = "BUNDLE_RESTAURANT_TAG_LIST";
    public static final String BUNDLE_DIALOG_TITLE = "BUNDLE_DIALOG_TITLE";
    public static final String BUNDLE_DIALOG_HEADER = "BUNDLE_DIALOG_HEADER";
    public static final String BUNDLE_DIALOG_DESCRIPTION = "BUNDLE_DIALOG_DESCRIPTION";
    public static final String BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT = "BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT";
    public static final String BUNDLE_DIALOG_NEGATIVE_BUTTON_TEXT = "BUNDLE_DIALOG_NEGATIVE_BUTTON_TEXT";
    public static final String BUNDLE_DIALOG_CANCELLABLE = "BUNDLE_DIALOG_CANCELLABLE";
    public static final String BUNDLE_DATE_PICKER_DATE_VALUE = "DATE_PICKER_DATE_VALUE";
    public static final String BUNDLE_DATE_PICKER_TIME_VALUE = "DATE_PICKER_TIME_VALUE";
    public static final String DATE_PICKER_DATE = "DATE_PICKER_DATE";
    public static final String DATE_PICKER_TIME = "DATE_PICKER_TIME";
    public static final String DATE_PICKER_DISPLAY_DATE = "DATE_PICKER_DISPLAY_DATE";
    public static final String DATE_PICKER_DISPLAY_TIME = "DATE_PICKER_DISPLAY_TIME";
    public static final String DATE_PICKER_DATE_VALUE_INDEX = "DATE_PICKER_DATE_VALUE_INDEX";
    public static final String DATE_PICKER_TIME_VALUE_INDEX = "DATE_PICKER_TIME_VALUE_INDEX";
    public static final String DATE_PICKER_AM_PM_VALUE_INDEX = "DATE_PICKER_AM_PM_VALUE_INDEX";

    public static final String BUNDLE_TICKET_GROUPS = "BUNDLE_TICKET_GROUPS";
    public static final String BUNDLE_OFFER_ID = "BUNDLE_OFFER_ID";
    public static final String BUNDLE_DEAL_ID = "BUNDLE_DEAL_ID";
    public static final String BUNDLE_OFFER_TYPE = "BUNDLE_OFFER_TYPE";
    public static final String BUNDLE_OFFER_START_FROM = "BUNDLE_OFFER_START_FROM";
    public static final String BUNDLE_OFFER_JSON = "BUNDLE_OFFER_JSON";

    public static final String BUNDLE_PREVIOUS_OFFER_ID = "BUNDLE_PREVIOUS_OFFER_ID";
    public static final String BUNDLE_PREVIOUS_OFFER_TYPE = "BUNDLE_PREVIOUS_OFFER_TYPE";
    public static final String BUNDLE_PREVIOUS_OFFER_START_FROM = "BUNDLE_PREVIOUS_OFFER_START_FROM";
    public static final String BUNDLE_PREVIOUS_OFFER_JSON = "BUNDLE_PREVIOUS_OFFER_JSON";

    public static final String BUNDLE_REST_START_FROM = "BUNDLE_REST_START_FROM";

    public static final String BUNDLE_SELECTED_TIME_SLOT_OFFER_STATUS = "BUNDLE_SELECTED_TIME_SLOT_OFFER_STATUS";
    public static final String BUNDLE_OFFER_TITLE = "BUNDLE_OFFER_TITLE"; // Used in old Reservation Tab Fragment / Adapter
    public static final String BUNDLE_OFFER_VALIDITY = "BUNDLE_OFFER_VALIDITY"; // Used in old Reservation Tab Fragment / Adapter
    public static final String BUNDLE_IS_EDIT_BOOKING = "IS_EDITING_BOOKING";
    public static final String BUNDLE_DATE_TIMESTAMP = "BUNDLE_DATE_TIMESTAMP";
    public static final String BUNDLE_REVIEW_ID = "BUNDLE_REVIEW_ID";
    public static final String BUNDLE_REVIEW_TEXT = "BUNDLE_REVIEW_TEXT";
    public static final String BUNDLE_REVIEW_RATING = "BUNDLE_REVIEW_RATING";
    public static final String BUNDLE_REVIEW_TAG = "BUNDLE_REVIEW_TAG";
    public static final String BUNDLE_DISTANCE = "BUNDLE_DISTANCE";
    public static final String BUNDLE_ACCEPT_PAYMENT = "BUNDLE_ACCEPT_PAYMENT";
    public static final String BUNDLE_RECENCY = "BUNDLE_RECENCY";
    public static final String BUNDLE_RATING = "BUNDLE_RATING";
    public static final String BUNDLE_SELECTED_DEAL_COUNT = "BUNDLE_SELECTED_DEAL_COUNT";
    public static final String BUNDLE_EVENTS = "BUNDLE_EVENTS";
    public static final String BUNDLE_RESTAURANT_TAB = "BUNDLE_RESTAURANT_TAB";

    public static final String BUNDLE_PHONE_NUMBER = "BUNDLE_PHONE_NUMBER";
    public static final String BUNDLE_VIA_DEEPLINK = "BUNDLE_VIA_DEEPLINK";

    public static final String DAY = "DAY";
    public static final String DATE = "DATE";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String DATE_KEY = "DATE_KEY";
    public static final String IS_EDIT_DEAL = "is_edit";
    public static final String BOOKING_ID = "BOOKING_ID";
    public static final String DINER_EMAIL = "DINER_EMAIL";
    public static final String DINER_NAMES = "DINER_NAME";
    public static final String DINER_PHONE = "DINER_PHONE";
    public static final String SPCL_REQUEST = "SPCL_REQUEST";
    public static final String PARAM_DINING_TIMESTAMP = "dining_dt_time_ts";

    // Share Message Constants
    public static final String DINER_NAME_PLACE_HOLDER = "#diner_name#";
    public static final String DINER_FIRST_NAME_PLACE_HOLDER = "#diner_first_name#";
    public static final String RESTAURANT_NAME_PLACE_HOLDER = "#restaurant_name#";
    public static final String TINY_URL_PLACE_HOLDER = "#tiny_url#";
    public static final String BOOKING_ID_PLACE_HOLDER = "#booking_id#";
    public static final String BOOKING_DATE_PLACE_HOLDER = "#booking_date#";
    public static final String BOOKING_TIME_PLACE_HOLDER = "#booking_time#";
    public static final String DINER_COUNT_PLACE_HOLDER = "#diner_count#";
    public static final String RESTAURANT_LOCALITY_PLACE_HOLDER = "#locality_name#";
    public static final String DEAL_PLACE_HOLDER = "#deal#";
    public static final String EVENT_PLACE_HOLDER = "#event#";
    public static final String TINY_URL_AMOUNT = "#amount#";

    //General Constants
    public static final String ERROR_MESSAGE = "error_msg";
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_TYPE = "error_type";

    public static final String REST_EDIT_OFFER_BOOKING_RESERVATION_DETAILS = "edit";
    public static final String REST_DETAIL_RESERVATION_DETAILS = "reservation";
    public static final String REST_DETAIL_EVENT_DETAILS = "events";
    public static final String REST_DETAIL_INFO_DETAILS = "info";
    public static final String REST_DETAIL_EXTRA_INFO = "extraInfo";

    //API Constants
    public static final String RESTAURANT_ID = "restaurant_id";
    public static final String REST_NAME = "rest_name";
    public static final String DINER_NAME = "diner_name";
    public static final String DISP_ID = "disp_id";
    public static final String RES_AUTH = "res_auth";
    public static final String OTP_REQUIRED = "otp_required";
    public static final String CITY_ID = "city_id";
    public static final String DINER_ID = "diner_id";
    public static final String SHORT_URL = "short_url";
    public static final String IS_ACCEPT_PAYMENT = "is_accept_payment";
    public static final String WELCOME_MSG = "welcome_msg";
    public static final String LINE1 = "line1";
    public static final String LINE2 = "line2";
    public static final String FACEBOOK_EVENT_PARAM_CONTENT_TYPE_NAME = "product";
    public static final String FACEBOOK_EVENT_PARAM_DESCRIPTION = "facebook";
    public static final String RESTAURANT_TYPE_DISCOVERY = "discovery";
    public static final String RESTAURANT_TYPE_PARTIAL_FULFILLMENT = "partialFulfillment";
    public static final String RESTAURANT_TYPE_FULFILLMENT = "fulfillment";

    public static final String RESTAURANT_TYPE_DISCOVERY_ANALYTICS = "D";
    public static final String RESTAURANT_TYPE_PARTIAL_FULFILLMENT_ANALYTICS = "PF";
    public static final String RESTAURANT_TYPE_FULFILLMENT_ANALYTICS = "FF";
    public static final String REVIEW_TYPE_CRITIC = "critic";
    public static final String REVIEW_TYPE_AUTHENTICATION = "authentic";
    public static final String REVIEW_TYPE_TRIP_ADVISOR = "tripadvisor";
    public static final int PAYMENT_SUCCESS = 1;
    public static final int PAYMENT_FAILURE = 0;
    public static final int PAYMENT_PENDING = 2;

    /**
     * Code for specifying IOException
     */
    public static final String PARAM_TEMP = "f";
    public static final String DEVICE_TYPE = "android";
    public static final String DEFAULT_HEADER_DEVICE_ID = "d-id";
    public static final String DEFAULT_HEADER_DEVICE_ID_1 = "d-id-1";
    public static final String DEFAULT_HEADER_DEVICE_ID_2 = "d-id-2";
    public static final String DEFAULT_HEADER_DEVICE_TOKEN = "d-token";
    public static final String DEFAULT_HEADER_DEVICE_TYPE = "d-type";
    public static final String DEFAULT_HEADER_CITY_ID = "city-id";
    public static final String DEFAULT_HEADER_DINER_ID = "diner-id";
    public static final String DEFAULT_HEADER_APP_VERSION = "app-version";
    public static final String DEFAULT_HEADER_AUTH_KEY = "ak";
    public static final String DEFAULT_HEADER_DEVICE_LATITUDE = "d-latitude";
    public static final String DEFAULT_HEADER_DEVICE_LONGITUDE = "d-longitude";
    public static final String DEFAULT_HEADER_ENTITY_LATITUDE = "elat";
    public static final String DEFAULT_HEADER_ENTITY_LONGITUDE = "elng";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String GPS_ENABLED = "gps-enabled";
    public static final String PARAM_SOLR_SEARCH_LIMIT = "limit";
    public static final String PARAM_UPDATE_DINER_INFO_ID = "diner_id";
    public static final String PARAM_UPDATE_DINER_INFO_FIRST_NAME = "first_name";
    public static final String PARAM_UPDATE_DINER_INFO_LAST_NAME = "last_name";
    public static final String PARAM_UPDATE_DINER_INFO_NAME = "name";
    public static final String PARAM_UPDATE_DINER_INFO_PHONE = "phone";
    public static final String PARAM_UPDATE_DINER_INFO_GENDER = "gender";
    public static final String PARAM_UPDATE_DINER_INFO_DOB = "dob";
    public static final String PARAM_UPDATE_DINER_INFO_IMAGE = "image";

    public static final String UBER_PRICE_URL = UBER_BASE_URL + "price";
    public static final String UBER_TIME_URL = UBER_BASE_URL + "time";
    public static final String GET_BOOKING_FOR_REVIEW = "get_booking_for_review";
    public static final String PARAM_GET_ARR_AREA = "arr_area[]";
    public static final String PARAM_DO_MEMBER_REG_ADDRESS1 = "address_1";
    public static final String PARAM_DO_MEMBER_REG_ADDRESS2 = "address_2";
    public static final String PARAM_DO_MEMBER_REG_CITY = "city";
    public static final String PARAM_DO_MEMBER_REG_PIN = "pincode";
    public static final String PARAM_DO_MEMBER_REG_PRODUCT_ID = "product_id";
    public static final String GET_EARNINGS_HISTORY = "diner_promocode_transaction_history";
    public static final String JSON_PARAM_DINING_DATE_TIME = "diningDateTime";
    public static final String JSON_PARAM_EDIT_BOOKING_OFFER_ID = "editBookingOfferId";
    public static final String TAG_DINEOUT_PLUS = "Dineout Plus";
    public static final String COLOR_IDENTIFIER = "##";
    public static final String RUPEE_IDENTIFIER = "##";
    public static final int DEFAULT_PAGINATION_LIMIT = 10;
    public static final String BOOKING_TYPE_OFFER = "offer";
    public static final String BOOKING_TYPE_DOP_OFFER = "dopoffer";
    public static final String BOOKING_TYPE_DEAL = "deal";
    public static final String BOOKING_TYPE_EVENT = "event";
    public static final String BOOKING_TYPE_EDIT = "edit";
    public static final String BOOKING_TYPES = "booking";
    public static final int REDEEM_DEAL_STATUS_HIDE = 0;
    public static final int REDEEM_DEAL_STATUS_INACTIVE = 1;
    public static final int REDEEM_DEAL_STATUS_CAN_REDEEM = 2;
    public static final int REDEEM_DEAL_STATUS_REDEEMED = 3;
    public static final int REVIEW_STATUS_MODERATION = 0;

    public static final int DEFAULT_FIRST_TIME_SLOT_INCREMENT = 30;
    public static final String FROM_DEEP_LINK = "FROM_DEEP_LINK";
    public static final int UPCOMING_BOOKING_TAB = 0;
    public static final int HISTORY_BOOKING_TAB = 1;
    public static final String BOOKING_TYPE_UPCOMING = "upcoming";
    public static final String BOOKING_TYPE_HISTORY = "history";
    // URLs
    public static final String URL_PROMO_CODE = "pay_from_promocode";
    public static final String URL_SMARTPAY_PLACE_HOLDER = "smartPayEarning";
    public static final String URL_UPLOAD_BILL = "bill/billImage";
    public static final String URL_GET_BILL = "bill/bill";
    public static final String URL_GET_APP_CONFIG = "get_app_config";

    public static final String URL_REST_DETAILS = "restaurant_details";
    public static final String URL_REFFEREL_CODE = "verify_referral";
    public static final String URL_GENERATE_BOOKING = "generate_booking";

    public static final String URL_NODE_SRP = "search";
    public static final String URL_SOLR_SEARCH_AUTO_SUGGEST = "solr_search_auto_suggest";
    public static final String URL_NODE_AUTO_SUGGEST = "suggest";
    public static final String URL_REGISTER = "signup";
    public static final String URL_LOGIN = "login";

    public static final String URL_UPDATE_DINER_INFO = "update_diner_profile";
    public static final String URL_GET_REVIEW_TAG = "get_review_tag";
    public static final String URL_GET_BOOKING_REVIEW_TAG = "get_booking_review_tag";
    public static final String URL_GET_DINER_PROFILE = "get_diner_profile";
    public static final String URL_DINER_FAV_LIST = "get_diner_fav_rest_data";
    public static final String URL_SET_UNSET_FAV_REST = "set_unset_fav_rest";
    public static final String URL_USER_BOOKING_SEGG = "user_booking_segg";
    public static final String URL_GET_BOOKING_DETAIL = "get_booking_detail";
    public static final String URL_CANCEL_BOOKING = "cancel_booking";
    public static final String URL_SUBMIT_BOOKING_REVIEW = "submit_booking_review";
    public static final String URL_SUBMIT_REVIEW = "submit_review";
    public static final String URL_SEND_REG_ID = "insert_mobile_id";
    public static final String URL_NOTIFICATION_TRACKING = "set_is_viewed_notification_tracking";
    public static final String URL_EMAIL_SUBMISSION = "set_mobile_info";

    public static final String URL_LOGOUT = "logout";
    public static final String URL_PAYMENT_HISTORY = "get_diner_payment_history";
    public static final String URL_INIT_PAYMENT = "init_payment";
    public static final String URL_PAYMENT_OPTION = "get_payment_option";
    public static final String URL_REQUEST_INVITE = "doplus_query";
    public static final String URL_DOPLUS_CONVERSION_DETAIL = "doplus_conversion_detail";
    public static final String URL_REFER_EARN = "get_referral_data";
    public static final String URL_LOCATION_SEARCH = "location_api";
    public static final String URL_WAIT_TIME = "waiting_time";
    public static final String URL_BILL_PAYMENT = "bill_payment";
    public static final String URL_FREE_PAYMENT = "pay_from_gift_card";
    public static final String URL_LINK_WALLET = "link_wallet";
    public static final String URL_VERIFY_WALLET = "verify_wallet";
    public static final String URL_PAY_FROM_DOWALLET = "pay_from_diner_wallet";
    public static final String URL_WALLET_SUMMARY = "wallet_summary";
    public static final String URL_INIT_PG = "init_pg_payment";
    public static final String URL_GET_PAYMENT_INVOICE = "get_payment_invoice";
    public static final String URL_GET_PAYMENT_STATUS = "get_payment_status";
    public static final String URL_EDIT_BOOKING = "edit_booking";
    public static final String URL_PROMO = "promocode";

    public static final String URL_VALIDATE_JP_NUMBER = "validate_jp_number";
    public static final String URL_REDEEM_DEAL = "redeem_deal";
    public static final String URL_GET_DINER_WALLET_AMOUNT = "get_diner_wallet_amount";
    public static final String URL_HOME_PAGE_CARD = "home_page_card";
    public static final String URL_RING_FENCING = "get_promocode_list";
    public static final String URL_RING_FENCING_VERYFY_REFERAL = "verify_referral";
    public static final String URL_MY_EARNINGS = "get_diner_wallet";
    public static final String URL_TRANSACTION_HISTORY = "transaction_history";

    public static final String URL_BOOKING_TIME_SLOT = "restaurant/slot";
    public static final String URL_VALIDATE_BILL = "bill/bill";
    public static final String URL_GET_OFFERS = "restaurant/offers";

    public static final String URL_GET_NOTIFICATION = "get_user_notification";

    // Constants for CTA
    public static final int CTA_RESERVE_AGAIN = 1;
    public static final int CTA_PAY_NOW = 2;
    public static final int CTA_PAID = 3;
    public static final int CTA_UPLOAD_BILL = 4;
    public static final int CTA_UPLOADED = 5;
    public static final int CTA_WRITE_REVIEW = 6;
    public static final int CTA_REVIEWED = 7;
    public static final int CTA_EDIT_REVIEW = 7;
    public static final int CTA_GET_DIRECTION = 8;
    public static final int CTA_WEBVIEW = 9;
    public static final int CTA_CALL_MANAGER = 10;
    public static final int CTA_SHARE_BOOKING = 11;
    public static final int CTA_CANCEL_BOOKING = 12;
    public static final int CTA_RESTAURANT_DETAIL = 13;
    public static final int CTA_REDEEM = 14;
    public static final int CTA_DEEPLINK = 15;
    public static final int CTA_VIEW_INVOICE = 16;
    public static final int CTA_EDIT_BOOKING = 17;
    public static final int CTA_RIDE_WITH_UBER = 18;
    // Tab Constants
    public static final String RESTAURANT_TAB_RESERVATION = "reservation";
    public static final String RESTAURANT_TAB_EVENT = "events";
    public static final String RESTAURANT_TAB_INFO = "info";
    public static final String RESTAURANT_TAB_REVIEW = "reviews";
    // Rating Screen Constants
    public static final String RATING_FEEDBACK = "submit_app_rating";
    public static final String JSON_KEY_TITLE = "title";
    public static final String JSON_KEY_SECTION_NAME = "sectionName";
    public static final String JSON_KEY_IS_HEADER = "isHeader";
    public static final String PARAM_POSITION = "POSITION";
    public static String URL_VERIFY_DINER_OTP = "verify_diner_otp";
    public static String URL_RESET_PASSWORD = "reset_password";

    public static String URL_VALIDATE_ACCOUNT = "validate_diner_account";
    public static String URL_LOGIN_VIA_OTP = "send_otp_to_diner";

    public static String URL_ADD_MONEY = "add_money";
    public static String PARAM_BILL_UPLOAD_FILE = "img";
    public static String PARAM_BILL_ID = "billID";
    public static String PARAM_REVIEW_BILL_DATA = "reviewData";
    public static String PARAM_BILL_UPLOAD_RESTID = "restaurantID";
    public static String PARAM_BILL_UPLOAD_BOOKING_ID = "bookingID";
    public static String PARAM_BILL_UPLOAD_TYPE = "billType";
    public static String PARAM_DEEPLINK = "deeplink";
    public static String PARAM_SORTBY = "sortby";
    public static String PARAM_SF = "sf";
    public static String IS_SELECTED = "IS_SELECTED";
    public static String POSITION = "POSITION";
    public static String EDIT_BOOKING_DATE = "EDIT_BOOKING_DATE";
    public static String BUNDLE_SELECTED_DATE = "BUNDLE_SELECTED_DATE";
    public static String BUNDLE_SELECTED_TIME = "BUNDLE_SELECTED_TIME";
    public static String BUNDLE_DISPLAY_TIME = "BUNDLE_DISPLAY_TIME";
    public static String BUNDLE_EXTRA_INFO = "BUNDLE_EXTRA_INFO";
    public static String BUNDLE_USER_EARNINGS = "BUNDLE_USER_EARNINGS";
    public static String BUNDLE_SELECTED_DEAL_QUANTITY = "BUNDLE_SELECTED_DEAL_QUANTITY";
    public static String BUNDLE_EARNINGS_INFO = "BUNDLE_EARNINGS_INFO";
    public static String BUNDLE_CASHBACK_INFO = "BUNDLE_CASHBACK_INFO";


    public static String BUNDLE_BILL_AMOUNT = "bill_amount";
    public static String BUNDLE_RESTAURANT_WALLET_AMOUNT = "restaurant_wallet";
    public static String BUNDLE_RESTAURANT_ID_DETAILS = "restaurant_id";
    public static String BUNDLE_DEAL_IDS = "deal_ids";
    public static String BUNDLE_DEAL_QUANTITY = "deal_quantity";
    public static String BUNDLE_DATE_TIME = "date_time";
    public static String BUNDLE_RESTAURANT_NAME_DETAIL = "restaurant_name";
    public static String BUNDLE_TYPE = "type";
    public static String BUNDLE_CASHBACK_DISCOUNT = "cashback_discount";
    public static String BUNDLE_MAX_CASHBACK = "maxcashback";


    public static String DATE_SLOT_TEXT = "DATE_SLOT_TEXT";
    public static String HAS_SLOT = "HAS_SLOT";
    public static String DATE_CLICKABLE_KEY = "DATE_CLICKABLE_KEY";
    public static int HAS_SLOT_YES = 1;

    public static int HAS_SLOT_DONT_KNOW = -1;

    public static int DATE_CLICKABLE = 1;
    public static int DATE_CLICKABLE_NOT_SET = -1;

    public static String DEAL_DATA = "DEAL_DATA";
    public static String SAVINGS_DATA = "SAVINGS_DATA";

    public static int USER_LAST_SEARCH_COUNT = 3;

    // upload bill params
    public static final String UPLOAD_BILL_PARAM_DEVICE_LATITUDE = "d_latitude";
    public static final String UPLOAD_BILL_PARAM_DEVICE_LONGITUDE = "d_longitude";
}
