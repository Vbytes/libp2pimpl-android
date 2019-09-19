package com.vbyte.p2p;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2019-05-22.
 * Copyright (c) 2019 Tencent Cloud. All Rights Reserved.
 */
public class Statistic {
    private static final String TAG = Statistic.class.getSimpleName();

    private String videoType = "unknown";
    private boolean isP2p = false;
    private int streamId = -1;
    private long cdnDownloadBytes = 0;
    private long p2pPushBytes = 0;
    private long p2pPullBytes = 0;
    private long p2pUpPushBytes = 0;
    private long p2pUpPullBytes = 0;
    //真正分享出去的数据
    private long sendPackageBytes = 0;
    private long p2pPushDuplicateBytes = 0;
    private long p2pPullDuplicateBytes = 0;
    private long vodKnownDuplicateBytes = 0;
    private long vodNoDataSubscribeBytes = 0;
    private long expiredBytes = 0;

    private long p2pBytes = 0;
    private long cdnBytes = 0;

    private int standbyNum = 0;
    private int candidateNum = 0;
    private int partnerNum = 0;
    private int parentNum = 0;
    private int childNum = 0;

    public void update(String jsonStr) {
        try {
            JSONObject rootObject = new JSONObject(jsonStr);
            videoType = rootObject.getString("videoType");
            isP2p = rootObject.getBoolean("p2pStarted");
            streamId = (int) rootObject.getDouble("streamId");

            JSONObject flowObject = rootObject.getJSONObject("flow");
            p2pBytes += (long) flowObject.getDouble("p2pBytes");
            cdnBytes += (long) flowObject.getDouble("cdnBytes");
        } catch (JSONException e) {
            /* do nothing */
        }
        // 测试使用
        try {
            JSONObject rootObject = new JSONObject(jsonStr);
            videoType = rootObject.getString("videoType");
            isP2p = rootObject.getBoolean("p2pStarted");
            streamId = (int) rootObject.getDouble("streamId");

            JSONObject flowObject = rootObject.getJSONObject("flow");
            cdnDownloadBytes = (long) flowObject.getDouble("cdnDownloadBytes");
            p2pPushBytes = (long) flowObject.getDouble("p2pPushBytes");
            p2pPullBytes = (long) flowObject.getDouble("p2pPullBytes");
            p2pUpPushBytes = (long) flowObject.getDouble("p2pUpPushBytes");
            p2pUpPullBytes = (long) flowObject.getDouble("p2pUpPullBytes");
            sendPackageBytes = (long) flowObject.getDouble("sendPackageBytes");
            p2pPushDuplicateBytes = (long) flowObject.getDouble("p2pPushDuplicateBytes");
            p2pPullDuplicateBytes = (long) flowObject.getDouble("p2pPullDuplicateBytes");
            vodKnownDuplicateBytes = (long) flowObject.getDouble("vodKnownDuplicateBytes");

            vodNoDataSubscribeBytes = (long) flowObject.getDouble("vodNoDataSubscribeBytes");
            expiredBytes = (long) flowObject.getDouble("expiredBytes");

            JSONObject peerObject = rootObject.getJSONObject("peer");
            standbyNum = (int) peerObject.getDouble("standbyNum");
            candidateNum = (int) peerObject.getDouble("candidateNum");
            partnerNum = (int) peerObject.getDouble("partnerNum");
            parentNum = (int) peerObject.getDouble("parentNum");
            childNum = (int) peerObject.getDouble("childNum");
        } catch (JSONException e) {
            /* do nothing */
        }
    }

    public void reset() {
        p2pBytes = 0;
        cdnBytes = 0;
    }

    public String getVideoType() {
        return videoType;
    }

    public boolean isP2p() {
        return isP2p;
    }

    public int getStreamId() {
        return streamId;
    }

    public long getP2PBytes() {
        return p2pBytes;
    }

    public long getCDNBytes() {
        return cdnBytes;
    }

    public long getCdnDownloadBytes() {
        return cdnDownloadBytes;
    }

    public long getP2pPushBytes() {
        return p2pPushBytes;
    }

    public long getP2pPullBytes() {
        return p2pPullBytes;
    }

    public long getP2pUpPushBytes() {
        return p2pUpPushBytes;
    }

    public long getP2pUpPullBytes() {
        return p2pUpPullBytes;
    }

    public long getSendPackageBytes() {
        return sendPackageBytes;
    }

    public long getP2pPushDuplicateBytes() {
        return p2pPushDuplicateBytes;
    }

    public long getP2pPullDuplicateBytes() {
        return p2pPullDuplicateBytes;
    }

    public long getVodKnownDuplicateBytes() {
        return vodKnownDuplicateBytes;
    }

    public long getVodNoDataSubscribeBytes() {
        return vodNoDataSubscribeBytes;
    }

    public long getExpiredBytes() {
        return expiredBytes;
    }

    public int getStandbyNum() {
        return standbyNum;
    }

    public int getCandidateNum() {
        return candidateNum;
    }

    public int getPartnerNum() {
        return partnerNum;
    }

    public int getParentNum() {
        return parentNum;
    }

    public int getChildNum() {
        return childNum;
    }
}
