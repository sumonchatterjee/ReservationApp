package com.dineout.book.fragment.myaccount;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.CropOption;
import com.dineout.book.util.UiUtil;
import com.dineout.book.adapter.CropOptionAdapter;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.dialogs.UserEditNameDialog;
import com.dineout.recycleradapters.view.widgets.RoundedImageView;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;
import static com.dineout.book.dialogs.UserEditNameDialog.USER_NAME_KEY;



public class NewMyAccountFragment extends YouPageWrapperFragment
        implements View.OnClickListener, UserEditNameDialog.UserEditNameDialogCallback {

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;

    private EditText mEmailTxt;
    private EditText mPhoneEdt;
    private EditText accountHolderName;
    private RoundedImageView mProfileImage;
    private EditText bdayEdt;
    private AppCompatCheckBox maleChbx;
    private AppCompatCheckBox femaleChkbx;
    private Toolbar mToolbar;
    private Button logout;
    private RelativeLayout mMainLayout;

    private TextView emailVerifyTxt;
    private TextView phnVerifyTxt;

    private final int GET_DINER_PROFILE = 107;
    private final int LOG_OUT = 108;
    private final int UPDATE_DINER_PROFILE = 101;
    private boolean isSuccesfullyFetchedDetails = false;


    private Uri mImageCaptureUri;
    private File imageFile;
    private Bitmap photo;
    private Button updateBtn;
    private DatePickerDialog mDatePickerDialog;
    private DatePickerDialog.OnDateSetListener mDatePickerListener;


    private String mSessionState;

    public static final String DINER_GET_PROFILE_STATE= "diner_get_profile_state";
    public static final String DINER_UPDATE_PROFILE_STATE="diner_update_profile_state";


    private DialogListener mLogoutDialogListener = new DialogListener() {
        @Override
        public void onPositiveButtonClick(AlertDialog alertDialog) {
            alertDialog.dismiss();
            logOut();

            logoutButtonClickTracking();
        }

        @Override
        public void onNegativeButtonClick(AlertDialog alertDialog) {
            alertDialog.dismiss();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatePickerListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                String year = String.valueOf(selectedYear);
                String month = String.valueOf(selectedMonth + 1);
                String day = String.valueOf(selectedDay);

                if (month.length() == 1) {
                    month = "0" + month;
                }
                if (day.length() == 1) {
                    day = "0" + day;
                }

                if (bdayEdt != null) {
                    bdayEdt.setText(day + "/" + month + "/" + year);
                }
            }
        };

        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        mDatePickerDialog = new DatePickerDialog(getActivity(),R.style.datepickerCustom,
                mDatePickerListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        mDatePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_account, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //track screen
        trackScreenName(getString(R.string.countly_account));

        //track event
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_account_viewed),getString(R.string.d_account_viewed),hMap);
        inflateToolbar();
        initializeViews();
        AppUtil.hideKeyboard(getActivity());

        // authenticate the user
        authenticateUser();
    }


    private void inflateToolbar() {
        if (getView() != null) {
            mToolbar = (Toolbar) getView().findViewById(R.id.new_toolbar_fragment);
        }


        if (mToolbar != null) {
            mToolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_navigation_arrow_back, null));
            mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
            mToolbar.setTitle("My Account");
            logout = (Button) mToolbar.findViewById(R.id.logout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isSuccesfullyFetchedDetails) {
                        showLoginDialog();

                    } else {

                        Bundle bundle = new Bundle();
                        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION, getString(R.string.logout_dialog_content));
                        bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, "LOGOUT");
                        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "LOGOUT");
                        bundle.putString(AppConstant.BUNDLE_DIALOG_NEGATIVE_BUTTON_TEXT, "CANCEL");

                        UiUtil.showCustomDialog(getContext(), bundle, mLogoutDialogListener);
                    }
                }
            });
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleNavigation();
                }
            });
        }
    }


    private void initializeViews() {
        if (getView() != null) {
            mEmailTxt = (EditText) getView().findViewById(R.id.email_edtxt);
            mPhoneEdt = (EditText) getView().findViewById(R.id.phone_edtxt);
//            mEmailTxtLayout = (LinearLayout) getView().findViewById(R.id.email_lyt);
//            mPhoneEdtLayout = (LinearLayout) getView().findViewById(R.id.phone_number_lyt);
            accountHolderName = (EditText) getView().findViewById(R.id.account_holder_name);
            bdayEdt = (EditText) getView().findViewById(R.id.bday_edtxt);
            maleChbx = (AppCompatCheckBox) getView().findViewById(R.id.male_checkbx);
            femaleChkbx = (AppCompatCheckBox) getView().findViewById(R.id.female_chkbx);
            mProfileImage = (RoundedImageView) getView().findViewById(R.id.iv_edit_account_image);
            emailVerifyTxt = (TextView) getView().findViewById(R.id.email_verify_txt);
            phnVerifyTxt = (TextView) getView().findViewById(R.id.phone_no_verify_txt);
            mMainLayout= (RelativeLayout) getView().findViewById(R.id.main_layout);
            updateBtn=(Button)getView().findViewById(R.id.update_btn);


            accountHolderName.setOnClickListener(this);
            mEmailTxt.setOnClickListener(this);
            emailVerifyTxt.setOnClickListener(this);
            mPhoneEdt.setOnClickListener(this);
            phnVerifyTxt.setOnClickListener(this);

            bdayEdt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //track event
                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                    trackEventForCountlyAndGA(getString(R.string.countly_account), getString(R.string.d_dob_select),
                            getString(R.string.d_dob_select) , hMap);

                    if (mDatePickerDialog != null) {
                        mDatePickerDialog.show();
                    }
                    enableUpdateBtn();
                }
            });


        }
    }


    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);
        isSuccesfullyFetchedDetails = true;
        callScreenDataRequest();

        setupProfileImageListener();

    }


    private void callScreenDataRequest() {
        mSessionState=DINER_GET_PROFILE_STATE;
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGet(GET_DINER_PROFILE, AppConstant.URL_GET_DINER_PROFILE,
                null, this, this, false);
    }


    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        hideLoader();
        if (getView() == null || getActivity() == null)
            return;

        if (request.getIdentifier() == GET_DINER_PROFILE) {
            // Check Response
            if (responseObject != null && responseObject.optBoolean("status")) {
                // Get Output Params Object
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                if (outputParamsJsonObject != null) {
                    // Get Data Object
                    JSONObject data = outputParamsJsonObject.optJSONObject("data");

                    if (data != null) {
                        // Only if User has logged in fresh
                        //if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
                            // Handle User Response
                            AppUtil.handleUserLoginResponse(getActivity(), responseObject);
                        //}

                        // Set Details
                        setDetails(data);
                    }
                }
            } else {
                UiUtil.showSnackbar(getView(), getString(R.string.text_unable_fetch_details), 0);
            }
        } else if (request.getIdentifier() == LOG_OUT) {
            // do nothing .. just an intimation to the server
            processLogout();
        }


    }


    private void setDetails(JSONObject data) {

        updateUi();

        if (getActivity().getApplicationContext() != null) {

            String userFullName = (DOPreferences.getDinerFirstName(getActivity().getApplicationContext()) + " " +
                    DOPreferences.getDinerLastName(getActivity().getApplicationContext()));
            if(!AppUtil.isStringEmpty(userFullName)){
                accountHolderName.setText(userFullName);
            }else{
                accountHolderName.setHint(getResources().getString(R.string.my_account_name_hint));
            }

            if(!AppUtil.isStringEmpty(DOPreferences.getDinerPhone(getActivity().getApplicationContext()))) {
                mPhoneEdt.setText(DOPreferences.getDinerPhone(getActivity().getApplicationContext()));
            }else{
                mPhoneEdt.setHint(getResources().getString(R.string.my_account_enter_mobile_hint));
            }

            if(!AppUtil.isStringEmpty(DOPreferences.getDinerEmail(getActivity().getApplicationContext()))) {
                mEmailTxt.setText(DOPreferences.getDinerEmail(getActivity().getApplicationContext()));
            }else{
                mEmailTxt.setHint(getResources().getString(R.string.my_account_enter_email_hint));
            }

            if (data != null) {
                if (data.optInt("is_email_verified") == 1) {
                    emailVerifyTxt.setText("Verified/Update");
                } else {
                    emailVerifyTxt.setText("Verify/Update");
                }


                if (data.optInt("is_phone_verified") == 1) {
                    phnVerifyTxt.setText("Verified/Update");
                } else {
                    phnVerifyTxt.setText("Verify/Update");
                }

            }

            if (TextUtils.isEmpty(DOPreferences.getDinerProfileImage(getActivity().getApplicationContext()))) {
                mProfileImage.setImageResource(R.drawable.user_icon);
            } else {
                mProfileImage.setImageUrl(DOPreferences.getDinerProfileImage(getActivity().getApplicationContext()),
                        getImageLoader());
            }

            if (!TextUtils.isEmpty(DOPreferences.getDinerGender(getActivity().getApplicationContext()))) {
                String gender = DOPreferences.getDinerGender(getActivity().getApplicationContext());
                if (gender.equalsIgnoreCase("M")) {
                    checkUncheckGender(true, false);

                } else if (gender.equalsIgnoreCase("F")) {
                    checkUncheckGender(false, true);

                } else {
                    checkUncheckGender(false, false);
                }

            }
        }

        maleChbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //track event
                if(isChecked){
                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_gender),"Male",hMap);
                }
                enableUpdateBtn();
                checkUncheckGender(isChecked, !isChecked);
            }
        });


        femaleChkbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //track event
                if(isChecked){
                    HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                    trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_gender),"Female",hMap);
                }
                enableUpdateBtn();
                checkUncheckGender(!isChecked, isChecked);
            }
        });

        // set up birthday date
        String timeStampString = DOPreferences.getDinerDateOfbirth(getActivity());
        if (!TextUtils.isEmpty(timeStampString)) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(timeStampString) * 1000);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            bdayEdt.setText(day + "/" + month + "/" + year);
        } else {
            bdayEdt.setHint("Enter Date of Birth");
        }
    }


    private void logOut() {
        showLoader();
//        trackEventGA(getString(R.string.ga_screen_my_account),
//                getString(R.string.ga_action_logout), null);


        getNetworkManager().jsonRequestGet(LOG_OUT, AppConstant.URL_LOGOUT,
                ApiParams.getLogoutParams(DOPreferences.getDinerId(getContext())),
                this, this, false);
    }


    private void showLoginDialog() {
        // authenticate the user
        authenticateUser();
    }


    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            if (!TextUtils.isEmpty(mSessionState)) {
                switch (mSessionState) {
                    case DINER_GET_PROFILE_STATE:
                    isSuccesfullyFetchedDetails = true;

                    if (!TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
                        if (DOPreferences.isDinerNewUser(getActivity().getApplicationContext()))
                            DOPreferences.setDinerNewUser(getActivity().getApplicationContext(), "0");
                    }
                        break;

                    case DINER_UPDATE_PROFILE_STATE:
                        saveDetails();
                        break;
                }
            }


        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
        isSuccesfullyFetchedDetails = false;

        super.loginFlowCompleteFailure(loginFlowCompleteFailureObject);
    }


    private void checkUncheckGender(boolean isMaleChecked, boolean isFemaleChecked) {
        maleChbx.setChecked(isMaleChecked);
        femaleChkbx.setChecked(isFemaleChecked);
    }


    //add various image source listener,cropping functionality and image show on page
    void setupProfileImageListener() {

        //image selector
        final String[] items = new String[]{"Take from camera", "Select from gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) { //pick from camera
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                    imageFile = new File(Environment.getExternalStorageDirectory(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                    try {
                        intent.putExtra("return-data", true);

                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else { //pick from file

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), PICK_FROM_FILE);


                }
            }
        });

        final AlertDialog dialog = builder.create();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                enableUpdateBtn();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if (resultCode != RESULT_OK) return;

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop();
                break;

            case PICK_FROM_FILE:

                if (data != null) {
                    mImageCaptureUri = data.getData();

                    doCrop();
                }
                break;


            case CROP_FROM_CAMERA:
                if (data != null) {
                    Bundle extras = data.getExtras();

                    if (extras != null) {
                        photo = extras.getParcelable("data");
                        Bitmap bitmap = AppUtil.getCircle(photo, 100);
                        if (bitmap != null) {
                            mProfileImage.setImageBitmap(bitmap);
                        }
                        //isProfileImageSelected = true;
                        //mEditImage.bringToFront();

                    } else {
                        photo = BitmapFactory.decodeFile(getTempImageFile().getAbsolutePath());

                        if (photo != null)
                        {
                            Bitmap bitmap = AppUtil.getCircle(photo, 100);
                            if (bitmap != null) {

                                mProfileImage.setImageBitmap(bitmap);
                            }
                            //isProfileImageSelected = true;
                            //mEditImage.bringToFront();
                        }
                    }

                    File f = new File(mImageCaptureUri.getPath());
                    if (f.exists()) f.delete();
                }

                break;
        }
    }


    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            UiUtil.showSnackbar(getView(), getString(R.string.text_app_not_installed), 0);

        } else {
            intent.setData(mImageCaptureUri);

            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempImageFile()));
            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);

                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();

                    co.title = getActivity().getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getActivity().getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);

                    co.appIntent = new Intent(intent);
                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(getContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose App to Crop Image");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (imageFile != null) {
                            if (imageFile.exists()) {

                                if (mImageCaptureUri != null) {
                                    imageFile.delete();
                                    mImageCaptureUri = null;

                                }
                            }
                        }
                    }
                });

                AlertDialog alert = builder.create();

                alert.show();
            }
        }
    }


    private File getTempImageFile() {
        return new File(getActivity().getExternalFilesDir(null), "test.jpeg");
    }


    //logout
    private void processLogout() {
        DOPreferences.setloginSource(getContext(),"");
        DOPreferences.setloginDate(getContext(),"");
        //DOPreferences.setNeedtoShowCoachMark(getContext(),false);
        LoginManager.getInstance().logOut();

        DOPreferences.deleteDinerCredentials(getContext().getApplicationContext());
//        DOPreferences.setAuthKey(getContext().getApplicationContext(), null);
//        DOPreferences.setIsDinerDoPlusMember(getContext().getApplicationContext(), "0");
//        DOPreferences.setShowStepUpload(getContext().getApplicationContext(), true);
//        DOPreferences.setInAppRating(getContext().getApplicationContext(), 0);
//
//        Hotline.clearUserData(getContext().getApplicationContext());

        popToHome(getActivity());
    }


    public String getEncodedPicture() {
        if (null != photo) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        } else {
            return "";
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email_edtxt:
                // tracking
                emailClickTracking();

                String emailText = (mEmailTxt == null || mEmailTxt.getText() == null) ? "" : mEmailTxt.getText().toString();
                Bundle b = new Bundle();
                b.putString(USER_INPUT_TEXT_KEY, emailText);

                if (getActivity() != null) {
                    UserAuthenticationController.getInstance(getActivity()).startMyAccountEmailVerifyUpdateFlow(b, this);
                }
                enableUpdateBtn();

                break;

            case R.id.email_verify_txt:
                // tracking
                emailClickTracking();

                emailText = (mEmailTxt == null || mEmailTxt.getText() == null) ? "" : mEmailTxt.getText().toString();
                b = new Bundle();
                b.putString(USER_INPUT_TEXT_KEY, emailText);

                if (getActivity() != null) {
                    UserAuthenticationController.getInstance(getActivity()).startMyAccountEmailVerifyUpdateFlow(b, this);
                }
                enableUpdateBtn();

                break;

            case R.id.phone_edtxt:
                // tracking
                mobileClickTracking();

                String mobileNoText = (mPhoneEdt == null || mPhoneEdt.getText() == null) ? "" : mPhoneEdt.getText().toString();
                b = new Bundle();
                b.putString(USER_INPUT_TEXT_KEY, mobileNoText);

                if (getActivity() != null) {
                    UserAuthenticationController.getInstance(getActivity()).startMyAccountMobileVerifyUpdateFlow(b, this);
                }
                enableUpdateBtn();

                break;

            case R.id.phone_no_verify_txt:
                // tracking
                mobileClickTracking();

                mobileNoText = (mPhoneEdt == null || mPhoneEdt.getText() == null) ? "" : mPhoneEdt.getText().toString();
                b = new Bundle();
                b.putString(USER_INPUT_TEXT_KEY, mobileNoText);

                if (getActivity() != null) {
                    UserAuthenticationController.getInstance(getActivity()).startMyAccountMobileVerifyUpdateFlow(b, this);
                }
                enableUpdateBtn();

                break;

            case R.id.update_btn:
                // tracking
                updateButtonClickTracking();

                saveDetails();

                break;

            case R.id.account_holder_name:
                MasterDOFragment fragment = UserEditNameDialog.getInstance(this);
                Bundle bundle = new Bundle();
                if (!TextUtils.isEmpty(accountHolderName.getText())) {
                    bundle.putString(USER_NAME_KEY, accountHolderName.getText().toString());
                }
                fragment.setArguments(bundle);

                MasterDOFragment.showFragment(getActivity().getSupportFragmentManager(), fragment);
                enableUpdateBtn();

                break;
        }
    }


    private void saveDetails() {
        mSessionState=DINER_UPDATE_PROFILE_STATE;

        String name = accountHolderName.getText().toString();
        String encodedImage = getEncodedPicture();
        String dateOfTime = "";
        String gender;
        if(maleChbx.isChecked()){
            gender="M";
        }else if(femaleChkbx.isChecked()){
            gender="F";
        }else{
            gender="";
        }

        String dob = bdayEdt.getText().toString();
        if(!AppUtil.isStringEmpty(dob)){
            dateOfTime= AppUtil.convertDateToTimestamp(dob);
        }

        final Map<String, String> params = new HashMap<>();
        params.put(AppConstant.PARAM_TEMP, "1");
        params.put(AppConstant.PARAM_UPDATE_DINER_INFO_NAME, name);
        params.put(AppConstant.PARAM_UPDATE_DINER_INFO_GENDER, gender);
        params.put(AppConstant.PARAM_UPDATE_DINER_INFO_DOB, dateOfTime);
        params.put(AppConstant.PARAM_UPDATE_DINER_INFO_IMAGE, encodedImage);

         if(!TextUtils.isEmpty(name)){
             showLoader();

             getNetworkManager().stringRequestPost(UPDATE_DINER_PROFILE, AppConstant.URL_UPDATE_DINER_INFO,
                     params, new Response.Listener<String>() {
                         @Override
                         public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                             try {
                                 parseJSON(new JSONObject(responseObject));
                             } catch (JSONException e) {

                             }
                         }
                     }, this, false);
         }


    }



    public void parseJSON(JSONObject responseObject) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject != null) {
            if (responseObject.optBoolean("status")) {
                JSONObject outputParams = responseObject.optJSONObject("output_params");

                if (outputParams != null) {
                    JSONObject dinerData = outputParams.optJSONObject("data");

                    if (dinerData != null) {
                        DOPreferences.saveDinerCredentials(getContext(), dinerData);


                    }

                    UiUtil.showSnackbar(getView(),
                            getString(R.string.text_details_updated_success),
                            R.color.snackbar_success_background_color);


                }
            } else {
                handleErrorResponse(responseObject);
            }
        } else {
            // Show Message
            UiUtil.showSnackbar(getView(),
                    getString(R.string.text_general_error_message),
                    R.color.snackbar_error_background_color);
        }
    }


    public void handleErrorResponse(JSONObject responseObject) {
        super.handleErrorResponse(responseObject);
    }


    @Override
    public void LoginSessionExpiredPositiveClick() {
       showLoginDialog();
    }


    @Override
    public void onOkClick(String name) {
        if (accountHolderName != null) {
            if(!TextUtils.isEmpty(name)) {
                accountHolderName.setText(name);
            }else{
                accountHolderName.getText().clear();
                accountHolderName.setHint(getResources().getString(R.string.user_edit_name_hint));
            }
        }
    }

    @Override
    public void onCancelClick() {

    }



    private void updateUi() {
        updateBtn.setBackgroundResource(R.drawable.grey_rectangle_light);
        updateBtn.setOnClickListener(null);
        logout.setVisibility(View.VISIBLE);
        mMainLayout.setVisibility(View.VISIBLE);
    }


    private void enableUpdateBtn(){
        updateBtn.setBackgroundResource(R.drawable.round_rectangle_primary_button_selector);
        updateBtn.setOnClickListener(this);
    }

    private void emailClickTracking() {
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_my_account_screen_update_email_action),
//                getString(R.string.ga_new_login_my_account_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_email_type),getString(R.string.d_email_type),hMap);
    }

    private void mobileClickTracking() {
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_my_account_screen_update_mobile_action),
//                getString(R.string.ga_new_login_my_account_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_mobile_type),getString(R.string.d_mobile_type),hMap);
    }

    private void updateButtonClickTracking() {
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_my_account_screen_update_action),
//                getString(R.string.ga_new_login_my_account_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_update_click),getString(R.string.d_update_click),hMap);
    }

    private void logoutButtonClickTracking() {
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_my_account_screen_logout_action),
//                getString(R.string.ga_new_login_my_account_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_logout_click),getString(R.string.d_logout_click),hMap);
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_account),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }

}
