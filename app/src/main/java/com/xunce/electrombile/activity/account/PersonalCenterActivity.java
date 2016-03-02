package com.xunce.electrombile.activity.account;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BaseActivity;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

public class PersonalCenterActivity extends BaseActivity{
    private TextView tv_UserName;
    private TextView tv_Gender;
    private TextView tv_BirthDate;
    private SettingManager settingManager;

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
        Button btn_back = (Button)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalCenterActivity.this.finish();
            }
        });

        tv_UserName = (TextView)findViewById(R.id.tv_UserName);
        tv_Gender = (TextView)findViewById(R.id.tv_Gender);
        tv_BirthDate = (TextView)findViewById(R.id.tv_BirthDate);

        RelativeLayout layout_gender = (RelativeLayout)findViewById(R.id.layout_Gender);
        RelativeLayout layout_BirthDate = (RelativeLayout)findViewById(R.id.layout_BirthDate);
        LinearLayout layout_ChangePassword = (LinearLayout)findViewById(R.id.layout_ChangePassword);
        btn_back = (Button)findViewById(R.id.btn_back);

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
//                                settingManager.setGender(GenderStr);
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
                    
                }

            }
        });


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

        //设置布局
        dialog.addContentView(view, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.show();
        //设置宽度
//        WindowManager m = this.getWindowManager();
//        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); // 获取对话框当前的参数值
////        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
//        //虽然过时,为了兼容性,还是用老方法. API 13以上才能使用新方法
//        p.width = (int) (d.getWidth() * 0.794); // 宽度设置为屏幕的0.65
////        p.height = (int) (d.getHeight()*0.372);
//        dialog.getWindow().setAttributes(p);
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

    @Override
    public void onBackPressed() {
        settingManager.setNickname(tv_UserName.getText().toString().trim());
        super.onBackPressed();

    }
}
