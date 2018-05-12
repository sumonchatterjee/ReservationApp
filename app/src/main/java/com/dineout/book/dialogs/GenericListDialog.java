package com.dineout.book.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.dineout.book.R;

import java.util.ArrayList;

public class GenericListDialog extends DialogFragment {

    Context mContext;
    DialogInterface.OnCancelListener onCancelListener = null;
    private ListView mListView;
    private ArrayList<String> timingList = new ArrayList<String>();

    @SuppressLint("ValidFragment")
    public GenericListDialog(ArrayList<String> timingList, Context context) {
        this.mContext = context;
        this.timingList = timingList;
    }

    public GenericListDialog() {

    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null)
            onCancelListener.onCancel(getDialog());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.timings_dialog, null);
        builder.setView(convertView);

        mListView = (ListView) convertView.findViewById(R.id.lv_timing_list);
        Button mDismiss = (Button) convertView.findViewById(R.id.btn_dismiss);
        mDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setUpTimingList();
        return builder.create();
    }

    private void setUpTimingList() {
        if (mContext != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.gen_text_option, R.id.textViewOption, timingList);

            // Assign adapter to ListView
            mListView.setAdapter(adapter);
        }
    }
}
