package com.dineout.book.fragment.promovoucher;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;

public class GiftVouchersFragment extends YouPageWrapperFragment
        implements View.OnClickListener {

    private final int REQUEST_CODE_VERIFY_REFERRAL_API = 101;

    private View parentViewGiftVoucher;
    private EditText editTextViewGiftCode;
    private EditText editTextViewUserPin;
    private AlertDialog submitVoucherCodeDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gift_vouchers, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //track
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_giftvoucher),getString(R.string.d_giftvoucher),hMap);

        initiateView();

        authenticateUser();
    }

    private void initiateView() {
        // Get Verify Button
        Button verifyButton = (Button) getView().findViewById(R.id.button_gift_voucher_verify);
        verifyButton.setOnClickListener(this);

        parentViewGiftVoucher = getView().findViewById(R.id.parentView_gift_voucher);
        editTextViewGiftCode = (EditText) getView().findViewById(R.id.editText_gift_voucher_code);
        editTextViewUserPin = (EditText) getView().findViewById(R.id.editText_gift_voucher_pin);

        TextView textViewGiftVoucherLink = (TextView) getView().findViewById(R.id.textView_gift_voucher_link);
        textViewGiftVoucherLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Get Id
        int viewId = view.getId();

        if (viewId == R.id.button_gift_voucher_verify) {
            handleVerifyGiftVoucherCodeClick();

        } else if (viewId == R.id.textView_submit_voucher_code_button) {
            handleSubmitVoucherCodeDialogOkClick();

        } else if (viewId == R.id.textView_gift_voucher_link) {
            handleGiftVoucherLinkClick();
        }
    }

    private void handleVerifyGiftVoucherCodeClick() {
        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());

        // Check if User Input is Valid
        if (isUserInputValid()) {

            //track for ga and countly
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_gift_voucher_code),editTextViewGiftCode.getText().toString(),hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_gift_pin),editTextViewUserPin.getText().toString(),hMap);
            trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_verify_click),getString(R.string.d_verify_click),hMap);


            //track for qgraph, mixpanel
            HashMap<String,Object> map= new HashMap<>();
            map.put("label",editTextViewGiftCode.getText().toString());
            trackEventQGraphApsalar(getString(R.string.d_gift_voucher_code),map,true,false,false);

            HashMap<String,Object>maps= new HashMap<>();
            map.put("label",getString(R.string.d_verify_click));
            trackEventQGraphApsalar(getString(R.string.d_verify_click),maps,true,false,false);

            // authenticate the user
            if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity()))) {
                UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
            } else {
                // submit voucher code
                submitVoucherCode();
            }
        }
    }

    private void handleGiftVoucherLinkClick() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_faq_click),getString(R.string.d_faq_click),hMap);

        // Set Bundle
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.text_gift_voucher_faqs));
        bundle.putString("url", AppConstant.DINEOUT_GIFT_VOUCHER_URL);

        // Get Instance
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.setArguments(bundle);

        addToBackStack(getParentFragment().getFragmentManager(), webViewFragment);
    }

    private boolean isUserInputValid() {
        // Get Gift Code
        String giftCode = editTextViewGiftCode.getText().toString();
        if (AppUtil.isStringEmpty(giftCode)) {
            UiUtil.showSnackbar(parentViewGiftVoucher, String.format(getString(R.string.container_is_required),
                    getString(R.string.text_voucher_code)), R.color.red_CC2);
            return false;

        } else if (giftCode.length() != 16) {
            UiUtil.showSnackbar(parentViewGiftVoucher, String.format(getString(R.string.container_enter_valid),
                    getString(R.string.text_voucher_code)), R.color.red_CC2);
            return false;
        }

        // Get Pin
        String userPin = editTextViewUserPin.getText().toString();
        if (AppUtil.isStringEmpty(userPin)) {
            UiUtil.showSnackbar(parentViewGiftVoucher, String.format(getString(R.string.container_is_required),
                    getString(R.string.text_pin)), R.color.red_CC2);
            return false;

        } else if (userPin.length() != 6) {
            UiUtil.showSnackbar(parentViewGiftVoucher, String.format(getString(R.string.container_enter_valid),
                    getString(R.string.text_pin)), R.color.red_CC2);
            return false;
        }

        return true;
    }

    private void handleSubmitVoucherCodeDialogOkClick() {
        if (submitVoucherCodeDialog != null) {
            submitVoucherCodeDialog.dismiss();
        }
    }

    private void submitVoucherCode() {
        // Show Loader
        showLoader();

        // Get Gift Voucher Code
        String giftCode = editTextViewGiftCode.getText().toString();

        // Get Pin
        String pin = editTextViewUserPin.getText().toString();

        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_CODE_VERIFY_REFERRAL_API, AppConstant.URL_RING_FENCING_VERYFY_REFERAL,
                ApiParams.getGiftVoucherVerifyReferralParams("qwikcilver", giftCode, pin),
                this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_CODE_VERIFY_REFERRAL_API) {
            if (responseObject != null) {
                boolean status = responseObject.optBoolean("status");

                if (status) {
                    JSONObject outputParams = responseObject.optJSONObject("output_params");

                    if (outputParams != null) {
                        JSONObject data = outputParams.optJSONObject("data");

                        if (data != null) {
                            String title = data.optString("title");
                            String message = data.optString("msg");

                            prepareSubmitVoucherCodeAlert(((AppUtil.isStringEmpty(title)) ? "Success" : title), message, true);

                            refreshUserInput();
                        }
                    }
                }

                // Track Event
//                trackEventGA(getString(R.string.ga_screen_gift_vouchers),
//                        getString((status) ? R.string.ga_action_success : R.string.ga_action_failed),
//                        editTextViewGiftCode.getText().toString());
            } else {
                // Show Message
                UiUtil.showToastMessage(getActivity().getApplicationContext(),
                        getString(R.string.text_general_error_message));
            }
        }
    }

    private void refreshUserInput() {
        editTextViewGiftCode.setText("");
        editTextViewUserPin.setText("");
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        // Show General Message
        UiUtil.showToastMessage(getActivity().getApplicationContext(),
                getString(R.string.text_general_error_message));
    }

    private void prepareSubmitVoucherCodeAlert(String title, String message, boolean isSuccess) {
        // Check for NULL
        if (submitVoucherCodeDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);

            View alertDialogView = LayoutInflater.from(getActivity().getApplicationContext()).
                    inflate(R.layout.dialog_submit_voucher_code_message, null, false);

            // Set Title
            if (!AppUtil.isStringEmpty(title)) {
                TextView titleTextView = (TextView) alertDialogView.findViewById(R.id.textView_submit_voucher_code_title);
                titleTextView.setText(title);
                titleTextView.setEnabled(isSuccess);
            }

            // Set Message
            if (!AppUtil.isStringEmpty(message)) {
                TextView messageTextView = (TextView) alertDialogView.findViewById(R.id.textView_submit_voucher_code_message);
                messageTextView.setText(message);
            }

            // Handle OK Click
            TextView textViewSubmitVoucherCodeButton = (TextView) alertDialogView.findViewById(R.id.textView_submit_voucher_code_button);
            textViewSubmitVoucherCodeButton.setOnClickListener(this);

            // Set View
            builder.setView(alertDialogView);

            // Show Alert Dialog
            submitVoucherCodeDialog = builder.create();
            submitVoucherCodeDialog.getWindow().
                    setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            submitVoucherCodeDialog.show();

        } else if (!submitVoucherCodeDialog.isShowing()) {
            submitVoucherCodeDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        parentViewGiftVoucher = null;
        editTextViewGiftCode = null;
        editTextViewUserPin = null;
        submitVoucherCodeDialog = null;
    }
}
