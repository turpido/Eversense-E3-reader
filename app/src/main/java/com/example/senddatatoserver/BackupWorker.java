package com.example.senddatatoserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import javax.xml.transform.Result;

public class BackupWorker extends Worker {

    private static final String TAG = "BackupWorker";

    public BackupWorker (@NonNull Context context, @NonNull WorkerParameters workerParams ) {
        super ( context, workerParams );
    }

    @NonNull
    @Override
    public Result doWork() {
        // Start your NotificationListenerService here
        ComponentName componentName = new ComponentName(getApplicationContext(), NotificationService.class);
        getApplicationContext().getPackageManager().setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        return Result.success();
    }
}
