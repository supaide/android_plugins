package org.apache.cordova.plugin.pay;

import android.app.Activity;
import android.text.TextUtils;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cyij on 2017/4/27.
 */

public class Pay extends CordovaPlugin implements IWXAPIEventHandler {

    public static final int ILLEGAL_ARGUMENTS = 0;
    public static final int PAY_APP_NOT_EXIST = 1;
    public static final int PAY_CANCELED = 2;
    public static final int PAY_FAILED = 3;
    private static final int PAY_SUCCESS = 1000;

    private CallbackContext currentCallbackCtx;
    private boolean hasWXPayInited = false;
    private Activity activity;
    private IWXAPI wxApi;

    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.activity = cordova.getActivity();
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (args.length() < 1) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ILLEGAL_ARGUMENTS));
            return true;
        }
        JSONObject payArgs = args.optJSONObject(0);
        if (payArgs == null) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ILLEGAL_ARGUMENTS));
            return true;
        }
        this.currentCallbackCtx = callbackContext;
        if (action.equalsIgnoreCase("wx")) {
            wxPay(payArgs);
            return true;
        }
        return false;
    }

    private void wxPay(JSONObject payArgs) {
        String appId = payArgs.optString("appId");
        if (TextUtils.isEmpty(appId)) {
            currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ILLEGAL_ARGUMENTS));
            currentCallbackCtx = null;
            return;
        }

        if (!hasWXPayInited) {
            if (wxApi == null) {
                wxApi = WXAPIFactory.createWXAPI(this.activity, null);
            }
            if(!wxApi.registerApp(appId)) {
                currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PAY_APP_NOT_EXIST));
                currentCallbackCtx = null;
                return;
            }
            hasWXPayInited = true;
        }
        PayReq request = new PayReq();
        request.appId = appId;
        request.partnerId = payArgs.optString("partnerId");
        request.prepayId= payArgs.optString("prepayId");
        request.packageValue = payArgs.optString("packageValue");
        request.nonceStr= payArgs.optString("nonceStr");
        request.timeStamp= payArgs.optString("timeStamp");
        request.sign= payArgs.optString("sign");
        if (!wxApi.sendReq(request)) {
            currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ILLEGAL_ARGUMENTS));
            currentCallbackCtx = null;
            return;
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            // 根据返回码
            int code = baseResp.errCode;
            switch (code) {
                case 0:
                    currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PAY_SUCCESS));
                    break;
                case -2:
                    currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PAY_CANCELED));
                    break;
                default:
                    currentCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PAY_FAILED));
                    break;
            }
        }
    }
}
