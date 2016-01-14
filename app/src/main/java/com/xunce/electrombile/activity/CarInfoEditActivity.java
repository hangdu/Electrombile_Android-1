package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.List;

public class CarInfoEditActivity extends Activity {
    TextView tv_CarIMEI;
    EditText et_CarName;
    Button btn_DeleteDevice;
    String IMEI;
    SettingManager setManager;
    private ProgressDialog progressDialog;
    Button btn_DeviceChange;
    Boolean Flag_Maincar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);

        Intent intent = getIntent();
        IMEI = intent.getStringExtra("string_key");




    }

    @Override
    protected void onStart(){
        super.onStart();
        initView();

    }

    void initView(){
        tv_CarIMEI = (TextView)findViewById(R.id.tv_CarIMEI);
        tv_CarIMEI.setText(IMEI);

        setManager = new SettingManager(CarInfoEditActivity.this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在设置,请稍后");

        et_CarName = (EditText)findViewById(R.id.et_CarName);
        btn_DeleteDevice = (Button)findViewById(R.id.btn_DeleteDevice);
        btn_DeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                releaseBinding();
            }
        });

        //设备切换
        btn_DeviceChange = (Button)findViewById(R.id.btn_DeviceChange);
        btn_DeviceChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceChange();

            }
        });
        Flag_Maincar = JudgeMainCarOrNot();

    }

    //设备解绑
    private void DeviceUnbinded(){
        Intent intent;
        intent = new Intent("com.xunce.electrombile.alarmservice");
        CarInfoEditActivity.this.stopService(intent);
        intent = new Intent();
        //判断正在查看的设备是否是正在被管理的成立


        intent.putExtra("string_key","设备解绑");
        intent.putExtra("boolean_key",Flag_Maincar);

        setResult(RESULT_OK,intent);
        CarInfoEditActivity.this.finish();
    }


    //设备切换
    private void DeviceChange(){
        Intent intent = new Intent();
        intent.putExtra("string_key","设备切换");
        intent.putExtra("boolean_key",Flag_Maincar);
        setResult(RESULT_OK,intent);
        //解除订阅
        CarInfoEditActivity.this.finish();
    }

    private void releaseBinding() {
        //先判断IMEI是否为空，若为空证明没有绑定设备。
//        if (setManager.getIMEI().isEmpty()) {
//            ToastUtils.showShort(this, "未绑定设备");
//            progressDialog.dismiss();
//            return;
//        }
        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, "网络连接失败");
            progressDialog.dismiss();
            return;
        }
        //若不为空，则先查询所在绑定类，再删除，删除成功后取消订阅，并删除本地的IMEI，关闭FragmentActivity,进入绑定页面
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
//        String IMEI = setManager.getIMEI();
        query.whereEqualTo("IMEI", IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null && avObjects.size() > 0) {
                    AVObject bindClass = avObjects.get(0);
                    Connection connection = Connections.getInstance(CarInfoEditActivity.this).getConnection(ServiceConstants.handler);
                    MqttAndroidClient mac = connection.getClient();
                    boolean isUnSubscribe = unSubscribe(mac);

                    if (isUnSubscribe) {
                        bindClass.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    if(Flag_Maincar == true){
                                        setManager.cleanDevice();
                                    }

//                                    ToastUtils.showShort(CarInfoEditActivity.this, "解除绑定成功!");
                                    progressDialog.dismiss();
                                    DeviceUnbinded();
                                }
                            }
                        });
                    }

                } else {
                    if (e != null)
                        Log.d("失败", "问题： " + e.getMessage());
                    ToastUtils.showShort(CarInfoEditActivity.this, "解除绑定失败!");
                    progressDialog.dismiss();
                }
            }
        });
    }

    private boolean unSubscribe(MqttAndroidClient mac) {
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
            ToastUtils.showShort(this, "取消订阅失败!请稍后重启再试！");
            return false;
        }

    }

    //判断正在查看的设备是否是主设备
    Boolean JudgeMainCarOrNot(){
        if(setManager.getIMEI().equals(IMEI)){
            btn_DeviceChange.setVisibility(View.INVISIBLE);
            return true;
        }
        else{
            return false;
        }
    }
}
