package com.dineout.book.model;

import android.app.AlertDialog;
import android.content.Context;
//import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class YourBillItem {

    private String restaurant;
    private String statusText;
    private String dateText ;
    private int status;
    private static final int SUCCESS = 2;

    private static final int INITIATED = 0;
    private static final int MODERATION = 1;


    private boolean cashback;
    private String cashbackText;
    private String reviewMsg;
    private String title;
    private Context mContext;

    public YourBillItem(Context context,JSONObject data){

        if(data != null){
            this.restaurant = data.optString("restaurant");
            this.statusText = data.optString("statusText");
            this.status = data.optInt("status");
            this.dateText = getDateText(data.optString(status == MODERATION ?"moderationDate" :"uploadDate"));

            this.reviewMsg = data.optString("remarks");
            this.title = data.optString("title");
            this.cashback = data.optInt("cashback") > 0 ? true:false ;
            this.cashbackText = "Total Earnings:"+data.optInt("cashback");
            this.mContext = context;
        }
    }

    private String getDateText(String date){

        if(!TextUtils.isEmpty(date)){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date d = null;
            try{
                d = format.parse(date.split("T")[0]);

                format = new SimpleDateFormat("dd MMMM,yyyy");
                return format.format(d);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        return "";
    }

    public String getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getDateText() {
        return dateText;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCashbackText() {
        return cashbackText;
    }

    public void setCashbackText(String cashbackText) {
        this.cashbackText = cashbackText;
    }

    public boolean getCashback() {
        return cashback;
    }

    public void setCashback(boolean cashback) {
        this.cashback = cashback;
    }

    public static void setImageResource(TextView view, int status){
        view.setBackgroundResource(status == INITIATED || status == MODERATION ? R.drawable.bill_moderation :
        status == SUCCESS ? R.drawable.bill_confirmed : R.drawable.bill_failure);
    }

    public final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //track event
            String statusTxt="";
            int status=getStatus();
            if(status == SUCCESS){
                statusTxt= "Approved";
            }else{
                statusTxt= "Rejected";
            }
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(mContext);
            if(hMap!=null){
                hMap.put("category","D_YourBills");
                hMap.put("action","BillStatusClick");
                hMap.put("label",restaurant + "_"+ statusTxt);
            }
            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly("BillStatusClick",hMap);
            AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA("D_YourBills","BillStatusClick",restaurant + "_"+ statusTxt);

            showDialog();
        }
    };

    private void showDialog(){

        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE,title);
        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,reviewMsg);
        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT,"Ok, Got it!");

        UiUtil.showCustomDialog(mContext, bundle, new DialogListener() {
            @Override
            public void onPositiveButtonClick(AlertDialog alertDialog) {
                alertDialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(AlertDialog alertDialog) {

            }
        });
    }



}
