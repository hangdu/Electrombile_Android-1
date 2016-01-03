package com.xunce.electrombile.activity;

import android.content.Context;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by lybvinci on 16/1/2.
 */
public class SwitchManagedCar {
    private Context mcontext;
    private Context mApplicationContext;
    protected SettingManager setManager;
    private String mIMEI_now;
    private String mIMEI_previous;
    public SwitchManagedCar(Context context,Context ApplicationContext,String IMEI_now,String IMEI_previous){
        mcontext = context;
        mApplicationContext = ApplicationContext;
        setManager = new SettingManager(mApplicationContext);
        mIMEI_now = IMEI_now;
        mIMEI_previous = IMEI_previous;

        MqttAndroidClient mac = getMqttAndroidClient();
        boolean isUnSubscribe = unSubscribe(mac);
        if (isUnSubscribe) {
            RefreshSettingManager();
            Historys.finishAct(FragmentActivity.class);
//            reStartFragAct();  这个地方放在类的外面执行
        } else {
            ToastUtils.showShort(mcontext, "清除订阅失败!,请检查网络后再试!");
        }
    }


    /**
     * 返回mqtt客户端
     *
     * @return
     */
    private MqttAndroidClient getMqttAndroidClient() {
        Connection connection = Connections.getInstance(mcontext).getConnection(ServiceConstants.handler);
        return connection.getClient();
    }

    private boolean unSubscribe(MqttAndroidClient mac) {
        //订阅命令字
        String initTopic = setManager.getIMEI();
        String topic1 = "dev2app/" + initTopic + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + initTopic + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + initTopic + "/433";

        String topic4 = "dev2app/" + initTopic + "/alarm";
        String[] topic = {topic1, topic2, topic3, topic4};
        try {
            mac.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(mcontext, "取消订阅失败!请稍后重启再试！");
            return false;
        }
    }

    public void RefreshSettingManager(){
        setManager.cleanDevice();
        setManager.setIMEI(mIMEI_now);
    }
}
