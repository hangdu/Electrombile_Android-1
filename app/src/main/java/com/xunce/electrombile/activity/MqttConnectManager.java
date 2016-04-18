package com.xunce.electrombile.activity;

import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.log.MyLog;
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

//使用单例模式
/**
 * Created by lybvinci on 16/1/21.
 */
public class MqttConnectManager {
    Context mcontext;
    public MqttAndroidClient mac;
    MqttConnectOptions mcp;
    Connection connection;
    OnMqttConnectListener onMqttConnectListener;

    public static final String OK = "OK";
    public static final String LOST = "LOST";
    public static final String IS_CONNECTING = "IS_CONNECTING";
    public static final String CONNECTING_FAIL = "CONNECTING_FAIL";
    public static String status = OK;

    private MqttConnectManager(){

    }

    private final static MqttConnectManager INSTANCE = new MqttConnectManager();

    public static MqttConnectManager getInstance() {
        return INSTANCE;
    }

//    获取到对象之后首先执行这个函数
    public void setContext(Context context){
        mcontext = context;
    }

    public void initMqtt(){
        connection = Connection.createConnection(ServiceConstants.clientId,
                ServiceConstants.MQTT_HOST,
                ServiceConstants.PORT,
                mcontext,
                false);
        ServiceConstants.handler = connection.handle();
        Log.d("initMqtt",ServiceConstants.handler);
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
                if(status.equals(OK)||status.equals(CONNECTING_FAIL)){
                    MyLog.d("MqttConnectManager", "connectionLost  正在重连");
                    ToastUtils.showShort(mcontext, "mqtt连接断开,正在重连中");
                    if(mac == null){
                        return;
                    }

                    //设置重连
                    if (!mac.isConnected()&&!status.equals(IS_CONNECTING)) {
                        MyLog.d("MqttConnectManager", "getMqttConnection开始连接");
                        status = IS_CONNECTING;
                        getMqttConnection();
                    }
                    else{
                        ToastUtils.showShort(mcontext, "mac为空 或者 连接好的状态");
                    }
                }
                else if(status.equals(IS_CONNECTING)){
                    //do nothing
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
    }

    public void getMqttConnection(){
        try {
            MyLog.d("getMqttConnection", "1");
            mac.connect(mcp, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    onMqttConnectListener.MqttConnectSuccess();
                    MyLog.d("getMqttConnection", "MqttConnectSuccess 连接服务器成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    onMqttConnectListener.MqttConnectFail();
                    MyLog.d("getMqttConnection", "MqttConnectSuccess 连接服务器失败");
                }
            });
//            MyLog.d("getMqttConnection", "2");
//            Connections.getInstance(mcontext).addConnection(connection);
//            MyLog.d("getMqttConnection", "3");
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

//    public void removeConnectionInDatabase(){
//        Connections.getInstance(mcontext).removeConnection(connection);
//    }

    public void MqttDisconnect(){
        if(returnMqttStatus()){
            try{
                mac.disconnect(this,new IMqttActionListener(){
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        MyLog.d("MqttDisconnect","断开连接成功");
                        mac = null;
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        MyLog.d("MqttDisconnect", "断开连接失败");
                    }

                });
                MyLog.d("MqttDisconnect","MqttDisconnect");
            }catch(MqttException e){
                e.printStackTrace();
            }
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


    public void sendMessage(final byte[] message, final String IMEI) {
        if (mac == null||!mac.isConnected()) {
            ToastUtils.showShort(App.getInstance(), "请先连接设备，或等待连接。");
            return;
        }
        try {
            //向服务器发送命令
            mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String IMEI){
        //订阅命令字
        String topic1 = "dev2app/" + IMEI + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + IMEI + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + IMEI + "/433";
        //订阅报警
        String topic4 = "dev2app/" + IMEI + "/alarm";

        String topic5 = "dev2app/" + IMEI + "/notify";

        String[] topic = {topic1, topic2, topic3, topic4, topic5};
        int[] qos = {ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE,ServiceConstants.MQTT_QUALITY_OF_SERVICE};
        try {
            mac.subscribe(topic, qos);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic1);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic2);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic3);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic4);
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(App.getInstance(), "订阅失败!请稍后重启再试！");
        }
    }

    public boolean unSubscribe(String IMEI) {
        //订阅命令字
        String topic1 = "dev2app/" + IMEI + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + IMEI + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + IMEI + "/433";

        String topic4 = "dev2app/" + IMEI + "/alarm";

        String topic5 = "dev2app/" + IMEI + "/notify";
        String[] topic = {topic1, topic2, topic3, topic4, topic5};
        try {
            mac.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(App.getInstance(), "取消订阅失败!请稍后重启再试！");
            return false;
        }
    }
}
