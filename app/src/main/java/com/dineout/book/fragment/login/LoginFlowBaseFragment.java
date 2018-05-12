package com.dineout.book.fragment.login;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.analytics.tracker.AnalyticsHelper;
import com.analytics.utilities.AnalyticsUtil;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.activity.MasterDOLauncherActivity;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import in.til.core.TilSDK;
import io.branch.referral.Branch;


public class LoginFlowBaseFragment extends MasterDOStringReqFragment {

    protected final int GOOGLES_SIGNIN_API = 0x405;
    protected final int FB_SIGNIN_API = 0x410;
    protected final int NATIVE_SIGNIN_API = 0x425;

    public final static String DO_LOGIN_RECEIVER = "com.dineout.book.login";
    protected LoginCallbacks mCallback;
  
    String CHILD_CONSUMED_EVENT = "is_event_consumed_by_child";

    public interface LoginType {

        String Facebook = "fb";

        String GOOGLE = "google";

        String NATIVE = "native";

        String NONE_CANCELLED = "cancelled";

    }



    public HashMap<String, Object> getUserDataJSONObject(Context context) {
        HashMap<String, Object> props = new HashMap<>();

        if (context != null &&
                !TextUtils.isEmpty(DOPreferences.getDinerId(context.getApplicationContext()))) {

            props.put("email", DOPreferences.getDinerEmail(context.getApplicationContext()));
            props.put("name", DOPreferences.getDinerFirstName(context) + " " +
                    DOPreferences.getDinerLastName(context));
            props.put("avatarUrl", DOPreferences.getDinerProfileImage(context));
            props.put("phone", DOPreferences.getDinerPhone(context));
            props.put("isDineoutPlusMember", DOPreferences.isDinerDoPlusMember(context));
            props.put("dinnerId", DOPreferences.getDinerId(context));
            props.put("cityId", DOPreferences.getCityId(context));
        }

        return props;
    }


    public String getTypeOnRequestId(int id) {

        if (id == GOOGLES_SIGNIN_API) {
            return LoginType.GOOGLE;
        } else if (id == FB_SIGNIN_API) {
            return LoginType.Facebook;
        } else if (id == NATIVE_SIGNIN_API) {
            return LoginType.NATIVE;
        }

        return LoginType.NATIVE;
    }

    public void makeLoginRequest(int id, HashMap<String, String> params,String url, LoginCallbacks callback) {
        if(getActivity()==null)
            return;
        this.mCallback = callback;
        AppUtil.hideKeyboard(getActivity());
        showLoader();
        ((MasterDOLauncherActivity) getActivity()).getNetworkManager().stringRequestPost(id, url,
                params, this, this, false);
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject == null) {
            // hide keyboard and show toast
            hideKeyboardAndShowToast(getActivity(), getResources().getString(R.string.login_error_message));

            JSONObject object = new JSONObject();
            try {
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, getTypeOnRequestId(request.getIdentifier()));
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, getResources().getString(R.string.login_error_message));
            } catch (JSONException e) {
                // object
            }

            if (mCallback != null) {
                mCallback.loginFailure(object);
            }
        }
        try {
            JSONObject responseObj = new JSONObject(responseObject);
            if(responseObj.optJSONObject("res_auth")!=null){
                if (responseObj.optJSONObject("res_auth").optBoolean("status")) {

                    DOPreferences.setAuthKey(getActivity(),
                            responseObj.optJSONObject("res_auth").optString("ak"));
                }
            }

            if (responseObj.optBoolean("status")) {
                handleLoginResponse(responseObj.
                        optJSONObject("output_params").optJSONObject("data"), getTypeOnRequestId(request.getIdentifier()));

//                //save login date
//                String dateDetails = AnalyticsUtil.getDateFormat(System.currentTimeMillis());
//                DOPreferences.setloginDate(getContext(),dateDetails);

                // set login data for app resume and app pause
                DOPreferences.setLoginDateForFirstTimeAppOpen(getContext(), AnalyticsUtil.getDateFormat(System.currentTimeMillis()));
                DOPreferences.setLoginDateForFirstTimeAppClose(getContext(), AnalyticsUtil.getDateFormat(System.currentTimeMillis()));

                JSONObject object = new JSONObject();
                try {
                    object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, getTypeOnRequestId(request.getIdentifier()));
                    object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_MSG, "Login successful");

                    if (mCallback != null) {
                        mCallback.loginSuccess(object);
                    }
                } catch (JSONException e) {
                    // object
                }

            } else {
                JSONObject object = new JSONObject();
                try {
                    object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, getTypeOnRequestId(request.getIdentifier()));
                    object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, (TextUtils.isEmpty(responseObj.optString("error_msg")) ?
                            getResources().getString(R.string.login_error_message) : responseObj.optString("error_msg")));

                    // hide keyboard and show toast
                    hideKeyboardAndShowToast(getActivity(), responseObj.optString("error_msg"));

                } catch (Exception e) {
                    // object
                }

                if (mCallback != null) {
                    mCallback.loginFailure(object);
                }
            }
        } catch (Exception e) {
            JSONObject object = new JSONObject();
            try {
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, getTypeOnRequestId(request.getIdentifier()));
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, getResources().getString(R.string.login_error_message));

                // hide keyboard and show toast
                hideKeyboardAndShowToast(getActivity(), getResources().getString(R.string.login_error_message));
            } catch (Exception e1) {
                // object
            }

            if (mCallback != null) {
                mCallback.loginFailure(object);
            }
        }

        mCallback = null;
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        // hide loader
        hideLoader();

        JSONObject object = new JSONObject();

        if (getActivity() != null) {
            try {
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE, getTypeOnRequestId(request.getIdentifier()));

                String errorMsg = "";
                if (getResources() != null) {
                    if (AppUtil.hasNetworkConnection(getActivity())) {
                        errorMsg = getResources().getString(R.string.login_error_message);
                    } else {
                        errorMsg = getResources().getString(R.string.no_network_connection);

                        // hide keyboard and show toast
                        hideKeyboardAndShowToast(getActivity(), errorMsg);
                    }
                }
                object.put(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG, errorMsg);
            } catch (JSONException e) {
                // object
            }
        }

        // send callback
        if (mCallback != null) {
            mCallback.loginFailure(object);
        }

        mCallback = null;
    }


    public void handleLoginResponse(JSONObject data, String type) {

        if (data == null)
            return;
        DOPreferences.saveDinerCredentials(getContext(), data);

        // Create User Details Map
        HashMap<String, Object> props = getUserDataJSONObject(getContext());
       // props.put("Medium", type.equalsIgnoreCase("native") ? "Email" : type);

        if(props!=null){
            props.put("signedIn",true);
            props.put("medium",DOPreferences.getLoginSource(getContext()));
        }

        //track login adtech event
        AnalyticsHelper.getAnalyticsHelper(getContext()).sendLoginAdTechEvent();
        AnalyticsHelper.getAnalyticsHelper(getContext()).completeAdTechSession();

        //track ga, qgraph,branch
        HashMap<String,String>hMap=DOPreferences.getGeneralEventParameters(getContext());

        if(hMap!=null){
            hMap.put("medium",DOPreferences.getLoginSource(getContext()));
            hMap.put("city",DOPreferences.getCityName(getContext()));
            hMap.put("isDOPlusMember",DOPreferences.isDinerDoPlusMember(getContext()));
        }

        trackEventForCountlyAndGA("L_SignedIn","SignedIn","SignedIn",hMap);

        trackEventQGraphApsalar("SignedIn",props,true,true,true);

        //branch
        Branch.getInstance(getActivity().getApplicationContext()).
                userCompletedAction("SignedIn");

        // Send User Attributes
        AnalyticsHelper.getAnalyticsHelper(getContext()).sendUserAttributes();
    }

    public interface LoginCallbacks {
        void loginSuccess(JSONObject loginSuccessObject);

        void loginFailure(JSONObject loginFailureObject);
    }

    public void setFragmentStackEntryCount(JSONObject object) {
        int fragmentStackEntryCount = 0;
        if (getArguments() != null) {
            fragmentStackEntryCount= getArguments().getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        }
        if (object != null) {
            try {
                object.put(UserAuthenticationController.FRAG_STACK_COUNT_KEY, fragmentStackEntryCount);
            } catch (JSONException e) {
                // exception
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppUtil.hideKeyboard(getActivity());
    }


    public void hideKeyboardAndShowToast(FragmentActivity context, String msg) {
        if (context != null) {
            // hide keyboard
            AppUtil.hideKeyboard(context);

            if (!TextUtils.isEmpty(msg)) {
                UiUtil.showToastMessage(context, msg);
            }
        }
    }
}
