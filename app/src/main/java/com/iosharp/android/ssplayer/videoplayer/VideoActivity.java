package com.iosharp.android.ssplayer.videoplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.FrameLayout;
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


public class VideoActivity extends ActionBarActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl, MediaPlayer.OnErrorListener {
    private static final String TAG = VideoActivity.class.getSimpleName();

    private static final int sDefaultTimeout = 3000;

    private SurfaceView mSurfaceView;
    private MediaPlayer mPlayer;
    private VideoControllerView mController;
    private String mURL;
    private SurfaceHolder mSurfaceHolder;
    private VideoCastManager mCastManager;
    private MediaInfo mSelectedMedia;
    private Tracker mTracker;
    private VideoCastConsumerImpl mCastConsumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = PlayerApplication.getCastManager();
        setContentView(R.layout.activity_video);
        hideSoftKeys();

        mSurfaceView = (SurfaceView) findViewById(R.id.videoSurface);
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

            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);
            mPlayer = new MediaPlayer();
            mController = new VideoControllerView(this, false);
            mPlayer.setOnPreparedListener(this);

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

            mPlayer.setOnErrorListener(this);
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
        mController.show();
        showActionBar();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Hide to prevent illegal state exception of getCurrentPosition
        mController.hide();
        if (mPlayer != null) {
            mPlayer.reset();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mController.hide();
        mPlayer.setOnPreparedListener(null);
        mPlayer.reset();

        if (mCastManager != null) {
            mCastManager.removeVideoCastConsumer(mCastConsumer);
            mCastManager.decrementUiCounter();
        }
    }

    /**
     * SurfaceHolder.Callback
     */


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mPlayer.setDisplay(mSurfaceHolder);
        mPlayer.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    // End SurfaceHolder.Callback

    /**
     * MediaPlayer.OnPreparedListener
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        getSupportActionBar().hide();
        mController.setMediaPlayer(this);
        mController.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        mPlayer.start();
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
    // End MediaPlayer.OnPreparedListener

    /**
     * MediaPlayer.OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Crashlytics.log(Log.ERROR, "MediaPlayer", String.format("Error(%s, %s)", what, extra));

        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            mPlayer.reset();
        } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Toast.makeText(getApplicationContext(),
                    "Unknown media error. Try a different protocol/quality or open in an external player.",
                    Toast.LENGTH_SHORT).show();
            mPlayer.reset();
        }
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);

        return true;
    }

    /**
     * VideoMediaController.MediaPlayerControl
     *
     * @return
     */
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    @Override
    public void seekTo(int i) {
        mPlayer.seekTo(i);
    }

    @Override
    public void start() {
        mPlayer.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    // End VideoMediaController.MediaPlayerControl

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
        if (getSupportActionBar().isShowing() == false) {
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        v.setSystemUiVisibility(uiOptions);
        v.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setSystemUiVisibility(uiOptions);
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
