package com.xunce.electrombile.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.MapOfflineActivity;
import com.xunce.electrombile.bean.OfflinemapBean;

import java.util.List;

/**
 * Created by Xingw on 2015/12/15.
 */
public class OfflineMap_ExpandableList_Adapter extends BaseExpandableListAdapter {
    private class buttonViewHolder {
        TextView cityName;
        TextView downstate;
        ImageButton buttonDownload;
    }

    private LayoutInflater mInflater;
    private static List<OfflinemapBean> offlinemapBeen;
    private Context mContext;
    private buttonViewHolder holder;
    private Drawable downloaddrawable; //下载的图片
    private Drawable closedrawable; //删除的图片
    private Drawable pausedrawable;
    private MKOfflineMap mkOfflineMap;


    public OfflineMap_ExpandableList_Adapter(List<OfflinemapBean> offlinemapBeen, Context mContext,
                                             MKOfflineMap mkOfflineMap) {
        this.offlinemapBeen = offlinemapBeen;
        this.mContext = mContext;
        this.mkOfflineMap = mkOfflineMap;
        this.downloaddrawable = new IconDrawable(mContext, Iconify.IconValue.zmdi_download);
        this.closedrawable = new IconDrawable(mContext, Iconify.IconValue.zmdi_close_circle_o);
        this.pausedrawable = new IconDrawable(mContext, Iconify.IconValue.zmdi_pause);
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return offlinemapBeen.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return offlinemapBeen.get(groupPosition).childCities.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return offlinemapBeen.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return offlinemapBeen.get(groupPosition).childCities.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView != null) {
            holder = (buttonViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.item_mapofflinelist_parent, null);
            holder = new buttonViewHolder();
            holder.cityName = (TextView) convertView.findViewById(R.id.Item_TextView_Parent);
//            holder.buttonDownload = (ImageButton) convertView.findViewById(R.id
//                    .Item_ImageButton_Parent);
            convertView.setTag(holder);
        }
        OfflinemapBean bean = offlinemapBeen.get(groupPosition);
        if (bean != null) {
            holder.cityName.setText(bean.getCityName());
        }
//        if(isExpanded) {
//            holder.buttonDownload.setImageDrawable(chervondrawable_down);
//        }else{
//            holder.buttonDownload.setImageDrawable(chervondrawable_up);
//        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
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
        OfflinemapBean bean = offlinemapBeen.get(groupPosition).getChildCities().get(childPosition);
        if (bean != null) {
            holder.cityName.setText(bean.getCityName());
            holder.buttonDownload.setOnClickListener(new lvButtonListener(bean));
            UpdateStateText(holder.downstate, bean);
            UpdateStateDrawable(holder.buttonDownload, bean);
        }

        return convertView;
    }

    /**
     * 设置下载进度
     *
     * @param progress 下载进度
     * @param cityId   城市的ID
     */
    public void setDownProgress(int progress, int cityId) {
        getBeanbyId(cityId).setProgress(progress);
    }

    /**
     * 根据状态更新文字显示 逻辑：初始状态为不可见 点击下载按钮 更新下载百分比 下载完成后显示已下载
     *
     * @param downstate
     * @param bean
     */
    private void UpdateStateText(TextView downstate, OfflinemapBean bean) {
        switch (bean.getState()) {
            case NONE:
                downstate.setText("");
                break;
            case LOADING:
                downstate.setText(bean.getProgress() + "%");
                break;
            case FINISHED:
                downstate.setText("已完成");
                break;
            case UPDATE:
                downstate.setText("可更新");
                break;
            case SUSPENDED:
                downstate.setText("暂停");
                break;
        }
    }

    /**
     * 更新图片显示
     *
     * @param imageButton 显示的图片
     * @param bean        检查状态
     */
    private void UpdateStateDrawable(ImageButton imageButton, OfflinemapBean bean) {
        if (bean.childCities == null) {
            switch (bean.getState()) {
                case NONE:
                    imageButton.setImageDrawable(downloaddrawable);
                    break;
                case SUSPENDED:
                    imageButton.setImageDrawable(downloaddrawable);
                    break;
//                case LOADING:
//                    imageButton.setImageDrawable(pausedrawable);
//                    break;
                default:
                    imageButton.setImageDrawable(closedrawable);
                    break;
            }
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * 按城市ID检索相应Bean
     *
     * @param cityId 城市ID号
     * @return
     */
    public OfflinemapBean getBeanbyId(int cityId) {
        for (int i = 0; i < offlinemapBeen.size(); i++) {
            for (OfflinemapBean bean : offlinemapBeen.get(i).getChildCities()) {
                if (bean.getCityId() == cityId)
                    return bean;
            }
        }
        return null;
    }

//    /**
//     * 开始下载
//     */
//    public void downloadCity(int cityId) {
//        com.orhanobut.logger.Logger.i("开始下载离线地图ID是：" + cityId);
//        getBeanbyId(cityId).setState(OfflinemapBean.STATE.LOADING);
//        //MapOfflineActivity.ManagernotifyDataSetChanged();
//        mkOfflineMap.start(cityId);
//        Toast.makeText(mContext, "开始下载离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
//                .show();
//        updateView();
//    }
//
//    /**
//     * 暂停下载
//     */
//    public void stop(int cityId) {
//        mkOfflineMap.pause(cityId);
//        getBeanbyId(cityId).setState(OfflinemapBean.STATE.SUSPENDED);
//        Toast.makeText(mContext, "暂停下载离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
//                .show();
//        updateView();
//    }
//
//    /**
//     * 删除离线地图
//     */
//    public void remove(int cityId) {
//        mkOfflineMap.remove(cityId);
//        Toast.makeText(mContext, "删除离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
//                .show();
//        getBeanbyId(cityId).setState(OfflinemapBean.STATE.NONE);
//        //MapOfflineActivity.ManagernotifyDataSetChanged();
//        updateView();
//    }

    public void updateView() {
//        localMapList = mkOfflineMap.getAllUpdateInfo();
//        if (localMapList == null) {
//            localMapList = new ArrayList<MKOLUpdateElement>();
//        }
        notifyDataSetChanged();
    }

    /**
     * 下载按钮事件监听
     */
    class lvButtonListener implements View.OnClickListener {
        private OfflinemapBean bean;

        public lvButtonListener(OfflinemapBean bean) {
            this.bean = bean;
        }

        @Override
        public void onClick(View v) {
            if (bean.getState() == OfflinemapBean.STATE.NONE || bean.getState() ==
                    OfflinemapBean.STATE.SUSPENDED) {
                //downloadCity(bean.getCityId());
                MapOfflineActivity.downloadCity(bean.getCityId());
            }
//                else if(bean.getState() == OfflinemapBean.STATE.LOADING){
//                stop(bean.getCityId());
//            }
            else {
                //remove(bean.getCityId());
                MapOfflineActivity.remove(bean.getCityId());
            }
        }
    }
}
