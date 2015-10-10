package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.protocol.JsonKeys;
import com.xunce.electrombile.utils.system.ToastUtils;


/**
 * Created by lybvinci on 2015/4/24.
 * 实现机智云的所有回调接口，监听器。
 * 解除绑定功能：
 * 1.需要先绑定设备，如果界面先出现正在登陆设备，而未出现登陆成功，就可能是之前解除绑定过，否则不会出现。
 * 2.解除绑定功能需要先使用unbindDevice函数，解除绑定，在didunbindDevice里得到结果
 * 3.同时还需要调用disconnnect函数，断开连接。如果不断开连接，是不能重新绑定设备的。
 * 4.当解除绑定时，如果出现解除绑定成功，就可以重新绑定新设备了。
 * 5当解除绑定时出现设备断开连接，则表明解除绑定操作失败，重新打开app，重新解除绑定操作……（我也不知道怎么操作才能正常解除绑定）
 */
public class BaseFragment extends Fragment{

    private static String TAG = "BaseFragmet";
    //timeOut
//    protected final int TIME_OUT = 0;
    //判断是否关闭页面
    public boolean close = false;
    protected CmdCenter mCenter;
    protected SettingManager setManager;
    protected GPSDataChangeListener mGpsChangedListener;
    protected ProgressDialog waitDialog;
    protected Context m_context;
    protected Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            waitDialog.dismiss();
            ToastUtils.showShort(m_context, "指令下发失败，请检查网络和设备工作是否正常。");
        }
    };

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setManager = new SettingManager(getActivity().getApplicationContext());
        mCenter = CmdCenter.getInstance(getActivity().getApplicationContext());
        waitDialog = new ProgressDialog(m_context);
        waitDialog.setMessage("正在查询位置信息，请稍后……");
        waitDialog.setCancelable(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

   @Override
        public void onResume() {
            super.onResume();
        }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mGpsChangedListener = (GPSDataChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement GPSDataChangeListener");
        }
        m_context = activity;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        close = true;
    }

    public void cancelWaitTimeOut() {
        waitDialog.dismiss();
        timeHandler.removeMessages(JsonKeys.TIME_OUT);
    }
    public interface GPSDataChangeListener {
        void gpsCallBack(LatLng desLat, TracksManager.TrackPoint trackPoint);
    }
}

