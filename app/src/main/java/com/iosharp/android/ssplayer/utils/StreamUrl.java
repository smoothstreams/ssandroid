package com.iosharp.android.ssplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class StreamUrl {
    public static final int HTML5 = 0;
    public static final int RTMP = 1;
    public static final int RTSP = 2;



    public static String getUrl(Context context, int channel, int protocol) {
        // String format because the URL needs 01, 02, 03, etc when we have single digit integers
        String channelId = Utils.twoDigitsString(channel);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uid = sharedPreferences.getString(context.getString(R.string.pref_ss_uid_key), null);
        String password = sharedPreferences.getString(context.getString(R.string.pref_ss_password_key), null);
        String server = sharedPreferences.getString(context.getString(R.string.pref_server_key), null);
        String service = sharedPreferences.getString(context.getString(R.string.pref_service_key), null);
        boolean quality = sharedPreferences.getBoolean(context.getString(R.string.pref_quality_key), false);

        String streamQuality = quality ? "1" : "2";

        //TODO
        if (!Service.hasActive()) throw new IllegalStateException("Service is not selected");

        Service serviceMapping = Service.getCurrent();
        String port = (protocol == 0) ? serviceMapping.getHtmlPort() : serviceMapping.getRTPort();
        String servicePath = serviceMapping.getView();

        String SERVICE_URL_AND_PORT = server + ":" + port;
        String STREAM_CHANNEL_AND_QUALITY;
        String BASE_URL;
        String WMSAUTH_PARAM = "wmsAuthSign";
        STREAM_CHANNEL_AND_QUALITY = String.format("ch%sq%s.stream", channelId, streamQuality);
        switch (protocol) {
            case StreamUrl.HTML5:
                BASE_URL = "http://" + SERVICE_URL_AND_PORT + "/"+ servicePath +"/" + STREAM_CHANNEL_AND_QUALITY + "/playlist.m3u8";
                break;
            case StreamUrl.RTMP:
                BASE_URL = "rtmp://" + SERVICE_URL_AND_PORT + "/"+ servicePath + "?" + WMSAUTH_PARAM +  "="+ password + "/" + STREAM_CHANNEL_AND_QUALITY;
                return(BASE_URL);
            case StreamUrl.RTSP:
                BASE_URL = "rtsp://" + SERVICE_URL_AND_PORT + "/"+ servicePath +"/" + STREAM_CHANNEL_AND_QUALITY;
                break;
            default:
                throw new UnsupportedOperationException("Unknown protocol: " + protocol);
        }
        Uri uri = Uri.parse(BASE_URL).buildUpon()
            .appendQueryParameter(WMSAUTH_PARAM, password)
            .build();

        String url = null;
        // In case password has special characters
        try {
            url = URLDecoder.decode(uri.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }
        return url;
    }


}
