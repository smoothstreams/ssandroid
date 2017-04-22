package com.iosharp.android.ssplayer.cast;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.activity.ExpandedControlsActivity;

import java.util.List;

/**
 * Created by Yan Yurkin
 * 22 April 2017
 */
public final class CastOptionsProvider implements OptionsProvider {
    private static String sApplicationId;

    @Override
    public CastOptions getCastOptions(Context context) {
        if (sApplicationId == null) {
            sApplicationId = context.getString(R.string.chromecast_app_id);
        }
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
            .setTargetActivityClassName(ExpandedControlsActivity.class.getName())
            .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName())
            .build();
        return new CastOptions.Builder()
            .setReceiverApplicationId(sApplicationId)
            .setEnableReconnectionService(true)
            .setCastMediaOptions(mediaOptions)
            .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
