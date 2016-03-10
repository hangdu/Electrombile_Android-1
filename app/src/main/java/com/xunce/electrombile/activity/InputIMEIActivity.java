package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.LeancloudManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.IntentUtils;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class InputIMEIActivity extends Activity {
    private EditText et_IMEI;
    private Button btn_Sure;
    private String IMEI;
    private ProgressDialog progressDialog;
    private SettingManager settingManager;
    private String FromActivity;
    private String previous_IMEI;
    private MqttConnectManager mqttConnectManager;
    public CmdCenter mCenter;
    private List<String> IMEIlist;
    private LeancloudManager leancloudManager;
    private enum handler_key{
        START_BIND,
        SUCCESS,
        FAILED,
        BIND_DEVICE_NUMBER,
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case SUCCESS:
                    previous_IMEI = settingManager.getIMEI();
                    settingManager.cleanDevice();
                    settingManager.setIMEI(IMEI);
                    ToastUtils.showShort(InputIMEIActivity.this, "设备登陆成功");
                    progressDialog.cancel();
                    getAlarmStatus();
                    break;
                case FAILED:
                    progressDialog.cancel();
                    ToastUtils.showShort(InputIMEIActivity.this, msg.obj.toString());
                    break;

                case BIND_DEVICE_NUMBER:
                    int BindedDeviceNum = (int)msg.obj;
                    getAlarmStatus_next(BindedDeviceNum);
                    break;
            }
        }

    };


    public void gotoAct(){
        if(FromActivity.equals("CarManageActivity")){
            IMEIlist = settingManager.getIMEIlist();
            String IMEI_first = IMEIlist.get(0);
            IMEIlist.add(IMEI_first);

            IMEIlist.set(0, settingManager.getIMEI());
            settingManager.setIMEIlist(IMEIlist);

            //添加设备的话  需要从服务器上获取车辆的头像啊  他们的执行顺序不是你想的那个样子
            leancloudManager.getHeadImageFromServer(settingManager.getIMEI());
            leancloudManager.getCarcreatedAt(settingManager.getIMEI());


            Intent intent = new Intent(InputIMEIActivity.this,FragmentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else{
            IMEIlist = new ArrayList<>();
            IMEIlist.add(settingManager.getIMEI());
            settingManager.setIMEIlist(IMEIlist);

            leancloudManager.getHeadImageFromServer(settingManager.getIMEI());
            Intent intent = new Intent(InputIMEIActivity.this,WelcomeActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void getAlarmStatus(){
        mCenter = CmdCenter.getInstance();
        mqttConnectManager = MqttConnectManager.getInstance();
        if(mqttConnectManager.getMac() == null){
            //说明是fragmentActivity还没有进去  可以在fragmentActivity中再去查询开关状态
            gotoAct();
        }
        else{
            //说明是fragmentActivity已经在栈中
            if(mqttConnectManager.returnMqttStatus()){
                //需要判断这个是不是绑定的第一个设备
                QueryBindList();
            }
            else{
                ToastUtils.showShort(InputIMEIActivity.this,"在Binding中mqtt连接断开了");
            }
        }
    }

    private void QueryBindList(){
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    android.os.Message msg = Message.obtain();
                    msg.what = handler_key.BIND_DEVICE_NUMBER.ordinal();
                    msg.obj = list.size();
                    mHandler.sendMessage(msg);
                } else {
                    e.printStackTrace();
                    ToastUtils.showShort(InputIMEIActivity.this,"查询绑定设备数目出错");
                }
            }
        });
    }

    private void getAlarmStatus_next(int BindedDeviceNum){
        if(1 == BindedDeviceNum){
            //刚刚绑定的就是第一个设备:订阅;查询
            mqttConnectManager.subscribe(settingManager.getIMEI());
            mqttConnectManager.sendMessage(mCenter.cmdFenceGet(), settingManager.getIMEI());

        }
        else if(BindedDeviceNum > 1){
            //
            if(mqttConnectManager.unSubscribe(previous_IMEI)){
                //解订阅成功
                mqttConnectManager.subscribe(settingManager.getIMEI());
                mqttConnectManager.sendMessage(mCenter.cmdFenceGet(), settingManager.getIMEI());
            }
        }
        gotoAct();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_imei);
        initView();
        initEvent();
    }

    private void initView(){
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("输入设备序列号");
        Button btn_back = (Button)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputIMEIActivity.this.finish();
            }
        });

        et_IMEI = (EditText)findViewById(R.id.et_IMEI);
        btn_Sure = (Button)findViewById(R.id.btn_Sure);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("连接中，请稍候...");

        settingManager = SettingManager.getInstance();
    }

    private void initEvent(){
        leancloudManager = LeancloudManager.getInstance();

        btn_Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMEI = et_IMEI.getText().toString();
                if (IMEI.equals("")) {
                    ToastUtils.showShort(InputIMEIActivity.this, "设备号不能为空");
                }
//                else if (IMEI.length() != 16) {
//                    ToastUtils.showShort(InputIMEIActivity.this, "设备号的长度不对");
//                }
                else {
                    //开始查找IMEI
                    progressDialog.show();
                    startBind(IMEI);
                    //超时设置
                    timeOut();
                }
            }
        });

        //解析是从哪个activity跳转过来的
        Intent intent = getIntent();
        FromActivity = intent.getStringExtra("From");
    }

    private void timeOut(){
        new Thread() {
            public void run() {
                try {
                    sleep(10000);
                    if(progressDialog.isShowing())
                        mHandler.sendEmptyMessage(handler_key.FAILED.ordinal());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void startBind(final String IMEI){
        final AVQuery<AVObject> queryBinding = new AVQuery<>("Bindings");


        final AVObject bindDevice = new AVObject("Bindings");
        final AVUser currentUser = AVUser.getCurrentUser();
        bindDevice.put("user", currentUser);

        //查询该IMEI号码是否存在
        AVQuery<AVObject> query = new AVQuery<>("DID");
        query.whereEqualTo("IMEI", this.IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(final List<AVObject> avObjects, AVException e) {
                if (e == null && avObjects.size() > 0) {
                    Log.d("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                    queryBinding.whereEqualTo("IMEI", IMEI);
                    queryBinding.whereEqualTo("user", currentUser);
                    queryBinding.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            Log.d("成功", "IMEI查询到" + list.size() + " 条符合条件的数据");
                            //一个设备可以绑定多个用户,但是这个类是唯一的，确保不重复生成相同的对象。
                            if (list.size() > 0) {
                                android.os.Message message = new android.os.Message();
                                message.what = handler_key.FAILED.ordinal();
                                message.obj = "设备已经被绑定！";
                                mHandler.sendMessage(message);
                                return;
                            }
                            bindDevice.put("device", avObjects.get(0));
                            //Log.d("tag",avObjects.get(0));
                            if (list.size() > 0) {
                                bindDevice.put("isAdmin", false);
                                ToastUtils.showShort(InputIMEIActivity.this, "您正在绑定附属车辆...");
                            } else {
                                bindDevice.put("isAdmin", true);
                                ToastUtils.showShort(InputIMEIActivity.this, "您正在绑定主车辆...");
                            }
                            bindDevice.put("IMEI", IMEI);
                            bindDevice.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        mHandler.sendEmptyMessage(handler_key.SUCCESS.ordinal());
                                    } else {
                                        Log.d("失败", "绑定错误: " + e.getMessage());
                                        android.os.Message message = new android.os.Message();
                                        message.what = handler_key.FAILED.ordinal();
                                        message.obj = e.getMessage();
                                        mHandler.sendMessage(message);
                                    }
                                }
                            });

                        }
                    });


                } else {
                    android.os.Message message = new android.os.Message();
                    message.what = handler_key.FAILED.ordinal();
                    if (e != null) {
                        Log.d("失败", "查询错误: " + e.getMessage());
                        message.obj = e.getMessage();
                    } else {
                        message.obj = "查询设备出错！";
                    }
                    mHandler.sendMessage(message);
                }
            }
        });
    }
}
