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

        Othercarlist = new ArrayList<>();
        settingManager = new SettingManager(CarManageActivity.this);

        layout_CurrentCar = (RelativeLayout)findViewById(R.id.RelativeLayout_currentcar);
        layout_CurrentCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCarEdit = new Intent(CarManageActivity.this, CarInfoEditActivity.class);
                intentCarEdit.putExtra("string_key", tv_CurrentCar.getText());

                String NextCarIMEI;
                if(Othercarlist.size()>=1){
                    NextCarIMEI = Othercarlist.get(0).get("WhichCar").toString();
                }
                else{
                    NextCarIMEI = "空";
                }

                intentCarEdit.putExtra("NextCarIMEI",NextCarIMEI);
                startActivityForResult(intentCarEdit, 0);
            }
        });

        btn_AddDevice = (Button)findViewById(R.id.btn_AddDevice);
        btn_AddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarManageActivity.this, BindingActivity2.class);
                intent.putExtra("From","CarManageActivity");
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
//                intentCarEdit.putExtra("NextCarIMEI");
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
        map.put("img",R.drawable.othercar);

        String s = Othercarlist.get(OthercarPositon).get("WhichCar").toString();
        settingManager.setIMEI(s);
        //UI变化
        tv_CurrentCar.setText(s);
        Othercarlist.set(OthercarPositon, map);
        adapter.notifyDataSetChanged();
        //逻辑上切换:原来的设备解订阅,新设备订阅,查询alarmstatus
//        reSubscribe();
//        mqttConnectManager = MqttConnectManager.getInstance();
        settingManager.setFlagCarSwitched("切换");
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
                            map.put("img",R.drawable.othercar);
                            Othercarlist.add(map);
                        }
                    }
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
            //被绑定的只有一辆车 根本就不会到这个分支来 之前就处理了
        }
        else{
            //把现在的IMEI号设置为Othercarlist里的第一辆车
//            settingManager.setIMEI((String) Othercarlist.get(0).get("WhichCar"));
            //UI上的改变
            tv_CurrentCar.setText((String)Othercarlist.get(0).get("WhichCar"));
            Othercarlist.remove(0);
            //数据更新
            adapter.notifyDataSetChanged();
            //逻辑上的改变 需要重新订阅一次
//            reSubscribe();
        }
    }

    private void caseOtherCarUnbind(){
        Othercarlist.remove(OthercarPositon);
        adapter.notifyDataSetChanged();
    }
}
