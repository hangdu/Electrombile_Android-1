package com.xunce.electrombile.activity.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BindingActivity2;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.Timer;
import java.util.TimerTask;


public class SMSandPasswordActivity extends Activity {
    private TextView tv_Current_Phone;
    private TextView tv_leftsecond;
    private EditText et_SMSCode;
    private TextView btn_ResendSysCode;
    private EditText et_Password;
    private Button btn_NextStep;

    private Timer timer;
    private int secondleft;
    private String phone;
    private String password;
    private TextView title;


    ProgressDialog dialog;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case TICK_TIME:
                    secondleft--;
                    if (secondleft <= 0) {
                        timer.cancel();
//                        btn_ResendSysCode.setEnabled(true);
//                        btn_ResendSysCode.setTextColor(Color.parseColor("#1dcf94"));
                        changeButtonState(true);
                        tv_leftsecond.setText("60");
                    } else {
                        tv_leftsecond.setText(secondleft + "");
                        btn_ResendSysCode.setTextColor(Color.parseColor("#8b8b8b"));
                    }
                    break;

                case TOAST:
                    dialog.cancel();
                    ToastUtils.showShort(SMSandPasswordActivity.this, (String) msg.obj);
                    break;

                case REG_SUCCESS:
                    ToastUtils.showShort(SMSandPasswordActivity.this, (String) msg.obj);
                    break;

                case UPDATEPASS_SUCCESS:
                    ToastUtils.showShort(SMSandPasswordActivity.this, "成功修改密码");
                    //进入绑定设备的页面
                    Intent intent = new Intent(SMSandPasswordActivity.this, BindingActivity2.class);
                    intent.putExtra("From","SMSandPasswordActivity");
                    startActivity(intent);
                    break;

                case LOGIN_TIMEOUT:
                    handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
                    Toast.makeText(SMSandPasswordActivity.this, "请检查网络连接！", Toast.LENGTH_SHORT).show();
//                    dialog.cancel();
                    break;

            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_password);
    }

    @Override
    protected void onStart(){
        super.onStart();
        initView();
        initEvent();
    }

    public void initView(){
        tv_Current_Phone = (TextView)findViewById(R.id.tv_Current_Phone);
        et_SMSCode = (EditText)findViewById(R.id.et_SMSCode);
        btn_ResendSysCode = (TextView)findViewById(R.id.btn_ResendSysCode);
        et_Password = (EditText)findViewById(R.id.et_Password);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);
        tv_leftsecond = (TextView)findViewById(R.id.tv_leftsecond);
        title = (TextView)findViewById(R.id.title);

        title.setText("注册密码");

        btn_ResendSysCode.setEnabled(false);
        timer = new Timer();
//        settingManager = new SettingManager(SMSandPasswordActivity.this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("处理中，请稍候...");
    }

    public void initEvent(){
        //获取到当前手机号
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        tv_Current_Phone.setText("当前手机号:"+phone);

        //开始60s倒计时
        secondleft = 60;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 1000);

        btn_NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断验证码是否为空
                String code = et_SMSCode.getText().toString().trim();
                if ("".equals(code)) {
                    ToastUtils.showShort(getApplicationContext(), "验证码不能为空!");
                    return;
                }

                //判断密码是否符合要求
                if (!PasswordOK()) {
                    return;
                }

                //判断验证码是否正确
                AVUser.verifyMobilePhoneInBackground(code, new AVMobilePhoneVerifyCallback() {
                    @Override
                    public void done(AVException e) {
                        Message msg = Message.obtain();
                        //验证码错误
                        if (e != null) {
                            msg.what = handler_key.TOAST.ordinal();
                            msg.obj = "验证失败";
                            handler.sendMessage(msg);
                        } else {
                            //成功验证,接下来登陆进去更新密码
                            LoginInandUpdatePass();
                        }
                    }
                });
            }
        });

        //重新获取验证码
        btn_ResendSysCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegetSysCode();
            }
        });
    }

    private void RegetSysCode(){
        dialog.show();
        changeButtonState(false);
        secondleft = 60;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 1000);
        //此方法会再次发送验证短信
        AVUser.requestMobilePhoneVerifyInBackground(phone, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                Message msg = Message.obtain();
                if (e == null) {
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                } else {
                    LogUtil.log.e("注册" + e.toString());
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送失败";
                }
                handler.sendMessage(msg);
            }
        });
    }

    private void  changeButtonState(Boolean canclicked){
        if(canclicked == false){
            btn_ResendSysCode.setEnabled(false);
            btn_ResendSysCode.setTextColor(Color.parseColor("#8b8b8b"));
        }
        else{
            btn_ResendSysCode.setEnabled(true);
            btn_ResendSysCode.setTextColor(Color.parseColor("#1dcf94"));
        }
    }

    //先完成了登录才可以修改密码
    void LoginInandUpdatePass(){
        AVUser.logInInBackground(phone, "123456", new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e != null) {
                    LogUtil.log.i(e.toString());
//                    if (AVException.CONNECTION_FAILED != e.getCode()) {
//                        handler.sendEmptyMessage(handler_key.LOGIN_TIMEOUT.ordinal());
//                    }
//                    else {
//                        handler.sendEmptyMessage(handler_key.LOGIN_TIMEOUT.ordinal());
//                    }
                    Message msg = Message.obtain();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = e.toString();
                    handler.sendMessage(msg);
                }

                //登录成功
                else{
                    //修改密码
                    avUser.updatePasswordInBackground("123456", password, new UpdatePasswordCallback() {
                        @Override
                        public void done(AVException e) {
                            Message msg = Message.obtain();
                            if(e == null){
                                msg.what = handler_key.UPDATEPASS_SUCCESS.ordinal();
                            }
                            else{
                                msg.what = handler_key.UPDATEPASS_ERROR.ordinal();
                            }
                            handler.sendMessage(msg);
                        }
                    });
                }
            }
        });
    }

    private Boolean PasswordOK(){
        password = et_Password.getText().toString();
        if (password.contains(" ")) {
            Toast.makeText(this, "密码不能有空格", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6 || password.length() > 16) {
            Toast.makeText(this, "密码长度应为6~16", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private enum handler_key {
        TICK_TIME,
        REG_SUCCESS,
        TOAST,
        LOGIN_FAIL,
        LOGIN_TIMEOUT,
        UPDATEPASS_ERROR,
        UPDATEPASS_SUCCESS,
    }
}
