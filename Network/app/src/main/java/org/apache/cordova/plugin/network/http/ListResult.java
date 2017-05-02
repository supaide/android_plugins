package org.apache.cordova.plugin.network.http;

import java.util.List;

/**
 * Created by cyij on 2017/3/19.
 */

public class ListResult {
    private int total;
    private List<Object> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }
}
