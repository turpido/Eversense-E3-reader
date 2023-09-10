package com.example.senddatatoserver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class KillForegroundService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            context.stopService(new Intent().setComponent(service.service));
        }
        Intent stopServiceIntent = new Intent(context, NotificationService.class);
        context.stopService(stopServiceIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(0);
    }
}
