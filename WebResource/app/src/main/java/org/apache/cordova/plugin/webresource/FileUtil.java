package org.apache.cordova.plugin.webresource;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by cyij on 2017/3/17.
 */

public class FileUtil {
    private static String externalFilePath = "";
    private static String externalCacheFilePath = "";
    private static final String WWW2_PATH = "www2";
    private static final long VALUE_2048 = 2048;
    private static final long VALUE_1024 = 1024;
    private static final String TAG = "FileUtil";

    public static String parseFileSize(long size) {
        String ret = "";
        if (size < VALUE_2048) {
            ret = String.valueOf(size) + "B";
        } else if ((size /= VALUE_1024) < VALUE_2048) {
            ret = String.valueOf(size) + "K";
        } else if ((size /= VALUE_1024) < VALUE_2048) {
            ret = String.valueOf(size) + "M";
        } else if ((size /= VALUE_1024) < VALUE_2048) {
            ret = String.valueOf(size) + "G";
        } else if ((size /= VALUE_1024) < VALUE_2048) {
            ret = String.valueOf(size) + "T";
        }
        return ret;
    }
//
//    public static String getExternalFilePath() {
//        if (externalFilePath == null || externalFilePath.isEmpty()) {
//            File file = SPDApplication.getInstance().getExternalFilesDir(null);
//            if (file != null) {
//                externalFilePath = file.getAbsolutePath();
//            }
//        }
//        return externalFilePath;
//    }
//
//    public static String getExternalCacheFilePath() {
//        if (externalCacheFilePath == null || externalCacheFilePath.isEmpty()) {
//            File file = SPDApplication.getInstance().getExternalCacheDir();
//            if (file != null) {
//                externalCacheFilePath = file.getAbsolutePath();
//            }
//        }
//        return externalCacheFilePath;
//    }

    //文件是否存在
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    //获得文件名
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int pos = filePath.lastIndexOf(File.separator);
        return (pos == -1) ? filePath : filePath.substring(pos + 1);
    }

    //获得文件夹名字
    public static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int pos = filePath.lastIndexOf(File.separator);
        return (pos == -1) ? "" : filePath.substring(0, pos);
    }

    //获得文件子文件的所有文件名
    public static ArrayList<String> getFileNames(String path) {
        ArrayList<String> fileNames = new ArrayList<>();

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (file.isFile()) {
            return null;
        }
        if (!file.isDirectory()) {
            return null;
        }
        for (File f : file.listFiles()) {

            fileNames.add(f.getName().replaceAll("[.][^.]+$", ""));
        }
        return fileNames;
    }

    //获得文件子文件的所有目录
    public static ArrayList<String> getFilePaths(String path) {
        ArrayList<String> filePaths = new ArrayList<>();

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (file.isFile()) {
            return null;
        }
        if (!file.isDirectory()) {
            return null;
        }
        for (File f : file.listFiles()) {

            filePaths.add(f.getAbsolutePath());
        }
        return filePaths;
    }

    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public static boolean mkdirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (TextUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }

    public static boolean writeFile(String filePath, String content, boolean append) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
            mkdirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
            }
        }
    }

    public static String readFile(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            int length = inputStream.available();
            byte [] buffer = new byte[length];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(InputStreamReader fileStream) {
        StringBuilder fileContent = new StringBuilder("");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(fileStream);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent.toString();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static String readFile(String filePath) {
        File file = new File(filePath);
        if (file == null || !file.isFile()) {
            return null;
        }

        InputStreamReader is = null;
        try {
            is = new InputStreamReader(new FileInputStream(file), "UTF-8");
        } catch (Exception e) {
            return null;
        }
        return readFile(is);
    }

    public static void copy(File srcFile, File destFile) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(srcFile);
            os = new FileOutputStream(destFile);
            byte[] buffer = new byte[10240];
            int byteRead = 0;
            while ((byteRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteRead);
            }
            os.flush();
        } catch(Exception e) {
        }
        finally{
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            is = null;
            os = null;
        }
    }

    //删除文件或目录
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }

        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    //删除文件目录下出所给子文件名之外的的所有文件
    public static void deleteFileExcept(String path, String name) {
        ArrayList<String> filePaths = getFilePaths(path);
        if(filePaths != null && filePaths.size()>0) {
            int size = filePaths.size();
            for (int i = 0; i < size; i++) {
                if (filePaths.get(i).contains(name)) {
                    continue;
                } else {
                    deleteFile(filePaths.get(i));
                }
            }
        }
    }

    public static boolean unzip(String zipFilePath, String destDir) {
        try {
            return unzip(new FileInputStream(zipFilePath), destDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean unzip(InputStream inputStream, String destDir) {
        try {
//            if (!mkdirs(destDir)) {
//                return false;
//            }
//            InputStream inputStream = SPDApplication.getInstance().getResources().getAssets().open(zipFilePath);
//            FileInputStream inputStream = new FileInputStream(zipFilePath);
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry;
            String fileName;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                fileName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    File folder = new File(destDir+ File.separator+fileName);
                    folder.mkdirs();
                } else {
                    File file = new File(destDir+ File.separator+fileName);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = zipInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            zipInputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


//    public static String getWWW2Path(String appName) {
//        return FileUtil.getExternalFilePath()+ File.separator+WWW2_PATH +
//                File.separator + appName;
//    }
//
//    public static void unzipWWW2(final String appName) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                unzip(appName + ".zip", FileUtil.getExternalFilePath() + File.separator + WWW2_PATH);
//            }
//        }).start();
//    }
//
//    public static String getFileFromWWW2(String appName, String fileName) {
//        String absPath = getWWW2Path(appName) + File.separator + fileName;
//        File file = new File(absPath);
//        if (!file.exists()) {
//            return null;
//        } else {
//            StringBuilder content = new StringBuilder();
//            BufferedReader reader = null;
//            try {
//                InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
//                reader = new BufferedReader(is);
//                String line = null;
//                while ((line = reader.readLine()) != null) {
//                    content.append(line);
//                }
//                return content.toString();
//            } catch (Exception e) {
//                return null;
//            } finally {
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    public static String getImageFromWWW2(String appName, String imageFileName) {
//        String absPath = getWWW2Path(appName) + File.separator + imageFileName;
//        InputStream inputStream = null;
//        try {
//            inputStream = new FileInputStream(absPath);
//            byte[] bytes;
//            byte[] buffer = new byte[8192];
//            int bytesRead;
//            ByteArrayOutputStream output = new ByteArrayOutputStream();
//            try {
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    output.write(buffer, 0, bytesRead);
//                }
//            } catch (IOException e) {
//                SPDLog.e(TAG, "getImageFromWWW2", e);
//                return null;
//            }
//            bytes = output.toByteArray();
//            return Base64.encodeToString(bytes, Base64.DEFAULT);
//        } catch (FileNotFoundException e) {
//            SPDLog.e(TAG, "getImageFromWWW2", e);
//            return null;
//        }
//    }
}
