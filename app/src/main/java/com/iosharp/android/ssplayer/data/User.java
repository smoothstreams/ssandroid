package com.iosharp.android.ssplayer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.utils.Utils;

/**
 * Created by Yan Yurkin
 * 04 April 2017
 */

public class User {
    private static User currentUser = null;

    static {
        Context c = PlayerApplication.getAppContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String username = prefs.getString(c.getString(R.string.pref_service_username_key), null);
        String password = prefs.getString(c.getString(R.string.pref_service_password_key), null);
        if ((null != username) && (null != password)) {
            currentUser = new User(username, password);
            currentUser.valid = prefs.getLong(c.getString(R.string.pref_ss_valid_key), -1);
            currentUser.hash = prefs.getString(c.getString(R.string.pref_ss_password_key), null);
            if (!currentUser.hasActiveHash()) {
                Utils.revalidateCredentials(c, new Utils.OnRevalidateTaskCompleteListener.Silent());
            }
        }
    }

    public static boolean hasActive() {
        return null != currentUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static User newUser(String username, String password) {
        currentUser = new User(username, password);
        Context c = PlayerApplication.getAppContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit()
            .putString(c.getString(R.string.pref_service_username_key), username)
            .putString(c.getString(R.string.pref_service_password_key), password)
            .apply();
        return currentUser;
    }


    private String username;
    private String password;
    private String hash;
    private long valid;


    private User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean hasActiveHash() {
        return null != hash && System.currentTimeMillis() < valid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHash() {
        return hash;
    }

    public synchronized void updateHash(long endTime, String hash) {
        Context context = PlayerApplication.getAppContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
            .putLong(context.getString(R.string.pref_ss_valid_key), endTime)
            .putString(context.getString(R.string.pref_ss_password_key), hash)
            .apply();
        this.hash = hash;
        this.valid = endTime;
        Log.i(getClass().getSimpleName(), "SUCCESS: Valid Until: " + endTime);
    }
}
