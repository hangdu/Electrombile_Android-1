package com.xunce.electrombile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.BitmapUtils;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by lybvinci on 16/2/29.
 */
public class LeancloudManager {
    private Context context;
    public List<String> IMEIlist;
    private List<Date> CreateDate;
    public OnGetBindListListener onGetBindListListener = null;
    //单例模式
    private final static LeancloudManager INSTANCE = new LeancloudManager();
    private SettingManager settingManager = SettingManager.getInstance();
    public static LeancloudManager getInstance() {
        return INSTANCE;
    }

    private LeancloudManager(){
        context = App.getInstance();
    }

    //获取用户的基本信息:姓名 性别 出生日期
    public void getUserInfoFromServer(){
        AVUser currentUser = AVUser.getCurrentUser();
        String UserName = (String)currentUser.get("name");
        Boolean sex = (Boolean)currentUser.get("isMale");
        //不确定这个对不对
        Date date = (Date)currentUser.get("birth");
        int i = 0;

        if(UserName == null){
            //默认用户名设置为空
            currentUser.put("name","空");
            UserName = "空";
            i++;
        }
        if(sex == null){
            currentUser.put("isMale",true);
            sex = true;
            i++;
        }
        if(date == null){
            //这个得到的时间有点问题  为啥呢?
            date =new Date(System.currentTimeMillis());//获取当前时间
            currentUser.put("birth",date);
            i++;
        }
        if(i>0){
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
//                    Log.i("LeanCloud", "Save successfully.");

                    } else {
//                    Log.e("LeanCloud", "Save failed.");
                    }
                }
            });
        }

        //怎么获取和时区相关的时间啊
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        //刷新本地的数据
        settingManager.setUpdateTime(System.currentTimeMillis());
        settingManager.setBirthDate(sdf.format(date));
        settingManager.setNickname(UserName);
        settingManager.setGender(sex);
    }

    //用户基本信息  上传服务器  用了重载
    public void uploadUserInfo(final String Nickname){
        AVUser currentUser = AVUser.getCurrentUser();
        currentUser.put("name", Nickname);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(context, "用户昵称已经上传到服务器");
                    //存到本地
                    settingManager.setNickname(Nickname);
                } else {
                    ToastUtils.showShort(context, "用户昵称已经上传服务器失败");
                }
            }
        });
    }

    public void uploadUserInfo(final Boolean gender){
        AVUser currentUser = AVUser.getCurrentUser();
        currentUser.put("isMale", gender);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(context, "性别已经上传到服务器");
                    //保存到本地
                    settingManager.setGender(gender);
                } else {
                    ToastUtils.showShort(context, "性别已经上传服务器失败");
                }
            }
        });
    }

    public void uploadUserInfo(final Date birthdate){
        AVUser currentUser = AVUser.getCurrentUser();
        currentUser.put("birth", birthdate);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(context, "出生日期已经上传到服务器");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    settingManager.setBirthDate(sdf.format(birthdate));
                } else {
                    ToastUtils.showShort(context, "出生日期已经上传服务器失败");
                }
            }
        });
    }

    public void getUserIMEIlistFromServer(){
        IMEIlist = new ArrayList<>();
        CreateDate = new ArrayList<>();
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    LeancloudManager.this.onGetBindListListener.onGetBindListSuccess(list);
                    if (list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            String IMEI = list.get(i).get("IMEI").toString();
                            IMEIlist.add(IMEI);
                            Date createdDate = (Date) list.get(i).get("createdAt");
                            CreateDate.add(createdDate);
                            //写到settingManager中去

                        }
                        settingManager.setIMEIlist(IMEIlist);
                        settingManager.setIMEI(IMEIlist.get(0));

                        //获取IMEIlist中每一个设备对应的头像
                        for (String IMEI : IMEIlist) {
                            getHeadImageFromServer(IMEI);
                        }

                    } else {
                        settingManager.setIMEIlist(null);
                    }
                } else {
                    e.printStackTrace();
                    LeancloudManager.this.onGetBindListListener.onGetBindListFail();
                }
            }
        });
    }

    //根据IMEIlist把每一个的  object:车辆绑定时间和key:createdAt(Date)从服务器上面拉取上来

    public void getUserIMEIinfoFromServer(){

    }

    public void uploadImageToServer(final String IMEI,Bitmap bitmap){
        AVQuery<AVObject> query = new AVQuery<>("DID");
        query.whereEqualTo("IMEI", IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (!list.isEmpty()) {
                        if (list.size() != 1) {
                            ToastUtils.showShort(context, "DID表中  该IMEI对应多条记录");
                            return;
                        }
                        AVObject avObject = list.get(0);
                        try {
                            AVFile avfile = AVFile.withAbsoluteLocalPath("crop_result.png",
                                    Environment.getExternalStorageDirectory() + "/" + IMEI + "crop_result.png");


                            avfile.saveInBackground();
                            avObject.put("Image", avfile);
                            avObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        ToastUtils.showShort(context, "车辆头像已经上传到服务器");
                                        //保存到本地
                                    } else {
                                        ToastUtils.showShort(context, "车辆头像上传服务器失败");
                                    }
                                }
                            });
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    }
                } else {
                    ToastUtils.showShort(context, "在DID表中查询该IMEI 查询失败");
                }
            }
        });
    }


    //从服务器获取车辆头像(如果头像为空  则从本地上传到服务器)
    public void getHeadImageFromServer(final String IMEI){
        AVQuery<AVObject> query = new AVQuery<>("DID");
        query.whereEqualTo("IMEI", IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (!list.isEmpty()) {
                        if (list.size() != 1) {
                            ToastUtils.showShort(context, "DID表中  该IMEI对应多条记录");
                            return;
                        }
                        AVObject avObject = list.get(0);
                        if (avObject.get("Image") == null) {
                            //服务器上的头像数据为空  所以需要上传数据到数据库啊
                            Log.d("test", "test");
                            String fileName = Environment.getExternalStorageDirectory() + "/"+IMEI+"crop_result.png";
                            File f = new File(fileName);
                            if(f.exists()){
                                f.delete();
                            }
                            if (settingManager.getIMEI().equals(IMEI)) {
                                Intent intent = new Intent("com.app.bc.test");
                                context.sendBroadcast(intent);//发送广播事件
                            }

                        } else {
                            //从服务器拉头像  如果是在inputIMEIactivity中调用的  就需要在本地存文件;
//                             如果是在更改头像里调用的   就不需要存文件  (因为已经存了)
                            AVFile avFile = avObject.getAVFile("Image");
                            avFile.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, AVException e) {
                                    //process data or exception.
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    String fileName = Environment.getExternalStorageDirectory() + "/" + IMEI + "crop_result.png";
                                    try {
                                        BitmapUtils.saveBitmapToFile(bitmap, fileName);
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }

                                    if (settingManager.getIMEI().equals(IMEI)) {
                                        Intent intent = new Intent("com.app.bc.test");
                                        context.sendBroadcast(intent);//发送广播事件
                                    }
                                }
                            });
                        }
                    }
                    else {
                        //不可能出现这种情况啊
                        ToastUtils.showShort(context, "DID表中  IMEI对应0条记录");
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface OnGetBindListListener {
        void onGetBindListSuccess(List<AVObject> list);
        void onGetBindListFail();
    }

    public void setonGetBindListListener(OnGetBindListListener var1) {
        onGetBindListListener = var1;
    }
}
