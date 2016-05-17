package com.xunce.electrombile;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.activity.MqttConnectManager;
import com.xunce.electrombile.activity.WelcomeActivity;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.util.List;

/**
 * Created by lybvinci on 16/5/17.
 */
public class Binding {
    private static final int SUCCESS = 0;
    private static final int BIND_DEVICE_NUMBER = 1;
    private SettingManager settingManager;
    private String IMEI;
    private String previous_IMEI;

    private Context mContext;
    private CmdCenter mCenter;
    private MqttConnectManager mqttConnectManager;
    private BindingCallback bindingCallback;
    private String FromActivity;
    private LeancloudManager leancloudManager;
    public Binding(Context context,String FromActivity,BindingCallback bindingCallback){
        this.bindingCallback = bindingCallback;
        mContext = context;
        settingManager = SettingManager.getInstance();
        mCenter = CmdCenter.getInstance();
        this.FromActivity = FromActivity;
        leancloudManager = LeancloudManager.getInstance();
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
//                case START_BIND:
//                    progressDialog.show();
//                    startBind(IMEI);
//                    //超时设置
//                    timeOut();
//                    break;

                case SUCCESS:
                    //在cleanDevice中将setAlarmFlag设为false了,此时需要从服务器查询
                    previous_IMEI = settingManager.getIMEI();
                    settingManager.cleanDevice();
                    settingManager.setIMEI(IMEI);
                    ToastUtils.showShort(mContext, "设备登陆成功");
//                    progressDialog.cancel();
                    getBindDeviceNumber();
                    break;

//                case FAILED:
////                    times = 0;
//                    progressDialog.cancel();
//                    ToastUtils.showShort(mContext, msg.obj.toString());
//                    break;

                case BIND_DEVICE_NUMBER:
                    int BindedDeviceNum = (int)msg.obj;
                    getAlarmStatus_next(BindedDeviceNum);
                    break;
            }
        }

    };

    private void getAlarmStatus_next(int BindedDeviceNum){
        if(1 == BindedDeviceNum){
            //刚刚绑定的就是第一个设备:订阅;查询
            mqttConnectManager.subscribe(settingManager.getIMEI());
            mqttConnectManager.sendMessage(mCenter.cmdFenceGet(), settingManager.getIMEI());

        }
        else if(BindedDeviceNum > 1){
            //
            if(mqttConnectManager.unSubscribe(previous_IMEI)){
                //解订阅成功
                mqttConnectManager.subscribe(settingManager.getIMEI());
                mqttConnectManager.sendMessage(mCenter.cmdFenceGet(), settingManager.getIMEI());
            }
        }
        gotoAct();
    }



    private void QueryBindList(){
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    Message msg = Message.obtain();
                    msg.what = BIND_DEVICE_NUMBER;
                    msg.obj = list.size();
                    mHandler.sendMessage(msg);
                } else {
                    e.printStackTrace();
                    ToastUtils.showShort(mContext, "查询绑定设备数目出错");

                }
            }
        });
    }

    private void getBindDeviceNumber(){
        mqttConnectManager = MqttConnectManager.getInstance();
        if(mqttConnectManager.getMac() == null){
            //说明是fragmentActivity还没有进去  可以在fragmentActivity中再去查询开关状态
            gotoAct();
        }
        else{
            //说明是fragmentActivity已经在栈中
            if(mqttConnectManager.returnMqttStatus()){
                //需要判断这个是不是绑定的第一个设备
                QueryBindList();
            }
            else{
                ToastUtils.showShort(mContext,"在Binding中mqtt连接断开了");
            }
        }
    }

    private void gotoAct(){
        List<String> IMEIlist = settingManager.getIMEIlist();
        if(FromActivity.equals("CarManageActivity")){

            String IMEI_first = IMEIlist.get(0);
            IMEIlist.add(IMEI_first);

            IMEIlist.set(0, settingManager.getIMEI());
            settingManager.setIMEIlist(IMEIlist);

            //添加设备的话  需要从服务器上获取车辆的头像啊  他们的执行顺序不是你想的那个样子
            leancloudManager.getHeadImageFromServer(settingManager.getIMEI());
            leancloudManager.getCarcreatedAt(settingManager.getIMEI());

            Intent intent = new Intent(mContext,FragmentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
        else{
            IMEIlist.clear();
            IMEIlist.add(settingManager.getIMEI());
            settingManager.setIMEIlist(IMEIlist);

            leancloudManager.getHeadImageFromServer(settingManager.getIMEI());
            Intent intent = new Intent(mContext,WelcomeActivity.class);
            mContext.startActivity(intent);
        }
    }

    public void startBind(final String IMEI){
        this.IMEI = IMEI;
        final AVQuery<AVObject> queryBinding = new AVQuery<>("Bindings");

        final AVObject bindDevice = new AVObject("Bindings");
        final AVUser currentUser = AVUser.getCurrentUser();
        bindDevice.put("user", currentUser);

        //查询该IMEI号码是否存在
        AVQuery<AVObject> query = new AVQuery<>("DID");
        query.whereEqualTo("IMEI", IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(final List<AVObject> avObjects, AVException e) {
                if (e == null && avObjects.size() > 0) {
                    Log.d("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                    queryBinding.whereEqualTo("IMEI", IMEI);
                    queryBinding.whereEqualTo("user", currentUser);
                    queryBinding.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            Log.d("成功", "IMEI查询到" + list.size() + " 条符合条件的数据");
                            //一个设备可以绑定多个用户,但是这个类是唯一的，确保不重复生成相同的对象。
                            if (list.size() > 0) {
//                                Message message = Message.obtain();
//                                message.what = handler_key.FAILED.ordinal();
//                                message.obj = "设备已经被绑定！";
//                                mHandler.sendMessage(message);
                                ToastUtils.showShort(mContext,"设备已经被绑定");
                                return;
                            }
                            bindDevice.put("device", avObjects.get(0));
                            if (list.size() > 0) {
                                bindDevice.put("isAdmin", false);
                                ToastUtils.showShort(mContext, "您正在绑定附属车辆...");
                            } else {
                                bindDevice.put("isAdmin", true);
                                ToastUtils.showShort(mContext, "您正在绑定主车辆...");
                            }
                            bindDevice.put("IMEI", IMEI);
                            bindDevice.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        mHandler.sendEmptyMessage(SUCCESS);
                                    } else {
                                        fail("绑定发生错误");
                                    }
                                }
                            });
                        }
                    });

                } else {
                    if (e != null) {
                        fail("绑定发生错误");
                    } else {
                        fail("查询设备出错");
                    }
                }
            }
        });
    }

    private void fail(String failMessage){
        bindingCallback.startBindFail();
        ToastUtils.showShort(mContext, failMessage);
    }
}
