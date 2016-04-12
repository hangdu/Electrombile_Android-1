package com.xunce.electrombile.database;

/**
 * Created by lybvinci on 16/1/23.
 */
public class DateTrack {
    public long timestamp;
    public int trackNumber;
    public String StartPoint;
    public String EndPoint;
    public String time;
    public int miles;

    public DateTrack(){

    }

    public DateTrack(long timestamp,int trackNumber,String StartPoint,String EndPoint,String time,int miles){
        this.timestamp = timestamp;
        this.trackNumber = trackNumber;
        this.StartPoint = StartPoint;
        this.EndPoint = EndPoint;
        this.time = time;
        this.miles = miles;
    }


}
