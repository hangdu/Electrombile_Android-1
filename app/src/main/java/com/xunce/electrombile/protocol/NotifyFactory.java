package com.xunce.electrombile.protocol;

/**
 * Created by lybvinci on 16/4/9.
 */
public class NotifyFactory implements ProtocolFactoryInterface {

    @Override
    public Protocol createProtocol(String jsonString) {
        return new NotifyProtocol(jsonString);
    }
}
