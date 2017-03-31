package com.iosharp.android.ssplayer.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;

import com.iosharp.android.ssplayer.R;

public class NoticeDialogFragment extends DialogFragment {
    private CheckBox mCheckBox;

    public NoticeDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.notice_dialog_title))
                .setPositiveButton(getString(R.string.notice_dialog_dismiss), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (mCheckBox.isChecked()) {

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(getString(R.string.pref_protocol_notice_checkbox_key), false);
                            editor.commit();
                        }
                        dismiss();
                    }
                });
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_notice, null);
        mCheckBox = (CheckBox) view.findViewById(R.id.notice_checkBox);

        d.setView(view);
        return d.create();
    }
}
