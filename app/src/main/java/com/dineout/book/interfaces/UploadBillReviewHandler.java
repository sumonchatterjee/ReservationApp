package com.dineout.book.interfaces;

//import com.dineout.book.billutility.ObservableString;

import org.json.JSONObject;

/**
 * Created by prateek.aggarwal on 8/21/16.
 */
public interface UploadBillReviewHandler {

     void submitRating(String id, String file, JSONObject data);

     void addDescription(String desc);
}
