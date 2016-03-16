package com.xunce.electrombile.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lybvinci on 16/3/2.
 */
public class ClipView extends View {
    private Paint paint = new Paint();
    private Paint borderPaint = new Paint();
    private int customTopBarHeight = 0;
    private double clipRatio = 0.936;
    private int clipWidth = -1;
    private int clipHeight = -1;
    private int clipLeftMargin = 0;
    private int clipTopMargin = 0;
    private int clipBorderWidth = 3;
    private boolean isSetMargin = false;
    private OnDrawListenerComplete listenerComplete;

    public ClipView(Context context) {
        this(context, null);
    }

    public ClipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //设置透明度的
        paint.setAlpha(100);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(clipBorderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();

        if(clipWidth == -1||clipHeight == -1){
            clipWidth = width-50;
            clipHeight = (int)(clipWidth*clipRatio);

        }

        if(!isSetMargin){
            clipLeftMargin = (width-clipWidth)/2;
            clipTopMargin = (height-clipHeight)/2;
        }

        //画阴影

        canvas.drawRect(0,customTopBarHeight,width,clipTopMargin,paint);
        canvas.drawRect(0, clipTopMargin, clipLeftMargin, clipTopMargin
                + clipHeight, paint);
        // right
        canvas.drawRect(clipLeftMargin + clipWidth, clipTopMargin, width,
                clipTopMargin + clipHeight, paint);
        // bottom
        canvas.drawRect(0, clipTopMargin + clipHeight, width, height, paint);

        // 画边框
        canvas.drawRect(clipLeftMargin, clipTopMargin, clipLeftMargin
                + clipWidth, clipTopMargin + clipHeight, borderPaint);

//        canvas.drawRect(0,0,width,height,paint);
//
//        float r = width / 2f;
//        canvas.drawCircle(width/2,height/2, r, borderPaint);

        if (listenerComplete != null) {
            listenerComplete.onDrawCompelete();
        }
    }

    public int getCustomTopBarHeight() {
        return customTopBarHeight;
    }

    public void setCustomTopBarHeight(int customTopBarHeight) {
        this.customTopBarHeight = customTopBarHeight;
    }

    public double getClipRatio() {
        return clipRatio;
    }

    public void setClipRatio(double clipRatio) {
        this.clipRatio = clipRatio;
    }

    public int getClipWidth() {
        // 减clipBorderWidth原因：截图时去除边框白线
        return clipWidth - clipBorderWidth;
    }

    public void setClipWidth(int clipWidth) {
        this.clipWidth = clipWidth;
    }

    public int getClipHeight() {
        return clipHeight - clipBorderWidth;
    }

    public void setClipHeight(int clipHeight) {
        this.clipHeight = clipHeight;
    }

    public int getClipLeftMargin() {
        return clipLeftMargin + clipBorderWidth;
    }

    public void setClipLeftMargin(int clipLeftMargin) {
        this.clipLeftMargin = clipLeftMargin;
        isSetMargin = true;
    }

    public int getClipTopMargin() {
        return clipTopMargin + clipBorderWidth;
    }

    public void setClipTopMargin(int clipTopMargin) {
        this.clipTopMargin = clipTopMargin;
        isSetMargin = true;
    }

    public void addOnDrawCompleteListener(OnDrawListenerComplete listener) {
        this.listenerComplete = listener;
    }

    public void removeOnDrawCompleteListener() {
        this.listenerComplete = null;
    }

    /**
     * 裁剪区域画完时调用接口
     *
     * @author Cow
     */
    public interface OnDrawListenerComplete {
        public void onDrawCompelete();
    }
}


