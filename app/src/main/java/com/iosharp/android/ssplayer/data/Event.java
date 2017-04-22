package com.iosharp.android.ssplayer.data;

import com.iosharp.android.ssplayer.utils.Utils;

import ru.johnlife.lifetools.data.JsonData;

/**
 * Created by Yan Yurkin
 * 19 April 2017
 */
public class Event extends JsonData {

    private String id;
    private String network;
    private String network_id;
    private String network_switched;
    private String name;
    private String description;
    private String time;
    private String end_time;
    private String runtime;
    private String channel;
    private String pool;
    private String status;
    private String version;
    private String language;
    private String category;
    private String timered;
    private String auto;
    private String auto_assigned_cat;
    private String parent_id;
    private String quality;
    private long beginTimeStamp = -1;
    private long endTimeStamp = -1;

    public long getBeginTimeStamp() {
        if (-1 == beginTimeStamp) beginTimeStamp = Utils.convertDateToLong(time);
        return beginTimeStamp;
    }

    public long getEndTimeStamp() {
        if (-1 == endTimeStamp) endTimeStamp = Utils.convertDateToLong(end_time);
        return endTimeStamp;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getQuality() {
        return quality;
    }
}
