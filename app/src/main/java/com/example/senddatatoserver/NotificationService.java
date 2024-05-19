package com.example.senddatatoserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class NotificationService extends NotificationListenerService {
    private String TAG = this .getClass().getSimpleName() ;
    private static final String CHANNEL_ID = "2";

    private StatusBarNotification lastNotification;
    SupabaseAPI api = new SupabaseAPI();

    private final Handler handler = new Handler();

//    public NotificationService(){
//        Log.e("NotificationService constructor","service started");
//    }
    Context context ;
    @Override
    public void onCreate () {
        super .onCreate() ;
        Log.e("NotificationService","service started");
        context = getApplicationContext();
        Notification notification = createForegroundNotification();
        startForeground(Integer.parseInt(CHANNEL_ID), notification);
    }

    private Notification createForegroundNotification() {

        // Create and customize your foreground notification here
        // ...

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        NotificationCompat.Builder builder;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);
        builder = new NotificationCompat.Builder(this, channel.getId());

        builder.setDefaults(Notification.DEFAULT_LIGHTS);

        Intent notificationIntent = new Intent(this, KillForegroundService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
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
                .setSmallIcon(R.drawable.ic_baseline_water_drop_24)
                .addAction(0, "Stop", pendingIntent);

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        assert launchIntent != null;
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent,
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

                if (Objects.equals(sbn.getPackageName(), "com.senseonics.gen12androidapp")) {
                    Log.d(TAG, "onNotificationPosted: " + sbn.getNotification().tickerText);
                    if(!isDuplicateNotification(sbn)) {
                        lastNotification = sbn;

//                        FirebaseDatabase database = FirebaseDatabase.getInstance();
//
//                        DatabaseReference sugarValue = database.getReference("sugar");
//                        DatabaseReference calibrate = database.getReference("calibrate");
//                        DatabaseReference error = database.getReference("error");
//                        DatabaseReference tredludek = database.getReference("tredludek");
//                        DatabaseReference latestMessage = database.getReference("latestMessage");
//                        DatabaseReference lastTredludecDate = database.getReference("lastTredludecDate");
                        try {
                            if(sbn.getNotification() != null && sbn.getNotification().tickerText != null) {
                                Log.e("data received", "data: " + sbn.getNotification().tickerText);
//                                latestMessage.setValue(sbn.getNotification().tickerText.toString());
                                if (sbn.getNotification().tickerText.toString().equals("HI") ||
                                        sbn.getNotification().tickerText.toString().equals("Out of Range High Glucose")) {
//                                    sugarValue.setValue(400);
                                    api.updateSugarValue(400);
                                } else if (sbn.getNotification().tickerText.toString().equals("Calibrate Now") ||
                                        sbn.getNotification().tickerText.toString().equals("Calibrate Past Due")) {
//                                    calibrate.setValue(true);
                                    api.updateCalibrate(true);
                                } else if (sbn.getNotification().tickerText.toString().contains("---")) {
//                                    sugarValue.setValue(-1);
                                    api.updateSugarValue(-1);
                                } else {
                                    try {
//                                        sugarValue.setValue(Integer.parseInt((String) sbn.getNotification().tickerText));
                                        api.updateSugarValue(Integer.parseInt((String) sbn.getNotification().tickerText));
                                    } catch (Exception e) {

//                                        DatabaseReference otherData;
//                                        otherData = database.getReference("otherData/" + otherDataIndex);
//                                        otherData.setValue(sbn.getNotification().tickerText);
//                                        error.setValue(e.getCause() + " - " + e.getMessage());
//                                        otherDataIndex++;
                                    }
                                }
                            } else {
                                if(sbn.getNotification() != null) {
//                                    latestMessage.setValue(sbn.getNotification());
                                }
                            }
                        } catch (Exception e) {
//                            error.setValue(e.getMessage());
                            api.updateError(String.valueOf(e.getCause()), "onNotificationPosted -> parse notification");
                        }
//                        DatabaseReference date = database.getReference("date");
//                        date.getDatabase().getReference().addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                try {
//                                    lastTredludecDate.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                            if(task.isSuccessful()) {
//                                                try {
//                                                    Date resault = new Date((Long) task.getResult().getValue());
//                                                    Calendar preTime = Calendar.getInstance();
//                                                    preTime.setTime(resault);
//                                                    Calendar currentTime = Calendar.getInstance();
//                                                    currentTime.setTime(new Date());
//                                                    if(currentTime.get(Calendar.DAY_OF_MONTH) != preTime.get(Calendar.DAY_OF_MONTH)){
//                                                        tredludek.setValue(true);
////                                                        api.updateTredludec(true);
//                                                        lastTredludecDate.setValue(new Date().getTime());
//                                                    }
//                                                } catch (Exception e){
//                                                    Log.e("error 1", e.getMessage());
//                                                }
//                                            }
//                                        }
//                                    });
//
//                                } catch (Exception e){
//                                    Log.e("error",e.getMessage());
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//                        date.setValue(new Date().getTime() / 1000);

//                        Log.e("sentToFirebase", String.valueOf(sbn.getNotification().tickerText));
                        handler.postDelayed(() -> lastNotification = null, 5000);
                    }
                }
            }
        } catch (Exception e){
            Log.e("error NotificationService", e.toString());
            api.updateError(String.valueOf(e.getCause()), "onNotificationPosted");
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
    public void onDestroy() {
        super.onDestroy();
        ForegroundServiceLauncher.getInstance().startService(this);
    }
}

