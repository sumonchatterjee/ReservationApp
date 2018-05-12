package com.dineout.book.fragment.uploadbill;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.service.ImageUploadService;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.interfaces.UploadBillReviewHandler;
import com.dineout.book.model.uploadBill.viewmodel.UploadReviewViewModel;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import static com.dineout.book.model.uploadBill.viewmodel.UploadReviewViewModel.setImageResource;

public class UploadBillReviewFragment extends MasterDOFragment implements UploadBillReviewHandler {

    private UploadReviewViewModel uploadReviewViewModel;
    private String mBillId;
    private String mFilePath;
    private JSONObject mData;

    public static UploadBillReviewFragment newInstance(String id, String path, JSONObject data) {

        UploadBillReviewFragment fragment = new UploadBillReviewFragment();
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
        uploadReviewViewModel = new UploadReviewViewModel(mData, mBillId, mFilePath, new UploadReviewViewModel.UpdateDescCallback() {
            @Override
            public void onDescUpdate(String desc) {
                if (getView() != null) {
                    ((TextView) getView().findViewById(R.id.rating_desc_tv)).setText(desc);
                }
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;

        window.setLayout((int) (width * 0.75), 600);
        window.setGravity(Gravity.CENTER);
        //getDialog().getWindow().setLayout(MainApplicationClass.getDeviceResourceManager().getScreenWidth(), MainApplicationClass.getDeviceResourceManager().getScreenHeight());

        Drawable d = new ColorDrawable();
        d.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        getDialog().getWindow().setBackgroundDrawable(d);

        View view = inflater.inflate(R.layout.fragment_upload_bill_review, container, false);
        NetworkImageView networkImageView = (NetworkImageView) view.findViewById(R.id.network_image_view);
        TextView nameTv = (TextView) view.findViewById(R.id.name_tv);
        TextView ratingDescTv = (TextView) view.findViewById(R.id.rating_desc_tv);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        Button submitBtn = (Button) view.findViewById(R.id.submit_btn);

        setImageResource(networkImageView, uploadReviewViewModel.imageUrl);
        nameTv.setText(uploadReviewViewModel.name);
        ratingBar.setRating(uploadReviewViewModel.ratingValue);
        uploadReviewViewModel.onRatingChanged(ratingBar, uploadReviewViewModel.ratingValue, true);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                uploadReviewViewModel.onRatingChanged(ratingBar, rating, fromUser);
            }
        });
        ratingDescTv.setText(uploadReviewViewModel.ratingDesc);
        ratingDescTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDescription(uploadReviewViewModel.ratingDesc);
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRating(uploadReviewViewModel.billId,
                        uploadReviewViewModel.path,uploadReviewViewModel.getData());
            }
        });

        return view;

//        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_upload_bill_review, container, false);
//        binding.setVariable(BR.review, uploadReviewViewModel);
//        binding.setVariable(BR.handler, (UploadBillReviewHandler) this);
//        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        return super.onCreateDialog(savedInstanceState);
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

        if (requestCode == AppConstant.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                submitRating(mBillId, mFilePath, uploadReviewViewModel.getData());
                dismissAllowingStateLoss();

            } else {

                UiUtil.showToastMessage(getContext(), "We don't have permission to access your storage we could not upload bill.");
                dismissAllowingStateLoss();

            }
        }

    }

    @Override
    public void submitRating(String id, String file, JSONObject data) {

        if (data == null) {
            uploadImage(id, file, data);
            return;
        }
        if (data.optInt("rating") > 0 || data.optInt("rating_food") > 0) {

            int rating = data.optInt("rating_food");
            String desc = data.optString("rating_desc", "");
            if (rating < 3) {

                if (TextUtils.isEmpty(desc)) {
                    UiUtil.showToastMessage(getContext(), "It seems you had a bad experience, Please tell us more.");
                    return;
                }

            }
            uploadImage(id, file, data);
        } else {
            UiUtil.showToastMessage(getContext(), "Please provide rating");
        }
    }


    private void uploadImage(String id, String file, JSONObject data) {

        if (PermissionUtils.checkStoragePermission(getActivity())) {
            if (data != null)
                trackEventGA("UploadBill", "SubmitRating", null, data.optInt("rating_food"));
            dismissAllowingStateLoss();

            sendIntent(id, file, data);

        } else if (!PermissionUtils.checkStoragePermission(getActivity())) {
            askStoragePermission();
            return;
        }
    }

    private void sendIntent(String id, String path, JSONObject data) {
        showUploadInBcgDialog();

        Intent intent = new Intent(getContext(), ImageUploadService.class);
        intent.putExtra(ImageUploadService.ARG_FILE_URI_ARRAY, new String[]{path});
        intent.putExtra(ImageUploadService.ARG_BILL_ID, id);
        if (data != null)
            intent.putExtra(ImageUploadService.ARG_REVIEW_DATA, data.toString());
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
    public void addDescription(String desc) {

        ReviewDescFragment fragment = ReviewDescFragment.newInstance(desc,
                uploadReviewViewModel.getDescriptionCallback());
        fragment.show(getActivity().getSupportFragmentManager(), "reviewdesc");
    }
}
