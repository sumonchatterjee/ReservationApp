package com.dineout.book.util;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.BuildConfig;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.dialogs.ProgressDialog;
import com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.dialogs.LoginSessionExpireDialog;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.mobikwik.sdk.MobikwikSDK;
import com.mobikwik.sdk.lib.Transaction;
import com.mobikwik.sdk.lib.TransactionConfiguration;
import com.mobikwik.sdk.lib.User;
import com.mobikwik.sdk.lib.payinstrument.PaymentInstrumentType;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment.OTP_VERIFY_CODE;
import static com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment.LOGIN_SESSION_EXPIRE_CODE;
import static com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment.OTP_REQUIRE_CODE;
import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;


public class PaymentUtils {
    public static final int MOBIQKWIK_REQUEST_CODE = 101;

    public static PaymentParams getPaymentParams(String info) {

        try {

            if (TextUtils.isEmpty(info))
                return new PaymentParams();

            JSONObject payUInfo = new JSONObject(info);
            PaymentParams mPaymentParams = new PaymentParams();
            mPaymentParams.setKey(payUInfo.optString("key"));
            mPaymentParams.setAmount(payUInfo.optString("amount"));
            mPaymentParams.setProductInfo(payUInfo.optString("product_info"));
            mPaymentParams.setFirstName(payUInfo.optString("first_name"));
            mPaymentParams.setEmail(payUInfo.optString("email"));
            mPaymentParams.setSurl(payUInfo.optString("SURL"));
            mPaymentParams.setFurl(payUInfo.optString("FURL"));
            mPaymentParams.setTxnId(payUInfo.optString("transaction_id"));
            mPaymentParams.setUdf1(payUInfo.optString("udf1"));
            mPaymentParams.setUdf2(payUInfo.optString("udf2"));
            mPaymentParams.setUdf3(payUInfo.optString("udf3"));
            mPaymentParams.setUdf4(payUInfo.optString("udf4"));
            mPaymentParams.setUdf5(payUInfo.optString("udf5"));
            mPaymentParams.setUserCredentials(payUInfo.optString("user_credentials"));
            return mPaymentParams;
        } catch (JSONException e) {
            e.printStackTrace();
            return new PaymentParams();
        }
    }

    public static PayuHashes getPayUHashes(String hash) {

        try {

            if (TextUtils.isEmpty(hash))
                return new PayuHashes();
            JSONObject hashObject = new JSONObject(hash);
            PayuHashes hashes = new PayuHashes();
            hashes.setPaymentHash(hashObject.optString("payment_hash"));
            hashes.setPaymentRelatedDetailsForMobileSdkHash(hashObject.optString("payment_related_details_for_mobile_sdk_hash"));
            hashes.setVasForMobileSdkHash(hashObject.optString("vas_for_mobile_sdk_hash"));
            hashes.setDeleteCardHash(hashObject.optString("delete_user_card_hash"));
            hashes.setEditCardHash(hashObject.optString("edit_user_card_hash"));
            hashes.setSaveCardHash(hashObject.optString("save_user_card_hash"));
            hashes.setStoredCardsHash(hashObject.optString("get_user_cards_hash"));
            return hashes;
        } catch (Exception e) {
            return new PayuHashes();
        }
    }


    public static void paymentThroughMobikwik(Fragment context, JSONObject data, String amount) {


        TransactionConfiguration config = new TransactionConfiguration();
        config.setDebitWallet(data.optString("debit_wallet").equalsIgnoreCase("1") ? true : false);
        config.setChecksumUrl(data.optString("checksum_url"));
        config.setPgResponseUrl(data.optString("merchant_return_url"));
        config.setMerchantName(data.optString("merchant_name"));
        config.setMbkId(data.optString("merchant_id"));
//        config.setMode(BuildConfig.DEBUG ? "0" : "1");
        config.setMode("1");
        User usr = new User(data.optString("email_id"), data.optString("phone_no"));

//        data.optString("amt")
        Transaction transaction = Transaction.Factory.newTransaction(usr, data.optString("order_id")
                , data.optString("amount", "0"), PaymentInstrumentType.MK_WALLET);

        Intent inten = new Intent(context.getActivity(), MobikwikSDK.class);
        inten.putExtra(MobikwikSDK.EXTRA_TRANSACTION_CONFIG, config);
        inten.putExtra(MobikwikSDK.EXTRA_TRANSACTION, transaction);
        context.startActivityForResult(inten, MOBIQKWIK_REQUEST_CODE);


    }

    public static void paymentThroughPayTm(Fragment context, JSONObject data, PaytmPaymentTransactionCallback callback) {

//        "mid": "TimesI82520496633460",
//                "channel_id": "WAP",
//                "industry_type_id": "Retail",
//                "website": "Timeswap",
//                "amt": "10.00",
//                "order_id": "TESTPLUS10517",
//                "cust_id": "249929",
//                "mobile_no": "7838923772",
//                "theme": "merchant",
//                "checksum_generation_url": "http:\/\/www.dineout_ci_new.com\/app_api\/mobile_app_api_v5\/paytm_generate_checksum",
//                "checksum_validation_url": "http:\/\/www.dineout_ci_new.com\/app_api\/mobile_app_api_v9\/paytm_payment_response",
//                "server_type": "dev",
//                "MERC_UNQ_REF": "wallet"

        //Instantiate Service
        PaytmPGService paytmPGService = ((data.optString("server_type").equalsIgnoreCase(AppConstant.SERVER_TYPE_DEV)) ?
                PaytmPGService.getStagingService() : PaytmPGService.getProductionService());

        //Instantiate Paytm Merchant
        PaytmMerchant paytmMerchant = new PaytmMerchant(data.optString("checksum_generation_url"), data.optString("checksum_validation_url"));

        //Prepare Paytm Order Map
        Map<String, String> params = new HashMap<String, String>();
        params.put("REQUEST_TYPE", "DEFAULT");
        params.put("ORDER_ID", data.optString("order_id"));
        params.put("MID", data.optString("mid"));
        params.put("CUST_ID", data.optString("cust_id"));
        params.put("CHANNEL_ID", data.optString("channel_id"));
        params.put("INDUSTRY_TYPE_ID", data.optString("industry_type_id"));
        params.put("WEBSITE", data.optString("website"));
        params.put("TXN_AMOUNT", data.optString("amt", "0.0"));
        params.put("THEME", data.optString("theme"));
        params.put("MERC_UNQ_REF", data.optString("MERC_UNQ_REF"));
        params.put("CALLBACK_URL", data.optString("checksum_validation_url"));

        //Instantiate Paytm Order
        PaytmOrder paytmOrder = new PaytmOrder(params);

        //Initialize Service
        paytmPGService.initialize(paytmOrder, paytmMerchant, null);

        //Start Paytm Transaction
        paytmPGService.startPaymentTransaction(context.getActivity(), false, false, callback);
    }

    public static PayuConfig getPayUConfig(String server) {

        PayuConfig config = new PayuConfig();
        if (!TextUtils.isEmpty(server)) {

            config.setEnvironment(server.equalsIgnoreCase("prod") ? PayuConstants.PRODUCTION_ENV : PayuConstants.STAGING_ENV);
        }
        return config;
    }


    public static void initiatePayment(final FragmentActivity context, final Bundle bundle, final DineoutNetworkManager manager) {

        if (bundle == null)
            return;

        showLoader(context.getSupportFragmentManager());

        Map<String, String> param = new HashMap<>();
        param.put("action", "get_bill_amount");
        param.put("diner_id", DOPreferences.getDinerId(context));
        param.put("obj_id", bundle.getString(com.dineout.book.fragment.payments.PaymentConstant.BOOKING_ID, ""));
        param.put("obj_type", bundle.getString(PaymentConstant.PAYMENT_TYPE,""));
        param.put("f", "1");
        manager.stringRequestPost(101, AppConstant.URL_BILL_PAYMENT
                , param, new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

                        try {
                            dismissLoader(context.getSupportFragmentManager());

                            JSONObject resp = new JSONObject(responseObject);
                            if (resp.optBoolean("status")) {
                                JSONObject outputParam = resp.optJSONObject("output_params");
                                if (outputParam != null) {
                                    JSONObject data = outputParam.optJSONObject("data");

                                    // Track Event
                                    AnalyticsHelper.getAnalyticsHelper(context).trackEventGA("PayNowDialog",
                                            "OnlyOnConfirmed", null);

                                    Bundle arg = bundle;
                                    arg.putString(PaymentConstant.DO_WALLET_AMOUNT, data.optString("do_wallet_amt"));
                                    arg.putString(PaymentConstant.BOOKING_AMT, data.optString("bill_amount"));

                                    arg.putString(PaymentConstant.DO_RESTAURANT_NAME, data.optString("restaurant_name"));
                                    arg.putString(PaymentConstant.RESTAURANT_AMT, data.optString("rest_wallet_amt"));

                                    com.dineout.book.fragment.payments.fragment.BillAmountFragment fragment = com.dineout.book.fragment.payments.fragment.BillAmountFragment.newInstance(bundle);
                                    MasterDOFragment.addToBackStack(context, fragment);


                                }
                            } else {
                                String errorCode = resp.optString("error_code");

                                if (errorCode != null) {
                                    switch (errorCode) {
                                        case LOGIN_SESSION_EXPIRE_CODE:
                                            //show session expired dialog
                                            String errorMsg = resp.optString("error_msg");
                                            if (context != null) {
                                                UserAuthenticationController.getInstance(context).showSessionExpireDialog(errorMsg, new PaymentLoginSessionExpiredButtonCallbacks(context, bundle, manager));
                                            }
                                            break;

                                        case OTP_REQUIRE_CODE:
                                            Bundle phoneBundle = new Bundle();

                                            if (context != null) {
                                                phoneBundle.putString(AppConstant.BUNDLE_PHONE_NUMBER, DOPreferences.getDinerPhone(context.getApplicationContext()));
                                            }
                                            if (context != null) {
                                                UserAuthenticationController.getInstance(context).startOTPFlow(phoneBundle, new PaymentOtpFlowCompleteCallback(context, bundle, manager));
                                            }
                                            break;

                                        case OTP_VERIFY_CODE:
                                            JSONObject errorResponse;
                                            JSONObject otpInfo;
                                            if ((errorResponse = resp.optJSONObject("error_response")) != null
                                                    && (otpInfo = errorResponse.optJSONObject("otp_info")) != null) {
                                                if (otpInfo.optBoolean("status")) {
                                                    JSONObject outputParams;
                                                    JSONObject data;
                                                    if ((outputParams = otpInfo.optJSONObject("output_params")) != null
                                                            && (data = outputParams.optJSONObject("data")) != null) {
                                                        Bundle bundle = new Bundle();
                                                        String userInput = data.optString("phone");
                                                        if (TextUtils.isEmpty(userInput)) {
                                                            userInput = data.optString("email");
                                                        }
                                                        bundle.putString(USER_INPUT_TEXT_KEY, userInput);
                                                        bundle.putString("otp_id", data.optString("otp_id"));
                                                        bundle.putString("type", data.optString("type"));
                                                        bundle.putString("resend_otp_time", data.optString("resend_otp_time"));

                                                        if (context != null) {
                                                            UserAuthenticationController.getInstance(context).startOTPVerificationFlow(bundle, new PaymentOtpFlowCompleteCallback(context, bundle, manager));
                                                        }
                                                    }
                                                } else {
                                                    errorMsg = resp.optString("error_msg");
                                                    if (!TextUtils.isEmpty(errorMsg) && context != null) {
                                                        UiUtil.showToastMessage(context, errorMsg);
                                                    }
                                                }
                                            }
                                            break;

                                        default:
                                            errorMsg = resp.optString("error_msg");
                                            if (!TextUtils.isEmpty(errorMsg) && context != null) {
                                                UiUtil.showToastMessage(context, errorMsg);
                                            }
                                            break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Some error in initializing payment", Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        dismissLoader(context.getSupportFragmentManager());

                    }
                }, false);

    }
    private static void showLoader(FragmentManager manager) {

        dismissLoader(manager);
        ProgressDialog dialog = new ProgressDialog();
        dialog.show(manager, "Progress");
    }

    private static void dismissLoader(FragmentManager manager) {

        if (manager != null && manager.findFragmentByTag("Progress") != null) {

            ((ProgressDialog) manager.findFragmentByTag("Progress")).dismissAllowingStateLoss();
        }
    }

    private static class PaymentLoginFlowCompleteCallbacks implements UserAuthenticationController.LoginFlowCompleteCallbacks {
        FragmentActivity mContext;
        Bundle mBundle;
        DineoutNetworkManager mManager;

        PaymentLoginFlowCompleteCallbacks(FragmentActivity context, Bundle bundle, DineoutNetworkManager manager) {
            this.mContext = context;
            this.mBundle = bundle;
            this.mManager = manager;
        }

        @Override
        public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
            initiatePayment(mContext, mBundle, mManager);
        }

        @Override
        public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
            String errorMsg = "";
            if (loginFlowCompleteFailureObject != null) {
                errorMsg = loginFlowCompleteFailureObject.optString(AuthenticationWrapperStringReqFragment.API_RESPONSE_ERROR_MSG);
            }

            if (TextUtils.isEmpty(errorMsg) && mContext != null && mContext.getResources() != null) {
                errorMsg = mContext.getResources().getString(R.string.default_error_message);
            }

            if (mContext != null) {
                UiUtil.showToastMessage(mContext, errorMsg);
            }
        }
    }

    private static class PaymentOtpFlowCompleteCallback implements UserAuthenticationController.OtpFlowCompleteCallback {
        FragmentActivity mContext;
        Bundle mBundle;
        DineoutNetworkManager mManager;

        PaymentOtpFlowCompleteCallback(FragmentActivity context, Bundle bundle, DineoutNetworkManager manager) {
            this.mContext = context;
            this.mBundle = bundle;
            this.mManager = manager;
        }

        @Override
        public void otpFlowCompleteSuccess(JSONObject otpFlowCompleteSuccessObject) {
            initiatePayment(mContext, mBundle, mManager);
        }

        @Override
        public void otpFlowCompleteFailure(JSONObject otpFlowCompleteFailureObject) {
            String errorMsg = "";
            if (otpFlowCompleteFailureObject != null) {
                errorMsg = otpFlowCompleteFailureObject.optString(AuthenticationWrapperStringReqFragment.API_RESPONSE_ERROR_MSG);
            }

            if (TextUtils.isEmpty(errorMsg) && mContext != null && mContext.getResources() != null) {
                errorMsg = mContext.getResources().getString(R.string.default_error_message);
            }

            if (mContext != null) {
                UiUtil.showToastMessage(mContext, errorMsg);
            }
        }
    }

    private static class PaymentLoginSessionExpiredButtonCallbacks implements LoginSessionExpireDialog.LoginSessionExpiredButtonCallbacks {
        FragmentActivity mContext;
        Bundle mBundle;
        DineoutNetworkManager mManager;

        PaymentLoginSessionExpiredButtonCallbacks(FragmentActivity context, Bundle bundle, DineoutNetworkManager manager) {
            this.mContext = context;
            this.mBundle = bundle;
            this.mManager = manager;
        }

        @Override
        public void LoginSessionExpiredPositiveClick() {
            UserAuthenticationController.getInstance(mContext).startLoginFlow(null, new PaymentLoginFlowCompleteCallbacks(mContext, mBundle, mManager));
        }

        @Override
        public void LoginSessionExpiredNegativeClick() {

        }
    }

}
