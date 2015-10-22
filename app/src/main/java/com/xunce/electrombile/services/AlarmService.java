package com.xunce.electrombile.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.xunce.electrombile.receiver.AlarmReceiver;

public class AlarmService extends Service {
    private AlarmReceiver receiver;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
