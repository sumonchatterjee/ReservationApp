package com.dineout.book.interfaces;



import org.json.JSONObject;



public interface UserAuthenticationCallback {
    void userAuthenticationLoginSuccess(JSONObject loginSuccessObject);

    void userAuthenticationLoginFailure(JSONObject loginFailureObject);

    void userAuthenticationOTPSuccess(JSONObject otpSuccessObject);

    void userAuthenticationOTPFailure(JSONObject otpFailureObject);
}
