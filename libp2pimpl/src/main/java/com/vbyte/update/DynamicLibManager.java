package com.vbyte.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DynamicLibManager {
    /** 动态库名称 **/
    public static String LIB_XP2P_SO_NAME = "libp2pmodule";

    /** 动态库根目录 **/
    private static String ROOT_DIR = "vlib";

    /** jni接口版本 **/
    private String JNI_VERSION = "v2";

    /** 新so(升级)的路径 **/
    private String libPath;

    /** 当前设备的架构ABI **/
    private static String archCpuAbi = "";

    private Context context;

    public DynamicLibManager(Context context) {
        this.context = context;
        scanDirs();
    }

    /**
     * 获取我们升级so所在的文件路径 -> libPath
     *
     * 1.能获取App版本:
     * /data/data/cn.vbyte.android.sample/files/vlib/0.4.3.5/v2/http/armeabi-v7a
     *
     * 2.不能获取App版本:
     * /data/data/cn.vbyte.android.sample/files/vlib/v2/http/armeabi-v7a
     *
     */
    private void scanDirs() {
        //路径拼接: /data/data/包名/files/vlib
        StringBuilder tmpLibDirPath = new StringBuilder();
        tmpLibDirPath.append(context.getFilesDir().getAbsolutePath())
                .append(File.separator)
                .append(ROOT_DIR);

        //路径拼接: /APP版本
        try {
            tmpLibDirPath.append(File.separator)
                    .append(getAppVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //路径拼接: /jni版本/http
        tmpLibDirPath.append(File.separator)
                .append(JNI_VERSION)
                .append(File.separator)
                .append("http");
        libPath = tmpLibDirPath.toString();

        File tmpHttpOrHttpsDir = new File(tmpLibDirPath.toString());
        if(!tmpHttpOrHttpsDir.exists()) {
            tmpHttpOrHttpsDir.mkdirs();
        }

        //路径拼接: /CPU架构
        if(scanArchDir(tmpHttpOrHttpsDir) == 1 && !archCpuAbi.isEmpty()) {
            tmpLibDirPath.append(File.separator).append(archCpuAbi);
            libPath = tmpLibDirPath.toString();
            if(!(new File(libPath)).exists()) {
                (new File(libPath)).mkdirs();
            }
        } else {
            deleteDir(tmpHttpOrHttpsDir);
        }
    }

    private int scanArchDir(File parentPath) {
        int archCpuAbiNum = 0;
        for (File file : parentPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        })) {
            final String[] ABIs = {"armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"};
            archCpuAbiNum += 1;

            if(Arrays.asList(ABIs).contains(file.getName())) {
                archCpuAbi = file.getName();
            }
        }
        return archCpuAbiNum;
    }

    private void makeArchDir(final String arch) {
        if(!libPath.endsWith(File.separator + arch)) {
            libPath = libPath + File.separator + arch;
            File tmpDir = new File(libPath);
            if(!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
        }
    }

    /**
     * 请求升级信息
     * @param arch 需要的架构ABI
     * @param soName 需要升级的so名称
     * @param soVersion 当前已加载的so版本
     * @return 以json格式返回请求升级的结果
     * @throws Exception
     */
    private String doRequestUpdate(String arch, String soName, String soVersion) throws Exception {
        if (arch.isEmpty() || soName.isEmpty() || soVersion.isEmpty()) {
            return "";
        }

        String packageName = context.getPackageName();
        String timeStamp = Long.toString((new Date().getTime()) / 1000); //获取10位unix时间戳
        String token = MD5Util.MD5((timeStamp + "qvb2017tencent" + packageName).getBytes());

        StringBuffer sb = new StringBuffer();
        sb.append("http://update.qvb.qcloud.com/checkupdate").append("/v2")
                .append("?abi=").append(arch)
                .append("&token=").append(token)
                .append("&timeStamp=").append(timeStamp)
                .append("&jniVersion=").append(JNI_VERSION)
                .append("&packageName=").append(context.getPackageName())
                .append("&fileId=").append(soName)
                .append("&fifoVersion=").append(soVersion);

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
            return jsonStr;
        }
        return "";
    }

    /**
     * 检查返回的json中是否存在需要升级的so，
     * 针对libp2pmodule.so会检查jniVersion字段
     * @param soName 需要升级的so
     * @param jsonObj 服务端返回的升级信息
     * @return 返回需要升级so对应的json
     *
     * @throws JSONException
     */
    private JSONObject checkSoExistInJson(String soName, JSONObject jsonObj) throws JSONException {
        if (jsonObj.has("downloadUrl")) {
            JSONObject jsonObjDownload = jsonObj.getJSONObject("downloadUrl");
            if (jsonObjDownload.has(soName)) {
                JSONObject soJsObj = jsonObjDownload.getJSONObject(soName);
                if (soName.equals(LIB_XP2P_SO_NAME)) {
                    if (soJsObj.has("jniVersion")
                            && !TextUtils.isEmpty(soJsObj.getString("jniVersion"))
                            && soJsObj.has("version")
                            && !TextUtils.isEmpty(soJsObj.getString("version"))
                            && soJsObj.has("url")
                            && !TextUtils.isEmpty(soJsObj.getString("url"))
                            && soJsObj.has("md5token")
                            && !TextUtils.isEmpty(soJsObj.getString("md5token"))) {
                        return soJsObj;
                    }
                } else {
                    if (soJsObj.has("version")
                            && !TextUtils.isEmpty(soJsObj.getString("version"))
                            && soJsObj.has("url")
                            && !TextUtils.isEmpty(soJsObj.getString("url"))
                            && soJsObj.has("md5token")
                            && !TextUtils.isEmpty(soJsObj.getString("md5token"))) {
                        return soJsObj;
                    }
                }
            }
        }
        return null;
    }

    private boolean doDownloadSo(String downloadUrl, String tmpFileName, String soPath, String md5) throws IOException {
        // 开始能断点式地下载
        String soPathFileTmpName = libPath + File.separator + tmpFileName;

        File tmpFile = new File(soPathFileTmpName);
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
                //下载buffer要为4k以上，不然安全团队找
                byte[] bytes = new byte[10240];
                int count;
                while ((count = bis.read(bytes)) != -1) {
                    raf.seek(finishedSize);
                    raf.write(bytes, 0, count);
                    finishedSize += count;
                }

                // 对比指纹是否正确
                String md5sum = MD5Util.MD5(tmpFile);
                if (md5sum.toLowerCase(Locale.US).equals(md5.toLowerCase())) {
                    tmpFile.renameTo(new File(soPath));
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

    private void clearTmpFiles(String excludeFile) {
        File tmpDir = new File(libPath);
        // 删除无用的tmp文件
        for (File file : tmpDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".tmp");
            }
        })) {
            if (!file.getName().equals(excludeFile)) {
                file.delete();
            }
        }
    }

    private boolean downloadSo(String soName, String downloadUrl, String newVersion, String md5) throws Exception {

        String soPath;
        String soFileName;
        String tmpFileName;

        if (soName.equals(LIB_XP2P_SO_NAME)) {
            soFileName = soName + "_" + newVersion + "_" + md5 + ".so";
            tmpFileName = soName + "_" + newVersion + "_" + md5 + ".tmp";
        } else {
            soFileName = soName + ".so";
            tmpFileName = soName + ".tmp";
        }
        soPath = libPath + File.separator + soFileName;

        if (new File(soPath).exists()) {
            return true;
        } else {
            clearTmpFiles(soFileName);
            return doDownloadSo(downloadUrl, tmpFileName, soPath, md5);
        }
    }

    /**
     * 检查升级
     * @param soName 指定需要升级的so
     * @param arch 当前设备的ABI
     * @param soVersion 当前已加载的so版本
     */
    public void checkUpdate(final String soName, final String arch, final String soVersion) {
        makeArchDir(arch);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String updateInfos = doRequestUpdate(arch, soName, soVersion);
                    JSONObject soJsonObj = checkSoExistInJson(soName, new JSONObject(updateInfos));
                    if (soJsonObj != null) {
                        downloadSo(soName, soJsonObj.getString("url"), soJsonObj.getString("version"), soJsonObj.getString("md5token"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean newSoNotExist() {
        return archCpuAbi.isEmpty();
    }

    public String locate(final String fileid) throws Exception {

        if(newSoNotExist()) {
            return null;
        }

        String vlibdir = this.context.getFilesDir().getAbsolutePath() + File.separator + ROOT_DIR;

        //删除旧的版本号的version是为了极端情况下，升级失败，用户升级app就好了
        File libDir = new File(vlibdir);
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

        //校验md5值
        String md5 = "";
        for (File file : (new File(libPath)).listFiles(new FileFilter() {
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
                //获取文件名里面写的md5值
                md5 = info[2];
                //md5是类似 21a948385c11706669a7740309968ee1.so 的
            }
        }

        // 对比指纹是否正确
        String md5sum = MD5Util.MD5(destFile);
        if ((md5sum + ".so").toLowerCase(Locale.US).equals(md5.toLowerCase())) {
            return (destFile == null ? null : (libPath + File.separator + destFile.getName()));
        }
        return null;
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