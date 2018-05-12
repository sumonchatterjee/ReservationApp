package com.dineout.book.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.dineout.book.R;


public class BillNotificationManager {


    public static void buildIntermediateProgressNotification(Context context,int id){

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Bill upload")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_notification);

        builder.setProgress(0, 0, true);
        notificationManager.notify(id, builder.build());
    }

    public static void updateProgressNotification(Context context,int id,long progress){

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Bill upload")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.ic_notification);

        builder.setProgress(100, (int)progress, false);
        builder.setAutoCancel(false);
        notificationManager.notify(id+1, builder.build());
    }

    public static void removeProgressNotification(Context context,int id){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id+1);

    }

    public static void displayNotification(Context context,String message,String title,int id){
        if(TextUtils.isEmpty(message))
            return;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);

        resultIntent.setData(Uri.parse(DeeplinkParserManager.SCHEMA + DeeplinkParserManager.hostYourBill));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(TextUtils.isEmpty(title) ?"Bill upload" : title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification).
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_notification_panel));
        builder.setAutoCancel(true);
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, builder.build());
    }
}
