package com.iosharp.android.ssplayer.fragment;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iosharp.android.ssplayer.adapter.EventAdapter;
import com.iosharp.android.ssplayer.data.Event;
import com.iosharp.android.ssplayer.events.EventsListEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.util.List;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

public class EventListFragment extends BaseAbstractFragment {
    private EventAdapter adapter;


    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected String getTitle(Resources r) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = inflater.getContext();
        ViewGroup view = (ViewGroup) inflater.inflate(ru.johnlife.lifetools.R.layout.fragment_list, container, true);
        RecyclerView list = (RecyclerView) view.findViewById(ru.johnlife.lifetools.R.id.list);
        if (null != list) {
            list.setHasFixedSize(true);
            list.setLayoutManager(new StickyHeaderLayoutManager());
            adapter = new EventAdapter(context);
            list.setAdapter(adapter);
//            list.setAdapter(new SectioningAdapter());
        }
        EventBus.getDefault().register(this);
        return view;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventsListEvent(EventsListEvent event) {
        LongSparseArray<List<Event>> events = event.getEvents();
        if (null != events && (events.size() > 0) && null != adapter) {
            adapter.adapt(events);
        }
    }

}


