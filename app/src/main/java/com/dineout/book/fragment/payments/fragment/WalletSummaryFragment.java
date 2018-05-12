package com.dineout.book.fragment.payments.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.util.PaymentUtils;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.mobikwik.sdk.MobikwikSDK;
import com.mobikwik.sdk.lib.MKTransactionResponse;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONObject;

import java.util.HashMap;


public class WalletSummaryFragment extends DOBaseFragment implements Response.Listener<String>, View.OnClickListener {

    private final int WALLET_INITIALIZATION = 0x10;
    private final int WALLET_SUMMARY_REQUEST = 0x34;
    private TextView mAssociatedNumber;
    private TextView mWalletAmt;
    private TextView mToBePaid;
    private TextView mDOWalletAmt;
    private TextView mBillAmt;
    private View mDOWalletDivider, mEarningDivider;
    private String doHash;
    private String infoKYC;
    private Button mPayNow;
    private TextView restName;
    private TextView restWalletAmount;
    private LinearLayout mainContainer;
    private JSONObject mSummaryResponse;
    private PaytmPaymentTransactionCallback mCallback = new PaytmPaymentTransactionCallback() {
        @Override
        public void onTransactionSuccess(Bundle bundle) {


            System.out.println(bundle.toString());
            final Bundle statusBundle = getArguments();
            statusBundle.putBoolean(PaymentConstant.PAYMENT_STATUS, true);
            statusBundle.putString(PaymentConstant.BOOKING_AMT, bundle.getString("TXNAMOUNT", "0.0"));
            statusBundle.putString(PaymentConstant.DISPLAY_ID, bundle.getString("ORDERID", "0.0"));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showStatusScreen(statusBundle);
                }
            }, 100);

//            showStatusScreen(statusBundle);


        }

        @Override
        public void onTransactionFailure(String s, Bundle bundle) {
            //            ORDER_ID TXN_AMOUNT STATUS RESPCODE
            System.out.println(bundle.toString());

            String status = bundle.getString("RESPMSG");

            if (!TextUtils.isEmpty(status)) {

                if (status.toLowerCase().contains("cancel")) {
                    return;
                }

                String orderID = !bundle.getString("ORDERID", "0").equalsIgnoreCase("0") ?
                        bundle.getString("ORDERID") : bundle.getString("ORDER_ID", "0");

                if (!orderID.equalsIgnoreCase("0")) {
                    final Bundle statusBundle = getArguments();
                    statusBundle.putBoolean(com.dineout.book.fragment.payments.PaymentConstant.PAYMENT_STATUS, false);
                    statusBundle.putString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_AMT, bundle.getString("TXN_AMOUNT", "0.0"));
                    statusBundle.putString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID, orderID);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showStatusScreen(statusBundle);
                        }
                    }, 100);
                }
            }
        }


        @Override
        public void networkNotAvailable() {

            Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void clientAuthenticationFailed(String s) {
            Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void someUIErrorOccurred(String s) {

            Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onErrorLoadingWebPage(int i, String s, String s1) {

            Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onBackPressedCancelTransaction() {
            Toast.makeText(getActivity(), "Transaction couldn't be completed due to some error,Try again", Toast.LENGTH_LONG).show();
        }
    };

    public static WalletSummaryFragment newInstance(Bundle bundle,JSONObject data) {
        WalletSummaryFragment fragment = new WalletSummaryFragment();
        fragment.mSummaryResponse =data;
        fragment.setArguments(bundle);
        return fragment;
    }

    private void changeBrandLogo(ImageView view, int type, TextView name) {
        if (type == PaymentConstant.PAYTM) {
            trackScreenToGA("WalletSummaryPaytm");
            view.setImageResource(R.drawable.img_paytm_title);
            name.setText("PayTm");
            setToolbarTitle("PayTm Wallet");
            infoKYC = String.format(getString(R.string.kyc_info), "PayTm");
        } else if(type == PaymentConstant.MOBIKWIK) {
            trackScreenToGA("WalletSummaryMobiKwik");
            view.setImageResource(R.drawable.img_mobikwik_title);
            name.setText("MobiKwik");
            setToolbarTitle("MobiKwik Wallet");
            infoKYC = String.format(getString(R.string.kyc_info), "MobiKwik");

        }else if(type == PaymentConstant.FREECHARGE){

            trackScreenToGA("WalletSummaryFreeCharge");
            view.setImageResource(R.drawable.img_fc_title);
            name.setText("FreeCharge");
            setToolbarTitle("FreeCharge Wallet");
            infoKYC = String.format(getString(R.string.kyc_info), "FreeCharge");
        }

    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_summary, container, false);
    }




    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        mDOWalletDivider = view.findViewById(R.id.dineout_wallet_divider);
        mBillAmt = (TextView) view.findViewById(R.id.bill_amt);
        ImageView brand = (ImageView) view.findViewById(R.id.imageViewLogo);
        TextView name = (TextView) view.findViewById(R.id.wallet_name);
        mAssociatedNumber = (TextView) view.findViewById(R.id.textViewNumberInfo);
        mWalletAmt = (TextView) view.findViewById(R.id.wallet_amt);
        mToBePaid = (TextView) view.findViewById(R.id.amt_to_be_paid);
        mDOWalletAmt = (TextView) view.findViewById(R.id.do_wallet_amt);
        restName = (TextView) view.findViewById(R.id.rest_name);
        restWalletAmount = (TextView) view.findViewById(R.id.rest_wallet_amt);
        mEarningDivider = view.findViewById(R.id.earning_divider);
        mainContainer = (LinearLayout) view.findViewById(R.id.main_container_payment_summary);
        (mPayNow = (Button) view.findViewById(R.id.confirm_payment)).setOnClickListener(this);

        if (getArguments() != null) {
            changeBrandLogo(brand, getArguments().getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE), name);
            handleSummaryResponse(mSummaryResponse);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MOBIQKWIK_REQUEST_CODE) {
            mainContainer.setVisibility(View.VISIBLE);
            MKTransactionResponse response = (MKTransactionResponse) data.getSerializableExtra(MobikwikSDK.EXTRA_TRANSACTION_RESPONSE);

            if (response.statusCode.equalsIgnoreCase("43")) {
                return;
            }

            Bundle bundle = getArguments();
            bundle.putBoolean(PaymentConstant.PAYMENT_STATUS, response.statusCode.equalsIgnoreCase("0") ? true : false);
            bundle.putString(PaymentConstant.DISPLAY_ID, response.orderId);
            bundle.putString(PaymentConstant.BOOKING_AMT, response.amount);
            showStatusScreen(bundle);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onRemoveErrorView() {

    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == WALLET_INITIALIZATION) {
            handleWalletInitializationResponse(responseObject);
        }
    }

    private void handleSummaryResponse(JSONObject data) {

        try {
            mAssociatedNumber.setText(data.optString("phone_number"));
            mBillAmt.setText(String.format(getString(R.string.container_rupee), data.optString("bill_amount")));
            mWalletAmt.setText(String.format(getString(R.string.container_rupee), data.optString("wallet_amount", "0.0")));
            if (getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.IS_WALLET_ACCEPTED))
                mDOWalletAmt.setText(String.format(getString(R.string.container_rupee), data.optString("do_wallet_amt", "0.0")));
            else {
                ((LinearLayout) mDOWalletAmt.getParent()).setVisibility(View.GONE);
                mDOWalletDivider.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(data.optString("restaurant_name")) && !TextUtils.isEmpty(data.optString("rest_wallet_amt"))) {

                restName.setText(data.optString("restaurant_name"));
                restWalletAmount.setText(data.optString("rest_wallet_amt"));
                ((LinearLayout) restName.getParent()).setVisibility(View.VISIBLE);
                mEarningDivider.setVisibility(View.VISIBLE);
            } else {
                ((LinearLayout) restName.getParent()).setVisibility(View.GONE);
                mEarningDivider.setVisibility(View.GONE);
            }


            doHash = data.optString("do_hash", "");
            mToBePaid.setText(String.format(getString(R.string.container_rupee), data.optString("additional_amount", "0.0")));


            mPayNow.setText( data.optString("action", "Pay Now"));
            mPayNow.setTag(data.optInt("action_id",-1));
            mainContainer.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        int action = (int)v.getTag();
        String paymentType="";
        if(action == -1)
            return;
        int type = getArguments().getInt(PaymentConstant.WALLET_TYPE) ;

        if(type==1){
            paymentType="paytm_wallet";
        }else if(type==2){
            paymentType="mobikwik";
        }else if(type==4){
            paymentType="freecharge";
        }else if(type==5){
            paymentType="phonepe";
        }

        int amount = (int) Double.parseDouble(getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0"));
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

//        AnalyticsHelper.getAnalyticsHelper(getActivity())
//                .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_paytm_wallet),
//                        getString(com.dineout.book.R.string.ga_action_pay_now), "amount" + amount + "");

        trackEventForCountlyAndGA("P_"+paymentType,"AddMoneyClick-"+paymentType,amount + "",hMap);

        if (amount <= 10000){

            if(action == 1)
            initializeWallet(type, DOPreferences.getDinerId(getActivity().getApplicationContext()),
                    getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, ""));
        }
        else
            showConfirmWalletLimitDialog();
    }

    private void initializeWallet(int type, String dinerId, String bookingId) {

        showLoader();


        getNetworkManager().stringRequestPost(WALLET_INITIALIZATION,
                AppConstant.URL_INIT_PAYMENT, ApiParams.getInitPaymentParams(dinerId, bookingId,
                        getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED),
                        getArguments().getString(PaymentConstant.PAYMENT_TYPE), type + ""),
                this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                    }
                }, false);

    }

    private void handleWalletInitializationResponse(Object responseObject) {
        try {
            if (responseObject != null) {
                JSONObject resp = new JSONObject((String) responseObject);
                if (resp != null && resp.optBoolean("status")) {
                    JSONObject outputParam = resp.optJSONObject("output_params");
                    if (outputParam != null) {
                        JSONObject data = outputParam.optJSONObject("data");
                        if (getArguments().getInt(PaymentConstant.WALLET_TYPE) == ApiParams.PAYMENT_TYPE_MOBIKWIK) {

                            // Track Event
                            AnalyticsHelper.getAnalyticsHelper(getActivity())
                                    .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_mobikwik),
                                            getString(com.dineout.book.R.string.ga_action_pay_now), null);


                            PaymentUtils.paymentThroughMobikwik(this, data, getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0"));

                        } else  if(getArguments().getInt(PaymentConstant.WALLET_TYPE) == ApiParams.PAYMENT_TYPE_PAYTM){

                            // Track Event
                            AnalyticsHelper.getAnalyticsHelper(getActivity())
                                    .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_paytm_wallet),
                                            getString(com.dineout.book.R.string.ga_action_pay_now), null);


                            PaymentUtils.paymentThroughPayTm(this, data, mCallback);
                        }else{

                            final Bundle statusBundle = getArguments();
                            statusBundle.putBoolean(PaymentConstant.PAYMENT_STATUS, data.optInt("trans_status") == 1 ? true:false);
                            statusBundle.putString(PaymentConstant.BOOKING_AMT, data.optString("amt","0.0"));
                            statusBundle.putString(PaymentConstant.DISPLAY_ID, data.optString("trans_id"));
                            showStatusScreen(statusBundle);
                        }
                    }
                } else {

                    JSONObject apiResponseObject = resp.optJSONObject("apiResponse");
                    if (apiResponseObject != null) {
                        Toast.makeText(getContext(), apiResponseObject.optString("statusdescription"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), resp.optString(AppConstant.ERROR_MESSAGE), Toast.LENGTH_LONG).show();

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showConfirmWalletLimitDialog() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        //Yes button clicked
                        int type = getArguments().getBoolean(PaymentConstant.WALLET_TYPE) ?
                                ApiParams.PAYMENT_TYPE_PAYTM : ApiParams.PAYMENT_TYPE_MOBIKWIK;
                        initializeWallet(type, DOPreferences.getDinerId(getActivity().getApplicationContext()),
                                getArguments().getString(PaymentConstant.BOOKING_ID, ""));
                    }
                    break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
                dialog.dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Info");
        builder.setMessage(infoKYC).setPositiveButton("Proceed", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }


    @Override
    public void onErrorResponse(Request request, VolleyError error) {

        hideLoader();
    }
}
