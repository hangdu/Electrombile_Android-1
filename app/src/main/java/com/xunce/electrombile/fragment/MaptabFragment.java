package com.xunce.electrombile.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.LogUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BindingActivity;
import com.xunce.electrombile.activity.FindActivity;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.activity.RecordActivity;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.manager.TracksManager.TrackPoint;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MaptabFragment extends BaseFragment {


    //保存数据所需要的最短距离
    private static final double MIN_DISTANCE = 100;
    private static final int DELAY = 5000;
    public static MapView mMapView;
    //maptabFragment 维护一组历史轨迹坐标列表
    public static List<TrackPoint> trackDataList;
    private static String TAG = "MaptabFragment:";
    private final String KET_LONG = "lon";
    private final String KET_LAT = "lat";
    //获取位置信息的http接口
    private final String httpBase = "http://api.gizwits.com/app/devdata/";
    public TrackPoint currentTrack;
    //正在播放轨迹标志
    public boolean isPlaying = false;
    private TextView btnLocation;
    private TextView btnRecord;
    private TextView tvFindEle;
    private Button btnPlay;
    private Button btnPause;
    private Button btnClearTrack;
    //电动车标志
    private Marker markerMobile;
    private MarkerOptions option2;
    //轨迹图层
    private Overlay tracksOverlay;
    private TextView tvUpdateTime;
    private TextView tvStatus;
    private InfoWindow mInfoWindow;
    private View markerView;
    private LinearLayout ll_map;
    //dialogs
    private Dialog networkDialog;
    private Dialog didDialog;

    private int playOrder = 0;
    private BaiduMap mBaiduMap;

    //缓存布局
    private View rootView;
    //轨迹显示图层
    private Overlay lineDraw;
    //移动图标的动画
    private ImageView moveMarker;
    private MapViewLayoutParams moveMarkerParams;
    /**
     * 处理消息
     */
    private Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleKey key = handleKey.values()[msg.what];
            switch (key) {
                case CHANGE_POINT:
                    try {
                        Log.i(TAG, "playOrder:" + playOrder);
                        if ((int) msg.obj < trackDataList.size()) {
                            reSetMoveMarkerLocation(trackDataList.get((int) msg.obj).point);
                            playLocateMobile((int) msg.obj);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SET_MARKER: {
                    if (msg.obj != null) {
                        TrackPoint trackPoint = (TrackPoint) msg.obj;
                        markerMobile.setPosition(trackPoint.point);
                        reSetMoveMarkerLocation(trackPoint.point);
//                        markerMobile.setVisible(true);
                        mInfoWindow = new InfoWindow(markerView, trackPoint.point, -100);
                        SimpleDateFormat sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        tvUpdateTime.setText(sdfWithSecond.format(trackPoint.time));
                        mBaiduMap.showInfoWindow(mInfoWindow);
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Log.i(TAG, "onCreate called!");
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(this.m_context);

        trackDataList = new ArrayList<>();
        currentTrack = new TrackPoint(new Date(), 0, 0);
        LayoutInflater inflater = (LayoutInflater) m_context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        markerView = inflater.inflate(R.layout.view_marker, null);
        tvUpdateTime = (TextView) markerView.findViewById(R.id.tv_updateTime);
        tvStatus = (TextView) markerView.findViewById(R.id.tv_statuse);

        didDialog = new AlertDialog.Builder(m_context).setMessage(R.string.bindErrorSet)
                .setTitle(R.string.bindSet)
                .setPositiveButton(R.string.goBind, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent;
                        intent = new Intent(m_context, BindingActivity.class);
                        m_context.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.i(TAG, "onCreateView called!");
        if (rootView == null) {
            View view = inflater.inflate(R.layout.map_fragment, container, false);
            initView(view);
            rootView = view;
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //定义Maker坐标点
        //leacloud服务器清空，暂时自定义数据代替
        LatLng point;
        if ((!setManager.getInitLocationLat().isEmpty()) && (!setManager.getInitLocationLongitude().isEmpty())) {
            LogUtil.log.i("lat:::" + Double.valueOf(setManager.getInitLocationLat()));
            LogUtil.log.i("longitude:::" + Double.valueOf(setManager.getInitLocationLongitude()));
            point = new LatLng(Double.valueOf(setManager.getInitLocationLat()),
                    Double.valueOf(setManager.getInitLocationLongitude()));
            point = mCenter.convertPoint(point);
        } else {
            LogUtil.log.i("到了初始位置？");
            point = new LatLng(30.5171, 114.4392);
        }

        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);
        //构建MarkerOption，用于在地图上添加Marker
        option2 = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        markerMobile = (Marker) mBaiduMap.addOverlay(option2);
        markerMobile.setVisible(false);


        moveMarkerParams = new MapViewLayoutParams.Builder()
                .layoutMode(MapViewLayoutParams.ELayoutMode.mapMode)
                .height(100)
                .width(100)
                .position(markerMobile.getPosition())
                .build();
        mMapView.addView(moveMarker, moveMarkerParams);

        //将电动车位置移至中心
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(mBaiduMap.getMapStatus().zoom * Double.valueOf(1.5).floatValue())
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void onDestroy() {
        if (lineDraw != null)
            lineDraw.remove();
        //清除轨迹
        if (tracksOverlay != null)
            tracksOverlay.remove();
        //结束播放
        pausePlay();
        exitPlayTrackMode();
        mBaiduMap.clear();
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        //   Log.i(TAG, "onResume called!");
        mMapView.setVisibility(View.VISIBLE);
        mMapView.onResume();
        super.onResume();

        //检查历史轨迹列表，若不为空，则需要绘制轨迹
        if (trackDataList.size() > 0) {
            reSetMoveMarkerLocation(trackDataList.get(0).point);
            enterPlayTrackMode();
            drawLine();
        } else {
            updateLocation();
        }
    }

    @Override
    public void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

    /**
     * 初始化view
     *
     * @param v view
     */
    private void initView(View v) {
        mMapView = (MapView) v.findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
//                if(marker == markerMobile){
//                //    mBaiduMap.hideInfoWindow();
//                }
                return true;
            }
        });

        //开始/暂停播放按钮
        btnPlay = (Button) v.findViewById(R.id.btn_play);
        btnPlay.setVisibility(View.INVISIBLE);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playHandler.removeMessages(handleKey.SET_MARKER.ordinal());
                Message msg = Message.obtain();
                msg.what = handleKey.CHANGE_POINT.ordinal();
                msg.obj = playOrder;
                playHandler.sendMessage(msg);
            }
        });

        btnPause = (Button) v.findViewById(R.id.btn_pause);
        btnPause.setVisibility(View.INVISIBLE);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sendPlayMessage();
                pausePlay();
            }
        });

        //定位电动车按钮
        btnLocation = (TextView) v.findViewById(R.id.btn_location);
        tvFindEle = (TextView) v.findViewById(R.id.tv_find_ele);
        tvFindEle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查网络
                if (checkNetwork()) return;
                //检查是否绑定
                if (checkBind()) return;
                Intent intent = new Intent(m_context, FindActivity.class);
                startActivity(intent);
            }
        });
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查网络
                if (checkNetwork()) return;
                //检查是否绑定
                if (checkBind()) return;

                if (mBaiduMap != null) {
                    //LatLng point = getLatestLocation();
                    showWaitDialog();
                    timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE);
                    updateLocation();
                }
            }
        });

        //历史记录按钮
        btnRecord = (TextView) v.findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //检查网络
                if (checkNetwork()) return;
                //检查是否绑定
                if (checkBind()) return;
                clearDataAndView();

                Intent intent = new Intent(m_context, RecordActivity.class);
                startActivity(intent);
            }
        });

        //退出查看历史轨迹按钮
        btnClearTrack = (Button) v.findViewById(R.id.btn_cancel_track);
        btnClearTrack.setVisibility(View.INVISIBLE);
        btnClearTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("确定要退出历史轨迹查看模式？")
                        .setPositiveButton("否",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                    }
                                }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearDataAndView();
                                TracksManager.clearTracks();
                                updateLocation();
                            }
                        }).create();
                dialog.show();
            }
        });
        //移动的图标
        moveMarker = new ImageView(m_context);
        moveMarker.setImageResource(R.drawable.move_marker);

        //按钮容器
        ll_map = (LinearLayout) v.findViewById(R.id.ll_map);
    }

    /**
     * 重新设置车辆图标位置
     */
    private void reSetMoveMarkerLocation(LatLng point) {
        mMapView.removeView(moveMarker);
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(mBaiduMap.getMapStatus().zoom)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
        moveMarkerParams = new MapViewLayoutParams.Builder()
                .layoutMode(MapViewLayoutParams.ELayoutMode.mapMode)
                .height(100)
                .width(100)
                .position(point)
                .build();
        mMapView.addView(moveMarker, moveMarkerParams);
        LogUtil.log.e(TAG, "reSetMoveMarkerLocation");

    }

    /**
     * 进入历史轨迹播放模式
     */
    private void enterPlayTrackMode() {
        isPlaying = true;
        playOrder = 0;
        playHandler.removeMessages(handleKey.SET_MARKER.ordinal());
        mBaiduMap.hideInfoWindow();
//        markerMobile.setVisible(false);
        btnClearTrack.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        ((FragmentActivity) m_context).getMain_radio().setVisibility(View.GONE);
        ll_map.setVisibility(View.GONE);

    }

    /**
     * 退出历史轨迹播放模式
     */
    private void exitPlayTrackMode() {
        isPlaying = false;
        playOrder = 0;
        mBaiduMap.showInfoWindow(mInfoWindow);
//        markerMobile.setVisible(true);
        btnClearTrack.setVisibility(View.INVISIBLE);
        btnPlay.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.INVISIBLE);
        ((FragmentActivity) m_context).getMain_radio().setVisibility(View.VISIBLE);
        ll_map.setVisibility(View.VISIBLE);
    }

    /**
     * 将地图中心移到某点
     */
    public void locateMobile(TrackPoint track) {
        if (mBaiduMap == null) return;
        if (isPlaying) {
            refreshTrack(track);
            return;
        }

//        markerMobile.setVisible(false);
        mBaiduMap.hideInfoWindow();
        Point p1 = mBaiduMap.getProjection().toScreenLocation(markerMobile.getPosition());
        Point p2 = mBaiduMap.getProjection().toScreenLocation(track.point);
        Log.e(TAG, "p1.x:" + p1.x + "p1.y:" + p1.y + "p2.x:" + p2.x + "p2.y:" + p2.y);
        carAnimation(p1, p2, DELAY);
        //延迟出现定位图标
        Message msg = Message.obtain();
        msg.what = handleKey.SET_MARKER.ordinal();
        msg.obj = track;
        playHandler.sendMessageDelayed(msg, DELAY);
        refreshTrack(track);

    }

    /**
     * 车的动画
     */
    private void carAnimation(Point p1, Point p2, int time) {
        Animation myAnimation_Translate = new TranslateAnimation(0, p2.x - p1.x, 0, p2.y - p1.y);
        myAnimation_Translate.setDuration(time);
        myAnimation_Translate.setFillEnabled(true);
        myAnimation_Translate.setFillAfter(true);
        myAnimation_Translate.setFillBefore(true);
        moveMarker.startAnimation(myAnimation_Translate);
    }


    /**
     * 更新当前轨迹
     */
    private void refreshTrack(TrackPoint track) {
        currentTrack = track;
        dismissWaitDialog();
    }

    /**
     * 播放历史轨迹的时候调用的绘图方法,减少了文本框的显示
     */
    private void playLocateMobile(int track) {
        if (mBaiduMap == null) return;
        Point p1 = mBaiduMap.getProjection().toScreenLocation(trackDataList.get(track).point);
        Point p2;
        if (track == 0) {
            Log.e(TAG, "track==" + track);
            p2 = mBaiduMap.getProjection().toScreenLocation(trackDataList.get(track + 1).point);
            carAnimation(p1, p2, DELAY);
        } else if ((track + 1) == (trackDataList.size())) {
            //不运动
            Log.e(TAG, "track==" + track);
            playOrder = 0;
            refreshTrack(trackDataList.get(track));
            return;
        } else {
            Log.e(TAG, "track==" + track);
            p2 = mBaiduMap.getProjection().toScreenLocation(trackDataList.get(track + 1).point);
            carAnimation(p1, p2, DELAY);
        }
        refreshTrack(trackDataList.get(track));
        playOrder += 1;
        sendPlayMessage(DELAY);
    }

    /**
     * 发送播放消息
     */
    private void sendPlayMessage(int delay) {
        Message msg = Message.obtain();
        msg.what = handleKey.CHANGE_POINT.ordinal();
        msg.obj = playOrder;
        playHandler.sendMessageDelayed(msg, delay);
    }


    /**
     * 获取最新的位置
     */
    public void updateLocation() {
        if (((FragmentActivity) m_context).mac != null && ((FragmentActivity) m_context).mac.isConnected())
            ((FragmentActivity) m_context).sendMessage(m_context, mCenter.cmdWhere(), setManager.getIMEI());

    }

    /**
     * 划线
     */
    private void drawLine() {
        mBaiduMap.clear();
        ArrayList<LatLng> points = new ArrayList<>();
        for (TrackPoint tp : trackDataList) {
            points.add(tp.point);
        }
        //构建用户绘制多边形的Option对象
        OverlayOptions polylineOption = new PolylineOptions()
                .points(points)
                .width(5)
                .color(0xAA00FF00);
        //在地图上添加多边形Option，用于显示
        tracksOverlay = mBaiduMap.addOverlay(polylineOption);
    }

    /**
     * 暂停播放
     */
    private void pausePlay() {
        playHandler.removeMessages(handleKey.CHANGE_POINT.ordinal());
    }

    /**
     * 检查是否已经绑定设备
     *
     * @return true绑定设备 false 未绑定
     */
    private boolean checkBind() {
        if (setManager.getIMEI().isEmpty()) {
            didDialog.show();
            return true;
        }
        return false;
    }

    /**
     * 检查网络
     *
     * @return 返回是否有网络连接
     */
    private boolean checkNetwork() {
        if (!NetworkUtils.isNetworkConnected(m_context)) {
            networkDialog = NetworkUtils.networkDialog(m_context, true);
            return true;
        }
        return false;
    }

    /**
     * 清楚数据和界面
     */
    private void clearDataAndView() {
        //清除轨迹
        if (tracksOverlay != null)
            tracksOverlay.remove();
        //结束播放线程
//        playHandler.removeMessages(handleKey.CHANGE_POINT.ordinal());
        pausePlay();
        //清除轨迹数
        trackDataList.clear();
        //退出播放轨迹模式
        exitPlayTrackMode();
    }

    //播放线程消息类型
    enum handleKey {
        CHANGE_POINT,
        SET_MARKER
    }
}
