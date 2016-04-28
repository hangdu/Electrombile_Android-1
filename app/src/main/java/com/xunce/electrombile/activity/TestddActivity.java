package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.baidu.mapapi.SDKInitializer;
import com.xunce.electrombile.R;
import com.xunce.electrombile.bean.TracksBean;
import com.xunce.electrombile.database.DBManage;
import com.xunce.electrombile.log.MyLog;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.manager.TracksManager.TrackPoint;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.xunce.electrombile.view.RefreshableView;

//这个函数太大了
public class TestddActivity extends Activity{
    private static final String DATE = "date";
    private static final String DISTANCEPERDAY = "distancePerDay";
    private static final String STARTPOINT = "startPoint";
    private static final String ENDPOINT = "endPoint";
    private static final String MILES = "miles";

    private TracksManager tracksManager;
    //查询的开始和结束时间
    private Date startT;
    private Date endT;

    private ExpandableAdapter adapter;
    //用来获取时间
    static Calendar can;
    //查询失败对话框
    private Dialog dialog;
    //管理应用数据的类
    private SettingManager sm;
    private SimpleDateFormat sdfWithSecond;
    private SimpleDateFormat sdf;
    //需要跳过的个数
    private int totalSkip;
    private List<AVObject> totalAVObjects;
    //等待对话框
    private ProgressDialog watiDialog;

    static int GroupPosition = 0;
    private ExpandableListView expandableListView;

    private RefreshableView refreshableView;
    private int Refresh_count = 1;

    private int totalTrackNumber = 0;
    private int ReverseNumber = 0;
    private Boolean DatabaseExistFlag;
    private Date todayDate;
    private Boolean FlagRecentDate;//30天之内

    public DBManage dbManage;
    public DBManage dbManageSecond;

    private List<Map<String, String>> groupData;
    private List<List<Map<String, String>>> childData;

    private int trackCount;
    private ArrayList<Integer> localmilesList;
    private String IMEI;

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case 0:
                    Refresh_count++;
                    ConstructListview(Refresh_count);
                    refreshableView.finishRefreshing();
                    adapter.notifyDataSetChanged();

                    //刷新每天的公里数
                    int result = Refresh_count*7;
                    for(int i = result-7;i<result;i++){
                        findMilesOnedayFromCloud(i);
                    }
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
        ArrayList<Integer> milesList = tracksManager.milesMap.get(String.valueOf(GroupPosition));
        long timeStamp = endT.getTime()/1000;
        for(int i = 0; i <childData.get(GroupPosition).size();i++){
            dbManage.insert(timeStamp,i,childData.get(GroupPosition).get(i).get(STARTPOINT),
                    childData.get(GroupPosition).get(i).get(ENDPOINT),null,milesList.get(i));
        }
        insertDateTrackSecond();
    }

    private void insertNullData(){
        long timeStamp = endT.getTime()/1000;
        dbManage.insert(timeStamp, -1, null, null, null, 0);
    }

    private void insertDateTrackSecond(){
        ArrayList<ArrayList<TrackPoint>> tracks = tracksManager.getTracks();
        for(int i = 0;i<tracks.size();i++){
            for(int j = 0;j<tracks.get(i).size();j++){
                dbManageSecond.insertSecondTable(i, tracks.get(i).get(j).time.getTime() / 1000,
                        tracks.get(i).get(j).point.longitude,tracks.get(i).get(j).point.latitude,
                        tracks.get(i).get(j).speed);
            }
        }
    }

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

        tracksManager = new TracksManager(getApplicationContext());
        can = Calendar.getInstance();
        sm = SettingManager.getInstance();
        IMEI = sm.getIMEI();
        sdf = new SimpleDateFormat("yyyy年MM月dd日");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfWithSecond.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        totalAVObjects = new ArrayList<AVObject>();

        init();

        if (TracksBean.getInstance().getTracksData().size() != 0) {
            tracksManager.clearTracks();
        }

        //删除数据库啊
//        String path = "/data/data/com.xunce.electrombile/databases";
//        File filePath = new File(path);
//        File[] files = filePath.listFiles();
//        for(File file:files){
//            String filename = file.getName();
//            Log.d("test","test");
//            if(filename.contains("IMEI")){
//                file.delete();
//            }
//        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(this);
        }
    }

    private void init(){
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("历史报告");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestddActivity.this.finish();
            }
        });

        watiDialog = new ProgressDialog(this);
        dialog = new AlertDialog.Builder(this)
                .setPositiveButton("稍后再查",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                watiDialog.dismiss();
                            }
                        }).create();

        groupData = new ArrayList<>();
        childData = new ArrayList<>();
        ConstructListview(1);

        expandableListView = (ExpandableListView)findViewById(R.id.expandableListView);
        adapter = new ExpandableAdapter(this,
                groupData,
                R.layout.expandgroupview,
                new String[] {DATE,DISTANCEPERDAY},
                new int[]{R.id.groupDate,R.id.distance},
                childData,
                R.layout.expandchildview,
                new String[] {STARTPOINT,ENDPOINT},
                new int[]{R.id.tv_startPoint,R.id.tv_endPoint},
                this);

        expandableListView.setAdapter(adapter);

        //跳转到下一个activity
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                closeDatabaseCollect();
                SpecificHistoryTrackActivity.trackDataList = tracksManager.getMapTrack().
                        get(String.valueOf(groupPosition)).get(childPosition);

                if(SpecificHistoryTrackActivity.trackDataList.size() == 0){
                    ToastUtils.showShort(TestddActivity.this,"该段轨迹没有点,不跳转");
                    return true;
                }
                else if(SpecificHistoryTrackActivity.trackDataList.size() == 1){
                    ToastUtils.showShort(TestddActivity.this,"该段轨迹只有一个点,不跳转");
                    return true;
                }

                Intent intent = new Intent(TestddActivity.this,SpecificHistoryTrackActivity.class);
                Bundle extras = new Bundle();
                extras.putString("startPoint", childData.get(groupPosition).get(childPosition).get(STARTPOINT));
                extras.putString("endPoint", childData.get(groupPosition).get(childPosition).get(ENDPOINT));
                extras.putString("miles", childData.get(groupPosition).get(childPosition).get(MILES));
                intent.putExtras(extras);

                //这个地方传数据总是有问题啊
                startActivity(intent);
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

        for(int i = 0;i<6;i++){
            findMilesOnedayFromCloud(i);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void findCloud(){
        if(NetworkUtils.checkNetwork(this)){
            return;
        }

        String ItineraryTableName = "Itinerary_"+IMEI;
        AVQuery<AVObject> query = new AVQuery<AVObject>(ItineraryTableName);
        query.whereGreaterThanOrEqualTo("createdAt", startT);
        query.whereLessThan("createdAt", endT);

        watiDialog.setMessage("正在查询数据，请稍后…");
        watiDialog.show();

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null) {
                    if (avObjects.size() == 0) {
                        //该天没有轨迹
                        watiDialog.dismiss();
                        dialog.setTitle("此时间段内没有数据");
                        dialog.show();

                        //在数据表里插入一条数据  表示没有数据
                        if (!startT.equals(todayDate) && FlagRecentDate) {
                            long timeStamp = endT.getTime() / 1000;
                            //存到数据库
                            dbManage.insert(timeStamp, -1, null, null, null, 0);
                        }

                        //需要插入到数据库中  表示没有数据啊
                        List<Map<String, String>> listMap = new ArrayList<>();
                        childData.set(GroupPosition, listMap);
                        adapter.notifyDataSetChanged();
                        return;

                    } else {
                        //创建数据库
                        if (!startT.equals(todayDate) && (FlagRecentDate)) {
                            if (dbManage == null) {
                                dbManage = new DBManage(TestddActivity.this, IMEI);
                            }
                            //什么时候需要创建这张表  当为近期的数据的时候
                            String date = sdf.format(endT);
                            dbManageSecond = new DBManage(TestddActivity.this, IMEI, date);
                        }

                        final int count = avObjects.size();

                        tracksManager.initTracks(count);
                        trackCount = 0;

                        if (localmilesList == null) {
                            localmilesList = new ArrayList<Integer>();
                        } else {
                            localmilesList.clear();
                        }

                        for (final AVObject avObject : avObjects) {
                            //再查询一次  查询对应的gps
                            long start_timestamp = avObject.getLong("start");
                            long end_timestamp = avObject.getLong("end");
                            if (start_timestamp > 10 && end_timestamp > 10) {
                                AVQuery<AVObject> query = new AVQuery<AVObject>("GPS");
                                query.setLimit(1000);
                                query.whereEqualTo("IMEI", IMEI);

                                //单位转换:  由秒转换成毫秒
                                Date startDate = new Date(start_timestamp * 1000);
                                Date endDate = new Date(end_timestamp * 1000);
                                query.whereGreaterThanOrEqualTo("createdAt", startDate);
                                query.whereLessThan("createdAt", endDate);
                                query.findInBackground(new FindCallback<AVObject>() {
                                    @Override
                                    public void done(List<AVObject> list, AVException e) {
                                        if (e == null) {
                                            //如果固件分段有问题的话  这个地方的size可能为0或者1
                                            if (list.size() > 0) {
                                                tracksManager.setOneTrack(list, trackCount);
                                                int mile = (int) avObject.get("miles");
                                                localmilesList.add(mile);
                                            }
                                            trackCount++;
                                            if (trackCount == count) {
                                                updateListView();
//                                                watiDialog.dismiss();
                                                tracksManager.setMilesMap(GroupPosition, localmilesList);
                                            }
                                        } else {
                                            dialog.setTitle("查询失败" + e.getMessage());
                                            dialog.show();
                                            watiDialog.dismiss();
                                        }
                                    }
                                });
                            } else {
                                trackCount++;
                                if (trackCount == count) {
                                    //说明当天查到的数据是由测试数据的  这个时候就处理为无数据
                                    watiDialog.dismiss();
                                    dialog.setTitle("此时间段内没有数据");
                                    dialog.show();
                                }
                            }
                        }
                    }
                } else {
                    watiDialog.dismiss();
                    //在leancloud上没有对应的里程表
                    if (e.getCode() == 101) {
                        ToastUtils.showShort(TestddActivity.this, "无数据表");
                    }
                }
            }
        });
    }

    private void updateListView(){
        //如果没有数据，弹出对话框
        if(tracksManager.getTracks().size() == 0){
            List<Map<String, String>> listMap = new ArrayList<>();
            childData.set(GroupPosition, listMap);
            adapter.notifyDataSetChanged();
            insertNullData();
            dialog.setTitle("此时间段内没有数据");
            dialog.show();
            return;
        }

        List<Map<String, String>> listMap = new ArrayList<>();
        Map<String, String> map;
        for(int i=0;i<tracksManager.getTracks().size();i++)
        {
            totalTrackNumber++;
            ArrayList<TrackPoint> trackList = tracksManager.getTracks().get(i);

            //获取当前路线段的开始和结束点
            TrackPoint startP = trackList.get(0);
            GeoCodering geoCoder1 = new GeoCodering(this,startP.point,i,0);
            geoCoder1.init();
            TrackPoint endP = trackList.get(trackList.size() - 1);
            GeoCodering geoCoder2 = new GeoCodering(this,endP.point,i,1);
            geoCoder2.init();

            map = new HashMap<>();
            listMap.add(map);
        }
        //指定容量  虽然里面的数据是空  但是容量是对的  这样就不会溢出
        childData.set(GroupPosition, listMap);
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

        gcStart.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH),
                can.get(Calendar.DAY_OF_MONTH)-groupPosition, 0, 0, 0);

        startT= gcStart.getTime();
        gcEnd.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH),
                can.get(Calendar.DAY_OF_MONTH)-groupPosition+1, 0, 0, 0);

        endT = gcEnd.getTime();

        //如果查询的数据是30天之外的  就直接和leancloud交互,不要存取数据库
        if(groupPosition>30){
            FlagRecentDate = false;
            findCloud();
            return;
        }

        FlagRecentDate = true;

        if(startT.equals(todayDate)){
            //直接从leancloud上获取数据
            findCloud();
            return;

        }
        else{
            IfDatabaseExist();
            if(DatabaseExistFlag){
                dbManage = new DBManage(TestddActivity.this,IMEI);

                //由毫秒转换成秒
                long timeStamp = endT.getTime()/1000;

                String filter = "timestamp="+timeStamp;
                int resultCount = dbManage.query(filter);
                if(0 == resultCount){
                    //之前没有查过,数据库里没有相关的数据
                    findCloud();
                    return;
                }
                else if(1 == resultCount){
                    //判断是只有一条数据还是空数据
                    if(dbManage.dateTrackList.get(0).trackNumber == -1){
                        //为空数据
                        List<Map<String, String>> listMap = new ArrayList<>();
                        childData.set(groupPosition,listMap);
                        adapter.notifyDataSetChanged();
                        dialog.setTitle("此时间段内没有数据");
                        dialog.show();

                        return;
                    }
                    else{
                        //有一条轨迹
                        Map<String,String> map = new HashMap<>();
                        map.put(STARTPOINT,dbManage.dateTrackList.get(0).StartPoint);
                        map.put(ENDPOINT,dbManage.dateTrackList.get(0).EndPoint);
                        map.put(MILES,String.valueOf(dbManage.dateTrackList.get(0).miles));

                        List<Map<String, String>> listMap = new ArrayList<>();
                        listMap.add(map);
                        childData.set(groupPosition, listMap);

                        adapter.notifyDataSetChanged();
                        getSecondTableData();

                        return;
                    }
                }
                else{
                    Map<String,String> map;
                    List<Map<String, String>> listMap = new ArrayList<>();
                    for(int i = 0;i<dbManage.dateTrackList.size();i++){
                        map = new HashMap<>();
                        map.put(STARTPOINT,dbManage.dateTrackList.get(i).StartPoint);
                        map.put(ENDPOINT, dbManage.dateTrackList.get(i).EndPoint);
                        map.put(MILES, String.valueOf(dbManage.dateTrackList.get(i).miles));

                        listMap.add(map);
                    }
                    childData.set(groupPosition, listMap);
                    adapter.notifyDataSetChanged();
                    getSecondTableData();
                    return;
                }
            }
            else{
                findCloud();
            }
        }
    }

    private void getSecondTableData(){
        //提取二级数据库的数据
        String date = sdf.format(endT);
        dbManageSecond = new DBManage(TestddActivity.this,IMEI,date);
        dbManageSecond.querySecondTable(dbManage.dateTrackList.size());

        //把数据库的数据填充到tracksManager的tracks中
        tracksManager.setTracksData(dbManageSecond.trackList);
        tracksManager.SetMapTrack(GroupPosition, tracksManager.getTracks());
    }

    //由经纬度转换成了具体的地址之后就调用这个函数
    void RefreshMessageList(int TrackPosition,int Start_End_TYPE,String result)
    {
        ReverseNumber++;
        if(0 == Start_End_TYPE)
        {
            childData.get(GroupPosition).get(TrackPosition).put(STARTPOINT,result);
            childData.get(GroupPosition).get(TrackPosition).put(MILES,
                    String.valueOf(localmilesList.get(TrackPosition)));
        }

        else{
            childData.get(GroupPosition).get(TrackPosition).put(ENDPOINT, result);
        }

        adapter.notifyDataSetChanged();

        if(ReverseNumber == totalTrackNumber*2){
            watiDialog.dismiss();
            if(!startT.equals(todayDate)&&(FlagRecentDate)){
                //确实是近期的数据才会插入到数据库
                android.os.Message msg = android.os.Message.obtain();
                msg.what = 1;
                mhandler.sendMessage(msg);
            }
        }
    }

   //更新ItemList
    private void ConstructListview(int Count)
    {
        String[] result = new String[7*Count];
        //parse作用: 把String型的字符串转换成特定格式的date类型
        Calendar c = Calendar.getInstance();
        result[0] = sdf.format(c.getTime());

        for(int i=1;i<result.length;i++){
            c.add(Calendar.DAY_OF_MONTH, -1);
            result[i] = sdf.format(c.getTime());
        }

        Map<String, String> map;
        for(int i = result.length-7;i<result.length;i++)
        {
            map = new HashMap<>();
            map.put(DATE, result[i]);
            map.put(DISTANCEPERDAY,"0km");
            groupData.add(map);
        }

        List<Map<String, String>> listmap;
        for(int i = result.length-7;i<result.length;i++)
        {
            listmap = new ArrayList<>();
            childData.add(listmap);
        }
    }

    //关闭数据库连接
    private void closeDatabaseCollect(){
        if(dbManage!=null){
            dbManage.closeDB();
        }
        if(dbManageSecond!=null){
            dbManageSecond.closeDB();
        }
    }


    //去leancloud查询每天的总公里数
    private void findMilesOnedayFromCloud(final int groupPosition){
        //一次需要获取到7天的里程
        GregorianCalendar gcStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
        gcStart.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH),
                can.get(Calendar.DAY_OF_MONTH)-groupPosition, 0, 0, 0);

        Date startTime= gcStart.getTime();

        GregorianCalendar gcEnd = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"));
        gcEnd.set(can.get(Calendar.YEAR), can.get(Calendar.MONTH),
                can.get(Calendar.DAY_OF_MONTH)-groupPosition+1, 0, 0, 0);

        Date endTime = gcEnd.getTime();

        String ItineraryTableName = "Itinerary_"+IMEI;
        //查出当天的总里程
        AVQuery<AVObject> query = new AVQuery<AVObject>(ItineraryTableName);
        query.whereGreaterThanOrEqualTo("createdAt", startTime);
        query.whereLessThan("createdAt", endTime);

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if(e == null){
                    if(list.size() > 0){
                        int milesOneDay = 0;
                        for(AVObject avObject:list){
                           int milesOneTrack = (int)avObject.get("miles");
                            milesOneDay+=milesOneTrack;
                        }
                        groupData.get(groupPosition).put(DISTANCEPERDAY,milesOneDay/1000.0+"km");
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    e.printStackTrace();
                }
            }
        });
    }
}