package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.Binding;
import com.xunce.electrombile.BindingCallback;
import com.xunce.electrombile.LeancloudManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;
import java.util.List;

public class InputIMEIActivity extends Activity {
    private EditText et_IMEI;
    private Button btn_Sure;
    private String IMEI;
    private Binding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_imei);
        initView();
        initEvent();
    }

    private void initView(){
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("输入设备序列号");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputIMEIActivity.this.finish();
            }
        });

        et_IMEI = (EditText)findViewById(R.id.et_IMEI);
        btn_Sure = (Button)findViewById(R.id.btn_Sure);

    }

    private void initEvent(){
        //解析是从哪个activity跳转过来的
        Intent intent = getIntent();
        final String FromActivity = intent.getStringExtra("From");

        binding = new Binding(InputIMEIActivity.this, FromActivity, new BindingCallback() {
            @Override
            public void startBindFail() {

            }
        });

        btn_Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMEI = et_IMEI.getText().toString();
                //判断IMEI号是否是15位
                if(IMEI.length()!=15){
                    ToastUtils.showShort(InputIMEIActivity.this, "IMEI的长度不对或者为空");
                } else{
                    ToastUtils.showShort(InputIMEIActivity.this, "IMEI为" + IMEI);
                    binding.startBind(IMEI);
                }
            }
        });
    }
}
