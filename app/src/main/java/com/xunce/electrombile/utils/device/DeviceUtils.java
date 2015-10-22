package com.xunce.electrombile.utils.device;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.FragmentActivity;

import java.util.Random;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by lybvinci on 2015/5/13.
 */
public class DeviceUtils {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean showNotifation(Context context, String topic, String msg) {
        try {
            Uri alarmSound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            long[] pattern = { 500, 500, 500 };
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(topic).setContentText(msg)
                    .setSound(alarmSound).setVibrate(pattern).setAutoCancel(true);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, FragmentActivity.class);
            if (!topic.isEmpty())
                resultIntent.putExtra(YunBaManager.MQTT_TOPIC, topic);
            if (!msg.isEmpty())
                resultIntent.putExtra(YunBaManager.MQTT_MSG, msg);
            // The stack builder object will contain an artificial back stack
            // for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out
            // of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(FragmentActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            Random r = new Random();
            mNotificationManager.notify(r.nextInt(), mBuilder.build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        kl.disableKeyguard();
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        wl.acquire();
        wl.release();
    }
}
