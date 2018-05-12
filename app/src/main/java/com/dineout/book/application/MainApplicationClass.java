package com.dineout.book.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v4.app.NotificationCompat;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.activity.DineoutMainActivity;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.facebook.FacebookSdk;
import com.freshdesk.hotline.Hotline;
import com.freshdesk.hotline.HotlineNotificationConfig;
import com.phonepe.android.sdk.api.PhonePe;

import io.branch.referral.Branch;

public class MainApplicationClass extends Application {

    private static MainApplicationClass mInstance;

    private DineoutNetworkManager networkManager;

   public static synchronized MainApplicationClass getInstance() {
        return mInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        // Instantiate IO Branch
        Branch.getAutoInstance(this);
        try {
            PhonePe.init(this, null);
        } catch (Throwable e){
            e.printStackTrace();
        }


        networkManager=DineoutNetworkManager.newInstance(this);



        mInstance = this;

        // Initialize Tracking Libraries
        AnalyticsHelper.getAnalyticsHelper(this);
        setHotlineNotificationConfig();
    }

    private void setHotlineNotificationConfig() {
        HotlineNotificationConfig notificationConfig = new HotlineNotificationConfig()
                .setNotificationSoundEnabled(true)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setLargeIcon(R.drawable.ic_notification_panel)
                .launchDeepLinkTargetOnNotificationClick(true)
                .launchActivityOnFinish(DineoutMainActivity.class.getName())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Hotline.getInstance(getApplicationContext()).setNotificationConfig(notificationConfig);
    }



    @Override
    public void onTerminate() {
        // Unregister Apsalar Receiver
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext())
                .onAppTerminated();

        // Cancel All Requests
        if(networkManager!=null) {
            networkManager.cancel();
        }

        super.onTerminate();
    }
}
