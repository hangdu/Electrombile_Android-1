package com.xunce.electrombile.activity;

import java.util.List;

/**
 * Created by lybvinci on 15/12/16.
 */
public class Item {
    private String date;
    private List<Message> messagelist;
    private boolean IfDataExistFlag;
    public Item(String date,List<Message> messagelist,boolean IfDataExistFlag){
        this.date = date;
        this.messagelist = messagelist;
        this.IfDataExistFlag = IfDataExistFlag;
    }

    public boolean getIfDataExistFlag(){
        return  IfDataExistFlag;
    }

    public void setIfDataExistFlag(boolean IfDataExistFlag){
        this.IfDataExistFlag = IfDataExistFlag;
    }



    public List<Message> getMessagelist() {
        return messagelist;
    }

    public void setMessagelist(List<Message> messagelist) {
        this.messagelist = messagelist;
    }

    public String getDate(){
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

}
