package com.dineout.book.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.controller.MyClipboardManager;
import com.dineout.book.util.UiUtil;
import com.dineout.book.adapter.ShareIntentGridAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by prateek.aggarwal on 4/14/16.
 */
public class GenericShareDialog extends DialogFragment {

    public static final String WHATSAPP_PACKAGE = "com.whatsapp";
    public static final String GMAIL_PACAKGE = "com.google.android.gm";
    public static final String FACEBOOK_PACKAGE = "com.facebook.katana";
    public static final String MESSAGE_PACKAGE = "com.android.mms";
    public static final String GOOGLE_PLUS = "com.google.android.apps.plus";
    public static final String TWITTER_PACKAGE = "com.twitter.android";
    @SuppressWarnings("unused")
    public static final int FB_RESULT_CODE = 101;
    public static final int GMAIL_RESULT_CODE = 102;
    public static final int WHATSAPP_RESULT_CODE = 103;
    public static final int MESSAGE_RESULT_CODE = 104;
    public static final int GOOGLE_PLUS_RESULT_CODE = 105;
    GridView mGridView;
    DialogInterface.OnCancelListener onCancelListener = null;
    ShareIntentGridAdapter adapter;
    List<ResolveInfo> activities = null, finalListPackages = null;
    HashMap<String, ResolveInfo> activitiesMap = null;

    private JSONObject shareJSON;

    public static GenericShareDialog getInstance(String share) {

        GenericShareDialog dialog = new GenericShareDialog();
        try {
            dialog.shareJSON = new JSONObject(share);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void setTitle() {
        getDialog().setTitle(R.string.share);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(R.string.share);

        getDialog().getWindow().setBackgroundDrawableResource(R.color.white);
        View view = inflater.inflate(R.layout.share_dialog_fragment, container);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        activitiesMap = new HashMap<String, ResolveInfo>();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");

        if (getActivity() != null) {
            activities = getActivity().getPackageManager().queryIntentActivities(sendIntent, 0);
            for (ResolveInfo info : activities) {
                activitiesMap.put(info.activityInfo.packageName, info);
            }
        }

        manageInstalledPackages();

        mGridView = (GridView) getView().findViewById(R.id.gridview_share_apps);
        if (adapter == null)
            adapter = new ShareIntentGridAdapter(getActivity(), new ArrayList<ResolveInfo>());

        adapter.clear();
        adapter.notifyDataSetChanged();

        if (finalListPackages == null || finalListPackages.size() == 0) {
            // Show Message
            UiUtil.showToastMessage(getContext(), getString(R.string.text_application_not_installed));

            // Dismiss Dialog
            dismissAllowingStateLoss();

        } else {
            int packageSize = finalListPackages.size();
            for (int i = 0; i < packageSize; i++) {
                adapter.add(finalListPackages.get(i));
            }

            mGridView.setAdapter(adapter);

            // Hide More option
            getView().findViewById(R.id.tv_more_option).setVisibility(View.GONE);

            setupListeners();
        }
    }

    private void setupListeners() {

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if (finalListPackages == null)
                    return;
                ResolveInfo info = finalListPackages.get(position);
                if (info.activityInfo.packageName.equals(FACEBOOK_PACKAGE)) {
                    shareOnFacebook(info);
                } else if (info.activityInfo.packageName.equals(WHATSAPP_PACKAGE)) {
                    shareOnWhatsapp(info);
                } else if (info.activityInfo.packageName.equals(GMAIL_PACAKGE)) {
                    shareOnGmail(info);
                } else if (info.activityInfo.packageName.equals(MESSAGE_PACKAGE)) {
                    shareOnMessages(info);
                } else if (info.activityInfo.packageName.equals(TWITTER_PACKAGE)) {
                    shareOnTwitter(info);
                } else {
                    shareOnOthers(info);
                }
                GenericShareDialog.this.dismiss();
            }
        });

        getView().findViewById(R.id.tv_more_option).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);

                for (int i = 4; i < finalListPackages.size(); i++) {
                    adapter.add(finalListPackages.get(i));
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void manageInstalledPackages() {
        finalListPackages = new ArrayList<ResolveInfo>();

        if (activitiesMap.containsKey(WHATSAPP_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(WHATSAPP_PACKAGE));
            activitiesMap.remove(WHATSAPP_PACKAGE);
        }
        if (activitiesMap.containsKey(GMAIL_PACAKGE)) {
            finalListPackages.add(activitiesMap.get(GMAIL_PACAKGE));
            activitiesMap.remove(GMAIL_PACAKGE);
        }
        if (activitiesMap.containsKey(FACEBOOK_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(FACEBOOK_PACKAGE));
            activitiesMap.remove(FACEBOOK_PACKAGE);
        }
        if (activitiesMap.containsKey(MESSAGE_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(MESSAGE_PACKAGE));
            activitiesMap.remove(MESSAGE_PACKAGE);
        }
        if (activitiesMap.containsKey(GOOGLE_PLUS)) {
            finalListPackages.add(activitiesMap.get(GOOGLE_PLUS));
            activitiesMap.remove(GOOGLE_PLUS);
        }
        if (activitiesMap.containsKey(TWITTER_PACKAGE)) {
            finalListPackages.add(activitiesMap.get(TWITTER_PACKAGE));
            activitiesMap.remove(TWITTER_PACKAGE);
        }

        // Commented by Shashank - 17 May'16 (to hide options like Zomato)
        /*for (String packageName : activitiesMap.keySet()) {
            finalListPackages.add(activitiesMap.get(packageName));
        }*/
    }


    private void shareOnFacebook(ResolveInfo info) {

        final String finalSharingText = shareJSON.optString("facebook");

        final MoreDialog moreDialog = new MoreDialog(getContext().getResources().getString(R.string.referral_invite_fb_title),
                finalSharingText, "Cancel", "Copy", getContext(), true);
        moreDialog.setTextStyle(getResources().getDimension(R.dimen.font_size_16), Gravity.CENTER);
        moreDialog.setTextTitleStyle(getResources().getDimension(R.dimen.font_size_14), Gravity.CENTER);

        moreDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {


                UiUtil.showToastMessage(MainApplicationClass.getInstance(),
                        MainApplicationClass.getInstance().getString(R.string.text_copied));

                moreDialog.dismiss();

                MyClipboardManager clipboardManager = new MyClipboardManager();
                clipboardManager.copyToClipboard(MainApplicationClass.getInstance(), finalSharingText);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setPackage("com.facebook.katana");

                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setType("text/plain");
//                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://www.dineout.co.in/get");
                MainApplicationClass.getInstance().startActivity(shareIntent);
            }
        });

        moreDialog.show(getActivity().getSupportFragmentManager(), "ShareOnFB");
    }

    private void shareOnWhatsapp(ResolveInfo info) {

        if (shareJSON == null)
            return;

        String sharingText = shareJSON.optString("whatsapp");


        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(WHATSAPP_PACKAGE, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        startActivityForResult(sharingIntent, WHATSAPP_RESULT_CODE);
    }

    private void shareOnGmail(ResolveInfo info) {

        if (shareJSON == null)
            return;

        JSONObject emailContent = shareJSON.optJSONObject("email");
        String sharingText = emailContent.optString("body", "").replace("\\n", "\n");
        String subject = emailContent.optString("subject");

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(GMAIL_PACAKGE, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);

        // sharingIntent.putExtra(Intent.EXTRA_TEXT, getSharingTextOthers());
        startActivityForResult(sharingIntent, GMAIL_RESULT_CODE);
    }

    private void shareOnMessages(ResolveInfo info) {

        String sharingText = shareJSON.optString("sms");


        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(MESSAGE_PACKAGE, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        startActivityForResult(sharingIntent, MESSAGE_RESULT_CODE);
    }

    private void shareOnTwitter(ResolveInfo info) {

        String sharingText = shareJSON.optString("twitter");

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        try {
            sharingIntent.setClassName(TWITTER_PACKAGE, "com.twitter.android.composer.ComposerActivity");
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
            startActivityForResult(sharingIntent, MESSAGE_RESULT_CODE);
        } catch (Exception e) {
            sharingIntent.setClassName(TWITTER_PACKAGE, info.activityInfo.name);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
            startActivityForResult(sharingIntent, MESSAGE_RESULT_CODE);
        }
    }

    private void shareOnOthers(ResolveInfo info) {

        String sharingText = shareJSON.optString("default");

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Let's plan");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        startActivityForResult(sharingIntent, GOOGLE_PLUS_RESULT_CODE);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null)
            onCancelListener.onCancel(getDialog());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null)
            getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


}