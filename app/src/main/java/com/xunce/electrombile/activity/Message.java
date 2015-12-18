package com.xunce.electrombile.activity;

/**
 * Created by lybvinci on 15/12/16.
 */
public class Message {
    private String time;
    private String StartLocation;
    private String EndLocation;

    public Message(String time,String StartLocaiton,String EndLocation){
        this.time = time;
        this.StartLocation = StartLocaiton;
        this.EndLocation = EndLocation;
    }
    public String getTime(){
        return time;
    }
    public String getStartLocation(){
        return StartLocation;
    }
    public String getEndLocation(){
        return EndLocation;
    }

}
