package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.AboutActivity;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.activity.HelpActivity;
import com.xunce.electrombile.activity.TestActivity;
import com.xunce.electrombile.utils.device.SDCardUtils;
import com.xunce.electrombile.utils.system.BitmapUtils;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.io.File;
import java.io.IOException;


public class PersonalCenterActivity extends BaseActivity{
    private TextView tv_UserName;
    private TextView tv_Gender;
    private TextView tv_BirthDate;

    LinearLayout layout_UserName;
    LinearLayout layout_gender;
    LinearLayout layout_BirthDate;
    View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_personal_center);
        super.onCreate(savedInstanceState);
    }



    @Override
    public void initViews() {
        tv_UserName = (TextView)findViewById(R.id.tv_UserName);
        tv_Gender = (TextView)findViewById(R.id.tv_Gender);
        tv_BirthDate = (TextView)findViewById(R.id.tv_BirthDate);

        layout_UserName = (LinearLayout)findViewById(R.id.layout_UserName);
        layout_gender = (LinearLayout)findViewById(R.id.layout_Gender);
        layout_BirthDate = (LinearLayout)findViewById(R.id.layout_BirthDate);


        layout_UserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        layout_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        layout_BirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(PersonalCenterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tv_BirthDate.setText(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
//                        Toast.makeText(PersonalCenterActivity.this, year+"year "+(monthOfYear+1)+"month "+dayOfMonth+"day", Toast.LENGTH_SHORT).show();
                    }
                }, 2013, 7, 20);
                datePicker.show();
            }
        });
    }


    @Override
    public void initEvents() {
//        loadAndSetImg(faceImage, "/faceImage.png");
//        int ImageState = setManager.getPersonCenterImage();
//
//        //设置图片
//        for (int i = 1; i <= ImageState; i++) {
//            iv[i].setVisibility(View.VISIBLE);
//            loadAndSetImg(iv[i], path[i]);
//            if (i == ImageState && ImageState != 4) {
//                iv[i + 1].setVisibility(View.VISIBLE);
//            }
//        }
//
//        et_sim_number.setText(setManager.getPersonCenterSimNumber());
//        et_car_number.setText(setManager.getPersonCenterCarNumber());
    }

    @Override
    protected void onStop() {
        super.onStop();

        //保存输入的数据
//        setManager.setPersonCenterCarNumber(et_car_number.getText().toString().trim());
//        setManager.setPersonCenterSimNumber(et_sim_number.getText().toString().trim());

    }

//    private void loadAndSetImg(ImageView imageView, String nameImg) {
//        com.lidroid.xutils.BitmapUtils bitmapUtils = new com.lidroid.xutils.BitmapUtils(this);
//        bitmapUtils.configDefaultLoadFailedImage(R.drawable.iv_person_head);//加载失败图片
//        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);//设置图片压缩类型
//        bitmapUtils.configDefaultCacheExpiry(0);
//        bitmapUtils.configDefaultAutoRotation(true);
//        bitmapUtils.display(imageView, imgFilePath + nameImg);
//    }

//    public void changeHeadPortrait(View view) {
//        photoNumber = 0;
//        showDialog();
//    }






}
