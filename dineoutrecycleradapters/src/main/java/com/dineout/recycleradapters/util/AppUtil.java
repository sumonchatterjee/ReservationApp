package com.dineout.recycleradapters.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dineout.recycleradapters.R;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static com.example.dineoutnetworkmodule.AppConstant.RUPEE_IDENTIFIER;

public class AppUtil {

    public static SimpleDateFormat dateFormatOne = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static SimpleDateFormat dateFormatTimeTwo = new SimpleDateFormat("HH:mm:ss", Locale.US);
    public static SimpleDateFormat dateFormatTime = new SimpleDateFormat("hh:mm a", Locale.US);

    public static final String VALID_COLOR_REGEX = "(^#[0-9A-Fa-f]{6}$)|(^#[0-9A-Fa-f]{3}$)";

    /**
     * Format Floating Point Numbers with Maximum Fraction Digits
     *
     * @param data              - float point value
     * @param maxFractionDigits - maximum fraction digits
     * @return
     */
    public static String formatFloatDigits(float data, int maxFractionDigits, int minFractionDigits) {
        //Instantiate NumberFormat Float
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(maxFractionDigits);
        numberFormat.setMinimumFractionDigits(minFractionDigits);

        return (numberFormat.format(data));
    }

    // Has Rating
    public static boolean hasNoRating(String rating) {
        return rating == null || rating.isEmpty() || rating.equals("0") || rating.equals("0.0");

    }

    /**
     * Check if String is Empty
     *
     * @param data
     * @return
     */
    public static boolean isStringEmpty(String data) {
        if (data == null || data.trim().isEmpty() || "null".equalsIgnoreCase(data)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set String empty
     *
     * @param data
     * @return
     */
    public static String setStringEmpty(String data) {

        if (isStringEmpty(data))
            return "";

        return data.trim();
    }

    /**
     * Check if Collection is Empty
     *
     * @param data
     * @return
     */
    public static boolean isCollectionEmpty(Collection data) {

        return data == null || data.isEmpty();

    }

    /**
     * Format Integer Numbers with Maximum Integer Digits
     *
     * @param data
     * @param maxIntegerDigits
     * @param minIntegerDigits
     * @return
     */
    public static String formatIntegerDigits(int data, int maxIntegerDigits, int minIntegerDigits) {
        //Instantiate NumberFormat Integer
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumIntegerDigits(maxIntegerDigits);
        numberFormat.setMinimumIntegerDigits(minIntegerDigits);

        return (numberFormat.format(data));
    }

    /**
     * Set Rating Value and Corresponding background
     *
     * @param rating   - Rating value
     * @param textView - View to show Rating
     */
    public static void setRatingValueBackground(String rating, TextView textView) {
        //Check for blank
        if (hasNoRating(rating)) {
            textView.setText("- -"); //DIN 567
            rating = "0";
        } else {
            textView.setText(AppUtil.formatFloatDigits(Float.valueOf(rating), 1, 1));
        }

        int value = (int) Float.parseFloat(rating);

        switch (value) {
            case 0:
                textView.setBackgroundResource(R.drawable.grey_circle);
                break;

            case 1:
                textView.setBackgroundResource(R.drawable.red_circle);
                break;

            case 2:
                textView.setBackgroundResource(R.drawable.orange_circle);
                break;

            case 3:
                textView.setBackgroundResource(R.drawable.yellow_circle);
                break;

            case 4:
                textView.setBackgroundResource(R.drawable.light_green_circle);
                break;

            case 5:
                textView.setBackgroundResource(R.drawable.green_circle);
                break;
        }
    }

    //Get Color for Booking Status
    public static int getBookingStatusColor(String status, String bookingStatus, TextView bookingStatusTextView) {
        //Instantiate Drawable
        int resourceId = 0;

        //Check Booking Status Code
        if (!AppUtil.isStringEmpty(bookingStatus)) {
            status = ((bookingStatus.equalsIgnoreCase("NW")) ? "Pending" :
                    ((bookingStatus.equalsIgnoreCase("DN")) ? "Denied" : status));

            // Set Status Text
            bookingStatusTextView.setText(status.toUpperCase());
        }

        resourceId = getColorResourceId(status);

        return resourceId;
    }

    public static int getColorResourceId(String status) {
        //Instantiate Color
        int resourceId = 0;

        switch (status.toUpperCase()) {
            case "DENIED":
                resourceId = R.color.app_status_denied;
                break;

            case "CONFIRMED":
                resourceId = R.color.app_status_confirmed;
                break;

            case "CANCELLED":
                resourceId = R.color.app_status_cancelled;
                break;

            case "PENDING":
                resourceId = R.color.app_status_confirmed;
                break;

            case "DELETED":
                resourceId = R.color.app_status_denied;
                break;

            case "WAITLIST":
                resourceId = R.color.app_status_waitlist;
                break;

            default:
                resourceId = R.color.app_status_expired;
                break;
        }

        return resourceId;
    }

    //Get Drawable for Booking Status
    public static int getBookingStatusDrawable(String status, String bookingStatus, TextView bookingStatusTextView) {
        //Instantiate Drawable
        int resourceId = 0;

        //Check Booking Status Code
        if (!AppUtil.isStringEmpty(bookingStatus)) {
            status = ((bookingStatus.equalsIgnoreCase("NW")) ? "PENDING" :
                    ((bookingStatus.equalsIgnoreCase("DN")) ? "DENIED" : status));

            // Set Status Text
            bookingStatusTextView.setText(status.toUpperCase());
        }

        resourceId = getResourceId(status);

        // Set Background Color
        bookingStatusTextView.setBackgroundResource(resourceId);

        return resourceId;
    }

    public static int getResourceId(String status) {
        //Instantiate Drawable
        int resourceId = 0;

        switch (status.toUpperCase()) {
            case "DENIED":
                resourceId = R.drawable.booking_status_denied_shape;
                break;

            case "CONFIRMED":
                resourceId = R.drawable.booking_status_confirmed_shape;
                break;

            case "CANCELLED":
                resourceId = R.drawable.booking_status_cancelled_shape;
                break;

            case "PENDING":
                resourceId = R.drawable.booking_status_pending_shape;
                break;

            case "DELETED":
                resourceId = R.drawable.booking_status_denied_shape;
                break;

            case "WAITLIST":
                resourceId = R.drawable.booking_status_waitlist_shape;
                break;

            default:
                resourceId = R.drawable.booking_status_expired_shape;
                break;
        }

        return resourceId;
    }


    /**
     * This function is used to set Progress Bar Decor
     *
     * @param context
     * @param progressBar
     */
    public static void setProgressBarDecor(Context context, ProgressBar progressBar) {
        // Check for NULL
        if (progressBar != null) {
            // Set Decor
            progressBar.getIndeterminateDrawable().
                    setColorFilter(context.getResources().getColor(R.color.colorPrimary),
                            android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }


    // Check if Recency value is lower than 15
    public static boolean showRecency(String recency) {
        // Either Recency does not exists or if its higher than 15 do not show New Tag
        if (isStringEmpty(recency) || Integer.parseInt(recency.trim()) == 0) {
            return false;
        } else {
            return true;
        }
    }


    public static String getDisplayFormatDate(String dt_str) {

        if (dt_str == null)
            return " ";
        if ("".equals(dt_str.trim()))
            return "";

        final Calendar c = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        try {
            c.setTime(dateFormatOne.parse(dt_str));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String resutlDt = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) +
                ", " + c.get(Calendar.DAY_OF_MONTH) + getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH)) +
                " " + new SimpleDateFormat("MMM", Locale.US).format(c.getTime()) + " " + c.get(Calendar.YEAR);

        return resutlDt;
    }


    /*
     * take date string with format like 01-08-2015 return string like Jan 8
     */
    private static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    /**
     * Convert DP to PX by providing dimension's resource identifier
     *
     * @param dimenResourceIdentifier
     * @return
     */
    public static int convertDpToPx(Context context, int dimenResourceIdentifier) {
        return (context.getResources().getDimensionPixelSize(dimenResourceIdentifier));
    }


    public static boolean handleCallPermission(final Activity activityContext) {
        boolean isPermissionGranted;

        //Check if Permission was Granted
        int permissionCallCheck = ContextCompat.checkSelfPermission(activityContext, Manifest.permission.CALL_PHONE);

        //If Permission is Granted
        if (permissionCallCheck == PackageManager.PERMISSION_GRANTED) {
            //Set Flag
            isPermissionGranted = true;

        } else {
            //Set Flag
            isPermissionGranted = false;

            //Check if the Permission was denied byb User previously
            boolean isPermissionDenied = ActivityCompat.shouldShowRequestPermissionRationale(activityContext,
                    Manifest.permission.CALL_PHONE);

            if (isPermissionDenied) { //Handling Second Time
                //Prepare Bundle
                Bundle bundle = new Bundle();
                bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, activityContext.getString(R.string.title_permission_required));
                bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION, "Calling permission is required to make calls directly to the Restaurant");
                bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OK");

                //Show Explanation
                showExplanatoryAlert(activityContext, bundle, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Dismiss Dialog
                        dialog.dismiss();

                        //Request for Permission
                        ActivityCompat.requestPermissions(activityContext,
                                new String[]{Manifest.permission.CALL_PHONE},
                                AppConstant.REQUEST_PERMISSION_CALL_PHONE);
                    }
                });

            } else { //Handling First Time
                //Request for Permission
                ActivityCompat.requestPermissions(activityContext,
                        new String[]{Manifest.permission.CALL_PHONE},
                        AppConstant.REQUEST_PERMISSION_CALL_PHONE);
            }
        }

        return isPermissionGranted;
    }


    private static void showExplanatoryAlert(Context context, Bundle bundle,
                                             final DialogInterface.OnClickListener listener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(bundle.getString(AppConstant.BUNDLE_DIALOG_TITLE));
        alert.setMessage(bundle.getString(AppConstant.BUNDLE_DIALOG_DESCRIPTION));
        alert.setPositiveButton(bundle.getString(AppConstant.BUNDLE_DIALOG_DESCRIPTION), listener);
    }


    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }


    public static String getTimeForBookingDetails(String timeInMS) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTimeInMillis(Long.parseLong(timeInMS));
            DateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            String formattedTime = outputFormat.format(c.getTime());


            return formattedTime;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }


    }

    /*
     * take date string with format like 01-08-2015 return string like 8th Jan 2015
     */
    public static String getDisplayFormatDateBookingDetails(String dt_str) {

        if (dt_str == null)
            return " ";
        if ("".equals(dt_str.trim()))
            return "";

        Calendar c = Calendar.getInstance();
//        Calendar today = Calendar.getInstance();
//        today.setTime(new Date());

        try {
            c.setTime(new Date(Long.parseLong(dt_str)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String resultDt = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + "," +
                c.get(Calendar.DAY_OF_MONTH) + getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH)) +
                " " + new SimpleDateFormat("MMM", Locale.US).format(c.getTime());

        return resultDt;
    }


    public static String getDisplayFormatTime(String time) {
        String result = null;

        try {
            final Date dateObj = dateFormatTimeTwo.parse(time);
            result = dateFormatTime.format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static Spannable getColoredText(String sourceText, int color) {
        int startIndex = sourceText.indexOf(AppConstant.COLOR_IDENTIFIER);
        int nextIndex = sourceText.indexOf(AppConstant.COLOR_IDENTIFIER, (startIndex + AppConstant.COLOR_IDENTIFIER.length()));

        SpannableString spannableString = new SpannableString(sourceText.replaceAll(AppConstant.COLOR_IDENTIFIER, ""));

        if (startIndex > -1 && nextIndex > -1) {
            spannableString.setSpan(new ForegroundColorSpan(color), startIndex,
                    (nextIndex - AppConstant.COLOR_IDENTIFIER.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    public static Spannable getStrikedText(String sourceText) {
        SpannableString spannableString = new SpannableString(sourceText);

        spannableString.setSpan(new StrikethroughSpan(), 0, sourceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static long convertMillisecondsToSeconds(long timestamp) {
        return (timestamp / 1000L);
    }

    public static long convertSecondsToMilliseconds(long timestamp) {
        return (timestamp * 1000);
    }

    public static String getDisplayMyReviewDateTime(long dateTimeMilliSec) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM, yyyy-hh:mm aa", Locale.US);

        Date date = new Date();
        date.setTime(dateTimeMilliSec);

        String dateStr = simpleDateFormat.format(date);
        dateStr = dateStr.replace("-", " at ");

        return dateStr;
    }

    public static String getDisplayReviewDateTime(long dateTimeMilliSec) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.US);

        Date date = new Date();
        date.setTime(dateTimeMilliSec);

        return simpleDateFormat.format(date);
    }

    public static String getRestaurantType(int restType) {
        String restaurantType = "";

        if (restType == 0) {
            restaurantType = AppConstant.RESTAURANT_TYPE_DISCOVERY;

        } else if (restType == 1) {
            restaurantType = AppConstant.RESTAURANT_TYPE_PARTIAL_FULFILLMENT;

        } else if (restType == 2) {
            restaurantType = AppConstant.RESTAURANT_TYPE_FULFILLMENT;
        }

        return restaurantType;
    }


    public static String getRestaurantTypeForAnalytics(int restType) {
        String restaurantType = "";

        if (restType == 0) {
            restaurantType = AppConstant.RESTAURANT_TYPE_DISCOVERY_ANALYTICS;

        } else if (restType == 1) {
            restaurantType = AppConstant.RESTAURANT_TYPE_PARTIAL_FULFILLMENT_ANALYTICS;

        } else if (restType == 2) {
            restaurantType = AppConstant.RESTAURANT_TYPE_FULFILLMENT_ANALYTICS;
        }

        return restaurantType;
    }

    public static Spanned renderRupeeSymbol(String text) {
        String modifiedString = "";

        if (!isStringEmpty(text)) {
            modifiedString = text.replaceAll("\\\\u20B9", "&#x20b9;");
        }

        return Html.fromHtml(modifiedString);
    }

    public static int getCtaButtonDrawable(int action, boolean isEnabled, boolean isDarkTheme) {
        int drawableResource = 0;

        switch (action) {
            case AppConstant.CTA_RESERVE_AGAIN: // Reserve Now/Reserve Again
                drawableResource = ((isDarkTheme) ? R.drawable.ic_reserve_again_enabled : R.drawable.ic_reserve_again_dark_enabled);
                break;

            case AppConstant.CTA_PAY_NOW: // Pay Now
                drawableResource = ((isDarkTheme) ?
                        ((isEnabled) ? R.drawable.ic_paynow_enabled : R.drawable.ic_paynow_disabled) :
                        ((isEnabled) ? R.drawable.ic_paynow_dark_enabled : R.drawable.ic_paynow_dark_disabled));
                break;

            case AppConstant.CTA_PAID: // Paid
                drawableResource = ((isDarkTheme) ?
                        ((isEnabled) ? R.drawable.ic_paynow_enabled : R.drawable.ic_paynow_disabled) :
                        ((isEnabled) ? R.drawable.ic_paynow_dark_enabled : R.drawable.ic_paynow_dark_disabled));
                break;

            case AppConstant.CTA_UPLOAD_BILL:
                drawableResource = ((isDarkTheme) ?
                        ((isEnabled) ? R.drawable.ic_upload_enabled : R.drawable.ic_upload_disabled) :
                        ((isEnabled) ? R.drawable.ic_upload_dark_enabled : R.drawable.ic_upload_dark_disabled));
                break;

            case AppConstant.CTA_UPLOADED:
                drawableResource = ((isDarkTheme) ?
                        ((isEnabled) ? R.drawable.ic_uploaded_enabled : R.drawable.ic_uploaded_disabled) :
                        ((isEnabled) ? R.drawable.ic_uploaded_dark_enabled : R.drawable.ic_uploaded_dark_disabled));
                break;

            case AppConstant.CTA_WRITE_REVIEW: // Write A Review
                drawableResource = ((isDarkTheme) ?
                        ((isEnabled) ? R.drawable.ic_write_review_enabled : R.drawable.ic_write_review_disabled) :
                        ((isEnabled) ? R.drawable.ic_write_review_dark_enabled : R.drawable.ic_write_review_dark_disabled));
                break;

            case AppConstant.CTA_REVIEWED: // Reviewed
                drawableResource = ((isDarkTheme) ?
                        ((isEnabled) ? R.drawable.ic_already_reviewed_enabled : R.drawable.ic_already_reviewed_disabled) :
                        ((isEnabled) ? R.drawable.ic_already_reviewed_dark_enabled : R.drawable.ic_already_reviewed_dark_disabled));
                break;

            case AppConstant.CTA_GET_DIRECTION: // Get Direction
                drawableResource = ((isDarkTheme) ? R.drawable.ic_get_directions_enabled : R.drawable.ic_get_directions_dark_enabled);
                break;
        }

        return drawableResource;
    }

    public static int getCtaButtonColor(boolean isEnabled, boolean isDarkTheme) {
        return ((isDarkTheme) ?
                ((isEnabled) ? R.color.white : R.color.white_66) :
                ((isEnabled) ? R.color.grey_4D : R.color.black_40));
    }

    public static Spanned replaceForRupeeSign(String text) {
        if (!isStringEmpty(text)) {
            text = text.replaceAll(RUPEE_IDENTIFIER, "\u20B9");
        }

        return Html.fromHtml(text);
    }

    public static void hideKeyboard(Activity mActivity) {
        if (mActivity != null) {
            hideKeyboard(mActivity.getWindow().getDecorView(), mActivity);
        }
    }

    public static void hideKeyboard(View view, Activity mActivity) {
        if (mActivity != null && view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static void showKeyBoard(Activity mActivity){
        if(mActivity!=null) {
            InputMethodManager imgr = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public static String getCTABtnBg(Context context, int action) {
        String returnValue = "";
        try {
            String btnBgMapString = DOPreferences.getCTABtnBgColorFromDOPref(context);
            JSONObject btnBgMap = new JSONObject(btnBgMapString);

            returnValue = btnBgMap.optString(String.valueOf(action));
        } catch (Exception e) {
            // Exception
        }

        if(TextUtils.isEmpty(returnValue)){
            returnValue = "#fa5757";
        }
        return returnValue;
    }

    public static void setTextViewInfo(View v, JSONObject infoObj) {
        try {
            if (v instanceof TextView) {
                if (infoObj != null) {
                    v.setVisibility(View.VISIBLE);

                    TextView tv = (TextView) v;
                    String text = infoObj.optString("text");
                    String color = infoObj.optString("color");

                    if (tv != null) {
                        if (!TextUtils.isEmpty(text)) {
                            text = AppUtil.renderRupeeSymbol(text).toString();
                            tv.setText(text);
                        } else {
                            v.setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(color)) {
                            tv.setTextColor(Color.parseColor(color));
                        } else {
                            v.setVisibility(View.GONE);
                        }
                    }

                } else {
                    v.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            // Exception
        }
    }

    public static void appendTo(JSONArray oldJSONArray, JSONArray newJSONArray) {
        if (oldJSONArray != null && newJSONArray != null) {
            for (int i = 0; i < newJSONArray.length(); i++) {
                oldJSONArray.put(newJSONArray.opt(i));
            }
        }
    }

    public static void appendTo(JSONObject oldJSONObject, JSONObject newJSONObject) {
        try {
            if (oldJSONObject != null && newJSONObject != null) {
                Iterator keys = newJSONObject.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    oldJSONObject.put(key, newJSONObject.optJSONObject(key));
                }
            }
        } catch (Exception e) {
            // Exception
        }
    }

    public static boolean isTrue(JSONObject obj, String key) {
        return obj != null
                && (obj.optBoolean(key)
                || "1".equals(obj.optString(key))
                || 1 == obj.optInt(key));
    }

    public static String prepareString(String... params) {
        String returnValue = "";

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (!TextUtils.isEmpty(params[i])) {
                    if (!TextUtils.isEmpty(returnValue) && i < params.length - 1) {
                        returnValue += ":";
                    }
                    returnValue += params[i];
                }
            }
        }

        return returnValue;
    }


}
