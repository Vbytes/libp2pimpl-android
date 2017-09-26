package com.vbyte.p2p;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by passion on 16-12-23.
 */

public class SecurityUrl {
    private String url = "";
    private String method = "GET";
    private HashMap<String, String> headers = new HashMap<String, String>();

    /**
     * 构造函数
     */
    public SecurityUrl() {

    }

    /**
     * 构造函数
     * @param url 防盗链请求的url
     */
    public SecurityUrl(String url) {
        this.url = url;
    }

    /**
     * 构造函数
     * @param url 防盗链请求的url
     * @param method 防盗链请求的方法
     */
    public SecurityUrl(String url, String method) {
        this.url = url;
        this.method = method;
    }

    /**
     * 设置防盗链请求的url
     * @param url 要设置的防盗链的url
     * @return 对象本身，可以链式调用
     */
    public SecurityUrl setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 获取防盗链请求的url
     * @return 防盗链请求的url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置防盗链请求的http方法
     * @param method 要设置的防盗链请求的http方法
     * @return 对象本身，可以链式调用
     */
    public SecurityUrl setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * 获取防盗链请求的http方法
     * @return 防盗链请求的http方法，默认是"GET"
     */
    public String getMethod() {
        return method;
    }

    /**
     * 为防盗链请求添加自定义header
     * @param header header的名字，如"User-Agent"、"Authorization"
     * @param value header的值
     * @return 对象本身，可以链式调用
     */
    public SecurityUrl addHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    /**
     * 获取所有自定义的防盗链请求的请求头
     * @return 所有自定义的防盗链请求的请求头
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * 将设置好的防盗链请求以json格式序列化
     * @return 防盗链请求以json格式序列化的字符串
     */
    public String toString() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("url", url);
            obj.put("method", method);

            JSONObject headersObj = new JSONObject();
            Iterator iter = headers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String headerName = (String) entry.getKey();
                String headerValue = (String) entry.getValue();
                headersObj.put(headerName, headerValue);
            }
            obj.put("headers", headersObj);

            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
