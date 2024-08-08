package com.example.senddatatoserver;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationCheckWorker extends Worker {

    private static final int NOTIFICATION_ID = 2;

    public NotificationCheckWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        if (!isNotificationActive()) {
            sendNotification();
            return Result.success();
        }
        return Result.success();
    }

    private boolean isNotificationActive() {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return false;
        }

        // Android versions prior to API level 24 don't support this method
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == NOTIFICATION_ID) {
                return true;
            }
        }

        return false;
    }

    private void sendNotification() {
        Notification notification = NotificationService.createForegroundNotification(getApplicationContext());
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
