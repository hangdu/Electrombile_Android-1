package com.xunce.electrombile.bean;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;

import java.util.ArrayList;

/**
 * Created by Xingw on 2015/12/13.
 */
public class OfflinemapBean {
    private String cityName;
    private int cityId;
    private STATE state;
    private int progress;
    public ArrayList<OfflinemapBean> childCities;

    public enum STATE{
        //NONE("未安装",1),LOADING("下载中",2),FINISHED("已完成",3),UPDATE("需更新",4);
        NONE,LOADING, FINISHED,UPDATE,SUSPENDED;
    }

    public ArrayList<OfflinemapBean> getChildCities() {
        return childCities;
    }

    public void setChildCities(ArrayList<OfflinemapBean> childCities) {
        this.childCities = childCities;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
