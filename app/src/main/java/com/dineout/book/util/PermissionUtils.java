package com.dineout.book.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.dineout.book.R;
import com.example.dineoutnetworkmodule.AppConstant;

public class PermissionUtils {



    public static boolean grantLocationPermission(final Activity context) {

        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, "Permission Request");
        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,
                context.getString(R.string.location_permission_explaination_info));
        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OK");


       if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            showExplanatoryAlert(context, bundle,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            ActivityCompat.requestPermissions(context,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                    },
                                    AppConstant.REQUEST_PERMISSION_LOCATION);
                        }
                    });

        } else {


               ActivityCompat.requestPermissions(context,
                       new String[]{
                               Manifest.permission.ACCESS_FINE_LOCATION},
                       AppConstant.REQUEST_PERMISSION_LOCATION);




        }

        return true;
    }



    public static boolean hasLocationPermission(Context context) {
        if(context!=null) {

            return (ContextCompat
                    .checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }

    public static boolean checkStoragePermission(Context context) {

        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean checkReadSMSPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static void showExplanatoryAlert(Context context, Bundle bundle,
                                             final DialogInterface.OnClickListener listener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(bundle.getString(AppConstant.BUNDLE_DIALOG_TITLE));
        alert.setMessage(bundle.getString(AppConstant.BUNDLE_DIALOG_DESCRIPTION));
        alert.setPositiveButton(bundle.getString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT), listener);
        alert.show();
    }


    public static boolean hasAccountPermission(Context context) {

        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean grantAccountPermission(final Activity context) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                Manifest.permission.GET_ACCOUNTS)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Bundle bundle = new Bundle();
            bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, "Permission Request");
            bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,
                    context.getString(R.string.account_permission_explanation_info));
            bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OK");
            showExplanatoryAlert(context, bundle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(context,
                            new String[]{Manifest.permission.GET_ACCOUNTS},
                            AppConstant.REQUEST_PERMISSION_ACCOUNTS);
                }
            });
        } else {



            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    AppConstant.REQUEST_PERMISSION_ACCOUNTS);


        }

        return true;
    }

    public static boolean handleCallPermission(final Activity activityContext) {
        boolean isPermissionGranted;

        //Check if Permission was Granted
        int permissionCallCheck = ContextCompat.checkSelfPermission(activityContext, Manifest.permission.CALL_PHONE);

        //If Permission is Granted
        if (permissionCallCheck == PackageManager.PERMISSION_GRANTED) {
            //Set Flag
            isPermissionGranted = true;

        } else {
            //Set Flag
            isPermissionGranted = false;

            //Check if the Permission was denied byb User previously
            boolean isPermissionDenied = ActivityCompat.shouldShowRequestPermissionRationale(activityContext,
                    Manifest.permission.CALL_PHONE);

            if (isPermissionDenied) { //Handling Second Time
                //Prepare Bundle
                Bundle bundle = new Bundle();
                bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, activityContext.getString(R.string.title_permission_required));
                bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION, "Calling permission is required to make calls directly to the Restaurant");
                bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OK");

                //Show Explanation
                showExplanatoryAlert(activityContext, bundle, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Dismiss Dialog
                        dialog.dismiss();

                        //Request for Permission
                        ActivityCompat.requestPermissions(activityContext,
                                new String[]{Manifest.permission.CALL_PHONE},
                                AppConstant.REQUEST_PERMISSION_CALL_PHONE);
                    }
                });

            } else { //Handling First Time
                //Request for Permission
                ActivityCompat.requestPermissions(activityContext,
                        new String[]{Manifest.permission.CALL_PHONE},
                        AppConstant.REQUEST_PERMISSION_CALL_PHONE);
            }
        }

        return isPermissionGranted;
    }

}
