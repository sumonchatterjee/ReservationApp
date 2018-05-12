package com.dineout.book.fragment.payments.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.dialogs.DOShareDialog;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.dineout.book.fragment.mybookings.BookingDetailsFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.branch.referral.Branch;

public class PaymentStatusFragment extends MasterDOStringReqFragment implements View.OnClickListener, Response.Listener<String> {

    private final int GET_INOVICE_REQUEST = 0x10;
    private final int PAYMENT_STATUS_REQUEST = 0x15;
    private final int RATE_RESTAURANT_REQUEST = 0x200;
    private View mInvoiceContainer;
    private TextView mStatusMsg;
    private ImageView mStatusImage;
    private TextView mStatusTitle;
    private TextView mAmount;
    private TextView mTransId;
    private View mRootView;
    private TextView mDate;
    //private View mProgress;
    private TextView mDOPlusName, mDOPlusCard, mDOPlusValidity;
    private View mDOPlusContainer;
    private View mDOPlusCardContainer;
    private View mBookingInforContainer;
    private View mCheckInvoiceContainer;
    private Button mDone;
    private TextView mInvoiceText;
    private int mPaymentStatus;
    private LinearLayout dealsSharingLayout;
    private TextView dealsSharingTxtVw;
    private String deal, restaurantName, localityName, dinerName, bookingTime, bookingDate, tinyUrl, dealCount;
    private String mType;
    private AppEventsLogger logger;


    private View mDOInvoice;

    public static PaymentStatusFragment newInstance(Bundle bundle) {
        PaymentStatusFragment fragment = new PaymentStatusFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        //trackScreenToGA("PaymentStatus");
       // trackScreenName("P_Success");

        setHasOptionsMenu(true);
        logger = AppEventsLogger.newLogger(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_status, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        setToolbarTitle("Confirmation");

        mInvoiceText = (TextView) view.findViewById(R.id.invoice_text_doplus);
        NetworkImageView mCardBcg = (NetworkImageView) view.findViewById(R.id.do_bcg);
        mDOInvoice = view.findViewById(R.id.invoice_do_plus);
        mBookingInforContainer = view.findViewById(R.id.info_container_status);
        mRootView = view.findViewById(R.id.status_main_container);

        mStatusTitle = (TextView) view.findViewById(R.id.textViewPayFinalGreeting);
        mStatusMsg = (TextView) view.findViewById(R.id.textViewPayFinalMessage);
        mStatusImage = (ImageView) view.findViewById(R.id.image_status);
        mInvoiceContainer = view.findViewById(R.id.invoice_container);
        mAmount = (TextView) view.findViewById(R.id.status_bill_amount);
        mTransId = (TextView) view.findViewById(R.id.status_trans_id);

        mInvoiceContainer.setVisibility(View.GONE);
        mDone = (Button) view.findViewById(R.id.button_done);
        mDone.setOnClickListener(this);
        mInvoiceContainer.setOnClickListener(this);
        mDOInvoice.setOnClickListener(this);
        mDate = (TextView) view.findViewById(R.id.transaction_status_time);

        mDOPlusContainer = getView().findViewById(R.id.doplus_status_container);
        mDOPlusContainer.setVisibility(View.GONE);
        mDOPlusName = (TextView) getView().findViewById(R.id.doplus_status_member_name);
        mDOPlusCard = (TextView) getView().findViewById(R.id.doplus_status_member_card_number);
        mDOPlusValidity = (TextView) getView().findViewById(R.id.doplus_status_member_validity);
        dealsSharingLayout = (LinearLayout) view.findViewById(R.id.deal_success_share);
        dealsSharingTxtVw = (TextView) getView().findViewById(R.id.sharing_txt);
        dealsSharingTxtVw.setOnClickListener(this);
        getPaymentStatus();

        mCardBcg.setImageUrl(DOPreferences.getDoPlusCardBackground(getContext()), getImageLoader());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RATE_RESTAURANT_REQUEST && resultCode == Activity.RESULT_OK) {

            mDone.setText(getResources().getString(R.string.button_done));
            mDone.setTag(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private void initializeView(JSONObject data) {

        int status = mPaymentStatus;

        if (status == 1) {

            setToolbarTitle("Confirmation");

            mStatusImage.setImageResource(R.drawable.img_paid_stamp);
            ((LinearLayout) mAmount.getParent()).setVisibility(View.VISIBLE);
            mInvoiceContainer.setVisibility(View.VISIBLE);

            trackScreenName("P_Success");
            Branch.getInstance(getActivity().getApplicationContext()).
                    userCompletedAction("PaymentSuccessView");

            trackEventQGraphApsalar("PaymentSuccessView",new HashMap<String, Object>(),true,true,true);
            HashMap<String,String>hMap=DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("P_Success","PaymentSuccessView","",hMap);


        } else if (status == 0) {
            setToolbarTitle("Confirmation");
            mStatusImage.setImageResource(R.drawable.img_failed_stamp);
            mInvoiceContainer.setVisibility(View.GONE);

            trackScreenName("P_Failure");
            Branch.getInstance(getActivity().getApplicationContext()).
                    userCompletedAction("PaymentFailureView");

            HashMap<String,String>hMaps=DOPreferences.getGeneralEventParameters(getContext());
            trackEventQGraphApsalar("PaymentFailureView",new HashMap<String, Object>(),true,true,true);
            trackEventForCountlyAndGA("P_Failure","PaymentFailureView","",hMaps);

        } else if (status == 2) {
            setToolbarTitle("Payment in Progress");
            mStatusImage.setImageResource(R.drawable.img_inprogress_stamp);
            mInvoiceContainer.setVisibility(View.GONE);
        }

        mStatusTitle.setText(data.optString("title"));
        mStatusMsg.setText(data.optString("text"));
    }

    private void initializeView(boolean isSuccessful) {
        if (isSuccessful) {
            mStatusImage.setImageResource(R.drawable.img_paid_stamp);
            mStatusTitle.setText(getResources().getString(R.string.text_bill_paid_success));
            mStatusMsg.setText(getResources().getString(R.string.text_bill_payment_confirmation_message));
            mInvoiceContainer.setVisibility(View.VISIBLE);

        } else {
            mStatusImage.setImageResource(R.drawable.img_failed_stamp);
            mStatusTitle.setText(getResources().getString(R.string.text_bill_paid_failed));
            mStatusMsg.setText(getResources().getString(R.string.text_bill_payment_failure_message));

        }

        mInvoiceContainer.setVisibility(View.GONE);

        mDOPlusContainer.setVisibility(View.GONE);

        mAmount.setText(String.format(getResources().getString(R.string.container_rupee),
                getArguments().getString(PaymentConstant.FINAL_AMOUNT)));
        mTransId.setText(getArguments().getString(PaymentConstant.DISPLAY_ID));
//        mDate.setText(data.optString("date_time"));
    }

    private void getPaymentStatus() {

        mRootView.setVisibility(View.GONE);
        showLoader();
        Map<String, String> param = new HashMap<>();
        param.put("trans_id", getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID));
        //param.put("diner_id", DOPreferences.getDinerId(getContext()));
        //param.put("f", "1");

        getNetworkManager().stringRequestPost(PAYMENT_STATUS_REQUEST,
                AppConstant.URL_GET_PAYMENT_STATUS, param, this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                        initializeView(getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.PAYMENT_STATUS));
                    }
                }, false);
    }


    @Override
    public void onClick(View v) {

        if (v == mInvoiceContainer || v == mDOInvoice) {

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA("P_Success","ViewInvoiceClick",
                    "ViewInvoiceClick",hMap);

            getPaymentInvoice();
        } else if (v == dealsSharingTxtVw) {
           // trackEventGA("PaymentStatus", getString(R.string.ga_action_share_deals), null);
            shareDealStatus();
        } else {

            JSONObject action = (JSONObject) v.getTag();

            handleDoneButton(action);

        }

    }

    @Override
    public boolean onPopBackStack() {
        return true;
    }

    private void handleDoneButton(JSONObject object) {

        if (object == null) {
            finishPayment();

            return;
        }

        if (mPaymentStatus == 0) {
            popBackStack(getActivity().getSupportFragmentManager());
        }

        String action = object.optString("action");
        if (TextUtils.isEmpty(action)) {

            finishPayment();
        } else {
            if (action.equalsIgnoreCase("restaurant_rating")) {

               // trackEventGA("PaymentStatus", getString(R.string.ga_action_rate_restaurant), null);

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA("P_Success","RateRestaurantClick",
                        object.optString("rest_id"),hMap);

                Bundle bundle = new Bundle();
                bundle.putString("rest_name", object.optString("rest_name"));
                bundle.putString("rest_id", object.optString("rest_id"));
                bundle.putString("b_id", object.optString("b_id"));

                RestaurantReviewFragment frg = new RestaurantReviewFragment();
                frg.setArguments(bundle);
                frg.setTargetFragment(this, RATE_RESTAURANT_REQUEST);
                addToBackStack(getActivity(), frg);

//                WriteReviewFragment reviewFragment = new WriteReviewFragment();
//                reviewFragment.setArguments(bundle);
//                reviewFragment.setTargetFragment(this, RATE_RESTAURANT_REQUEST);
//                addToBackStack(getActivity(), reviewFragment);


            } else if (action.equalsIgnoreCase("booking_detail")) {

                //trackEventGA("PaymentStatus", getString(R.string.ga_action_view_booking_details), null);

                String bookingId = object.optString("booking_id");
                Bundle bundle = new Bundle();
                bundle.putString("b_id", bookingId);
                bundle.putString("type", object.optString("b_type"));
                BookingDetailsFragment bookingDetailFragment = new BookingDetailsFragment();
                bookingDetailFragment.setArguments(bundle);
                addToBackStack(getActivity(), bookingDetailFragment);


            } else {
                finishPayment();
            }
        }
    }

    private void finishPayment() {
        popToHome(getActivity());
        sendBroadcastToHomePage();

    }

    private void getPaymentInvoice() {


        showLoader();
        Map<String, String> param = new HashMap<>();
        param.put("diner_id", DOPreferences.getDinerId(getContext()));
        param.put("trans_id", getArguments().getString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID));
        param.put("f", "1");

        getNetworkManager().stringRequestPost(GET_INOVICE_REQUEST,
                AppConstant.URL_GET_PAYMENT_INVOICE, param, this, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                        Toast.makeText(getContext(), "Unable to get invoice.Please try again.", Toast.LENGTH_LONG).show();
                    }
                }, false);
    }


    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        mRootView.setVisibility(View.VISIBLE);

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == GET_INOVICE_REQUEST) {
            handleInvoiceResponse(responseObject);
        } else if (request.getIdentifier() == PAYMENT_STATUS_REQUEST) {
            handleStatusResponse(responseObject);
        }
    }

    private void handleStatusResponse(String responseObject) {

        try {

            JSONObject resp = new JSONObject(responseObject);
            if (resp != null ) {
                if (resp.optBoolean("status")) {
                    JSONObject outputParam = resp.optJSONObject("output_params");
                    if (outputParam != null) {
                        JSONObject data = outputParam.optJSONObject("data");
                        if (data != null) {
                            initializeStatusFragment(data);
                        } else {
                            initializeView(getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.PAYMENT_STATUS));
                        }
                    }
                }else{
                    initializeView(false);

                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            initializeView(getArguments().getBoolean(com.dineout.book.fragment.payments.PaymentConstant.PAYMENT_STATUS));
        }
    }

    private void handleInvoiceResponse(String responseObject) {


        try {
            JSONObject resp = new JSONObject(responseObject);
            if (resp != null) {
                if (resp.optBoolean("status")) {
                    JSONObject outputParam = resp.optJSONObject("output_params");
                    if (outputParam != null) {
                        JSONObject data = outputParam.optJSONObject("data");

                        Bundle bundle = new Bundle();
                        bundle.putString(com.dineout.book.fragment.payments.fragment.InvoiceFragment.INVOICE_RESPONSE_KEY, data.toString());
                        bundle.putString("source", "PaymentStatus");
                        addToBackStack(getActivity(), com.dineout.book.fragment.payments.fragment.InvoiceFragment.newInstance(bundle));
                    }
                }else{
                    Toast.makeText(getContext(), TextUtils.isEmpty(resp.optString("error_msg")) ?
                            "Some error in initializing payment" : resp.optString("error_msg"), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Unable to get invoice.Please try again.", Toast.LENGTH_LONG).show();

        }
    }


    private void initializeStatusFragment(JSONObject data) {

        JSONArray sectionArray = data.optJSONArray("section");
        JSONObject sectionDetails = data.optJSONObject("section_detail");
        mPaymentStatus = data.optInt("payment_status");
        if (sectionArray != null) {

            for (int i = 0; i < sectionArray.length(); i++) {

                JSONObject content = sectionArray.optJSONObject(i);


                if (content != null) {
                    String key = content.optString("section_key");
                    String type = content.optString("section_type");
                    if (type.equalsIgnoreCase("booking")|| type.equalsIgnoreCase("restaurant")) {


                        initializeBreakupView(sectionDetails.optJSONObject(key),
                                (sectionDetails.optJSONObject("section3") != null) ? sectionDetails.optJSONObject("section3").optString("rest_id") : "",
                                (sectionDetails.optJSONObject("section3") != null) ? sectionDetails.optJSONObject("section3").optString("rest_name") : "");
                    } else if (type.equalsIgnoreCase("doplus")) {
                        initializeDOPlusView(sectionDetails.optJSONObject(key));
                    } else if (type.equalsIgnoreCase("cell") ) {
                        initializeView(sectionDetails.optJSONObject(key));
                    } else if (type.equalsIgnoreCase("button")) {
                        initializeButtonContainer(sectionDetails.optJSONObject(key));
                    } else if (type.equalsIgnoreCase("deal") || (type.equalsIgnoreCase("event"))) {
                        mType = type;
                        initializeDealTypeView(sectionDetails, type, getTicketGroups(data));
                    }
                }
            }
        }
    }

    private void initializeBreakupView(JSONObject data,String resId,String name) {

        if (data == null) {
            mDate.setVisibility(View.GONE);
            mBookingInforContainer.setVisibility(View.GONE);
            dealsSharingLayout.setVisibility(View.GONE);
            return;
        }

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("RestaurantName", name);
        props.put("RestaurantID",resId);
        props.put("Amount",data.optString("bill_amount"));

        if (mPaymentStatus == 1) {

            Branch.getInstance(getActivity().getApplicationContext()).
                    userCompletedAction(getString(R.string.branch_paid_bill));

            // Apsalar event added
            //trackEventGA(getString(R.string.branch_paid_bill), props);


            //trackMixPanelEvent("PaidBill",props);
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status), getString(R.string.branch_paid_bill), resId);

            mBookingInforContainer.setVisibility(View.VISIBLE);
            mDOPlusContainer.setVisibility(View.GONE);
            mDate.setVisibility(View.VISIBLE);
            mInvoiceContainer.setVisibility(View.VISIBLE);
            mDate.setText(data.optString("date_time"));
            dealsSharingLayout.setVisibility(View.GONE);
        } else {
            //AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status), getString(R.string.branch_paid_bill_failure), null);


            mBookingInforContainer.setVisibility(View.GONE);
            mDOPlusContainer.setVisibility(View.GONE);
            mDate.setVisibility(View.GONE);
            mInvoiceContainer.setVisibility(View.GONE);
            dealsSharingLayout.setVisibility(View.GONE);
        }
        mAmount.setText(String.format(getResources().getString(R.string.container_rupee),
                data.optString("bill_amount")));
        mTransId.setText(data.optString("trans_id"));

//        Product product = new Product();
//        product.setId(resId);
//        product.setName(name);
//        ProductAction action = new ProductAction(ProductAction.ACTION_PURCHASE);
//        action.setTransactionId(data.optString("trans_id"));
//        action.setTransactionRevenue(data.optDouble("bill_amount"));
//
//        measureProductAction(product,action,"PaymentStatus");
    }

    private void initializeDOPlusView(JSONObject data) {

        if (data == null) {
            mDOPlusContainer.setVisibility(View.INVISIBLE);
            dealsSharingLayout.setVisibility(View.GONE);
            return;
        }

        if (mPaymentStatus == 1 && data != null) {
            //AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status), getString(R.string.ga_dop_card_bought), null);

            DOPreferences.setIsDinerDoPlusMember(getActivity(), "1");
            mDOPlusContainer.setVisibility(View.VISIBLE);
            dealsSharingLayout.setVisibility(View.GONE);
            mDate.setVisibility(View.GONE);
            mBookingInforContainer.setVisibility(View.GONE);
            mDOPlusName.setText(DOPreferences.getDinerFirstName(getContext()) + " " +
                    DOPreferences.getDinerLastName(getContext()));
            mDOPlusCard.setText(data.optString("card_number"));
            mDOPlusValidity.setText(data.optString("valid_till"));
        } else {
           // AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status), getString(R.string.ga_dop_card_failure), null);
            mDOPlusContainer.setVisibility(View.INVISIBLE);
            mDate.setVisibility(View.GONE);
            mBookingInforContainer.setVisibility(View.GONE);
            dealsSharingLayout.setVisibility(View.GONE);
        }


    }

    private void initializeDealTypeView(JSONObject data, String type, String ticketGroups) {
        String amount = data.optJSONObject("section1").optString("amount");

        String title = data.optJSONObject("section1").optString("title");
        mStatusTitle.setText(AppUtil.renderRupeeSymbol(title));
        // mStatusTitle.setText(String.format(title,getContext().getString(com.dineout.recycleradapters.R.string.container_rupee)) + " "+ amount);
        mStatusMsg.setText(data.optJSONObject("section1").optString("text"));

        if (data == null) {
            mDate.setVisibility(View.GONE);
            mDOPlusContainer.setVisibility(View.GONE);
            mBookingInforContainer.setVisibility(View.GONE);
            dealsSharingLayout.setVisibility(View.GONE);
            return;
        }

        if (mPaymentStatus == 1) {

            if (type.equalsIgnoreCase("event")){

                Branch.getInstance(getActivity().getApplicationContext()).
                        userCompletedAction(getString(R.string.branch_event_bought));

                //AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status), getString(R.string.branch_event_bought), null);

            }else if(type.equalsIgnoreCase("deal")){

                Branch.getInstance(getActivity().getApplicationContext()).
                        userCompletedAction(getString(R.string.branch_deal_bought));
                JSONObject shareParameters = data.optJSONObject("section2").optJSONObject("share_parameters");
//                AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status),
//                        getString(R.string.branch_deal_bought), shareParameters.optString("r_id") + "_" + ticketGroups);

            }


            mDOPlusContainer.setVisibility(View.GONE);
            mBookingInforContainer.setVisibility(View.GONE);
            dealsSharingLayout.setVisibility(View.VISIBLE);
            dealsSharingTxtVw.setText(data.optJSONObject("section2").optString("text"));

            JSONObject shareParameters = data.optJSONObject("section2").optJSONObject("share_parameters");
            if (shareParameters != null) {

                // Track apsalar,qgraph Event

                restaurantName = shareParameters.optString("restaurant_name");
                localityName = shareParameters.optString("locality_name");
                dinerName = shareParameters.optString("diner_name");
                bookingTime = shareParameters.optString("booking_time");
                bookingDate = shareParameters.optString("booking_date");
                tinyUrl = shareParameters.optString("tiny_url");
                if (type.equalsIgnoreCase("deal")) {
                    dealCount = shareParameters.optString("deal_count");
                } else if (type.equalsIgnoreCase("event")) {
                    dealCount = shareParameters.optString("event_count");
                }

                HashMap<String, Object> props = new HashMap<String, Object>();
                props.put("Successful", true);
                props.put("RestaurantName", shareParameters.optString("restaurant_name"));
                props.put("RestaurantId",shareParameters.optString("r_id"));
                props.put("Amount",amount);

                if (type.equalsIgnoreCase("event")) {
                    deal = shareParameters.optString("event");
                    dealCount = shareParameters.optString("event_count");
                    props.put("EventType","Paid");
                    trackEventQGraphApsalar("EventBought", props,true,false,false);
                } else if (type.equalsIgnoreCase("deal")) {
                    deal = shareParameters.optString("deal");
                    props.put("deal", shareParameters.optString("deal"));
                    props.put("ticketGroups", ticketGroups);
                    props.put("NumberOfDeals",shareParameters.optString("deal_count"));
                    dealCount = shareParameters.optString("deal_count");
                    trackEventQGraphApsalar(getString(R.string.branch_deal_bought), props,true,false,false);
                    logFacebookPurchaseEvent();
                }

//                Product product = new Product();
//                product.setName(deal);
//                product.setQuantity(Integer.valueOf(dealCount));
//                product.setCategory(type);
//                ProductAction action = new ProductAction(ProductAction.ACTION_PURCHASE);
//
//                action.setTransactionId(getArguments().
//                        getString(com.dineout.book.fragment.payments.PaymentConstant.DISPLAY_ID));
//                action.setTransactionRevenue(Double.valueOf(amount));
//
//                measureProductAction(product,action,"PaymentStatus");


            }


            // dealsSharingTxtVw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shape, 0, 0, 0);


        } else {
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(getContext().getString(R.string.ga_screen_payment_status), getString(R.string.branch_deal_bought_failure), null);

            mBookingInforContainer.setVisibility(View.GONE);
            mDOPlusContainer.setVisibility(View.GONE);
            mDate.setVisibility(View.GONE);
            mInvoiceContainer.setVisibility(View.GONE);
            dealsSharingLayout.setVisibility(View.GONE);
        }
    }

    private void initializeButtonContainer(JSONObject data) {

        if (data != null) {

            String action = data.optString("action");
            if (action.equalsIgnoreCase("invoice")) {

                mInvoiceText.setText(data.optString("text"));
                mDOInvoice.setVisibility(View.VISIBLE);
                mDone.setVisibility(View.GONE);
            } else {
                if (!action.equalsIgnoreCase("retry")) {
                    mDone.setText(data.optString("text"));
                    mDone.setTag(data);
                }
                mDOInvoice.setVisibility(View.GONE);
                mDone.setVisibility(View.VISIBLE);
            }

        } else {
            mDOInvoice.setVisibility(View.GONE);
            mDone.setVisibility(View.VISIBLE);
            mDone.setTag(null);
        }
    }

    @Override
    public void handleNavigation() {

        //TRACK EVENT
        String status="";
        if(mPaymentStatus ==1){
            status="P_Success";
        }else if(mPaymentStatus == 0){
            status="P_Failure";
        }
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(status,getString(R.string.d_back_click),
                getString(R.string.d_back_click),hMap);
        finishPayment();
    }

    private void sendBroadcastToHomePage() {
        Intent intent = new Intent();
        intent.setAction("doplus_payment");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void shareDealStatus() {
        DOShareDialog doShareDialog =
                DOShareDialog.newInstance(getActivity(), dinerName, deal, dealCount, restaurantName,
                        tinyUrl, bookingDate, bookingTime,
                        "",
                        "",
                        localityName);

        doShareDialog.show(getActivity().getSupportFragmentManager(), mType);
    }

    private void logFacebookPurchaseEvent() {
        Bundle bundle=new Bundle();
        bundle.putString(getString(R.string.branch_deal_bought),TextUtils.isEmpty(restaurantName)? "":restaurantName);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, bundle);

    }

    private String getTicketGroups(JSONObject data) {
        JSONArray groups = data.optJSONArray("tg_id");

        String ticketGroup = "";
        if(groups != null) {
            for(int i = 0;i < groups.length(); i++){
                ticketGroup += groups.optInt(i);

                if (i < groups.length() - 1) {
                    ticketGroup += "_";
                }
            }
        }

        return ticketGroup;
    }
}
