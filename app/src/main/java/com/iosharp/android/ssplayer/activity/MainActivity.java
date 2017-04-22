package com.iosharp.android.ssplayer.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.iosharp.android.ssplayer.BuildConfig;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.events.LoginEvent;
import com.iosharp.android.ssplayer.fragment.ChannelListFragment;
import com.iosharp.android.ssplayer.fragment.EventListFragment;
import com.iosharp.android.ssplayer.service.BackgroundService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.fabric.sdk.android.Fabric;
import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseActivity;
import ru.johnlife.lifetools.service.BaseBackgroundService;

public class MainActivity extends BaseActivity {
    private final static String[] TAB_TITLES = {"Channels", "Events"};
    private CastContext mCastManager;
    private Tracker mTracker;
    private MenuItem loginItem;

    @Override
    protected boolean shouldBeLoggedIn() {
        return false;
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            GoogleAnalytics.getInstance(this).setDryRun(true);
            GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);;
        }

        googleAnalytics();
        setupActionBar();
        setupTabs();

        mCastManager = PlayerApplication.getCastManager();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void googleAnalytics() {
        mTracker = ((PlayerApplication)getApplication()).getTracker(
                PlayerApplication.TrackerName.APP_TRACKER);

        mTracker.setScreenName(TAB_TITLES[0]);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagertabstrip);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.SteelBlue));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTracker = ((PlayerApplication) getApplication()).getTracker(
                        PlayerApplication.TrackerName.APP_TRACKER);

                mTracker.setScreenName(TAB_TITLES[position]);
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void getChannels() {
        requestService(new BaseBackgroundService.Requester<BackgroundService>() {
            @Override
            public void requestService(BackgroundService service) {
                service.refreshSchedule();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChannels();
    }

    public void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_main));
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(false);

        loginItem = menu.findItem(R.id.action_login);
        if (User.hasActive()) {
            loginItem.setTitle(User.getCurrentUser().getUsername());
        } else {
            loginItem.setIcon(R.drawable.ic_action_loged_out);
        }
        if (mCastManager != null) {
            CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            getChannels();
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        } else if (id == R.id.action_login) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (null != loginItem) {
            int type = event.getType();
            if (type == LoginEvent.Type.Failed) {
                loginItem.setIcon(R.drawable.ic_action_loged_out);
                loginItem.setTitle(R.string.not_logged_in);
            } else if (type == LoginEvent.Type.Success) {
                loginItem.setIcon(R.drawable.ic_action_login);
                loginItem.setTitle(User.getCurrentUser().getUsername());
            }
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChannelListFragment();
                case 1:
                    return new EventListFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position].toUpperCase();
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length ;
        }

    }


}
