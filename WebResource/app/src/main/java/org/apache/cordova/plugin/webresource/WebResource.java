package org.apache.cordova.plugin.webresource;

import android.app.Activity;
import android.text.TextUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cyij on 2017/3/17.
 */

public class WebResource extends CordovaPlugin {
    private static final String TAG = "WebResource";

    private static final String VERSION_FILE = "version";
    private static final String SEP = ",";
    private static final String APP_ZIP_FILE = "static/app.zip";
    private static final String WWW_PATH = "/www/";

    private static final int RESOURCE_OK = 1;
    private static final int RESOURCE_ERROR = -1;
    private static final int RESOURCE_IMPERFECT = 0;

    private String originVersion = "";
    private String preVersion = "";
    private String currentVersion = "";
    private String newVersion = "";

    private int resourceStatus = RESOURCE_OK;

    private interface FileOp {
        void run(CordovaArgs args);
    }

    private void threadhelper(final FileOp f, final CordovaArgs args, final CallbackContext callbackContext){
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    f.run(args);
                } catch ( Exception e) {
                    callbackContext.error(-1);
                }
            }
        });
    }

    private String settingForKey(String key) {
        if(this.preferences.contains(key)) {
            return this.preferences.getString(key, null);
        }
        return null;
    }

    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        LOG.v(TAG, "initialize");
        super.initialize(cordova, webView);
        Activity activity = cordova.getActivity();
        try {
            originVersion = FileUtil.readFile(activity.getResources().getAssets().open(VERSION_FILE));
        } catch (IOException e) {
            LOG.e(TAG, e.getMessage());
        }

        String versionInfo = FileUtil.readFile(VERSION_FILE);
        if (versionInfo == null) {
            // version file is not exist
            preVersion = originVersion;
            currentVersion = originVersion;
            newVersion = originVersion;
            FileUtil.mkdirs(WWW_PATH);
            try {
                if(!FileUtil.unzip(activity.getResources().getAssets().open(APP_ZIP_FILE), WWW_PATH)) {
                    resourceStatus = RESOURCE_ERROR;
                }
            } catch (IOException e) {
                resourceStatus = RESOURCE_ERROR;
                LOG.e(TAG, e.getMessage());
            }
        } else {
            String[] vs = TextUtils.split(versionInfo, SEP);
            if (vs.length == 3) {
                preVersion = vs[0];
                currentVersion = vs[1];
                newVersion = vs[2];
            }
        }

//        _wwwPath = [[NSBundle mainBundle] pathForResource:_wwwPath ofType:nil];
//        _docPath = [[[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:_wwwPath] stringByAppendingPathComponent:_appName];
//
//        NSString* originAppDir = [_wwwPath stringByAppendingPathComponent:_appName];
//        NSString* originAppZipFile = [originAppDir stringByAppendingString:@".zip"];
//
//        [self unzipResources:originAppZipFile fromWWW:YES delDestDir:NO];
    }

    private void unzipResources(InputStream inputStream, boolean fromWWW, boolean delDestDir) {
//        FileUtil.unzip(inputStream, _docPath);
//        String versionFile = _docPath + File.separator + "dist/" + _versionFileName;
//        _version = FileUtil.readFile(versionFile);
//        System.out.println(_version);
//
//        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
//        ZipEntry zipEntry;
//        String fileName;
//        try {
//            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
//                fileName = zipEntry.getName();
//                if (zipEntry.isDirectory()) {
//                    if (fileName.endsWith(File.separator)) {
//                        fileName = fileName.substring(0, fileName.length()-1);
//                    }
//                    File folder = new File(workDir.getAbsolutePath()+ File.separator+fileName);
//                    folder.mkdirs();
////                    File folder = application.getDir(_appName+ File.separator+fileName, Context.MODE_PRIVATE);
////                    folder.mkdirs();
//                } else {
//                    FileOutputStream outputStream = application.openFileOutput(_appName+ File.separator+fileName, Context.MODE_PRIVATE);
//                    int len;
//                    byte[] buffer = new byte[1024];
//                    while ((len = zipInputStream.read(buffer)) != -1) {
//                        outputStream.write(buffer, 0, len);
//                        outputStream.flush();
//                    }
//                    outputStream.close();
//                }
//            }
//            zipInputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        FileUtil.unzip(inputStream, _appName);

//        NSFileManager *fileManager = [NSFileManager defaultManager];
//        NSString* versionFile = [_docPath stringByAppendingPathComponent:_versionFileName];
//
//        if (![fileManager fileExistsAtPath:_docPath]) {
//            [fileManager createDirectoryAtPath:_docPath withIntermediateDirectories:YES attributes:nil error:nil];
//            if (fromWWW) {
//                [SSZipArchive unzipFileAtPath:zipFile toDestination: _docPath];
//                return;
//            }
//        } else {
//            if (fromWWW) {
//                return;
//            }
//        }
//
//        if ([fileManager fileExistsAtPath:zipFile]) {
//            [SSZipArchive unzipFileAtPath:zipFile toDestination: _docPath];
//            [fileManager removeItemAtPath:zipFile error:nil];
//        }
//        _version = [NSString stringWithContentsOfFile:versionFile encoding:NSUTF8StringEncoding error:nil];
//
    }

    private void unzipResources(String zipFile, boolean fromWWW, boolean delDestDir) {
        try {
            unzipResources(new FileInputStream(zipFile), fromWWW, delDestDir);
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
//        _docPath.charAt(0);
//        System.out.println(_version);

        threadhelper( new FileOp( ){
            public void run(CordovaArgs args) {
                // The getFreeDiskSpace plugin API is not documented, but some apps call it anyway via exec().
                // For compatibility it always returns free space in the primary external storage, and
                // does NOT fallback to internal store if external storage is unavailable.
//                long l = DirectoryManager.getFreeExternalStorageSpace();
//                String info = null;
//                try {
////                    info = FileUtil.readFile(_docPath + File.separator + args.getString(0));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, info));
            }
        }, args, callbackContext);

//        String info = FileUtil.readFile(_docPath + File.separator + args.getString(0));
//        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, info));

        return true;
//        LOG.v(TAG, "Executing action: " + action);
//        final Activity activity = this.cordova.getActivity();
//        final Window window = activity.getWindow();
//
//        if ("_ready".equals(action)) {
//            boolean statusBarVisible = (window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
//            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, statusBarVisible));
//            return true;
//        }
//
//        if ("show".equals(action)) {
//            this.cordova.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // SYSTEM_UI_FLAG_FULLSCREEN is available since JellyBean, but we
//                    // use KitKat here to be aligned with "Fullscreen"  preference
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        int uiOptions = window.getDecorView().getSystemUiVisibility();
//                        uiOptions &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//                        uiOptions &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
//
//                        window.getDecorView().setSystemUiVisibility(uiOptions);
//                    }
//
//                    // CB-11197 We still need to update LayoutParams to force status bar
//                    // to be hidden when entering e.g. text fields
//                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                }
//            });
//            return true;
//        }
//
//        if ("hide".equals(action)) {
//            this.cordova.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // SYSTEM_UI_FLAG_FULLSCREEN is available since JellyBean, but we
//                    // use KitKat here to be aligned with "Fullscreen"  preference
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        int uiOptions = window.getDecorView().getSystemUiVisibility()
//                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//
//                        window.getDecorView().setSystemUiVisibility(uiOptions);
//                    }
//
//                    // CB-11197 We still need to update LayoutParams to force status bar
//                    // to be hidden when entering e.g. text fields
//                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                }
//            });
//            return true;
//        }
//
//        if ("backgroundColorByHexString".equals(action)) {
//            this.cordova.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        setStatusBarBackgroundColor(args.getString(0));
//                    } catch (JSONException ignore) {
//                        LOG.e(TAG, "Invalid hexString argument, use f.i. '#777777'");
//                    }
//                }
//            });
//            return true;
//        }

//        return false;
    }

    private void setStatusBarBackgroundColor(final String colorPref) {
//        if (Build.VERSION.SDK_INT >= 21) {
//            if (colorPref != null && !colorPref.isEmpty()) {
//                final Window window = cordova.getActivity().getWindow();
//                // Method and constants not available on all SDKs but we want to be able to compile this code with any SDK
//                window.clearFlags(0x04000000); // SDK 19: WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                window.addFlags(0x80000000); // SDK 21: WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                try {
//                    // Using reflection makes sure any 5.0+ device will work without having to compile with SDK level 21
//                    window.getClass().getDeclaredMethod("setStatusBarColor", int.class).invoke(window, Color.parseColor(colorPref));
//                } catch (IllegalArgumentException ignore) {
//                    LOG.e(TAG, "Invalid hexString argument, use f.i. '#999999'");
//                } catch (Exception ignore) {
//                    // this should not happen, only in case Android removes this method in a version > 21
//                    LOG.w(TAG, "Method window.setStatusBarColor not found for SDK level " + Build.VERSION.SDK_INT);
//                }
//            }
//        }
    }
}
