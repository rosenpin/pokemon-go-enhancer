package com.tomerrosenfeld.tweaksforgo.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.tomerrosenfeld.tweaksforgo.Globals;
import com.tomerrosenfeld.tweaksforgo.Prefs;
import com.tomerrosenfeld.tweaksforgo.Receivers.ScreenReceiver;

import java.io.IOException;
import java.util.List;

public class MainService extends Service {
    private Prefs prefs;
    private PowerManager.WakeLock wl;
    private boolean isGoOpen = false;
    private WindowManager windowManager;
    private LinearLayout black;
    private WindowManager.LayoutParams windowParams;
    private int originalBrightness;
    private SensorManager sensorManager;
    private ScreenReceiver screenReceiver;
    private IntentFilter filter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(MainService.class.getSimpleName(), "Main service started");
        wl = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "Tweaks For GO Tag");
        prefs = new Prefs(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        originalBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
        checkIfGoIsCurrentApp();
        initAccelerometer();
        initScreenReceiver();
    }

    private void checkIfGoIsCurrentApp() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final long INTERVAL = 1000;
            final long end = System.currentTimeMillis();
            final long begin = end - INTERVAL;
            UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            final UsageEvents usageEvents = manager.queryEvents(begin, end);
            while (usageEvents.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    if (event.getPackageName().equals("com.nianticlabs.pokemongo")) {
                        GOLaunched();
                    } else {
                        GOClosed();
                    }
                }
            }
        } else {
            ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals("com.nianticlabs.pokemongo")) {
                GOLaunched();
            } else {
                GOClosed();
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkIfGoIsCurrentApp();
            }
        }, 1000);
    }

    private void GOLaunched() {
        if (prefs.getBoolean(Prefs.batterySaver, false))
            setBatterySaver(true);
        if (prefs.getBoolean(Prefs.keepAwake, true))
            wl.acquire();
        if (prefs.getBoolean(Prefs.overlay, false))
            registerAccelerator();

        isGoOpen = true;
    }

    private void GOClosed() {
        if (isGoOpen) {
            if (prefs.getBoolean(Prefs.batterySaver, false))
                setBatterySaver(false);
            if (wl.isHeld())
                wl.release();
            unregisterAccelerator();
        }
        isGoOpen = false;
    }

    private void initScreenReceiver() {
        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        screenReceiver = new ScreenReceiver();
    }

    private void unregisterScreenReceiver() {
        try {
            unregisterReceiver(screenReceiver);
        } catch (Exception ignored) {
        }
    }

    private void initAccelerometer() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        black = new LinearLayout(getApplicationContext());
        black.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        black.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
        Globals.blackLayout = black;
        windowParams = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
        windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
    }

    private void registerAccelerator() {
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        Sensor accelerometerSensor;
        if (sensorList.size() > 0) {
            accelerometerSensor = sensorList.get(0);
            sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void unregisterAccelerator() {
        try {
            sensorManager.unregisterListener(accelerometerListener);
        } catch (Exception ignored) {
        }
    }

    private void setBatterySaver(boolean status) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global low_power " + (status ? 1 : 0)});
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SensorEventListener accelerometerListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        boolean isBlack;

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            float y_value = arg0.values[1];
            if (y_value < -3 && y_value > -17) {
                if (!isBlack) {
                    isBlack = true;
                    darkenTheScreen(true);
                }
            } else if (isBlack) {
                isBlack = false;
                darkenTheScreen(false);
            }
        }
    };

    private void darkenTheScreen(boolean state) {
        if (state && isGoOpen) {
            windowManager.addView(black, windowParams);
            registerReceiver(screenReceiver, filter);
        } else {
            try {
                windowManager.removeView(black);
                unregisterScreenReceiver();
            } catch (Exception ignored) {
                Log.d("Receiver", "View is not attached");
            }
        }
        if (prefs.getBoolean(Prefs.dim, false))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, state ? 0 : originalBrightness);
    }
}
