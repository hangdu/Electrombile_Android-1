package com.xunce.electrombile.activity.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgetPassActivity2 extends Activity {

    private EditText et_PhoneNumber;
    private Button btn_NextStep;
    private String phone;
    ProgressDialog dialog;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case TOAST:
                    dialog.cancel();
                    ToastUtils.showShort(ForgetPassActivity2.this, (String) msg.obj);
                    if(msg.obj.toString().equals("发送成功")){
                        //跳转到下一个页面
                        Intent intent = new Intent(ForgetPassActivity2.this,ResetPassActivity.class);
                        intent.putExtra("phone",et_PhoneNumber.getText().toString());
                        startActivity(intent);
                        Log.d("test", "test");
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass2);
        initView();
        initEvent();
    }

    private void initView(){
        et_PhoneNumber = (EditText)findViewById(R.id.et_PhoneNumber);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);
        dialog = new ProgressDialog(ForgetPassActivity2.this);
        dialog.setMessage("处理中，请稍候...");
    }

    private void initEvent(){
        btn_NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = et_PhoneNumber.getText().toString().trim();
                if (StringUtils.isEmpty(phone)) {
                    ToastUtils.showShort(ForgetPassActivity2.this, "手机号码不能为空");
                    return;
                }
                if (phone.length() != 11) {
                    ToastUtils.showShort(ForgetPassActivity2.this, "手机号码的长度不对");
                    return;
                }
                if (!isMobileNO(phone)) {
                    ToastUtils.showShort(ForgetPassActivity2.this, "手机号码不正确");
                    return;
                }
                sendVerifyCode(phone);
            }
        });
    }

    private void sendVerifyCode(final String phone) {
        dialog.show();
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
                    if(e.getCode() == 213){
                        //不存在该用户
                        msg.obj = "不存在该用户";
                    }
                    handler.sendMessage(msg);
                }
            }
        });
    }

    enum handler_key{
        TOAST,
    };

    public boolean isMobileNO(String MobileNumber){
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,2,5-9]))\\d{8}$");
        Matcher m = p.matcher(MobileNumber);
        return m.matches();
    }
}
