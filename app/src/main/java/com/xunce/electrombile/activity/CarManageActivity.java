package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.avos.avoscloud.AVObject;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

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
                intentCarEdit.putExtra("string_key",tv_CurrentCar.getText());
                startActivity(intentCarEdit);
            }
        });


        adapter = new SimpleAdapter(this,Othercarlist,R.layout.item_othercarlistview,
                new String[]{"img","WhichCar"},
                new int[]{R.id.img,R.id.WhichCar});

        listview.setAdapter(adapter);
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

}
