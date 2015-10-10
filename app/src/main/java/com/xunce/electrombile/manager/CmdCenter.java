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
import com.xunce.electrombile.protocol.JsonKeys;

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

//    private byte[] packetOrder(byte[] cmd, byte[] serial, String order, String remarks) {
//        order = order + remarks;
//        byte[] frameHead = {-0x56, 0x55};
//        byte[] orderByte = order.getBytes();
//        int length = 2 + orderByte.length;
//        byte[] len = {(byte) (length >> 8 & 0xff), (byte) (length & 0xff)};
//        byte[] orderData = ByteUtils.arrayCat(frameHead, cmd);
//        orderData = ByteUtils.arrayCat(orderData, len);
//        orderData = ByteUtils.arrayCat(orderData, serial);
//        orderData = ByteUtils.arrayCat(orderData, orderByte);
//        Log.i("OrderData:::", orderData.toString());
//        return orderData;
//    }


    //解析gps数据
    //返回经度
//    public float parsePushServiceLat(byte[] mData) {
//        return (mData[6] & 0xFF |
//                (mData[7] & 0xFF) << 8 |
//                (mData[8] & 0xFF) << 16 |
//                (mData[9] & 0xFF) << 24);
//    }
//
//    public float parsePushServiceLat3(byte[] mData) {
//        byte b[] = {mData[9], mData[8], mData[7], mData[6]};
//        //方法1 流输入，适用于ME/SE环境
//        //默认大端数，如果小端数，可以先翻转数组
//        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
//        float f = 0;
//        try {
//            f = dis.readFloat();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (dis != null) {
//                try {
//                    dis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return f;
//    }
//
//    public int parsePushServiceLongInt(byte[] mData) {
//        return (mData[9] & 0xFF |
//                (mData[8] & 0xFF) << 8 |
//                (mData[7] & 0xFF) << 16 |
//                (mData[6] & 0xFF) << 24);
//    }
//
//    //返回纬度
//    public float parsePushServiceLong(byte[] mData) {
//        return (mData[10] & 0xFF |
//                (mData[11] & 0xFF) << 8 |
//                (mData[12] & 0xFF) << 16 |
//                (mData[13] & 0xFF) << 24);
//    }
//
//    public float parsePushServiceLong3(byte[] mData) {
//        byte b[] = {mData[13], mData[12], mData[11], mData[10]};
//        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
//        float f = 0;
//        try {
//            f = dis.readFloat();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (dis != null) {
//                try {
//                    dis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return f;
//    }
//
//    public int parsePushServiceLat2(byte[] mData) {
//        return (mData[13] & 0xFF |
//                (mData[12] & 0xFF) << 8 |
//                (mData[11] & 0xFF) << 16 |
//                (mData[10] & 0xFF) << 24);
//    }
//
//    //返回方向
//    public String parsePushServiceDirection(byte[] mData) {
//        return String.valueOf(mData[14]) + String.valueOf(mData[15]);
//    }
//
//    //返回速度
//    public int parsePushServiceSpeed(byte[] mData) {
//        return Integer.valueOf(mData[16]);
//    }
//
//    //返回是否是GPS定位
//    public boolean parsePushServiceIsGPS(byte[] mData) {
//        return Boolean.valueOf(String.valueOf(mData[17]));
//    }
//
//    //返回时间
//    public int parsePushServiceTime(byte[] mData) {
//        return (mData[5] & 0xFF |
//                (mData[4] & 0xFF) << 8 |
//                (mData[3] & 0xFF) << 16 |
//                (mData[2] & 0xFF) << 24);
//    }
//
//    public int parseFindInt(byte[] mData) {
//        return (mData[3] & 0xFF |
//                (mData[2] & 0xFF) << 8 |
//                (mData[4] & 0xFF) << 16 |
//                (mData[5] & 0xFF) << 24);
//    }
//
//    public float parseGPSData(float gps) {
//        int x = (int) gps / 60;
//        float y = gps - 60 * x;
//        y = y / 60;
//        return x + y;
//    }
//
//    public int parseGPSDataToInt(float gps) {
//        int x = (int) gps / 60;
//        float y = gps - 60 * x;
//        y = y / 60;
//        return (int) (x + y);
//    }

    public LatLng convertPoint(LatLng sourcePoint) {
        CoordinateConverter cdc = new CoordinateConverter();
        cdc.from(CoordinateConverter.CoordType.GPS);
        cdc.coord(sourcePoint);
        LatLng desPoint = cdc.convert();
        return desPoint;
    }

//    //3 、GPRS 定时发送设置
//    public void cGprsSend(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x01}, serial, JsonKeys.GPRS_SEND, "");
//        Log.i("定时发送设置", data.toString());
//        //发送数据
//        //xpgWifiDevice.write(data);
//    }
//
//
//    //6 、设置 SOS 管理员 m命令字是 5
//    public byte[] cSOSManagerAdd(byte[] serial, String phoneNumber) {
//        phoneNumber = phoneNumber + "#";
//        byte[] data = packetOrder(new byte[]{0x00, 0x05}, serial, JsonKeys.SOS_ADD, phoneNumber);
//        return data;
//    }
//
//    //7 、删除 SOS  管理员
//    public byte[] cSOSManagerDelete(byte[] serial, String phoneNumber) {
//        phoneNumber = phoneNumber + "#";
//        byte[] data = packetOrder(new byte[]{0x00, 0x05}, serial, JsonKeys.SOS_DELETE, phoneNumber);
//        return data;
//        //	String data = packetOrder(JsonKeys.SOS_DELETE,phoneNumber);
//        //xpgWifiDevice.write(data);
//    }
//
//    //10 工作模式设置
//     /*
//    *  0# 追踪模式：GPS 一直开启；
//    *  1# 智能省电：设备静止时 GPS 关闭，运动或被查询位置时，GPS 会开启； （有些设备有传感器检测自身运动状态）
//    *  2# 睡眠模式：被查询位置时，GPS 会开启；
//    *  3# 冬眠模式：GPS 一直关闭；
//    *  若设置成功，设备会回复：SET SAVING OK
//    */
//    //模式一
//    public void cModeSet0() {
//        //	String data = packetOrder(JsonKeys.MODE_SET_0,"");
//        //xpgWifiDevice.write(data);
//    }
//
//    //模式二
//    public void cModeSet1() {
//        //	String data = packetOrder(JsonKeys.MODE_SET_1,"");
//        //xpgWifiDevice.write(data);
//    }
//
//    //模式三
//    public void cModeSet2() {
//        //	String data = packetOrder(JsonKeys.MODE_SET_2,"");
//        //	xpgWifiDevice.write(data);
//    }
//
//    //模式四
//    public void cModeSet3() {
//        //	String data = packetOrder(JsonKeys.MODE_SET_3,"");
//        //	xpgWifiDevice.write(data);
//    }
//
//    //添加电子围栏  命令字 1
//    public byte[] cFenceAdd(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x01}, serial, JsonKeys.FENCE_SET_1, "");
//        return data;
//    }
//
//    //删除电子围栏 命令字 2
//    public byte[] cFenceDelete(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x02}, serial, JsonKeys.FENCE_DELETE, "");
//        return data;
//    }
//
//    //查询电子围栏  命令字 3
//    public byte[] cFenceSearch(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x03}, serial, "FENCE,1?", "");
//        return data;
//    }
//
//    //开始找车 命令字是4
//    public byte[] cFindEle(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x04}, serial, "START_FIND?", "");
//        return data;
//    }
//
//    //停止找车 命令字是5
//    public byte[] cStopFindEle(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x05}, serial, "STOP_FIND?", "");
//        return data;
//    }
//
//    //25 查询经纬度  命令字 6
//    public byte[] cWhere(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x06}, serial, JsonKeys.GET_GPS, "");
//        return data;
//    }
//
///***************************************以下功能还未开通************************************************************/
//    /**
//     * @param serial
//     * @return
//     */
//    //查询管理员 命令字是7
//    public byte[] cSOSSearch(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, 0x06}, serial, "SOS?", "");
//        return data;
//    }
//
//
//    //测试报警
//    public byte[] cTest(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, -1}, serial, "AA", "");
//        return data;
//    }
//
//    //测试GPS
//    public byte[] cTestGPS(byte[] serial) {
//        byte[] data = packetOrder(new byte[]{0x00, -2}, serial, "AA", "");
//        return data;
//    }
//
//    //21 重启设备
//    public void cResetDevice() {
//        //	String data = packetOrder(JsonKeys.RESET,"");
//        //xpgWifiDevice.write(data);
//    }
//
//
//    public byte[] getSerial(byte firstByte, byte secondByte) {
//        if (secondByte == 127) {
//            secondByte = 0x00;
//            if (firstByte == 127) {
//                firstByte = 0x00;
//            } else {
//                firstByte++;
//            }
//        } else {
//            secondByte++;
//        }
//        return new byte[]{firstByte, secondByte};
//    }

    /*************************************************************
     * 新协议
     ************************************************************/
    public byte[] cmdFenceOn() {
        return getCmdString(JsonKeys.FENCE_ON).getBytes();
    }

    public byte[] cmdFenceOff() {
        return getCmdString(JsonKeys.FENCE_OFF).getBytes();
    }

    public byte[] cmdFenceGet() {
        return getCmdString(JsonKeys.FENCE_GET).getBytes();
    }

    public byte[] cmdWhere() {
        return getCmdString(JsonKeys.GET_GPS).getBytes();
    }

    ;
    public byte[] cmdSeekOn() {
        return getCmdString(JsonKeys.SEEK_ON).getBytes();
    }

    public byte[] cmdSeekOff() {
        return getCmdString(JsonKeys.SEEK_OFF).getBytes();
    }

    private String getCmdString(int cmd) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(JsonKeys.CMD, cmd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }


}
