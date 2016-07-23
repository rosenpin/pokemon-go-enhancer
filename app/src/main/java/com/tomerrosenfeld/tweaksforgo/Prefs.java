package com.tomerrosenfeld.tweaksforgo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
    public static String batterySaver = "battery_saver";
    public static String keepAwake = "keep_awake";
    public static String overlay = "overlay";
    public static String dim = "dim";
    public static String setup = "setup";
    public static String theme = "theme";
    public static String kill_background_processes = "kill_background_processes";
    public static String extreme_battery_saver = "extreme_battery_saver";
    public static String maximize_brightness = "maximize_brightness";
    public static String showFAB = "show_fab";
    SharedPreferences preferences;

    public Prefs(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return preferences.getInt(key, def);
    }

    public String getString(String key, String def) {
        return preferences.getString(key, def);
    }

    public void set(String key, boolean val) {
        preferences.edit().putBoolean(key, val).apply();
    }

    public void set(String key, int val) {
        preferences.edit().putInt(key, val).apply();
    }

    public void set(String key, String val) {
        preferences.edit().putString(key, val).apply();
    }

    public void apply(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
}
