package com.iosharp.android.ssplayer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.framework.CastContext;
import com.iosharp.android.ssplayer.activity.LoginActivity;
import com.iosharp.android.ssplayer.events.LoginEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import ru.johnlife.lifetools.reporter.LifetoolsExceptionReporter;

public class PlayerApplication extends Application {

    private static Context applicationContext;
    private static final String PROPERTY_ID = "UA-57141244-1";
    @SuppressLint("StaticFieldLeak")
    private static CastContext sCastMgr = null;

    private void initializeCastManager() {
        if (!(Build.MODEL.contains("AFT") || Build.MANUFACTURER.equals("Amazon"))) {
            try {
                sCastMgr = CastContext.getSharedInstance(this);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error initializing cast manager", e);
            }
        }
    }

    public static CastContext getCastManager() {
        return sCastMgr;
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(LifetoolsExceptionReporter.getInstance(this));
        EventBus.getDefault().register(this);
        applicationContext = getApplicationContext();
        initializeCastManager();
        startService(new Intent(this, Constants.CLASS_CONSTANTS.getBackgroundServiceClass()));
    }

    public static Context getAppContext() {
        return applicationContext;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginFailed(LoginEvent event) {
        Intent intent = new Intent(this, LoginActivity.class);
        String error = event.getError();
        if (null != error) {
            intent.putExtra(LoginActivity.EXTRA_ERROR, error);
        }
        startActivity(intent);
    }
}
