package com.xunce.electrombile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.avos.avoscloud.LogUtil;
import com.baidu.mapapi.model.LatLng;
import com.orhanobut.logger.Logger;
import com.xunce.electrombile.Constants.ActivityConstants;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.activity.Autolock;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.protocol.CmdFactory;
import com.xunce.electrombile.protocol.GPSFactory;
import com.xunce.electrombile.protocol.Protocol;
import com.xunce.electrombile.protocol.ProtocolFactoryInterface;
import com.xunce.electrombile.protocol._433Factory;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.util.Date;

/**
 * Created by lybvinci on 2015/10/22.
 */
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private Context mContext;
    Handler alarmHandler;
    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ToastUtils.showShort(mContext, "设备超时！");
        }
    };
    private String callbackStatus;
    private String callbackAction;
    private String destinationName;
    private byte select = 0;
    private Protocol protocol;
    private SettingManager settingManager;

    public MyReceiver(Context context) {
        mContext = context;
        settingManager = SettingManager.getInstance();
    }

    private Protocol createFactory(byte msg, String jsonString) {
        ProtocolFactoryInterface factory;
        Protocol protocol = null;
        switch (msg) {
            case 0x01:
                LogUtil.log.d("收到CMD");
                factory = new CmdFactory();
                protocol = factory.createProtocol(jsonString);
                break;
            case 0x02:
                LogUtil.log.d("收到GPS");
                factory = new GPSFactory();
                protocol = factory.createProtocol(jsonString);
                break;
            case 0x03:
                LogUtil.log.d("收到找车信息");
                factory = new _433Factory();
                protocol = factory.createProtocol(jsonString);
                break;
            default:
                break;
        }
        return protocol;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.i(TAG, "接收调用");
        Logger.i("接收调用%s",intent.getExtras().toString());
        //Log.i(TAG, intent.getExtras().toString());
        Bundle bundle = intent.getExtras();
        callbackStatus = bundle.get(ActivityConstants.callbackStatus).toString();
        callbackAction = bundle.get(ActivityConstants.callbackAction).toString();
        if (ActivityConstants.OK.equals(callbackStatus)) {
            if (callbackAction.equals(ActivityConstants.messageArrived)) {
                destinationName = bundle.get(ActivityConstants.destinationName).toString();
                String s = bundle.get(ActivityConstants.PARCEL).toString();
                if (destinationName.contains("cmd")) {
                    select = 0x01;
                    protocol = createFactory(select, s);
                    Log.i(TAG, "得到命令字");
                    onCmdArrived(protocol);
                } else if (destinationName.contains("gps")) {
                    select = 0x02;
                    protocol = createFactory(select, s);
                    Log.i(TAG, "得到GPS");
                    ((FragmentActivity) mContext).cancelWaitTimeOut();
                     onGPSArrived(protocol);
                } else if (destinationName.contains("433")) {
                    select = 0x03;
                    protocol = createFactory(select, s);
                    Log.i(TAG, "433找车");
                    on433Arrived(protocol);
                }

            } else if (callbackAction.equals(ActivityConstants.onConnectionLost)) {
                ToastUtils.showShort(mContext, "服务器连接已断开");
                Logger.wtf("服务器连接已断开");
            }
        }
    }


    private void on433Arrived(Protocol protocol) {
        int intensity = protocol.getIntensity();
        caseSeekSendToFindAct(intensity);
    }

    private void onCmdArrived(Protocol protocol) {
        int cmd = protocol.getCmd();
        int code = protocol.getCode();
        int result = protocol.getResult();
        timeHandler.removeMessages(ProtocolConstants.TIME_OUT);

        switch (cmd) {
            //如果是设置围栏的命令
            case ProtocolConstants.CMD_FENCE_ON:
                Message msg = Message.obtain();
                msg.what = 2;
                alarmHandler.sendMessage(msg);
                caseFence(code, true, "防盗开启成功");
                break;

            //如果是设置关闭围栏的命令
            case ProtocolConstants.CMD_FENCE_OFF:
                //新加代码
                Message msg1 = Message.obtain();
                msg1.what = 2;
                alarmHandler.sendMessage(msg1);
                caseFence(code, false, "防盗关闭成功");
                break;

            //如果是获取围栏的命令
            case ProtocolConstants.CMD_FENCE_GET:
                caseFenceGet(code,protocol);
                break;

            //如果是开始找车的命令
            case ProtocolConstants.CMD_SEEK_ON:
                caseSeek(code, "开始找车");
                break;

            //如果是停止找车的命令
            case ProtocolConstants.CMD_SEEK_OFF:
                caseSeek(code, "停止找车");
                break;

            case ProtocolConstants.CMD_LOCATION:
                ((FragmentActivity) mContext).cancelWaitTimeOut();
                caseGetGPS(code,protocol);
                break;

            case ProtocolConstants.APP_CMD_AUTO_LOCK_ON:
                //开启自动落锁
                caseOpenAutoLock(code);
                break;

            case ProtocolConstants.APP_CMD_AUTO_LOCK_OFF:
                caseCloseAutoLock(code);
                break;

            case ProtocolConstants.APP_CMD_AUTO_PERIOD_GET:
                caseGetAutolockPeriod(code, protocol);
                break;

            case ProtocolConstants.APP_CMD_AUTO_PERIOD_SET:
                caseSetAutoLockTime(code);
                break;

            //获取自动落锁的状态
            case ProtocolConstants.APP_CMD_AUTOLOCK_GET:
                caseGetAutoLockStatus(code,protocol);
                break;

            case ProtocolConstants.APP_CMD_BATTERY:
                caseGetBatteryInfo(code,protocol);
                break;

            case ProtocolConstants.APP_CMD_STATUS_GET:
                caseGetInitialStatus(code,protocol);
                break;

            default:
                break;
        }
    }

    //这个函数是主动查询gps的时候执行的函数 后面那个服务器主动上报用的
    private void caseGetGPS(int code,Protocol protocol) {
        switch (code) {
            case ProtocolConstants.ERR_SUCCESS:
                cmdGPSgetresult(protocol);
                return;

            case ProtocolConstants.ERR_WAITING:
                cmdGPSgetresult(protocol);
                return;
            case ProtocolConstants.ERR_OFFLINE:
                ToastUtils.showShort(mContext, "设备不在线，请检查电源。");
                break;
            case ProtocolConstants.ERR_INTERNAL:
                ToastUtils.showShort(mContext, "服务器内部错误，请稍后再试。");
                break;
        }
    }

    private void caseOpenAutoLock(int code){
        //执行fragmentactivity中的函数
        if(0 == code){
            //默认是自动落锁5分钟
            ((FragmentActivity) mContext).sendMessage((FragmentActivity) mContext,
                    ((FragmentActivity) mContext).mCenter.cmdAutolockTimeSet(5), ((FragmentActivity) mContext).setManager.getIMEI());

            ((FragmentActivity)mContext).setManager.setAutoLockStatus(true);
            return;
        }
        dealErr(code);
    }

    private void caseCloseAutoLock(int code){
        if(0 == code){
            ToastUtils.showShort(mContext, "自动落锁关闭");
            ((FragmentActivity) mContext).setManager.setAutoLockStatus(false);
            return;
        }
        dealErr(code);
    }

    private void caseGetAutolockPeriod(int code,Protocol protocol) {
        if (0 == code) {
            int period = protocol.getPeriod();
            ((FragmentActivity) mContext).setManager.setAutoLockTime(period);
            return;
        }
        dealErr(code);
    }

    public void caseSetAutoLockTime(int code){
        if(code == 0){
           //自动落锁时间成功设置之后  把时间写到本地
            ToastUtils.showShort(mContext, "自动落锁成功");
            ((FragmentActivity) mContext).setManager.setAutoLockTime(Autolock.period);

            return;
        }
        dealErr(code);
    }

    public void caseGetAutoLockStatus(int code,Protocol protocol){
        if(code == 0){
            //已经获取到了自动落锁的状态
            int state = protocol.getNewState();
            if(state == 1){
                ToastUtils.showShort(mContext, "自动落锁为打开状态");
                ((FragmentActivity) mContext).setManager.setAutoLockStatus(true);
                //若为打开状态  还要查询到自动落锁的时间
                ((FragmentActivity) mContext).sendMessage((FragmentActivity) mContext,
                        ((FragmentActivity) mContext).mCenter.cmdAutolockTimeGet(), ((FragmentActivity) mContext).setManager.getIMEI());

            }
            else if(state == 0){
                ToastUtils.showShort(mContext, "自动落锁为关闭状态");
                ((FragmentActivity) mContext).setManager.setAutoLockStatus(false);
            }
            return;
        }
        dealErr(code);
    }

    private void caseGetInitialStatus(int code,Protocol protocol){
        if(code == 0){
            TracksManager.TrackPoint trackPoint = protocol.getInitialStatusResult();
            if(trackPoint!=null){
                ToastUtils.showShort(mContext, "设备状态查询成功");
                Date date = trackPoint.time;
                CmdCenter mCenter = CmdCenter.getInstance();
                LatLng bdPoint = mCenter.convertPoint(trackPoint.point);
                trackPoint = new TracksManager.TrackPoint(date,bdPoint);
                ((FragmentActivity) mContext).maptabFragment.locateMobile(trackPoint);

                //设置小安宝的开关状态
                if(settingManager.getAlarmFlag()){
                    ((FragmentActivity) mContext).switchFragment.openStateAlarmBtn();
                    ((FragmentActivity) mContext).switchFragment.showNotification("安全宝防盗系统已启动");
                }
                else{
                    ((FragmentActivity) mContext).switchFragment.closeStateAlarmBtn();
                }

                //设置电池的电量
                ((FragmentActivity) mContext).switchFragment.refreshBatteryInfo();

                //自动落锁的状态设置
//                ((FragmentActivity) mContext).settingsFragment.refreshAutolockStatus();
            }
        }
        else{
            dealErr(code);
        }
    }

    private void caseGetBatteryInfo(int code,Protocol protocol){
        if(code == 0){
            if(!protocol.getBatteryInfo()){
                ToastUtils.showShort(mContext,"获取电量失败");
            }
            else{
                ToastUtils.showShort(mContext,"获取电量成功");
                ((FragmentActivity) mContext).switchFragment.refreshBatteryInfo();
            }
        }
        else{
            dealErr(code);
        }
    }


    private void caseSeek(int result, String success) {
        if (ProtocolConstants.ERR_SUCCESS == result) {
            ToastUtils.showShort(mContext, success);
        } else {
            dealErr(result);
        }
        caseSeekSendToFindAct(0);
    }

    private void caseSeekSendToFindAct(int value) {
        if (value == -1) {
            return;
        }
        Intent intent7 = new Intent();
        intent7.putExtra("intensity", value);
        intent7.setAction("com.xunce.electrombile.find");
        mContext.sendBroadcast(intent7);
    }

    private void caseFenceGet(int code,Protocol protocol) {
        if (ProtocolConstants.ERR_SUCCESS == code) {
            int state = protocol.getNewState();

            if (ProtocolConstants.ON == state) {
                ((FragmentActivity) mContext).setManager.setAlarmFlag(true);

            } else if (ProtocolConstants.OFF == state) {
                ((FragmentActivity) mContext).setManager.setAlarmFlag(false);
            }

            Message msg = Message.obtain();
            msg.what = 4;
            alarmHandler.sendMessage(msg);
            ToastUtils.showShort(mContext, "查询小安宝开关状态成功");
        } else {
            dealErr(code);
        }
    }

    private void caseFence(int code, boolean successAlarmFlag, String success) {
        if (ProtocolConstants.ERR_SUCCESS == code) {
            ((FragmentActivity) mContext).setManager.setAlarmFlag(successAlarmFlag);

            Message msg = Message.obtain();
            msg.what = 3;
            alarmHandler.sendMessage(msg);

            ToastUtils.showShort(mContext, success);
        } else {
            dealErr(code);
        }
    }

    private void dealErr(int result) {
        switch (result) {
            case ProtocolConstants.ERR_WAITING:
                ToastUtils.showShort(mContext, "正在设置命令，请稍后...");
                timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE * 2);
                return;
            case ProtocolConstants.ERR_OFFLINE:
                ToastUtils.showShort(mContext, "设备不在线，请检查电源。");
                ((FragmentActivity) mContext).setManager.setAlarmFlag(false);
                break;
            case ProtocolConstants.ERR_INTERNAL:
                ToastUtils.showShort(mContext, "服务器内部错误，请稍后再试。");
                break;
        }
    }

    private void onGPSArrived(Protocol protocol) {
        float Flat = protocol.getLat();
        float Flong = protocol.getLng();
        if (Flat == -1 || Flong == -1) {
            return;
        }

        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        TracksManager.TrackPoint trackPoint = null;
        trackPoint = new TracksManager.TrackPoint(curDate, ((FragmentActivity) mContext).mCenter.convertPoint(new LatLng(Flat, Flong)));
        LogUtil.log.i("保存数据1");
        ((FragmentActivity) mContext).setManager.setInitLocation(Flat + "", Flong + "");
        if (trackPoint != null) {
            timeHandler.removeMessages(ProtocolConstants.TIME_OUT);
            ((FragmentActivity) mContext).maptabFragment.locateMobile(trackPoint);
        }
    }

    private void cmdGPSgetresult(Protocol protocol){
        TracksManager.TrackPoint trackPoint = protocol.getNewResult();
        if(trackPoint!=null){
            Date date = trackPoint.time;
            CmdCenter mCenter = CmdCenter.getInstance();
            LatLng bdPoint = mCenter.convertPoint(trackPoint.point);
            trackPoint = new TracksManager.TrackPoint(date,bdPoint);
            ((FragmentActivity) mContext).maptabFragment.locateMobile(trackPoint);
        }
    }

    public void setAlarmHandler(Handler AlarmHandler){
        alarmHandler = AlarmHandler;

    }
}