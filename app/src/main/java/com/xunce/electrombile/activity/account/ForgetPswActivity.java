/**
 * Project Name:XPGSdkV4AppBase
 * File Name:ForgetPswActivity.java
 * Package Name:com.gizwits.framework.activity.account
 * Date:2015-1-27 14:44:57
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.xpg.common.useful.NetworkUtils;
import com.xunce.electrombile.xpg.common.useful.StringUtils;
import com.xunce.electrombile.xpg.ui.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * ClassName: Class ForgetPswActivity. <br/>
 * 忘记密码，该类主要包含了两个修改密码的方法，手机号注册的用户通过获取验证码修改密码，邮箱注册的用户需要通过注册邮箱的重置邮件进行重置。<br/>
 * date: 2014-12-09 17:27:10 <br/>
 * 
 * @author StephenC
 */
public class ForgetPswActivity extends BaseActivity implements OnClickListener {
	/**
	 * The tv phone switch.
	 */
	// private TextView tvPhoneSwitch;

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
	 * The iv back.
	 */
	private ImageView ivBack;

	/**
	 * The tb psw flag.
	 */
	private ToggleButton tbPswFlag;

	/** 是否邮箱注册标识位 */
	private boolean isEmail = false;

	/**
	 * The secondleft.
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
		 * 修改成功
		 */
		CHANGE_SUCCESS,

		/**
		 * Toast弹出通知
		 */
		TOAST,

	}

	/**
	 * ClassName: Enum ui_statu. <br/>
	 * UI状态枚举类<br/>
	 * date: 2014-12-3 10:52:52 <br/>
	 * 
	 * @author Lien
	 */
	private enum ui_statu {

		/**
		 * 默认状态
		 */
		DEFAULT,

		/**
		 * 手机注册的用户
		 */
		PHONE,

		/**
		 * email注册的用户
		 */
		EMAIL,
	}

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
					btnReGetCode.setText("重新获取验证码");
					btnReGetCode
							.setBackgroundResource(R.drawable.button_blue_short);
				} else {
					btnReGetCode.setText(secondleft + "秒后\n重新获取");

				}
				break;

			case CHANGE_SUCCESS:
				finish();
				break;

			case TOAST:
				ToastUtils.showShort(ForgetPswActivity.this, (String) msg.obj);
				dialog.cancel();
				break;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.framework.activity.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forget_reset);
		initViews();
		initEvents();
	}
    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(this);
        }
    }

	/**
	 * Inits the views.
	 */
	private void initViews() {
		etName = (EditText) findViewById(R.id.etName);
		etInputCode = (EditText) findViewById(R.id.etInputCode);
		etInputPsw = (EditText) findViewById(R.id.etInputPsw);
		btnGetCode = (Button) findViewById(R.id.btnGetCode);
		btnReGetCode = (Button) findViewById(R.id.btnReGetCode);
		btnSure = (Button) findViewById(R.id.btnSure);
		llInputCode = (LinearLayout) findViewById(R.id.llInputCode);
		llInputPsw = (LinearLayout) findViewById(R.id.llInputPsw);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		tbPswFlag = (ToggleButton) findViewById(R.id.tbPswFlag);
		toogleUI(ui_statu.DEFAULT);
		dialog = new ProgressDialog(this);
		dialog.setMessage("处理中，请稍候...");
	}

	/**
	 * Inits the events.
	 */
	private void initEvents() {
		btnGetCode.setOnClickListener(this);
		btnReGetCode.setOnClickListener(this);
		btnSure.setOnClickListener(this);
		// tvPhoneSwitch.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		tbPswFlag
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							etInputPsw
									.setInputType(InputType.TYPE_CLASS_TEXT
											| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//							etInputPsw.setKeyListener(DigitsKeyListener
//									.getInstance(getResources().getString(
//                                            R.string.register_name_digits)));
						} else {
							etInputPsw.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//							etInputPsw.setKeyListener(DigitsKeyListener
//									.getInstance(getResources().getString(
//                                            R.string.register_name_digits)));
						}
					}

				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnGetCode:
			String name = etName.getText().toString().trim();
			if (!StringUtils.isEmpty(name)) {
				if (name.contains("@")) {
					isEmail = true;
					toogleUI(ui_statu.EMAIL);
					getEmail(name);
				} else if (name.length() == 11) {
					toogleUI(ui_statu.PHONE);
					sendVerifyCode(name);
				} else {
					ToastUtils.showShort(this, "请输入正确的账号");
				}
			} else {
				ToastUtils.showShort(this, "请输入正确的账号");
			}

			break;
		case R.id.btnReGetCode:
			String phone2 = etName.getText().toString().trim();
			if (!StringUtils.isEmpty(phone2) && phone2.length() == 11) {
				toogleUI(ui_statu.PHONE);
				sendVerifyCode(phone2);
			} else {
				ToastUtils.showShort(this, "请输入正确的手机号码。");
			}
			break;
		case R.id.btnSure:
			doChangePsw();
			break;
//		case R.id.tvPhoneSwitch:
//			ToastUtils.showShort(this, "该功能暂未实现，敬请期待。^_^");
//			break;
		case R.id.ivBack:
			onBackPressed();
			break;
		}
	}

	/**
	 * Toogle ui.
	 * 
	 * @param statu
	 *            the statu
	 */
	private void toogleUI(ui_statu statu) {
		if (statu == ui_statu.DEFAULT) {
			llInputCode.setVisibility(View.GONE);
			llInputPsw.setVisibility(View.GONE);
			btnSure.setVisibility(View.GONE);
			btnGetCode.setVisibility(View.VISIBLE);
		} else if (statu == ui_statu.PHONE) {
			llInputCode.setVisibility(View.VISIBLE);
			llInputPsw.setVisibility(View.VISIBLE);
			btnSure.setVisibility(View.VISIBLE);
			btnGetCode.setVisibility(View.GONE);
		} else {

		}
	}

	/**
	 * Gets the email.
	 * 
	 * @param email
	 *            the email
	 * @return the email
	 */
	private void getEmail(String email) {
		mCenter.cChangePassworfByEmail(email);
	}

	/**
	 * 执行手机号重置密码操作
	 */
	private void doChangePsw() {

		String phone = etName.getText().toString().trim();
		String code = etInputCode.getText().toString().trim();
		String password = etInputPsw.getText().toString();
		if (phone.length() != 11) {
			Toast.makeText(this, "电话号码格式不正确", Toast.LENGTH_SHORT).show();
			return;
		}
		if (code.length() == 0) {
			Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
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
		mCenter.cChangeUserPasswordWithCode(phone, code, password);
		dialog.show();
	}

	/**
	 * 发送验证码
	 * 
	 * @param phone
	 *            the phone
	 */
	private void sendVerifyCode(final String phone) {
		dialog.show();
		btnReGetCode.setEnabled(false);
		btnReGetCode.setBackgroundResource(R.drawable.button_gray_short);
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
		mCenter.cRequestSendVerifyCode(phone);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.framework.activity.BaseActivity#didRequestSendVerifyCode(int,
	 * java.lang.String)
	 */
	@Override
	protected void didRequestSendVerifyCode(int error, String errorMessage) {
	//	Log.i("error message ", error + " " + errorMessage);
		if (error == 0) {// 发送成功
			Message msg = new Message();
			msg.what = handler_key.TOAST.ordinal();
			msg.obj = "发送成功";
			handler.sendMessage(msg);
		} else {// 发送失败
			Message msg = new Message();
			msg.what = handler_key.TOAST.ordinal();
			msg.obj = errorMessage;
			handler.sendMessage(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.framework.activity.BaseActivity#didChangeUserPassword(int,
	 * java.lang.String)
	 */
	@Override
	protected void didChangeUserPassword(int error, String errorMessage) {
		if (error == 0) {// 修改成功
			Message msg = new Message();
			msg.what = handler_key.TOAST.ordinal();
			if (isEmail) {
				msg.obj = "重置密码链接已发送您的邮箱，在邮箱中可执行重置密码操作";
			} else {
				msg.obj = "修改成功";
			}
			handler.sendMessage(msg);
			handler.sendEmptyMessageDelayed(
					handler_key.CHANGE_SUCCESS.ordinal(), 2000);

		} else {// 修改失败
			Message msg = new Message();
			msg.what = handler_key.TOAST.ordinal();
			msg.obj = errorMessage;
			handler.sendMessage(msg);
		}
		super.didChangeUserPassword(error, errorMessage);
	}
}
