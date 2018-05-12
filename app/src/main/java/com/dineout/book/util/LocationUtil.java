package com.dineout.book.util;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dineout.book.interfaces.OnLocationPermissionGrantedListener;
import com.dineout.book.activity.MasterDOLauncherActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationUtil implements OnLocationPermissionGrantedListener,
        com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks {

    public final static int REQUEST_CHECK_SETTINGS = 2000;

    public static Location location;
    LocationManager locationManager;
    LocationRequest locationRequest = null;
    private GoogleApiClient googleApiClient;
    private LocationUtilityHelper locationUtilityHelper;
    private Activity activityContext;

    public LocationUtil(Activity activityContext, LocationUtilityHelper locationUtilityHelper) {

        this.locationUtilityHelper = locationUtilityHelper;
        this.activityContext = activityContext;
        locationManager = (LocationManager) activityContext.getSystemService(activityContext.LOCATION_SERVICE);
        
        registerForLocationPermission(activityContext);
        createLocationRequest();
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public void getLocationFromGPS(Activity activityContext) {
        if (isLocationPermissionGrantedToUser(activityContext)) {
            if (locationRequest == null) {
                createLocationRequest();
            }
            checkGPSEnabled(locationRequest);
        } else {
            PermissionUtils.grantLocationPermission(activityContext);
        }
    }

    public void requestLocationUpdates() {
        try {
            // Check for NULL and GoogleApiClient is connected
            if(googleApiClient!=null && googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this);
            }else if(googleApiClient!=null && !googleApiClient.isConnected()){
                googleApiClient.connect();
            }
        } catch (SecurityException e) {
            locationUtilityHelper.onLocationFailed();

        }
    }

    boolean isLocationPermissionGrantedToUser(Activity activityContext) {
        return PermissionUtils.hasLocationPermission(activityContext);
    }

    void stopLocationUpdates() {
        try {
            if(googleApiClient!=null && googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }
        } catch (SecurityException e) {

        }

        locationRequest = null;
    }

    private void registerForLocationPermission(Activity activityContext) {
        if (activityContext instanceof MasterDOLauncherActivity) {
            ((MasterDOLauncherActivity) activityContext).registerListenerForLocationPermission(this);
        }
    }

    private void unregisterForLocationPermission(Activity activityContext) {
        if (activityContext instanceof MasterDOLauncherActivity) {
            ((MasterDOLauncherActivity) activityContext).unregisterListenerForLocationPermission();
        }
    }

    @Override
    public void onLocationPermissionGranted(boolean granted) {
        if (granted) {
            getLocationFromGPS(activityContext);

        } else {
            locationUtilityHelper.onLocationFailed();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationUtilityHelper.onLocationFetched(location);

        stopLocationUpdates();
    }

    public LocationRequest createLocationRequest() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        this.locationRequest = locationRequest;

        return locationRequest;
    }

    public void checkGPSEnabled(final LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true); // this is the key ingredient
        if(googleApiClient==null)
            return;
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        locationUtilityHelper.onLocationSettingsSuccess();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        locationUtilityHelper.onLocationSettingsResolutionRequired(status);


                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    case LocationSettingsStatusCodes.ERROR:
                    case LocationSettingsStatusCodes.TIMEOUT:

                        locationUtilityHelper.onLocationFailed();

                        break;
                }
            }
        });
    }




    /**
     * Location Interacter
     */
    public interface LocationUtilityHelper {
        /**
         * This function is called after Location Fix is fetched
         */
        void onLocationFetched(Location location);

        /**
         * This function is called before handling Settings Resolution
         */
        void onLocationFailed();

        void onLocationSettingsResolutionRequired(Status status);

        void onLocationSettingsSuccess();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {

            if(googleApiClient!=null && googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } catch (SecurityException e) {
            locationUtilityHelper.onLocationFailed();

        }

    }


    @Override
    public void onConnectionSuspended(int i) {

    }
}
