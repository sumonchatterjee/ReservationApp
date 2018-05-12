package com.dineout.book.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.dineout.book.R;
import com.dineout.book.interfaces.CallPhonePermissionGrantedListener;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;

import java.util.ArrayList;

public class PhoneDialog extends DialogFragment
        implements AdapterView.OnItemClickListener, CallPhonePermissionGrantedListener {

    ListView mListView;

    Activity activityContext;
    DialogInterface.OnCancelListener onCancelListener = null;
    private ArrayList<String> phoneList = new ArrayList<String>();
    private int itemSelectedIndex;

    public PhoneDialog() {
        super();
    }

    @SuppressLint("ValidFragment")
    public PhoneDialog(ArrayList<String> phoneList, Activity activityContext) {
        this.activityContext = activityContext;
        this.phoneList = phoneList;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Register Call Phone Permission Listener
//        if (getActivity() instanceof MasterDOLauncherActivity) {
//            ((MasterDOLauncherActivity) getActivity()).registerListenerForCallPhonePermission(this);
//        }

        return inflater.inflate(R.layout.phone_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onViewCreated(View convertView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(convertView, savedInstanceState);

        mListView = (ListView) convertView.findViewById(R.id.lv_phone_list);
        Button mDismiss = (Button) convertView.findViewById(R.id.btn_dismiss);

        mDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setUpPhoneList();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null)
            onCancelListener.onCancel(getDialog());
    }

    private void setUpPhoneList() {
        if (activityContext == null) {
            activityContext = getActivity();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityContext, R.layout.gen_text_option, R.id.textViewOption, phoneList);
        mListView.setOnItemClickListener(this);

        // Assign adapter to ListView
        mListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Set Item Selected
        itemSelectedIndex = position;

        //If Permission is Granted
        if (PermissionUtils.handleCallPermission(activityContext)) {
            //Dismiss Dialog
            dismiss();

            //Perform Calling Function
            performCall();
        }
    }

    @Override
    public void onDestroyView() {
        // UnRegister Call Phone Permission Listener
//        if (getActivity() instanceof MasterDOLauncherActivity) {
//            ((MasterDOLauncherActivity) getActivity()).unregisterListenerForCallPhonePermission();
//        }

        super.onDestroyView();
    }

    /**
     * Perform Calling Function
     */
    public void performCall() {
        String itemValue = (String) mListView.getItemAtPosition(itemSelectedIndex);

        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + itemValue));

        try {
            phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(phoneIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            UiUtil.showToastMessage(activityContext, activityContext.getString(R.string.call_failed_response));
        }
    }

    @Override
    public void onCallPhonePermissionGrant(boolean granted) {
        if (granted) {
            //Dismiss Dialog
            dismissAllowingStateLoss();

            //Perform Calling Function
            performCall();
        }
    }
}

