package com.xunce.electrombile.activity;

/**
 * Created by lybvinci on 15/12/16.
 */
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.content.Context;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.xunce.electrombile.R;

public class ExpandableAdapter extends BaseExpandableListAdapter{
    private TestddActivity activity;
    private Context context;
    private ChildViewHold childViewHold;
    private List<Item> ItemList;


    class ChildViewHold{
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView textView4;
    }
    public ExpandableAdapter(TestddActivity activity,List<Item> ItemList) {
        this.activity = activity;
        this.context = activity;
        this.ItemList = ItemList;
    }

    public void ChangeItemList(List<Item> ItemList)
    {
        this.ItemList = ItemList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return ItemList.get(groupPosition).getDate();
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
            convertView = minflater.inflate(R.layout.groupview,null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.grouptextview1);
        tv.setText(ItemList.get(groupPosition).getDate());
        tv.setTextSize(25);
        tv.setPadding(15, 5, 0, 0);
        return convertView;

    }
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

            if(convertView == null) {
            LayoutInflater minflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = minflater.inflate(R.layout.listviewchildren,null);
            childViewHold = new ChildViewHold();
            childViewHold.textView1 = (TextView) convertView.findViewById(R.id.textview1);
            childViewHold.textView2 = (TextView) convertView.findViewById(R.id.textview2);
            childViewHold.textView3 = (TextView) convertView.findViewById(R.id.textview3);
            childViewHold.textView4 = (TextView) convertView.findViewById(R.id.textview4);


            convertView.setTag(childViewHold);
        }else{
            childViewHold = (ChildViewHold)convertView.getTag();
        }

        List<Message> messageList= ItemList.get(groupPosition).getMessagelist();

        childViewHold.textView1.setText(messageList.get(childPosition).getTime());
        childViewHold.textView2.setText(ItemList.get(groupPosition).getMessagelist().get(childPosition).getStartLocation());
        childViewHold.textView3.setText(ItemList.get(groupPosition).getMessagelist().get(childPosition).getEndLocation());
        childViewHold.textView4.setText("时间行程");
        return convertView;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return ItemList.get(groupPosition).getMessagelist().size();
    }
    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return ItemList.get(groupPosition);
    }
    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return ItemList.size();
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
