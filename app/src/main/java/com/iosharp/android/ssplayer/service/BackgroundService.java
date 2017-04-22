package com.iosharp.android.ssplayer.service;

import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.data.Channel;
import com.iosharp.android.ssplayer.data.Schedule;
import com.iosharp.android.ssplayer.events.ChannelsListEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.orm.OrmHelper;
import ru.johnlife.lifetools.service.BaseBackgroundService;

/**
 * Created by Yan Yurkin
 * 22 April 2017
 */

public class BackgroundService extends BaseBackgroundService {
    private static BackgroundService instance = null;
    public static BackgroundService getInstance() {
        return instance;
    }


    private OrmHelper<Long> db;
    private Timer timer = new Timer();
    private Rest restService;
    private List<Channel> channels;
    private TimerTask channelPersister = null;
    private class ChannelPersister extends TimerTask {
        @Override
        public void run() {
            for (Channel channel : channels) {
                db.persist(channel);
            }
            channelPersister = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = new OrmHelper<>(this, Constants.dataClasses);
        initializeRestService();
        channels = db.getAll(Channel.class);
        postChannels();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshSchedule();
            }
        }, 0, Constants.UPDATE_DELAY);
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    private void initializeRestService() {
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
            OkHttpClient client = new OkHttpClient.Builder()
                .followSslRedirects(true)
                .followRedirects(true)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT).build();
                        return chain.proceed(request);
                    }
                })
                .build();
            restService = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URI)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(Rest.class);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(getClass().getSimpleName(), "Play Services Error", e);
        }
    }

    public void refreshSchedule() {
        restService.getSchedule().enqueue(new Callback<Schedule>() {
            @Override
            public void onResponse(Call<Schedule> call, Response<Schedule> response) {
                if (response.isSuccessful()) {
                    Schedule schedule = response.body();
                    //TODO: merge
                    channels = schedule.getChannels();
                    postChannels();
                    if (channelPersister != null) channelPersister.cancel();
                    channelPersister = new ChannelPersister();
                    timer.schedule(channelPersister, 200);
                    Log.i("REST", "Got response with " + schedule);
                }
            }

            @Override
            public void onFailure(Call<Schedule> call, Throwable t) {
                Log.e("REST", "Error: " + t);
            }
        });
    }

    private void postChannels() {
        EventBus.getDefault().post(new ChannelsListEvent(channels));
    }


}
