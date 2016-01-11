package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.manager.SettingManager;

public class PersonalCenterActivity extends BaseActivity{
    private EditText tv_UserName;
    private TextView tv_Gender;
    private TextView tv_BirthDate;

    LinearLayout layout_gender;
    LinearLayout layout_BirthDate;
    LinearLayout layout_ChangePassword;

    //修改密码的dialog
    AlertDialog dialog;
    LinearLayout passDialog;

    EditText oldPass;
    EditText newPass;

    private SettingManager settingManager;

    Button btn_back;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_personal_center);
        super.onCreate(savedInstanceState);
    }



    @Override
    public void initViews() {
        tv_UserName = (EditText)findViewById(R.id.tv_UserName);
        tv_Gender = (TextView)findViewById(R.id.tv_Gender);
        tv_BirthDate = (TextView)findViewById(R.id.tv_BirthDate);
        layout_gender = (LinearLayout)findViewById(R.id.layout_Gender);
        layout_BirthDate = (LinearLayout)findViewById(R.id.layout_BirthDate);
        layout_ChangePassword = (LinearLayout)findViewById(R.id.layout_ChangePassword);
        btn_back = (Button)findViewById(R.id.btn_back);

        LayoutInflater inflater = (LayoutInflater)PersonalCenterActivity.this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        passDialog= (LinearLayout) inflater.inflate(R.layout.changepassword, null);
        oldPass = (EditText)passDialog.findViewById(R.id.et_oldpass);
        newPass = (EditText)passDialog.findViewById(R.id.et_newpass);

        //修改密码的dialog
        createDialog();



        layout_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonalCenterActivity.this);
                builder.setTitle("选择性别");
                final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();

                builder.setSingleChoiceItems(R.array.gender, 0, choiceListener);

                DialogInterface.OnClickListener btnListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                int choiceWhich = choiceListener.getWhich();
                                String GenderStr = getResources().getStringArray(R.array.gender)[choiceWhich];
                                tv_Gender.setText(GenderStr);
                                settingManager.setGender(GenderStr);
                            }
                        };
                builder.setPositiveButton("确定", btnListener);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        layout_BirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(PersonalCenterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tv_BirthDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        settingManager.setBirthDate(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }
                }, 2013, 7, 20);
                datePicker.show();
            }
        });


        layout_ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }



    @Override
    public void initEvents() {
        settingManager = new SettingManager(PersonalCenterActivity.this);

        //获取setting中的昵称 出生年代 性别
        tv_UserName.setText(settingManager.getNickname());
        tv_Gender.setText(settingManager.getGender());
        tv_BirthDate.setText(settingManager.getBirthdate());

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

    private class ChoiceOnClickListener implements DialogInterface.OnClickListener {
        private int which = 0;
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            this.which = which;
        }

        public int getWhich() {
            return which;
        }
    }


    @Override
    public void onBackPressed() {
        settingManager.setNickname(tv_UserName.getText().toString().trim());
        super.onBackPressed();

    }

    void createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PersonalCenterActivity.this);
        builder.setTitle("修改密码");
        builder.setView(passDialog);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //判断旧密码是不是对的
                String name = settingManager.getPhoneNumber();
                String old_password = oldPass.getText().toString().trim();
                String new_password = newPass.getText().toString().trim();
                if(new_password.isEmpty() == false)
                {
                    AVUser currentUser = AVUser.getCurrentUser();
                    currentUser.updatePasswordInBackground(old_password, new_password, new UpdatePasswordCallback() {
                        @Override
                        public void done(AVException e) {
                            Log.d("TAG", "something wrong");
                            if (e == null) {
                                //修改密码成功
                                Toast.makeText(PersonalCenterActivity.this, "修改密码成功", Toast.LENGTH_SHORT).show();
                                oldPass.setText("");
                                newPass.setText("");
                            } else {
                                //原密码错误
                                Toast.makeText(PersonalCenterActivity.this, "原密码错误,修改密码失败", Toast.LENGTH_SHORT).show();
                                oldPass.setText("");
                                newPass.setText("");
                            }
                        }
                    });
                }
                //新密码为空
                else{
                    Toast.makeText(PersonalCenterActivity.this, "新密码为空,修改密码失败", Toast.LENGTH_SHORT).show();
                    oldPass.setText("");
                    newPass.setText("");
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        dialog=builder.create();
    }

}
