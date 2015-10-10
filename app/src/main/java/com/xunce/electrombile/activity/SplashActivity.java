package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import im.fir.sdk.FIR;


public class SplashActivity extends BaseActivity {

    private final int UPDATE = 0;
    private final int UN_UPDATE = 1;
    private boolean isUpdate;
    private File file;
    private ProgressDialog progressDialog;
    Handler MyHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    Bundle bundle = msg.getData();
                    isUpdate = bundle.getBoolean("isupdate");
                    if (isUpdate) {
                        upData();
                        isUpdate = false;
                    } else {
                        MyHandler.sendEmptyMessageDelayed(UN_UPDATE, 2000);
                    }
                    break;
                case UN_UPDATE:
                    AVUser currentUser = AVUser.getCurrentUser();
                    if (currentUser != null) {
                        FIR.addCustomizeValue("user", currentUser.getUsername());
                        Intent intent = new Intent(SplashActivity.this, FragmentActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Context context = this;
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(context);
        }else {
            checkVersion();
        }
    }

    public void checkVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                String baseUrl = "http://fir.im/api/v2/app/version/%s?token=%s";
                //下面是正式的 版本调整
                // String checkUpdateUrl = String.format(baseUrl, "553ca95096a9fc5c14001802", "39d16f30ebf111e4a2da4efe6522248a4b9d9ed4");
                String checkUpdateUrl = String.format(baseUrl, "556c810d2bb8ac0e5d001a30", "b9d54ba0b12411e4bc2c492c76a46d264a53ba2f");
                try {
                    URL url = new URL(checkUpdateUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");// 设置请求的方式
                    urlConnection.setReadTimeout(5000);// 设置超时的时间
                    urlConnection.setConnectTimeout(5000);// 设置链接超时的时间
                    if (urlConnection.getResponseCode() == urlConnection.HTTP_OK) {
                        // 获取响应的输入流对象
                        InputStream is = urlConnection.getInputStream();
                        // 创建字节输出流对象
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        // 定义读取的长度
                        int len = 0;
                        // 定义缓冲区
                        byte buffer[] = new byte[1024];
                        // 按照缓冲区的大小，循环读取
                        while ((len = is.read(buffer)) != -1) {
                            // 根据读取的长度写入到os对象中
                            os.write(buffer, 0, len);
                        }
                        // 释放资源
                        is.close();
                        os.close();
                        // 返回字符串
                        String firResponse = new String(os.toByteArray());
                        JSONObject versionJsonObj = new JSONObject(firResponse);
                        //FIR上当前的versionCode
                        int firVersionCode = Integer.parseInt(versionJsonObj.getString("version"));
                        //FIR上当前的versionName
                        String firVersionName = versionJsonObj.getString("versionShort");
                        PackageManager pm = getApplicationContext().getPackageManager();
                        PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(),
                                PackageManager.GET_ACTIVITIES);
                        if (pi != null) {
                            int currentVersionCode = pi.versionCode;
                            String currentVersionName = pi.versionName;
//                            Log.i("当前版本",currentVersionCode+"");
//                            Log.i("查看版本",firVersionCode+"");
                            if (firVersionCode > currentVersionCode) {
                                //需要更新
                                bundle.putBoolean("isupdate", true);
                            } else if (firVersionCode == currentVersionCode) {
                                //如果本地app的versionCode与FIR上的app的versionCode一致，则需要判断versionName.
                                if (!currentVersionName.equals(firVersionName)) {
                                    bundle.putBoolean("isupdate", true);
                                }
                            } else {
                                //不需要更新,当前版本高于FIR上的app版本.
                                bundle.putBoolean("isupdate", false);
                            }
                            msg.what = UPDATE;
                            msg.setData(bundle);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = UN_UPDATE;
                }
                MyHandler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 检查更新
     */
    public void upData() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("软件升级")
                .setMessage("发现新版本,建议立即更新使用.")
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new updateRunnable()).start();//这个是下载的重点，是下载的过程
                        createProgressDialog();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MyHandler.sendEmptyMessage(UN_UPDATE);
                    }
                });
        alert.create().show();
    }

    private void createProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setTitle("下载中...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    class updateRunnable implements Runnable {
        int downnum = 0;//已下载的大小
        int downcount = 0;//下载百分比

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                //test  DownLoadApp("http://fir.im/api/v2/app/install/554331e6bf7f222c2600493b?token=39d16f30ebf111e4a2da4efe6522248a4b9d9ed4");
                //正式链接
                DownLoadApp("http://fir.im/api/v2/app/install/556c810d2bb8ac0e5d001a30?token=b9d54ba0b12411e4bc2c492c76a46d264a53ba2f");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void DownLoadApp(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int length = urlConnection.getContentLength();
            InputStream inputStream = urlConnection.getInputStream();
            OutputStream outputStream = new FileOutputStream(getFile());

            Uri uri = Uri.fromFile(file);
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            byte buffer[] = new byte[1024 * 3];
            int readsize = 0;
            while ((readsize = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readsize);
                downnum += readsize;
                if ((downcount == 0) || (downnum * 100 / length) - 1 > downcount) {
                    downcount += 1;
                    progressDialog.setProgress(downnum * 100 / length);
                }
                if (downnum == length) {
                    progressDialog.dismiss();
                    Intent intent1 = new Intent();
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.setAction(android.content.Intent.ACTION_VIEW);
                    intent1.setDataAndType(Uri.fromFile(file),
                            "application/vnd.android.package-archive");
                    startActivity(intent1);
                    SplashActivity.this.finish();
                }
            }
            inputStream.close();
            outputStream.close();
        }

        //获取文件的保存路径
        public File getFile() throws Exception {
            String SavePath = getSDCardPath() + "/App";
            File path = new File(SavePath);
            file = new File(SavePath + "/anquanbao.apk");
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        }

        //获取SDCard的目录路径功能
        private String getSDCardPath() {
            File sdcardDir = null;
            // 判断SDCard是否存在
            boolean sdcardExist = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
            if (sdcardExist) {
                sdcardDir = Environment.getExternalStorageDirectory();
            }
            return sdcardDir.toString();
        }
    }

}
