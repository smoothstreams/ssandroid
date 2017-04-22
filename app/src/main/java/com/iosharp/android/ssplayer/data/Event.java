package com.iosharp.android.ssplayer.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Created by Yan Yurkin
 * 19 April 2017
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Event {

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
}
