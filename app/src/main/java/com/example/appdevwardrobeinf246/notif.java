package com.example.appdevwardrobeinf246;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ – check if exact alarms are permitted
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheck, pendingIntent);
                Log.d("WashNotif", "Exact alarm scheduled (permission granted)");
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheck, pendingIntent);
                Log.w("WashNotif", "Exact alarm not permitted – using inexact alarm");
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheck, pendingIntent);
        }
    }
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
    private void checkAndNotify() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            stopSelf();
            return;
        }

        ApiService.GetWashSchedulesRequest request = new ApiService.GetWashSchedulesRequest(userId);
        retrofitclient.getClient().getWashSchedules(request).enqueue(new retrofit2.Callback<ApiService.GetWashSchedulesResponse>() {
            @Override
            public void onResponse(Call<ApiService.GetWashSchedulesResponse> call, Response<ApiService.GetWashSchedulesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.GetWashSchedulesResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        List<ApiService.WashScheduleSummary> schedules = res.getSchedules();
                        boolean needsWash = false;
                        StringBuilder message = new StringBuilder();

                        for (ApiService.WashScheduleSummary schedule : schedules) {
                            if (schedule.nextWashDate != null &&
                                    System.currentTimeMillis() >= schedule.nextWashDate &&
                                    schedule.notificationsEnabled) {

                                needsWash = true;
                                message.append("• ").append(schedule.name).append("\n");
                                int itemCount = 0;
                                if (schedule.items != null) {
                                    for (clothitem item : schedule.items) {
                                        if (item.timesWornSinceWash >= schedule.maxWearsBeforeWash) {
                                            itemCount++;
                                        }
                                    }
                                }
                                if (schedule.outfits != null) {
                                    for (ApiService.OutfitSummary outfit : schedule.outfits) {
                                        if (outfit.getClothing_items() != null) {
                                            for (clothitem item : outfit.getClothing_items()) {
                                                if (item.timesWornSinceWash >= schedule.maxWearsBeforeWash) {
                                                    itemCount++;
                                                }
                                            }
                                        }
                                    }
                                }
                                message.append("  ").append(itemCount).append(" items need washing\n");
                            }
                        }

                        if (needsWash) {
                            sendWashNotification(message.toString());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.GetWashSchedulesResponse> call, Throwable t) {
                Log.e("WashNotif", "Failed to fetch schedules", t);
            }
        });
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