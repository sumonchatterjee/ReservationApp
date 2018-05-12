package com.dineout.book.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.controller.DeeplinkParserManager;
import com.freshdesk.hotline.Hotline;
import com.google.android.gms.gcm.GcmListenerService;
import com.quantumgraph.sdk.GcmNotificationIntentService;
import com.quantumgraph.sdk.QG;
public class GCMListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (data != null) {
            // Handle Message for Hot line

            if (Hotline.isHotlineNotification(data)) {
                Hotline instance = Hotline.getInstance(this);
                instance.handleGcmMessage(data);
                return;
            }


            else if (data.containsKey("message") && QG.isQGMessage(data.getString("message"))) {
                Intent intent =
                        new Intent(getApplicationContext(), GcmNotificationIntentService.class);
                intent.setAction(Constants.SOURCE_QG);
                intent.putExtras(data);
                getApplicationContext().startService(intent);
            }
                //return;
//            } else if(data.containsKey("mp_message")){
////
//                String message = data.getString("mp_message");
//                String iconName = data.getString("mp_icnm");
//                String uriString = data.getString(DeeplinkParserManager.payloadURI);
//                Object notificationTitle = data.getString("mp_title");
//
//                ApplicationInfo appInfo = getAppInfo();
//                if(null == notificationTitle && null != appInfo) {
//                    notificationTitle = getBaseContext().getPackageManager().getApplicationLabel(appInfo);
//                }
//                String uriKey  =data.getString(DeeplinkParserManager.payloadUKey);
//                if (TextUtils.isEmpty(uriKey)) uriKey = "";
//
//                showNotification(notificationTitle.toString(),message,uriString,uriKey);
////                com.mixpanel.android.mpmetrics.GCMReceiver receiver= new GCMReceiver();
////                Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");
////                intent.putExtras(data);
////                receiver.onReceive(getBaseContext(),intent);
//            } else{
            else {
                String title = data.getString(DeeplinkParserManager.payloadTitle);
                String message = data.getString(DeeplinkParserManager.payloadMessage);

                String uri = data.getString(DeeplinkParserManager.payloadURI);
                String ukey = data.getString(DeeplinkParserManager.payloadUKey);
                if (TextUtils.isEmpty(ukey)) ukey = "";

                if(!(TextUtils.isEmpty(title) && TextUtils.isEmpty(message))) {

                    showNotification(title, message, uri, ukey);
                }

//                if (!TextUtils.isEmpty(data.getString(DeeplinkParserManager.payloadUKey))) {
//                    String ukey = data.getString(DeeplinkParserManager.payloadUKey);
//                    String uri = data.getString(DeeplinkParserManager.payloadURI);
//
//                    showNotification(title, message, uri, ukey);
//
//                }
            }
        } else {
            AppUtil.i("Push Notification is disabled");
        }
    }

    private ApplicationInfo getAppInfo(){

        ApplicationInfo appInfo;
        try {
            appInfo = getBaseContext().getPackageManager().
                    getApplicationInfo(getBaseContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException var12) {
            appInfo = null;
        }

        return appInfo;
    }

    void showNotification(String title, String message, String uri, String ukey) {

        int smallIcon = R.drawable.ic_stat_notification;
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_panel);

        NotificationCompat.BigTextStyle notificationBig = new NotificationCompat.BigTextStyle();
        notificationBig.bigText(message);
        notificationBig.setBigContentTitle(title);

        Intent myIntent = getDefaultIntent(uri, ukey);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentText(message).setContentTitle(title)
                .setSmallIcon(smallIcon).setAutoCancel(true)
                .setTicker(title)
                .setLargeIcon(largeIcon).
                        setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent).setStyle(notificationBig);

        Notification notification = notificationBuilder.build();

        //Notify User
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private Intent getDefaultIntent(String uri, String ukey) {

        Intent resultIntent = new Intent(Intent.ACTION_VIEW);

        if (!TextUtils.isEmpty(uri)) {
            resultIntent.setData(Uri.parse(uri));
        }

        resultIntent.putExtra(DeeplinkParserManager.payloadUKey, ukey);

        return resultIntent;
    }
}

