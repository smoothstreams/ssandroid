package com.iosharp.android.ssplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;

public class DbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 51;
	
    public static String DATABASE_NAME = "smoothstreams.db";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + ChannelEntry.TABLE_NAME + " ( " +
                ChannelEntry._ID + " INTEGER PRIMARY KEY, " +
                ChannelEntry.COLUMN_NAME + " TEXT, " +
                ChannelEntry.COLUMN_ICON + " TEXT);";

        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + " ( " +
                EventEntry._ID + " INTEGER PRIMARY KEY, " +
                EventEntry.COLUMN_KEY_CHANNEL + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_NETWORK + " TEXT, " +
                EventEntry.COLUMN_NAME + " TEXT, " +
                EventEntry.COLUMN_DESCRIPTION + " TEXT, " +
                EventEntry.COLUMN_START_DATE + " REAL, " +
                EventEntry.COLUMN_END_DATE + " REAL, " +
                EventEntry.COLUMN_RUNTIME + " REAL, " +
                EventEntry.COLUMN_LANGUAGE + " TEXT, " +
                EventEntry.COLUMN_CATEGORY + " TEXT, " +
                EventEntry.COLUMN_QUALITY + " TEXT, " +
                EventEntry.COLUMN_DATE + " TEXT, " +

                "FOREIGN KEY (" + EventEntry.COLUMN_KEY_CHANNEL + ") REFERENCES " +
                ChannelEntry.TABLE_NAME + " (" + ChannelEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_CHANNEL_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChannelEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}
