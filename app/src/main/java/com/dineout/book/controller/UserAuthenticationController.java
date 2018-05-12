package com.dineout.book.controller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.login.OTPFlowFragment;
import com.dineout.book.fragment.login.UpdateEmailFragment;
import com.dineout.book.fragment.login.UpdateMobileNoFragment;
import com.dineout.book.fragment.login.ValidateAccountFragment;
import com.dineout.book.fragment.login.VerifyMobileNoFragment;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.dialogs.LoginSessionExpireDialog;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;



public class UserAuthenticationController implements UserAuthenticationCallback {
    public static final String LOGIN_FLOW = "login_flow";
    public static final String SIGN_UP_FLOW = "sign_up_flow";
    public static final String OTP_FLOW = "otp_flow";
    public static final String FRAG_STACK_COUNT_KEY = "frag_stack_count";
    public static final String FROM_SCREEN = "from_screen";

    private FragmentActivity mContext;
    private Bundle mBundle;

    private LoginFlowCompleteCallbacks mLoginFlowCompleteCallbacks;
    private OtpFlowCompleteCallback mOTPFlowCompleteCallback;
    private static UserAuthenticationController mInstance;

    private int mFragmentStackCountBeforeStartFlow;


    // interfaces
    public interface LoginFlowCompleteCallbacks {
        void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject);

        void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject);
    }

    public interface OtpFlowCompleteCallback {
        void otpFlowCompleteSuccess(JSONObject otpFlowCompleteSuccessObject);

        void otpFlowCompleteFailure(JSONObject otpFlowCompleteFailureObject);
    }

    private UserAuthenticationController() {}

    private void setContext(FragmentActivity context) {
        this.mContext = context;
    }

    public static UserAuthenticationController getInstance(@NonNull FragmentActivity context) {
        if (mInstance == null) {
            mInstance = new UserAuthenticationController();
        }
        mInstance.setContext(context);

        return mInstance;
    }

    public void startLoginFlow(Bundle bundle, LoginFlowCompleteCallbacks callback) {
        this.mLoginFlowCompleteCallbacks = callback;
        this.mBundle = bundle;
        this.mFragmentStackCountBeforeStartFlow = getCurrentFragmentStackCount(mContext);

        if (mBundle == null) {
            mBundle = new Bundle();
        }

        initiateLogin(mBundle);
    }

    public void startOTPFlow(Bundle bundle, OtpFlowCompleteCallback callback) {
        this.mOTPFlowCompleteCallback = callback;
        this.mBundle = bundle;
        this.mFragmentStackCountBeforeStartFlow = getCurrentFragmentStackCount(mContext);

        if (mBundle == null) {
            mBundle = new Bundle();
        }

        initiateOTP(mBundle);
    }


    public void startOTPVerificationFlow(Bundle bundle, OtpFlowCompleteCallback callback) {
        this.mOTPFlowCompleteCallback = callback;
        this.mBundle = bundle;
        this.mFragmentStackCountBeforeStartFlow = getCurrentFragmentStackCount(mContext);

        if (mBundle == null) {
            mBundle = new Bundle();
        }

        initiateOTPVerification(mBundle);
    }

    public void startMyAccountEmailVerifyUpdateFlow(Bundle bundle, OtpFlowCompleteCallback callback) {
        this.mOTPFlowCompleteCallback = callback;
        this.mBundle = bundle;
        this.mFragmentStackCountBeforeStartFlow = getCurrentFragmentStackCount(mContext);

        if (mBundle != null) {
            mBundle.putString(FROM_SCREEN, OTP_FLOW);
            mBundle.putInt(FRAG_STACK_COUNT_KEY, 0);
        }

        MasterDOFragment fragment = UpdateEmailFragment.newInstance(mBundle, this);
        MasterDOFragment.addToBackStack(mContext, fragment);
    }

    public void startMyAccountMobileVerifyUpdateFlow(Bundle bundle, LoginFlowCompleteCallbacks callback) {
        this.mLoginFlowCompleteCallbacks = callback;
        this.mBundle = bundle;
        this.mFragmentStackCountBeforeStartFlow = getCurrentFragmentStackCount(mContext);

        if (mBundle != null) {
            mBundle.putString(FROM_SCREEN, OTP_FLOW);
            mBundle.putInt(FRAG_STACK_COUNT_KEY, 0);
        }

        MasterDOFragment fragment = UpdateMobileNoFragment.newInstance(mBundle, this);
        MasterDOFragment.addToBackStack(mContext, fragment);
    }

    private void initiateLogin(Bundle bundle) {
        if (bundle != null) {
            bundle.putString(FROM_SCREEN, LOGIN_FLOW);
            bundle.putInt(FRAG_STACK_COUNT_KEY, 0);
        }
        MasterDOFragment fragment = ValidateAccountFragment.newInstance(bundle, this);
        MasterDOFragment.addToBackStack(mContext, fragment);
    }


    private void initiateOTP(Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(FROM_SCREEN, OTP_FLOW);
        bundle.putInt(FRAG_STACK_COUNT_KEY, 0);

        MasterDOFragment fragment = OTPFlowFragment.newInstance(bundle, this);
        MasterDOFragment.addToBackStack(mContext, fragment);
    }


    private void initiateOTPVerification(Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(FROM_SCREEN, OTP_FLOW);
        bundle.putInt(FRAG_STACK_COUNT_KEY, 0);

        MasterDOFragment fragment = VerifyMobileNoFragment.newInstance(bundle, this);
        MasterDOFragment.addToBackStack(mContext, fragment);
    }

    private int getCurrentFragmentStackCount(FragmentActivity context) {
        int count = -1;
        FragmentManager mManager;
        if (context != null && (mManager = context.getSupportFragmentManager()) != null) {
            count = mManager.getBackStackEntryCount();
        }

        return count;
    }

    @Override
    public void userAuthenticationLoginSuccess(JSONObject loginSuccessObject) {
        // navigate back to the same fragment from where login flow as called

        if (mFragmentStackCountBeforeStartFlow != -1) {
            FragmentManager manager;
            if (mContext != null && (manager = mContext.getSupportFragmentManager()) != null) {
                int currentBackStackEntryCount = manager.getBackStackEntryCount();
                if (currentBackStackEntryCount >= mFragmentStackCountBeforeStartFlow) {
                    FragmentManager.BackStackEntry entry;
                    if ((entry = manager.getBackStackEntryAt(mFragmentStackCountBeforeStartFlow)) != null) {
                        manager.popBackStackImmediate(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
            }
        }

        if (mLoginFlowCompleteCallbacks != null) {
            mLoginFlowCompleteCallbacks.loginFlowCompleteSuccess(loginSuccessObject);
        }
    }

    @Override
    public void userAuthenticationOTPSuccess(JSONObject otpSuccessObject) {
        // navigate back to the same fragment from where otp flow as called

        if (mFragmentStackCountBeforeStartFlow != -1) {
            FragmentManager manager;
            if (mContext != null && (manager = mContext.getSupportFragmentManager()) != null) {
                int currentBackStackEntryCount = manager.getBackStackEntryCount();
                if (currentBackStackEntryCount >= mFragmentStackCountBeforeStartFlow) {
                  FragmentManager.BackStackEntry entry;
                    if ((entry = manager.getBackStackEntryAt(mFragmentStackCountBeforeStartFlow)) != null) {
                        manager.popBackStackImmediate(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
            }
        }

        if (mOTPFlowCompleteCallback != null) {
            mOTPFlowCompleteCallback.otpFlowCompleteSuccess(null);
        }
    }


    @Override
    public void userAuthenticationOTPFailure(JSONObject otpFailureObject) {

    }


    @Override
    public void userAuthenticationLoginFailure(JSONObject loginFailureObject) {
        if (loginFailureObject != null) {
            if (mLoginFlowCompleteCallbacks != null) {
                mLoginFlowCompleteCallbacks.loginFlowCompleteFailure(loginFailureObject);
            }
        }
    }

    public void showSessionExpireDialog(String errorMsg, LoginSessionExpireDialog.LoginSessionExpiredButtonCallbacks callbacks) {
        if (mContext != null) {
            DOPreferences.deleteDinerCredentials(mContext);
            LoginSessionExpireDialog fragment = new LoginSessionExpireDialog();
            fragment.setLoginSessionExpireButtonCallback(callbacks);

            // set up bundle
            Bundle bundle = new Bundle();
            bundle.putString(LoginSessionExpireDialog.ERROR_MSG_KEY, errorMsg);
            fragment.setArguments(bundle);

            MasterDOFragment.showFragment(mContext.getSupportFragmentManager(), fragment);
        }
    }
}



