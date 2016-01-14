package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.AboutActivity;
import com.xunce.electrombile.activity.Autolock;
import com.xunce.electrombile.activity.CarManageActivity;
import com.xunce.electrombile.activity.DeviceActivity;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.activity.HelpActivity;
import com.xunce.electrombile.activity.TestActivity;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.activity.account.PersonalCenterActivity;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private static String TAG = "SettingsFragment";
    //临时变量
    public int temp = 0;
    //缓存view
    private View rootView;
    private String AutoLockStatus;
    private String new_AutoLockStatus;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    //没有重写oncreate函数

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.settings_fragment, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

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
                if (!hasUser()) {
                    return;
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
                if (!hasUser()) {
                    return;
                }
                goToPersonCenterAct();
                break;
            //设备管理条目
            case R.id.rl_1:
                if (!hasUser()) {
                    return;
                }
                goToDeviceAct();
                break;

            //自动落锁
            case R.id.layout_autolock:
                gotoAutolockAct();
                break;


            case R.id.rl_1l:
                int what = 999;
                m_context.timeHandler.removeMessages(what);
                if (temp == 10) {
                    Intent intent = new Intent(m_context, TestActivity.class);
                    startActivity(intent);
                    temp = 0;
                    return;
                }
                temp += 1;
                Message msg = Message.obtain();
                msg.what = what;
                m_context.timeHandler.sendMessageDelayed(msg, 3000);

            default:
                break;
        }
    }

    /**
     * 判断是否有用户
     *
     * @return
     */
    private boolean hasUser() {
        if (AVUser.getCurrentUser() == null) {
            Intent intent = new Intent(m_context, LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return false;
        }
        return true;
    }

    /**
     * 跳转到设备界面
     */
    private void goToDeviceAct() {
//        Intent intent = new Intent(m_context, DeviceActivity.class);
        Intent intent = new Intent(m_context, CarManageActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转到个人中心界面
     */
    private void goToPersonCenterAct() {
        Intent intent = new Intent(m_context, PersonalCenterActivity.class);
        startActivity(intent);
    }

    private void gotoAutolockAct(){
        Intent intent = new Intent(m_context, Autolock.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1){
            new_AutoLockStatus = (m_context).setManager.getAutoLockStatus();


        }
    }

    /**
     * 退出登录
     */
    private void loginOut() {
        //解析xml
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_logout, null);
        final Dialog dialog = new Dialog(m_context, R.style.Translucent_NoTitle_white);

        //找按钮
        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        TextView message = (TextView) view.findViewById(R.id.dialog_message);
        TextView confirm = (TextView) view.findViewById(R.id.dialog_confirm);
        TextView cancel = (TextView) view.findViewById(R.id.dialog_cancel);

        //设置对应的事件
        title.setText("退出登录");
        message.setText("将删除本地所有信息，确定退出么？");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unSubscribe(((FragmentActivity) m_context).getMac());
                Intent intent;
                intent = new Intent();
                intent.setAction("com.xunce.electrombile.alarmservice");
                intent.setPackage(m_context.getPackageName());
                m_context.stopService(intent);
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
        dialog.show();

        //设置宽度
        WindowManager m = ((FragmentActivity) m_context).getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); // 获取对话框当前的参数值
//        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        //虽然过时,为了兼容性,还是用老方法. API 13以上才能使用新方法
        p.width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65
        dialog.getWindow().setAttributes(p);
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
        if (!TextUtils.isEmpty(initTopic)) {
            try {
                mac.unsubscribe(topic);
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                ToastUtils.showShort(m_context, "取消订阅失败!请稍后重启再试！");
                return false;
            }
        } else {
            return true;
        }

    }

    /**
     * 初始化布局
     */
    private void initView(View view) {
//        ((TextView) (view.findViewById(R.id.tv_imei))).setText("设备号：" + setManager.getIMEI());
        view.findViewById(R.id.layout_about).setOnClickListener(this);
        view.findViewById(R.id.layout_help).setOnClickListener(this);
        view.findViewById(R.id.btn_logout).setOnClickListener(this);
        view.findViewById(R.id.layout_person_center).setOnClickListener(this);
        view.findViewById(R.id.rl_1).setOnClickListener(this);
        view.findViewById(R.id.rl_1l).setOnClickListener(this);
        view.findViewById(R.id.layout_autolock).setOnClickListener(this);

        AutoLockStatus = (m_context).setManager.getAutoLockStatus();
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }
}
