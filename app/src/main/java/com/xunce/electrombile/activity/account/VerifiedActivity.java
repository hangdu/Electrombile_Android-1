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

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.IntentUtils;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class VerifiedActivity extends Activity {

    private EditText etInputCode;
    private Button btnReGetCode;
    private Button btnSure;
    private ProgressDialog dialog;
    //验证码重发倒计时
    private int secondLeft = 60;
    //The timer.
    private Timer timer;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case TICK_TIME:
                    secondLeft--;
                    if (secondLeft <= 0) {
                        timer.cancel();
                        btnReGetCode.setEnabled(true);
                        btnReGetCode.setText("重新获取");
                        btnReGetCode.setBackgroundResource(R.drawable.btn_getverifycode_1_act);
                    } else {
                        btnReGetCode.setText(secondLeft + "秒后重新获取");
                    }
                    break;
                case REG_SUCCESS:
                    ToastUtils.showShort(VerifiedActivity.this, (String) msg.obj);
                    dialog.cancel();
                    finish();
                    IntentUtils.getInstance().startActivity(VerifiedActivity.this, FragmentActivity.class);
                    break;
                case TOAST:
                    ToastUtils.showLong(VerifiedActivity.this, (String) msg.obj);
                    dialog.cancel();
                    break;
            }
        }
    };
    private SettingManager settingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verified);
        initView();
        btnReGetCode.performClick();
    }

    private void initView() {
        etInputCode = (EditText) findViewById(R.id.etInputCode);
        btnReGetCode = (Button) findViewById(R.id.btnReGetCode);
        btnSure = (Button) findViewById(R.id.btnReGetCode);
        dialog = new ProgressDialog(this);
        dialog.setMessage("处理中，请稍候...");
        settingManager = new SettingManager(this);
    }

    public void getVerifiedCode(View view) {
        dialog.show();
        btnReGetCode.setEnabled(false);
        btnReGetCode.setBackgroundResource(R.drawable.btn_getverifycode_2_act);
        secondLeft = 60;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 1000);
        //此方法会再次发送验证短信
        final AVUser user = AVUser.getCurrentUser();
        user.setMobilePhoneNumber(user.getUsername());
        AVUser.requestMobilePhoneVerifyInBackground(user.getMobilePhoneNumber(), new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                    handler.sendMessage(msg);
                } else {
                    LogUtil.log.e("phoneNumber"+user.getMobilePhoneNumber()+"code:" +e.getCode()+ e.toString());
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = e.toString();
                    handler.sendMessage(msg);
                }
            }
        });
    }

    public void verified(View view) {
        String code = etInputCode.getText().toString().trim();
        if ("".equals(code)) {
            ToastUtils.showShort(getApplicationContext(), getString(R.string.validateCodeNull));
        } else {
            AVUser.verifyMobilePhoneInBackground(code, new AVMobilePhoneVerifyCallback() {
                @Override
                public void done(AVException e) {
                    Message msg = Message.obtain();
                    if (e == null) {
                        LogUtil.log.i(getString(R.string.validateSuccess));
                        msg.what = handler_key.REG_SUCCESS.ordinal();
                        msg.obj = getString(R.string.validateSuccess);
                    } else if (AVException.CONNECTION_FAILED == e.getCode()) {
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = "网络连接失败";
                    } else {
                        LogUtil.log.d(e.toString());
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = "验证失败";
                    }
                    handler.sendMessage(msg);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        IntentUtils.getInstance().startActivity(this,LoginActivity.class);
        this.finish();
    }

    private enum handler_key {
        //倒计时通知
        TICK_TIME,
        //验证成功
        REG_SUCCESS,
        //Toast弹出通知
        TOAST,

    }
}
