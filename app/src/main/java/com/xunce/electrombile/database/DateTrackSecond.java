package com.xunce.electrombile.database;

/**
 * Created by lybvinci on 16/1/24.
 */
public class DateTrackSecond {


    public int trackNumber;
    public long timestamp;
    public double longitude;
    public double latitude;

    public DateTrackSecond(){

    }

    public DateTrackSecond(int trackNumber,long timestamp,double longitude,long latitude){
        this.trackNumber = trackNumber;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
