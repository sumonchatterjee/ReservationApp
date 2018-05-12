package com.dineout.book.fragment.payments.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.util.PaymentUtils;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.payuui.PaymentsActivity;

import org.json.JSONObject;

public class PayUBaseFragment extends DOBaseFragment {

    private final int INIT_PAYU_PG = 0x76;

    protected void payThroughPayU(final PayuConfig config, final PaymentParams params, final String mType) {
        showLoader();

        getNetworkManager().stringRequestPost(INIT_PAYU_PG,
                AppConstant.URL_INIT_PAYMENT, ApiParams.getInitPaymentParams(DOPreferences.getDinerId(getContext()),
                        getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, "-1"),
                        getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.IS_WALLET_ACCEPTED),
                        getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.PAYMENT_TYPE), ApiParams.PAYMENT_TYPE_PAYU + ""),
                new TransFromWalletHandler(params, config, mType), new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                    }
                }, false);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            Bundle bundle = new Bundle();
            if (data != null && data.hasExtra(PayuConstants.PAYU_RESPONSE) && resultCode == Activity.RESULT_OK) {
                try {
                    String payuData = data.getStringExtra(PayuConstants.PAYU_RESPONSE);
                    JSONObject resp = new JSONObject(payuData);
                    bundle.putString(PaymentConstant.DISPLAY_ID, resp.optString("txnid"));
                    bundle.putString(PaymentConstant.FINAL_AMOUNT, (int) resp.optDouble("transaction_fee") +
                            "");

                    if (resp.optString("status").equalsIgnoreCase("success")) {
                        bundle.putBoolean(PaymentConstant.PAYMENT_STATUS, true);

                    } else {
                        bundle.putBoolean(PaymentConstant.PAYMENT_STATUS, false);
                    }

                    showStatusScreen(bundle);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Transaction cancelled", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }

    @Override
    public void onRemoveErrorView() {

    }

    private class TransFromWalletHandler implements Response.Listener<String> {

        private PayuConfig mConfig;
        private PaymentParams mParams;
        private String mType;

        public TransFromWalletHandler(PaymentParams params, PayuConfig config, String type) {
            mConfig = config;
            mParams = params;
            mType = type;
        }

        @Override
        public void onResponse(Request<String> request, String responseObject, Response<String> response) {

            hideLoader();

            if (getActivity() == null || getView() == null)
                return;

            if (request.getIdentifier() == INIT_PAYU_PG) {
                try {
                    JSONObject resp = new JSONObject(responseObject);
                    if (resp != null && resp.optBoolean("status")) {

                        JSONObject params = resp.optJSONObject("output_params");
                        if (params != null) {
                            JSONObject data = params.optJSONObject("data");

                            if (data != null) {
                                PaymentParams param = PaymentUtils.getPaymentParams(data.toString());
                                PayuHashes hashes = PaymentUtils
                                        .getPayUHashes(data.optJSONObject("hashes").toString());
                                if (mType.equalsIgnoreCase(PayuConstants.NB))
                                    param.setBankCode(mParams.getBankCode());
                                else if (mType.equalsIgnoreCase(PayuConstants.CC) || mType.equalsIgnoreCase(PayuConstants.STORED_CARD)) {
                                    param.setCardNumber(mParams.getCardNumber());
                                    param.setCardName(mParams.getCardName());
                                    param.setNameOnCard(mParams.getNameOnCard());
                                    param.setExpiryMonth(mParams.getExpiryMonth());
                                    param.setExpiryYear(mParams.getExpiryYear());
                                    param.setCvv(mParams.getCvv());
                                    param.setCardToken(mParams.getCardToken());

                                    param.setStoreCard(mParams.getStoreCard());

                                }
                                param.setHash(hashes.getPaymentHash());
                                PostData postData = new PaymentPostParams(param, mType).getPaymentPostParams();

                                if (postData.getCode() == PayuErrors.NO_ERROR) {
                                    mConfig.setData(postData.getResult());
                                    Intent intent = new Intent(getActivity(), PaymentsActivity.class);
                                    intent.putExtra(PayuConstants.PAYU_CONFIG, mConfig);
                                    startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                                } else {
                                    Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    } else {

                        if (resp != null)
                            Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                                    "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {

                }
            }
        }
    }
}
