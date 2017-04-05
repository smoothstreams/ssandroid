package com.iosharp.android.ssplayer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.iosharp.android.ssplayer.ui.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yan Yurkin
 * 01 April 2017
 */

public class Service implements Spinner.Listable {
    private static final String[] mapperView = {"view247", "viewms", "viewss", "viewmma", "viewmmasr", "viewstvn"};
    private static final String[] mapperRtPorts = {"3625", "3655", "3665", "3645", "3645", "3615"};
    private static String[] ids;

    private static final SparseArray<Service> mapper = new SparseArray<>(mapperView.length);
    private static Service currentService;

    static {
        Context c = PlayerApplication.getAppContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String current = prefs.getString(c.getString(R.string.pref_service_key), null);
        String[] labels = c.getResources().getStringArray(R.array.list_services);
        ids = c.getResources().getStringArray(R.array.list_services_values);
        boolean valid =
            labels.length == ids.length &&
            labels.length == mapperView.length &&
            labels.length == mapperRtPorts.length;
        if (!valid) throw new IllegalStateException("mappers have different length");

        int size = ids.length;
        for (int i = 0; i < size; i++) {
            String id = ids[i];
            Service service = new Service(id, labels[i], mapperView[i], mapperRtPorts[i]);
            if (id.equals(current)) {
                currentService = service;
            }
            mapper.put(id.hashCode(), service);
        }
    }

    public static boolean hasActive() {
        return currentService != null;
    }

    public static Service getCurrent() {
        return currentService;
    }

    public static void setCurrent(Service current) {
        currentService = current;
    }

    public static Service getService(String id) {
        return mapper.get(id.hashCode());
    }

    public static List<Service> getAvailable() {
        List<Service> values = new ArrayList<>();
        for (String id : ids) {
            values.add(mapper.get(id.hashCode()));
        }
        return values;
    }

    private String id;
    private String label;
    private String view;
    private String port;

    private Service(String id, String label, String view, String port) {
        this.id = id;
        this.label = label;
        this.view = view;
        this.port = port;
    }

    public String getView() {
        return view;
    }

    public String getHtmlPort() {
        //maybe someday we'll need one more mapper. But right now port is the same for all services
        return "9100";
    }

    public String getRTPort() {
        return port;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }
}
