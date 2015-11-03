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
import com.xunce.electrombile.Constants.ActivityConstants;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.activity.FragmentActivity;
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

    public MyReceiver(Context context) {
        mContext = context;
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
        Log.i(TAG, "接收调用");
        Log.i(TAG, intent.getExtras().toString());
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
                    ((FragmentActivity) mContext).maptabFragment.cancelWaitTimeOut();
                    onGPSArrived(protocol);
                } else if (destinationName.contains("433")) {
                    select = 0x03;
                    protocol = createFactory(select, s);
                    // 找车
                    on433Arrived(protocol);
                }

            } else if (callbackAction.equals(ActivityConstants.onConnectionLost)) {
                ToastUtils.showShort(mContext, "网络连接已断开");
            }
        }
    }


    private void on433Arrived(Protocol protocol) {
        int intensity = protocol.getIntensity();
        caseSeekSendToFindAct(intensity);
    }

    private void onCmdArrived(Protocol protocol) {
        int cmd = protocol.getCmd();
        int result = protocol.getResult();
        timeHandler.removeMessages(ProtocolConstants.TIME_OUT);
        switch (cmd) {
            //如果是设置围栏的命令
            case ProtocolConstants.CMD_FENCE_ON:
                ((FragmentActivity) mContext).switchFragment.cancelWaitTimeOut();
                caseFence(result, true, "防盗开启成功");
                break;
            //如果是设置关闭围栏的命令
            case ProtocolConstants.CMD_FENCE_OFF:
                ((FragmentActivity) mContext).switchFragment.cancelWaitTimeOut();
                caseFence(result, false, "防盗关闭成功");
                break;
            //如果是获取围栏的命令
            case ProtocolConstants.CMD_FENCE_GET:
                caseFenceGet(protocol, result);
                break;
            //如果是开始找车的命令
            case ProtocolConstants.CMD_SEEK_ON:
                caseSeek(result, "开始找车");
                break;
            //如果是停止找车的命令
            case ProtocolConstants.CMD_SEEK_OFF:
                caseSeek(result, "停止找车");
                break;
            case ProtocolConstants.CMD_LOCATION:
                caseGetGPS(result);
            default:
                break;
        }
    }

    private void caseGetGPS(int result) {
        ((FragmentActivity) mContext).maptabFragment.cancelWaitTimeOut();
        dealErr(result);
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
        Intent intent7 = new Intent();
        intent7.putExtra("intensity", value);
        intent7.setAction("com.xunce.electrombile.find");
        mContext.sendBroadcast(intent7);
    }

    private void caseFenceGet(Protocol protocol, int result) {
        if (ProtocolConstants.ERR_SUCCESS == result) {
            int state = protocol.getState();
            if (ProtocolConstants.ON == state) {
                ((FragmentActivity) mContext).setManager.setAlarmFlag(true);
                ((FragmentActivity) mContext).switchFragment.openStateAlarmBtn();
            } else if (ProtocolConstants.OFF == state) {
                ((FragmentActivity) mContext).setManager.setAlarmFlag(false);
                ((FragmentActivity) mContext).switchFragment.closeStateAlarmBtn();
            }
            ToastUtils.showShort(mContext, "查询状态成功");
        } else {
            dealErr(result);
        }
    }

    private void caseFence(int result, boolean successAlarmFlag, String success) {
        if (ProtocolConstants.ERR_SUCCESS == result) {
            ((FragmentActivity) mContext).setManager.setAlarmFlag(successAlarmFlag);
            ((FragmentActivity) mContext).switchFragment.msgSuccessArrived();
            ToastUtils.showShort(mContext, success);
        } else {
            dealErr(result);
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
                break;
            case ProtocolConstants.ERR_INTERNAL:
                ToastUtils.showShort(mContext, "服务器内部错误，请稍后再试。");
                break;
        }
    }

    private void onGPSArrived(Protocol protocol) {
        float Flat = protocol.getLat();
        float Flong = protocol.getLng();
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        TracksManager.TrackPoint trackPoint = null;
        trackPoint = new TracksManager.TrackPoint(curDate, ((FragmentActivity) mContext).mCenter.convertPoint(new LatLng(Flat, Flong)));
        LogUtil.log.i("保存数据1");
        ((FragmentActivity) mContext).setManager.setInitLocation(Flat + "", Flong + "");
        if (trackPoint != null) {
            if (!((FragmentActivity) mContext).maptabFragment.isPlaying) {
                ((FragmentActivity) mContext).maptabFragment.locateMobile(trackPoint);
            }
            ((FragmentActivity) mContext).switchFragment.reverserGeoCedec(trackPoint.point);
        }
    }
}
