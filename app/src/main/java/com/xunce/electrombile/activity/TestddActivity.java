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
import android.widget.TextView;
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
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.baidu.mapapi.search.geocode.GeoCoder;
import com.xunce.electrombile.view.RefreshableView;

//这个函数太大了
public class TestddActivity extends Activity{
    private static final String DATE = "date";
    private static final String DISTANCEPERDAY = "distancePerDay";
    private static final String STARTPOINT = "startPoint";
    private static final String ENDPOINT = "endPoint";

    TracksManager tracksManager;
    //查询的开始和结束时间
    Date startT;
    Date endT;

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

    static int GroupPosition = 0;
    ExpandableListView expandableListView;

    RefreshableView refreshableView;
    private int Refresh_count = 1;

    int totalTrackNumber = 0;
    int ReverseNumber = 0;
    Boolean DatabaseExistFlag;
    Boolean SecondTableExistFlag;
    Date todayDate;
    Boolean FlagRecentDate;//30天之内

    public DBManage dbManage;
    public DBManage dbManageSecond;

    List<Map<String, String>> groupData;
    List<List<Map<String, String>>> childData;

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

        long timeStamp = endT.getTime()/1000;
        for(int i = 0; i <childData.get(GroupPosition).size();i++){
            dbManage.insert(timeStamp,i,childData.get(GroupPosition).get(i).get(STARTPOINT),
                    childData.get(GroupPosition).get(i).get(ENDPOINT),null);
        }
        insertDateTrackSecond();
        return;
    }

    private void insertNullData(){
        long timeStamp = endT.getTime()/1000;
        dbManage.insert(timeStamp, -1, null, null, null);
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

//        deleteAllSecondTable();

        tracksManager = new TracksManager(getApplicationContext());
        can = Calendar.getInstance();
        sm = SettingManager.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfWithSecond.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        totalAVObjects = new ArrayList<AVObject>();

        if (TracksBean.getInstance().getTracksData().size() != 0) {
            tracksManager.clearTracks();
//            tracksManager.setTracksData(TracksBean.getInstance().getTracksData());
//            updateListView();
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
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("历史报告");
        Button btn_back = (Button)titleView.findViewById(R.id.btn_back);
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
        adapter = new ExpandableAdapter(this,groupData, R.layout.expandgroupview,new String[] {DATE,DISTANCEPERDAY},new int[]{R.id.groupDate,R.id.distance},
                childData,R.layout.expandchildview,new String[] {STARTPOINT,ENDPOINT},new int[]{R.id.tv_startPoint,R.id.tv_endPoint},this);

        expandableListView.setAdapter(adapter);

        //跳转到下一个activity
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                closeDatabaseCollect();
                SpecificHistoryTrackActivity.trackDataList = tracksManager.getMapTrack().get(String.valueOf(groupPosition)).get(childPosition);

                if(SpecificHistoryTrackActivity.trackDataList.size() == 0){
                    ToastUtils.showShort(TestddActivity.this,"trackDataList的size为0,无法完成跳转");
                    return true;
                }

                Intent intent = new Intent(TestddActivity.this,SpecificHistoryTrackActivity.class);
                Bundle extras = new Bundle();
//                extras.putSerializable("trackDataList", (Serializable)trackDataList);
                extras.putString("startPoint", childData.get(groupPosition).get(childPosition).get(STARTPOINT));
                extras.putString("endPoint", childData.get(groupPosition).get(childPosition).get(ENDPOINT));
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void findCloud(final Date st, final Date et, int skip) {
        //创建数据库
        if(!startT.equals(todayDate)&&(FlagRecentDate)){
            if(dbManage == null){
                dbManage = new DBManage(TestddActivity.this,sm.getIMEI());
            }
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
                        if(!startT.equals(todayDate)&&FlagRecentDate){
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
                            String date = formatter.format(endT);
                            long timeStamp = endT.getTime()/1000;
                            //存到数据库
                            dbManage.insert(timeStamp, -1, null, null, null);
                        }

                        //需要插入到数据库中  表示没有数据啊
                        List<Map<String, String>> listMap = new ArrayList<>();
                        childData.set(GroupPosition, listMap);
                        adapter.notifyDataSetChanged();
                        dialog.setTitle("此时间段内没有数据");
                        dialog.show();
                        return;
                    }
                    else if(startT.equals(todayDate)&&avObjects.size() == 1){
                        List<Map<String, String>> listMap = new ArrayList<>();
                        childData.set(GroupPosition, listMap);
                        adapter.notifyDataSetChanged();
                        dialog.setTitle("此时间段内没有数据");
                        dialog.show();
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
//                    clearListViewWhenFail();
                    dialog.setTitle("查询失败");
                    dialog.show();
                    watiDialog.dismiss();
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
            //如果当前路线段只有一个点 不显示
            if(tracksManager.getTracks().get(i).size() == 1) {
                //tracksManager.getTracks().remove(i);
                continue;
            }
            totalTrackNumber++;
            ArrayList<TrackPoint> trackList = tracksManager.getTracks().get(i);

            //获取当前路线段的开始和结束点
            TrackPoint startP = trackList.get(0);
            GeoCodering geoCoder1 = new GeoCodering(this,startP.point,i,0);
            geoCoder1.init();
            TrackPoint endP = trackList.get(trackList.size() - 1);
            GeoCodering geoCoder2 = new GeoCodering(this,endP.point,i,1);
            geoCoder2.init();

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
                String TableName = "IMEI_"+sm.getIMEI()+".db";
                dbManage = new DBManage(TestddActivity.this,sm.getIMEI());

//                dbManage.delete();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                String date = sdf.format(endT);
//                dbManageSecond = new DBManage(TestddActivity.this,sm.getIMEI(),date);
//                dbManage.deleteSecondTable();

                IfDatabaseExist();
                IfSecondTableExist();

                //看里面有没有想要的数据

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
                        List<Map<String, String>> listMap = new ArrayList<>();
//                        listMap.add(map);
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
                        listMap.add(map);
                    }
                    childData.set(groupPosition, listMap);
                    adapter.notifyDataSetChanged();
                    getSecondTableData();
                    return;
                }
            }
            else{
                findCloud(startT, endT, 0);
                return;
            }
        }
    }

    private void getSecondTableData(){
        //提取二级数据库的数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String date = sdf.format(endT);
        dbManageSecond = new DBManage(TestddActivity.this,sm.getIMEI(),date);
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
        }

        else{
            childData.get(GroupPosition).get(TrackPosition).put(ENDPOINT, result);
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

        for(int i=1;i<result.length;i++){
            c.add(Calendar.DAY_OF_MONTH, -1);
            result[i] = sdf.format(c.getTime());
        }

        Map<String, String> map;
        for(int i = result.length-7;i<result.length;i++)
        {
            map = new HashMap<>();
            map.put(DATE, result[i]);
            map.put(DISTANCEPERDAY,"16km");
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

    private void IfSecondTableExist(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String date = sdf.format(endT);

        String path = "/data/data/com.xunce.electrombile/databases/"+date+"_IMEI_"+sm.getIMEI()+".db";
        File dbtest = new File(path);
        if (dbtest.exists()) {
            Log.d("test", "test");
            SecondTableExistFlag = true;

//            getApplication().deleteDatabase(path);
        } else {
            Log.d("test", "test");
            SecondTableExistFlag = false;
        }
    }

//    private void deleteAllSecondTable(){
//        if(dbManage == null){
//            dbManage = new DBManage(TestddActivity.this,sm.getIMEI());
//        }
//        List<String> dateList = dbManage.getAllDateInDateTrackTable();
//        for(int i = 0;i<dateList.size();i++){
//            String SecondTableName = dateList.get(i)+"_IMEI_"+sm.getIMEI()+".db";
//            Boolean deleteSecondTable = getApplication().deleteDatabase(SecondTableName);
//            Log.d("test","test");
//        }
//        String TableName = "IMEI_"+sm.getIMEI()+".db";
//        Boolean delete = getApplication().deleteDatabase(TableName);
//    }

    //test


    //test


    //    private Boolean IfDatabaseExist() {
//        File dbtest = new File("/data/data/com.xunce.electrombile/databases/IMEI_8650670216630370.db");
//        if (dbtest.exists()) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    //刷新数据库  把其中非近期的数据删掉:把每一个字符串都转变为date 比较  然后觉得是否删除.优化:可以先以日期来合并数据库里的数据(GroupBy)  然后再刷新
}