package com.vbyte.p2p;

/**
 * Created by passion on 16-12-20.
 */

public interface UrlGenerator {

    /**
     * 防盗链请求策略的实现
     * @param sourceId 点播资源的id
     * @return 防盗链请求结果
     */
    SecurityUrl createSecurityUrl(String sourceId);
}
