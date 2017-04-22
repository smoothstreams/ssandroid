package com.iosharp.android.ssplayer.data;

import com.j256.ormlite.field.DatabaseField;

import java.util.List;

import ru.johnlife.lifetools.data.JsonOrmData;

public class Channel extends JsonOrmData{
    @DatabaseField(id = true)
    private int channel_id;
    private String name;
    private String img;

    private List<Event> items;

    public int getChannelId() {
        return channel_id;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public List<Event> getEvents() {
        return items;
    }

    @Override
    public String toString() {
        return name + ": "+(items == null ? "no events" : items.size()+" events");
    }
}
