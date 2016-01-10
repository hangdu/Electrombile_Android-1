package com.xunce.electrombile.activity;

import android.content.Context;
import android.widget.Toast;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


/**
 * Created by lybvinci on 15/12/19.
 */
public class GeoCodering implements OnGetGeoCoderResultListener {

    com.baidu.mapapi.search.geocode.GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    Context context;
    LatLng point;
    int TrackPosition;
    int Start_End_Type;
    TestddActivity activity;
    String ReverseGeoCodeResult;

    public GeoCodering(TestddActivity activity,LatLng point,int TrackPosition,int Start_End_Type){
        this.activity = activity;
        this.context = activity;
        this.point = point;
        this.TrackPosition = TrackPosition;
        this.Start_End_Type = Start_End_Type;
        init();
    }

    void init() {
        SDKInitializer.initialize(context);
        // 初始化搜索模块，注册事件监听
        mSearch = com.baidu.mapapi.search.geocode.GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        // 反Geo搜索
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(point));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(context, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//            Toast.makeText(context, "抱歉，未能找到结果", Toast.LENGTH_LONG)
//                    .show();
            return;
        }
        ReverseGeoCodeResult = result.getAddress();

        //是由testddactivity调用的
        activity.RefreshMessageList(TrackPosition, Start_End_Type, result.getAddress());
    }
}

