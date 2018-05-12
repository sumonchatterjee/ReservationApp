package com.dineout.book.util;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.dineout.book.application.MainApplicationClass;
import com.example.dineoutnetworkmodule.DOPreferences;

public class GoogleAdIdAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        // Check if Device Id is saved
        if (TextUtils.isEmpty(DOPreferences.getGoogleAdId(MainApplicationClass.getInstance()))) {
            DOPreferences.setGoogleAdId(MainApplicationClass.getInstance(), AppUtil.getAdvertisingInfoId());
        }

        return null;
    }
}