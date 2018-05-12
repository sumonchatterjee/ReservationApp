package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.util.PaymentUtils;
import com.dineout.book.adapter.StoredCardAdapter;
import com.payu.india.Interfaces.DeleteCardApiListener;
import com.payu.india.Interfaces.GetStoredCardApiListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.DeleteCardTask;
import com.payu.india.Tasks.GetStoredCardTask;

public class StoredCardFragment extends PayUBaseFragment implements GetStoredCardApiListener, StoredCardAdapter.CVVValidListener, DeleteCardApiListener, View.OnClickListener {


    private RecyclerView mStoredCard;
    private StoredCardAdapter mStoredCardAdapter;
    private Button mPayNow;
    private StoredCard mSelectedCard;
    private String mCVV;
    private View mEmptyContainer;
    private View mStoredCardContainer;
    private View.OnClickListener mDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteCard((StoredCard) v.getTag());
        }
    };

    public static StoredCardFragment newInstance(Bundle bundle) {

        StoredCardFragment fragment = new StoredCardFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stored_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trackScreenToGA("PayNowSavedCards");
        mStoredCard = (RecyclerView) view.findViewById(R.id.stored_card_list);
        mStoredCard.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPayNow = (Button) view.findViewById(R.id.button_pay_now);
        mEmptyContainer = view.findViewById(R.id.empty_saved_container);
        mStoredCardContainer = view.findViewById(R.id.saved_card_container);
        mPayNow.setText(String.format(getString(R.string.pay_now_label),
                getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.FINAL_AMOUNT, "0")));
        mPayNow.setOnClickListener(this);
        mEmptyContainer.setVisibility(View.GONE);
        mStoredCardContainer.setVisibility(View.GONE);
        fetchStoredCard();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Saved Cards");
    }

    private void fetchStoredCard() {


        PayuHashes mPayuHashes = PaymentUtils.getPayUHashes(getArguments().getString(PayuConstants.PAYU_HASHES));
        PaymentParams mPaymentParams = PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS));
        PayuConfig payuConfig = PaymentUtils.getPayUConfig(getArguments().getString(PayuConstants.PAYU_CONFIG));
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_USER_CARDS);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
        merchantWebService.setHash(mPayuHashes.getStoredCardsHash());

        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // ok we got the post params, let make an api call to payu to fetch the payment related details

            payuConfig.setData(postData.getResult());
            payuConfig.setEnvironment(payuConfig.getEnvironment());
            showLoader();
            GetStoredCardTask getStoredCardTask = new GetStoredCardTask(this);
            getStoredCardTask.execute(payuConfig);
        } else {
            Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteCard(StoredCard storedCard) {


        PayuHashes mPayuHashes = PaymentUtils.getPayUHashes(getArguments().getString(PayuConstants.PAYU_HASHES));
        PaymentParams mPaymentParams = PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS));
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.DELETE_USER_CARD);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
        merchantWebService.setVar2(storedCard.getCardToken());
        merchantWebService.setHash(mPayuHashes.getDeleteCardHash());

        PostData postData = null;
        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        PayuConfig payuConfig = PaymentUtils.getPayUConfig(getArguments().getString(PayuConstants.PAYU_CONFIG));
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // ok we got the post params, let make an api call to payu to fetch
            // the payment related details
            showLoader();
            payuConfig.setData(postData.getResult());
            payuConfig.setEnvironment(payuConfig.getEnvironment());

            DeleteCardTask deleteCardTask = new DeleteCardTask(this);
            deleteCardTask.execute(payuConfig);
        } else {
            Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetStoredCardApiResponse(PayuResponse payuResponse) {


        if (getActivity() == null) {
            return;
        }

        hideLoader();

        if (!payuResponse.isStoredCardsAvailable()) {
            mEmptyContainer.setVisibility(View.VISIBLE);
            mStoredCardContainer.setVisibility(View.GONE);

        } else {
            mEmptyContainer.setVisibility(View.GONE);
            mStoredCardContainer.setVisibility(View.VISIBLE);
            mStoredCardAdapter =
                    new StoredCardAdapter(getActivity().getApplicationContext(), payuResponse
                            .getStoredCards());
            mStoredCardAdapter.setCVVValidateListener(this);
            mStoredCardAdapter.setDeleteListener(mDeleteListener);
            mStoredCard.setAdapter(mStoredCardAdapter);
        }
    }

    @Override
    public void updateStoredCard(StoredCard card, String cvv) {

        mPayNow.setEnabled(card == null ? false : true);
        this.mSelectedCard = card;
        this.mCVV = cvv;
    }

    private void makePayment(StoredCard storedCard, String cvv) {
        PostData postData = new PostData();
        // lets try to get the post params
        postData = null;
        PayuHashes mPayuHashes = PaymentUtils.getPayUHashes(getArguments().getString(PayuConstants.PAYU_HASHES));
        PaymentParams mPaymentParams = PaymentUtils.getPaymentParams(getArguments().getString(PayuConstants.PAYMENT_PARAMS));
        PayuConfig payuConfig = PaymentUtils.getPayUConfig(getArguments().getString(PayuConstants.PAYU_CONFIG));
        storedCard.setCvv(cvv); // make sure that you set the cvv also
        mPaymentParams.setHash(mPayuHashes.getPaymentHash()); // make sure that you set payment hash
        mPaymentParams.setCardToken(storedCard.getCardToken());
        mPaymentParams.setCvv(cvv);
        mPaymentParams.setNameOnCard(storedCard.getNameOnCard());
        mPaymentParams.setCardName(storedCard.getCardName());
        mPaymentParams.setExpiryMonth(storedCard.getExpiryMonth());
        mPaymentParams.setExpiryYear(storedCard.getExpiryYear());

        payThroughPayU(payuConfig, mPaymentParams, PayuConstants.CC);


    }

    @Override
    public void onDeleteCardApiResponse(PayuResponse payuResponse) {
        if(getActivity() ==null)
            return;
        hideLoader();
        if (payuResponse.isResponseAvailable()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
        if (payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {
            fetchStoredCard();
        }
    }

    @Override
    public void onClick(View v) {

        if (mSelectedCard != null) {
            // Track Event
            AnalyticsHelper.getAnalyticsHelper(getActivity())
                    .trackEventGA(getString(com.dineout.book.R.string.ga_pay_now_saved_cards),
                            getString(com.dineout.book.R.string.ga_action_pay_now_card), null);
            makePayment(mSelectedCard, mCVV);
        }
    }
}
