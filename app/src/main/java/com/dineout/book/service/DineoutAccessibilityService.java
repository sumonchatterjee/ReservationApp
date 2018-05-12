//package com.dineout.book.service;
//
//import android.accessibilityservice.AccessibilityService;
//import android.accessibilityservice.AccessibilityServiceInfo;
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.Settings;
//import android.view.accessibility.AccessibilityEvent;
//
//import com.dineout.book.R;
//import com.dineout.livehandler.AccessibilityController;
//import com.dineout.livehandler.AccessibilityPreference;
//import com.dineout.livehandler.AccessibilityUtils;
//import com.dineout.livehandler.DeviceOverViewButtonWatcher;
//import com.dineout.livehandler.LiveHandler;
//
//
//public class DineoutAccessibilityService extends AccessibilityService {
//    private LiveHandler mLiveHandler;
//    private DeviceOverViewButtonWatcher mOverViewButtonWatcher;
//
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
//        try {
//            if (AccessibilityPreference.isAccessibilityFeatureEnabled()
//                    && AccessibilityPreference.shouldShowDialog()) {
//                mLiveHandler.handleEvent(accessibilityEvent);
//            }
//        } catch (Exception e) {
//            // exception
//        }
//    }
//
//    @Override
//    public void onInterrupt() {
//
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        try {
//            AccessibilityController.newInstance(this);
//            AccessibilityController.getInstance().registerBus();
//
//            loadPreference();
//
//            mLiveHandler = new LiveHandler(this);
//            mOverViewButtonWatcher = new DeviceOverViewButtonWatcher(this);
//            mOverViewButtonWatcher.startWatch();
//        } catch (Exception e) {
//            // exception
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        AccessibilityController.getInstance().unRegisterBus();
//        if (mOverViewButtonWatcher != null) mOverViewButtonWatcher.stopWatch();
//    }
//
//    @Override
//    protected void onServiceConnected() {
//        super.onServiceConnected();
//        try {
//            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//
//            String[] packages = {"com.application.zomato"};
//
//
//            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
////                            | AccessibilityEvent.TYPE_WINDOWS_CHANGED;
//
//            info.packageNames = packages;
//
//            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//
//            info.notificationTimeout = 0;
//
//            info.flags |= AccessibilityServiceInfo.DEFAULT;
//            info.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
////        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
//            }
////        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
////        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
//
//            this.setServiceInfo(info);
//
////        checkForOverLayPermission(this.getBaseContext());
//        } catch (Exception e) {
//            // exception
//        }
//    }
//
//    private void loadPreference() {
//        boolean isAccessibilityFeatureEnableFlag = AccessibilityPreference.isAccessibilityFeatureEnabledRaw(this);
//        AccessibilityPreference.storeAccessibilityFeatureEnableFalg(this, isAccessibilityFeatureEnableFlag);
//
//        boolean shouldShowDialog = AccessibilityPreference.shouldShowDialogRaw(this);
//        AccessibilityPreference.storeShowDialogFlag(this, shouldShowDialog);
//    }
//
//    public void checkForOverLayPermission(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(context)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//
//                // ga tracking
//                try {
//                    String category = getString(R.string.ga_booking_assistant);
//                    String action = getString(R.string.ga_booking_assistant_taken_to_draw_over_app);
//                    AccessibilityUtils.trackEventGA(this, category, action, null);
//                } catch (Exception e) {
//                    // exception
//                }
//
//            }
//        }
//    }
//}
