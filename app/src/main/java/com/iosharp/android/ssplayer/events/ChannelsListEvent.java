package com.iosharp.android.ssplayer.events;

import com.iosharp.android.ssplayer.data.Channel;

import java.util.List;

/**
 * Created by Yan Yurkin
 * 22 April 2017
 */

public class ChannelsListEvent {
    private List<Channel> channels;

    public ChannelsListEvent(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Channel> getChannels() {
        return channels;
    }
}
