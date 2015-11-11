package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.utils.system.ToastUtils;


/**
 * Created by lybvinci on 2015/4/24.
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
    protected Context m_context;
    private Dialog waitDialog;
    protected Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dismissWaitDialog();
            ToastUtils.showShort(m_context, "指令下发失败，请检查网络和设备工作是否正常。");
        }
    };

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setManager = new SettingManager(getActivity().getApplicationContext());
        mCenter = CmdCenter.getInstance(getActivity().getApplicationContext());
    }

    /**
     * 显示等待框
     */
    protected void showWaitDialog() {
        LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_wait, null);
        Animation animation = AnimationUtils.loadAnimation(m_context, R.anim.alpha);
        view.findViewById(R.id.iv).startAnimation(animation);
        waitDialog = new Dialog(m_context, R.style.Translucent_NoTitle_trans);
        waitDialog.addContentView(view, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        waitDialog.setContentView(view);
        waitDialog.setCancelable(false);
        waitDialog.show();
        WindowManager.LayoutParams params = waitDialog.getWindow().getAttributes();
        params.y = -156;
        waitDialog.getWindow().setAttributes(params);
    }

    /**
     * 取消显示等待框
     */
    protected void dismissWaitDialog() {
        if (waitDialog != null) {
            waitDialog.dismiss();
        }
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
        if (waitDialog != null) {
            dismissWaitDialog();
            timeHandler.removeMessages(ProtocolConstants.TIME_OUT);
        }
    }
    public interface GPSDataChangeListener {
        void gpsCallBack(LatLng desLat, TracksManager.TrackPoint trackPoint);
    }
}

