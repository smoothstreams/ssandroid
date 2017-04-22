package com.iosharp.android.ssplayer.service;

import android.content.Intent;

public class SmoothService {
    private static final String TAG = SmoothService.class.getSimpleName();
    private static final String TAG_CHANNEL_NAME = "name";
    private static final String TAG_CHANNEL_ICON = "img";
    private static final String TAG_CHANNEL_ITEMS = "items";
    private static final String TAG_EVENT_ID = "id";
    private static final String TAG_EVENT_NETWORK = "network";
    private static final String TAG_EVENT_NAME = "name";
    private static final String TAG_EVENT_DESCRIPTION = "description";
    private static final String TAG_EVENT_START_DATE = "time";
    private static final String TAG_EVENT_END_DATE = "end_time";
    private static final String TAG_EVENT_RUNTIME = "runtime";
    private static final String TAG_EVENT_CHANNEL = "channel";
    private static final String TAG_EVENT_LANGUAGE = "language";
    private static final String TAG_EVENT_QUALITY = "quality";
    private static final String TAG_EVENT_CATEGORY = "category";

    protected void onHandleIntent(Intent intent) {
//                        String cleanEventName = null;
//                        JSONObject e = (JSONObject) items.get(j);
//
//                        int eventId = Integer.parseInt(e.getString(TAG_EVENT_ID));
//                        String eventNetwork = e.getString(TAG_EVENT_NETWORK);
//                        // For example, Mike &amp; Mike -> Mike & Mike
//                        String eventName = e.getString(TAG_EVENT_NAME).replace("&amp;", "&");
//                        String eventDescription = e.getString(TAG_EVENT_DESCRIPTION);
//                        long eventStartDate = Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE));
//                        long eventEndDate = Utils.convertDateToLong(e.getString(TAG_EVENT_END_DATE));
//                        int eventRuntime = Integer.parseInt(e.getString(TAG_EVENT_RUNTIME));
//                        int eventChannel = Integer.parseInt(e.getString(TAG_EVENT_CHANNEL));
//                        String eventCategory = e.getString(TAG_EVENT_CATEGORY);
//                        String eventLanguage = e.getString(TAG_EVENT_LANGUAGE);
//                        String eventQuality = e.getString(TAG_EVENT_QUALITY);
//                        String eventDate = getDbDateString(Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE)));
//
//                        // Handle umlaute http://hootcook.blogspot.de/2009/04/java-charset-encoding-utf-8.html
//                        try {
//                            byte[] bytes = eventName.getBytes("ISO-8859-1");
//                            cleanEventName = new String(bytes, "UTF-8");
//                        } catch (UnsupportedEncodingException e1) {
//                            Crashlytics.logException(e1);
//                            e1.printStackTrace();
//                        }
//
//
//        // Delete events that have already passed
//        String now = Long.toString(new Date().getTime());
//        getContentResolver()
//                .delete(ChannelContract.EventEntry.CONTENT_URI,
//                        ChannelContract.EventEntry.COLUMN_END_DATE + "< ?",
//                        new String[]{now});
//
    }
}
