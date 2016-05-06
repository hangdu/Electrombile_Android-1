package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.xunce.electrombile.utils.system.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener{

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private List<View> views;
    private ImageView[] dots;
    private int[] ids={R.id.iv_point1,R.id.iv_point2,R.id.iv_point3,R.id.iv_point4};
    private Button b_enter;
    private ImageView img_guide1;
    private ImageView img_guide2;
    private ImageView img_guide3;
    private ImageView img_guide4;
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

        View view1 = layoutInflater.inflate(R.layout.guide_one, null);
        View view2 = layoutInflater.inflate(R.layout.guide_two,null);
        View view3 = layoutInflater.inflate(R.layout.guide_three,null);
        View view4 = layoutInflater.inflate(R.layout.guide_four,null);

        img_guide1 = (ImageView)view1.findViewById(R.id.img_guide1);
        img_guide1.setImageBitmap(BitmapUtils.readBitMap(this,R.drawable.img_guide1));

        img_guide2 = (ImageView)view2.findViewById(R.id.img_guide2);
        img_guide2.setImageBitmap(BitmapUtils.readBitMap(this,R.drawable.img_guide2));

        img_guide3 = (ImageView)view3.findViewById(R.id.img_guide3);
        img_guide3.setImageBitmap(BitmapUtils.readBitMap(this,R.drawable.img_guide3));

        img_guide4 = (ImageView)view4.findViewById(R.id.img_guide4);
        img_guide4.setImageBitmap(BitmapUtils.readBitMap(this,R.drawable.img_guide4));

        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);

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
                dots[i].setImageResource(R.drawable.guide_point_selected1);
            }else{
                dots[i].setImageResource(R.drawable.guide_point1);
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


    @Override
    public void onDestroy(){
        super.onDestroy();
        releaseImageViewResouce(img_guide1);
        releaseImageViewResouce(img_guide2);
        releaseImageViewResouce(img_guide3);
        releaseImageViewResouce(img_guide4);
    }


    public static void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
}

