package com.xunce.electrombile.manager;

import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.AVObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by heyukun on 2015/4/22.
 */
public class TracksManager implements Serializable{
    private static ArrayList<ArrayList<TrackPoint>> tracks;
    private static ArrayList<ArrayList<ArrayList<TrackPoint>>> GroupTracks;


    private final String TAG = "TracksManager";
    private final String KET_TIME = "createdAt";
    private final String KET_LONG = "lon";
    private final String KET_LAT = "lat";
    private final long MAX_TIMEINRVAL = 30 * 60;//30分钟
    private final long MAX_DISTANCE = 200;//30分钟
    private CmdCenter mCenter;
    private HashMap<String, ArrayList<ArrayList<TrackPoint>>> map;

    public TracksManager(Context context){
        tracks = new ArrayList<ArrayList<TrackPoint>>();
        mCenter = CmdCenter.getInstance();
        map = new HashMap<>();
    }

    public static ArrayList<ArrayList<TrackPoint>> getTracks(){
        return tracks;
    }

    public static void clearTracks(){
        tracks.clear();
    }

    public ArrayList<TrackPoint> getTrack(int position) {
        return tracks.get(position);
    }

    public void setTracksData(ArrayList<ArrayList<TrackPoint>> data) {
        tracks = data;
    }

    public boolean isOutOfHubei(LatLng point){
            return !((point.longitude > 108) && (point.longitude < 116) && (point.latitude > 29) && (point.latitude < 33));
    }

    //这个函数看的不是很懂啊
    public void setTranks(int groupposition,List<AVObject> objects){
        tracks = new ArrayList<>();
        Log.i("Track managet-----", "setTranks" + objects.size());
        AVObject lastSavedObject = null;
        LatLng lastSavedPoint = null;
        ArrayList<TrackPoint> dataList = null;

        for(AVObject thisObject: objects){
            if(dataList == null){
                dataList = new ArrayList<>();
                tracks.add(dataList);
            }
            double lat = thisObject.getDouble(KET_LAT);
            double lon = thisObject.getDouble(KET_LONG);

            //百度地图的LatLng类对输入有限制，如果longitude过大，则会导致结果不正确
            //lybvinci 修改 @date 9.28
            LatLng oldPoint = new LatLng(lat, lon);
            LatLng bdPoint = mCenter.convertPoint(oldPoint);

            //如果本次循环数据跟上一个已保存的数据坐标相同，则跳过
            double dis = Math.abs(DistanceUtil.getDistance(lastSavedPoint, bdPoint));
            Log.i("******", dis + "");
            //如果上次的点和这次的点之间的距离小于200m就不记录了  这样合理吗????
            if(lastSavedObject != null && dis  <= MAX_DISTANCE){
                //Log.i("","distance should less 200M:::" + dis);
                continue;
            }

            //如果下一个数据与上一个已保存的数据时间间隔大于MAX_TIMEINRVAL
            if(lastSavedObject != null &&((thisObject.getCreatedAt().getTime() - lastSavedObject.getCreatedAt().getTime()) / 1000 >= MAX_TIMEINRVAL)){
                Log.e("stilllllll point", "");
//                if(dataList.size() > 1) {
//                    tracks.add(dataList);
//                }
                if(tracks.get(tracks.size() - 1).size() <= 1)
                    tracks.remove(tracks.size() - 1);
                    dataList = new ArrayList<>();
                    tracks.add(dataList);
                }

            //打印当前点信息
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TrackPoint p = new TrackPoint(thisObject.getCreatedAt(), bdPoint);
            //不确定这样处理会不会有什么错误   现在关于日期处理的这个部分还没有搞得很清楚
            p.time.setHours(p.time.getHours());
            if(isOutOfHubei(bdPoint)){
                Log.i(TAG, "out range");
                continue;
            }
            dataList.add(p);
            lastSavedObject = thisObject;
            lastSavedPoint = bdPoint;

        }
        //当只有一个列表且列表内只有一个数据时，移除
        if(tracks.size() == 1 && tracks.get(0).size() <= 1){
            tracks.remove(tracks.size() - 1);
        }
        Log.i(TAG, "tracks1 size:" + tracks.size());
        SetMapTrack(groupposition,tracks);
    }
    
    public static class TrackPoint implements Serializable{
        public Date time;
        public LatLng point;

        public TrackPoint(Date t, LatLng p) {
            time = t;
            point = p;
        }

        public TrackPoint(Date t, double lat, double lon) {
            time = t;
            point = new LatLng(lat, lon);
        }
    }

    public void SetMapTrack(int groupposition, ArrayList<ArrayList<TrackPoint>> tracks){
        String grouppositon_str = String.valueOf(groupposition);
        map.put(grouppositon_str, tracks);

        int size =tracks.size();

        Log.d(" tracks_size",String.valueOf(size));
    }

    public HashMap<String, ArrayList<ArrayList<TrackPoint>>> getMapTrack(){
        return map;
    }

    public void RefreshTracks()
    {
        tracks = new ArrayList<>();
    }

}
