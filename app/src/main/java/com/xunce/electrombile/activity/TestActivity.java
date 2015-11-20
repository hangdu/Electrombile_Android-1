package com.xunce.electrombile.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

public class TestActivity extends Activity {

    private EditText etServer;
    private SettingManager settingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        settingManager = new SettingManager(this);

        etServer = (EditText) findViewById(R.id.et_server);
        etServer.setText(ServiceConstants.MQTT_HOST);
        Historys.put(this);

    }

    public void setServer(View view) {
        ToastUtils.showShort(this, "请重新打开");
        String server = etServer.getText().toString().trim();
        settingManager.setServer(server);
        Historys.exit();
    }
}
