package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.adapter.InvoiceAdapter;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;


import org.json.JSONObject;

import java.util.HashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by prateek.aggarwal on 2/25/16.
 */
public class InvoiceFragment extends AuthenticationWrapperJSONReqFragment {

    public static final String INVOICE_RESPONSE_KEY = "invoice";
    private final int REQUEST_CODE_INVOICE = 0x06;
    private RecyclerView mInvoiceList;

    public static InvoiceFragment newInstance(Bundle bundle) {
        InvoiceFragment fragment = new InvoiceFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        //trackScreenToGA("PaymentInvoice");

        // Set Style
        //AppUtil.setHoloLightTheme(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invoice, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mInvoiceList = (RecyclerView) view.findViewById(R.id.invoice_list);
        mInvoiceList.setLayoutManager(new LinearLayoutManager(getContext()));
        String transactionId = getArguments().getString("d_invoice");
        if (!TextUtils.isEmpty(getArguments().getString(INVOICE_RESPONSE_KEY))) {
            String invoiceBreakUp = getArguments().getString(INVOICE_RESPONSE_KEY);
            try {
                JSONObject breakup = new JSONObject(invoiceBreakUp);
                mInvoiceList.setAdapter(new InvoiceAdapter(getContext(),
                        breakup));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(!TextUtils.isEmpty(transactionId)){
            getPaymentInvoice(transactionId);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Payment Invoice");
    }


//    @Override
//    public void handleNavigation() {
//        super.handleNavigation();
//        if(!TextUtils.isEmpty(getArguments().getString("source"))){
//            if(getArguments().getString("source").equalsIgnoreCase("TransactionHistory")){
//                dismiss();
//            }else{
//                popBackStack(getFragmentManager());
//            }
//        }
//
//    }


    private void getPaymentInvoice(String transID) {

        showLoader();

        Map<String, String> param = new HashMap<>();
        param.put("diner_id", DOPreferences.getDinerId(getContext()));
        param.put("trans_id", transID);

        getNetworkManager().stringRequestPost(REQUEST_CODE_INVOICE,
                AppConstant.URL_GET_PAYMENT_INVOICE, param, new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

                        hideLoader();

                        handleInvoiceResponse(responseObject);
                    }
                }, this, false);
    }


    private void handleInvoiceResponse(String responseObject) {

        if (getActivity() == null || getView() == null)
            return;

        try {
            JSONObject resp = new JSONObject(responseObject);
            if (resp != null && resp.optBoolean("status")) {

                JSONObject outputParam = resp.optJSONObject("output_params");
                if (outputParam != null) {
                    JSONObject data = outputParam.optJSONObject("data");

                    JSONObject breakup = new JSONObject(data.toString());
                    mInvoiceList.setAdapter(new InvoiceAdapter(getContext(),
                            breakup));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_transaction),"SuccessfulTransactionInvioceBackClick","SuccessfulTransactionInvioceBackClick",hMap);
        super.handleNavigation();
    }
}
