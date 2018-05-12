package com.dineout.book.controller;

import android.os.Bundle;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;




public class OTPApiController {


    public static void sendOTPToDiner(final Bundle bundle, DineoutNetworkManager manager, final OTPCallbacks callback, final String source, final String type) {
        if (bundle == null)
            return;

        Map<String, String> param = new HashMap<>();
        param.put("input", bundle.getString(USER_INPUT_TEXT_KEY));
        param.put("type", type);
        param.put("f", "1");


        manager.stringRequestPost(102, AppConstant.URL_LOGIN_VIA_OTP
                , param, new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

                        try {
                            JSONObject resp = new JSONObject(responseObject);
                            if (resp.optBoolean("status")) {
                                if(callback!=null){
                                    callback.sendOTPToDinerSuccess(resp,source);
                                }

                            } else {
                                 callback.sendOTPToDinerFailure(resp.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG));
                            }
                        } catch (Exception e) {
                            callback.sendOTPToDinerFailure("Something went wrong.Please try again");

                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        callback.sendOTPToDinerFailure("");

                    }
                }, false);

    }


    public interface OTPCallbacks {
        void sendOTPToDinerSuccess(JSONObject resp, String source);

        void sendOTPToDinerFailure(String msg);
    }


}