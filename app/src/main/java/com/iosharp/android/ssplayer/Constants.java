package com.iosharp.android.ssplayer;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

/**
 * Created by Yan Yurkin
 * 01 April 2017
 */

public interface Constants {
    String YEAR_TIME_FORMAT = "EEE MMM dd yyyy HH:mm";
    String AUTH_SS_URL = "http://auth.smoothstreams.tv/hash_api.php";
    String AUTH_MMA_URL = "http://www.MMA-TV.net/loginForm.php";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat CONDENSED_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    String SMOOTHSTREAMS_ICON_PREFIX = "http://smoothstreams.tv/schedule/includes/images/uploads/";
    String SMOOTHSTREAMS_LOGO = "https://pbs.twimg.com/profile_images/378800000147953484/7af5bfc30ff182f852da32be5af79dfd.jpeg";
    String CONTENT_TYPE = "application/x-mpegurl";
    String DEFAULT_SERVER = "dEU.smoothstreams.tv";
    String BASE_URI = "https://speed.guide.smoothstreams.tv/";
    String FEED_JSON = "feed.json";
    String SMOOTHSTREAMS_SCHEDULE_FEED = BASE_URI + FEED_JSON;
    String WMSAUTH_PARAM = "wmsAuthSign";
}
