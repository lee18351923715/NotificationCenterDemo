package com.sample.notificationcenter;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class ADBReceiver extends BroadcastReceiver {

    public static BoardcastListener boardcastListensr;

    private final String ADB_ACTION = "adb.addmessage";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "您收到一条新的消息，请尽快查收！", Toast.LENGTH_SHORT).show();
        String action = intent.getAction();
        if (ADB_ACTION.equals(action)) {
            if (isBackground(context)) {
                if (boardcastListensr != null) {
                    //非前台运行  插入数据库
                    boardcastListensr.addMessage();
                }
            } else {
                //前台运行，插入list中
                boardcastListensr.insertMessage();
            }
        }
    }

    /**
     * 判断Activity是否在栈顶
     *
     * @param context
     * @param myPackage
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean isForeground(Context context, String myPackage) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equals(myPackage)) return true;
        return false;
    }

    /**
     * 判断某个app进程是否在运行
     *
     * @param context
     * @param appInfo
     * @return
     */
    public static boolean isRunningProcess(Context context, String appInfo) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppPs = myManager.getRunningAppProcesses();
        if (runningAppPs != null && runningAppPs.size() > 0) {
            if (runningAppPs.contains(appInfo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个Activity是否正在运行
     *
     * @param pkg     pkg为应用包名
     * @param cls     cls为类名eg
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isClsRunning(Context context, String pkg, String cls) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        ActivityManager.RunningTaskInfo task = tasks.get(0);
        if (task != null) {
            return TextUtils.equals(task.topActivity.getPackageName(), pkg) &&
                    TextUtils.equals(task.topActivity.getClassName(), cls);
        }
        return false;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    public interface BoardcastListener {
        void insertMessage();//讲新增的信息插入到list中

        void addMessage();//将新增的信息插入到数据库中
    }

    public void setBoardcastListensr(BoardcastListener boardcastListensr) {
        this.boardcastListensr = boardcastListensr;
    }
}
