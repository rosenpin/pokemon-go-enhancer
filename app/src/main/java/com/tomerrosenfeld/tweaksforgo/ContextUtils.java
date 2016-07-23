package com.tomerrosenfeld.tweaksforgo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ContextUtils {
    public static void openUrl(Context activity, String url) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
