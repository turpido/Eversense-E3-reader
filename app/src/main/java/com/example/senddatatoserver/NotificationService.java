package com.example.senddatatoserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NotificationService extends NotificationListenerService {
    private String TAG = this .getClass().getSimpleName() ;
    private static final String CHANNEL_ID = "2";
    private StatusBarNotification lastNotification;
    public static int mCurrentNotificationsCounts = 0;
    SupabaseAPI api = new SupabaseAPI();

    private final Handler handler = new Handler();

//    public NotificationService(){
//        Log.e("NotificationService constructor","service started");
//    }
    Context context ;
    @Override
    public void onCreate () {
        super .onCreate();
        Log.e("NotificationService","service started");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        Notification notification = createForegroundNotification(this);
        startForeground(Integer.parseInt(CHANNEL_ID), notification);
        scheduleNotificationCheck();
        return START_STICKY;
    }

    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(this, NotificationService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    private void scheduleNotificationCheck() {
        PeriodicWorkRequest notificationCheckRequest =
                new PeriodicWorkRequest.Builder(NotificationCheckWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueue(notificationCheckRequest);
    }

    public static Notification createForegroundNotification(Context context) {

        // Create and customize your foreground notification here
        // ...

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        NotificationCompat.Builder builder;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);
        builder = new NotificationCompat.Builder(context, channel.getId());

        builder.setDefaults(Notification.DEFAULT_LIGHTS);

        Intent notificationIntent = new Intent(context, KillForegroundService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        builder.setContentTitle("Sugar Share")
                .setChannelId(CHANNEL_ID)
                .setContentText("Service is running, sending sugar value to server.")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColor(Color.WHITE)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_water_drop_24)
                .addAction(0, "Stop", pendingIntent);

        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        assert launchIntent != null;
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        return notification;
    }
    @Override
    public void onNotificationPosted (StatusBarNotification sbn) {
        try {
            Log.i(TAG, "********** onNotificationPosted");
            Log.i(TAG, sbn.toString());
            Log.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());
            if (sbn.getPackageName() != null) {

                if (Objects.equals(sbn.getPackageName(), "com.dexcom.g7")) {
                    Log.d(TAG, "onNotificationPosted: " + sbn.getNotification().tickerText);
                    if(!isDuplicateNotification(sbn)) {
                        lastNotification = sbn;
                        try {

                            String sugar = extractSugarFromNotification(sbn.getNotification());
                            int sugarInt;
                            try {
                                sugarInt = Integer.parseInt(sugar);
                                api.updateSugarValue(sugarInt);
                            } catch (Exception e) {
//                                api.updateSugarValue(-1);
                            }


//                            if(sbn.getNotification() != null && sbn.getNotification().tickerText != null) {
//                                Log.e("data received", "data: " + sbn.getNotification().tickerText);
//                                if (sbn.getNotification().tickerText.toString().equals("HI") ||
//                                        sbn.getNotification().tickerText.toString().equals("Out of Range High Glucose")) {
//                                    api.updateSugarValue(400);
//                                } else if (sbn.getNotification().tickerText.toString().equals("Calibrate Now") ||
//                                        sbn.getNotification().tickerText.toString().equals("Calibrate Past Due")) {
//                                    api.updateCalibrate(true);
//                                } else if (sbn.getNotification().tickerText.toString().contains("---")) {
//                                    api.updateSugarValue(-1);
//                                } else {
//                                    try {
//                                        api.updateSugarValue(Integer.parseInt((String) sbn.getNotification().tickerText));
//                                    } catch (Exception e) {
//                                    }
//                                }
//                            } else {
//                                if(sbn.getNotification() != null) {
//                                }
//                            }
                        } catch (Exception e) {
                            api.updateError(String.valueOf(e.getCause()), "onNotificationPosted -> parse notification");
                        }
                        handler.postDelayed(() -> lastNotification = null, 5000);
                    }
                }
            }
        } catch (Exception e){
            Log.e("error NotificationService", e.toString());
            api.updateError(String.valueOf(e.getCause()), "onNotificationPosted");
        }
    }

    private String extractSugarFromNotification(Notification notification){
        try {
            RemoteViews contentView  = notification.contentView;
            View applied = contentView.apply(this, null);
            ViewGroup root = (ViewGroup) applied.getRootView();
            ArrayList<TextView> texts = new ArrayList<>();
            getTextViews(texts, root);
            return texts.get(0).getText().toString();
        } catch (Exception e){
            return "";
        }
    }
    private void getTextViews(final List<TextView> output, final ViewGroup parent) {
        int children = parent.getChildCount();
        for (int i = 0; i < children; i++) {
            View view = parent.getChildAt(i);
            if (view.getVisibility() == View.VISIBLE) {
                if (view instanceof TextView) {
                    output.add((TextView) view);
                } else if (view instanceof ViewGroup) {
                    getTextViews(output, (ViewGroup) view);
                }
            }
        }
    }

    private boolean isDuplicateNotification(StatusBarNotification sbn) {
        return lastNotification != null && lastNotification.getKey().equals(sbn.getKey());
    }

    @Override
    public void onListenerDisconnected() {
        // Notification listener disconnected - requesting rebind
        requestRebind(new ComponentName(this, NotificationListenerService.class));
        Log.e("NotificationService", "disconnect - reconnecting...");
    }

    @Override
    public void onNotificationRemoved (StatusBarNotification sbn) {
        Log.e("NotificationService", "onNotificationRemoved");
        if(sbn.getPackageName() != null) {
            Log.i(TAG, "********** onNotificationRemoved");
            Log.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toggleNotificationListenerService();
        ForegroundServiceLauncher.getInstance().startService(this);
    }
}

