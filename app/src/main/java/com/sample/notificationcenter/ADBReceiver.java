package com.sample.notificationcenter;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 进入d:sdk/platform-tools文件夹下
 * 发送adb指令   adb shell am broadcast -a adb.addmessage -n com.sample.notificationcenter/.ADBReceiver
 */
public class ADBReceiver extends BroadcastReceiver {

    public static BoardcastListener boardcastListensr;

    private final String ADB_ACTION = "adb.addmessage";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (ADB_ACTION.equals(action)) {
            if(isAppRunning(context)){
                if (isBackground(context)) {
                        //非前台运行  插入数据库
                        MessageBean bean = MessageDAO.getMessage();
                        MessageDAO.saveMessage(context, bean);
                } else {
                    Toast.makeText(context, "您收到一条新的消息，请尽快查收！", Toast.LENGTH_SHORT).show();
                    //前台运行，插入list中
                    boardcastListensr.insertMessage();
                }
            }else {
                MessageBean bean = MessageDAO.getMessage();
                MessageDAO.saveMessage(context, bean);
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
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = null;
        if (activityManager != null) {
            list = activityManager.getRunningTasks(100);
        }
        if (list == null || list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(context.getPackageName())) {
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
    }
}
