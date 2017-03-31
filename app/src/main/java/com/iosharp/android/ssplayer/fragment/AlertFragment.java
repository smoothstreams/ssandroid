package com.iosharp.android.ssplayer.fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.service.SmoothService;
import com.iosharp.android.ssplayer.utils.Utils;

public class AlertFragment extends DialogFragment {
    private static final String TAG = AlertFragment.class.getSimpleName();

    public static final String BUNDLE_NAME = "name";
    public static final String BUNDLE_CHANNEL = "channel";
    public static final String BUNDLE_TIME = "time";
    public static final String BUNDLE_ID = "id";

    public static final String TIME_FORMAT = "EEE MMM dd yyyy HH:mm";

    private String mSelectedValue;
    private int mId;
    private String mEventName;
    private int mEventChannel;
    private long mEventTime;
    private Tracker mTracker;

    public AlertFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTracker = ((PlayerApplication) getActivity().getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
        mTracker.setScreenName(getString(R.string.ga_screen_alert_dialog));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Bundle b = getArguments();

        if (b != null) {
            mId = b.getInt(BUNDLE_ID);
            mEventName = b.getString(BUNDLE_NAME);
            mEventChannel = b.getInt(BUNDLE_CHANNEL);
            mEventTime = b.getLong(BUNDLE_TIME);
        } else {
            Crashlytics.log(Log.ERROR, TAG, "Bundle is null!");
        }

        final TypedArray spinnerValues = getResources().obtainTypedArray(R.array.list_times_values);

        AlertDialog.Builder d = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_launcher)
                .setTitle(getString(R.string.alert_title))
                .setPositiveButton(getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int reminder = Integer.valueOf(mSelectedValue);
                        long reminderMilliseconds = reminder * 60 * 1000;

                        Intent intent = new Intent(getActivity(), SmoothService.AlertReceiver.class);
                        intent.putExtra(SmoothService.AlertReceiver.EXTRA_NAME, mEventName);
                        intent.putExtra(SmoothService.AlertReceiver.EXTRA_TIME, mEventTime);
                        intent.putExtra(SmoothService.AlertReceiver.EXTRA_CHANNEL, mEventChannel);

                        PendingIntent eventAlertIntent = PendingIntent.getBroadcast(getActivity(),
                                mId * 100,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        am.set(AlarmManager.RTC_WAKEUP, mEventTime, eventAlertIntent);

                        if (reminder != 0) {
                            PendingIntent reminderAlertIntent = PendingIntent.getBroadcast(getActivity(),
                                    mId * 100 - 1,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                            am.set(AlarmManager.RTC_WAKEUP, mEventTime - reminderMilliseconds, reminderAlertIntent);
                        }

                        Toast.makeText(getActivity(),
                                getActivity().getString(R.string.alert_successful),
                                Toast.LENGTH_SHORT).show();

                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.ga_events_category_alert))
                                .setAction(getString(R.string.ga_events_action_set_alert))
                                .build());

                        dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_alert, null);

        TextView nameView = (TextView) view.findViewById(R.id.alert_dialog_event_name_field);
        nameView.setText(mEventName);

        TextView channelView = (TextView) view.findViewById(R.id.alert_dialog_event_channel_field);
        channelView.setText(String.valueOf(mEventChannel));

        TextView timeView = (TextView) view.findViewById(R.id.alert_dialog_event_time_field);
        timeView.setText(Utils.formatLongToString(mEventTime, TIME_FORMAT));

        Spinner spinner = (Spinner) view.findViewById(R.id.alert_dialog_spinner);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.list_times, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedValue = spinnerValues.getString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        d.setView(view);
        return d.create();
    }
}