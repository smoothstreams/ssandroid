package com.iosharp.android.ssplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.activity.LoginActivity;
import com.iosharp.android.ssplayer.data.Channel;
import com.iosharp.android.ssplayer.data.Event;
import com.iosharp.android.ssplayer.data.Service;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.events.ChannelsListEvent;
import com.iosharp.android.ssplayer.utils.Utils;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.fragment.BaseListFragment;

import static com.iosharp.android.ssplayer.utils.CastUtils.mediaInfoToBundle;

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
                            if (Service.hasActive() && User.hasActive()) {
                                if (!User.getCurrentUser().hasActiveHash()) {
                                    Utils.revalidateCredentials(view.getContext(), new Utils.OnRevalidateTaskCompleteListener() {
                                        @Override
                                        public void success(String result) {
                                            onClick(v);
                                        }
                                    });
                                }
                                handleNavigation(context, getItem());
                            } else {
                                Context context = view.getContext();
                                context.startActivity(new Intent(context, LoginActivity.class));
                            }
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void handleNavigation(Context context, Channel channel) {
        MediaInfo info = Utils.buildMediaInfo(context, channel);
        if (Utils.isInternetAvailable(context)) {
            Tracker t = ((PlayerApplication) getActivity().getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
            CastContext castManager = PlayerApplication.getCastManager();
            if (castManager != null && castManager.getCastState() == CastState.CONNECTED && castManager.getSessionManager().getCurrentCastSession() != null) {
                t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.ga_events_category_playback))
                    .setAction(getString(R.string.ga_events_action_chromecast))
                    .build());
                GoogleAnalytics.getInstance(getActivity().getBaseContext()).dispatchLocalHits();
                castManager.getSessionManager().getCurrentCastSession().getRemoteMediaClient().load(info);
//                castManager.startVideoCastControllerActivity(context, info, 0, true);

            } else {
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("media", mediaInfoToBundle(info));
                intent.putExtra("channel", channel.getChannelId());
                t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.ga_events_category_playback))
                    .setAction(getString(R.string.ga_events_action_local))
                    .build());
                GoogleAnalytics.getInstance(getActivity().getBaseContext()).dispatchLocalHits();
                context.startActivity(intent);
            }
        }
    }

    @Override
    protected String getTitle(Resources resources) {
        return null;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChannelsEvent(ChannelsListEvent event) {
        List<Channel> channels = event.getChannels();
        if (null != channels && !channels.isEmpty() && null != adapter) {
            adapter.clear();
            adapter.addAll(channels);
        }
    }
}


