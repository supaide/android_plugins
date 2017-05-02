package org.apache.cordova.plugin.network.http;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by cyij on 2017/3/19.
 */

public class AppRequest {

    private static final String DEFAULE_ERROR_MESSAGE = "网络异常";

    private static OkHttpClient client;
    private static Gson gson;

    public static void init() {
        init(20, 20);
    }

    public static void init(int readTimeout, int writeTimeout) {
        if (client != null) {
            return;
        }
        client = new OkHttpClient.Builder()
                .connectTimeout(writeTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        gson = new GsonBuilder().registerTypeAdapter(
                Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.intValue()) {
                            return new JsonPrimitive(src.intValue());
                        } else if (src == src.longValue()) {
                            return new JsonPrimitive(src.longValue());
                        } else {
                            return new JsonPrimitive(src);
                        }
                    }
                }
        ).create();
    }

    public static void get(Activity activity, Handler handler, String url, List<Pair<String, String>> params, Class clz, RequestCallback.Success success, RequestCallback.Failed error, LoadingCallback loadingCallback) {
        get(activity, handler, url, params, clz, success, error, loadingCallback, true);
    }

    public static void get(Activity activity, Handler handler, String url, List<Pair<String, String>> params, Class clz, RequestCallback.Success success, RequestCallback.Failed error, LoadingCallback loadingCallback, boolean parseAsAppResult) {
        if (client == null) {
            return;
        }
        if (params != null) {
            FormBody.Builder paramsBuilder = new FormBody.Builder();
            if (params != null) {
                for (Pair<String, String> param : params) {
                    if (param.first == null || param.second == null) {
                        continue;
                    }
                    paramsBuilder.add(param.first, param.second.toString());
                }
            }
            FormBody formBody = paramsBuilder.build();
            Map<String, List<String>> paramsMap = new HashMap<>();
            for(int i=0; i < formBody.size(); i++) {
                String name = formBody.encodedName(i);
                String value = formBody.encodedValue(i);
                if (paramsMap.containsKey(name)) {
                    paramsMap.get(name).add(value);
                } else {
                    List<String> v1 = new ArrayList<>();
                    v1.add(value);
                    paramsMap.put(name, v1);
                }
            }
            List<String> params0 = new ArrayList<>();
            for (String name : paramsMap.keySet()) {
                List<String> values = paramsMap.get(name);
                if (values.size() > 1) {
                    for (String v : values) {
                        params0.add(name + "[]=" + v);
                    }
                } else {
                    params0.add(name + "=" + values.get(0));
                }
            }
            if (url.contains("?")) {
                url += "&" + TextUtils.join("&", params0);
            } else {
                url += "?" + TextUtils.join("&", params0);
            }
        }
        final Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new InnerCallback(activity, handler, clz, success, error, loadingCallback, parseAsAppResult));
    }

    public static void post(Activity activity, Handler handler, String url, List<Pair<String, String>> params, Class clz, RequestCallback.Success success, RequestCallback.Failed error, LoadingCallback loadingCallback) {
        post(activity, handler, url, params, clz, success, error, loadingCallback, true);
    }

    public static void post(Activity activity, Handler handler, String url, List<Pair<String, String>> params, Class clz, RequestCallback.Success success, RequestCallback.Failed error, LoadingCallback loadingCallback, boolean parseAsAppResult) {
        if (client == null) {
            return;
        }
        FormBody.Builder paramsBuilder = new FormBody.Builder();
        if (params != null) {
            for (Pair<String, String> param : params) {
                if (param.first == null || param.second == null) {
                    continue;
                }
                paramsBuilder.add(param.first, param.second.toString());
            }
        }

        final Request request = new Request.Builder()
                .url(url)
                .post(paramsBuilder.build())
                .build();

        client.newCall(request).enqueue(new InnerCallback(activity, handler, clz, success, error, loadingCallback, parseAsAppResult));
    }

    private static class InnerCallback implements Callback {
        private static final long DETECT_API_RESPONSE_TIME = 200;
        private static final long LOADING_DELAY_TIME = 500;
        private Activity activity;
        private Handler handler;
        private LoadingCallback loadingCallback;
        private Class clz;
        private RequestCallback.Success success;
        private RequestCallback.Failed error;
        private boolean parseAsSPDResult;

        private boolean onResponse;
        private boolean isloading;

        private boolean isDestoryed() {
            if (activity == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.isDestroyed() || activity.isFinishing();
            } else {
                return activity == null || activity.isFinishing();
            }
        }

        private Handler getHandler() {
            if (!isDestoryed() && handler != null) {
                return handler;
            } else {
                return null;
            }
        }

        private Activity getActivity() {
            if (!isDestoryed() && activity != null) {
                return activity;
            } else {
                return null;
            }
        }

        private LoadingCallback getLoadingCallback() {
            if (getActivity() != null && loadingCallback != null) {
                return loadingCallback;
            } else {
                return null;
            }
        }

        public InnerCallback(Activity activity, Handler handler, Class clz, final RequestCallback.Success success, final RequestCallback.Failed error, LoadingCallback loadingCallback, boolean parseAsSPDResult) {

            this.activity = activity;
            this.handler = handler;
            if (activity != null && handler != null) {
                this.loadingCallback = loadingCallback;
            }
            if (handler == null) {
                this.loadingCallback = null;
            }
            this.clz = clz;
            this.success = success;
            this.error = error;
            this.parseAsSPDResult = parseAsSPDResult;

            if (this.loadingCallback != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!onResponse) {
                            isloading = true;
                            Handler handler = getHandler();
                            if (handler == null) {
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    LoadingCallback cb = getLoadingCallback();
                                    if (cb != null) {
                                        cb.showLoading();
                                    } else {
                                        isloading = false;
                                    }
                                }
                            });
                        }
                    }
                }, DETECT_API_RESPONSE_TIME);
            }
        }

        private void hideLoading() {
            if (!isloading) {
                return;
            }
            isloading = false;
            LoadingCallback cb = getLoadingCallback();
            if (cb != null) {
                cb.hideLoading();
            }
        }

        private void processError(final AppResult result) {
            hideLoading();
            if (error == null) {
                return;
            }
            if (result != null && result.getCodes() != null) {
                error.listener(result.getStatus(), result.getResult(), result.getCodes(), result.getMsgs());
            } else {
                Object errorMsg = DEFAULE_ERROR_MESSAGE;
                error.listener(AppResult.ERROR, null, Arrays.asList(AppResult.ERROR), Arrays.asList(errorMsg));
            }
        }

        private void doFailure(final Call call, final AppResult result) {
            Handler handler = getHandler();
            if (handler == null) {
                processError(result);
            } else {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        processError(result);
                    }
                };
                if (isloading) {
                    handler.postDelayed(runnable, LOADING_DELAY_TIME);
                } else {
                    handler.post(runnable);
                }
            }
        }

        @Override
        public void onFailure(Call call, IOException e) {
            onResponse = true;
            if (isDestoryed()) {
                hideLoading();
                return;
            }
            doFailure(call, null);
        }

        @Override
        public void onResponse(final Call call, final Response response) throws IOException {
            onResponse = true;
            if (isDestoryed() || success == null) {
                hideLoading();
                return;
            }

            if (!response.isSuccessful()) {
                doFailure(call, null);
                return;
            }
            String result0 = null;
            try {
                result0 = response.body().string();
            } catch (Exception e) {
                doFailure(call, null);
                return;
            }
            try {
                AppResult result = null;
                Map<String, String> extras = null;
                int status = AppResult.OK;
                if (parseAsSPDResult) {
                    result = gson.fromJson(result0, AppResult.class);
                    if (result == null || result.getStatus() != AppResult.OK) {
                        doFailure(call, result);
                        return;
                    }
                    status = result.getStatus();
                    result0 = gson.toJson(result.getResult());
                    extras = result.getExtras();
                }

                final String resultStr = result0;
                Object result1 = null;
                int total = 0;
                int listenerType = -1;
                if (success instanceof RequestCallback.StringSuccess) {
                    listenerType = 0;
                    result1 = result0;
                } else if (success instanceof RequestCallback.CommonSuccess) {
                    listenerType = 1;
                    result1 = gson.fromJson(result0, clz);
                } else if (success instanceof RequestCallback.ListSuccess) {
                    listenerType = 2;
                    ListResult listResult = gson.fromJson(result0, ListResult.class);
                    List list = new ArrayList();
                    if (listResult.getList() != null) {
                        boolean isString = false;
                        if (clz.getSimpleName().equalsIgnoreCase("string")) {
                            isString = true;
                        }
                        for (Object obj : listResult.getList()) {
                            if (isString) {
                                list.add(obj.toString());
                            } else {
                                list.add(gson.fromJson(gson.toJson(obj), clz));
                            }
                        }
                    }
                    result1 = list;
                    total = listResult.getTotal();
                }

                if (listenerType < 0) {
                    doFailure(call, result);
                    return;
                }

                final int finalListenerType = listenerType;
                final Object finalResult = result1;
                final int finalTotal = total;
                final int finalStatus = status;
                final Map<String, String> finalExtras = extras;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        if (finalListenerType == 0) {
                            ((RequestCallback.StringSuccess) success).listener(finalStatus, resultStr, finalExtras);
                        } else if (finalListenerType == 1) {
                            ((RequestCallback.CommonSuccess) success).listener(finalStatus, finalResult, finalExtras);
                        } else if (finalListenerType == 2) {
                            ((RequestCallback.ListSuccess) success).listener(finalStatus, (List) finalResult, finalTotal, finalExtras);
                        }
                    }
                };
                Handler handler = getHandler();
                if (handler != null) {
                    if (isloading) {
                        handler.postDelayed(runnable, LOADING_DELAY_TIME);
                    } else {
                        handler.post(runnable);
                    }
                }
            } catch (final Exception e) {
                doFailure(call, null);
            }
        }
    }
}
