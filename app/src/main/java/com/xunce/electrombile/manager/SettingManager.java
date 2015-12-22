/**
 * Project Name:XPGSdkV4AppBase
 * File Name:SettingManager.java
 * Package Name:com.gizwits.framework.sdk
 * Date:2015-1-27 14:47:24
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xunce.electrombile.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.xunce.electrombile.Constants.ServiceConstants;

/**
 * SharePreference处理类.
 *
 * @author lybvinci
 */
public class SettingManager {

    private final String ALARM_FLAG = "alarmFlag";
    /**
     * The share preferences.
     */
    private final String SHARE_PREFERENCES = "set";

    /**
     * The phone num.
     */
    private final String PHONE_NUM = "phoneNumber";
    // 手机号码
    /**
     * The password.
     */
    //private final String PASSWORD = "password";
    // 密码
    /**
     * The token.
     */
    // private final String TOKEN = "token";
    // 用户名
    //did
    private final String IMEI = "imie";

    //用户中心相关
    private final String PHOTO_FLAG = "photoFlag";
    private final String CAR_NUMBER = "carNumber";
    private final String SIM_NUMBER = "simNumber";

    //用户的初始位置
    private final String Lat = "lat";
    private final String Longitude = "longitude";

    private final String Server = "Server";

    /**
     * The spf.
     */
    SharedPreferences spf;
    /**
     * The c.
     */
    private Context c;


    /**
     * Instantiates a new setting manager.
     *
     * @param c the c
     */
    public SettingManager(Context c) {
        this.c = c;
        spf = c.getSharedPreferences(SHARE_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * 清理所有信息
     */
    public void cleanAll() {
        setPhoneNumber("");
        setPersonCenterCarNumber("");
        setPersonCenterImage(0);
        setPersonCenterSimNumber("");
        cleanDevice();
    }

    /**
     * 清理设备信息
     */
    public void cleanDevice() {
        setIMEI("");
        setAlarmFlag(false);
        setInitLocation("", "");
    }

    /**
     * 设置初始位置
     *
     * @param lat       纬度
     * @param longitude 经度
     */
    public void setInitLocation(String lat, String longitude) {
        spf.edit().putString(Lat, lat).commit();
        spf.edit().putString(Longitude, longitude).commit();
    }

    /**
     * 获得初始纬度
     * @return
     */
    public String getInitLocationLat() {
        return spf.getString(Lat, "");
    }

    /**
     * 获得初始经度
     * @return
     */
    public String getInitLocationLongitude() {
        return spf.getString(Longitude, "");
    }

    /**
     * 获得报警标志状态
     * @return
     */
    public boolean getAlarmFlag() {
        return spf.getBoolean(ALARM_FLAG, false);
    }

    /**
     * 设置报警状态标志
     * @param alarmFlag
     */
    public void setAlarmFlag(boolean alarmFlag) {
        spf.edit().putBoolean(ALARM_FLAG, alarmFlag).commit();
    }

    /**
     *
     * @return 电话号码
     */
    public String getPhoneNumber() {
        return spf.getString(PHONE_NUM, "");
    }

    /**
     * 设置电话号码
     *
     * @param phoneNumber 电话号码
     */
    public void setPhoneNumber(String phoneNumber) {
        spf.edit().putString(PHONE_NUM, phoneNumber).commit();
    }

    /**
     *
     * @return 获取IMEI号
     */
    public String getIMEI() {
        return spf.getString(IMEI, "");
    }

    /**
     * 设置IMEI号
     * @param did  IMEI号
     */
    public void setIMEI(String did) {
        spf.edit().putString(IMEI, did).commit();
    }

    public String getServer() {
        return spf.getString(Server, ServiceConstants.MQTT_HOST);
    }

    public void setServer(String server) {
        spf.edit().putString(Server, server).commit();
    }


    /********************************个人中心相关****************************************/

    /**
     * 获取个人中心已经设置的图片个数
     *
     * @return
     */
    public int getPersonCenterImage() {
        return spf.getInt(PHOTO_FLAG, 0);
    }

    /**
     * 设置个人中心设置的图片个数
     *
     * @param did
     */
    public void setPersonCenterImage(int did) {
        spf.edit().putInt(PHOTO_FLAG, did).commit();
    }

    /**
     * 取出个人中心车辆牌照
     *
     * @return
     */
    public String getPersonCenterCarNumber() {
        return spf.getString(CAR_NUMBER, "");
    }

    /**
     * 保存个人中心车辆牌照
     *
     * @param carNumber
     */
    public void setPersonCenterCarNumber(String carNumber) {
        spf.edit().putString(CAR_NUMBER, carNumber).commit();
    }

    /**
     * 取出个人中心SIM卡号
     *
     * @return
     */
    public String getPersonCenterSimNumber() {
        return spf.getString(SIM_NUMBER, "");
    }

    /**
     * 保存个人中心SIM卡号
     *
     * @param simNumber
     */
    public void setPersonCenterSimNumber(String simNumber) {
        spf.edit().putString(SIM_NUMBER, simNumber).commit();
    }

    /********************************以上为个人中心相关****************************************/
}
