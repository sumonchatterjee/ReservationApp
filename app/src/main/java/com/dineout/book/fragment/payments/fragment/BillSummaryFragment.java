package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class BillSummaryFragment extends DOBaseFragment implements View.OnClickListener, Response.Listener, Response.ErrorListener {

    private final int REQUEST_BILL_SUMMARY = 0x01;
    private final int REQUEST_DOWALLET_PAYMENT = 0x45;
    private final int GET_PAYMENT_OPTION = 0x02;
    private final int REQUEST_DO_CODE_PAYMENT = 0x80;
    RelativeLayout restaurantEarning, dineoutWallet;
    private TextView mAmount;
    private TextView mTotal;
    private Button mSelectPayment;
    private TextView mWallet;
    private TextView mRestaurantName, restaurantEarningName, restaurantAmt;
    private TextView mBookingId;
    private boolean isDOWalletPayment = false;
    private String doHash, restaurantEarningsAmt;
    private String restaurantId;

    public static BillSummaryFragment newInstance(Bundle bundle) {

        BillSummaryFragment fragment = new BillSummaryFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenName("P_PaymentSummary");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bill_summary_modified, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Payment Summary");

        Bundle bundle = getArguments();
        restaurantId = bundle.getString(PaymentConstant.RESTAURANT_ID);


        View view = getView();

        mRestaurantName = (TextView) view.findViewById(R.id.biller_name);
        mBookingId = (TextView) view.findViewById(R.id.booking_id);
        mAmount = (TextView) view.findViewById(R.id.amount_tobe_paid);
        mWallet = (TextView) view.findViewById(R.id.amount_wallet);
        mTotal = (TextView) view.findViewById(R.id.amount_net);

        restaurantEarningName = (TextView) view.findViewById(R.id.restaurant_name);
        restaurantAmt = (TextView) view.findViewById(R.id.restaurant_wallet);
        restaurantEarning = (RelativeLayout) view.findViewById(R.id.restaurant_earning_layout);
        dineoutWallet = (RelativeLayout) view.findViewById(R.id.dineout_earnings);

        mSelectPayment = (Button) view.findViewById(R.id.btn_payment_option);
        mSelectPayment.setOnClickListener(this);
        calculatePaymentBreakUp();

    }

    @Override
    public void onRemoveErrorView() {

    }

    private void calculatePaymentBreakUp() {

        if (getArguments() != null) {


            mWallet.setText("0");
            mRestaurantName.setText(getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.RESTAURANT_NAME, ""));

            String type = getArguments().getString(PaymentConstant.PAYMENT_TYPE,"");

            if(type.equalsIgnoreCase(ApiParams.BOOKING_TYPE)) {
                mBookingId.setText("Booking ID: " + getArguments().getString(PaymentConstant.DISPLAY_ID, ""));
                mBookingId.setVisibility(View.VISIBLE);
            }
            else{
                mBookingId.setVisibility(View.GONE);
            }
            mAmount.setText("0");
            mTotal.setText("0");

            fetchSummary();
        }
    }

    private void fetchSummary() {

        showLoader();
        getNetworkManager().stringRequestPost(REQUEST_BILL_SUMMARY, AppConstant.URL_BILL_PAYMENT
                , ApiParams.getBillingSummaryParamsBooking(DOPreferences.getDinerId(getContext()), getArguments().
                                getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, "")
                        ,getArguments().getString(PaymentConstant.PAYMENT_TYPE,""),
                        getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.IS_WALLET_ACCEPTED),
                        getArguments().getString(PaymentConstant.BOOKING_AMT, "0")), this, this, false);
    }

    @Override
    public void onClick(View v) {

        if (getView() == null || getActivity() == null) {
            return;
        }
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        String type = (String) v.getTag();
        if (type != null) {
            if (type.equalsIgnoreCase("pg")) {

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity())
//                        .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_bill_summary),
//                                getString(com.dineout.book.R.string.ga_action_payment_gateway_used), restaurantId);

                trackEventForCountlyAndGA("P_PaymentSummary","PaymentSummaryCTAClick",
                        getString(com.dineout.book.R.string.ga_action_payment_gateway_used),hMap);
                showOtherPaymentOption();

            } else if (type.equalsIgnoreCase("dw")) {

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity())
//                        .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_bill_amount),
//                                getString(com.dineout.book.R.string.ga_action_dineout_wallet_used), restaurantId);

                trackEventForCountlyAndGA("P_PaymentSummary","PaymentSummaryCTAClick",
                        getString(com.dineout.book.R.string.ga_action_dineout_wallet_used),hMap);

                makePaymentFromDOWallet();
            } else if (type.equalsIgnoreCase("pc")) {

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity())
//                        .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_bill_amount),
//                                getString(com.dineout.book.R.string.ga_action_ringfencing_used), restaurantId);


                trackEventForCountlyAndGA("P_PaymentSummary","PaymentSummaryCTAClick",
                        getString(com.dineout.book.R.string.ga_action_ringfencing_used),hMap);

                //pay from restaurant earnings
                payFromRestaurantEarnings();

            }
        }

    }

    private void makePaymentFromDOWallet() {
        showLoader();


        Map<String, String> param = ApiParams.getPayFromWalletParam(getArguments().
                        getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, ""), DOPreferences.getDinerId(getActivity()
                        .getApplicationContext()), doHash, getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.IS_WALLET_ACCEPTED),
                getArguments().getString(PaymentConstant.PAYMENT_TYPE,""), mAmount.getText().toString());


        getNetworkManager().stringRequestPost(REQUEST_DOWALLET_PAYMENT, AppConstant.URL_PAY_FROM_DOWALLET
                , param, this, this, false);
    }

    private void showOtherPaymentOption() {
        if (getArguments() != null) {
            getPaymentOption();
        }
    }

    private void payFromRestaurantEarnings() {
        showLoader();
        Map<String, String> param = ApiParams.getPayFromPromocodeParam(getArguments().
                        getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, ""), DOPreferences.getDinerId(getActivity()
                        .getApplicationContext()), doHash, getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.IS_WALLET_ACCEPTED),
                getArguments().getString(PaymentConstant.PAYMENT_TYPE,""), restaurantEarningsAmt);


        getNetworkManager().stringRequestPost(REQUEST_DO_CODE_PAYMENT, AppConstant.URL_PROMO_CODE
                , param, this, this, false);


    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

        showLoader();
        Toast.makeText(getContext(), "Some error occured.Please try again", Toast.LENGTH_LONG).show();
        if (request.getIdentifier() == REQUEST_BILL_SUMMARY) {

            getActivity().getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onResponse(Request request, Object responseObject, Response response) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_BILL_SUMMARY && responseObject != null) {
            try {
                JSONObject resp = new JSONObject((String) responseObject);
                if (resp != null && resp.optBoolean("status")) {
                    JSONObject outputParam = resp.optJSONObject("output_params");
                    if (outputParam != null) {
                        JSONObject data = outputParam.optJSONObject("data");

                        mWallet.setText("" + (int) Math.abs(data.optDouble("do_wallet_amt")));
                        mAmount.setText("" + (int) Math.abs(data.optDouble("bill_amount")));
                        mTotal.setText("" + (int) Math.abs(data.optDouble("additional_amount")));
                        doHash = data.optString("do_hash");
                        mSelectPayment.setText(data.optString("button_text", getString(R.string.label_select_payment)));
                        mSelectPayment.setTag(data.optString("payment_method"));


                        if (!TextUtils.isEmpty(data.optString("restaurant_name")) && (!TextUtils.isEmpty(data.optString("rest_wallet_amt")))) {
                            if (!data.optString("rest_wallet_amt").equalsIgnoreCase("0")) {
                                restaurantEarning.setVisibility(View.VISIBLE);
                                restaurantEarningName.setText(data.optString("restaurant_name") + " Earnings");
                                restaurantAmt.setText(data.optString("rest_wallet_amt"));
                                restaurantEarningsAmt = data.optString("rest_wallet_amt");
                            } else {
                                restaurantEarning.setVisibility(View.GONE);
                            }

                        } else {
                            restaurantEarning.setVisibility(View.GONE);
                        }

                        int doWalletAmt = (int) Math.abs(data.optDouble("do_wallet_amt"));
                        if (doWalletAmt == 0) {
                            dineoutWallet.setVisibility(View.GONE);
                        } else {
                            dineoutWallet.setVisibility(View.VISIBLE);
                        }

                    }
                } else {
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                    //if(resp.optInt("error_type") == 566){
                        popBackStackImmediate(getActivity().getSupportFragmentManager());
                   // }
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
            }
        } else if (request.getIdentifier() == REQUEST_DOWALLET_PAYMENT && responseObject != null) {

            try {

                JSONObject resp = new JSONObject((String) responseObject);
                if (resp != null && resp.optBoolean("status")) {

                    Bundle bundle = new Bundle();
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID, resp.optJSONObject("output_params").optJSONObject("data").optString("trans_id"));
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, resp.optJSONObject("output_params").optJSONObject("data").optString("total_bill"));
                    if (resp.optJSONObject("output_params").optJSONObject("data").optInt("trans_status", 0) == 1)
                        bundle.putBoolean("payment.SUCCESSFUL", true);
                    else
                        bundle.putBoolean("payment.SUCCESSFUL", false);
                    showStatusScreen(bundle);
                } else {
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
            }
        } else if (request.getIdentifier() == GET_PAYMENT_OPTION && responseObject != null) {
            try {
                JSONObject resp = new JSONObject((String) responseObject);
                if (resp != null && resp.optBoolean("status")
                        && resp.optJSONObject("output_params") != null) {

                    Bundle bundle = getArguments();
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, mTotal.getText().toString());
                    bundle.putString(PaymentConstant.PAYMENT_TYPE, getArguments().getString(PaymentConstant.PAYMENT_TYPE,""));
                    com.dineout.book.fragment.payments.fragment.SelectPaymentFragment fragment = com.dineout.book.fragment.payments.fragment.SelectPaymentFragment.newInstance(bundle);
                    fragment.setApiResp(resp.optJSONObject("output_params").optJSONObject("data").toString());
                    addToBackStack(getActivity(), fragment);

                }else {
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
            }
        } else if (request.getIdentifier() == REQUEST_DO_CODE_PAYMENT && responseObject != null) {
            try {
                JSONObject resp = new JSONObject((String) responseObject);
                if (resp != null && resp.optBoolean("status")) {

                    Bundle bundle = new Bundle();
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID, resp.optJSONObject("output_params").optJSONObject("data").optString("trans_id"));

                    bundle.putString("payement.BOOKING_ID", resp.optJSONObject("output_params").optJSONObject("data").optString("b_id"));

                    bundle.putString(PaymentConstant.PAYMENT_TYPE, getArguments().getString(PaymentConstant.PAYMENT_TYPE,""));
                    if (resp.optJSONObject("output_params").optJSONObject("data").optInt("trans_status", 1) == 1)
                        bundle.putBoolean("payment.SUCCESSFUL", true);
                    else
                        bundle.putBoolean("payment.SUCCESSFUL", false);
                    showStatusScreen(bundle);

                } else {
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
            }
        }
    }


    private void getPaymentOption() {

        showLoader();


        Map<String, String> param = new HashMap<>();
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("booking_id", getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, ""));
        param.put("is_do_wallet_used", getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.IS_WALLET_ACCEPTED) ? "1" : "0");
        param.put("f", "1");

        getNetworkManager().stringRequestPost(GET_PAYMENT_OPTION, AppConstant.URL_PAYMENT_OPTION,
                param, this, this, true);
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("P_PaymentSummary",getString(R.string.d_back_click),
                getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
