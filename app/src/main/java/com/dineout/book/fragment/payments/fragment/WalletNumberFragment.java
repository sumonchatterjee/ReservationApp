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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WalletNumberFragment extends DOBaseFragment implements View.OnClickListener, Response.Listener<String>, TextWatcher {

    private final int LINK_WALLET_REQUEST = 0x25;
    private TextView mNumberHintError;
    private EditText mInput;
    private String screenName = "";



    public static WalletNumberFragment newInstance(Bundle bundle) {
        WalletNumberFragment fragment = new WalletNumberFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet_number, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInput = (EditText) view.findViewById(R.id.number_wallet);
        mNumberHintError = (TextView) view.findViewById(R.id.textViewNumberError);

        view.findViewById(R.id.buttonNumberValidate).setOnClickListener(this);
        ImageView brand = (ImageView) view.findViewById(R.id.imageViewWalletLogo);
        if (getArguments() != null) {
            changeBrandLogo(brand, getArguments().getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE));
            changeHintText(getArguments().getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Add Wallet");
    }

    @Override
    public void onRemoveErrorView() {

    }

    private void changeBrandLogo(ImageView view, int type) {

        //String screenName = "";

        switch (type){

            case PaymentConstant.MOBIKWIK:{
                view.setImageResource(R.drawable.img_mobikwik_title);
                screenName = "MobiKwikAddWallet";
                break;
            }
            case PaymentConstant.PAYTM:{
                view.setImageResource(R.drawable.img_paytm_title);
                screenName = "PaytmAddWallet";
                break;
            }

            case PaymentConstant.FREECHARGE:{
                view.setImageResource(R.drawable.img_fc_title);
                screenName = "FreeChargeAddWallet";
            }
        }

        trackScreenName("P_Payment_"+screenName);
    }

    private void linkWallet() {
        Map<String, String> param = new HashMap<>();
        param.put("diner_phone", mInput.getText().toString().trim());
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("amount", getArguments().getString(PaymentConstant.FINAL_AMOUNT, "0.0"));
        param.put("payment_type", getArguments().getInt(PaymentConstant.WALLET_TYPE) +"");
        param.put("f", "1");
        getNetworkManager().stringRequestPost(LINK_WALLET_REQUEST, AppConstant.URL_LINK_WALLET
                , param, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(Request request, VolleyError error) {

               hideLoader();
            }
        }, false);
    }

    @Override
    public void onClick(View v) {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("P_"+screenName,"EnterNumberType",
                mInput.getText().toString(),hMap);

        trackEventForCountlyAndGA("P_"+screenName,"RequestOPTClick",
                "RequestOPTClick",hMap);

        if (validateNumber()) {
            showLoader();
            linkWallet();
        }
    }

    private boolean validateNumber() {
        String number = mInput.getText().toString().trim();

        Matcher matcher = Pattern.compile("[7-9][0-9]{9}$").matcher(number);

        if (matcher.matches()) {
            return true;
        } else {
            mNumberHintError.setText("Invalid number!");
            mNumberHintError.setTextColor(getResources().getColor(R.color.red_CC2));
            return false;
        }

    }


    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == LINK_WALLET_REQUEST) {
            try {
                JSONObject resp = new JSONObject(responseObject);
                if (resp != null && resp.optBoolean("status")) {

                    Bundle bundle = getArguments();
                    bundle.putString(com.dineout.book.fragment.payments.PaymentConstant.WALLET_LINK_NUMBER, mInput.getText().toString().trim());
                    com.dineout.book.fragment.payments.fragment.WalletOTPFragment fragment = com.dineout.book.fragment.payments.fragment.WalletOTPFragment.newInstance(bundle);
                    addToBackStack(getActivity(), fragment);
                } else if(resp != null && !resp.optBoolean("status")) {
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
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        changeHintText(getArguments().getInt(com.dineout.book.fragment.payments.PaymentConstant.WALLET_TYPE));
    }

    private void changeHintText(int type) {

        mNumberHintError.setTextColor(getResources().getColor(R.color.grey_75));
        switch (type){

            case PaymentConstant.FREECHARGE:
                mNumberHintError.setText(String.format(getString(R.string.number_hint), "Freecharge"));

                break;
            case PaymentConstant.MOBIKWIK:
                mNumberHintError.setText(String.format(getString(R.string.number_hint), "MobiKwik"));
                break;
            case PaymentConstant.PAYTM:
                mNumberHintError.setText(String.format(getString(R.string.number_hint), "PayTm"));
                break;


        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }
}
