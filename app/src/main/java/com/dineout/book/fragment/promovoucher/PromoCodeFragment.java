package com.dineout.book.fragment.promovoucher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.payments.fragment.PaymentBreakUpFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.fragment.SelectPaymentFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

public class PromoCodeFragment extends MasterDOStringReqFragment implements View.OnClickListener {

    public static final String AMOUNT_DATA = "bill_amount_data";
    public static final String WALLET_AMOUNT_DATA = "wallet_amount_data";
    private final int REQUEST_APPLY_CODE = 0x106;
    private final int REQUEST_REMOVE_CODE = 0x107;
    private final int REQUEST_BILL_SUMMARY = 0x109;
    private final int GET_PAYMENT_OPTION = 0x234;
    private View mInputContainer;
    private View mAppliedContainer, mRemoveContainer;
    private EditText mInputView;
    private AppCompatCheckBox mWalletOpted;
    private String mAmount;
    private TextView mPromoCodeText, mFinalAmount, mInitialValue, mPromoDiscount, mWalletAmount;

    public static PromoCodeFragment getInstance(Bundle bundle) {

        PromoCodeFragment fragment = new PromoCodeFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promo_code, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Promo Code");
        findViewByIDS();
        initializeView();
    }

    private void findViewByIDS() {

        mWalletOpted = (AppCompatCheckBox) getView().findViewById(R.id.wallet_opted_promocode);
        mInputView = (EditText) getView().findViewById(R.id.promo_code_input);
        mPromoCodeText = (TextView) getView().findViewById(R.id.promo_code_after_applied);
        mInputContainer = getView().findViewById(R.id.promo_input_container);
        mFinalAmount = (TextView) getView().findViewById(R.id.membership_fees);
        mAppliedContainer = getView().findViewById(R.id.applied_promo_detail_container);
        mInitialValue = (TextView) getView().findViewById(R.id.after_promo_value);
        mWalletAmount = (TextView) getView().findViewById(R.id.wallet);
        mPromoDiscount = (TextView) getView().findViewById(R.id.after_promo_dicsount);
        mRemoveContainer = getView().findViewById(R.id.promo_remove_container);
        getView().findViewById(R.id.promo_code_continue_btn).setOnClickListener(this);
        getView().findViewById(R.id.promo_code_apply).setOnClickListener(this);
        getView().findViewById(R.id.promo_code_remove).setOnClickListener(this);
        mInputView.requestFocus();

    }

    private void initializeView() {

        if (getArguments() == null)
            return;

        double mWalletAmt = Double.parseDouble(getArguments().getString(WALLET_AMOUNT_DATA, "0"));
        mFinalAmount.setText(String.format(getResources().getString(R.string.container_rupee),
                getArguments().getString(AMOUNT_DATA, "0")));
        mWalletAmount.setText(String.format(getResources().getString(R.string.container_rupee),
                getArguments().getString(WALLET_AMOUNT_DATA, "0")));

        if (mWalletAmt <= 0) {
            mWalletOpted.setEnabled(false);
        } else {
            mWalletOpted.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.promo_code_apply) {
            AppUtil.hideKeyboard(getActivity());

            String code = mInputView.getText().toString();
            if (!TextUtils.isEmpty(code)) {

                trackEventGA(getString(R.string.ga_screen_do_plus_membership_details),
                        getString(R.string.ga_action_apply_do_plus_promo_code), null);

                applyPromoCode(code);
            }
        } else if (v.getId() == R.id.promo_code_remove) {

            removePromoCode(mPromoCodeText.getText().toString());
        } else if (v.getId() == R.id.promo_code_continue_btn) {
            getBillingSummary();
        }
    }

    private void applyPromoCode(String code) {

        showLoader();
        getNetworkManager().stringRequestPost(REQUEST_APPLY_CODE, AppConstant.URL_PROMO,
                ApiParams.getDOPromoCodeParams("apply", code, "1", ApiParams.DOPLUS_TYPE, DOPreferences.getDinerId(getContext())), this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, false);
    }

    private void removePromoCode(String code) {

        showLoader();
        getNetworkManager().stringRequestPost(REQUEST_REMOVE_CODE, AppConstant.URL_PROMO,
                ApiParams.getDOPromoCodeParams("remove", code, "1", ApiParams.DOPLUS_TYPE, DOPreferences.getDinerId(getContext())), this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, false);
    }

    private void getBillingSummary() {
        showLoader();
        getNetworkManager().stringRequestPost(REQUEST_BILL_SUMMARY, AppConstant.URL_BILL_PAYMENT,
                ApiParams.getBillingSummaryParams(DOPreferences.getDinerId(getContext()), "1", ApiParams.DOPLUS_TYPE, mWalletOpted.isChecked()), this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, false);
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject != null) {

            try {
                JSONObject resp = new JSONObject(responseObject);
                if (resp.optBoolean("status")) {


                    JSONObject dataObject = resp.optJSONObject("output_params").optJSONObject("data");
                    if (request.getIdentifier() == REQUEST_APPLY_CODE)
                        handleAppliedResponse(dataObject);
                    else if (request.getIdentifier() == REQUEST_REMOVE_CODE)
                        handleRemoveResponse(dataObject);
                    else if (request.getIdentifier() == REQUEST_BILL_SUMMARY)
                        handleSummaryResponse(dataObject);
                    else if (request.getIdentifier() == GET_PAYMENT_OPTION)
                        handleOptionResponse(dataObject);
                } else {
                    UiUtil.showToastMessage(getContext(), resp.optString("error_msg", "Some error occured!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void handleOptionResponse(JSONObject data) {

        Bundle bundle = getArguments();
        bundle.putString(PaymentConstant.FINAL_AMOUNT, mAmount);
        bundle.putString(PaymentConstant.PAYMENT_TYPE, ApiParams.DOPLUS_TYPE);
        bundle.putString(PaymentConstant.BOOKING_ID, "1");
        bundle.putBoolean(PaymentConstant.IS_WALLET_ACCEPTED, getArguments().getBoolean(PaymentConstant.IS_WALLET_ACCEPTED));
        SelectPaymentFragment fragment = SelectPaymentFragment
                .newInstance(bundle);
        fragment.setApiResp(data.toString());
        addToBackStack(getActivity(), fragment);
    }

    private void handleAppliedResponse(JSONObject data) {

        if (data != null) {

            double amt = data.optDouble("amount_after_discount", 0);

            if (amt <= 0) {
                mWalletOpted.setChecked(false);
                mWalletOpted.setEnabled(false);
            }
            mFinalAmount.setText(String.format(getResources().getString(R.string.container_rupee),
                    data.optString("amount_after_discount", "0")));
            mInitialValue.setText(String.format(getResources().getString(R.string.container_rupee),
                    data.optString("amount_before_discount", "0")));
            mPromoCodeText.setText(data.optString("promocode", "0"));
            mPromoDiscount.setText("( " + data.optString("promo_text") + " )");
            mRemoveContainer.setVisibility(View.VISIBLE);
            mInputContainer.setVisibility(View.GONE);
            mAppliedContainer.setVisibility(View.VISIBLE);
        }
    }

    private void handleRemoveResponse(JSONObject data) {

        if (data != null) {

            mFinalAmount.setText(String.format(getResources().getString(R.string.container_rupee),
                    data.optString("amount_to_pay", "0")));
            mWalletOpted.setEnabled(true);

            mRemoveContainer.setVisibility(View.GONE);
            mInputContainer.setVisibility(View.VISIBLE);
            mAppliedContainer.setVisibility(View.GONE);
        }
    }

    private void handleSummaryResponse(JSONObject data) {
        if (data != null) {

            Bundle bundle = new Bundle();

            mAmount = data.optString("additional_amount");
            MasterDOFragment fragment = null;
            if (mWalletOpted.isChecked() || !TextUtils.isEmpty(mInputView.getText().toString().trim())) {

                fragment = PaymentBreakUpFragment.getFragment(bundle, data);
                bundle.putBoolean(PaymentConstant.IS_WALLET_ACCEPTED, mWalletOpted.isChecked());
                addToBackStack(getActivity(), fragment);

            } else {
                getPaymentOptions();

            }

        }
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


}
