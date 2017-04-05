package com.iosharp.android.ssplayer.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.Constants;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Service;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.events.LoginEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class FetchLoginInfoTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = FetchLoginInfoTask.class.getSimpleName();
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    private static final String SITE_PARAM = "site";

    private Context mContext;

    private String mUsername;
    private String mPassword;
    private OnTaskCompleteListener<String> listener;
    private String mService;
    private boolean isRevalidating = false;
    private JSONObject json = null;

    public FetchLoginInfoTask(Context context, OnTaskCompleteListener<String> listener) {
        if (!Service.hasActive() || !User.hasActive()) throw new IllegalStateException("Service is not selected");
        mContext = context;
        mService = Service.getCurrent().getId();
        User user = User.getCurrentUser();
        mUsername = user.getUsername();
        mPassword = user.getPassword();
        isRevalidating = true;
        this.listener = listener;
    }

    public FetchLoginInfoTask(Context context, String userName, String password, String service, OnTaskCompleteListener<String> listener) {
        mContext = context;
        mService = service;
        mUsername = userName;
        mPassword = password;
        this.listener = listener;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        final String USER_AGENT = PlayerApplication.getUserAgent(mContext);
        final OkHttpClient client = new OkHttpClient();
        try {
            String builtUrl = (mService.contains("mma") ? Constants.AUTH_MMA_URL : Constants.AUTH_SS_URL) + "?"
                    + USERNAME_PARAM + "=" + Uri.encode(mUsername) + "&"
                    + PASSWORD_PARAM + "=" + Uri.encode(mPassword) + "&"
                    + SITE_PARAM + "=" + Service.getService(mService).getView();

            URL url = new URL(builtUrl);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                json = new JSONObject(response.body().string());
            } else {
                //TODO: handle Internet errors
                throw new IOException("Unexpected code " + response);
            }
        } catch (JSONException|IOException e) {
            Crashlytics.logException(e);
            Log.w(getClass().getSimpleName(), e.getLocalizedMessage(), e);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            if (json.has("error")) {
                String message = json.getString("error");
                if (!isRevalidating && listener != null) {
                    listener.error(message);
                } else {
                    postLoginFailed(message);
                }
            } else if (json.has("hash")) {
                String password = json.getString("hash");
                Integer validMinutes = json.getInt("valid") - 5; //subtract 5 minutes for good measure
                long curTime = System.currentTimeMillis();
                Long endTime = curTime + (validMinutes * 60 * 1000);
                Service.setCurrent(Service.getService(mService));
                User.newUser(mUsername, mPassword).updateHash(endTime, password);
                if (null != listener){
                    listener.success(mUsername);
                }
                EventBus.getDefault().post(new LoginEvent(LoginEvent.Type.Success));
            } else {
                Log.e(TAG, "Unknown response!\n"+json);
                String error = mContext.getString(R.string.error_unknown_response);
                if (isRevalidating) {
                    postLoginFailed(error);
                } else {
                    if (listener != null) {
                        listener.error(error);
                    }
                }
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }
    }

    private void postLoginFailed(String message) {
        EventBus.getDefault().post(new LoginEvent(message));
    }

}