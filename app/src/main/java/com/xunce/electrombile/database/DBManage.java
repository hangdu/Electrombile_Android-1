package com.xunce.electrombile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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

    public DBManage(Context context,String IMEI){
        mcontext = context;
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

    public void insert(long timestamp,int trackNumber,String StartPoint,String EndPoint,String time){
        cv.put("timestamp",timestamp);
        cv.put("trackNumber",trackNumber);
        cv.put("StartPoint",StartPoint);
        cv.put("EndPoint",EndPoint);
        cv.put("time",time);
        long res = sqldb.insert("datetrack", null, cv);
        if(-1 == res){
            Toast.makeText(mcontext, "insert失败", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(mcontext,"insert成功",Toast.LENGTH_SHORT).show();
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
                dateTrackList.add(dateTrack);
            } while (mCursor.moveToNext());
        }
        //这个地方不是很懂
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        return resultCount;
    }

    public void RefreshDateTrack(){
        //删除部分数据
        //找到距离现在30天之前的日期作为比较的参考物
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -30);
//        Date ComparedDate = c.getTime();

        //转换成秒
        long ComeparedTimeStamp = c.getTime().getTime()/1000;
        String filter = "timestamp<"+"'"+ComeparedTimeStamp+"'";
        delete(filter);
    }


    public void insertSecondTable(int trackNumber,long timestamp,double longitude,double latitude){
        cv.put("trackNumber",trackNumber);
        cv.put("timestamp",timestamp);
        cv.put("longitude",longitude);
        cv.put("latitude",latitude);
        long res = sqldb2.insert("datetracksecond", null, cv);
        if(-1 == res){
            Toast.makeText(mcontext, "insert失败", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(mcontext,"insert成功",Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSecondTable(){
        int res = sqldb.delete("datetracksecond", null, null);
        if(0 == res){
            Toast.makeText(mcontext,"删除失败",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(mcontext,"成功删除了"+res+"行的数据",Toast.LENGTH_SHORT).show();
        }
    }

    public int querySecondTable(String filter){
        Cursor mCursor = sqldb.query("datetracksecond", null, filter, null, null, null, null);

        int resultCount = mCursor.getCount();
        if(0 == resultCount){
            return 0;
        }

        dateTrackSecondList = new ArrayList<>();
        if (mCursor.moveToFirst()) {
            do {
                DateTrackSecond dateTrackSecond = new DateTrackSecond();

                dateTrackSecond.trackNumber = mCursor.getInt(mCursor.getColumnIndex("trackNumber"));
                dateTrackSecond.timestamp = mCursor.getLong(mCursor.getColumnIndex("timestamp"));
                dateTrackSecond.longitude = mCursor.getDouble(mCursor.getColumnIndex("longitude"));
                dateTrackSecond.latitude = mCursor.getDouble(mCursor.getColumnIndex("latitude"));

                dateTrackSecondList.add(dateTrackSecond);
            } while (mCursor.moveToNext());
        }
        //这个地方不是很懂
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        return resultCount;
    }


    //String日期转换成Date数据类型
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

    //有两张表 第一张记录的是一级界面
}
