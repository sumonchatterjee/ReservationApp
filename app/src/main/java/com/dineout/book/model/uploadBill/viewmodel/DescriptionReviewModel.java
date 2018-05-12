package com.dineout.book.model.uploadBill.viewmodel;

import android.text.Editable;
import android.text.TextUtils;


public class DescriptionReviewModel {

    public String desc = new String();


    public DescriptionReviewModel(String desc){
        this.desc = desc;
    }




    public void afterTextChanged(Editable s) {

        if(s != null){

            String desc =  s.toString();
            if(!TextUtils.isEmpty(desc)){
                this.desc =desc.trim();
            }else{
                this.desc = "";
            }
        }
    }

}
