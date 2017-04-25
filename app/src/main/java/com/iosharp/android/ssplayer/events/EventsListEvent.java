package com.iosharp.android.ssplayer.events;

import android.util.LongSparseArray;

import com.iosharp.android.ssplayer.data.Event;

import java.util.List;

/**
 * Created by Yan Yurkin
 * 24 April 2017
 */

public class EventsListEvent {
    private LongSparseArray<List<Event>> events;

    public EventsListEvent(LongSparseArray<List<Event>> events) {
        this.events = events;
    }

    public LongSparseArray<List<Event>> getEvents() {
        return events;
    }
}
