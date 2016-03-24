package com.xunce.electrombile.protocol;

import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.manager.SettingManager;
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
    private SettingManager settingManager;


    public CmdProtocol(String tmp) {
        super(tmp);
        settingManager = SettingManager.getInstance();
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
        date.setHours(date.getHours());
        TracksManager.TrackPoint trackPoint = new TracksManager.TrackPoint(date,Double.parseDouble(lat),Double.parseDouble(lng));
        return trackPoint;
    }

    @Override
    public TracksManager.TrackPoint getInitialStatusResult(){
        String temp = keyForValue(ProtocolConstants.RESULT);
        if (isEmpty(temp)) return null;

        //获取gps位置信息
        String gps = keyForValue(ProtocolConstants.GPS,temp);
        String lat = keyForValue(ProtocolConstants.LAT,gps);
        String lng = keyForValue(ProtocolConstants.LNG,gps);
        long long_timestamp = Long.parseLong(keyForValue(ProtocolConstants.TIMESTAMP, gps));
        Date date = new Date(long_timestamp*1000);
        date.setHours(date.getHours());
        TracksManager.TrackPoint trackPoint = new TracksManager.TrackPoint(date,Double.parseDouble(lat),Double.parseDouble(lng));

        //小安宝的开关状态查询
        String lock = keyForValue(ProtocolConstants.LOCK,temp);
        if(lock.equals("true")){
            settingManager.setAlarmFlag(true);
        }
        else{
            settingManager.setAlarmFlag(false);
        }

        //自动落锁的状态查询
        String autolock = keyForValue(ProtocolConstants.AUTOLOCK,temp);
        String isOn = keyForValue(ProtocolConstants.isOn,autolock);
        if(isOn.equals("true")){
            settingManager.setAutoLockStatus(true);
            //获取到自动落锁的时间
            String period = keyForValue(ProtocolConstants.PERIOD,autolock);
            settingManager.setAutoLockTime(Integer.parseInt(period));
        }
        else{
            settingManager.setAutoLockStatus(false);
        }

        //电池的状态查询
        String battery = keyForValue(ProtocolConstants.BATTERY,temp);
        String percent = keyForValue(ProtocolConstants.PERCENT,battery);
        settingManager.setBatteryPercent(Integer.parseInt(percent));

        String miles = keyForValue(ProtocolConstants.MILES,battery);
        settingManager.setMiles(Integer.parseInt(miles));

        return trackPoint;
    }


    @Override
    public Boolean getBatteryInfo(){
        String temp = keyForValue(ProtocolConstants.RESULT);
        if (isEmpty(temp)) return false;

        //获取gps位置信息
        String percent = keyForValue(ProtocolConstants.PERCENT,temp);
        settingManager.setBatteryPercent(Integer.parseInt(percent));

        String miles = keyForValue(ProtocolConstants.MILES,temp);
        settingManager.setMiles(Integer.parseInt(miles));
        return true;
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
