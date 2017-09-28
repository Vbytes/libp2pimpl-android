package com.vbyte.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by passion on 16-9-20.
 */
public class DynamicLibManager {
    private static final String UPDATE_HOST = "http://update.qvb.qcloud.com/checkupdate";

    private Context context;
    private String libDirPath;
    public String currentLibDirPath;

    //jni接口版本
    public String jniVersion = "v2";
    //非https要下载的so
    public String[] soNameArr = new String[]{"libp2pmodule", "libstun", "libevent"};
    public boolean supportHttps = false;
    //https情况下要下载的so
    public String[] soNameArrSupportHttps = new String[]{"libp2pmodule", "libstun", "libevent", "libevent_openssl", "libcrypto", "libssl"};



    public DynamicLibManager(Context context) {
        this.context = context;
        libDirPath = this.context.getFilesDir().getAbsolutePath() + File.separator + "vlib";
        try {
            currentLibDirPath = this.context.getFilesDir().getAbsolutePath() + File.separator + "vlib" + File.separator +  getAppVersion() + File.separator + jniVersion + File.separator + Build.CPU_ABI;
        } catch (Exception e) {
            currentLibDirPath = this.context.getFilesDir().getAbsolutePath() + File.separator + "vlib" + File.separator + jniVersion + File.separator + Build.CPU_ABI;
            e.printStackTrace();
        }
    }

    public boolean isSoReady() {
        //如果ready存在,  files/vlib/当前jniVersion/当前armeabi/ready 那么hasAllJniSo = true
        Log.e("s22s", currentLibDirPath);

        File currentLibDir = new File(currentLibDirPath);
        if (!currentLibDir.exists()) {
            currentLibDir.mkdirs();
        }
        try {
            return new File(currentLibDirPath + File.separator + "ready").exists();
        } catch (Exception e) {
            return false;
        }
    }

    //第一次升级， true "", 第二次只检查libp2pmodule的升级
    public void checkUpdateV2(final boolean firstDownload, final String soName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String packageName = context.getPackageName();
                    //获取10位unix时间戳
                    String timeStamp = Long.toString((new Date().getTime()) / 1000);
                    String token = MD5Util.MD5((timeStamp + "qvb2017tencent" + packageName).getBytes());

                    StringBuffer sb = new StringBuffer();
                    sb.append("http://update.qvb.qcloud.com/checkupdate").append("/v2")
                            .append("?abi=").append(Build.CPU_ABI)
                            .append("&token=").append(token)
                            .append("&timeStamp=").append(timeStamp)
                            .append("&jniVersion=").append(jniVersion)
                            .append("&packageName=").append(context.getPackageName());


                    if (supportHttps) {
                        sb.append("&supportHttps=true");
                        soNameArr = soNameArrSupportHttps;
                    }
                    if (firstDownload) {
                        sb.append("&fileId=").append(TextUtils.join(",", soNameArr));
                    } else {
                        String[] tmpArr = soName.split("_");

                        if (tmpArr.length == 3) {
                            sb.append("&fileId=").append("libp2pmodule")
                                    .append("&fifoVersion").append(tmpArr[1]);
                        } else {
                            return;
                        }
                    }
                    URL url = new URL(sb.toString());
                    Log.e("s22s", sb.toString());

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
                        Log.e("s22s", sb.toString());
                        Log.e("s22s bidy", jsonStr);

                        JSONObject jsonObj = new JSONObject(jsonStr);

                        Log.e("s22s", jsonObj.toString());

                        if (jsonObj.has("downloadUrl")) {
                            String[] downloadSoArr;
                            if (firstDownload) {
                                downloadSoArr = soNameArr;
                            } else {
                                downloadSoArr = new String[]{"libp2pmodule"};
                            }

                            Map<String, JSONObject> soJsonMap = new HashMap<>();

                            JSONObject jsonObjDownload = jsonObj.getJSONObject("downloadUrl");


                            for (String soName : downloadSoArr) {
                                if (jsonObjDownload.has(soName)) {
                                    JSONObject jsonObjTmp = jsonObjDownload.getJSONObject(soName);
                                    if (soName.equals("libp2pmodule")) {
                                        //如果是libp2pmodule还检查jniVersion字段
                                        if (jsonObjTmp.has("jniVersion")
                                                && !jsonObjTmp.getString("jniVersion").isEmpty()
                                                && jsonObjTmp.has("version")
                                                && !jsonObjTmp.getString("version").isEmpty()
                                                && jsonObjTmp.has("url")
                                                && !jsonObjTmp.getString("url").isEmpty()
                                                && jsonObjTmp.has("md5token")
                                                && !jsonObjTmp.getString("md5token").isEmpty()) {
                                            soJsonMap.put(soName, jsonObjTmp);
                                        }
                                    } else {
                                        //不是libp2pmodule不检查jniVersion字段
                                        if (jsonObjTmp.has("version")
                                                && !jsonObjTmp.getString("version").isEmpty()
                                                && jsonObjTmp.has("url")
                                                && !jsonObjTmp.getString("url").isEmpty()
                                                && jsonObjTmp.has("md5token")
                                                && !jsonObjTmp.getString("md5token").isEmpty()) {
                                            soJsonMap.put(soName, jsonObjTmp);
                                        }
                                    }
                                }
                            }

                            if (soJsonMap.size() == downloadSoArr.length) {
                                boolean writeReady = true;
                                for (Map.Entry<String, JSONObject> entry : soJsonMap.entrySet()) {

                                    JSONObject jsonObject = entry.getValue();
                                    writeReady = (writeReady && updateDynamicLib(entry.getKey(), jsonObject.getString("url"), jsonObject.getString("version"), jsonObject.getString("md5token")));
                                }
                                if (firstDownload && writeReady) {
                                    //第一次下载且都下载成功创建文件标识符
                                    new File(currentLibDirPath + File.separator + "ready").createNewFile();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            //存在so或者下载完成, 返回true
            private boolean updateDynamicLib(String soName, String downloadUrl, String newVersion, String md5) throws Exception {
                Log.e("s22s", String.format("soName = %s, downloadUrl = %s, newVersion= %s, md5 = %s", soName, downloadUrl, newVersion, md5));


                String soPathFileName;
                String soFileName;
                String tmpFileName;

                if (soName.equals("libp2pmodule")) {
                    soFileName = soName + "_" + newVersion + "_" + md5 + ".so";
                    tmpFileName = soName + "_" + newVersion + "_" + md5 + ".tmp";
                } else {
                    soFileName = soName + ".so";
                    tmpFileName = soName + ".tmp";
                }
                soPathFileName = currentLibDirPath + File.separator + soFileName;

                Log.e("s22s", String.format("soPathFileName = %s, soFileName = %s, tmpFileName= %s", soPathFileName, soFileName, tmpFileName));
                //上次下载未完成，存在文件，返回true
                if (new File(soPathFileName).exists()) {
                    return true;
                }
                File tmpDir = new File(currentLibDirPath);
                // 删除无用的tmp文件
                for (File file : tmpDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".tmp");
                    }
                })) {
                    if (!file.getName().equals(soFileName)) {
                        file.delete();
                    }
                }

                // 开始能断点式地下载
                String soPathFileTmpName = currentLibDirPath + File.separator + tmpFileName;

                File tmpFile = new File(soPathFileTmpName);
                if (!tmpFile.exists()) {
                    tmpFile.createNewFile();
                }
                long finishedSize = tmpFile.length();

                Log.e("s22s", "download " + downloadUrl);
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
                        if (md5sum.toLowerCase(Locale.US).equals(md5.toLowerCase())) {
                            tmpFile.renameTo(new File(soPathFileName));
                            return true;
                        }
                        tmpFile.delete();
                    } finally {
                        raf.close();
                        bis.close();
                    }
                }

                return false;
            }
        }).start();
    }

    public String locate(final String fileid) throws Exception {
        //删除旧的版本号的version是为了极端情况下，升级失败，用户升级app就好了
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


        File destFile = null;
        String maxVersion = "";
        for (File file : (new File(currentLibDirPath)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getName().startsWith(fileid) && file.getName().endsWith(".so"));
            }
        })) {
            /**
             * e.g.
             *   libp2pmodule_v1.2.0_3a4e2bdc231.so
             */
            String[] info = file.getName().split("_");
            if (info.length == 3 && info[info.length - 2].compareTo(maxVersion) > 0) {
                if (destFile != null) {
                    destFile.delete();
                }
                maxVersion = info[info.length - 2];
                destFile = file;
            }
        }
        return (destFile == null ? null : destFile.getName());
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
