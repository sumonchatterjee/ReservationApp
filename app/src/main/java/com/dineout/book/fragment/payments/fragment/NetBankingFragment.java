package com.dineout.book.fragment.payments.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.book.R;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;

import java.util.ArrayList;

public class NetBankingFragment extends PayUBaseFragment implements PaymentRelatedDetailsListener, View.OnClickListener, AdapterView.OnItemClickListener {

    private String bankcode;
    private PaymentDetails mSelectedBankDetails;
    private ListView mBankList;
    private PayUNetBankingAdapter mAdapter;
    private TextView mSelectedBank;
    private View mNetBankListParent;
    private View mSelectedBankParent;

    public static NetBankingFragment newInstance(Bundle bundle) {

        NetBankingFragment fragment = new NetBankingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void fetchBank() {

        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS)).getKey());
        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
        merchantWebService.setVar1(PaymentUtils.getPaymentParams(getArguments
                ().getString(PayuConstants.PAYMENT_PARAMS)).getUserCredentials() == null ?
                "default" : PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS)).getUserCredentials());

        merchantWebService.setHash(PaymentUtils.getPayUHashes(getArguments().getString(PayuConstants.PAYU_HASHES))
                .getPaymentRelatedDetailsForMobileSdkHash());


        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
        PayuConfig payuConfig = PaymentUtils.getPayUConfig(getArguments().getString(PayuConstants.PAYU_CONFIG));
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
           showLoader();
            GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(this);
            paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
        } else {
            Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenToGA("PayNowNetbanking");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_net_banking, container, false);
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
       hideLoader();
        if (payuResponse.isResponseAvailable() && payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) { // ok we are good to go
            if (payuResponse.isNetBanksAvailable()) { // okay we have net banks now.

                mAdapter = new PayUNetBankingAdapter(getActivity(), payuResponse.getNetBanks());
                mBankList.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNetBankListParent = view.findViewById(R.id.net_bank_list_parent);
        mSelectedBankParent = view.findViewById(R.id.selected_bank_parent);
        mBankList = (ListView) view.findViewById(R.id.bank_list);
        mSelectedBank = (TextView) view.findViewById(R.id.selected_bank);
        mSelectedBank.setOnClickListener(this);
        view.findViewById(R.id.button_pay_now).setOnClickListener(this);
        mBankList.setOnItemClickListener(this);

        Button paynow = (Button) view.findViewById(R.id.button_pay_now);
        paynow.setText(String.format(getString(R.string.pay_now_label),
                getArguments().getString(PaymentConstant.FINAL_AMOUNT, "0")));

        fetchBank();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle("Net Banking");
    }


    @Override
    public void onClick(View v) {
        // okey we need hash fist

        if (v.getId() == R.id.button_pay_now) {
            makePaymentThroughNB();

        } else {
            mSelectedBankParent.setVisibility(View.GONE);
            mNetBankListParent.setVisibility(View.VISIBLE);
        }
    }

    private void makePaymentThroughNB() {

        if (mSelectedBankDetails == null) {
            mSelectedBankParent.setVisibility(View.GONE);
            mNetBankListParent.setVisibility(View.VISIBLE);
            return;
        }
        PostData postData = new PostData();
        PayuHashes mPayuHashes = PaymentUtils.getPayUHashes(getArguments().getString(PayuConstants.PAYU_HASHES));
        PaymentParams mPaymentParams = PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS));
        PayuConfig payuConfig = PaymentUtils.getPayUConfig(getArguments().getString(PayuConstants.PAYU_CONFIG));
        mPaymentParams.setHash(mPayuHashes.getPaymentHash());
        mPaymentParams.setBankCode(mSelectedBankDetails.getBankCode());
        payThroughPayU(payuConfig, mPaymentParams, PayuConstants.NB);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //TODO get bank code
        if (mAdapter != null && mAdapter.getItem(position) != null) {
            mSelectedBankDetails = (PaymentDetails) mAdapter.getItem(position);
            mSelectedBank.setText(mSelectedBankDetails.getBankName());
            mSelectedBankParent.setVisibility(View.VISIBLE);
            mNetBankListParent.setVisibility(View.GONE);
        }
    }


    private class PayUNetBankingAdapter extends BaseAdapter {
        Context mContext;
        ArrayList<PaymentDetails> mNetBankingList;

        public PayUNetBankingAdapter(Context context, ArrayList<PaymentDetails> netBankingList) {
            mContext = context;
            mNetBankingList = netBankingList;
        }

        @Override
        public int getCount() {
            return mNetBankingList.size();
        }

        @Override
        public Object getItem(int i) {
            if (null != mNetBankingList) return mNetBankingList.get(i);
            else return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NetbankingViewHolder netbankingViewHolder = null;
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.net_banking_item, null);
                netbankingViewHolder = new NetbankingViewHolder(convertView);
                convertView.setTag(netbankingViewHolder);
            } else {
                netbankingViewHolder = (NetbankingViewHolder) convertView.getTag();
            }

            PaymentDetails paymentDetails = mNetBankingList.get(position);

            // set text here
            netbankingViewHolder.netbankingTextView.setText(paymentDetails.getBankName());
            return convertView;
        }


        class NetbankingViewHolder {
            TextView netbankingTextView;

            NetbankingViewHolder(View view) {
                netbankingTextView = (TextView) view.findViewById(R.id.bank_label);
            }
        }
    }
}
