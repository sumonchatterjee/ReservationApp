package com.dineout.book.dialogs;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOFragment;

/**
 * Created by sawai on 12/12/16.
 */

public class UserEditNameDialog extends MasterDOFragment implements View.OnClickListener {
    private EditText mUserEditText;
    private Button mOkButton;
    private Button mCancelButton;

    private static UserEditNameDialog mInstance;
    private UserEditNameDialogCallback mCallback;

    public static String USER_NAME_KEY = "user_name_key";

    public interface UserEditNameDialogCallback {
        void onOkClick(String name);

        void onCancelClick();
    }

    public static UserEditNameDialog getInstance(UserEditNameDialogCallback callback) {
        mInstance = new UserEditNameDialog();
        mInstance.mCallback = callback;

        return mInstance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_edit_name_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }


    //    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        if (getDialog() != null ) {
//            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//            if (getDialog().getWindow() != null) {
//                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            }
//        }
//
//        mUserEditText = (EditText) view.findViewById(R.id.user_edit_name);
//        mOkButton = (Button) view.findViewById(R.id.left_button);
//        mCancelButton = (Button) view.findViewById(R.id.right_button);
//
//
//        if (getArguments() != null) {
//            String name = getArguments().getString(USER_NAME_KEY);
//            name = TextUtils.isEmpty(name) ? "" : name;
//            mUserEditText.setText(name);
//            mUserEditText.setSelection(name.length());
//        }
//
//        mOkButton.setOnClickListener(this);
//        mCancelButton.setOnClickListener(this);
//    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (getView() != null) {
            mUserEditText = (EditText) getView().findViewById(R.id.user_edit_name);
            mOkButton = (Button) getView().findViewById(R.id.left_button);
            mCancelButton = (Button) getView().findViewById(R.id.right_button);


            if (getArguments() != null) {
                String name = getArguments().getString(USER_NAME_KEY);
                name = TextUtils.isEmpty(name) ? "" : name;
                mUserEditText.setText(name);
                mUserEditText.setSelection(mUserEditText.getText().length());
            }
            mOkButton.setOnClickListener(this);
            mCancelButton.setOnClickListener(this);
        }

        AppUtil.showKeyBoard(getActivity());
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_button:
                AppUtil.hideKeyboard(mUserEditText, getActivity());

                if (mUserEditText != null) {
                    String userName = mUserEditText.getText().toString();
                    userName = userName.trim();

                    if (TextUtils.isEmpty(userName)) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.my_account_enter_name_error), Toast.LENGTH_SHORT).show();
                    } else {
                        dismiss();

                        if (mCallback != null) {
                            mCallback.onOkClick(userName);
                        }
                    }
                }
                break;

            case R.id.right_button:
                AppUtil.hideKeyboard(mUserEditText, getActivity());

                dismiss();

                if (mCallback != null) {
                    mCallback.onCancelClick();
                }
                break;
        }
    }
}
