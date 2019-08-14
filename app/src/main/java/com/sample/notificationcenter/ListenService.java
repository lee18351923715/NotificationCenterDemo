package com.sample.notificationcenter;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

public class ListenService extends Service {

    private ADBReceiver adbReceiver;

    private final String ADB_ACTION = "adb.addmessage";
    public ListenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        //Toast.makeText(this,"Service已经启动了",Toast.LENGTH_SHORT).show();
//        //动态注册广播接收器，拦截"adb.addmessage"的广播
//        adbReceiver = new ADBReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ADB_ACTION);
//        registerReceiver(adbReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(adbReceiver);
    }
}
