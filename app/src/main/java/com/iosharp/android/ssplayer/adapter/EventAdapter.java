package com.iosharp.android.ssplayer.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.applidium.headerlistview.SectionAdapter;
import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.fragment.AlertFragment;
import com.iosharp.android.ssplayer.model.Event;
import com.iosharp.android.ssplayer.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EventAdapter extends SectionAdapter {
    private Context mContext;
    private ArrayList<ArrayList<Event>> mDateEvents;
    private ArrayList<String> mDate;

    public EventAdapter(Context context, ArrayList<String> date, ArrayList<ArrayList<Event>> dateEvents) {
        mContext = context;
        mDate = date;
        mDateEvents = dateEvents;
    }

    @Override
    public int numberOfSections() {
        return mDateEvents.size();
    }

    @Override
    public int numberOfRows(int section) {
        if (section > -1) {
            return mDateEvents.get(section).size();
        }
        return 1;
    }

    private static class RowViewHolder {
        TextView tvChannel;
        TextView tvTime;
        TextView tvTitle;
    }

    @Override
    public View getRowView(int section, int row, View convertView, ViewGroup parent) {
        RowViewHolder rowViewHolder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_item_row, null);
            rowViewHolder = new RowViewHolder();

            rowViewHolder.tvChannel = (TextView) convertView.findViewById(R.id.event_item_row_channel);
            rowViewHolder.tvTime = (TextView) convertView.findViewById(R.id.event_item_row_time);
            rowViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.event_item_row_title);

            convertView.setTag(rowViewHolder);
        } else {
            rowViewHolder = (RowViewHolder) convertView.getTag();
        }

        Event e = getRowItem(section, row);
        String channel = String.format("%02d", e.getChannel());
        String quality = e.getQuality();
        String language = e.getLanguage();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(new Date(e.getStartDate()));

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
        return mDateEvents.get(section).get(row);
    }

    @Override
    public boolean hasSectionHeaderView(int section) {
        return true;
    }

    @Override
    public void onRowItemClick(AdapterView<?> parent, View view, int section, int row, long id) {
        Event e = getRowItem(section, row);

        Date now = new Date();
        Date startDate = new Date(e.getStartDate());

        if (now.before(startDate)) {

            if (mContext instanceof FragmentActivity) {
                FragmentActivity activity = ((FragmentActivity) mContext);

                FragmentManager fm = activity.getSupportFragmentManager();
                Bundle b = new Bundle();

                b.putInt(AlertFragment.BUNDLE_ID, e.getId());
                b.putString(AlertFragment.BUNDLE_NAME, e.getName());
                b.putInt(AlertFragment.BUNDLE_CHANNEL, e.getChannel());
                b.putLong(AlertFragment.BUNDLE_TIME, e.getStartDate());

                AlertFragment alertFragment = new AlertFragment();
                alertFragment.setArguments(b);
                alertFragment.show(fm, AlertFragment.class.getSimpleName());
            }
        }


    }

    private static class SectionHeaderViewHolder {
        TextView date;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        SectionHeaderViewHolder sectionHeaderViewHolder = null;

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
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public String getSectionHeaderItem(int section) {
        return getFormattedDate(mDate.get(section));
    }

    private String getFormattedDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ChannelContract.DATE_FORMAT);
            Date newDate = sdf.parse(date);

            SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String newDateString = desiredDateFormat.format(newDate);

            return newDateString;
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }
        return null;
    }
}


