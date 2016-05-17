package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.BitmapUtils;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarManageActivity extends Activity {
    private TextView tv_CurrentCar;
    private List<Map<String, Object>> Othercarlist;
    private SettingManager settingManager;
    private SimpleAdapter adapter;
    private int OthercarPositon;
    private List<String> IMEIlist;
    private ImageView img_car;

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

    private void initView(){
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("车辆管理");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CarManageActivity.this.finish();
            }
        });

        img_car = (ImageView)findViewById(R.id.menuimage1);

        settingManager = SettingManager.getInstance();
        String IMEI = settingManager.getIMEI();
        Bitmap bitmap = BitmapUtils.compressImageFromFile(IMEI);
        if(bitmap!=null){
            img_car.setImageBitmap(bitmap);
        }

        tv_CurrentCar = (TextView)findViewById(R.id.menutext1);
//        String carname = settingManager.getCarName(settingManager.getIMEI());
        tv_CurrentCar.setText(settingManager.getCarName(settingManager.getIMEI()));

        ListView listview = (ListView)findViewById(R.id.OtherCarListview);

        Othercarlist = new ArrayList<>();
        settingManager = SettingManager.getInstance();

        IMEIlist = new ArrayList<>();

        RelativeLayout layout_CurrentCar = (RelativeLayout)findViewById(R.id.RelativeLayout_currentcar);
        layout_CurrentCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCarEdit = new Intent(CarManageActivity.this, CarInfoEditActivity.class);
                intentCarEdit.putExtra("string_key", settingManager.getIMEI());

                String NextCarIMEI;
                if(Othercarlist.size()>=1){
                    NextCarIMEI = settingManager.getIMEIlist().get(1);
                }
                else{
                    NextCarIMEI = "空";
                }

                intentCarEdit.putExtra("NextCarIMEI",NextCarIMEI);
                startActivityForResult(intentCarEdit, 0);
            }
        });

        Button btn_AddDevice = (Button)findViewById(R.id.btn_AddDevice);
        btn_AddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkUtils.checkNetwork(CarManageActivity.this)){
                    ToastUtils.showShort(CarManageActivity.this, "请检查网络连接,该操作无法完成");
                    return;
                }
//                Intent intent = new Intent(CarManageActivity.this, BindingActivity2.class);
                Intent intent = new Intent(CarManageActivity.this, CaptureActivity.class);
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
//                intentCarEdit.putExtra("string_key", Othercarlist.get(position).get("WhichCar").toString());
//                String imei = settingManager.getIMEIlist().get(position+1);
                intentCarEdit.putExtra("string_key", settingManager.getIMEIlist().get(position+1));

                intentCarEdit.putExtra("list_position", position);
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
                        if(Flag_Maincar){
                            CaseManagedCarUnbinded();

                            //发送广播提醒switchFragment  发生了切解绑主车辆的行为(只有主车辆被解绑了  才需要去发送广播)
                            Intent intent = new Intent("com.app.bc.test");
                            intent.putExtra("KIND","DELETEMAINDEVICE");
                            sendBroadcast(intent);//发送广播事件
                        }
                        else{
                            caseOtherCarUnbind();
                            Intent intent = new Intent("com.app.bc.test");
                            intent.putExtra("KIND","DELETENONMAINDEVICE");
                            intent.putExtra("POSITION",OthercarPositon);
                            sendBroadcast(intent);//发送广播事件
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

    private void caseDeviceChange(){
        getIMEIlist();

        //UI变化
        tv_CurrentCar.setText(settingManager.getCarName(settingManager.getIMEI()));

        Othercarlist.get(OthercarPositon).put("WhichCar", tv_CurrentCar.getText());
        adapter.notifyDataSetChanged();

        //逻辑上切换:原来的设备解订阅,新设备订阅,查询alarmstatus
        settingManager.setFlagCarSwitched("切换");

        //改变车辆的照片
        DeviceChangeHeadImage();

        //发送广播提醒switchFragment  发生了切换车辆的行为
        Intent intent = new Intent("com.app.bc.test");
        intent.putExtra("KIND","SWITCHDEVICE");
        intent.putExtra("POSITION",OthercarPositon);
        sendBroadcast(intent);//发送广播事件
    }

    private void DeviceChangeHeadImage(){
//        String fileName = Environment.getExternalStorageDirectory() + "/"+settingManager.getIMEI()+"crop_result.jpg";
//        File f = new File(fileName);
        File f = new File(this.getExternalFilesDir(null), settingManager.getIMEI()+"crop_result.jpg");
        if(f.exists()){
            Bitmap bitmap = BitmapUtils.compressImageFromFile(settingManager.getIMEI());
            if(bitmap!=null){
                img_car.setImageBitmap(bitmap);
                return;
            }
            else{
                //默认图像
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_carimage);
                img_car.setImageBitmap(bitmap);
            }
        }
        else{
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_carimage);
            img_car.setImageBitmap(bitmap);
        }
    }

    private void initEvents(){
        getIMEIlist();
        HashMap<String, Object> map;
        for(int i = 1;i<IMEIlist.size();i++){
            map = new HashMap<String, Object>();
            map.put("WhichCar",settingManager.getCarName(IMEIlist.get(i)));
            map.put("img",R.drawable.othercar);
            Othercarlist.add(map);
        }
        adapter.notifyDataSetChanged();
    }


    //在二级界面的时候正在被管理的car被解绑了
    private void CaseManagedCarUnbinded(){
        //如果没有其他的车被绑定了  还没有处理相关的情况
        if(Othercarlist == null){
            //被绑定的只有一辆车 根本就不会到这个分支来 之前就处理了
        }
        else{
            //UI上的改变
            tv_CurrentCar.setText((String)Othercarlist.get(0).get("WhichCar"));
            Othercarlist.remove(0);
            adapter.notifyDataSetChanged();
            //头像也要变过来

            String IMEI = settingManager.getIMEI();
            Bitmap bitmap = BitmapUtils.compressImageFromFile(IMEI);
            if(bitmap!=null){
                img_car.setImageBitmap(bitmap);
            }
            else{
                bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.default_carimage);
                img_car.setImageBitmap(bitmap);
            }
        }
    }

    private void caseOtherCarUnbind(){
        Othercarlist.remove(OthercarPositon);
        adapter.notifyDataSetChanged();
    }


    public void getIMEIlist(){
        IMEIlist = settingManager.getIMEIlist();
    }
}
