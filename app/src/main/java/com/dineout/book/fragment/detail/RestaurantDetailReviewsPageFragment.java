package com.dineout.book.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.recycleradapters.RestaurantReviewPageAdapter;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.dineout.recycleradapters.view.widgets.OtherReviewTextView;
import com.dineout.recycleradapters.viewmodel.ReviewsIconsAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;

import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_NAME;
import static com.dineout.recycleradapters.util.RateNReviewUtil.updateInfo;


import java.util.HashMap;

public class RestaurantDetailReviewsPageFragment extends AuthenticationWrapperJSONReqFragment
        implements View.OnClickListener, RestaurantReviewPageAdapter.RestaurantReviewsClickListener,
        OtherReviewTextView.OtherReviewTextViewCallback, RateNReviewUtil.RateNReviewCallbacks {

    private boolean isFetchingData;
    private int nextIndex;
    private String restaurantId;
    private String restaurantName;
    private Bundle restaurantBundle;
    private RestaurantReviewPageAdapter reviewPageAdapter;
    private ReviewsIconsAdapter reviewIconsAdapter;

    private View buttonRestDetailWriteReview;
    private RecyclerView recyclerViewRestDetailReviews;
    private RecyclerView recyclerViewReviewIcons;
    private ViewGroup noReviewScreen;

    private String restaurantCategory;
    private String restaurantType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reviewPageAdapter = new RestaurantReviewPageAdapter(getActivity().getApplicationContext());
        reviewPageAdapter.setRestaurantReviewsClickListener(this);
        reviewPageAdapter.setOtherReviewTextViewCallback(this);

        reviewIconsAdapter = new ReviewsIconsAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail_reviews_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get Bundle
        restaurantBundle = getArguments();

        // Set Restaurant Id
        if (restaurantBundle != null) {
            restaurantId = restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID, "");
            restaurantName = restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME, "");

            restaurantCategory = restaurantBundle.getString("RESTAURANT_CATEGORY");
            restaurantType = restaurantBundle.getString("RESTAURANT_TYPE");
        }

        // Initialize View
        initializeView();

        // Take API Hit
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity()));
    }

    private void initializeView() {
        // Define Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        // Get Write Review
        buttonRestDetailWriteReview = getView().findViewById(R.id.layout_secondary_button_list_item);
        buttonRestDetailWriteReview.setOnClickListener(this);

        // Set Button Icon
        ImageView imageViewButtonIcon = (ImageView) getView().findViewById(R.id.imageView_button_icon);
        imageViewButtonIcon.setImageResource(R.drawable.write_review_star);

        // Set Button Text
        TextView textViewButtonText = (TextView) getView().findViewById(R.id.textView_button_text);
        textViewButtonText.setText(R.string.button_leave_review);

        // Get Recycler View instance
        recyclerViewRestDetailReviews = (RecyclerView) getView().findViewById(R.id.recyclerView_rest_detail_reviews);
        recyclerViewRestDetailReviews.setLayoutManager(linearLayoutManager);
        if (reviewPageAdapter != null) {
            recyclerViewRestDetailReviews.setAdapter(reviewPageAdapter);
        }

        recyclerViewRestDetailReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerViewRestDetailReviews.getLayoutManager();

                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isFetchingData && nextIndex > 0 && (visibleItemCount + pastVisibleItems) >= totalItemCount - 3) {
                        // Set Flag
                        isFetchingData = true;

                        // Get Next Data Set
                        getNextDataSet(nextIndex);
                    }
                }
            }
        });

        // review icons recycler view
        LinearLayoutManager lm = new LinearLayoutManager(getActivity().getApplicationContext());
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        lm.setSmoothScrollbarEnabled(true);
        recyclerViewReviewIcons = (RecyclerView) getView().findViewById(R.id.recyclerView_review_icons);
        recyclerViewReviewIcons.setLayoutManager(lm);
        recyclerViewReviewIcons.setAdapter(reviewIconsAdapter);

        // Get No Reviews Container
        noReviewScreen = (ViewGroup) getView().findViewById(R.id.no_review_wrapper_layout);
        ((TextView) noReviewScreen.findViewById(R.id.leave_review_tv)).setText(R.string.button_leave_review);
        ((ImageView) noReviewScreen.findViewById(R.id.leave_review_iv)).setImageResource(R.drawable.leave_review_star);
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        // Take API Hit
        getRestaurantReviewsFromAPI();
    }

    private void getRestaurantReviewsFromAPI() {
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(0, AppConstant.NODE_RESTAURANT_REVIEW_URL,
                ApiParams.getRestaurantReviewParams(restaurantId, Integer.toString(AppConstant.DEFAULT_PAGINATION_LIMIT), "0"), this, this, true);
    }

    private void getNextReviewSet(int nextOffset) {
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(nextOffset, AppConstant.NODE_RESTAURANT_REVIEW_URL,
                ApiParams.getRestaurantReviewParams(restaurantId,
                        Integer.toString(AppConstant.DEFAULT_PAGINATION_LIMIT),
                        Integer.toString(nextOffset)), this, this, true);
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        switch (viewId) {
            case R.id.layout_secondary_button_list_item:
                handleWriteReviewAction();
                break;

            case R.id.write_review_btn_wrapper:
                handleWriteReviewAction();
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        restaurantId = null;
        restaurantName = null;
        restaurantBundle = null;
        reviewPageAdapter = null;
        buttonRestDetailWriteReview = null;
        recyclerViewRestDetailReviews = null;
        noReviewScreen = null;
    }


    /**
     * API Response Handlers
     */
    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject != null && responseObject.optBoolean("status")) {
            // Get Data
            JSONObject dataJsonObject = responseObject.optJSONObject("data");

            // Handle Restaurant Reviews UI
            handleRestaurantReviewsUI(request, dataJsonObject);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        isFetchingData = false;
    }

    private void handleRestaurantReviewsUI(Request<JSONObject> request, JSONObject dataJsonObject) {
        if (dataJsonObject != null) {
            // set review box data
            setReviewBoxData(dataJsonObject);

            if (dataJsonObject.optJSONArray("stream") == null ||
                    dataJsonObject.optJSONArray("stream").length() == 0) { // Empty
                showNoReviewsMessage();
            } else {
                prepareReviews(request, dataJsonObject);
            }
        }
    }

    private void showNoReviewsMessage() {
        // Show Write Review Button
        if (buttonRestDetailWriteReview != null)
            buttonRestDetailWriteReview.setVisibility(View.GONE);

        // Hide Reviews Icon Recycler View
        recyclerViewReviewIcons.setVisibility(View.INVISIBLE);

        // Hide Recycler View
        if (recyclerViewRestDetailReviews != null)
            recyclerViewRestDetailReviews.setVisibility(View.GONE);

        // Show No Reviews
        if (noReviewScreen != null) {
            noReviewScreen.setVisibility(View.VISIBLE);
            noReviewScreen.findViewById(R.id.write_review_btn_wrapper).setOnClickListener(this);
        }
    }

    private void prepareReviews(Request<JSONObject> request, JSONObject dataJsonObject) {
        // Check if Fragment is attached to Window
        if (getView() == null)
            return;

        // Show / Hide Write a Review
        if (request.getIdentifier() == 0) {
            handleWriteReviewVisibility(dataJsonObject);
        }

        // Hide No Reviews
        noReviewScreen.setVisibility(View.GONE);

        // Show Recycler View
        recyclerViewRestDetailReviews.setVisibility(View.VISIBLE);

        // Show Reviews Icon Recycler View
        recyclerViewReviewIcons.setVisibility(View.VISIBLE);

        // Check if this is first Page
        int offset = dataJsonObject.optInt("offset");
        if (offset <= AppConstant.DEFAULT_PAGINATION_LIMIT) { // 1st Page
            // Remove all Views
            recyclerViewRestDetailReviews.removeAllViews();

            // Set List
            reviewPageAdapter.setJsonArray(dataJsonObject.optJSONArray("stream"));

            // update review icon adapter and notify
            reviewIconsAdapter.setJsonArray(getReviewsIconsArray(dataJsonObject));
            reviewIconsAdapter.notifyDataSetChanged();

        } else { // Next Page

            // Update List
            reviewPageAdapter.updateJsonArray(dataJsonObject.optJSONArray("stream"));
        }

        // Notify Data Change
        reviewPageAdapter.notifyDataSetChanged();

        // Set Next Index
        nextIndex = dataJsonObject.optInt("nextIndex");

        // Set Flag
        isFetchingData = false;
    }

    private void handleWriteReviewVisibility(JSONObject dataJsonObject) {
        if (dataJsonObject != null) {
            JSONArray streamJsonArray = dataJsonObject.optJSONArray("stream");

            // Check for NULL / Empty
            if (streamJsonArray != null && streamJsonArray.length() > 0) {
                // Get First Review
                JSONObject reviewJsonObject = streamJsonArray.optJSONObject(0);
                if (reviewJsonObject != null) {
                    // Check if its User's Review
                    JSONObject reviewData = reviewJsonObject.optJSONObject("data");
                    if (reviewData != null && reviewData.optBoolean("myReview")) {
                        // Show
                        showHideWriteReviewButton(View.GONE);
                    } else {
                        // Show
                        showHideWriteReviewButton(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void setReviewBoxData(JSONObject dataJsonObject) {
        if (dataJsonObject != null) {
            // put review data into arguments
            JSONObject reviewBoxData = dataJsonObject.optJSONObject("reviewBoxData");
            if (reviewBoxData != null && getArguments() != null) {
                getArguments().putString(INFO_STRING, reviewBoxData.toString());
            }
        }
    }


    /**
     * Click Handlers
     */
    private void handleWriteReviewAction() {
        // Track Event

        //trackEventGA(getString(R.string.ga_screen_reviews), getString(R.string.ga_action_write_reviews), restaurantId);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("resId",restaurantId);
        }

        if(!TextUtils.isEmpty(restaurantCategory)){
            trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, "RestaurantWriteReviewClick",
                    restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantId,hMap);
        }else{
            trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType, "RestaurantWriteReviewClick",
                    restaurantBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"+restaurantId,hMap);
        }


        // Check if User is logged In
        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
            // Ask User to Login
            askUserToLogin();

        } else {
            // Proceed with Writing Review
            proceedWriteReview();
        }
    }

    // Ask User to Login
    private void askUserToLogin() {

        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }

    private void showHideWriteReviewButton(int visibility) {
        buttonRestDetailWriteReview.setVisibility(visibility);
    }

    private void proceedWriteReview() {
        try {
            JSONObject temp = new JSONObject(getArguments().getString(INFO_STRING));
            temp.put(RESTAURANT_NAME, restaurantName);
            temp.put(RESTAURANT_ID, restaurantId);
            temp.put(BOOKING_ID, "");

            Bundle bl = new Bundle();
            bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_restaurant_detail)+"_"+restaurantType+"_"+restaurantCategory);
            bl.putString(INFO_STRING, temp.toString());

            RateNReviewDialog dialog = new RateNReviewDialog();
            dialog.setArguments(bl);
            dialog.setRateNReviewCallback(this);

            // Open Rate And Review Dialog
            showFragment(getActivity().getSupportFragmentManager(), dialog);
        } catch (Exception e) {
            // Exception
        }

        // tracking
        try {
            String categoryName = getString(R.string.ga_rnr_category_restaurant_detail)+"_"+restaurantType+"_"+restaurantCategory;
            String actionName = getString(R.string.ga_rnr_action_restaurant_leave_a_review_click);
            String labelName = restaurantName + "_" + restaurantId;

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("resId",restaurantId);
            }

            trackEventForCountlyAndGA(categoryName,actionName,labelName,hMap);
        } catch (Exception e) {
            // Exception
        }
    }

    @Override
    public void onEditReviewClick(JSONObject updateInfoObj) {
        //track event

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("resId",restaurantId);
        }

        if(!TextUtils.isEmpty(restaurantCategory)) {
            trackEventForCountlyAndGA(getString(R.string.ga_screen_reviews) + "_" + restaurantType + "_" + restaurantCategory, getString(R.string.ga_action_edit_reviews), restaurantId, hMap);
        }else{
            trackEventForCountlyAndGA(getString(R.string.ga_screen_reviews) + "_" + restaurantType, getString(R.string.ga_action_edit_reviews), restaurantId, hMap);
        }


        try {
            JSONObject temp = new JSONObject(getArguments().getString(INFO_STRING));
            temp.put(RESTAURANT_NAME, restaurantName);
            temp.put(RESTAURANT_ID, restaurantId);
            temp.put(BOOKING_ID, "");
            updateInfo(temp, updateInfoObj);

            Bundle bl = new Bundle();
            bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_restaurant_detail)+ "_" + restaurantType + "_" + restaurantCategory);
            bl.putString(INFO_STRING, temp.toString());

            RateNReviewDialog dialog = new RateNReviewDialog();
            dialog.setArguments(bl);
            dialog.setRateNReviewCallback(this);

            // Open Rate And Review Dialog
            showFragment(getActivity().getSupportFragmentManager(), dialog);
        } catch (Exception e) {
            // Exception
        }

        // tracking
        try {
            String categoryName = getString(R.string.ga_rnr_category_restaurant_detail)+ "_" + restaurantType + "_" + restaurantCategory;
            String actionName = getString(R.string.ga_rnr_action_restaurant_edit_review_click);
            String labelName = restaurantName + "_" + restaurantId;

            if(hMap!=null){
                hMap.put("resId",restaurantId);
            }
            trackEventForCountlyAndGA(categoryName, actionName,labelName,hMap);
        } catch (Exception e) {
            // Exception
        }
    }

    public void getNextDataSet(int nextOffset) {
        // Take API Hit
        getNextReviewSet(nextOffset);
    }

    private JSONArray getReviewsIconsArray(JSONObject dataJsonObject) {
        JSONArray returnArray = new JSONArray();

        if (dataJsonObject != null) {
            int showsItemCount = dataJsonObject.optInt("imageCount", 0);
            int reviewsCount = dataJsonObject.optInt("reviewCount", 0);

            JSONArray array = dataJsonObject.optJSONArray("stream");
            if (array != null) {
                for (int i = 0; i < array.length() && i < showsItemCount; i++) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("type", ReviewsIconsAdapter.TYPE_AUTHOR_ICON_ITEM_LAYOUT);
                        object.put("imageUrl", array.optJSONObject(i).optJSONObject("data").optString("profileImage"));
                        returnArray.put(object);
                    } catch (JSONException e) {}
                }
            }

            if (reviewsCount - showsItemCount > 0) {
                JSONObject object = new JSONObject();
                try {
                    object.put("type", ReviewsIconsAdapter.TYPE_AUTHOR_ICON_LAST_ITEM_LAYOUT);
                    object.put("reviewCount", reviewsCount - showsItemCount);
                    returnArray.put(object);
                } catch (JSONException e) {}
            }
        }

        return returnArray;
    }

    @Override
    public void onDialogDismiss() {
        // do nothing
    }

    @Override
    public void onReviewSubmission() {
        refreshScreen();
    }

    @Override
    public void onRNRError(JSONObject errorObject) {
        if (errorObject != null) {
            String errorCode = errorObject.optString("error_code", "");
            switch (errorCode) {
                case LOGIN_SESSION_EXPIRE_CODE:
                    refreshScreen();
                    break;
            }
        }
    }

    private void refreshScreen() {
        // hide n reset leave a review layout
        reviewIconsAdapter.setJsonArray(new JSONArray());
        reviewIconsAdapter.notifyDataSetChanged();
        recyclerViewReviewIcons.setVisibility(View.INVISIBLE);
        showHideWriteReviewButton(View.GONE);

        // Hide No Reviews
        noReviewScreen.setVisibility(View.GONE);

        // hide and reset reviews
        reviewPageAdapter.setJsonArray(new JSONArray());
        reviewPageAdapter.notifyDataSetChanged();
        recyclerViewRestDetailReviews.setVisibility(View.GONE);


        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity()));
    }

    @Override
    public void reviewExpandableStatus(boolean isExpanded) {
        // tracking
        try {
            String categoryName = getString(R.string.ga_rnr_category_restaurant_detail)+ "_" + restaurantType + "_" + restaurantCategory;
            String actionName = isExpanded ? getString(R.string.ga_rnr_action_restaurant_review_see_more)
                    : getString(R.string.ga_rnr_action_restaurant_review_see_more_collapse);
            String labelName = restaurantName + "_" + restaurantId;

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("resId",restaurantId);
            }
            trackEventForCountlyAndGA(categoryName,actionName,labelName,hMap);
        } catch (Exception e) {
            // Exception
        }
    }
}
