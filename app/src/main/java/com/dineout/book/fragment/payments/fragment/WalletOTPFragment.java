package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class WalletOTPFragment extends DOBaseFragment implements Response.Listener<String>, View.OnClickListener {

    private final int VERIFY_WALLET_CODE = 0x34;
    private final int OTP_REQUEST_CODE = 0x56;
    private final int WALLET_SUMMARY_REQUEST = 0x40;
    private TextView mWalletTimer;
    private EditText mOTPInput;
    private TextView mWalletLinkError;
    private int second = 25;
    private String screenName="";
    private android.os.Handler mTimerHandler = new android.os.Handler();
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {

            if (second <= 0) {

                mWalletTimer.setText("Resend OTP");
                mWalletTimer.setOnClickListener(WalletOTPFragment.this);
                mWalletTimer.setTextColor(getResources().getColor(R.color.blue));
            } else {
                mWalletTimer.setText(String.format(getString(R.string.otp_request_hint), second + "sec"));
                mWalletTimer.setTextColor(getResources().getColor(R.color.grey_75));
                mWalletTimer.setOnClickListener(null);
                second--;
                mTimerHandler.postDelayed(this, 1000);
            }
        }
    };

    public static WalletOTPFragment newInstance(Bundle bundle) {
        WalletOTPFragment fragment = new WalletOTPFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet_enter_otp, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        setToolbarTitle("Add Wallet");
        mWalletTimer = (TextView) view.findViewById(R.id.textViewOTPHintResent);
        mWalletLinkError = (TextView) view.findViewById(R.id.textViewOTPError);
        mOTPInput = (EditText) view.findViewById(R.id.otp_wallet);
        ImageView brand = (ImageView) view.findViewById(R.id.imageViewWalletLogo);
        view.findViewById(R.id.buttonOTPValidate).setOnClickListener(this);
        if (getArguments() != null) {
            changeBrandLogo(brand, getArguments().
                    getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE));
            getWalletName(getArguments().
                    getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE));
        }


//        mOTPInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
//                trackEventForCountlyAndGA("P_"+screenName,"OTPType",
//                        editable.toString(),hMap);
//            }
//        });

    }


    private void getWalletName(int type){
        switch (type){

            case PaymentConstant.MOBIKWIK:{
                screenName = "MobiKwikAddWallet";
                break;
            }
            case PaymentConstant.PAYTM:{
                screenName = "PaytmAddWallet";
                break;
            }

            case PaymentConstant.FREECHARGE:{
                screenName = "FreeChargeAddWallet";
            }
        }



    }

    private void changeBrandLogo(ImageView view, int type) {

        if (type == PaymentConstant.PAYTM) {
            view.setImageResource(R.drawable.img_paytm_title);

        } else if(type == PaymentConstant.MOBIKWIK) {
            view.setImageResource(R.drawable.img_mobikwik_title);
        }
        else if(type == PaymentConstant.FREECHARGE) {
            view.setImageResource(R.drawable.img_fc_title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimerHandler.post(mTimerRunnable);

    }

    @Override
    public void onRemoveErrorView() {

    }

    @Override
    public void onPause() {
        super.onPause();
        mTimerHandler.removeCallbacks(mTimerRunnable);
    }

    private void verifyOTP() {
        showLoader();

        trackEventGA("", "VerifyOTP", "");
        Map<String, String> param = new HashMap<>();
        param.put("otp", mOTPInput.getText().toString().trim());
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("amount", getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0.0"));
        param.put("payment_type", getArguments().getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE) +"");
        param.put("f", "1");
        getNetworkManager().stringRequestPost(VERIFY_WALLET_CODE, AppConstant.URL_VERIFY_WALLET
                , param, this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                    }
                }, false);
    }

    private void resendOTP() {

        showLoader();
        trackEventGA("", "resendOTP", "");
        Map<String, String> param = new HashMap<>();
        param.put("diner_phone", getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.WALLET_LINK_NUMBER));
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("amount", getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0.0"));
        param.put("payment_type", getArguments().getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE) +"");
        param.put("f", "1");
        getNetworkManager().stringRequestPost(OTP_REQUEST_CODE, AppConstant.URL_LINK_WALLET
                , param, this, new Response.ErrorListener() {
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
                if (resp != null && resp.optBoolean("status")) {

                    if (request.getIdentifier() == VERIFY_WALLET_CODE) {
                    trackScreenToGA(getArguments().getInt(PaymentConstant.WALLET_TYPE)  == PaymentConstant.PAYTM? "PaytmLinked" : (
                            getArguments().getInt(PaymentConstant.WALLET_TYPE)  == PaymentConstant.MOBIKWIK ? "MobiKwikLinked": "FreeChargeLinked"));
                        callWalletSummary(getArguments().getInt(PaymentConstant.WALLET_TYPE));
                    }
                    else if(request.getIdentifier() == WALLET_SUMMARY_REQUEST) {
                        JSONObject data = resp.optJSONObject("output_params").optJSONObject("data");
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
//                    com.dineout.book.fragment.payments.fragment.WalletSummaryFragment fragment = com.dineout.book.fragment.payments.fragment.WalletSummaryFragment.newInstance(getArguments());
//                    addToBackStack(getActivity(), fragment);
                } else if (resp != null && !resp.optBoolean("status")) {
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error occured.Please try again" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();

                } else if (resp == null) {
                    Toast.makeText(getContext(),
                            "Some error occured.Please try again", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(),
                        "Some error occured.Please try again", Toast.LENGTH_LONG).show();
            }

        if (request.getIdentifier() == OTP_REQUEST_CODE) {

            second = 25;
            mTimerHandler.post(mTimerRunnable);
        }

    }

    private void callWalletSummary(int type){

        showLoader();
        Map<String, String> param = new HashMap<>();
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("payment_type",type +"");

        param.put("obj_type", getArguments().getString(PaymentConstant.PAYMENT_TYPE, "booking"));
        if (!getArguments().getString(PaymentConstant.PAYMENT_TYPE).equalsIgnoreCase("deal")) {

            param.put("obj_id", getArguments().getString(PaymentConstant.BOOKING_ID, "0"));
        }
        param.put("is_do_wallet_used", getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED) ? "1" : "0");
        param.put("f", "1");

        getNetworkManager().stringRequestPost(WALLET_SUMMARY_REQUEST, AppConstant.URL_WALLET_SUMMARY
                , param, this, this, false);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.textViewOTPHintResent) {
            resendOTP();

        } else if (v.getId() == R.id.buttonOTPValidate) {

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("P_"+screenName,"OTPType",
                    mOTPInput.getText().toString(),hMap);

            trackEventForCountlyAndGA("P_"+screenName,"VerifyOTPClick",
                    "VerifyOTPClick",hMap);
            verifyOTP();
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }
}
