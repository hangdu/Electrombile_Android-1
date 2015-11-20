package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.utils.device.SDCardUtils;
import com.xunce.electrombile.utils.system.BitmapUtils;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.io.File;
import java.io.IOException;

public class PersonalCenterActivity extends BaseActivity {

    /*头像名称*/
    //private static final String IMAGE_FILE_NAME = "faceImage.png";
    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;
    private String[] items = new String[]{"选择本地图片", "拍照"};
    private String imgFilePath = Environment.getExternalStorageDirectory().toString()
            + "/safeGuard";
    private ImageView faceImage;
    private ImageView carView1;
    private ImageView carView2;
    private ImageView carView3;
    private ImageView carView4;
    private int photoNumber = 0;
    private ImageView[] iv;
    private String[] path = {"/faceImage.png", "/car1Image.png", "/car2Image.png", "/car3Image.png", "/car4Image.png"};

    //设置设备号
    private TextView tv_imei;
    //设置车牌号
    private EditText et_car_number;
    //设置sim卡号
    private EditText et_sim_number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_personal_center);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        faceImage = (ImageView) findViewById(R.id.head_portrait);
        carView1 = (ImageView) findViewById(R.id.car1View);
        carView2 = (ImageView) findViewById(R.id.car2View);
        carView3 = (ImageView) findViewById(R.id.car3View);
        carView4 = (ImageView) findViewById(R.id.car4View);
        iv = new ImageView[]{faceImage, carView1, carView2, carView3, carView4};

        tv_imei = (TextView) findViewById(R.id.tv_imei);
        et_car_number = (EditText) findViewById(R.id.et_car_number);
        et_sim_number = (EditText) findViewById(R.id.et_sim_number);
    }

    @Override
    public void initEvents() {
        loadAndSetImg(faceImage, "/faceImage.png");
        int ImageState = setManager.getPersonCenterImage();

        //设置图片
        for (int i = 1; i <= ImageState; i++) {
            iv[i].setVisibility(View.VISIBLE);
            loadAndSetImg(iv[i], path[i]);
            if (i == ImageState && ImageState != 4) {
                iv[i + 1].setVisibility(View.VISIBLE);
            }
        }

        //设置文字
        tv_imei.setText("设备卡号:" + setManager.getIMEI());
        et_sim_number.setText(setManager.getPersonCenterSimNumber());
        et_car_number.setText(setManager.getPersonCenterCarNumber());
    }

    @Override
    protected void onStop() {
        super.onStop();

        //保存输入的数据
        setManager.setPersonCenterCarNumber(et_car_number.getText().toString().trim());
        setManager.setPersonCenterSimNumber(et_sim_number.getText().toString().trim());

    }

    private void loadAndSetImg(ImageView imageView, String nameImg) {
        com.lidroid.xutils.BitmapUtils bitmapUtils = new com.lidroid.xutils.BitmapUtils(this);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.iv_person_head);//加载失败图片
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);//设置图片压缩类型
        bitmapUtils.configDefaultCacheExpiry(0);
        bitmapUtils.configDefaultAutoRotation(true);
        bitmapUtils.display(imageView, imgFilePath + nameImg);
    }

    public void changeHeadPortrait(View view) {
        photoNumber = 0;
        showDialog();
    }

    /**
     * 显示选择对话框
     */
    private void showDialog() {

        new AlertDialog.Builder(this)
                .setTitle("设置头像")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*"); // 设置文件类型
                                intentFromGallery
                                        .setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery,
                                        IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (SDCardUtils.hasSdcard()) {

                                    intentFromCapture.putExtra(
                                            MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(new File(imgFilePath)));
                                }

                                startActivityForResult(intentFromCapture,
                                        CAMERA_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //结果码不等于取消的时候
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (SDCardUtils.hasSdcard()) {
                        File tempFile = new File(imgFilePath + "/tempImg.png");
                        startPhotoZoom(Uri.fromFile(tempFile));
                    } else {
                        ToastUtils.showShort(this, "未找到存储卡，无法存储照片！");
                    }
                    break;
                case RESULT_REQUEST_CODE:
                    if (data != null) {
                        getImageToView(iv[photoNumber], data, path[photoNumber]);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 338);
        intent.putExtra("outputY", 338);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param
     */
    private void getImageToView(ImageView imageView, Intent data, String path) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            imageView.setImageDrawable(drawable);
            saveFaceImg(photo, path);
        }
    }

    private void saveFaceImg(Bitmap photo, String path) {
        try {
            BitmapUtils.saveBitmapToFile(photo, imgFilePath + path);
            ToastUtils.showShort(this, "设置成功！");
            setNextImageDisplay();
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showShort(this, "保存失败，请重试。");
        }
    }

    private void setNextImageDisplay() {
        if (photoNumber < 4)
            iv[photoNumber + 1].setVisibility(View.VISIBLE);
        setManager.setPersonCenterImage(photoNumber);
    }

    public void addDevice1Photo(View view) {
        photoNumber = 1;
        showDialog();
    }

    public void addDevice2Photo(View view) {
        photoNumber = 2;
        showDialog();
    }

    public void addDevice3Photo(View view) {
        photoNumber = 3;
        showDialog();
    }

    public void addDevice4Photo(View view) {
        photoNumber = 4;
        showDialog();
    }

}
