package com.dineout.book.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dineout.book.R;

/**
 * Created by Hemant on 23-03-2015.
 */
public class MoreDialog extends DialogFragment {

    Context mContext;
    boolean showCancelOk = false;
    float titleTextSize, textSize = 0;
    int titleTextGravity, textGravity = 0;
    DialogInterface.OnCancelListener onCancelListener = null;
    DialogInterface.OnDismissListener onDismissListener = null;
    private String text, title, cancelText, okText;

    @SuppressLint("ValidFragment")
    public MoreDialog(String text, Context context) {
        this.mContext = context;
        this.text = text;
    }

    @SuppressLint("ValidFragment")
    public MoreDialog(String title, String text, Context context) {
        this.title = title;
        this.text = text;
        this.mContext = context;
    }

    @SuppressLint("ValidFragment")
    public MoreDialog(String title, String text, Context context, boolean showCancelOk) {
        this.mContext = context;
        this.text = text;
        this.title = title;
        this.showCancelOk = showCancelOk;

    }

    @SuppressLint("ValidFragment")
    public MoreDialog(String title, String text, String cancelText, String okText, Context context, boolean showCancelOk) {
        this.mContext = context;
        this.text = text;
        this.title = title;
        this.cancelText = cancelText;
        this.okText = okText;
        this.showCancelOk = showCancelOk;

    }

    public MoreDialog() {

    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void setTextStyle(float textSize, int gravity) {
        this.textSize = textSize;
        this.textGravity = gravity;
       /* mTextFull.setTextSize(textSize);
        mTextFull.setGravity(gravity);*/
    }

    public void setTextTitleStyle(float textSize, int gravity) {
        titleTextSize = textSize;
        titleTextGravity = gravity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.more_dialog, null);
        builder.setView(convertView);

        TextView mTextFull = (TextView) convertView.findViewById(R.id.tv_full_text);
        Button mDismiss = (Button) convertView.findViewById(R.id.btn_dismiss);
        TextView mTitle = (TextView) convertView.findViewById(R.id.tv_dialog_title);
        Button mCancel = (Button) convertView.findViewById(R.id.tv_dialog_cancel);
        Button mOkButton = (Button) convertView.findViewById(R.id.tv_dialog_ok);
        LinearLayout mCancelOkLayout = (LinearLayout) convertView.findViewById(R.id.ll_cancel_ok);

        //mTextFull.setText(text);
        mTextFull.setText(Html.fromHtml(text));
        //mTextFull.setTypeface(DoFonts.ROBOTTO_REGULAR);

        if (textSize != 0) {
            mTextFull.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mTextFull.setGravity(textGravity);
        }
        if (titleTextSize != 0) {
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mTitle.setGravity(textGravity);
        }

        if (title != null && !(title.equalsIgnoreCase(""))) {
            //mTitle.setTypeface(DoFonts.ROBOTTO_MEDIUM);
            mTitle.setText(title);
            mTitle.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(cancelText) && !TextUtils.isEmpty(okText)) {
            mCancel.setText(cancelText);
            mOkButton.setText(okText);
        }

        if (showCancelOk) {
            mCancelOkLayout.setVisibility(View.VISIBLE);
            mDismiss.setVisibility(View.GONE);
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelListener.onCancel(getDialog());
                }
            });
        } else {
            mCancelOkLayout.setVisibility(View.GONE);
            mDismiss.setVisibility(View.VISIBLE);
            mDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDismissListener == null) {
                        dismiss();
                    } else {
                        onDismissListener.onDismiss(getDialog());
                    }
                }
            });
        }
        return builder.create();
    }
}
