package com.iosharp.android.ssplayer.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.applidium.headerlistview.SectionAdapter;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Event;
import com.iosharp.android.ssplayer.fragment.AlertFragment;
import com.iosharp.android.ssplayer.fragment.VideoNavigationHandler;
import com.iosharp.android.ssplayer.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventAdapter extends SectionAdapter {
    private Context mContext;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private LongSparseArray<List<Event>> events = new LongSparseArray<>();

    public EventAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int numberOfSections() {
        return events.size();
    }

    @Override
    public int numberOfRows(int section) {
        if (section > -1) {
            return events.valueAt(section).size();
        }
        return 1;
    }

    public void adapt(LongSparseArray<List<Event>> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    private static class RowViewHolder {
        TextView tvChannel;
        TextView tvTime;
        TextView tvTitle;
    }

    @Override
    public View getRowView(int section, int row, View convertView, ViewGroup parent) {
        RowViewHolder rowViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_item_row, null);
            rowViewHolder = new RowViewHolder();

            rowViewHolder.tvChannel = (TextView) convertView.findViewById(R.id.channel);
            rowViewHolder.tvTime = (TextView) convertView.findViewById(R.id.time);
            rowViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);

            convertView.setTag(rowViewHolder);
        } else {
            rowViewHolder = (RowViewHolder) convertView.getTag();
        }

        Event e = getRowItem(section, row);
        String channel = e.getChannelBackReference().getName();
        String quality = e.getQuality();
        String language = e.getLanguage();

        String time = SIMPLE_DATE_FORMAT.format(new Date(e.getBeginTimeStamp())) + " - " + SIMPLE_DATE_FORMAT.format(new Date(e.getEndTimeStamp()));

        rowViewHolder.tvChannel.setText(channel);
        rowViewHolder.tvTime.setText(time);

        SpannableString qualitySpannableString = new SpannableString("");
        SpannableString languageSpannableString = new SpannableString("");

        SpannableString title = new SpannableString(e.getName());

        if (quality.equalsIgnoreCase("720p")) {
            qualitySpannableString = Utils.getHighDefBadge();
        }
        if (!language.equals("")) {
            languageSpannableString = Utils.getLanguageImg(mContext, language);
        }

        rowViewHolder.tvTitle.setText(TextUtils.concat(title, languageSpannableString, qualitySpannableString));

        return convertView;
    }

    @Override
    public Event getRowItem(int section, int row) {
        return events.valueAt(section).get(row);
    }

    @Override
    public boolean hasSectionHeaderView(int section) {
        return true;
    }

    @Override
    public void onRowItemClick(AdapterView<?> parent, View view, int section, int row, long id) {
        Event e = getRowItem(section, row);

        Date now = new Date();
        Date startDate = new Date(e.getBeginTimeStamp());
        Date endDate = new Date(e.getEndTimeStamp());

        if (mContext instanceof FragmentActivity) {
            FragmentActivity activity = ((FragmentActivity) mContext);
            if (now.before(startDate)) {

                FragmentManager fm = activity.getSupportFragmentManager();
                Bundle b = new Bundle();

                b.putInt(AlertFragment.BUNDLE_ID, e.getId());
                b.putString(AlertFragment.BUNDLE_NAME, e.getName());
                b.putInt(AlertFragment.BUNDLE_CHANNEL, e.getChannelBackReference().getChannelId());
                b.putLong(AlertFragment.BUNDLE_TIME, e.getBeginTimeStamp());

                AlertFragment alertFragment = new AlertFragment();
                alertFragment.setArguments(b);
                alertFragment.show(fm, AlertFragment.class.getSimpleName());
            } else if (endDate.after(now)) {
                VideoNavigationHandler.handleNavigation(e.getChannelBackReference(), activity);
            }
        }
    }

    private static class SectionHeaderViewHolder {
        TextView date;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        SectionHeaderViewHolder sectionHeaderViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_header_row, null);
            sectionHeaderViewHolder = new SectionHeaderViewHolder();

            sectionHeaderViewHolder.date = (TextView) convertView.findViewById(R.id.event_header_row_title);
            convertView.setTag(sectionHeaderViewHolder);
        } else {
            sectionHeaderViewHolder = (SectionHeaderViewHolder) convertView.getTag();
        }

        String date = getSectionHeaderItem(section);
        sectionHeaderViewHolder.date.setText(date);

        return convertView;
    }

    @Override
    public String getSectionHeaderItem(int section) {
        Date newDate = new Date(events.keyAt(section));
        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return desiredDateFormat.format(newDate);
    }

}


