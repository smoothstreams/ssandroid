package com.iosharp.android.ssplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applidium.headerlistview.HeaderListView;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.adapter.EventAdapter;
import com.iosharp.android.ssplayer.data.Event;
import com.iosharp.android.ssplayer.events.EventsListEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class EventListFragment extends Fragment {
    private EventAdapter adapter;

    public EventListFragment() {}

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);
        HeaderListView list = (HeaderListView) rootView.findViewById(R.id.channel_list_view);
        // This can be removed when HeaderListView fixes a bug https://github.com/applidium/HeaderListView/issues/28
        //noinspection ResourceType
        list.setId(2);
        adapter = new EventAdapter(getActivity());
        list.setAdapter(adapter);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventsListEvent(EventsListEvent event) {
        LongSparseArray<List<Event>> events = event.getEvents();
        if (null != events && (events.size() > 0) && null != adapter) {
            adapter.adapt(events);
        }
    }

}


