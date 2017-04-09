package com.iosharp.android.ssplayer.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
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

    private MediaPlayer mPlayer;
    private String mURL;
    private VideoCastManager mCastManager;
    private MediaInfo mSelectedMedia;
    private Tracker mTracker;
    private VideoCastConsumerImpl mCastConsumer;
    private View progress;

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mPlayer.setDisplay(holder);
            mPlayer.prepareAsync();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}
    };

    private final MediaPlayer.OnPreparedListener mediaPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            getSupportActionBar().hide();
            mPlayer.start();
            progress.setVisibility(View.GONE);
        }
    };

    private final MediaPlayer.OnErrorListener mediaErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //TODO: handle errors properly
            Crashlytics.log(Log.ERROR, "MediaPlayer", String.format("Error(%s, %s)", what, extra));
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                mPlayer.reset();
                //TODO: autoretry
            } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                Toast.makeText(getApplicationContext(),
                    "Unknown media error. Try a different protocol/quality or open in an external player.",
                    Toast.LENGTH_SHORT).show();
                mPlayer.reset();
            }
            //TODO: WHAT>?????
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnPreparedListener(mediaPreparedListener);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = PlayerApplication.getCastManager();
        setContentView(R.layout.activity_video);
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
            SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.videoSurface);
            mSurfaceView.getHolder().addCallback(surfaceCallback);
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(mediaPreparedListener);
        }
    }

    private void googleAnalytics() {
        mTracker = ((PlayerApplication) getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
        mTracker.setScreenName(getString(R.string.ga_screen_video_player));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        GoogleAnalytics.getInstance(this.getBaseContext()).dispatchLocalHits();
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(mURL);

            mPlayer.setOnErrorListener(mediaErrorListener);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        if (mCastManager != null) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "onTouchEvent");
        showActionBar();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mPlayer != null) {
            mPlayer.reset();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.setOnPreparedListener(null);
        mPlayer.reset();

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

    private Intent createStreamIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setDataAndType(Uri.parse(mSelectedMedia.getContentId()), "application/x-mpegURL");
        return shareIntent;
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
            mShareActionProvider.setShareIntent(createStreamIntent());
        }
        return true;
    }
}
