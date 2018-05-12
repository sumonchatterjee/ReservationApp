package com.dineout.book.fragment.payments.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dineout.book.R;
import com.dineout.book.util.CreditCardUtil;
import com.dineout.book.util.PaymentUtils;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;

public class CreditCardFragment extends PayUBaseFragment implements View.OnClickListener, TextWatcher {

//

    private Button payNowButton;
    private EditText cardNameEditText;
    private EditText cardNumberEditText;
    private EditText cardCvvEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private Bundle bundle;
    private CheckBox saveCardCheckBox;
    private String issuer;
    private Drawable issuerDrawable;
    private PayuUtils payuUtils;
    private String mCardNumber;
    private String mCardCVV;
    private TextWatcher mCardWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            if (charSequence.length() > 5) { // to confirm rupay card we need min 6 digit.
                if (null == issuer) issuer = payuUtils.getIssuer(charSequence.toString());
                if (issuer != null && issuer.length() > 1 && issuerDrawable == null) {
                    issuerDrawable = getIssuerDrawable(issuer);

                }
            } else {
                issuer = null;
                issuerDrawable = null;
                mCardNumber = null;
            }
            cardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, issuerDrawable, null);
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (payuUtils.validateCardNumber(s.toString())) {
                mCardNumber = s.toString();

            } else {
                mCardNumber = null;
            }
            validateAndEnable();

        }
    };

    private TextWatcher mExpiryWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            int month = TextUtils.isEmpty(cardExpiryMonthEditText.getText().toString().trim()) ? 0 : Integer.valueOf(cardExpiryMonthEditText.getText().toString());
            int year = TextUtils.isEmpty(cardExpiryYearEditText.getText().toString().trim()) ? 0000 : Integer.valueOf(cardExpiryYearEditText.getText().toString().trim());

            if (payuUtils.validateExpiry(month, year)) {
                cardExpiryMonthEditText.
                        setCompoundDrawablesWithIntrinsicBounds(null, null,
                                null, null);
                cardExpiryYearEditText.
                        setCompoundDrawablesWithIntrinsicBounds(null, null,
                                null, null);

            } else {

                cardExpiryMonthEditText.
                        setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getResources().getDrawable(R.drawable.ic_card_error), null);
                cardExpiryYearEditText.
                        setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getResources().getDrawable(R.drawable.ic_card_error), null);

            }


        }
    };

    public static CreditCardFragment newInstance(Bundle bundle) {
        CreditCardFragment fragment = new CreditCardFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenToGA("PayNowDebitCreditCards");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        (payNowButton = (Button) view.findViewById(R.id.button_make_payment)).setOnClickListener(this);
        payNowButton.setText(String.format(getString(R.string.pay_now_label),
                getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0")));

        cardNameEditText = (EditText) view.findViewById(R.id.name_card);
        cardNumberEditText = (EditText) view.findViewById(R.id.card_number);
        cardCvvEditText = (EditText) view.findViewById(R.id.card_cvv);
        cardExpiryMonthEditText = (EditText) view.findViewById(R.id.expiry_month);
        cardExpiryYearEditText = (EditText) view.findViewById(R.id.expiry_year);
        saveCardCheckBox = (CheckBox) view.findViewById(R.id.check_save_card);
        cardExpiryYearEditText.addTextChangedListener(mExpiryWatcher);
        cardExpiryMonthEditText.addTextChangedListener(mExpiryWatcher);
        cardNumberEditText.addTextChangedListener(mCardWatcher);
        cardCvvEditText.addTextChangedListener(this);
        payuUtils = new PayuUtils();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Debit/Credit Cards");
    }

    private Drawable getIssuerDrawable(String issuer) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            switch (issuer) {
                case PayuConstants.VISA:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.visa);
                case PayuConstants.LASER:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.laser);
                case PayuConstants.DISCOVER:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.discover);
                case PayuConstants.MAES:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.maestro);
                case PayuConstants.MAST:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.master);
                case PayuConstants.AMEX:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.amex);
                case PayuConstants.DINR:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.diner);
                case PayuConstants.JCB:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.jcb);
                case PayuConstants.SMAE:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.maestro);
                case PayuConstants.RUPAY:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.rupay);
            }
            return null;
        } else {

            switch (issuer) {
                case PayuConstants.VISA:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.visa, null);
                case PayuConstants.LASER:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.laser, null);
                case PayuConstants.DISCOVER:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.discover, null);
                case PayuConstants.MAES:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.maestro, null);
                case PayuConstants.MAST:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.master, null);
                case PayuConstants.AMEX:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.amex, null);
                case PayuConstants.DINR:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.diner, null);
                case PayuConstants.JCB:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.jcb, null);
                case PayuConstants.SMAE:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.maestro, null);
                case PayuConstants.RUPAY:
                    return getResources().getDrawable(com.payu.payuui.R.drawable.rupay, null);
            }
            return null;
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_make_payment) {
            makeCreditCardPayment();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (!TextUtils.isEmpty(cardNumberEditText.getText().toString().trim()) &&
                cardNumberEditText.getText().toString().trim().length() >= 4)
            cardCvvEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(payuUtils.getIssuer(CreditCardUtil
                    .cleanNumber(cardNumberEditText.getText().toString()))
                    .contentEquals(PayuConstants.AMEX) ? 4 : 3)});

        boolean cvvValid = !TextUtils.isEmpty(s.toString().trim()) && payuUtils.validateCvv(CreditCardUtil
                .cleanNumber(cardNumberEditText.getText().toString()), s.toString().trim());
        if (cvvValid) {
            mCardCVV = s.toString().trim();
            cardCvvEditText.
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                            null, null);
        } else if (!cvvValid && !TextUtils.isEmpty(s.toString().trim())) {
            cardCvvEditText.
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.drawable.ic_card_error), null);
        } else if (TextUtils.isEmpty(s.toString().trim())) {
            cardCvvEditText.
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                            null, null);

        }

    }

    private void makeCreditCardPayment() {
        if (validateField()) {
            PayuHashes mPayuHashes = PaymentUtils.getPayUHashes(getArguments().getString(PayuConstants.PAYU_HASHES));
            PaymentParams mPaymentParams = PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS));
            PayuConfig payuConfig = PaymentUtils.getPayUConfig(getArguments().getString(PayuConstants.PAYU_CONFIG));
//            payuConfig = null != payuConfig ? payuConfig : new PayuConfig();
            if (saveCardCheckBox.isChecked()) {
                mPaymentParams.setStoreCard(1);
            } else {
                mPaymentParams.setStoreCard(0);
            }
            // setup the hash
            mPaymentParams.setHash(mPayuHashes.getPaymentHash());
            mPaymentParams.setCardNumber(mCardNumber);
            mPaymentParams.setCardName(cardNameEditText.getText().toString());
            mPaymentParams.setNameOnCard(cardNameEditText.getText().toString());
            mPaymentParams.setExpiryMonth(cardExpiryMonthEditText.getText().toString());
            mPaymentParams.setExpiryYear(cardExpiryYearEditText.getText().toString());
            mPaymentParams.setCvv(mCardCVV);

            payThroughPayU(payuConfig, mPaymentParams, PayuConstants.CC);
        }
    }

    private boolean validateField() {


        int month = TextUtils.isEmpty(cardExpiryMonthEditText.getText().toString().trim()) ? 0 : Integer.valueOf(cardExpiryMonthEditText.getText().toString());
        int year = TextUtils.isEmpty(cardExpiryYearEditText.getText().toString().trim()) ? 0000 : Integer.valueOf(cardExpiryYearEditText.getText().toString().trim());
        if (!TextUtils.isEmpty(mCardNumber) && !TextUtils.isEmpty(mCardCVV) &&
                payuUtils.validateExpiry(month, year)) {

            return true;

        } else if (!TextUtils.isEmpty(mCardNumber)) {
            Toast.makeText(getActivity(), "Please provide valid card number", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!TextUtils.isEmpty(mCardCVV)) {

            Toast.makeText(getActivity(), "Please provide valid CVV", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!payuUtils.validateExpiry(month, year)) {
            Toast.makeText(getActivity(), "Please provide valid expiry month and year", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

    @Override
    public void afterTextChanged(Editable s) {
        validateAndEnable();
    }

    void validateAndEnable() {
        int month = TextUtils.isEmpty(cardExpiryMonthEditText.getText().toString().trim()) ? 0 : Integer.valueOf(cardExpiryMonthEditText.getText().toString());
        int year = TextUtils.isEmpty(cardExpiryYearEditText.getText().toString().trim()) ? 0000 : Integer.valueOf(cardExpiryYearEditText.getText().toString().trim());
        if (!TextUtils.isEmpty(mCardNumber) && !TextUtils.isEmpty(mCardCVV) &&
                payuUtils.validateExpiry(month, year)) {


            payNowButton.setEnabled(true);

        } else if (TextUtils.isEmpty(mCardNumber)) {

            cardNumberEditText.
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.drawable.ic_card_error), null);
            payNowButton.setEnabled(false);
        } else if (TextUtils.isEmpty(mCardCVV) && !TextUtils.isEmpty(mCardNumber) && payuUtils.validateExpiry(month, year)) {
            cardCvvEditText.
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.drawable.ic_card_error), null);
            payNowButton.setEnabled(false);
        } else if (!payuUtils.validateExpiry(month, year)) {
            payNowButton.setEnabled(false);
        }
    }


}
