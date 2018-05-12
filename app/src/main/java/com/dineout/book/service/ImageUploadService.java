package com.dineout.book.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.controller.BillNotificationManager;
import com.dineout.book.controller.UploadBillTask;
import com.dineout.book.interfaces.UploadStateObserver;
import com.dineout.book.broadcastreceiver.NetworkChangeReciever;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ImageUploadService extends Service {



    public static final String ARG_FILE_URI_ARRAY = "image_upload_file_name";
    public static final String ARG_BILL_ID = "image_upload_bill_rest_id";
    public static final String ARG_REVIEW_DATA = "image_upload_review";

    private int mStartId;

    private DineoutNetworkManager mManager;
    private HashMap<Integer ,UploadBillTask> taskMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = DineoutNetworkManager.newInstance(getBaseContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mStartId = startId;

        if(intent == null)
            retryPendingRequest();
        else if(intent != null && intent.getAction() != null &&
                intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)){
            retryPendingRequest();
        }else{

           toggleNetworkChangeReceiver(true);
            if(intent != null){

                execService(intent);
            }
        }



        return START_STICKY;
    }

    private void retryPendingRequest(){

        if(taskMap.size() <= 0){
            toggleNetworkChangeReceiver(false);
            stopSelf();
            return;
        }


        for(Map.Entry<Integer,UploadBillTask> taskEntry : taskMap.entrySet()){

            UploadBillTask task = taskEntry.getValue();
            if(task != null){
            if(task.getTaskState() == UploadStateObserver.TRANSFER_INTERRUPTED
                    || task.getTaskState() == UploadStateObserver.TRANSFER_PENDING){

                    task.retryRequest(mManager,taskEntry.getKey());
                }
            }
        }
    }

    private void execService(Intent intent){

        toggleNetworkChangeReceiver(true);
        Bundle bundle = intent.getExtras();
        if(bundle != null && validateData(bundle)){

            String[] file = bundle.getStringArray(ARG_FILE_URI_ARRAY);
            for(int i=0;i<file.length;i++){
                    String f = file[i];
                    int val = taskMap.size() +(i+1);
                    int id = ((val |5)|mStartId)&Integer.parseInt(bundle.getString(ARG_BILL_ID));
                UploadBillTask task =new UploadBillTask(getBaseContext(),bundle.getString(ARG_BILL_ID)
                        ,f,bundle.getString(ARG_REVIEW_DATA),new ImageUploadStateObserver() );

                task.createUploadRequest(mManager,id);
                    taskMap.put(id,task);
            }

        }else{
            Log.e("UploadBill","Intent extra could not be null &&" +
                    " must contain mandatory valid file array for valid image and bill id");
        }
    }

    private boolean validateData(Bundle bundle){


        if( bundle.getStringArray(ARG_FILE_URI_ARRAY) == null  ){
            return false;
        }

        if(TextUtils.isEmpty(bundle.getString(ARG_BILL_ID))){
            return false;
        }


        if(isAlphaNumeric(bundle.getString(ARG_BILL_ID)))
            return false;
        return true;
    }

    private boolean isAlphaNumeric(String source){

        try{
            Integer.parseInt(source);
            return false;
        }catch (Exception e){
            return true;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void toggleNetworkChangeReceiver(boolean enable){
        ComponentName receiver = new ComponentName ( getBaseContext() , NetworkChangeReciever.class );
        PackageManager pm = getBaseContext().getPackageManager ();
        int currentState = pm.getComponentEnabledSetting(receiver);

        if(enable && currentState !=PackageManager.COMPONENT_ENABLED_STATE_ENABLED){
            pm . setComponentEnabledSetting ( receiver ,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP );
        }else if(!enable && currentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
            pm . setComponentEnabledSetting ( receiver ,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP );
        }


    }

    private class ImageUploadStateObserver implements UploadStateObserver{


        @Override
        public void transferStateChanged(int id,int state, Object obj) {

            if(state == TRANSFER_COMPLETED){

                AnalyticsHelper.getAnalyticsHelper(getApplication()).trackEventGA("UploadBill","BillUploaded",null);
                BillNotificationManager.removeProgressNotification(getBaseContext(),id);
                showCompleteNotification((JSONObject) obj,id);
                taskMap.remove(id);
                stopService();
            }else if(state == TRANSFER_STARTED){
                BillNotificationManager.buildIntermediateProgressNotification(getBaseContext(),id);

            }else if(state == TRANSFER_ERROR){
                taskMap.remove(id);
                BillNotificationManager.removeProgressNotification(getBaseContext(),id);
                BillNotificationManager.displayNotification(getBaseContext(),(String)obj,null,id);
                stopSelf(mStartId);

            }else if(state == TRANSFER_INTERRUPTED){
                mManager.cancel();
                BillNotificationManager.removeProgressNotification(getBaseContext(),id);
            }
        }
        private void showCompleteNotification(JSONObject data,int id){
            BillNotificationManager.displayNotification(getBaseContext(),data.optString("subtitle",
                    "Bill updated successfully"),data.optString("title","UploadBill"),id);
        }

        @Override
        public void progressChanged(int id,long completed, long total) {

            BillNotificationManager
                    .updateProgressNotification(getBaseContext(),id, (completed/total)*100);

        }
    }


    private void stopService(){

        if(taskMap.size() >0){
            stopSelf(mStartId);
        }else if(taskMap.size() <= 0) {
            stopSelf();
        }
    }


}
