package com.xunce.electrombile.utils.useful;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.App;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by lybvinci on 16/5/17.
 */
public class JPushUtils {
    private static final String TAG = "JPushUtils";
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;
    private Context mContext;

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (JPushUtils.isConnected(mContext)) {
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }

            JPushUtils.showToast(logs, mContext);
        }

    };


    final Handler handler = new Handler( ) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set tags in handler.");
                    JPushInterface.setAliasAndTags(mContext, (String) msg.obj, null, mAliasCallback);
                    break;
            }
        }
    };


    private final static JPushUtils INSTANCE = new JPushUtils();
    public static JPushUtils getInstance() {
        return INSTANCE;
    }

    private JPushUtils() {
        mContext = App.getInstance();
    }

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    public static void showToast(final String toast, final Context context)
    {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    public void setJPushAlias(String alias){
        if (TextUtils.isEmpty(alias)) {
            Toast.makeText(mContext, "alias为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!JPushUtils.isValidTagAndAlias(alias)) {
            Toast.makeText(mContext,"alias格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        handler.sendMessage(handler.obtainMessage(MSG_SET_ALIAS, alias));
        //调用JPush API设置Alias
    }
}

