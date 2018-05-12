package com.dineout.livehandler;

import android.content.Context;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.google.gson.JsonObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by sawai on 19/08/16.
 */
public class AccessibilityController {
    private Context mContext;
    private static AccessibilityController instance;
    private ViewController mViewController;
    private LayoutInflater mInflater;
    private WindowManager mWindowManager;
    private WindowContainer mWindowContainer;
    public static Bus mBus = new Bus(ThreadEnforcer.MAIN);

    private Handler mDialogShowHandler = new Handler();
    private Runnable mDialogShowRunnable;
    private static final long mDialogShowDelay = 3000;

    private static boolean mAccessibilityFlag = true;

    private AccessibilityController(Context context) {
        setUpThings(context);
    }

    public static void newInstance(Context context){
        instance = new AccessibilityController(context);
    }

    public static AccessibilityController getInstance() {
        return instance;
    }

    public void setUpThings(Context context) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppTheme);
        this.mContext = contextThemeWrapper;

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mInflater = (LayoutInflater) contextThemeWrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mViewController = new ViewController(mContext, mInflater);
        mWindowContainer = new WindowContainer(mContext, mWindowManager, mInflater);

        mDialogShowRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    showWindow();
                } catch (Exception e) {
                    // exception
                }
            }
        };
        hideWindow();
    }

    public boolean setDialogInfo(JsonObject jsonObject) {
        return setDialogData(jsonObject) && setDialogView() && showWindowWithDelay();
    }

    private boolean setDialogData(JsonObject jsonObject) {
        boolean returnValue = false;
        IView dialogView = mViewController.getDialogView();
        if (dialogView != null) {
            ((DialogView) dialogView).setData(jsonObject);
            returnValue = true;
        }
        return returnValue;
    }

    private boolean setDialogView() {
        boolean returnValue = false;
        if (mViewController != null) {
            IView dialogView = mViewController.getDialogView();
            if (mWindowContainer != null) {
                mWindowContainer.setView(dialogView);
                returnValue = true;
            }
        }
        return returnValue;
    }

    public void destroyDialogView() {
        if (mViewController != null) {
            mViewController.destroyDialogView();
        }
    }

    public boolean hideWindow() {
        mDialogShowHandler.removeCallbacksAndMessages(null);

        boolean returnValue = false;
        if (mWindowContainer != null) {
            mWindowContainer.hideWindow();
            returnValue = true;
        }
        return returnValue;
    }

    public boolean showWindow() {
        boolean returnValue = false;
        if (mWindowContainer != null) {
            mWindowContainer.showWindow();
            returnValue = true;
        }
        return returnValue;
    }

    public boolean showWindowWithDelay() {
        boolean returnValue;
        try {
            mDialogShowHandler.removeCallbacksAndMessages(null);
            mDialogShowHandler.postDelayed(mDialogShowRunnable, mDialogShowDelay);

            returnValue = true;
        } catch (Exception e) {
            // exception
            returnValue = false;
        }
        return returnValue;
    }

    public static boolean isAccessibilityFlagEnabled() {
        return mAccessibilityFlag;
    }

    public static void setAccessibilityFlag(boolean accessibilityFlag) {
        AccessibilityController.mAccessibilityFlag = accessibilityFlag;
    }

    public static String getLocalRestId() {
        return RestValidator.getLocalRestId(instance);
    }

    public void restaurantValidator(JsonObject jsonObject) {
        new RestValidator(mContext).hitSearchApi(jsonObject.get(AccessibilityUtils.RESTAURANT_NAME).getAsString(),
                jsonObject.get(AccessibilityUtils.RESTAURANT_ADDRESS).getAsString());
    }

    public void registerBus() {
        if (mBus != null) {
            mBus.register(this);
        }
    }

    public void unRegisterBus() {
        if (mBus != null) {
            mBus.unregister(this);
        }
    }

    @Subscribe
    public void receiveEvents(JsonObject jsonObject) {
        AccessibilityUtils.receiveEvent(jsonObject);
    }

}
