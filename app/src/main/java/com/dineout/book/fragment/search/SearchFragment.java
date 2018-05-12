package com.dineout.book.fragment.search;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.example.dineoutnetworkmodule.SearchUtils;
import com.dineout.recycleradapters.AutoSuggestRecyclerAdapter;
import com.dineout.recycleradapters.PreSuggestedSearchRecyclerAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.JSON_KEY_IS_HEADER;
import static com.example.dineoutnetworkmodule.AppConstant.JSON_KEY_SECTION_NAME;
import static com.example.dineoutnetworkmodule.AppConstant.JSON_KEY_TITLE;
import static com.example.dineoutnetworkmodule.AppConstant.PARAM_SF;

public class SearchFragment extends MasterDOJSONReqFragment implements View.OnClickListener,
                                                                       TextView.OnEditorActionListener, PreSuggestedSearchRecyclerAdapter.PreSuggestClickListener,
                                                                       AutoSuggestRecyclerAdapter.AutoSuggestClickListener {

    private final int REQUEST_CODE_SEARCH = 0x01;

    private ImageView mCancelSearch;
    private EditText mSuggestions;
    private TextView mSuggestedSearchText;
    private RelativeLayout mSuggestedSearchLayout;
    private RecyclerView recyclerViewSearchSuggestion;
    //private TextView mUserSearchText;
    private boolean isCreated;

    private PreSuggestedSearchRecyclerAdapter preSuggestedSearchRecyclerAdapter;
    private AutoSuggestRecyclerAdapter autoSuggestRecyclerAdapter;
    private ArrayList<String> searches = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set Flag
        isCreated = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
         trackScreenName(getString(R.string.ga_screen_search));

        // Set Title of Screen
        setToolbarTitle(getActivity().getResources().getString(R.string.search_activity_title));
        getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popToHome(getActivity());
            }
        });

        initializeView();

        // Call API only when Fragment is Created first time
        // Subsequent hits are handled via mSuggestions.TextWatcher
        if (isCreated) {
            isCreated = false;
            initializeData();
        }
    }

    public void initializeView() {
        /*mUserSearchText = (TextView) getView().findViewById(R.id.tv_user_search_text);
        mUserSearchText.setOnClickListener(this);*/

        mSuggestions = (EditText) getView().findViewById(R.id.tv_rest_suggestions);
        mSuggestions.setOnEditorActionListener(this);
        mSuggestions.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence searchString, int start, int count, int after) {
                // Do Nothing...
            }

            @Override
            public void onTextChanged(CharSequence searchString, int start, int before, int count) {
                // Do Nothing...
            }

            @Override
            public void afterTextChanged(Editable searchString) {
                // Cancel Request
                getNetworkManager().cancel();

                //track event


                if(getView()!=null) {
                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    if (hMap != null && !TextUtils.isEmpty(searchString.toString())) {
                        hMap.put("searchtypedvalue", searchString.toString());

                    }
                    trackEventForCountlyAndGA(getString(R.string.countly_search), getString(R.string.d_search_type), searchString
                            .toString(), hMap);
                    //track event for qgraph and apsalar
                    HashMap<String, Object> props = new HashMap<>();
                    props.put("label", searchString.toString());
                    trackEventQGraphApsalar(getString(R.string.d_search_type),props,true,false,false);
                }



                // Take API Hit
                getAutoSuggestResults(searchString.toString());
            }
        });

        mCancelSearch = (ImageView) getView().findViewById(R.id.iv_search_cancel);
        mCancelSearch.setOnClickListener(this);

        // Define Layout Manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerViewSearchSuggestion = (RecyclerView) getView().findViewById(R.id.recyclerView_search_suggestions);
        recyclerViewSearchSuggestion.setLayoutManager(linearLayoutManager);
        mSuggestedSearchLayout = (RelativeLayout) getView().findViewById(R.id.rl_suggested_search);
        mSuggestedSearchText = (TextView) getView().findViewById(R.id.tv_suggested_search);

        preSuggestedSearchRecyclerAdapter = new PreSuggestedSearchRecyclerAdapter(getContext());
        autoSuggestRecyclerAdapter = new AutoSuggestRecyclerAdapter(getContext());


        recyclerViewSearchSuggestion
                .addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                        if(newState == RecyclerView.SCROLL_STATE_IDLE){

                            trackEventForCountlyAndGA(getString(R.string.countly_search),
                                    getString(R.string.d_search_scroll), Integer.toString(lastVisibleItem), hMap);
                        }
                    }

                });

    }


    public void initializeData() {
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        getAutoSuggestResults("");
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {

            if (getActivity() == null || getView() == null)
                return false;

            // Hide Keyboard
            hideKeyboard();

            // Get Search Keyword
            String suggestion = mSuggestions.getText().toString();

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_search),"SearchEnter",suggestion,hMap);


            //save last three user search
            SearchUtils.saveSearchedText(getContext(),suggestion);

            RestaurantListFragment fragment = getRestaurantListInstance(suggestion);
            addToBackStack(getActivity(), fragment);

            return true;
        }

        return false;
    }

    private void hideKeyboard() {
        if (getActivity() != null)
            AppUtil.hideKeyboard(getActivity());
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        switch (viewId) {
            case R.id.iv_search_cancel:
                handleCancelSearchClick();
                break;



            default:
                break;
        }
    }

    private void handleCancelSearchClick() {
        //TRACK
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_search),getString(R.string.d_search_close),getString(R.string.d_search_close),hMap);
        mSuggestions.setText("");
    }


    private RestaurantListFragment getRestaurantListInstance(String searchKeyword) {
        // Prepare Bundle
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SEARCH_KEYWORD, searchKeyword);

        // Instantiate Restaurant List instance
        RestaurantListFragment restaurantListFragment = new RestaurantListFragment();
        restaurantListFragment.setArguments(bundle);

        return restaurantListFragment;
    }

    void getAutoSuggestResults(final String searchText) {

        if (getActivity() == null || getView() == null) {
            return;
        }

        //Set Cancel Button visibility
        mCancelSearch.setVisibility((AppUtil.isStringEmpty(searchText)) ?
                ImageView.GONE : ImageView.VISIBLE);

        // Show Loader
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_SEARCH,
                AppConstant.URL_NODE_AUTO_SUGGEST,
                ApiParams.getSolrSearchAutoSuggestParams(
                        DOPreferences.getCityName(getActivity().getApplicationContext()),
                        searchText,
                        false
                ), this, null, true);
    }



    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_SEARCH && responseObject != null) {

            if (responseObject.optBoolean("status")) {
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                if (outputParamsJsonObject != null) {
                    JSONObject dataJsonObject = outputParamsJsonObject.optJSONObject("data");
                    JSONArray orderedKeys = outputParamsJsonObject.optJSONArray("orderedKeys");

                    if (dataJsonObject != null && orderedKeys!=null && orderedKeys.length() > 0) {
                        // Set Visibility
                        mSuggestedSearchLayout.setVisibility(View.GONE);

                        // Set Data
                        autoSuggestRecyclerAdapter.setJsonArray(getAutoSuggestJsonArray(dataJsonObject,orderedKeys));

                        // Set Auto Suggest Click Listener
                        autoSuggestRecyclerAdapter.setAutoSuggestClickListener(this);

                        recyclerViewSearchSuggestion.setAdapter(autoSuggestRecyclerAdapter);
                        autoSuggestRecyclerAdapter.notifyDataSetChanged();

                    } else {
                        // Empty Recycler
                        recyclerViewSearchSuggestion.removeAllViews();

                        // Set Empty Data
                        autoSuggestRecyclerAdapter.setJsonArray(null);
                        preSuggestedSearchRecyclerAdapter.setJsonArray(null);

                        // Hide User Search Text
                        //mUserSearchText.setVisibility(View.GONE);

                        // Hide Pre-Suggested Search Title
                        mSuggestedSearchLayout.setVisibility(View.GONE);

                        // Check for Suggested Text
                        /*if (!TextUtils.isEmpty(mSuggestions.getText().toString())) { // Check for suggested text
                            afterSearchAPIHit();

                        } else {*/
                        // Check for NULL / Empty
                        JSONArray suggestedSearchJsonArray = outputParamsJsonObject.optJSONArray("suggested_search");
                        if (suggestedSearchJsonArray != null && suggestedSearchJsonArray.length() > 0) {
                            // Set Visibility
                            mSuggestedSearchLayout.setVisibility(View.VISIBLE);

                            // Set Suggestion Title
                            mSuggestedSearchText.setText(
                                    String.format(getResources().getString(R.string.suggested_search),
                                            DOPreferences.getCityName(getActivity().getApplicationContext()).toUpperCase()));

                            // Set Data
                            preSuggestedSearchRecyclerAdapter.setJsonArray(suggestedSearchJsonArray);

                            // Set Pre Suggest Click Listener
                            preSuggestedSearchRecyclerAdapter.setPreSuggestClickListener(this);

                            recyclerViewSearchSuggestion.setAdapter(preSuggestedSearchRecyclerAdapter);
                            preSuggestedSearchRecyclerAdapter.notifyDataSetChanged();
                        }
                        //}
                    }
                }
            }
        }
    }

    private JSONArray getAutoSuggestJsonArray(JSONObject dataJsonObject,JSONArray orderedKeysArray) {
        JSONArray autoSuggestJsonArray = new JSONArray();
        int noOfKeys=orderedKeysArray.length();
        for(int i=0;i< noOfKeys;i++){
            String currentKey=orderedKeysArray.optString(i);
            JSONObject keyHeaderJsonObject = prepareKeyHeaderJsonObject(currentKey);

            if (keyHeaderJsonObject != null) {
                // Add Key Header Object in JSON Array
                autoSuggestJsonArray.put(keyHeaderJsonObject);

                // Add Key Item JSON Object to Array
                addKeyItemJsonObjectToArray(currentKey, dataJsonObject.optJSONArray(currentKey), autoSuggestJsonArray);
            }
        }




        return autoSuggestJsonArray;
    }

    private JSONObject prepareKeyHeaderJsonObject(String key) {
        JSONObject keyHeaderJsonObject = null;

        if (!AppUtil.isStringEmpty(key)) {
            keyHeaderJsonObject = new JSONObject();

            try {
                // Add key as Title
                keyHeaderJsonObject.put(JSON_KEY_TITLE, key);

                // Set isHeader flag
                keyHeaderJsonObject.put(JSON_KEY_IS_HEADER, true);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return keyHeaderJsonObject;
    }

    private void addKeyItemJsonObjectToArray(String key, JSONArray keyItemJsonArray, JSONArray autoSuggestJsonArray) {
        // Check for NULL
        if (!AppUtil.isStringEmpty(key) && keyItemJsonArray != null && keyItemJsonArray.length() > 0) {
            // Check for NULL
            if (autoSuggestJsonArray == null) {
                autoSuggestJsonArray = new JSONArray();
            }

            int keyItemSize = keyItemJsonArray.length();

            for (int index = 0; index < keyItemSize; index++) {
                // Get Key Item JSON Object
                JSONObject keyItemJsonObject = keyItemJsonArray.optJSONObject(index);

                if (keyItemJsonObject != null) {
                    try {
                        // Set Section Name
                        keyItemJsonObject.put(JSON_KEY_SECTION_NAME, key);

                        // Set isHeader flag
                        keyItemJsonObject.put(JSON_KEY_IS_HEADER, false);

                        // Add Key Item JSON Object to Array
                        autoSuggestJsonArray.put(keyItemJsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onPreSuggestClick(JSONObject listItemJsonObject) {
        // Hide Keyboard
        hideKeyboard();

        if (listItemJsonObject != null) {
            // Track Event
            trackEventPreSuggest(listItemJsonObject);

            // Navigate to Restaurant List
            navigateToRestaurantListFromPreSuggest(listItemJsonObject);
        }
    }

    private void trackEventPreSuggest(JSONObject listItemJsonObject) {
//        trackEventGA(getString(R.string.ga_screen_search), "SuggestedSearch",
//                listItemJsonObject.optString("st"));
//
        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("poc",listItemJsonObject.optString("position"));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_search),"SuggestedSearchClick", listItemJsonObject.optString("st"),hMap);

        //Get CleverTap Instance
        HashMap<String, Object> props = new HashMap<String, Object>();

        if (!TextUtils.isEmpty(DOPreferences.getCityName(getActivity().getApplicationContext()))) {
            props.put("City", DOPreferences.getCityName(getActivity().getApplicationContext()));
        }

        //trackEventGA(getString(R.string.ga_action_searched), props);
    }

    private void navigateToRestaurantListFromPreSuggest(JSONObject listItemJsonObject) {
        // Instantiate Restaurant List instance


        // Get sf Section
        Bundle bundle = new Bundle();
        JSONArray arr = listItemJsonObject.optJSONArray("sf");
        String deeplink = listItemJsonObject.optString("deeplink");

        if (arr != null) {
            String sfString = arr.toString();
            bundle.putString(PARAM_SF, sfString);
            RestaurantListFragment fragment = new RestaurantListFragment();
            fragment.setArguments(bundle);
            addToBackStack(getFragmentManager(), fragment);
        } else if (!TextUtils.isEmpty(deeplink)) {
            MasterDOFragment fragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
            addToBackStack(getFragmentManager(), fragment);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mCancelSearch = null;
        mSuggestions = null;
        mSuggestedSearchText = null;
        mSuggestedSearchLayout = null;
        recyclerViewSearchSuggestion = null;
        //mUserSearchText = null;
        preSuggestedSearchRecyclerAdapter = null;
        autoSuggestRecyclerAdapter = null;
    }

    @Override
    public void onAutoSuggestClick(JSONObject autoSuggestItemJsonObject) {
        // Hide Keyboard
        hideKeyboard();

        // Check for NULL
        if (autoSuggestItemJsonObject != null) {

            //save last searches
            SearchUtils.saveSearchedText(getContext(),autoSuggestItemJsonObject.optString("suggestion"));

            // Track Event
            trackEventAutoSuggest(autoSuggestItemJsonObject);

            // Navigate User to Screen
            navigateUserToScreen(autoSuggestItemJsonObject);
        }
    }

    private void trackEventAutoSuggest(JSONObject autoSuggestItemJsonObject) {

        //track countly and ga
        int position = autoSuggestItemJsonObject.optInt("position");
        HashMap<String,String> hMap=DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("poc",Integer.toString(position));
        }
        trackEventForCountlyAndGA(getString(R.string.countly_search),
                getString(R.string.d_search_results),autoSuggestItemJsonObject.optString("suggestion")+ "_"+autoSuggestItemJsonObject.optString("sectionName"),hMap);

        //Get CleverTap Instance
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(autoSuggestItemJsonObject.optString(JSON_KEY_SECTION_NAME, ""),
                autoSuggestItemJsonObject.optString("suggestion", ""));

        if (!TextUtils.isEmpty(DOPreferences.getCityName(getActivity().getApplicationContext()))) {
            props.put("City", DOPreferences.getCityName(getActivity().getApplicationContext()));
        }

        //trackEventGA(getString(R.string.ga_action_searched), props);

        //track event for qgraph,apsalar,branch
        HashMap<String, Object> qprops = new HashMap<>();
        props.put("label",autoSuggestItemJsonObject.optString("suggestion", ""));
        trackEventQGraphApsalar(getString(R.string.d_search_results)
                ,qprops,true,false,false);

    }

    private void navigateUserToScreen(JSONObject autoSuggestItemJsonObject) {
        // Check for NULL
        if (autoSuggestItemJsonObject != null) {
            Bundle bundle = new Bundle();

            // Get Deeplink
            String deeplink = autoSuggestItemJsonObject.optString("deeplink", "");

            if (AppUtil.isStringEmpty(deeplink)) {
                // Get Landing Fragment
                MasterDOFragment masterDOFragment = getResultantFragment(autoSuggestItemJsonObject, bundle);

                // Get sf Section
                JSONArray sfJsonArray = autoSuggestItemJsonObject.optJSONArray("sf");
                if (sfJsonArray != null && sfJsonArray.length() > 0) {
                    bundle.putString(PARAM_SF, sfJsonArray.toString());
                }

                // Add Bundle to Fragment
                if (masterDOFragment != null) {
                    masterDOFragment.setArguments(bundle);

                    addToBackStack(getFragmentManager(), masterDOFragment);
                }
            } else {
                // Handle Deeplink
                MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), deeplink);
                addToBackStack(getActivity(), frag);
            }
        }
    }

    private MasterDOFragment getResultantFragment(JSONObject autoSuggestItemJsonObject, Bundle bundle) {
        MasterDOFragment masterDOFragment = null;

        // Get Restaurant Id
        String restaurantId = autoSuggestItemJsonObject.optString("r_id", "");

        if (AppUtil.isStringEmpty(restaurantId)) { // Restaurant List Page
            // Get sf Section
            JSONArray sfJsonArray = autoSuggestItemJsonObject.optJSONArray("sf");
            if (sfJsonArray != null && sfJsonArray.length() > 0) {
                // Fragment
                masterDOFragment = new RestaurantListFragment();
            }
        } else { // Restaurant Detail Page
            // Add Restaurant Id in Bundle
            bundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, restaurantId);

            // Fragment
            masterDOFragment = new RestaurantDetailFragment();
        }

        return masterDOFragment;
    }

    @Override
    public boolean onPopBackStack() {
        popToHome(getActivity());
        return true;
    }
}
