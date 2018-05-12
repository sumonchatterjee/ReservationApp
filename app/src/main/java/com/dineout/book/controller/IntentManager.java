package com.dineout.book.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.dineout.book.util.AppUtil;
import com.example.dineoutnetworkmodule.AppConstant;

public class IntentManager {

    public static Intent navigateToMaps(Bundle bundle) {

        //Get Source Lat Long
        String srcLat = bundle.getString(AppConstant.BUNDLE_SOURCE_LATITUDE);
        srcLat = AppUtil.setStringEmpty(srcLat);

        String srcLong = bundle.getString(AppConstant.BUNDLE_SOURCE_LONGITUDE);
        srcLong = AppUtil.setStringEmpty(srcLong);

        String srcLatLong = "";

        if (!(AppUtil.isStringEmpty(srcLat) || AppUtil.isStringEmpty(srcLong))) {
            srcLatLong = (srcLat + "," + srcLong);
        }

        //Get Destination Lat Long
        String destLat = bundle.getString(AppConstant.BUNDLE_DESTINATION_LATITUDE);
        String destLong = bundle.getString(AppConstant.BUNDLE_DESTINATION_LONGITUDE);

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + srcLatLong +
                        "&daddr=" + destLat + "," + destLong));

        return intent;
    }
}
