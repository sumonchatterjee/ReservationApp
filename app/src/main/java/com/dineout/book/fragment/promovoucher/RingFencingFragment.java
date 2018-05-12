package com.dineout.book.fragment.promovoucher;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.Constants;
import com.dineout.book.util.UiUtil;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.dialogs.MoreDialog;
import com.dineout.recycleradapters.RingFencingAdapter;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class RingFencingFragment extends YouPageWrapperFragment
        implements RingFencingAdapter.OnReserveButtonClickListener,
        RingFencingAdapter.OnPromoButtonClickListener {

    private static final int REQUEST_CODE_GET_PROMO_LIST_API = 999;
    JSONObject jsonObject;
    private RingFencingAdapter ringFencingAdapter;
    private String deepLinkQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ring_fencing, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //track
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_promotion),getString(R.string.d_promotion),hMap);

        if (getArguments() != null) {
            deepLinkQuery = getArguments().getString(Constants.SEARCH_KEYWORD);
        }

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_tem);

        ringFencingAdapter = new
                RingFencingAdapter(getActivity(), getNetworkManager());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager
                (getActivity(), LinearLayoutManager.VERTICAL, false);

        // Set Layout Manager
        recyclerView.setLayoutManager(linearLayoutManager);
        ringFencingAdapter.setOnReserveButtonClickListener(this);
        ringFencingAdapter.setOnPromoButtonClickListener(this);

        recyclerView.setAdapter(ringFencingAdapter);

        // authenticate the user
        authenticateUser();
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        initiateRingfencingAPI();
    }



    private void initiateRingfencingAPI() {
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_PROMO_LIST_API, AppConstant.URL_RING_FENCING,
                ApiParams.getRingFencingParams(DOPreferences.getCityId(getActivity().getApplicationContext())),
                this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_CODE_GET_PROMO_LIST_API) {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    if (responseObject.optJSONObject("output_params") != null) {

                        // Get Data
                        JSONObject jsonObjectData = responseObject.optJSONObject("output_params").optJSONObject("data");

                        if (jsonObjectData != null) {
                            // Get Section
                            JSONArray jsonArraySection = jsonObjectData.optJSONArray("section");
                            JSONObject jsonObjectSectionData = jsonObjectData.optJSONObject("section_data");

                            if (jsonArraySection != null && jsonArraySection.length() > 0 && jsonObjectSectionData != null) {

                                prepareItem(jsonArraySection, jsonObjectSectionData);
                            }
                        }
                    }
                } else if (!responseObject.optBoolean("status")) {
                    // handle session expire
//                    handleApiResponse(responseObject);

                } else {
                    // Show Message
                    UiUtil.showToastMessage(getActivity().getApplicationContext(),
                            getString(R.string.text_unable_fetch_details));
                }
            } else {
                // Show Message
                UiUtil.showToastMessage(getActivity().getApplicationContext(),
                        getString(R.string.text_general_error_message));
            }
        }
    }

    private void prepareItem(JSONArray jsonArraySection, JSONObject jsonObjectSectionData) {
        if (ringFencingAdapter != null) {
            ringFencingAdapter.setRingFencingDataObject(jsonArraySection, jsonObjectSectionData);
            setJSONObject(jsonObjectSectionData);
            ringFencingAdapter.setRingfencingPromocode(deepLinkQuery);
            ringFencingAdapter.notifyDataSetChanged();
        }
    }

    private void setJSONObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    private JSONObject getJsonObject() {
        return jsonObject;
    }

    @Override
    public void onReserveButtonClicked(String deeplink) {
        if (!TextUtils.isEmpty(deeplink)) {
            MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), deeplink);
            if (masterDOFragment != null) {
                addToBackStack(getParentFragment().getFragmentManager(), masterDOFragment);
            }
        }
    }

    @Override
    public void onPromoButtonClicked(String title, String msg) {
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(msg)) {
            MoreDialog dialog = new MoreDialog(title, msg, getActivity(), false);

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                    ringFencingAdapter.notifyItemChanged(0);

                }
            });

            dialog.show(getParentFragment().getFragmentManager(), "promo_code");
        }
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowSucessObject) {
        if (getActivity() != null) {
            initiateRingfencingAPI();

            if (DOPreferences.isDinerNewUser(getActivity().getApplicationContext())) {
                DOPreferences.setDinerNewUser(getActivity().getApplicationContext(), "0");
            }
        }
    }



}
