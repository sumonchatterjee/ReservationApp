package com.dineout.book.dialogs;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.recycleradapters.RateNReviewRestaurantNotVisitedReasonsAdapter;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.dialogs.RateNReviewThankYouDialog.THANK_YOU_MSG_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.ASK_USER_DIALOG_TYPE;
import static com.dineout.recycleradapters.util.RateNReviewUtil.ASK_USER_DIALOG_TYPE_STICKY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.updateViewState;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewRestaurantNotVisitedDialog extends MasterDOStringReqFragment implements View.OnClickListener,
        RateNReviewRestaurantNotVisitedReasonsAdapter.AdapterCallback {
    private final static int REQUEST_CODE_SUBMIT_REVIEW = 102;
    private RateNReviewUtil.RateNReviewCallbacks mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rate_n_review_not_visited_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);

        // set dialog width
        Rect displayRectangle = new Rect();
        Window window = getDialog().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        getDialog().getWindow().setLayout((int) (displayRectangle.width() * .95), WindowManager.LayoutParams.WRAP_CONTENT);

        ImageView crossIv = (ImageView) view.findViewById(R.id.cross_iv);
        crossIv.setOnClickListener(this);

        TextView headerTv = (TextView) view.findViewById(R.id.header_tv);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.restaurant_not_visited_recycler_view);

        try {
            if (getArguments() != null) {
                String infoString = getArguments().getString(INFO_STRING);
                if (!TextUtils.isEmpty(infoString)) {
                    JSONObject data = new JSONObject(infoString);

                    // header text
                    String headerText = data.optString("text");
                    if (TextUtils.isEmpty(headerText)) headerText = "";

                    headerTv.setText(headerText);

                    // reason list
                    JSONArray arr = data.optJSONArray("tags");
                    if (arr != null) {
                        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        rv.setLayoutManager(llm);
                        RateNReviewRestaurantNotVisitedReasonsAdapter adapter = new RateNReviewRestaurantNotVisitedReasonsAdapter(getActivity());
                        adapter.setAdapterCallback(this);
                        adapter.setJsonArray(arr);
                        rv.setAdapter(adapter);
                    }
                }
            }

        } catch (Exception e) {
            // Exception
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().getAttributes().windowAnimations = R.style.RNRStyle;
    }

    @Override
    public void itemClick(JSONObject object) {
        if (object != null) {
            JSONObject obj = RateNReviewUtil.getJsonObject(getArguments().getString(INFO_STRING));
            String bId = obj.optString(BOOKING_ID);
            String restId = obj.optString(RESTAURANT_ID);
            String reviewId = obj.optString(REVIEW_ID);
            String action = obj.optString(REVIEW_ACTION);
            String reviewTags = object.optString("id");

            showLoader();

            disableScreenClicks();

            sendTracking(object);

            getNetworkManager().stringRequestPost(REQUEST_CODE_SUBMIT_REVIEW, AppConstant.URL_SUBMIT_REVIEW,
                    ApiParams.getSubmitReviewRestaurantNotVisitedParams(
                            DOPreferences.getDinerId(getContext()), bId, restId,
                            reviewId, reviewTags, action), this, this, false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cross_iv:
                getDialog().cancel();
                break;
        }
    }

    @Override
    public void onResponse(Request<String> request, String responseString, Response<String> response) {
        super.onResponse(request, responseString, response);

        enableScreenClicks();

        if (request.getIdentifier() == REQUEST_CODE_SUBMIT_REVIEW) {
            handleRestaurantReviewResponse(responseString);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        if(getActivity() == null || getView() == null)
            return;

        // hide loader
        hideLoader();

        // enabled clicks
        enableScreenClicks();

        // show toast
        if (!AppUtil.hasNetworkConnection(getActivity())) {
            Toast.makeText(getActivity(), getResources().getString(R.string.cb_no_internet),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRestaurantReviewResponse(String responseString) {
        if (!TextUtils.isEmpty(responseString)) {
            try {
                JSONObject responseObject = new JSONObject(responseString);
                if (responseObject.optBoolean("status")) {
                    JSONObject outputParams = responseObject.optJSONObject("output_params");
                    if (outputParams != null) {
                        JSONObject dataObject = outputParams.optJSONObject("data");
                        if (dataObject != null) {
                            String msg = dataObject.optString("msg", "");

                            // dismiss the dialog
                            getDialog().dismiss();

                            // show thank you screen
                            showThankYouScreen(msg);
                        }
                    }
                } else {
                    String errMsg = responseObject.optString("error_msg", "");
                    if (!TextUtils.isEmpty(errMsg)) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }

                    String errorCode = responseObject.optString("error_code", "");
                    switch (errorCode) {
                        case LOGIN_SESSION_EXPIRE_CODE:
                            DOPreferences.deleteDinerCredentials(getActivity());

                            // send dismiss callback
                            if (mCallback != null) {
                                mCallback.onRNRError(responseObject);
                            }
                            break;
                    }

                    // dismiss the dialog
                    getDialog().dismiss();
                }
            } catch (Exception e) {
                // Exception
            }
        }
    }

    private void showThankYouScreen(String msg) {
        RateNReviewThankYouDialog dialog = new RateNReviewThankYouDialog();

        Bundle bl = new Bundle();
        bl.putString(THANK_YOU_MSG_KEY, msg);
        dialog.setArguments(bl);

        // Open Rate And Review Dialog
        showFragment(getActivity().getSupportFragmentManager(), dialog);
    }

    private void sendTracking(JSONObject object) {
        if (getArguments() != null) {
            String dialogType = getArguments().getString(ASK_USER_DIALOG_TYPE, "");
            String action;
            if (dialogType.equals(ASK_USER_DIALOG_TYPE_STICKY)) {
                action = getString(R.string.ga_rnr_action_recon_sticky_no_click);
            } else {
                action = getString(R.string.ga_rnr_action_recon_pop_up_no_click);
            }

            // reason
            String reason = (object != null) ? object.optString("value") : null;

            // Track Event
            AnalyticsHelper.getAnalyticsHelper(getContext())
                    .trackEventGA(getString(R.string.ga_rnr_category_home), action, reason);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("category",getString(R.string.ga_rnr_category_home));
                hMap.put("action", action);
                hMap.put("label",reason);
            }

            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(action,hMap);
        }
    }

    private void disableScreenClicks() {
        updateViewState(getView(), false);
    }

    private void enableScreenClicks() {
        updateViewState(getView(), true);
    }

    public void setRateNReviewCallback(RateNReviewUtil.RateNReviewCallbacks callback) {
        this.mCallback = callback;
    }
}
