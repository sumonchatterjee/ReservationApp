package com.dineout.book.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.example.dineoutnetworkmodule.DOPreferences;

public class ForceUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.force_update_activity);

        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).
                trackScreen("Force Update");

        // Initialize View
        initializeView();
    }

    private void initializeView() {
        Button updateButton = (Button) findViewById(R.id.bt_force_update);
        updateButton.setOnClickListener(this);

        Button skipButton = (Button) findViewById(R.id.bt_skip_force_update);
        skipButton.setOnClickListener(this);

        TextView mTitle = (TextView) findViewById(R.id.tv_fu_title);
        TextView mSubtitle = (TextView) findViewById(R.id.tv_fu_subtitle);

        if (getIntent().getBooleanExtra("fu_can_skip", false)) {
            skipButton.setVisibility(View.VISIBLE);
        } else {
            skipButton.setVisibility(View.GONE);
        }


        String title = DOPreferences.getForceUpdateTitle(getApplicationContext());
        String subTitle = DOPreferences.getForceUpdateSubTitle(getApplicationContext());

        mTitle.setText(TextUtils.isEmpty(title) ? "Force Upgrade" : title);
        mSubtitle.setText(TextUtils.isEmpty(subTitle) ? "Force Upgrade" : subTitle);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_force_update:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dineout.book")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dineout.book")));
                }
                break;

            case R.id.bt_skip_force_update:
                finish();
                break;

            default:
                break;
        }
    }
}
