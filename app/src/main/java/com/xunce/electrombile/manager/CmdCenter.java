/**
 * Project Name:XPGSdkV4AppBase
 * File Name:CmdCenter.java
 * Package Name:com.gizwits.framework.sdk
 * Date:2015-1-27 14:47:19
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

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.xunce.electrombile.Constants.ProtocolConstants;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * ClassName: Class CmdCenter.
 * 控制指令类
 *
 * @author lyb
 */
public class CmdCenter {

    /**
     * The Constant TAG.
     */
    private static final String TAG = "CmdCenter";

    /**
     * The m center.
     */
    private static CmdCenter mCenter;

    /**
     * The m setting manager.
     */
    private SettingManager mSettingManager;

    //报警用的标志位


    /**
     * Instantiates a new cmd center.
     *
     * @param c the c
     */
    private CmdCenter(Context c) {
        if (mCenter == null) {
            init(c);
        }
    }

    /**
     * Gets the single instance of CmdCenter.
     *
     * @param c the c
     * @return single instance of CmdCenter
     */
    public static CmdCenter getInstance(Context c) {
        if (mCenter == null) {
            mCenter = new CmdCenter(c);
        }
        return mCenter;
    }

    /**
     * Inits the.
     *
     * @param c the c
     */
    private void init(Context c) {
        mSettingManager = new SettingManager(c);
    }

    //由GPS坐标转换成百度经纬度坐标
    public LatLng convertPoint(LatLng sourcePoint) {
        CoordinateConverter cdc = new CoordinateConverter();
        cdc.from(CoordinateConverter.CoordType.GPS);
        cdc.coord(sourcePoint);
        LatLng desPoint = cdc.convert();
        return desPoint;
    }

    /*************************************************************
     * 新协议
     ************************************************************/
    public byte[] cmdFenceOn() {
        return getCmdString(ProtocolConstants.CMD_FENCE_ON).getBytes();
    }

    public byte[] cmdFenceOff() {
        return getCmdString(ProtocolConstants.CMD_FENCE_OFF).getBytes();
    }

    public byte[] cmdFenceGet() {
        return getCmdString(ProtocolConstants.CMD_FENCE_GET).getBytes();
    }

    public byte[] cmdWhere() {
        return getCmdString(ProtocolConstants.CMD_LOCATION).getBytes();
    }

    ;
    public byte[] cmdSeekOn() {
        return getCmdString(ProtocolConstants.CMD_SEEK_ON).getBytes();
    }

    public byte[] cmdSeekOff() {
        return getCmdString(ProtocolConstants.CMD_SEEK_OFF).getBytes();
    }

    //自动落锁系列命令
    public byte[] cmdAutolockOn() {
        return getCmdString(ProtocolConstants.APP_CMD_AUTO_LOCK_ON).getBytes();
    }

    public byte[] cmdAutolockOff() {
        return getCmdString(ProtocolConstants.APP_CMD_AUTO_LOCK_OFF).getBytes();
    }

    public byte[] cmdAutolockTimeSet(int period){
        return getCmdString(ProtocolConstants.APP_CMD_AUTO_PERIOD_SET,period).getBytes();
    }

    public byte[] cmdAutolockTimeGet(){
        return getCmdString(ProtocolConstants.APP_CMD_AUTO_PERIOD_GET).getBytes();
    }

    private String getCmdString(int cmd) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(ProtocolConstants.CMD, cmd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String s = obj.toString();
        return s;
//        return obj.toString();
    }

    private String getCmdString(int cmd,int period) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(ProtocolConstants.CMD, cmd);
            obj.put("period",period);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String s = obj.toString();
        return s;
//        return obj.toString();
    }


}
