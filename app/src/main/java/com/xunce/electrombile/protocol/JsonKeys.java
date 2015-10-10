/**
 * Project Name:XPGSdkV4AppBase
 * File Name:JsonKeys.java
 * Package Name:com.gizwits.framework.config
 * Date:2015-1-27 14:47:10
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
package com.xunce.electrombile.protocol;

/**
 * ClassName: Class JsonKeys. <br/>
 * Json对应字段表<br/>
 *
 * @author Lien
 */
public class JsonKeys {

//    /**
//     * 产品名.
//     */
//    public final static String PRODUCT_NAME = "安全宝";
//
//    /**
//     * 实体字段名，代表对应的项目.
//     */
//    public final static String KEY_ACTION = "electrombile";
//
//    //GPRS 定时发送设置
//    public final static String GPRS_SEND = "TIMER,30#";
//
//    // 设置 SOS  管理员  增加号码需要在打包函数的remarks里面设置电话号码，以#结束
//    public final static String SOS_ADD = "SOS,A,";
//
//    //7 、删除 SOS  管理员  删除号码需要在打包函数的remarks里面设置电话号码，以#结束
//    public final static String SOS_DELETE = "SOS,D,";
//
//    //10 、工作模式设置
//    /*
//    *  0# 追踪模式：GPS 一直开启；
//    *  1# 智能省电：设备静止时 GPS 关闭，运动或被查询位置时，GPS 会开启； （有些设备有传感器检测自身运动状态）
//    *  2# 睡眠模式：被查询位置时，GPS 会开启；
//    *  3# 冬眠模式：GPS 一直关闭；
//    *  若设置成功，设备会回复：SET SAVING OK
//    */
//    public final static String MODE_SET_0 = "SAVING,0#";
//    public final static String MODE_SET_1 = "SAVING,1#";
//    public final static String MODE_SET_2 = "SAVING,2#";
//    public final static String MODE_SET_3 = "SAVING,3#";
//
//    //13 、添加电子围栏FENCE,< 围栏编号 >,< 围栏标记 >,<围栏参数 1>,< 围栏参数 2>,< 围栏参数3>,< 围栏参数 4>#
//    //设定 1 号圆形围栏，OR 报警方式：出围栏报警；以当前点为圆心，半径 500 米。
//    public final static String FENCE_SET_1 = "FENCE,1,OR,,,100#";
//
//    //14 、删除电子围栏
//    public final static String FENCE_DELETE = "FENCE,0#";
//
//    //21 、重启设备    设置成功，设备会回复：RESET OK
//    public final static String RESET = "RESET#";
//
//    //25 、查询经纬度  设备会回复：Lat:N22.55787,Lon:E113.93509,Course:0.00,Speed:0.00km/h,DateTime:2014-12-12 07:32:133426
////    public final static String GET_GPS = "GET_GPS#";
//
//    // GPS定位状态，0表示未定位，1表示已定位
//    public final static String GPSSTATUS = "GPSStatus";
//
//    //报警类型
//    public final static String ALARM = "alarm";
//
//    //小区识别码
//    public final static String CI = "ci";
//
//    //航向：单位 度， 以正北为0度，顺时针
//    public final static String COURSE = "course";
//
//    // location area code 位置区码
//    public final static String LAC = "lac";
//
//
//
//    //mcc Mobile Country Code，移动国家码，MCC的资源由国际电联（ITU）统一分配和管理，唯一识别移动用户所属的国家，共3位，中国为460;
//    public final static String MCC = "mcc";
//
//    //mnc  Mobile Network Code，移动网络号码，用于识别移动客户所属的移动网络。
//    public final static String MNC = "mnc";
//
//    // 运动速度： 单位 km/h
//    public final static String SPEED = "speed";
//
//    // Longtitude:经度，单位为分
//    public final static String LONG = "long";
//
//    public final static String ON_OFF = "switch";


    /******************************
     * 新协议中的Json关键字
     **************************************************/
    public final static String CMD = "cmd";
    public final static String RESULT = "result";
    public final static String STATE = "state";
    public final static String LAT = "lat";
    public final static String LNG = "lng";
    //    public final static String FENCE_ON = "FENCE_ON";
//    public final static String FENCE_OFF = "FENCE_OFF";
//    public final static String FENCE_GET = "FENCE_GET";
//    public final static String SEEK_ON = "SEEK_ON";
//    public final static String SEEK_OFF = "SEEK_OFF";
//    public final static String GET_GPS = "GET_GPS";
//    public final static String ON = "ON";
//    public final static String OFF = "OFF";
    public final static String TIMESTAMP = "timestamp";
    public final static String INTENSITY = "intensity";

    public final static int FENCE_ON = 1;
    public final static int FENCE_OFF = 2;
    public final static int FENCE_GET = 3;
    public final static int SEEK_ON = 4;
    public final static int SEEK_OFF = 5;
    public final static int GET_GPS = 6;
    public final static int ON = 1;
    public final static int OFF = 0;

    //成功
    public final static int ERR_SUCCESS = 0;
    //服务器内部错误
    public final static int ERR_INTERNAL = 100;
    //等待设备回应
    public final static int ERR_WAITING = 101;
    //设备不在线
    public final static int ERR_OFFLINE = 102;
    //设备超时码
    public final static int TIME_OUT = 404;
    //设备超时时间
    public final static int TIME_OUT_VALUE = 5000;


}
