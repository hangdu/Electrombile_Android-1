package com.xunce.electrombile.protocol;

import com.xunce.electrombile.Constants.ProtocolConstants;

/**
 * Created by lybvinci on 2015/9/28.
 */
public class GPSProtocol extends Protocol {
    protected int timestamp;
    protected float lat;
    protected float lng;

    public GPSProtocol(String tmp) {
        super(tmp);
    }

    @Override
    public float getLng() {
        String tmp = keyForValue(ProtocolConstants.LNG);
        if (isEmpty(tmp)) return -1;
        lng = Float.parseFloat(tmp);
        return lng;
    }


    @Override
    public int getTimestamp() {
        String tmp = keyForValue(ProtocolConstants.TIMESTAMP);
        if (isEmpty(tmp)) return -1;
        timestamp = Integer.parseInt(tmp);
        return timestamp;
    }

    @Override
    public float getLat() {
        String tmp = keyForValue(ProtocolConstants.LAT);
        if (isEmpty(tmp)) return -1;
        lat = Float.parseFloat(tmp);
        return lat;
    }
}
