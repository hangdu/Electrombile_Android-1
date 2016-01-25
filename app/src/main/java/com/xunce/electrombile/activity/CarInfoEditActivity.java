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
import com.xunce.electrombile.database.DBManage;
import com.xunce.electrombile.manager.CmdCenter;
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
    MqttConnectManager mqttConnectManager;
    public CmdCenter mCenter;
    String NextCarIMEI;

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
            //1.解订阅, 2.logout 3.跳转到登录界面  并且把之前的activity清空栈???
            if(mqttConnectManager.returnMqttStatus()){
                if(mqttConnectManager.unSubscribe(IMEI,CarInfoEditActivity.this)){
                    AVUser currentUser = AVUser.getCurrentUser();
                    currentUser.logOut();
                    Intent intent = new Intent(CarInfoEditActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    ToastUtils.showShort(CarInfoEditActivity.this,"解订阅失败  但是数据库记录已经删除了");
                }
            }
            else{
                ToastUtils.showShort(CarInfoEditActivity.this,"mqtt连接失败");
            }

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
        NextCarIMEI = intent.getStringExtra("NextCarIMEI");
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
      //解绑的不是最后一辆车
        if(true == Flag_Maincar){
            if(mqttConnectManager.returnMqttStatus()){
                mqttConnectManager.unSubscribe(IMEI,CarInfoEditActivity.this);
                if(!NextCarIMEI.equals("空")){
                    setManager.setIMEI(NextCarIMEI);
                    mqttConnectManager.subscribe(NextCarIMEI, CarInfoEditActivity.this);
                    mqttConnectManager.sendMessage(getApplicationContext(), mCenter.cmdFenceGet(), NextCarIMEI);
                }
                else{
                    Log.d("test","test");
                }

            }
            else{
                ToastUtils.showShort(CarInfoEditActivity.this, "mqtt连接断开");
            }
        }


        Intent intent = new Intent(CarInfoEditActivity.this,CarManageActivity.class);
//        intent = new Intent("com.xunce.electrombile.alarmservice");
//        CarInfoEditActivity.this.stopService(intent);

        intent.putExtra("string_key","设备解绑");
        intent.putExtra("boolean_key", Flag_Maincar);

        setResult(RESULT_OK, intent);
        startActivity(intent);
        finish();
    }


    //设备切换
    private void DeviceChange(){
        //在这里就解订阅原来的设备号,并且订阅新的设备号,然后查询小安宝的开关状态
        mqttConnectManager = MqttConnectManager.getInstance();
        mCenter = CmdCenter.getInstance(this);
        if(mqttConnectManager.returnMqttStatus()){
            //mqtt连接良好
            mqttConnectManager.unSubscribe(setManager.getIMEI(),CarInfoEditActivity.this);
            setManager.setIMEI(IMEI);
            mqttConnectManager.subscribe(IMEI, CarInfoEditActivity.this);
            mqttConnectManager.sendMessage(getApplicationContext(), mCenter.cmdFenceGet(), IMEI);
            ToastUtils.showShort(CarInfoEditActivity.this,"切换成功");
        }
        else{
            ToastUtils.showShort(CarInfoEditActivity.this,"mqtt连接失败");
        }

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
                if (e == null) {
                    //list的size一定是1
                    if (list.size() == 1) {
                        AVObject avObject = list.get(0);
                        avObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    //成功删除了记录
                                    Message msg = Message.obtain();
                                    msg.what = handler_key.DELETE_SUCCESS.ordinal();
                                    mHandler.sendMessage(msg);

                                } else {
                                    ToastUtils.showShort(CarInfoEditActivity.this, e.toString());
                                }
                            }
                        });
                    } else {
                        ToastUtils.showShort(CarInfoEditActivity.this, "list的size一定是1  哪里出错了?");
                    }
                }

            }
        });
    }


    //设备解绑
    private void releaseBinding() {

        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, "网络连接失败");
            progressDialog.dismiss();
            return;
        }
        mqttConnectManager = MqttConnectManager.getInstance();
        mCenter = CmdCenter.getInstance(this);
        //删除本地的数据库文件  待测试
        deleteDatabaseFile();
        QueryBindList();
    }

    //解绑一台设备的时候需要把本地相关的一级和二级数据库文件也删除掉
    private void deleteDatabaseFile(){
        //先删除二级数据库  在删除一级数据库
        //先看一下本地有没有相应的数据库文件
        DBManage dbManage = new DBManage(CarInfoEditActivity.this,IMEI);
        List<String> dateList = dbManage.getAllDateInDateTrackTable();
        dbManage.closeDB();

        String TableName = "IMEI_"+IMEI+".db";
        for(int i = 0;i<dateList.size();i++){
            String SecondTableName = dateList.get(i)+"_IMEI_"+IMEI+".db";
            Boolean deleteSecondTable = getApplication().deleteDatabase(SecondTableName);
            Log.d("test","test");
        }
        Boolean delete = getApplication().deleteDatabase(TableName);
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

                                android.os.Message msg = Message.obtain();
                                msg.what = handler_key.DELETE_RECORD.ordinal();
                                mHandler.sendMessage(msg);

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
                        //两种情况:1.解绑主设备  2.解绑从设备
                        if(setManager.getIMEI().equals(IMEI)){
                            //1.解绑主设备
                            Flag_Maincar = true;

                        }
                        else{
                            //2.解绑从设备
                            Flag_Maincar = false;
                        }

                        android.os.Message msg = Message.obtain();
                        //已经成功解绑
                        msg.what = handler_key.DELETE_RECORD.ordinal();
                        mHandler.sendMessage(msg);
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
