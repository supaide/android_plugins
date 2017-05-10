package org.apache.cordova.plugin.network.http;

import java.util.List;
import java.util.Map;

/**
 * Created by cyij on 2017/3/19.
 */

public class AppResult {

    public static final int ERROR = -1;
    public static final int OK = 1;
    public static final int NEED_REFRESH_TOKEN = -2;
    public static final int NEED_LOGIN = -3;

    private int status;
    private Object result;
    private List<Integer> codes;
    private List<Object> msgs;
    private Map<String, String> extras;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public List<Integer> getCodes() {
        return codes;
    }

    public void setCodes(List<Integer> codes) {
        this.codes = codes;
    }

    public List<Object> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<Object> msgs) {
        this.msgs = msgs;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }
}
