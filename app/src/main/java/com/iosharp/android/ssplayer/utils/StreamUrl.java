package com.iosharp.android.ssplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Service;
import com.iosharp.android.ssplayer.data.User;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class StreamUrl {

    public static String getUrl(Context context, int channel) {
        if (!Service.hasActive() || !User.hasActive() || !User.getCurrentUser().hasActiveHash()) {
            Utils.forceLogin();
            return "";
        }
        // String format because the URL needs 01, 02, 03, etc when we have single digit integers
        String channelId = Utils.twoDigitsString(channel);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String password = User.getCurrentUser().getHash();
        String server = sharedPreferences.getString(context.getString(R.string.pref_server_key), Constants.DEFAULT_SERVER);
        boolean quality = sharedPreferences.getBoolean(context.getString(R.string.pref_quality_key), false);

        String streamQuality = quality ? "1" : "2";

        Service serviceMapping = Service.getCurrent();
        String port = serviceMapping.getHtmlPort();
        String servicePath = serviceMapping.getView();

        Uri uri = Uri.parse(
            new StringBuilder()
                .append("http://")
                .append(server)
                .append(":")
                .append(port)
                .append("/")
                .append(servicePath)
                .append("/")
                .append(String.format("ch%sq%s.stream", channelId, streamQuality))
                .append("/playlist.m3u8").toString()
            ).buildUpon()
            .appendQueryParameter(Constants.WMSAUTH_PARAM, password)
            .build();

        String url = null;
        // In case password has special characters
        try {
            url = URLDecoder.decode(uri.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.w("StreamUrl", e.getLocalizedMessage(), e);
        }
        return url;
    }


}
