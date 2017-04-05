package com.iosharp.android.ssplayer.events;

/**
 * Created by Yan Yurkin
 * 04 April 2017
 */

public class LoginEvent {
    private int type;

    public interface Type {
        int Success = 0;
        int Failed = 1;
        int InProgress = 2;
    }

    private String error;

    public LoginEvent(int type) {
        this.type = type;
    }

    public LoginEvent(String error) {
        this.type = Type.Failed;
        this.error = error;
    }

    public int getType() {
        return type;
    }

    public String getError() {
        return error;
    }
}
