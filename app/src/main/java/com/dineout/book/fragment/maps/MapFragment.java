package com.dineout.book.fragment.maps;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.dialogs.UberCabsDialog;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.LocationUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.activity.DineoutMainActivity;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MapFragment extends MasterDOJSONReqFragment
        implements View.OnClickListener, LocationUtil.LocationUtilityHelper , OnMapReadyCallback {

    protected static CameraPosition cameraPosition = null;

    protected GoogleMap mMapFullScreen;
    protected LatLng location = null;
    protected Marker markerFullScreenMap = null;
    LocationUtil locationUtil;
    private String restaurantName, restLatitude, restLongitude,restaurantId;
    private SupportMapFragment mapFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
       // trackScreenToGA(getString(R.string.ga_screen_maps));

        // Initialize View
        initializeView();

        mapFragment.getMapAsync(this);

    }

    private void initializeView() {
        // Set Title
        setToolbarTitle(R.string.title_map);

        // Get Data from Bundle
        Bundle bundle = getArguments();

        // Get Distance
        if (bundle != null && !AppUtil.isStringEmpty(bundle.getString(AppConstant.BUNDLE_DISTANCE))) {
            TextView textViewDistance = (TextView) getView().findViewById(R.id.textView_distance);
            textViewDistance.setText(String.format(getString(R.string.container_distance),
                    bundle.getString(AppConstant.BUNDLE_DISTANCE)));
            textViewDistance.setVisibility(View.VISIBLE);
        }

        // Get Direction Button
        TextView textViewGetDirections = (TextView) getView().findViewById(R.id.textView_get_direction);
        textViewGetDirections.setOnClickListener(this);

        // Set Restaurant Name
        if (bundle != null && !AppUtil.isStringEmpty(bundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME))) {
            restaurantName = bundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME);
            restaurantId=bundle.getString(AppConstant.BUNDLE_RESTAURANT_ID);
            TextView textViewRestaurantName = (TextView) getView().findViewById(R.id.textView_restaurant_name);
            textViewRestaurantName.setText(restaurantName);
            textViewRestaurantName.setVisibility(View.VISIBLE);
        }

        // Set Restaurant Address
        if (bundle != null && !AppUtil.isStringEmpty(bundle.getString(AppConstant.BUNDLE_RESTAURANT_ADDRESS))) {
            TextView textViewRestaurantAddress = (TextView) getView().findViewById(R.id.textView_restaurant_address);
            textViewRestaurantAddress.setText(bundle.getString(AppConstant.BUNDLE_RESTAURANT_ADDRESS));
            textViewRestaurantAddress.setVisibility(View.VISIBLE);
        }


        // Set Rating / Recency
        if (bundle != null) {
            // Check Recency
            if (AppUtil.showRecency(bundle.getString(AppConstant.BUNDLE_RECENCY))) {
                getView().findViewById(R.id.textView_recency_tag).setVisibility(View.VISIBLE);
            } else {
                // Check Rating
                if (!AppUtil.hasNoRating(bundle.getString(AppConstant.BUNDLE_RATING))) {
                    TextView textViewRating = (TextView) getView().findViewById(R.id.textView_rating);
                    AppUtil.setRatingValueBackground(bundle.getString(AppConstant.BUNDLE_RATING), textViewRating);
                    textViewRating.setVisibility(View.VISIBLE);
                }
            }
        }

        // Set Map
        if (mMapFullScreen == null) {
            FragmentManager fm = getChildFragmentManager();

            mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapFullScreen);

            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.mapFullScreen, mapFragment).commit();
            }
        } else {
            if (markerFullScreenMap != null) {
                markerFullScreenMap = mMapFullScreen.addMarker(
                        new MarkerOptions()
                                .position(location)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_selected)));
            }
        }

        if (bundle != null) {
            restLatitude = bundle.getString(AppConstant.BUNDLE_DESTINATION_LATITUDE);
            restLongitude = bundle.getString(AppConstant.BUNDLE_DESTINATION_LONGITUDE);
        }

        // Set Click Listener for Uber
        getView().findViewById(R.id.uber_button).setOnClickListener(this);
    }



    private void showFullScreenMap() {
        double lat = Double.parseDouble(restLatitude);
        double longitude = Double.parseDouble(restLongitude);

        location = new LatLng(lat, longitude);

        setUpFullScreenMap();

        cameraPosition = new CameraPosition.Builder().target(location).zoom(17.3f).bearing(0).tilt(0).build();
        mMapFullScreen.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), null);

        FragmentManager fm1 = getChildFragmentManager();

        mapFragment = (SupportMapFragment) fm1.findFragmentById(R.id.mapFullScreen);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm1.beginTransaction().replace(R.id.mapFullScreen, mapFragment).commit();
        }

    }

    protected void setUpFullScreenMap() {
        // Hide the zoom controls as the button panel will cover it.
        mMapFullScreen.getUiSettings().setZoomControlsEnabled(false);
        mMapFullScreen.setTrafficEnabled(true);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMapFullScreen.setMyLocationEnabled(false);
        mMapFullScreen.setIndoorEnabled(true);
        mMapFullScreen.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMapFullScreen.getUiSettings().setAllGesturesEnabled(true);
        mMapFullScreen.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 5));

        // Add lots of markers to the map.
        addMarkerToFullScreenMap();
    }

    protected void addMarkerToFullScreenMap() {
        markerFullScreenMap = mMapFullScreen.addMarker(new MarkerOptions().position(location).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_selected)));

        mMapFullScreen.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_selected));
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        switch (viewId) {

            case R.id.textView_get_direction:
                directionsButtonClicked();
                break;

            case R.id.uber_button:
                callUber();
                break;
        }
    }

    public void directionsButtonClicked() {
        // Track Event
//        trackEventGA(getString(R.string.ga_screen_detail),
//                getString(R.string.ga_action_get_directions), restaurantName);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantName+"_"+restaurantId,getString(R.string.d_restaurant_get_diretion_click),restaurantName+"_"+restaurantId,hMap);

        String restLat = "";
        if (!AppUtil.isStringEmpty(restLatitude)) {
            restLat = restLatitude;
        }

        String restLong = "";
        if (!AppUtil.isStringEmpty(restLongitude)) {
            restLong = restLongitude;
        }

        //Navigate to Maps App
        Intent intent = AppUtil.getMapDirectionsIntent(restLat, restLong);
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void callUber() {
        // Track Event
//        trackEventGA(getString(R.string.ga_screen_detail),
//                getString(R.string.ga_action_uber_ride), restaurantName);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantName+"_"+restaurantId,getString(R.string.d_restaurant_map_uber_click),restaurantName,hMap);



        //locationUtil = new LocationUtil(getActivity(), this);
        locationUtil = new LocationUtil(getActivity(), this);
        locationUtil.setGoogleApiClient(((DineoutMainActivity) getActivity()).getGoogleApiClientForLocation());

        getLocationFromGPS();
//            locationUtil.setGoogleApiClient(((DineoutMainActivity) getActivity()).getGoogleApiClientForLocation());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cameraPosition = null;
        mMapFullScreen = null;
        location = null;
        markerFullScreenMap = null;
        restaurantName = null;
        restLatitude = null;
        restLongitude = null;
        mapFragment = null;
    }

    private void showUberDialog(Location location) {
        UberCabsDialog dialog = UberCabsDialog.newInstance(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), restLatitude, restLongitude, restaurantName);
        showFragment(getActivity().getSupportFragmentManager(), dialog);

    }

    public void getLocationFromGPS() {
        locationUtil.getLocationFromGPS(getActivity());
    }

    @Override
    public void onLocationFetched(Location location) {
        if (location != null && getContext() != null) {
            showUberDialog(location);

        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLocationFailed() {
        showErrorMessage();
    }

    @Override
    public void onLocationSettingsResolutionRequired(Status status) {
        try {
            if (getActivity() != null) {
                status.startResolutionForResult(getActivity(), LocationUtil.REQUEST_CHECK_SETTINGS);
            } else {
                showErrorMessage();
            }
        } catch (IntentSender.SendIntentException e) {
            showErrorMessage();
        }
    }

    @Override
    public void onLocationSettingsSuccess() {
        locationUtil.requestLocationUpdates();

    }

    public void showErrorMessage() {

        if (getActivity() == null || getView() == null)
            return;

        // Show Message
        UiUtil.showToastMessage(getActivity().getApplicationContext(),
                getString(R.string.text_unable_fetch_location));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMapFullScreen=googleMap;
        showFullScreenMap();

    }


    @Override
    public void handleNavigation() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantName+"_"+restaurantId,"RestaurantMapBackClick","",hMap);

        super.handleNavigation();
    }
}
