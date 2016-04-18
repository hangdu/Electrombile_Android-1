package com.xunce.electrombile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

/**
 * Created by lybvinci on 16/1/22.
 */
public class DBManage {
    SQLiteDatabase sqldb;
    SQLiteDatabase sqldb2;
    private MyDatabaseHelper dbHelper;
    Context mcontext;
    ContentValues cv;
    public List<DateTrack> dateTrackList;
    public List<DateTrackSecond> dateTrackSecondList;
    public ArrayList<ArrayList<TracksManager.TrackPoint>>  trackList;
    String IMEI;


    public DBManage(Context context,String IMEI){
        mcontext = context;
        this.IMEI = IMEI;
        String TableName = "IMEI_"+IMEI+".db";
        dbHelper = new MyDatabaseHelper(mcontext,TableName,null,5);
        sqldb = dbHelper.getWritableDatabase();
        cv = new ContentValues();
    }

    //第二种表的构造函数
    public DBManage(Context context,String IMEI,String date){
        mcontext = context;
        String TableName = date+"_IMEI_"+IMEI+".db";
        dbHelper = new MyDatabaseHelper(mcontext,TableName,null,5);
        sqldb2 = dbHelper.getWritableDatabase();
        cv = new ContentValues();
    }

    public void insert(long timestamp,int trackNumber,String StartPoint,String EndPoint,String time,int OneTrackMile){
        cv.put("timestamp",timestamp);
        cv.put("trackNumber",trackNumber);
        cv.put("StartPoint",StartPoint);
        cv.put("EndPoint",EndPoint);
        cv.put("time",time);
        cv.put("OneTrackMile",OneTrackMile);

        long res = sqldb.insert("datetrack", null, cv);
        if(-1 == res){
            Toast.makeText(mcontext, "insert失败", Toast.LENGTH_SHORT).show();

        }

    }



    public void delete(){
        int res = sqldb.delete("datetrack", null, null);
        if(0 == res){
            Toast.makeText(mcontext,"删除失败",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(mcontext,"成功删除了"+res+"行的数据",Toast.LENGTH_SHORT).show();
        }
    }

    //方法重载
    public void delete(String filter){
        int res = sqldb.delete("datetrack", filter, null);
        if(0 == res){
            Toast.makeText(mcontext,"删除失败",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(mcontext,"成功删除了"+res+"行的数据",Toast.LENGTH_SHORT).show();
        }
    }


    public int query(String filter) {
        Cursor mCursor = sqldb.query("datetrack", null, filter, null, null, null, null);

        int resultCount = mCursor.getCount();
        if(0 == resultCount){
            return 0;
        }

        dateTrackList = new ArrayList<DateTrack>();

        if (mCursor.moveToFirst()) {
            do {
                DateTrack dateTrack = new DateTrack();
                dateTrack.timestamp = mCursor.getLong(mCursor.getColumnIndex("timestamp"));
                dateTrack.trackNumber = mCursor.getInt(mCursor.getColumnIndex("trackNumber"));
                dateTrack.StartPoint = mCursor.getString(mCursor.getColumnIndex("StartPoint"));
                dateTrack.EndPoint = mCursor.getString(mCursor.getColumnIndex("EndPoint"));
                dateTrack.time = mCursor.getString(mCursor.getColumnIndex("time"));
                dateTrack.miles = mCursor.getInt(mCursor.getColumnIndex("OneTrackMile"));

                dateTrackList.add(dateTrack);
            } while (mCursor.moveToNext());
        }
        //这个地方不是很懂
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        return resultCount;
    }

//    public void RefreshDateTrack(){
//        //删除部分数据
//        //找到距离现在30天之前的日期作为比较的参考物
//        Calendar c = Calendar.getInstance();
//        c.add(Calendar.DAY_OF_MONTH, -30);
////        Date ComparedDate = c.getTime();
//
//        //转换成秒
//        long ComeparedTimeStamp = c.getTime().getTime()/1000;
//        String filter = "timestamp<"+"'"+ComeparedTimeStamp+"'";
//        delete(filter);
//    }


    public void insertSecondTable(int trackNumber,long timestamp,double longitude,double latitude){
        cv.put("trackNumber",trackNumber);
        cv.put("timestamp", timestamp);
        cv.put("longitude",longitude);
        cv.put("latitude",latitude);
        long res = sqldb2.insert("datetracksecond", null, cv);
        if(-1 == res){
            Toast.makeText(mcontext, "insert失败", Toast.LENGTH_SHORT).show();

        }
//        else{
//            Toast.makeText(mcontext,"insert成功",Toast.LENGTH_SHORT).show();
//        }
    }

//    public void deleteSecondTable(){
//        int res = sqldb2.delete("datetracksecond", null, null);
//        if(0 == res){
//            Toast.makeText(mcontext,"删除失败",Toast.LENGTH_SHORT).show();
//        }
//        else{
//            Toast.makeText(mcontext,"成功删除了"+res+"行的数据",Toast.LENGTH_SHORT).show();
//        }
//    }

    public void querySecondTable(int TotalTrackNumber){
        //有几条轨迹就按照几条轨迹装载好
        trackList = new ArrayList<>();

        ArrayList<TracksManager.TrackPoint> track;
        TracksManager.TrackPoint trackPoint;

        for(int j=0;j<TotalTrackNumber;j++){
            track = new ArrayList<>();

            String filter = "trackNumber="+j;
            Cursor mCursor = sqldb2.query("datetracksecond", null, filter, null, null, null, null);
            int result = mCursor.getCount();
            if (mCursor.moveToFirst()) {
                do {
                    //点的集合  形成的一条轨迹
                    long timestamp = mCursor.getLong(mCursor.getColumnIndex("timestamp"));
                    double longitude = mCursor.getDouble(mCursor.getColumnIndex("longitude"));
                    double latitude = mCursor.getDouble(mCursor.getColumnIndex("latitude"));

                    trackPoint = new TracksManager.TrackPoint(new Date(1000*timestamp),latitude,longitude);
                    track.add(trackPoint);
                } while (mCursor.moveToNext());
            }
            trackList.add(track);
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
        }
    }

    public void closeDB(){
        if(sqldb != null){
            sqldb.close();
        }

        if(sqldb2 != null){
            sqldb2.close();
        }
    }


//    //String日期转换成Date数据类型
//    private Date StringtoDate(String StringDate){
//        SimpleDateFormat fmt =new SimpleDateFormat("yyyy年MM月dd日");
//        Date date = new Date();
//        try{
//            date = fmt.parse(StringDate);
//        }catch(ParseException e){
//            Toast.makeText(mcontext,"日期解析失败",Toast.LENGTH_SHORT);
//        }
//        return date;
//    }

    //在一级数据库里把所有的时间戳都记录下来  转换成日期  装到一个list中去
//    public List<String> getAllDateInDateTrackTable(){
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
//        List<String> dateList = new ArrayList<>();
//
////        String TableName = "IMEI_"+IMEI+".db";
//        Cursor mCursor = sqldb.query(true, "datetrack", new String[]{"timestamp"}, null, null, "timestamp", null, null, null);
//        if (mCursor.moveToFirst()) {
//            do {
//                //点的集合  形成的一条轨迹
//                long timestamp = mCursor.getLong(mCursor.getColumnIndex("timestamp"));
//                Date date = new Date(1000*timestamp);
//                String date_string = sdf.format(date);
//                dateList.add(date_string);
//            } while (mCursor.moveToNext());
//        }
//        if (mCursor != null && !mCursor.isClosed()) {
//            mCursor.close();
//        }
//        return dateList;
//    }

    //删除30天之外的数据
    static public void updateDatabase(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //doing some work
                SettingManager settingManager = SettingManager.getInstance();
                long lastUpdateTime = settingManager.getUpdateDatabaseTime();
                if(lastUpdateTime == 0){
                    //清理  表示第一次调用这个函数
                    sub_updateDatabase();
                    settingManager.setUpdateDatabaseTime(System.currentTimeMillis());
                }

                else{
                    long timestamp_now = System.currentTimeMillis();
                    long compare = 720 * 3600 * 1000;
                    if(timestamp_now-lastUpdateTime>compare){
                        sub_updateDatabase();
                        settingManager.setUpdateDatabaseTime(System.currentTimeMillis());
                    }
                }
            }
        };
        new Thread(runnable).start();

    }

    static public void sub_updateDatabase(){
        String path = "/data/data/com.xunce.electrombile/databases";
        File file = new File(path);
        if(file.exists()){
            Date nowtime = new Date();
            Calendar now = Calendar.getInstance();
            now.setTime(nowtime);
            now.set(Calendar.DATE, now.get(Calendar.DATE) - 30);
            Date comparedDate = now.getTime();

            File[] files = file.listFiles();
            for(File file1:files){
                String fileName = file1.getName();
                if(fileName.contains("年")){
                    String str_date = fileName.substring(0,11);
                    DateFormat fmt =new SimpleDateFormat("yyyy年MM月dd日");
                    try{
                        Date date = fmt.parse(str_date);
                        if(date.before(comparedDate)){
                            file1.delete();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
