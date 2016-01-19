package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.util.List;

public class InputIMEIActivity extends Activity {

    private EditText et_IMEI;
    private Button btn_Sure;
    private String IMEI;
    private ProgressDialog progressDialog;
    private SettingManager settingManager;
    private String FromActivity;
    private enum handler_key{
        START_BIND,
        SUCCESS,
        FAILED,
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case SUCCESS:
                    settingManager.cleanDevice();
                    settingManager.setIMEI(IMEI);
                    ToastUtils.showShort(InputIMEIActivity.this, "设备登陆成功");
                    progressDialog.cancel();

                    if(FromActivity.equals("CarManageActivity")){
                        Intent intent = new Intent(InputIMEIActivity.this,FragmentActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(InputIMEIActivity.this,WelcomeActivity.class);
                        startActivity(intent);
                    }
                    finish();
                    break;
                case FAILED:
//                    times = 0;
                    progressDialog.cancel();
                    ToastUtils.showShort(InputIMEIActivity.this, msg.obj.toString());
//                    onResume();
                    break;
            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_imei);
        initView();
        initEvent();
    }

    private void initView(){
        et_IMEI = (EditText)findViewById(R.id.et_IMEI);
        btn_Sure = (Button)findViewById(R.id.btn_Sure);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("连接中，请稍候...");

        settingManager = new SettingManager(InputIMEIActivity.this);
    }

    private void initEvent(){
        btn_Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMEI = et_IMEI.getText().toString();
                if (IMEI.equals("")) {
                    ToastUtils.showShort(InputIMEIActivity.this, "设备号不能为空");
                } else if (IMEI.length() != 16) {
                    ToastUtils.showShort(InputIMEIActivity.this, "设备号的长度不对");
                } else {
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
        final AVObject bindDevice = new AVObject("Bindings");
        final AVUser currentUser = AVUser.getCurrentUser();
        bindDevice.put("user", currentUser);
        AVQuery<AVObject> query = new AVQuery<>("DID");
        final AVQuery<AVObject> queryBinding = new AVQuery<>("Bindings");
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
