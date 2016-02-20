package com.xunce.electrombile.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

public class MqttService extends Service {

    public MqttAndroidClient mac;
    SettingManager settingManager;
    MqttConnectOptions mcp;
    Connection connection;
    public MqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 开始执行后台任务
                getMqttConnection();
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }


    public void getMqttConnection() {
        connection = Connection.createConnection(ServiceConstants.clientId,
                ServiceConstants.MQTT_HOST,
                ServiceConstants.PORT,
                getApplicationContext(),
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
//                ToastUtils.showShort(mcontext, "mqtt连接断开,正在重连中");
                //设置重连
//                com.orhanobut.logger.Logger.w("这是回调方法中尝试重连");
                if (mac != null) {
                    reMqttConnect();
                }
                else{
//                    ToastUtils.showShort(mcontext, "mac为空");
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
//                    onMqttConnectListener.MqttConnectSuccess();
                    //在这里发送广播  然后在FragmentActivity里收到广播
                    Intent intent = new Intent();
                    intent.putExtra("MQTT_connection_msg", true);
                    sendBroadcast(intent);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    ToastUtils.showShort(mcontext, "mac非空,服务器连接失败");
//                    onMqttConnectListener.MqttConnectFail();
                    Intent intent = new Intent();
                    intent.setAction("polly.liu.Image");//用隐式意图来启动广播
                    intent.putExtra("MQTT_connection_msg", false);
                    sendBroadcast(intent);
                }
            });
            Connections.getInstance(getApplicationContext()).addConnection(connection);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

    private void reMqttConnect(){
        try {
            mac.connect(mcp, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    ToastUtils.showShort(mcontext, "mac非空,服务器连接成功");
//                    onMqttConnectListener.MqttConnectSuccess();
                    //在这里发送广播   然后在Fragment里接受广播
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    ToastUtils.showShort(mcontext, "mac非空,服务器连接失败");
//                    onMqttConnectListener.MqttConnectFail();
                }
            });
            Connections.getInstance(getApplicationContext()).addConnection(connection);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

}
