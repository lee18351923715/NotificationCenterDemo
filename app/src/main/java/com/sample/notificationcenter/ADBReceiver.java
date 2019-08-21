package com.sample.notificationcenter;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.List;

/**
 * 进入d:sdk/platform-tools文件夹下
 * 发送adb指令   adb shell am broadcast -a adb.addmessage -n com.sample.notificationcenter/.ADBReceiver
 */
public class ADBReceiver extends BroadcastReceiver {

    public static BoardcastListener boardcastListensr;

    private final String START_MESSAGE_CENTER = "start_message_center";

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

        }else if (START_MESSAGE_CENTER.equals(action)){
            Intent intent1 = new Intent(context,MainActivity.class);
            //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//添加标记
            context.startActivity(intent1);
        }
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
