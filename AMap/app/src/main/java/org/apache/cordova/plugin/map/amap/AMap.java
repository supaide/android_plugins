package org.apache.cordova.plugin.map.amap;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyij on 2017/4/27.
 */

public class AMap extends CordovaPlugin implements AMapLocationListener, PoiSearch.OnPoiSearchListener {

    private CallbackContext currentCallbackCtx;
    private Activity activity;
    private CordovaInterface cordova;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOptionForOneTime = null;

    private final String LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final String PERMISSION_DENIED_ERROR = "-100";
    private final int LOCATION_REQUEST_CODE = 1000;

    private final static String poiTypes = "住宿服务|风景名胜|商务住宅|地名地址信息|公共设施|餐饮服务|购物服务" +
            "|政府机构及社会团体|公司企业|道路附属设施|医疗保健服务|科教文化服务|交通设施服务|金融保险服务" +
            "汽车服务|汽车销售|汽车维修|生活服务|体育休闲服务|";

    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.activity = cordova.getActivity();
        locationClient = new AMapLocationClient(activity);
        locationClient.setLocationListener(this);
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equalsIgnoreCase("getLocation")) {
            this.currentCallbackCtx = callbackContext;
            if (cordova.hasPermission(LOCATION_PERMISSION)) {
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        getLocation();
                    }
                });
            } else {
                final CordovaPlugin that = this;
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cordova.requestPermission(that, LOCATION_REQUEST_CODE, LOCATION_PERMISSION);
                    }
                });

            }
        } else if (action.equalsIgnoreCase("poiSearch")) {
            final String keyword = args.optString(0);
            if (TextUtils.isEmpty(keyword)) {
                currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
            }
            final String city = args.optString(1);
            final int page = args.optInt(2, 1);
            final int pageSize = args.optInt(3, 10);
            this.currentCallbackCtx = callbackContext;
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    poiSearch(keyword, city, page, pageSize);
                }
            });
        } else {
            return false;
        }
        return true;
    }

    private void getLocation() {
        if (locationOptionForOneTime == null) {
            locationOptionForOneTime = new AMapLocationClientOption();
            locationOptionForOneTime.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOptionForOneTime.setOnceLocationLatest(true);
            locationOptionForOneTime.setNeedAddress(true);
            locationOptionForOneTime.setLocationCacheEnable(true);
            locationClient.setLocationOption(locationOptionForOneTime);
        }
        locationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (currentCallbackCtx == null) {
            return;
        }
        if (aMapLocation.getErrorCode() != 0) {
            currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
        } else {
            JSONObject result = new JSONObject();
            try {
                result.put("poi", aMapLocation.getLongitude() + "," + aMapLocation.getLatitude());
                result.put("address", aMapLocation.getAddress());
                result.put("province", aMapLocation.getProvince());
                result.put("city", aMapLocation.getCity());
                result.put("district", aMapLocation.getDistrict());
                result.put("street", aMapLocation.getStreet());
                result.put("cityCode", aMapLocation.getCityCode());
                result.put("adCode", aMapLocation.getAdCode());
                result.put("aoi", aMapLocation.getAoiName());
                currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
            } catch (JSONException e) {
                currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
            }
        }
        currentCallbackCtx = null;
    }

    private void poiSearch(String keyword, String city, int page, int pageSize) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", city);
        query.setPageSize(pageSize);
        query.setPageNum(page);
        PoiSearch poiSearch = new PoiSearch(this.activity, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int code) {
        if (currentCallbackCtx == null) {
            return;
        }
        if (code != 1000) {
            currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
        }
        List<PoiItem> pois = poiResult.getPois();
        List<JSONObject> result = new ArrayList<>();
        try {
            for (PoiItem poi : pois) {
                JSONObject item = new JSONObject();
                item.put("poi", poi.getLatLonPoint().getLongitude() + "," + poi.getLatLonPoint().getLatitude());
                item.put("adName", poi.getAdName());
                item.put("adCode", poi.getAdCode());
                item.put("cityCode", poi.getCityCode());
                item.put("city", poi.getCityName());
                item.put("province", poi.getProvinceName());
                item.put("title", poi.getTitle());
                item.put("typeCode", poi.getTypeCode());
                result.add(item);
            }
        } catch (JSONException e) {
        }
        currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONArray(result)));
        currentCallbackCtx = null;
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int code) {
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        if (currentCallbackCtx == null) {
            return;
        }
        for(int r:grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        getLocation();
                    }
                });
        }
    }

}
