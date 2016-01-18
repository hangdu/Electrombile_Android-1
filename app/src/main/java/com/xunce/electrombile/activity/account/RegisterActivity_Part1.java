package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity_Part1 extends BaseActivity {
    private EditText et_PhoneNumber;
    private Button btn_Back;
    private Button btn_NextStep;
    ProgressDialog dialog;
    public AlertDialog.Builder builder;
    private String phone;
    AVUser user;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case REG_SUCCESS_NOTVALID:
                    dialog.cancel();
                    gotoSMSandPassword();
                    break;

                case TOAST:
                    dialog.cancel();
                    String s = (String) msg.obj;
                    if(s.equals("该用户已经被注册,请直接登录")){
                        ToastUtils.showShort(RegisterActivity_Part1.this, (String) msg.obj);
                        finish();
                    }
                    //发送验证码失败
                    else{
                        ToastUtils.showShort(RegisterActivity_Part1.this, s);
                    }
                    break;

            }
        }
    };


    public void gotoSMSandPassword(){
        Intent intent = new Intent(RegisterActivity_Part1.this, SMSandPasswordActivity.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //这个地方这两句的位置换过来的原因是super.onCreate函数里还执行initView和initEvent  所以在执行之前需要把界面渲染好
        setContentView(R.layout.activity_register_activity__part1);
        super.onCreate(savedInstanceState);
    }

    private enum handler_key {
        REG_SUCCESS_NOTVALID,
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
//        btn_NextStep.setClickable(false);
//        btn_NextStep.setBackgroundResource(R.drawable.btn_switch_selector_1);

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
//        et_PhoneNumber.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.toString().equals("")){
//                    btn_NextStep.setClickable(false);
//                    btn_NextStep.setBackgroundResource(R.drawable.btn_switch_selector_1);
//                }
//                else{
//                    btn_NextStep.setClickable(true);
//                    btn_NextStep.setBackgroundResource(R.drawable.btn_switch_selector_2);
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        btn_NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断手机号是否是对的
                phone = et_PhoneNumber.getText().toString().trim();
                if (StringUtils.isEmpty(phone)) {
                    ToastUtils.showShort(RegisterActivity_Part1.this, "手机号码不能为空");
                    return;
                }
                if (phone.length() != 11) {
                    ToastUtils.showShort(RegisterActivity_Part1.this, "手机号码的长度不对");
                    return;
                }
                if (!isMobileNO(phone)) {
                    ToastUtils.showShort(RegisterActivity_Part1.this, "手机号码不正确");
                    return;
                }

                dialog.show();

                user = new AVUser();
                user.setUsername(phone);
                //先将密码统一写成123456  之后再更新
                user.setPassword("123456");
                user.setMobilePhoneNumber(phone);
                //此方法会注册并且发送验证短信
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(AVException e) {
                        android.os.Message msg = android.os.Message.obtain();
                        if (e == null) {
                            //这个地方需要执行setManager.setPhoneNumber(phone)吗???
                            setManager.setPhoneNumber(phone);
                            ToastUtils.showShort(getApplicationContext(), getString(R.string.registerSuccess));
                            msg.what = handler_key.REG_SUCCESS_NOTVALID.ordinal();
                            handler.sendMessage(msg);
                        } else {
                            LogUtil.log.e("注册" + e.toString());
                            if (e.getCode() == 214) {
                                //有两种情况:1.用户已经注册并且验证过了  2.用户注册了;但是没有验证和重设密码
                                LoginIn();
                            } else {
                                msg.what = handler_key.TOAST.ordinal();
                                msg.obj = "发送验证码失败";
                                handler.sendMessage(msg);
                            }
                        }
                    }
                });
            }
        });
    }

    public void LoginIn() {
        AVUser.logInInBackground(phone, "123456", new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e != null) {
                    android.os.Message msg = android.os.Message.obtain();
                    LogUtil.log.i(e.toString());
                    if(e.getCode() == 210){
                        //该用户已经注册并且验证   但是此处密码错误
                        setManager.setPhoneNumber(phone);
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = "该用户已经被注册,请直接登录";
                        handler.sendMessage(msg);
                    }
//                    if (AVException.CONNECTION_FAILED != e.getCode()) {
////                        handler.sendEmptyMessage(handler_key.LOGIN_FAIL.ordinal());
//                    }
                   else {
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = e.toString();
                        handler.sendEmptyMessage(handler_key.TOAST.ordinal());
                    }
                }
                else{
                    android.os.Message msg = android.os.Message.obtain();
                    //成功登陆:1.没有验证的用户2.已经验证但是密码是123456的用户
                    if (avUser != null) {
                        if (avUser.isMobilePhoneVerified()) {
                            //已经验证的用户   请直接登陆
//                            handler.sendEmptyMessage(handler_key.LOGIN_SUCCESS.ordinal());
                            Log.d("test","test");
                            msg.what = handler_key.TOAST.ordinal();
                            msg.obj = "该用户已经被注册,请直接登录";
                            handler.sendMessage(msg);
                        }
                        else{
                            //没有验证的用户  请继续完成验证
                            getVerifiedCode();
                        }
                    }
                }
            }
        });
    }

    //手机号码已经被注册过  但是没有完成验证
    public void getVerifiedCode() {
        //此方法会再次发送验证短信
        user.setMobilePhoneNumber(user.getUsername());
        AVUser.requestMobilePhoneVerifyInBackground(user.getMobilePhoneNumber(), new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    gotoSMSandPassword();
                }
                else {
                    LogUtil.log.e("phoneNumber" + user.getMobilePhoneNumber() + "code:" + e.getCode() + e.toString());
                    android.os.Message msg = new android.os.Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = e.toString();
                    handler.sendMessage(msg);
                }
            }
        });
    }
}
