package com.dineout.book.fragment.payments.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.dialogs.MoreDialog;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BillAmountFragment extends DOBaseFragment
        implements View.OnClickListener, TextWatcher,
        Response.Listener<String>, Response.ErrorListener {

    private final int REQUEST_BILL_AMOUNT = 0x01;
    private final int REFFERAL_CODE = 120;
    RelativeLayout resturantEarning;
    LinearLayout promocode;
    TextView restaurantName, restaurantEarning;
    boolean isPromoCodeApplied;
    AppCompatCheckBox doWAlletPromocode;
    private EditText mAmount, promocodeAmt;
    private Button mMoveToSummary;
    private TextView mWallet;
    private LinearLayout mWalletContainer;
    private double mWalletAmount = 0;
    private Button applyPromocode;
    private String restaurantId, restaurantNames, restaurantAmt;

    public static BillAmountFragment newInstance(Bundle bundle) {

        BillAmountFragment fragment = new BillAmountFragment();
        if (bundle != null)
            fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trackScreenName(getString(R.string.countly_payment_details));


        if (getArguments() != null) {
            setDOEarningAmount(Double.parseDouble(getArguments().getString(PaymentConstant.DO_WALLET_AMOUNT, "0")));


            restaurantAmt = getArguments().getString(PaymentConstant.RESTAURANT_AMT, "0");
            restaurantNames = getArguments().getString(PaymentConstant.DO_RESTAURANT_NAME, "0");
            setRestaurantAmount(restaurantAmt);
            setRestaurantName(restaurantNames);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bill_payment_module, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Bill Amount");
        AppUtil.hideKeyboard(getActivity());

        if (getArguments() != null)
            initializeView(getArguments());
    }

    private void initializeView(Bundle arguments) {
        double amt = 0.0;
        String bookingAmt = arguments.getString(PaymentConstant.BOOKING_AMT);
        if (!TextUtils.isEmpty(bookingAmt)) {
            amt = Double.parseDouble(bookingAmt);
        }


        if (amt > 0)
            mAmount.setText(arguments.getString(PaymentConstant.BOOKING_AMT, ""));


        mWallet.setText(getString(R.string.container_rupee,getDOEarningAmount()));


        initializeEarningSection();

        restaurantId = arguments.getString(PaymentConstant.RESTAURANT_ID, "");

        makeWalletContainerSelected();

        mAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                makeWalletContainerSelected();
            }
        });


    }


    private void initializeEarningSection() {


        if (restaurantAmt.equalsIgnoreCase("") && (restaurantNames.equalsIgnoreCase(""))) {
            resturantEarning.setVisibility(View.GONE);


        } else {
            restaurantName.setText(restaurantNames);
            restaurantEarning.setText(String.format(getContext().getString(com.dineout.recycleradapters.R.string.container_rupee), restaurantAmt));
            resturantEarning.setVisibility(View.VISIBLE);


        }


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWalletContainer = (LinearLayout) view.findViewById(R.id.wallet_selector_container);
        mAmount = (EditText) view.findViewById(R.id.amount);
        mMoveToSummary = (Button) view.findViewById(R.id.btn_view_summary);
        mWallet = (TextView) view.findViewById(R.id.wallet);
        TextView mRestaurantName = (TextView) view.findViewById(R.id.biller_name);
        TextView mBookingId = (TextView) view.findViewById(R.id.booking_id);
        doWAlletPromocode = (AppCompatCheckBox) view.findViewById(R.id.wallet_opted_promocode);

        resturantEarning = (RelativeLayout) view.findViewById(R.id.earnings_selector_container);
        promocode = (LinearLayout) view.findViewById(R.id.middle_container);

        promocodeAmt = (EditText) view.findViewById(R.id.promo_code_input);
        restaurantName = (TextView) view.findViewById(R.id.earnings_txt);
        restaurantEarning = (TextView) view.findViewById(R.id.earnings_amount);

        applyPromocode = (Button) view.findViewById(R.id.promo_code_apply);


        mAmount.addTextChangedListener(this);
        if (getArguments() != null) {
            mWallet.setText(getString(R.string.container_rupee, "0"));
            mRestaurantName.setText(getArguments().getString(PaymentConstant.RESTAURANT_NAME, ""));
            mBookingId.setText("Booking ID: " + getArguments().getString(PaymentConstant.DISPLAY_ID, ""));
            mBookingId.setVisibility(getArguments().getString(PaymentConstant.PAYMENT_TYPE).equalsIgnoreCase(ApiParams.BOOKING_TYPE)?View.VISIBLE:View.GONE
            );

        }

        String amount = mAmount.getText().toString();
        if (AppUtil.isStringEmpty(amount)) {
            mMoveToSummary.setEnabled(false);
        } else {
            mMoveToSummary.setEnabled(true);
        }

        mMoveToSummary.setOnClickListener(this);
        mWalletContainer.setOnClickListener(this);
        applyPromocode.setOnClickListener(this);

        promocodeAmt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //TRACK EVENT
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"PromoCodeType",editable.toString(),hMap);

            }
        });


        doWAlletPromocode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                //track event
                String val="";
                if(b){
                   val= "Check";
                }else{
                   val = "Uncheck";
                }
                //TRACK EVENT
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"UseDOEarningsClick",val,hMap);
            }
        });

    }

    private void fetchBillingAmount() {

        showLoader();
        Map<String, String> param = new HashMap<>();
        param.put("action", "get_bill_amount");
        param.put("diner_id", DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put("obj_id", getArguments().getString(PaymentConstant.BOOKING_ID, ""));
        param.put("obj_type", getArguments().getString(PaymentConstant.PAYMENT_TYPE, ""));

        param.put("f", "1");

        getNetworkManager().stringRequestPost(REQUEST_BILL_AMOUNT, AppConstant.URL_BILL_PAYMENT,
                param, this, this, false);
    }

    @Override
    public void onClick(View v) {

        if (v == mMoveToSummary) {


            boolean isWalletSelected;

            if (doWAlletPromocode.isEnabled()) {
                isWalletSelected = doWAlletPromocode.isChecked();
            } else {
                isWalletSelected = false;
            }

            // Track Event (for GA only)
            boolean isRestaurantSelected;
            if (restaurantAmt.equalsIgnoreCase("") && (restaurantNames.equalsIgnoreCase(""))) {
                isRestaurantSelected = false;
            } else {
                isRestaurantSelected = true;
            }


//            AnalyticsHelper.getAnalyticsHelper(getActivity())
//                    .trackEventGA("PayNowBillAmount",
//                            getString(com.dineout.book.R.string.ga_action_continue), restaurantId + "_" + isWalletSelected + "_" + isRestaurantSelected);


            //TRACK EVENT
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"CTAClick","Continue",hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"BillAmountType",mAmount.getText().toString(),hMap);

            if (getArguments() != null) {
                Bundle bundle = getArguments();
                bundle.putBoolean(PaymentConstant.IS_WALLET_ACCEPTED, isWalletSelected);
                bundle.putString(PaymentConstant.BOOKING_AMT, mAmount.getText().toString());
                bundle.putString(PaymentConstant.RESTAURANT_ID, restaurantId);
                BillSummaryFragment fragment = BillSummaryFragment.newInstance(bundle);
                addToBackStack(getActivity(), fragment);

            }
        } else if (v == mWalletContainer) {
            makeWalletContainerSelected();
        } else if (v == applyPromocode) {

            AppUtil.hideKeyboard(getActivity());
            //trackEventGA("PayNowBillAmount", "ApplyCode", restaurantId);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"ApplyClick","ApplyClick",hMap);
            verifyReferralCode(promocodeAmt.getText().toString());
        }
    }

    private void makeWalletContainerSelected() {

        if (mWalletAmount <= 0) {
            doWAlletPromocode.setEnabled(false);
            doWAlletPromocode.setChecked(false);
            return;
        }


        if (restaurantAmt.equalsIgnoreCase("") && (restaurantNames.equalsIgnoreCase(""))) {

            doWAlletPromocode.setEnabled(true);
            doWAlletPromocode.setChecked(true);

        } else {

            if (!TextUtils.isEmpty(mAmount.getText().toString())) {
                if (Integer.parseInt(mAmount.getText().toString()) <= Integer.parseInt(restaurantAmt)) {
                    doWAlletPromocode.setEnabled(false);
                    doWAlletPromocode.setChecked(false);
                } else {
                    doWAlletPromocode.setEnabled(true);
                    doWAlletPromocode.setChecked(true);
                }
            } else {
                doWAlletPromocode.setEnabled(true);
                doWAlletPromocode.setChecked(true);
            }

        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (!TextUtils.isEmpty(s.toString()) && Integer.parseInt(s.toString()) > 0) {
            mMoveToSummary.setEnabled(true);
        } else {
            mMoveToSummary.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {


    }

    @Override
    public boolean onPopBackStack() {
        return super.onPopBackStack();
    }

    @Override
    public void onRemoveErrorView() {

    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        hideLoader();
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        if (getActivity() == null || getView() == null) {
            return;
        }

        try {
            hideLoader();

            if (responseObject != null) {

                JSONObject resp = new JSONObject(responseObject);
                if (resp.optBoolean("status")) {
                    JSONObject outputParam = resp.optJSONObject("output_params");
                    if (outputParam != null) {
                        JSONObject data = outputParam.optJSONObject("data");
                        if (request.getIdentifier() == REQUEST_BILL_AMOUNT) {

                            double amt = data.optDouble("bill_amount");
                           // if (amt > 0)
                                // mAmount.setText(data.optString("bill_amount"));

                                String walletAmt=data.optString("do_wallet_amt");
                            if(!AppUtil.isStringEmpty(walletAmt))

                                mWalletAmount = Double.parseDouble(walletAmt);
                            setDOEarningAmount(mWalletAmount);
                            mWallet.setText(getString(R.string.container_rupee, data.optString("do_wallet_amt")));


                            restaurantAmt = data.optString("rest_wallet_amt");
                            restaurantNames = data.optString("restaurant_name");
                            makeWalletContainerSelected();

                            initializeEarningSection();

                        } else if (request.getIdentifier() == REFFERAL_CODE) {
                            //track event for promocode apply sucess
                           // trackEventGA("PayNowBillAmount", getString(R.string.ga_action_coupon_success), null);


                            isPromoCodeApplied = true;

//                            HashMap<String, Object> props = new HashMap<>();
//                            props.put("Code", promocodeAmt.getText().toString().trim());
//                            trackMixPanelEvent("CouponApplied", props);

                            MoreDialog dialog = new MoreDialog("PromoCode Applied", data.optString("msg"), getActivity(), false);
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    dialog.dismiss();
                                    promocodeAmt.setText("");
                                    fetchBillingAmount();
                                }
                            });
                            dialog.show(getFragmentManager(), "bill_amount");
                        }
                    } else {
                        Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                                "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();

                    }


                } else {
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
        }
    }

    @Override
    public void handleNavigation() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_payment_details),getString(R.string.d_back_click),
                getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }


    private void verifyReferralCode(String refCode) {
        showLoader();

        getNetworkManager().stringRequestPost(REFFERAL_CODE, AppConstant.URL_REFFEREL_CODE,
                ApiParams.getRefferelCodeParams(
                        ((TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) ? "" :
                                DOPreferences.getDinerId(getContext())), restaurantId,
                        refCode), this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, true);
    }



    private String getDOEarningAmount(){
        return Double.toString(mWalletAmount);
    }

    private void setDOEarningAmount(double doEarningAmount){
        mWalletAmount=doEarningAmount;
    }

    private void setRestaurantAmount(String restaurantsAmount){
        restaurantAmt=restaurantsAmount;
    }

    private void setRestaurantName(String restName){
        restaurantNames=restName;
    }

}
