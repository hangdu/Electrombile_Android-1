/**
 * Project Name:XPGSdkV4AppBase
 * File Name:ProtocolConstants.java
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
package com.xunce.electrombile.Constants;

/**
 * ClassName: Class ProtocolConstants. <br/>
 * Json对应字段表<br/>
 *
 * @author lybvinci
 */
public class ProtocolConstants {

    /******************************
     * 新协议中的Json关键字
     **************************************************/
    public final static String CMD = "cmd";
    public final static String RESULT = "result";
    public final static String STATE = "state";
    public final static String CODE = "code";
    public final static String LAT = "lat";
    public final static String LNG = "lng";
    public final static String TIMESTAMP = "timestamp";
    public final static String INTENSITY = "intensity";
    public final static String TYPE = "type";
    public final static String PERIOD = "period";

    public final static String LOCK = "lock";
    public final static String AUTOLOCK = "autolock";
    public final static String isOn = "isOn";
    public final static String BATERRY = "baterry";
    public final static String PERCENT = "percent";
    public final static String MILES = "miles";



    //开启电子围栏告警
    public final static int CMD_FENCE_ON = 1;
    //关闭电子围栏告警
    public final static int CMD_FENCE_OFF = 2;
    //查询电子围栏告警开关状态
    public final static int CMD_FENCE_GET = 3;
    //打开找车模式开关
    public final static int CMD_SEEK_ON = 4;
    // 关闭找车模式开关
    public final static int CMD_SEEK_OFF = 5;
    // 获取当前位置
    public final static int CMD_LOCATION = 6;
    //    7	APP_CMD_AUTO_LOCK_ON	打开自动锁车开关
    public final static int APP_CMD_AUTO_LOCK_ON = 7;
    //    8	APP_CMD_AUTO_LOCK_OFF	关闭自动锁车开关
    public final static int APP_CMD_AUTO_LOCK_OFF = 8;
    //    9	APP_CMD_AUTO_PERIOD_SET	设置自动锁车的检测周期
    public final static int APP_CMD_AUTO_PERIOD_SET = 9;
    //    10	APP_CMD_AUTO_PERIOD_GET	查询自动锁车的检测周期
    public final static int APP_CMD_AUTO_PERIOD_GET = 10;

    public final static int APP_CMD_AUTOLOCK_GET = 11;

    public final static int APP_CMD_STATUS_GET = 13;

    //围栏打开状态
    public final static int ON = 1;
    //围栏关闭状态
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
