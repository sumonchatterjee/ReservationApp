package com.dineout.book.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;

import com.dineout.book.service.ImageUploadService;


public class NetworkChangeReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager networkManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfoFromBroadcast = ConnectivityManagerCompat
                .getNetworkInfoFromBroadcast(networkManager, intent);
        if(networkInfoFromBroadcast==null)
            return;

        if(networkInfoFromBroadcast.isConnected()){

            Intent serviceIntent = new Intent(context, ImageUploadService.class);
            serviceIntent.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.startService(serviceIntent);

        }
    }
}
