package com.dineout.livehandler;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by sawai on 22/08/16.
 */
public class LiveHandler {
    Context mContext;

    public LiveHandler(Context context) {
        mContext = context;
    }

    public boolean handleEvent(AccessibilityEvent event) {
        boolean returnType;

//        AccessibilityUtils.printLog(event);

        // if accessibility is enabled then process the event
        String packageName = "";
        if (event != null && !TextUtils.isEmpty(event.getPackageName())) {
            packageName = event.getPackageName().toString();
        }

        try {
            IHandler handler = AccessibilityMap.getHandler(packageName, mContext, AccessibilityController.mBus);
            returnType = handler.handleEvent(event);
        } catch (ClassNotFoundException e) {
            // exception
            returnType = false;
        }

        return returnType;
    }
}
