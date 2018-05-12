package com.dineout.book.model.uploadBill.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.uploadbill.UploadBillRateNReviewFragment;
import com.dineout.book.service.ImageUploadService;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class UploadModel {
    
    private final int TAKE_PHOTO = 0x02;
    private Context mContext;
    private String mBillId;
    private boolean skipReview;
    private Uri mFileUri;
    private JSONObject mReviewData;

    public UploadModel(Context context, String id, JSONObject data, boolean skipReview) {

        mContext = context;
        mBillId = id;
        mReviewData = data;
        this.skipReview = skipReview;
    }

    public Bitmap getImage() {
        return null;
    }

    public void takePhoto(Fragment fragment) {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mFileUri = Uri.fromFile(getOutputMediaFile());
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        fragment.startActivityForResult(takePicture, TAKE_PHOTO);
    }

    private File getOutputMediaFile() {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(Environment.getExternalStorageDirectory() + File.separator
                + "bill_" + timeStamp + ".png");
        return mediaFile;
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                case TAKE_PHOTO:
                    if(mFileUri!=null) {
                        handleCaptureImageResult(mFileUri);
                    }
                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == TAKE_PHOTO && mContext!=null) {
            Toast.makeText(mContext, "You have cancelled upload", Toast.LENGTH_LONG).show();
        }
    }

    private void handleCaptureImageResult(Uri uri) {
        if (skipReview || mReviewData == null)
            sendIntent(uri.getPath());

        else {
            UploadBillRateNReviewFragment reviewFragment = UploadBillRateNReviewFragment.
                    newInstance(mBillId, mFileUri.getPath(), mReviewData);

            MasterDOFragment.addToBackStack(((FragmentActivity) mContext).getSupportFragmentManager(), reviewFragment);
//            reviewFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "UploadbillReview");
        }
    }

    private void sendIntent(String path) {
        showUploadInBcgDialog();

        Intent intent = new Intent(mContext, ImageUploadService.class);
        intent.putExtra(ImageUploadService.ARG_FILE_URI_ARRAY, new String[]{path});
        intent.putExtra(ImageUploadService.ARG_BILL_ID, mBillId);

        mContext.startService(intent);
        freeMemory();
    }

    private void sendIntent(String path, JSONObject data) {
        showUploadInBcgDialog();
        Intent intent = new Intent(mContext, ImageUploadService.class);
        intent.putExtra(ImageUploadService.ARG_FILE_URI_ARRAY, new String[]{path});
        intent.putExtra(ImageUploadService.ARG_BILL_ID, mBillId);
        intent.putExtra(ImageUploadService.ARG_REVIEW_DATA, data.toString());
        mContext.startService(intent);
        freeMemory();
    }

    private void freeMemory() {
        mContext = null;
        mBillId = null;
        mReviewData = null;
    }

    private void showUploadInBcgDialog() {
        UiUtil.showToastMessage(mContext, DOPreferences.getUploadDisplayMessage(mContext));
    }
}
