package com.iosharp.android.ssplayer.fragment;

import android.content.res.Resources;

import com.iosharp.android.ssplayer.R;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.fragment.PagerFragment;

/**
 * Created by Yan Yurkin
 * 04 May 2017
 */

public class MainPagerFragment extends PagerFragment{
    @Override
    protected String getTitle(Resources r) {
        return r.getString(R.string.app_name);
    }

    @Override
    protected TabDescriptor[] getTabDescriptors() {
        return new TabDescriptor[]{
            new TabDescriptor(R.string.fragment_channels, new FragmentFactory() {
                @Override
                public BaseAbstractFragment createFragment() {
                    return new ChannelListFragment();
                }
            }),
            new TabDescriptor(R.string.fragment_events, new FragmentFactory() {
                @Override
                public BaseAbstractFragment createFragment() {
                    return new EventListFragment();
                }
            })
        };
    }

    @Override
    protected boolean isUpAsHomeEnabled() {
        return false;
    }
}
