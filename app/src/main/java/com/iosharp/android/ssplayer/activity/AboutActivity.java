package com.iosharp.android.ssplayer.activity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setupActionBar();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AboutFragment())
                    .commit();
        }
    }

    public void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_about));
        setSupportActionBar(toolbar);
    }

    public static class AboutFragment extends Fragment {
        private static final String TAG = AboutFragment.class.getSimpleName();

        private int mDebugTapCount = 8;
        boolean mDebugMode = false;
        private Tracker mTracker;

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);

            mTracker = ((PlayerApplication) getActivity().getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
            mTracker.setScreenName(getString(R.string.ga_screen_about));
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            TextView appTitle = (TextView) rootView.findViewById(R.id.about_app_name);
            final Button debugButton = (Button) rootView.findViewById(R.id.disable_debug);

            // Check to see if debug mode is enabled and if so, show the enable button
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mDebugMode = sharedPreferences.getBoolean(getString(R.string.pref_debug_mode_key), false);

            if (mDebugMode) {
                debugButton.setVisibility(View.VISIBLE);
            }

            debugButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.pref_debug_mode_key), false);
                    editor.commit();

                    debugButton.setVisibility(View.GONE);
                    Crashlytics.log(Log.INFO, TAG, "Debug mode disabled.");
                    Toast.makeText(getActivity(), "Debug mode disabled!", Toast.LENGTH_LONG).show();
                }
            });


            appTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mDebugMode) {
                        if (mDebugTapCount > 1) {
                            mDebugTapCount--;
                            Toast.makeText(getActivity(), "Tap " + mDebugTapCount + " more times to enable debug mode.", Toast.LENGTH_SHORT).show();
                        } else {
                            mDebugMode = true;
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(getString(R.string.pref_debug_mode_key), true);
                            editor.commit();

                            debugButton.setVisibility(View.VISIBLE);
                            Crashlytics.log(Log.INFO ,TAG, "Debug mode enabled.");
                            Toast.makeText(getActivity(), "Debug mode enabled!", Toast.LENGTH_LONG).show();

                            mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.ga_events_category_debug))
                            .setAction(getString(R.string.ga_events_action_debug_enable))
                            .build());
                        }
                    }
                }
            });


            ((TextView) rootView.findViewById(R.id.about_app_version)).setText(getVersionInfo());
            ((TextView) rootView.findViewById(R.id.about_body)).setText(getString(R.string.about_body));

            return rootView;
        }

        public String getVersionInfo() {
            String strVersion = "v";

            PackageInfo packageInfo;
            try {
                packageInfo = getActivity().getPackageManager().getPackageInfo(
                        getActivity().getPackageName(), 0);
                strVersion += packageInfo.versionName;

            } catch (PackageManager.NameNotFoundException e) {
                Crashlytics.logException(e);
                strVersion += "Unknown";
            }

            return strVersion;
        }

    }


}
