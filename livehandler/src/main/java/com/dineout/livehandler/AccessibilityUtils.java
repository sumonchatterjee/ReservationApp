package com.dineout.livehandler;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Created by prateek.aggarwal on 6/27/16.
 */
public class AccessibilityUtils {
    public static final int DRAW_OVER_APP_REQUEST_CODE = 1;

    public static void printLog(AccessibilityEvent event) {
        if (event == null) return;

        String className = "NA";
        AccessibilityNodeInfo nodeInfo;
        String nodeClass = "NA";
        String classContent = "NA";
        String classText = "NA";
        String nodeContent = "NA";
        String nodeId = "NA";
        String nodeText = "NA";
        String nodeFocus = "NA";

        if (event.getClassName() != null) className = event.getClassName().toString();

        if (event.getContentDescription() != null)
            classContent = event.getContentDescription().toString();

        if (event.getText() != null) classText = event.getText().toString();

        if ((nodeInfo = event.getSource()) != null) {
            if (nodeInfo.getClassName() != null) nodeClass = nodeInfo.getClassName().toString();

            if (nodeInfo.getContentDescription() != null)
                nodeContent = nodeInfo.getContentDescription().toString();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (nodeInfo.getViewIdResourceName() != null)
                    nodeId = nodeInfo.getViewIdResourceName();
            }

            if (nodeInfo.getText() != null) nodeText = nodeInfo.getText().toString();

            nodeFocus = String.valueOf(nodeInfo.isFocused());
        }
        System.out.println("event_info: " + className + "  " + classContent + "  " + classText + "  " +
                nodeClass + "  " + nodeId + "  " + nodeContent + "  " + nodeText + " "
                /* + getEventType(event)*/ + " nodeFocus: " + nodeFocus);
    }


    public static final String EVENT_ID = "event_id";
    public static final String SHOW_WINDOW_EVENT = "show_window_event";
    public static final String HIDE_WINDOW_EVENT = "hide_window_event";
    public static final String DIALOG_EVENT = "dialog_event";
    public static final String RESTAURANT_VALIDATOR_EVENT = "restaurant_validator_event";
    public static final String RESTAURANT_ID = "restaurant_id";
    public static final String RESTAURANT_NAME = "restaurant_name";
    public static final String RESTAURANT_ADDRESS = "restaurant_address";


    public static void receiveEvent(JsonObject jsonObject) {
        if (jsonObject != null) {
            final String id = jsonObject.get(EVENT_ID).getAsString();

            switch (id) {
                case SHOW_WINDOW_EVENT:
                    AccessibilityController.getInstance().showWindow();
                    break;

                case HIDE_WINDOW_EVENT:
                    AccessibilityController.getInstance().hideWindow();
                    break;

                case DIALOG_EVENT:
                    AccessibilityController.getInstance().setDialogInfo(jsonObject);
                    break;

                case RESTAURANT_VALIDATOR_EVENT:
                    AccessibilityController.getInstance().restaurantValidator(jsonObject);
                    break;
            }
        }
    }

    public static final String accessibilityPermission = "acc_pm";

    public static boolean isAccessibilityWalkThroughDeepLink(String deepLink) {
        boolean returnValue = false;
        try {
            Uri uri = Uri.parse(deepLink);
            String queryParameter = uri.getHost();
            returnValue = accessibilityPermission.equals(queryParameter);
        } catch (Exception e) {
            // exception
        }

        return returnValue;
    }

    public static void handleAccessibilityWalkThrough(Activity activity) {
        boolean isAnyActionPerformed = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isDrawOverAppPermissionEnabled(activity)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivityForResult(intent, DRAW_OVER_APP_REQUEST_CODE);

                    isAnyActionPerformed = true;

                    // ga tracking
                    try {
                        String category = activity.getString(R.string.ga_booking_assistant);
                        String action = activity.getString(R.string.ga_booking_assistant_taken_to_draw_over_app);
                        trackEvent(activity, category, action, null);
                    } catch (Exception e) {
                        // exception
                    }

                } else if (!isAccessibilityServiceEnabled(activity, activity.getPackageName() + "/.DineoutAccessibilityService")) {
                    openAccessibilityServiceScreen(activity);

                    isAnyActionPerformed = true;

                    // ga tracking
                    try {
                        String category = activity.getString(R.string.ga_booking_assistant);
                        String action = activity.getString(R.string.ga_booking_assistant_taken_to_acc_screen);
                        trackEvent(activity, category, action, null);
                    } catch (Exception e) {
                        // exception
                    }
                }
            } else {
                if (!isAccessibilityServiceEnabled(activity, activity.getPackageName() + "/.DineoutAccessibilityService")) {
                    openAccessibilityServiceScreen(activity);

                    isAnyActionPerformed = true;

                    // ga tracking
                    try {
                        String category = activity.getString(R.string.ga_booking_assistant);
                        String action = activity.getString(R.string.ga_booking_assistant_taken_to_acc_screen);
                        trackEvent(activity, category, action, null);
                    } catch (Exception e) {
                        // exception
                    }
                }
            }
        } catch (Exception e) {
            // exception
            isAnyActionPerformed = false;
        }

        String toastText = "Feature already enabled!";
        if (!isAnyActionPerformed) {
            Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show();

            // ga tracking
            try {
                String category = activity.getString(R.string.ga_booking_assistant);
                String action = activity.getString(R.string.ga_booking_assistant_feature_already_enabled_toast);
                trackEvent(activity, category, action, null);
            } catch (Exception e) {
                // exception
            }
        }
    }

    public static void handleDrawOverAppIntentResult(Activity activity) {
        if (activity != null && isDrawOverAppPermissionEnabled(activity)) {
            if (!isAccessibilityServiceEnabled(activity, activity.getPackageName() + "/.DineoutAccessibilityService")) {
                openAccessibilityServiceScreen(activity);

                // ga tracking
                try {
                    String category = activity.getString(R.string.ga_booking_assistant);
                    String action = activity.getString(R.string.ga_booking_assistant_taken_to_acc_screen);
                    trackEvent(activity, category, action, null);
                } catch (Exception e) {
                    // exception
                }
            }
        }
    }
    public static void handleAccessibilityWalkThrough1(Context mContext) {
        boolean isAnyActionPerformed = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isAccessibilityServiceEnabled(mContext, mContext.getPackageName() + "/.DineoutAccessibilityService")) {
                    openAccessibilityServiceScreen(mContext);

                    isAnyActionPerformed = true;

                    // ga tracking
                    try {
                        String category = mContext.getString(R.string.ga_booking_assistant);
                        String action = mContext.getString(R.string.ga_booking_assistant_taken_to_acc_screen);
                        trackEvent(mContext, category, action, null);
                    } catch (Exception e) {
                        // exception
                    }
                } else {
                    if (!Settings.canDrawOverlays(mContext)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);

                        isAnyActionPerformed = true;

                        // ga tracking
                        try {
                            String category = mContext.getString(R.string.ga_booking_assistant);
                            String action = mContext.getString(R.string.ga_booking_assistant_taken_to_draw_over_app);
                            trackEvent(mContext, category, action, null);
                        } catch (Exception e) {
                            // exception
                        }
                    }
                }
            } else {
                if (!isAccessibilityServiceEnabled(mContext, mContext.getPackageName() + "/.DineoutAccessibilityService")) {
                    openAccessibilityServiceScreen(mContext);

                    isAnyActionPerformed = true;

                    // ga tracking
                    try {
                        String category = mContext.getString(R.string.ga_booking_assistant);
                        String action = mContext.getString(R.string.ga_booking_assistant_taken_to_acc_screen);
                        trackEvent(mContext, category, action, null);
                    } catch (Exception e) {
                        // exception
                    }
                }
            }
        } catch (Exception e) {
            // exception

            isAnyActionPerformed = false;
        }

        String toastText = "Feature already enabled!";
        if (!isAnyActionPerformed) {
            Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();

            // ga tracking
            try {
                String category = mContext.getString(R.string.ga_booking_assistant);
                String action = mContext.getString(R.string.ga_booking_assistant_feature_already_enabled_toast);
                trackEvent(mContext, category, action, null);
            } catch (Exception e) {
                // exception
            }
        }
    }

    public static boolean isDrawOverAppPermissionEnabled(Context context) {
        boolean returnValue = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            returnValue = Settings.canDrawOverlays(context);
        }

        return returnValue;
    }
    public static boolean isAccessibilityServiceEnabled(Context context, String accessibilityServiceName) {
        boolean returnValue = false;
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            if (accessibilityServiceName.equals(service.getId())) {
                returnValue = true;
                break;
            }
        }
        return returnValue;
    }

    private static void openAccessibilityServiceScreen(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // exception
        }
    }

    public static void trackEvent(Context context, String category, String  action, String label) {
        try {
            AnalyticsHelper.getAnalyticsHelper(context).trackEventGA(category, action, label);
        } catch (Exception e) {
            // exception
        }
    }

    public static void trackEvent(Context context, String category, String action, String label, long value) {
        try {
            AnalyticsHelper.getAnalyticsHelper(context).trackEventGA(category, action, label, value);
        } catch (Exception e) {
            // exception
        }
    }
}
