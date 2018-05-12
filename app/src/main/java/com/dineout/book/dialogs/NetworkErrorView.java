package com.dineout.book.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.activity.DineoutMainActivity;

public class NetworkErrorView extends RelativeLayout implements OnClickListener {

    CharSequence msg = "", buttonText = getContext().getString(R.string.button_try_again);
    TextView tvConditionalMsg;
    ImageView mIcon;
    Button mRefresh;
    ImageView mCallDineout;
    String customErrorMessage;
    Context mContext;
    ConditionalDialog conditionalDialogType = ConditionalDialog.INTERNET_CONNECTION;


    public NetworkErrorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initViews(attrs, defStyle);
    }

    public NetworkErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews(attrs, 0);
    }

    public NetworkErrorView(Context context) {
        super(context);
        mContext = context;
        initViews(null, 0);
    }

    private void initViews(AttributeSet attrs, int defStyle) {

        LayoutInflater.from(getContext()).inflate(R.layout.layout_conditional_screen, this, true);

        if (attrs != null) {
            initAttributes(attrs, defStyle);
        }

        tvConditionalMsg = (TextView) findViewById(R.id.tv_conditional_message);
        mIcon = (ImageView) findViewById(R.id.iv_conditional_icon);
        mRefresh = (Button) findViewById(R.id.btn_conditional_button);
        //mRefresh.setTypeface(DoFonts.ROBOTTO_MEDIUM);
        mCallDineout = (ImageView) findViewById(R.id.iv_dineout_call);

        tvConditionalMsg.setText(msg);
        mRefresh.setText(buttonText);

        mRefresh.setOnClickListener(this);
        mCallDineout.setOnClickListener(this);
        setType(conditionalDialogType);

        setVisibility(View.GONE);

        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TcConditionalView, defStyle, 0);

            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {

                int attr = a.getIndex(i);

                switch (attr) {
                    case R.styleable.TcConditionalView_viewMessage:

                        msg = a.getText(attr);

                        break;
                    case R.styleable.TcConditionalView_viewButtonMessage:

                        buttonText = a.getText(attr);

                        break;
                    case R.styleable.TcConditionalView_viewLoaderBackgroundVisible:

                        break;

                    case R.styleable.TcConditionalView_viewType:

                        switch (a.getInt(attr, 0)) {
                            case 1:
                                conditionalDialogType = ConditionalDialog.SERVER_ERROR;

                                break;
                            case 2:
                                conditionalDialogType = ConditionalDialog.INTERNET_CONNECTION;
                                break;
                            case 3:
                                conditionalDialogType = ConditionalDialog.INTERNET_CHECKING;
                                break;
                            case 4:
                                conditionalDialogType = ConditionalDialog.LOADING;
                                break;

                            default:
                                break;
                        }

                        break;

                    default:
                        break;
                }
            }
            a.recycle();
        }
    }

    public void setType(ConditionalDialog type) {
        conditionalDialogType = type;
        if (type == ConditionalDialog.SERVER_ERROR) {

            //mIcon.setImageResource(R.drawable.tc_condition_graphic_serverdown);
            tvConditionalMsg.setText(R.string.server_down);
            mRefresh.setVisibility(View.VISIBLE);

        } else if (type == ConditionalDialog.INTERNET_CONNECTION) {

            mIcon.setImageResource(R.drawable.ic_no_internet);
            tvConditionalMsg.setText(R.string.no_network_connection);
            mRefresh.setVisibility(View.VISIBLE);

        } else if (type == ConditionalDialog.INTERNET_CHECKING) {


            if (AppUtil.hasNetworkConnection(mContext)) {
                tvConditionalMsg.setText(R.string.checking);
                mRefresh.setVisibility(View.INVISIBLE);
            } else {
                setType(type);
                mRefresh.setVisibility(View.VISIBLE);
            }

        } else if (type == ConditionalDialog.CUSTOM_ERROR) {

            tvConditionalMsg.setText(customErrorMessage);
            mRefresh.setVisibility(View.VISIBLE);

        } else if (type == ConditionalDialog.LOADING) {

        } else if (type == ConditionalDialog.CUSTOM_ERROR) {
            tvConditionalMsg.setText(getCustomErrorMessage());
            mRefresh.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_conditional_button) {
            // if current dialog is Network Error, then first we need to show Internet checking dialog for some time then

            //setType(ConditionalDialog.INTERNET_CHECKING);

            boolean isConnected = AppUtil.hasNetworkConnection(mContext.getApplicationContext());
            ((DineoutMainActivity) mContext).onNetworkChanged(isConnected);


            // }
        }

        if (v.getId() == R.id.iv_dineout_call) {
            //If Permission is Granted
            if (PermissionUtils.handleCallPermission((Activity) mContext)) {
                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                phoneIntent.setData(Uri.parse("tel:+919212340202"));

                try {
                    phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(phoneIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    //Consumed Exception
                }
            }
        }
    }

    public String getCustomErrorMessage() {
        return customErrorMessage;
    }

    public void setCustomErrorMessage(String customErrorMessage) {
        this.customErrorMessage = customErrorMessage;
    }

    public enum ConditionalDialog {
        SERVER_ERROR,
        INTERNET_CONNECTION,
        INTERNET_CHECKING,
        LOADING,
        CUSTOM_ERROR
    }

}
