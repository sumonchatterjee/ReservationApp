package com.dineout.book.fragment.home;


import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.controller.LocationApiManager;
import com.dineout.book.util.LocationUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.activity.DineoutMainActivity;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.recycleradapters.SearchLocationRecyclerAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.dineoutnetworkmodule.AppConstant.JSON_KEY_IS_HEADER;
import static com.example.dineoutnetworkmodule.AppConstant.JSON_KEY_TITLE;

public class LocationSelectionFragment extends MasterDOJSONReqFragment
        implements View.OnClickListener, TextWatcher,
        LocationUtil.LocationUtilityHelper, LocationApiManager.OnLocationApiResponseListener,
        SearchLocationRecyclerAdapter.SearchLocationClickListener {

    private static final int REQUEST_CODE_SEARCH_LOCATION = 102;

    //private OnManualLocationSelectedListener onManualLocationSelectedListener;

    private LocationUtil locationUtil;
    private EditText editTextSearch;
    private SearchLocationRecyclerAdapter searchLocationRecyclerAdapter;
    private JSONArray recentLocationJsonArray;
    private JSONArray recentLocationWithHeaderJsonArray;
    private LocationApiManager locationApiManager;
    private Timer locationTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setOnManualLocationSelectedListener(((DineoutMainActivity) getActivity()));

        // Set Style
        AppUtil.setHoloLightTheme(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_selection, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // Track Screen
        trackScreenName(getString(R.string.countly_select_location));


        initializeView();
    }

    private void initializeView() {

        // Track My Location Instance
        TextView textViewFetchLocation = (TextView) getView().findViewById(R.id.text_view_fetch_location);
        textViewFetchLocation.setOnClickListener(this);

        // Search Edit Text
        editTextSearch = (EditText) getView().findViewById(R.id.et_search_location);
        editTextSearch.addTextChangedListener(this);

        // Get Adapter instance
        searchLocationRecyclerAdapter = new SearchLocationRecyclerAdapter(getContext());
        searchLocationRecyclerAdapter.setSearchLocationClickListener(this);

        // Get Layout Manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        // Search List Instance
        RecyclerView recyclerViewSearchList = (RecyclerView) getView().findViewById(R.id.location_search_list);
        recyclerViewSearchList.setAdapter(searchLocationRecyclerAdapter);
        recyclerViewSearchList.setLayoutManager(linearLayoutManager);

        // Prepare History View
        prepareHistoryView();
    }

    // Prepare History Container
    private void prepareHistoryView() {
        // Hide Loader
        hideLoader();

        // Get Recent Location JSON Array
        try {
            String savedRecent = DOPreferences.getRecentLocation(getActivity());

            if (!AppUtil.isStringEmpty(savedRecent)) {
                recentLocationJsonArray = new JSONArray(savedRecent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (recentLocationJsonArray != null) {
            // Prepare Recent Search with Header List
            prepareRecentSearchWithHeaderList();

            // Refresh Adapter
            refreshSearchList(recentLocationWithHeaderJsonArray);
        }
    }

    private void prepareRecentSearchWithHeaderList() {
        // Get Header Title
        JSONObject recentSearchHeaderJsonObject =
                getLocationTitleJSONObject(getString(R.string.text_search_history));

        recentLocationWithHeaderJsonArray = new JSONArray();
        recentLocationWithHeaderJsonArray.put(recentSearchHeaderJsonObject);

        int recentSearchSize = recentLocationJsonArray.length();
        for (int index = 0; index < recentSearchSize; index++) {
            // Get Recent Search
            JSONObject recentSearchJsonObject = recentLocationJsonArray.optJSONObject(index);

            if (recentSearchJsonObject != null) {
                // Add Recent Search to Array
                recentLocationWithHeaderJsonArray.put(recentSearchJsonObject);
            }
        }
    }

    // Handle Track My Location
    private void handleTrackMyLocation() {
        // Show Progress Wheel
        showLoader();

        locationTimer = new Timer();
        locationTimer.schedule(new TimeoutLocation(), 9000);
        locationUtil = new LocationUtil(getActivity(), this);
        locationUtil.setGoogleApiClient(((DineoutMainActivity) getActivity()).getGoogleApiClientForLocation());

        getLocationFromGPS();
    }

    public void getLocationFromGPS() {
        locationUtil.getLocationFromGPS(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Save Searched Location List
        if (recentLocationJsonArray != null) {
            DOPreferences.putRecentLocation(getActivity().getApplicationContext(),
                    recentLocationJsonArray.toString());
        }
    }

    // Fetch Search String Location
    private void fetchSearchStringLocation() {

        if (getActivity() == null || getView() == null)
            return;

        // Refresh Search List
        refreshSearchList(null);

        // Show Progress Wheel
        showLoader();

        // Cancel All Request
        getNetworkManager().cancel();

        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_CODE_SEARCH_LOCATION, AppConstant.URL_LOCATION_SEARCH,
                ApiParams.getLocationParams(editTextSearch.getText().toString().trim(),
                        DOPreferences.getCurrentLatitude(getActivity().getApplicationContext()),
                        DOPreferences.getCurrentLongitude(getActivity().getApplicationContext())), this, null, false);
    }

    private void refreshSearchList(JSONArray searchJsonArray) {
        // Empty List
        searchLocationRecyclerAdapter.setJsonArray(searchJsonArray);
        searchLocationRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> resp) {
        // Hide Loader
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_SEARCH_LOCATION) {

            if (responseObject != null && responseObject.optBoolean("status")) {
                // Get Output Params
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                if (outputParamsJsonObject != null) {
                    JSONArray searchJsonArray = prepareSearchResponse(outputParamsJsonObject);

                    if (searchJsonArray != null) {
                        // Set Array in Adapter
                        searchLocationRecyclerAdapter.setJsonArray(searchJsonArray);
                        searchLocationRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }

            // Check if Location Result is NULL
            if (searchLocationRecyclerAdapter.getJsonArray() == null) {
                // Show Location History
                showLocationSearchHistory();
            }
        }
    }

    private JSONArray prepareSearchResponse(JSONObject outputParamsJsonObject) {
        JSONArray searchJsonArray = null;

        if (outputParamsJsonObject != null) {
            // Get Keys
            JSONArray keysJsonArray = outputParamsJsonObject.optJSONArray("location_keys");

            if (keysJsonArray != null && keysJsonArray.length() > 0) {
                // Get Location
                JSONObject locationJsonObject = outputParamsJsonObject.optJSONObject("locations");

                if (locationJsonObject != null) {
                    searchJsonArray = new JSONArray();
                    int keySize = keysJsonArray.length();

                    for (int index = 0; index < keySize; index++) {
                        // Get Location Key
                        String locationKey = keysJsonArray.optString(index, "");

                        if (!AppUtil.isStringEmpty(locationKey)) {
                            // Get Location Array
                            JSONArray locationJsonArray = locationJsonObject.optJSONArray(locationKey);

                            if (locationJsonArray != null && locationJsonArray.length() > 0) {
                                // Get Location Title JSON Object
                                JSONObject titleJsonObject = getLocationTitleJSONObject(locationKey);

                                // Add Title JSON Object in JSON Array
                                searchJsonArray.put(titleJsonObject);

                                // Add Location Result JSON Array
                                getLocationResultJSONArray(searchJsonArray, locationJsonArray);
                            }
                        }
                    }
                }
            }
        }

        return searchJsonArray;
    }

    private JSONObject getLocationTitleJSONObject(String locationName) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(JSON_KEY_TITLE, locationName);
            jsonObject.put(JSON_KEY_IS_HEADER, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private void getLocationResultJSONArray(JSONArray searchJsonArray, JSONArray locationJsonArray) {
        int locationArray = locationJsonArray.length();
        for (int index = 0; index < locationArray; index++) {
            // Get Location Result Object
            JSONObject locationResultJsonObject = locationJsonArray.optJSONObject(index);

            if (locationResultJsonObject != null) {
                searchJsonArray.put(locationResultJsonObject);
            }
        }
    }

    private void showLocationSearchHistory() {
        // Check for NULL
        if (recentLocationWithHeaderJsonArray != null) {
            refreshSearchList(recentLocationWithHeaderJsonArray);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        if (request.getIdentifier() == REQUEST_CODE_SEARCH_LOCATION) {
            // Hide Progress Wheel
            hideLoaderAndShowError();
        }
    }

    @Override
    public boolean onPopBackStack() {
        if (!TextUtils.isEmpty(DOPreferences.getELongitude(getActivity().getApplicationContext()))) {
            return false;
        } else {
            return true;
        }
    }

//    public void setOnManualLocationSelectedListener(OnManualLocationSelectedListener onManualLocationSelectedListener) {
//        this.onManualLocationSelectedListener = onManualLocationSelectedListener;
//    }

    @Override
    public void onLocationFetched(Location location) {
        if (getActivity() == null || getView() == null)
            return;

        if (location != null) {
            DOPreferences.saveLatAndLong(getActivity().getApplicationContext(), location);
            DOPreferences.setAutoMode(getActivity().getApplicationContext(), true);

            locationApiManager = LocationApiManager.getInstance(getActivity(), getNetworkManager());
            locationApiManager.setOnLocationApiResponseListener(this);
            locationApiManager.getLocationDetailsFromApi();
            locationTimer.cancel();

        } else {
            hideLoaderAndShowError();
        }
    }

    @Override
    public void onLocationFailed() {
        hideLoaderAndShowError();
    }

    public void hideLoaderAndShowError() {
        // Hide Loader
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        UiUtil.showSnackbar(editTextSearch,
                getString(R.string.text_unable_fetch_location), 0);

        if (locationTimer != null) {
            locationTimer.cancel();
        }
    }

    @Override
    public void onLocationSettingsResolutionRequired(Status status) {
        if (getActivity() == null || getView() == null)
            return;

        try {
            status.startResolutionForResult(getActivity(), LocationUtil.REQUEST_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {

        }
    }

    @Override
    public void onLocationSettingsSuccess() {
        if(locationUtil!=null) {
            locationUtil.requestLocationUpdates();
        }
    }

    @Override
    public void onLocationApiSuccess(JSONObject locationJsonObject) {
        // Hide Loader
        hideLoader();

        if (getView() == null || getView() == null) {
            return;
        }

        if (locationJsonObject == null) {
            UiUtil.showSnackbar(editTextSearch,
                    getString(R.string.text_unable_fetch_location), 0);
            return;
        }

        DOPreferences.saveLocationDetails(getActivity().getApplicationContext(), locationJsonObject, true);
        locationApiManager.setOnLocationApiResponseListener(null);
        popBackStack(getFragmentManager());
    }

    @Override
    public void onLocationApiErrorMessageShow(String error_msg) {
        hideLoaderAndShowError();
    }

    @Override
    public void onLocationApiFailure() {
        hideLoaderAndShowError();
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.text_view_fetch_location) {
            handleAutoDetectLocationClick();
        }
    }

    private void handleAutoDetectLocationClick() {
        // Track Event

        HashMap<String,String> hMap=DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_select_location),
                getString(R.string.d_auto_detect_location_type),"AutoDetectLocation",hMap);

        //track event for qgraph and apsalar
        HashMap<String, Object> props = new HashMap<>();
        props.put("label", "AutoDetectLocation");
        trackEventQGraphApsalar(getString(R.string.d_auto_detect_location_type),props,true,false,false);

        // Handle Track My Location
        handleTrackMyLocation();
    }

    /* Text Watcher Callbacks - STARTS */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do Nothing...
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do Nothing...
    }

    @Override
    public void afterTextChanged(Editable searchString) {
        // Cancel Network Request
        getNetworkManager().cancel();

         //track event

        if(getContext()!=null) {
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_select_location), getString(R.string.d_location_search_type), searchString
                    .toString(), hMap);

            //track event for qgraph and apsalar
            HashMap<String, Object> props = new HashMap<>();
            props.put("label", searchString.toString());
            trackEventQGraphApsalar(getString(R.string.d_location_search_type), props, true, false, false);
        }

        if (searchString.length() >= 2) {
            // Fetch Location on Type
            fetchSearchStringLocation();

        } else if (searchString.length() < 2) {
            // Check if search text is removed
            if (searchString.toString().trim().length() == 0) {
                // Prepare History View
                prepareHistoryView();
            }
        }
    }
    /* Text Watcher Callbacks - ENDS */

    @Override
    public void onSearchLocationItemClick(JSONObject locationJsonObject) {
        // Check for NULL
        if (locationJsonObject != null) {
            // Show Progress Wheel
            showLoader();

            //track event for qgraph and apsalar
            HashMap<String, Object> props = new HashMap<>();
            props.put("label",locationJsonObject.optString("city_name"));
            trackEventQGraphApsalar( getString(R.string.d_location_search_result),props,true,false,false);


            // track screen
            HashMap<String,String> hMap=DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("poc" , locationJsonObject.optString("position"));
            }
            trackEventForCountlyAndGA(getString(R.string.countly_select_location),
                    getString(R.string.d_location_search_result),locationJsonObject.optString("city_name"),hMap);



            // Save Location Details
            DOPreferences.saveLocationDetails(getActivity().getApplicationContext(), locationJsonObject, false);

            // Save Searched Location in List
            putSearchLocation(locationJsonObject);

            // Hide Loader
            hideLoader();

            // Remove Current Fragment
            popBackStack(getFragmentManager());

            //onManualLocationSelectedListener.onManualLocationSelected(true);
        }
    }

    public void putSearchLocation(JSONObject locationJsonObject) {
        // Check for NULL or Empty
        if (recentLocationJsonArray == null) {
            recentLocationJsonArray = new JSONArray();
        }

        int recentLocationSize = recentLocationJsonArray.length();

        if (recentLocationSize == 0) {
            recentLocationJsonArray.put(locationJsonObject);

        } else {
            int matchedIndex = getMatchedLocationFromRecentLocationList(locationJsonObject);

            if (matchedIndex == -1) {
                if (recentLocationSize < 5) {
                    //recentLocationJsonArray.put(locationJsonObject);
                    // Update Location in Recent List
                    updateSelectedLocationInRecentLocation(-1, locationJsonObject);

                } else {
                    // Update Location in Recent List
                    updateSelectedLocationInRecentLocation((recentLocationSize - 1), locationJsonObject);
                }
            } else {
                // Update Location in Recent List
                updateSelectedLocationInRecentLocation(matchedIndex, locationJsonObject);
            }
        }
    }

    private int getMatchedLocationFromRecentLocationList(JSONObject locationJsonObject) {
        int matchedIndex = -1;

        // Iterate Recent Locations
        int recentLocationSize = recentLocationJsonArray.length();
        for (int index = 0; index < recentLocationSize; index++) {
            // Get Location
            JSONObject recentLocationJsonObject = recentLocationJsonArray.optJSONObject(index);

            if (recentLocationJsonObject != null) {
                // Recent Location UID
                String recentLocationUID = recentLocationJsonObject.optString("uid", "");

                // Selected Location UID
                String selectedLocationUID = locationJsonObject.optString("uid", "");

                if (!AppUtil.isStringEmpty(recentLocationUID) &&
                        !AppUtil.isStringEmpty(selectedLocationUID)) {
                    // If Same
                    if (recentLocationUID.equalsIgnoreCase(selectedLocationUID)) {
                        matchedIndex = index;
                        break;
                    }
                }
            }
        }

        return matchedIndex;
    }

    private void updateSelectedLocationInRecentLocation(int skipIndex, JSONObject locationJsonObject) {
        JSONArray newRecentLocationJsonArray = new JSONArray();

        // Add Selected Location in Array
        newRecentLocationJsonArray.put(locationJsonObject);

        int recentSearchSize = recentLocationJsonArray.length();
        for (int index = 0; index < recentSearchSize; index++) {
            // Get Recent Location
            JSONObject recentLocationJsonObject = recentLocationJsonArray.optJSONObject(index);

            if (index != skipIndex && recentLocationJsonObject != null) {
                newRecentLocationJsonArray.put(recentLocationJsonObject);
            }
        }

        // Set New Recent List
        recentLocationJsonArray = newRecentLocationJsonArray;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationUtil = null;
        editTextSearch = null;
        searchLocationRecyclerAdapter = null;
        recentLocationJsonArray = null;
        recentLocationWithHeaderJsonArray = null;
        locationApiManager = null;
        locationTimer = null;
    }



    public class TimeoutLocation extends TimerTask {
        @Override
        public void run() {
            if (getActivity() == null)
                return;
            if (locationApiManager != null) {
                locationApiManager.setOnLocationApiResponseListener(null);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideLoaderAndShowError();
                }
            });
        }
    }
}
