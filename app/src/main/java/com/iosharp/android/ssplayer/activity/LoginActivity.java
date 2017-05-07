package com.iosharp.android.ssplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.data.Service;
import com.iosharp.android.ssplayer.data.User;
import com.iosharp.android.ssplayer.tasks.FetchLoginInfoTask;
import com.iosharp.android.ssplayer.tasks.OnTaskCompleteListener;
import com.iosharp.android.ssplayer.ui.Spinner;

import ru.johnlife.lifetools.tools.OnDoneActionListener;

/**
 * Created by Yan Yurkin
 * 04 April 2017
 */

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_ERROR = "error.extra";

    View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            error.setVisibility(View.GONE);
            boolean valid = true;
            if (null == selectedService) {
                setError(service, R.string.no_service_selected);
                showError(R.string.error_form_incomplete);
                valid = false;
            }
            if (username.getText().length() == 0) {
                setError(username, R.string.error_username_empty);
                showError(R.string.error_form_incomplete);
                valid = false;
            }
            if (password.getText().length() == 0) {
                setError(password, R.string.error_password_empty);
                showError(R.string.error_form_incomplete);
                valid = false;
            }
            if (valid) {
                progress.setVisibility(View.VISIBLE);
                submit.setVisibility(View.GONE);
                new FetchLoginInfoTask(
                    v.getContext(),
                    username.getText().toString().trim(),
                    password.getText().toString().trim(),
                    selectedService.getId(),
                    new OnTaskCompleteListener<String>() {
                        @Override
                        public void success(String result) {
                            progress.setVisibility(View.GONE);
                            finish();
                        }

                        @Override
                        public void error(String error) {
                            progress.setVisibility(View.GONE);
                            submit.setVisibility(View.VISIBLE);
                            showError(error);
                        }
                    }
                ).execute();
            }
        }

        private void setError(TextView v, @StringRes int error) {
            v.setError(v.getContext().getString(error));
        }
    };

    private Service selectedService = null;
    private Spinner<Service> service;
    private TextView username;
    private TextView password;
    private TextView error;
    private View progress;
    private View submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //noinspection unchecked
        service = (Spinner) findViewById(R.id.service);
        service.setItems(Service.getAvailable());
        service.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<Service>() {
            @Override
            public void onItemSelectedListener(Service item, int selectedIndex) {
                selectedService = item;
            }
        });
        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.password);
        password.setOnEditorActionListener(new OnDoneActionListener() {
            @Override
            protected void act(TextView v) {
                loginClickListener.onClick(v);
            }
        });
        progress = findViewById(R.id.progress);
        error = (TextView) findViewById(R.id.error);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(loginClickListener);
        //fill-up data
        if (Service.hasActive()) {
            selectedService = Service.getCurrent();
            service.setText(selectedService.getLabel());
        }
        if (User.hasActive()) {
            User user = User.getCurrentUser();
            username.setText(user.getUsername());
            password.setText(user.getPassword());
        }
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ERROR)) {
            showError(intent.getStringExtra(EXTRA_ERROR));
        }
    }

    private void showError(@StringRes int errorMsg) {
        error.setVisibility(View.VISIBLE);
        error.setText(errorMsg);
    }

    private void showError(String errorMsg) {
        error.setVisibility(View.VISIBLE);
        error.setText(errorMsg);
    }
}
