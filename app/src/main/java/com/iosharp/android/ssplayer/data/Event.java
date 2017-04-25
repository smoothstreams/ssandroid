package com.iosharp.android.ssplayer.data;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ru.johnlife.lifetools.data.JsonData;

/**
 * Created by Yan Yurkin
 * 19 April 2017
 */
public class Event extends JsonData implements Comparable<Event>{
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("EST"));
    }

    private int id;
    private String network;
    private String network_id;
    private String network_switched;
    private String name;
    private String description;
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

    @JsonIgnore
    private Channel channelBackReference;

    @JsonProperty("time")
    /*auto*/ void setTime(String time) {
        beginTimeStamp = convertDateToLong(time);
    }

    @JsonProperty("end_time")
    /*auto*/ void setEndTime(String time) {
        endTimeStamp = convertDateToLong(time);
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name.replace("&amp;", "&");
    }

    public long getBeginTimeStamp() {
        return beginTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    private static long convertDateToLong(String dateString) {
        Date convertedDate;
        try {
            convertedDate = DATE_FORMAT.parse(dateString);
            // If we adjust justDate for DST, we could be an hour behind and the date is not correct.
            if (isDst()) {
                return adjustForDst(convertedDate);
            }
            return convertedDate.getTime();
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }
        return -1;
    }

    private static boolean isDst() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    private static long adjustForDst(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime().getTime();
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

    public int getId() {
        return id;
    }

    public Channel getChannelBackReference() {
        return channelBackReference;
    }

    public void setChannelBackReference(Channel channelBackReference) {
        this.channelBackReference = channelBackReference;
    }

    @Override
    public int compareTo(@NonNull Event o) {
        return o.getBeginTimeStamp() > getBeginTimeStamp() ? -1:
            o.getBeginTimeStamp() < getBeginTimeStamp() ? 1 : 0;
    }
}
