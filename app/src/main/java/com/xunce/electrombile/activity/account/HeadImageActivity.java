package com.xunce.electrombile.activity.account;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.CropActivity;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

public class HeadImageActivity extends Activity {
    private ImageView iv_CameraImg;
    private Uri imageUri;
    public static final int TAKE_PHOTE=1;
    public static final int CROP_PHOTO=2;
    public static final int CHOOSE_PHOTO=3;
    private SettingManager settingManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_image);

        Button btn_StartCamera = (Button)findViewById(R.id.btn_StartCamera);
        Button btn_choose = (Button)findViewById(R.id.choose_from_album);
        iv_CameraImg = (ImageView) findViewById(R.id.iv_CameraImg);

        settingManager = SettingManager.getInstance();

        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch(IOException e){
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.intent.action.PICK");
                intent.setType("image/*");
                startActivityForResult(intent,CHOOSE_PHOTO);

            }
        });

        btn_StartCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(Environment.getExternalStorageDirectory(),"tempImage.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch(IOException e){
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,TAKE_PHOTE);
            }
        });

        //加载图片

//        Bitmap bitmap = BitmapUtils.compressImageFromFile();
//        if(bitmap!=null){
//            iv_CameraImg.setImageBitmap(bitmap);
//        }

//        getHeadImageFromServer();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case TAKE_PHOTE:
                if(resultCode == RESULT_OK) {
                    Intent intent = new Intent(this,CropActivity.class);
                    intent.setData(imageUri);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;

            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    imageUri = data.getData();
                    Intent intent = new Intent(this,CropActivity.class);
                    intent.setData(imageUri);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;

            case CROP_PHOTO:
                if(resultCode == RESULT_OK){
                    //上传到服务器



                    //本地存储并显示
                    Bitmap bitmap = BitmapUtils.compressImageFromFile(settingManager.getIMEI());
                    iv_CameraImg.setImageBitmap(bitmap);
                    Log.d("test", "test");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
        super.onBackPressed();
    }

//    public void getHeadImageFromServer(){
//        AVQuery<AVObject> query = new AVQuery<>("DID");
//        query.whereEqualTo("IMEI", settingManager.getIMEI());
//        query.findInBackground(new FindCallback<AVObject>() {
//            @Override
//            public void done(List<AVObject> list, AVException e) {
//                if (e == null) {
//                    if (!list.isEmpty()) {
//                        AVObject avObject = list.get(0);
//
//                        if (avObject.get("Image") == null) {
//                            //服务器上的头像数据为空  所以需要上传数据到数据库啊
//                            Log.d("test", "test");
//
//                            try {
//                                AVFile avfile = AVFile.withAbsoluteLocalPath("crop_result.png",
//                                        Environment.getExternalStorageDirectory() + "/crop_result.png");
//
//                                avfile.saveInBackground();
//                                avObject.put("Image", avfile);
//                                avObject.saveInBackground();
//
//                            }catch (Exception ee){
//
//                            }
//
//                            Bitmap bitmap = BitmapFactory.decodeResource(HeadImageActivity.this.getResources(),
//                                    R.drawable.person);
//                            iv_CameraImg.setImageBitmap(bitmap);
//
//                        } else {
//                            //从服务器拉头像  下来存文件
//                            AVFile avFile = avObject.getAVFile("Image");
//                            avFile.getDataInBackground(new GetDataCallback(){
//                                public void done(byte[] data, AVException e){
//                                    //process data or exception.
//                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                                    iv_CameraImg.setImageBitmap(bitmap);
//                                }
//                            });
//                        }
//                    } else {
//                        //不可能出现这种情况啊
//                    }
//                } else {
//                    e.printStackTrace();
//
//                }
//            }
//        });
//    }
}
