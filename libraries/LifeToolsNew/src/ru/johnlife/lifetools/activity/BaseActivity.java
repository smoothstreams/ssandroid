package ru.johnlife.lifetools.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.reporter.UpmobileExceptionReporter;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import ru.johnlife.lifetools.service.BaseBackgroundService.ServiceBinder;

public abstract class BaseActivity extends AppCompatActivity {
	
	private List<BaseBackgroundService.Requester<? extends BaseBackgroundService>> listeners = new ArrayList<BaseBackgroundService.Requester<?>>();
	
	private Intent serviceIntent;
	private boolean unbound = true;
	private boolean paused = true;
	private BaseBackgroundService service;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder binder) {
			Log.d(BaseActivity.this.getClass().getSimpleName(), "Service connected");
			service = ((ServiceBinder) binder).getService(); 
			BaseActivity.this.onServiceConnected(service);
			unbound = false;
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d(BaseActivity.this.getClass().getSimpleName(), "Service disconnected");
			unbound = true;
		}
	};

	protected FragmentManager fragmentManager() {
		return getSupportFragmentManager();
	}

	//------------------------------ protected API
	/**
	 * override this in project's base activity class - do you need login functionality
	 * @return
	 */
	protected abstract boolean shouldBeLoggedIn();
	protected abstract ClassConstantsProvider getClassConstants();


	public void setToolbar(Toolbar toolbar) {
		setSupportActionBar(toolbar);
	}

	
	//--------------------------------- internals
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(UpmobileExceptionReporter.getInstance(this));
		ClassConstantsProvider classConstants = getClassConstants();
		serviceIntent = classConstants.getBackgroundServiceIntent(this);
	}

	@Override
	protected void onPause() {
		paused = true;
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		paused = false;
		requestServiceConnection();
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	private void requestServiceConnection() {
		if (unbound) {
			bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
		}
	}
	
	@Override
	protected void onDestroy() {
		if (!unbound) {
			unbindService(serviceConnection);
		}
		super.onDestroy();
	}
	
	public boolean isServiceConnected() {
		return !(unbound || service == null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void onServiceConnected(BaseBackgroundService service) {
		if (!service.isLoggedIn() && shouldBeLoggedIn()) {
			getClassConstants().forceLogin(this);
			finish();
		}
		Log.d(getClass().getSimpleName(), "Requesting listeners");
		synchronized (listeners) {
			for (int i = 0; i < listeners.size(); i++) {
				BaseBackgroundService.Requester listener = listeners.get(i);
				Log.d(getClass().getSimpleName(), "Requesting " + listener);
				listener.requestService(service);
			}
			listeners.clear();
			Log.d(getClass().getSimpleName(), "Done requesting listeners");
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void requestService(BaseBackgroundService.Requester requester) {
		if (isServiceConnected()) {
			Log.d(getClass().getSimpleName(), "Service already connected. Requesting "+requester);
			requester.requestService(service);
		} else {
			synchronized (listeners) {
				Log.d(getClass().getSimpleName(), "Service not connected. Queueing "+requester);
				listeners.add(requester);
			}
		}
	}
	
}