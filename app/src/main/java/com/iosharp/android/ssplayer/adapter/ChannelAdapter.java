package com.iosharp.android.ssplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.fragment.ChannelListFragment;
import com.iosharp.android.ssplayer.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class ChannelAdapter extends CursorAdapter {

    public ChannelAdapter(Context context, Cursor c) {
        super(context, c);
    }

    private static class ViewHolder {
        ImageView channelIcon;
        TextView eventTitle;
        TextView channelName;

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View retView = inflater.inflate(R.layout.channel_list_row, viewGroup, false);

        ViewHolder holder = new ViewHolder();
        holder.channelIcon = (ImageView) retView.findViewById(R.id.imageView1);
        holder.eventTitle = (TextView) retView.findViewById(R.id.textView2);
        holder.channelName = (TextView) retView.findViewById(R.id.textView1);
        retView.setTag(holder);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        showIcon(viewHolder, context, cursor);
        setCurrentEvent(viewHolder, cursor);

        viewHolder.channelName.setText(cursor.getString(ChannelListFragment.COL_CHANNEL_NAME));
    }

    private void showIcon(ViewHolder viewHolder, Context context, Cursor cursor) {
        String SMOOTHSTREAMS_ICON_BASE = "http://smoothstreams.tv/schedule/includes/images/uploads/";
        String channelIcon = cursor.getString(ChannelListFragment.COL_CHANNEL_ICON);


        if (channelIcon != null || !channelIcon.equalsIgnoreCase("")) {
            String SMOOTHSTREAMS_ICON_URL = SMOOTHSTREAMS_ICON_BASE + channelIcon;

            Picasso.with(context)
                    .load(SMOOTHSTREAMS_ICON_URL)
                    .resize(100, 100)
                    .centerInside()
                    .into(viewHolder.channelIcon);
        }
    }

    private void setCurrentEvent(ViewHolder viewHolder, Cursor cursor) {
        String id = cursor.getString(ChannelListFragment.COL_EVENT_ID);

        if (id != null) {
            Date now = new Date();
            String title = cursor.getString(ChannelListFragment.COL_EVENT_NAME);
            Date startDate = new Date(cursor.getLong(ChannelListFragment.COL_EVENT_START_DATE));
            Date endDate = new Date(cursor.getLong(ChannelListFragment.COL_EVENT_END_DATE));
            String language = cursor.getString(ChannelListFragment.COL_EVENT_LANGUAGE);
            String quality = cursor.getString(ChannelListFragment.COL_EVENT_QUALITY);

            if (title != null || !title.equalsIgnoreCase("")) {

                if (now.after(startDate) && now.before(endDate)) {
                    SpannableString qualitySpannableString = new SpannableString("");
                    SpannableString languageSpannableString = new SpannableString("");

                    if (!language.equals("")) {
                        languageSpannableString = Utils.getLanguageImg(mContext, language);
                    }
                    if (quality.equalsIgnoreCase("720p")) {
                        qualitySpannableString = Utils.getHighDefBadge();
                    }

                    viewHolder.eventTitle.setText(TextUtils.concat(title, languageSpannableString, qualitySpannableString));
                    viewHolder.eventTitle.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 50;
    }
}