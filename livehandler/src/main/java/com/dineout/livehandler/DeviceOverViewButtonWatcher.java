package com.dineout.livehandler;

/**
 * Created by sawai on 13/09/16.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class DeviceOverViewButtonWatcher {

    static final String TAG = "HomeWatcher";
    private Context mContext;
    private IntentFilter mFilter;
    private OverViewButtonReceiver mReceiver;

    public DeviceOverViewButtonWatcher(Context context) {
        mContext = context;
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mReceiver = new OverViewButtonReceiver();
    }


    public void startWatch() {
        if (mReceiver != null) {
            mContext.registerReceiver(mReceiver, mFilter);
        }
    }

    public void stopWatch() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    class OverViewButtonReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    switch (reason) {
                        case SYSTEM_DIALOG_REASON_HOME_KEY:
                            AccessibilityController.getInstance().hideWindow();
                            break;

                        case SYSTEM_DIALOG_REASON_RECENT_APPS:
                            AccessibilityController.getInstance().hideWindow();
                            break;
                    }
                }
            }
        }
    }
}