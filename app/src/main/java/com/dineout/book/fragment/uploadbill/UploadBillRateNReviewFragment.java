package com.dineout.book.fragment.uploadbill;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.service.ImageUploadService;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import static com.dineout.recycleradapters.util.RateNReviewUtil.CANCELABLE_FLAG;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.addValueToJsonObject;

public class UploadBillRateNReviewFragment extends YouPageWrapperFragment implements RateNReviewUtil.RateNReviewCallbacks, RateNReviewUtil.UploadBillRateNReviewCallback {

    private String mBillId;
    private String mFilePath;
    private JSONObject mData;
    private JSONObject mReviewSendToApiData;

    public static UploadBillRateNReviewFragment newInstance(String id, String path, JSONObject data) {

        UploadBillRateNReviewFragment fragment = new UploadBillRateNReviewFragment();
        fragment.mBillId = id;
        fragment.mFilePath = path;
        fragment.mData = data;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            mBillId = savedInstanceState.getString("ID");
            mFilePath = savedInstanceState.getString("URI");
            try {
                mData = new JSONObject(savedInstanceState.getString("ReviewData", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload_bill_rate_n_review, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RateNReviewDialog RNRDialog = new RateNReviewDialog();
        RNRDialog.setRateNReviewCallback(this);
        RNRDialog.setUploadBillRateNReviewCallback(this);

        // set cancelable flag
        addValueToJsonObject(mData, CANCELABLE_FLAG, false);

        Bundle bl = new Bundle();
        bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_upload_bill));
        bl.putString(INFO_STRING, mData.toString());
        RNRDialog.setArguments(bl);

        showFragment(getActivity().getSupportFragmentManager(), RNRDialog);
    }

    @Override
    public void onReviewSubmission() {
        // do noThing
    }

    @Override
    public void onRNRError(JSONObject errorObject) {
        // do noThing
    }

    @Override
    public void onDialogDismiss() {
        popBackStack(getActivity().getSupportFragmentManager());
    }

    private void askStoragePermission() {
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
                public void onNegativeButtonClick(AlertDialog alertDialog) {}
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

        if (requestCode == AppConstant.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uploadImage();
                dismissAllowingStateLoss();
            } else {
                UiUtil.showToastMessage(getContext(), "We don't have permission to access your storage we could not upload bill.");
                dismissAllowingStateLoss();
            }
        }
    }

    private void uploadImage() {
        if (PermissionUtils.checkStoragePermission(getActivity())) {
            dismissAllowingStateLoss();
            sendIntent();
        } else if (!PermissionUtils.checkStoragePermission(getActivity())) {
            askStoragePermission();
        }
    }

    private void sendIntent() {
        showUploadInBcgDialog();

        Intent intent = new Intent(getContext(), ImageUploadService.class);
        intent.putExtra(ImageUploadService.ARG_FILE_URI_ARRAY, new String[]{mFilePath});
        intent.putExtra(ImageUploadService.ARG_BILL_ID, mBillId);
        if (mReviewSendToApiData != null) {
            intent.putExtra(ImageUploadService.ARG_REVIEW_DATA, mReviewSendToApiData.toString());
        }
        getContext().startService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ID", mBillId);
        outState.putString("URI", mFilePath);

        if (mData != null) {
            outState.putString("ReviewData", mData.toString());
        }
    }

    private void showUploadInBcgDialog() {
        UiUtil.showToastMessage(getContext(), DOPreferences.getUploadDisplayMessage(getContext()));
    }

    @Override
    public void onReviewSubmitClick(JSONObject object) {
        mReviewSendToApiData = object;
        uploadImage();
    }
}
