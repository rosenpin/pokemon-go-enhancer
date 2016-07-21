package com.tomerrosenfeld.tweaksforgo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.WindowManager;

import com.tomerrosenfeld.tweaksforgo.Activities.MainActivity;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    boolean shouldAllowOverlay;
    boolean shouldAllowDim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        updateNotificationPreference();
        findPreference("battery_saver").setOnPreferenceChangeListener(this);
        findPreference("overlay").setOnPreferenceChangeListener(this);
        findPreference("dim").setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNotificationPreference();
        updatePermissionsBasedPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateNotificationPreference();
    }

    private void updatePermissionsBasedPreferences() {
        if (!hasDrawingPermission())
            ((TwoStatePreference) findPreference("overlay")).setChecked(false);
        else if (shouldAllowOverlay)
            ((TwoStatePreference) findPreference("overlay")).setChecked(true);
        if (!hasModifySettingsPermission())
            ((TwoStatePreference) findPreference("dim")).setChecked(false);
        else if (shouldAllowDim)
            ((TwoStatePreference) findPreference("dim")).setChecked(true);
    }

    private void updateNotificationPreference() {
        findPreference("notification").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isNotificationForGOInstalled())
                    startActivity(getActivity().getPackageManager().getLaunchIntentForPackage("com.tomer.poke.notifier"));
                else
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tomer.poke.notifier")));
                return false;
            }
        });
    }

    private boolean isNotificationForGOInstalled() {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getActivity().getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.tomer.poke.notifier"))
                return true;
        }
        return false;
    }

    private boolean hasDrawingPermission() {
        try {
            View view = new View(getActivity());
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).addView(view, lp);
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).removeView(view);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasModifySettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getActivity())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals("battery_saver"))
            if (Shell.SU.available())
                return true;
            else
                Snackbar.make(getActivity().findViewById(android.R.id.content), "This feature requires root", Snackbar.LENGTH_LONG).show();
        if (preference.getKey().equals("overlay")) {
            if (!hasDrawingPermission()) {
                MainActivity.askForPermission(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            shouldAllowOverlay = true;
                        }
                    }
                }, false, "show a black screen over other apps");
            } else
                return true;
        }
        if (preference.getKey().equals("dim")) {
            if (!hasModifySettingsPermission()) {
                MainActivity.askForPermission(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            shouldAllowDim = true;
                        }
                    }
                }, false, "change system settings");
            } else
                return true;
        }
        return false;
    }
}
