package com.xunce.electrombile.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.xunce.electrombile.R;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.util.List;

public class BindingActivity extends BaseActivity implements View.OnClickListener {
    private Button bind_btn;
    private TextView jump_bind;

    //passCode
  //  private EditText et_passCode;

    //IMEI
    private EditText et_did;

    //bindSuccess
    private Button bindSuccess;

    //String
    private String IMEI;
 //   private String passcode;
    /** The progress dialog. */
    private ProgressDialog progressDialog;
    //自动重登的次数
    private int times = 0;
//绑定步骤：
    /*
    * 1.先 获取passcede 和 IMEI  进行startBind
    * 2.接着回调 didBindDevice 如果成功getBoundDevices函数，进行搜索
    * 3.接着回调 didDiscovered 如果成功，会获取一个设备列表
    * 4.接着调用 loginDevice 登陆设备，如果成功 会回调 didlogin函数。
    * 5.接着跳转界面
    */
    //handler
    Handler mHandler = new Handler(){
      public void handleMessage(Message msg){
          super.handleMessage(msg);
          handler_key key = handler_key.values()[msg.what];
          switch (key){
              case START_BIND:
                  progressDialog.show();
                  startBind(IMEI);
                  //超时设置
                  timeOut();
                  break;
              case SUCCESS:
                  setManager.cleanDevice();
                  setManager.setIMEI(IMEI);
                  ToastUtils.showShort(BindingActivity.this, "设备登陆成功");
                  progressDialog.cancel();
                  Intent intent = new Intent(BindingActivity.this,FragmentActivity.class);
                  startActivity(intent);
                  finish();
                  break;
              case FAILED:
                  times = 0;
                  progressDialog.cancel();
                  ToastUtils.showShort(BindingActivity.this, msg.obj.toString());
                  break;
          }
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle(getString(R.string.bindEquipment));
        setContentView(R.layout.activity_binding);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews(){
        bind_btn = (Button) findViewById(R.id.bind_btn);
        jump_bind = (TextView) findViewById(R.id.jump_bind);

        et_did = (EditText) findViewById(R.id.et_did);
        //et_passCode = (EditText) findViewById(R.id.et_passCode);

        bindSuccess = (Button) findViewById(R.id.bindSuccess);
        jump_bind.setOnClickListener(this);
        bind_btn.setOnClickListener(this);
        bindSuccess.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("连接中，请稍候...");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bind_btn:
                Intent intent1 = new Intent(BindingActivity.this,ScannerActivity.class);
                startActivityForResult(intent1, 0x01);
                break;
            case R.id.jump_bind:
                Intent intent2 = new Intent(BindingActivity.this, FragmentActivity.class);
                startActivity(intent2);
                this.finish();
                break;
            case R.id.bindSuccess:
                if( et_did != null){
                    IMEI = et_did.getText().toString();
                    mHandler.sendEmptyMessage(handler_key.START_BIND.ordinal());
                }
                break;
            default:break;
        }
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
                                Message message = new Message();
                                message.what = handler_key.FAILED.ordinal();
                                message.obj = "设备已经被绑定！";
                                mHandler.sendMessage(message);
                                return;
                            }
                            bindDevice.put("device", avObjects.get(0));
                            if (list.size() > 0) {
                                bindDevice.put("isAdmin", false);
                                ToastUtils.showShort(BindingActivity.this, "您正在绑定附属车辆...");
                            } else {
                                bindDevice.put("isAdmin", true);
                                ToastUtils.showShort(BindingActivity.this, "您正在绑定主车辆...");
                            }
                            bindDevice.put("IMEI", IMEI);
                            bindDevice.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        mHandler.sendEmptyMessage(handler_key.SUCCESS.ordinal());
                                    } else {
                                        Log.d("失败", "绑定错误: " + e.getMessage());
                                        Message message = new Message();
                                        message.what = handler_key.FAILED.ordinal();
                                        message.obj = e.getMessage();
                                        mHandler.sendMessage(message);
                                    }
                                }
                            });

                        }
                    });


                } else {
                    Message message = new Message();
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
      //  mCenter.cBindDevice(setManager.getUid(),setManager.getToken(),IMEI,passcode,"");
    }

    /**
     * 扫描结果处理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == 0x02 && data != null) {
            if (data.getExtras().containsKey("result")) {
                String text = data.getExtras().getString("result");
                if (text.contains("IMEI")) {
                    IMEI = JSONUtils.ParseJSON(text,"IMEI");
                    et_did.setText(IMEI);
                    setManager.setIMEI(IMEI);
                  //  setManager.setPassCode(passcode);
                  //  et_passCode.setText(passcode);
                    mHandler.sendEmptyMessage(handler_key.START_BIND.ordinal());
                }
            }else{
                ToastUtils.showShort(BindingActivity.this, "扫描失败，请重新扫描！");
            }
        }else{
            ToastUtils.showShort(BindingActivity.this, "扫描失败，请重新扫描！");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressDialog.cancel();
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

    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            if(builder == null) {
                builder = NetworkUtils.networkDialogNoCancel(this);
            }else{
                builder.show();
            }
        }else{
            builder = null;
        }
    }

    private enum handler_key {
        START_BIND,
        SUCCESS,
        FAILED,
    }
}
