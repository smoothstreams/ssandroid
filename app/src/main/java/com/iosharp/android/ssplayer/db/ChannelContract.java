package com.iosharp.android.ssplayer.db;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChannelContract {

    public static final String CONTENT_AUTHORITY = "com.iosharp.android.ssplayer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_EVENT = "event";
    public static final String PATH_CHANNEL = "channel";
    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getDbDateString(long unixTimestamp) {
        Date date = new Date(unixTimestamp);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    public static String getDbDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    public static final class EventEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;

        public static final String NAMESPACE_DATE = "date";

        public static final String TABLE_NAME = "events";
        public static final String COLUMN_KEY_CHANNEL = "channel_id";
        public static final String COLUMN_NETWORK = "network";
        public static final String COLUMN_NAME = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        // time since Unix epoch stored as a long
        public static final String COLUMN_START_DATE = "start_date";
        // time since Unix epoch stored as a long
        public static final String COLUMN_END_DATE = "end_date";
        // runtime stored as long
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_QUALITY = "quality";

        public static final String COLUMN_DATE = "date";

        public static Uri buildEventUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEventDate() {
            return CONTENT_URI.buildUpon().appendPath(NAMESPACE_DATE).build();
        }

        public static Uri buildEventWithDate(String date){
            return CONTENT_URI.buildUpon().appendPath(NAMESPACE_DATE).appendPath(date).build();
        }

        public static Uri buildEventChannel(long channel) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(channel)).build();
        }

        public static Uri buildEventChannelWithStartDate(long id, String startDate) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id))
                    .appendQueryParameter(COLUMN_START_DATE, startDate).build();
        }

        public static Uri buildEventChannelWithDate(long id, String date) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(date).build();
        }

        public static String getChannelFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_START_DATE);
        }
    }

    public static final class ChannelEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHANNEL).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CHANNEL;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CHANNEL;

        public static final String TABLE_NAME = "channels";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ICON = "icon";

        public static Uri buildChannelUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
