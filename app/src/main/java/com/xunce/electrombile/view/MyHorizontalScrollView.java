package com.xunce.electrombile.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import com.xunce.electrombile.R;

public class MyHorizontalScrollView extends HorizontalScrollView {
    private LinearLayout linearLayout;
    private ViewGroup myMenu;
    private ViewGroup myContent;
    private int myMenuWidth;
    private int screenWidth;
    private int myMenuPaddingRight = 50;
    private boolean once = false;
    private boolean IsOpen = false;
    public ListView otherCarlistView;
    public ArrayList<HashMap<String, Object>> list;
    private SimpleAdapter simpleAdapter;
    private Context mContext;
    private Handler handler;


    public MyHorizontalScrollView(Context context,AttributeSet attrs){
        super(context,attrs);
        mContext = context;

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;// 屏幕宽度
        myMenuPaddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, context.getResources()
                .getDisplayMetrics());
    }

    public void setHandler(Handler mhandler){
        handler = mhandler;
    }

    /**
     * 设置子View的宽高，决定自身View的宽高，每次启动都会调用此方法
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!once) {//使其只调用一次
            // this指的是HorizontalScrollView，获取各个元素
            linearLayout = (LinearLayout) this.getChildAt(0);// 第一个子元素
            myMenu = (ViewGroup) linearLayout.getChildAt(0);// HorizontalScrollView下LinearLayout的第一个子元素
            myContent = (ViewGroup) linearLayout.getChildAt(1);// HorizontalScrollView下LinearLayout的第二个子元素

            // 设置子View的宽高，高于屏幕一致
            myMenuWidth=myMenu.getLayoutParams().width = screenWidth - myMenuPaddingRight;// 菜单的宽度=屏幕宽度-右边距
            myContent.getLayoutParams().width = screenWidth;// 内容宽度=屏幕宽度
            // 决定自身View的宽高，高于屏幕一致
            // 由于这里的LinearLayout里只包含了Menu和Content所以就不需要额外的去指定自身的宽
            once = true;
        }
    }

    //设置View的位置，首先，先将Menu隐藏（在eclipse中ScrollView的画面内容（非滚动条）正数表示向左移，向上移）
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //刚载入界面的时候隐藏Menu菜单也就是ScrollView向左滑动菜单自身的大小
        if(changed){
            this.scrollTo(myMenuWidth, 0);//向左滑动，相当于把右边的内容页拖到正中央，菜单隐藏
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action=ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                int scrollX=this.getScrollX();//滑动的距离scrollTo方法里，也就是onMeasure方法里的向左滑动那部分
                if(scrollX>=myMenuWidth/3){
                    this.smoothScrollTo(myMenuWidth,0);//向左滑动展示内容  关闭左滑菜单
                    IsOpen = false;
                }else{
                    this.smoothScrollTo(0, 0); //打开左滑菜单
                    IsOpen = true;
                    Message msg = Message.obtain();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    public void toggle(){
        if(IsOpen){
            this.smoothScrollTo(myMenuWidth,0);//向左滑动展示内容  关闭左滑菜单
            IsOpen = false;
        }
        else{
            this.smoothScrollTo(0, 0); //打开左滑菜单
            IsOpen = true;
        }
    }

    public void InitView()
    {
        list = new ArrayList<>();

        otherCarlistView = (ListView) findViewById(R.id.OtherCarListview);
        String[] strings = {"img","whichcar"};
        int[] ids = {R.id.img,R.id.WhichCar};
        getData();
        simpleAdapter = new SimpleAdapter(mContext, list, R.layout.item_othercarlistview_green, strings, ids);
        otherCarlistView.setAdapter(simpleAdapter);
    }

    //用于更新车辆列表
    public void UpdateListview()
    {
        simpleAdapter.notifyDataSetChanged();
    }



    private void getData() {

    }

    public boolean getIsOpen(){
        return IsOpen;
    }
}

