package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.AboutActivity;
import com.xunce.electrombile.activity.DeviceActivity;
import com.xunce.electrombile.activity.HelpActivity;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.activity.account.PersonalCenterActivity;

//import io.yunba.android.manager.YunBaManager;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private static String TAG = "SettingsFragment:";
    private Context m_context;
   // private LinearLayout btnPhoneNumber;
   //  private LinearLayout btnBind;
    private LinearLayout btnAbout;
    private LinearLayout btnHelp;
    //  private LinearLayout releaseBind;
    // private LinearLayout btnAddSOS;
    private LinearLayout btnLogout;

    //缓存view
    private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        initView();
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.settings_fragment, container, false);
        }
		return rootView;
	}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) (view.findViewById(R.id.tv_imei))).setText("设备号：" + setManager.getIMEI());
//        view.findViewById(R.id.layout_bind).setOnClickListener(this);
        view.findViewById(R.id.layout_about).setOnClickListener(this);
        view.findViewById(R.id.layout_help).setOnClickListener(this);
//        view.findViewById(R.id.layout_release_bind).setOnClickListener(this);
        view.findViewById(R.id.btn_logout).setOnClickListener(this);
        view.findViewById(R.id.layout_person_center).setOnClickListener(this);
//        view.findViewById(R.id.layout_addSOS).setOnClickListener(this);
//        view.findViewById(R.id.layout_bind_list).setOnClickListener(this);
//        view.findViewById(R.id.layout_find).setOnClickListener(this);
        view.findViewById(R.id.rl_1).setOnClickListener(this);
        //releaseBind = (LinearLayout) view.findViewById(R.id.layout_release_bind);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        m_context = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
//            case R.id.layout_bind:
//                //systemBtnClicked();
//                bind();
//                break;
            case R.id.layout_help:
                Intent intentHelp = new Intent(m_context, HelpActivity.class);
                startActivity(intentHelp);
                break;
            case R.id.btn_logout:
                loginOut();
                break;
            case R.id.layout_about:
                Intent intentAbout = new Intent(m_context, AboutActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.layout_person_center:
                goToPersonCenterAct();
                break;
            case R.id.rl_1:
                goToDeviceAct();
                break;
//            case R.id.layout_release_bind:
//                releaseBind_btn();
//                break;
//            case R.id.layout_addSOS:
//                if(!NetworkUtils.isNetworkConnected(m_context)){
//                    ToastUtils.showShort(m_context,"网络连接错误！");
//                    return ;
//                }
//                if(setManager.getIMEI().isEmpty()) {
//                    ToastUtils.showShort(m_context,"请先绑定设备！");
//                    return;
//                }
//                Intent intent = new Intent(m_context, AddSosActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.layout_bind_list:
//                if (!NetworkUtils.isNetworkConnected(m_context)) {
//                    ToastUtils.showShort(m_context, "网络连接错误！");
//                    return;
//                }
//                Intent intentBindList = new Intent(m_context, BindListActivity.class);
//                startActivity(intentBindList);
//                break;
//            case R.id.layout_find:
//                goToFindAct();
//                break;
            default:
                break;
        }
    }

    private void goToDeviceAct() {
        Intent intent = new Intent(m_context, DeviceActivity.class);
        startActivity(intent);
    }

    private void goToPersonCenterAct() {
        Intent intent = new Intent(m_context, PersonalCenterActivity.class);
        startActivity(intent);
    }

//    private void goToFindAct() {
//        Intent intent = new Intent(m_context, FindActivity.class);
//        startActivity(intent);
//    }

//    private void releaseBind_btn() {
//        AlertDialog dialog2 = new AlertDialog.Builder(getActivity())
//                .setTitle("*****解除绑定*****")
//                .setMessage("将要解除绑定的设备，确定解除么？")
//                .setPositiveButton("否",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        releaseBind.setClickable(false);
//                        releaseBinding();
//                        releaseBind.setClickable(true);
//                    }
//                }).create();
//        dialog2.show();
//    }

//    private void bind() {
//        if (NetworkUtils.isNetworkConnected(m_context)) {
//            if (setManager.getIMEI().isEmpty()) {
//                //      Log.i(TAG, "clicked item layout_relieve_bind");
//                setManager.cleanDevice();
//                Intent intentStartBinding = new Intent(m_context, BindingActivity.class);
//                startActivity(intentStartBinding);
//                getActivity().finish();
//            } else {
//                System.out.println(setManager.getIMEI() + "aaaaaaaaaaa");
//                ToastUtils.showShort(m_context, "设备已绑定");
//            }
//        } else {
//            ToastUtils.showShort(m_context, "网络连接错误");
//        }
//    }

    private void loginOut() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("退出登录")
                .setMessage("退出登录将清除所有已有账户及已经绑定的设备，确定退出么？")
                .setPositiveButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String topic = "e2link_" + setManager.getIMEI();
//                        YunBaManager.unsubscribe(m_context, topic, new IMqttActionListener() {
//
//                            @Override
//                            public void onSuccess(IMqttToken arg0) {
//                                Log.d(TAG, "UnSubscribe topic succeed");
//                                //删除本地的IMEI 和报警标志
//                                setManager.setIMEI("");
//                                setManager.setAlarmFlag(false);
//                                ToastUtils.showShort(m_context, "退出登录成功");
//                                setManager.cleanAll();
//                            }
//
//                            @Override
//                            public void onFailure(IMqttToken arg0, Throwable arg1) {
//                                Log.d(TAG, "UnSubscribe topic failed");
//                                ToastUtils.showShort(m_context, "退订服务失败，请确保网络通畅！");
//                            }
//                        });
                        Intent intentStartLogin = new Intent(m_context, LoginActivity.class);
                        startActivity(intentStartLogin);
                        AVUser.logOut();
                        getActivity().finish();
                    }
                }).create();
        dialog.show();
    }

//    private void releaseBinding() {
//        //先判断IMEI是否为空，若为空证明没有绑定设备。
//        if(setManager.getIMEI().isEmpty()){
//            ToastUtils.showShort(m_context,"未绑定设备");
//            return;
//        }
//        if(!NetworkUtils.isNetworkConnected(m_context)){
//            ToastUtils.showShort(m_context,"网络连接失败");
//            return;
//        }
//        //若不为空，则先查询所在绑定类，再删除，删除成功后取消订阅，并删除本地的IMEI，关闭FragmentActivity,进入绑定页面
//        AVQuery<AVObject> query = new AVQuery<>("Bindings");
//        String IMEI = setManager.getIMEI();
//        query.whereEqualTo("IMEI", IMEI);
//        query.findInBackground(new FindCallback<AVObject>() {
//            @Override
//            public void done(List<AVObject> avObjects, AVException e) {
//                if (e == null && avObjects.size() > 0) {
//                    AVObject bindClass = avObjects.get(0);
//                    bindClass.deleteInBackground(new DeleteCallback() {
//                        @Override
//                        public void done(AVException e) {
//                            if (e == null) {
//                                String topic = "e2link_" + setManager.getIMEI();
//                                Log.i(TAG + "SSSSSSSSSS", topic);
//                                //退订云巴推送
//                                YunBaManager.unsubscribe(m_context, topic, new IMqttActionListener() {
//
//                                    @Override
//                                    public void onSuccess(IMqttToken arg0) {
//                                        Log.d(TAG, "UnSubscribe topic succeed");
//                                        //删除本地的IMEI 和报警标志
//                                        setManager.setIMEI("");
//                                        setManager.setAlarmFlag(false);
//                                        ToastUtils.showShort(m_context, "解除绑定成功!");
//                                        Intent intent = new Intent(m_context, BindingActivity.class);
//                                        startActivity(intent);
//                                        getActivity().finish();
//                                    }
//
//                                    @Override
//                                    public void onFailure(IMqttToken arg0, Throwable arg1) {
//                                        Log.d(TAG, "UnSubscribe topic failed");
//                                        ToastUtils.showShort(m_context, "解除绑定失败，请确保网络通畅！");
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } else {
//                    if(e != null)
//                        Log.d("失败", "问题： " + e.getMessage());
//                    ToastUtils.showShort(m_context, "解除绑定失败!");
//                }
//            }
//        });
//
//
//    }

    private void initView() {
//        btnBind = (LinearLayout)getActivity().findViewById(R.id.layout_bind);
        btnAbout = (LinearLayout)getActivity().findViewById(R.id.layout_about);
        btnHelp = (LinearLayout)getActivity().findViewById(R.id.layout_help);
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
