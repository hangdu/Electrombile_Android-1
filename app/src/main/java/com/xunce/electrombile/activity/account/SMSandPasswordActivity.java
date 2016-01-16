package com.xunce.electrombile.activity.account;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xunce.electrombile.R;

public class SMSandPasswordActivity extends Activity {
    private TextView tv_Current_Phone;
    private EditText et_SMSCode;
    private TextView tv_LeftSecond;
    private Button btn_ResendSysCode;
    private EditText et_Password;
    private Button btn_NextStep;

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
        tv_LeftSecond = (TextView)findViewById(R.id.tv_LeftSecond);
        btn_ResendSysCode = (Button)findViewById(R.id.btn_ResendSysCode);
        et_Password = (EditText)findViewById(R.id.et_Password);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);
    }

    public void initEvent(){
        //开始20s倒计时


    }
}
