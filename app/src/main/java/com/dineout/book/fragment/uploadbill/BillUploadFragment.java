package com.dineout.book.fragment.uploadbill;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.model.uploadBill.viewmodel.UploadModel;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_NAME;


public class BillUploadFragment extends MasterDOFragment {

    private UploadModel mBillViewModel;

    private String mBillId;
    private JSONObject mReviewData;
    private boolean skipReview;

    public static BillUploadFragment newInstance(String id, JSONObject review, boolean skip){

        BillUploadFragment view = new BillUploadFragment();
        view.mReviewData= review;
        view.mBillId = id;
        view.skipReview = skip;

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(TextUtils.isEmpty(mBillId))
            Log.e("UploadBill","Bill id cannot be null");

        trackScreenToGA("UploadBill");
        mBillViewModel = new UploadModel(getActivity(),
                mBillId,mReviewData,skipReview);
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        UploadBillBinding binding = DataBindingUtil.inflate(inflater,R.layout.upload_bill,container,false);
//
//         //Set view model to the binding of root view
//       binding.setViewModel(mBillViewModel);
//
//        return binding.getRoot();
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("UploadBill");
        if(!PermissionUtils.checkStoragePermission(getActivity())){
            askStoragePermission();
        }else {
            mBillViewModel.takePhoto(this);
        }
    }

    private void askStoragePermission(){

        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Bundle bundle = new Bundle();
            bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, "Permission Request");
            bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,
                    getString(R.string.storage_permission_explaination_info));
            bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OK");
            UiUtil.showCustomDialog(getActivity(), bundle, new DialogListener() {
                @Override
                public void onPositiveButtonClick(AlertDialog alertDialog) {

                    alertDialog.dismiss();

                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            AppConstant.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
                }

                @Override
                public void onNegativeButtonClick(AlertDialog alertDialog) {

                }
            });

        } else {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppConstant.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);


        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == AppConstant.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

              mBillViewModel.takePhoto(this);

            } else {

                Bundle bundle = new Bundle();
                bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, "Permission Request");
                bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,
                        "Permission Denied. " +
                                "Could not upload bill");
                bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OK");
                UiUtil.showCustomDialog(getActivity(), bundle, new DialogListener() {
                    @Override
                    public void onPositiveButtonClick(AlertDialog alertDialog) {

                        alertDialog.dismiss();
                        MasterDOFragment.popBackStack(getActivity().getSupportFragmentManager());

                    }

                    @Override
                    public void onNegativeButtonClick(AlertDialog alertDialog) {

                    }

                });
            }
        }

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // tracking
        try {
            String categoryName = getString(R.string.ga_rnr_category_upload_bill);
            String actionName = getString(R.string.ga_rnr_action_steps_to_upload_bill_use_photo_click);
            String label = mReviewData.optString(RESTAURANT_NAME) + "_" + mReviewData.optString(RESTAURANT_ID);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(actionName,hMap);
            trackEventForCountlyAndGA(categoryName,actionName,label,hMap);
        } catch (Exception e) {
            // Exception
        }

        MasterDOFragment.popBackStackImmediate(getActivity().getSupportFragmentManager());
        mBillViewModel.handleResult(requestCode,resultCode,data);
        dismissAllowingStateLoss();


    }
}
