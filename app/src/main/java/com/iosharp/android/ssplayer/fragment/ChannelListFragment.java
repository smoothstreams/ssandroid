package com.iosharp.android.ssplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Channel;
import com.iosharp.android.ssplayer.data.Event;
import com.iosharp.android.ssplayer.events.ChannelsListEvent;
import com.iosharp.android.ssplayer.utils.Utils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;

public class ChannelListFragment extends BaseListFragment<Channel> {
    private static class Header extends Channel {
        public Header(String title) {
            setName(title);
        }
    }

    private BaseAdapter<Channel> adapter;

    @Override
    protected BaseAdapter<Channel> instantiateAdapter(final Context context) {
        adapter = new BaseAdapter<Channel>(R.layout.channel_list_row) {
            @Override
            protected ViewHolder<Channel> createViewHolder(final View view) {
                return new ViewHolder<Channel>(view) {
                    private TextView title = (TextView) view.findViewById(R.id.title);
                    private TextView event = (TextView) view.findViewById(R.id.event);
                    private ImageView icon = (ImageView) view.findViewById(R.id.icon);

                    {
                        view.setOnClickListener(v -> {
                            if (getItem() instanceof Header) return;
                            VideoNavigationHandler.handleNavigation(getItem(), getActivity());
                        });
                    }

                    @Override
                    protected void hold(Channel channel) {
                        boolean isHeader = channel instanceof Header;
                        title.setText(channel.getName());
                        title.setTextAppearance(title.getContext(), isHeader ? R.style.Header : R.style.ItemTitle);
                        icon.setVisibility(isHeader ? View.GONE : View.VISIBLE);
                        if (isHeader) {
                            event.setVisibility(View.GONE);
                            return;
                        }
                        setEvent(channel);
                        Picasso.with(context)
                            .load(channel.getImg())
                            .resize(100, 100)
                            .centerInside()
                            .into(icon);
                    }

                    private void setEvent(Channel channel) {
                        Event current = null;
                        long now = System.currentTimeMillis();
                        List<Event> events = channel.getEvents();
                        if (null == events) return;
                        for (Event event : events) {
                            String title = event.getName();
                            if (title != null && !title.isEmpty() && (now > event.getBeginTimeStamp()) && (now < event.getEndTimeStamp())) {
                                current = event;
                                break;
                            }
                        }
                        if (null != current) {
                            SpannableString qualitySpannableString = new SpannableString("");
                            SpannableString languageSpannableString = new SpannableString("");
                            String language = current.getLanguage();
                            String quality = current.getQuality();
                            if (!language.equals("")) {
                                languageSpannableString = Utils.getLanguageImg(context, language);
                            }
                            if (quality.equalsIgnoreCase("720p")) {
                                qualitySpannableString = Utils.getHighDefBadge();
                            }
                            event.setText(TextUtils.concat(current.getName(), languageSpannableString, qualitySpannableString));
                            event.setVisibility(View.VISIBLE);
                        } else {
                            event.setText("");
                            event.setVisibility(View.GONE);
                        }

                    }
                };
            }
        };
        EventBus.getDefault().register(this);
        return adapter;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected String getTitle(Resources resources) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        return null;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChannelsEvent(ChannelsListEvent event) {
        List<Channel> channels = event.getChannels();
        if (channels.isEmpty() || (null == adapter)) return;
        List<ChannelTop> top = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (Channel channel : channels) {
            int total = prefs.getInt("" + channel.getChannelId(), 0);
            if (total > 0) {
                top.add(new ChannelTop(total, channel));
            }
        }
        Collections.sort(top);
        top = top.subList(0,Math.min(3, top.size()));
        int i=1;
        adapter.set(channels);
        if (!top.isEmpty()) {
            adapter.add(new Header("Top watched"), 0);
            for (ChannelTop t : top) {
                if (t.total > 0) adapter.add(t.channel, i++);
            }
            adapter.add(new Header("All channels"), i);
        }
    }


    private static class ChannelTop implements Comparable<ChannelTop> {
        private int total;
        private Channel channel;

        public ChannelTop(int total, Channel channel) {
            this.total = total;
            this.channel = channel;
        }

        @Override
        public int compareTo(@NonNull ChannelTop o) {
            int y = total;
            int x = o.total;
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    }
}


