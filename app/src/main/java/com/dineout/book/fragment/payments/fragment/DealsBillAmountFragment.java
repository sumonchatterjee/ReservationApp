package com.dineout.book.fragment.payments.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
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
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.dialogs.MoreDialog;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class DealsBillAmountFragment extends MasterDOFragment
        implements View.OnClickListener, Response.Listener<String> {

    private final int REFFERAL_CODE = 120;
    private final int REQUEST_BILL_SUMMARY = 121;
    private final int REQUEST_BILLING_AMOUNT = 101;
    private final int GET_PAYMENT_OPTION = 0x234;
    RelativeLayout earnings;
    private String billAmount;
    private String doWalletAmount;
    private String restaurantWallet;
    private String restaurantId;
    private String restaurantName;
    private String spclRequest;
    private String paymentType;
    private AppCompatCheckBox mWalletOpted;
    private EditText promocodeEdtxt;
    private String[] dealIds;
    private String[] dealQty;
    private long datetTime;
    private boolean isPromoCodeApplied = false;
    private TextView walletAmountTxtvw;
    private String mAmount;
    private String cashbackDiscount;
    private String maximumCashback;
    private String earningsInfo;
    private String cashbackInfo;
    private RelativeLayout cashbackInfoContainer;
    private LinearLayout earningsInfosLayout;
    private TextView cashbackInfoTxt;

    public static DealsBillAmountFragment getFragment(Bundle bundle) {

        if (bundle == null)
            bundle = new Bundle();
        DealsBillAmountFragment dealBillAmountFragment = new DealsBillAmountFragment();
        dealBillAmountFragment.setArguments(bundle);
        return dealBillAmountFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackScreenName(getString(R.string.countly_payment_details));
        if (getArguments() != null) {
            setDoWalletAmount(getArguments().getString("do_wallet_amount"));
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deals_bill_payment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle(getActivity().getResources().getString(R.string.payment_details));
        AppUtil.hideKeyboard(getActivity());
        Bundle bundle = getArguments();
        if (bundle != null) {

            this.billAmount = bundle.getString(AppConstant.BUNDLE_BILL_AMOUNT);
            this.doWalletAmount = getDOWalletAmount();
            this.restaurantWallet = bundle.getString(AppConstant.BUNDLE_RESTAURANT_WALLET_AMOUNT);
            this.restaurantId = bundle.getString(AppConstant.BUNDLE_RESTAURANT_ID_DETAILS);
            this.dealIds = bundle.getStringArray(AppConstant.BUNDLE_DEAL_IDS);
            this.dealQty = bundle.getStringArray(AppConstant.BUNDLE_DEAL_QUANTITY);
            this.datetTime = bundle.getLong(AppConstant.BUNDLE_DATE_TIME);
            this.restaurantName = bundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME_DETAIL);
            this.spclRequest = bundle.getString("spcl_request");
            this.paymentType = bundle.getString(AppConstant.BUNDLE_TYPE);
            this.cashbackDiscount = bundle.getString(AppConstant.BUNDLE_CASHBACK_DISCOUNT);
            this.maximumCashback = bundle.getString(AppConstant.BUNDLE_MAX_CASHBACK);
            this.earningsInfo = bundle.getString(AppConstant.BUNDLE_EARNINGS_INFO);
            this.cashbackInfo = bundle.getString(AppConstant.BUNDLE_CASHBACK_INFO);

            if (paymentType.equalsIgnoreCase("deal")) {
                trackScreenToGA(getString(R.string.ga_pay_payment_deals));
            } else {
                trackScreenToGA(getString(R.string.ga_pay_payment_events));
            }

        }
        initilizeData();
    }


    private void initilizeData() {
        showBillAmountDetails(billAmount, doWalletAmount, restaurantWallet, earningsInfo, cashbackInfo);
        promocodeEdtxt = (EditText) getView().findViewById(R.id.promo_code_input);
        Button promoCode = (Button) getView().findViewById(R.id.promo_code_apply);
        promoCode.setOnClickListener(this);

        Button continueBtn = (Button) getView().findViewById(R.id.promo_code_continue_btn);
        continueBtn.setOnClickListener(this);
//
//        promocodeEdtxt.addTextChangedListener(new TextWatcher() {
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
//                //TRACK EVENT
//                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
//                trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"PromoCodeType","PromoCodeType",hMap);
//
//            }
//        });
    }




    private void showBillAmountDetails(String billAmount, String doWalletAmount, String restaurantWalletAmt,
                                       String earningsInfo, final String cashbackInfo) {

        restaurantWallet = restaurantWalletAmt;
        TextView billAmountTxtvw = (TextView) getView().findViewById(R.id.membership_fees);
        billAmountTxtvw.setText(String.format(getContext().getString(com.dineout.recycleradapters.R.string.container_rupee), billAmount) + "/-");

        TextView earningAmountTxtvw = (TextView) getView().findViewById(R.id.wallet);
        earningAmountTxtvw.setText(String.format(getContext().getString(com.dineout.recycleradapters.R.string.container_rupee), getDOWalletAmount()));

        TextView restaurantEarningTxt = (TextView) getView().findViewById(R.id.earnings_txt);
        restaurantEarningTxt.setText(restaurantName + " Earnings Applied");

        walletAmountTxtvw = (TextView) getView().findViewById(R.id.earnings_amount);
        earnings = (RelativeLayout) getView().findViewById(R.id.earnings_selector_container);

        RelativeLayout earningsInfoContainer = (RelativeLayout) getView().findViewById(R.id.deals_earnings_container);
        cashbackInfoContainer = (RelativeLayout) getView().findViewById(R.id.cashback_container);

        earningsInfosLayout = (LinearLayout) getView().findViewById(R.id.earnings_section_view);
        cashbackInfoTxt = (TextView) getView().findViewById(R.id.cashback_info);

        mWalletOpted = (AppCompatCheckBox) getView().findViewById(R.id.wallet_opted_promocode);


        if (!TextUtils.isEmpty(doWalletAmount)) {
            if (Integer.parseInt(doWalletAmount) <= 0) {
                mWalletOpted.setChecked(false);
                mWalletOpted.setEnabled(false);

                earningsInfoContainer.setVisibility(View.GONE);
            } else {
                mWalletOpted.setChecked(true);
                mWalletOpted.setEnabled(true);

                if (TextUtils.isEmpty(earningsInfo)) {
                    earningsInfoContainer.setVisibility(View.GONE);
                } else {
                    earningsInfoContainer.setVisibility(View.VISIBLE);
                    showEarningsSection(earningsInfo);
                }
            }
        }

        if (!TextUtils.isEmpty(restaurantWalletAmt)) {
            if (Integer.parseInt(doWalletAmount) <= 0) {
                mWalletOpted.setChecked(false);
                mWalletOpted.setEnabled(false);
            } else {
                if (Integer.parseInt(billAmount) <= Integer.parseInt(restaurantWalletAmt)) {
                    mWalletOpted.setEnabled(false);
                    mWalletOpted.setChecked(false);
                } else {
                    mWalletOpted.setChecked(true);
                    mWalletOpted.setEnabled(true);
                }
            }
            earnings.setVisibility(View.VISIBLE);
            walletAmountTxtvw.setText(String.format(getContext().getString(com.dineout.recycleradapters.R.string.container_rupee), restaurantWalletAmt));

        } else {
            earnings.setVisibility(View.GONE);
        }

        showCashbackSection(cashbackInfo);

        mWalletOpted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

               //track event for check uncheck
                String val="";
                if(isChecked){
                    val= "Check";
                }else{
                    val = "Uncheck";
                }
                //TRACK EVENT
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"UseDOEarningsClick",val,hMap);

                showCashbackSection(cashbackInfo);
            }
        });
    }


    // show cashback text

    private void showCashbackText(String cashbackInfo) {
        if (!TextUtils.isEmpty(cashbackInfo)) {
            double cahbckAmt = calculateCashbackInfo();
            Double d = new Double(cahbckAmt);
            int val = d.intValue();

            cashbackInfo = TextUtils.isEmpty(cashbackInfo) ? cashbackInfo.replaceAll(AppConstant.TINY_URL_AMOUNT, "") : cashbackInfo.replaceAll(AppConstant.TINY_URL_AMOUNT, "Rs. " + String.valueOf(val));
            if (cashbackInfo != null) {
                cashbackInfoTxt.setText(cashbackInfo);
            }
        }

    }


   //show cashback section to user only if cashback discount > 0, maxcashback > 0

    private void showCashbackSection(String cashbackInfo) {
        if (!TextUtils.isEmpty(cashbackDiscount) && !TextUtils.isEmpty(maximumCashback)) {

            int bill = 0;
            int doWallet = 0;
            int restaurantwalletMoney = 0;
            int cashbackDis=0;
            int maxCashbck=0;

            if (!TextUtils.isEmpty(billAmount)) {
                bill = Integer.parseInt(billAmount);
            }

            if(mWalletOpted.isChecked()){
                if (!TextUtils.isEmpty(doWalletAmount)) {
                    doWallet = Integer.parseInt(doWalletAmount);
                }
            }else{
                doWallet=0;
            }

            if (!TextUtils.isEmpty(restaurantWallet)) {
                restaurantwalletMoney = Integer.parseInt(restaurantWallet);
            }

            if (!TextUtils.isEmpty(cashbackDiscount)) {
                cashbackDis = Integer.parseInt(cashbackDiscount);
            }

            if (!TextUtils.isEmpty(maximumCashback)) {
                maxCashbck = Integer.parseInt(maximumCashback);
            }

            int cashback = (bill - (doWallet + restaurantwalletMoney));
            if ((cashbackDis > 0) && (maxCashbck > 0) && cashback > 0) {
                if (TextUtils.isEmpty(cashbackInfo)) {
                    cashbackInfoContainer.setVisibility(View.GONE);
                } else {
                    cashbackInfoContainer.setVisibility(View.VISIBLE);

                    showCashbackText(cashbackInfo);
                }

            } else {
                cashbackInfoContainer.setVisibility(View.GONE);
            }
        }

    }


   // show earnings sections to user,data coming from api

    private void showEarningsSection(String earningsInfo) {
        JSONArray extraJsonArray = null;

        try {
            if (!TextUtils.isEmpty(earningsInfo)) {
                extraJsonArray = new JSONArray(earningsInfo);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (extraJsonArray != null && extraJsonArray.length() > 0) {
            earningsInfosLayout.removeAllViews();
            for (int i = 0; i < extraJsonArray.length(); i++) {

                final JSONObject infoJsonObject = extraJsonArray.optJSONObject(i);

                if (infoJsonObject != null) {
                    // Get View
                    View extraInfoView = LayoutInflater.from(getContext()).
                            inflate(R.layout.earnings_section, null, false);

                    TextView textViewDealTitle = (TextView) extraInfoView.findViewById(R.id.info_txt);
                    if (!AppUtil.isStringEmpty(infoJsonObject.optString("title_1"))) {
                        textViewDealTitle.setText(infoJsonObject.optString("title_1"));
                    }

                    TextView textViewInfo = (TextView) extraInfoView.findViewById(R.id.info_value);
                    if (!AppUtil.isStringEmpty(infoJsonObject.optString("title_2"))) {
                        textViewInfo.setText(String.format(getContext().getString(com.dineout.recycleradapters.R.string.container_rupee), infoJsonObject.optString("title_2")));
                    }
                    earningsInfosLayout.addView(extraInfoView);

                }

            }

        }
    }

 // calculate cashback that needs to be shown to user

    private double calculateCashbackInfo() {
        int billAmt = 0;
        int dowallet = 0;
        int restAmt = 0;
        int cashbckdis = 0;
        int maxCashback = 0;
        int cashback;
        double result=0.0;

        if (!TextUtils.isEmpty(billAmount)) {
            billAmt = Integer.parseInt(billAmount);
        }

         if(mWalletOpted.isChecked()){
             if (!TextUtils.isEmpty(doWalletAmount)) {
                 dowallet = Integer.parseInt(doWalletAmount);
             }
         }else{
             dowallet=0;
         }


        if (!TextUtils.isEmpty(restaurantWallet)) {
            restAmt = Integer.parseInt(restaurantWallet);
        }

        if (!TextUtils.isEmpty(cashbackDiscount)) {
            cashbckdis = Integer.parseInt(cashbackDiscount);

        }

        if (!TextUtils.isEmpty(maximumCashback)) {
            maxCashback = Integer.parseInt(maximumCashback);

        }


        cashback = (billAmt - (dowallet + restAmt));
        if(cashback > 0){
            result = (cashback * cashbckdis * 1.0 / 100);
            result = Math.ceil(result);

            if (result > maxCashback) {
                result = maxCashback;
            }
        }

        return result;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promo_code_apply:
                AppUtil.hideKeyboard(getActivity());

                if (AppUtil.hasNetworkConnection(getActivity())) {
                    String code = promocodeEdtxt.getText().toString();
                    if (!TextUtils.isEmpty(code)) {
                        //track event
                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"ApplyClick","ApplyClick",hMap);

//                        if (paymentType.equalsIgnoreCase("deal")) {
//                            trackEventGA(getString(R.string.ga_pay_payment_deals), getString(R.string.ga_action_deals_apply_code), restaurantId);
//                        } else {
//                            trackEventGA(getString(R.string.ga_pay_payment_events), getString(R.string.ga_action_deals_apply_code), restaurantId);
//                        }

                        verifyReferralCode(code);
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.promo_code_continue_btn:
                if (AppUtil.hasNetworkConnection(getActivity())) {

                    //track event
                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"PromoCodeType",promocodeEdtxt.getText().toString(),hMap);
                    trackEventForCountlyAndGA(getString(R.string.countly_payment_details),"CTAClick","Continue",hMap);

                    getBillingSummary();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                }

                break;
        }
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



    private void reloadScreenAfterApplyingPromocode() {

        getNetworkManager().stringRequestPost(REQUEST_BILLING_AMOUNT, AppConstant.URL_BILL_PAYMENT
                , getBillAmountParams(), this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, false);
    }



    private HashMap<String, String> getBillAmountParams() {
        long unixTime;
        String userFullName = (DOPreferences.getDinerFirstName(getContext()) + " " +
                DOPreferences.getDinerLastName(getContext()));

        if (paymentType.equalsIgnoreCase("deal")) {
            unixTime = datetTime / 1000L;
        } else {
            unixTime = datetTime;
        }

        return ApiParams.getBillAmountParams(userFullName, DOPreferences.getDinerPhone(getContext()), DOPreferences.getDinerEmail(getContext()), dealIds, dealQty, restaurantId, Long.toString(unixTime), paymentType);
    }

    private void getBillingSummary() {
        showLoader();
        getNetworkManager().stringRequestPost(REQUEST_BILL_SUMMARY, AppConstant.URL_BILL_PAYMENT,
                ApiParams.getDealsBillingSummaryParams(paymentType, mWalletOpted.isChecked()), this, new Response.ErrorListener() {
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

        if (responseObject != null) {
            try {
                JSONObject resp = new JSONObject(responseObject);
                if (resp.optBoolean("status")) {

                    JSONObject dataObject = resp.optJSONObject("output_params").optJSONObject("data");
                    if (request.getIdentifier() == REFFERAL_CODE) {

                        //track event
                        if (paymentType.equalsIgnoreCase("deal")) {
                            //trackEventGA(getString(R.string.ga_pay_payment_deals), getString(R.string.ga_action_coupon_success), null);
                        } else {
                            //trackEventGA(getString(R.string.ga_pay_payment_events), getString(R.string.ga_action_coupon_success), null);
                        }

                        isPromoCodeApplied = true;

                        MoreDialog dialog = new MoreDialog("PromoCode Applied", dataObject.optString("msg"), getActivity(), false);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                dialog.dismiss();
                                promocodeEdtxt.setText("");
                                reloadScreenAfterApplyingPromocode();
                            }
                        });
                        dialog.show(getFragmentManager(), "bill_amount");

                    } else if (request.getIdentifier() == REQUEST_BILLING_AMOUNT) {
                        String earnings = "";
                        cashbackDiscount = dataObject.optString("cashback_discount");
                        maximumCashback = dataObject.optString("max_cashback");

                        billAmount=dataObject.optString("bill_amount");
                        restaurantWallet=dataObject.optString("rest_wallet_amt");
                        restaurantName=dataObject.optString("restaurant_name");

                        JSONArray extraInfoJsonArray = dataObject.optJSONArray("section_1");
                        setDoWalletAmount(dataObject.optString("do_wallet_amt"));

                        if(extraInfoJsonArray!=null){
                            earnings=extraInfoJsonArray.toString();
                        }

                        showBillAmountDetails(dataObject.optString("bill_amount"), dataObject.optString("do_wallet_amt"),
                                dataObject.optString("rest_wallet_amt"), earnings, dataObject.optString("cashback_text"));

                    } else if (request.getIdentifier() == REQUEST_BILL_SUMMARY) {
                        handleBillingSummaryResponse(dataObject);

                    } else if (request.getIdentifier() == GET_PAYMENT_OPTION) {
                        handleOptionResponse(dataObject);
                    }

                } else {
                    UiUtil.showToastMessage(getContext(), resp.optString("error_msg", "Some error occured!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                UiUtil.showToastMessage(getContext(), "Some error in initializing payment");
            }
        }

    }

    private void handleBillingSummaryResponse(JSONObject data) {

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("Successful", true);
        props.put("RestaurantId", restaurantId);
        props.put("Amount",billAmount);
        //trackEventGA(getString(R.string.deal_pay_initiated), props);

        int walletAmount = 0;
        mAmount = data.optString("additional_amount");
        if (earnings.getVisibility() == View.VISIBLE) {
            String walletAmt = walletAmountTxtvw.getText().toString();
            if (!TextUtils.isEmpty(walletAmt)) {
                walletAmount = Integer.parseInt(restaurantWallet);
            }
        }
        Bundle bundle = new Bundle();
        if (mWalletOpted.isChecked() || isPromoCodeApplied || walletAmount > 0) {
            if (mWalletOpted.isEnabled()) {
                bundle.putBoolean(PaymentConstant.IS_WALLET_ACCEPTED, mWalletOpted.isChecked());
            } else {
                bundle.putBoolean(PaymentConstant.IS_WALLET_ACCEPTED, false);
            }
            bundle.putString(PaymentConstant.RESTAURANT_ID, restaurantId);
            bundle.putString(PaymentConstant.PAYMENT_TYPE, paymentType);
            DealsPaymentBreakupFragment fragment = DealsPaymentBreakupFragment.getFragment(bundle, data);

            addToBackStack(getActivity(), fragment);

        } else {
            getPaymentOptions();
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

    private void handleOptionResponse(JSONObject data) {

        Bundle bundle = getArguments();
        bundle.putString(PaymentConstant.FINAL_AMOUNT, mAmount);
        bundle.putString(PaymentConstant.PAYMENT_TYPE, paymentType);
        SelectPaymentFragment fragment = SelectPaymentFragment
                .newInstance(bundle);
        fragment.setApiResp(data.toString());
        addToBackStack(getActivity(), fragment);
    }


    private String getDOWalletAmount() {
        return doWalletAmount;
    }

    private void setDoWalletAmount(String amount) {
        doWalletAmount = amount;
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_payment_details),getString(R.string.d_back_click),
                getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
