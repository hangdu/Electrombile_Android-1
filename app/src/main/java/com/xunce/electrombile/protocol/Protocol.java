package com.xunce.electrombile.protocol;

import android.text.TextUtils;

import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.utils.useful.JSONUtils;

import org.json.JSONException;

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
        try {
            return JSONUtils.ParseJSON(tmp, key);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected final String keyForValue(String key,String tmp){
        try {
            return JSONUtils.ParseJSON(tmp, key);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public int getTimestamp() {
        throw new NullPointerException();
    }



    public int getIntensity() {
        throw new NullPointerException();
    }

    ;

    public int getCmd() {
        throw new NullPointerException();
    }

    ;

    public TracksManager.TrackPoint getInitialStatusResult(){
        throw new NullPointerException();
    }


    public Boolean getBatteryInfo(){
        throw new NullPointerException();
    }

    public int getResult() {
        throw new NullPointerException();
    }

    ;

    public TracksManager.TrackPoint getNewResult(){
        throw new NullPointerException();
    }

    public int getState() {
        throw new NullPointerException();
    }

    public int getNewState() {
        throw new NullPointerException();
    }

    public int getCode() {
        throw new NullPointerException();
    }


    public float getLng() {
        throw new NullPointerException();
    }

    ;

    public float getLat() {
        throw new NullPointerException();
    }

    public int getPeriod() {
        throw new NullPointerException();
    }

    protected boolean isEmpty(String temp) {
        if (TextUtils.isEmpty(temp)) {
            return true;
        }
        return false;
    }
}
