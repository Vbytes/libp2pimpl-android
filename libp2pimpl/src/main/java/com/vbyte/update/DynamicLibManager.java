package com.vbyte.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by passion on 16-9-20.
 */
public class DynamicLibManager {
    private static final String UPDATE_HOST = "http://update.qvb.qcloud.com/checkupdate";

    private Context context;
    private String libDirPath;
    private static String jniVersion = "v2";

    public DynamicLibManager(Context context) {
        this.context = context;
        libDirPath = context.getFilesDir().getAbsolutePath() + File.separator + "vlib";
    }

    public String locate(final String fileid) throws Exception {
        // 删掉不必要的之前app版本的文件夹
        File libDir = new File(libDirPath);
        if (!libDir.exists()) {
            libDir.mkdirs();
        }
        final String appVersion = getAppVersion();
        File[] dirs = libDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().equals(appVersion);
            }
        });
        for (File dir: dirs) {
            deleteDir(dir);
        }

        String appLibPath = libDirPath + File.separator + getAppVersion() + File.separator + jniVersion;
        File appLibDir = new File(appLibPath);
        if (!appLibDir.exists()) {
            appLibDir.mkdirs();
        }
        File destFile = null;
        String maxVersion = "";
        for (File file: appLibDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getName().startsWith(fileid) && file.getName().endsWith(".so"));
            }
        })) {
            /**
             * e.g.
             *   libp2pmodule_armeabi-v7a_v1.2.0_3a4e2bdc231.so
             *   libvbyte-v7a_arm64-v8a_V2.2.6_3a4e2bdc231.so
             */
            String[] info = file.getName().split("_");
            if (info.length > 2 && info[info.length - 2].compareTo(maxVersion) > 0) {
                if (destFile != null) {
                    destFile.delete();
                }
                maxVersion = info[info.length - 2];
                destFile = file;
            }
        }
        return (destFile == null ? null : destFile.getAbsolutePath());
    }

    public void checkUpdate(final String fileId, final String version, final String abi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String token = MD5Util.MD5((fileId + "ventureinc").getBytes());
                    StringBuffer sb = new StringBuffer();
                    sb.append(UPDATE_HOST)
                            .append("?fileId=").append(fileId)
                            .append("&abi=").append(abi)
                            .append("&fifoVersion=").append(version)
                            .append("&token=").append(token)
                            .append("&jniVersion=v2")
                            .append("&packageName=").append(context.getPackageName());
                    URL url = new URL(sb.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30_000);
                    conn.setReadTimeout(10_000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(
                                conn.getInputStream()));
                        String jsonStr = "";
                        String line;
                        while ((line = input.readLine()) != null) {
                            jsonStr += line;
                        }

                        JSONObject jsonObj = new JSONObject(jsonStr);
                        boolean needUpdate = jsonObj.getBoolean("update");
                        if (needUpdate) {
                            // 此时需要更新
                            String downloadUrl = jsonObj.getString("downloadUrl");
                            String newVersion = jsonObj.getString("version");
                            String fingerprint = jsonObj.getString("md5token");
                            updateDynamicLib(fileId, downloadUrl, newVersion, abi, fingerprint);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void updateDynamicLib(String fileId, String downloadUrl, String newVersion,
                                          String abi, String fingerprint) throws Exception {
                // vlib/7.0.3/libp2pmodule_armeabi-v7a_v1.2.0_3a4e2bdc231.tmp
                String tmpFileName = fileId + "_" + abi + "_" + newVersion + "_" + fingerprint + ".tmp";
                String tmpDirPath = libDirPath + File.separator + getAppVersion() + File.separator + jniVersion;
                File tmpDir = new File(tmpDirPath);
                // 删除无用的tmp文件
                for (File file: tmpDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".tmp");
                    }
                })) {
                    if (!file.getName().equals(tmpFileName)) {
                        file.delete();
                    }
                }

                // 开始能断点式地下载
                String tmpFilePath = tmpDirPath + File.separator + tmpFileName;
                File tmpFile = new File(tmpFilePath);
                if (!tmpFile.exists()) {
                    tmpFile.createNewFile();
                }
                long finishedSize = tmpFile.length();

                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30_000);
                conn.setReadTimeout(30_000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range", "bytes=" + finishedSize + "-");
                if (conn.getResponseCode() == 206 || conn.getResponseCode() == 200) {
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");
                    try {
                        byte[] bytes = new byte[1024];
                        int count;
                        while ((count = bis.read(bytes)) != -1) {
                            raf.seek(finishedSize);
                            raf.write(bytes, 0, count);
                            finishedSize += count;
                        }

                        // 对比指纹是否正确
                        String md5sum = MD5Util.MD5(tmpFile);
                        if (md5sum.toLowerCase(Locale.US).equals(fingerprint.toLowerCase())) {
                            String filePath = libDirPath + File.separator + getAppVersion() + File.separator + jniVersion
                                    + File.separator + fileId + "_" + abi + "_"
                                    + newVersion + "_" + fingerprint + ".so";
                            tmpFile.renameTo(new File(filePath));
                        }
                        tmpFile.delete();
                    } finally {
                        raf.close();
                        bis.close();
                    }
                }
            }
        }).start();
    }

    private String getAppVersion () throws Exception {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        String version = packInfo.versionName;
        return version;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
