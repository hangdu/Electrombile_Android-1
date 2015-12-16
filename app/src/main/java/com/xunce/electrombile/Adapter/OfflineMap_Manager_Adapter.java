package com.xunce.electrombile.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.MapOfflineActivity;
import com.xunce.electrombile.bean.OfflinemapBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xingw on 2015/12/15.
 */
public class OfflineMap_Manager_Adapter extends BaseAdapter {

    private class buttonViewHolder {
        TextView cityName;
        TextView downstate;
        ImageButton buttonDownload;
    }
    private LayoutInflater mInflater;
    private Context mContext;
    private buttonViewHolder holder;
    private Drawable downloaddrawable; //下载的图片
    private Drawable closedrawable; //删除的图片
    private Drawable pausedrawable; //暂停的图片
    private static MKOfflineMap mkOfflineMap;
    private ArrayList<MKOLUpdateElement> mkolUpdateElements;

//    public static void setDataList(){
//        mkolUpdateElements = mkOfflineMap.getAllUpdateInfo();
//    }

    public OfflineMap_Manager_Adapter(Context mContext,
                                      MKOfflineMap mkOfflineMap) {
        this.mContext = mContext;
        this.mkOfflineMap = mkOfflineMap;
        this.downloaddrawable = new IconDrawable(mContext, Iconify.IconValue.zmdi_download);
        this.closedrawable = new IconDrawable(mContext, Iconify.IconValue.zmdi_close_circle_o);
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.pausedrawable = new IconDrawable(mContext, Iconify.IconValue.zmdi_pause);
        mkolUpdateElements = mkOfflineMap.getAllUpdateInfo();
        if (mkolUpdateElements == null) {
            mkolUpdateElements = new ArrayList<MKOLUpdateElement>();
        }
    }

    /**
     * 更新界面
     */
    public void updateView() {
        mkolUpdateElements = mkOfflineMap.getAllUpdateInfo();
       if (mkolUpdateElements == null) {
            mkolUpdateElements = new ArrayList<MKOLUpdateElement>();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mkolUpdateElements.size();
    }

    @Override
    public Object getItem(int position) {
        return mkolUpdateElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            holder = (buttonViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.item_mapofflinelist_child, null);
            holder = new buttonViewHolder();
            holder.cityName = (TextView) convertView.findViewById(R.id.Item_TextView_Child);
            holder.buttonDownload = (ImageButton) convertView.findViewById(R.id
                    .Item_ImageButton_child);
            holder.downstate = (TextView) convertView.findViewById(R.id.Item_downstate_child);
            convertView.setTag(holder);
        }
        MKOLUpdateElement element= mkolUpdateElements.get(position);
        if (element != null) {
            holder.cityName.setText(element.cityName);
            holder.buttonDownload.setOnClickListener(new lvButtonListener(element));
            UpdateStateText(holder.downstate, element);
            UpdateStateDrawable(holder.buttonDownload, element);
        }

        return convertView;
    }

    private void UpdateStateDrawable(ImageButton buttonDownload, MKOLUpdateElement element) {
        switch (element.status){
//            case MKOLUpdateElement.DOWNLOADING:
//                buttonDownload.setImageDrawable(closedrawable);
//                break;
//            case MKOLUpdateElement.FINISHED:
//                buttonDownload.setImageDrawable(closedrawable);
//                break;
//            case MKOLUpdateElement.SUSPENDED:
//                buttonDownload.setImageDrawable(downloaddrawable);
//                break;
            default:
                buttonDownload.setImageDrawable(closedrawable);
                break;
        }
    }

    private void UpdateStateText(TextView downstate, MKOLUpdateElement element) {
        switch (element.status){
            case MKOLUpdateElement.DOWNLOADING:
                downstate.setText(element.ratio+"%");
                break;
            case MKOLUpdateElement.FINISHED:
                downstate.setText("已完成");
                break;
            case MKOLUpdateElement.WAITING:
                downstate.setText(element.ratio+"%");
                break;
            default:
                downstate.setText("下载失败");
                break;
        }
    }

    private class lvButtonListener implements View.OnClickListener {
        private MKOLUpdateElement element;
        public lvButtonListener(MKOLUpdateElement element) {
            this.element = element;
        }

        @Override
        public void onClick(View v) {
            switch (element.status){
//                case MKOLUpdateElement.DOWNLOADING:
//                    mkOfflineMap.remove(element.cityID);
//                    updateView();
//                    break;
//                case MKOLUpdateElement.FINISHED:
//                    mkOfflineMap.remove(element.cityID);
//                    updateView();
//                    break;
////                case MKOLUpdateElement.SUSPENDED:
////                    mkOfflineMap.start(element.cityID);
////                    updateView();
////                    break;
                //删除相应的下载，并通知另一个Adapter刷新信息
                default:
//                    mkOfflineMap.remove(element.cityID);
                    MapOfflineActivity.remove(element.cityID);
//                    updateView();
                    break;
            }
        }
    }
}
