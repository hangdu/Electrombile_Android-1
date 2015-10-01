package com.xunce.electrombile.protocol;

import com.xunce.electrombile.utils.useful.JSONUtils;

import java.io.Serializable;

/**
 * Created by lybvinci on 2015/9/28.
 */
public class Protocol implements Serializable {
    protected String tmp;

    public Protocol(String tmp) {
        this.tmp = tmp;
    }

    protected final String keyForValue(String key) {
        return JSONUtils.ParseJSON(tmp, key);
    }

    public int getTimestamp() {
        return 0;
    }

    ;

    public int getIntensity() {
        return 0;
    }

    ;

    public int getCmd() {
        return 0;
    }

    ;

    public int getResult() {
        return 0;
    }

    ;

    public int getState() {
        return 0;
    }

    ;

    public float getLng() {
        return 0;
    }

    ;

    public float getLat() {
        return 0;
    }

    ;
}
