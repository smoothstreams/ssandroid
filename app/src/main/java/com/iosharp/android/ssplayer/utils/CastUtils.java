package com.iosharp.android.ssplayer.utils;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Yan Yurkin
 * 22 April 2017
 */

public class CastUtils {

    private static final String TAG = CastUtils.class.getSimpleName();
    private static final String KEY_MEDIA_TYPE = "media-type";
    private static final String KEY_IMAGES = "images";
    private static final String KEY_URL = "movie-urls";
    private static final String KEY_CONTENT_TYPE = "content-type";
    private static final String KEY_STREAM_TYPE = "stream-type";
    private static final String KEY_CUSTOM_DATA = "custom-data";
    private static final String KEY_STREAM_DURATION = "stream-duration";
    private static final String KEY_TRACK_ID = "track-id";
    private static final String KEY_TRACK_CONTENT_ID = "track-custom-id";
    private static final String KEY_TRACK_NAME = "track-name";
    private static final String KEY_TRACK_TYPE = "track-type";
    private static final String KEY_TRACK_CONTENT_TYPE = "track-content-type";
    private static final String KEY_TRACK_SUBTYPE = "track-subtype";
    private static final String KEY_TRACK_LANGUAGE = "track-language";
    private static final String KEY_TRACK_CUSTOM_DATA = "track-custom-data";
    private static final String KEY_TRACKS_DATA = "track-data";


    public static Bundle mediaInfoToBundle(MediaInfo info) {
        if (info == null) {
            return null;
        }

        MediaMetadata md = info.getMetadata();
        Bundle wrapper = new Bundle();
        wrapper.putString(MediaMetadata.KEY_TITLE, md.getString(MediaMetadata.KEY_TITLE));
        wrapper.putString(MediaMetadata.KEY_SUBTITLE, md.getString(MediaMetadata.KEY_SUBTITLE));
        wrapper.putString(MediaMetadata.KEY_ALBUM_TITLE,
            md.getString(MediaMetadata.KEY_ALBUM_TITLE));
        wrapper.putString(MediaMetadata.KEY_ALBUM_ARTIST,
            md.getString(MediaMetadata.KEY_ALBUM_ARTIST));
        wrapper.putString(MediaMetadata.KEY_COMPOSER, md.getString(MediaMetadata.KEY_COMPOSER));
        wrapper.putString(MediaMetadata.KEY_SERIES_TITLE,
            md.getString(MediaMetadata.KEY_SERIES_TITLE));
        wrapper.putInt(MediaMetadata.KEY_SEASON_NUMBER,
            md.getInt(MediaMetadata.KEY_SEASON_NUMBER));
        wrapper.putInt(MediaMetadata.KEY_EPISODE_NUMBER,
            md.getInt(MediaMetadata.KEY_EPISODE_NUMBER));
        Calendar releaseCalendar = md.getDate(MediaMetadata.KEY_RELEASE_DATE);
        if (releaseCalendar != null) {
            long releaseMillis = releaseCalendar.getTimeInMillis();
            wrapper.putLong(MediaMetadata.KEY_RELEASE_DATE, releaseMillis);
        }
        wrapper.putInt(KEY_MEDIA_TYPE, info.getMetadata().getMediaType());
        wrapper.putString(KEY_URL, info.getContentId());
        wrapper.putString(MediaMetadata.KEY_STUDIO, md.getString(MediaMetadata.KEY_STUDIO));
        wrapper.putString(KEY_CONTENT_TYPE, info.getContentType());
        wrapper.putInt(KEY_STREAM_TYPE, info.getStreamType());
        wrapper.putLong(KEY_STREAM_DURATION, info.getStreamDuration());
        if (!md.getImages().isEmpty()) {
            ArrayList<String> urls = new ArrayList<>();
            for (WebImage img : md.getImages()) {
                urls.add(img.getUrl().toString());
            }
            wrapper.putStringArrayList(KEY_IMAGES, urls);
        }
        JSONObject customData = info.getCustomData();
        if (customData != null) {
            wrapper.putString(KEY_CUSTOM_DATA, customData.toString());
        }
        if (info.getMediaTracks() != null && !info.getMediaTracks().isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (MediaTrack mt : info.getMediaTracks()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(KEY_TRACK_NAME, mt.getName());
                    jsonObject.put(KEY_TRACK_CONTENT_ID, mt.getContentId());
                    jsonObject.put(KEY_TRACK_ID, mt.getId());
                    jsonObject.put(KEY_TRACK_LANGUAGE, mt.getLanguage());
                    jsonObject.put(KEY_TRACK_TYPE, mt.getType());
                    jsonObject.put(KEY_TRACK_CONTENT_TYPE, mt.getContentType());
                    if (mt.getSubtype() != MediaTrack.SUBTYPE_UNKNOWN) {
                        jsonObject.put(KEY_TRACK_SUBTYPE, mt.getSubtype());
                    }
                    if (mt.getCustomData() != null) {
                        jsonObject.put(KEY_TRACK_CUSTOM_DATA, mt.getCustomData().toString());
                    }
                    jsonArray.put(jsonObject);
                }
                wrapper.putString(KEY_TRACKS_DATA, jsonArray.toString());
            } catch (JSONException e) {
                Log.e(TAG, "mediaInfoToBundle(): Failed to convert Tracks data to json", e);
            }
        }

        return wrapper;
    }

    public static MediaInfo bundleToMediaInfo(Bundle wrapper) {
        if (wrapper == null) {
            return null;
        }

        MediaMetadata metaData = new MediaMetadata(wrapper.getInt(KEY_MEDIA_TYPE));

        metaData.putString(MediaMetadata.KEY_SUBTITLE,
            wrapper.getString(MediaMetadata.KEY_SUBTITLE));
        metaData.putString(MediaMetadata.KEY_TITLE, wrapper.getString(MediaMetadata.KEY_TITLE));
        metaData.putString(MediaMetadata.KEY_STUDIO, wrapper.getString(MediaMetadata.KEY_STUDIO));
        metaData.putString(MediaMetadata.KEY_ALBUM_ARTIST,
            wrapper.getString(MediaMetadata.KEY_ALBUM_ARTIST));
        metaData.putString(MediaMetadata.KEY_ALBUM_TITLE,
            wrapper.getString(MediaMetadata.KEY_ALBUM_TITLE));
        metaData.putString(MediaMetadata.KEY_COMPOSER,
            wrapper.getString(MediaMetadata.KEY_COMPOSER));
        metaData.putString(MediaMetadata.KEY_SERIES_TITLE,
            wrapper.getString(MediaMetadata.KEY_SERIES_TITLE));
        metaData.putInt(MediaMetadata.KEY_SEASON_NUMBER,
            wrapper.getInt(MediaMetadata.KEY_SEASON_NUMBER));
        metaData.putInt(MediaMetadata.KEY_EPISODE_NUMBER,
            wrapper.getInt(MediaMetadata.KEY_EPISODE_NUMBER));

        long releaseDateMillis = wrapper.getLong(MediaMetadata.KEY_RELEASE_DATE, 0);
        if (releaseDateMillis > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(releaseDateMillis);
            metaData.putDate(MediaMetadata.KEY_RELEASE_DATE, calendar);
        }
        ArrayList<String> images = wrapper.getStringArrayList(KEY_IMAGES);
        if (images != null && !images.isEmpty()) {
            for (String url : images) {
                Uri uri = Uri.parse(url);
                metaData.addImage(new WebImage(uri));
            }
        }
        String customDataStr = wrapper.getString(KEY_CUSTOM_DATA);
        JSONObject customData = null;
        if (!TextUtils.isEmpty(customDataStr)) {
            try {
                customData = new JSONObject(customDataStr);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to deserialize the custom data string: custom data= "
                    + customDataStr);
            }
        }
        List<MediaTrack> mediaTracks = null;
        if (wrapper.getString(KEY_TRACKS_DATA) != null) {
            try {
                JSONArray jsonArray = new JSONArray(wrapper.getString(KEY_TRACKS_DATA));
                mediaTracks = new ArrayList<>();
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        MediaTrack.Builder builder = new MediaTrack.Builder(
                            jsonObj.getLong(KEY_TRACK_ID), jsonObj.getInt(KEY_TRACK_TYPE));
                        if (jsonObj.has(KEY_TRACK_NAME)) {
                            builder.setName(jsonObj.getString(KEY_TRACK_NAME));
                        }
                        if (jsonObj.has(KEY_TRACK_SUBTYPE)) {
                            builder.setSubtype(jsonObj.getInt(KEY_TRACK_SUBTYPE));
                        }
                        if (jsonObj.has(KEY_TRACK_CONTENT_ID)) {
                            builder.setContentId(jsonObj.getString(KEY_TRACK_CONTENT_ID));
                        }
                        if (jsonObj.has(KEY_TRACK_CONTENT_TYPE)) {
                            builder.setContentType(jsonObj.getString(KEY_TRACK_CONTENT_TYPE));
                        }
                        if (jsonObj.has(KEY_TRACK_LANGUAGE)) {
                            builder.setLanguage(jsonObj.getString(KEY_TRACK_LANGUAGE));
                        }
                        if (jsonObj.has(KEY_TRACKS_DATA)) {
                            builder.setCustomData(
                                new JSONObject(jsonObj.getString(KEY_TRACKS_DATA)));
                        }
                        mediaTracks.add(builder.build());
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to build media tracks from the wrapper bundle", e);
            }
        }
        MediaInfo.Builder mediaBuilder = new MediaInfo.Builder(wrapper.getString(KEY_URL))
            .setStreamType(wrapper.getInt(KEY_STREAM_TYPE))
            .setContentType(wrapper.getString(KEY_CONTENT_TYPE))
            .setMetadata(metaData)
            .setCustomData(customData)
            .setMediaTracks(mediaTracks);

        if (wrapper.containsKey(KEY_STREAM_DURATION)
            && wrapper.getLong(KEY_STREAM_DURATION) >= 0) {
            mediaBuilder.setStreamDuration(wrapper.getLong(KEY_STREAM_DURATION));
        }

        return mediaBuilder.build();
    }

}
