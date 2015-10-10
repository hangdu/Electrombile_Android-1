package com.xunce.electrombile.protocol;

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
        cmd = Integer.parseInt(keyForValue(JsonKeys.CMD));
        return cmd;
    }

    @Override
    public int getResult() {
        result = Integer.parseInt(keyForValue(JsonKeys.RESULT));
        return result;
    }

    @Override
    public int getState() {
        state = Integer.parseInt(keyForValue(JsonKeys.STATE));
        return state;
    }

}
