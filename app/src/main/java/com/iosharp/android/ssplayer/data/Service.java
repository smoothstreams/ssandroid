package com.iosharp.android.ssplayer.data;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Yan Yurkin
 * 01 April 2017
 */

public class Service {
    private static final List<String> names = Arrays.asList("live247", "mystreams", "starstreams", "mma-tv", "mma-sr", "streamtvnow");
    private static final String[] view = {"view247", "viewms", "viewss", "viewmma", "viewmmasr", "viewstvn"};
    private static final String[] rtPorts = {"3625", "3655", "3665", "3645", "3645", "3615"};

    private int id;

    static {
        boolean valid =
            names.size() == view.length &&
            names.size() == rtPorts.length;
        if (!valid) throw new IllegalStateException("mappers have different length");
    }

    public Service(String serviceName) {
        id = names.indexOf(serviceName);
        if (-1 == id) throw new IllegalArgumentException("Cannot find service with name "+serviceName);
    }

    public String getView() {
        return view[id];
    }

    public String getHtmlPort() {
        //maybe someday we'll need one more mapper. But right now port is the same for all services
        return "9100";
    }

    public String getRTPort() {
        return rtPorts[id];
    }
}
