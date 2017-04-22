package com.iosharp.android.ssplayer.service;

import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.data.Schedule;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Yan Yurkin
 * 19 April 2017
 */

public interface Rest {
    @GET(Constants.FEED_JSON)
    Call<Schedule> getSchedule();
}
