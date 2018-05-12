package com.dineout.book.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

public class DebugTestFragment extends MasterDOFragment implements View.OnClickListener {

    EditText editTextDebugPhpUrl;
    EditText editTextDebugNodeUrl;
    EditText editTextDebugPhpVersionUrl;
    EditText editTextDebugNodeVersionUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dineout_debug_test, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextDebugPhpUrl = (EditText) getView().findViewById(R.id.editText_debug_php_url);

        if (AppUtil.isStringEmpty(DOPreferences.getApiBaseUrl(getActivity().getApplicationContext()))) {
            ((TextView) getView().findViewById(R.id.textView_debug_set_php_url)).
                    setText(AppConstant.BASE_DOMAIN_URL);
            //DOPreferences.setApiBaseUrl(getActivity().getApplicationContext(), AppConstant.BASE_DOMAIN_URL);
        }
        else {

            ((TextView) getView().findViewById(R.id.textView_debug_set_php_url)).
                    setText(DOPreferences.getApiBaseUrl(getActivity().getApplicationContext()));
        }

        editTextDebugNodeUrl = (EditText) getView().findViewById(R.id.editText_debug_node_url);

        if (AppUtil.isStringEmpty(DOPreferences.getApiBaseUrlNode(getActivity().getApplicationContext()))) {
            ((TextView) getView().findViewById(R.id.textView_debug_set_node_url)).
                    setText(AppConstant.BASE_DOMAIN_URL_NODE);
            //DOPreferences.setApiBaseUrlNode(getActivity().getApplicationContext(), AppConstant.BASE_DOMAIN_URL_NODE);
        }
        else {

            ((TextView) getView().findViewById(R.id.textView_debug_set_node_url)).
                    setText(DOPreferences.getApiBaseUrlNode(getActivity().getApplicationContext()));
        }


        editTextDebugPhpVersionUrl = (EditText) getView().findViewById(R.id.editText_php_url);

        if (AppUtil.isStringEmpty(DOPreferences.getPhpBaseUrl(getActivity().getApplicationContext()))) {
            ((TextView) getView().findViewById(R.id.textView_set_php_url)).
                    setText(AppConstant.BASE_PHP_URL);
            //DOPreferences.setPhpBaseUrl(getActivity().getApplicationContext(), AppConstant.BASE_PHP_URL);
        }
        else {

            ((TextView) getView().findViewById(R.id.textView_set_php_url)).
                    setText(DOPreferences.getPhpBaseUrl(getActivity().getApplicationContext()));
        }


        editTextDebugNodeVersionUrl = (EditText) getView().findViewById(R.id.editText_node_url);

        if (AppUtil.isStringEmpty(DOPreferences.getNodeBaseUrl(getActivity().getApplicationContext()))) {
            ((TextView) getView().findViewById(R.id.textView_set_node_url)).
                    setText(AppConstant.NODE_BASE_URL);
            //DOPreferences.setNodeBaseUrl(getActivity().getApplicationContext(), AppConstant.NODE_BASE_URL);
        }
        else {

            ((TextView) getView().findViewById(R.id.textView_set_node_url)).
                    setText(DOPreferences.getNodeBaseUrl(getActivity().getApplicationContext()));
        }


        getView().findViewById(R.id.button_debug).setOnClickListener(this);

        // Show Details
        showDetails();
    }

    @Override
    public void onClick(View view) {
        // Handle PHP API
        String phpApiBaseUrl = editTextDebugPhpUrl.getText().toString();
//        phpApiBaseUrl = ((AppUtil.isStringEmpty(phpApiBaseUrl)) ?
//                DOPreferences.getApiBaseUrl(getActivity().getApplicationContext()) :
//                phpApiBaseUrl);
        if(!AppUtil.isStringEmpty(phpApiBaseUrl)) {

            DOPreferences.setApiBaseUrl(getActivity().getApplicationContext(), phpApiBaseUrl);
        }

        // Handle NODE API
        String nodeApiBaseUrl = editTextDebugNodeUrl.getText().toString();
//        nodeApiBaseUrl = ((AppUtil.isStringEmpty(nodeApiBaseUrl)) ?
//                DOPreferences.getApiBaseUrlNode(getActivity().getApplicationContext()) :
//                nodeApiBaseUrl);

        if(!AppUtil.isStringEmpty(nodeApiBaseUrl)) {
            DOPreferences.setApiBaseUrlNode(getActivity().getApplicationContext(), nodeApiBaseUrl);
        }


        String phpVersionUrl = editTextDebugPhpVersionUrl.getText().toString();
//        phpVersionUrl = ((AppUtil.isStringEmpty(phpVersionUrl)) ?
//                DOPreferences.getPhpBaseUrl(getActivity().getApplicationContext()) :
//                phpVersionUrl);
        if(!AppUtil.isStringEmpty(phpVersionUrl)) {

            DOPreferences.setPhpBaseUrl(getActivity().getApplicationContext(), phpVersionUrl);
        }

        String nodeBaseUrl = editTextDebugNodeVersionUrl.getText().toString();
//        nodeBaseUrl = ((AppUtil.isStringEmpty(nodeBaseUrl)) ?
//                DOPreferences.getNodeBaseUrl(getActivity().getApplicationContext()) :
//                nodeBaseUrl);

        if(!AppUtil.isStringEmpty(nodeBaseUrl)) {
            DOPreferences.setNodeBaseUrl(getActivity().getApplicationContext(), nodeBaseUrl);
        }

        // Hide Keyboard
        AppUtil.hideKeyboard(view, getActivity());

        String str = null;
        str.length();
    }

    private void showDetails() {
        // GCM Token
        String gcmRegToken = DOPreferences.getGcmRegistrationToken(getActivity().getApplicationContext());
        if (!AppUtil.isStringEmpty(gcmRegToken)) {
            TextView textViewDebugGcmToken = (TextView) getView().findViewById(R.id.textView_debug_gcm_token);
            textViewDebugGcmToken.setText(gcmRegToken);
            textViewDebugGcmToken.setVisibility(View.VISIBLE);
        }

        // Google Ad Id
        String googleAdId = DOPreferences.getGoogleAdId(getActivity().getApplicationContext());
        if (!AppUtil.isStringEmpty(googleAdId)) {
            TextView textViewDebugGoogleAdId = (TextView) getView().findViewById(R.id.textView_debug_google_ad_id);
            textViewDebugGoogleAdId.setText(googleAdId);
            textViewDebugGoogleAdId.setVisibility(View.VISIBLE);
        }

        // Device Id
        String deviceId = DOPreferences.getDeviceId(getActivity().getApplicationContext());
        if (!AppUtil.isStringEmpty(deviceId)) {
            TextView textViewDebugDeviceId = (TextView) getView().findViewById(R.id.textView_debug_device_id);
            textViewDebugDeviceId.setText(deviceId);
            textViewDebugDeviceId.setVisibility(View.VISIBLE);
        }

        // Dinner Id
        String dinnerId = DOPreferences.getDinerId(getActivity().getApplicationContext());
        if (!AppUtil.isStringEmpty(dinnerId)) {
            TextView textViewDebugDinnerId = (TextView) getView().findViewById(R.id.textView_debug_dinner_id);
            textViewDebugDinnerId.setText(dinnerId);
            textViewDebugDinnerId.setVisibility(View.VISIBLE);
        }
    }
}
