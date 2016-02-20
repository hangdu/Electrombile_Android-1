package com.xunce.electrombile.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

//使用单例模式
/**
 * Created by lybvinci on 16/1/21.
 */
public class MqttConnectManager {
    Context mcontext;
    public MqttAndroidClient mac;
    SettingManager settingManager;
    MqttConnectOptions mcp;
    Connection connection;
    OnMqttConnectListener onMqttConnectListener;

    private MqttConnectManager(){

    }

    private final static MqttConnectManager INSTANCE = new MqttConnectManager();

    public static MqttConnectManager getInstance() {
        return INSTANCE;
    }

    //获取到对象之后首先执行这个函数
    public void setContext(Context context){
        mcontext = context;
    }


    //mqtt的初始连接
    public void getMqttConnection() {
        connection = Connection.createConnection(ServiceConstants.clientId,
                ServiceConstants.MQTT_HOST,
                ServiceConstants.PORT,
                mcontext,
                false);
        ServiceConstants.handler = connection.handle();
        mcp = new MqttConnectOptions();
        /*
         * true :那么在客户机建立连接时，将除去客户机的任何旧预订。当客户机断开连接时，会除去客户机在会话期间创建的任何新预订。
         * false:那么客户机创建的任何预订都会被添加至客户机在连接之前就已存在的所有预订。当客户机断开连接时，所有预订仍保持活动状态。
         * 简单来讲，true的话就是每次连接都要重新订阅，false的话就是不用重新订阅
         */
        mcp.setCleanSession(false);
        connection.addConnectionOptions(mcp);
        mac = connection.getClient();

        //设置监听函数
        mac.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
//                settingManager.setMqttStatus(false);
                ToastUtils.showShort(mcontext,"mqtt连接断开,正在重连中");
                //设置重连
//                com.orhanobut.logger.Logger.w("这是回调方法中尝试重连");
                if (mac != null) {
                    MqttConnect();
                }
                else{
                    ToastUtils.showShort(mcontext, "mac为空");
                }
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//                com.orhanobut.logger.Logger.i("收到MQTT服务器的消息：" + s);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                //publish后会执行到这里
            }
        });
        MqttConnect();
    }


    private void MqttConnect(){
        try {
            mac.connect(mcp, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    ToastUtils.showShort(mcontext, "mac非空,服务器连接成功");
                    onMqttConnectListener.MqttConnectSuccess();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    ToastUtils.showShort(mcontext, "mac非空,服务器连接失败");
                    onMqttConnectListener.MqttConnectFail();
                }
            });
            Connections.getInstance(mcontext).addConnection(connection);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

    public void ReMqttConnect(){
        try {
            mac.connect(mcp, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    ToastUtils.showShort(mcontext, "mac非空,重连服务器连接成功");
//                    onMqttConnectListener.MqttConnectSuccess();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ToastUtils.showShort(mcontext, "mac非空,重连服务器连接失败");
//                    onMqttConnectListener.MqttConnectFail();
                }
            });
            Connections.getInstance(mcontext).addConnection(connection);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

    public interface OnMqttConnectListener {
        void MqttConnectSuccess();
        void MqttConnectFail();
    }

    public void setOnMqttConnectListener(OnMqttConnectListener var1) {
        this.onMqttConnectListener = var1;
    }

    public Boolean returnMqttStatus(){
        if(mac != null&& mac.isConnected()){
                return true;
        }
        else{
            return false;
        }
    }

    public MqttAndroidClient getMac(){
        return mac;
    }


    public void sendMessage(Context context, final byte[] message, final String IMEI) {
        if (mac == null) {
            ToastUtils.showShort(context, "请先连接设备，或等待连接。");
            return;
        }
        else if (!mac.isConnected()) {

        }
        try {
            //向服务器发送命令
            mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String IMEI,Context context){
        //订阅命令字
//        String initTopic = setManager.getIMEI();
        String initTopic = IMEI;
        String topic1 = "dev2app/" + initTopic + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + initTopic + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + initTopic + "/433";
        //订阅报警
        String topic4 = "dev2app/" + initTopic + "/alarm";
        String[] topic = {topic1, topic2, topic3, topic4};
        int[] qos = {ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE};
        try {
            mac.subscribe(topic, qos);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic1);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic2);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic3);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic4);
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(context, "订阅失败!请稍后重启再试！");
        }

    }

    public boolean unSubscribe(String IMEI,Context context) {
        //订阅命令字
        String initTopic = IMEI;
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
            ToastUtils.showShort(context, "取消订阅失败!请稍后重启再试！");
            return false;
        }
    }
}
