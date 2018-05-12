package com.dineout.book.fragment.payments.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.view.contract.AddMoneyContract;

import org.json.JSONObject;

/**
 * Created by prateek.aggarwal on 10/17/16.
 */
public class AddMoneyFragment extends DOBaseFragment implements AddMoneyContract {


    private AddMoneyPresenter moneyPresenter;
    private RecyclerView mRecyclerView;

    public static AddMoneyFragment newInstance(Bundle bundle,JSONObject data){

        AddMoneyFragment fragment = new AddMoneyFragment();
        fragment.moneyPresenter = new AddMoneyPresenter(data,bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(moneyPresenter != null)
        moneyPresenter.setView(getContext(),this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_money,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle("Add Money");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(moneyPresenter != null)
        moneyPresenter.onAttach();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(moneyPresenter != null)
        moneyPresenter.onDetach();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_add_money);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(moneyPresenter.getAdapter(getContext()));
    }

    @Override
    public void navigateToWebView(String link,String post) {

        Bundle bundle = new Bundle();
        bundle.putString("url",link);
        bundle.putString("post_data",post);

        PGWebFragment fragment = PGWebFragment.newInstance(link,post);
        fragment.setTargetFragment(this,101);
        addToBackStack(getActivity(),fragment);
    }

    public void handlePaymentSuccess(boolean status , String id , String amount){

        Bundle bundle = new Bundle();
        bundle.putBoolean(PaymentConstant.PAYMENT_STATUS, status);
        bundle.putString(PaymentConstant.DISPLAY_ID, id);
        bundle.putString(PaymentConstant.BOOKING_AMT, amount);
        showStatusScreen(bundle);

    }

    public void handleFailureResponse(String msg){

        UiUtil.showToastMessage(getContext(),msg);
        popBackStackImmediate(getActivity().getSupportFragmentManager());
        popBackStackImmediate(getActivity().getSupportFragmentManager());


    }

    @Override
    public void showProgress() {
        showLoader();
    }

    @Override
    public void hideProgress() {

        hideLoader();
    }

    @Override
    public void showError(String message) {

        UiUtil.showToastMessage(getContext(),message);
    }


    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }
}
