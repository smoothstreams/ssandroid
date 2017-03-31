package com.iosharp.android.ssplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.tasks.FetchLoginInfoTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static String formatLongToString(long date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date(date));
    }

    public static Long convertDateToLong(String dateString) {
        SimpleDateFormat dateFormat;
        // For the start/end datetime
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
            // If we adjust justDate for DST, we could be an hour behind and the date is not correct.
            if (isDst()) {
                return adjustForDst(convertedDate);
            }
            return convertedDate.getTime();
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    public static boolean checkForSetServiceCredentials(Context c) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        Long validTime = sharedPreferences.getLong(c.getString(R.string.pref_ss_valid_key),0);
        String password = sharedPreferences.getString(c.getString(R.string.pref_ss_password_key), null);

        if (password != null) {
            long curTime = System.currentTimeMillis();
            if(curTime>=validTime){
                FetchLoginInfoTask loginTask = new FetchLoginInfoTask(c, true);
                try {
                    loginTask.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                password = sharedPreferences.getString(c.getString(R.string.pref_ss_password_key), null);
                if(password==null)
                    return false;
            }
            return true;
        }
        return false;
    }

    public static MediaInfo buildMediaInfo(String channel, String studio, String url, String iconUrl) {
        final String SMOOTHSTREAMS_ICON_PREFIX = "http://smoothstreams.tv/schedule/includes/images/uploads/";
        final String SMOOTHSTREAMS_LOGO =
                "https://pbs.twimg.com/profile_images/378800000147953484/7af5bfc30ff182f852da32be5af79dfd.jpeg";
        final String CONTENT_TYPE = "application/x-mpegurl";

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, channel);
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        mediaMetadata.addImage(new WebImage(Uri.parse(SMOOTHSTREAMS_ICON_PREFIX + iconUrl)));
        mediaMetadata.addImage(new WebImage(Uri.parse(SMOOTHSTREAMS_LOGO)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(CONTENT_TYPE)
                .setMetadata(mediaMetadata)
                .build();
    }

    private static boolean isDst() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    private static Long adjustForDst(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime().getTime();
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

    public static String getCleanTitle(String title) {
        // For example, Mike &amp; Mike -> Mike & Mike
        String cleanTitle = title.replace("&amp;", "&");
        return cleanTitle;
    }

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (info == null) {
            Toast.makeText(context, context.getString(R.string.no_internet_connection_detected), Toast.LENGTH_LONG).show();
            Log.e(TAG, context.getString(R.string.no_internet_connection_detected));
            return false;
        }
        return true;
    }
}
