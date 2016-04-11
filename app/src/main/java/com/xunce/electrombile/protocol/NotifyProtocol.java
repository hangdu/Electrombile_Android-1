package com.xunce.electrombile.protocol;


import com.xunce.electrombile.Constants.ProtocolConstants;

/**
 * Created by lybvinci on 16/4/9.
 */
public class NotifyProtocol extends Protocol{

    protected int notify;

    public NotifyProtocol(String tmp) {
        super(tmp);
    }

    @Override
    public int getNotify() {
        String temp = keyForValue(ProtocolConstants.NOTIFY);
        if (isEmpty(temp)) return -1;
        notify = Integer.parseInt(temp);
        return notify;
    }

    @Override
    public NotifyAutolockData getData(){
        String temp = keyForValue(ProtocolConstants.DATA);
        if (isEmpty(temp)) return null;
        long long_timestamp = Long.parseLong(keyForValue(ProtocolConstants.TIMESTAMP, temp));
        int lock = Integer.parseInt(keyForValue(ProtocolConstants.LOCK, temp));
        if(lock!=1){
            return null;
        }
        return new NotifyAutolockData(long_timestamp,lock);
    }

    public class NotifyAutolockData{
        public long Timestamp;
        public int Lockstatus;
        public NotifyAutolockData(long timestamp,int lockstatus){
            this.Timestamp = timestamp;
            this.Lockstatus = lockstatus;
        }
    }
}
