package com.iosharp.android.ssplayer.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;

import java.io.IOException;


public class VideoActivity extends AppCompatActivity  {
    private static final String TAG = VideoActivity.class.getSimpleName();
    @SuppressLint("InlinedApi")
    private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
    private static final int sDefaultTimeout = 3000;

    private String mURL;
    private VideoCastManager mCastManager;
    private MediaInfo mSelectedMedia;
    private Tracker mTracker;
    private VideoCastConsumerImpl mCastConsumer;
    private View progress;
    private SimpleExoPlayer player;
    private boolean userCancelled;

    private AdaptiveMediaSourceEventListener eventListener = new AdaptiveMediaSourceEventListener() {
        private static final String TAG = "ExoEventListener";

        @Override
        public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
            getSupportActionBar().hide();
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
            if (!userCancelled) {
                Log.w(TAG, "onLoadCanceled");
                Toast.makeText(getApplicationContext(),
                    "Unknown media error. Try a different protocol/quality or open in an external player.",
                    Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
            Log.w(TAG, "onLoadError: ", error);
        }

        @Override
        public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {}
        @Override
        public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {}
        @Override
        public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = PlayerApplication.getCastManager();
        setContentView(R.layout.activity_video);
        findViewById(R.id.videoSurfaceContainer).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(TAG, "onTouchEvent");
                showActionBar();
                return false;
            }
        });
        progress = findViewById(R.id.progress);
        hideSoftKeys();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupActionBar();
        setupCastListeners();
        goFullscreen();
        getSupportActionBar().show();
        googleAnalytics();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mSelectedMedia = Utils.bundleToMediaInfo(getIntent().getBundleExtra("media"));
            String title = mSelectedMedia.getMetadata().getString(MediaMetadata.KEY_TITLE);
            getSupportActionBar().setTitle(title);
            mURL = mSelectedMedia.getContentId();
        } else {
            Log.w(getClass().getSimpleName(), "You have to start this activity with parameters. Exiting.");
            finish();
        }
    }

    private void googleAnalytics() {
        mTracker = ((PlayerApplication) getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
        mTracker.setScreenName(getString(R.string.ga_screen_video_player));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        GoogleAnalytics.getInstance(this.getBaseContext()).dispatchLocalHits();
    }

    private void initPlayer() {
        userCancelled = false;
        progress.setVisibility(View.VISIBLE);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl());
        SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.videoSurface);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "yourApplicationName"));
        MediaSource videoSource = new HlsMediaSource(Uri.parse(mURL), dataSourceFactory, new Handler(Looper.getMainLooper()), eventListener);
        player.setPlayWhenReady(true);
        player.prepare(videoSource);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPlayer();
        if (mCastManager != null) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
    }

    @Override
    protected void onPause() {
        userCancelled = true;
        super.onPause();
        player.release();
        player = null;
        if (mCastManager != null) {
            mCastManager.removeVideoCastConsumer(mCastConsumer);
            mCastManager.decrementUiCounter();
        }
    }

    private void setupCastListeners() {
        if (mCastManager != null) {
            mCastConsumer = new VideoCastConsumerImpl() {
                @Override
                public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                    super.onApplicationConnected(appMetadata, sessionId, wasLaunched);
                    if (mSelectedMedia != null) {
                        try {
                            loadRemoteMedia(true);
                            finish();
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                        }
                    }
                }

            };
        }
    }

    private void loadRemoteMedia(boolean autoPlay) {
        mCastManager.startVideoCastControllerActivity(this, mSelectedMedia, 0, autoPlay);
    }


    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.video_toolbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.menu_video);

        setSupportActionBar(toolbar);
    }

    private void showActionBar() {
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().hide();
                }
            }, sDefaultTimeout);
        }
    }

    private void goFullscreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    private void hideSoftKeys() {
        final View v = getWindow().getDecorView();
        v.setSystemUiVisibility(UI_OPTIONS);
        v.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setSystemUiVisibility(UI_OPTIONS);
                    }
                }, sDefaultTimeout);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_video, menu);

        if (mCastManager != null) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.ga_events_category_playback))
                    .setAction(getString(R.string.ga_events_action_chromecast))
                    .build());

            GoogleAnalytics.getInstance(this.getBaseContext()).dispatchLocalHits();
            mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }

        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {

            mTracker.send(new HitBuilders.EventBuilder()
            .setCategory(getString(R.string.ga_events_category_playback))
            .setAction(getString(R.string.ga_events_action_external))
            .build());

            GoogleAnalytics.getInstance(this.getBaseContext()).dispatchLocalHits();
            Intent shareIntent = new Intent(Intent.ACTION_VIEW);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setDataAndType(Uri.parse(mSelectedMedia.getContentId()), "application/x-mpegURL");
            mShareActionProvider.setShareIntent(shareIntent);
        }
        return true;
    }
}
