package com.sample.notificationcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class BootCompleted extends BroadcastReceiver {

    public static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
//            Intent welcomeIntent = new Intent(context, ListenService.class);
//            context.startForegroundService(welcomeIntent);
            Toast.makeText(context,"开机了",Toast.LENGTH_SHORT).show();
        }
    }
}
