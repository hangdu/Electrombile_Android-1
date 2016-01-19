package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
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
    Boolean LastCar;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case DELETE_RECORD:
                    DeleteInBindingList();
                    break;

                case DELETE_SUCCESS:
                    AfterDeleteSuccess();
                    break;
            }
        }
    };

    public void AfterDeleteSuccess(){
        if(LastCar == true){
            //跳转到登录界面  并且把之前的activity清空栈
            Intent intent = new Intent(CarInfoEditActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            //回退到车辆管理的界面
            DeviceUnbinded();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);

        Intent intent = getIntent();
        IMEI = intent.getStringExtra("string_key");
        LastCar = false;
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

        intent.putExtra("string_key","设备解绑");
        intent.putExtra("boolean_key",Flag_Maincar);

        setResult(RESULT_OK, intent);
        CarInfoEditActivity.this.finish();
    }


    //设备切换
    private void DeviceChange(){
        Intent intent = new Intent();
        intent.putExtra("string_key","设备切换");
        intent.putExtra("boolean_key", Flag_Maincar);
        setResult(RESULT_OK, intent);
        //解除订阅
        CarInfoEditActivity.this.finish();
    }

    //在Binding数据表里删除一条记录
    public void DeleteInBindingList(){
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        //通过下面这两个约束条件,唯一确定了一条记录
        query.whereEqualTo("IMEI", IMEI);
        query.whereEqualTo("user", AVUser.getCurrentUser());

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if(e == null){
                    //list的size一定是1
                    if(list.size() == 1){
                        AVObject avObject = list.get(0);
                        avObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    //成功删除了记录
                                    Message msg = Message.obtain();
                                    msg.what = handler_key.DELETE_SUCCESS.ordinal();
                                    mHandler.sendMessage(msg);

                                }
                                else{
                                    ToastUtils.showShort(CarInfoEditActivity.this,e.toString());
                                }
                            }
                        });
                    }

                   else{
                        ToastUtils.showShort(CarInfoEditActivity.this, "list的size一定是1  哪里出错了?");
                    }
                }

            }
        });
    }


    private void releaseBinding() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, "网络连接失败");
            progressDialog.dismiss();
            return;
        }
        QueryBindList();
    }

    enum handler_key{
        DELETE_RECORD,
        DELETE_SUCCESS,
    }

    public void QueryBindList(){
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                progressDialog.dismiss();
                if (e == null) {
                    if(0 == list.size()){
                        //不可能出现这种情况
                    }
                    else if(1 == list.size()){
                        //解绑之前只剩下最后一辆车了
                        AlertDialog.Builder builder = new AlertDialog.Builder(CarInfoEditActivity.this);
                        builder.setMessage("因没有绑定设备,将弹出登录界面");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LastCar = true;
                                dialog.dismiss();
                                //解绑该设备号

                                if(unSubscribe()){
                                    android.os.Message msg = Message.obtain();
                                    //已经成功解绑
                                    msg.what = handler_key.DELETE_RECORD.ordinal();
                                    mHandler.sendMessage(msg);
                                }
                                else{
                                    ToastUtils.showShort(CarInfoEditActivity.this,"解绑失败");
                                    return;
                                }
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                return;

                            }
                        });

                        Dialog dialog = builder.create();
                        dialog.show();
                    }

                    else{
                        //解绑之前还剩下至少两辆车
                        //先判断被解绑的设备是否是主设备; 解绑该设备号
                        if(setManager.getIMEI().equals(IMEI)){
                            //现在正在查看的且被解绑的是主车辆
                            Flag_Maincar = true;
                        }
                        else{
                            Flag_Maincar = false;
                        }

                        if(unSubscribe()){
                            android.os.Message msg = Message.obtain();
                            //已经成功解绑
                            msg.what = handler_key.DELETE_RECORD.ordinal();
                            mHandler.sendMessage(msg);
                        }
                        else{
                            ToastUtils.showShort(CarInfoEditActivity.this,"解绑失败");
                            return;
                        }
                    }
                }
                else {
                    e.printStackTrace();
                    ToastUtils.showShort(CarInfoEditActivity.this,e.toString());
                }
            }
        });
    }

    private boolean unSubscribe() {
        Connection connection = Connections.getInstance(CarInfoEditActivity.this).getConnection(ServiceConstants.handler);
        MqttAndroidClient mac = connection.getClient();
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
