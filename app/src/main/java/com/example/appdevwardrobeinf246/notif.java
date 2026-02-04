package com.example.appdevwardrobeinf246;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import java.util.List;

public class notif extends Service {

    private static final String CHANNEL_ID = "wash_notification_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL = 30 * 60 * 1000;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        scheduleNextCheck();
        checkAndNotify();
        return START_STICKY;
    }

    private void scheduleNextCheck() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, notif.class);
        pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long nextCheck = SystemClock.elapsedRealtime() + CHECK_INTERVAL;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nextCheck, pendingIntent);
    }

    private void checkAndNotify() {
        boolean needsWash = false;
        StringBuilder message = new StringBuilder();

        for (washsched schedule : tempdb.washSchedules) {
            if (schedule.needsWashing() && schedule.notificationsEnabled) {
                needsWash = true;
                message.append("• ").append(schedule.name).append("\n");

                List<clothitem> itemsNeedingWash = schedule.getItemsNeedingWash();
                if (!itemsNeedingWash.isEmpty()) {
                    message.append("  Items needing wash: ");
                    for (int i = 0; i < Math.min(3, itemsNeedingWash.size()); i++) {
                        if (i > 0) message.append(", ");
                        message.append(itemsNeedingWash.get(i).name);
                    }
                    if (itemsNeedingWash.size() > 3) {
                        message.append(" and ").append(itemsNeedingWash.size() - 3).append(" more");
                    }
                    message.append("\n");
                }
            }
        }

        if (needsWash) {
            sendWashNotification(message.toString());
        }
    }

    private void sendWashNotification(String message) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, mainapp.class);
        intent.putExtra("openWashTab", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Time to wash your clothes")
                .setContentText("Some items have reached their wash limit")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setColor(Color.parseColor("#9ED0FF"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify(NOTIFICATION_ID + 1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wash Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for wash day reminders");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Wash Tracker Running")
                .setContentText("Monitoring wash schedules")
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}