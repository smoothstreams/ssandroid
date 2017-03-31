package com.iosharp.android.ssplayer.activity;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.db.SearchSuggestionsProvider;
import com.iosharp.android.ssplayer.fragment.NoticeDialogFragment;
import com.iosharp.android.ssplayer.tasks.FetchLoginInfoTask;
import com.iosharp.android.ssplayer.utils.Utils;




public class SettingsActivity extends ActionBarActivity {

    private static final int INDEX_SERVERS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);
        setupActionBar();

        getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();

        Tracker t = ((PlayerApplication) getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
        t.setScreenName(getString(R.string.ga_screen_settings));
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_settings));
        setSupportActionBar(toolbar);
    }

    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences,
                    false);

            addPreferencesFromResource(R.xml.preferences);

            Preference clearHistory = (Preference) findPreference(getString(R.string.pref_clear_search_history_key));
            clearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    System.out.println("Changed key");
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                            SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
                    suggestions.clearHistory();
                    return true;
                }
            });

            // Show the current value in the settings screen
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initSummary(getPreferenceScreen().getPreference(i));
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            updatePreferenceSummary(findPreference(key));

            // Check only if one of the three below keys is changed to call method
            // to get service id and password
            if (key.equals(getString(R.string.pref_service_key))
                    || key.equals(getString(R.string.pref_service_username_key))
                    || key.equals(getString(R.string.pref_service_password_key))) {

                if (hasSetServiceDetails() && Utils.isInternetAvailable(getActivity())) {

                    FetchLoginInfoTask fetchLoginInfoTask = new FetchLoginInfoTask(getActivity());
                    fetchLoginInfoTask.execute();
                }
            }
            //StreamTVnow only works on certain servers.
            if (key.equals(getString(R.string.pref_service_key))) {

                ListPreference servers = (ListPreference) findPreference(getString(R.string.pref_server_key));
                servers.setEntries(R.array.list_servers);
                servers.setEntryValues(R.array.list_servers_values);

            }

            if (key.equals(getString(R.string.pref_protocol_key))) {
                // check checkbox key to see if we should bother showing dialog
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean showNotice = sharedPreferences.getBoolean(getString(R.string.pref_protocol_notice_checkbox_key), true);

                if (showNotice) {
                    FragmentManager fm = getFragmentManager();
                    NoticeDialogFragment noticeDialogFragment = new NoticeDialogFragment();
                    noticeDialogFragment.show(fm, NoticeDialogFragment.class.getSimpleName());
                }
            }
        }

        private void initSummary(Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initSummary(cat.getPreference(i));
                }
            } else {
                updatePreferenceSummary(p);
            }
        }

        private void updatePreferenceSummary(Preference p) {
            if (p instanceof ListPreference) {
                if (!p.getTitle().toString().equals(getString(R.string.preferences_clear_search_history))) {
                    // We don't want some summaries to be set as some are already defined in XML
                    ListPreference listPref = (ListPreference) p;
                    p.setSummary(listPref.getEntry());
                }
            }
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                // To show the password as masked instead of it being visible
                if (p.getTitle().toString().contains("assword")) {
                    // Don't mask what isn't there
                    if (editTextPref.getText() == null) {
                        p.setSummary("");
                    } else {
                        p.setSummary(editTextPref.getText().replaceAll(".", "*"));
                    }

                } else {
                    // If it isn't a password field just show the text
                    p.setSummary(editTextPref.getText());
                }
            }
            if (p instanceof MultiSelectListPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                p.setSummary(editTextPref.getText());
            }
        }

        /**
         * Checks to see if all needed fields for retrieving service id and username
         * are filled out.
         *
         * @return boolean
         */
        private boolean hasSetServiceDetails() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());

            String username = prefs.getString(getString(R.string.pref_service_username_key), null);
            String password = prefs.getString(getString(R.string.pref_service_password_key), null);
            String service = prefs.getString(getString(R.string.pref_service_key), null);

            if (username == null || password == null || service == null) {
                return false;
            } else {
                return true;
            }
        }

    }






}

