package com.iosharp.android.ssplayer;

import android.test.AndroidTestCase;

import com.iosharp.android.ssplayer.fragment.AlertFragment;
import com.iosharp.android.ssplayer.utils.Utils;

import java.util.Date;

public class TestUtils extends AndroidTestCase {

    public void testAlertDialogTimeString() {
        String desiredResult = "Thu Dec 04 2014 00:00";

        Date date = new Date(1417703324790l);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);

        String result = Utils.formatLongToString(date.getTime(), AlertFragment.TIME_FORMAT);

        assertEquals(desiredResult, result);
    }
}
