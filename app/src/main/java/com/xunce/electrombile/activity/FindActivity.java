package com.xunce.electrombile.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class FindActivity extends BaseActivity {

    private static final String TAG = "FindActivity";
    private final int TIME_OUT = 0;
    private boolean isFinding = false;

    private String IMEI;
    private ImageView scanner;
    private ProgressDialog progressDialog;
    private CmdCenter mCenter;
    private MyReceiver receiver;
    private Animation operatingAnim;
    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.dismiss();
            ToastUtils.showShort(FindActivity.this, "指令下发失败，请检查网络和设备工作是否正常。");
        }
    };
    private MqttAndroidClient mac;
    private Button findBtn;

    private LinearLayout ll_draw;
    private XYSeries series;
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer renderer;
    private GraphicalView chart;
    private int addX = -1, addY;
    private double xMin = 0;
    private double xMax = 100;
    //刷新的间隔.
    private int separated = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find);
        super.onCreate(savedInstanceState);
        initDrawView();
    }

    @Override
    public void initViews() {
        scanner = (ImageView) findViewById(R.id.iv_scanner);
        //ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        findBtn = (Button) findViewById(R.id.find_button);
        progressDialog = new ProgressDialog(this);
        mCenter = CmdCenter.getInstance();

        ll_draw = (LinearLayout) findViewById(R.id.ll_draw);
    }

    @Override
    public void initEvents() {
        startMqttClient();
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        progressDialog.setMessage("正在配置...");
        progressDialog.setCancelable(false);
        registerBroadCast();
        IMEI = setManager.getIMEI();
    }

    private void startMqttClient() {
        if (ServiceConstants.handler.isEmpty()) {
            return;
        }
        Connection connection = Connections.getInstance(this).getConnection(ServiceConstants.handler);
        mac = connection.getClient();
    }

    private void registerBroadCast() {
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xunce.electrombile.find");
        try {
            registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startFind(View view) {
        timeHandler.sendEmptyMessageDelayed(TIME_OUT, 5000);
        if (!isFinding) {
            if (mac != null && mac.isConnected()) {
                sendMessage(FindActivity.this, mCenter.cmdSeekOn(), IMEI);
                progressDialog.show();
            } else {
                ToastUtils.showShort(this, "服务未连接...");
                timeHandler.removeMessages(TIME_OUT);
                return;
            }
            isFinding = !isFinding;
        } else {
            if (mac != null && mac.isConnected()) {
                sendMessage(FindActivity.this, mCenter.cmdSeekOff(), IMEI);
                progressDialog.show();
            } else {
                ToastUtils.showShort(this, "服务未连接...");
                timeHandler.removeMessages(TIME_OUT);
                return;
            }
            isFinding = !isFinding;
        }

    }

    private void sendMessage(Context context, byte[] message, String IMEI) {
        if (mac == null) {
            ToastUtils.showShort(context, "请先连接设备，或等待连接。");
            return;
        }
        try {
            mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //取消等待框，并且刷新界面
    private void cancelDialog(int data) {
        timeHandler.removeMessages(TIME_OUT);
        progressDialog.dismiss();
        // float rating = (float) (data / 200.0);
        //ratingBar.setRating(rating);
        int rating = data / 20 - 20;
        Log.d(TAG, "rating:" + rating);
        updateChart(rating);
        if (isFinding) {
            if (operatingAnim != null) {
                scanner.startAnimation(operatingAnim);
            }
            findBtn.setText("停止找车");
        } else {
            scanner.clearAnimation();
            findBtn.setText("开始找车");
        }
    }

    private void initDrawView() {
        //这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        series = new XYSeries("信号强度图");

        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();

        //将点集添加到这个数据集中
        mDataset.addSeries(series);

        //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int color = Color.BLUE;
        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(color, style, true);

        //设置好图表的样式
        setChartSettings(renderer, "X", "Y", 0, 100, xMin, xMax, Color.WHITE, Color.WHITE);

        //生成图表
        chart = ChartFactory.getLineChartView(this, mDataset, renderer);

        //将图表添加到布局中去
        ll_draw.addView(chart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    }

    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(3);
        renderer.addSeriesRenderer(r);

        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
                                    double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        //有关对图表的渲染可参看api文档
        renderer.setChartTitle("信号强度图");
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GREEN);
        //renderer.setMarginsColor(Color.CYAN);
        renderer.setXLabels(10);
        renderer.setYLabels(10);
        renderer.setXTitle("");
        renderer.setYTitle("信号强度图");
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setPointSize((float) 2);
        renderer.setShowLegend(false);
        renderer.setPanEnabled(true, false);
    }

    private void updateChart(int y) {
        //设置好下一个需要增加的节点
        addX++;
        if (addX != 0 && addX % separated == 0) {
            xMin += 50;
            xMax += 50;
            renderer.setXAxisMin(xMin);
            renderer.setXAxisMax(xMax);
            if (addX == 100) {
                separated = 50;
            }
        }
        addY = y;
        //移除数据集中旧的点集
        mDataset.removeSeries(series);
        //将新产生的点加入到点集中
        series.add(addX, addY);
        //在数据集中添加新的点集
        mDataset.addSeries(series);
        //视图更新，没有这一步，曲线不会呈现动态
        //如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        chart.invalidate();
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "find接收调用");
            Bundle bundle = intent.getExtras();
            int data = bundle.getInt("intensity");
            Log.i(TAG, data + "");
            cancelDialog(data);
        }
    }
}
