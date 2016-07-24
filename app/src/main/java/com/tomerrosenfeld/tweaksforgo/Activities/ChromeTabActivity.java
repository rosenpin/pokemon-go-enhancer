package com.tomerrosenfeld.tweaksforgo.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;

import com.tomerrosenfeld.tweaksforgo.Constants;
import com.tomerrosenfeld.tweaksforgo.Globals;
import com.tomerrosenfeld.tweaksforgo.R;

public class ChromeTabActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chrome_tabs);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(ChromeTabActivity.this, Uri.parse(Globals.url));
    }

    boolean firstLaunch = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstLaunch) {
            startActivity(getPackageManager().getLaunchIntentForPackage(Constants.GOPackageName));
            finish();
        } else
            firstLaunch = false;
    }
}
