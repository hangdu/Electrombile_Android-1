package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.activity.Message;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity_Part1 extends BaseActivity {
    private EditText et_PhoneNumber;
    private Button btn_Back;
    private Button btn_NextStep;
    ProgressDialog dialog;
    Timer timer;
    public AlertDialog.Builder builder;
    int secondleft;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case TOAST:
                    dialog.cancel();
//                    ToastUtils.showShort(RegisterActivity_Part1.this, (String) msg.obj);
                    String s = (String) msg.obj;
                    if(s.equals("发送验证码成功")){
                        //跳转到下一个页面  填写验证码和密码
                        gotoSMSandPassword();

                    }
                    //发送验证码失败
                    else{
//                        ToastUtils.showShort(RegisterActivity_Part1.this, (String) msg.obj);
                    }

                    dialog.cancel();
                    break;

            }

        }
    };

    public void gotoSMSandPassword(){
        Intent intent = new Intent(RegisterActivity_Part1.this, SMSandPasswordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //这个地方这两句的位置换过来的原因是super.onCreate函数里还执行initView和initEvent  所以在执行之前需要把界面渲染好
        setContentView(R.layout.activity_register_activity__part1);
        super.onCreate(savedInstanceState);
    }

    private enum handler_key {

        /**
         * 倒计时通知
         */
        TICK_TIME,

        /**
         * 注册成功
         */
        REG_SUCCESS,

        /**
         * Toast弹出通知
         */
        TOAST,

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            if (builder == null) {
                builder = NetworkUtils.networkDialogNoCancel(this);
            } else {
                builder.show();
            }
        } else {
            builder = null;
        }

    }

    @Override
    public void initViews() {
        et_PhoneNumber = (EditText)findViewById(R.id.etName);
        btn_Back = (Button)findViewById(R.id.btn_back);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);

        //这句话没有起到应有的作用.....
        btn_NextStep.setClickable(false);
        btn_NextStep.setBackgroundResource(R.drawable.btn_switch_selector_1);

        dialog = new ProgressDialog(this);
        dialog.setMessage("处理中，请稍候...");
    }

    public boolean isMobileNO(String MobileNumber){
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,2,5-9]))\\d{8}$");
        Matcher m = p.matcher(MobileNumber);
        return m.matches();
    }

    @Override
    public void initEvents(){
        et_PhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    btn_NextStep.setClickable(false);
                    btn_NextStep.setBackgroundResource(R.drawable.btn_switch_selector_1);
                }
                else{
                    btn_NextStep.setClickable(true);
                    btn_NextStep.setBackgroundResource(R.drawable.btn_switch_selector_2);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断手机号是否是对的
                final String phone = et_PhoneNumber.getText().toString().trim();
                if(StringUtils.isEmpty(phone)){
                    ToastUtils.showShort(RegisterActivity_Part1.this, "手机号码不能为空");
                    return;
                }
                if (phone.length() != 11) {
                    ToastUtils.showShort(RegisterActivity_Part1.this, "手机号码的长度不对");
                    return;
                }
                if(!isMobileNO(phone)){
                    ToastUtils.showShort(RegisterActivity_Part1.this, "手机号码不正确");
                    return;
                }
                //进行其他的操作

                dialog.show();

                //对于这个倒计时  还没有理解
                secondleft = 60;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
                    }
                }, 1000, 1000);
//                AVUser user = new AVUser();
//                user.setUsername(phone);
//                user.setPassword("123456");
//                user.setMobilePhoneNumber(phone);

                AVOSCloud.requestSMSCodeInBackground(phone, new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        android.os.Message msg = android.os.Message.obtain();
                        if(e == null){
                            //发送短信验证码成功
                            msg.what = handler_key.TOAST.ordinal();
                            msg.obj = "发送验证码成功";
                            handler.sendMessage(msg);
                        }
                        else{
                            LogUtil.log.e("注册" + e.toString());
                            msg.what = handler_key.TOAST.ordinal();
                            msg.obj = "发送验证码失败";
                            handler.sendMessage(msg);
                        }
                    }
                });

            }
        });
    }
}
