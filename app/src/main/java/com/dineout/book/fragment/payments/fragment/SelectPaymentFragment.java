package com.dineout.book.fragment.payments.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.adapter.PaymentOptionAdapter;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.phonepe.android.sdk.api.PhonePe;
import com.phonepe.android.sdk.base.models.DebitRequest;
import com.phonepe.android.sdk.base.models.InstrumentSplitPreference;
import com.phonepe.android.sdk.base.models.OrderInfo;
import com.phonepe.android.sdk.base.models.PaymentInstrumentsPreference;
import com.phonepe.android.sdk.base.models.UserInfo;
import com.phonepe.android.sdk.domain.builders.DebitRequestBuilder;
import com.phonepe.android.sdk.domain.builders.OrderInfoBuilder;
import com.phonepe.android.sdk.domain.builders.PaymentInstrumentPreferenceBuilder;
import com.phonepe.android.sdk.domain.builders.UserInfoBuilder;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SelectPaymentFragment extends DOBaseFragment implements Response.Listener, Response.ErrorListener, View.OnClickListener {

    public static final String ARG_OPTION_RESP = "arg_option_resp";
    private final int WALLET_SUMMARY_REQUEST = 0x09;
    private final int INIT_PG = 0x05;
    private final int INIT_PHONEPE_REQUEST = 0x03;
    private RecyclerView mRecyclerView;
    private PaymentOptionAdapter mOptionAdapter;
    private String type;
    private JSONObject payuInfo;
    private JSONObject phonePeInfo;
    private String apiResp;

    public static SelectPaymentFragment newInstance(Bundle bundle) {

        SelectPaymentFragment fragment = new SelectPaymentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenName("P_PaymentOptions");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_payment, container, false);
    }

    public void setApiResp(String resp) {
        this.apiResp = resp;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        ((TextView) view.findViewById(R.id.amount_value)).setText(String.format(getResources().getString(R.string.container_rupee),
                getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0")));
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_payment_option);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new PaymentOptionAdapter(getActivity(),
                null, this));
        setToolbarTitle("Select Payment Options");

        if (getArguments() != null && !TextUtils.isEmpty(apiResp)) {

            try {
                JSONObject data = new JSONObject(apiResp);
                handleGetPaymentOptionResponse(data);
            } catch (Exception e) {
                // Exception
            }
        }
    }

    @Override
    public void onRemoveErrorView() {

    }


    @Override
    public void onResponse(Request request, Object responseObject, Response response) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;


        try {
            JSONObject resp = new JSONObject((String) responseObject);
            if (resp != null && resp.optBoolean("status")) {
                JSONObject outputParam = resp.optJSONObject("output_params");
                if (outputParam != null) {
                    JSONObject data = outputParam.optJSONObject("data");
                    if (request.getIdentifier() == INIT_PG) {

                        paymentThroughPG(data);
                    } else if (request.getIdentifier() == WALLET_SUMMARY_REQUEST) {

                        int action = data.optInt("action_id");
                        Bundle bundle = getArguments();
                        bundle.putInt(PaymentConstant.WALLET_TYPE, data.optInt("payment_type"));
                        if (action == 1) {
                            WalletSummaryFragment fragment = WalletSummaryFragment.newInstance(bundle, data);
                            addToBackStack(getActivity(), fragment);
                        } else if (action == 2) {

                            AddMoneyFragment fragment = AddMoneyFragment.newInstance(bundle, data);
                            addToBackStack(getActivity(), fragment);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Exception

        }

    }

    private void handleGetPaymentOptionResponse(JSONObject data) {
        try {
            if (data != null) {

                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mRecyclerView.setAdapter(new PaymentOptionAdapter(getActivity(),
                        data.optJSONArray("payment_options"), this));
                payuInfo = data.optJSONObject("section").optJSONObject("payu");

            }
        } catch (Exception e) {
            // Exception
        }
    }


    private void paymentThroughPG(JSONObject payUInfo) {

        if (payuInfo != null) {
            Bundle bundle = getArguments();
            bundle.putString(PayuConstants.PAYU_HASHES, payUInfo.optJSONObject("hashes")
                    .toString());
            bundle.putString(PayuConstants.PAYMENT_PARAMS, payUInfo.toString());
            bundle.putString(PayuConstants.PAYU_CONFIG, payUInfo.optString("server_type"));
            DOBaseFragment fragment = null;
            Payu.setInstance(getActivity());

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if (type.equalsIgnoreCase(PayuConstants.CC)) {

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity())
//                        .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_select_payment),
//                                getString(com.dineout.book.R.string.ga_action_card_payment), null);
//
                trackEventForCountlyAndGA("P_PaymentOptions","PaymentOptionClick",
                        getString(com.dineout.book.R.string.ga_action_card_payment),hMap);

                fragment = CreditCardFragment
                        .newInstance(bundle);

            } else if (type.equalsIgnoreCase(PayuConstants.STORED_CARD) ||
                    type.equalsIgnoreCase("")) {

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity())
//                        .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_select_payment),
//                                getString(com.dineout.book.R.string.ga_action_saved_cards), null);

                trackEventForCountlyAndGA("P_PaymentOptions","PaymentOptionClick",
                        getString(com.dineout.book.R.string.ga_action_saved_cards),hMap);

                fragment = StoredCardFragment
                        .newInstance(bundle);

            } else if (type.equalsIgnoreCase(PayuConstants.NB)) {

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity())
//                        .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_select_payment),
//                                getString(com.dineout.book.R.string.ga_action_netbanking), null);

                trackEventForCountlyAndGA("P_PaymentOptions","PaymentOptionClick",
                        getString(com.dineout.book.R.string.ga_action_netbanking),hMap);

                fragment = NetBankingFragment.newInstance(bundle);
            }

            if (fragment != null) {
                addToBackStack(getActivity(), fragment);
            }
        }
    }


    @Override
    public void onClick(View v) {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        JSONObject option = (JSONObject) v.getTag();
        if (option != null) {
            if (AppUtil.hasNetworkConnection(getActivity().getApplicationContext())) {
                if (option.optString("type").toLowerCase().contains("wallet")) {
                    String paymentType = option.optString("type");
                    if (paymentType.equalsIgnoreCase("paytm_wallet")) {

                        // Track Event
//                        AnalyticsHelper.getAnalyticsHelper(getActivity())
//                                .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_select_payment),
//                                        getString(com.dineout.book.R.string.ga_action_paytm), null);

                        trackEventForCountlyAndGA("P_PaymentOptions","AddWalletScreenClick",
                                getString(com.dineout.book.R.string.ga_action_paytm),hMap);

                    } else {
                        // Track Event
//                        AnalyticsHelper.getAnalyticsHelper(getActivity())
//                                .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_select_payment),
//                                        getString(com.dineout.book.R.string.ga_action_mobiwik), null);

                        trackEventForCountlyAndGA("P_PaymentOptions","AddWalletScreenClick",
                                getString(com.dineout.book.R.string.ga_action_mobiwik),hMap);
                    }

                    int type = option.optString("type").toLowerCase().contains("mobikwik") ?
                            PaymentConstant.MOBIKWIK :
                            (option.optString("type").toLowerCase().contains("paytm") ?
                                    PaymentConstant.PAYTM : PaymentConstant.FREECHARGE);
                    boolean linked =
                            option.optString("linked", "0").equalsIgnoreCase("1") ? true : false;

                    Bundle bundle = getArguments();
                    bundle.putInt(PaymentConstant.WALLET_TYPE, type);
                    if (linked) {
                        callWalletSummary(type);

                    } else {
                        DOBaseFragment fragment = WalletNumberFragment.newInstance(bundle);
                        addToBackStack(getActivity(), fragment);
                    }


                } else if (option.optString("type").toLowerCase().contains("payu")) {
                    type = option.optString("pg").toLowerCase();
                    paymentThroughPG(payuInfo);

                } else if (option.optString("type").equalsIgnoreCase("phonepe")) {
                    trackEventForCountlyAndGA("P_PaymentOptions","AddWalletScreenClick",
                            getString(com.dineout.book.R.string.ga_action_phonePe),hMap);
                    paymentThroughPhonePe(PaymentConstant.PHONEPE);
                }
            }
            else {
                UiUtil.showSnackbar(v,
                        getString(R.string.error_no_network), 0);
            }
        }

    }


    private void callWalletSummary(int type) {

        showLoader();
        Map<String, String> param = new HashMap<>();
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("payment_type", type + "");

        param.put("obj_type", getArguments().getString(PaymentConstant.PAYMENT_TYPE, "booking"));
        if (!getArguments().getString(PaymentConstant.PAYMENT_TYPE).equalsIgnoreCase("deal")) {

            param.put("obj_id", getArguments().getString(PaymentConstant.BOOKING_ID, "0"));
        }
        param.put("is_do_wallet_used", getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED) ? "1" : "0");

        getNetworkManager().stringRequestPost(WALLET_SUMMARY_REQUEST, AppConstant.URL_WALLET_SUMMARY
                , param, this, this, false);
    }


    @Override
    public void onErrorResponse(Request request, VolleyError error) {

        hideLoader();
    }

    private void paymentThroughPhonePe(int type) {
        showLoader();
        getNetworkManager().stringRequestPost(INIT_PHONEPE_REQUEST,
                AppConstant.URL_INIT_PAYMENT, ApiParams
                        .getInitPaymentParams(DOPreferences.getDinerId(getContext()),
                                getArguments().getString(PaymentConstant.BOOKING_ID, "-1"),
                                getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED)
                                , getArguments().getString(PaymentConstant.PAYMENT_TYPE),
                                type + ""), new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        hideLoader();
                        if (getActivity() == null || getView() == null)
                            return;
                        handlePhonePeInitiationResponse(responseObject);


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, false);
    }


    private void handlePhonePeInitiationResponse(String response) {

        try {
            if (!TextUtils.isEmpty(response)) {
                JSONObject resp = new JSONObject(response);
                if (resp != null && resp.optBoolean("status")) {
                    JSONObject outputParams = resp.optJSONObject("output_params");
                    if (outputParams != null) {
                        JSONObject data = outputParams.optJSONObject("data");
                        if (data != null) {
                            initiateCallToPhonePeSDK(data);
                        }
                    }
                }
            }
        } catch (Exception e) {

        }


    }

    private void initiateCallToPhonePeSDK(JSONObject data) {
        String merchantId = data.optString("m_id");
        String userId = data.optString("user_id");
        String mobileNum = data.optString("mobile_number");
        String email = data.optString("email");
        String orderId = data.optString("order_id");
        String orderNote = data.optString("order_note");
        String txnId = data.optString("txnId");
        String amount = data.optString("amount");
        String checkSum = data.optString("checksum");
        ;
        UserInfo userInfo = new UserInfoBuilder()
                .setUserId(userId)
                .setMobileNumber(mobileNum)
                .setEmail(email)
                .build();

        OrderInfo orderInfo = new OrderInfoBuilder()
                .setOrderId(orderId) // Mandatory. Used to maintain context
                .setMessage(orderNote)
                .build();



        //use this for release
        PaymentInstrumentsPreference paymentInstrumentsPreference = new PaymentInstrumentPreferenceBuilder()
                .setWalletAllowed(true)
                .setAccountAllowed(true)
                .setCreditCardAllowed(true)
                .setDebitCardAllowed(true)
                .setNetBankingAllowed(false)
                .setEGVAllowed(false)
                .setInstrumentSplitPreference(InstrumentSplitPreference.MULTI_INSTRUMENT_MODE)
                .build();

        DebitRequest debitRequest = new DebitRequestBuilder()
                .setTransactionId(txnId)  //procured as explained above
                .setAmount(Long.valueOf(amount))
                .setPaymentInstrumentsPreference(paymentInstrumentsPreference)
                .setOrderInfo(orderInfo)
                .setUserInfo(userInfo)
                .setAccountingInfo(null)
                .setChecksum(checkSum)
                .build();

        Intent payIntent = PhonePe.getDebitIntent(getActivity(), merchantId, debitRequest);

        getActivity().startActivityForResult(payIntent, AppConstant.PHONEPE_DEBIT_REQUEST);
    }


}