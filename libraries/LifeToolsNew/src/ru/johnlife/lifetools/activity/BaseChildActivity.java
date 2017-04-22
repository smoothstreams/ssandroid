package ru.johnlife.lifetools.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import ru.johnlife.lifetools.Constants;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by Yan Yurkin
 * 15 June 2016
 */
public abstract class BaseChildActivity extends BaseMainActivity {

    @NonNull
    @Override
    protected BaseAbstractFragment createInitialFragment() {
        String fragmentClassName = getIntent().getStringExtra(Constants.EXTRA_FRAGMENT);
        if (fragmentClassName == null) {
            throw new IllegalArgumentException("Intent should contain string extra Constants.EXTRA_FRAGMENT");
        }
        BaseAbstractFragment fragment = (BaseAbstractFragment) Fragment.instantiate(this, fragmentClassName);
        fragment.setArguments(getIntent().getBundleExtra(Constants.EXTRA_ARGUMENTS));
        return fragment;
    }

}

