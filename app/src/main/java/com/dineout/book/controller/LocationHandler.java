package com.dineout.book.controller;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.dineout.book.interfaces.LocationHandlerCallbacks;
import com.dineout.book.util.LocationUtil;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

public class LocationHandler implements GoogleApiClient.OnConnectionFailedListener,
                                        GoogleApiClient.ConnectionCallbacks,
                                        LocationUtil.LocationUtilityHelper {

    public static final int REQUEST_CHECK_SETTINGS_UPLOAD_BILL = 2005;
    private static final int REQUEST_CODE_GET_LOCATION_DETAILS = 501;

    private FragmentActivity mContext;
    private LocationHandlerCallbacks mCallback;
    private GoogleApiClient googleApiClient;
    private LocationUtil locationUtil;
    private LocationApiManager locationApiManager;

    public LocationHandler(@NonNull FragmentActivity context, @NonNull LocationHandlerCallbacks callbacks) {

        if (context == null)
            throw new RuntimeException("Context could not be null");

        if (callbacks == null)
            throw new RuntimeException("LocationHandlerCallbacks could not be null");

        this.mContext = context;
        this.mCallback = callbacks;
        locationUtil = new LocationUtil(mContext, this);


    }

    private void initializeGoogleAPIClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void fetchLocationFromGPS() {
        if (mContext == null)
            return;

        if (googleApiClient == null) {
            fetchCurrentLocation();

        } else {
            locationUtil.setGoogleApiClient(googleApiClient);
            locationUtil.getLocationFromGPS(mContext);
        }
    }

    public void fetchCurrentLocation() {
        initializeGoogleAPIClient();

        if (googleApiClient == null)
            return;

        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();

        } else if (googleApiClient.isConnected()) {
            fetchLocationFromGPS();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        fetchLocationFromGPS();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        GoogleApiAvailability.getInstance().showErrorNotification(mContext, connectionResult);

    }

    @Override
    public void onLocationFetched(Location location) {

        if (location != null) {
            DOPreferences.saveLatAndLongForUploadBill(mContext, location);


            if (mCallback != null) {
                mCallback.locationUpdateSuccess();
                mCallback = null;
            }
        } else {
            if (mCallback != null)
                mCallback.locationUpdateFailed("Failed to fetch your location.Please try again.");
        }
    }

    @Override
    public void onLocationFailed() {
        if (mCallback != null)
            mCallback.locationUpdateFailed("Could not fetch current location");
    }



    @Override
    public void onLocationSettingsResolutionRequired(Status status) {
        try {
            status.startResolutionForResult(
                    mContext,
                    REQUEST_CHECK_SETTINGS_UPLOAD_BILL);
        } catch (IntentSender.SendIntentException e) {

        }
    }

    @Override
    public void onLocationSettingsSuccess() {
        if (locationUtil != null)
            locationUtil.requestLocationUpdates();
    }


}
