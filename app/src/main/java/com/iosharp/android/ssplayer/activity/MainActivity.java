package com.iosharp.android.ssplayer.activity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.iosharp.android.ssplayer.BuildConfig;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.events.LoginEvent;
import com.iosharp.android.ssplayer.fragment.AboutFragment;
import com.iosharp.android.ssplayer.fragment.MainPagerFragment;
import com.iosharp.android.ssplayer.service.BackgroundService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;

public class MainActivity extends BaseMainActivity {
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
        final int playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (playServicesStatus != ConnectionResult.SUCCESS){
            //If google play services in not available show an error dialog and return
            final Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, playServicesStatus, 0, null);
            errorDialog.show();
        }
        mCastManager = PlayerApplication.getCastManager();
//        setContentView(null == mCastManager ? R.layout.activity_main_nocast : R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            GoogleAnalytics.getInstance(this).setDryRun(true);
            GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);;
        }

        googleAnalytics();
        EventBus.getDefault().register(this);
    }

    @NonNull
    @Override
    protected BaseAbstractFragment createInitialFragment() {
        return new MainPagerFragment();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void googleAnalytics() {
        mTracker = ((PlayerApplication)getApplication()).getTracker(
                PlayerApplication.TrackerName.APP_TRACKER);

        mTracker.setScreenName(getString(R.string.fragment_channels));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        } else {
            menu.findItem(R.id.media_route_menu_item).setVisible(false);
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
            changeFragment(new AboutFragment(), true);
            return true;
        } else if (id == R.id.action_login) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void changeFragment(BaseAbstractFragment fragment, boolean addToBack) {
        if (addToBack) {
            Intent i = new Intent(this, ChildActivity.class);
            i.putExtra(ru.johnlife.lifetools.Constants.EXTRA_FRAGMENT, fragment.getClass().getName());
            i.putExtra(ru.johnlife.lifetools.Constants.EXTRA_ARGUMENTS, fragment.getArguments());
            startActivity(i);
        } else {
            super.changeFragment(fragment, addToBack);
        }
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
}
