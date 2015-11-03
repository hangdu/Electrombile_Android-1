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
        timestamp = Integer.parseInt(keyForValue(ProtocolConstants.TIMESTAMP));
        return timestamp;
    }

    @Override
    public int getIntensity() {
        intensity = Integer.parseInt(keyForValue(ProtocolConstants.INTENSITY));
        return intensity;
    }

}
