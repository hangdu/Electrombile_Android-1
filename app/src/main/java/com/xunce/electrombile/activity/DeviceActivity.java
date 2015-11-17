package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.List;

//import io.yunba.android.manager.YunBaManager;

public class DeviceActivity extends BaseActivity {
    private static final String TAG = "DeviceActivity";
    private LinearLayout releaseBind;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_device);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        releaseBind = (LinearLayout) findViewById(R.id.layout_release_bind);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在设置,请稍后");
    }

    @Override
    public void initEvents() {
        ((TextView) (findViewById(R.id.tv_imei))).setText("设备号：" + setManager.getIMEI());

    }

    public void bindDev(View view) {
        bind();
    }

    public void unBindDev(View view) {
        releaseBind();
    }

    public void multDevManage(View view) {
        goToBindingListAct();
    }

    private void goToBindingListAct() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, "网络连接错误！");
            return;
        }
        Intent intentBindList = new Intent(this, BindListActivity.class);
        startActivity(intentBindList);
    }

    private void bind() {
        if (NetworkUtils.isNetworkConnected(this)) {
            if (setManager.getIMEI().isEmpty()) {
                setManager.cleanDevice();
                Historys.finishAct(FragmentActivity.class);
                Intent intentStartBinding = new Intent(this, BindingActivity.class);
                startActivity(intentStartBinding);
                this.finish();

            } else {
                System.out.println(setManager.getIMEI() + "aaaaaaaaaaa");
                ToastUtils.showShort(this, "设备已绑定");
            }
        } else {
            ToastUtils.showShort(this, "网络连接错误");
        }
    }

    private void releaseBind() {
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                .setTitle("*****解除绑定*****")
                .setMessage("将要解除绑定的设备，确定解除么？")
                .setPositiveButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        releaseBind.setClickable(false);
                        releaseBinding();
                        releaseBind.setClickable(true);
                    }
                }).create();
        dialog2.show();
    }

    private void releaseBinding() {
        //先判断IMEI是否为空，若为空证明没有绑定设备。
        if (setManager.getIMEI().isEmpty()) {
            ToastUtils.showShort(this, "未绑定设备");
            progressDialog.dismiss();
            return;
        }
        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, "网络连接失败");
            progressDialog.dismiss();
            return;
        }
        //若不为空，则先查询所在绑定类，再删除，删除成功后取消订阅，并删除本地的IMEI，关闭FragmentActivity,进入绑定页面
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        String IMEI = setManager.getIMEI();
        query.whereEqualTo("IMEI", IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null && avObjects.size() > 0) {
                    AVObject bindClass = avObjects.get(0);
                    Connection connection = Connections.getInstance(DeviceActivity.this).getConnection(ServiceConstants.handler);
                    MqttAndroidClient mac = connection.getClient();
                    boolean isUnSubscribe = unSubscribe(mac);
                    if (isUnSubscribe) {
                        bindClass.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    setManager.cleanDevice();
                                    ToastUtils.showShort(DeviceActivity.this, "解除绑定成功!");
                                    Historys.finishAct(FragmentActivity.class);
                                    progressDialog.dismiss();
                                    Intent intent;
                                    intent = new Intent("com.xunce.electrombile.alarmservice");
                                    DeviceActivity.this.stopService(intent);
                                    intent = new Intent(DeviceActivity.this, BindingActivity.class);
                                    startActivity(intent);
                                    DeviceActivity.this.finish();
                                }
                            }
                        });
                    }

                } else {
                    if (e != null)
                        Log.d("失败", "问题： " + e.getMessage());
                    ToastUtils.showShort(DeviceActivity.this, "解除绑定失败!");
                    progressDialog.dismiss();
                }
            }
        });


    }

    private boolean unSubscribe(MqttAndroidClient mac) {
        //订阅命令字
        String initTopic = setManager.getIMEI();
        String topic1 = "dev2app/" + initTopic + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + initTopic + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + initTopic + "/433";

        String topic4 = "dev2app/" + initTopic + "/alarm";
        String[] topic = {topic1, topic2, topic3, topic4};
        try {
            mac.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(this, "取消订阅失败!请稍后重启再试！");
            return false;
        }

    }
}
