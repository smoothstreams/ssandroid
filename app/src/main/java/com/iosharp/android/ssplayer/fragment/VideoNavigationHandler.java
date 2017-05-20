package com.iosharp.android.ssplayer.fragment;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.activity.LoginActivity;
import com.iosharp.android.ssplayer.data.Channel;
import com.iosharp.android.ssplayer.data.Service;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.utils.Utils;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;

import static com.iosharp.android.ssplayer.utils.CastUtils.mediaInfoToBundle;

/**
 * Created by Yan Yurkin
 * 24 April 2017
 */
public class VideoNavigationHandler {

    public static void handleNavigation(final Channel channel, final Activity activity) {
        if (Service.hasActive() && User.hasActive()) {
            if (!User.getCurrentUser().hasActiveHash()) {
                Utils.revalidateCredentials(activity, new Utils.OnRevalidateTaskCompleteListener() {
                    @Override
                    public void success(String result) {
                        handleNavigation(channel, activity);
                    }
                });
            } else {
                MediaInfo info = Utils.buildMediaInfo(activity, channel);
                if (Utils.isInternetAvailable(activity)) {
                    Tracker t = ((PlayerApplication) activity.getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
                    CastContext castManager = PlayerApplication.getCastManager();
                    if (castManager != null && castManager.getCastState() == CastState.CONNECTED && castManager.getSessionManager().getCurrentCastSession() != null) {
                        t.send(new HitBuilders.EventBuilder()
                            .setCategory(activity.getString(R.string.ga_events_category_playback))
                            .setAction(activity.getString(R.string.ga_events_action_chromecast))
                            .build());
                        GoogleAnalytics.getInstance(activity.getBaseContext()).dispatchLocalHits();
                        castManager.getSessionManager().getCurrentCastSession().getRemoteMediaClient().load(info);
//                      castManager.startVideoCastControllerActivity(context, info, 0, true);
                    } else {
                        Intent intent = new Intent(activity, VideoActivity.class);
                        intent.putExtra(Constants.EXTRA_MEDIA, mediaInfoToBundle(info));
                        intent.putExtra(Constants.EXTRA_CHANNEL, channel.getChannelId());
                        t.send(new HitBuilders.EventBuilder()
                            .setCategory(activity.getString(R.string.ga_events_category_playback))
                            .setAction(activity.getString(R.string.ga_events_action_local))
                            .build());
                        GoogleAnalytics.getInstance(activity.getBaseContext()).dispatchLocalHits();
                        activity.startActivity(intent);
                    }
                }
            }
        } else {
            activity.startActivity(new Intent(activity, LoginActivity.class));
        }
    }
}
