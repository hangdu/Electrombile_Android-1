package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity_Part1 extends BaseActivity {
    private EditText et_PhoneNumber;
//    private Button btn_Back;
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
//        btn_Back = (Button)findViewById(R.id.btn_back);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);

        dialog = new ProgressDialog(this);
        dialog.setMessage("处理中，请稍候...");
    }

    @Override
    public void initEvents(){
        btn_NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断手机号是否是对的
                phone = et_PhoneNumber.getText().toString().trim();

                if(!ForgetPassActivity2.IsPhoneNomberOK(phone,RegisterActivity_Part1.this)){
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
                            QueryUser();
                            //有两种情况:1.用户已经注册并且验证过了  2.用户注册了;但是没有验证
                            //此时需要做的是通过手机号 从用户数据库中查询到相应的那条记录,然后看该用户是否验证过
                        }
                    }
                });
            }
        });
    }


    public void QueryUser(){
        AVQuery<AVObject> query = new AVQuery<>("_User");
        query.whereEqualTo("username", phone);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    android.os.Message msg = android.os.Message.obtain();
                    //list中只有一个数据
                    AVUser user = (AVUser) list.get(0);
//                    Boolean MobileNumberVerified = user.isMobilePhoneVerified();
                    if (user.isMobilePhoneVerified()) {
                        //注册过且已经验证
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = "该用户已经被注册,请直接登录";
                        handler.sendMessage(msg);
                    } else {
//                        注册过没验证
                        getVerifiedCode();
                    }
                } else {
                    e.printStackTrace();
                    ToastUtils.showShort(RegisterActivity_Part1.this,e.toString());
                }
            }
        });
    }

    //手机号码已经被注册过  但是没有完成验证
    public void getVerifiedCode() {
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