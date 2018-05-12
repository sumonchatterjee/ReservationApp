package com.dineout.book.dialogs;

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
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;



public class LoginSessionExpireDialog extends MasterDOFragment implements View.OnClickListener {

    private TextView mContent;
    private Button mOkBtn;
    private Button mCancelBtn;
    private LoginSessionExpiredButtonCallbacks mCallback;

    public static final String ERROR_MSG_KEY = "error_msg_key";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.session_expire_dialog,container,false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // initialize views
        initializeViews();

        if (getArguments() != null) {
            String errorMsg = getArguments().getString(ERROR_MSG_KEY);
            if (TextUtils.isEmpty(errorMsg)) {
                errorMsg = getResources().getString(R.string.login_error_message);
            }

            mContent.setText(errorMsg);
        }
    }

    public void setLoginSessionExpireButtonCallback(LoginSessionExpiredButtonCallbacks callback) {
        this.mCallback = callback;
    }

    private void initializeViews(){
        if(getView()!= null) {
            mContent = (TextView) getView().findViewById(R.id.tv_full_text);
            mOkBtn = (Button) getView().findViewById(R.id.tv_dialog_ok);
            mCancelBtn = (Button) getView().findViewById(R.id.tv_dialog_cancel);

            mOkBtn.setOnClickListener(this);
            mCancelBtn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_dialog_ok:
                dismiss();

                if (mCallback != null) {
                    mCallback.LoginSessionExpiredPositiveClick();
                }
                break;

            case R.id.tv_dialog_cancel:
                dismiss();

                if (mCallback != null) {
                    mCallback.LoginSessionExpiredNegativeClick();
                }
                break;
        }
    }

    public interface LoginSessionExpiredButtonCallbacks {
        void LoginSessionExpiredPositiveClick();
        void LoginSessionExpiredNegativeClick();
    }
}
