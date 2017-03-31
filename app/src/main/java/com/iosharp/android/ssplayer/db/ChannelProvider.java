package com.iosharp.android.ssplayer.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import static com.iosharp.android.ssplayer.db.ChannelContract.CONTENT_AUTHORITY;
import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.PATH_CHANNEL;
import static com.iosharp.android.ssplayer.db.ChannelContract.PATH_EVENT;

public class ChannelProvider extends ContentProvider {
    private static final String TAG = ChannelProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int EVENT = 100;
    private static final int EVENT_WITH_CHANNEL = 101;
    private static final int EVENT_WITH_CHANNEL_AND_DATE = 102;
    private static final int EVENT_DATE = 103;
    private static final int EVENT_WITH_DATE = 104;
    private static final int CHANNEL = 300;
    private static final int CHANNEL_ID = 301;

    private static final SQLiteQueryBuilder sEventByChannelIdQueryBuilder;
    static {
        sEventByChannelIdQueryBuilder = new SQLiteQueryBuilder();
        sEventByChannelIdQueryBuilder.setTables(
                EventEntry.TABLE_NAME + " INNER JOIN " +
                        ChannelEntry.TABLE_NAME +
                        " ON " + EventEntry.TABLE_NAME +
                        "." + EventEntry.COLUMN_KEY_CHANNEL +
                        " = " + ChannelEntry.TABLE_NAME +
                        "." + ChannelEntry._ID);

    }

    private static final SQLiteQueryBuilder sChannelWithAllEventsQueryBuilder;
    static {
        sChannelWithAllEventsQueryBuilder = new SQLiteQueryBuilder();
        sChannelWithAllEventsQueryBuilder.setTables(ChannelEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                EventEntry.TABLE_NAME +
                " ON " + EventEntry.TABLE_NAME +
                "." + EventEntry.COLUMN_KEY_CHANNEL +
                " = " + ChannelEntry.TABLE_NAME +
                "." + ChannelEntry._ID);
    }

    private static final String sChannelIdSelection =
            ChannelEntry.TABLE_NAME +
                    "." + ChannelEntry._ID + " = ? ";
    private static final String sChannelIdWithStartDateSelection =
            ChannelEntry.TABLE_NAME +
                    "." + ChannelEntry._ID + " = ? AND " +
                    EventEntry.COLUMN_START_DATE + " >= ?";
    private static final String sChannelIdAndDaySelection =
            ChannelEntry.TABLE_NAME +
                    "." + ChannelEntry._ID + " = ? AND " +
                    EventEntry.COLUMN_START_DATE + " = ?";
    private static final String sDateSelection =
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_DATE +
                    " = ?";

    private DbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, PATH_EVENT, EVENT);
        matcher.addURI(authority, PATH_EVENT + "/#", EVENT_WITH_CHANNEL);
        matcher.addURI(authority, PATH_EVENT + "/#/*", EVENT_WITH_CHANNEL_AND_DATE);
        matcher.addURI(authority, PATH_EVENT + "/" + EventEntry.NAMESPACE_DATE, EVENT_DATE);
        matcher.addURI(authority, PATH_EVENT + "/" + EventEntry.NAMESPACE_DATE + "/*", EVENT_WITH_DATE);

        matcher.addURI(authority, PATH_CHANNEL, CHANNEL);
        matcher.addURI(authority, PATH_CHANNEL + "/#", CHANNEL_ID);

        return matcher;
    }

    private Cursor getEventByChannelId(Uri uri, String[] projection, String sortOrder) {
        String channel = EventEntry.getChannelFromUri(uri);
        String startDate = EventEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sChannelIdSelection;
            selectionArgs = new String[]{channel};
        } else {
            selectionArgs = new String[]{channel, startDate};
            selection = sChannelIdWithStartDateSelection;
        }

        return sEventByChannelIdQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getEventByChannelIdAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String channel = EventEntry.getChannelFromUri(uri);
        String date = EventEntry.getDateFromUri(uri);

        return sEventByChannelIdQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sChannelIdAndDaySelection,
                new String[]{channel, date},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "event/*/*"
            case EVENT_WITH_CHANNEL_AND_DATE: {
                retCursor = getEventByChannelIdAndDate(uri, projection, sortOrder);
                break;
            }
            // "event/*"
            case EVENT_WITH_CHANNEL: {
                retCursor = getEventByChannelId(uri, projection, sortOrder);
                break;
            }
            // "event/date/yyyyMMDD"
            case EVENT_WITH_DATE: {
                String date = EventEntry.getDateFromUri(uri);

                retCursor = mDbHelper.getReadableDatabase().query(
                        EventEntry.TABLE_NAME,
                        projection,
                        sDateSelection,
                        new String[] {date},
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "event/date"
            case EVENT_DATE: {
                String groupBy = EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_DATE;
                if (projection == null) {
                    projection = new String[]{EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_DATE};
                }
                retCursor = mDbHelper.getReadableDatabase().query(
                        EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        sortOrder);
                break;
            }
            // "event"
            case EVENT: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "channel/*"
            case CHANNEL_ID: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        ChannelEntry.TABLE_NAME,
                        projection,
                        ChannelEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "channel"
            // TODO: Rewrite this to look like the above EVENT_WITH_CHANNEL and EVENT_WITH_CHANNEL_AND_DATE cases
            case CHANNEL: {
                String groupBy =  ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID;

                retCursor = sChannelWithAllEventsQueryBuilder.query(mDbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        groupBy,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case EVENT_WITH_CHANNEL_AND_DATE:
                return EventEntry.CONTENT_ITEM_TYPE;
            case EVENT_WITH_CHANNEL:
                return EventEntry.CONTENT_TYPE;
            case EVENT:
                return EventEntry.CONTENT_TYPE;
            case CHANNEL:
                return ChannelEntry.CONTENT_TYPE;
            case CHANNEL_ID:
                return ChannelEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case EVENT: {
                long _id = db.insertWithOnConflict(EventEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = EventEntry.buildEventUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CHANNEL: {
                long _id = db.insertWithOnConflict(ChannelEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = ChannelEntry.buildChannelUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case EVENT:
                rowsDeleted = db.delete(
                        EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHANNEL:
                rowsDeleted = db.delete(
                        EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case EVENT:
                rowsUpdated = db.update(EventEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CHANNEL:
                rowsUpdated = db.update(ChannelEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int returnCount;
        switch (match) {
            case EVENT:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(EventEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1 ) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case CHANNEL:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(ChannelEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1 ) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
