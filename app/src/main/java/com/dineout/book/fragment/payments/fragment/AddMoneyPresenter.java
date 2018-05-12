package com.dineout.book.fragment.payments.fragment;

import android.content.Context;
import android.os.Bundle;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.adapter.AddMoneyAdapter;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.view.contract.AddMoneyContract;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by prateek.aggarwal on 10/17/16.
 */
public class AddMoneyPresenter implements AddMoneyContract.FooterContract,AddMoneyContract.AmountContract ,Response.Listener<String>{

    private JSONObject mData;
    private AddMoneyContract addMoneyContract;
    private AddMoneyAdapter addMoneyAdapter;
    private double mAmount;
    private Bundle mBundle;
    private final int WALLET_INITIALIZATION = 0x10;
    private Context mContext;

    public AddMoneyPresenter(JSONObject data, Bundle bundle){

        this.mData = data;
        this.mBundle = bundle;
    }

    public void setView(Context context,AddMoneyContract contract){

        this.addMoneyContract = contract;
        this.mContext = context;

    }

    public void onAttach(){

    }

    public void onDetach(){

    }



    public AddMoneyAdapter getAdapter(Context context){

        if(addMoneyAdapter == null)
            addMoneyAdapter = new AddMoneyAdapter(context,mData,this,this);
        return addMoneyAdapter;
    }
    @Override
    public void confirmAdd() {

        //track event
        int paymentType = mData.optInt("payment_type");
        String screenName="";
        switch (paymentType){

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

        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(mContext);
        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("AddMoneyClick_"+screenName,hMap);
        AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("P_"+screenName,"AddMoneyClick_"+screenName,Double.toString(mAmount));

        if(this.mAmount> 0L && this.mAmount >= mData.optDouble("minimum_amt_to_add") ){

            callAddMoneyApi();
        }else{
            addMoneyContract.showError("Please provide minimum amount.");
        }
    }

    private void callAddMoneyApi(){

        addMoneyContract.showProgress();
        DineoutNetworkManager.newInstance(mContext).stringRequestPost(WALLET_INITIALIZATION,
                AppConstant.URL_ADD_MONEY, ApiParams.getAddMoneyPaymentParams(DOPreferences.getDinerId(mContext),
                        mBundle.getString(PaymentConstant.BOOKING_ID,""),
                        mBundle.getBoolean(PaymentConstant.IS_WALLET_ACCEPTED),
                        mBundle.getString(PaymentConstant.PAYMENT_TYPE),
                        mBundle.getInt(PaymentConstant.WALLET_TYPE) + "", ((int)mAmount)+""),
                this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                       addMoneyContract.hideProgress();
                    }
                }, false);

    }



    @Override
    public void updateAmount(double amount) {

        this.mAmount = amount;
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        addMoneyContract.hideProgress();
        try {
            if (responseObject != null) {
                JSONObject resp = new JSONObject((String) responseObject);
                if (resp != null && resp.optBoolean("status")) {
                    JSONObject outputParam = resp.optJSONObject("output_params");
                    if (outputParam != null) {
                        JSONObject data = outputParam.optJSONObject("data");
                        addMoneyContract.navigateToWebView(data.optString("submit_url"),
                                getPostData(data.optJSONObject("postparam")));


                    }
                } else {

                    JSONObject apiResponseObject = resp.optJSONObject("apiResponse");
                    if (apiResponseObject != null) {
                      addMoneyContract.showError(apiResponseObject.optString("statusdescription"));
                    } else {
                        addMoneyContract.showError(resp.optString(AppConstant.ERROR_MESSAGE));

                    }

                }
            }
        } catch (Exception e) {
            // Exception
        }
    }


    private String getPostData(JSONObject data){


        StringBuilder sb = new StringBuilder();

        Iterator<String> keys = data.keys();

        while(keys.hasNext()){

            String param = keys.next();
            try{
            sb.append(URLEncoder.encode(param,"UTF-8")+"="+URLEncoder.encode(data.optString(param,"UTF-8"))+"&");
            }catch (Exception e){
                // exception
            }
        }

        return sb.toString();

    }
}
