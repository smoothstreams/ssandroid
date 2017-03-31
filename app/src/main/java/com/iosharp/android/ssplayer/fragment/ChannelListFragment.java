package com.iosharp.android.ssplayer.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.activity.SettingsActivity;
import com.iosharp.android.ssplayer.adapter.ChannelAdapter;
import com.iosharp.android.ssplayer.utils.StreamUrl;
import com.iosharp.android.ssplayer.utils.Utils;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;

import static com.google.android.libraries.cast.companionlibrary.utils.Utils.mediaInfoToBundle;
import static com.iosharp.android.ssplayer.PlayerApplication.TrackerName;
import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;

public class ChannelListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ChannelListFragment.class.getSimpleName();

    private static final int CURSOR_LOADER_ID = 0;

    private static final String[] CHANNEL_EVENT_COLUMNS = {
            ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID,
            ChannelEntry.TABLE_NAME + "." + ChannelEntry.COLUMN_NAME,
            ChannelEntry.TABLE_NAME + "." + ChannelEntry.COLUMN_ICON,
            EventEntry.TABLE_NAME + "." + EventEntry._ID,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NAME,
            "MIN(" + EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE +") AS " +
                    EventEntry.COLUMN_START_DATE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_END_DATE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_QUALITY,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_LANGUAGE,

    };

    // Indices tied to CHANNEL_COLUMNS
    public static final int COL_CHANNEL_ID = 0;
    public static final int COL_CHANNEL_NAME = 1;
    public static final int COL_CHANNEL_ICON = 2;
    public static final int COL_EVENT_ID = 3;
    public static final int COL_EVENT_NAME = 4;
    public static final int COL_EVENT_START_DATE = 5;
    public static final int COL_EVENT_END_DATE = 6;
    public static final int COL_EVENT_QUALITY = 7;
    public static final int COL_EVENT_LANGUAGE = 8;

    private ChannelAdapter mAdapter;
    private MiniController mMini;
    private VideoCastManager mCastManager;
    private int mChannelId;

    public ChannelListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static boolean getDebugMode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_debug_mode_key), false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    public void handleNavigation(Context context, MediaInfo info) {
        if (Utils.isInternetAvailable(context)) {

            Tracker t = ((PlayerApplication) getActivity().getApplication()).getTracker(TrackerName.APP_TRACKER);

            if (mCastManager != null && mCastManager.isConnected()) {
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.ga_events_category_playback))
                        .setAction(getString(R.string.ga_events_action_chromecast))
                        .build());
                GoogleAnalytics.getInstance(getActivity().getBaseContext()).dispatchLocalHits();

                mCastManager.startVideoCastControllerActivity(context, info, 0, true);

            } else {
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("media", mediaInfoToBundle(info));
                intent.putExtra("channel", mChannelId);

                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.ga_events_category_playback))
                        .setAction(getString(R.string.ga_events_action_local))
                        .build());

                GoogleAnalytics.getInstance(getActivity().getBaseContext()).dispatchLocalHits();
                context.startActivity(intent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_channel_list, container, false);

        // MiniController
        if (mCastManager != null) {
            mMini = (MiniController) rootView.findViewById(R.id.miniController_channel);
            mCastManager.addMiniController(mMini);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.channel_list_view);
        mAdapter = new ChannelAdapter(getActivity(), null);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                c.moveToPosition(position);

                // Get channel details
                mChannelId = c.getInt(COL_CHANNEL_ID);
                String channelName = c.getString(COL_CHANNEL_NAME);
                String channelIcon = c.getString(COL_CHANNEL_ICON);

                if (Utils.checkForSetServiceCredentials(getActivity())) {

                    // Create MediaInfo based off channel
                    String url;
                    if (mCastManager != null && mCastManager.isConnected()) {
                        // Don't bother respecting protocol choice as Chromecast only supports HTML5
                        url = StreamUrl.getUrl(getActivity(), mChannelId, StreamUrl.HTML5);
                    } else {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String protocol = sharedPreferences.getString(getActivity().getString(R.string.pref_protocol_key), "-1");

                        url = StreamUrl.getUrl(getActivity(), mChannelId, Integer.valueOf(protocol));
                    }
                    MediaInfo mediaInfo = Utils.buildMediaInfo(channelName, "SmoothStreams", url, channelIcon);

                    if (getDebugMode(getActivity())) {
                        // If debug mode is enabled, we do not want to launch a stream instead in a toast put the URL
                        Toast.makeText(getActivity(), "=====DEBUG MODE!=====\nURL: " + url + " copied to clipboard!"
                                , Toast.LENGTH_LONG).show();

                        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData streamUrl = ClipData.newPlainText("url", url);
                        clipboardManager.setPrimaryClip(streamUrl);

                    } else {
                        // Pass to handleNavigation
                        handleNavigation(getActivity(), mediaInfo);
                    }
                } else {
                    // Launch settings
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    getActivity().startActivity(intent);
                    Toast.makeText(getActivity(),
                            "ERROR: No login credentials found! Set your login and password first.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder =  ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID +
                ", " + EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE;

        return new CursorLoader(getActivity(), ChannelEntry.CONTENT_URI, CHANNEL_EVENT_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}


