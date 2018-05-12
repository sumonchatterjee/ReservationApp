package com.dineout.book.fragment.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.recycleradapters.HomeNotificationRecyclerAdapter;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by sumon.chatterjee on 10/04/17.
 */

public class NotificationFragment extends YouPageWrapperFragment
        implements View.OnClickListener,HomeNotificationRecyclerAdapter.OnNotificationClickedListener {

    private RecyclerView recyclerViewNotificationList;
    private LinearLayout linearLayoutNotificationNoResultFound;
    private HomeNotificationRecyclerAdapter mAdapter;
    private boolean isRestaurantListGettingLoaded;
    private int mTotalItemCount = Integer.MAX_VALUE;

    private boolean isRequestInTransit = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new HomeNotificationRecyclerAdapter(getContext());
        mAdapter.setNetworkManager(getNetworkManager(), getImageLoader());
        mAdapter.setOnCardClickedListener(this);

       if(!TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) {
            onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_notification, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set toolbar
        setToolbarTitle("Notification");

        // find view by id
        findViewById();

        // check if user is logged in or not
        if(TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) {
          showLoginView();

        } else {
            hideLoginView();

            if (isRequestInTransit) {
                showLoader();
            } else{
                hideLoader();
            }
        }
    }


    protected void findViewById() {
        recyclerViewNotificationList = (RecyclerView) getView().findViewById(R.id.recyclerViewNotificationList);
        recyclerViewNotificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewNotificationList.setAdapter(mAdapter);
        recyclerViewNotificationList.addOnScrollListener(new NotificationScrollListener());

        linearLayoutNotificationNoResultFound = (LinearLayout) getView().findViewById(R.id.linearLayoutNotificationNoResultFound);

    }


    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

            //fetch notificaton
            fetchNotification(0);

    }


    private HashMap<String, String> setNotificationApiParams(int pageNum) {
        int start = (pageNum * Constants.PAGE_LIMIT);
        HashMap<String, String> params = new HashMap<>();
        params.put("start", Integer.toString(start));
        params.put("limit", Integer.toString(Constants.PAGE_LIMIT));
        return params;

    }

    private void fetchNotification(int pageNum) {

        HashMap<String, String> params = setNotificationApiParams(pageNum);

        isRequestInTransit = true;

        // Take API Hit
        showLoader();
        getNetworkManager().jsonRequestGet(pageNum, AppConstant.URL_GET_NOTIFICATION,
                params,
                this, this, false);
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void onItemClicked(JSONObject jsonObject) {
      if(jsonObject!=null){

          //track event
          try {
              HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
              if (hMap != null) {
                  hMap.put("poc", ""+jsonObject.get("position"));
              }
          }catch (Exception ex){

          }

          String deeplink = jsonObject.optString("link");
          if(!TextUtils.isEmpty(deeplink)){
              MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
              if (masterDOFragment != null) {
                  addToBackStack(getFragmentManager(), masterDOFragment);
              }
          }
      }
    }


    private void showNoResults() {
        // Remove Views
        mAdapter.setJsonArray(null);
        recyclerViewNotificationList.removeAllViews();

        // Show No Results Message
        linearLayoutNotificationNoResultFound.setVisibility(View.VISIBLE);
    }


    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        isRequestInTransit = false;
        hideLoader();

        // user click on notifcaiton icon so reset the seen notification time
        DOPreferences.setNotificationSeenTime(getContext(), System.currentTimeMillis() / 1000);

        if (responseObject != null && !responseObject.optBoolean("status")) {
            showNoResults();
            return;
        }

        if (responseObject != null) {
            if (responseObject.optBoolean("status")) {

                isRestaurantListGettingLoaded = false;
                // Get Output Params Object
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");
                if (outputParamsJsonObject != null) {

                    JSONObject data = outputParamsJsonObject.optJSONObject("data");

                    if (data != null) {
                        // set total item count
                        if (data.has("total_count")) {
                            mTotalItemCount = data.optInt("total_count");
                        }

                        // Get Data Object
                        JSONArray dataJsonArray = data.optJSONArray("list");
                        if (mAdapter != null) {
                            if (dataJsonArray != null) {
                                mAdapter.setData(dataJsonArray, request.getIdentifier(),null);
                            }

                            if (mAdapter.getJsonArray() != null && mAdapter.getJsonArray().length() > 0) {
                                linearLayoutNotificationNoResultFound.setVisibility(View.GONE);
                            } else {
                                showNoResults();
                            }
                        }

                    }

                }

            }
        }
    }


    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        isRequestInTransit = false;
        hideLoader();

        showNoResults();

    }


    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
         hideLoginView();
        fetchNotification(0);
    }


    @Override
    public void LoginSessionExpiredNegativeClick() {
        popBackStack(getFragmentManager());
    }


    class NotificationScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!isRestaurantListGettingLoaded && (totalItemCount < mTotalItemCount)
                        && ((visibleItemCount + pastVisibleItems) >= totalItemCount - 3)) {
                    isRestaurantListGettingLoaded = true;
                    int pageNumToBeFetched = totalItemCount / AppConstant.DEFAULT_PAGINATION_LIMIT;

                    // Hide No Results Message
                    linearLayoutNotificationNoResultFound.setVisibility(View.GONE);

                    fetchNotification(pageNumToBeFetched);


                }
            }
        }
    }


    private void showLoginView() {
        if (getView() != null) {
            ViewGroup loginView = (ViewGroup) getView().findViewById(R.id.login_screen);
            loginView.setVisibility(View.VISIBLE);

            View button = loginView.findViewById(R.id.login_btn);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, NotificationFragment.this);

                    // tracking
//                    trackEventGA(getString(R.string.ga_new_login_category_name),
//                            getString(R.string.ga_new_login_artwork_screen_action),
//                            getString(R.string.ga_new_login_artwork_screen_label));

                }
            });
        }

    }

    private void hideLoginView() {
        if (getView() != null) {
            View loginView = getView().findViewById(R.id.login_screen);
            if (loginView != null) {
                loginView.setVisibility(View.GONE);
            }
        }
    }

}
