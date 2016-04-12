package com.xunce.electrombile.database;

/**
 * Created by lybvinci on 16/1/22.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
/**
 * Created by lybvinci on 16/1/20.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_DATETRACK = "create table datetrack("
            +"id integer primary key autoincrement,"
            +"timestamp integer,"
            +"trackNumber integer,"
            +"StartPoint text,"
            +"EndPoint text,"
            +"time text,"
            +"OneTrackMile integer)";

    public static final String CREATE_DATETRACKSECOND = "create table datetracksecond("
            +"id integer primary key autoincrement,"
            +"trackNumber integer,"
            +"timestamp integer,"
            +"longitude REAL,"
            +"latitude REAL)";

    private Context mContext;
    public MyDatabaseHelper(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_DATETRACK);
        db.execSQL(CREATE_DATETRACKSECOND);

        Toast.makeText(mContext, "created succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("drop table if exists datetrack");
        onCreate(db);

    }
}
