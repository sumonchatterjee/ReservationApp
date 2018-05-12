package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.Map;

public class PaymentBreakUpFragment extends DOBaseFragment
        implements Response.Listener<String>, View.OnClickListener {

    private final int GET_PAYMENT_OPTION = 0x02;
    private final int REQUEST_DOWALLET_PAYMENT = 0x45;
    private final int REQUEST_DO_CODE_PAYMENT = 0x50;
    private View mWalletContainer, mCouponContainer;
    private TextView mMembershipFees, mWalletFees, mCouponAmount, mFinalAmount, mCouponDetail;
    private String mAmount, doHash;
    private Button mContinueBtn;
    private JSONObject mResp;

    public static PaymentBreakUpFragment getFragment(Bundle bundle, JSONObject data) {

        if (bundle == null)
            bundle = new Bundle();
        PaymentBreakUpFragment fragment = new PaymentBreakUpFragment();
        fragment.setArguments(bundle);
        fragment.mResp = data;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promo_breakup_summary, container, false);
    }

    private void findViewByIds() {
        mWalletContainer = getView().findViewById(R.id.wallet_summary_container);
        mCouponContainer = getView().findViewById(R.id.coupon_summary_container);
        mMembershipFees = (TextView) getView().findViewById(R.id.amount_tobe_paid_breakup);
        mWalletFees = (TextView) getView().findViewById(R.id.amount_wallet_breakup);
        mCouponAmount = (TextView) getView().findViewById(R.id.amount_coupon);
        mCouponDetail = (TextView) getView().findViewById(R.id.coupon_text);
        mFinalAmount = (TextView) getView().findViewById(R.id.amount_net_summary);
        mContinueBtn = (Button) getView().findViewById(R.id.btn_payment_option_breakup);
        mContinueBtn.setOnClickListener(this);
    }

    private void initalizeView() {
        if (mResp != null) {

            JSONObject resp = mResp;

            double walletAmt = resp.optDouble("do_wallet_amt", 0);
            if (walletAmt > 0) {
                mWalletContainer.setVisibility(View.VISIBLE);
                mWalletFees.setText((int) walletAmt + "");
            } else {
                mWalletContainer.setVisibility(View.GONE);
            }

            double couponAmt = resp.optDouble("promo_discount", 0);
            if (couponAmt > 0) {
                mCouponContainer.setVisibility(View.VISIBLE);
                mCouponDetail.setText("Coupon ( " + resp.optString("promo_text") + " )");
                mCouponAmount.setText((int) couponAmt + "");
            } else {
                mCouponContainer.setVisibility(View.GONE);
            }

            doHash = resp.optString("do_hash");
            mAmount = resp.optString("additional_amount");
            mFinalAmount.setText(mAmount);
            mMembershipFees.setText(resp.optString("bill_amount"));
            mContinueBtn.setText(resp.optString("button_text"));
            mContinueBtn.setTag(resp.optString("payment_method"));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle(getString(R.string.title_payment_summary));

        findViewByIds();

        initalizeView();
    }

    @Override
    public void onRemoveErrorView() {

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
                    bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.DOPLUS_TYPE);
                    bundle.putString(PaymentConstant.BOOKING_ID, "1");
                    bundle.putBoolean(PaymentConstant.IS_WALLET_ACCEPTED, getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED));
                    SelectPaymentFragment fragment = SelectPaymentFragment

                            .newInstance(bundle);
                    fragment.setApiResp(resp.optJSONObject("output_params").optJSONObject("data").toString());
                    addToBackStack(getActivity(), fragment);
                } else if (request.getIdentifier() == REQUEST_DOWALLET_PAYMENT
                        || request.getIdentifier() == REQUEST_DO_CODE_PAYMENT) {

                    Bundle bundle = new Bundle();
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID, resp.optJSONObject("output_params").optJSONObject("data").optString("trans_id"));
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, resp.optJSONObject("output_params").optJSONObject("data").optString("total_bill"));
                    bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.DOPLUS_TYPE);
                    if (resp.optJSONObject("output_params").optJSONObject("data").optInt("trans_status", 1) == 1)
                        bundle.putBoolean("payment.SUCCESSFUL", true);
                    else
                        bundle.putBoolean("payment.SUCCESSFUL", false);
                    showStatusScreen(bundle);
                }
            }else if(resp != null && !resp.optBoolean("status")){
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error occured.Please try again" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();

            }else if(resp == null){
                Toast.makeText(getContext(),
                        "Some error occured.Please try again" , Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "Some error occured.Please try again" , Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        String type = (String) v.getTag();

        if (type.equalsIgnoreCase("pg")) {

            trackEventGA(getString(R.string.ga_screen_do_plus_membership_details),
                    getString(R.string.ga_action_select_payment_method), null);

            getPaymentOptions();

        } else if (type.equalsIgnoreCase("dw")) {

            trackEventGA(getString(R.string.ga_screen_do_plus_membership_details),
                    getString(R.string.ga_action_continue),
                    getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED) ? "Y" : "N");


            makePaymentFromDOWallet();
        } else if (type.equalsIgnoreCase("pc")) {

            paymentThroughPromoCode();
        }
    }

    private void paymentThroughPromoCode() {

        showLoader();
        Map<String, String> param = ApiParams.getPayFromPromoCode("1", DOPreferences.getDinerId(getActivity()
                .getApplicationContext()), doHash, ApiParams.DOPLUS_TYPE);


        getNetworkManager().stringRequestPost(REQUEST_DO_CODE_PAYMENT, AppConstant.URL_PROMO_CODE
                , param, this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                    }
                }, false);
    }

    private void makePaymentFromDOWallet() {
        showLoader();
        Map<String, String> param = ApiParams.getPayFromWalletParam("1", DOPreferences.getDinerId(getActivity()
                        .getApplicationContext()), doHash, getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED),
                ApiParams.DOPLUS_TYPE, mAmount);


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
    public void onErrorResponse(Request request, VolleyError error) {

    }
}
