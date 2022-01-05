package com.supermarket.store.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.supermarket.store.R;
import com.supermarket.store.activity.HomeActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseService";
    private LocalBroadcastManager broadcaster;
    String id;
    NotificationManager notificationManager;
    String title = "", text = "",notif_action="";
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG, "From: " + new Gson().toJson(remoteMessage));
        title = remoteMessage.getNotification().getTitle();
        text = remoteMessage.getNotification().getBody();
        try {
            notif_action = remoteMessage.getData().get("notif_action");
        }catch(Exception exception) {
            exception.printStackTrace();
        }
        Log.e("title", title + text +notif_action);
        sendNotification(title);

    }
    private void sendNotification(String message) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.bell_ring_new);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "CH_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            id = "id_product";
            CharSequence name = "Product";
            String description = "Notifications regarding our products";
            int importance = NotificationManager.IMPORTANCE_MAX;
            @SuppressLint("WrongConstant") NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLACK);
            notificationManager.createNotificationChannel(mChannel);

            if (soundUri != null) {
                // Changing Default mode of notification
                notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                // Creating an Audio Attribute
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                // Creating Channel
                NotificationChannel notificationChannel = new NotificationChannel("CH_ID", "Testing_Audio", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setSound(soundUri, audioAttributes);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationManager.notify(0, notificationBuilder.build());
        }
        if (notif_action != null && notif_action.equalsIgnoreCase("new_booking_qrt")){
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bell_ring_new);
            Intent intent1 = new Intent(this, HomeActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent1);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            broadcaster.sendBroadcast(intent);
            broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
            PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder1 = new NotificationCompat.Builder(getApplicationContext(), "id_product")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setChannelId(id)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setSound(sound)
                    .setContentIntent(pendingIntent1);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notificationBuilder1.build());
        }
    }

}