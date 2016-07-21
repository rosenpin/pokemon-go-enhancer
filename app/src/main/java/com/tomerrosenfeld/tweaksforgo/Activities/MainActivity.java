package com.tomerrosenfeld.tweaksforgo.Activities;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.tomerrosenfeld.tweaksforgo.Services.MainService;
import com.tomerrosenfeld.tweaksforgo.Prefs;
import com.tomerrosenfeld.tweaksforgo.R;
import com.tomerrosenfeld.tweaksforgo.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        if (!prefs.getBoolean(Prefs.setup, false)) {
            //Start setup
            startActivity(new Intent(this, TeamPicker.class));
            finish();
        } else {
            //Actual oncreate
            setTheme(prefs.getInt(Prefs.theme, prefs.getInt(Prefs.theme, R.style.AppTheme)));
            setContentView(R.layout.activity_main);
            getFragmentManager().beginTransaction()
                    .replace(R.id.preferences_holder, new SettingsFragment())
                    .commit();
            startService(new Intent(getApplicationContext(), MainService.class));
        }
    }

    private boolean hasUsageAccess() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static void askForPermission(Activity activity, DialogInterface.OnClickListener onClickListener, boolean app, String permissionName) {
        new AlertDialog.Builder(activity)
                .setTitle("A permission is required")
                .setMessage("This " + (app ? "app " : "feature ") + "requires a special permission to " + permissionName + ", click grant to allow it now")
                .setPositiveButton("Grant permission", onClickListener)
                .show();
    }

    private void handleUsageAccessPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                if (!hasUsageAccess()) {
                    askForPermission(this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    }, true, "access usage");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                askForPermission(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivity(intent);
                    }
                }, true, "access usage");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleUsageAccessPermission();
    }
}
