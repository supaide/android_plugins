package org.apache.cordova.plugin.network.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by cyij on 16/3/2.
 */
public class FileDownloader {

    public interface FileDownloaderCallback {
        void onFileDownloadFailed();
        void onFileDownloadSuccess();
        void onProgress(long bytesRead, long contentLength);
    }

    private FileDownloaderCallback callback;

    public FileDownloader(FileDownloaderCallback callback) {
        this.callback = callback;
    }

    public void download(String url, final String fileName) {
        Request request = new Request.Builder().url(url).build();

        final ProgressResponseBody.ProgressListener progressListener = new ProgressResponseBody.ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                if (callback != null) {
                    callback.onProgress(bytesRead, contentLength);
                }
            }
        };

        OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        }).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFileDownloadFailed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                File file = new File(fileName);
                FileOutputStream fout = new FileOutputStream(file);
                InputStream is = response.body().byteStream();
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fout.write(buffer, 0, read);
                }
                is.close();
                fout.flush();
                fout.close();
                callback.onFileDownloadSuccess();
            }
        });
    }

}
