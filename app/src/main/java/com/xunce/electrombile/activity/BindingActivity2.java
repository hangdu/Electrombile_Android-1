package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.covics.zxingscanner.OnDecodeCompletionListener;
import com.covics.zxingscanner.ScannerView;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;

import org.json.JSONException;

import java.util.List;

public class BindingActivity2 extends Activity implements OnDecodeCompletionListener {
    private ScannerView scannerView;
    private Button btn_InputIMEI;
    private Button btn_BuyProduct;
    private String IMEI;
    private ProgressDialog progressDialog;
    private SettingManager settingManager;
    private String FromActivity;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case START_BIND:
                    progressDialog.show();
                    startBind(IMEI);
                    //超时设置
                    timeOut();
                    break;

                case SUCCESS:
                    settingManager.cleanDevice();
                    settingManager.setIMEI(IMEI);
                    ToastUtils.showShort(BindingActivity2.this, "设备登陆成功");
                    progressDialog.cancel();

                    if(FromActivity.equals("CarManageActivity")){
                        Intent intent = new Intent(BindingActivity2.this,FragmentActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(BindingActivity2.this,WelcomeActivity.class);
                        startActivity(intent);
                    }
                    finish();
                    break;
                case FAILED:
//                    times = 0;
                    progressDialog.cancel();
                    ToastUtils.showShort(BindingActivity2.this, msg.obj.toString());
                    break;
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding2);
        initView();
        initEvent();
    }
    private void initView() {
        scannerView = (ScannerView) findViewById(R.id.scanner_view);
        btn_InputIMEI = (Button)findViewById(R.id.btn_InputIMEI);
        btn_BuyProduct = (Button)findViewById(R.id.btn_BuyProduct);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("连接中，请稍候...");
        settingManager = new SettingManager(BindingActivity2.this);
    }

    private void initEvent(){
        scannerView.setOnDecodeListener(this);
        btn_InputIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BindingActivity2.this, InputIMEIActivity.class);
                intent.putExtra("From",FromActivity);
                startActivity(intent);
            }
        });

        //解析是从哪个activity跳转过来的
        Intent intent = getIntent();
        FromActivity = intent.getStringExtra("From");
    }

    @Override
    public void onDecodeCompletion(String barcodeFormat, String barcode, Bitmap bitmap) {
        if (barcode != null) {
            if (barcode.contains("IMEI")) {
                try {
                    IMEI = JSONUtils.ParseJSON(barcode, "IMEI");
                } catch (JSONException e) {
                    ToastUtils.showShort(BindingActivity2.this, "扫描失败，请重新扫描！");
                    e.printStackTrace();
                    return;
                }
//                setManager.setIMEI(IMEI);
                mHandler.sendEmptyMessage(handler_key.START_BIND.ordinal());
            }
        }
        else{
            //扫描失败
            ToastUtils.showShort(BindingActivity2.this, "扫描失败，请重新扫描！");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        scannerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.onPause();
    }

    private enum handler_key{
        START_BIND,
        SUCCESS,
        FAILED,
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
                                ToastUtils.showShort(BindingActivity2.this, "您正在绑定附属车辆...");
                            } else {
                                bindDevice.put("isAdmin", true);
                                ToastUtils.showShort(BindingActivity2.this, "您正在绑定主车辆...");
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
                    android.os.Message message = new Message();
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
