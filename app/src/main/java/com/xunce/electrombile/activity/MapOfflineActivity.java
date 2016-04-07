package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.orhanobut.logger.Logger;
import com.xunce.electrombile.Adapter.OfflineMap_ExpandableList_Adapter;
import com.xunce.electrombile.Adapter.OfflineMap_Manager_Adapter;
import com.xunce.electrombile.R;
import com.xunce.electrombile.bean.OfflinemapBean;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.util.ArrayList;

import static com.baidu.mapapi.map.offline.MKOLUpdateElement.DOWNLOADING;
import static com.baidu.mapapi.map.offline.MKOLUpdateElement.FINISHED;
import static com.baidu.mapapi.map.offline.MKOLUpdateElement.SUSPENDED;
import static com.baidu.mapapi.map.offline.MKOLUpdateElement.WAITING;

/**
 * Created by Xingw on 2015/12/10.
 */
public class MapOfflineActivity extends Activity implements MKOfflineMapListener {
    private static MKOfflineMap mkOfflineMap = null;
    private ArrayList<MKOLUpdateElement> localMapList = null;
    private static OfflineMap_ExpandableList_Adapter ExpandableListadapter;
    private static OfflineMap_Manager_Adapter ManagerListadapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapoffline);
        mkOfflineMap = new MKOfflineMap();
        mkOfflineMap.init(this);
        initView();

    }

    private void initView() {
        View v = findViewById(R.id.ll_button);
        TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
        tv_title.setText("地图下载");
        RelativeLayout btn_back = (RelativeLayout)v.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ExpandableListView allCityList = (ExpandableListView) findViewById(R.id.lv_mapOffline_allCity);
        ListView managerList = (ListView) findViewById(R.id.lv_mapOffline_manager);
        ArrayList<OfflinemapBean> allCities = new ArrayList<OfflinemapBean>();
        //获取城市列表
        ArrayList<MKOLSearchRecord> records = mkOfflineMap.getOfflineCityList();
        if (records != null) {
            for (MKOLSearchRecord r : records) {
                OfflinemapBean bean = new OfflinemapBean();
                bean.setCityName(r.cityName);
                bean.setCityId(r.cityID);
                bean.setState(OfflinemapBean.STATE.NONE);
                ArrayList<OfflinemapBean> childCities = new ArrayList<OfflinemapBean>();
                if (r.childCities != null) {
                    for (MKOLSearchRecord childCity : r.childCities) {
                        OfflinemapBean childbean = new OfflinemapBean();
                        childbean.setCityName(childCity.cityName + "(" + childCity.cityID + ")" + "   --"
                                + this.formatDataSize(childCity.size));
                        childbean.setCityId(childCity.cityID);
                        childbean.setState(OfflinemapBean.STATE.NONE);
                        childCities.add(childbean);
                    }
                } else {
                    OfflinemapBean childbean = new OfflinemapBean();
                    childbean.setCityName(r.cityName + "(" + r.cityID + ")" + "   --"
                            + this.formatDataSize(r.size));
                    childbean.setCityId(r.cityID);
                    childbean.setState(OfflinemapBean.STATE.NONE);
                    childCities.add(childbean);
                }
                bean.setChildCities(childCities);
                allCities.add(bean);
            }
            ExpandableListadapter = new OfflineMap_ExpandableList_Adapter(allCities, this, mkOfflineMap);
            allCityList.setAdapter(ExpandableListadapter);
            //根据本地信息更新状态
            localMapList = mkOfflineMap.getAllUpdateInfo();
            if (localMapList == null) {
                localMapList = new ArrayList<MKOLUpdateElement>();
            } else {
                for (MKOLUpdateElement element : localMapList) {
                    switch (element.status) {
                        case FINISHED:
                            ExpandableListadapter.getBeanbyId(element.cityID).setState(OfflinemapBean.STATE
                                    .FINISHED);
                            break;
                        case WAITING:
                            ExpandableListadapter.getBeanbyId(element.cityID).setState(OfflinemapBean.STATE.LOADING);
                            break;
                        case DOWNLOADING:
                            ExpandableListadapter.getBeanbyId(element.cityID).setState(OfflinemapBean.STATE.LOADING);
                            ExpandableListadapter.getBeanbyId(element.cityID).setProgress(element.ratio);
                            break;
                        case SUSPENDED:
                            ExpandableListadapter.getBeanbyId(element.cityID).setState(OfflinemapBean.STATE.SUSPENDED);
                    }
                }
                ExpandableListadapter.notifyDataSetChanged();
            }
        } else {
            ToastUtils.showLong(this, "获取城市列表失败，请检查网络");
        }
        ManagerListadapter = new OfflineMap_Manager_Adapter(this, mkOfflineMap);
        managerList.setAdapter(ManagerListadapter);
    }

    public String formatDataSize(int size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }


    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                // 处理下载进度更新提示
                    Logger.i("下载更新");
                    MKOLUpdateElement update = mkOfflineMap.getUpdateInfo(state);
                    // 处理下载进度更新提示
                    if (update != null) {
                        if (update.ratio == 100) {
                            ExpandableListadapter.getBeanbyId(state).setState(OfflinemapBean.STATE.FINISHED);
                        } else {
                            ExpandableListadapter.setDownProgress(update.ratio, state);
                        }
                        ExpandableListadapter.updateView();
                    }
                    ManagerListadapter.updateView();
                }
                break;

            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Logger.i("新的离线地图安装");
//                Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                Logger.i("版本更新");
                MKOLUpdateElement update = mkOfflineMap.getUpdateInfo(state);
                if (update != null) {
                    ExpandableListadapter.getBeanbyId(state).setState(OfflinemapBean.STATE.UPDATE);
                }
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                break;
        }
    }

    /**
     * 开始下载
     */
    public static void downloadCity(int cityId) {
        com.orhanobut.logger.Logger.i("开始下载离线地图ID是：" + cityId);
        ExpandableListadapter.getBeanbyId(cityId).setState(OfflinemapBean.STATE.LOADING);
        //MapOfflineActivity.ManagernotifyDataSetChanged();
        mkOfflineMap.start(cityId);
        ExpandableListadapter.updateView();
        ManagerListadapter.updateView();
    }

    /**
     * 暂停下载
     */
    public static void stop(int cityId) {
        mkOfflineMap.pause(cityId);
        ExpandableListadapter.getBeanbyId(cityId).setState(OfflinemapBean.STATE.SUSPENDED);
        ExpandableListadapter.updateView();
        ManagerListadapter.updateView();
    }

    /**
     * 删除离线地图
     */
    public static void remove(int cityId) {
        mkOfflineMap.remove(cityId);
        OfflinemapBean bean = ExpandableListadapter.getBeanbyId(cityId);
        if (bean != null) {
            bean.setState(OfflinemapBean.STATE.NONE);
            bean.setProgress(0);
        }
        //MapOfflineActivity.ManagernotifyDataSetChanged();
        ExpandableListadapter.updateView();
        ManagerListadapter.updateView();
    }
}
