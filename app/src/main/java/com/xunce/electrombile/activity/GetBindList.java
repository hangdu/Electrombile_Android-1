package com.xunce.electrombile.activity;

import android.os.*;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lybvinci on 16/1/13.
 */
public class GetBindList {
    public List<AVObject> ListValue;
    public OnGetBindListListener onGetBindListListener = null;

    public void GetBindList(){
        //构造函数是空的

    }

    public void QueryBindList(){
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    ListValue = new ArrayList<AVObject>();
                    ListValue = list;
                    GetBindList.this.onGetBindListListener.onGetBindListSuccess(ListValue);

                } else {
                    e.printStackTrace();

                    GetBindList.this.onGetBindListListener.onGetBindListFail();
                }
            }
        });
    }

    public interface OnGetBindListListener {
        void onGetBindListSuccess(List<AVObject> list);
        void onGetBindListFail();
    }

    public void setonGetBindListListener(OnGetBindListListener var1) {
        this.onGetBindListListener = var1;
    }
}
