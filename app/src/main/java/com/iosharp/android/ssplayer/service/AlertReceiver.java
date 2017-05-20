package com.iosharp.android.ssplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.activity.MainActivity;
import com.iosharp.android.ssplayer.utils.Utils;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Yan Yurkin
 * 22 April 2017
 */
public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra(Constants.EXTRA_NAME);
        int channel = intent.getIntExtra(Constants.EXTRA_CHANNEL, -1);
        long time = intent.getLongExtra(Constants.EXTRA_TIME, -1);

        String formattedDateString = Utils.formatLongToString(time, Constants.YEAR_TIME_FORMAT);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationManager notificationManager;

        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
            .setContentTitle(eventName)
            .setContentText("On channel " + channel + " at " + formattedDateString)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build();

        notificationManager.notify(0, notification);
    }
}
