package com.xunce.electrombile.protocol;

import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.manager.TracksManager;

import java.util.Date;

/**
 * Created by lybvinci on 2015/9/28.
 */
public class CmdProtocol extends Protocol {
    protected int cmd;
    protected int result;
    protected int state;
    protected int period;
    protected int code;


    public CmdProtocol(String tmp) {
        super(tmp);
    }

    @Override
    public int getCmd() {
        String temp = keyForValue(ProtocolConstants.CMD);
        if (isEmpty(temp)) return -1;
        cmd = Integer.parseInt(temp);
        return cmd;
    }

    @Override
    public int getResult() {
        String temp = keyForValue(ProtocolConstants.RESULT);
        if (isEmpty(temp)) return -1;

        String lat = keyForValue(ProtocolConstants.LAT,tmp);
        String lng = keyForValue(ProtocolConstants.LNG,tmp);
        return result;
    }

    @Override
    public TracksManager.TrackPoint getNewResult() {
        String temp = keyForValue(ProtocolConstants.RESULT);
        if (isEmpty(temp)) return null;

        String lat = keyForValue(ProtocolConstants.LAT,temp);
        String lng = keyForValue(ProtocolConstants.LNG,temp);
        long long_timestamp = Long.parseLong(keyForValue(ProtocolConstants.TIMESTAMP, temp));
        Date date = new Date(long_timestamp*1000);
        //为什么相差了16个小时
        date.setHours(date.getHours()+16);
        TracksManager.TrackPoint trackPoint = new TracksManager.TrackPoint(date,Double.parseDouble(lat),Double.parseDouble(lng));
        return trackPoint;
    }



    @Override
    public int getState() {
        String temp = keyForValue(ProtocolConstants.STATE);
        if (isEmpty(temp)) return -1;
        state = Integer.parseInt(temp);
        return state;
    }

    @Override
    public int getNewState() {
        String temp = keyForValue(ProtocolConstants.RESULT);
        if (isEmpty(temp)) return -1;

        String s = keyForValue(ProtocolConstants.STATE,temp);
        state = Integer.parseInt(s);
        return state;
    }


    @Override
    public int getCode(){
        String temp = keyForValue(ProtocolConstants.CODE);
        if (isEmpty(temp)) return -1;
        code = Integer.parseInt(temp);
        return code;
    }

    @Override
    public int getPeriod() {
        String temp = keyForValue(ProtocolConstants.RESULT);
        if (isEmpty(temp)) return -1;

        String s = keyForValue(ProtocolConstants.PERIOD, temp);
        period = Integer.parseInt(s);
        return period;
    }
}
