package com.xunce.electrombile.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.xunce.electrombile.R;
import com.xunce.electrombile.bean.ViewPagerBean;
import com.xunce.electrombile.utils.useful.JSONUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by lybvinci on 15/11/20.
 */
public class InformationFragment extends BaseFragment {

    private static String TAG = "InformationFragment";
    protected int lastPosition;
    //缓存view
    private View rootView;
    //viewpager
    private ViewPager viewPager;
    private LinearLayout pointGroup;
    private TextView imageDesc;
    // 图片资源ID
    private int[] imageIds = new int[5];
    //    private int[] imageIds = {R.drawable.first_fragment_iv, R.drawable.linshitupian1, R.drawable.linshitupian2,
//            R.drawable.linshitupian3,R.drawable.first_fragment_iv,R.drawable.first_fragment_iv,R.drawable.first_fragment_iv,R.drawable.first_fragment_iv,
//            R.drawable.first_fragment_iv,R.drawable.first_fragment_iv};
    private ViewPagerBean viewPagerBean;
    //判断是否自动滚动
    private boolean isRunning = false;
    private Handler viewPagerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //让viewPager 滑动到下一页
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            if (isRunning) {
                viewPagerHandler.sendEmptyMessageDelayed(0, 2000);
            }
        }
    };

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtils http = new HttpUtils();
                http.send(HttpRequest.HttpMethod.GET,
                        "http://www.tngou.net/tnfs/api/news",
                        new RequestCallBack<String>() {

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String data = StringUtils.decodeUnicode(responseInfo.result);
                                Log.i(TAG, "获取图片信息：" + data);
                                String tngou = null;
                                try {
                                    tngou = JSONUtils.ParseJSON(data, "tngou");
                                    tngou = tngou.substring(1, tngou.length() - 1);
                                    String[] tngous = tngou.split("\\},");
                                    String[] imgs = new String[imageIds.length];
                                    String[] titles = new String[imageIds.length];
                                    for (int i = 0; i < imageIds.length; i++) {
                                        imgs[i] = JSONUtils.ParseJSON(tngous[i] + "}", "img");
                                        titles[i] = JSONUtils.ParseJSON(tngous[i] + "}", "title");
                                    }
                                    for (int j = 0; j < imageIds.length; j++) {
                                        loadAndSetImg(viewPagerBean.imageList.get(j), viewPagerBean.url + imgs[j]);
                                    }
                                    viewPagerBean.imageDescriptions = titles;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return;
                                }

                            }

                            @Override
                            public void onFailure(HttpException e, String s) {

                            }
                        });
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.informatin_fragment, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewpager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    //初始化viewpager
    private void initViewpager() {
        if (viewPager == null) {
            viewPagerBean = new ViewPagerBean();
            viewPager = (ViewPager) getActivity().findViewById(R.id.banner_viewpager);
            pointGroup = (LinearLayout) getActivity().findViewById(R.id.point_group);
            imageDesc = (TextView) getActivity().findViewById(R.id.image_desc);
            imageDesc.setText(viewPagerBean.imageDescriptions[0]);
            viewPagerBean.imageList = new ArrayList<ImageView>();
            for (int i = 0; i < imageIds.length; i++) {
                //初始化图片资源
                ImageView image = new ImageView(m_context);
                image.setImageResource(imageIds[i]);
                viewPagerBean.imageList.add(image);

                //添加指示点
                ImageView point = new ImageView(m_context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.rightMargin = 10;
                point.setLayoutParams(params);
                point.setBackgroundResource(R.drawable.point_bg);
                if (i == 0) {
                    point.setEnabled(false);
                } else {
                    point.setEnabled(true);
                }
                pointGroup.addView(point);
            }

            viewPager.setAdapter(new MyPagerAdapter());

            viewPager.setCurrentItem(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % viewPagerBean.imageList.size()));

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                /**
                 * 页面切换后调用
                 * position  新的页面位置
                 */
                public void onPageSelected(int position) {

                    position = position % viewPagerBean.imageList.size();

                    //设置文字描述内容
                    imageDesc.setText(viewPagerBean.imageDescriptions[position]);

                    //改变指示点的状态
                    //把当前点enbale 为true
                    pointGroup.getChildAt(position).setEnabled(true);
                    //把上一个点设为false
                    pointGroup.getChildAt(lastPosition).setEnabled(false);
                    lastPosition = position;

                }

                @Override
                /**
                 * 页面正在滑动的时候，回调
                 */
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }

                @Override
                /**
                 * 当页面状态发生变化的时候，回调
                 */
                public void onPageScrollStateChanged(int state) {

                }
            });

		 /*
          * 自动循环：
		  * 1、定时器：Timer
		  * 2、开子线程 while  true 循环
		  * 3、ColckManager
		  * 4、 用handler 发送延时信息，实现循环
		  */
            isRunning = true;
            viewPagerHandler.sendEmptyMessageDelayed(0, 2000);

        }
    }

    private void loadAndSetImg(ImageView imageView, String url) {
        com.lidroid.xutils.BitmapUtils bitmapUtils = new com.lidroid.xutils.BitmapUtils(m_context);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.iv_person_head);//加载失败图片
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);//设置图片压缩类型
        bitmapUtils.configDefaultCacheExpiry(500);
        BitmapDisplayConfig bitmapDisplayConfig = new BitmapDisplayConfig();
        bitmapUtils.configDefaultDisplayConfig(bitmapDisplayConfig);
        bitmapUtils.configDefaultBitmapMaxSize(BitmapCommonUtils.getScreenSize(getActivity()).getWidth(),
                BitmapCommonUtils.getScreenSize(getActivity()).getWidth() / 3);
        bitmapUtils.configDefaultAutoRotation(true);
        bitmapUtils.display(imageView, url);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    //viewpager 适配器
    private class MyPagerAdapter extends PagerAdapter {
        @Override
        /**
         * 获得页面的总数
         */
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        /**
         * 获得相应位置上的view
         * container  view的容器，其实就是viewpager自身
         * position 	相应的位置
         */
        public Object instantiateItem(ViewGroup container, int position) {
            // 给 container 添加一个view
            container.addView(viewPagerBean.imageList.get(position % viewPagerBean.imageList.size()));
            //返回一个和该view相对的object
            return viewPagerBean.imageList.get(position % viewPagerBean.imageList.size());
        }

        @Override
        /**
         * 判断 view和object的对应关系
         */
        public boolean isViewFromObject(View view, Object object) {
            if (view == object) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        /**
         * 销毁对应位置上的object
         */
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            object = null;
        }
    }
}
