package com.dineout.book.interfaces;

import android.app.AlertDialog;

public interface DialogListener {
    /**
     * Positive Button Listener
     */
    void onPositiveButtonClick(AlertDialog alertDialog);

    /**
     * Negative Button Listener
     */
    void onNegativeButtonClick(AlertDialog alertDialog);
}