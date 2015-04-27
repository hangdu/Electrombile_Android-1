package com.xunce.electrombile.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.xunce.electrombile.R;
import com.xunce.electrombile.UniversalTool.VibratorUtil;
import com.xunce.electrombile.activity.AlarmActivity;
import com.xunce.electrombile.xpg.common.system.IntentUtils;
import com.xunce.electrombile.xpg.common.useful.NetworkUtils;

public class SwitchFragment extends BaseFragment implements OnGetGeoCoderResultListener {

    private static String TAG = "SwitchFragment:";
    private final int IS_FINISH = 1;
    private boolean systemState = false;
    private boolean alarmState = false;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

    private String[] SWITCHKEY = {
            "switch",
            "ring"
    };


    private Button btnAlarm;
    private ToggleButton btnSystem;
    private Button btnTest;
    private ImageView iv_SystemState;

    //textview 设置当前位置
    private TextView switch_fragment_tvLocation;


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnSystem = (ToggleButton) getActivity().findViewById(R.id.btn_SystemState);
        switch_fragment_tvLocation = (TextView) getActivity().findViewById(R.id.switch_fragment_tvLocation);
        iv_SystemState = (ImageView) getActivity().findViewById(R.id.iv_SystemState);
        btnSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    if(mXpgWifiDevice !=null) {
                        mCenter.alarmFlag = true;
                        //mCenter.cGetStatus(mXpgWifiDevice);
                        //  mCenter.cGprsSend(mXpgWifiDevice);
                        VibratorUtil.Vibrate(getActivity(),1000);
                        iv_SystemState.setBackgroundResource(R.drawable.switch_fragment_zhuangtai1);
                    }else{
                        btnSystem.setChecked(false);
                        iv_SystemState.setBackgroundResource(R.drawable.switch_fragment_zhuangtai2);
                    }
                }else{
                    mCenter.alarmFlag =false;
                    iv_SystemState.setBackgroundResource(R.drawable.switch_fragment_zhuangtai2);
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called!");
        return inflater.inflate(R.layout.switch_fragment, container, false);
    }

    public void reverserGeoCedec(LatLng pCenter){
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(pCenter));
    }
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        switch_fragment_tvLocation.setText(result.getAddress());
    }

}
