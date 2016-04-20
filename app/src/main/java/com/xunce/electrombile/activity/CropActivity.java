package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.xunce.electrombile.LeancloudManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;

public class CropActivity extends Activity{
    private String filePath;
    private ImageView srcPic;
    private SettingManager settingManager;
    private LeancloudManager leancloudManager;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        //解析intent数据 还原uri
        Intent intent = getIntent();
        Uri imageUri = intent.getData();
        final String IMEI = intent.getStringExtra("IMEI");

        srcPic = (ImageView) findViewById(R.id.src_pic);

        try{
            bitmap = getThumbnail(imageUri);
            srcPic.setImageBitmap(bitmap);

        }catch(Exception e){
            e.printStackTrace();
        }

        View sure = findViewById(R.id.sure);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = ((BitmapDrawable)srcPic.getDrawable()).getBitmap();
                saveMyBitmaptoFile(bitmap,IMEI);
                Intent intent = new Intent();
                intent.putExtra("filePath", filePath);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        settingManager = SettingManager.getInstance();
        leancloudManager = LeancloudManager.getInstance();
    }

    public void bitmapRelease(){
        if(bitmap != null && !bitmap.isRecycled()){
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 300) ? (originalSize / 300) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    //bitmap写文件
    public void saveMyBitmaptoFile(Bitmap mBitmap,String IMEI){
        //如果用户没有内存卡这句话会不会出错
        filePath = Environment.getExternalStorageDirectory() + "/"+IMEI+"crop_result.jpg";
        File f = new File(filePath);

        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        leancloudManager.uploadImageToServer(IMEI, mBitmap);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        srcPic.setImageURI(null);
        bitmapRelease();
    }
}
