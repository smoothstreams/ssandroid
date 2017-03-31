package com.iosharp.android.ssplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.activity.MainActivity;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.fragment.AlertFragment;
import com.iosharp.android.ssplayer.fragment.EventListFragment;
import com.iosharp.android.ssplayer.utils.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import static com.iosharp.android.ssplayer.db.ChannelContract.getDbDateString;

public class SmoothService extends IntentService {
    private static final String TAG = SmoothService.class.getSimpleName();

    public SmoothService() {
        super("SmoothService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String USER_AGENT = PlayerApplication.getUserAgent(getApplicationContext());
        final OkHttpClient client = new OkHttpClient();
        String channelsJsonStr = null;

        try {
            final String SMOOTHSTREAMS_JSON_FEED = "http://cdn.smoothstreams.tv/schedule/feed.json";

            URL url = new URL(SMOOTHSTREAMS_JSON_FEED);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            channelsJsonStr = response.body().string();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Channel information
        final String TAG_CHANNEL_NAME = "name";
        final String TAG_CHANNEL_ICON = "img";

        // Event information. Each event is an element of the 'items' array
        final String TAG_CHANNEL_ITEMS = "items";

        final String TAG_EVENT_ID = "id";
        final String TAG_EVENT_NETWORK = "network";
        final String TAG_EVENT_NAME = "name";
        final String TAG_EVENT_DESCRIPTION = "description";
        final String TAG_EVENT_START_DATE = "time";
        final String TAG_EVENT_END_DATE = "end_time";
        final String TAG_EVENT_RUNTIME = "runtime";
        final String TAG_EVENT_CHANNEL = "channel";
        final String TAG_EVENT_LANGUAGE = "language";
        final String TAG_EVENT_QUALITY = "quality";
        final String TAG_EVENT_CATEGORY = "category";

        try {
            long startTime = System.currentTimeMillis();

            JSONObject channelList = new JSONObject(channelsJsonStr);
            Vector<ContentValues> channelsVector = new Vector<ContentValues>(channelList.length());
            Vector<ContentValues> eventsVector = new Vector<ContentValues>();

            for (int i = 1; i < channelList.length() + 1; i++) {
                // Channel starts at 1, not zero

                // For every channel..
                JSONObject c = channelList.getJSONObject(Integer.toString(i));

                int channelId = i;
                String channelName = c.getString(TAG_CHANNEL_NAME);
                String channelIcon = c.getString(TAG_CHANNEL_ICON);

                ContentValues channelValues = new ContentValues();
                channelValues.put(ChannelContract.ChannelEntry._ID, channelId);
                channelValues.put(ChannelContract.ChannelEntry.COLUMN_NAME, channelName);
                channelValues.put(ChannelContract.ChannelEntry.COLUMN_ICON, channelIcon);

                channelsVector.add(channelValues);

                // Get the channels' events, if any are there.
                if (c.has(TAG_CHANNEL_ITEMS)) {
                    JSONArray items = c.getJSONArray(TAG_CHANNEL_ITEMS);

                    for (int j = 0; j < items.length(); j++) {
                        String cleanEventName = null;
                        JSONObject e = (JSONObject) items.get(j);

                        int eventId = Integer.parseInt(e.getString(TAG_EVENT_ID));
                        String eventNetwork = e.getString(TAG_EVENT_NETWORK);
                        String eventName = Utils.getCleanTitle(e.getString(TAG_EVENT_NAME));
                        String eventDescription = e.getString(TAG_EVENT_DESCRIPTION);
                        long eventStartDate = Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE));
                        long eventEndDate = Utils.convertDateToLong(e.getString(TAG_EVENT_END_DATE));
                        int eventRuntime = Integer.parseInt(e.getString(TAG_EVENT_RUNTIME));
                        int eventChannel = Integer.parseInt(e.getString(TAG_EVENT_CHANNEL));
                        String eventCategory = e.getString(TAG_EVENT_CATEGORY);
                        String eventLanguage = e.getString(TAG_EVENT_LANGUAGE);
                        String eventQuality = e.getString(TAG_EVENT_QUALITY);
                        String eventDate = getDbDateString(Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE)));

                        // Handle umlaute http://hootcook.blogspot.de/2009/04/java-charset-encoding-utf-8.html
                        try {
                            byte[] bytes = eventName.getBytes("ISO-8859-1");
                            cleanEventName = new String(bytes, "UTF-8");
                        } catch (UnsupportedEncodingException e1) {
                            Crashlytics.logException(e1);
                            e1.printStackTrace();
                        }

                        ContentValues eventValues = new ContentValues();

                        eventValues.put(ChannelContract.EventEntry._ID, eventId);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_KEY_CHANNEL, eventChannel);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_NETWORK, eventNetwork);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_NAME, cleanEventName);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_DESCRIPTION, eventDescription);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_START_DATE, eventStartDate);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_END_DATE, eventEndDate);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_RUNTIME, eventRuntime);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_CATEGORY, eventCategory);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_LANGUAGE, eventLanguage);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_QUALITY, eventQuality);
                        eventValues.put(ChannelContract.EventEntry.COLUMN_DATE, eventDate);

                        eventsVector.add(eventValues);
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            Log.v(TAG, "PARSING TIME: " + totalTime);

            if (eventsVector.size() > 0) {
                ContentValues[] eventsArray = new ContentValues[eventsVector.size()];
                eventsVector.toArray(eventsArray);
                int eventRowsInserted = this.getContentResolver()
                        .bulkInsert(ChannelContract.EventEntry.CONTENT_URI, eventsArray);

//                        Log.v(TAG, "inserted " + eventRowsInserted + " rows of events");
            }

            if (channelsVector.size() > 0) {
                ContentValues[] channelsArray = new ContentValues[channelsVector.size()];
                channelsVector.toArray(channelsArray);
                int channelRowsInserted = this.getContentResolver()
                        .bulkInsert(ChannelContract.ChannelEntry.CONTENT_URI, channelsArray);

//                Log.v(TAG, "inserted " + channelRowsInserted + " rows of channels");
            }
        }  catch (JSONException e) {
            Crashlytics.logException(e);
        } catch (NullPointerException e) {
            Crashlytics.logException(e);
        }

        // Delete events that have already passed
        String now = Long.toString(new Date().getTime());
        getContentResolver()
                .delete(ChannelContract.EventEntry.CONTENT_URI,
                        ChannelContract.EventEntry.COLUMN_END_DATE + "< ?",
                        new String[]{now});

        Intent eventIntent = new Intent(this, EventReceiver.class);
        sendBroadcast(eventIntent);
    }

    static public class SyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SmoothService.class);
            context.startService(sendIntent);
        }
    }

    static public class EventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                EventListFragment.updateEvents(context);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    static public class AlertReceiver extends BroadcastReceiver {
        public static final String EXTRA_NAME = "name";
        public static final String EXTRA_CHANNEL = "channel";
        public static final String EXTRA_TIME = "time";

        @Override
        public void onReceive(Context context, Intent intent) {
            String eventName = intent.getStringExtra(EXTRA_NAME);
            int channel = intent.getIntExtra(EXTRA_CHANNEL, -1);
            long time = intent.getLongExtra(EXTRA_TIME, -1);

            String formattedDateString = Utils.formatLongToString(time, AlertFragment.TIME_FORMAT);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            NotificationManager notificationManager;

            notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(context)
                    .setContentTitle(eventName)
                    .setContentText("On channel " + channel + " at " + formattedDateString)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();

            notificationManager.notify(0, notification);
        }
    }
}
