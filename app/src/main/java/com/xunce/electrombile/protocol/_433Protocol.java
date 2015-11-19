package com.xunce.electrombile.protocol;

import com.xunce.electrombile.Constants.ProtocolConstants;

/**
 * Created by lybvinci on 2015/9/28.
 */
public class _433Protocol extends Protocol {
    protected int timestamp;
    protected int intensity;

    public _433Protocol(String tmp) {
        super(tmp);
    }

    @Override
    public int getTimestamp() {
        String temp = keyForValue(ProtocolConstants.TIMESTAMP);
        if (isEmpty(temp)) return -1;
        timestamp = Integer.parseInt(temp);
        return timestamp;
    }

    @Override
    public int getIntensity() {
        String temp = keyForValue(ProtocolConstants.INTENSITY);
        if (isEmpty(temp)) return -1;
        intensity = Integer.parseInt(temp);
        return intensity;
    }

}
