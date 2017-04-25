package com.iosharp.android.ssplayer.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Channel;
import com.iosharp.android.ssplayer.data.Service;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.events.LoginEvent;
import com.iosharp.android.ssplayer.tasks.FetchLoginInfoTask;
import com.iosharp.android.ssplayer.tasks.OnTaskCompleteListener;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static String formatLongToString(long date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date(date));
    }

    public static String twoDigitsString(int number) {
        return String.format(Locale.US, "%02d", number);
    }

    public static abstract class OnRevalidateTaskCompleteListener implements OnTaskCompleteListener<String> {
        public static class Silent extends OnRevalidateTaskCompleteListener {
            @Override
            public void success(String result) {
                //do nothing
            }
        }
        @Override
        public void error(String error) {
            EventBus.getDefault().post(new LoginEvent(error)); //show login form
        }
    }

    public static void revalidateCredentials(Context c, OnRevalidateTaskCompleteListener listener) {
        if (!Service.hasActive() || !User.hasActive()) {
            forceLogin();
        } else {
            new FetchLoginInfoTask(c, listener).execute();
        }
    }

    public static void forceLogin() {
        EventBus.getDefault().post(new LoginEvent(LoginEvent.Type.Failed)); //show login form
    }

    public static MediaInfo buildMediaInfo(Context context, Channel channel) {
        String channelName = channel.getName();
        String channelIcon = channel.getImg();
        // Create MediaInfo based off channel
        String url = StreamUrl.getUrl(context, channel.getChannelId());
        return Utils.buildMediaInfo(channelName, "SmoothStreams", url, channelIcon);
    }

    private static MediaInfo buildMediaInfo(String channel, String studio, String url, String iconUrl) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, channel);
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        mediaMetadata.addImage(new WebImage(Uri.parse(iconUrl)));
        mediaMetadata.addImage(new WebImage(Uri.parse(Constants.SMOOTHSTREAMS_LOGO)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(Constants.CONTENT_TYPE)
                .setMetadata(mediaMetadata)
                .build();
    }

    public static SpannableString getHighDefBadge() {
        String highDefStr = " HD";
        final StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);
        SpannableString highDefSpannableString = new SpannableString(highDefStr);

        // Starting at one to not bold the space
        highDefSpannableString.setSpan(boldStyleSpan, 1, highDefStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return highDefSpannableString;
    }

    public static SpannableString getLanguageImg(Context context, String language) {
        int flag = R.drawable.gb;
        String lng = language.toLowerCase();

        if (lng.equals("eng")) {
            flag = R.drawable.gb;
        } else if (lng.equals("nl")) {
            flag = R.drawable.nl;
        } else if (lng.equals("es")) {
            flag = R.drawable.es;
        } else if (lng.equals("fr")) {
            flag = R.drawable.fr;
        } else if (lng.equals("de")) {
            flag = R.drawable.de;
        }

        String languageStr = " f";
        ImageSpan imageSpan = new ImageSpan(context, flag, DynamicDrawableSpan.ALIGN_BASELINE);
        SpannableString languageSpannableString = new SpannableString(languageStr);

        // Starting at one to not italic the space
        languageSpannableString.setSpan(imageSpan, 1, languageStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return languageSpannableString;
    }

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (info == null) {
            String message = context.getString(R.string.no_internet_connection_detected);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.e(TAG, message);
            return false;
        }
        return true;
    }
}
