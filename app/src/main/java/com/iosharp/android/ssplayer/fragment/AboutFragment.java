package com.iosharp.android.ssplayer.fragment;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by Yan Yurkin
 * 04 May 2017
 */
public class AboutFragment extends BaseAbstractFragment {
    private int mDebugTapCount = 8;
    boolean mDebugMode = false;
    private Tracker mTracker;

    @Override
    protected String getTitle(Resources r) {
        return r.getString(R.string.action_about);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                        Toast.makeText(getActivity(), "Debug mode enabled!", Toast.LENGTH_LONG).show();

                        mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.ga_events_category_debug))
                            .setAction(getString(R.string.ga_events_action_debug_enable))
                            .build());
                    }
                }
            }
        });
        ((TextView) rootView.findViewById(R.id.about_app_version)).setText(Constants.VERSION);
        ((TextView) rootView.findViewById(R.id.about_body)).setText(getString(R.string.about_body));

        return rootView;
    }
}
