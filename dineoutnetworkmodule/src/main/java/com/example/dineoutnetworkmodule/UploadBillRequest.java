package com.example.dineoutnetworkmodule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.dineout.android.volley.AuthFailureError;
import com.dineout.android.volley.NetworkResponse;
import com.dineout.android.volley.ParseError;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prateek.aggarwal on 7/25/16.
 */
public class UploadBillRequest extends VolleyMultipartRequest<JSONObject> {

    private Response.Listener<JSONObject> mListener ;
    private Map<String,String> mParam =  new HashMap<>();
    private DataPart fileData;

    public UploadBillRequest(String url,String id, String data,File file, Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener){

        super (url,errorListener);

        mListener = listener;
        mParam.put(AppConstant.PARAM_BILL_ID,id);
        if(!TextUtils.isEmpty(data))
        mParam.put(AppConstant.PARAM_REVIEW_BILL_DATA,data);
        fileData = new DataPart(file.getName(), getFileDataFromName(file.getAbsolutePath()),"image/png");
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return super.getHeaders();
    }

    @Override
    public Map<String, DataPart> getByteData() throws AuthFailureError {

        Map<String, DataPart> params = new HashMap<>();
        params.put(AppConstant.PARAM_BILL_UPLOAD_FILE,fileData);
        return params;
    }

    private byte[] getFileDataFromName( String file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParam;
    }

    @Override
    protected Response parseNetworkResponseUnpacked(NetworkResponse response) {
        try {
            String jsonString = new String(response.processedData,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e, response));
        } catch (JSONException je) {
            return Response.error(new ParseError(je, response));
        }
    }


    @Override
    protected void deliverResponse(Request<JSONObject> request, JSONObject response, Response<JSONObject> fullResponse) {

        if(mListener != null)
            mListener.onResponse(request,response,fullResponse);
    }





}
