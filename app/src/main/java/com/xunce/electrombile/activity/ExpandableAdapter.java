package com.xunce.electrombile.activity;

/**
 * Created by lybvinci on 15/12/16.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.content.Context;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.xunce.electrombile.R;

public class ExpandableAdapter extends SimpleExpandableListAdapter{

    private static final String DATE = "date";
    private static final String DISTANCEPERDAY = "distancePerDay";
    private static final String STARTPOINT = "startPoint";
    private static final String ENDPOINT = "endPoint";


    private TestddActivity activity;
    private Context context;
    private ChildViewHold childViewHold;
//    private List<Item> ItemList;

    List<Map<String, String>> groupData;
    List<List<Map<String, String>>> childData;

    class ChildViewHold{
        TextView textView1;
        TextView textView2;
//        TextView textView3;
//        TextView textView4;
    }
//    public ExpandableAdapter(TestddActivity activity,List<Item> ItemList) {
//        this.activity = activity;
//        this.context = activity;
//        this.ItemList = ItemList;
//    }

    public ExpandableAdapter(Context context,
                             List<Map<String, String>> groupData, int groupLayout,
                             String[] groupFrom, int[] groupTo,
                             List<List<Map<String, String>>> childData,
                             int childLayout, String[] childFrom, int[] childTo,
                             TestddActivity activity) {

        super(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
                childLayout, childLayout, childFrom, childTo);
        this.activity = activity;
        this.context = activity;
//        this.ItemList = ItemList;

        this.groupData = groupData;
        this.childData = childData;


    }

//    public void ChangeItemList(List<Item> ItemList)
//    {
//        this.ItemList = ItemList;
//    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childData.get(groupPosition).get(childPosition);
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if(convertView == null) {
            LayoutInflater minflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = minflater.inflate(R.layout.expandgroupview,null);
        }
        TextView tv_groupDate = (TextView) convertView.findViewById(R.id.groupDate);
        TextView tv_distance = (TextView) convertView.findViewById(R.id.distance);

        tv_groupDate.setText(groupData.get(groupPosition).get(DATE));
        tv_distance.setText(groupData.get(groupPosition).get(DISTANCEPERDAY));

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if(convertView == null) {
        LayoutInflater minflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = minflater.inflate(R.layout.expandchildview,null);
        childViewHold = new ChildViewHold();
        childViewHold.textView1 = (TextView) convertView.findViewById(R.id.tv_startPoint);
        childViewHold.textView2 = (TextView) convertView.findViewById(R.id.tv_endPoint);
        convertView.setTag(childViewHold);
        }
        else{
            childViewHold = (ChildViewHold)convertView.getTag();
        }

        childViewHold.textView1.setText(childData.get(groupPosition).get(childPosition).get(STARTPOINT));
        childViewHold.textView2.setText(childData.get(groupPosition).get(childPosition).get(ENDPOINT));

        return convertView;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return childData.get(groupPosition).size();
    }
    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return groupData.get(groupPosition);
    }
    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return groupData.size();
    }
    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }
    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        activity.GetHistoryTrack(groupPosition);
    }
}
