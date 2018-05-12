package com.dineout.recycleradapters.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sumon.chatterjee on 17/05/16.
 */
public class BookingDetailUtil {

    public static boolean isSuccess(JSONObject jsonObject) {
        return (jsonObject != null && !jsonObject.isNull("status") && jsonObject.optBoolean("status"));
    }

    public static JSONObject getOutputParams(JSONObject jsonObject) {
        return (jsonObject != null && !jsonObject.isNull("output_params")) ?
                jsonObject.optJSONObject("output_params") : null;
    }

    public static JSONObject getData(JSONObject jsonObject) {
        JSONObject outputParams = getOutputParams(jsonObject);
        if (outputParams != null) {
            return outputParams.optJSONObject("data");
        }
        return null;
    }

    public static JSONObject getResAuth(JSONObject jsonObject) {
        return (jsonObject != null && !jsonObject.isNull("res_auth")) ?
                jsonObject.optJSONObject("res_auth") : null;
    }

    public static String getDinerId(JSONObject data) {
        if (data != null && !data.isNull("diner_id")) {
            return data.optString("diner_id");
        }
        return null;
    }

    public static String getRecency(JSONObject data) {
        if (data != null && !AppUtil.isStringEmpty(data.optString("recency"))) {
            return data.optString("recency");
        }

        return "";
    }

    public static double getAvgRating(JSONObject data) {
        if (data != null && !data.isNull("avg_rating")) {
            return data.optDouble("avg_rating");
            //return TextUtils.isEmpty(recency)?0:Integer.parseInt(recency);
        }
        return 0;

    }

    public static int isAcceptPayment(JSONObject data) {
        if (data != null && !data.isNull("is_accept_payment")) {
            return data.optInt("is_accept_payment");

        }
        return 0;
    }

    public static String isAlreadyPaid(JSONObject data) {
        if (data != null && !data.isNull("already_paid")) {
            return data.optString("already_paid");
        }
        return null;
    }


    public static String getDiningDataTime(JSONObject data) {
        if (data != null && !data.isNull("dining_dt_time")) {
            return data.optString("dining_dt_time");
        }
        return null;
    }

    public static String getDinerName(JSONObject data) {
        if (data != null && !data.isNull("diner_name")) {
            return data.optString("diner_name");
        }
        return null;

    }

    public static String getResturantName(JSONObject data) {
        if (data != null && !data.isNull("rest_name")) {
            return data.optString("rest_name");
        }
        return null;
    }

    public static String getDisplayId(JSONObject data) {
        if (data != null && !data.isNull("disp_id")) {
            return data.optString("disp_id");
        }
        return null;
    }

    public static String getResturantAddress(JSONObject data) {
        if (data != null && !data.isNull("address")) {
            return data.optString("address");
        }
        return null;
    }

    public static double getDistance(JSONObject data) {
        if (data != null && !data.isNull("distance")) {
            return data.optDouble("distance");
        }
        return 0;
    }


    public static ArrayList<ImageData> getProfileImageData(JSONObject data) {
        ArrayList<ImageData> imageDatas = new ArrayList<>();

        if (data != null && !data.isNull("image_data")) {
            JSONArray imageDataJsonArray = data.optJSONArray("image_data");
            if (imageDataJsonArray != null && imageDataJsonArray.length() > 0) {
                for (int i = 0; i < imageDataJsonArray.length(); i++) {
                    try {
                        JSONObject imageobj = imageDataJsonArray.getJSONObject(i);
                        if (imageobj != null && !imageobj.isNull("image_type")) {

                            if (imageobj.optString("image_type").equalsIgnoreCase("profile")) {

                                ImageData imageData = new ImageData();
                                imageData.setGil_id(imageobj.optString("gil_id"));
                                imageData.setImage_name(imageobj.optString("image_name"));
                                imageData.setImage_type(imageobj.optString("image_type"));
                                imageData.setImage_url(imageobj.optString("image_url"));
                                imageData.setObj_id(imageobj.optString("obj_id"));
                                imageData.setObj_type(imageobj.optString("obj_type"));
                                imageDatas.add(imageData);
                            }
                        }
                    } catch (JSONException ex) {
                    }
                }
            }
        }
        return imageDatas;
    }


    public static JSONObject getAttributes(JSONObject data) {
        if (data != null) {
            return data.optJSONObject("attributes");
        }
        return null;
    }


    public static int getRefCode(JSONObject obj) {
        if (obj != null && !obj.isNull("ref_code")) {
            return obj.optInt("ref_code", 0);
        }
        return 0;
    }

    public static ArrayList<String> getProfileImages(JSONObject data) {

        JSONObject jsonObject = getData(data);

        if (jsonObject != null) {
            data = jsonObject;
        }

        ArrayList<String> arrayList = new ArrayList<>();
        if (data != null && !data.isNull("image_data")) {
            JSONArray imageDataJsonArray = data.optJSONArray("image_data");
            if (imageDataJsonArray != null && imageDataJsonArray.length() > 0) {

                for (int i = 0; i < imageDataJsonArray.length(); i++) {
                    JSONObject profileObject = imageDataJsonArray.optJSONObject(i);
                    if (!profileObject.isNull("image_type")) {
                        if (profileObject.optString("image_type").equalsIgnoreCase("profile")) {
                            if (!profileObject.isNull("image_url")) {
                                arrayList.add(profileObject.optString("image_url"));
                            }
                        }
                    }
                }
            }
        }

        return arrayList;
    }

    public static ArrayList<String> getPhoneNumbers(JSONObject data) {
        ArrayList<String> numbers = new ArrayList<>();
        if (data != null && !data.isNull("phone")) {
            JSONArray phoneNumber = data.optJSONArray("phone");
            if (phoneNumber != null && phoneNumber.length() > 0) {
                for (int i = 0; i < phoneNumber.length(); i++) {
                    try {
                        String c = phoneNumber.getString(i);
                        numbers.add(c);
                    } catch (JSONException ex) {

                    }
                }

            }
        }

        return numbers;
    }
}
