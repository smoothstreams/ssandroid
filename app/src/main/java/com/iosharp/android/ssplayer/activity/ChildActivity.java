package com.iosharp.android.ssplayer.activity;

import com.iosharp.android.ssplayer.Constants;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseChildActivity;

/**
 * Created by Yan Yurkin
 * 15 June 2016
 */
public class ChildActivity extends BaseChildActivity {
    @Override
    protected boolean shouldBeLoggedIn() {
        return false;
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }
}
