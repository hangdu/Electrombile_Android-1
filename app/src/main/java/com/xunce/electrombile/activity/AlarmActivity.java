package com.xunce.electrombile.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.xunce.electrombile.R;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.view.SlidingButton;


/**
 * Created by heyukun on 2015/4/3.
 * Modify by liyanbo on 2015/10/27
 */
public class AlarmActivity extends BaseActivity {
    MediaPlayer mPlayer;
    private SlidingButton mSlidingButton;
    private TextView tv_sliding;
    private TextView tv_alarm;
    private Animation operatingAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alarm);
        super.onCreate(savedInstanceState);
        alarm();
    }

    private void alarm() {
        VibratorUtil.Vibrate(this, new long[]{1000, 2000, 2000, 1000}, true);
        //播放警铃
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    @Override
    public void initViews() {
        mSlidingButton = (SlidingButton) this.findViewById(R.id.mainview_answer_1_button);
        tv_sliding = (TextView) findViewById(R.id.tv_sliding);
        tv_alarm = (TextView) findViewById(R.id.tv_alarm);
    }

    @Override
    public void initEvents() {
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        tv_alarm.startAnimation(operatingAnim);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mPlayer.stop();
        VibratorUtil.VibrateCancle(AlarmActivity.this);
        AlarmActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSlidingButton.handleActivityEvent(event)) {
            //stop alarm
            tv_sliding.setText("停止告警！");
            tv_alarm.clearAnimation();
            VibratorUtil.VibrateCancle(AlarmActivity.this);
            mPlayer.stop();
            AlarmActivity.this.finish();
            FragmentActivity.pushService.sendMessage1(mCenter.cmdFenceOff());

        } else {
            tv_sliding.setText("滑动关闭报警");
            tv_alarm.startAnimation(operatingAnim);
        }
        return super.onTouchEvent(event);
    }
}
