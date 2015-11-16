/**
 * Project Name:XPGSdkV4AppBase
 * File Name:RegisterActivity.java
 * Package Name:com.gizwits.framework.activity.account
 * Date:2015-1-27 14:45:08
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xunce.electrombile.activity.account;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.activity.BindingActivity;
import com.xunce.electrombile.utils.system.IntentUtils;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * ClassName: Class RegisterActivity. <br/>
 * 用户注册，该类用于新用户的注册<br/>
 * 
 * @author Lien
 */

public class RegisterActivity extends BaseActivity implements OnClickListener {

	/**
     * 验证码重发倒计时
     */
    int secondleft = 60;
    /**
     * The timer.
     */
    Timer timer;
    /**
     * The dialog.
     */
    ProgressDialog dialog;
    /**
     * The tv phone switch.
	 */
	private TextView tvPhoneSwitch;
	/** The tv tips. */
	private TextView tvTips;
	/**
	 * The et name.
	 */
	private EditText etName;
	/**
	 * The et input code.
	 */
	private EditText etInputCode;
	/**
	 * The et input psw.
	 */
	private EditText etInputPsw;
	/**
	 * The btn get code.
	 */
	private Button btnGetCode;
	/**
	 * The btn re get code.
	 */
	private Button btnReGetCode;
	/**
	 * The handler.
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {

			case TICK_TIME:
				secondleft--;
				if (secondleft <= 0) {
					timer.cancel();
					btnReGetCode.setEnabled(true);
                    btnReGetCode.setText("重新获取");
                    btnReGetCode
                            .setBackgroundResource(R.drawable.btn_getverifycode_1_act);
                } else {
                    btnReGetCode.setText(secondleft + "秒后重新获取");

				}
				break;

			case REG_SUCCESS:
				ToastUtils.showShort(RegisterActivity.this, (String) msg.obj);
				dialog.cancel();
				IntentUtils.getInstance().startActivity(RegisterActivity.this,
						BindingActivity.class);
				break;

			case TOAST:
                ToastUtils.showShort(RegisterActivity.this, (String) msg.obj);
				dialog.cancel();
				break;
            }
        }
    };
    /**
     * The btn sure.
     */
    private Button btnSure;
    /**
     * The ll input code.
     */
    private LinearLayout llInputCode;
    /**
     * The ll input psw.
     */
    private LinearLayout llInputPsw;
    /**
     * The tb psw flag.
     */
    private ToggleButton tbPswFlag;
    /**
     * 是否邮箱注册标识位
     */
    private boolean isEmail = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_register);
        super.onCreate(savedInstanceState);
	}

    /**
	 * Inits the views.
	 */
    @Override
	public void initViews() {
		etName = (EditText) findViewById(R.id.etName);
		etInputCode = (EditText) findViewById(R.id.etInputCode);
		etInputPsw = (EditText) findViewById(R.id.etInputPsw);
		btnGetCode = (Button) findViewById(R.id.btnGetCode);
		btnReGetCode = (Button) findViewById(R.id.btnReGetCode);
		btnSure = (Button) findViewById(R.id.btnSure);
		llInputCode = (LinearLayout) findViewById(R.id.llInputCode);
		llInputPsw = (LinearLayout) findViewById(R.id.llInputPsw);
		tbPswFlag = (ToggleButton) findViewById(R.id.tbPswFlag);
		toogleUI(ui_statue.DEFAULT);
		dialog = new ProgressDialog(this);
		dialog.setMessage("处理中，请稍候...");
	}

    @Override
	public void initEvents() {
		btnGetCode.setOnClickListener(this);
		btnReGetCode.setOnClickListener(this);
		btnSure.setOnClickListener(this);
		tbPswFlag.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    etInputPsw.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    etInputPsw.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }

            }

        });
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnGetCode:
            //注册并且发送验证码
            getVerifyCode();
			break;
		case R.id.btnReGetCode:
            //重新获取短信验证码
            reGetVerifyCode();
			break;
		case R.id.btnSure:
			//验证短信验证码是否正确
            verifySmsCode();
			break;
		}

	}

    //验证短信验证码
    private void verifySmsCode() {
        String code = etInputCode.getText().toString().trim();
        if("".equals(code)){
            ToastUtils.showShort(getApplicationContext(), getString(R.string.validateCodeNull));
        }else{
            AVUser.verifyMobilePhoneInBackground(code, new AVMobilePhoneVerifyCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        LogUtil.log.i(getString(R.string.validateSuccess));
                        ToastUtils.showShort(getApplicationContext(), getString(R.string.validateSuccess));
                        Message msg = new Message();
                        msg.what = handler_key.REG_SUCCESS.ordinal();
                        msg.obj = "注册成功";
                        handler.sendMessage(msg);
                    } else if (AVException.CONNECTION_FAILED == e.getCode()) {
                        Message msg = new Message();
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = "网络连接失败";
                        handler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = handler_key.TOAST.ordinal();
                        msg.obj = "验证失败";
                        handler.sendMessage(msg);
                    }
                }
            });
        }
    }

    //获取短信验证码
    private void getVerifyCode() {
        String phone = etName.getText().toString().trim();
        String password = etInputPsw.getText().toString();
        if (StringUtils.isEmpty(phone) || phone.length() != 11) {
            ToastUtils.showShort(this, "请输入正确的手机号码。");
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
        toogleUI(ui_statue.PHONE);
        registerAndSendVerifyCode(phone, password);
    }

    //再次获取验证码的方法
    private void reGetVerifyCode() {
        String phone = etName.getText().toString().trim();
        if (StringUtils.isEmpty(phone) || phone.length() != 11) {
            ToastUtils.showShort(this, "请输入正确的手机号码。");
            return;
        }
        toogleUI(ui_statue.PHONE);
        dialog.show();
        btnReGetCode.setEnabled(false);
        btnReGetCode.setBackgroundResource(R.drawable.btn_getverifycode_2_act);
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
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                    handler.sendMessage(msg);
                } else {
                    LogUtil.log.e("注册" + e.toString());
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送失败";
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
	 * 处理发送验证码动作
	 * 实际上是先注册，再验证。
	 * @param phone
	 *            the phone
	 */
	private void registerAndSendVerifyCode(final String phone, final String password) {
        dialog.show();
        btnReGetCode.setEnabled(false);
        btnReGetCode.setBackgroundResource(R.drawable.btn_getverifycode_2_act);
        secondleft = 60;
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 1000);

        AVUser user = new AVUser();
        user.setUsername(phone);
        user.setPassword(password);
        user.setMobilePhoneNumber(phone);
        //此方法会注册并且发送验证短信
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    setManager.setPhoneNumber(phone);
                    LogUtil.log.i(getString(R.string.registerSuccess));
                    ToastUtils.showShort(getApplicationContext(), getString(R.string.registerSuccess));
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                    handler.sendMessage(msg);
                } else {
                    LogUtil.log.e("注册" + e.toString());
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送失败";
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 改变布局
     *
     * @param statue
     *            the statu
     */
    private void toogleUI(ui_statue statue) {
        if (statue == ui_statue.DEFAULT) {
            llInputCode.setVisibility(View.GONE);
            //	llInputPsw.setVisibility(View.GONE);
            btnSure.setVisibility(View.GONE);
            btnGetCode.setVisibility(View.VISIBLE);
            etName.setHint("手机号");
            etName.setText("");
			/*tvTips.setVisibility(View.GONE);*/
        } else if (statue == ui_statue.PHONE) {
            llInputCode.setVisibility(View.VISIBLE);
            //	llInputPsw.setVisibility(View.VISIBLE);
            btnSure.setVisibility(View.VISIBLE);
            btnGetCode.setVisibility(View.GONE);
            etName.setHint("手机号");
			/*tvPhoneSwitch.setText("邮箱注册");*/
			/*tvTips.setVisibility(View.GONE);*/
        } else {
            llInputCode.setVisibility(View.GONE);
            btnGetCode.setVisibility(View.GONE);
            //	llInputPsw.setVisibility(View.VISIBLE);
            btnSure.setVisibility(View.VISIBLE);
            etName.setHint("邮箱");
            etName.setText("");
            tvPhoneSwitch.setText("手机注册");
            tvTips.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            if (builder == null) {
                builder = NetworkUtils.networkDialogNoCancel(this);
            } else {
                builder.show();
            }
        } else {
            builder = null;
        }
    }

    /**
     * ClassName: Enum handler_key. <br/>
     * <br/>
     * date: 2014-11-26 17:51:10 <br/>
     *
     * @author Lien
     */
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

    /**
     * ClassName: Enum ui_statu. <br/>
     * <br/>
     * date: 2014-12-3 10:52:52 <br/>
     *
     * @author Lien
     */
    private enum ui_statue {

        /**
         * 默认状态
         */
        DEFAULT,

        /**
         * 手机注册
         */
        PHONE,

		/**
         * email注册
		 */
		EMAIL,
	}

}
