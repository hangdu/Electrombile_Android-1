package com.xunce.electrombile.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.receiver.AlarmReceiver;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class AlarmService extends Service {
    public SettingManager setManager;
    private AlarmReceiver receiver;
    private MqttAndroidClient mac;

    public AlarmService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setManager = SettingManager.getInstance();
        startMqttClient();
        registerBroadCast();
    }

    private void registerBroadCast() {
        receiver = new AlarmReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000);
        filter.addAction("MqttService.callbackToActivity.v0");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void startMqttClient() {
        if (ServiceConstants.handler.isEmpty()) {
            return;
        }
        Connection connection = Connections.getInstance(this).getConnection(ServiceConstants.handler);
        mac = connection.getClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
