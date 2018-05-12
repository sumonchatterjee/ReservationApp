package com.dineout.livehandler;

import android.content.Context;
import android.view.LayoutInflater;

/**
 * Created by sawai on 20/08/16.
 */
public class ViewController {
    private Context mContext;
    private LayoutInflater mInflater;

    public ViewController(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
    }

    private DialogView dialogView;
    public IView getDialogView() {
        if (dialogView == null) {
            dialogView = new DialogView(mContext, mInflater);
        }
        return dialogView;
    }

    public void destroyDialogView() {
        try {
            dialogView.getView().removeAllViews();
            dialogView = null;
        } catch (Exception e) {
            // exception
        }
    }
}
