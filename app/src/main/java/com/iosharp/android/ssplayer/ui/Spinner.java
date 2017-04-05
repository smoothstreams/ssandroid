package com.iosharp.android.ssplayer.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Yan Yurkin
 * 04 April 2017
 */
public class Spinner<T extends Spinner.Listable> extends AppCompatEditText {
    private static final List<Integer> allowedKeycodes = Arrays.asList(
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_ESCAPE,
        KeyEvent.KEYCODE_MENU

    );

    public interface Listable {
        String getLabel();
    }

    List<T> mItems;
    String[] mListableItems;

    OnItemSelectedListener<T> onItemSelectedListener;

    public Spinner(Context context) {
        super(context);
        init(context);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setLongClickable(false);
        setFocusable(true);
        setClickable(true);
        setInputType(InputType.TYPE_NULL);
    }

    public void setItems(List<T> items) {
        this.mItems = items;
        this.mListableItems = new String[items.size()];
        int i = 0;
        for (T item : mItems) {
            mListableItems[i++] = item.getLabel();
        }
        configureOnClickListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (allowedKeycodes.contains(keyCode)) {
            return super.onKeyDown(keyCode, event);
        } else {
            performClick();
            return true;
        }
    }

    private void configureOnClickListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                    .setTitle(getHint())
                    .setItems(mListableItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int selectedIndex) {
                            setText(mListableItems[selectedIndex]);

                            if (onItemSelectedListener != null) {
                                onItemSelectedListener.onItemSelectedListener(mItems.get(selectedIndex), selectedIndex);
                            }
                        }
                    })
                    .create().show();
            }
        });
    }

    public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public interface OnItemSelectedListener<T> {
        void onItemSelectedListener(T item, int selectedIndex);
    }
}