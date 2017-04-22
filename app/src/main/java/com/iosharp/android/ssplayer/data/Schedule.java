package com.iosharp.android.ssplayer.data;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yan Yurkin
 * 19 April 2017
 */

public class Schedule {
    private List<Channel> channels = new ArrayList<>();

    @JsonAnySetter
    public void set(String name, Channel value) {
        channels.add(value);
    }

    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public String toString() {
        return channels.size()+" channels";
    }
}
