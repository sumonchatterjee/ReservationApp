package com.dineout.recycleradapters.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.example.dineoutnetworkmodule.DOPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

/**
 * Created by sawai.parihar on 16/03/17.
 */

public class RateNReviewUtil {
    public static final String BOOKING_ID = "b_id";
    public static final String RESTAURANT_NAME = "rest_name";
    public static final String RESTAURANT_ID = "rest_id";
    public static final String INFO_STRING = "info_string";
    public static final String REVIEW_ID = "REVIEW_ID";
    public static final String REVIEW_HINT_TEXT = "place_holder_txt";
    public static final String REVIEW_TEXT = "reviewText";
    public static final String REVIEW_RATING = "rating";
    public static final String REVIEW_TAGS = "tags";
    public static final String SUBMIT_BTN_TEXT_KEY = "submit_button_text";
    public static final String VALIDATE_TAGS_KEY = "validate_tags";
    public static final String APP_RATING_THRESHOLD_TEXT_KEY = "threshold_text";

    public static final String REVIEW_ACTION = "review_action";
    public static final String REVIEW_POSITIVE_ACTION = "1";
    public static final String REVIEW_NEGATIVE_ACTION = "2";
    public static final String REVIEW_NONE_ACTION = "3";

    public static final String TARGET_SCREEN_KEY = "target_screen_key";
    public static final String RESTAURANT_RAVIEW = "restaurant_review";
    public static final String IN_APP_RATING = "in_app_rating";
    public static final String CANCELABLE_FLAG = "cancelable_flag";

    public static final String TYPE = "type";
    public static final String HEADER_LAYOUT = "header_layout";
    public static final String RATING_LAYOUT = "rating_layout";
    public static final String REVIEW_CHIPS_LAYOUT = "review_chips_layout";
    public static final String TAP_TO_WRITE_REVIEW_LAYOUT = "tap_to_write_review_layout";


    public static final String ASK_USER_DIALOG_TYPE = "ask_user_dialog_type";
    public static final String ASK_USER_DIALOG_TYPE_STICKY = "ask_user_dialog_type_sticky";
    public static final String ASK_USER_DIALOG_TYPE_POP_UP = "ask_user_dialog_type_pop_up";
    public static final String GA_TRACKING_CATEGORY_NAME_KEY = "ga_tracking_category_name_key";


    public interface RateNReviewCallbacks {
        void onDialogDismiss();
        void onReviewSubmission();
        void onRNRError(JSONObject errorObject);
    }

    public interface UploadBillRateNReviewCallback {
        void onReviewSubmitClick(JSONObject object);
    }

    public static void initMainData(JSONArray mainData, JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                mainData.put(array.get(i));
            } catch (JSONException e) {}
        }
    }

    public static JSONObject getJsonObject(String str) {
        JSONObject returnValue;

        try {
            returnValue = new JSONObject(str);
        } catch (Exception e) {
            returnValue = new JSONObject();
        }

        return returnValue;
    }

    public static void addValueToJsonObject(JSONObject obj, String key, Object value) {
        try {
            obj.put(key, value);
        } catch (Exception e) {
            // Exception
        }
    }

    public static int getCurrentRatingValue(JSONArray array) {
        int returnValue = 0;

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                if (array.optJSONObject(i).optString(TYPE).equals(RATING_LAYOUT)) {
                    returnValue = array.optJSONObject(i).optInt(REVIEW_RATING);
                    break;
                }
            }
        }
        return returnValue;
    }

    public static String getReviewText(JSONArray array) {
        String returnValue = "";

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                if (array.optJSONObject(i).optString(TYPE).equals(TAP_TO_WRITE_REVIEW_LAYOUT)) {
                    returnValue =  array.optJSONObject(i).optString(REVIEW_TEXT);
                }
            }
        }
        return returnValue;
    }

    public static String getReviewTags(int rating, JSONArray array) {
        String returnValue = "";

        if (rating > 0) {
            rating--;
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    if (array.optJSONObject(i).optString(TYPE).equals(REVIEW_CHIPS_LAYOUT)) {
                        JSONArray tagsArr = array.optJSONObject(i).optJSONArray("ratingTagsArr");

                        if (tagsArr != null) {
                            JSONArray ratingTagArr = tagsArr.optJSONArray(rating);

                            if (ratingTagArr != null) {
                                for (int j = 0; j < ratingTagArr.length(); j++) {
                                    if ("1".equals(ratingTagArr.optJSONObject(j).optString("status"))
                                            || "true".equals(ratingTagArr.optJSONObject(j).optString("status"))) {
                                        if (!TextUtils.isEmpty(returnValue)) {
                                            returnValue += ",";
                                        }
                                        returnValue += ratingTagArr.optJSONObject(j).optString("id");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return returnValue;
    }

    public static JSONArray getFilteredArrayAtZeroRating(JSONArray mainData) {
        JSONArray array = new JSONArray();
        if (mainData != null) {
            try {
                array.put(mainData.get(0));
                array.put(mainData.get(1));
            } catch (JSONException e) {

            }
        }

        return array;
    }

    public static JSONArray getFilteredArrayAtNonZeroRating(Context context, JSONArray mainData) {
        JSONArray array = new JSONArray();
        if (mainData != null) {

            // set item 1
            array.put(mainData.opt(0));

            // set item 2
            array.put(mainData.opt(1));

            if (IN_APP_RATING.equals(mainData.optJSONObject(2).optString(TARGET_SCREEN_KEY))) {
                int rating = getCurrentRatingValue(mainData);
                int thresholdValue = DOPreferences.retrieveAppRatingThresholdValue(context);

                if (rating < thresholdValue) {
                    array.put(mainData.opt(2));
                } else {
                    array.put(mainData.opt(3));
                }
            } else {
                array.put(mainData.opt(2));
                array.put(mainData.opt(3));
            }
        }

        return array;
    }

    public static JSONArray prepareAdapterData(String infoString) {
        JSONArray returnValue = new JSONArray();
        try {
            if (!TextUtils.isEmpty(infoString)) {
                JSONObject obj = new JSONObject(infoString);

                // set item 1
                JSONObject child = new JSONObject();
                child.put(TYPE, HEADER_LAYOUT);
                child.put("text", obj.optString("text"));
                child.put(TARGET_SCREEN_KEY, obj.optString(TARGET_SCREEN_KEY));
                returnValue.put(child);

                // set item 2
                child = new JSONObject();
                child.put(TYPE, RATING_LAYOUT);
                child.put(REVIEW_RATING, obj.optInt(REVIEW_RATING, 0));
                child.put(TARGET_SCREEN_KEY, obj.optString(TARGET_SCREEN_KEY));

                JSONArray ratingReviewArr = new JSONArray();
                if (obj.optJSONObject("1") != null) ratingReviewArr.put(obj.optJSONObject("1").optString("rating_text"));
                if (obj.optJSONObject("2") != null) ratingReviewArr.put(obj.optJSONObject("2").optString("rating_text"));
                if (obj.optJSONObject("3") != null) ratingReviewArr.put(obj.optJSONObject("3").optString("rating_text"));
                if (obj.optJSONObject("4") != null) ratingReviewArr.put(obj.optJSONObject("4").optString("rating_text"));
                if (obj.optJSONObject("5") != null) ratingReviewArr.put(obj.optJSONObject("5").optString("rating_text"));
                child.put("ratingReviewArr", ratingReviewArr);

                returnValue.put(child);


                // set item 3
                child = new JSONObject();
                child.put(TYPE, REVIEW_CHIPS_LAYOUT);
                child.put(TARGET_SCREEN_KEY, obj.optString(TARGET_SCREEN_KEY));
                JSONArray ratingFeedbackArr = new JSONArray();
                if (obj.optJSONObject("1") != null) ratingFeedbackArr.put(obj.optJSONObject("1").optString("feedback_text"));
                if (obj.optJSONObject("2") != null) ratingFeedbackArr.put(obj.optJSONObject("2").optString("feedback_text"));
                if (obj.optJSONObject("3") != null) ratingFeedbackArr.put(obj.optJSONObject("3").optString("feedback_text"));
                if (obj.optJSONObject("4") != null) ratingFeedbackArr.put(obj.optJSONObject("4").optString("feedback_text"));
                if (obj.optJSONObject("5") != null) ratingFeedbackArr.put(obj.optJSONObject("5").optString("feedback_text"));
                child.put("ratingFeedbackArr", ratingFeedbackArr);

                JSONArray ratingTagsArr = new JSONArray();
                if (obj.optJSONObject("1") != null) ratingTagsArr.put(obj.optJSONObject("1").optJSONArray("tags"));
                if (obj.optJSONObject("2") != null) ratingTagsArr.put(obj.optJSONObject("2").optJSONArray("tags"));
                if (obj.optJSONObject("3") != null) ratingTagsArr.put(obj.optJSONObject("3").optJSONArray("tags"));
                if (obj.optJSONObject("4") != null) ratingTagsArr.put(obj.optJSONObject("4").optJSONArray("tags"));
                if (obj.optJSONObject("5") != null) ratingTagsArr.put(obj.optJSONObject("5").optJSONArray("tags"));
                child.put("ratingTagsArr", ratingTagsArr);

                returnValue.put(child);

                child = new JSONObject();
                child.put(TYPE, TAP_TO_WRITE_REVIEW_LAYOUT);
                child.put(TARGET_SCREEN_KEY, obj.optString(TARGET_SCREEN_KEY));
                child.put(REVIEW_TEXT, obj.optString(REVIEW_TEXT));
                child.put(REVIEW_HINT_TEXT, obj.optString(REVIEW_HINT_TEXT));

                JSONArray appThresholdTextArr = new JSONArray();
                if (obj.optJSONObject("1") != null) appThresholdTextArr.put(obj.optJSONObject("1").optString(APP_RATING_THRESHOLD_TEXT_KEY));
                if (obj.optJSONObject("2") != null) appThresholdTextArr.put(obj.optJSONObject("2").optString(APP_RATING_THRESHOLD_TEXT_KEY));
                if (obj.optJSONObject("3") != null) appThresholdTextArr.put(obj.optJSONObject("3").optString(APP_RATING_THRESHOLD_TEXT_KEY));
                if (obj.optJSONObject("4") != null) appThresholdTextArr.put(obj.optJSONObject("4").optString(APP_RATING_THRESHOLD_TEXT_KEY));
                if (obj.optJSONObject("5") != null) appThresholdTextArr.put(obj.optJSONObject("5").optString(APP_RATING_THRESHOLD_TEXT_KEY));
                child.put("appThresholdTextArr", appThresholdTextArr);
                returnValue.put(child);
            }
        } catch (Exception e) {
            // Exception
        }

        return returnValue;
    }

    public static JSONObject appendObject(JSONObject newObj, JSONObject oldObj) {
        try {
            Iterator<String> keys;
            if (newObj != null && oldObj != null && (keys = oldObj.keys()) != null) {
                while (keys.hasNext()) {
                    String key = keys.next();
                    newObj.put(key, oldObj.opt(key));
                }
            }
        } catch (Exception e) {
            // Exception
        }

        return newObj;
    }

    public static String getSubmitButtonText(String objString, int rating) {
        String returnValue = "";

        try {
            JSONObject obj = new JSONObject(objString);
            if (obj != null) {
                JSONObject objAccordingToRating = obj.optJSONObject(String.valueOf(rating));

                if (objAccordingToRating != null) {
                    returnValue = objAccordingToRating.optString(SUBMIT_BTN_TEXT_KEY, "");
                }
            }
        } catch (Exception e) {
            // Exception
        }
        return returnValue;
    }

    public static boolean isValidateTags(String objString, int rating) {
        boolean returnValue = false;

        try {
            JSONObject obj = new JSONObject(objString);
            if (obj != null) {
                JSONObject objAccordingToRating = obj.optJSONObject(String.valueOf(rating));

                if (objAccordingToRating != null) {
                    String validate = objAccordingToRating.optString(VALIDATE_TAGS_KEY);
                    returnValue = "1".equals(validate) || "true".equals(validate);
                }
            }
        } catch (Exception e) {
            // Exception
        }
        return returnValue;
    }

    public static void updateInfo(JSONObject obj, JSONObject newObj) {
        try {
            if (obj != null && newObj != null) {
                // update review id
                String reviewId = newObj.optString(REVIEW_ID);
                obj.put(REVIEW_ID, reviewId);

                // update rating
                int rating = newObj.optInt(REVIEW_RATING, 0);
                obj.put(REVIEW_RATING, rating);

                // update review text
                String reviewDesc = newObj.optString(REVIEW_TEXT);
                obj.put(REVIEW_TEXT, reviewDesc);

                // update review tags
                JSONObject ratingObj = obj.optJSONObject(String.valueOf(rating));
                if (ratingObj != null) {
                    String reviewTagsStr = newObj.optString("reviewTags");
                    if (!TextUtils.isEmpty(reviewTagsStr)) {
                        JSONArray newTagsArr = new JSONArray(reviewTagsStr);
                        if (newTagsArr != null) {
                            JSONArray tagsArr;
                            if ((tagsArr = ratingObj.optJSONArray(REVIEW_TAGS)) != null) {
                                for (int i = 0; i < newTagsArr.length(); i++) {
                                    JSONObject newTagObj = newTagsArr.optJSONObject(i);
                                    for (int j = 0; j < tagsArr.length(); j++) {
                                        JSONObject tagObj = tagsArr.optJSONObject(j);
                                        if (newTagObj != null && tagObj != null
                                                && newTagObj.optInt("id") == tagObj.optInt("id")) {
                                            tagObj.put("status", true);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }


        } catch (Exception e) {
            // Exception
        }
    }

    public static void updateViewState(View view, boolean enabled) {
        try {
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;

                for (int idx = 0; idx < group.getChildCount(); idx++) {
                    updateViewState(group.getChildAt(idx), enabled);
                }
            }
        } catch (Exception e) {
            // Exception
        }
    }

    public static boolean getDialogCancelableFlagStatus(String infoString) {
        boolean returnValue = true;

        try {
            if (!TextUtils.isEmpty(infoString)) {
                JSONObject obj = new JSONObject(infoString);
                returnValue = obj.optBoolean(CANCELABLE_FLAG, true);
            }
        } catch (Exception e) {
            // Exception
        }

        return returnValue;
    }

    private static final long IN_APP_RATING_TIMER_THRESHOLD_DIFF = 24 * 60 * 60 * 1000;
    private static final String IN_APP_RATING_TIME_KEY = "app_rating_time_key";
    private static final String IN_APP_RATING_VALUE_KEY = "app_rating_value_key";
    public static void storeAppRatingFlagToPref(Context context, int appRating) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(IN_APP_RATING_TIME_KEY, System.currentTimeMillis());
            jsonObject.put(IN_APP_RATING_VALUE_KEY, appRating);

            DOPreferences.setInAppRatingJSON(context, jsonObject.toString());
        } catch (Exception e) {
            // Exception
        }
    }

    public static boolean isInAppRatingTimerExpired(Context context) {
        boolean returnValue = true;

        String appRatingObjectStr = DOPreferences.getInAppRatingJSON(context);

        if (!TextUtils.isEmpty(appRatingObjectStr)) {
            try {
                JSONObject jsonObject = new JSONObject(appRatingObjectStr);
                long appRatingTime = jsonObject.optLong(IN_APP_RATING_TIME_KEY);
                long currTime = System.currentTimeMillis();

                returnValue = (currTime - appRatingTime) > IN_APP_RATING_TIMER_THRESHOLD_DIFF;
            } catch (Exception e) {
                // Exception
            }
        }

        return returnValue;
    }

    public static int getInAppRatingValuePref(Context context) {
        int returnValue = 0;

        String appRatingObjectStr = DOPreferences.getInAppRatingJSON(context);

        if (!TextUtils.isEmpty(appRatingObjectStr)) {
            try {
                JSONObject jsonObject = new JSONObject(appRatingObjectStr);
                returnValue = jsonObject.optInt(IN_APP_RATING_VALUE_KEY);

            } catch (Exception e) {
                // Exception
            }
        }

        return returnValue;
    }
}
