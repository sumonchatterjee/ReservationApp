package com.dineout.book.fragment.uploadbill;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.StepsUploadAdapter;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.controller.UploadBillController.UPLOAD_BILL_PREVIOUS_SCREEN_NAME_KEY;


public class StepsToUploadFragment extends MasterDOFragment implements StepsUploadAdapter.OnSubmitButtonClickListener,
        StepsUploadAdapter.CheckBoxStateChangeCallback {



    JSONObject response;
   RecyclerView recyclerView;
   StepsUploadAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo);
        adapter=new StepsUploadAdapter(response);
        adapter.setOnSubmitButtonClickListener(this);
        adapter.setCheckBoxStateChangeCallback(this);

        //stepsToUploadModel = new StepsToUploadModel(getActivity(),this,response);
    }

    public static StepsToUploadFragment newInstance(JSONObject response){

        StepsToUploadFragment fragment = new StepsToUploadFragment();
        fragment.response= response;


        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_upload_bill_steps,container,false);
        trackScreenToGA("StepsToUploadBill");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView= (RecyclerView) getView().findViewById(R.id.steps_upload_recycler);
        RecyclerView.LayoutManager mManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mManager);
        recyclerView.setAdapter(adapter);

    }








    @Override
    public void onButtonCTAClicked(boolean isChecked) {
        if(isChecked)
           trackEventGA("StepsToUploadBill","StopShowing",null);
        trackEventGA("StepsToUploadBill","ClickBillImage",null);
        DOPreferences.setShowStepUpload(getContext().getApplicationContext(),!isChecked);
        if(response!=null) {

            String billId=response.optString("billID");
            boolean skipReview = response.optBoolean("skipReview");
            JSONObject reviewData = response.optJSONObject("reviewData");

            JSONObject object = new JSONObject();
            RateNReviewUtil.appendObject(object, response.optJSONObject("reviewData"));
            RateNReviewUtil.appendObject(object, response.optJSONObject("reviewBox"));

            MasterDOFragment fragment =
                    BillUploadFragment.newInstance(billId, object, skipReview);
            MasterDOFragment.addToBackStack(getActivity(),
                    fragment);
        }

        // send tracking
        try {
            String categoryName = getString(R.string.ga_rnr_category_upload_bill);
            String actionName = getString(R.string.ga_rnr_action_steps_to_upload_bill_click_bill_image_cta_click);
            String labelName = (getArguments() != null) ? getArguments().getString(UPLOAD_BILL_PREVIOUS_SCREEN_NAME_KEY) : "";

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(categoryName,actionName,labelName,hMap);

        } catch (Exception e) {
            // Exception
        }

        dismiss();
    }

    @Override
    public void declarationCheckBoxStatus(boolean isTrue) {
        // tracking
        try {
            String categoryName = getString(R.string.ga_rnr_category_upload_bill);
            String actionName = isTrue ? getString(R.string.ga_rnr_action_steps_to_upload_bill_check_box_select)
                    : getString(R.string.ga_rnr_action_steps_to_upload_bill_check_box_un_select);
            String labelName = (getArguments() != null) ? getArguments().getString(UPLOAD_BILL_PREVIOUS_SCREEN_NAME_KEY) : "";
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, labelName);
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("category",categoryName);
                hMap.put("label",labelName);
                hMap.put("action",actionName);
            }
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(actionName,hMap);


        } catch (Exception e) {
            // Exception
        }
    }


}
