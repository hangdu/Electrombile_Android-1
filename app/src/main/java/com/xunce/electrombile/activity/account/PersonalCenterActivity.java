package com.xunce.electrombile.activity.account;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.xunce.electrombile.LeancloudManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.manager.SettingManager;

import java.util.GregorianCalendar;

public class PersonalCenterActivity extends BaseActivity{
    private TextView tv_UserName;
    private TextView tv_Gender;
    private TextView tv_BirthDate;
    private SettingManager settingManager;
    private LeancloudManager leancloudManager;
    private Boolean gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_personal_center);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("个人中心");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalCenterActivity.this.finish();
            }
        });

        tv_UserName = (TextView)findViewById(R.id.tv_UserName);
        tv_Gender = (TextView)findViewById(R.id.tv_Gender);
        tv_BirthDate = (TextView)findViewById(R.id.tv_BirthDate);

        RelativeLayout layout_nickname = (RelativeLayout)findViewById(R.id.layout_UserName);
        RelativeLayout layout_gender = (RelativeLayout)findViewById(R.id.layout_Gender);
        RelativeLayout layout_BirthDate = (RelativeLayout)findViewById(R.id.layout_BirthDate);
        LinearLayout layout_ChangePassword = (LinearLayout)findViewById(R.id.layout_ChangePassword);
        btn_back = (RelativeLayout)findViewById(R.id.btn_back);

        layout_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNickName();
            }
        });

        layout_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGender();
            }
        });

        layout_BirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(PersonalCenterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tv_BirthDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        GregorianCalendar gregorianCalendar = new GregorianCalendar (year,monthOfYear,dayOfMonth);
                        leancloudManager.uploadUserInfo(gregorianCalendar.getTime());
                    }
                }, 2013, 7, 20);
                datePicker.show();
            }
        });

        layout_ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePass();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }

    private void changeNickName(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_changenickname, null);
        final Dialog dialog = new Dialog(PersonalCenterActivity.this, R.style.Translucent_NoTitle_white);

        Button btn_suretochangeName = (Button)view.findViewById(R.id.btn_sure);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText et_nickname = (EditText)view.findViewById(R.id.et_nickname);

        btn_suretochangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = et_nickname.getText().toString();
                if(nickname.equals("")){
                    dialog.dismiss();
                }
                else{
                    tv_UserName.setText(nickname);
                    dialog.dismiss();
                    //上传服务器
                    leancloudManager.uploadUserInfo(nickname);
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager m = PersonalCenterActivity.this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65

        //设置布局  有个问题啊  没有做适配
        dialog.addContentView(view, new LinearLayout.LayoutParams(
                dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void changeGender(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_changegender, null);
        final Dialog dialog = new Dialog(PersonalCenterActivity.this, R.style.Translucent_NoTitle_white);

        Button btn_suretochangeGender = (Button)view.findViewById(R.id.btn_sure);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        RadioGroup radioGroup_chooseGender = (RadioGroup)view.findViewById(R.id.radioGroup_chooseGender);
        RadioButton rb_male = (RadioButton)view.findViewById(R.id.rb_male);
        RadioButton rb_female = (RadioButton)view.findViewById(R.id.rb_female);
        //初始化
        if(settingManager.getGender()){
            rb_male.setChecked(true);
        }
        else{
            rb_female.setChecked(true);
        }

        radioGroup_chooseGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_male) {
                    //some code
                    gender = true;
                } else if (checkedId == R.id.rb_female) {
                    //some code
                    gender = false;
                }
            }
        });

        btn_suretochangeGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用户什么也没有点击
                if(gender == null){
                    dialog.dismiss();
                }
                else if(gender){
                    tv_Gender.setText("男");
                    dialog.dismiss();
                    //上传服务器
                    leancloudManager.uploadUserInfo(gender);
                }
                else{
                    tv_Gender.setText("女");
                    dialog.dismiss();
                    //上传服务器
                    leancloudManager.uploadUserInfo(gender);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager m = PersonalCenterActivity.this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65

        //设置布局  有个问题啊  没有做适配
        dialog.addContentView(view, new LinearLayout.LayoutParams(
                dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void changePass() {
        //解析xml
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.changepassword, null);
        final Dialog dialog = new Dialog(PersonalCenterActivity.this, R.style.Translucent_NoTitle_white);

        //找按钮
        Button confirm = (Button) view.findViewById(R.id.btn_sure);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);

        final EditText oldPass = (EditText)view.findViewById(R.id.et_oldpass);
        final EditText newPass = (EditText)view.findViewById(R.id.et_newpass);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old_password = oldPass.getText().toString().trim();
                String new_password = newPass.getText().toString().trim();
                if(!new_password.isEmpty())
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
                                dialog.dismiss();
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
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager m = PersonalCenterActivity.this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65

        //设置布局
        dialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    @Override
    public void initEvents() {
        settingManager = SettingManager.getInstance();
        //获取setting中的昵称 出生年代 性别
        tv_UserName.setText(settingManager.getNickname());
        if(settingManager.getGender()){
            tv_Gender.setText("男");
        }
        else{
            tv_Gender.setText("女");
        }
        tv_BirthDate.setText(settingManager.getBirthdate());

        leancloudManager = LeancloudManager.getInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

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
}
