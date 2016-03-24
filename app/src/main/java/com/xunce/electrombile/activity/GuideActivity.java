package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener{

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private List<View> views;
    private ImageView[] dots;
    private int[] ids={R.id.iv_point1,R.id.iv_point2,R.id.iv_point3,R.id.iv_point4};
    private Button b_enter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        //初始化界面
        initViews();
        //初始化小圆点
        initDots();
    }

    public void initViews(){
        LayoutInflater layoutInflater= LayoutInflater.from(this);
        views=new ArrayList<>();
        views.add(layoutInflater.inflate(R.layout.guide_one,null));
        views.add(layoutInflater.inflate(R.layout.guide_two,null));
        views.add(layoutInflater.inflate(R.layout.guide_three,null));
        views.add(layoutInflater.inflate(R.layout.guide_four,null));
        viewPagerAdapter=new ViewPagerAdapter(views,this);
        viewPager= (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(viewPagerAdapter);
        b_enter= (Button) views.get(3).findViewById(R.id.b_enter);
        b_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        viewPager.setOnPageChangeListener(this);
    }

    public void initDots(){
        dots=new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i=0;i<ids.length;i++){
            if(position==i){
                dots[i].setImageResource(R.drawable.guide_point_selected);
            }else{
                dots[i].setImageResource(R.drawable.guide_point);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class ViewPagerAdapter extends PagerAdapter {

        //界面列表
        private List<View> views;
        private Context context;

        public ViewPagerAdapter(List<View> views,Context context){
            this.views=views;
            this.context=context;
        }

        //销毁position位置的界面
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView(views.get(position));
        }

        //初始化position位置的界面
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager)container).addView(views.get(position));
            return views.get(position);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view==object);
        }
    }
}

