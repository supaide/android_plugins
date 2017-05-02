package org.apache.cordova.plugin.network.http;

import java.util.List;
import java.util.Map;

/**
 * Created by cyij on 2017/3/19.
 */

public interface RequestCallback {
    interface Success<T> {
    }

    interface Failed {
        void listener(int status, Object result, List<Integer> codes, List<Object> msgs);
    }

    interface ListSuccess<T> extends Success {
        void listener(int status, List<T> result, int total, Map<String, String> extras);
    }

    interface CommonSuccess<T> extends Success {
        void listener(int status, T object, Map<String, String> extras);
    }

    interface StringSuccess extends Success {
        void listener(int status, String object, Map<String, String> extras);
    }
}
