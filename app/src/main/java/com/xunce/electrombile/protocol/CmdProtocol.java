package com.xunce.electrombile.protocol;

import com.xunce.electrombile.Constants.ProtocolConstants;

/**
 * Created by lybvinci on 2015/9/28.
 */
public class CmdProtocol extends Protocol {
    protected int cmd;
    protected int result;
    protected int state;

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
        result = Integer.parseInt(temp);
        return result;
    }

    @Override
    public int getState() {
        String temp = keyForValue(ProtocolConstants.STATE);
        state = Integer.parseInt(temp);
        return state;
    }

}
