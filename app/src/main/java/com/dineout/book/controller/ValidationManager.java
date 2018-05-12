package com.dineout.book.controller;

import android.content.Context;
import android.util.Patterns;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;

public class ValidationManager {

    /**
     * Function to validate Email
     *
     * @param context
     * @param data
     * @return
     */
    public static String validateEmail(Context context, String data) {
        String errorMessage = null;

        if (AppUtil.isStringEmpty(data)) {
            errorMessage = String.format(context.getString(R.string.container_is_required), context.getString(R.string.text_email));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(data).matches()) {
            errorMessage = String.format(context.getString(R.string.container_enter_valid), context.getString(R.string.text_email));
        }

        return errorMessage;
    }

    /**
     * Function to validate Password
     *
     * @param context
     * @param data
     * @return
     */
    public static String validatePassword(Context context, String data) {
        String errorMessage = null;

        if (AppUtil.isStringEmpty(data)) {
            errorMessage = context.getString(R.string.text_password_range_error);
        } else if (data.contains(" ")) {
            errorMessage = context.getString(R.string.text_password_space_error);
        } else if (data.length() < 6 || data.length() > 12) {
            errorMessage = context.getString(R.string.text_password_range_error);
        }

        return errorMessage;
    }

    /**
     * Function to validate Phone Number
     *
     * @param context
     * @param data
     * @return
     */
    public static String validatePhoneNumber(Context context, String data) {
        String errorMessage = null;

        if (AppUtil.isStringEmpty(data)) {
            errorMessage = String.format(context.getString(R.string.container_is_required), context.getString(R.string.text_phone_number));
        } else if (data.length() < 10 || data.length() > 10) {
            errorMessage = context.getString(R.string.text_phone_number_10_digit);
        } else if (data.contains("+")) {
            errorMessage = context.getString(R.string.text_accept_digits);
        } else if (!Patterns.PHONE.matcher(data).matches()) {
            errorMessage = String.format(context.getString(R.string.container_enter_valid), context.getString(R.string.text_phone_number));
        }

        return errorMessage;
    }

    /**
     * Function to validate Name
     *
     * @param context
     * @param data
     * @return
     */
    public static String validateName(Context context, String data) {
        String errorMessage = null;

        if (AppUtil.isStringEmpty(data)) {
            errorMessage = String.format(context.getString(R.string.container_is_required), context.getString(R.string.text_name));
        } else if (data.length() <= 3) {
            errorMessage = context.getString(R.string.text_name_atleast_4);
        } else if (!data.matches("[a-zA-Z ]+")) {
            errorMessage = context.getString(R.string.text_accept_alphabets);
        }

        return errorMessage;
    }
}
