package com.example.senddatatoserver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Objects;

public class NotificationService extends NotificationListenerService {

    private String TAG = this .getClass().getSimpleName() ;
    private static final String CHANNEL_ID = "2";

    private StatusBarNotification lastNotification;
    private final Handler handler = new Handler();

//    public NotificationService(){
//        Log.e("NotificationService constructor","service started");
//    }
    Context context ;
    @Override
    public void onCreate () {
        super .onCreate() ;
        Log.e("NotificationService","service started");
        context = getApplicationContext() ;
        Notification notification = createForegroundNotification();
        startForeground(1, notification);
    }

    private Notification createForegroundNotification() {

        // Create and customize your foreground notification here
        // ...

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification with your desired content
        Intent notificationIntent = new Intent(this, KillForegroundService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID).setOngoing(true)
                .setContentTitle("Sugar Share")
                .setContentText("Service is running, sending sugar value to server.")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_baseline_water_drop_24)
                .addAction(0, "Force Stop", pendingIntent)
                .build();
    }
    @Override
    public void onNotificationPosted (StatusBarNotification sbn) {
        try {
            Log.i(TAG, "********** onNotificationPosted");
            Log.i(TAG, sbn.toString());
            Log.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());
            if (sbn.getPackageName() != null) {

                if (Objects.equals(sbn.getPackageName(), "com.senseonics.gen12androidapp")) {

                    if(!isDuplicateNotification(sbn)) {
                        lastNotification = sbn;

                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        DatabaseReference sugarValue = database.getReference("sugar");
                        try {
                            if (sbn.getNotification().tickerText != "High Glucose") {
                                sugarValue.setValue(Integer.parseInt((String) sbn.getNotification().tickerText));
                            } else {
                                sugarValue.setValue(400);
                            }
                        } catch (Exception e) {
                            sugarValue.setValue(0);
                        }

//                DatabaseReference noValue = database.getReference("noValue");
//                noValue.setValue(false);

                        DatabaseReference date = database.getReference("date");
                        date.setValue(new Date().toString());

                        Log.e("sentToFirebase", String.valueOf(sbn.getNotification().tickerText));
                        handler.postDelayed(() -> lastNotification = null, 5000);
                    }
                }
            }
        } catch (Exception e){
            Log.e("error NotificationService", e.toString());
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
        if(sbn.getPackageName() != null) {
            Log.i(TAG, "********** onNotificationRemoved");
            Log.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());
        }
    }

    @Override
    public void onDestroy(){
        Log.e("NotificationService","service destroyed");
    }
}