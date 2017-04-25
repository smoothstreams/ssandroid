package com.iosharp.android.ssplayer;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.iosharp.android.ssplayer.data.Channel;
import com.iosharp.android.ssplayer.service.BackgroundService;

import java.util.Arrays;
import java.util.List;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.service.BaseBackgroundService;

/**
 * Created by Yan Yurkin
 * 01 April 2017
 */

public interface Constants extends ru.johnlife.lifetools.Constants {
    @SuppressWarnings({"unchecked", "RedundantArrayCreation"})
    List<Class<? extends AbstractData>> dataClasses = Arrays.<Class<? extends AbstractData>>asList(new Class[]{
        Channel.class
    });
    ClassConstantsProvider CLASS_CONSTANTS = new ClassConstantsProvider() {
        @Override
        public Class<? extends Activity> getLoginActivityClass() {
            return null;
        }

        @Override
        public Class<? extends BaseBackgroundService> getBackgroundServiceClass() {
            return BackgroundService.class;
        }
    };

    String YEAR_TIME_FORMAT = "EEE MMM dd yyyy HH:mm";
    String AUTH_SS_URL = "https://auth.smoothstreams.tv/hash_api.php";
    String AUTH_MMA_URL = "http://www.MMA-TV.net/loginForm.php";
    @SuppressLint("SimpleDateFormat")
    String SMOOTHSTREAMS_LOGO = "https://pbs.twimg.com/profile_images/378800000147953484/7af5bfc30ff182f852da32be5af79dfd.jpeg";
    String CONTENT_TYPE = "application/x-mpegurl";
    String DEFAULT_SERVER = "dEU.smoothstreams.tv";
    String BASE_URI = "https://speed.guide.smoothstreams.tv/";
    String FEED_JSON = "feed.json";
    String WMSAUTH_PARAM = "wmsAuthSign";
    String USER_AGENT = "SmoothStreamsPlayer v"+BuildConfig.VERSION_NAME;

    long UPDATE_DELAY = 15*MINUTE;
}
