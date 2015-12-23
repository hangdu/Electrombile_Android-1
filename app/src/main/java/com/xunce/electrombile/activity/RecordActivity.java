package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.xunce.electrombile.R;
import com.xunce.electrombile.bean.TracksBean;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.manager.TracksManager.TrackPoint;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by heyukun on 2015/4/18.
 */
public class RecordActivity extends Activity{

    private final String TAG = "RecordActivity";
    Button btnCuston;
    Button btnBegin;
    Button btnEnd;
    Button btnOK;
    Button btnOneDay;
    Button btnTwoDay;
    DatePicker dpBegin;
    DatePicker dpEnd;
    ListView m_listview;
    TracksManager tracksManager;

    //查询的开始和结束时间
    Date startT;
    Date endT;
    //生成动态数组，加入数据
    ArrayList<HashMap<String, Object>> listItem;
    //数据适配器
    SimpleAdapter listItemAdapter;
    //用来获取时间
    Calendar can;
    //查询失败对话框
    Dialog dialog;
    //管理应用数据的类
    SettingManager sm;
    SimpleDateFormat sdfWithSecond;
    SimpleDateFormat sdf;
    //需要跳过的个数
    int totalSkip;
    List<AVObject> totalAVObjects;
    //等待对话框
    private ProgressDialog watiDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        tracksManager = new TracksManager(getApplicationContext());
        can = Calendar.getInstance();
        sm = new SettingManager(this);

        sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfWithSecond.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        totalAVObjects = new ArrayList<AVObject>();
        initView();
        setCustonViewVisibility(false);
        m_listview.setVisibility(View.INVISIBLE);

        if (TracksBean.getInstance().getTracksData().size() != 0) {
            // Log.i(TAG, "TracksBean.getInstance().getTracksData().size()" + TracksBean.getInstance().getTracksData().size());
            m_listview.setVisibility(View.VISIBLE);
            tracksManager.clearTracks();
            tracksManager.setTracksData(TracksBean.getInstance().getTracksData());
            //Log.i(TAG, "TrackManager size:" + tracksManager.getTracks().size());
            updateListView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView(){
        watiDialog = new ProgressDialog(this);
        btnCuston = (Button)findViewById(R.id.btn_custom);
        btnCuston.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!btnBegin.isShown()) {
                    setCustonViewVisibility(true);
                    m_listview.setVisibility(View.INVISIBLE);
                }
                else {
                }
            }
        });
        btnOneDay = (Button)findViewById(R.id.btn_oneday);
        btnOneDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_listview.setVisibility(View.VISIBLE);
                setCustonViewVisibility(false);

                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                GregorianCalendar gcStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
                gcStart.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                startT= gcStart.getTime();

                //Log.i(TAG, "timezone:" + gcStart.getTimeZone().getDisplayName() + "Local:" + Locale.getDefault().getDisplayName());

                GregorianCalendar gcEnd = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
                gcEnd.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
                endT = gcEnd.getTime();
                totalSkip = 0;
                if(totalAVObjects != null)
                    totalAVObjects.clear();
                findCloud(startT, endT, 0);
            }
        });
        btnTwoDay = (Button)findViewById(R.id.btn_twoday);
        btnTwoDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_listview.setVisibility(View.VISIBLE);
                setCustonViewVisibility(false);




                //set start time and end time
                GregorianCalendar gcStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
                gcStart.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
                startT= gcStart.getTime();


                GregorianCalendar gcEnd = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
                gcEnd.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
                endT = gcEnd.getTime();

                //get data form cloud
                totalSkip = 0;
                if(totalAVObjects != null)
                    totalAVObjects.clear();
                findCloud(startT, endT, 0);
            }
        });
        btnBegin = (Button)findViewById(R.id.btn_begin);
        btnEnd = (Button)findViewById(R.id.btn_end);
        dpBegin = (DatePicker)findViewById(R.id.datePicker_begin);
        dpEnd = (DatePicker)findViewById(R.id.datePicker_end);
        btnOK = (Button)findViewById(R.id.btn_OK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCustonViewVisibility(false);
                m_listview.setVisibility(View.VISIBLE);

                //set start time and end time
                GregorianCalendar gcStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
                gcStart.set(dpBegin.getYear(), dpBegin.getMonth(), dpBegin.getDayOfMonth(), 0, 0, 0);
                startT= gcStart.getTime();

                GregorianCalendar gcEnd = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
                gcEnd.set(dpEnd.getYear(), dpEnd.getMonth(), dpEnd.getDayOfMonth() + 1, 0, 0, 0);
                endT = gcEnd.getTime();

                totalSkip = 0;
                if(totalAVObjects != null)
                    totalAVObjects.clear();
                findCloud(startT, endT, 0);
            }
        });

        //绑定Layout里面的ListView
        m_listview = (ListView) findViewById(R.id.listview);

        //生成动态数组，加入数据
        listItem = new ArrayList<HashMap<String, Object>>();
        //生成适配器的Item和动态数组对应的元素
        listItemAdapter = new SimpleAdapter(this,listItem,//数据源
                R.layout.listview_item,//ListItem的XML实现
                //动态数组与ImageItem对应的子项
                new String[] {"ItemTotalTime", "ItemStartTime", "ItemEndTime", "ItemDistance"},
                //,两个TextView ID
                new int[] {R.id.ItemTotalTime,R.id.ItemStartTime, R.id.ItemEndTime, R.id.ItemDistance}
        );

        //添加并且显示
        m_listview.setAdapter(listItemAdapter);

        //添加点击
        m_listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                MaptabFragment.trackDataList = tracksManager.getTrack(arg2);
                //Toast.makeText(getApplicationContext(), "点击第" + arg2 + "个项目", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        dialog = new AlertDialog.Builder(this)
                .setPositiveButton("继续查询",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        }).setNegativeButton("返回地图", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                    }
                }).create();


//        tracksManager.setTracksData(TracksBean.getInstance().getTracksData());
//        updateListView();
    }


    private void findCloud(final Date st, final Date et, int skip) {
        totalSkip += skip;
        final int finalSkip = totalSkip;
        AVQuery<AVObject> query = new AVQuery<AVObject>("GPS");
        String IMEI = sm.getIMEI();
            // Log.i(TAG, "IMEI+++++" + IMEI);
        query.setLimit(1000);
        query.whereEqualTo("IMEI", IMEI);
        query.whereGreaterThanOrEqualTo("createdAt", startT);
        query.whereLessThan("createdAt", endT);
        query.setSkip(finalSkip);
        watiDialog.setMessage("正在查询数据，请稍后…");
        watiDialog.show();
        query.findInBackground(new FindCallback<AVObject>() {
           @Override
           public void done(List<AVObject> avObjects, AVException e) {
               //  Log.i(TAG, e + "");
               if (e == null) {
                   if (avObjects.size() > 0)
                       //     Log.e(TAG,"oooooooooooooook--------" + avObjects.size());
                       if (avObjects.size() == 0) {
                           clearListViewWhenFail();

                           dialog.setTitle("此时间段内没有数据");
                           dialog.show();
                           watiDialog.dismiss();
                           return;
                       }
                   for (AVObject thisObject : avObjects) {
                       totalAVObjects.add(thisObject);
                   }
                   if (avObjects.size() >= 1000) {
                       //     Log.d(TAG, "data more than 1000");
                       findCloud(st, et, 1000);
                   }
                   if ((totalAVObjects.size() > 1000) && (avObjects.size() < 1000) ||
                           (totalSkip == 0) && (avObjects.size() < 1000)) {
                       tracksManager.clearTracks();

//                        //清楚本地数据
                       TracksBean.getInstance().getTracksData().clear();
//                       tracksManager.setTranks(groupposition,totalAVObjects);

//                        //更新本地数据
                       TracksBean.getInstance().setTracksData(tracksManager.getTracks());

                       updateListView();
                       watiDialog.dismiss();
                       listItemAdapter.notifyDataSetChanged();
                   }

               } else {
                   clearListViewWhenFail();

                   dialog.setTitle("查询失败");
                   dialog.show();
                   watiDialog.dismiss();
               }
           }
       } );
    }

    private void clearListViewWhenFail() {
        tracksManager.clearTracks();
        updateListView();
        listItemAdapter.notifyDataSetChanged();
    }

    private void updateListView(){
     //   Log.i(TAG, "update list View");
        listItem.clear();
        //如果没有数据，弹出对话框
        if(tracksManager.getTracks().size() == 0){
            dialog.setTitle("此时间段内没有数据");
            dialog.show();
            return;
        }

        for(int i=0;i<tracksManager.getTracks().size();i++)
        {
            //如果当前路线段只有一个点 不显示
            if(tracksManager.getTracks().get(i).size() == 1) {
                //tracksManager.getTracks().remove(i);
                continue;
            }
            ArrayList<TrackPoint> trackList = tracksManager.getTracks().get(i);

            //获取当前路线段的开始和结束点
            TrackPoint startP = trackList.get(0);
            TrackPoint endP = trackList.get(trackList.size() - 1);

            //计算开始点和结束点时间间隔
            long diff = (endP.time.getTime() - startP.time.getTime()) / 1000 +1;
            long days = diff / (60 * 60 * 24);
            long hours = (diff-days*(60 * 60 * 24))/(60 * 60);
            double minutes = (diff-days*( 60 * 60 * 24.0)-hours*(60 * 60))/(60.0);
            int secodes = (int)((minutes - Math.floor(minutes)) * 60);


            //计算路程
            double distance = 0;
            for(int j = 0; j < trackList.size() - 1; j++){
                LatLng m_start = trackList.get(j).point;
                LatLng m_end = trackList.get(j +1).point;
                distance += DistanceUtil.getDistance(m_start, m_end);

            }
            int distanceKM = (int)(distance / 1000);
            int diatanceM = (int)(distance - distanceKM * 1000);
            //更新列表信息
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemTotalTime", "历时:" + days + "天" + hours +"小时" + (int)Math.floor(minutes) + "分钟" + secodes + "秒");
            map.put("ItemStartTime", "开始时间:" + sdfWithSecond.format(startP.time));
            map.put("ItemEndTime", "结束时间:" + sdfWithSecond.format(endP.time));
            map.put("ItemDistance", "距离:" + distanceKM + "千米" + diatanceM + "米");
            listItem.add(map);
        }

    }


    /**
     * 设置自定义选择界面是否可见
     * @param visible-是否可见
     */
private void setCustonViewVisibility(Boolean visible){
        if(visible){
            btnBegin.setVisibility(View.VISIBLE);
            btnEnd.setVisibility(View.VISIBLE);
            dpBegin.setVisibility(View.VISIBLE);
            dpEnd.setVisibility(View.VISIBLE);
            btnOK.setVisibility(View.VISIBLE);
        }
        else{
            btnBegin.setVisibility(View.INVISIBLE);
            btnEnd.setVisibility(View.INVISIBLE);
            dpBegin.setVisibility(View.INVISIBLE);
            dpEnd.setVisibility(View.INVISIBLE);
            btnOK.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(this);
        }
    }

}
