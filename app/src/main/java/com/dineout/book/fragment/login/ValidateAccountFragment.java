package com.dineout.book.fragment.login;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValidateAccountFragment extends LoginFlowBaseFragment implements View.OnClickListener,
        FacebookCallback<LoginResult>, GoogleApiClient.OnConnectionFailedListener {

    private EditText userEmail;
    private CallbackManager callbackManager;
    private RelativeLayout relativeLayoutFacebookLoginInProcess;
    private RelativeLayout relativeLayoutGplusLoginInProcess;

    private UserAuthenticationCallback mCallback;

    protected final int VALIDATE_ACCOUNT_API = 0x425;
    private static final int SIGNIN_REQUEST = 0x01;


    private GoogleApiClient mGoogleApiClient;

    public static ValidateAccountFragment newInstance(Bundle bundle, UserAuthenticationCallback callback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        ValidateAccountFragment fragment = new ValidateAccountFragment();
        fragment.mCallback = callback;
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackScreenToGA(getString(R.string.ga_screen_login_dialog));

        trackScreenName(getString(R.string.countly_login));

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_in_layout, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle("Log in / Sign up");
        initializeView();

//        AppUtil.showKeyBoard(getActivity());
    }


    private void initializeView() {
        if(getView()!=null) {
            userEmail = (EditText) getView().findViewById(R.id.email_edtxt);
            relativeLayoutFacebookLoginInProcess = (RelativeLayout) getView().findViewById(R.id.relativeLayout_facebook_login_in_process);
            relativeLayoutGplusLoginInProcess = (RelativeLayout) getView().findViewById(R.id.relativeLayout_gplus_login_in_process);
            (getView().findViewById(R.id.continue_btn)).setOnClickListener(this);
            (getView().findViewById(R.id.rl_login_dialog_gplus)).setOnClickListener(this);
            (getView().findViewById(R.id.rl_login_dialog_fb)).setOnClickListener(this);


            //Get Terms and Conditions Instance
            TextView textViewSignupTermsCondition = (TextView) getView().findViewById(R.id.textViewSignupTermsCondition);

            Spannable spannableString = Spannable.Factory.getInstance().newSpannable(getString(R.string.text_term_condition_privacy_policy));

            //Set Span for Terms and Condition
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //Navigate to Terms and Condition
                    navigateToWebView("Terms & Conditions", AppConstant.DINEOUT_TNC);


                }
            }, 30, 48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_75)), 30, 48, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 30, 48, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            //Set Span for Privacy Policy
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                    navigateToWebView("Privacy Policy", AppConstant.DINEOUT_PRIVACY_POLICY);


                }
            }, 53, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_75)), 53, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 53, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            textViewSignupTermsCondition.setText(spannableString);
            textViewSignupTermsCondition.setGravity(Gravity.CENTER_HORIZONTAL);
            textViewSignupTermsCondition.setMovementMethod(LinkMovementMethod.getInstance());

        }
    }

    private void validateDinerAccount() {
        if(getActivity() == null) {
            return;
        }

        showLoader();

        getNetworkManager().stringRequestPost(VALIDATE_ACCOUNT_API, AppConstant.URL_VALIDATE_ACCOUNT,
                ApiParams.getValidateAccountParams(userEmail.getText().toString()), this, this, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.continue_btn:

                // track events
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                if (userEmail.getText().toString().matches("[0-9]+")){

                    trackEventForCountlyAndGA(getString(R.string.countly_login),"EmailMobileType","mobile",hMap);
                }else{
                    trackEventForCountlyAndGA(getString(R.string.countly_login),"EmailMobileType","email",hMap);
                }

                trackEventForCountlyAndGA(getString(R.string.countly_login),getString(R.string.d_continue_click),getString(R.string.d_continue_click),hMap);

                if(!TextUtils.isEmpty(userEmail.getText().toString())) {
                    validateDinerAccount();
                }

                break;

            case R.id.rl_login_dialog_gplus:

                AppUtil.hideKeyboard(getActivity());
                loginWithGoogle();
                break;

            case R.id.rl_login_dialog_fb:

                AppUtil.hideKeyboard(getActivity());
                loginWithFacebook();
                break;
        }
    }

    private void loginWithGoogle() {

        DOPreferences.setloginSource(getContext(),LoginType.GOOGLE);
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login),getString(R.string.d_google_click),getString(R.string.d_google_click),hMap);

        // Show Progress Bar
        relativeLayoutGplusLoginInProcess.setVisibility(View.VISIBLE);

        if (mGoogleApiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(getString(R.string.server_client_id))
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        this.startActivityForResult(signInIntent, SIGNIN_REQUEST);
    }

    private void loginWithFacebook() {
        // Track Event
//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackEventGA(getString(R.string.ga_screen_login_dialog),
//                        getString(R.string.ga_action_login_facebook), null);


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login),getString(R.string.d_facebook_click),getString(R.string.d_facebook_click),hMap);

        // Show Progress Bar
        relativeLayoutFacebookLoginInProcess.setVisibility(View.VISIBLE);

        LoginManager manager = LoginManager.getInstance();
        manager.logInWithReadPermissions(this, getFacebookPermission());
        manager.registerCallback(callbackManager, this);
    }



    private List<String> getFacebookPermission() {
        List<String> permission = new ArrayList<>();
        /*needed for user id */
        permission.add("email");
        /*for providing location based notification and mailer*/
        permission.add("user_location");
        /*to show friends who have already made booking on selected restaurant*/
        permission.add("user_friends");
        /*to get info about user mobile number*/
        permission.add("public_profile");

        return permission;
    }



    @Override
    public void onSuccess(com.facebook.login.LoginResult loginResult) {
        if (loginResult != null) {
            String accessToken = loginResult.getAccessToken().getToken();
            if (!TextUtils.isEmpty(accessToken))
            DOPreferences.setloginSource(getContext(),LoginType.Facebook);
                makeLoginRequest(
                        FB_SIGNIN_API, ApiParams.getSocialLoginParameters(LoginType.Facebook,
                                accessToken), AppConstant.URL_LOGIN, new LoginFlowBaseFragment.LoginCallbacks() {
                            @Override
                            public void loginSuccess(JSONObject loginSuccessObject) {
                                setFragmentStackEntryCount(loginSuccessObject);
                                if (mCallback != null) {
                                    mCallback.userAuthenticationLoginSuccess(loginSuccessObject);
                                }
                            }

                            @Override
                            public void loginFailure(JSONObject loginFailureObject) {
                                if (mCallback != null) {
                                    mCallback.userAuthenticationLoginFailure(loginFailureObject);
                                }
                            }
                        });
        }
    }


    @Override
    public void onCancel() {

    }


    @Override
    public void onError(FacebookException error) {
        // Hide Progress Bar
        relativeLayoutGplusLoginInProcess.setVisibility(View.GONE);
        relativeLayoutFacebookLoginInProcess.setVisibility(View.GONE);

        JSONObject object = new JSONObject();
        try {
            object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, LoginType.Facebook);
            String errorMsg;
            if (AppUtil.hasNetworkConnection(getActivity())) {
                errorMsg = getResources().getString(R.string.fb_error);
            } else {
                errorMsg = getResources().getString(R.string.no_network_connection);
            }
            // hide keyboard and show toast
            hideKeyboardAndShowToast(getActivity(), errorMsg);

            object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, errorMsg);
        } catch (JSONException e) {
            // exception
        }

        if (mCallback != null) {
            mCallback.userAuthenticationLoginFailure(object);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mCallback != null) {
            // Hide Progress Bar
            relativeLayoutGplusLoginInProcess.setVisibility(View.GONE);
            relativeLayoutFacebookLoginInProcess.setVisibility(View.GONE);

            JSONObject object = new JSONObject();
            try {
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, LoginType.GOOGLE);
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, getResources().getString(R.string.google_plus_error));
            } catch (JSONException e) {
                // object
            }

            if (mCallback != null) {
                mCallback.userAuthenticationLoginFailure(object);
            }
        }
    }


    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        if (getActivity() == null || getView() == null)
            return;

        try {
            if (request.getIdentifier() == VALIDATE_ACCOUNT_API) {
                if (responseObject != null) {
                    JSONObject responseObj = new JSONObject(responseObject);

                    if (responseObj.optBoolean("status")) {
                        if (responseObj.
                                optJSONObject("output_params") != null) {

                            handleValidateAccountResponse(responseObj.
                                    optJSONObject("output_params").optJSONObject("data"));
                        }

                    }else{
                        hideKeyboardAndShowToast(getActivity(), responseObj.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG));
                    }

                } else {
                    hideKeyboardAndShowToast(getActivity(), getResources().getString(R.string.login_error_message));
                }

            } else if (request.getIdentifier() == GOOGLES_SIGNIN_API || request.getIdentifier() == FB_SIGNIN_API) {
                super.onResponse(request, responseObject, response);
            }

        } catch (Exception e) {
            hideKeyboardAndShowToast(getActivity(), getResources().getString(R.string.login_error_message));
        }
    }


    private void handleValidateAccountResponse(JSONObject data) {
        String source="";
        if(!TextUtils.isEmpty(data.optString("email"))){
            source="email";
        }else if(!TextUtils.isEmpty(data.optString("phone"))){
           source="phone";
        }
        DOPreferences.setloginSource(getContext(),source);
        if (data != null) {
            if (!AppUtil.isStringEmpty(data.optString("type"))) {
                if (data.optString("type").equalsIgnoreCase("login")) {
                    processLogin(data);
                }
                else if (data.optString("type").equalsIgnoreCase("create_account")) {
                    processSignUp(data);
                }

            }
        }

    }


    private void processLogin(JSONObject data) {

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(AppConstant.BUNDLE_GUEST_PHONE, data.optString("phone"));
        bundle.putString(AppConstant.BUNDLE_GUEST_EMAIL, data.optString("email"));
        bundle.putInt(AppConstant.BUNDLE_GUEST_LOGIN_VIA_OTP, data.optInt("login_via_otp"));

        EnterPasswordPhoneFragment frag = EnterPasswordPhoneFragment.newInstance(bundle, mCallback);
        frag.setArguments(bundle);

        addToBackStack(getFragmentManager(), frag,
                R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);


    }

    private void processSignUp(JSONObject data) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(AppConstant.BUNDLE_GUEST_PHONE, data.optString("phone"));
        bundle.putString(AppConstant.BUNDLE_GUEST_EMAIL, data.optString("email"));
        bundle.putInt(AppConstant.BUNDLE_GUEST_LOGIN_VIA_OTP, data.optInt("login_via_otp"));

        CreateAccountFragment frag = CreateAccountFragment.newInstance(bundle, mCallback);
        frag.setArguments(bundle);
        addToBackStack(getFragmentManager(), frag, R.anim.f_enter, R.anim.f_exit);

    }

    private void validationFailure(String cause) {

        UiUtil.showToastMessage(getContext(),cause);

    }


        @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Hide Progress Bar
        relativeLayoutGplusLoginInProcess.setVisibility(View.GONE);
        relativeLayoutFacebookLoginInProcess.setVisibility(View.GONE);

        if (requestCode == SIGNIN_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null) {
                handleGoogleSignInResult(result);
            }

            // sign out so that google account popup is displayed each time
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            }

        }
    }


    private void handleGoogleSignInResult(GoogleSignInResult result) {

        // Check for SUCCESS
        if (result.isSuccess()) {
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();

            if (googleSignInAccount != null) {
                String idToken = googleSignInAccount.getIdToken();

                makeLoginRequest(GOOGLES_SIGNIN_API,
                        ApiParams.getSocialLoginParameters(LoginType.GOOGLE, idToken),
                        AppConstant.URL_LOGIN, new LoginFlowBaseFragment.LoginCallbacks() {
                            @Override
                            public void loginSuccess(JSONObject loginSuccessObject) {
                                // set stack count
                                setFragmentStackEntryCount(loginSuccessObject);

                                if(mCallback != null) {
                                    mCallback.userAuthenticationLoginSuccess(loginSuccessObject);
                                }
                            }

                            @Override
                            public void loginFailure(JSONObject loginFailureObject) {
                                if (mCallback != null) {
                                    mCallback.userAuthenticationLoginFailure(loginFailureObject);
                                }
                            }
                        });
            }
        } else {
            JSONObject object = new JSONObject();
            try {
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, LoginType.GOOGLE);
                String errorMsg;
                if (AppUtil.hasNetworkConnection(getActivity())) {
                    errorMsg = getResources().getString(R.string.google_plus_error);
                } else {
                    errorMsg = getResources().getString(R.string.no_network_connection);
                }
                // hide keyboard and show toast
                hideKeyboardAndShowToast(getActivity(), errorMsg);

                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, errorMsg);
            } catch (JSONException e) {
                // exception
            }

            if (mCallback != null) {
                mCallback.userAuthenticationLoginFailure(object);
            }
        }
    }

        @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    private void navigateToWebView(String title, String url) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        webViewFragment.setArguments(bundle);
        addToBackStack(getActivity(), webViewFragment);
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
