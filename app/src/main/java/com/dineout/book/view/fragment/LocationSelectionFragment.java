package com.dineout.book.view.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.model.webservice.LocationSearch.LocationResponse;
import com.dineout.book.model.webservice.LocationSearch.SearchedLocation;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.view.activity.DineoutMainActivity;
import com.dineout.book.view.adapter.LocationSearchAdapter;
import com.dineout.book.view.widgets.ProgressWheel;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationSelectionFragment extends MasterDOFragment
        implements Response.Listener<JSONObject>, Response.ErrorListener {

    private static final int REQUEST_CODE_SEARCH_LOCATION = 102;

    private FrameLayout frameLayoutContent;
    private EditText mSearchView;
    private ListView mSearchResult;
    private ProgressWheel progressWheel;
    private LocationSearchAdapter mSearchAdapter;
    private View mHistoryContainer;
    private ListView mRecentList;
    private LocationSearchAdapter mRecentAdapter;
    private ArrayList<SearchedLocation> selectedLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreen(getString(R.string.ga_screen_select_location));

        // Set Style
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_selection, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Cancellable
        setCancelable(true);

        // Initialize View
        initializeView();
    }

    // Initialize View
    private void initializeView() {
        frameLayoutContent = (FrameLayout) getView().findViewById(R.id.frame_layout_content);

        progressWheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        progressWheel.setBarColor(getResources().getColor(R.color.colorPrimary));
        progressWheel.setProgress(0.0f);
        progressWheel.setVisibility(View.GONE);

        mHistoryContainer = getView().findViewById(R.id.recent_container);

        // Track My Location Instance
        TextView textViewFetchLocation = (TextView) getView().findViewById(R.id.text_view_fetch_location);
        textViewFetchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Track Event
                trackEvent(getString(R.string.ga_screen_select_location),
                        getString(R.string.ga_action_auto_detect), null);

                // Handle Track My Location
                handleTrackMyLocation();
            }
        });

        // Search Edit Text
        mSearchView = (EditText) getView().findViewById(R.id.et_search_location);
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable searchString) {
                // Cancel Network Request
                getNetworkManager().cancel();

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
        });

        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Hide Keyboard
                    AppUtil.hideKeyboard(mSearchView, getActivity());

                    return true;
                }

                return false;
            }
        });

        // Search History List Instance
        mRecentList = (ListView) getView().findViewById(R.id.recent_location_list);
        mRecentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get Selected Location
                SearchedLocation item = (SearchedLocation) mRecentAdapter.getItem(position);

                // Process Selected Location
                processSelectedLocation(item);
            }
        });

        // Search List Instance
        mSearchResult = (ListView) getView().findViewById(R.id.location_search_list);
        mSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get Selected Location
                SearchedLocation item = (SearchedLocation) mSearchAdapter.getItem(position);

                // Process Selected Location
                processSelectedLocation(item);
            }
        });

        // Get Saved Search Locations from Persistent Storage
        selectedLocation = new Gson().fromJson(DOPreferences.getRecentLocation(getActivity()),
                new TypeToken<List<SearchedLocation>>() {
                }.getType());

        selectedLocation = ((AppUtil.isCollectionEmpty(selectedLocation)) ?
                new ArrayList<SearchedLocation>(5) : selectedLocation);

        // Prepare History View
        prepareHistoryView();
    }

    // Prepare History Container
    private void prepareHistoryView() {
        // Hide Progress Wheel
        progressWheel.setVisibility(View.GONE);

        // Hide Search List
        mSearchResult.setVisibility(View.GONE);

        // Check if we have Location History
        if (selectedLocation.size() > 0) {
            // Show History Container
            mHistoryContainer.setVisibility(View.VISIBLE);

            // Instantiate Adapter
            mRecentAdapter = new LocationSearchAdapter(getActivity(), selectedLocation);

            // Set Adapter
            mRecentList.setAdapter(mRecentAdapter);

        } else {
            // Hide History Container
            mHistoryContainer.setVisibility(View.GONE);
        }
    }

    // Handle Track My Location
    private void handleTrackMyLocation() {
        // Show Progress Wheel
        progressWheel.setVisibility(View.VISIBLE);
        progressWheel.spin();

        ((DineoutMainActivity) getActivity()).connectGoogleAPIClient();
    }



    @Override
    public void onStop() {
        super.onStop();

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());

        // Save Searched Location List
        DOPreferences.putSearchedLocation(getActivity().getApplicationContext(),
                new Gson().toJson(selectedLocation));
    }

    // Fetch Search String Location
    private void fetchSearchStringLocation() {
        if (mSearchAdapter == null)
            mSearchResult.setVisibility(View.GONE);

        // Hide History Container
        mHistoryContainer.setVisibility(View.GONE);

        // Show Progress Wheel
        progressWheel.setVisibility(View.VISIBLE);
        progressWheel.spin();

        // Cancel All Request
        getNetworkManager().cancel();

        getNetworkManager().jsonRequestGet(REQUEST_CODE_SEARCH_LOCATION, AppConstant.URL_LOCATION_SEARCH,
                ApiParams.getLocationParams(mSearchView.getText().toString().trim(),
                        DOPreferences.getCurrentLatitude(getActivity()),
                        DOPreferences.getCurrentLongitude(getActivity())), this, null, false);
    }

    // Process Selected Location
    private void processSelectedLocation(SearchedLocation searchedLocation) {
        // Check for NULL
        if (searchedLocation != null) {
            // Persist location selected
            DOPreferences.setAutoMode(getActivity(), false);
            DOPreferences.setELatitude(getActivity(), String.valueOf(searchedLocation.getLatitude()));
            DOPreferences.setELongitude(getActivity(), String.valueOf(searchedLocation.getLongitude()));
            DOPreferences.setCityId(getActivity(), searchedLocation.getCityId());
            DOPreferences.setCityName(getActivity(), searchedLocation.getCityName());
            DOPreferences.setAreaName(getActivity(), searchedLocation.getAreaName());
            DOPreferences.setLocalityName(getActivity(), searchedLocation.getLocationName());
            DOPreferences.setSuggestion(getActivity(), searchedLocation.getSuggestion());

            // Check for Search Needle
            if (searchedLocation.getSearchNeedle() != null &&
                    !searchedLocation.getSearchNeedle().isEmpty()) {

                DOPreferences.setSfArrLocarea(getActivity(),
                        searchedLocation.getSearchNeedle().get(AppConstant.SEARCH_FILTER_ARR_LOCAREA));
                DOPreferences.setSfArrArea(getActivity(),
                        searchedLocation.getSearchNeedle().get(AppConstant.SEARCH_FILTER_ARR_AREA));
                DOPreferences.setSfByCity(getActivity(),
                        searchedLocation.getSearchNeedle().get(AppConstant.SEARCH_FILTER_BY_CITY));
            }

            // Save Searched Location in List
            putSearchLocation(searchedLocation);

            MasterDOFragment fragment = (MasterDOFragment) getFragmentManager()
                    .findFragmentById
                            (R.id.fragment_base_container);

            if (fragment instanceof HomePageMasterFragment) {
                ((HomePageMasterFragment) fragment).refreshPagerAdapters();
            }

            // Remove Current Fragment
           dismissAllowingStateLoss();
        }
    }

    public void putSearchLocation(SearchedLocation item) {

        if (item == null)
            return;

        if (selectedLocation == null) {
            selectedLocation = new ArrayList<>();
        }

        if (!selectedLocation.contains(item)) {
            if (selectedLocation.size() == 5) {
                selectedLocation.remove(4);
            }

            selectedLocation.add(0, item);

        } else {
            selectedLocation.remove(item);
            selectedLocation.add(0, item);
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> resp) {

        if (request.getIdentifier() == REQUEST_CODE_SEARCH_LOCATION) {

            if (responseObject != null && responseObject.optBoolean("status")) {
                // Get Location Output Params
                LocationResponse.LocationOutputParams locationOutputParams =
                        new Gson().fromJson(responseObject.optJSONObject("output_params").toString(),
                                LocationResponse.LocationOutputParams.class); //TODO - Use JSONObject

                if (locationOutputParams != null) {
                    // Get Locations
                    Map<String, ArrayList<SearchedLocation>> locations = locationOutputParams.getLocations();

                    if (locations != null && !locations.isEmpty()) {
                        // Show Search Result
                        mSearchResult.setVisibility(View.VISIBLE);

                        if (mSearchAdapter == null) {
                            mSearchAdapter = new LocationSearchAdapter(getActivity(), locations);

                            mSearchResult.setAdapter(mSearchAdapter);

                        } else {
                            mSearchAdapter.updateData(locations);
                        }
                    } else {
                        prepareHistoryView();
                    }
                } else {
                    prepareHistoryView();
                }
            } else {
                prepareHistoryView();
            }

            // Hide Progress Wheel
            progressWheel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        if (request.getIdentifier() == REQUEST_CODE_SEARCH_LOCATION) {
            // Hide Progress Wheel
            progressWheel.setVisibility(View.GONE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    public void onPostLocationFetched(boolean isLocationFetched) {
        // Hide Progress Wheel
        progressWheel.setVisibility(View.GONE);

        if (isLocationFetched) {
           dismissAllowingStateLoss();

        } else {
            UiUtil.showSnackbar(frameLayoutContent,
                    getString(R.string.text_unable_fetch_location), 0);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Intent intent = new Intent("location-changed");
        // add data
        intent.putExtra("message", "changed");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
//        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_base_container);
//
//        if (fragment != null && fragment instanceof HomePageMasterFragment) {
//            ((HomePageMasterFragment) fragment).refreshPagerAdapters();
//        }


    }
}
