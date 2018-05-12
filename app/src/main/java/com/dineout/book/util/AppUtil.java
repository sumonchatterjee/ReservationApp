package com.dineout.book.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.BuildConfig;
import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.controller.IntentManager;
import com.dineout.book.fragment.master.MasterDOFragment;
//import com.dineout.livehandler.AccessibilityPreference;
//import com.dineout.livehandler.AccessibilityUtils;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.facebook.appevents.AppEventsConstants;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.analytics.utilities.AnalyticsUtil.IS_DEVELOPMENT;

public class AppUtil {
    public final static String DO_LOGIN_RECEIVER = "com.dineout.book.login";
    final private static String LOG_TAG = "DineOut";
    public static SimpleDateFormat dateFormatOne = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static String mPhoneNumber;

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static void v(String msg) {
        if (IS_DEVELOPMENT) {
            Log.v(LOG_TAG, TextUtils.isEmpty(msg) ? "null" : msg);
        }
    }

    public static void i(String msg) {
        if (IS_DEVELOPMENT) {
            Log.i(LOG_TAG, TextUtils.isEmpty(msg) ? "null" : msg);
        }
    }

    public static void w(String msg) {
        if (IS_DEVELOPMENT) {
            Log.w(LOG_TAG, TextUtils.isEmpty(msg) ? "null" : msg);
        }
    }

    public static void d(String msg) {
        if (IS_DEVELOPMENT) {
            Log.d(LOG_TAG, TextUtils.isEmpty(msg) ? "null" : msg);
        }
    }

    public static void e(String msg) {
        Log.e(LOG_TAG, TextUtils.isEmpty(msg) ? "null" : msg);
    }

    public static void e(String msg, Exception e) {
        Log.e(LOG_TAG, TextUtils.isEmpty(msg) ? "null" : msg, e);
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

    public static String getVersionName() {

        return BuildConfig.VERSION_NAME;
    }

    public static int getVersionCode() {

        return BuildConfig.VERSION_CODE;
    }

    public static String getImageUrlSizeParameter(String url) {

        String queryString = "";

        if (!TextUtils.isEmpty(url)) {
            if (Connectivity.getNetworkType() == Connectivity.NetworkType.NW_2G) {
                queryString = url + "?q=70";
            } else {
                queryString = url + "?q=80";
            }
        }

        return queryString;
    }

    public static boolean isConnectionAvailable(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {
                return true;
            }
        }

        return false;
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

    public static boolean hasNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        if (context == null)
            return false;

        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            for (NetworkInfo ni : cm.getAllNetworkInfo()) {
                if (ni.getType() == ConnectivityManager.TYPE_WIFI && ni.isConnected())
                    haveConnectedWifi = true;
                if (ni.getType() == ConnectivityManager.TYPE_MOBILE && ni.isConnected())
                    haveConnectedMobile = true;
            }

            return haveConnectedWifi || haveConnectedMobile;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static void sendMail(Context context) {
        // send email
                        /* Create the Intent */
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        /* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.DINEOUT_INFO_EMAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Dineout Android Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

                        /* Send it off to the Activity-Chooser */
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    // Has Rating
    public static boolean hasNoRating(String rating) {
        return rating == null || rating.isEmpty() || rating.equals("0") || rating.equals("0.0") || rating.equalsIgnoreCase("null");
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

//    public static void redirectToAccessibilityWalkthrough(Activity activity, String deeplink){
//        if (AccessibilityPreference.isAccessibilityFeatureEnabled() &&
//                AccessibilityUtils.isAccessibilityWalkThroughDeepLink(deeplink)) {
//            AccessibilityUtils.handleAccessibilityWalkThrough(activity);
//
//        }
//    }

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
     * Get Content String
     *
     * @param context
     * @param content
     * @return
     */
    public static String getContentString(Context context, Object content) {

        if (content instanceof String) {
            return (String) content;
        } else {
            return context.getString((Integer) content);
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
     * Get Map Directions Intent
     *
     * @param restLat
     * @param restLong
     * @return
     */
    public static Intent getMapDirectionsIntent(String restLat, String restLong) {
        Intent intent = null;

        if (!(AppUtil.isStringEmpty(restLat) || AppUtil.isStringEmpty(restLong))) {
            //Prepare Location Bundle
            Bundle bundle = new Bundle();

            String destLat = DOPreferences.getCurrentLatitude(MainApplicationClass.getInstance());
            String destLong = DOPreferences.getCurrentLongitude(MainApplicationClass.getInstance());

            if (!AppUtil.isStringEmpty(destLat) && !AppUtil.isStringEmpty(destLong)) {
                bundle.putString(AppConstant.BUNDLE_SOURCE_LATITUDE, destLat);
                bundle.putString(AppConstant.BUNDLE_SOURCE_LONGITUDE, destLong);
            }

            bundle.putString(AppConstant.BUNDLE_DESTINATION_LATITUDE, restLat);
            bundle.putString(AppConstant.BUNDLE_DESTINATION_LONGITUDE, restLong);

            intent = IntentManager.navigateToMaps(bundle);
        }

        return intent;
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

    public static Bundle getFacebookAppEventData() {
        Bundle bundle = new Bundle();

        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE,
                AppConstant.FACEBOOK_EVENT_PARAM_CONTENT_TYPE_NAME);
        bundle.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION,
                AppConstant.FACEBOOK_EVENT_PARAM_DESCRIPTION);

        return bundle;
    }

    // Check if Recency value is lower than 15
    public static boolean showRecency(String recency) {
        // Either Recency does not exists or if its higher than 15 do not show New Tag
        //return !(isStringEmpty(recency) || Integer.parseInt(recency.trim()) == 0);
        return (!isStringEmpty(recency) && Integer.parseInt(recency.trim()) == 1);
    }

    // Get Card Expiry
    public static String getCardExpiry(String expiry) {

        StringBuilder sb = new StringBuilder();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date expiryDate = format.parse(expiry);
            Calendar cal = Calendar.getInstance();

            cal.setTime(expiryDate);

            sb.append(new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)].substring(0, 3).toUpperCase()
                    + " " + String.valueOf(cal.get(Calendar.YEAR)));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // Get Rounded Corner
    public static Bitmap getRoundedCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = MainApplicationClass.getInstance().getResources().getColor(R.color.grey_4D);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getCircle(Bitmap bitmap, int pixels) {
        if (bitmap != null) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                    .getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = MainApplicationClass.getInstance().getResources().getColor(R.color.grey_4D);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);

            canvas.drawCircle(rectF.centerX(), rectF.centerY(), roundPx, paint);
//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
        return null;
    }



    // Handle User Response by - saving in Pref file, update Tabs on Home Screen, Log Event,
    // Send User Attributes
    public static void handleUserLoginResponse(Context mContext, JSONObject responseObject) {
        if (responseObject != null && responseObject.optJSONObject("output_params") != null) {
            DOPreferences.saveDinerCredentials(mContext,
                    responseObject.optJSONObject("output_params").optJSONObject("data"));
        }

        // Update User Details
        //triggerUserDetailsUpdate(mContext);

        // Create User Details Map
        HashMap<String, Object> props = getUserDataJSONObject(mContext);

        // Tracks as Event
//        AnalyticsHelper.getAnalyticsHelper(mContext)
//                .trackEventGA(mContext.getString(R.string.push_label_signed_in), props);

        // Send User Attributes
        AnalyticsHelper.getAnalyticsHelper(mContext).sendUserAttributes();
    }

    // Get Advertising Info Id
    public static String getAdvertisingInfoId() {
        AdvertisingIdClient.Info adInfo = null;
        String uuid = null;

        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainApplicationClass.getInstance());
        } catch (Exception e) {

        }

        if (adInfo != null) {
            uuid = adInfo.getId();
        }

        return uuid;
    }

    /**
     * This function is used to capture Device Id
     */
    public static void captureDeviceId() {
        // Check if Device Id is saved
        if (TextUtils.isEmpty(DOPreferences.getDeviceId(MainApplicationClass.getInstance()))) {
            DOPreferences.setDeviceId(MainApplicationClass.getInstance(),
                    Settings.Secure.getString(MainApplicationClass.getInstance().getContentResolver(),
                            Settings.Secure.ANDROID_ID));
        }
    }


    /**
     * This function is used to capture Google Ad Id
     */
    public static void captureGoogleAdId() {

        GoogleAdIdAsyncTask asyncTask = new GoogleAdIdAsyncTask();
        asyncTask.execute();
    }

    public static long convertMillisecondsToSeconds(long timestamp) {
        return (timestamp / 1000L);
    }

    public static long convertSecondsToMilliseconds(long timestamp) {
        return (timestamp * 1000);
    }

    public static void setHoloLightTheme(MasterDOFragment masterDOFragment) {
        // Set Theme
        masterDOFragment.setStyle(MasterDOFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }


//    public static void sendAccessibilityGATracking(Context context) {
//        try {
//            int isAccEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(context, context.getPackageName() + "/.DineoutAccessibilityService") ? 1 : 0;
//            int isAccEnabledPrefValue = DOPreferences.isAccessibilityEnabled(context);
//
//            int isDrawOverAppEnabled = AccessibilityUtils.isDrawOverAppPermissionEnabled(context) ? 1 : 0;
//            int isDrawOverAppEnabledPrefValue = DOPreferences.isDrawOverAppEnabled(context);
//
//            if (isAccEnabled != isAccEnabledPrefValue) {
//                DOPreferences.setAccessibilityEnabled(context, isAccEnabled);
//                AccessibilityUtils.trackEventGA(context, context.getString(R.string.ga_booking_assistant), context.getString(R.string.ga_booking_assistant_acc_enabled), String.valueOf(isAccEnabled == 1));
//            }
//
//            if (isDrawOverAppEnabled != isDrawOverAppEnabledPrefValue) {
//                DOPreferences.setDrawOverAppEnabled(context, isDrawOverAppEnabled);
//                AccessibilityUtils.trackEventGA(context, context.getString(R.string.ga_booking_assistant), context.getString(R.string.ga_booking_assistant_draw_on_app), String.valueOf(isDrawOverAppEnabled == 1));
//            }
//
//            if (isAccEnabled != isAccEnabledPrefValue || isDrawOverAppEnabled != isDrawOverAppEnabledPrefValue) {
//                AccessibilityUtils.trackEventGA(context, context.getString(R.string.ga_booking_assistant), context.getString(R.string.ga_booking_assistant_feature_successfully_enabled), String.valueOf((isAccEnabled == 1) && (isDrawOverAppEnabled == 1)));
//            }
//        } catch (Exception e) {
//            // exception
//        }
//    }


    public static String convertDateToTimestamp(String dob){
        String dobTimeStamp;
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date parsedDate = dateFormat.parse(dob);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            long dateOfBirth = timestamp.getTime() / 1000;

            dobTimeStamp = String.valueOf(dateOfBirth);

        } catch (Exception e) {
            dobTimeStamp = "";
        }


        return dobTimeStamp;
    }


    public static HashMap<String, String> convertAttributes(final HashMap<String, Object> attributes) {
        final HashMap<String, String> result = new HashMap<String, String>();
        for (final HashMap.Entry<String, Object> entry : attributes.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }

}
