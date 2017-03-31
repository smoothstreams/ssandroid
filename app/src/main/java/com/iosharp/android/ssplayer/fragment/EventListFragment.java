package com.iosharp.android.ssplayer.fragment;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applidium.headerlistview.HeaderListView;
import com.crashlytics.android.Crashlytics;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.adapter.EventAdapter;
import com.iosharp.android.ssplayer.model.Event;

import java.util.ArrayList;
import java.util.Date;

import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;

public class EventListFragment extends Fragment {
    private static final String TAG = EventListFragment.class.getSimpleName();

    private static ArrayList<ArrayList<Event>> mDateEvents;
    private static ArrayList<String> mDate;
    private static EventAdapter mAdapter;
    private VideoCastManager mCastManager;
    private MiniController mMini;

    public EventListFragment() {

    }

    private static void getDateEvents(Context context, ArrayList<String> dates, ArrayList<ArrayList<Event>> events) {
        if (!dates.isEmpty()) dates.clear();
        if (!events.isEmpty()) events.clear();

        Uri uri = EventEntry.buildEventDate();
        Cursor dateCursor = context.getContentResolver().query(uri, null, null, null, null);
        String date;

        try {

            if (dateCursor != null) {
                while (dateCursor.moveToNext()) {
                    ArrayList<Event> channelEvents = new ArrayList<Event>();
                    date = dateCursor.getString(dateCursor.getColumnIndex(EventEntry.COLUMN_DATE));

                    dates.add(date);

                    Uri u = EventEntry.buildEventWithDate(date);
                    String selection = EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_END_DATE +
                            " <= ?";
                    String now = Long.toString(new Date().getTime());
                    String[] selectionArgs = new String[]{now};
                    String sortOrder = EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE +
                            ", " + EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_KEY_CHANNEL;

                    Cursor eventCursor = context.getContentResolver().query(u, null, selection, selectionArgs, sortOrder);

                    if (eventCursor != null) {
                        while (eventCursor.moveToNext()) {
                            int id = eventCursor.getInt(eventCursor.getColumnIndex(EventEntry._ID));
                            String name = eventCursor.getString(eventCursor.getColumnIndex(EventEntry.COLUMN_NAME));
                            int channel = eventCursor.getInt(eventCursor.getColumnIndex(EventEntry.COLUMN_KEY_CHANNEL));
                            String quality = eventCursor.getString(eventCursor.getColumnIndex(EventEntry.COLUMN_QUALITY));
                            long startDate = eventCursor.getLong(eventCursor.getColumnIndex(EventEntry.COLUMN_START_DATE));
                            String language = eventCursor.getString(eventCursor.getColumnIndex(EventEntry.COLUMN_LANGUAGE));

                            Event e = new Event();
                            e.setId(id);
                            e.setName(name);
                            e.setChannel(channel);
                            e.setQuality(quality);
                            e.setStartDate(startDate);
                            e.setLanguage(language);

                            channelEvents.add(e);
                        }
                    }
                    events.add(channelEvents);
                    if (eventCursor != null) {
                        eventCursor.close();
                    }
                }
            }
            if (dateCursor != null) {
                dateCursor.close();
            }
        } catch (NullPointerException e) {
            Crashlytics.logException(e);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static void updateEvents(Context context) {
        if (mDate == null) mDate = new ArrayList<>();
        if (mDateEvents == null) mDateEvents = new ArrayList<>();

        getDateEvents(context, mDate, mDateEvents);

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

   }

    @Override
    public void onResume() {
        super.onResume();
        updateEvents(getActivity());

        mCastManager = PlayerApplication.getCastManager();

        if (mCastManager != null) {
            mCastManager.incrementUiCounter();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        updateEvents(getActivity());

        //MiniController
        if (mCastManager != null) {
            mMini = (MiniController) rootView.findViewById(R.id.miniController_event);
            mCastManager.addMiniController(mMini);
        }

        HeaderListView list = (HeaderListView) rootView.findViewById(R.id.channel_list_view);
        // This can be removed when HeaderListView fixes a bug https://github.com/applidium/HeaderListView/issues/28
        //noinspection ResourceType
        list.setId(2);
        mAdapter = new EventAdapter(getActivity(), mDate, mDateEvents);
        list.setAdapter(mAdapter);

        return rootView;
    }
}


