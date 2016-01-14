package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarManageActivity extends Activity {
    GetBindList getBindList;
    TextView tv_CurrentCar;
    ListView listview;
    List<Map<String, Object>> Othercarlist;
    SettingManager settingManager;
    SimpleAdapter adapter;
    RelativeLayout layout_CurrentCar;

    Button btn_AddDevice;
    int OthercarPositon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_manage);
    }

    @Override
    protected void onStart(){
        super.onStart();
        initView();
        initEvents();
    }

    void initView(){
        tv_CurrentCar = (TextView)findViewById(R.id.menutext1);
        listview = (ListView)findViewById(R.id.OtherCarListview);

        Othercarlist = new ArrayList<Map<String, Object>>();
        settingManager = new SettingManager(CarManageActivity.this);

        layout_CurrentCar = (RelativeLayout)findViewById(R.id.RelativeLayout_currentcar);
        layout_CurrentCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("test","test");
                Intent intentCarEdit = new Intent(CarManageActivity.this, CarInfoEditActivity.class);
                intentCarEdit.putExtra("string_key", tv_CurrentCar.getText());
                startActivityForResult(intentCarEdit, 0);
            }
        });

        btn_AddDevice = (Button)findViewById(R.id.btn_AddDevice);
        btn_AddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarManageActivity.this, BindingActivity.class);
                startActivity(intent);

            }
        });

        adapter = new SimpleAdapter(this,Othercarlist,R.layout.item_othercarlistview,
                new String[]{"img","WhichCar"},
                new int[]{R.id.img,R.id.WhichCar});

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OthercarPositon = position;
                Intent intentCarEdit = new Intent(CarManageActivity.this, CarInfoEditActivity.class);
                intentCarEdit.putExtra("string_key", Othercarlist.get(position).get("WhichCar").toString());
                startActivityForResult(intentCarEdit, 0);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode) {
            case 0:
                if(RESULT_OK == resultCode){
                //现在这个部分是有问题的  先把绑定设备的功能添加进去再来做这一个部分
                    //将intent里面的数据解析出来
                    String s = data.getStringExtra("string_key");
                    if(s.equals("设备解绑")){
                        Boolean Flag_Maincar = data.getBooleanExtra("boolean_key",false);
                        if(true == Flag_Maincar){
                            CaseManagedCarUnbinded();
                        }
                        else{
                            caseOtherCarUnbind();
                        }
                    }
                    //设备切换
                    else{
                        caseDeviceChange();
                    }
                }
                break;
        }
    }

    void caseDeviceChange(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("WhichCar", tv_CurrentCar.getText());
        map.put("img",R.drawable.img_1);

        String s = Othercarlist.get(OthercarPositon).get("WhichCar").toString();
        settingManager.setIMEI(s);
        //UI变化
        tv_CurrentCar.setText(s);
        Othercarlist.set(OthercarPositon, map);
        adapter.notifyDataSetChanged();
        //逻辑上切换
        reSubscribe();

        settingManager.setFlagCarSwitched("切换");
        settingManager.setAlarmFlag(false);
    }

    void initEvents(){
        getBindList = new GetBindList();
        getBindList.setonGetBindListListener(new GetBindList.OnGetBindListListener() {
            @Override
            public void onGetBindListSuccess(List<AVObject> list) {
                if (list.size() > 0) {
                    Othercarlist.clear();
                    HashMap<String, Object> map = null;
                    for(int i = 0;i<list.size();i++){
                        if(settingManager.getIMEI().equals(list.get(i).get("IMEI"))){
                            tv_CurrentCar.setText(settingManager.getIMEI());

                        }
                        else{
                            map = new HashMap<String, Object>();
                            map.put("WhichCar",list.get(i).get("IMEI"));
                            map.put("img",R.drawable.img_1);
                            Othercarlist.add(map);
                        }
                    }
                    //数据更新
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onGetBindListFail() {
                ToastUtils.showShort(CarManageActivity.this, "查询错误");

            }
        });
        getBindList.QueryBindList();
    }

    //在二级界面的时候正在被管理的car被解绑了
    void CaseManagedCarUnbinded(){
        //如果没有其他的车被绑定了  还没有处理相关的情况
        if(Othercarlist == null){
            //被绑定的只有一辆车  而且这辆车被解绑了  这种情况要怎么处理????

        }
        else{
            //把现在的IMEI号设置为Othercarlist里的第一辆车
            settingManager.setIMEI((String) Othercarlist.get(0).get("WhichCar"));
            //UI上的改变
            tv_CurrentCar.setText((String)Othercarlist.get(0).get("WhichCar"));
            Othercarlist.remove(0);
            //数据更新
            adapter.notifyDataSetChanged();
            //逻辑上的改变 需要重新订阅一次
            reSubscribe();
        }
    }

    private void caseOtherCarUnbind(){
        Othercarlist.remove(OthercarPositon);
        //数据更新
        adapter.notifyDataSetChanged();

    }

    private void reSubscribe() {
        Connection connection = Connections.getInstance(CarManageActivity.this).getConnection(ServiceConstants.handler);
        MqttAndroidClient mac = connection.getClient();
        subscribe(mac);
    }

    private void subscribe(MqttAndroidClient mac) {
        //订阅命令字
        String initTopic = settingManager.getIMEI();
        String topic1 = "dev2app/" + initTopic + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + initTopic + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + initTopic + "/433";
        //订阅报警
        String topic4 = "dev2app/" + initTopic + "/alarm";
        String[] topic = {topic1, topic2, topic3, topic4};
        int[] qos = {ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE};
        try {
            mac.subscribe(topic, qos);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic1);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic2);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic3);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic4);
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(this, "订阅失败!请稍后重启再试！");
        }
    }
}
