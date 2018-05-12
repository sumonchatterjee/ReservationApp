package com.dineout.book.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.interfaces.DialogListener;
import com.example.dineoutnetworkmodule.AppConstant;

public class UiUtil {


    public static AlertDialog.Builder getAlertDialog(Context context, Object title, Object message, Object positiveButtonName, Object negativeButtonName, boolean isDefaultPositiveButtonRequired, boolean isDefaultNegativeButtonRequired, boolean isCancellable) {

        //Check if Context is NULL
        if (context == null) {
            return null;
        }

        //Instantiate AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Set Title
        if (title != null) {
            builder.setTitle(AppUtil.getContentString(context, title));
        }

        //Set Message
        if (message != null) {
            builder.setMessage(AppUtil.getContentString(context, message));
        }

        //Set Cancellable
        builder.setCancelable(isCancellable);

        //Set Positive Button
        if (isDefaultPositiveButtonRequired) {
            builder.setPositiveButton(((positiveButtonName == null) ? context.getString(R.string.button_ok) : AppUtil.getContentString(context, positiveButtonName)), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        //Set Negative Button
        if (isDefaultNegativeButtonRequired) {
            builder.setNegativeButton(((negativeButtonName == null) ? context.getString(R.string.button_dismiss) : AppUtil.getContentString(context, positiveButtonName)), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        return builder;
    }

    /**
     * Prepare Snackbar
     *
     * @param snackbarLoginPosition - View where to show Snackbar
     * @param message               - Message of Snackbar
     * @param backgroundColor       - Background color of Snackbar
     * @return
     */
    private static Snackbar prepareSnackbar(View snackbarLoginPosition, String message, int backgroundColor) {
        //Get Snackbar Instance
        Snackbar snackbar = Snackbar.make(snackbarLoginPosition, message, Snackbar.LENGTH_LONG);

        //Get Snackbar View
        View snackbarView = snackbar.getView();

        //Set Background Color
        snackbarView.setBackgroundColor(MainApplicationClass
                .getInstance().getResources().getColor((backgroundColor == 0) ? R.color.snackbar_default_background_color : backgroundColor));

        //Get TextView Instance
        TextView textViewMessage = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textViewMessage.setTextColor(MainApplicationClass.getInstance().getResources().getColor(R.color.snackbar_default_text_color));

        return snackbar;
    }

    /**
     * Show Snackbar
     *
     * @param snackbarLoginPosition - View where to show Snackbar
     * @param message               - Message of Snackbar
     * @param backgroundColor       - Background color of Snackbar
     */
    public static void showSnackbar(View snackbarLoginPosition, String message, int backgroundColor) {
        //Get Snackbar Instance
        Snackbar snackbar = prepareSnackbar(snackbarLoginPosition, message, backgroundColor);

        //Show Snackbar
        snackbar.show();
    }




    /**
     * Show Toast Message
     *
     * @param context - context instance
     * @param message - Message for Toast
     */
    public static void showToastMessage(Context context, String message) {

        if (context == null)
            return;

        //Instantiate Toast
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show Alert Dialog Custom Dialog
     *
     * @param context
     * @param bundle
     * @param dialogListener
     */
    public static void showCustomDialog(Context context, Bundle bundle, final DialogListener dialogListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Get View
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_generic, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        //Set Title
        TextView textViewDialogTitle = (TextView) dialogView.findViewById(R.id.textViewDialogTitle);
        textViewDialogTitle.setText(bundle.getString( AppConstant.BUNDLE_DIALOG_TITLE));

        //Set Header
        String header = bundle.getString(AppConstant.BUNDLE_DIALOG_HEADER);
        if (!AppUtil.isStringEmpty(header)) {
            TextView textViewDialogHeader = (TextView) dialogView.findViewById(R.id.textViewDialogHeader);
            textViewDialogHeader.setText(header);
            textViewDialogHeader.setVisibility(TextView.VISIBLE);
        }

        //Set Description
        TextView textViewDialogDescription = (TextView) dialogView.findViewById(R.id.textViewDialogDescription);
        textViewDialogDescription.setText(TextUtils.isEmpty(bundle.getString(AppConstant.BUNDLE_DIALOG_DESCRIPTION)) ?
                "" : bundle.getString(AppConstant.BUNDLE_DIALOG_DESCRIPTION));

        //Set Positive Button
        Button buttonDialogPositive = (Button) dialogView.findViewById(R.id.buttonDialogPositive);
        buttonDialogPositive.setText(bundle.getString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT,"Okay"));
        buttonDialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dialogListener != null){
                dialogListener.onPositiveButtonClick(alertDialog);
                    return;
                }

                alertDialog.dismiss();
            }
        });

        //Set Negative Button
        Button buttonDialogNegative = (Button) dialogView.findViewById(R.id.buttonDialogNegative);
        String negativeButtonText = bundle.getString(AppConstant.BUNDLE_DIALOG_NEGATIVE_BUTTON_TEXT);
        if (AppUtil.isStringEmpty(negativeButtonText)) {
            //Hide Negative Button
            buttonDialogNegative.setVisibility(Button.GONE);
        } else {
            //Show Negative Button
            buttonDialogNegative.setVisibility(Button.VISIBLE);

            buttonDialogNegative.setText(negativeButtonText);
            buttonDialogNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(dialogListener != null){
                        dialogListener.onNegativeButtonClick(alertDialog);
                        return;
                    }

                    alertDialog.dismiss();

                }
            });
        }

        //Set Cancellable
        alertDialog.setCancelable(bundle.getBoolean(AppConstant.BUNDLE_DIALOG_CANCELLABLE));

        //Show AlertDialog
        alertDialog.show();
    }


}
