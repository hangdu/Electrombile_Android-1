package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.view.RefreshableView;

import java.util.HashMap;
import java.util.List;

public class BindListActivity extends BaseActivity {

    RefreshableView refreshableView;
    private MyAdapter mAdapter;
    private ListView bind_list;
    private TextView tv_default;
    private HashMap<Integer, AVObject> bindList = null;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_bind_list);
        super.onCreate(savedInstanceState);
    }

    //初始化View
    @Override
    public void initViews() {
        bind_list = (ListView) findViewById(R.id.list_view_bind_list);
        tv_default = (TextView) findViewById(R.id.tv_default);
        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在查询，请稍后...");
    }

    //初始化事件
    @Override
    public void initEvents() {
        bindList = new HashMap<>();
        refreshBindList(progressDialog);
        mAdapter = new MyAdapter();
        bind_list.setAdapter(mAdapter);
        bind_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //  Log.e("确定进入长按事件？",i+"");
                final int location = i;
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(BindListActivity.this);
                deleteDialog.setTitle("删除此车？");
                deleteDialog.setMessage("确定删除此车么？");
                deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //拒绝删除正在使用的车
                        if (bindList.get(location).get("IMEI").equals(setManager.getIMEI())) {
                            ToastUtils.showShort(BindListActivity.this, "正在使用此设备，请切换后再试。");
                            return;
                        }
                        //删除绑定车
                        deleteEqu(location);
                    }
                });
                deleteDialog.setNegativeButton("取消", null);
                deleteDialog.show();
                return false;
            }
        });

        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                refreshBindList(null);
                //   mAdapter.notifyDataSetChanged();
            }
        }, 0);
    }

    //删除设备
    private void deleteEqu(final int location) {
        Log.e("location", location + "");
        AVObject tmp = bindList.get(location);
        tmp.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showShort(BindListActivity.this, "删除成功！");
                    bindList.put(location, bindList.get(bindList.size() - 1));
                    bindList.remove(bindList.size() - 1);
                    //刷新listview
                    mAdapter.notifyDataSetChanged();
                } else {
                    ToastUtils.showShort(BindListActivity.this, "删除失败，请下拉刷新列表！");
                }
            }
        });

    }

    //刷新车列表
    private void refreshBindList(final ProgressDialog progressDialog) {
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    bindList.clear();
                    Log.e("BINDLISTACT", list.size() + "");
                    if (list.size() > 0) {
                        bindList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            bindList.put(i, list.get(i));
                            if (progressDialog == null) {
                                refreshableView.finishRefreshing();
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    } else {
                        ToastUtils.showShort(BindListActivity.this, "无可用设备");
                    }
                } else {
                    e.printStackTrace();
                    ToastUtils.showShort(BindListActivity.this, "查询错误");
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

            }
        });
        if (progressDialog != null) {
            progressDialog.show();
        }

    }

    //添加设备
    public void addEquip(View view) {
        Intent intent = new Intent(this, BindingActivity.class);
        startActivity(intent);
        this.finish();
    }


    //设备列表listview 适配器
    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (bindList != null) {
                return bindList.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (bindList == null)
                return null;

            if (tv_default.getVisibility() != View.GONE)
                tv_default.setVisibility(View.GONE);

            View mView;
            if (view == null) {
                LayoutInflater inflater = BindListActivity.this.getLayoutInflater();
                mView = inflater.inflate(R.layout.listview_bind_list, null);
            } else {
                mView = view;
            }
            TextView tvAdmin = (TextView) mView.findViewById(R.id.tv_admin);
            Log.e("bindList" + i, bindList.toString());
            if (bindList.get(i).get("isAdmin").equals("true")) {
                tvAdmin.setText("主车");
            } else {
                tvAdmin.setText("其他车");
            }
            mView.setBackgroundColor(getResources().getColor(R.color.transation));
            if (setManager.getIMEI().equals(bindList.get(i).get("IMEI"))) {
                mView.setBackgroundColor(getResources().getColor(R.color.red));
            }
            Button switchBtn = (Button) mView.findViewById(R.id.btn_switch);
            switchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //不能使用==方法，因为其判断的是对象是否相同。
                    if (setManager.getIMEI().equals(bindList.get(i).get("IMEI"))) {
                        ToastUtils.showShort(BindListActivity.this, "正在使用此设备，无须切换~");
                        return;
                    }
                    setManager.setIMEI((String) bindList.get(i).get("IMEI"));
                    Historys.finishAct(FragmentActivity.class);
                    //FragmentActivity.fragmentActivity.finish();
                    ToastUtils.showShort(BindListActivity.this, "切换中~");
                    Intent intent = new Intent(BindListActivity.this, FragmentActivity.class);
                    startActivity(intent);
                    BindListActivity.this.finish();
                }
            });
            return mView;
        }
    }

}
