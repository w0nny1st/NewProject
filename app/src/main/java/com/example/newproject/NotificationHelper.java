package com.example.newproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "notes_channel";
    private static final String CHANNEL_NAME = "Заметки";
    private static final String CHANNEL_DESCRIPTION = "Уведомления о заметках";

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void showSimpleNotification(String title, String message) {

        if (!checkNotificationPermission()) {
            Toast.makeText(context, "Разрешение на уведомления не предоставлено", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!notificationManager.areNotificationsEnabled()) {
            Toast.makeText(context, "Уведомления отключены в настройках", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Bitmap largeIcon = BitmapFactory.decodeResource(
                context.getResources(),
                R.mipmap.ic_launcher
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notes)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.primary_color))
                .setVibrate(new long[]{100, 200, 300})
                .setLights(Color.BLUE, 1000, 1000)
                .build();

        try {
            notificationManager.notify((int) System.currentTimeMillis(), notification);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка отправки уведомления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showNoteReminder(Note note) {

        if (!checkNotificationPermission()) {
            Toast.makeText(context, "Разрешение на уведомления не предоставлено", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!notificationManager.areNotificationsEnabled()) {
            Toast.makeText(context, "Уведомления отключены в настройках", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("note_id", note.getId());
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent deleteIntent = new Intent(context, NotificationReceiver.class);
        deleteIntent.setAction("DELETE_NOTE");
        deleteIntent.putExtra("note_id", note.getId());

        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                context, 1, deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent remindLaterIntent = new Intent(context, NotificationReceiver.class);
        remindLaterIntent.setAction("REMIND_LATER");
        remindLaterIntent.putExtra("note_id", note.getId());

        PendingIntent remindLaterPendingIntent = PendingIntent.getBroadcast(
                context, 2, remindLaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String message = note.getContent();
        if (message.length() > 100) {
            message = message.substring(0, 100) + "...";
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notes)
                .setContentTitle("Напоминание: " + note.getTitle())
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(note.getContent()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.primary_color))
                .setVibrate(new long[]{100, 200, 300})
                .setLights(Color.BLUE, 1000, 1000)
                .addAction(R.drawable.ic_delete, "Удалить", deletePendingIntent)
                .addAction(R.drawable.ic_alarm, "Напомнить позже", remindLaterPendingIntent)
                .build();

        try {
            int notificationId = note.getId().hashCode();
            notificationManager.notify(notificationId, notification);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка отправки уведомления", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Для Android 13+ нужно проверять разрешение И настройки
            return checkNotificationPermission() && notificationManager.areNotificationsEnabled();
        }
        return notificationManager.areNotificationsEnabled();
    }

    public void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public void requestNotificationPermission(android.app.Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkNotificationPermission()) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }
}