package com.example.senddatatoserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import androidx.work.Constraints;
//import androidx.work.NetworkType;
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    int REQUEST_NOTIFICATION_CODE = 101;
    @SuppressLint("BatteryLife")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
//            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BackupWorker.class)
//                    .setConstraints(new Constraints.Builder()
//                            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
//                            .build())
//                    .build();
//            WorkManager.getInstance(this).enqueue(workRequest);
//
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},REQUEST_NOTIFICATION_CODE);
            }
            if (!isNotificationAccessEnabled()) {
                // If not, request the permission
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
            if(ContextCompat.checkSelfPermission(
                    MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                    isNotificationAccessEnabled()) {
                ForegroundServiceLauncher.getInstance().startService(this);
                finishAndRemoveTask();
            }
        } catch (Exception e){
            Log.e("error MainActivity", e.toString());
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                SupabaseAPI api = new SupabaseAPI();
                api.updateError(String.valueOf(e.getCause()), t.getName());
            }
        });
    }

    private boolean isNotificationAccessEnabled() {
        String packageName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(packageName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode == REQUEST_NOTIFICATION_CODE) {
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(isNotificationAccessEnabled()) {
                            ForegroundServiceLauncher.getInstance().startService(this);
                            finishAndRemoveTask();
                        } else {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                            startActivity(intent);
                        }
                    }
                }
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "activity destroyed");
    }
}
