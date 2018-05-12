package com.dineout.book.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.AnalyticsReceiver;

import io.branch.referral.InstallListener;

public class GenericInstallReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        // Initiate branch io Receiver
        InstallListener listener = new InstallListener();
        listener.onReceive(context, intent);


        // google analytics Referrer Receiver
        AnalyticsReceiver receiver = new AnalyticsReceiver();
        receiver.onReceive(context, intent);
    }
}
