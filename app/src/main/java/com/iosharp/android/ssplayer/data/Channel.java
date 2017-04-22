package com.iosharp.android.ssplayer.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Channel {
    private int channel_id;
    private String name;
    private String img;

    private List<Event> items;

    @Override
    public String toString() {
        return name + ": "+(items == null ? "no events" : items.size()+" events");
    }
}
