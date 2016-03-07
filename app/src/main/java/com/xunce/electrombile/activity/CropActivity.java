package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.xunce.electrombile.LeancloudManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.view.ClipView;

public class CropActivity extends Activity implements View.OnTouchListener,View.OnClickListener{
    String filePath;

    private ImageView srcPic;
    private View sure;
    private ClipView clipView;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    /** 动作标志：无 */
    private static final int NONE = 0;
    /** 动作标志：拖动 */
    private static final int DRAG = 1;
    /** 动作标志：缩放 */
    private static final int ZOOM = 2;
    /** 初始化动作标志 */
    private int mode = NONE;

    /** 记录起始坐标 */
    private PointF start = new PointF();
    /** 记录缩放时两指中间点坐标 */
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Bitmap bitmap;
    private Uri imageUri;
    private SettingManager settingManager;
    private LeancloudManager leancloudManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        //解析intent数据 还原uri
        Intent intent =  getIntent();
        imageUri = intent.getData();



        srcPic = (ImageView)findViewById(R.id.src_pic);
        srcPic.setOnTouchListener(this);

        ViewTreeObserver observer = srcPic.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                srcPic.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initClipView(srcPic.getTop());
            }
        });

        sure = (View)findViewById(R.id.sure);
        sure.setOnClickListener(this);

        settingManager = SettingManager.getInstance();
        leancloudManager = LeancloudManager.getInstance();
    }

    private void initClipView(int top){
        //bitmap从哪里来? 下面这句要改
//        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pic);

        try{
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }catch (Exception e){
            return;
        }


        clipView = new ClipView(this);
        clipView.setCustomTopBarHeight(top);
        clipView.addOnDrawCompleteListener(new ClipView.OnDrawListenerComplete() {
            @Override
            public void onDrawCompelete() {
                clipView.removeOnDrawCompleteListener();
                int clipHeight = clipView.getClipHeight();
                int clipWidth = clipView.getClipWidth();
                int midX = clipView.getClipLeftMargin() + (clipWidth / 2);
                int midY = clipView.getClipTopMargin() + (clipHeight / 2);

                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                // 按裁剪框求缩放比例
                float scale = (clipWidth * 1.0f) / imageWidth;
                if (imageWidth > imageHeight) {
                    scale = (clipHeight * 1.0f) / imageHeight;
                }

                // 起始中心点
                float imageMidX = imageWidth * scale / 2;
                float imageMidY = clipView.getCustomTopBarHeight()
                        + imageHeight * scale / 2;
                srcPic.setScaleType(ImageView.ScaleType.MATRIX);

                // 缩放
                matrix.postScale(scale, scale);
                // 平移
                matrix.postTranslate(midX - imageMidX, midY - imageMidY);

                srcPic.setImageMatrix(matrix);
                srcPic.setImageBitmap(bitmap);
            }
        });
        this.addContentView(clipView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                // 设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void onClick(View v) {
        Bitmap clipBitmap = getBitmap();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        clipBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] bitmapByte = baos.toByteArray();
        saveMyBitmaptoFile(clipBitmap);
        Intent intent = new Intent();
        intent.putExtra("filePath",filePath);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {
        // 获取截屏
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        Bitmap finalBitmap = Bitmap.createBitmap(view.getDrawingCache(),
                clipView.getClipLeftMargin(), clipView.getClipTopMargin()
                        + statusBarHeight, clipView.getClipWidth(),
                clipView.getClipHeight());

        // 释放资源
        view.destroyDrawingCache();
        return finalBitmap;
    }

    //bitmap写文件
    public void saveMyBitmaptoFile(Bitmap mBitmap){
        //如果用户没有内存卡这句话会不会出错
//        File f = new File(Environment.getExternalStorageDirectory(),"crop_result.png");
//        filePath = f.getAbsolutePath();

        filePath = Environment.getExternalStorageDirectory() + "/"+settingManager.getIMEI()+"crop_result.png";
        File f = new File(filePath);

        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        leancloudManager.uploadImageToServer(settingManager.getIMEI(),mBitmap);
    }
}
