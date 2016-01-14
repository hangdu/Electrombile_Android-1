package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xunce.electrombile.R;

public class CarInfoEditActivity extends Activity {
    TextView tv_CarIMEI;
    EditText et_CarName;
    Button btn_DeleteDevice;
    String IMEI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);

        Intent intent = getIntent();
        IMEI = intent.getStringExtra("string_key");



    }

    @Override
    protected void onStart(){
        super.onStart();
        initView();

    }

    void initView(){
        tv_CarIMEI = (TextView)findViewById(R.id.tv_CarIMEI);
        tv_CarIMEI.setText(IMEI);

        et_CarName = (EditText)findViewById(R.id.et_CarName);
        btn_DeleteDevice = (Button)findViewById(R.id.btn_DeleteDevice);
        btn_DeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
