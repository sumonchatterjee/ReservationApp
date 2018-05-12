package com.dineout.book.service;

import android.content.Intent;

import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.android.gms.iid.InstanceIDListenerService;

public class GCMInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Reset Flag
        DOPreferences.setSentTokenToServer(getApplicationContext(), false);

        // Save Registration Token
        DOPreferences.setGcmRegistrationToken(getApplicationContext(), "");

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }
}

