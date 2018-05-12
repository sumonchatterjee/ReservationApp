package com.dineout.book.fragment.payments.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.book.util.AppUtil;
import com.dineout.book.controller.FragmentTransactionManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.view.PaymentLoaderDialog;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import java.util.HashMap;

public abstract class DOBaseFragment extends MasterDOFragment implements Response.ErrorListener {
    ImageRequestManager imageRequestManager;
    private DineoutNetworkManager networkManager;
    private boolean isChildFragment;
    public static final int MOBIQKWIK_REQUEST_CODE = 101;


    @Override
    public void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        networkManager = DineoutNetworkManager.newInstance(getActivity());
        imageRequestManager = ImageRequestManager.getInstance(getActivity());

    }



    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageRequestManager.getImageLoader();
    }

    public void setChildFragment(boolean childFragment) {
        isChildFragment=childFragment;
    }

    public boolean isChildFragment(){
        return isChildFragment;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView ();
        if(networkManager!=null) {
            networkManager.cancel();
        }
    }







    @Override
    public void onPause() {
        super.onPause();

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());
    }

//    public boolean onPopBackStack() {
//        super.onPopBackStack()
//        return false;
//    }



//    public void trackScreenToGA(String screenName) {
//        super.trackScreenToGA();
//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackScreenToGA(screenName);
//    }
//
//    public void trackEventGA(String category, String action, String label) {
//        super.tr
//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackEventGA(category, action, label);
//    }
//
//    public void trackEventGA(String eventName, HashMap<String, Object> hashMap) {
//        AnalyticsHelper.getAnalyticsHelper(getContext())
//                .trackEventGA(eventName, hashMap);
//    }



    public void showStatusScreen(Bundle bundle){
        if(getActivity() != null){


            if(getArguments() != null)
            bundle.putString(PaymentConstant.BOOKING_ID, getArguments().getString
                    (PaymentConstant.BOOKING_ID));
            MasterDOFragment fragment = PaymentStatusFragment.newInstance(bundle);
            popToHome(getActivity());
            addToBackStack(getActivity(),fragment);

        }

    }


}
