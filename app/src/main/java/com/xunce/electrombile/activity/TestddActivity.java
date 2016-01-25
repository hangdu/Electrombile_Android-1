package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.xunce.electrombile.R;
import com.xunce.electrombile.bean.TracksBean;
import com.xunce.electrombile.database.DBManage;
import com.xunce.electrombile.database.DateTrack;
import com.xunce.electrombile.database.DateTrackSecond;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.manager.TracksManager.TrackPoint;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.baidu.mapapi.search.geocode.GeoCoder;
import com.xunce.electrombile.view.RefreshableView;

public class TestddActivity extends Activity{

    private final String TAG = "RecordActivity";
//    ListView m_listview;
    TracksManager tracksManager;
    List<Item> ItemList = new ArrayList<>();

    //查询的开始和结束时间
    Date startT;
    Date endT;
    //生成动态数组，加入数据
    ArrayList<HashMap<String, Object>> listItem;
    //数据适配器
//    SimpleAdapter listItemAdapter;
    ExpandableAdapter adapter;
    //用来获取时间
    static Calendar can;
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

    List<Message> messageList;

    Item item;

    GeoCodering geoCoder1;
    GeoCodering geoCoder2;

    static int j = 0;

    static int GroupPosition = 0;
    ArrayList<ArrayList<TrackPoint>> tracks;

    ExpandableListView expandableListView;

    RefreshableView refreshableView;
    private int Refresh_count = 1;

    int totalTrackNumber = 0;
    int ReverseNumber = 0;
    Boolean DatabaseExistFlag;
    Date todayDate;
    Boolean FlagRecentDate;//30天之内

    public DBManage dbManage;
    public DBManage dbManageSecond;


    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case 0:
                    Refresh_count++;
                    ConstructListview(Refresh_count);
                    refreshableView.finishRefreshing();
                    adapter.notifyDataSetChanged();
                    break;

                case 1:
                    insertDatabase();
                    break;
            }
        }
    };

    //存到数据库
    private void insertDatabase(){
        //前面已经加过判断数据是近期数据的逻辑了
        //获取到当天的日期
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        long timeStamp = endT.getTime()/1000;

        for(int i = 0;i<messageList.size();i++){
            dbManage.insert(timeStamp,i,messageList.get(i).getStartLocation(),
                    messageList.get(i).getEndLocation(),messageList.get(i).getTime());
        }

        insertDateTrackSecond();
        return;
    }

    private void insertDateTrackSecond(){
        ArrayList<ArrayList<TrackPoint>> tracks = tracksManager.getTracks();
        for(int i = 0;i<tracks.size();i++){
            for(int j = 0;j<tracks.get(i).size();j++){
                //下面这5句是为test用
                DateTrackSecond dateTrackSecond = new DateTrackSecond();
                dateTrackSecond.trackNumber = i;
                dateTrackSecond.timestamp = tracks.get(i).get(j).time.getTime()/1000;
                dateTrackSecond.latitude = tracks.get(i).get(j).point.latitude;
                dateTrackSecond.longitude = tracks.get(i).get(j).point.longitude;

                dbManageSecond.insertSecondTable(i, tracks.get(i).get(j).time.getTime() / 1000,
                        tracks.get(i).get(j).point.longitude,tracks.get(i).get(j).point.latitude);
            }
        }
    }

//    private Boolean IfDatabaseExist() {
//        File dbtest = new File("/data/data/com.xunce.electrombile/databases/IMEI_8650670216630370.db");
//        if (dbtest.exists()) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    private void IfDatabaseExist() {
        String path = "/data/data/com.xunce.electrombile/databases/IMEI_"+sm.getIMEI()+".db";
        File dbtest = new File(path);
        if (dbtest.exists()) {
            Log.d("test", "test");
            DatabaseExistFlag = true;
        } else {
            Log.d("test", "test");
            DatabaseExistFlag = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(TestddActivity.this);
        setContentView(R.layout.activity_testdd);
        init();

        tracksManager = new TracksManager(getApplicationContext());
        can = Calendar.getInstance();
        sm = new SettingManager(this);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfWithSecond.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        totalAVObjects = new ArrayList<AVObject>();

        if (TracksBean.getInstance().getTracksData().size() != 0) {
            tracksManager.clearTracks();
            tracksManager.setTracksData(TracksBean.getInstance().getTracksData());
            updateListView();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(this);
        }
    }

    private void init(){
        watiDialog = new ProgressDialog(this);
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

        ConstructListview(1);

        expandableListView = (ExpandableListView)findViewById(R.id.expandableListView);
        adapter = new ExpandableAdapter(this,ItemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(TestddActivity.this, "你点击的是第" + (groupPosition + 1) + "的菜单下的第" + (childPosition + 1) + "选项", Toast.LENGTH_SHORT).show();

                MaptabFragment.trackDataList = tracksManager.getMapTrack().get(String.valueOf(groupPosition)).get(childPosition);
                finish();
                return true;
            }
        });

        refreshableView = (RefreshableView)findViewById(R.id.refreshable_view_Date);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                android.os.Message msg = android.os.Message.obtain();
                msg.what = 0;
                mhandler.sendMessage(msg);
            }
        }, 1);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void findCloud(final Date st, final Date et, int skip) {
        //创建数据库

        if(!startT.equals(todayDate)&&(FlagRecentDate == true)){
            dbManage = new DBManage(TestddActivity.this,sm.getIMEI());

            //什么时候需要创建这张表  当为近期的数据的时候
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            String date = sdf.format(endT);
            dbManageSecond = new DBManage(TestddActivity.this,sm.getIMEI(),date);
        }

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
                    if (avObjects.size() == 0) {
                        watiDialog.dismiss();
                        //如果查的不是今天的数据 ,且确实是30天之内的数据   才会把无数据插入数据库
                        if(!startT.equals(todayDate)&&(FlagRecentDate == true)){
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
                            String date = formatter.format(endT);
                            long timeStamp = endT.getTime()/1000;
                            //存到数据库
                            dbManage.insert(timeStamp, -1, null, null, null);
                        }

                        messageList = new ArrayList<Message>();
                        //不知道这个地方有没有问题
                        Message message = new Message("无数据",
                                null,null);
                        messageList.add(message);
                        ItemList.get(GroupPosition).setMessagelist(messageList);
                        adapter.notifyDataSetChanged();
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
                        tracksManager.setTranks(GroupPosition, totalAVObjects);

//                        //更新本地数据
                        TracksBean.getInstance().setTracksData(tracksManager.getTracks());

                        updateListView();
                        watiDialog.dismiss();
                    }

                } else {
                    clearListViewWhenFail();
                    dialog.setTitle("查询失败");
                    dialog.show();
                    watiDialog.dismiss();
                }
            }
        });
    }

    private void clearListViewWhenFail() {
        tracksManager.clearTracks();
        updateListView();
    }

    private void updateListView(){
        //如果没有数据，弹出对话框
        if(tracksManager.getTracks().size() == 0){
            dialog.setTitle("此时间段内没有数据");
            dialog.show();

            return;
        }

        Date startdate = new Date();
        Message message;
        messageList = new ArrayList<Message>();

        for(int i=0;i<tracksManager.getTracks().size();i++)
        {
            //如果当前路线段只有一个点 不显示
            if(tracksManager.getTracks().get(i).size() == 1) {
                //tracksManager.getTracks().remove(i);
                continue;
            }
            totalTrackNumber++;
            ArrayList<TrackPoint> trackList = tracksManager.getTracks().get(i);

            //获取当前路线段的开始和结束点
            TrackPoint startP = trackList.get(0);
            startdate = startP.time;
            geoCoder1 = new GeoCodering(this,startP.point,i,0);
            TrackPoint endP = trackList.get(trackList.size() - 1);
            geoCoder2 = new GeoCodering(this,endP.point,i,1);

            message = new Message(String.valueOf(startdate.getHours())+":"+String.valueOf(startdate.getMinutes()),
                    null,
                    null);
            messageList.add(message);

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
//            HashMap<String, Object> map = new HashMap<String, Object>();
//            map.put("ItemTotalTime", "历时:" + days + "天" + hours +"小时" + (int)Math.floor(minutes) + "分钟" + secodes + "秒");
//            map.put("ItemStartTime", "开始时间:" + sdfWithSecond.format(startP.time));
//            map.put("ItemEndTime", "结束时间:" + sdfWithSecond.format(endP.time));
//            map.put("ItemDistance", "距离:" + distanceKM + "千米" + diatanceM + "米");
//            listItem.add(map);

        }
        ItemList.get(GroupPosition).setMessagelist(messageList);
        adapter.notifyDataSetChanged();

        tracksManager.SetMapTrack(GroupPosition, tracksManager.getTracks());
    }

    void GetHistoryTrack(int groupPosition)
    {

        GroupPosition = groupPosition;
        //由groupPosition得到对应的日期
        GregorianCalendar gcStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
        gcStart.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        startT= gcStart.getTime();
        todayDate = startT;

        GregorianCalendar gcEnd = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
        gcEnd.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
        endT = gcEnd.getTime();
        totalSkip = 0;
        if(totalAVObjects != null)
            totalAVObjects.clear();

        gcStart.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH)-groupPosition, 0, 0, 0);
        startT= gcStart.getTime();
        gcEnd.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH), can.get(Calendar.DAY_OF_MONTH)-groupPosition+1, 0, 0, 0);
        endT = gcEnd.getTime();

        //如果查询的数据是30天之外的  就不要直接和leancloud交互了,不要存取数据库
        if(groupPosition>30){
            FlagRecentDate = false;
            findCloud(startT, endT, 0);
            return;
        }

        FlagRecentDate = true;

        if(startT.equals(todayDate)){
            //直接从leancloud上获取数据
            findCloud(startT, endT, 0);
            return;

        }
        else{
            IfDatabaseExist();
            if(true == DatabaseExistFlag){
                //看里面有没有想要的数据
                dbManage = new DBManage(TestddActivity.this,sm.getIMEI());


                //test
//                dbManage.RefreshDateTrack();

                //delete是为了测试
//                dbManage.delete();
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
//                String date = formatter.format(endT);
                //由毫秒转换成秒
                long timeStamp = endT.getTime()/1000;

                String filter = "timestamp="+timeStamp;
                int resultCount = dbManage.query(filter);
                if(0 == resultCount){
                    //之前没有查过,数据库里没有相关的数据
                    findCloud(startT, endT, 0);
                    return;
                }
                else if(1 == resultCount){
                    //判断是只有一条数据还是空数据
                    if(dbManage.dateTrackList.get(0).trackNumber == -1){
                        //为空数据
                        messageList = new ArrayList<Message>();
                        //不知道这个地方有没有问题
                        Message message = new Message("无数据",
                                null,null);
                        messageList.add(message);
                        ItemList.get(GroupPosition).setMessagelist(messageList);
                        adapter.notifyDataSetChanged();
                        return;

                    }
                    else{
                        messageList = new ArrayList<Message>();
                        Message message = new Message(dbManage.dateTrackList.get(0).time,
                                dbManage.dateTrackList.get(0).StartPoint,dbManage.dateTrackList.get(0).EndPoint);
                        messageList.add(message);
                        ItemList.get(GroupPosition).setMessagelist(messageList);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
                else{
                    messageList = new ArrayList<Message>();
                    for(int i = 0;i<dbManage.dateTrackList.size();i++){
                        Message message = new Message(dbManage.dateTrackList.get(i).time,
                                dbManage.dateTrackList.get(i).StartPoint,dbManage.dateTrackList.get(i).EndPoint);
                        messageList.add(message);
                    }
                    ItemList.get(GroupPosition).setMessagelist(messageList);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
            else{
                findCloud(startT, endT, 0);
                return;
            }
        }
    }

    //由经纬度转换成了具体的地址之后就调用这个函数
    void RefreshMessageList(int TrackPosition,int Start_End_TYPE,String result)
    {
        ReverseNumber++;

        if(0 == Start_End_TYPE)
        {
            ItemList.get(GroupPosition).getMessagelist().get(TrackPosition).setStartLocation(result);
        }
        else{
            ItemList.get(GroupPosition).getMessagelist().get(TrackPosition).setEndLocation(result);
        }
        adapter.notifyDataSetChanged();

        if(ReverseNumber == totalTrackNumber*2){
            if(!startT.equals(todayDate)&&(FlagRecentDate == true)){
                //确实是近期的数据才会插入到数据库
                android.os.Message msg = android.os.Message.obtain();
                msg.what = 1;
                mhandler.sendMessage(msg);
            }
        }
        return;
    }



   //更新ItemList
    private void ConstructListview(int Count)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String[] result = new String[7*Count];
        //parse作用: 把String型的字符串转换成特定格式的date类型
        Calendar c = Calendar.getInstance();
        result[0] = sdf.format(c.getTime());
        List<Message> test = new ArrayList<Message>();

        for(int i=1;i<result.length;i++){
            c.add(Calendar.DAY_OF_MONTH, -1);
            result[i] = sdf.format(c.getTime());
        }

        for(int i = result.length-7;i<result.length;i++)
        {
            ItemList.add(new Item(result[i],test,true));
        }
    }


    //刷新数据库  把其中非近期的数据删掉:把每一个字符串都转变为date 比较  然后觉得是否删除.优化:可以先以日期来合并数据库里的数据(GroupBy)  然后再刷新


}