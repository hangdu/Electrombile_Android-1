package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.AboutActivity;
import com.xunce.electrombile.activity.DeviceActivity;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.activity.HelpActivity;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.activity.account.PersonalCenterActivity;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;



public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private static String TAG = "SettingsFragment:";
    private LinearLayout btnAbout;
    private LinearLayout btnHelp;
    private LinearLayout btnLogout;

    //缓存view
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initView();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.settings_fragment, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) (view.findViewById(R.id.tv_imei))).setText("设备号：" + setManager.getIMEI());
        view.findViewById(R.id.layout_about).setOnClickListener(this);
        view.findViewById(R.id.layout_help).setOnClickListener(this);
        view.findViewById(R.id.btn_logout).setOnClickListener(this);
        view.findViewById(R.id.layout_person_center).setOnClickListener(this);
        view.findViewById(R.id.rl_1).setOnClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            //帮助条目
            case R.id.layout_help:
                Intent intentHelp = new Intent(m_context, HelpActivity.class);
                startActivity(intentHelp);
                break;
            //退出登录条目
            case R.id.btn_logout:
                if (!NetworkUtils.isNetworkConnected(m_context)) {
                    ToastUtils.showShort(m_context, "请先连接网络!");
                    return;
                }
                if (AVUser.getCurrentUser() == null) {
                    Intent intent = new Intent(m_context, LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                loginOut();
                break;
            //关于条目
            case R.id.layout_about:
                Intent intentAbout = new Intent(m_context, AboutActivity.class);
                startActivity(intentAbout);
                break;
            //个人中心条目
            case R.id.layout_person_center:
                goToPersonCenterAct();
                break;
            //设备管理条目
            case R.id.rl_1:
                goToDeviceAct();
                break;
            default:
                break;
        }
    }

    /**
     * 跳转到设备界面
     */
    private void goToDeviceAct() {
        Intent intent = new Intent(m_context, DeviceActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转到个人中心界面
     */
    private void goToPersonCenterAct() {
        Intent intent = new Intent(m_context, PersonalCenterActivity.class);
        startActivity(intent);
    }

    /**
     * 退出登录
     */
    private void loginOut() {
        //解析xml
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_logout, null);
        final Dialog dialog = new Dialog(m_context, R.style.Translucent_NoTitle);

        //找按钮
        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        TextView message = (TextView) view.findViewById(R.id.dialog_message);
        TextView confirm = (TextView) view.findViewById(R.id.dialog_confirm);
        TextView cancel = (TextView) view.findViewById(R.id.dialog_cancel);

        //设置对应的事件
        title.setText("退出登录");
        message.setText("退出登录将清除所有已有账户及已经绑定的设备，确定退出么？");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unSubscribe(((FragmentActivity) m_context).getMac());
                Intent intent;
                intent = new Intent("com.xunce.electrombile.alarmservice");
                m_context.stopService(intent);
                setManager.setIMEI("");
                setManager.setAlarmFlag(false);
                ToastUtils.showShort(m_context, "退出登录成功");
                setManager.cleanAll();
                intent = new Intent(m_context, LoginActivity.class);
                startActivity(intent);
                AVUser.logOut();
                getActivity().finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //设置布局
        dialog.addContentView(view, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.setContentView(view);
        dialog.show();

        //设置宽度
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = 900;
        dialog.getWindow().setAttributes(params);
    }

    /**
     * 取消订阅
     *
     * @param mac mqtt连接
     * @return
     */
    private boolean unSubscribe(MqttAndroidClient mac) {
        //订阅命令字
        String initTopic = setManager.getIMEI();
        String topic1 = "dev2app/" + initTopic + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + initTopic + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + initTopic + "/433";

        String topic4 = "dev2app/" + initTopic + "/alarm";
        String[] topic = {topic1, topic2, topic3, topic4};
        try {
            mac.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(m_context, "取消订阅失败!请稍后重启再试！");
            return false;
        }

    }

    /**
     * 初始化布局
     */
    private void initView() {
//        btnBind = (LinearLayout)getActivity().findViewById(R.id.layout_bind);
        btnAbout = (LinearLayout) getActivity().findViewById(R.id.layout_about);
        btnHelp = (LinearLayout) getActivity().findViewById(R.id.layout_help);
        btnLogout = (LinearLayout) getActivity().findViewById(R.id.btn_logout);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }
}
