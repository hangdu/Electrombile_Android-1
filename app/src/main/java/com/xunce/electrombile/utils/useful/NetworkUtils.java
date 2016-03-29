package com.xunce.electrombile.utils.useful;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.xunce.electrombile.R;
import com.xunce.electrombile.utils.system.ToastUtils;

/**
 * 
 * <P>
 * 网络连接判断
 * <P>
 * 
 * @author StephenC
 * @version 1.00
 */
public class NetworkUtils {

	/**
	 * 移动网络是否可用
	 * 
	 * @param context
	 *            上下文
	 * @return   移动网络是否可用
	 * 
	 * */
	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * WIFI网络是否可用
	 * 
	 * @param context
	 *            上下文
	 * @return   WIFI网络是否可用
	 * 
	 * */
	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 当前网络是否可用
	 * 
	 * @param context
	 *            上下文
	 * @return   当前网络是否可用
	 * 
	 * */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}



    public static boolean checkNetwork(Context context) {
        if (!NetworkUtils.isNetworkConnected(context)) {
            ToastUtils.showShort(context,"无网络连接");
            return true;
        }
        return false;
    }
	
 	/**
 	 * 获取当前WIFI的SSID.
 	 *
 	 * @param context 上下文
 	 * @return ssid
 	 * 
 	 * *
 	 */
	 public static String getCurentWifiSSID(Context context){
		 String ssid = "";
		 if(context!=null){
			 WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			 WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			 ssid = wifiInfo.getSSID();
			 if (ssid.substring(0, 1).equals("\"")
						&& ssid.substring(ssid.length() - 1).equals("\"")) {
					ssid = ssid.substring(1, ssid.length() - 1);
				}
		 }
		 return ssid;
	 }

    //提醒设置网络,并且设置是否可以点击旁边取消
    public static AlertDialog networkDialog(final Context context,boolean cancelAble) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setMessage(R.string.networkErrorSet)
                .setCancelable(cancelAble)
                .setTitle(R.string.networkSet)
                .setPositiveButton(R.string.networkSettings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = null;
                        if (Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(componentName);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
        return dialog;
    }
    //提醒设置网络并且没有取消按钮
    public static AlertDialog.Builder networkDialogNoCancel(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.networkErrorSet)
                .setCancelable(false)
                .setTitle(R.string.networkSet)
                .setPositiveButton(R.string.networkSettings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = null;
                        if (Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(componentName);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);
                    }
                }).show();
        return builder;
    }

    //网络设置同时需要跳转页面的
    public static void networkDialog(final Context context, final Activity activity1, final Class activity2) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.networkErrorSet)
                .setCancelable(false)
                .setTitle(R.string.networkSet)
                .setPositiveButton(R.string.networkSettings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = null;
                        if (Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName componentName = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
                            intent.setComponent(componentName);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(context, activity2);
                        context.startActivity(intent);
                        activity1.finish();
                    }
                }).show();
    }

}
