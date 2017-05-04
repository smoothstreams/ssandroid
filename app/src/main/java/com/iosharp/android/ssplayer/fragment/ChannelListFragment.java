package com.iosharp.android.ssplayer.fragment;

import android.content.Context;
import android.content.res.Resources;
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

import java.util.List;

import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;

public class ChannelListFragment extends BaseListFragment<Channel> {
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

                    private View.OnClickListener itemClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            VideoNavigationHandler.handleNavigation(getItem(), getActivity());
                        }
                    };

                    {
                        view.setOnClickListener(itemClickListener);
                    }

                    @Override
                    protected void hold(Channel channel) {
                        title.setText(channel.getName());
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
        if (null != channels && !channels.isEmpty() && null != adapter) {
            adapter.clear();
            adapter.addAll(channels);
        }
    }
}


