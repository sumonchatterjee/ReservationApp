package com.dineout.book.controller;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.dineout.android.volley.DefaultRetryPolicy;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.RetryPolicy;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.interfaces.UploadStateObserver;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.ImageCompressionUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.example.dineoutnetworkmodule.ProgressListener;
import com.example.dineoutnetworkmodule.UploadBillRequest;

import org.json.JSONObject;

import java.io.File;


public class UploadBillTask implements ProgressListener,Response.Listener<JSONObject>,Response.ErrorListener {


    private UploadStateObserver mObserver;
    private UploadBillRequest mRequest;
    private String billId;
    private String mFile;
    private String mUploadUrl;
    private int id;
    private String reviewData;
    private Context mContext;
    private RetryPolicy mRetryPolicy;
    private int mState;


    public UploadBillTask(Context context,String billid, String file,String reviewData ,UploadStateObserver observer){

        mContext = context;
        String nodeBaseUrl=AppUtil.isStringEmpty(DOPreferences.getApiBaseUrlNode(context)) ? AppConstant.BASE_DOMAIN_URL_NODE :
                DOPreferences.getApiBaseUrlNode(context);
        String nodeVersionUrl= AppUtil.isStringEmpty(DOPreferences.getNodeBaseUrl(context)) ? AppConstant.NODE_BASE_URL :
                        DOPreferences.getNodeBaseUrl(context);
        mUploadUrl= nodeBaseUrl+nodeVersionUrl+ AppConstant.URL_UPLOAD_BILL;
        this.billId = billid;
        this.mFile = file;
        this.mObserver = observer;
        this.reviewData = reviewData;
        mRetryPolicy = new DefaultRetryPolicy(60 * 1000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    }
    public void createUploadRequest(DineoutNetworkManager manager,int id){

        if(TextUtils.isEmpty(mFile)){
            throw new RuntimeException("File param could not be null");
        }

        if(!ImageCompressionUtil.compressImage(mFile)){

            Log.d(UploadBillTask.class.getSimpleName(),"Unable to compress image");
        }

        if(!AppUtil.hasNetworkConnection(mContext)){
            mState = UploadStateObserver.TRANSFER_PENDING;
            return;
        }

        mState = UploadStateObserver.TRANSFER_STARTED;
        this.id = id;
        mRequest = new UploadBillRequest(mUploadUrl,billId,reviewData,new File(mFile),this,this);
        mRequest.setOnProgressListener(this);
        manager.multipartRequest( mRequest,id,mRetryPolicy);
    }



    public int getTaskState(){

        return  mState;
    }

    public void retryRequest(DineoutNetworkManager manger,int id){
        createUploadRequest(manger,id);
    }


    public void cancelTask(){

        if(mRequest != null)
            mRequest.cancel();
    }

    @Override
    public void onProgress(long transferredBytes, long totalSize) {

        mObserver.progressChanged(id,transferredBytes,totalSize);
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

        if(AppUtil.hasNetworkConnection(mContext)
                && request.getRetryPolicy().getCurrentRetryCount() <=0){
            mState = UploadStateObserver.TRANSFER_ERROR;
            if(error.networkResponse != null)
            {
                Response<JSONObject> response = ((UploadBillRequest)request).parseNetworkResponse(error.networkResponse);
                processFailedResponse(response.result);
            }else{
                mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_ERROR,"Error in uploading bill.");

            }
        }else if(!AppUtil.hasNetworkConnection(mContext)){

            mState =UploadStateObserver.TRANSFER_INTERRUPTED;
            mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_INTERRUPTED,"No Network.");

            cancelTask();
        }

    }

    private void processFailedResponse(JSONObject response){

        try{
            mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_ERROR,response.optString("message"));
        }catch (Exception e){
            mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_ERROR,"Error in uploading bill.");

        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {

        mState = UploadStateObserver.TRANSFER_COMPLETED;

        if(responseObject != null){
            if(responseObject.optBoolean("status")){
                JSONObject data = responseObject.optJSONObject("data");
                if(data != null){
                    mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_COMPLETED,data);
                }
            }else{
                JSONObject data = responseObject.optJSONObject("data");
                 if(data != null)
                mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_ERROR,data.optString("error_msg"));
                else
                     mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_ERROR,responseObject.
                             optString("msg","Error in uploading bill."));
            }
        }else{
            mObserver.transferStateChanged(id,UploadStateObserver.TRANSFER_ERROR,"Error in uploading bill.");

        }
    }
}
