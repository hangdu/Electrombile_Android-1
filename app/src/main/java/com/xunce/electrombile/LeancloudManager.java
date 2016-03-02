package com.xunce.electrombile;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by lybvinci on 16/2/29.
 */
public class LeancloudManager {
    public final static String ChangeNickname = "changenickname";
    public final static String ChangeGender = "changegender";
    public final static String ChangeBirth = "changebirth";

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
        currentUser.put("name",Nickname);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(context,"用户昵称已经上传到服务器");
                    //存到本地
                    settingManager.setNickname(Nickname);
                } else {
                    ToastUtils.showShort(context,"用户昵称已经上传服务器失败");
                }
            }
        });
    }


    public void uploadUserInfo(final Boolean gender){
        AVUser currentUser = AVUser.getCurrentUser();
        currentUser.put("isMale",gender);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(context,"性别已经上传到服务器");
                    //保存到本地
                    settingManager.setGender(gender);
                } else {
                    ToastUtils.showShort(context,"性别已经上传服务器失败");
                }
            }
        });
    }

    public void uploadUserInfo(final Date birthdate){
        AVUser currentUser = AVUser.getCurrentUser();
        currentUser.put("birth",birthdate);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(context,"出生日期已经上传到服务器");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    settingManager.setBirthDate(sdf.format(birthdate));
                } else {
                    ToastUtils.showShort(context,"出生日期已经上传服务器失败");
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
                    if(list.size()>0){
                        for (int i = 0; i < list.size(); i++) {
                            String IMEI = list.get(i).get("IMEI").toString();
                            IMEIlist.add(IMEI);
                            Date createdDate = (Date)list.get(i).get("createdAt");
                            CreateDate.add(createdDate);
                            //写到settingManager中去
                        }
                        settingManager.setIMEIlist(IMEIlist);
                    }
                    else{
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

    public interface OnGetBindListListener {
        void onGetBindListSuccess(List<AVObject> list);
        void onGetBindListFail();
    }

    public void setonGetBindListListener(OnGetBindListListener var1) {
        onGetBindListListener = var1;
    }
}
