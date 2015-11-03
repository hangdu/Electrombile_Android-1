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
        lng = Float.parseFloat(keyForValue(ProtocolConstants.LNG));
        return lng;
    }

    @Override
    public int getTimestamp() {
        timestamp = Integer.parseInt(keyForValue(ProtocolConstants.TIMESTAMP));
        return timestamp;
    }

    @Override
    public float getLat() {
        lat = Float.parseFloat(keyForValue(ProtocolConstants.LAT));
        return lat;
    }


}
