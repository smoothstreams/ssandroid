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
import android.widget.TextView;

import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Event;
import com.iosharp.android.ssplayer.fragment.AlertFragment;
import com.iosharp.android.ssplayer.fragment.VideoNavigationHandler;
import com.iosharp.android.ssplayer.utils.Utils;

import org.zakariya.stickyheaders.SectioningAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventAdapter extends SectioningAdapter {
    private Context mContext;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private LongSparseArray<List<Event>> events = new LongSparseArray<>();
    private final LayoutInflater inflater;

    public EventAdapter(Context context) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getNumberOfSections() {
        return events.size();
    }

    @Override
    public int getNumberOfItemsInSection(int section) {
        if (section > -1) {
            return events.valueAt(section).size();
        }
        return 1;
    }

    public void adapt(LongSparseArray<List<Event>> events) {
        this.events = events;
        notifyAllSectionsDataSetChanged();
//        notifyDataSetChanged();
    }

    private class RowViewHolder extends ItemViewHolder{
        TextView tvChannel;
        TextView tvTime;
        TextView tvTitle;

        RowViewHolder(View v) {
            super(v);
            tvChannel = (TextView) v.findViewById(R.id.channel);
            tvTime = (TextView) v.findViewById(R.id.time);
            tvTitle = (TextView) v.findViewById(R.id.title);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(events.valueAt(getSection()).get(getPositionInSection()));
                }
            });
        }
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
        return true;
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemUserType) {
        return new RowViewHolder(inflater.inflate(R.layout.event_item_row, parent, false));
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int sectionIndex, int itemIndex, int itemUserType) {
        Event e = getRowItem(sectionIndex, itemIndex);
        String channel = e.getChannelBackReference().getName();
        String quality = e.getQuality();
        String language = e.getLanguage();

        String time = SIMPLE_DATE_FORMAT.format(new Date(e.getBeginTimeStamp())) + " - " + SIMPLE_DATE_FORMAT.format(new Date(e.getEndTimeStamp()));

        RowViewHolder rowViewHolder = (RowViewHolder) viewHolder;
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
    }

    private Event getRowItem(int section, int row) {
        return events.valueAt(section).get(row);
    }

    private void onItemClick(Event e) {
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
                b.putInt(Constants.EXTRA_CHANNEL, e.getChannelBackReference().getChannelId());
                b.putLong(AlertFragment.BUNDLE_TIME, e.getBeginTimeStamp());

                AlertFragment alertFragment = new AlertFragment();
                alertFragment.setArguments(b);
                alertFragment.show(fm, AlertFragment.class.getSimpleName());
            } else if (endDate.after(now)) {
                VideoNavigationHandler.handleNavigation(e.getChannelBackReference(), activity);
            }
        }
    }

    private static class SectionHeaderViewHolder extends HeaderViewHolder {
        TextView date;

        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.event_header_row_title);
        }
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerUserType) {
        return new SectionHeaderViewHolder(inflater.inflate(R.layout.event_header_row, parent, false));
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int sectionIndex, int headerUserType) {
        String date = getSectionHeaderItem(sectionIndex);
        ((SectionHeaderViewHolder)viewHolder).date.setText(date);
    }

    private String getSectionHeaderItem(int section) {
        Date newDate = new Date(events.keyAt(section));
        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return desiredDateFormat.format(newDate);
    }

}


