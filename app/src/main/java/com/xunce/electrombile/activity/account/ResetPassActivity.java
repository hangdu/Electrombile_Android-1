package com.xunce.electrombile.activity.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BindingActivity2;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class ResetPassActivity extends Activity {
    private TextView tv_CurentNumber;
    private EditText et_VerifyCode;
    private Button btn_ResendSysCode;
    private Button btn_NextStep;
    private EditText et_NewPass;
    Timer timer;
    int secondleft;
    private String password;
    ProgressDialog dialog;
    String phone;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {

                case TICK_TIME:
                    secondleft--;
                    if (secondleft <= 0) {
                        timer.cancel();
                        btn_ResendSysCode.setEnabled(true);
                        btn_ResendSysCode.setText("重新获取");
                        btn_ResendSysCode.setBackgroundResource(R.drawable.btn_getverifycode_1_act);
                    } else {
                        btn_ResendSysCode.setText(secondleft + "秒后重新获取");
                    }
                    break;

                case CHANGE_SUCCESS:
                    finish();
                    break;

                case TOAST:
                    ToastUtils.showShort(ResetPassActivity.this, (String) msg.obj);
                    dialog.cancel();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);
        initView();
        initEvent();
    }

    private void initView(){
        tv_CurentNumber = (TextView)findViewById(R.id.tv_CurentNumber);
        et_VerifyCode = (EditText)findViewById(R.id.et_VerifyCode);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);
        et_NewPass = (EditText)findViewById(R.id.et_NewPass);

        btn_ResendSysCode = (Button)findViewById(R.id.btn_ResendSysCode);
        btn_ResendSysCode.setEnabled(false);
        btn_ResendSysCode.setBackgroundResource(R.drawable.btn_getverifycode_2_act);

        timer = new Timer();

        dialog = new ProgressDialog(this);
        dialog.setMessage("处理中，请稍候...");
    }

    private void initEvent(){
        //获取到当前手机号
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        tv_CurentNumber.setText("当前手机号" + phone);

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
                doChangePsw();
            }
        });

        //重新获取验证码
        btn_ResendSysCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerifyCode(phone);

            }
        });

    }

    private void sendVerifyCode(final String phone) {
        dialog.show();
        btn_ResendSysCode.setEnabled(false);
        btn_ResendSysCode.setBackgroundResource(R.drawable.btn_getverifycode_2_act);
        secondleft = 60;
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                //倒计时通知
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 1000);
        //发送请求验证码指令
        AVUser.requestPasswordResetBySmsCodeInBackground(phone, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                    handler.sendMessage(msg);
                } else {
                    LogUtil.log.i(e.toString());
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = e.toString();
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 执行手机号重置密码操作
     */
    private void doChangePsw() {
        String code = et_VerifyCode.getText().toString().trim();
        String password = et_NewPass.getText().toString();

        if (code.length() == 0) {
            Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.contains(" ")) {
            Toast.makeText(this, "密码不能有空格", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6 || password.length() > 16) {
            Toast.makeText(this, "密码长度应为6~16", Toast.LENGTH_SHORT).show();
            return;
        }
        AVUser.resetPasswordBySmsCodeInBackground(code, password, new UpdatePasswordCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "修改成功";
                    handler.sendMessage(msg);
                    handler.sendEmptyMessageDelayed(
                            handler_key.CHANGE_SUCCESS.ordinal(), 2000);
                } else {
                    LogUtil.log.i(e.toString());
                    ToastUtils.showShort(getApplicationContext(), getString(R.string.validatedFailed));
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "修改失败,验证码错误";
                    handler.sendMessage(msg);
                }
            }
        });
        dialog.show();
    }

    enum handler_key{
        TICK_TIME,
        TOAST,
        CHANGE_SUCCESS,

    }
}
