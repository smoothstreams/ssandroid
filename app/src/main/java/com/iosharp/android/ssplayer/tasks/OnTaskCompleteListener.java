package com.iosharp.android.ssplayer.tasks;

/**
 * Created by Yan Yurkin
 * 04 April 2017
 */

public interface OnTaskCompleteListener<T> {
    void success(T result);
    void error(String error);
}
