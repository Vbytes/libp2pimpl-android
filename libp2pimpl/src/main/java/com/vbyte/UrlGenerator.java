package com.vbyte;

import com.vbyte.p2p.SecurityUrl;

/**
 * Url生成器接口，客户端通过该接口生成防盗链
 */

public interface UrlGenerator {

    /**
     * 防盗链请求策略的实现
     * @param sourceId 点播资源的id
     * @return 防盗链请求结果
     */
    SecurityUrl createSecurityUrl(String sourceId);
}
