package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class DealsPaymentBreakupFragment extends MasterDOStringReqFragment implements View.OnClickListener {
    private final int REQUEST_DO_CODE_PAYMENT = 0x80;
    private final int REQUEST_DOWALLET_PAYMENT = 0x45;
    private final int GET_PAYMENT_OPTION = 0x02;
    private JSONObject mResp;
    private TextView billAmount;
    private LinearLayout mWalletContainer;
    private LinearLayout restaurantEarning;
    private TextView walletRestaurantName;
    private TextView walletAmount, mWalletFees;
    private Button mContinueBtn;
    private TextView finalAmount;
    private String mAmount;
    private String doHash;
    private TextView totalAmount;
    private String restaurantId;
    private String paymentType;

    public static DealsPaymentBreakupFragment getFragment(Bundle bundle, JSONObject data) {

        if (bundle == null)
            bundle = new Bundle();
        DealsPaymentBreakupFragment fragment = new DealsPaymentBreakupFragment();
        fragment.setArguments(bundle);
        fragment.mResp = data;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_breakup, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.title_payment_summary));

        Bundle bundle=getArguments();
        if(bundle!=null) {
            restaurantId = bundle.getString(PaymentConstant.RESTAURANT_ID);
            paymentType = bundle.getString(PaymentConstant.PAYMENT_TYPE);
        }
        if(paymentType.equalsIgnoreCase("deal")){
            trackScreenToGA("PayNowBillSummary_Deals");
        }else{
            trackScreenToGA("PayNowBillSummary_Events");
        }
        initializeViews();
        initializeData();
    }

    @Override
    public void onRemoveErrorView() {

    }

    private void initializeViews() {

        billAmount = (TextView) getView().findViewById(R.id.membership_fees);
        totalAmount = (TextView) getView().findViewById(R.id.amount_tobe_paid_breakup);

        //do wallet
        mWalletContainer = (LinearLayout) getView().findViewById(R.id.wallet_summary_container);
        mWalletFees = (TextView) getView().findViewById(R.id.amount_wallet_breakup);

        //restaurant earning
        restaurantEarning = (LinearLayout) getView().findViewById(R.id.coupon_summary_container);
        walletRestaurantName = (TextView) getView().findViewById(R.id.coupon_text);
        walletAmount = (TextView) getView().findViewById(R.id.amount_coupon);
        mContinueBtn = (Button) getView().findViewById(R.id.btn_payment_option_breakup);
        finalAmount = (TextView) getView().findViewById(R.id.amount_net_summary);
        mContinueBtn.setOnClickListener(this);


    }


    private void initializeData() {

        if (mResp != null) {

            JSONObject resp = mResp;

            String walletAmt = resp.optString("do_wallet_amt");

            totalAmount.setText(resp.optString("bill_amount"));

            if (!TextUtils.isEmpty(walletAmt)) {
                if (Double.parseDouble(walletAmt) > 0) {
                    mWalletContainer.setVisibility(View.VISIBLE);
                    mWalletFees.setText(walletAmt);
                } else {
                    mWalletContainer.setVisibility(View.GONE);
                }
            } else {
                mWalletContainer.setVisibility(View.GONE);
            }

            String restaurantEarnings = resp.optString("rest_wallet_amt");

            if (!TextUtils.isEmpty(restaurantEarnings)) {
                if (Double.parseDouble(restaurantEarnings) > 0) {
                    restaurantEarning.setVisibility(View.VISIBLE);
                    walletAmount.setText(restaurantEarnings);
                    walletRestaurantName.setText(resp.optString("restaurant_name") + " Earning");
                } else {
                    restaurantEarning.setVisibility(View.GONE);
                }
            } else {
                restaurantEarning.setVisibility(View.GONE);
            }

            doHash = resp.optString("do_hash");
            mAmount = resp.optString("additional_amount");
            finalAmount.setText(mAmount);
            billAmount.setText(String.format(getContext().getString(com.dineout.recycleradapters.R.string.container_rupee), mAmount) + "/-");


            mContinueBtn.setText(resp.optString("button_text"));
            mContinueBtn.setTag(resp.optString("payment_method"));

        }
    }

    @Override
    public void onClick(View v) {
        String type = (String) v.getTag();
        if (type.equalsIgnoreCase("pg")) {

            //track event

            if(paymentType.equalsIgnoreCase("deal")) {
                trackEventGA("PayNowBillSummary_Deals", getString(com.dineout.book.R.string.ga_action_payment_gateway_used), restaurantId);
            }else{
                trackEventGA("PayNowBillSummary_Events", getString(com.dineout.book.R.string.ga_action_payment_gateway_used), restaurantId);
            }

            getPaymentOptions();


        } else if (type.equalsIgnoreCase("dw")) {

            //track event

            if(paymentType.equalsIgnoreCase("deal")) {
                trackEventGA("PayNowBillSummary_Deals", getString(com.dineout.book.R.string.ga_action_dineout_wallet_used), restaurantId);
            }else{
                trackEventGA("PayNowBillSummary_Events", getString(com.dineout.book.R.string.ga_action_dineout_wallet_used), restaurantId);
            }
            //amount to be paid from do wallet
            makePaymentFromDOWallet(mResp.optString("do_wallet_amt"));


        } else if (type.equalsIgnoreCase("pc")) {
            //track event

            if(paymentType.equalsIgnoreCase("deal")) {
                trackEventGA("PayNowBillSummary_Deals", getString(com.dineout.book.R.string.ga_action_ringfencing_used), restaurantId);
            }else{
                trackEventGA("PayNowBillSummary_Events", getString(com.dineout.book.R.string.ga_action_ringfencing_used), restaurantId);
            }

            //amount to be paid through promocode
            paymentThroughPromoCode(mResp.optString("rest_wallet_amt"));
        }
    }


    private void paymentThroughPromoCode(String amtToPay) {
        showLoader();
        Map<String, String> param = ApiParams.getDealsPayFromPromoCode(amtToPay, doHash, paymentType);

        getNetworkManager().stringRequestPost(REQUEST_DO_CODE_PAYMENT, AppConstant.URL_PROMO_CODE
                , param, this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                    }
                }, false);


    }


    private void makePaymentFromDOWallet(String amtToPay) {
        showLoader();
        Map<String, String> param = ApiParams.getDealPayFromWalletParam(doHash,
                paymentType, amtToPay);

        getNetworkManager().stringRequestPost(REQUEST_DOWALLET_PAYMENT, AppConstant.URL_PAY_FROM_DOWALLET
                , param, this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                    }
                }, false);
    }


    private void getPaymentOptions() {

        showLoader();
        getNetworkManager().stringRequestPost(GET_PAYMENT_OPTION, AppConstant.URL_PAYMENT_OPTION,
                ApiParams.getDOPaymentOptions(DOPreferences.getDinerId(getContext())), this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                    }
                }, false);
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        try {
            JSONObject resp = new JSONObject(responseObject);
            if (resp != null && resp.optBoolean("status")
                    && resp.optJSONObject("output_params") != null) {
                if (request.getIdentifier() == GET_PAYMENT_OPTION) {
                    Bundle bundle = getArguments();
                    bundle.putString(PaymentConstant.FINAL_AMOUNT, mAmount);
                    bundle.putString(PaymentConstant.PAYMENT_TYPE, paymentType);

                    SelectPaymentFragment fragment = SelectPaymentFragment

                            .newInstance(bundle);
                    fragment.setApiResp(resp.optJSONObject("output_params").optJSONObject("data").toString());
                    addToBackStack(getActivity(), fragment);

                } else if (request.getIdentifier() == REQUEST_DOWALLET_PAYMENT
                        || request.getIdentifier() == REQUEST_DO_CODE_PAYMENT) {

                    Bundle bundle = new Bundle();
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID, resp.optJSONObject("output_params").optJSONObject("data").optString("trans_id"));
                    //bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, resp.optJSONObject("output_params").optJSONObject("data").optString("total_bill"));
                    bundle.putString("payement.BOOKING_ID", resp.optJSONObject("output_params").optJSONObject("data").optString("b_id"));
                    bundle.putString(PaymentConstant.PAYMENT_TYPE, paymentType);
                    if (resp.optJSONObject("output_params").optJSONObject("data").optInt("trans_status", 1) == 1)
                        bundle.putBoolean("payment.SUCCESSFUL", true);
                    else
                        bundle.putBoolean("payment.SUCCESSFUL", false);
                    showStatusScreen(bundle);
                }
            } else {
                Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                        "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
        }


    }


    public void showStatusScreen(Bundle bundle) {
        if (getActivity() != null) {

            bundle.putString(PaymentConstant.BOOKING_ID, getArguments().getString
                    (PaymentConstant.BOOKING_ID));
            MasterDOFragment fragment = PaymentStatusFragment.newInstance(bundle);
            popToHome(getActivity());
            addToBackStack(getActivity(), fragment);


        }

    }
}
